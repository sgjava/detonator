/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.lmax.disruptor.EventFactory;

/**
 * Disruptor event factory.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrderEventFactory implements EventFactory<OrderEvent> {

    /**
     * Return new inventory event.
     *
     * @return Inventory event.
     */
    @Override
    public OrderEvent newInstance() {
        return new OrderEvent();
    }

}
