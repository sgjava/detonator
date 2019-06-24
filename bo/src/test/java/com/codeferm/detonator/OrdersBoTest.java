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
import com.lmax.disruptor.TimeoutException;
import java.math.BigDecimal;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test Order OB.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrdersBoTest {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(OrdersBoTest.class);
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
        dataSource = ds;
        // Create database?
        if (Boolean.parseBoolean(properties.getProperty("db.create"))) {
            common.createDb(dataSource, properties.getProperty("db.sample"), properties.getProperty("db.delimiter"), Boolean.
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
        final var ordersBo = new OrdersBo();
        ordersBo.setOrders(orders);
        ordersBo.setOrderItems(orderItems);
        ordersBo.setProducts(products);
        ordersBo.setInventories(inventories);
        return ordersBo;
    }

    /**
     * Test createOrder method.
     */
    @Test
    public void createOrder() {
        logger.debug("createOrder");
        final var ordersBo = createBo();
        // Database pool size - 1 threads
        final var executor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("db.pool.size")) - 1);
        //
        for (int i = 0; i < 100; i++) {
            final Runnable task = () -> {
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Wait for Disruptor threads to finish
        logger.debug("Waiting for Disruptor to finish");
        try {
            ordersBo.getDisruptor().shutdown(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Disruptor shutdown timeout", e);
        }
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
