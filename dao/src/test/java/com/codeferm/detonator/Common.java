/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Contacts;
import com.codeferm.dto.ContactsKey;
import com.codeferm.dto.Countries;
import com.codeferm.dto.CountriesKey;
import com.codeferm.dto.Customers;
import com.codeferm.dto.CustomersKey;
import com.codeferm.dto.Employees;
import com.codeferm.dto.EmployeesKey;
import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;
import com.codeferm.dto.Locations;
import com.codeferm.dto.LocationsKey;
import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.ProductCategories;
import com.codeferm.dto.ProductCategoriesKey;
import com.codeferm.dto.Products;
import com.codeferm.dto.ProductsKey;
import com.codeferm.dto.Regions;
import com.codeferm.dto.RegionsKey;
import com.codeferm.dto.RegionscCountries;
import com.codeferm.dto.RegionscCountriesKey;
import com.codeferm.dto.Warehouses;
import com.codeferm.dto.WarehousesKey;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;

/**
 * Common test methods.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class Common {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(Common.class);

    /**
     * Default constructor.
     */
    public Common() {
    }

    /**
     * Load properties file from file path or fail back to class path.
     *
     * @param propertyFile Name of property file.
     * @return Properties.
     */
    public Properties loadProperties(final String propertyFile) {
        Properties props = new Properties();
        try {
            // Get properties from file
            props.load(new FileInputStream(propertyFile));
            logger.debug("Properties loaded from file {}", propertyFile);
        } catch (IOException e1) {
            // Get properties from classpath
            try (final var stream = GenMapDbDaoTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
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
     * @param dataSource Data source.
     * @param fileName SQL script to create database.
     * @param delimiter Line delimiter.
     * @param removeDelimiter True to remove delimiter from statement
     */
    public void createDb(final DataSource dataSource, final String fileName, final String delimiter, boolean removeDelimiter) {
        logger.debug("Creating database from {}", fileName);
        final var dataLoader = new DataLoader(dataSource);
        dataLoader.execScript(fileName, delimiter, removeDelimiter);
    }

    /**
     * Copy RDBMS table to MapDB using K/V classes.
     *
     * @param dataSource Data source.
     * @param db MapDB.
     * @param mapName MapDB name of map.
     * @param propName Property file containing SQL statements
     * @param kClass Key class.
     * @param vClass Value class.
     * @return Map of K/V types.
     */
    public ConcurrentMap copyTable(DataSource dataSource, final DB db, final String mapName, final String propName,
            final Class kClass, final Class vClass) {
        logger.debug("Copying table to map {}", mapName);
        final ConcurrentMap map = db.treeMap(mapName, Serializer.JAVA, Serializer.JAVA).createOrOpen();
        // Start with empty Map
        map.clear();
        final var sql = loadProperties(propName);
        // Create generic RDBMS DAO
        final GenDbDao dao = new GenDbDao(dataSource, sql, kClass, vClass);
        // Get all records
        final var list = dao.findAll();
        list.forEach(dto -> {
            map.put(dao.getKey(dto), dto);
        });
        // Create auto increment key starting with last key in map
        final var lastKey = ((BTreeMap) map).lastKey();
        // Get value fields
        final var fields = kClass.getDeclaredFields();
        // Only set atomic values if single field key
        if (fields.length == 1) {
            // Get field
            final var field = fields[0];
            // Only Long can be auto increment
            if (field.getType() == Long.class) {
                try {
                    final var readMethod = new PropertyDescriptor(field.getName(), kClass).getReadMethod();
                    final var lastValue = readMethod.invoke(lastKey, (Object[]) null);
                    logger.debug("Setting atomic to {}", lastValue);
                    db.atomicLong(String.format("%s_key", mapName), (Long) lastValue).create();
                } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            db.atomicLong(String.format("%s_key", mapName), 0L).create();
        }
        return map;
    }

    /**
     * Create test SQL database and populate MapDB from RDBMS.
     *
     * @param dataSource Data source.
     * @param db MapDB.
     * @param fileName SQL script to create database.
     * @param delimiter Line delimiter.
     * @param removeDelimiter True to remove delimiter from statement
     */
    public void copyDbToMap(DataSource dataSource, final DB db, final String fileName, final String delimiter,
            final boolean removeDelimiter) {
        // Use DataLoader to create RDBMS database
        createDb(dataSource, fileName, delimiter, removeDelimiter);
        // Copy all RDBMS tables to MapDB
        copyTable(dataSource, db, "contacts", "contacts.properties", ContactsKey.class, Contacts.class);
        copyTable(dataSource, db, "countries", "countries.properties", CountriesKey.class, Countries.class);
        copyTable(dataSource, db, "customers", "customers.properties", CustomersKey.class, Customers.class);
        copyTable(dataSource, db, "employees", "employees.properties", EmployeesKey.class, Employees.class);
        copyTable(dataSource, db, "inventories", "inventories.properties", InventoriesKey.class, Inventories.class);
        copyTable(dataSource, db, "locations", "locations.properties", LocationsKey.class, Locations.class);
        copyTable(dataSource, db, "orderitems", "orderitems.properties", OrderItemsKey.class, OrderItems.class);
        copyTable(dataSource, db, "orders", "orders.properties", OrdersKey.class, Orders.class);
        copyTable(dataSource, db, "productcategories", "productcategories.properties", ProductCategoriesKey.class,
                ProductCategories.class);
        copyTable(dataSource, db, "products", "products.properties", ProductsKey.class, Products.class);
        copyTable(dataSource, db, "regions", "regions.properties", RegionsKey.class, Regions.class);
        copyTable(dataSource, db, "regionsccountries", "regionsccountries.properties", RegionscCountriesKey.class,
                RegionscCountries.class);
        copyTable(dataSource, db, "warehouses", "warehouses.properties", WarehousesKey.class, Warehouses.class);
        logger.debug("Committing Map to file {}", fileName);
        db.commit();
    }
}
