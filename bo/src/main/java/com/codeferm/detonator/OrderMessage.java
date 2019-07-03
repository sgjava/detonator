/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.OrderItems;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Message used to create order.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrderMessage implements Serializable {

    /**
     * Mapped from database field CUSTOMER_ID, type BIGINT, precision 19, scale 0.
     */
    private Long customerId;
    /**
     * Mapped from database field SALESMAN_ID, type BIGINT, precision 19, scale 0.
     */
    private Long salesmanId;
    /**
     * Order items to add to order.
     */
    private List<OrderItems> orderItemsList;

    /**
     * Default constructor. Initialize validator.
     */
    public OrderMessage() {
    }

    /**
     * Accessor for field customerId.
     *
     * @return customerId Get customerId.
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * Mutator for field customerId.
     *
     * @param customerId Set customerId.
     */
    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Accessor for field salesmanId.
     *
     * @return salesmanId Get salesmanId.
     */
    public Long getSalesmanId() {
        return salesmanId;
    }

    /**
     * Mutator for field salesmanId.
     *
     * @param salesmanId Set salesmanId.
     */
    public void setSalesmanId(final Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    /**
     * Accessor for field orderItemsList.
     *
     * @return orderItemsList Get orderItemsList.
     */
    public List<OrderItems> getOrderItemsList() {
        return orderItemsList;
    }

    /**
     * Mutator for field orderItemsList.
     *
     * @param orderItemsList Set orderItemsList.
     */
    public void setOrderItemsList(final List<OrderItems> orderItemsList) {
        this.orderItemsList = orderItemsList;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.customerId);
        hash = 71 * hash + Objects.hashCode(this.salesmanId);
        hash = 71 * hash + Objects.hashCode(this.orderItemsList);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderMessage other = (OrderMessage) obj;
        if (!Objects.equals(this.customerId, other.customerId)) {
            return false;
        }
        if (!Objects.equals(this.salesmanId, other.salesmanId)) {
            return false;
        }
        if (!Objects.equals(this.orderItemsList, other.orderItemsList)) {
            return false;
        }
        return true;
    }

    /**
     * toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "OrderMessage{" + "customerId=" + customerId + ", salesmanId=" + salesmanId + ", orderItemsList=" + orderItemsList
                + '}';
    }
}
