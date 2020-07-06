/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;
import com.codeferm.dto.OrderItems;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test transactions.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class TransactionTest {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(TransactionTest.class);
    /**
     * Test properties.
     */
    private static Properties properties;
    /**
     * Orders DAO.
     */
    @Inject
    private Dao<OrdersKey, Orders> ordersDao;
    /**
     * Inventories DAO.
     */
    @Inject
    private Dao<InventoriesKey, Inventories> inventories;
    /**
     * Business object.
     */
    @Inject
    private OrdersBoBean ordersBo;
    /**
     * EJB container.
     */
    private static EJBContainer ejbContainer;
    /**
     * EJB context.
     */
    private static Context context;

    /**
     * Create test database.
     *
     * @param ds Datasource used to create database.
     * @param fileName SQL script to create database.
     * @param delimiter Line delimiter.
     * @param removeDelimiter True to remove delimiter from statement
     */
    public static void createDb(final DataSource ds, final String fileName, final String delimiter, boolean removeDelimiter) {
        logger.debug("Create test database with {}", fileName);
        final var dataLoader = new DataLoader(ds);
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
            // Get properties from classpath
            try (final var stream = TransactionTest.class.getClassLoader().getResourceAsStream(propertyFile)) {
                props.load(stream);
                logger.debug("Properties loaded from class path {}", propertyFile);
            } catch (IOException e2) {
                throw new RuntimeException("No properties found", e2);
            }
        }
        return props;
    }

    /**
     * Initialize database and EjbContainer one time.
     */
    @BeforeAll
    public static void beforeAll() {
        // Get database properties from dto project
        properties = loadProperties("../dto/src/test/resources/database.properties");
        // Merge app properties
        properties.putAll(loadProperties("app.properties"));
        // Create DBCP DataSource
        final var ds = new BasicDataSource();
        ds.setDriverClassName(properties.getProperty("db.driver"));
        ds.setUsername(properties.getProperty("db.user"));
        ds.setPassword(properties.getProperty("db.password"));
        ds.setUrl(properties.getProperty("db.url"));
        ds.setMaxTotal(Integer.parseInt(properties.getProperty("db.pool.size")));
        // Create database?
        if (Boolean.parseBoolean(properties.getProperty("db.create"))) {
            createDb(ds, properties.getProperty("db.sample"), properties.getProperty("db.delimiter"), Boolean.parseBoolean(
                    properties.getProperty("db.remove.delimiter")));
        }
        // Shutdown DBCP pool
        try {
            ((BasicDataSource) ds).close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Use log4j2 for logging
        System.setProperty("openejb.logger.external", "true");
        System.setProperty("openejb.log.factory", "log4j2");
        // Set up for JMS
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
        System.setProperty("openejb.environment.default", "true");
        final Properties p = new Properties();
        // XADataSource
        p.put("dataSourceXa", String.format("new://Resource?type=XADataSource&class-name=%s", properties.getProperty(
                "db.xa.driver")));
        p.put("dataSourceXa.url", properties.getProperty("db.xa.url"));
        p.put("dataSourceXa.user", properties.getProperty("db.xa.user"));
        p.put("dataSourceXa.password", properties.getProperty("db.xa.password"));
        p.put("dataSourceXa.SkipImplicitAttributes", "true");
        // Otherwise goes to connection properties        
        p.put("dataSourceXa.SkipPropertiesFallback", "true");
        // DataSource
        p.put("dataSource", "new://Resource?type=DataSource");
        p.put("dataSource.DataSourceCreator", "dbcp");
        p.put("dataSource.xaDataSource", "dataSourceXa");
        p.put("dataSource.userName", properties.getProperty("db.user"));
        p.put("dataSource.password", properties.getProperty("db.password"));
        p.put("dataSource.jtaManaged", true);
        p.put("dataSource.initialSize", properties.getProperty("db.xa.pool.size"));
        p.put("dataSource.maxIdle", Integer.parseInt(properties.getProperty("db.xa.pool.size")) / 10);
        ejbContainer = EJBContainer.createEJBContainer(p);
        context = ejbContainer.getContext();
        // Delete/create dir for orders output
        try {
            Files.walk(Paths.get(properties.getProperty("output.dir"))).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(
                    File::delete);
            Files.createDirectories(Paths.get(properties.getProperty("output.dir")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }        
    }

    /**
     * Inject stuff for each test.
     */
    @BeforeEach
    public void beforeEach() {
        logger.debug("EJBContainer context bind");
        try {
            context.bind("inject", this);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uninject stuff for each test.
     */
    @AfterEach
    public void afterEach() {
        logger.debug("EJBContainer context unbind");
        try {
            ejbContainer.getContext().unbind("inject");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close EJB container.
     */
    @AfterAll
    public static void afterAll() {
        logger.debug("Closing EJBContainer");
        ejbContainer.close();
    }

    /**
     * Max out inventory for all products.
     *
     * @param value Quantity of each inventory record.
     */
    public void updateInventory(final int value) {
        final var list = inventories.findAll();
        // Max out inventory
        for (final Inventories inv : list) {
            inv.setQuantity(value);
            inventories.update(inv.getKey(), inv);
        }
    }

    /**
     * Test JTA commit.
     */
    @Test
    public void commit() {
        logger.debug("commit");
        final var maxOrders = Integer.parseInt(properties.getProperty("orders.max.create"));
        updateInventory(maxOrders);
        // Database pool size - 1 threads
        final var executor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("client.max.threads")));
        // Create some OrderItems
        final List<OrderItems> list = new ArrayList<>();
        final var item1 = new OrderItems();
        item1.setItemId(1L);
        item1.setProductId(3L);
        item1.setQuantity(1);
        list.add(item1);
        final var item2 = new OrderItems();
        item2.setItemId(2L);
        item2.setProductId(4L);
        item2.setQuantity(1);
        list.add(item2);
        logger.debug("Waiting for JMS startup");
        final Runnable firstTask = () -> {
            ordersBo.createOrder(1, 1, list);
        };
        executor.execute(firstTask);
        // Wait for JMS Start up, so it doesn't impact elapsed time
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.debug("Start BO client threads");
        for (int i = 0; i < maxOrders; i++) {
            final Runnable task = () -> {
                ordersBo.createOrder(1, 1, list);
            };
            executor.execute(task);
        }
        final var start = System.nanoTime();
        // Shutdow executor service
        executor.shutdown();
        // Wait for BO client threads to finish
        logger.debug("Waiting for BO client threads to finish");
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            logger.debug("BO client threads finished");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ordersBo.getOrdersBo().getOrderQueue().shutdown();
        final var stop = System.nanoTime();
        logger.debug("TPS: {}", maxOrders / ((stop - start) / 1000000000L));
        // See if last order created
        final var dto = ordersBo.getOrdersBo().getOrders().find(new OrdersKey(306L));
        assertNotNull(dto);
        logger.debug("Last order: {}", dto);
    }

    /**
     * Test JTA rollback.
     */
    @Test
    public void rollback() {
        logger.debug("rollback");
        // Record doesn't exist and should rollback
        Assertions.assertThrows(RuntimeException.class, () -> {
            ordersBo.updateStatus(0, "Shipped");
        });
    }

    /**
     * Test linking tables.
     */
    @Test
    public void linkTables() {
        logger.debug("linkTables");
        ordersBo.orderInfo(1);
    }

}
