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
package org.identityconnectors.framework.impl.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * Proxy responsible for logging operations from the API.
 */
public class LoggingProxy implements InvocationHandler {

    private static final Log.Level LOG_LEVEL = Log.Level.OK;

    private static final Log LOG = Log.getLog(LoggingProxy.class);

    private final Object target;
    private final Class<? extends APIOperation> op;

    public LoggingProxy(Class<? extends APIOperation> api, Object target) {
        op = api;
        this.target = target;
    }

    /**
     * {@inheritDoc}
     */
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        // do not log equals, hashCode, toString
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(target, args);
        }
        final String methodName = method.getName();
        if (LOG.isLoggable(LOG_LEVEL)) {
            StringBuilder bld = new StringBuilder();
            bld.append("Enter: ").append(method.getName()).append('(');
            for (int i = 0; args != null && i < args.length; i++) {
                if (i != 0) {
                    bld.append(", ");
                }
                bld.append(args[i]);
            }
            bld.append(')');
            final String msg = bld.toString();
            LOG.log(op, methodName, LOG_LEVEL, msg, null);
        }
        // invoke the method
        try {
            Object ret = method.invoke(target, args);
            if (LOG.isLoggable(LOG_LEVEL)) {
                LOG.log(op, methodName, LOG_LEVEL, "Return: " + ret, null);
            }
            return ret;
        } catch (InvocationTargetException e) {
            Throwable root = e.getCause();

            try {
                LOG.log(op, methodName, LOG_LEVEL, "Exception: ", root);
            } catch (Throwable t) {
                // Ignore. Don't let a failed log prevent this from completing.
            }

            if (root instanceof RuntimeException) {
                throw (RuntimeException) root;
            } else if (root instanceof Exception) {
                throw (Exception) root;
            } else if (root instanceof Error) {
                throw (Error) root;
            } else {
                throw ConnectorException.wrap(root);
            }
        }
    }
}
