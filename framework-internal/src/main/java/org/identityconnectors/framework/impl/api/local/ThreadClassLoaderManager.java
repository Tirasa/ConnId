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
package org.identityconnectors.framework.impl.api.local;

import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.logging.Log;

/**
 * Provides a for managing the thread-local class loader
 *
 */
public class ThreadClassLoaderManager {

    private static final Log _log = Log.getLog(ThreadClassLoaderManager.class);

    private static ThreadLocal<ThreadClassLoaderManager> _instance
        = new ThreadLocal<ThreadClassLoaderManager>() {
        public ThreadClassLoaderManager initialValue() {
            return new ThreadClassLoaderManager();
        }
    };
    
    private final List<ClassLoader> _loaderStack = 
        new ArrayList<ClassLoader>();
        
    private ThreadClassLoaderManager() {
        
    }
    
    /**
     * Returns the thread-local instance of the manager
     * @return
     */
    public static ThreadClassLoaderManager getInstance() {
        return _instance.get();
    }
    
    /**
     * Sets the given loader as the thread-local classloader.
     * @param loader The class loader. May be null.
     */
    public void pushClassLoader(ClassLoader loader) {
        _loaderStack.add(getCurrentClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
    }
    
    /**
     * Restores the previous loader as the thread-local classloader.
     */
    public void popClassLoader() {
        if (_loaderStack.size() == 0) {
            throw new IllegalStateException("Stack size is 0");
        }
        ClassLoader previous = _loaderStack.remove(_loaderStack.size()-1);
        Thread.currentThread().setContextClassLoader(previous);
    }
    
    /**
     * Hack for OIM. See BundleClassLoader. Pops and returns all class loaders
     * previously pushed, therefore effectively setting the thread's current
     * context class loader to the initial class loader.
     */
    public List<ClassLoader> popAll() {
        List<ClassLoader> rv = new ArrayList<ClassLoader>(_loaderStack);
        while (!_loaderStack.isEmpty()) {
            popClassLoader();
        }
        return rv;
    }

    /**
     * Hack for OIM. See BundleClassLoader. Pushes all class loaders in
     * the list as the context class loader.
     * @param loaders the loaders to push; never null.
     */
    public void pushAll(List<ClassLoader> loaders) {
        for (ClassLoader loader : loaders) {
            pushClassLoader(loader);
        }
    }

    /**
     * Returns the current thread-local class loader
     * @return the current thread-local class loader
     */
    public ClassLoader getCurrentClassLoader() {
        ClassLoader result = Thread.currentThread().getContextClassLoader();
        // Attempt to provide more information to issue 604.
        if (result == null) {
            _log.error(new Throwable(), "The CCL of current thread ''{0}'' is null", Thread.currentThread().getName());
        }
        return result;
    }
    
}
