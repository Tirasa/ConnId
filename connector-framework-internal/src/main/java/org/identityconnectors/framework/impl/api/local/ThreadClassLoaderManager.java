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
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.api.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.identityconnectors.common.logging.Log;

/**
 * Provides a for managing the thread-local class loader
 *
 */
public final class ThreadClassLoaderManager {

    private static final Log LOG = Log.getLog(ThreadClassLoaderManager.class);

    private static final ThreadLocal<ThreadClassLoaderManager> INSTANCE =
            new ThreadLocal<ThreadClassLoaderManager>() {

                @Override
                public ThreadClassLoaderManager initialValue() {
                    return new ThreadClassLoaderManager();
                }
            };

    private final Stack<ClassLoader> loaderStack = new Stack<ClassLoader>();

    private ThreadClassLoaderManager() {
        // empty constructor for singleton class
    }

    /**
     * Returns the thread-local instance of the manager
     *
     * @return the thread-local instance of the manager
     */
    public static ThreadClassLoaderManager getInstance() {
        return INSTANCE.get();
    }

    /**
     * Clear the thread-local instance of the manager.
     */
    public static void clearInstance() {
        INSTANCE.remove();
    }

    /**
     * Sets the given loader as the thread-local classloader.
     *
     * @param loader
     *            The class loader. May be null.
     */
    public void pushClassLoader(final ClassLoader loader) {
        loaderStack.push(getCurrentClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
    }

    /**
     * Restores the previous loader as the thread-local classloader.
     */
    public void popClassLoader() {
        if (loaderStack.isEmpty()) {
            throw new IllegalStateException("Stack size is 0");
        }
        final ClassLoader previous = loaderStack.pop();
        Thread.currentThread().setContextClassLoader(previous);
    }

    /**
     * Hack for OIM. See BundleClassLoader. Pops and returns all class loaders
     * previously pushed, therefore effectively setting the thread's current
     * context class loader to the initial class loader.
     */
    public List<ClassLoader> popAll() {
        final List<ClassLoader> rv = new ArrayList<ClassLoader>(loaderStack);
        while (!loaderStack.isEmpty()) {
            popClassLoader();
        }
        return rv;
    }

    /**
     * Hack for OIM. See BundleClassLoader. Pushes all class loaders in the list
     * as the context class loader.
     *
     * @param loaders
     *            the loaders to push; never null.
     */
    public void pushAll(final List<ClassLoader> loaders) {
        for (ClassLoader loader : loaders) {
            pushClassLoader(loader);
        }
    }

    /**
     * Returns the current thread-local class loader
     *
     * @return the current thread-local class loader
     */
    public ClassLoader getCurrentClassLoader() {
        final ClassLoader result = Thread.currentThread().getContextClassLoader();
        // Attempt to provide more information to issue 604.
        if (result == null) {
            LOG.error(new Throwable(), "The CCL of current thread ''{0}'' is null", Thread
                    .currentThread().getName());
        }
        return result;
    }
}
