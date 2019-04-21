/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test MetadataExtract.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class MakeDtoTest {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(MakeDtoTest.class);

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
        try (final var stream = MakeDtoTest.class.getClassLoader().getResourceAsStream("app.properties")) {
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
     * Test dtoTemplate.
     */
    @Test
    void getClasses() {
        logger.debug("getClasses");
        final var makeDto = new MakeDto(dataSource, "src/main/resources/templates");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sqlMap.get("md_orders"));
        // Test all columns
        var classes = makeDto.getClasses(map, false);
        // Set should not be empty
        assertFalse(classes.isEmpty());
        // Set should contain two items
        assertEquals(classes.size(), 2);
        for (final var className : classes) {
            logger.debug(className);
        }
        // Test only PK columns
        classes = makeDto.getClasses(map, true);
        // Set should be empty
        assertTrue(classes.isEmpty());
    }

    /**
     * Test dtoTemplate.
     */
    @Test
    void dtoTemplate() {
        logger.debug("dtoTemplate");
        final var makeDto = new MakeDto(dataSource, "src/main/resources/templates");
        final var metadataExtract = new MetadataExtract();
        final var tables = metadataExtract.uniqueTableNames(sqlMap.get("md_orders"));
        // Use camelCase of table name
        final var className = metadataExtract.toCamelCase(tables.get(0));
        // Use StringWriter for template
        final var out = new StringWriter();
        makeDto.dtoTemplate(sqlMap.get("md_orders"), "com.codeferm.dto", className, out);
        logger.debug(out.toString());
    }

    /**
     * Test sqlTemplate.
     */
    @Test
    void sqlTemplate() {
        logger.debug("sqlTemplate");
        final var makeDto = new MakeDto(dataSource, "src/main/resources/templates");
        // Use StringWriter for template
        final var out = new StringWriter();
        makeDto.sqlTemplate(sqlMap.get("md_order_items"), out);
        logger.debug(out.toString());
    }
}
