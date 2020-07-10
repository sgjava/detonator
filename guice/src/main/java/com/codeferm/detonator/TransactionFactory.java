/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import java.lang.reflect.InvocationTargetException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple factory class that returns and Object with methods wrapped in a transaction if the method is annotated with
 * {@link com.codeferm.detonator.Transaction}. The method interceptor null {@link com.codeferm.detonator.TransactionInterceptor},
 * {@link com.codeferm.dbaccess.transaction.QueryRunnerTransInterceptor} or
 * {@link com.codeferm.dbaccess.transaction.JdbcTransInterceptor} is binded with Guice using
 * {@link com.codeferm.dbaccess.transaction.TransactionModule}.
 *
 * @see com.codeferm.detonator.Transaction
 * @see com.codeferm.detonator.TransactionInterceptor
 * @see com.codeferm.detonator.TransactionModule
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class TransactionFactory {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(TransactionFactory.class);

    /**
     * Suppress default constructor for non-instantiability.
     */
    private TransactionFactory() {
        throw new AssertionError();
    }

    /**
     * Create object of type T wrapped in transaction.
     *
     * @param <T> Return type.
     * @param clazz Type of object to create.
     * @param transModule Transaction module must extend AbstractModule.
     * @return Wrapped object.
     */
    public static <T> T createObject(final Class<T> clazz,
            final Class<? extends AbstractModule> transModule) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Creating transactional object for %s", clazz.getName()));
        }
        T object = null;
        try {
            object = Guice.createInjector(transModule.getDeclaredConstructor().newInstance()).getInstance(clazz);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return object;
    }
}
