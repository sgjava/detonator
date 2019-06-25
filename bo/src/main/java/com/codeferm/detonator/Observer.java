/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

/**
 * Type safe Observer interface.
 * <p>
 * @author sgoldsmith
 * @param <ObservedType> Observed type.
 * @param <DataType> Data type.
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Observer<ObservedType, DataType> {

    /**
     * Called by Observable notifyObservers.
     *
     * @param object Object to be observed.
     * @param data Data passed on notify.
     */
    void update(final Observable<ObservedType, DataType> object,
            final DataType data);
}
