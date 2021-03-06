/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.RegionscCountries;
import com.codeferm.dto.RegionscCountriesKey;
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
        final var sql = common.loadProperties("orders.properties");
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
        final var sql = common.loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create ID to find
        final var key = new OrdersKey(4L);
        final var dto = dao.find(key);
        // Verify record exists
        assertNotNull(dto);
        // Verify ID matches
        assertEquals(key.getOrderId(), dto.getOrderId());
        // Create ID that doesn't exist
        final var badId = new OrdersKey(0L);
        final var badDto = dao.find(badId);
        // DTO should be null if not found
        assertNull(badDto);
    }

    /**
     * Test DAO findRange method.
     */
    @Test
    public void findRange() {
        logger.debug("findRange");
        // Get generated SQL
        final var sql = common.loadProperties("orderitems.properties");
        // Create generic DAO
        final Dao<OrderItemsKey, OrderItems> dao = new GenDbDao<>(dataSource, sql, OrderItemsKey.class, OrderItems.class);
        // Get OrderItems by key range
        var list = dao.findRange(new OrderItemsKey(0L, 4L), new OrderItemsKey(999L, 4L));
        assertNotNull(list);
        // Verify exact count
        assertEquals(8, list.size());
    }

    /**
     * Test DAO find method using composite DTO and custom SQL.
     */
    @Test
    public void findComposite() {
        logger.debug("findComposite");
        // Get generated SQL
        final var sql = common.loadProperties("regionsccountries.properties");
        // Merge custom SQL
        sql.putAll(common.loadProperties("regionsccountries-custom.properties"));
        // Create generic DAO
        final Dao<RegionscCountriesKey, RegionscCountries> dao = new GenDbDao<>(dataSource, sql, RegionscCountriesKey.class,
                RegionscCountries.class);
        // Create ID to find
        final var key = new RegionscCountriesKey("DE", 1L);
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
        final var sql = common.loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(1L);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1L);
        dto.setStatus("Pending");
        // Save DTO
        dao.save(dto);
        // Create ID to find (should be 107 based on last orderId)
        final var key = new OrdersKey(106L);
        final var findDto = dao.find(key);
        assertNotNull(findDto);
        // Verify ID matches
        assertEquals(106, findDto.getOrderId());
    }

    /**
     * Test DAO save method.
     */
    @Test
    public void saveBatch() {
        logger.debug("saveBatch");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        // Merge custom SQL
        sql.putAll(common.loadProperties("orders-custom.properties"));
        // Create generic DAO
        final DbDao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(1L);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1L);
        dto.setStatus("Pending");
        // Preserve insertion order
        final Map<OrdersKey, Orders> map = new LinkedHashMap<>();
        // For RDBMS ID is ignored
        for (long i = 0; i < 10; i++) {
            map.put(new OrdersKey(i), dto);
        }
        // Save Map of DTOs
        dao.save(map);
        // Select new records (ORDER_ID > 107L)
        final var newRecs = dao.findBy("findByIdGreaterThan", new Object[]{106});
        // List should not be empty
        assertFalse(newRecs.isEmpty());
        // Verify exact count
        assertEquals(10, newRecs.size());
    }

    /**
     * Test DAO save and return generated key method.
     */
    @Test
    public void saveReturnKey() {
        logger.debug("saveReturnKey");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        // Create generic DAO
        final DbDao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(1L);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(1L);
        dto.setStatus("Pending");
        // Save DTO and return identity key
        final var key = dao.saveReturnKey(dto, new String[]{"ORDER_ID"});
        // Make sure keys match
        final var retDto = dao.find(key);
        assertEquals(retDto.getKey(), key);
    }

    /**
     * Test DAO update method.
     */
    @Test
    public void update() {
        logger.debug("update");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Create ID to find
        final var key = new OrdersKey(4L);
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
        final var sql = common.loadProperties("orders.properties");
        // Merge custom SQL
        sql.putAll(common.loadProperties("orders-custom.properties"));
        // Create generic DAO
        final DbDao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
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
        final var sql = common.loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
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
        logger.debug("deleteBatch");
        // Get generated SQL
        final var sql = common.loadProperties("orders.properties");
        // Create generic DAO
        final Dao<OrdersKey, Orders> dao = new GenDbDao<>(dataSource, sql, OrdersKey.class, Orders.class);
        // Get all records
        final var countList = dao.findAll();
        List<OrdersKey> list = new ArrayList<>();
        // Build list of orders to delete
        for (long i = 0; i < 3; i++) {
            list.add(new OrdersKey(i + 6));
        }
        // Delete List of records
        dao.delete(list);
        assertEquals(countList.size() - 3, dao.findAll().size());
    }
}
