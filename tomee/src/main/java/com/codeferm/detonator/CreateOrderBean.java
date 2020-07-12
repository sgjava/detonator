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
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import org.apache.logging.log4j.LogManager;

/**
 * MDB used for asynchronous order creation using single session.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "1")})
public class CreateOrderBean implements MessageListener {

    /**
     * Logger.
     */
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CreateOrderBean.class);
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
     * Order Created MDB.
     *
     * For TomEE use openejb.deploymentId.format={ejbJarId}/{ejbName}
     */
    @Resource(name = "OrderCreatedBean")
    private Queue orderCreatedBean;
    /**
     * Create order logic.
     */
    private CreateOrder createOrder;

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.debug("PostConstruct");
        createOrder = new CreateOrder(new UpdateInventoryDao(orderItems, inventories));
        createOrder.setOrderItems(orderItems);
        createOrder.setOrders(orders);
        createOrder.setProducts(products);
    }

    /**
     * Destroy cache.
     */
    @PreDestroy
    public void destroy() {
        logger.debug("PreDestroy");
    }

    /**
     * A MessageListener is used to receive asynchronously delivered messages. Each session must insure that it passes messages
     * serially to the listener. This means that a listener assigned to one or more consumers of the same session can assume that
     * the onMessage method is not called with the next message until the session has completed the last call.
     *
     * @param message Message.
     */
    @Override
    public void onMessage(final Message message) {
        final var objectMessage = (ObjectMessage) message;
        try {
            var orderMessage = (OrderMessage) objectMessage.getObject();
            var orders = createOrder.create(orderMessage);
            jmsContext.createProducer().send(orderCreatedBean, orders);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
