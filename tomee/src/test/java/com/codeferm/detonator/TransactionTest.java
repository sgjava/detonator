/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private OrdersBo ordersBo;
    /**
     * EJB container.
     */
    EJBContainer ejbContainer;

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
     * Initialize database one time.
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
    }

    /**
     * Fire up EJB container.
     */
    @BeforeEach
    void beforeEach() {
        logger.debug("Setting up dataSource");
        final Properties p = new Properties();
        // XADataSource
        p.put("dataSourceXa", String.format("new://Resource?type=javax.sql.XADataSource&class-name=%s", properties.getProperty(
                "db.xa.driver")));
        p.put("dataSourceXa.DatabaseName", "test");
        // DataSource
        p.put("dataSource", "new://Resource?type=DataSource");
        p.put("dataSource.DataSourceCreator", "dbcp");
        p.put("dataSource.XaDataSource", "dataSourceXa");        
        p.put("dataSource.JdbcDriver", properties.getProperty("db.xa.driver"));
        p.put("dataSource.JdbcUrl", properties.getProperty("db.xa.url"));
        p.put("dataSource.userName", properties.getProperty("db.xa.user"));
        p.put("dataSource.password", properties.getProperty("db.xa.password"));
        p.put("dataSource.jtaManaged", true);
        p.put("dataSource.maxActive", 10);
        p.put("dataSource.maxIdle", 5);
        // Transaction manager
        //p.put("transactionManager", "new://TransactionManager?type=TransactionManager");
        ejbContainer = EJBContainer.createEJBContainer(p);
        final Context context = ejbContainer.getContext();
        try {
            context.bind("inject", this);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close EJB container.
     */
    @AfterEach
    void after() {
        try {
            ejbContainer.getContext().unbind("inject");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        ejbContainer.close();
    }

    /**
     * Test JTA commit.
     */
    @Test
    public void commit() {
        logger.debug("commit");
        final var key = ordersBo.createOrder(1, 1);
        // Verify record was commited
        var dto = ordersDao.find(key);
        assertNotNull(dto);
        // Status should be pending
        assertEquals("Pending", dto.getStatus());
        // Update status
        ordersBo.updateStatus(key.getOrderId(), "Shipped");
        dto = ordersDao.find(key);
        // Verify status update was commited
        assertNotNull(dto);
        assertEquals("Shipped", dto.getStatus());
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
