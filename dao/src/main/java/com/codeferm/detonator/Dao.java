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
     * Save the value.
     *
     * @param value Value to save.
     */
    void save(final V value);

    /**
     * Save Map of key/values.
     *
     * @param map Map of keys and values to save.
     */
    void save(final Map<K, V> map);

    /**
     * Delete the value by key.
     *
     * @param key Key of value to delete.
     */
    void delete(final K key);

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
     * Update map of key/values.
     *
     * @param map Map of keys and values to update.
     */
    void update(final Map<K, V> map);
}
