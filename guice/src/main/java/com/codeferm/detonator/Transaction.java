/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Runtime annotation for method level transaction.
 *
 * @see com.codeferm.detonator.TransactionInterceptor
 * @see com.codeferm.detonator.TransactionModule
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Transaction {
}
