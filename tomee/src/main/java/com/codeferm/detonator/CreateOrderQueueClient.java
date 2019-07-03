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
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * CreateOrder is not thread safe because it updates the inventory. SingleThreadExecutor used to make sure only one update at a
 * time.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateOrderQueueClient implements OrderQueue {

    /**
     * Orders DAO.
     */
    private Dao<OrdersKey, Orders> orders;
    /**
     * OrderItems DAO.
     */
    private Dao<OrderItemsKey, OrderItems> orderItems;
    /**
     * Products DAO.
     */
    private Dao<ProductsKey, Products> products;
    /**
     * Inventories DAO.
     */
    private Dao<InventoriesKey, Inventories> inventories;
    /**
     * JMS context.
     */
    final private JMSContext jmsContext;
    /**
     * Create order MDB.
     */
    final private Queue createOrderBean;

    /**
     * Construct queue client with JMSContext and Queue.
     *
     * @param jmsContext JMS context.
     * @param createOrderBean Order queue.
     */
    public CreateOrderQueueClient(final JMSContext jmsContext, final Queue createOrderBean) {
        this.jmsContext = jmsContext;
        this.createOrderBean = createOrderBean;
    }

    @Override
    public Dao<OrdersKey, Orders> getOrders() {
        return orders;
    }

    @Override
    public void setOrders(Dao<OrdersKey, Orders> orders) {
        this.orders = orders;
    }

    @Override
    public Dao<OrderItemsKey, OrderItems> getOrderItems() {
        return orderItems;
    }

    @Override
    public void setOrderItems(Dao<OrderItemsKey, OrderItems> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public Dao<ProductsKey, Products> getProducts() {
        return products;
    }

    @Override
    public void setProducts(Dao<ProductsKey, Products> products) {
        this.products = products;
    }

    @Override
    public Dao<InventoriesKey, Inventories> getInventories() {
        return inventories;
    }

    @Override
    public void setInventories(Dao<InventoriesKey, Inventories> inventories) {
        this.inventories = inventories;
    }

    @Override
    public void createOrder(final OrderMessage orderMessage) {
        jmsContext.createProducer().send(createOrderBean, orderMessage);
    }

    /**
     * Wait for the queue to empty out.
     */
    @Override
    public void shutdown() {
        var queueBrowser = jmsContext.createBrowser(createOrderBean);
        try {
            var enumeration = queueBrowser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                while (enumeration.hasMoreElements()) {
                    enumeration.nextElement();
                }
                queueBrowser.close();
                TimeUnit.MILLISECONDS.sleep(1);
                queueBrowser = jmsContext.createBrowser(createOrderBean);
                enumeration = queueBrowser.getEnumeration();
            }
            queueBrowser.close();
        } catch (JMSException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
