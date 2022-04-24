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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.ws.rs.Produces;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Business object producer.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Singleton
public class OrdersBoProducer {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersBoProducer.class);
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
    public OrdersBoProducer() {
    }

    @PostConstruct
    void init() {
        ordersBo = new OrdersBo(new CreateOrderQueueClient(jmsContext, createOrderBean));
        ordersBo.setOrderItems(orderItems);
        ordersBo.setOrders(orders);
        ordersBo.setProducts(products);
        ordersBo.setInventories(inventories);        
    }

    @Produces
    public OrdersBo getOrdersBo() {
        return ordersBo;
    }
}
