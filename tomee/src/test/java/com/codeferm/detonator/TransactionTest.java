/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
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
        final var dataLoader = new DataLoader(ds);
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
     * Initialize database one time.
     */
    @BeforeAll
    public static void beforeAll() {
        properties = new Properties();
        // Get properties from classpath
        properties = loadProperties("app.properties");
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
        final Properties p = new Properties();
        p.put("dataSource", "new://Resource?type=DataSource");
        p.put("dataSource.JdbcDriver", properties.getProperty("db.driver"));
        p.put("dataSource.JdbcUrl", properties.getProperty("db.url"));
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
