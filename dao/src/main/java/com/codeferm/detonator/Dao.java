/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.util.List;
import java.util.Map;

/**
 * DAO interface that simplifies common operations using key and value paradigm.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface Dao<K, V> {

    /**
     * Return all values.
     *
     * @return List of all values.
     */
    List<V> findAll();

    /**
     * Return one value by key.
     *
     * @param key Key of record to return.
     * @return Single record.
     */
    V find(final K key);

    /**
     * Return List of values using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     * @return List of values.
     */
    List<V> findBy(final String name, final Object[] params);

    /**
     * Save the value.
     *
     * @param value Value to save.
     */
    void save(final V value);

    /**
     * Save List of values.
     *
     * @param map Map of keys and values to save.
     */
    void save(final Map<K, V> map);

    /**
     * Save the value and return generated key.
     *
     * @param value Value to save.
     * @param keyNames Array of key column names.
     * @return Generated key.
     */
    K saveReturnKey(final V value, final String[] keyNames);

    /**
     * Delete the value by key.
     *
     * @param key Key of value to delete.
     */
    void delete(final K key);

    /**
     * Delete values using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    void deleteBy(final String name, final Object[] params);

    /**
     * Delete list of values by key.
     *
     * @param list List of keys to delete.
     */
    void delete(final List<K> list);

    /**
     * Update value by key.
     *
     * @param key Key of value to update.
     * @param value Updated value.
     */
    void update(final K key, final V value);

    /**
     * Update value using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    void updateBy(final String name, final Object[] params);

    /**
     * Update map of values.
     *
     * @param map Map of keys and values to update.
     */
    void update(final Map<K, V> map);
}
