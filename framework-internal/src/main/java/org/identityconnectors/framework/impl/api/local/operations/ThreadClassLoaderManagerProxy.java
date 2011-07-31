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
package org.identityconnectors.framework.impl.api.local.operations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.impl.api.ObjectStreamHandler;
import org.identityconnectors.framework.impl.api.StreamHandlerUtil;
import org.identityconnectors.framework.impl.api.local.ThreadClassLoaderManager;

/**
 * Proxy that handles setting up the thread-local classloader
 * as well as restoring it for any callback arguments.
 */
public class ThreadClassLoaderManagerProxy implements InvocationHandler {

    
    private final ClassLoader _bundleClassLoader;
    private final Object _target;
    
    /**
     * Wrapper for object streams such that we restore the
     * classloader to the application classloader when within
     * callback methods.
     */
    private static class ApplicationClassLoaderHandler implements ObjectStreamHandler
    {
        private final ClassLoader _applicationClassLoader;
        private final ObjectStreamHandler _target;
        
        public ApplicationClassLoaderHandler(ClassLoader applicationClassLoader,
                ObjectStreamHandler target) {
            Assertions.nullCheck(applicationClassLoader, "applicationClassLoader");
            Assertions.nullCheck(target, "target");
            _applicationClassLoader = applicationClassLoader;
            _target = target;
        }
        
        public boolean handle(Object object) {
            ThreadClassLoaderManager.getInstance().pushClassLoader(_applicationClassLoader);
            try {
                return _target.handle(object);
            }
            finally {
                ThreadClassLoaderManager.getInstance().popClassLoader();
            }
        }
        
    }
    
    public ThreadClassLoaderManagerProxy(ClassLoader bundleClassLoader,
            Object target) {
        Assertions.nullCheck(bundleClassLoader, "bundleClassLoader");
        Assertions.nullCheck(target, "target");
        _bundleClassLoader = bundleClassLoader;
        _target = target;
    }
    
    
    
    public Object invoke(final Object proxy, final Method method, Object [] arguments) 
    throws Throwable {
        final ClassLoader 
        applicationClassLoader =
            ThreadClassLoaderManager.getInstance().getCurrentClassLoader();
        Class<?> [] paramTypes =
            method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (StreamHandlerUtil.isAdaptableToObjectStreamHandler(paramType) && 
                    arguments[i] != null) {
                ObjectStreamHandler rawHandler =
                    StreamHandlerUtil.adaptToObjectStreamHandler(paramType, 
                            arguments[i]);
                ApplicationClassLoaderHandler appHandler =
                    new ApplicationClassLoaderHandler(applicationClassLoader,rawHandler);
                arguments[i] =
                    StreamHandlerUtil.adaptFromObjectStreamHandler(paramTypes[i], appHandler);
            }            
        }
        
        ThreadClassLoaderManager.getInstance().pushClassLoader(_bundleClassLoader);
        try {
            return method.invoke(_target, arguments);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        finally {
            ThreadClassLoaderManager.getInstance().popClassLoader();            
        }        
    } 
}
