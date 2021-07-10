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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Plain Java business object that only needs DAOs set from calling class. This way you can use DataSource or XADataSource. You can
 * also use transactions in your calling class to handle automatic rollback on exception. Bean validation is built in if your DTOs
 * are decorated with jakarta.validation.constraints.* annotations.
 *
 * This class should be considered thread safe.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrdersBo {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersBo.class);
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
     * Order queue used to create orders.
     */
    private OrderQueue orderQueue;

    /**
     * Default constructor.
     */
    public OrdersBo() {
    }

    /**
     * Construct with OrderQueue.
     *
     * @param orderQueue Order queue.
     */
    public OrdersBo(final OrderQueue orderQueue) {
        this.orderQueue = orderQueue;
    }

    /**
     * Construct with OrderQueue.
     *
     * @param orders Orders DAO.
     * @param orderItems OrderItems DAO.
     * @param products Products DAO.
     * @param inventories Inventories DAO.
     * @param orderQueue Order queue.
     */
    public OrdersBo(final OrderQueue orderQueue, final Dao<OrdersKey, Orders> orders,
            final Dao<OrderItemsKey, OrderItems> orderItems, final Dao<ProductsKey, Products> products,
            final Dao<InventoriesKey, Inventories> inventories) {
        this.orderQueue = orderQueue;
        this.orders = orders;
        this.orderItems = orderItems;
        this.products = products;
        this.inventories = inventories;
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

    public OrderQueue getOrderQueue() {
        return orderQueue;
    }

    /**
     * Throw exception if order doesn't exist or return dto if it does.
     *
     * @param ordersId Order ID to look up.
     * @return DTO if it exists.
     */
    public Orders orderExists(final long ordersId) {
        // Make sure order exists 
        final var dto = orders.find(new OrdersKey(ordersId));
        if (dto == null) {
            throw new RuntimeException(String.format("ordersId %d not found", ordersId));
        }
        return dto;
    }

    /**
     * Create order based on OrderMessage.
     *
     * @param customerId Customer ID.
     * @param salesmanId Salesman ID.
     * @param list List of OrderItems.
     */
    public void createOrder(final long customerId, final long salesmanId, final List<OrderItems> list) {
        final var orderMessage = new OrderMessage();
        orderMessage.setCustomerId(customerId);
        orderMessage.setSalesmanId(salesmanId);
        orderMessage.setOrderItemsList(list);
        // Send to queue and return right away
        orderQueue.create(orderMessage);
    }

    /**
     * Update status.
     *
     * @param ordersId Key to look up.
     * @param status New status value.
     */
    public void updateStatus(final long ordersId, final String status) {
        // Make sure order exists 
        final var dto = orderExists(ordersId);
        dto.setStatus(status);
        if (logger.isDebugEnabled()) {
            logger.debug("Updating status {}", dto);
        }
        // Update record
        orders.update(dto.getKey(), dto);
    }

    /**
     * Show how you can link child tables easily without composite SQL. This will create a model for FreeMarker template or other
     * uses.
     *
     * @param ordersId Orders ID.
     * @return Map of objects representing order.
     */
    public Map<String, Object> orderInfo(final long ordersId) {
        // Make sure order exists 
        final var ordersDto = orderExists(ordersId);
        // Template model
        final Map<String, Object> model = new HashMap<>();
        // Order
        model.put("ordersDto", ordersDto);
        // Get list of order items by key range
        final var orderItemsList = orderItems.findRange(new OrderItemsKey(0L, ordersDto.getOrderId()), new OrderItemsKey(Long.MAX_VALUE,
                ordersDto.getOrderId()));
        model.put("orderItemsList", orderItemsList);
        // Product list in same order as order items list
        final List<Products> productsList = new ArrayList<>();
        // Product for each order item
        orderItemsList.forEach(items -> {
            productsList.add(products.find(new ProductsKey(items.getProductId())));
        });
        model.put("productsList", productsList);
        return model;
    }
}
