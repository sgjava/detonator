/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final Logger logger = LogManager.getLogger(GenMapDbDaoTest.class);
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
        properties.putAll(common.loadProperties("app.properties"));
        // Delete MapDB file
        try {
            Files.deleteIfExists(Paths.get(properties.getProperty("map.file")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Create MapDB
        db = DBMaker.fileDB(properties.getProperty("map.file")).make();
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
            common.copyDbToMap(dataSource, db, properties.getProperty("db.sample"), properties.getProperty("db.delimiter"), Boolean.
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
        db.close();
    }

    /**
     * Test DAO findAll method.
     */
    @Test
    public void findAll() {
        logger.debug("findAll");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        // Get all records
        final var list = dao.findAll();
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(101, list.size());
    }

    /**
     * Test DAO findById method.
     */
    @Test
    public void findById() {
        logger.debug("findById");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        final var dto = dao.find(new OrdersKey(4L));
        // Verify record exists
        assertNotNull(dto);
        // Verify ID matches
        assertEquals(4, dto.getOrderId());
        logger.debug(dto);
        // Create ID that doesn't exist
        final var badId = new OrdersKey(0L);
        final var badDto = dao.find(badId);
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
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        // Create DTO to save
        final var dto = new Orders();
        dto.setOrderId(107L);
        dto.setCustomerId(1L);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1L);
        dto.setStatus("Pending");
        // Save DTO
        dao.save(dto);
        final var findDto = dao.find(dto.getKey());
        // Verify ID matches
        assertEquals(107, findDto.getOrderId());
    }

    /**
     * Test DAO save method.
     */
    @Test
    public void saveBatch() {
        logger.debug("saveBatch");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        // Preserve insertion order
        final Map<OrdersKey, Orders> map = new LinkedHashMap<>();
        // For RDBMS ID is ignored
        for (int i = 0; i < 10; i++) {
            // Create DTO to save
            final var dto = new Orders();
            dto.setOrderId(i + 108L);
            dto.setCustomerId(1L);
            dto.setOrderDate(Date.valueOf(LocalDate.now()));
            dto.setSalesmanId(1L);
            dto.setStatus("Pending");
            map.put(new OrdersKey(i + 108L), dto);
        }
        // Save Map of DTOs
        dao.save(map);
    }
    
    /**
     * Test DAO save and return generated key method.
     */
    @Test
    public void saveReturnKey() {
        logger.debug("saveReturnKey");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(1L);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1L);
        dto.setStatus("Pending");
        // Save DTO and return identity key
        final var key = dao.saveReturnKey(dto, new String[]{"orderId"});
        // Verify returned key
        assertEquals(106, key.getOrderId());
    }    

    /**
     * Test DAO update method.
     */
    @Test
    public void update() {
        logger.debug("update");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        // Create ID to find
        final var dto = dao.find(new OrdersKey(4L));
        dto.setStatus("Shipped");
        // Uopdate record
        dao.update(dto.getKey(), dto);
        // Verify update
        final var updateDto = dao.find(dto.getKey());
        // Verify status matches
        assertEquals("Shipped", updateDto.getStatus());
    }

    /**
     * Test DAO batch update method.
     */
    @Test
    public void updateBatch() {
        logger.debug("updateBatch");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        // Preserve insertion order
        final Map<OrdersKey, Orders> map = new LinkedHashMap<>();
        for (long i = 0; i < 10; i++) {
            final var dto = dao.find(new OrdersKey(i + 10L));
            dto.setStatus("Pending");
            map.put(dto.getKey(), dto);
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
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        // Create ID to delete
        final var key = new OrdersKey(1L);
        // Delete record
        dao.delete(key);
        final var dto = dao.find(key);
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
        final Dao<OrdersKey, Orders> dao = new GenMapDbDao<>(db, "orders", OrdersKey.class, Orders.class);
        // Get all records
        final var countList = dao.findAll();
        List<OrdersKey> list = new ArrayList<>();
        // Build list of orders to delete
        for (int i = 0; i < 3; i++) {
            list.add(new OrdersKey(i + 6L));
        }
        // Delete List of records
        dao.delete(list);
        assertEquals(countList.size() - 3, dao.findAll().size());
    }
}
