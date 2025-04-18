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
package org.identityconnectors.framework.common.objects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.GuardedByteArray;
import org.identityconnectors.common.security.GuardedString;

/**
 * Attributes Accessor convenience methods for accessing attributes.
 *
 * This class wraps a set of attributes to make lookup faster than the
 * {@link AttributeUtil#find(String, Set)} method, since that method must
 * re-create the map each time.
 *
 * @author Warren Strange
 */
public class AttributesAccessor {

    private final Map<String, Attribute> attributeMap;

    public AttributesAccessor(final Set<Attribute> attrs) {
        attributeMap = AttributeUtil.toMap(attrs);
    }

    /**
     * Find the named attribute.
     *
     * @param name
     *            the attribute name to search for
     * @return the Attribute, or null if not found.
     */
    public Attribute find(final String name) {
        return attributeMap.get(name);
    }

    /**
     * Get the {@link Name} attribute from the set of attributes.
     *
     * @return the {@link Name} attribute in the set.
     */
    public Name getName() {
        return (Name) find(Name.NAME);
    }

    /**
     * Get the {@link Uid} attribute from the set of attributes.
     *
     * @return the {@link Uid} attribute in the set.
     */
    public Uid getUid() {
        return (Uid) find(Uid.NAME);
    }

    /**
     * Return the enabled status of the account.
     *
     * If the ENABLE operational attribute is present, it's value takes
     * precedence over the current value. If it is missing, the currentlyEnabled
     * status is returned instead.
     *
     * @param defaultTo
     *            the default state if enable is not found.
     * @return true if the account is enabled, false otherwise
     */
    public boolean getEnabled(boolean defaultTo) {
        boolean result = defaultTo;
        final Attribute enable = find(OperationalAttributes.ENABLE_NAME);
        if (enable != null) {
            result = AttributeUtil.getBooleanValue(enable);
        }
        return result;
    }

    /**
     * Get the password as a GuardeString.
     *
     * @return the password as a guarded String
     */
    public GuardedString getPassword() {
        final Attribute attribute = find(OperationalAttributes.PASSWORD_NAME);
        return attribute == null ? null : AttributeUtil.getGuardedStringValue(attribute);
    }

    /**
     * Return a list of attributes.
     *
     * @param name
     *            name of attribute to search for.
     *
     * @return The List (generic object) if it exists otherwise null.
     */
    public List<Object> findList(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : attribute.getValue();
    }

    /**
     * Return the multivalued attribute as a list of strings. This will throw a
     * ClassCastException if the underlying attribute list is not of type
     * String.
     *
     * @param name
     *            the name of the attribute to search for
     * @return a List of String values for the attribute
     */
    public List<String> findStringList(final String name) {
        final List<Object> list = findList(name);
        if (list != null) {
            final List<String> ret = new ArrayList<String>(list.size());
            for (Object o : list) {
                ret.add((String) o);
            }
            return ret;
        }
        return null;
    }

    /**
     * Get the name of attributes this Accessor was created with.
     *
     * @return new Case Insensitive ReadOnly Set of attribute name the access
     *         has access to.
     * @since 1.4
     */
    public Set<String> listAttributeNames() {
        final Set<String> names = CollectionUtil.newCaseInsensitiveSet();
        names.addAll(attributeMap.keySet());
        return Collections.unmodifiableSet(names);
    }

    /**
     * Determines if the set as the attribute specified.
     *
     * @param name
     *            attribute name
     * @return true if the named attribute exists, false otherwise
     */
    public boolean hasAttribute(final String name) {
        return find(name) != null;
    }

    /**
     * Get the string value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the long value.
     * @return null if the value is null otherwise the long value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public String findString(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getStringValue(attribute);
    }

    /**
     * Get the character value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the character value.
     * @return null if the value is null otherwise the character value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a character.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     * @since 1.4
     */
    public Character findCharacter(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getCharacterValue(attribute);
    }

    /**
     * Get the integer value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the long value.
     * @return null if the value is null otherwise the long value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public Integer findInteger(final String name) {
        final Attribute attribute = find(name);
        return (attribute == null) ? null : AttributeUtil.getIntegerValue(attribute);
    }

    /**
     * Get the long value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the long value.
     * @return null if the value is null otherwise the long value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public Long findLong(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getLongValue(attribute);
    }

    /**
     * Get the date value from the specified (single-valued) attribute that
     * contains a long.
     *
     * @param name
     *            Attribute from which to retrieve the date value.
     * @return null if the value is null otherwise the date value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public Date findDate(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getDateValue(attribute);
    }

    /**
     * Get the double value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the double value.
     * @return null if the value is null otherwise the double value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a double.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued)..
     */
    public Double findDouble(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getDoubleValue(attribute);
    }

    /**
     * Get the float value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the float value.
     * @return null if the value is null otherwise the float value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a float.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     * @since 1.4
     */
    public Float findFloat(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getFloatValue(attribute);
    }

    /**
     * Get the big decimal value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the big decimal value.
     * @return null if the value is null otherwise the big decimal value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an big decimal.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public BigDecimal findBigDecimal(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getBigDecimalValue(attribute);
    }

    /**
     * Get the boolean value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the boolean value.
     * @return null if the value is null otherwise the boolean value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an {@link Boolean}.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public Boolean findBoolean(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getBooleanValue(attribute);
    }

    /**
     * Get the byte value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the byte value.
     * @return null if the value is null otherwise the byte value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a byte.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     * @since 1.4
     */
    public Byte findByte(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getByteValue(attribute);
    }

    /**
     * Get the byte array value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the byte array value.
     * @return null if the value is null otherwise the byte array value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a byte.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     * @since 1.4
     */
    public Byte[] findByteArray(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getByteArrayValue(attribute);
    }

    /**
     * Get the big integer value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the big integer value.
     * @return null if the value is null otherwise the big integer value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a big integer.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     * @since 1.4
     */
    public BigInteger findBigInteger(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getBigIntegerValue(attribute);
    }

    /**
     * Get the guarded byte array value from the specified (single-valued)
     * attribute.
     *
     * @param name
     *            Attribute from which to retrieve the guarded byte array value.
     * @return null if the value is null otherwise the guarded byte array value
     *         for the attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a guarded byte array.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     * @since 1.4
     */
    public GuardedByteArray findGuardedByteArray(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getGuardedByteArrayValue(attribute);
    }

    /**
     * Get the guarded string value from the specified (single-valued)
     * attribute.
     *
     * @param name
     *            Attribute from which to retrieve the guarded string value.
     * @return null if the value is null otherwise the guarded string value for
     *         the attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a guarded string.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     * @since 1.4
     */
    public GuardedString findGuardedString(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getGuardedStringValue(attribute);
    }

    /**
     * Get the map value from the specified (single-valued) attribute.
     *
     * @param name
     *            Attribute from which to retrieve the map value.
     * @return null if the value is null otherwise the map value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not a map.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     * @since 1.4
     */
    public Map<String, Object> findMap(final String name) {
        final Attribute attribute = find(name);
        return attribute == null ? null : AttributeUtil.getMapValue(attribute);
    }
}
