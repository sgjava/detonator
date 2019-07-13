/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Observe CreateOrderQueue, so you can do post create processing.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrderCreated implements Observer<CreateOrderQueue, Orders> {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrderCreated.class);
    /**
     * Multi threaded executor service.
     */
    private final ExecutorService executor;

    /**
     * Construct with pooled executor.
     *
     * @param poolSize Size of pool should be one less than DB pool size.
     */
    public OrderCreated(final int poolSize) {
        executor = Executors.newFixedThreadPool(poolSize, new ThreadFactoryBuilder().setNameFormat("order-created-%d").build());
    }

    /**
     * Observer update.
     *
     * @param object Observable that called.
     * @param data Orders DTO.
     */
    @Override
    public void update(final Observable<CreateOrderQueue, Orders> object, final Orders data) {
        final Runnable task = () -> {
            //logger.debug("Created {}", data);
        };
        executor.execute(task);
    }

    /**
     * Wait for queued threads to finish.
     */
    public void shutdown() {
        // Shutdow executor service
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
