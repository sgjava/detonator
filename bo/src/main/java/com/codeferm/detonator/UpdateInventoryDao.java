/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;
import com.codeferm.dto.OrderItems;
import com.codeferm.dto.OrderItemsKey;

/**
 * UpdateInventoryDao is not thread safe because it updates the inventory. Use a single threaded queue to handle updates.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class UpdateInventoryDao implements UpdateInventory {

    /**
     * OrderItems DAO.
     */
    private Dao<OrderItemsKey, OrderItems> orderItems;
    /**
     * Inventories DAO.
     */
    private Dao<InventoriesKey, Inventories> inventories;
    /**
     * Validation bean.
     */
    private final ValidateBean validateBean;

    /**
     * Construct with ValidateBean.
     */
    public UpdateInventoryDao() {
        validateBean = new ValidateBean();
    }

    /**
     * Construct with OrderItems and Inventories DAO.
     *
     * @param orderItems OrderItems DAO.
     * @param inventories Inventories DAO.
     */
    public UpdateInventoryDao(final Dao<OrderItemsKey, OrderItems> orderItems, final Dao<InventoriesKey, Inventories> inventories) {
        validateBean = new ValidateBean();
        this.orderItems = orderItems;
        this.inventories = inventories;
    }

    public Dao<OrderItemsKey, OrderItems> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Dao<OrderItemsKey, OrderItems> orderItems) {
        this.orderItems = orderItems;
    }

    public Dao<InventoriesKey, Inventories> getInventories() {
        return inventories;
    }

    public void setInventories(Dao<InventoriesKey, Inventories> inventories) {
        this.inventories = inventories;
    }

    /**
     * Since the customer address does not really allow for selecting by warehouse region we will find first warehouse with
     * inventory.
     *
     * @param item OrderItems DTO.
     * @return
     */
    @Override
    public Inventories update(final OrderItems item) {
        // Get warehouses by product.
        var list = inventories.findRange(new InventoriesKey(item.getProductId(), 0L), new InventoriesKey(item.getProductId(),
                Long.MAX_VALUE));
        // See if we get any hits
        if (list == null) {
            throw new RuntimeException(String.format("productId %d not found", item.getProductId()));
        }
        int i = 0;
        // Rifle through list and see if any warehouse has product
        while (i < list.size() && list.get(i).getQuantity() < item.getQuantity()) {
            i++;
        }
        Inventories inv = null;
        // There's enough quantity?
        if (i < list.size()) {
            inv = list.get(i);
            // Remove item quantity from inventory
            inv.setQuantity(inv.getQuantity() - item.getQuantity());
            // Save quantity update
            inventories.update(inv.getKey(), inv);
        } else {
            throw new RuntimeException(String.format("productId %d not in invenroty", item.getProductId()));
        }
        return inv;
    }
}
