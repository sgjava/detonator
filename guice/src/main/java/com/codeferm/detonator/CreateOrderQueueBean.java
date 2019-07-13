/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

/**
 * Simple bean to wrap createOrder in a transaction.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateOrderQueueBean extends CreateOrderQueue {

    /**
     * Default constructor.
     */
    public CreateOrderQueueBean() {
    }

    /**
     * Construct with CreateOrder and ExecutorService.
     *
     * @param createOrder CreateOrder
     */
    public CreateOrderQueueBean(CreateOrder createOrder) {
        super(createOrder);
    }

    /**
     * Create order with transaction.
     *
     * @param orderMessage Order message.
     */
    @Override
    @Transaction
    public void create(final OrderMessage orderMessage) {
        super.create(orderMessage);
    }
}
