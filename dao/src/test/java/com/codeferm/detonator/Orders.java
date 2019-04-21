/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

/**
 * Created by DeTOnator on 04-13-2019 10:56:46.
 *
 * select * from orders
 */
public class Orders {

    /**
     * Mapped from database field ORDER_ID, type INTEGER, precision 10, scale 0.
     */
    private Integer orderId;

    /**
     * Mapped from database field CUSTOMER_ID, type DECIMAL, precision 6, scale 0.
     */
    private BigDecimal customerId;

    /**
     * Mapped from database field STATUS, type VARCHAR, precision 20, scale 0.
     */
    private String status;

    /**
     * Mapped from database field SALESMAN_ID, type DECIMAL, precision 6, scale 0.
     */
    private BigDecimal salesmanId;

    /**
     * Mapped from database field ORDER_DATE, type DATE, precision 10, scale 0.
     */
    private Date orderDate;

    /**
     * Default constructor.
     */
    public Orders() {
    }

    /**
     * Accessor for field orderId.
     *
     * @return orderId Get orderId.
     */
    public Integer getOrderId() {
        return orderId;
    }

    /**
     * Mutator for field orderId.
     *
     * @param orderId Set orderId.
     */
    public void setOrderId(final Integer orderId) {
        this.orderId = orderId;
    }

    /**
     * Accessor for field customerId.
     *
     * @return customerId Get customerId.
     */
    public BigDecimal getCustomerId() {
        return customerId;
    }

    /**
     * Mutator for field customerId.
     *
     * @param customerId Set customerId.
     */
    public void setCustomerId(final BigDecimal customerId) {
        this.customerId = customerId;
    }

    /**
     * Accessor for field status.
     *
     * @return status Get status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Mutator for field status.
     *
     * @param status Set status.
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Accessor for field salesmanId.
     *
     * @return salesmanId Get salesmanId.
     */
    public BigDecimal getSalesmanId() {
        return salesmanId;
    }

    /**
     * Mutator for field salesmanId.
     *
     * @param salesmanId Set salesmanId.
     */
    public void setSalesmanId(final BigDecimal salesmanId) {
        this.salesmanId = salesmanId;
    }

    /**
     * Accessor for field orderDate.
     *
     * @return orderDate Get orderDate.
     */
    public Date getOrderDate() {
        return orderDate;
    }

    /**
     * Mutator for field orderDate.
     *
     * @param orderDate Set orderDate.
     */
    public void setOrderDate(final Date orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Equals method.
     *
     * @param o Object to check for equality.
     * @return True if objects equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Orders)) {
            return false;
        }
        Orders obj = (Orders) o;
        return Objects.equals(orderId, obj.orderId) && Objects.equals(customerId, obj.customerId) && Objects.equals(status,
                obj.status) && Objects.equals(salesmanId, obj.salesmanId) && Objects.equals(orderDate, obj.orderDate);
    }

    /**
     * Hash code method.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(orderId, customerId, status, salesmanId, orderDate);
    }

    /**
     * Generated toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "Orders{" + "orderId=" + orderId + ", customerId=" + customerId + ", status=" + status + ", salesmanId=" + salesmanId
                + ", orderDate=" + orderDate + "}";
    }
}
