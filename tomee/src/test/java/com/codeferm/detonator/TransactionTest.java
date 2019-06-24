/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.lmax.disruptor.TimeoutException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
     * Orders DAO.
     */
    @Inject
    private Dao<OrdersKey, Orders> ordersDao;
    /**
     * Business object.
     */
    @Inject
    private OrdersBoBean ordersBo;
    /**
     * EJB container.
     */
    private static EJBContainer ejbContainer;
    /**
     * EJB context.
     */
    private static Context context;

    /**
     * Create test database.
     *
     * @param ds Datasource used to create database.
     * @param fileName SQL script to create database.
     * @param delimiter Line delimiter.
     * @param removeDelimiter True to remove delimiter from statement
     */
    public static void createDb(final DataSource ds, final String fileName, final String delimiter, boolean removeDelimiter) {
        logger.debug("Create test database with {}", fileName);
        final var dataLoader = new DataLoader(ds);
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
     * Initialize database and EjbContainer one time.
     */
    @BeforeAll
    public static void beforeAll() {
        // Get database properties from dto project
        properties = loadProperties("../dto/src/test/resources/database.properties");
        // Merge app properties
        properties.putAll(loadProperties("app.properties"));
        // Create DBCP DataSource
        final var ds = new BasicDataSource();
        ds.setDriverClassName(properties.getProperty("db.driver"));
        ds.setUsername(properties.getProperty("db.user"));
        ds.setPassword(properties.getProperty("db.password"));
        ds.setUrl(properties.getProperty("db.url"));
        ds.setMaxTotal(Integer.parseInt(properties.getProperty("db.pool.size")));
        // Create database?
        if (Boolean.parseBoolean(properties.getProperty("db.create"))) {
            createDb(ds, properties.getProperty("db.sample"), properties.getProperty("db.delimiter"), Boolean.parseBoolean(
                    properties.getProperty("db.remove.delimiter")));
        }
        // Shutdown DBCP pool
        try {
            ((BasicDataSource) ds).close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Use log4j2 for logging
        System.setProperty("openejb.logger.external", "true");
        System.setProperty("openejb.log.factory", "log4j2");
        final Properties p = new Properties();
        // XADataSource
        p.put("dataSourceXa", String.format("new://Resource?type=XADataSource&class-name=%s", properties.getProperty(
                "db.xa.driver")));
        p.put("dataSourceXa.url", properties.getProperty("db.xa.url"));
        p.put("dataSourceXa.user", properties.getProperty("db.xa.user"));
        p.put("dataSourceXa.password", properties.getProperty("db.xa.password"));
        p.put("dataSourceXa.SkipImplicitAttributes", "true");
        // Otherwise goes to connection properties        
        p.put("dataSourceXa.SkipPropertiesFallback", "true");
        // DataSource
        p.put("dataSource", "new://Resource?type=DataSource");
        p.put("dataSource.DataSourceCreator", "dbcp");
        p.put("dataSource.xaDataSource", "dataSourceXa");
        p.put("dataSource.userName", properties.getProperty("db.user"));
        p.put("dataSource.password", properties.getProperty("db.password"));
        p.put("dataSource.jtaManaged", true);
        p.put("dataSource.maxActive", 11);
        p.put("dataSource.maxIdle", 5);
        ejbContainer = EJBContainer.createEJBContainer(p);
        context = ejbContainer.getContext();
    }

    /**
     * Inject stuff for each test.
     */
    @BeforeEach
    public void beforeEach() {
        logger.debug("EJBContainer context bind");
        try {
            context.bind("inject", this);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uninject stuff for each test.
     */
    @AfterEach
    public void afterEach() {
        logger.debug("EJBContainer context unbind");
        try {
            ejbContainer.getContext().unbind("inject");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close EJB container.
     */
    @AfterAll
    public static void afterAll() {
        logger.debug("Closing EJBContainer");
        ejbContainer.close();
    }

    /**
     * Test JTA commit.
     */
    @Test
    public void commit() {
        logger.debug("commit");
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
        // Wait for Disruptor threads to finish
        logger.debug("Waiting for Disruptor to finish");
        try {
            ordersBo.getOrdersBo().getDisruptor().shutdown(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Disruptor shutdown timeout", e);
        }
    }

    /**
     * Test JTA rollback.
     */
    @Test
    public void rollback() {
        logger.debug("rollback");
        // Record doesn't exist and should rollback
        Assertions.assertThrows(RuntimeException.class, () -> {
            ordersBo.updateStatus(0, "Shipped");
        });
    }

    /**
     * Test linking tables.
     */
    @Test
    public void linkTables() {
        logger.debug("linkTables");
        ordersBo.orderInfo(1);
    }

}
