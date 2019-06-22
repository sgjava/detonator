/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;

/**
 * Disruptor event to change quantity in inventories table.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class InventoryEvent {

    /**
     * Mapped from database field PRODUCT_ID, type BIGINT, precision 19, scale 0, PK sequence 1.
     */
    private Long productId;

    /**
     * Mapped from database field QUANTITY, type DECIMAL, precision 8, scale 0.
     */
    private Integer quantity;

    /**
     * Mapped from database field WAREHOUSE_ID, type BIGINT, precision 19, scale 0, PK sequence 2.
     */
    private Long warehouseId;

    /**
     * Inventories DAO.
     */
    private Dao<InventoriesKey, Inventories> inventories;

    /**
     * Default constructor.
     */
    public InventoryEvent() {
    }

    /**
     * Accessor for field productId.
     *
     * @return productId Get productId.
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * Mutator for field productId.
     *
     * @param productId Set productId.
     */
    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    /**
     * Accessor for field quantity.
     *
     * @return quantity Get quantity.
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Mutator for field quantity.
     *
     * @param quantity Set quantity.
     */
    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * Accessor for field warehouseId.
     *
     * @return warehouseId Get warehouseId.
     */
    public Long getWarehouseId() {
        return warehouseId;
    }

    /**
     * Mutator for field warehouseId.
     *
     * @param warehouseId Set warehouseId.
     */
    public void setWarehouseId(final Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Dao<InventoriesKey, Inventories> getInventories() {
        return inventories;
    }

    public void setInventories(Dao<InventoriesKey, Inventories> inventories) {
        this.inventories = inventories;
    }

    /**
     * toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "Inventories{" + "productId=" + productId + ", quantity=" + quantity + ", warehouseId=" + warehouseId
                + ", inventories=" + inventories + "}";
    }
}
