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
import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;
import javax.validation.Validation;
import javax.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Plain Java business object that only needs DAOs set from calling class. This way you can use DataSource or XADataSource. You can
 * also use transactions in your calling class to handle automatic rollback on exception. Bean validation is built in if your DTOs
 * are decorated with javax.validation.constraints.* annotations.
 *
 * This class should be considered thread safe since the Validator and DAOs are thread safe.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrdersBo {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(OrdersBo.class);
    /**
     * Bean validator.
     */
    private final Validator validator;
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
     * Lock for inventories update.
     */
    private final ReentrantLock lock;

    /**
     * Default constructor. Initialize validator and lock.
     */
    public OrdersBo() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.lock = new ReentrantLock();
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
     * Throw exception if product and warehouse doesn't exist or return dto if it does.
     *
     * @param productId
     * @param warehouseId
     * @return
     */
    public Inventories productExists(final Long productId, final Long warehouseId) {
        // Make sure order exists 
        final var dto = inventories.find(new InventoriesKey(productId, warehouseId));
        if (dto == null) {
            throw new RuntimeException(String.format("productId %d : warehouseId %d not found", productId, warehouseId));
        }
        return dto;
    }

    /**
     * Update inventory in thread safe manner. This could become a bottleneck depending on the persistence latency.
     *
     * @param productId Product ID.
     * @param warehouseId Warehouse ID.
     * @param quantity Quantity add, but you can use a negative number to subtract quantity.
     */
    public void updateInventory(final Long productId, final Long warehouseId, final Integer quantity) {
        lock.lock();
        try {
            final var dto = productExists(productId, warehouseId);
            final var newQuantity = dto.getQuantity() + quantity;
            // Must have > 0 products left in inventory
            if (newQuantity > 0) {
                dto.setQuantity(newQuantity);
                inventories.update(dto.getKey(), dto);
            } else {
                throw new RuntimeException(String.format("Low inventory: %d, product: %d, warehouse: %d", newQuantity, productId,
                        warehouseId));
            }
        } finally {
            lock.unlock();
        }
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
     * Create new order.
     *
     * @param customerId Customer ID.
     * @param salesmanId Salesman ID.
     * @return Generated key.
     */
    public OrdersKey createOrder(final long customerId, final long salesmanId) {
        // Create DTO to save (note we skip setting orderId since it's an identity field and will be auto generated)
        final var dto = new Orders();
        dto.setCustomerId(customerId);
        dto.setOrderDate(Date.valueOf(LocalDate.now()));
        dto.setSalesmanId(salesmanId);
        dto.setStatus("Pending");
        if (logger.isDebugEnabled()) {
            logger.debug("Creating {}", dto);
        }
        // Save DTO and return identity key
        var k = orders.saveReturnKey(dto, new String[]{"ORDER_ID"});
        // Set key in value
        dto.setOrderId(k.getOrderId());
        // Do bean validation after key created and throw exception on validation failure
        dtoValid(dto);
        return k;
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
