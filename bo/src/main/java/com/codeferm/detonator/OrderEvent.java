/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;
import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.Products;
import com.codeferm.dto.ProductsKey;
import java.util.List;

/**
 * Disruptor event to add order. Adjusting inventory quantity needs to be thread safe.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrderEvent {

    /**
     * Mapped from database field CUSTOMER_ID, type BIGINT, precision 19, scale 0.
     */
    private Long customerId;
    /**
     * Mapped from database field SALESMAN_ID, type BIGINT, precision 19, scale 0.
     */
    private Long salesmanId;
    /**
     * Orders DAO.
     */
    private Dao<OrdersKey, Orders> orders;
    /**
     * OrderItems DAO.
     */
    private Dao<OrderItemsKey, OrderItems> orderItems;
    /**
     * Products DAO.
     */
    private Dao<ProductsKey, Products> products;
    /**
     * Inventories DAO.
     */
    private Dao<InventoriesKey, Inventories> inventories;
    /**
     * Order items to add to order.
     */
    private List<OrderItems> orderItemsList;

    /**
     * Default constructor.
     */
    public OrderEvent() {
    }

    /**
     * Accessor for field customerId.
     *
     * @return customerId Get customerId.
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * Mutator for field customerId.
     *
     * @param customerId Set customerId.
     */
    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Accessor for field salesmanId.
     *
     * @return salesmanId Get salesmanId.
     */
    public Long getSalesmanId() {
        return salesmanId;
    }

    /**
     * Mutator for field salesmanId.
     *
     * @param salesmanId Set salesmanId.
     */
    public void setSalesmanId(final Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public Dao<OrdersKey, Orders> getOrders() {
        return orders;
    }

    public void setOrders(Dao<OrdersKey, Orders> orders) {
        this.orders = orders;
    }

    public Dao<OrderItemsKey, OrderItems> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Dao<OrderItemsKey, OrderItems> orderItems) {
        this.orderItems = orderItems;
    }

    public Dao<ProductsKey, Products> getProducts() {
        return products;
    }

    public void setProducts(Dao<ProductsKey, Products> products) {
        this.products = products;
    }

    public Dao<InventoriesKey, Inventories> getInventories() {
        return inventories;
    }

    public void setInventories(Dao<InventoriesKey, Inventories> inventories) {
        this.inventories = inventories;
    }

    public List<OrderItems> getOrderItemsList() {
        return orderItemsList;
    }

    public void setOrderItemsList(List<OrderItems> orderItemsList) {
        this.orderItemsList = orderItemsList;
    }

    /**
     * toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "OrderEvent{" + ", customerId=" + customerId + ", salesmanId=" + salesmanId + "}";
    }
}
