/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
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
            try (final var stream = MetadataExtractTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
                props.load(stream);
                logger.debug("Properties loaded from class path {}", propertyFile);
            } catch (IOException e2) {
                throw new RuntimeException("No properties found", e2);
            }
        }
        return props;
    }

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
     * Get properties, SQL Map and configure DataSource.
     */
    @BeforeAll
    public static void beforeAll() {
        // Get database properties
        properties = loadProperties("database.properties");
        // Merge app properties
        properties.putAll(loadProperties("app.properties"));
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
     * Shut down DataSource.
     *
     * @throws SQLException Possible exception.
     */
    @AfterAll
    public static void afterAll() throws SQLException {
        ((BasicDataSource) dataSource).close();
    }

    /**
     * Test getTableNames.
     */
    @Test
    public void uniqueTableNames() {
        logger.debug("uniqueTableNames");
        final var metadataExtract = new MetadataExtract();
        var tables = metadataExtract.uniqueTableNames(sqlMap.get("md_single_table"));
        // Set should not be empty
        assertFalse(tables.isEmpty());
        // Set should contain one item
        assertEquals(1, tables.size());
        //List item should equal "regions"
        assertEquals("regions", tables.get(0));
        tables = metadataExtract.uniqueTableNames(sqlMap.get("md_two_tables"));
        // Set should not be empty
        assertFalse(tables.isEmpty());
        // Set should contain two items
        assertEquals(2, tables.size());
        // First item should equal "regions"
        assertEquals("regions", tables.get(0));
        // Second item should equal "countries"
        assertEquals("countries", tables.get(1));
    }

    /**
     * Test toUpperCase.
     */
    @Test
    public void toUpperCase() {
        logger.debug("toUpperCase");
        final var metadataExtract = new MetadataExtract();
        // Test upper case with lower case
        assertEquals("LOWER", metadataExtract.toUpperCase("lower"));
        // Test upper case with upper case
        assertEquals("UPPER", metadataExtract.toUpperCase("UPPER"));
        // Test mixed case with upper case
        assertEquals("MIXED", metadataExtract.toUpperCase("MiXed"));
    }

    /**
     * Test toLowerCase.
     */
    @Test
    public void toLowerCase() {
        logger.debug("toLowerCase");
        final var metadataExtract = new MetadataExtract();
        // Test lower case with upper case
        assertEquals("upper", metadataExtract.toLowerCase("UPPER"));
        // Test lower case with lower case
        assertEquals("lower", metadataExtract.toLowerCase("lower"));
        // Test mixed case with lower case
        assertEquals("mixed", metadataExtract.toLowerCase("MiXed"));
    }

    /**
     * Test getResultSetMetaData.
     */
    @Test
    public void getResultSetMetaData() throws SQLException {
        logger.debug("getResultSetMetaData");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sqlMap.get("md_orders"), true);
        // Map should not be empty
        assertFalse(map.isEmpty());
        // Map should contain 5 items
        assertEquals(5, map.size());
        // Show DTOs
        map.entrySet().forEach((final          var entry) -> {
            logger.debug(entry.getValue());
        });
    }

    /**
     * Test camelCase.
     */
    @Test
    public void toCamelCase() {
        logger.debug("toCamelCase");
        final var metadataExtract = new MetadataExtract();
        // Test upper case with underscore
        assertEquals("CamelCase", metadataExtract.toCamelCase("CAMEL_CASE"));
        // Test lower case with underscore
        assertEquals("CamelCase", metadataExtract.toCamelCase("camel_case"));
        // Test mixed case with underscore
        assertEquals("CamelCase", metadataExtract.toCamelCase("CaMel_cASE"));
        // Test upper case without underscore
        assertEquals("Camelcase", metadataExtract.toCamelCase("CAMELCASE"));
        // Test lower case without underscore
        assertEquals("Camelcase", metadataExtract.toCamelCase("camelcase"));
        // Test mixed case without underscore
        assertEquals("Camelcase", metadataExtract.toCamelCase("caMelCAse"));
    }

    /**
     * Get map of primary key fields sorted by sequence.
     */
    @Test
    public void getPrimaryKey() {
        logger.debug("getPrimaryKey");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getPrimaryKey(dataSource, "inventories");
        // Map should not be empty
        assertFalse(map.isEmpty());
        // Map should contain two items
        assertEquals(2, map.size());
        // Show PK fields
        map.entrySet().forEach((final          var entry) -> {
            logger.debug("{} : {}", entry.getKey(), entry.getValue());
        });
    }

    /**
     * Override primary key columns in composite.
     */
    @Test
    public void overridePrimaryKey() {
        logger.debug("overridePrimaryKey");
        final var metadataExtract = new MetadataExtract();
        // Return metadata from composite with no key columns
        final var map = metadataExtract.getResultSetMetaData(dataSource, sqlMap.get("md_two_tables"), true);
        // Map should not be empty
        assertFalse(map.isEmpty());
        // Key fields to override
        List<String> list = new ArrayList<>();
        list.add("REGION_ID");
        list.add("COUNTRY_ID");
        // Do overide
        metadataExtract.overridePrimaryKey(map, list);
        assertEquals(1, map.get("REGION_ID").getKeySeq());
        assertEquals(2, map.get("COUNTRY_ID").getKeySeq());
    }

    /**
     * Get list of table names.
     */
    @Test
    public void getTableNames() {
        logger.debug("getTableNames");
        final var metadataExtract = new MetadataExtract();
        final var list = metadataExtract.getTableNames(dataSource, properties.getProperty("db.catalog"), properties.getProperty(
                "db.schema.pattern"), properties.getProperty("db.table.name.pattern"), new String[]{"TABLE"}, false);
        // Map should not be empty
        assertFalse(list.isEmpty());
        // List should contain 12 items
        assertEquals(12, list.size());
        // Show DTOs
        list.forEach((final          var tableName) -> {
            logger.debug(tableName);
        });
    }
}
