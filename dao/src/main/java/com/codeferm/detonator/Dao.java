/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.util.List;

/**
 * DAO interface that simplifies common CRUD operations.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 * @param <I> ID type.
 * @param <D> DTO type.
 */
public interface Dao<I, D> {
    
    public abstract List<D> findAll();
    public abstract D findById(final I id);
    public abstract void save(final D dto);
    public abstract void delete(final I id);
    public abstract void update(final I id, final D dto);
}
