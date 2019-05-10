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
 * Generic MapDB DAO.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <T> Identity Object.
 * @param <ID> Data Transfer Object.
 */
public class GenMapDbDao<ID, T> implements Dao<ID, T> {

    /**
     * MapDB database.
     */
    private final DB db;
    /**
     * MapDB ConcurrentMap to hold keys and values.
     */
    private final ConcurrentMap<ID, T> map;
    /**
     * getKey method.
     */
    private Method keyMethod;

    /**
     * Constructor.
     *
     * @param db MapDB DB.
     * @param collectionName Name of collection.
     * @param dtoClass DTO class type.
     */
    public GenMapDbDao(final DB db, final String collectionName, final Class dtoClass) {
        this.db = db;
        this.map = db.hashMap(collectionName, Serializer.JAVA, Serializer.JAVA).createOrOpen();
        // Get DTO fields
        final var fields = dtoClass.getDeclaredFields();
        // Get last field
        final var field = fields[fields.length - 1];
        // Last field should be key if it exists
        if (field.getName().equals("key")) {
            try {
                keyMethod = new PropertyDescriptor(field.getName(), dtoClass).getReadMethod();
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        } else {
            keyMethod = null;
        }
    }

    /**
     * Get ID or null;
     *
     * @param dto DTO to get ID from.
     * @return ID;
     */
    public ID getId(T dto) {
        ID id = null;
        if (keyMethod != null) {
            try {
                id = (ID) keyMethod.invoke(dto, (Object[]) null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return id;
    }

    /**
     * Return all records.
     *
     * @return List of all records.
     */
    @Override
    public List<T> findAll() {
        return map.values().stream().collect(Collectors.toList());
    }

    /**
     * Return one record by ID.
     *
     * @param id ID of record to return.
     * @return Single record.
     */
    @Override
    public T findById(ID id) {
        return map.get(id);
    }

    /**
     * Return List of records using named query parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     * @return List of records.
     */
    @Override
    public List<T> findBy(String name, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Save the record.
     *
     * @param dto Record to save.
     */
    @Override
    public void save(T dto) {
        final ID id = getId(dto);
        // Treat this like SQL and throw key violation if key exists
        if (!map.containsKey(id)) {
            map.put(id, dto);
        } else {
            throw new RuntimeException(String.format("ID already exists: %s", id));
        }
    }

    /**
     * Save Map of records using batch operation. Note for RDBMS implementation ID is not used.
     *
     * @param map Map of IDs and DTOs to save.
     */
    @Override
    public void save(Map<ID, T> map) {
        map.entrySet().forEach(entry -> {
            save(entry.getValue());
        });
    }

    /**
     * Save the record and return identity key.
     *
     * @param dto Record to save.
     * @return Generated ID.
     */
    @Override
    public ID saveReturnId(T dto) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Delete the record by ID.
     *
     * @param id ID of record to delete.
     */
    @Override
    public void delete(ID id) {
        map.remove(id);
    }

    /**
     * Delete records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    @Override
    public void deleteBy(String name, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Delete list of records.
     *
     * @param list List of IDs to delete.
     */
    @Override
    public void delete(List<ID> list) {
        list.forEach(id -> {
            delete(id);
        });
    }

    /**
     * Update the record.
     *
     * @param id ID of record to update.
     * @param dto Updated record.
     */
    @Override
    public void update(ID id, T dto) {
        map.put(id, dto);
    }

    /**
     * Update records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    @Override
    public void updateBy(String name, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Update map of records.
     *
     * @param map Map of DTOs and IDs to update.
     */
    @Override
    public void update(Map<ID, T> map) {
        map.entrySet().forEach(entry -> {
            update(entry.getKey(), entry.getValue());
        });
    }

}
