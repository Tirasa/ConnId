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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;


/**
 * A ConnectorObject represents an object (e.g., an Account or a Group) on the
 * target resource. Each ConnectorObject represents a resource object as a UID
 * and a bag of attributes.
 * 
 * The developer of a Connector will use a {@link ConnectorObjectBuilder} to
 * construct instances of ConnectorObject.
 */
public final class ConnectorObject {
    final ObjectClass _objectClass;
    final Map<String, Attribute> attrs;

    /**
     * Public only for serialization; please use {@link ConnectorObjectBuilder}.
     * 
     * @throws IllegalArgumentException
     *             iff {@link Name} or {@link Uid} is missing from the set.
     */
    public ConnectorObject(ObjectClass objectClass,
            Set<? extends Attribute> set) {
        if (objectClass == null) {
            throw new IllegalArgumentException("ObjectClass may not be null");
        }
        if (set == null || set.size() == 0) {
            final String MSG = "The set can not be null or empty.";
            throw new IllegalArgumentException(MSG);
        }
        _objectClass = objectClass;
        // create an easy look map..
        this.attrs = AttributeUtil.toMap(set);
        // make sure the Uid was added..
        if (!this.attrs.containsKey(Uid.NAME)) {
            final String MSG = "The Attribute set must contain a 'Uid'.";
            throw new IllegalArgumentException(MSG);
        }
        // make sure the Name attribute was added..
        if (!this.attrs.containsKey(Name.NAME)) {
            final String MSG = "The Attribute set must contain a 'Name'.";
            throw new IllegalArgumentException(MSG);
        }
    }

    /**
     * Get the set of attributes that represent this object. This includes the
     * {@link Uid} and all {@link OperationalAttributes}.
     */
    public Set<Attribute> getAttributes() {
        // create a copy/unmodifiable set..
        return CollectionUtil.newReadOnlySet(this.attrs.values());
    }

    /**
     * Get an attribute by if it exists else null.
     */
    public Attribute getAttributeByName(String name) {
        // no need to clone since it has no setters
        return this.attrs.get(name);
    }

    /**
     * Get the native identifier for this object.
     */
    public Uid getUid() {
        return (Uid) this.attrs.get(Uid.NAME);
    }

    /**
     * Gets the {@link Name} of the object.
     */
    public Name getName() {
        return (Name) this.attrs.get(Name.NAME);
    }

    /**
     * Gets the {@link ObjectClass} for this object.
     */
    public ObjectClass getObjectClass() {
        return _objectClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnectorObject) {
            ConnectorObject other = (ConnectorObject)obj;
            if (!_objectClass.equals(other.getObjectClass())) {
                return false;
            }
            return CollectionUtil.equals(getAttributes(),other.getAttributes());                       
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
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Uid", this.getUid());
        map.put("ObjectClass", this.getObjectClass());
        map.put("Name", this.getName());
        map.put("Attributes", this.getAttributes());
        return map.toString();
    }

}
