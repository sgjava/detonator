/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * Binds {@link com.codeferm.dbaccess.transaction.TransactionInterceptor} in the Guice module for methods annotated with
 * {@link com.codeferm.detonator.Transaction}.
 *
 * @see com.codeferm.detonator.Transaction
 * @see com.codeferm.detonator.TransactionInterceptorNew
 * @see com.codeferm.detonator.TransactionFactory
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class TransactionModule extends AbstractModule {

    /**
     * Inject transaction interceptor annotated with Transaction.
     */
    @Override
    protected void configure() {
        final TransactionInterceptor interceptor = new TransactionInterceptor();
        // Request injection of TransactionInterceptor
        requestInjection(interceptor);
        // Match all methods of class with @Transaction annotation
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transaction.class), interceptor);
    }
}
