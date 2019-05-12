/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
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
     * Load properties file from class path.
     *
     * @param propertyFile Name of property file.
     * @return Properties.
     */
    public static Properties loadProperties(final String propertyFile) {
        Properties props = new Properties();
        // Get properties from classpath
        try (final var stream = TransactionTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
            props.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Property file exception", e);
        }
        return props;
    }

    /**
     * Set up DataSource and initialize database.
     */
    @BeforeAll
    public static void beforeAll() {
        properties = new Properties();
        // Get properties from classpath
        properties = loadProperties("app.properties");
        // Create AtomikosXADataSourceBean
        final AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName("BaseAtomikosTest");
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

    /**
     * Test DAO findAll method.
     */
    @Test
    @Transaction
    public void commit() {
        logger.debug("commit");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create transactional business object
        OrdersBo bo = TransactionFactory.createObject(OrdersBo.class, AtomikosTransModule.class);
        bo.setDao(dao);
        final var key = bo.createOrder(1, 1);
    }
}
