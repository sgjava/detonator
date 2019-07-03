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

    public OrderCreatedBean(final int poolSize) {
        super(poolSize);
    }

}
