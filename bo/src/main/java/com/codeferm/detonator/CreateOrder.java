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
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * CreateOrder is not thread safe because it updates the inventory. Use a single threaded queue to process orders.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateOrder implements Serializable {

    /**
     * OrderMessage has all data to create order.
     */
    private OrderMessage orderMessage;
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
     * Bean validator.
     */
    private final Validator validator;

    /**
     * Default constructor. Initialize validator.
     */
    public CreateOrder() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public OrderMessage getOrderMessage() {
        return orderMessage;
    }

    public void setOrderMessage(OrderMessage orderMessage) {
        this.orderMessage = orderMessage;
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

    /**
     * Throws exception if bean validation fails.
     *
     * @param dto DTO to validate.
     */
    public void dtoValid(final Object dto) {
        var violations = validator.validate(dto);
        if (violations.size() > 0) {
            var message = "";
            // Build exception message
            message = violations.stream().map(violation -> String.format("%s.%s %s | ", violation.getRootBeanClass().
                    getSimpleName(), violation.getPropertyPath(), violation.getMessage())).reduce(message, String::concat);
            // Trim last seperator
            message = message.substring(0, message.length() - 3);
            throw new RuntimeException(String.format("Bean violations: %s", message));
        }
    }

    /**
     * Since the customer address does not really allow for selecting by warehouse region we will find first warehouse with
     * inventory.
     *
     * @param item OrderItems DTO.
     * @return
     */
    public Inventories updateInventory(final OrderItems item) {
        // Get warehouses by product.
        var list = inventories.findRange(new InventoriesKey(item.getProductId(), 0L), new InventoriesKey(item.getProductId(),
                Long.MAX_VALUE));
        // See if we get any hits
        if (list == null) {
            throw new RuntimeException(String.format("productId %d not found", item.getProductId()));
        }
        int i = 0;
        // Rifle through list and see if any warehouse has product
        while (i < list.size() && list.get(i).getQuantity() < item.getQuantity()) {
            i++;
        }
        Inventories inv = null;
        // There's enough quantity?
        if (i < list.size()) {
            inv = list.get(i);
            // Remove item quantity from inventory
            inv.setQuantity(inv.getQuantity() - item.getQuantity());
            // Save quantity update
            inventories.update(inv.getKey(), inv);
        } else {
            throw new RuntimeException(String.format("productId %d not in invenroty", item.getProductId()));
        }
        return inv;
    }

    /**
     * Add OrderItems to Orders. OrderItems.itemId must be set prior to calling.
     *
     * @param k Orders key.
     */
    public void addItems(final OrdersKey k) {
        // Process list of items
        for (final OrderItems item : orderMessage.getOrderItemsList()) {
            item.setOrderId(k.getOrderId());
            // Search warehouses for product
            final var inv = updateInventory(item);
            final var product = products.find(new ProductsKey(inv.getProductId()));
            item.setUnitPrice(product.getStandardCost());
            // Validate DTO before insert
            dtoValid(item);
            // Add item to order
            orderItems.update(item.getKey(), item);
        }
    }

    /**
     * Create order and return key.
     *
     * @return DTO with generated key.
     */
    public Orders createOrder() {
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
        dtoValid(dto);
        // Add items
        addItems(dto.getKey());
        // Notify observers DTO created
        return dto;
    }

    /**
     * toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "CreateOrder{" + "orderMessage=" + orderMessage + ", orders=" + orders + ", orderItems=" + orderItems + ", products=" +
                products + ", inventories=" + inventories + ", validator=" + validator + '}';
    }
}
