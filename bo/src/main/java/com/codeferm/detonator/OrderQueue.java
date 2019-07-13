/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

/**
 * You can use this as a queue client or client/server.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public interface OrderQueue {

    /**
     * Create order based on message.
     *
     * @param orderMessage Order message.
     */
    void create(final OrderMessage orderMessage);
    
    /**
     * Wait for queued threads to finish.
     */
    void shutdown();

}
