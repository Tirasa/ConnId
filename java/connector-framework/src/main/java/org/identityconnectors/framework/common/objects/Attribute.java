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
 * Portions Copyrighted 2016-2022 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import static org.identityconnectors.framework.common.objects.NameUtil.nameHashCode;
import static org.identityconnectors.framework.common.objects.NameUtil.namesEqual;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;

/**
 * Represents a named collection of values within a target object, although the
 * simplest case is a name-value pair (e.g., email, employeeID). Values can be
 * empty, null, or set with various types. Empty and null are supported because
 * it makes a difference on some resources (in particular database resources).
 * <p>
 * The developer of a Connector should use an {@link AttributeBuilder} to
 * construct an instance of Attribute.
 * <p>
 * The precise meaning of an instance of {@code Attribute} depends on the
 * context in which it occurs.
 * <ul>
 * <li>When
 * {@linkplain org.identityconnectors.framework.api.operations.GetApiOp#getObject
 * an object is read} or is returned by
 * {@linkplain org.identityconnectors.framework.api.operations.SearchApiOp#search
 * search}, an {@code Attribute} represents the <i>complete state</i> of an
 * attribute of the target object, current as of the point in time that the
 * object was read.</li>
 * <li>When an {@code Attribute} is supplied to
 * {@linkplain org.identityconnectors.framework.api.operations.UpdateApiOp the
 * update operation}, the {@code Attribute} represents a change to the
 * corresponding attribute of the target object:
 * <ul>
 * <li>For calls to
 * {@link org.identityconnectors.framework.api.operations.UpdateApiOp#update(ObjectClass, Uid, java.util.Set, OperationOptions)
 * update}, the {@code Attribute} contains the <i>complete, intended state</i>
 * of the attribute.</li>
 * <li>When the update type is
 * {@link org.identityconnectors.framework.api.operations.UpdateApiOp#addAttributeValues(ObjectClass, Uid, java.util.Set, OperationOptions)
 * addAttributeValues}, the {@code Attribute} contains <i>values to append</i>.</li>
 * <li>When the update type is
 * {@link org.identityconnectors.framework.api.operations.UpdateApiOp#removeAttributeValues(ObjectClass, Uid, java.util.Set, OperationOptions)
 * removeAttributeValues}, the {@code Attribute} contains <i>values to
 * remove</i>.</li>
 * </ul>
 * </li>
 * <li>When an {@code Attribute} is used to build a
 * {@link org.identityconnectors.framework.common.objects.filter.Filter Filter}
 * that is an argument to
 * {@linkplain org.identityconnectors.framework.api.operations.SearchApiOp#search
 * search}, an {@code Attribute} represents a <i>subset of the current state</i>
 * of an attribute that will be used as a search criterion. Specifically, the
 * {@code Attribute} {@linkplain #getName() names the attribute to match} and
 * {@linkplain #getValue() contains the values to match}.</li>
 * </ul>
 *
 * TODO: define the set of allowed values
 *
 * @author Will Droste
 * @since 1.0
 */
public class Attribute {

    /**
     * Name of the {@link Attribute}.
     */
    private final String name;

    /**
     * Values of the {@link Attribute}.
     */
    private final List<Object> value;

    /**
     * A status that indicates completeness of attribute values.
     * Normal resources always return all values of the attribute. However there may be
     * cases, when returning all the values is not an acceptable overhead
     * (e.g. returning all group members for big groups). This status can also be used
     * to indicate that an attribute has a value without revealing that value.
     * E.g. resource may indicate that the account has a password without revealing
     * that password.
     * The interpretation: If there is no Attribute object in the ConnectorObject for any
     * specific attribute, then the client should make no assumption about existence of
     * the attribute (e.g. the attribute may exists, but it security policies deny reading
     * of the attribute right now). If the Attribute object is returned then the client
     * may assume that the attribute exists and it has at least one value. If the
     * attributeValueCompleteness is set to COMPLETE then the client may also assume that
     * the returned set of value is complete.
     */
    private final AttributeValueCompleteness attributeValueCompleteness;

    Attribute(String name, List<Object> value) {
        this(name, value, AttributeValueCompleteness.COMPLETE);
    }

    /**
     * Create an attribute.
     */
    Attribute(String name, List<Object> value, AttributeValueCompleteness attributeValueCompleteness) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Name must not be blank!");
        }
        if (OperationalAttributes.PASSWORD_NAME.equals(name)
                || OperationalAttributes.CURRENT_PASSWORD_NAME.equals(name)) {
            // check the value..
            if (value != null && value.size() > 1) {
                throw new IllegalArgumentException("Password attribute must be single-value.");
            }
            if (value != null && value.size() == 1 && !(value.get(0) instanceof GuardedString)) {
                throw new IllegalArgumentException(
                        "Password value must be an instance of GuardedString");
            }
        }
        // make this case insensitive
        this.name = name;
        // copy to prevent corruption..
        this.value = (value == null) ? null : CollectionUtil.newReadOnlyList(value);
        this.attributeValueCompleteness = attributeValueCompleteness;
    }

    public String getName() {
        return this.name;
    }

    public List<Object> getValue() {
        return (this.value == null) ? null : Collections.unmodifiableList(this.value);
    }

    public AttributeValueCompleteness getAttributeValueCompleteness() {
        return attributeValueCompleteness;
    }

    /**
     * Determines if the 'name' matches this {@link Attribute}.
     *
     * @param name
     * case insensitive string representation of the attribute's name.
     * @return <code>true</code> if the case insentitive name is equal to that
     * of the one in {@link Attribute}.
     */
    public boolean is(String name) {
        return namesEqual(this.name, name);
    }

    // ===================================================================
    // Object Overrides
    // ===================================================================
    @Override
    public int hashCode() {
        return nameHashCode(name);
    }

    @Override
    public String toString() {
        // poor man's consistent toString impl..
        StringBuilder bld = new StringBuilder();
        bld.append("Attribute: ");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("Name", getName());
        map.put("Value", getValue());
        extendToStringMap(map);
        bld.append(map);
        return bld.toString();
    }

    protected void extendToStringMap(final Map<String, Object> map) {
        // Nothing to do here. Just for use in subclasses.
    }

    @Override
    public boolean equals(Object obj) {
        // test identity
        if (this == obj) {
            return true;
        }
        // test for null..
        if (obj == null) {
            return false;
        }
        // test that the exact class matches
        if (!(getClass().equals(obj.getClass()))) {
            return false;
        }
        // test name field..
        final Attribute other = (Attribute) obj;
        if (!is(other.name)) {
            return false;
        }

        if (!CollectionUtil.equals(value, other.value)) {
            return false;
        }

        if (this.attributeValueCompleteness != other.attributeValueCompleteness) {
            return false;
        }

        return true;
    }
}
