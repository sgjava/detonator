/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrdersKey;
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
@ApplicationScoped
public class OrdersBo {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersBo.class);
    /**
     * Plain Java business object.
     */
    @Inject
    @OrdersObjType
    private OrdersObj ordersObj;

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
    @Transactional
    public OrdersKey createOrder(final long customerId, final long salesmanId) {
        return ordersObj.createOrder(customerId, salesmanId);
    }

    /**
     * Update status.
     *
     * @param ordersId Key to look up.
     * @param status New status value.
     */
    @Transactional
    public void updateStatus(final long ordersId, final String status) {
        ordersObj.updateStatus(ordersId, status);
    }

    /**
     * Show how you can link child tables easily without composite SQL.
     *
     * @param ordersId Orders ID.
     */
    public void orderInfo(final long ordersId) {
        ordersObj.orderInfo(ordersId);
    }

}
