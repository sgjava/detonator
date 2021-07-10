/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import java.lang.reflect.Method;
import javax.transaction.UserTransaction;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Intercept methods annotated with {@link com.codeferm.detonator.Transaction} and commit on success or rollback if
 * {@link java.lang.Exception} is thrown.
 * <p>
 * <p>
 * Currently this class supports {@link jakarta.transaction.UserTransaction} and Narayana {@code UserTransactionImp} for the
 * implementation. Other JTA implementations could be leveraged as well.
 * <p>
 * <p>
 * Limitations
 * <p>
 * Behind the scenes, method interception is implemented by generating bytecode at runtime. Guice dynamically creates a subclass
 * that applies interceptors by overriding methods. If you are on a platform that doesn't support bytecode generation (such as
 * Android), you should use Guice without AOP support.
 * <p>
 * This approach imposes limits on what classes and methods can be intercepted:
 * <p>
 * <p>
 * Classes must be public or package-private. Classes must be non-final Methods must be public, package-private or protected Methods
 * must be non-final Instances must be created by Guice by an @Inject-annotated or no-argument constructor
 * <p>
 * <p>
 * It is not possible to use method interception on instances that aren't constructed by Guice.
 *
 * @see com.codeferm.detonator.Transaction
 * @see com.codeferm.detonator.TransactionModule
 * @see com.codeferm.detonator.TransactionFactory
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class TransactionInterceptor implements MethodInterceptor {

    /**
     * Logger.
     */
    private final Logger logger = LogManager.getLogger(TransactionInterceptor.class);

    /**
     * Invoke method wrapped in a transaction.
     *
     * @param invocation Method invocation.
     * @return Invoked Object.
     * @throws Throwable Possible exception.
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Method method = invocation.getMethod();
        final Transaction annotation = method.getAnnotation(Transaction.class);
        Object object = null;
        // Make sure interceptor was called for a @Transaction method
        if (annotation == null) {
            // Not a Transaction annotated method, so proceed without change
            object = invocation.proceed();

        } else {
            final UserTransaction userTransaction = new UserTransactionImple();
            // Begin transaction
            userTransaction.begin();
            try {
                // Proceed with the original method's invocation
                object = invocation.proceed();
                // Commit if successful
                userTransaction.commit();
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Committed transaction for method %s", invocation.getMethod().getName()));
                }
            } catch (Exception e) {
                // Rollback on Exception
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Rollback transaction for method %s", invocation.getMethod().getName()));
                }
                userTransaction.rollback();
                throw e;
            } finally {
                //userTransaction.close();
            }
        }
        return object;
    }
}
