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
package org.identityconnectors.framework.impl.api.local.operations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.impl.api.ObjectStreamHandler;
import org.identityconnectors.framework.impl.api.StreamHandlerUtil;
import org.identityconnectors.framework.impl.api.local.ThreadClassLoaderManager;

/**
 * Proxy that handles setting up the thread-local classloader as well as
 * restoring it for any callback arguments.
 */
public class ThreadClassLoaderManagerProxy implements InvocationHandler {

    private final ClassLoader bundleClassLoader;
    private final Object target;

    /**
     * Wrapper for object streams such that we restore the classloader to the
     * application classloader when within callback methods.
     */
    private static class ApplicationClassLoaderHandler implements ObjectStreamHandler {
        private final ClassLoader applicationClassLoader;
        private final ObjectStreamHandler target;

        public ApplicationClassLoaderHandler(final ClassLoader applicationClassLoader,
                final ObjectStreamHandler target) {
            Assertions.nullCheck(applicationClassLoader, "applicationClassLoader");
            Assertions.nullCheck(target, "target");
            this.applicationClassLoader = applicationClassLoader;
            this.target = target;
        }

        public boolean handle(final Object object) {
            ThreadClassLoaderManager.getInstance().pushClassLoader(applicationClassLoader);
            try {
                return target.handle(object);
            } finally {
                ThreadClassLoaderManager.getInstance().popClassLoader();
            }
        }
    }

    public ThreadClassLoaderManagerProxy(final ClassLoader bundleClassLoader, final Object target) {
        Assertions.nullCheck(bundleClassLoader, "bundleClassLoader");
        Assertions.nullCheck(target, "target");
        this.bundleClassLoader = bundleClassLoader;
        this.target = target;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] arguments)
            throws Throwable {
        final ClassLoader applicationClassLoader =
                ThreadClassLoaderManager.getInstance().getCurrentClassLoader();
        final Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            final Class<?> paramType = paramTypes[i];
            if (StreamHandlerUtil.isAdaptableToObjectStreamHandler(paramType)
                    && arguments[i] != null) {
                final ObjectStreamHandler rawHandler =
                        StreamHandlerUtil.adaptToObjectStreamHandler(paramType, arguments[i]);
                final ApplicationClassLoaderHandler appHandler =
                        new ApplicationClassLoaderHandler(applicationClassLoader, rawHandler);
                arguments[i] =
                        StreamHandlerUtil.adaptFromObjectStreamHandler(paramTypes[i], appHandler);
            }
        }

        ThreadClassLoaderManager.getInstance().pushClassLoader(bundleClassLoader);
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            ThreadClassLoaderManager.getInstance().popClassLoader();
        }
    }
}
