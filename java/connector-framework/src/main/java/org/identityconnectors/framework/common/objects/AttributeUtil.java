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
package org.identityconnectors.framework.common.objects;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.GuardedString;

/**
 * Utility methods to retrieve values from instances of {@link Attribute}.
 */
public final class AttributeUtil {

    /**
     * Never allow this to be instantiated.
     */
    private AttributeUtil() {
    }

    /**
     * Get the string value from the specified (single-valued) attribute.
     *
     * @param attr
     *            Attribute from which to retrieve the string value.
     * @return null if the value is null otherwise the string value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an string.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public static String getStringValue(final Attribute attr) {
        final Object obj = getSingleValue(attr);
        return obj == null ? null : (String) obj;
    }

    /**
     * Get the {@link GuardedString} value from the specified (single-valued)
     * attribute.
     *
     * @param attr
     *            Attribute from which to retrieve the string value.
     * @return null if the value is null otherwise the string value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an GuardedString.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public static GuardedString getGuardedStringValue(final Attribute attr) {
        final Object obj = getSingleValue(attr);
        return obj == null ? null : (GuardedString) obj;
    }

    /**
     * Get the string value from the specified (single-valued) attribute.
     *
     * @param attr
     *            Attribute from which to retrieve the string value.
     * @return null if the value is null otherwise the string value for the
     *         attribute.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public static String getAsStringValue(final Attribute attr) {
        final Object obj = getSingleValue(attr);
        return obj == null ? null : obj.toString();
    }

    /**
     * Get the integer value from the specified (single-valued) attribute.
     *
     * @param attr
     *            Attribute from which to retrieve the integer value.
     * @return null if the value is null otherwise the integer value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an integer.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public static Integer getIntegerValue(final Attribute attr) {
        final Object obj = getSingleValue(attr);
        return obj == null ? null : (Integer) obj;
    }

    /**
     * Get the long value from the specified (single-valued) attribute.
     *
     * @param attr
     *            Attribute from which to retrieve the long value.
     * @return null if the value is null otherwise the long value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public static Long getLongValue(final Attribute attr) {
        final Object obj = getSingleValue(attr);
        return obj == null ? null : (Long) obj;
    }

    /**
     * Get the date value from the specified (single-valued) attribute that
     * contains a long.
     *
     * @param attr
     *            Attribute from which to retrieve the date value.
     * @return null if the value is null otherwise the date value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an long.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public static Date getDateValue(final Attribute attr) {
        final Long value = getLongValue(attr);
        return value == null ? null : new Date(value.longValue());
    }

    /**
     * Get the integer value from the specified (single-valued) attribute.
     *
     * @param attr
     *            Attribute from which to retrieve the integer value.
     * @return null if the value is null otherwise the integer value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an integer.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued)..
     */
    public static Double getDoubleValue(final Attribute attr) {
        Object obj = getSingleValue(attr);
        return obj != null ? (Double) obj : null;
    }

    /**
     * Get the big decimal value from the specified (single-valued) attribute.
     *
     * @param attr
     *            Attribute from which to retrieve the big decimal value.
     * @return null if the value is null otherwise the big decimal value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an big decimal.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public static BigDecimal getBigDecimalValue(final Attribute attr) {
        final Object obj = getSingleValue(attr);
        return obj == null ? null : (BigDecimal) obj;
    }

    /**
     * Get the boolean value from the specified (single-valued) attribute.
     *
     * @param attr
     *            Attribute from which to retrieve the boolean value.
     * @return null if the value is null otherwise the boolean value for the
     *         attribute.
     * @throws ClassCastException
     *             if the object in the attribute is not an {@link Boolean}.
     * @throws IllegalArgumentException
     *             if the attribute is a multi-valued (rather than
     *             single-valued).
     */
    public static Boolean getBooleanValue(final Attribute attr) {
        final Object obj = getSingleValue(attr);
        return obj == null ? null : (Boolean) obj;
    }

    /**
     * Get the <code>Object</code> value from the specified (single-valued)
     * attribute.
     *
     * @return <code>null</code> if the attribute's list of values is
     *         <code>null</code> or empty.
     */
    public static Object getSingleValue(final Attribute attr) {
        Object ret = null;
        final List<Object> val = attr.getValue();
        if (val != null && !val.isEmpty()) {
            // make sure this only called for single value..
            if (val.size() > 1) {
                final StringBuilder msg =
                        new StringBuilder("The ").append(attr.getName()).append(
                                " attribute is not single value attribute.");
                throw new IllegalArgumentException(msg.toString());
            }
            ret = val.get(0);
        }
        return ret;
    }

    /**
     * Transform a <code>Collection</code> of {@link Attribute} instances into a
     * {@link Map}.
     *
     * The key to each element in the map is the <i>name</i> of an
     * <code>Attribute</code>. The value of each element in the map is the
     * <code>Attribute</code> instance with that name.
     *
     * @param attributes
     *            set of attribute to transform to a map.
     * @return a map of string and attribute.
     * @throws NullPointerException
     *             if the parameter <strong>attributes</strong> is
     *             <strong>null</strong>.
     */
    public static Map<String, Attribute> toMap(final Collection<? extends Attribute> attributes) {
        final Map<String, Attribute> ret = CollectionUtil.<Attribute> newCaseInsensitiveMap();
        for (Attribute attr : attributes) {
            ret.put(attr.getName(), attr);
        }
        return CollectionUtil.asReadOnlyMap(ret);
    }

    /**
     * Get the {@link Uid} from the specified set of attributes.
     *
     * @param attrs
     *            set of {@link Attribute}s that may contain a {@link Uid}.
     * @return null if the set does not contain a {@link Uid} object the first
     *         one found.
     */
    public static Uid getUidAttribute(final Set<Attribute> attrs) {
        return (Uid) find(Uid.NAME, attrs);
    }

    /**
     * Filter out any special attribute from the specified set.
     *
     * Special attributes include {@link Name}, {@link Uid}, and
     * {@link OperationalAttributes}.
     *
     * @param attrs
     *            set of {@link Attribute}s to filter out the operational and
     *            default attributes.
     * @return a set that only contains plain attributes or empty.
     */
    public static Set<Attribute> getBasicAttributes(final Set<Attribute> attrs) {
        final Set<Attribute> ret = new HashSet<Attribute>();
        for (Attribute attr : attrs) {
            // note this is dangerous because we need to be consistent
            // in the naming of special attributes.
            if (!isSpecial(attr)) {
                ret.add(attr);
            }
        }
        return ret;
    }

    /**
     * Filter out any basic attributes from the specified set, leaving only
     * special attributes.
     *
     * Special attributes include {@link Name}, {@link Uid}, and
     * {@link OperationalAttributes}.
     *
     * @param attrs
     *            set of {@link Attribute}s to filter out the basic attributes
     * @return a set that only contains special attributes or an empty set if
     *         there are none.
     */
    public static Set<Attribute> getSpecialAttributes(final Set<Attribute> attrs) {
        final Set<Attribute> ret = new HashSet<Attribute>();
        for (Attribute attr : attrs) {
            if (isSpecial(attr)) {
                ret.add(attr);
            }
        }
        return ret;
    }

    /**
     * Returns a mutable copy of the original set with the uid attribute
     * removed.
     *
     * @param attrs
     *            The original set. Must not be null.
     * @return A mutable copy of the original set with the uid attribute
     *         removed.
     */
    public static Set<Attribute> filterUid(final Set<Attribute> attrs) {
        Assertions.nullCheck(attrs, "attrs");
        final Set<Attribute> ret = new HashSet<Attribute>();
        for (Attribute attr : attrs) {
            if (!(attr instanceof Uid)) {
                ret.add(attr);
            }
        }
        return ret;
    }

    /**
     * Returns a mutable copy of the original set with the uid attribute added.
     *
     * @param attrs
     *            The original set. Must not be null.
     * @param uid
     *            The uid. Must not be null.
     * @return A mutable copy of the original set with the uid attribute added.
     */
    public static Set<Attribute> addUid(final Set<Attribute> attrs, final Uid uid) {
        Assertions.nullCheck(attrs, "attrs");
        Assertions.nullCheck(uid, "uid");
        final Set<Attribute> ret = new HashSet<Attribute>(attrs);
        ret.add(uid);
        return ret;
    }

    /**
     * Determines whether the specified attribute is a special attribute.
     * Special attributes include {@link Uid}, {@link ObjectClass} and
     * {@link OperationalAttributes}.
     *
     * @param attr
     *            {@link Attribute} to test for against.
     * @return true if the attribute value is a {@link Uid},
     *         {@link ObjectClass} or one of the {@link OperationalAttributes}.
     * @throws NullPointerException
     *             if the attribute parameter is null.
     */
    public static boolean isSpecial(final Attribute attr) {
        return isSpecialName(attr.getName());
    }

    /**
     * Determines whether the specified attribute info is for a special
     * attribute. Special attributes include {@link Uid}, {@link ObjectClass}
     * and {@link OperationalAttributes}.
     *
     * @param attr
     *            {@link AttributeInfo} to test for against.
     * @return true if the attribute value is a {@link Uid},
     *         {@link ObjectClass} or one of the {@link OperationalAttributes}.
     * @throws NullPointerException
     *             if the attribute parameter is null.
     */
    public static boolean isSpecial(final AttributeInfo attr) {
        final String name = attr.getName();
        return isSpecialName(name);
    }

    /**
     * Determines whether the specified attribute name is special in the sense
     * of {@link #createSpecialName}.
     *
     * @param name
     *            the attribute name to test against.
     * @return true if the attribute name is special.
     */
    public static boolean isSpecialName(final String name) {
        return NameUtil.isSpecialName(name);
    }

    /**
     * Create a special name from the specified name. Add the <code>__</code>
     * string as both prefix and suffix. This indicates that an attribute name
     * identifies a "special attribute" such as {@link Uid}, {@link ObjectClass}
     * or one of the {@link OperationalAttributes}.
     */
    public static String createSpecialName(final String name) {
        return NameUtil.createSpecialName(name);
    }

    /**
     * Compares two attribute names for equality.
     *
     * @param name1
     *            the first attribute name.
     * @param name2
     *            the second attribute name.
     * @return true if the two attribute names are equal.
     */
    public static boolean namesEqual(final String name1, final String name2) {
        return NameUtil.namesEqual(name1, name2);
    }

    /**
     * Get the {@link Name} attribute from the specified set of attributes.
     *
     * @param attrs
     *            set of attributes to search against.
     * @return the {@link Name} attribute it if exsist otherwise
     *         <code>null</code>.
     */
    public static Name getNameFromAttributes(final Set<Attribute> attrs) {
        return (Name) find(Name.NAME, attrs);
    }

    /**
     * Find the {@link Attribute} of the given name in the {@link Set}.
     *
     * @param name
     *            {@link Attribute}'s name to search for.
     * @param attrs
     *            {@link Set} of attribute to search.
     * @return {@link Attribute} with the specified otherwise <code>null</code>.
     */
    public static Attribute find(final String name, final Set<Attribute> attrs) {
        Assertions.nullCheck(name, "name");
        final Set<Attribute> set = CollectionUtil.nullAsEmpty(attrs);
        for (Attribute attr : set) {
            if (attr.is(name)) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Get the password value from the provided set of {@link Attribute}s.
     */
    public static GuardedString getPasswordValue(final Set<Attribute> attrs) {
        final Attribute pwd = find(OperationalAttributes.PASSWORD_NAME, attrs);
        return (pwd == null) ? null : getGuardedStringValue(pwd);
    }

    /**
     * Get the current password value from the provided set of {@link Attribute}
     * s.
     *
     * @param attrs
     *            Set of {@link Attribute}s that may contain the current
     *            password {@link OperationalAttributes#CURRENT_PASSWORD_NAME}
     *            {@link Attribute}.
     * @return <code>null</code> if it does not exist in the {@link Set} else
     *         the value.
     */
    public static GuardedString getCurrentPasswordValue(final Set<Attribute> attrs) {
        final Attribute pwd = find(OperationalAttributes.CURRENT_PASSWORD_NAME, attrs);
        return (pwd == null) ? null : getGuardedStringValue(pwd);
    }

    /**
     * Determine if the {@link ConnectorObject} is locked out. By getting the
     * value of the {@link OperationalAttributes#LOCK_OUT_NAME}.
     *
     * @param obj
     *            {@link ConnectorObject} object to inspect.
     * @throws NullPointerException
     *             if the parameter 'obj' is <code>null</code>.
     * @return <code>null</code> if the attribute does not exist otherwise to
     *         value of the {@link Attribute}.
     */
    public static Boolean isLockedOut(final ConnectorObject obj) {
        final Attribute attr = obj.getAttributeByName(OperationalAttributes.LOCK_OUT_NAME);
        return (attr == null) ? null : getBooleanValue(attr);
    }

    /**
     * Determine if the {@link ConnectorObject} is enable. By getting the value
     * of the {@link OperationalAttributes#ENABLE_NAME}.
     *
     * @param obj
     *            {@link ConnectorObject} object to inspect.
     * @throws IllegalStateException
     *             if the object does not contain attribute in question.
     * @throws NullPointerException
     *             if the parameter 'obj' is <code>null</code>.
     * @return <code>null</code> if the attribute does not exist otherwise to
     *         value of the {@link Attribute}.
     */
    public static Boolean isEnabled(final ConnectorObject obj) {
        final Attribute attr = obj.getAttributeByName(OperationalAttributes.ENABLE_NAME);
        return (attr == null) ? null : getBooleanValue(attr);
    }

    /**
     * Retrieve the password expiration date from the {@link ConnectorObject}.
     *
     * @param obj
     *            {@link ConnectorObject} object to inspect.
     * @throws IllegalStateException
     *             if the object does not contain attribute in question.
     * @throws NullPointerException
     *             if the parameter 'obj' is <code>null</code>.
     * @return <code>null</code> if the {@link Attribute} does not exist
     *         otherwise the value of the {@link Attribute}.
     */
    public static Date getPasswordExpirationDate(final ConnectorObject obj) {
        final Attribute attr =
                obj.getAttributeByName(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME);
        return (attr == null) ? null : new Date(getLongValue(attr));
    }

    /**
     * Get the password expired attribute from a {@link Collection} of
     * {@link Attribute}s.
     *
     * @param attrs
     *            set of attribute to find the expired password
     *            {@link Attribute}.
     * @return <code>null</code> if the attribute does not exist and the value
     *         of the {@link Attribute} if it does.
     */
    public static Boolean getPasswordExpired(final Set<Attribute> attrs) {
        final Attribute pwd = find(OperationalAttributes.PASSWORD_EXPIRED_NAME, attrs);
        return (pwd == null) ? null : getBooleanValue(pwd);
    }

    /**
     * Determine if the password is expired for this object.
     *
     * @param obj
     *            {@link ConnectorObject} that should contain a password expired
     *            attribute.
     * @return <code>null</code> if the attribute does not exist and the value
     *         of the {@link Attribute} if it does.
     */
    public static Boolean isPasswordExpired(final ConnectorObject obj) {
        final Attribute pwd = obj.getAttributeByName(OperationalAttributes.PASSWORD_EXPIRED_NAME);
        return (pwd == null) ? null : getBooleanValue(pwd);
    }

    /**
     * Get the enable date from the set of attributes.
     *
     * @param attrs
     *            set of attribute to find the enable date {@link Attribute}.
     * @return <code>null</code> if the attribute does not exist and the value
     *         of the {@link Attribute} if it does.
     */
    public static Date getEnableDate(final Set<Attribute> attrs) {
        final Attribute date = find(OperationalAttributes.ENABLE_DATE_NAME, attrs);
        return (date == null) ? null : getDateValue(date);
    }
}
