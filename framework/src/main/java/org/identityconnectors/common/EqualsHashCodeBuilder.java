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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Builder to simplify implementing the {@link Object#equals(Object)} and
 * {@link Object#hashCode()} methods. This class uses {@link ArrayList}'s
 * implementation of {@link ArrayList#equals(Object)} and
 * {@link ArrayList#hashCode()} and takes special care with deep arrays and
 * {@link Collection} based objects.
 */
public final class EqualsHashCodeBuilder {
    private final List<Object> _members;

    /**
     * Construct the builder.
     */
    public EqualsHashCodeBuilder() {
        _members = new ArrayList<Object>();
    }

    /**
     * Appends the field value to an ArrayList to help facilitate equality
     * testing.
     * 
     * @throws IllegalArgumentException
     *             iff a collection is passed since collections do <b>not</b>
     *             support value based equality. Sets, Lists, and Maps will work
     *             since they support value based equality.
     */
    public EqualsHashCodeBuilder append(final Object object) {
        if (object != null && object.getClass().isArray()) {
            // change the array to a builder to support value based equality
            // from arrays.. use equals/hashcode builders as protection..
            // this is also necessary for nested arrays..
            EqualsHashCodeBuilder bld = new EqualsHashCodeBuilder();
            if (object instanceof long[]) {
                for (long l : (long[]) object) {
                    bld.append(l);
                }
            } else if (object instanceof int[]) {
                for (int i : (int[]) object) {
                    bld.append(i);
                }
            } else if (object instanceof short[]) {
                for (short o : (short[]) object) {
                    bld.append(o);
                }
            } else if (object instanceof char[]) {
                for (char o : (char[]) object) {
                    bld.append(o);
                }
            } else if (object instanceof byte[]) {
                for (byte o : (byte[]) object) {
                    bld.append(o);
                }
            } else if (object instanceof double[]) {
                for (double o : (double[]) object) {
                    bld.append(o);
                }
            } else if (object instanceof float[]) {
                for (float o : (float[]) object) {
                    bld.append(o);
                }
            } else if (object instanceof boolean[]) {
                for (boolean o : (boolean[]) object) {
                    bld.append(o);
                }
            } else {
                // Not an array of primitives
                for (Object o : (Object[]) object) {
                    bld.append(o);
                }
            }
            _members.add(bld);
        } else if (object instanceof Set || object instanceof List) {
            // sets and lists are okay because they are value based equality
            _members.add(object);
        } else if (object instanceof Collection) {
            // collections only support identity based equality..
            final String ERR = "Collections are not accepted!";
            throw new IllegalArgumentException(ERR);
        } else {
            // this is just a regular object..
            _members.add(object);
        }
        return this;
    }

    /**
     * This method will attempt to use reflection to get all the properties that
     * make up the identity of the object. It will {@link #append(Object)} all
     * results from <strong>get</strong> methods that have a corresponding
     * <strong>set</strong> method.
     */
    public void appendBean(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object must not be null!");
        }
        // get all the methods..
        Class<?> clazz = obj.getClass();
        for (Method m : clazz.getMethods()) {
            // determine if its public..
            if (m.getModifiers() != Modifier.PUBLIC) {
                continue;
            }
            // it needs to return something..
            if (m.getReturnType() == Void.class) {
                continue;
            }
            // determine if the name starts w/ get[A-Z]..
            String name = m.getName();
            if (!name.startsWith("get")
                    || Character.isLowerCase(name.charAt(3))) {
                continue;
            }
            // determine if there's a corresponding set..
            try {
                // so it should be set<Bean>(get's return type){}
                clazz.getMethod("s" + name.substring(1), m.getReturnType());
            } catch (NoSuchMethodException e) {
                continue;
            }
            try {
                // attempt to append the value from the method..
                append(m.invoke(obj, new Object[] {}));
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Determine equality based on the value of the members append to the
     * builder.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof EqualsHashCodeBuilder) {
            EqualsHashCodeBuilder bld = (EqualsHashCodeBuilder) obj;
            ret = _members.equals(bld._members);
        }
        return ret;
    }

    /**
     * Determine the hashcode based on the various members.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return _members.hashCode();
    }

    /**
     * Show the contents that make up the key.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return _members.toString();
    }
}
