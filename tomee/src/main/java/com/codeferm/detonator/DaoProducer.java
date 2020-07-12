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
import com.codeferm.dto.Warehouses;
import com.codeferm.dto.WarehousesKey;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DAO producer. Probably a more type safe way of doing this, but this works and is efficient.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Singleton
public class DaoProducer {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(DaoProducer.class);
    /**
     * DataSource.
     */
    @Resource
    private DataSource dataSource;
    /**
     * DAO Map.
     */
    private final Map<String, DbDao<?, ?>> map;

    /**
     * Default constructor.
     */
    public DaoProducer() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Create DAOs. Treat DAOs as Singleton since GenDbDao is thread safe.
     */
    void daoConfig() {
        map.put("contacts", new GenDbDao<ContactsKey, Contacts>(dataSource, loadProperties("contacts.properties"),
                ContactsKey.class, Contacts.class));
        map.put("countries", new GenDbDao<CountriesKey, Countries>(dataSource, loadProperties("countries.properties"),
                CountriesKey.class, Countries.class));
        map.put("customers", new GenDbDao<CustomersKey, Customers>(dataSource, loadProperties("customers.properties"),
                CustomersKey.class, Customers.class));
        map.put("employees", new GenDbDao<EmployeesKey, Employees>(dataSource, loadProperties("employees.properties"),
                EmployeesKey.class, Employees.class));
        map.put("inventories", new GenDbDao<InventoriesKey, Inventories>(dataSource, loadProperties("inventories.properties"),
                InventoriesKey.class, Inventories.class));
        var orderitemsSql = loadProperties("orderitems.properties");
        // Merge custom SQL properties
        orderitemsSql.putAll(loadProperties("orderitems-custom.properties"));
        map.put("orderitems", new GenDbDao<OrderItemsKey, OrderItems>(dataSource, orderitemsSql, OrderItemsKey.class,
                OrderItems.class));
        map.put("orders", new GenDbDao<OrdersKey, Orders>(dataSource, loadProperties("orders.properties"), OrdersKey.class,
                Orders.class));
        map.put("productcategories", new GenDbDao<ProductCategoriesKey, ProductCategories>(dataSource, loadProperties(
                "productcategories.properties"), ProductCategoriesKey.class, ProductCategories.class));
        map.put("products", new GenDbDao<ProductsKey, Products>(dataSource, loadProperties("products.properties"),
                ProductsKey.class, Products.class));
        map.put("regions", new GenDbDao<RegionsKey, Regions>(dataSource, loadProperties("regions.properties"),
                RegionsKey.class, Regions.class));
        map.put("warehouses", new GenDbDao<WarehousesKey, Warehouses>(dataSource, loadProperties("warehouses.properties"),
                WarehousesKey.class, Warehouses.class));
    }

    @PostConstruct
    void init() {
        logger.debug("Init DAO Map");
        daoConfig();
        logger.debug("Done DAO Map");
    }

    /**
     * Load properties file from class path.
     *
     * @param propertyFile Name of property file.
     * @return Properties.
     */
    public Properties loadProperties(final String propertyFile) {
        Properties props = new Properties();
        // Get properties from classpath
        try (final var stream = DaoProducer.class.getClassLoader().getResourceAsStream(propertyFile)) {
            props.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Property file exception", e);
        }
        return props;
    }

    @Produces
    public Dao<ContactsKey, Contacts> getContacts() {
        return (Dao<ContactsKey, Contacts>) map.get("contacts");
    }

    @Produces
    public Dao<CountriesKey, Countries> getCountries() {
        return (Dao<CountriesKey, Countries>) map.get("countries");
    }

    @Produces
    public Dao<CustomersKey, Customers> getCustomers() {
        return (Dao<CustomersKey, Customers>) map.get("customers");
    }

    @Produces
    public Dao<EmployeesKey, Employees> getEmployees() {
        return (Dao<EmployeesKey, Employees>) map.get("employees");
    }

    @Produces
    public Dao<InventoriesKey, Inventories> getInventories() {
        return (Dao<InventoriesKey, Inventories>) map.get("inventories");
    }

    @Produces
    public Dao<LocationsKey, Locations> getLocations() {
        return (Dao<LocationsKey, Locations>) map.get("locations");
    }

    @Produces
    public Dao<OrderItemsKey, OrderItems> getOrderItems() {
        return (Dao<OrderItemsKey, OrderItems>) map.get("orderitems");
    }

    @Produces
    public Dao<OrdersKey, Orders> getOrders() {
        return (Dao<OrdersKey, Orders>) map.get("orders");
    }

    @Produces
    public Dao<ProductCategoriesKey, ProductCategories> getProductCategories() {
        return (Dao<ProductCategoriesKey, ProductCategories>) map.get("productcategories");
    }

    @Produces
    public Dao<ProductsKey, Products> getProducts() {
        return (Dao<ProductsKey, Products>) map.get("products");
    }

    @Produces
    public Dao<RegionsKey, Regions> getRegions() {
        return (Dao<RegionsKey, Regions>) map.get("regions");
    }

    @Produces
    public Dao<WarehousesKey, Warehouses> getWarehouses() {
        return (Dao<WarehousesKey, Warehouses>) map.get("warehouses");
    }
}
