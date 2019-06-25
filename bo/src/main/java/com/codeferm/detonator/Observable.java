/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.util.LinkedList;
import java.util.List;

/**
 * Type safe Observable class.
 * <p>
 * @param <ObservedType> Observed type.
 * @param <DataType> Data type.
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class Observable<ObservedType, DataType> {

    /**
     * List of observers.
     */
    private final List<Observer<ObservedType, DataType>> observers = new LinkedList<>();

    /**
     * Add observer to List.
     *
     * @param obs Observer.
     */
    public final synchronized void addObserver(
            final Observer<ObservedType, DataType> obs) {
        if (obs == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        // Only add to list if Observer doesn't exist
        if (!observers.contains(obs)) {
            observers.add(obs);
        }
    }

    /**
     * Notify all observers.
     *
     * @param data Data sent along with observed object.
     */
    public final void notifyObservers(final DataType data) {
        observers.stream().forEach((obs) -> {
            obs.update(this, data);
        });
    }
}
