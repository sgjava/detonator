/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.mapdb.DB;
import org.mapdb.Serializer;

/**
 * Generic MapDB DAO. Some methods cannot be implemented as you would with a RDBMS.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class GenMapDbDao<K, V> implements Dao<K, V> {

    /**
     * MapDB database.
     */
    private final DB db;
    /**
     * MapDB ConcurrentMap to hold keys and values.
     */
    private final ConcurrentMap<K, V> map;
    /**
     * getKey method of value object.
     */
    private Method keyMethod;

    /**
     * Constructor.
     *
     * @param db MapDB DB.
     * @param collectionName Name of collection.
     * @param vClass Value class type.
     */
    public GenMapDbDao(final DB db, final String collectionName, final Class vClass) {
        this.db = db;
        this.map = db.hashMap(collectionName, Serializer.JAVA, Serializer.JAVA).createOrOpen();
        // Get value fields
        final var fields = vClass.getDeclaredFields();
        // Get last field
        final var field = fields[fields.length - 1];
        // Last field should be key if it exists
        if (field.getName().equals("key")) {
            try {
                keyMethod = new PropertyDescriptor(field.getName(), vClass).getReadMethod();
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        } else {
            keyMethod = null;
        }
    }

    /**
     * Get key or null;
     *
     * @param value Value to get key from.
     * @return ID;
     */
    public K getKey(V value) {
        K key = null;
        if (keyMethod != null) {
            try {
                key = (K) keyMethod.invoke(value, (Object[]) null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return key;
    }

   /**
     * Return all values.
     *
     * @return List of all values.
     */
    @Override
    public List<V> findAll() {
        return map.values().stream().collect(Collectors.toList());
    }

    /**
     * Return one value by key.
     *
     * @param key Key of record to return.
     * @return Single record.
     */
    @Override
    public V find(K key) {
        return map.get(key);
    }

    /**
     * Return List of values using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     * @return List of values.
     */
    @Override
    public List<V> findBy(String name, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Save the value.
     *
     * @param value Value to save.
     */
    @Override
    public void save(V value) {
        final K key = getKey(value);
        // Treat this like SQL and throw key violation if key exists
        if (!map.containsKey(key)) {
            map.put(key, value);
        } else {
            throw new RuntimeException(String.format("Key already exists: %s", key));
        }
    }

    /**
     * Save List of values.
     *
     * @param map Map of keys and values to save.
     */
    @Override
    public void save(Map<K, V> map) {
        map.entrySet().forEach(entry -> {
            save(entry.getValue());
        });
    }

    /**
     * Save the value and return generated key.
     *
     * @param value Value to save.
     * @return Generated key.
     */
    @Override
    public K saveReturnKey(V value) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Delete the value by key.
     *
     * @param key Key of value to delete.
     */
    @Override
    public void delete(K key) {
        map.remove(key);
    }

    /**
     * Delete values using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    @Override
    public void deleteBy(String name, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Delete list of values by key.
     *
     * @param list List of keys to delete.
     */
    @Override
    public void delete(List<K> list) {
        list.forEach(id -> {
            delete(id);
        });
    }

    /**
     * Update value by key.
     *
     * @param key Key of value to update.
     * @param value Updated value.
     */
    @Override
    public void update(K key, V value) {
        map.put(key, value);
    }

    /**
     * Update value using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    @Override
    public void updateBy(String name, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Update map of values.
     *
     * @param map Map of keys and values to update.
     */
    @Override
    public void update(Map<K, V> map) {
        map.entrySet().forEach(entry -> {
            update(entry.getKey(), entry.getValue());
        });
    }
}
