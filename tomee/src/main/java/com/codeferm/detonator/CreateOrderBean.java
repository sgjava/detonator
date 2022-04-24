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
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
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
    @Named("test")
    private Dao<OrdersKey, Orders> orders;
    /**
     * OrderItems DAO.
     */
    @Inject
    @Named
    private Dao<OrderItemsKey, OrderItems> orderItems;
    /**
     * Products DAO.
     */
    @Inject
    @Named
    private Dao<ProductsKey, Products> products;
    /**
     * Inventories DAO.
     */
    @Inject
    @Named
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

    public CreateOrderBean() {
    }

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
