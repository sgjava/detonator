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
import javax.validation.Validation;
import javax.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Plain Java business object that only needs DAOs set from calling class. This way you can use DataSource or XADataSource. You can
 * also use transactions in your calling class to handle automatic rollback on exception. Bean validation is built in if your DTOs
 * are decorated with javax.validation.constraints.* annotations.
 *
 * This class should be considered thread safe since the Validator and DAOs are thread safe.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrdersObj {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersObj.class);
    /**
     * Bean validator.
     */
    private final Validator validator;
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
     * Default constructor. Validates bean instances. Implementations of this interface must be thread-safe.
     */
    public OrdersObj() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public Dao<OrdersKey, Orders> getOrders() {
        return orders;
    }

    public void setOrders(DbDao<OrdersKey, Orders> orders) {
        this.orders = orders;
    }

    public Dao<OrderItemsKey, OrderItems> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(DbDao<OrderItemsKey, OrderItems> orderItems) {
        this.orderItems = orderItems;
    }

    public Dao<ProductsKey, Products> getProducts() {
        return products;
    }

    public void setProducts(DbDao<ProductsKey, Products> products) {
        this.products = products;
    }

    /**
     * Throw exception if order doesn't exist.
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
     * Create new order.
     *
     * @param customerId Customer ID.
     * @param salesmanId Salesman ID.
     * @return Generated key.
     */
    public OrdersKey createOrder(final long customerId, final long salesmanId) {
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(customerId);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(salesmanId);
        dto.setStatus("Pending");
        if (logger.isDebugEnabled()) {
            logger.debug("Creating {}", dto);
        }
        // Save DTO and return identity key
        var k = orders.saveReturnKey(dto, new String[]{"ORDER_ID"});
        // Set key in value
        dto.setOrderId(k.getOrderId());
        // Do bean validation after key created and rollback on exception
        dtoValid(dto);
        return k;
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
     * Show how you can link child tables easily without composite SQL.
     *
     * @param ordersId Orders ID.
     */
    public void orderInfo(final long ordersId) {
        // Make sure order exists 
        final var ordersDto = orderExists(ordersId);
        logger.debug("Order {}", ordersDto);
        // Get list of order items by key range
        final var orderItemsList = orderItems.findRange(new OrderItemsKey(0L, ordersDto.getOrderId()), new OrderItemsKey(Long.MAX_VALUE,
                ordersDto.getOrderId()));
        logger.debug("Order items {}", orderItemsList);
        // Show product for each order item
        for (OrderItems orderItems : orderItemsList) {
            final var dto = products.find(new ProductsKey(orderItems.getProductId()));
            logger.debug("itemId {}, Product {}", orderItems.getItemId(), dto);
        }
    }
}
