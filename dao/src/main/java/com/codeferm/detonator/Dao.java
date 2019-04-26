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
    
    public abstract List<T> findAll();
    public abstract T findById(final ID id);
    public abstract void save(final T dto);
    public abstract void delete(final ID id);
    public abstract void update(final T dto, final ID id);
}
