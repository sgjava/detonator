/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
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
     * Common test methods.
     */
    private static Common common;

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
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        final Orders dto = dbDao.select(sql.getProperty("find"), new Object[]{1}, Orders.class);
        // Verify record exists
        assertNotNull(dto);
        // Verify ID matches
        assertEquals(1, dto.getOrderId());
        // Select record that doesn't exist
        final Orders badDto = dbDao.select(sql.getProperty("find"), new Object[]{0}, Orders.class);
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
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        final var list = dbDao.selectList(sql.getProperty("findAll"), Orders.class);
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(105, list.size());
    }

    /**
     * Test DbDao selectList method with params.
     */
    @Test
    void selectListParams() {
        logger.debug("selectListParams");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        // Add custom SQL
        sql.put("findBySalesman",
                "select CUSTOMER_ID, ORDER_DATE, ORDER_ID, SALESMAN_ID, STATUS from ORDERS where SALESMAN_ID = ?");
        final var dbDao = new DbUtilsDs(dataSource);
        final var list = dbDao.selectList(sql.getProperty("findBySalesman"), new Object[]{62}, Orders.class);
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(13, list.size());
    }

    /**
     * Test DbDao select method returning Map.
     */
    @Test
    void selectMap() {
        logger.debug("selectMap");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        final var map = dbDao.select(sql.getProperty("findAll"));
        // Verify record exists
        assertNotNull(map);
        // Verify size is 5fields
        assertEquals(5, map.size());
    }

    /**
     * Test DbDao select method with params returning Map.
     */
    @Test
    void selectMapParams() {
        logger.debug("selectMapParams");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        final var map = dbDao.select(sql.getProperty("find"), new Object[]{1});
        // Verify record exists
        assertNotNull(map);
        // Verify size is 5fields
        assertEquals(5, map.size());
    }

    /**
     * Test DbDao selectList method without params.
     */
    @Test
    void selectListMap() {
        logger.debug("selectListMap");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        final var list = dbDao.selectList(sql.getProperty("findAll"));
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(105, list.size());
    }

    /**
     * Test DbDao selectList method with params.
     */
    @Test
    void selectListMapParams() {
        logger.debug("selectListMapParams");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        // Add custom SQL
        sql.put("findBySalesman",
                "select CUSTOMER_ID, ORDER_DATE, ORDER_ID, SALESMAN_ID, STATUS from ORDERS where SALESMAN_ID = ?");
        final var dbDao = new DbUtilsDs(dataSource);
        final var list = dbDao.selectList(sql.getProperty("findBySalesman"), new Object[]{62});
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(13, list.size());
    }

    /**
     * Test DbDao select method return one field without params.
     */
    @Test
    void selectField() {
        logger.debug("selectField");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        final var field = dbDao.select(sql.getProperty("findAll"), "ORDER_ID");
        assertNotNull(field);
    }

    /**
     * Test DbDao select method return one field with params.
     */
    @Test
    void selectFieldParams() {
        logger.debug("selectFieldParams");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        final var field = dbDao.select(sql.getProperty("find"), new Object[]{1}, "ORDER_ID");
        // Here we deal with Oracle NUMBER because it will return as BigDecimal
        if (field instanceof BigDecimal) {
            assertEquals(BigDecimal.valueOf(1), field);
        } else {
            assertEquals(1L, field);
        }
    }

    /**
     * Test DbDao update method to insert record.
     */
    @Test
    void insert() {
        logger.debug("insert");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        // Insert new record (note null orderId is passed since it's an identity field and will be auto generated)
        final var key = dbDao.updateReturnKey(sql.getProperty("save"),
                new Object[]{1, Date.valueOf(LocalDate.now()), null, 1, "Pending"}, "ORDER_ID");
        // Make sure we got a key back
        assertNotNull(key);
        // New record order_id
        final Orders dto = dbDao.select(sql.getProperty("find"), new Object[]{key}, Orders.class);
        // Verify ID matches
        assertEquals(key, dto.getOrderId());
        // Delete inserted record
        var recs = dbDao.update(sql.getProperty("delete"), new Object[]{key});
        // Verify 1 record affected
        assertEquals(1, recs);
    }

    /**
     * Test DbDao update method to update record.
     */
    @Test
    void update() {
        logger.debug("update");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        // Insert new record (note null orderId is passed since it's an identity field and will be auto generated)
        var recs = dbDao.update(sql.getProperty("update"), new Object[]{BigDecimal.valueOf(1), Date.valueOf(LocalDate.now()), 1,
            BigDecimal.valueOf(1), "Shipped", 1});
        // Verify 1 record affected
        assertEquals(1, recs);
        // Get updated record
        final Orders dto = dbDao.select(sql.getProperty("find"), new Object[]{1}, Orders.class);
        // Verify status matches
        assertEquals("Shipped", dto.getStatus());
    }

    /**
     * Test DbDao update method to delete record.
     */
    @Test
    void delete() {
        logger.debug("delete");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        final var dbDao = new DbUtilsDs(dataSource);
        // Get updated record
        final Orders saveDto = dbDao.select(sql.getProperty("find"), new Object[]{1}, Orders.class);
        // Delete inserted record
        var recs = dbDao.update(sql.getProperty("delete"), new Object[]{1});
        // Verify 1 record affected
        assertEquals(1, recs);
        // Insert record back
        recs = dbDao.update(sql.getProperty("save"), new Object[]{saveDto.getCustomerId(), saveDto.getOrderDate(), saveDto.
            getOrderId(), saveDto.getSalesmanId(), saveDto.getStatus()});
        // Verify 1 record affected
        assertEquals(1, recs);
    }
}
