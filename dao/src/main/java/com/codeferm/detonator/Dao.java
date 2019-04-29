/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.util.List;

/**
 * DAO interface that simplifies common operations.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <T> DTO type.
 * @param <ID> ID type.
 */
public interface Dao<T, ID> {

    /**
     * Return all records.
     *
     * @return List of all records.
     */
    public abstract List<T> findAll();

    /**
     * Return one record by ID.
     *
     * @param id ID of record to return.
     * @return Single record.
     */
    public abstract T findById(final ID id);

    /**
     * Return List of records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     * @return List of records.
     */
    public abstract List<T> findBy(final String name, final Object[] params);

    /**
     * Save the record.
     *
     * @param dto Record to save.
     */
    public abstract void save(final T dto);

    /**
     * Save List of records.
     *
     * @param list Save List of records.
     */
    public abstract void save(final List<T> list);

    /**
     * Delete the record by ID.
     *
     * @param id ID of record to delete.
     */
    public abstract void delete(final ID id);

    /**
     * Delete records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    public abstract void deleteBy(final String name, final Object[] params);

    /**
     * Update the record.
     *
     * @param dto Updated record.
     * @param id ID of record to update.
     */
    public abstract void update(final T dto, final ID id);

    /**
     * Update records using named query and parameters.
     *
     * @param name Query name.
     * @param params Query parameters,
     */
    public abstract void updateBy(final String name, final Object[] params);
}