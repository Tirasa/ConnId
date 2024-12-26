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
 * Portions Copyrighted 2011 ConnId.
 */
package net.tirasa.connid.commons.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a Test helper class for testing expected method calls and return values of interfaces
 * <p>
 * Limitation:</p><p>
 * First implementation supports just a method name checking</p>
 *
 * @version $Revision 1.0$
 * @param <T> Type of the interface for testing
 * @since 1.0
 */
public class ExpectProxy<T> implements InvocationHandler {

    private final List<String> methodNames = new ArrayList<String>();

    private final List<Object> retVals = new ArrayList<Object>();

    private int count = 0;

    /**
     * Program the expected function call
     *
     * @param methodName the expected method name
     * @param retVal the expected return value or proxy
     * @return the proxy
     */
    public ExpectProxy<T> expectAndReturn(
            final String methodName, final Object retVal) {
        this.methodNames.add(methodName);
        this.retVals.add(retVal);
        return this;
    }

    /**
     * Program the expected method call
     *
     * @param methodName the expected method name
     * @return the proxy
     */
    public ExpectProxy<T> expect(final String methodName) {
        this.methodNames.add(methodName);
        //retVals must have same number of values as methodNames
        this.retVals.add(null);
        return this;
    }

    /**
     * Program the expected method call
     *
     * @param methodName the expected method name
     * @param throwEx the expected exception
     * @return the proxy
     */
    public ExpectProxy<T> expectAndThrow(
            final String methodName, final Throwable throwEx) {
        return this.expectAndReturn(methodName, throwEx);
    }

    /**
     * Test that all expected was called in the order
     *
     * @return true/false all was called
     */
    public boolean isDone() {
        return count == methodNames.size();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String ext = "";
        if (methodNames.size() > count) {
            final String mname = this.methodNames.get(count);
            ext = " The expected call no: " + (count + 1) + " was " + mname + ".";
            if (method.getName().equals(mname)) {
                final Object ret = retVals.get(count++);
                if (ret instanceof Throwable) {
                    throw (Throwable) ret;
                }
                return ret;
            }
        }
        throw new AssertionError(
                "The call of method :" + method + " was not expected." + ext
                + " Please call expectAndReturn(methodName,retVal) to fix it");
    }

    /**
     * Return the Proxy implementation of the Interface
     *
     * @param clazz of the interface
     * @return the proxy
     */
    @SuppressWarnings("unchecked")
    public T getProxy(Class<T> clazz) {
        ClassLoader cl = getClass().getClassLoader();
        Class<?> intef[] = new Class<?>[] { clazz };
        return (T) Proxy.newProxyInstance(cl, intef, this);
    }
}
