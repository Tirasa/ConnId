/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017-2018 Evolveum. All rights reserved.
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
 * Portions Copyrighted 2022 Evolveum
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

/**
 * <p>
 * Specifies a modification of a single {@link Attribute}. The delta is often a description
 * of a relative modification of the attribute. It describes values that are added
 * and removed. But it may also define an absolute modification: values that are replaced.
 * </p>
 * <p>
 * The added/removed/replaced attributes are defined by a separate value lists.
 * The valuesToAdd list defines the new attribute values to add to existing values.
 * The valuesToRemove list defines the attribute values that will be removed from existing attribute values.
 * The valuesToReplace list defines the new attribute values. In that case existing attribute values will be
 * removed and new attribute values from the valuesToReplace list will be placed in the attribute.
 * The delta may have add and remove lists at the same time. But if replace list is specified then no other
 * list may be present.
 * Empty list is not the same as null list, especially in the replace case. Null list means that there is no
 * modification of that particular type (add/remove/replace). Empty list means that there is a modification
 * of that particular type, but it does not include any value. This distinction is important especially for
 * the replace case. Delta with empty valuesToReplace list means that all existing values of an attribute
 * should be removed, but no new value is to be set in the attribute. The resulting state is attribute with
 * no values.
 * </p>
 * <p>
 * The delta does not guarantee ordering of the values. It is not guaranteed that the added attributes will
 * be appended at the end. Nor is the resulting order of values after application of remove delta guaranteed.
 * This behavior is connector-specific.
 * </p>
 * <p>
 * Password delta note: Password is often quite an special attribute. There are two related-but somehow
 * distinct password operations: password reset and password change. Password reset is usually initiated
 * by an administrator and it does not need old/current password value. It is represented as replace delta.
 * Password change is usually a self-service operation and it does require old/current password value.
 * Password change should be represented as add/delete delta, new password value being added, old/current
 * password value being removed. 
 * </p>
 * <p>
 * Terminology note: The term "delete" would be better than "remove", especially because "remove" may be
 * easily confused with "replace". But the framework is already using the term "remove", so we have preferred
 * naming consistency in this case.
 * </p>
 * <p>
 * The {@link AttributeDeltaBuilder} should be used to construct an instance of AttributeDelta.
 * </p>
 *
 * @author Radovan Semancik
 * @since 1.4.3
 */
public class AttributeDelta {

    /**
     * Name of the attribute
     */
    private final String name;

    /**
     * Attribute values to add
     */
    private final List<Object> valuesToAdd;

    /**
     * Attribute values to remove
     */
    private final List<Object> valuesToRemove;

    /**
     * Attribute values to replace
     */
    private final List<Object> valuesToReplace;

    /**
     * Create an attribute delta.
     */
    AttributeDelta(String name, List<Object> valuesToAdd, List<Object> valuesToRemove, List<Object> valuesToReplace) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Name must not be blank!");
        }
        // make this case insensitive
        this.name = name;
        // sanity
        if (valuesToReplace != null && (valuesToAdd != null || valuesToRemove != null)) {
            throw new IllegalArgumentException("Delta of attribute '" + name
                    + "' may be either replace or add/remove but not both at the same time");
        }
        // copy to prevent corruption..
        this.valuesToAdd = (valuesToAdd == null) ? null : CollectionUtil.newReadOnlyList(valuesToAdd);
        this.valuesToRemove = (valuesToRemove == null) ? null : CollectionUtil.newReadOnlyList(valuesToRemove);
        this.valuesToReplace = (valuesToReplace == null) ? null : CollectionUtil.newReadOnlyList(valuesToReplace);
    }

    public String getName() {
        return this.name;
    }

    public List<Object> getValuesToAdd() {
        return (this.valuesToAdd == null) ? null : Collections.unmodifiableList(this.valuesToAdd);
    }

    public List<Object> getValuesToRemove() {
        return (this.valuesToRemove == null) ? null : Collections.unmodifiableList(this.valuesToRemove);
    }

    public List<Object> getValuesToReplace() {
        return (this.valuesToReplace == null) ? null : Collections.unmodifiableList(this.valuesToReplace);
    }

    /**
     * Determines if the 'name' matches this {@link AttributeDelta}.
     */
    public boolean is(String name) {
        return namesEqual(this.name, name);
    }

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
        map.put("ValuesToAdd", getValuesToAdd());
        map.put("ValuesToRemove", getValuesToRemove());
        map.put("ValuesToReplace", getValuesToReplace());
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
        final AttributeDelta other = (AttributeDelta) obj;
        if (!is(other.name)) {
            return false;
        }

        if (!CollectionUtil.equals(valuesToAdd, other.valuesToAdd)) {
            return false;
        }

        if (!CollectionUtil.equals(valuesToRemove, other.valuesToRemove)) {
            return false;
        }

        if (!CollectionUtil.equals(valuesToReplace, other.valuesToReplace)) {
            return false;
        }

        return true;
    }
}
