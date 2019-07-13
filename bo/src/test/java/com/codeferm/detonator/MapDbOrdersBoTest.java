/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;
import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.Products;
import com.codeferm.dto.ProductsKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Test MapDB DAO.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class MapDbOrdersBoTest {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(MapDbOrdersBoTest.class);
    /**
     * Test properties.
     */
    private static Properties properties;
    /**
     * DataSource.
     */
    private static DataSource dataSource;
    /**
     * MapDB database.
     */
    private static DB db;
    /**
     * Common test methods.
     */
    private static Common common;

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
        // Delete MapDB file
        try {
            Files.deleteIfExists(Paths.get(properties.getProperty("map.file")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Create MapDB
        db = DBMaker.fileDB(properties.getProperty("map.file")).make();
        // Create DBCP DataSource
        final var ds = new BasicDataSource();
        ds.setDriverClassName(properties.getProperty("db.driver"));
        ds.setUsername(properties.getProperty("db.user"));
        ds.setPassword(properties.getProperty("db.password"));
        ds.setUrl(properties.getProperty("db.url"));
        ds.setMaxTotal(Integer.parseInt(properties.getProperty("db.pool.size")));
        dataSource = ds;
        // Create database?
        if (Boolean.parseBoolean(properties.getProperty("db.create"))) {
            common.copyDbToMap(dataSource, db, properties.getProperty("db.sample"), properties.getProperty("db.delimiter"), Boolean.
                    parseBoolean(properties.getProperty("db.remove.delimiter")));
        }
    }

    /**
     * Shut down DataSource.
     *
     * @throws SQLException Possible exception.
     */
    @AfterAll
    public static void afterAll() throws SQLException {
        ((BasicDataSource) dataSource).close();
        db.commit();
        db.close();
    }

    /**
     * Create Orders BO.
     *
     * @return Orders BO.
     */
    public OrdersBo createBo() {
        // Create generic DAOs
        final Dao<OrdersKey, Orders> orders = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        final Dao<OrderItemsKey, OrderItems> orderItems = new GenMapDbDao<>(db, "orderitems", OrderItemsKey.class, OrderItems.class);
        final Dao<ProductsKey, Products> products = new GenMapDbDao<>(db, "products", ProductsKey.class, Products.class);
        final Dao<InventoriesKey, Inventories> inventories
                = new GenMapDbDao<>(db, "inventories", InventoriesKey.class, Inventories.class);
        // Create BO
        return new OrdersBo(new CreateOrderQueue(
                new CreateOrder(new UpdateInventoryDao(orderItems, inventories), orders, orderItems, products)), orders, orderItems,
                products, inventories);
    }

    /**
     * Max out inventory for all products.
     *
     * @param value Quantity of each inventory record.
     */
    public void updateInventory(final int value) {
        final Dao<InventoriesKey, Inventories> inventories
                = new GenMapDbDao<>(db, "inventories", InventoriesKey.class, Inventories.class);
        final var list = inventories.findAll();
        // Max out inventory
        for (final Inventories inv : list) {
            inv.setQuantity(value);
            inventories.update(inv.getKey(), inv);
        }
    }
    
    /**
     * Create OrderItems List.
     *
     * @return List of OrderItems.
     */
    public List<OrderItems> createOrderItemsList() {
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
        return list;
    }

    /**
     * Test createOrder method.
     */
    @Test
    public void createOrder() {
        logger.debug("createOrder");
        final var maxOrders = Integer.parseInt(properties.getProperty("orders.max.create"));
        updateInventory(maxOrders);
        final var ordersBo = createBo();
        // Add observer
        final var orderCreated = new OrderCreated(Integer.parseInt(properties.getProperty("db.pool.size")) - 1);
        ((CreateOrderQueue) ordersBo.getOrderQueue()).addObserver(orderCreated);
        final List<OrderItems> list = createOrderItemsList();
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
        ordersBo.getOrderQueue().shutdown();
        final var stop = System.nanoTime();
        logger.debug("TPS: {}", maxOrders / ((stop - start) / 1000000000L));
        logger.debug("Create order thread finished");
        logger.debug("Waiting for order created thread to finish");
        orderCreated.shutdown();

    }

    /**
     * Test linking tables.
     */
    @Test
    public void linkTables() {
        logger.debug("linkTables");
        final var ordersBo = createBo();
        ordersBo.orderInfo(1);
    }
}
