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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Orders business object with transactions.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Transactional
@ApplicationScoped
public class OrdersBo {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(OrdersBo.class);

    /**
     * Orders DAO.
     */
    @Inject
    private Dao<OrdersKey, Orders> orders;
    /**
     * OrderItems DAO.
     */
    @Inject
    private Dao<OrderItemsKey, OrderItems> orderItems;
    /**
     * Products DAO.
     */
    @Inject
    private Dao<ProductsKey, Products> products;

    /**
     * Default constructor.
     */
    public OrdersBo() {
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
        return orders.saveReturnKey(dto, new String[]{"ORDER_ID"});
    }

    /**
     * Update status.
     *
     * @param ordersId Key to look up.
     * @param status New status value.
     */
    public void updateStatus(final int ordersId, final String status) {
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
    public void orderInfo(final int ordersId) {
        // Make sure order exists 
        final var ordersDto = orderExists(ordersId);
        logger.debug("Order {}", ordersDto);
        // Get list of order items using named query
        final var orderItemsList = orderItems.findBy("findByOrderId", new Object[]{ordersId});
        logger.debug("Order items {}", orderItemsList);
        // Show product for each order, note the Long to int is required because products.productId was auto increment
        orderItemsList.stream().map(dto -> products.find(new ProductsKey(dto.getProductId()))).forEachOrdered(
                productsDto -> {
            logger.debug("Product {}", productsDto);
        });
    }

}
