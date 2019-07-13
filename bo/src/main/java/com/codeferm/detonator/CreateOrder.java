/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.Products;
import com.codeferm.dto.ProductsKey;
import java.sql.Date;
import java.time.LocalDate;

/**
 * CreateOrder uses UpdateInventory to update the inventory in a thread safe way. UpdateInventoryDao is a single threaded queue used
 * to process orders. UpdateInventory can be implemented for concurrent updates as well.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateOrder {

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
     * Validation bean.
     */
    private final ValidateBean validateBean;
    /**
     * Updates to inventory.
     */
    private final UpdateInventory updateInventory;

    /**
     * Construct with UpdateInventory.
     *
     * @param updateInventory Inventory updater.
     */
    public CreateOrder(final UpdateInventory updateInventory) {
        validateBean = new ValidateBean();
        this.updateInventory = updateInventory;
    }

    /**
     * Construct with UpdateInventory.
     *
     * @param updateInventory Inventory updater.
     * @param orders Orders DAO.
     * @param orderItems OrderItems DAO.
     * @param products Products DAO.
     */
    public CreateOrder(final UpdateInventory updateInventory, final Dao<OrdersKey, Orders> orders,
            final Dao<OrderItemsKey, OrderItems> orderItems, final Dao<ProductsKey, Products> products) {
        validateBean = new ValidateBean();
        this.updateInventory = updateInventory;
        this.orders = orders;
        this.orderItems = orderItems;
        this.products = products;
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

    public UpdateInventory getUpdateInventory() {
        return updateInventory;
    }

    /**
     * Add OrderItems to Orders. OrderItems.itemId must be set prior to calling.
     *
     * @param k Orders key.
     * @param orderMessage Order message.
     */
    public void addItems(final OrdersKey k, final OrderMessage orderMessage) {
        // Process list of items
        for (final OrderItems item : orderMessage.getOrderItemsList()) {
            item.setOrderId(k.getOrderId());
            // Search warehouses for product
            final var inv = updateInventory.update(item);
            final var product = products.find(new ProductsKey(inv.getProductId()));
            // Set price.
            item.setUnitPrice(product.getStandardCost());
            // Validate DTO before insert
            validateBean.valid(item);
            // Add item to order
            orderItems.update(item.getKey(), item);
        }
    }

    /**
     * Create order and return key.
     *
     * @param orderMessage Order message.
     * @return DTO with generated key.
     */
    public Orders create(final OrderMessage orderMessage) {
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(orderMessage.getCustomerId());
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(orderMessage.getSalesmanId());
        dto.setStatus("New");
        // Save DTO and return identity key
        var k = orders.saveReturnKey(dto, new String[]{"ORDER_ID"});
        // Set key in value
        dto.setOrderId(k.getOrderId());
        // Do bean validation after key created and throw exception on validation failure
        validateBean.valid(dto);
        // Add items
        addItems(dto.getKey(), orderMessage);
        return dto;
    }

    /**
     * toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "CreateOrder{" + "orders=" + orders + ", orderItems=" + orderItems + ", products=" + products + ", validateBean="
                + validateBean + ", updateInventory=" + updateInventory + '}';
    }
}
