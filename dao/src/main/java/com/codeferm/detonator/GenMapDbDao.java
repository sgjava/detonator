/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Dto;
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
     * MapDB atomic Long use for primary key.
     */
    private final Atomic.Long keyInc;
    /**
     * Used for keys with single value.
     */
    private Method writeMethod;

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
        final var atomicKey = String.format("%s_key", collName);
        // See if key exists
        if (db.exists(atomicKey)) {
            // This should be created already.
            keyInc = db.atomicLong(atomicKey).open();
        } else {
            keyInc = null;
        }
        try {
            // Get key vFields
            final var kFields = kClass.getDeclaredFields();
            // Key write method method
            writeMethod = new PropertyDescriptor(kFields[0].getName(), vClass).getWriteMethod();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
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
        final Map<K, V> subMap = ((BTreeMap) map).subMap(fromKey, toKey);
        return new ArrayList(subMap.values());
    }

    /**
     * Save the value.
     *
     * @param value Value to save.
     */
    @Override
    public void save(final V value) {
        final K k = ((Dto) value).getKey();
        // Treat this like SQL DB and throw key violation if key exists
        if (!map.containsKey(k)) {
            map.put(k, value);
        } else {
            throw new RuntimeException(String.format("Key already exists: %s", k));
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
        try {
            // Write next atomic value to key field
            writeMethod.invoke(value, keyInc.incrementAndGet());
            // Save in map
            map.put(((Dto) value).getKey(), value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return ((Dto) value).getKey();
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
