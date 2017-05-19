/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Evolveum. All rights reserved.
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
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.GuardedByteArray;
import org.identityconnectors.common.security.GuardedString;

/**
 * Utility methods to retrieve values from instances of {@link AttributeDelta}.
 */
public final class AttributeDeltaUtil {

    /**
     * Never allow this to be instantiated.
     */
    private AttributeDeltaUtil() {
    }

    /**
     * Get the string value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the string value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the string value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a string.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static String getStringValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (String) obj;
    }

    /**
     * Get the character value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the character value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the character value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a character.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     * @since 1.4
     */
    public static Character getCharacterValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (Character) obj;
    }

    /**
     * Get the {@link GuardedByteArray} value from the specified (single-valued)
     * attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the guarded byte array value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the guarded byte array value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a GuardedByteArray.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     * @since 1.4
     */
    public static GuardedByteArray getGuardedByteArrayValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (GuardedByteArray) obj;
    }

    /**
     * Get the {@link GuardedString} value from the specified (single-valued)
     * attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the guarded string value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the guarded string value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a GuardedString.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static GuardedString getGuardedStringValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (GuardedString) obj;
    }

    /**
     * Get the string value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the string value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the string value from list
     * valuesToReplace for the attributeDelta.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static String getAsStringValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : obj.toString();
    }

    /**
     * Get the byte value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the byte value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the byte value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a byte.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     * @since 1.4
     */
    public static Byte getByteValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (Byte) obj;
    }

    /**
     * Get the byte array value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the byte array value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the byte array value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a byte array.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     * @since 1.4
     */
    public static Byte[] getByteArrayValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        if (obj instanceof byte[]) {
            Byte[] copy = new Byte[((byte[]) obj).length];
            for (int idx = 0; idx < ((byte[]) obj).length; ++idx) {
                copy[idx] = ((byte[]) obj)[idx];
            }
            return copy;
        } else {
            return obj == null ? null : (Byte[]) obj;
        }
    }

    /**
     * Get the integer value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the integer value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the integer value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not an integer.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static Integer getIntegerValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (Integer) obj;
    }

    /**
     * Get the long value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the long value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the long value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a long.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static Long getLongValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (Long) obj;
    }

    /**
     * Get the float value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the float value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the float value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a float.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     * @since 1.4
     */
    public static Float getFloatValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (Float) obj;
    }

    /**
     * Get the date value from the specified (single-valued) attributeDelta that
     * contains a long.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the date value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the date value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a long.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static Date getDateValue(final AttributeDelta attrDelta) {
        final Long value = getLongValue(attrDelta);
        return value == null ? null : new Date(value);
    }

    /**
     * Get the double value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the double value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the double value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a double.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static Double getDoubleValue(final AttributeDelta attrDelta) {
        Object obj = getSingleValue(attrDelta);
        return obj != null ? (Double) obj : null;
    }

    /**
     * Get the big decimal value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the big decimal value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the big decimal value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a big decimal.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static BigDecimal getBigDecimalValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (BigDecimal) obj;
    }

    /**
     * Get the big integer value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the big integer value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the big integer value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not a big integer.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     * @since 1.4
     */
    public static BigInteger getBigIntegerValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (BigInteger) obj;
    }

    /**
     * Get the boolean value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the boolean value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the boolean value from list
     * valuesToReplace for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not an {@link Boolean}.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    public static Boolean getBooleanValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (Boolean) obj;
    }

    /**
     * Get the map value from the specified (single-valued) attributeDelta.
     *
     * @param attrDelta
     * AttributeDelta from which to retrieve the map value.
     * @return null if the value is null or attributeDelta doesn't include
     * valuesToReplace otherwise the map value from list valuesToReplace
     * for the attributeDelta.
     * @throws ClassCastException
     * if the object in the attributeDelta is not an {@link Map}.
     * @throws IllegalArgumentException
     * if the attributeDelta is a multi-valued (rather than
     * single-valued) or valuesToReplace is null.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapValue(final AttributeDelta attrDelta) {
        final Object obj = getSingleValue(attrDelta);
        return obj == null ? null : (Map<String, Object>) obj;
    }

    /**
     * Get the <code>Object</code> value from the specified (single-valued)
     * attributeDelta.
     *
     * @return <code>null</code> if the attributeDelta's list of valuesToReplace is
     * <code>null</code> or empty.
     */
    public static Object getSingleValue(final AttributeDelta attr) {
        Object ret = null;
        final List<Object> val = attr.getValuesToReplace();
        if (val != null && !val.isEmpty()) {
            // make sure this only called for single value..
            if (val.size() > 1) {
                final StringBuilder msg = new StringBuilder("The ").append(attr.getName())
                        .append(" attributeDelta is not single value attribute.");
                throw new IllegalArgumentException(msg.toString());
            }

            ret = val.get(0);
        }
        return ret;
    }

    /**
     * Transform a <code>Collection</code> of {@link AttributeDelta} instances
     * into a {@link Map}.
     *
     * The key to each element in the map is the <i>name</i> of an
     * <code>AttributeDelta</code>. The value of each element in the map is the
     * <code>AttributeDelta</code> instance with that name.
     *
     * @param attributesDelta
     * set of attributesDelta to transform to a map.
     * @return a map of string and attributesDelta.
     * @throws NullPointerException
     * if the parameter <strong>attributesDelta</strong> is
     * <strong>null</strong>.
     */
    public static Map<String, AttributeDelta> toMap(final Collection<? extends AttributeDelta> attributesDelta) {
        final Map<String, AttributeDelta> ret = CollectionUtil.<AttributeDelta>newCaseInsensitiveMap();
        for (AttributeDelta attr : attributesDelta) {
            ret.put(attr.getName(), attr);
        }
        return CollectionUtil.asReadOnlyMap(ret);
    }

    /**
     * Get the {@link AttributeDelta} from the specified set of attributesDelta.
     *
     * @param attrsDelta
     * set of {@link AttributeDelta}s that may contain a
     * {@link AttributeDelta} with {@link Uid.NAME}.
     * @return null if the set does not contain a {@link AttributeDelta} with
     * {@link Uid.NAME} or object the first one found.
     */
    public static AttributeDelta getUidAttributeDelta(final Set<AttributeDelta> attrsDelta) {
        return find(Uid.NAME, attrsDelta);
    }

    /**
     * Get the {@link AttributeDelta} with name specific attributesDelta.
     *
     * @param attrDelta
     * {@link AttributeDelta} with name from which create {@link AttributeDelta}.
     */
    public static Attribute getEmptyAttribute(final AttributeDelta attrDelta) {
        return AttributeBuilder.build(attrDelta.getName());
    }

    /**
     * Filter out any special attributeDelta from the specified set.
     *
     * Special attributes include {@link Name}, {@link Uid}, and
     * {@link OperationalAttributes}.
     *
     * @param attrsDelta
     * set of {@link AttributeDelta}s to filter out the operational
     * and default attributes.
     * @return a set that only contains plain attributesDelta or empty.
     */
    public static Set<AttributeDelta> getBasicAttributes(final Set<AttributeDelta> attrsDelta) {
        final Set<AttributeDelta> ret = new HashSet<AttributeDelta>();
        for (AttributeDelta attrDelta : attrsDelta) {
            // note this is dangerous because we need to be consistent
            // in the naming of special attributes.
            if (!isSpecial(attrDelta)) {
                ret.add(attrDelta);
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
     * @param attrsDelta
     * set of {@link AttributeDelta}s to filter out the basic
     * attributes
     * @return a set that only contains special attributes or an empty set if
     * there are none.
     */
    public static Set<AttributeDelta> getSpecialAttributes(final Set<AttributeDelta> attrsDelta) {
        final Set<AttributeDelta> ret = new HashSet<AttributeDelta>();
        for (AttributeDelta attr : attrsDelta) {
            if (isSpecial(attr)) {
                ret.add(attr);
            }
        }
        return ret;
    }

    /**
     * Returns a mutable copy of the original set with the uid attributeDelta
     * removed.
     *
     * @param attrsDelta
     * The original set. Must not be null.
     * @return A mutable copy of the original set with the uid attributeDelta
     * removed.
     */
    public static Set<AttributeDelta> filterUid(final Set<AttributeDelta> attrsDelta) {
        Assertions.nullCheck(attrsDelta, "attrsDelta");
        final Set<AttributeDelta> ret = new HashSet<AttributeDelta>();
        for (AttributeDelta attrDelta : attrsDelta) {
            if (!(attrDelta.is(Uid.NAME))) {
                ret.add(attrDelta);
            }
        }
        return ret;
    }

    /**
     * Returns a mutable copy of the original set with the uid attributeDelta added.
     *
     * @param attrsDelta
     * The original set. Must not be null.
     * @param values
     * The uid's values. Must not be null.
     * @return A mutable copy of the original set with the uid attributeDelta added.
     */
    public static Set<AttributeDelta> addUid(final Set<AttributeDelta> attrsDelta, Object... values) {
        Assertions.nullCheck(attrsDelta, "attrs");
        Assertions.nullCheck(values, "uid's values");
        final Set<AttributeDelta> ret = new HashSet<AttributeDelta>(attrsDelta);
        ret.add(AttributeDeltaBuilder.build(Uid.NAME, values));
        return ret;
    }

    /**
     * Determines whether the specified name of attribute info is for a special
     * attribute. Special attributes include {@link Uid}, {@link ObjectClass}
     * and {@link OperationalAttributes}.
     *
     * @param attr
     * {@link AttributeInfo} to test for against.
     * @return true if the attributeDelta name is name of a {@link Uid}, {@link ObjectClass}
     * or one of the {@link OperationalAttributes}.
     * @throws NullPointerException
     * if the attribute parameter is null.
     */
    public static boolean isSpecial(final AttributeDelta attrDelta) {
        final String name = attrDelta.getName();
        return isSpecialName(name);
    }

    /**
     * Determines whether the specified attribute name is special in the sense
     * of {@link #createSpecialName}.
     *
     * @param name
     * the attribute name to test against.
     * @return true if the attribute name is special.
     */
    public static boolean isSpecialName(final String name) {
        return NameUtil.isSpecialName(name);
    }

    /**
     * Compares two attributeDelta names for equality.
     *
     * @param name1
     * the first attributeDelta name.
     * @param name2
     * the second attributeDelta name.
     * @return true if the two attributeDelta names are equal.
     */
    public static boolean namesEqual(final String name1, final String name2) {
        return NameUtil.namesEqual(name1, name2);
    }

    /**
     * Get the {@link AttributeDelta} attributeDelta for attribute Name from the
     * specified set of attributesDelta.
     *
     * @param attrsDelta
     * set of attributesDelta to search against.
     * @return the {@link AttributeDelta} attributeDelta for attribute Name if
     * it exist otherwise <code>null</code>.
     */
    public static AttributeDelta getAttributeDeltaForName(final Set<AttributeDelta> attrsDelta) {
        return find(Name.NAME, attrsDelta);
    }

    /**
     * Find the {@link AttributeDelta} of the given name in the {@link Set}.
     *
     * @param name
     * {@link AttributeDelta}'s name to search for.
     * @param attrsDelta
     * {@link Set} of attributeDelta to search.
     * @return {@link AttributeDelta} with the specified otherwise
     * <code>null</code>.
     */
    public static AttributeDelta find(final String name, final Set<AttributeDelta> attrsDelta) {
        Assertions.nullCheck(name, "name");
        final Set<AttributeDelta> set = CollectionUtil.nullAsEmpty(attrsDelta);
        for (AttributeDelta attrDelta : set) {
            if (attrDelta.is(name)) {
                return attrDelta;
            }
        }
        return null;
    }

    /**
     * Get the password value from the provided set of {@link AttributeDelta}s.
     */
    public static GuardedString getPasswordValue(final Set<AttributeDelta> attrsDelta) {
        final AttributeDelta pwd = find(OperationalAttributes.PASSWORD_NAME, attrsDelta);
        return (pwd == null) ? null : getGuardedStringValue(pwd);
    }

    /**
     * Get the current password value from the provided set of {@link AttributeDelta}
     * s.
     *
     * @param attrsDelta
     * Set of {@link AttributeDelta}s that may contain the current
     * password {@link OperationalAttributes#CURRENT_PASSWORD_NAME}
     *            {@link AttributeDelta}.
     * @return <code>null</code> if it does not exist in the {@link Set} else
     * the value.
     */
    public static GuardedString getCurrentPasswordValue(final Set<AttributeDelta> attrsDelta) {
        final AttributeDelta pwd = find(OperationalAttributes.CURRENT_PASSWORD_NAME, attrsDelta);
        return (pwd == null) ? null : getGuardedStringValue(pwd);
    }

    /**
     * Get the password expired attributeDelta from a {@link Collection} of
     * {@link AttributeDelta}s.
     *
     * @param attrsDelta
     * set of attributeDelta to find the expired password
     * {@link AttributeDelta}.
     * @return <code>null</code> if the attributeDelta does not exist and the value
     * of the {@link AttributeDelta} if it does.
     */
    public static Boolean getPasswordExpired(final Set<AttributeDelta> attrsDelta) {
        final AttributeDelta pwd = find(OperationalAttributes.PASSWORD_EXPIRED_NAME, attrsDelta);
        return (pwd == null) ? null : getBooleanValue(pwd);
    }

    /**
     * Get the enable date from the set of attributesDelta.
     *
     * @param attrsDelta
     * set of attributeDelta to find the enable date
     * {@link AttributeDelta}.
     * @return <code>null</code> if the attributeDelta does not exist and the
     * value of the {@link AttributeDelta} if it does.
     */
    public static Date getEnableDate(final Set<AttributeDelta> attrsDelta) {
        final AttributeDelta date = find(OperationalAttributes.ENABLE_DATE_NAME, attrsDelta);
        return (date == null) ? null : getDateValue(date);
    }
}
