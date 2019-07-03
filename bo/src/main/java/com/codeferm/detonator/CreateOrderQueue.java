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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CreateOrder is not thread safe because it updates the inventory. SingleThreadExecutor used to make sure only one update at a
 * time.
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
     * Single threaded executor service.
     */
    final private ExecutorService executor;

    public CreateOrderQueue() {
        executor = Executors.newSingleThreadExecutor();
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

    /**
     * Create order.
     *
     * @param orderMessage Order message.
     */
    @Override
    public void createOrder(final OrderMessage orderMessage) {
        final Runnable task = () -> {
            final var createOrder = new CreateOrder();
            createOrder.setOrderMessage(orderMessage);
            createOrder.setInventories(inventories);
            createOrder.setOrderItems(orderItems);
            createOrder.setOrders(orders);
            createOrder.setProducts(products);
            notifyObservers(createOrder.createOrder());
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
