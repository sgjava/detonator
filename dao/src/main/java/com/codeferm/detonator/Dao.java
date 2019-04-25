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
 * @param <P> PKO type.
 * @param <D> DTO type.
 */
public interface Dao<P, D> {
    
    public abstract List<D> findAll();
    public abstract D findById(final P pko);
    public abstract void save(final D dto);
    public abstract void delete(final P pko);
    public abstract void update(final P pko, final D dto);
    
}
