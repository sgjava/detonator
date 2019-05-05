/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersId;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
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
public class GenMapDbDaoTest {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(GenMapDbDaoTest.class);
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
     * Create test SQL database and populate MapDB.
     *
     * @param fileName SQL script to create database.
     * @param delimiter Line delimiter.
     * @param removeDelimiter True to remove delimiter from statement
     */
    public static void createDb(final String fileName, final String delimiter, boolean removeDelimiter) {
        final var dataLoader = new DataLoader(dataSource);
        dataLoader.execScript(fileName, delimiter, removeDelimiter);
        db = DBMaker.fileDB(properties.getProperty("map.file")).make();
        final ConcurrentMap map = db.hashMap("Orders").createOrOpen();
        final var sql = loadProperties("orders.properties");
        // Create generic RDBMS DAO
        final Dao<OrdersId, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Get all records
        final var list = dao.findAll();
        for (final var orders : list) {
            map.put(new OrdersId(orders.getOrderId()), orders);
        }
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
        try (final var stream = GenMapDbDaoTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
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
        ((BasicDataSource) dataSource).close();
        db.close();
    }

    /**
     * Test DAO save method.
     */
    @Test
    public void save() {
        logger.debug("save");
    }
}
