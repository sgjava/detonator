/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Inventories;
import com.codeferm.dto.InventoriesKey;
import com.codeferm.dto.OrderItems;
import com.codeferm.dto.Orders;
import com.codeferm.dto.OrdersKey;
import com.codeferm.dto.ProductsKey;
import com.lmax.disruptor.EventHandler;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Disruptor event handler.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrderEventHandler implements EventHandler<OrderEvent> {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrderEventHandler.class);
    /**
     * Bean validator.
     */
    private final Validator validator;

    /**
     * Default constructor. Initialize validator and lock.
     */
    public OrderEventHandler() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Throws exception if bean validation fails.
     *
     * @param dto DTO to validate.
     */
    public void dtoValid(final Object dto) {
        var violations = validator.validate(dto);
        if (violations.size() > 0) {
            var message = "";
            // Build exception message
            message = violations.stream().map(violation -> String.format("%s.%s %s | ", violation.getRootBeanClass().
                    getSimpleName(), violation.getPropertyPath(), violation.getMessage())).reduce(message, String::concat);
            // Trim last seperator
            message = message.substring(0, message.length() - 3);
            throw new RuntimeException(String.format("Bean violations: %s", message));
        }
    }

    /**
     * Create order using event data and return key.
     *
     * @param event Order event.
     * @return Generated key.
     */
    public OrdersKey createOrder(final OrderEvent event) {
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(event.getCustomerId());
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(event.getSalesmanId());
        dto.setStatus("New");
        if (logger.isDebugEnabled()) {
            logger.debug("Creating {}", dto);
        }
        // Save DTO and return identity key
        var k = event.getOrders().saveReturnKey(dto, new String[]{"ORDER_ID"});
        // Set key in value
        dto.setOrderId(k.getOrderId());
        // Do bean validation after key created and throw exception on validation failure
        dtoValid(dto);
        return k;
    }

    /**
     * Since the customer address does not really allow for selecting by warehouse region we will find first warehouse with
     * inventory.
     *
     * @param event Order event.
     * @param item OrderItems DTO.
     * @return
     */
    public Inventories updateInventory(final OrderEvent event, final OrderItems item) {
        // Get warehouses by product.
        var list = event.getInventories().findRange(new InventoriesKey(item.getProductId(), 0L), new InventoriesKey(item.getProductId(),
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
            // Update quantity
            event.getInventories().update(inv.getKey(), inv);
        } else {
            throw new RuntimeException(String.format("productId %d not in invenroty", item.getProductId()));
        }
        return inv;
    }

    /**
     * Add OrderItems to Orders. OrderItems.itemId must be set prior to calling.
     *
     * @param event Order event.
     * @param k Orders key.
     */
    public void addItems(final OrderEvent event, final OrdersKey k) {
        final List<OrderItems> list = event.getOrderItemsList();
        // Process list of items
        for (final OrderItems item : list) {
            item.setOrderId(k.getOrderId());
            // Search warehouses for product
            final var inv = updateInventory(event, item);
            final var product = event.getProducts().find(new ProductsKey(inv.getProductId()));
            item.setUnitPrice(product.getStandardCost());
            // Add item to order
            event.getOrderItems().update(item.getKey(), item);
        }
    }
    
    @Override
    public void onEvent(final OrderEvent event, final long sequence, final boolean endOfBatch) {
        // Create order and add items
        addItems(event, createOrder(event));
    }
}
