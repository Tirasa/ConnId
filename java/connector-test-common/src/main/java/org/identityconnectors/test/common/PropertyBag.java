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
 * Portions Copyrighted 2014 ForgeRock AS.
 */
package org.identityconnectors.test.common;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates a read-only bag of properties, which can be accessed in a
 * type-safe manner.
 *
 * <p>
 * The simplest way to obtain a required (i.e., the property must be in the bag,
 * otherwise an exception is thrown) property value is
 * {@link #getProperty(String, Class)}. If the property is not a required one,
 * the {@link #getProperty(String, Class, Object)} method can be used, which
 * also takes a default value which is returned when the property is not present
 * in the bag.
 * </p>
 */
public final class PropertyBag {

    private final Map<String, Object> bag;

    PropertyBag(Map<String, Object> bag) {
        this.bag = new HashMap<String, Object>(bag);
    }

    /**
     * Gets the value of a required property in a type-safe manner.
     *
     * If no property exists with the given name,
     * {@link IllegalArgumentException} is thrown.
     *
     * @param <T>
     *            the type of the property.
     * @param name
     *            the name of the property.
     * @param type
     *            the {@link Class} representing the type of the property.
     * @return the value of the property in bag; <code>null</code> if the value
     *         of the property was <code>null</code>.
     * @throws IllegalArgumentException
     *             if no property with the given name name exists in the bag.
     * @throws ClassCastException
     *             if the property exists, but is not of the specified type.
     */
    public <T> T getProperty(String name, Class<T> type) {
        if (!bag.containsKey(name)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Property named \"{0}\" not found in bag", name));
        }
        return castValue(name, type);
    }

    /**
     * Gets a property from the bag and casts it to the specified type, while
     * providing a nice error message to the caller when the property value is
     * not of the specified type.
     *
     * @param name
     *            the name of the property.
     * @param type
     *            the {@link Class} representing the type of the property.
     * @param <T>
     *            the type of the property.
     * @return <code>null</code> if the property does not exists or its value is
     *         <code>null</code>.
     *
     */
    private <T> T castValue(String name, Class<T> type) {
        Object value = bag.get(name);
        // This means the property value is null, so return null.
        if (value == null) {
            return null;
        }
        return castValue(name, value, type);
    }

    private <T> T castValue(String name, Object value, Class<T> type) {
        // This means the property value is null, so return null.
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            if (type.isArray() && value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                // return (T)
                // collection.toArray((Object[])Array.newInstance(type.getComponentType(),
                // collection.size()));

                Object array = Array.newInstance(type.getComponentType(), collection.size());
                int i = 0;
                for (Object itemValue : collection) {
                    Array.set(array, i++, castValue(name, itemValue, type.getComponentType()));
                }
                return (T) array;
            } else if (type.isArray()) {
                Object array = Array.newInstance(type.getComponentType(), 1);
                Array.set(array, 0, castValue(name, value, type.getComponentType()));
                return (T) array;
            }

            if (Byte.TYPE.equals(type) && value instanceof Byte) {
                return (T) value;
            } else if (Byte.class.equals(type) && Byte.TYPE.isInstance(value)) {
                return (T) value;
            } else if (Short.TYPE.equals(type) && value instanceof Short) {
                return (T) value;
            } else if (Short.class.equals(type) && Short.TYPE.isInstance(value)) {
                return (T) value;
            } else if (Integer.TYPE.equals(type) && value instanceof Integer) {
                return (T) value;
            } else if (Integer.class.equals(type) && Integer.TYPE.isInstance(value)) {
                return (T) value;
            } else if (Long.TYPE.equals(type) && value instanceof Long) {
                return (T) value;
            } else if (Long.class.equals(type) && Long.TYPE.isInstance(value)) {
                return (T) value;
            } else if (Float.TYPE.equals(type) && value instanceof Float) {
                return (T) value;
            } else if (Float.class.equals(type) && Float.TYPE.isInstance(value)) {
                return (T) value;
            } else if (Double.TYPE.equals(type) && value instanceof Double) {
                return (T) value;
            } else if (Double.class.equals(type) && Double.TYPE.isInstance(value)) {
                return (T) value;
            } else if (Boolean.TYPE.equals(type) && value instanceof Boolean) {
                return (T) value;
            } else if (Boolean.class.equals(type) && Boolean.TYPE.isInstance(value)) {
                return (T) value;
            } else if (Character.TYPE.equals(type) && value instanceof Character) {
                return (T) value;
            } else if (Character.class.equals(type) && Character.TYPE.isInstance(value)) {
                return (T) value;
            }

            throw new ClassCastException(MessageFormat.format(
                    "Property named \"{0}\" is of type \"{1}\" but expected type was \"{2}\"",
                    name, value.getClass(), type));
        }
        return type.cast(value);
    }

    /**
     * Gets a property value, returning a default value when no property with
     * the specified name exists in the bag.
     *
     * @param <T>
     *            the type of the property.
     * @param name
     *            the name of the property.
     * @param type
     *            the {@link Class} representing the type of the property.
     * @param def
     *            the default value returned when no property with the specified
     *            name exists in the bag.
     * @return the value of the property in bag or the default value;
     *         <code>null</code> if the value of the property was
     *         <code>null</code>.
     * @throws ClassCastException
     *             if the property exists, but is not of the specified type.
     */
    public <T> T getProperty(String name, Class<T> type, T def) {
        if (!bag.containsKey(name)) {
            return def;
        }
        return castValue(name, type);
    }

    /**
     * Gets a required property value known to be of string type.
     *
     * The method expects that the value is an instance of {@link String}. It
     * does not attempt to call {@link Object#toString()} on the value.
     *
     * @param name
     *            the name of the property.
     * @return the value of the property in bag; <code>null</code> if the value
     *         of the property was <code>null</code>.
     * @throws IllegalArgumentException
     *             if no property with the given name exists in the bag.
     * @throws ClassCastException
     *             if the property exists, but is not an instance of
     *             {@link String}.
     */
    public String getStringProperty(String name) {
        return getProperty(name, String.class);
    }

    @Override
    public String toString() {
        return bag.toString();
    }

    Map<String, Object> toMap() {
        return bag;
    }

}
