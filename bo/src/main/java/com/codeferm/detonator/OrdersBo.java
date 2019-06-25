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
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Plain Java business object that only needs DAOs set from calling class. This way you can use DataSource or XADataSource. You can
 * also use transactions in your calling class to handle automatic rollback on exception. Bean validation is built in if your DTOs
 * are decorated with javax.validation.constraints.* annotations.
 *
 * This class should be considered thread safe.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrdersBo implements Observer<OrderEventHandler, Orders> {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersBo.class);
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
     * Disruptor used for orders.
     */
    private final Disruptor<OrderEvent> disruptor;
    /**
     * Order event ring buffer.
     */
    private final RingBuffer<OrderEvent> ringBuffer;

    /**
     * Default constructor. Initialize validator and lock.
     */
    public OrdersBo() {
        // Construct the Disruptor
        disruptor = new Disruptor<>(new OrderEventFactory(), 128, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE,
                new BusySpinWaitStrategy());
        final var oeh = new OrderEventHandler();
        // Observe OrderEventHandler
        oeh.addObserver(this);
        // Connect the handler
        disruptor.handleEventsWith(oeh);
        // Start the Disruptor, starts all threads running
        ringBuffer = disruptor.start();
    }

    public Dao<OrdersKey, Orders> getOrders() {
        return orders;
    }

    public void setOrders(Dao<OrdersKey, Orders> orders) {
        this.orders = orders;
    }

    public Dao<OrderItemsKey, OrderItems> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Dao<OrderItemsKey, OrderItems> orderItems) {
        this.orderItems = orderItems;
    }

    public Dao<ProductsKey, Products> getProducts() {
        return products;
    }

    public void setProducts(Dao<ProductsKey, Products> products) {
        this.products = products;
    }

    public Dao<InventoriesKey, Inventories> getInventories() {
        return inventories;
    }

    public void setInventories(Dao<InventoriesKey, Inventories> inventories) {
        this.inventories = inventories;
    }

    public Disruptor<OrderEvent> getDisruptor() {
        return disruptor;
    }

    public RingBuffer<OrderEvent> getRingBuffer() {
        return ringBuffer;
    }

    /**
     * Observable used after order creation.
     *
     * @param object Object we are observing.
     * @param data Disruptor event.
     */
    @Override
    public void update(final Observable<OrderEventHandler, Orders> object, final Orders data) {
        logger.debug("Order created: {}", data);
    }

    /**
     * Throw exception if order doesn't exist or return dto if it does.
     *
     * @param ordersId Order ID to look up.
     * @return DTO if it exists.
     */
    public Orders orderExists(final long ordersId) {
        // Make sure order exists 
        final var dto = orders.find(new OrdersKey(ordersId));
        if (dto == null) {
            throw new RuntimeException(String.format("ordersId %d not found", ordersId));
        }
        return dto;
    }

    /**
     * Publish event to create order.
     *
     * @param customerId Customer ID.
     * @param salesmanId Salesman ID.
     * @param list List of OrderItems.
     */
    public void createOrder(final long customerId, final long salesmanId, final List<OrderItems> list) {
        final var sequenceId = ringBuffer.next();
        final var event = ringBuffer.get(sequenceId);
        event.setCustomerId(customerId);
        event.setSalesmanId(salesmanId);
        event.setInventories(inventories);
        event.setOrderItems(orderItems);
        event.setOrderItemsList(list);
        event.setOrders(orders);
        event.setProducts(products);
        ringBuffer.publish(sequenceId);
    }

    /**
     * Update status.
     *
     * @param ordersId Key to look up.
     * @param status New status value.
     */
    public void updateStatus(final long ordersId, final String status) {
        // Make sure order exists 
        final var dto = orderExists(ordersId);
        dto.setStatus(status);
        if (logger.isDebugEnabled()) {
            logger.debug("Updating status {}", dto);
        }
        // Update record
        orders.update(dto.getKey(), dto);
    }

    /**
     * Show how you can link child tables easily without composite SQL.
     *
     * @param ordersId Orders ID.
     */
    public void orderInfo(final long ordersId) {
        // Make sure order exists 
        final var ordersDto = orderExists(ordersId);
        logger.debug("Order {}", ordersDto);
        // Get list of order items by key range
        final var orderItemsList = orderItems.findRange(new OrderItemsKey(0L, ordersDto.getOrderId()), new OrderItemsKey(Long.MAX_VALUE,
                ordersDto.getOrderId()));
        logger.debug("Order items {}", orderItemsList);
        // Show product for each order item
        orderItemsList.forEach(items -> {
            final var dto = products.find(new ProductsKey(items.getProductId()));
            logger.debug("itemId {}, Product {}", items.getItemId(), dto);
        });
    }
}
