/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Inventories;
import com.codeferm.dto.OrderItems;

/**
 * Update inventory based on OrderItems.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public interface UpdateInventory {

    /**
     * Update quantity based on OrderItems.
     *
     * @param item OrderItems used for update.
     * @return Inventories DTO.
     */
    public Inventories update(final OrderItems item);

}
