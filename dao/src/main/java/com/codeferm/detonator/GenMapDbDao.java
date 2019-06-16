/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.mapdb.Atomic;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;

/**
 * Generic MapDB DAO. Some methods cannot be implemented as you would with a RDBMS, thus it's a basic Dao.
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
     * Value class type.
     */
    private final Class vClass;
    /**
     * Key class type.
     */
    private final Class kClass;
    /**
     * getKey method of value object.
     */
    private final Method keyReadMethod;
    /**
     * Used for keys with single value.
     */
    private Method writeMethod;
    /**
     * MapDB atomic Long use for primary key.
     */
    private final Atomic.Long keyInc;

    /**
     * Constructor.
     *
     * @param db MapDB DB.
     * @param collName Name of collection.
     * @param kClass Key class type.
     * @param vClass Value class type.
     */
    public GenMapDbDao(final DB db, final String collName, final Class kClass, final Class vClass) {
        this.kClass = kClass;
        this.vClass = vClass;
        this.db = db;
        this.map = db.treeMap(collName, Serializer.JAVA, Serializer.JAVA).createOrOpen();
        // This should be created already.
        keyInc = db.atomicLong(String.format("%s_key", collName)).open();
        // Get value vFields
        final var vFields = vClass.getDeclaredFields();
        // Get last kField
        final var vField = vFields[vFields.length - 1];
        // Last kField should be key if it exists
        if (vField.getName().equals("key")) {
            try {
                // Value key read method
                keyReadMethod = new PropertyDescriptor(vField.getName(), vClass).getReadMethod();
                final var kFields = kClass.getDeclaredFields();
                // Key write method method
                writeMethod = new PropertyDescriptor(kFields[0].getName(), kClass).getWriteMethod();
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        } else {
            keyReadMethod = null;
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
        if (keyReadMethod != null) {
            try {
                key = (K) keyReadMethod.invoke(value, (Object[]) null);
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
     * Return range of values using from and to keys inclusive.
     *
     * @param fromKey Search from.
     * @param toKey Search to.
     * @return List of values.
     */
    @Override
    public List<V> findRange(final K fromKey, final K toKey) {
        // Return map of values within range
        final Map<K, V> subMap = ((BTreeMap ) map).subMap(fromKey, toKey);
        return new ArrayList(subMap.values());
    }

    /**
     * Save the value.
     *
     * @param value Value to save.
     */
    @Override
    public void save(final V value) {
        final K key = getKey(value);
        // Treat this like SQL DB and throw key violation if key exists
        if (!map.containsKey(key)) {
            map.put(key, value);
        } else {
            throw new RuntimeException(String.format("Key already exists: %s", key));
        }
    }

    /**
     * Save the value and return generated key. Only Long single field types are supported. Atomic.Long is used to generate the key
     * value. This will be preserved across restarts if you use DBMaker.fileDB.
     *
     * @param value Value to save is ignored for MapDB.
     * @return Generated key.
     */
    @Override
    public K saveReturnKey(final V value, final String[] keyNames) {
        // Get key from value
        var k = getKey(value);
        try {
            // Write next atomic value to key field
            writeMethod.invoke(k, keyInc.incrementAndGet());
            // Save in map
            map.put(k, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return k;
    }

    /**
     * Save map of values.
     *
     * @param map Map of keys and values to save.
     */
    @Override
    public void save(final Map<K, V> map) {
        map.entrySet().forEach(entry -> {
            save(entry.getValue());
        });
    }

    /**
     * Delete the value by key.
     *
     * @param key Key of value to delete.
     */
    @Override
    public void delete(final K key) {
        map.remove(key);
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
    public void update(final K key, final V value) {
        map.put(key, value);
    }

    /**
     * Update map of values.
     *
     * @param map Map of keys and values to update.
     */
    @Override
    public void update(final Map<K, V> map) {
        map.entrySet().forEach(entry -> {
            update(entry.getKey(), entry.getValue());
        });
    }
}
