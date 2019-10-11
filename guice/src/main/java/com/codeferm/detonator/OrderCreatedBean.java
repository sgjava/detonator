/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

/**
 * Observe CreateOrderQueue, so you can do post create processing.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrderCreatedBean extends OrderCreated {

    /**
     * Construct with OrderShipped and maxThreads.
     *
     * @param orderShipped Order shipped logic.
     * @param maxThreads Maximum processing threads.
     */
    public OrderCreatedBean(final OrderShipped orderShipped, final int maxThreads) {
        super(orderShipped, maxThreads);
    }

}
