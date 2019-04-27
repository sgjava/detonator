/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import com.codeferm.dto.Orders;
import java.io.IOException;
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
        try (final var stream = DbDaoTest.class.getClassLoader().getResourceAsStream("app.properties")) {
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
    public static void afterAll() throws SQLException{
        ((BasicDataSource)dataSource).close();
    }    

    /**
     * Test DbDao.
     */
    @Test
    void dbDao() {
        final DbDao dbDao = new DbUtilsDsDao(dataSource);
        final List<Orders> list = dbDao.selectList(sqlMap.get("md_orders"), Orders.class);
        // List should not be empty
        assertFalse(list.isEmpty());
        for (Orders orders : list) {
            logger.debug(orders);
        }
    }
    
}
