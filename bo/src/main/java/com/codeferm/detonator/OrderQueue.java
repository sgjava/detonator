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

/**
 * CreateOrder is not thread safe because it updates the inventory. This Interface is for a single threaded queue to process orders.
 * You can use this as a client or client/server.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public interface OrderQueue {

    void createOrder(final OrderMessage orderMessage);

    Dao<InventoriesKey, Inventories> getInventories();

    Dao<OrderItemsKey, OrderItems> getOrderItems();

    Dao<OrdersKey, Orders> getOrders();

    Dao<ProductsKey, Products> getProducts();

    void setInventories(Dao<InventoriesKey, Inventories> inventories);

    void setOrderItems(Dao<OrderItemsKey, OrderItems> orderItems);

    void setOrders(Dao<OrdersKey, Orders> orders);

    void setProducts(Dao<ProductsKey, Products> products);

    /**
     * Wait for queued threads to finish.
     */
    void shutdown();
    
}
