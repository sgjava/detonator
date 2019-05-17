/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;


import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import java.sql.Date;
import java.time.LocalDate;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

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
     * Generic DAO.
     */
    @Inject
    private Dao<OrdersKey, Orders> dao;

    /**
     * Default constructor.
     */
    public OrdersBo() {
    }

    /**
     * Create new order.
     *
     * @param customerId Customer ID.
     * @param salesmanId Salesman ID.
     * @return Generated key.
     */
    public OrdersKey createOrder(final int customerId, final int salesmanId) {
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(customerId);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(salesmanId);
        dto.setStatus("Pending");
        // Save DTO and return identity key
        return dao.saveReturnKey(dto);
    }

    /**
     * Update status.
     *
     * @param ordersId Key to look up.
     * @param status New status value.
     */
    public void updateStatus(final int ordersId, final String status) {
        // Make sure order exists 
        final var dto = dao.find(new OrdersKey(ordersId));
        if (dto == null) {
            throw new RuntimeException(String.format("ordersId %d not found", ordersId));
        } else {
            // Set status
            dto.setStatus(status);
            // Update record
            dao.update(dto.getKey(), dto);
        }
    }
}
