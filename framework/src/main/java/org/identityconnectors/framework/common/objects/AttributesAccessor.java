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
package org.identityconnectors.framework.common.objects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    final Set<Attribute> _attrs;
    final Map<String, Attribute> _attrMap;

    public AttributesAccessor(Set<Attribute> attrs) {
        _attrs = attrs;
        _attrMap = AttributeUtil.toMap(attrs);
    }

    /**
     * Find the named attribute
     * 
     * @param name -
     *            the attribute name to search for
     * @return the Attribute, or null if not found.
     */
    public Attribute find(String name) {
        return _attrMap.get(name);
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
     * @param dflt
     *            the default state if enable is not found.
     * @return true if the account is enabled, false otherwise
     */
    public boolean getEnabled(boolean dflt) {
        boolean e = dflt;
        Attribute enable = find(OperationalAttributes.ENABLE_NAME);
        if (enable != null) {
            e = AttributeUtil.getBooleanValue(enable).booleanValue();
        }
        return e;
    }

    /**
     * Get the password as a GuardeString
     * 
     * @return the password as a guarded String
     */
    public GuardedString getPassword() {
        Attribute a = find(OperationalAttributes.PASSWORD_NAME);
        return a == null ? null : AttributeUtil.getGuardedStringValue(a);
    }

    /**
     * Return a list of attributes
     * 
     * @param name -
     *            name of attribute to search for.
     * 
     * @return The List (generic object) iff it exists otherwise null.
     */
    public List<Object> findList(String name) {
        Attribute a = find(name);
        return (a == null) ? null : a.getValue();
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
    public List<String> findStringList(String name) {
        List<Object> l = findList(name);
        if (l != null) {
            List<String> ret = new ArrayList<String>(l.size());
            for (Object o : l) {
                ret.add((String) o);
            }
            return ret;
        }
        return null;
    }

    /**
     * Determines if the set as the attribute specified.
     * 
     * @param name
     *            attribute name
     * @return true if the named attribute exists, false otherwise
     */
    public boolean hasAttribute(String name) {
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
     *             iff the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             iff the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public String findString(String name) {
        Attribute a = find(name);
        return a == null ? null : AttributeUtil.getStringValue(a);
    }

    /**
     * Get the integer value from the specified (single-valued) attribute.
     * 
     * @param name
     *            Attribute from which to retrieve the long value.
     * @return null if the value is null otherwise the long value for the
     *         attribute.
     * @throws ClassCastException
     *             iff the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             iff the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public Integer findInteger(String name) {
        Attribute a = find(name);
        return (a == null) ? null : AttributeUtil.getIntegerValue(a);
    }

    /**
     * Get the long value from the specified (single-valued) attribute.
     * 
     * @param name
     *            Attribute from which to retrieve the long value.
     * @return null if the value is null otherwise the long value for the
     *         attribute.
     * @throws ClassCastException
     *             iff the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             iff the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public Long findLong(String name) {
        Attribute a = find(name);
        return a == null ? null : AttributeUtil.getLongValue(a);
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
     *             iff the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             iff the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public Date findDate(String name) {
        Attribute a = find(name);
        return a == null ? null : AttributeUtil.getDateValue(a);
    }

    /**
     * Get the integer value from the specified (single-valued) attribute.
     * 
     * @param name
     *            Attribute from which to retrieve the integer value.
     * @return null if the value is null otherwise the integer value for the
     *         attribute.
     * @throws ClassCastException
     *             iff the object in the attribute is not an integer.
     * @throws IllegalArgumentException
     *             iff the attribute is a multi-valued (rather than
     *             single-valued)..
     */
    public Double findDouble(String name) {
        Attribute a = find(name);
        return a == null ? null : AttributeUtil.getDoubleValue(a);
    }

    /**
     * Get the big decimal value from the specified (single-valued) attribute.
     * 
     * @param name
     *            Attribute from which to retrieve the big decimal value.
     * @return null if the value is null otherwise the big decimal value for the
     *         attribute.
     * @throws ClassCastException
     *             iff the object in the attribute is not an big decimal.
     * @throws IllegalArgumentException
     *             iff the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public BigDecimal findBigDecimal(String name) {
        Attribute a = find(name);
        return a == null ? null : AttributeUtil.getBigDecimalValue(a);
    }

    /**
     * Get the boolean value from the specified (single-valued) attribute.
     * 
     * @param name
     *            Attribute from which to retrieve the boolean value.
     * @return null if the value is null otherwise the boolean value for the
     *         attribute.
     * @throws ClassCastException
     *             iff the object in the attribute is not an {@link Boolean}.
     * @throws IllegalArgumentException
     *             iff the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public Boolean findBoolean(String name) {
        Attribute a = find(name);
        return a == null ? null : AttributeUtil.getBooleanValue(a);
    }
}
