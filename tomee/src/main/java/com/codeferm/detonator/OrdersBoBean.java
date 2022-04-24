/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.transaction.Transactional;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Orders business object with transactions. This is an EJB wrapper for OrdersBo.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Singleton
public class OrdersBoBean {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersBoBean.class);
    /**
     * Injected JMS Context.
     */
    @Inject
    JMSContext jmsContext;
    /**
     * Create Order MDB.
     *
     * For TomEE use openejb.deploymentId.format={ejbJarId}/{ejbName}
     */
    @Resource(name = "CreateOrderBean")
    private Queue createOrderBean;
    /**
     * Business object.
     */
    @Inject
    @Named
    private OrdersBo ordersBo;

    /**
     * Default constructor.
     */
    public OrdersBoBean() {
    }

    /**
     * Create BO.
     */
    @PostConstruct
    void init() {
    }

    /**
     * Create new order.
     *
     * @param customerId Customer ID.
     * @param salesmanId Salesman ID.
     * @param list List of OrderItems.
     */
    public void createOrder(final long customerId, final long salesmanId, final List<OrderItems> list) {
        ordersBo.createOrder(customerId, salesmanId, list);
    }

    /**
     * Update status.
     *
     * @param ordersId Key to look up.
     * @param status New status value.
     */
    @Transactional
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
