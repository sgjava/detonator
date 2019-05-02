/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersId;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
 * Test DB DAO.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class GenDbDaoTest {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(GenDbDaoTest.class);
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
        try (final var stream = GenDbDaoTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
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
     * Test DAO findAll method.
     */
    @Test
    public void findAll() {
        logger.debug("findAll");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<Orders, OrdersId> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Get all records
        final var list = dao.findAll();
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(list.size(), 105);
    }

    /**
     * Test DAO findById method.
     */
    @Test
    public void findById() {
        logger.debug("findById");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<Orders, OrdersId> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Create ID to find
        final var id = new OrdersId(1);
        final var dto = dao.findById(id);
        // Verify record exists
        assertNotNull(dto);
        // Verify ID matches
        assertEquals(dto.getOrderId(), 1);
        // Create ID that doesn't exist
        final var badId = new OrdersId(0);
        final var badDto = dao.findById(badId);
        // DTO should be null if not found
        assertNull(badDto);
    }

    /**
     * Test DAO save method.
     */
    @Test
    public void save() {
        logger.debug("save");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<Orders, OrdersId> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(BigDecimal.valueOf(1));
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(BigDecimal.valueOf(1));
        dto.setStatus("Pending");
        // Save DTO
        dao.save(dto);
        // Create ID to find (should be 107 based on last orderId)
        final var id = new OrdersId(107);
        final var findDto = dao.findById(id);
        // Verify ID matches
        assertEquals(findDto.getOrderId(), 107);
        // Delete saved record
        dao.delete(id);
    }

    /**
     * Test DAO save method.
     */
    @Test
    public void saveBatch() {
        logger.debug("saveBatch");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Add named query for validation
        sql.put("findByIdGreaterThan",
                "select CUSTOMER_ID, ORDER_DATE, ORDER_ID, SALESMAN_ID, STATUS from ORDERS where ORDER_ID > ?");
        // Add named query record clean up
        sql.put("deleteByIdGreaterThan", "delete from ORDERS where ORDER_ID > ?");
        // Create generic DAO
        final Dao<Orders, OrdersId> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(BigDecimal.valueOf(1));
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(BigDecimal.valueOf(1));
        dto.setStatus("Pending");
        List<Orders> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(dto);
        }
        // Save List of DTOs
        dao.save(list);
        // Select new records (ORDER_ID > 106)
        final var newRecs = dao.findBy("findByIdGreaterThan", new Object[]{105});
        // List should not be empty
        assertFalse(newRecs.isEmpty());
        // Verify exact count
        assertEquals(newRecs.size(), 10);
        // Delete records (ORDER_ID > 106)
        dao.deleteBy("deleteByIdGreaterThan", new Object[]{105});
    }
    
    /**
     * Test DAO save method.
     */
    @Test
    public void saveReturnId() {
        logger.debug("saveReturnId");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<Orders, OrdersId> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(BigDecimal.valueOf(1));
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(BigDecimal.valueOf(1));
        dto.setStatus("Pending");
        // Save DTO and return identity key
        final var id = dao.saveReturnId(dto);
        logger.debug("{}", id);
        // Verify returned key
        assertEquals(id.getOrderId(), 106);
        // Delete saved record
        dao.delete(id);
    }
    
    /**
     * Test DAO update method.
     */
    @Test
    public void update() {
        logger.debug("update");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<Orders, OrdersId> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Create ID to find
        final var id = new OrdersId(1);
        final var dto = dao.findById(id);
        dto.setStatus("Shipped");
        // Uopdate record
        dao.update(dto, id);
        // Verify update
        final var updateDto = dao.findById(id);
        // Verify status matches
        assertEquals(updateDto.getStatus(), "Shipped");
        // Set status back
        dto.setStatus("Pending");
        dao.update(dto, id);
    }

    /**
     * Test DAO update method.
     */
    @Test
    public void delete() {
        logger.debug("delete");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<Orders, OrdersId> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Create ID to delete
        final var id = new OrdersId(1);
        // Save record
        final var saveDto = dao.findById(id);
        // Delete record
        dao.delete(id);
        final var dto = dao.findById(id);
        // Verify record was deleted
        assertNull(dto);
        // Save record back
        dao.save(saveDto);
    }
}
