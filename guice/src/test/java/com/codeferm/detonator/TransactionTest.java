/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;
import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.Products;
import com.codeferm.dto.ProductsKey;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test transactions.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class TransactionTest {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(TransactionTest.class);
    /**
     * Test properties.
     */
    private static Properties properties;
    /**
     * DataSource.
     */
    private static DataSource dataSource;
    /**
     * Common test methods.
     */
    private static Common common;

    /**
     * Create test database.
     *
     * @param fileName SQL script to create database.
     * @param delimiter Line delimiter.
     * @param removeDelimiter True to remove delimiter from statement
     */
    public static void createDb(final String fileName, final String delimiter, boolean removeDelimiter) {
        final var dataLoader = new DataLoader(dataSource);
        dataLoader.execScript(fileName, delimiter, removeDelimiter);
    }

    /**
     * Load properties file from file path or fail back to class path.
     *
     * @param propertyFile Name of property file.
     * @return Properties.
     */
    public static Properties loadProperties(final String propertyFile) {
        Properties props = new Properties();
        try {
            // Get properties from file
            props.load(new FileInputStream(propertyFile));
            logger.debug("Properties loaded from file {}", propertyFile);
        } catch (IOException e1) {
            logger.warn("Properties file not found {}", propertyFile);
            // Get properties from classpath
            try (final var stream = TransactionTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
                props.load(stream);
                logger.debug("Properties loaded from class path {}", propertyFile);
            } catch (IOException e2) {
                throw new RuntimeException("No properties found", e2);
            }
        }
        return props;
    }

    /**
     * Set up DataSource and initialize database.
     */
    @BeforeAll
    public static void beforeAll() {
        common = new Common();
        // Get database properties from dto project
        properties = loadProperties("../dto/src/test/resources/database.properties");
        // Merge app properties
        properties.putAll(loadProperties("app.properties"));
        // Create AtomikosXADataSourceBean
        final AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName("TransactionTest");
        ds.setXaDataSourceClassName(properties.getProperty("db.xa.driver"));
        Properties p = new Properties();
        p.setProperty("user", properties.getProperty("db.xa.user"));
        p.setProperty("password", properties.getProperty("db.xa.password"));
        p.setProperty("URL", properties.getProperty("db.xa.url"));
        ds.setXaProperties(p);
        ds.setPoolSize(Integer.parseInt(properties.getProperty("db.xa.pool.size")));
        dataSource = ds;
        // Create database?
        if (Boolean.parseBoolean(properties.getProperty("db.create"))) {
            createDb(properties.getProperty("db.sample"), properties.getProperty("db.delimiter"), Boolean.parseBoolean(properties.
                    getProperty("db.remove.delimiter")));
        }
    }

    /**
     * Shut down DataSource.
     *
     * @throws SQLException Possible exception.
     */
    @AfterAll
    public static void afterAll() throws SQLException {
        ((AtomikosDataSourceBean) dataSource).close();
    }

    public OrdersBo createBo() {
        // Create generic DAOs
        final Dao<OrdersKey, Orders> orders
                = new GenDbDao<>(dataSource, common.loadProperties("orders.properties"), OrdersKey.class, Orders.class);
        final Dao<OrderItemsKey, OrderItems> orderItems = new GenDbDao<>(dataSource, common.loadProperties("orderitems.properties"),
                OrderItemsKey.class, OrderItems.class);
        final Dao<ProductsKey, Products> products = new GenDbDao<>(dataSource, common.loadProperties("products.properties"),
                ProductsKey.class, Products.class);
        final Dao<InventoriesKey, Inventories> inventories = new GenDbDao<>(dataSource, common.loadProperties(
                "inventories.properties"),
                InventoriesKey.class, Inventories.class);
        // Create BO and set DAOs
        final var queue = TransactionFactory.createObject(CreateOrderQueueBean.class, AtomikosTransModule.class);
        queue.setOrders(orders);
        queue.setOrderItems(orderItems);
        queue.setProducts(products);
        queue.setInventories(inventories);
        queue.addObserver(new OrderCreatedBean(Integer.parseInt(properties.getProperty("db.pool.size")) - 1));
        final var ordersBo = new OrdersBo(queue);
        ordersBo.setOrders(orders);
        ordersBo.setOrderItems(orderItems);
        ordersBo.setProducts(products);
        ordersBo.setInventories(inventories);
        return ordersBo;
    }
    
    /**
     * Max out inventory for all products.
     *
     * @param value Quantity of each inventory record.
     */
    public void updateInventory(final int value) {
        final Dao<InventoriesKey, Inventories> inventories = new GenDbDao<>(dataSource, common.loadProperties(
                "inventories.properties"), InventoriesKey.class, Inventories.class);
        final var list = inventories.findAll();
        // Max out inventory
        for (final Inventories inv : list) {
            inv.setQuantity(value);
            inventories.update(inv.getKey(), inv);
        }
    }    

    /**
     * Test JTA commit.
     */
    @Test
    public void commit() {
        logger.debug("commit");
        final var maxOrders = Integer.parseInt(properties.getProperty("orders.max.create"));
        updateInventory(maxOrders);
        final List<OrderItems> list = new ArrayList<>();
        final OrderItems item1 = new OrderItems();
        item1.setItemId(1L);
        item1.setProductId(3L);
        item1.setQuantity(1);
        list.add(item1);
        final OrderItems item2 = new OrderItems();
        item2.setItemId(2L);
        item2.setProductId(4L);
        item2.setQuantity(1);
        list.add(item2);
        // Create transactional business object
        OrdersBoBean ordersBo = TransactionFactory.createObject(OrdersBoBean.class, AtomikosTransModule.class);
        ordersBo.setOrdersBo(createBo());
        // Database pool size - 1 threads
        final var executor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("db.pool.size")) - 1);
        final var start = System.nanoTime();
        for (int i = 0; i < maxOrders; i++) {
            final Runnable task = () -> {
                ordersBo.createOrder(1, 1, list);
            };
            executor.execute(task);
        }
        // Shutdow executor service
        executor.shutdown();
        // Wait for BO client threads to finish
        logger.debug("Waiting for BO client threads to finish");
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            logger.debug("BO client threads finished");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Wait for create order threads to finish
        logger.debug("Waiting for create order thread to finish");
        ordersBo.getOrdersBo().getOrderQueue().shutdown();
        final var stop = System.nanoTime();
        logger.debug("TPS: {}", maxOrders / ((stop - start) / 1000000000L));
        logger.debug("Create order thread finished");
    }

    /**
     * Test JTA rollback.
     */
    @Test
    public void rollback() {
        logger.debug("rollback");
        // Create transactional business object
        OrdersBoBean bo = TransactionFactory.createObject(OrdersBoBean.class, AtomikosTransModule.class);
        bo.setOrdersBo(createBo());
        // Record doesn't exist and should rollback
        Assertions.assertThrows(RuntimeException.class, () -> {
            bo.updateStatus(0, "Shipped");
        });
    }

    /**
     * Test linking tables.
     */
    @Test
    public void linkTables() {
        logger.debug("linkTables");
        // Create transactional business object
        OrdersBoBean bo = TransactionFactory.createObject(OrdersBoBean.class, AtomikosTransModule.class);
        bo.setOrdersBo(createBo());
        bo.orderInfo(1);
    }
}
