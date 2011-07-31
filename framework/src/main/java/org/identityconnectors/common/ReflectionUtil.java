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
package org.identityconnectors.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReflectionUtil {

    /**
     * Never allow this to be instantiated.
     */
    private ReflectionUtil() {
        throw new AssertionError();
    }

    /**
     * Builds a {@link Set} of interfaces from the target class.
     */
    public static Set<Class<?>> getAllInterfaces(final Class<?> target) {
        assert target != null;
        Set<Class<?>> ret = new HashSet<Class<?>>();
        getAllInteralInterfaces(target, ret);
        return ret;
    }

    private static void getAllInteralInterfaces(final Class<?> target,
            final Set<Class<?>> result) {
        // quick exit if target is null..
        if (target != null) {
            // get all the interfaces of the target class..
            for (Class<?> inter : target.getInterfaces()) {
                result.add(inter);
            }
            // get all the interfaces of the super class..
            getAllInteralInterfaces(target.getSuperclass(), result);
        }
    }

    /**
     * Determine if the target class implements the provided interface.
     * 
     * @param target
     *            class to look through for a matching interface.
     * @param clazz
     *            interface class to look for.
     * @return true if a matching interface is found otherwise false.
     */
    public static boolean containsInterface(final Class<?> target,
            final Class<?> clazz) {
        return clazz.isAssignableFrom(target);
    }

    /**
     * Get all interfaces the extends the type provided.
     */
    public static <T> List<Class<? extends T>> getInterfaces(
            final Class<?> target, final Class<T> type) {
        List<Class<? extends T>> ret = new ArrayList<Class<? extends T>>();
        Collection<Class<?>> interfs = getAllInterfaces(target);
        for (Class<?> clazz : interfs) {
            if (containsInterface(clazz, type)) {
                @SuppressWarnings("unchecked")
                Class<? extends T> o = (Class<? extends T>) clazz;
                ret.add(o);
            }
        }
        return ret;
    }
    
    /**
     * Returns true iff the given class overrides equals and hashCode
     * @param clazz The class to check.
     * @return True iff the given class overrides equals and hashCode
     */
    public static boolean overridesEqualsAndHashcode(Class<?> clazz) {
        try {
            Method equals = clazz.getMethod("equals", Object.class);
            if (equals.getDeclaringClass() == Object.class) {
                return false;
            }
            Method hashCode = clazz.getMethod("hashCode");
            if (hashCode.getDeclaringClass() == Object.class) {
                return false;
            }
            return true;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            //this should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the package the class is associated with.
     * 
     * @param clazz
     *            class to inspect for the package.
     * @return package for the class provided.
     * @throws NullPointerException
     *             iff clazz is <code>null</code>.
     */
    public static String getPackage(Class<?> clazz) {
        String name = clazz.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    /**
     * Determine the method name for the calling class.
     */
    public static String getMethodName(int depth) {
        // Hack (?) to get the stack trace.
        Throwable dummyException = new Throwable();
        StackTraceElement locations[] = dummyException.getStackTrace();
        // caller will be the depth element
        String method = "unknown";
        if (locations != null && locations.length > depth) {
            StackTraceElement caller = locations[depth];
            method = caller.getMethodName();
        }
        return method;
    }
}
