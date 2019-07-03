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

    public CreateOrderQueueBean() {
    }

    /**
     * Create order with transaction.
     *
     * @param orderMessage Order message.
     */
    @Override
    @Transaction
    public void createOrder(final OrderMessage orderMessage) {
        super.createOrder(orderMessage);
    }
}
