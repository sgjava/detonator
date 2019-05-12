/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Orders business object with transactions.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrdersBo {

    /**
     * Generic DAO.
     */
    private Dao<OrdersKey, Orders> dao;

    /**
     * Default constructor.
     */
    public OrdersBo() {
    }

    /**
     * Get DAO.
     *
     * @return DAO.
     */
    public Dao<OrdersKey, Orders> getDao() {
        return dao;
    }

    /**
     * Set DAO.
     *
     * @param dao DAO.
     */
    public void setDao(final Dao<OrdersKey, Orders> dao) {
        this.dao = dao;
    }

    /**
     * Create new order.
     *
     * @param customerId Customer ID.
     * @param salesmanId Salesman ID.
     * @return Generated key.
     */
    @Transaction
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
}
