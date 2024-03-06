/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2024 Evolveum. All rights reserved.
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

import org.identityconnectors.common.CollectionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helps implementing {@link ConnectorObject} and {@link ConnectorObjectIdentification}.
 */
public abstract class BaseConnectorObject {

    private final ObjectClass objectClass;
    final Map<String, Attribute> attributeMap;

    BaseConnectorObject(ObjectClass objectClass, Set<? extends Attribute> attributes) {
        // For connector object identification, it is legal to have no object class information.
        if (ObjectClass.ALL.equals(objectClass)) {
            throw new IllegalArgumentException("Connector object class can not be type of __ALL__");
        }
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("The set can not be null or empty.");
        }
        this.objectClass = objectClass;
        // create an easy look map..
        this.attributeMap = AttributeUtil.toMap(attributes);
    }

    /**
     * Get the set of attributes that represent this object.
     * <p>
     * This includes the {@link Uid} and all {@link OperationalAttributes}.
     */
    public Set<Attribute> getAttributes() {
        // create a copy/unmodifiable set..
        return CollectionUtil.newReadOnlySet(this.attributeMap.values());
    }

    /**
     * Get an attribute by if it exists else null.
     */
    public Attribute getAttributeByName(String name) {
        // no need to clone since it has no setters
        return this.attributeMap.get(name);
    }

    /**
     * Gets the {@link ObjectClass} for this object.
     * This is the "structural" object class. The primary object class that defines
     * basic object structure. It cannot be null.
     */
    public ObjectClass getObjectClass() {
        return objectClass;
    }

    /**
     * Returns the identification of this object. For full objects it means providing {@link Name} and {@link Uid} attributes.
     * (We have no other way of telling what attributes are the identifiers.)
     */
    public abstract ConnectorObjectIdentification getIdentification();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseConnectorObject) {
            BaseConnectorObject other = (BaseConnectorObject) obj;
            if (!objectClass.equals(other.getObjectClass())) {
                return false;
            }
            return CollectionUtil.equals(getAttributes(), other.getAttributes());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getAttributes().hashCode();
    }

    @Override
    public String toString() {
        // poor man's consistent toString()..
        Map<String, Object> map = new HashMap<>();
        map.put("ObjectClass", this.getObjectClass());
        map.put("Attributes", this.getAttributes());
        return this.getClass().getSimpleName() + ": " + map;
    }
}
