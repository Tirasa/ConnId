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

import org.identityconnectors.common.CollectionUtil;

import java.util.*;

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
public class ComplexAttributeDelta extends BaseAttributeDelta {



    /**
     * Attribute values to add
     */
    private final List<ComplexValueDelta> valueDeltas;

    /**
     * Create an attribute delta.
     */
    ComplexAttributeDelta(String name, List<ComplexValueDelta> valueDeltas) {
        super(name);
        // copy to prevent corruption..
        this.valueDeltas = (valueDeltas == null) ? null : CollectionUtil.newReadOnlyList(valueDeltas);
    }

    public String getName() {
        return super.getName();
    }

    public List<ComplexValueDelta> getValueDeltas() {
        return valueDeltas;
    }

    protected void extendToStringMap(final Map<String, Object> map) {
        // Nothing to do here. Just for use in subclasses.
        map.put("valueDeltas", valueDeltas);
    }

    @Override
    public boolean equals(Object obj) {
        // test identity
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        };
        // test that the exact class matches
        if (!(getClass().equals(obj.getClass()))) {
            return false;
        }
        ComplexAttributeDelta other = (ComplexAttributeDelta) obj;

        if (!CollectionUtil.equals(valueDeltas, other.valueDeltas)) {
            return false;
        }

        return true;
    }

    public Attribute applyTo(Attribute attribute) {
        var values = attribute != null ? new ArrayList<>(attribute.getValue()) :  new ArrayList<>();
        for (ComplexValueDelta delta : valueDeltas) {
            delta.applyTo(values);
        }
        return new Attribute(getName(), values);
    }

}
