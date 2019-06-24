/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import java.util.List;

/**
 * Orders business object with transactions.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrdersBoBean {

    /**
     * Orders business object.
     */
    private OrdersBo ordersBo;

    /**
     * Default constructor.
     */
    public OrdersBoBean() {
    }

    public OrdersBo getOrdersBo() {
        return ordersBo;
    }

    public void setOrdersBo(final OrdersBo ordersBo) {
        this.ordersBo = ordersBo;
    }

    /**
     * Create new order.
     *
     * @param customerId Customer ID.
     * @param salesmanId Salesman ID.
     * @param list List of OrderItems.
     */
    @Transaction
    public void createOrder(final long customerId, final long salesmanId, final List<OrderItems> list) {
        ordersBo.createOrder(customerId, salesmanId, list);
    }

    /**
     * Update status.
     *
     * @param ordersId Key to look up.
     * @param status New status value.
     */
    @Transaction
    public void updateStatus(final long ordersId, final String status) {
        ordersBo.updateStatus(ordersId, status);
    }

    /**
     * Show how you can link child tables easily without composite SQL.
     *
     * @param ordersId Orders ID.
     */
    public void orderInfo(final long ordersId) {
        ordersBo.orderInfo(ordersId);
    }
}
