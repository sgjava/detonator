/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersId;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
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
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

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
        final ConcurrentMap<OrdersId, Orders> map = db.hashMap("orders", Serializer.JAVA, Serializer.JAVA).createOrOpen();
        map.clear();
        final var sql = loadProperties("orders.properties");
        // Create generic RDBMS DAO
        final Dao<OrdersId, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersId.class, Orders.class);
        // Get all records
        final var list = dao.findAll();
        list.forEach((orders) -> {
            map.put(new OrdersId(orders.getOrderId()), orders);
        });
        db.commit();
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
     * Test DAO findAll method.
     */
    @Test
    public void findAll() {
        logger.debug("findAll");
        // Create generic DAO
        final Dao<OrdersId, Orders> dao = new GenMapDbDao<>(db, "orders");
        // Get all records
        final var list = dao.findAll();
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(list.size(), 101);
    }

    /**
     * Test DAO findById method.
     */
    @Test
    public void findById() {
        logger.debug("findById");
        // Create generic DAO
        final Dao<OrdersId, Orders> dao = new GenMapDbDao<>(db, "orders");
        // Create ID to find
        final var id = new OrdersId(4);
        final var dto = dao.findById(id);
        // Verify record exists
        assertNotNull(dto);
        // Verify ID matches
        assertEquals(dto.getOrderId(), 4);
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
        // Create generic DAO
        final Dao<OrdersId, Orders> dao = new GenMapDbDao<>(db, "orders");
        final var id = new OrdersId(107);
        // Create DTO to save
        final var dto = new Orders();
        dto.setOrderId(id.getOrderId());
        dto.setCustomerId(1);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1);
        dto.setStatus("Pending");
        // Save DTO
        dao.save(id, dto);
        final var findDto = dao.findById(id);
        // Verify ID matches
        assertEquals(findDto.getOrderId(), 107);
    }

    /**
     * Test DAO save method.
     */
    @Test
    public void saveBatch() {
        logger.debug("saveBatch");
        // Create generic DAO
        final Dao<OrdersId, Orders> dao = new GenMapDbDao<>(db, "orders");
        // Preserve insertion order
        final Map<OrdersId, Orders> map = new LinkedHashMap<>();
        // For RDBMS ID is ignored
        for (int i = 0; i < 10; i++) {
            // Create DTO to save
            final var dto = new Orders();
            dto.setOrderId(i + 108);
            dto.setCustomerId(1);
            dto.setOrderDate(Date.valueOf(LocalDate.now()));
            dto.setSalesmanId(1);
            dto.setStatus("Pending");
            map.put(new OrdersId(i + 108), dto);
        }
        // Save Map of DTOs
        dao.save(map);
    }

    /**
     * Test DAO update method.
     */
    @Test
    public void update() {
        logger.debug("update");
        // Create generic DAO
        final Dao<OrdersId, Orders> dao = new GenMapDbDao<>(db, "orders");
        // Create ID to find
        final var id = new OrdersId(4);
        final var dto = dao.findById(id);
        dto.setStatus("Shipped");
        // Uopdate record
        dao.update(id, dto);
        // Verify update
        final var updateDto = dao.findById(id);
        // Verify status matches
        assertEquals(updateDto.getStatus(), "Shipped");
    }

    /**
     * Test DAO batch update method.
     */
    @Test
    public void updateBatch() {
        logger.debug("updateBatch");
        // Create generic DAO
        final Dao<OrdersId, Orders> dao = new GenMapDbDao<>(db, "orders");
        // Preserve insertion order
        final Map<OrdersId, Orders> map = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            final var id = new OrdersId(i + 10);
            final var dto = dao.findById(id);
            dto.setStatus("Pending");
            map.put(id, dto);
        }
        dao.update(map);
    }

    /**
     * Test DAO delete method.
     */
    @Test
    public void delete() {
        // Get generated SQL
        // Create generic DAO
        final Dao<OrdersId, Orders> dao = new GenMapDbDao<>(db, "orders");
        // Create ID to delete
        final var id = new OrdersId(1);
        // Delete record
        dao.delete(id);
        final var dto = dao.findById(id);
        // Verify record was deleted
        assertNull(dto);
    }

    /**
     * Test DAO batch delete method.
     */
    @Test
    public void deleteBatch() {
        // Get generated SQL
        // Create generic DAO
        final Dao<OrdersId, Orders> dao = new GenMapDbDao<>(db, "orders");
        // Get all records
        final var countList = dao.findAll();
        List<OrdersId> list = new ArrayList<>();
        // Build list of orders to delete
        for (int i = 0; i < 3; i++) {
            list.add(new OrdersId(i + 6));
        }
        // Delete List of records
        dao.delete(list);
        assertEquals(dao.findAll().size(), countList.size() - 3);
    }
}
