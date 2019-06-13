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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Orders business object with transactions.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@ApplicationScoped
public class OrdersBo {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersBo.class);
    /**
     * Bean validator.
     */
    private final Validator validator;

    /**
     * Orders DAO.
     */
    @Inject
    private DbDao<OrdersKey, Orders> orders;
    /**
     * OrderItems DAO.
     */
    @Inject
    private DbDao<OrderItemsKey, OrderItems> orderItems;
    /**
     * Products DAO.
     */
    @Inject
    private DbDao<ProductsKey, Products> products;

    /**
     * Default constructor. Create bean validator.
     */
    public OrdersBo() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
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
     * Run bean validation.
     *
     * @param dto DTO to validate.
     */
    public void orderValid(final Orders dto) {
        var violations = validator.validate(dto);
        if (violations.size() > 0) {
            var message = "";
            // Build exception message
            message = violations.stream().map((violation) -> String.format("%s.%s %s | ", violation.getRootBeanClass().
                    getSimpleName(), violation.getPropertyPath(),
                    violation.getMessage())).reduce(message, String::concat);
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
    @Transactional
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
        var k = orders.saveReturnKey(dto, new String[]{"ORDER_ID"});
        // Set key in value
        dto.setOrderId(k.getOrderId());
        // Do bean validation after key created
        orderValid(dto);
        // Save DTO and return identity key
        return k;
    }

    /**
     * Update status.
     *
     * @param ordersId Key to look up.
     * @param status New status value.
     */
    @Transactional
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
        // Get list of order items using named query
        final var orderItemsList = orderItems.findBy("findByOrderId", new Object[]{ordersId});
        logger.debug("Order items {}", orderItemsList);
        // Show product for each order
        orderItemsList.stream().map(dto -> products.find(new ProductsKey(dto.getProductId()))).forEachOrdered(
                productsDto -> {
            logger.debug("Product {}", productsDto);
        });
    }

}
