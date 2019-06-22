/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.InventoriesKey;
import com.lmax.disruptor.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Disruptor event handler.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class InventoryEventHandler implements EventHandler<InventoryEvent> {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(InventoryEventHandler.class);

    @Override
    public void onEvent(final InventoryEvent event, final long sequence, final boolean endOfBatch) {
        // Make sure inventory record exists 
        final var dto = event.getInventories().find(new InventoriesKey(event.getProductId(), event.getWarehouseId()));
        final var newQuantity = dto.getQuantity() + event.getQuantity();
        if (newQuantity > 0) {
            dto.setQuantity(newQuantity);
            event.getInventories().update(dto.getKey(), dto);
        } else {
            throw new RuntimeException(String.format("Low inventory: %d, product: %d, warehouse: %d", newQuantity, event.
                    getProductId(), event.getWarehouseId()));
        }
    }
}
