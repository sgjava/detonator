/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on July 3, 2016
 * sgoldsmith@codeferm.com
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
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.apache.logging.log4j.LogManager;

/**
 * MDB used for asynchronous order creation using a single thread.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "1"),
    @ActivationConfigProperty(propertyName = "maxMessagesPerSessions", propertyValue = "1")})
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
    //CHECKSTYLE:OFF DesignForExtension - CDI beans cannot have final methods
    @PreDestroy
    //CHECKSTYLE:ON DesignForExtension
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
        final ObjectMessage objectMessage = (ObjectMessage) message;
        final OrderMessage orderMessage;
        try {
            orderMessage = (OrderMessage) objectMessage.getObject();
            createOrder.create(orderMessage);
            //logger.debug("Created {}", orderMessage);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
