/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.     
 * 
 * The contents of this file are subject to the terms of the Common Development 
 * and Distribution License("CDDL") (the "License").  You may not use this file 
 * except in compliance with the License.
 * 
 * You can obtain a copy of the License at 
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.api;

import java.util.Locale;
import java.util.concurrent.Executors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;


/**
 * General-purpose timeout proxy for providing timeouts on
 * all methods on the underlying object. Currently just used for APIOperations,
 * but could wrap any object.
 * NOTE: this is not used for search because search needs timeout on
 * an element by element basis. Moreover, it would be unsafe for search
 * since the thread could continue to return elements after it has timed
 * out and we need to guarantee that not happen.
 */
public class MethodTimeoutProxy implements InvocationHandler {

    /**
     * Get a pool of threads to use for operational timeouts.
     */
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();
    
    /**
     * The underlying operation that we are providing a timeout for
     */
    private final Object _target;
    
    /**
     * The timeout
     */
    private final long _timeoutMillis;
    
    /**
     * Create a new MethodTimeoutProxy
     * @param target The object we are wrapping
     * @param timeoutMillis
     */
    public MethodTimeoutProxy(Object target, 
            long timeoutMillis)
    {
        _target = target;
        _timeoutMillis = timeoutMillis;
    }

    

    
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        
        //do not timeout equals, hashCode, toString
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(_target, args);
        }
        
        final Locale locale = CurrentLocale.get();
        
        Callable<Object> callable = new Callable<Object>() {
            public Object call() throws Exception {
                try {
                    try {
                        //propagate current locale
                        //since this is a thread pool
                        CurrentLocale.set(locale);
                        return method.invoke(_target, args);
                    }
                    finally {
                        CurrentLocale.clear();
                    }
                }
                catch (InvocationTargetException e) {
                    Throwable root = e.getCause();
                    if ( root instanceof RuntimeException ) {
                        throw (RuntimeException)root;
                    }
                    else if ( root instanceof Exception ) {
                        throw (Exception)root;
                    }
                    else if ( root instanceof Error ) {
                        throw (Error)root;
                    }
                    else {
                        throw ConnectorException.wrap(root);
                    }
                }
            }
        };

        try {
            // package in a future task so we can set a timeout..
            FutureTask<Object> t = new FutureTask<Object>(callable);
            // execute it in the thread pool so we don't waste resources.
            THREADPOOL.execute(t);
            // execute and hope it doesn't timeout :)
            return t.get(_timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            throw new OperationTimeoutException(ex);
        }  
        catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }
}
