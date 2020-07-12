/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

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
     * JMS context.
     */
    private final JMSContext jmsContext;
    /**
     * Create order MDB.
     */
    private final Queue createOrderBean;

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
    public void create(final OrderMessage orderMessage) {
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
                TimeUnit.MILLISECONDS.sleep(100);
                queueBrowser = jmsContext.createBrowser(createOrderBean);
                enumeration = queueBrowser.getEnumeration();
            }
            queueBrowser.close();
        } catch (JMSException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
