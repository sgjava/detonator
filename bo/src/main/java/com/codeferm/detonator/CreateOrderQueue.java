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
 * UpdateInventoryDao is not thread safe because it updates the inventory. SingleThreadExecutor used to make sure only one update at
 * a time.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class CreateOrderQueue extends Observable<CreateOrderQueue, Orders> implements OrderQueue {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(CreateOrderQueue.class);
    /**
     * Single threaded executor service.
     */
    private final ExecutorService executor;
    /**
     * Create order logic.
     */
    private CreateOrder createOrder;

    /**
     * Construct with ValidateBean and ExecutorService.
     */
    public CreateOrderQueue() {
        executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("create-order-queue-%d").build());
    }

    /**
     * Construct with ValidateBean, CreateOrder and ExecutorService.
     *
     * @param createOrder CreateOrder
     */
    public CreateOrderQueue(final CreateOrder createOrder) {
        this.createOrder = createOrder;
        executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("create-order-queue-%d").build());
    }

    public CreateOrder getCreateOrder() {
        return createOrder;
    }

    public void setCreateOrder(final CreateOrder createOrder) {
        this.createOrder = createOrder;
    }

    /**
     * Create order.
     *
     * @param orderMessage Order message.
     */
    @Override
    public void create(final OrderMessage orderMessage) {
        final Runnable task = () -> {
            try {
                notifyObservers(createOrder.create(orderMessage));
            } catch (RuntimeException e) {
                // DeTOnator exception handling throws RuntimeException
                // You could have an exception queue deal with exceptions
                logger.error("Create order error {}", e.getMessage());
            }
        };
        executor.execute(task);
    }

    /**
     * Wait for queued threads to finish.
     */
    @Override
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
