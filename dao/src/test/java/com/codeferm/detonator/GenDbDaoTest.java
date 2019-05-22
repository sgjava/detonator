/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import static com.codeferm.detonator.DbDaoTest.loadProperties;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.RegionscCountries;
import com.codeferm.dto.RegionscCountriesKey;
import java.io.FileInputStream;
import java.io.IOException;
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
            logger.warn("Properties file not found {}", propertyFile);
            // Get properties from classpath
            try (final var stream = GenDbDaoTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
                props.load(stream);
                logger.debug("Properties loaded from class path {}", propertyFile);
            } catch (IOException e2) {
                throw new RuntimeException("No properties found", e2);
            }
        }
        return props;
    }

    /**
     * Set up DataSource and initialize database.
     */
    @BeforeAll
    public static void beforeAll() {
        // Get database properties from dto project
        properties = loadProperties("../dto/src/test/resources/database.properties");
        // Merge app properties
        properties.putAll(loadProperties("app.properties"));        // Create DBCP DataSource
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
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Get all records
        final var list = dao.findAll();
        // List should not be empty
        assertFalse(list.isEmpty());
        // Verify exact count
        assertEquals(101, list.size());
    }

    /**
     * Test DAO find method.
     */
    @Test
    public void find() {
        logger.debug("find");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create ID to find
        final var key = new OrdersKey(4);
        final var dto = dao.find(key);
        // Verify record exists
        assertNotNull(dto);
        // Verify ID matches
        assertEquals(key.getOrderId(), dto.getOrderId());
        // Create ID that doesn't exist
        final var badId = new OrdersKey(0);
        final var badDto = dao.find(badId);
        // DTO should be null if not found
        assertNull(badDto);
    }

    /**
     * Test DAO find method using simple type instead of ID bean.
     */
    @Test
    public void findSimpleType() {
        logger.debug("findSimpleType");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<Integer, Orders> dao = new GenDbDao<>(dataSource, sql, Integer.class, Orders.class);
        final var key = 4;
        final var dto = dao.find(key);
        // Verify record exists
        assertNotNull(dto);
        // Verify ID matches
        assertEquals(key, dto.getOrderId());
        // Find ID that doesn't exist
        final var badDto = dao.find(0);
        // DTO should be null if not found
        assertNull(badDto);
    }

    /**
     * Test DAO find method using composite DTO and custom SQL.
     */
    @Test
    public void findComposite() {
        logger.debug("findComposite");
        // Get generated SQL
        final var sql = loadProperties("regionsccountries.properties");
        // Merge custom SQL
        sql.putAll(loadProperties("regionsccountries-custom.properties"));
        // Create generic DAO
        final Dao<RegionscCountriesKey, RegionscCountries> dao = new GenDbDao<>(dataSource, sql, RegionscCountriesKey.class,
                RegionscCountries.class);
        // Create ID to find
        final var key = new RegionscCountriesKey("DE", 1);
        final var dto = dao.find(key);
        // Verify record exists
        assertNotNull(dto);
        // Verify country ID matches
        assertEquals(key.getCountryId(), dto.getCountryId());
        // Verify region ID matches
        assertEquals(key.getRegionId(), dto.getRegionId());
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
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(1);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1);
        dto.setStatus("Pending");
        // Save DTO
        dao.save(dto);
        // Create ID to find (should be 107 based on last orderId)
        final var key = new OrdersKey(107);
        final var findDto = dao.find(key);
        // Verify ID matches
        assertEquals(107, findDto.getOrderId());
    }

    /**
     * Test DAO save method.
     */
    @Test
    public void saveBatch() {
        logger.debug("saveBatch");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Merge custom SQL
        sql.putAll(loadProperties("orders-custom.properties"));
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(1);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1);
        dto.setStatus("Pending");
        // Preserve insertion order
        final Map<OrdersKey, Orders> map = new LinkedHashMap<>();
        // For RDBMS ID is ignored
        for (int i = 0; i < 10; i++) {
            map.put(new OrdersKey(i), dto);
        }
        // Save Map of DTOs
        dao.save(map);
        // Select new records (ORDER_ID > 107)
        final var newRecs = dao.findBy("findByIdGreaterThan", new Object[]{107});
        // List should not be empty
        assertFalse(newRecs.isEmpty());
        // Verify exact count
        assertEquals(10, newRecs.size());
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
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(1);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1);
        dto.setStatus("Pending");
        // Save DTO and return identity key
        final var key = dao.saveReturnKey(dto, new String[]{"ORDER_ID"});
        // Verify returned key
        assertEquals(106, key.getOrderId());
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
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create ID to find
        final var key = new OrdersKey(4);
        final var dto = dao.find(key);
        dto.setStatus("Shipped");
        // Uopdate record
        dao.update(key, dto);
        // Verify update
        final var updateDto = dao.find(key);
        // Verify status matches
        assertEquals("Shipped", updateDto.getStatus());
    }

    /**
     * Test DAO batch update method.
     */
    @Test
    public void updateBatch() {
        logger.debug("updateBatch");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Merge custom SQL
        sql.putAll(loadProperties("orders-custom.properties"));
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Select records to update
        final var list = dao.findBy("findByIdLessThan", new Object[]{10});
        // Preserve insertion order
        final Map<OrdersKey, Orders> map = new LinkedHashMap<>();
        list.forEach(dto -> {
            map.put(new OrdersKey(dto.getOrderId()), dto);
        });
        // Batch update
        dao.update(map);
    }

    /**
     * Test DAO delete method.
     */
    @Test
    public void delete() {
        logger.debug("delete");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create ID to delete
        final var key = new OrdersKey(1);
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
        logger.debug("deleteBatch");
        // Get generated SQL
        final var sql = loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Get all records
        final var countList = dao.findAll();
        List<OrdersKey> list = new ArrayList<>();
        // Build list of orders to delete
        for (int i = 0; i < 3; i++) {
            list.add(new OrdersKey(i + 6));
        }
        // Delete List of records
        dao.delete(list);
        assertEquals(countList.size() - 3, dao.findAll().size());
    }
}
