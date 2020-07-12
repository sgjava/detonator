/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.arjuna.ats.jta.TransactionManager;
import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;
import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.Products;
import com.codeferm.dto.ProductsKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test Guice transactions.
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
     * Max out inventory for all products.
     *
     * @param ds DataSource.
     * @param value Quantity of each inventory record.
     */
    public static void updateInventory(final DataSource ds, final int value) {
        final Dao<InventoriesKey, Inventories> inventories = new GenDbDao<>(ds, common.loadProperties("inventories.properties"),
                InventoriesKey.class, Inventories.class);
        final var list = inventories.findAll();
        // Max out inventory
        for (final Inventories inv : list) {
            inv.setQuantity(value);
            inventories.update(inv.getKey(), inv);
        }
    }

    /**
     * Set up DataSource and initialize database.
     */
    @BeforeAll
    public static void beforeAll() {
        common = new Common();
        // Get database properties from dto project
        properties = common.loadProperties("../dto/src/test/resources/database.properties");
        // Merge app properties
        properties.putAll(common.loadProperties("app.properties"));
        // Create DBCP DataSource
        final var ds = new BasicDataSource();
        ds.setDriverClassName(properties.getProperty("db.driver"));
        ds.setUsername(properties.getProperty("db.user"));
        ds.setPassword(properties.getProperty("db.password"));
        ds.setUrl(properties.getProperty("db.url"));
        ds.setMaxTotal(Integer.parseInt(properties.getProperty("db.pool.size")));
        // Create database?
        if (Boolean.parseBoolean(properties.getProperty("db.create"))) {
            common.createDb(ds, properties.getProperty("db.sample"), properties.getProperty("db.delimiter"), Boolean.parseBoolean(
                    properties.getProperty("db.remove.delimiter")));
        }
        updateInventory(ds, Integer.parseInt(properties.getProperty("orders.max.create")));
        // Shutdown DBCP pool
        try {
            ((BasicDataSource) ds).close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.getProperties().put("com.arjuna.ats.arjuna.objectstore.objectStoreDir", properties.getProperty(
                "narayana.object.store.dir"));
        System.getProperties().put("ObjectStoreEnvironmentBean.objectStoreDir", properties.getProperty("narayana.object.store.dir"));
        // Create BasicManagedDataSource
        final var xaDs = new BasicManagedDataSource();
        xaDs.setTransactionManager(TransactionManager.transactionManager());
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL(properties.getProperty("db.xa.url"));
        xaDs.setXaDataSourceInstance(jdbcDataSource);
        xaDs.setDriverClassName(properties.getProperty("db.xa.driver"));
        xaDs.setUrl(properties.getProperty("db.xa.url"));
        xaDs.setUsername(properties.getProperty("db.xa.user"));
        xaDs.setPassword(properties.getProperty("db.xa.password"));
        xaDs.setInitialSize(Integer.parseInt(properties.getProperty("db.xa.pool.size")));
        dataSource = xaDs;
        try {
            // Delete files
            if (Files.exists(Paths.get(properties.getProperty("output.dir")))) {
                Arrays.stream(new File(properties.getProperty("output.dir")).listFiles()).forEach(File::delete);
            }
            // Create dir
            Files.createDirectories(Paths.get(properties.getProperty("output.dir")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shut down DataSource.
     *
     * @throws SQLException Possible exception.
     */
    @AfterAll
    public static void afterAll() throws SQLException {
        ((BasicManagedDataSource) dataSource).close();
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
        // Queue bean uses Guice transactions 
        final var queue = TransactionFactory.createObject(CreateOrderQueueBean.class, TransactionModule.class);
        queue.setCreateOrder(new CreateOrder(new UpdateInventoryDao(orderItems, inventories), orders, orderItems, products));
        // Create BO
        return new OrdersBo(queue, orders, orderItems, products, inventories);
    }

    /**
     * Test JTA commit.
     */
    @Test
    public void commit() {
        logger.debug("commit");
        final var maxOrders = Integer.parseInt(properties.getProperty("orders.max.create"));
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
        OrdersBoBean bo = TransactionFactory.createObject(OrdersBoBean.class, TransactionModule.class);
        bo.setOrdersBo(createBo());
        // Add observer
        final var orderCreated = new OrderCreated(new OrderShipped(properties.getProperty("template.dir"), properties.getProperty(
                "template"), properties.getProperty("output.dir"), bo.getOrdersBo(), Integer.parseInt(properties.getProperty(
                "order.shipped.max.threads"))), Integer.parseInt(properties.getProperty("order.created.max.threads")));
        ((CreateOrderQueue) bo.getOrdersBo().getOrderQueue()).addObserver(orderCreated);
        // Database pool size - 1 threads
        final var executor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("db.xa.pool.size")) - 1);
        final var start = System.nanoTime();
        for (int i = 0; i < maxOrders; i++) {
            final Runnable task = () -> {
                bo.createOrder(1, 1, list);
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
        bo.getOrdersBo().getOrderQueue().shutdown();
        final var stop = System.nanoTime();
        logger.debug("TPS: {}", maxOrders / ((stop - start) / 1000000000L));
        logger.debug("Create order thread finished");
        logger.debug("Waiting for order created thread to finish");
        orderCreated.shutdown();
        logger.debug("Waiting for order shipped thread to finish");
        orderCreated.getOrderShipped().shutdown();
    }

    /**
     * Test JTA rollback.
     */
    @Test
    public void rollback() {
        logger.debug("rollback");
        // Create transactional business object
        OrdersBoBean bo = TransactionFactory.createObject(OrdersBoBean.class, TransactionModule.class);
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
        OrdersBoBean bo = TransactionFactory.createObject(OrdersBoBean.class, TransactionModule.class);
        bo.setOrdersBo(createBo());
        bo.orderInfo(1);
    }
}
