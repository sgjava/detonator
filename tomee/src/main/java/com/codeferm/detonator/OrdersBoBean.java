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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSContext;
import javax.jms.Queue;
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
@Singleton
public class OrdersBoBean {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersBoBean.class);
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
     * Inventories DAO.
     */
    @Inject
    private Dao<InventoriesKey, Inventories> inventories;
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
     * Plain Java business object.
     */
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
        ordersBo = new OrdersBo(new CreateOrderQueueClient(jmsContext, createOrderBean));
        ordersBo.setOrderItems(orderItems);
        ordersBo.setOrders(orders);
        ordersBo.setProducts(products);
        ordersBo.setInventories(inventories);
    }

    /**
     * Get orders BO.
     *
     * @return BO.
     */
    public OrdersBo getOrdersBo() {
        return ordersBo;
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
