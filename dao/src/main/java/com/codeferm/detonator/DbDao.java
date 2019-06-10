/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.util.List;

/**
 * Database DAO interface that allows named queries, etc that Dao doesn't allow for. Trying to keep Dao K/V compatible.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface DbDao<K, V> extends Dao<K, V> {

    /**
     * Return List of values using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     * @return List of values.
     */
    List<V> findBy(final String name, final Object[] params);

    /**
     * Delete values using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    void deleteBy(final String name, final Object[] params);

    /**
     * Update value using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    void updateBy(final String name, final Object[] params);
}
