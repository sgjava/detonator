/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsId;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
     * Query map.
     */
    private static Map<String, String> sqlMap;
    /**
     * DataSource.
     */
    private static DataSource dataSource;

    @BeforeAll
    static void beforeAll() {
        properties = new Properties();
        // Get properties from classpath
        try (final var stream = GenDbDaoTest.class.getClassLoader().getResourceAsStream("app.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Property file exception", e);
        }
        try {
            // Load SQL statements from classpath
            sqlMap = QueryLoader.instance().load("/sql.properties");
        } catch (IOException e) {
            throw new RuntimeException("SQL file exception", e);
        }
        // Create DBCP DataSource
        final var ds = new BasicDataSource();
        ds.setDriverClassName(properties.getProperty("db.driver"));
        ds.setUsername(properties.getProperty("db.user"));
        ds.setPassword(properties.getProperty("db.password"));
        ds.setUrl(properties.getProperty("db.url"));
        ds.setMaxTotal(Integer.parseInt(properties.getProperty("db.pool.size")));
        dataSource = ds;
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
     * Test GenDbDao.
     */
    @Test
    void genDbDao() {
        final Dao dao = new GenDbDao(dataSource, sqlMap, OrderItemsId.class, OrderItems.class);
        final List<OrderItems> list = dao.findAll();
        // List should not be empty
        assertFalse(list.isEmpty());
        list.forEach((orderItems) -> {
            logger.debug(orderItems);
        });
        final var orderItems = dao.findById(new OrderItemsId(BigDecimal.valueOf(9), BigDecimal.valueOf(69)));
        logger.debug(orderItems);
    }

}
