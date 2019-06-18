/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.Products;
import com.codeferm.dto.ProductsKey;
import java.sql.SQLException;
import java.util.Properties;
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
        properties.putAll(common.loadProperties("app.properties"));        // Create DBCP DataSource
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
        // Create BO and set DAOs
        final var ordersBo = new OrdersBo();
        ordersBo.setOrders(orders);
        ordersBo.setOrderItems(orderItems);
        ordersBo.setProducts(products);
        return ordersBo;
    }

    /**
     * Test createOrder method.
     */
    @Test
    public void createOrder() {
        logger.debug("createOrder");
        final var ordersBo = createBo();
        final var key = ordersBo.createOrder(1, 1);
        // Verify record was commited
        var dto = ordersBo.getOrders().find(key);
        assertNotNull(dto);
        // Status should be pending
        assertEquals("Pending", dto.getStatus());
        // Update status
        ordersBo.updateStatus(key.getOrderId(), "Shipped");
        dto = ordersBo.getOrders().find(key);
        // Verify status update was commited
        assertNotNull(dto);
        assertEquals("Shipped", dto.getStatus());
        
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
