/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import static com.codeferm.detonator.MakeDtoTest.createDb;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class MetadataExtractTest {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(MetadataExtractTest.class);

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

    /**
     * Create test database.
     *
     * @param fileName SQL script to create database.
     * @param delimiter Line delimiter.
     * @param removeDelimiter True to remove delimiter from statement
     */
    static void createDb(final String fileName, final String delimiter, boolean removeDelimiter) {
        final var dataLoader = new DataLoader(dataSource);
        dataLoader.execScript(fileName, delimiter, removeDelimiter);
    }

    @BeforeAll
    static void beforeAll() {
        properties = new Properties();
        // Get properties from classpath
        try (final var stream = MetadataExtractTest.class.getClassLoader().getResourceAsStream("app.properties")) {
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
        // Create database?
        if (Boolean.parseBoolean(properties.getProperty("db.create"))) {
            createDb(properties.getProperty("db.sample"), properties.getProperty("db.delimiter"), Boolean.parseBoolean(properties.
                    getProperty("db.remove.delimiter")));
        }
    }

    /**
     * Test getTableNames.
     */
    @Test
    void uniqueTableNames() {
        logger.debug("uniqueTableNames");
        final var metadataExtract = new MetadataExtract();
        var tables = metadataExtract.uniqueTableNames(sqlMap.get("md_single_table"));
        // Set should not be empty
        assertFalse(tables.isEmpty());
        // Set should contain one item
        assertEquals(tables.size(), 1);
        //List item should equal "regions"
        assertEquals(tables.get(0), "regions");
        tables = metadataExtract.uniqueTableNames(sqlMap.get("md_two_tables"));
        // Set should not be empty
        assertFalse(tables.isEmpty());
        // Set should contain two items
        assertEquals(tables.size(), 2);
        // First item should equal "regions"
        assertEquals(tables.get(0), "regions");
        // Second item should equal "countries"
        assertEquals(tables.get(1), "countries");
    }

    /**
     * Test toUpperCase.
     */
    @Test
    void toUpperCase() {
        logger.debug("toUpperCase");
        final var metadataExtract = new MetadataExtract();
        // Test upper case with lower case
        assertEquals(metadataExtract.toUpperCase("lower"), "LOWER");
        // Test upper case with upper case
        assertEquals(metadataExtract.toUpperCase("UPPER"), "UPPER");
        // Test mixed case with upper case
        assertEquals(metadataExtract.toUpperCase("MiXed"), "MIXED");
    }

    /**
     * Test toLowerCase.
     */
    @Test
    void toLowerCase() {
        logger.debug("toLowerCase");
        final var metadataExtract = new MetadataExtract();
        // Test lower case with upper case
        assertEquals(metadataExtract.toLowerCase("UPPER"), "upper");
        // Test lower case with lower case
        assertEquals(metadataExtract.toLowerCase("lower"), "lower");
        // Test mixed case with lower case
        assertEquals(metadataExtract.toLowerCase("MiXed"), "mixed");
    }

    /**
     * Test getResultSetMetaData.
     */
    @Test
    void getResultSetMetaData() throws SQLException {
        logger.debug("getResultSetMetaData");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sqlMap.get("md_orders"));
        // List should not be empty
        assertFalse(map.isEmpty());
        // List should contain 5 items
        assertEquals(map.size(), 5);
        // Show DTOs
        map.entrySet().forEach((var entry) -> {
            logger.debug(entry.getValue());
        });
    }

    /**
     * Test camelCase.
     */
    @Test
    void toCamelCase() {
        logger.debug("toCamelCase");
        final var metadataExtract = new MetadataExtract();
        // Test upper case with underscore
        assertEquals(metadataExtract.toCamelCase("CAMEL_CASE"), "CamelCase");
        // Test lower case with underscore
        assertEquals(metadataExtract.toCamelCase("camel_case"), "CamelCase");
        // Test mixed case with underscore
        assertEquals(metadataExtract.toCamelCase("CaMel_cASE"), "CamelCase");
        // Test upper case without underscore
        assertEquals(metadataExtract.toCamelCase("CAMELCASE"), "Camelcase");
        // Test lower case without underscore
        assertEquals(metadataExtract.toCamelCase("camelcase"), "Camelcase");
        // Test mixed case without underscore
        assertEquals(metadataExtract.toCamelCase("caMelCAse"), "Camelcase");
    }

    /**
     * Get map of primary key fields sorted by sequence.
     */
    @Test
    void getPrimaryKey() {
        logger.debug("getPrimaryKey");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getPrimaryKey(dataSource, "inventories");
        // Map should not be empty
        assertFalse(map.isEmpty());
        // Map should contain two items
        assertEquals(map.size(), 2);
        // Show PK fields
        map.entrySet().forEach((entry) -> {
            logger.debug("{} : {}", entry.getKey(), entry.getValue());
        });
    }

    @Test
    void getTableNames() {
        logger.debug("getTableNames");
        final var metadataExtract = new MetadataExtract();
        final var list = metadataExtract.getTableNames(dataSource, properties.getProperty("db.catalog"), properties.getProperty(
                "db.schema.pattern"), "%", new String[]{"TABLE"}, false);
        // Map should not be empty
        assertFalse(list.isEmpty());
        // List should contain 12 items
        assertEquals(list.size(), 12);
        // Show DTOs
        list.forEach((tableName) -> {
            logger.debug(tableName);
        });
    }
}
