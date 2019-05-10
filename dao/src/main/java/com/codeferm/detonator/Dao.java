/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.util.List;
import java.util.Map;

/**
 * DAO interface that simplifies common operations.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <ID> ID type.
 * @param <T> DTO type.
 */
public interface Dao<ID, T> {

    /**
     * Return all records.
     *
     * @return List of all records.
     */
    List<T> findAll();

    /**
     * Return one record by ID.
     *
     * @param id ID of record to return.
     * @return Single record.
     */
    T findById(final ID id);

    /**
     * Return List of records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     * @return List of records.
     */
    List<T> findBy(final String name, final Object[] params);

    /**
     * Save the record.
     *
     * @param dto Record to save.
     */
    void save(final T dto);

    /**
     * Save List of records.
     *
     * @param map Map of IDs and DTOs to save.
     */
    void save(final Map<ID, T> map);

    /**
     * Save the record and return identity key.
     *
     * @param dto Record to save.
     * @return Generated ID.
     */
    ID saveReturnId(final T dto);

    /**
     * Delete the record by ID.
     *
     * @param id ID of record to delete.
     */
    void delete(final ID id);

    /**
     * Delete records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    void deleteBy(final String name, final Object[] params);
    
    /**
     * Delete list of records.
     *
     * @param list List of IDs to delete.
     */
    void delete(final List<ID> list);

    /**
     * Update the record.
     *
     * @param id ID of record to update.
     * @param dto Updated record.
     */
    void update(final ID id, final T dto);

    /**
     * Update records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    void updateBy(final String name, final Object[] params);
    
    /**
     * Update map of records.
     *
     * @param map Map of IDs and DTOs to update.
     */
    void update(final Map<ID, T> map);
}
