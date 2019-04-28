/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test MetadataExtract.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DbDaoTest {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(DbDaoTest.class);

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
        try (final var stream = DbDaoTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
            props.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Property file exception", e);
        }
        return props;
    }

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
     * Shut down datasource.
     *
     * @throws SQLException Possible exception.
     */
    @AfterAll
    public static void afterAll() throws SQLException {
        ((BasicDataSource) dataSource).close();
    }

    /**
     * Test DbDao select method.
     */
    @Test
    void select() {
        logger.debug("select");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        final var dbDao = new DbUtilsDsDao(dataSource);
        final Orders dto = dbDao.select(sql.getProperty("findById"), new Object[]{1}, Orders.class);
        // Verify record exists
        assertNotNull(dto);
        // Verify ID matches
        assertEquals(dto.getOrderId(), 1);
        // Select record that doesn't exist
        final Orders badDto = dbDao.select(sql.getProperty("findById"), new Object[]{0}, Orders.class);
        // DTO should be null if not found
        assertNull(badDto);
    }
    
    /**
     * Test DbDao selectList method.
     */
    @Test
    void selectList() {
        logger.debug("selectList");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        final var dbDao = new DbUtilsDsDao(dataSource);
        final var list = dbDao.selectList(sql.getProperty("findAll"), Orders.class);
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(list.size(), 105);
    }

    /**
     * Test DbDao selectList method with params.
     */
    @Test
    void selectListParams() {
        logger.debug("selectListParams");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Add custom SQL
        sql.put("findBySalesman",
                "select CUSTOMER_ID, ORDER_DATE, ORDER_ID, SALESMAN_ID, STATUS from ORDERS where SALESMAN_ID = ?");
        final var dbDao = new DbUtilsDsDao(dataSource);
        final var list = dbDao.selectList(sql.getProperty("findBySalesman"), new Object[]{62}, Orders.class);
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(list.size(), 13);
    }
    
    /**
     * Test DbDao select method returning Map.
     */
    @Test
    void selectMap() {
        logger.debug("selectMap");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        final var dbDao = new DbUtilsDsDao(dataSource);
        final var map = dbDao.select(sql.getProperty("findAll"));
        // Verify record exists
        assertNotNull(map);
        // Verify size is 5fields
        assertEquals(map.size(), 5);
    }

    /**
     * Test DbDao select method with params returning Map.
     */
    @Test
    void selectMapParams() {
        logger.debug("selectMapParams");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        final var dbDao = new DbUtilsDsDao(dataSource);
        final var map = dbDao.select(sql.getProperty("findById"), new Object[]{1});
        // Verify record exists
        assertNotNull(map);
        // Verify size is 5fields
        assertEquals(map.size(), 5);
    }
    
    /**
     * Test DbDao selectList method without params.
     */
    @Test
    void selectListMap() {
        logger.debug("selectListMap");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        final var dbDao = new DbUtilsDsDao(dataSource);
        final var list = dbDao.selectList(sql.getProperty("findAll"));
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(list.size(), 105);
    }

    /**
     * Test DbDao selectList method with params.
     */
    @Test
    void selectListMapParams() {
        logger.debug("selectListMapParams");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Add custom SQL
        sql.put("findBySalesman",
                "select CUSTOMER_ID, ORDER_DATE, ORDER_ID, SALESMAN_ID, STATUS from ORDERS where SALESMAN_ID = ?");
        final var dbDao = new DbUtilsDsDao(dataSource);
        final var list = dbDao.selectList(sql.getProperty("findBySalesman"), new Object[]{62});
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(list.size(), 13);
    }
    
    /**
     * Test DbDao update method to insert record.
     */
    @Test
    void insert() {
        logger.debug("insert");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        final var dbDao = new DbUtilsDsDao(dataSource);
        // Insert new record (note null orderId is passed since it's an identity field and will be auto generated)
        var recs = dbDao.update(sql.getProperty("save"), new Object[]{BigDecimal.valueOf(1), Date.valueOf(LocalDate.now()), null,
            BigDecimal.valueOf(1), "Pending"});
        // Verify 1 record affected
        assertEquals(recs, 1);
        // New record order_id should be 106
        final Orders dto = dbDao.select(sql.getProperty("findById"), new Object[]{106}, Orders.class);
        // Verify ID matches
        assertEquals(dto.getOrderId(), 106);
        // Delete inserted record
        recs = dbDao.update(sql.getProperty("delete"), new Object[]{106});
        // Verify 1 record affected
        assertEquals(recs, 1);
    }

    /**
     * Test DbDao update method to update record.
     */
    @Test
    void update() {
        logger.debug("update");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        final var dbDao = new DbUtilsDsDao(dataSource);
        // Insert new record (note null orderId is passed since it's an identity field and will be auto generated)
        var recs = dbDao.update(sql.getProperty("update"), new Object[]{BigDecimal.valueOf(1), Date.valueOf(LocalDate.now()), 1,
            BigDecimal.valueOf(1), "Shipped", 1});
        // Verify 1 record affected
        assertEquals(recs, 1);
        // Get updated record
        final Orders dto = dbDao.select(sql.getProperty("findById"), new Object[]{1}, Orders.class);
        // Verify status matches
        assertEquals(dto.getStatus(), "Shipped");
    }

    /**
     * Test DbDao update method to delete record.
     */
    @Test
    void delete() {
        logger.debug("delete");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        final var dbDao = new DbUtilsDsDao(dataSource);
        // Get updated record
        final Orders saveDto = dbDao.select(sql.getProperty("findById"), new Object[]{1}, Orders.class);
        // Delete inserted record
        var recs = dbDao.update(sql.getProperty("delete"), new Object[]{1});
        // Verify 1 record affected
        assertEquals(recs, 1);
        // Insert record back
        recs = dbDao.update(sql.getProperty("save"), new Object[]{saveDto.getCustomerId(), saveDto.getOrderDate(), saveDto.
            getOrderId(), saveDto.getSalesmanId(), saveDto.getStatus()});
        // Verify 1 record affected
        assertEquals(recs, 1);
    }
}
