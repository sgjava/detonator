/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
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
            try (final var stream = MakeDtoTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
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
     * Test getClasses.
     */
    @Test
    public void getClasses() {
        logger.debug("getClasses");
        final var makeDto = new MakeDto(dataSource, "src/main/resources/templates", true);
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sqlMap.get("md_orders"), true);
        // Test all columns
        var classes = makeDto.getClasses(map, false);
        // Set should not be empty
        assertFalse(classes.isEmpty());
        // Set should contain one item
        assertEquals(1, classes.size());
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
    public void dtoTemplate() {
        logger.debug("dtoTemplate");
        final var makeDto = new MakeDto(dataSource, "src/main/resources/templates", true);
        final var metadataExtract = new MetadataExtract();
        final var tables = metadataExtract.uniqueTableNames(sqlMap.get("md_orders"));
        // Use camelCase of table name
        final var className = metadataExtract.toCamelCase(tables.get(0));
        // Use StringWriter for template
        final var out = new StringWriter();
        makeDto.dtoTemplate("dto.ftl", sqlMap.get("md_orders"), null, "com.codeferm.dto", className, out);
        logger.debug(out.toString());
    }

    /**
     * Test composite DTO.
     */
    @Test
    public void dtoComposite() {
        logger.debug("dtoComposite");
        final var makeDto = new MakeDto(dataSource, "src/main/resources/templates", true);
        // Key fields to override since composite metadata will not contain any primary key columns
        List<String> list = new ArrayList<>();
        list.add("REGION_ID");
        list.add("COUNTRY_ID");
        // Use StringWriter for template
        final var out = new StringWriter();
        makeDto.dtoTemplate("dto.ftl", sqlMap.get("md_two_tables"), list, "com.codeferm.dto", "Composite", out);
        logger.debug(out.toString());
    }

    /**
     * Test idTemplate.
     */
    @Test
    public void idTemplate() {
        logger.debug("idTemplate");
        final var makeDto = new MakeDto(dataSource, "src/main/resources/templates", true);
        final var metadataExtract = new MetadataExtract();
        final var tables = metadataExtract.uniqueTableNames(sqlMap.get("md_orders"));
        // Use camelCase of table name
        final var className = metadataExtract.toCamelCase(tables.get(0)) + "Pk";
        // Use StringWriter for template
        final var out = new StringWriter();
        makeDto.idTemplate("key.ftl", sqlMap.get("md_orders"), null, "com.codeferm.dto", className, out);
        logger.debug(out.toString());
    }

    /**
     * Test sqlTemplate.
     */
    @Test
    public void sqlTemplate() {
        logger.debug("sqlTemplate");
        final var makeDto = new MakeDto(dataSource, "src/main/resources/templates", true);
        // Use StringWriter for template
        final var out = new StringWriter();
        makeDto.sqlTemplate("sql.ftl", sqlMap.get("md_order_items"), null, out);
        logger.debug(out.toString());
    }
}
