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
import java.util.Properties;
import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

/**
 * DAO producer.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DaoProducer {

    /**
     * DataSource.
     */
    @Resource
    private DataSource dataSource;

    /**
     * Default constructor.
     */
    public DaoProducer() {
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
        return new GenDbDao<>(dataSource, loadProperties("contacts.properties"), ContactsKey.class, Contacts.class);
    }

    @Produces
    public Dao<CountriesKey, Countries> getCountries() {
        return new GenDbDao<>(dataSource, loadProperties("countries.properties"), CountriesKey.class, Countries.class);
    }

    @Produces
    public Dao<CustomersKey, Customers> getCustomers() {
        return new GenDbDao<>(dataSource, loadProperties("customers.properties"), CustomersKey.class, Customers.class);
    }

    @Produces
    public Dao<EmployeesKey, Employees> getEmployees() {
        return new GenDbDao<>(dataSource, loadProperties("employees.properties"), EmployeesKey.class, Employees.class);
    }

    @Produces
    public Dao<InventoriesKey, Inventories> getInventories() {
        return new GenDbDao<>(dataSource, loadProperties("inventories.properties"), InventoriesKey.class, Inventories.class);
    }
    
    @Produces
    public Dao<LocationsKey, Locations> getLocations() {
        return new GenDbDao<>(dataSource, loadProperties("locations.properties"), LocationsKey.class, Locations.class);
    }
    
    @Produces
    public Dao<OrderItemsKey, OrderItems> getOrderItems() {
        return new GenDbDao<>(dataSource, loadProperties("ordersitems.properties"), OrderItemsKey.class, OrderItems.class);
    }

    @Produces
    public Dao<OrdersKey, Orders> getOrders() {
        return new GenDbDao<>(dataSource, loadProperties("orders.properties"), OrdersKey.class, Orders.class);
    }
    
    @Produces
    public Dao<ProductCategoriesKey, ProductCategories> getProductCategories() {
        return new GenDbDao<>(dataSource, loadProperties("productcategories.properties"), ProductCategoriesKey.class, ProductCategories.class);
    }

    @Produces
    public Dao<ProductsKey, Products> getProducts() {
        return new GenDbDao<>(dataSource, loadProperties("products.properties"), ProductsKey.class, Products.class);
    }

    @Produces
    public Dao<RegionsKey, Regions> getRegions() {
        return new GenDbDao<>(dataSource, loadProperties("regions.properties"), RegionsKey.class, Regions.class);
    }
    
    @Produces
    public Dao<WarehousesKey, Warehouses> getWarehouses() {
        return new GenDbDao<>(dataSource, loadProperties("warehouses.properties"), WarehousesKey.class, Warehouses.class);
    }
}
