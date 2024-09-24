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
 * Portions Copyrighted 2015 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A ConnectorObject represents an object (e.g., an Account or a Group) on the
 * target resource. Each ConnectorObject represents a resource object as a UID
 * and a bag of attributes.
 * <p>
 * The developer of a Connector will use a {@link ConnectorObjectBuilder} to
 * construct instances of ConnectorObject.
 */
public final class ConnectorObject extends BaseConnectorObject {

    /**
     * Public only for serialization; please use {@link ConnectorObjectBuilder}.
     *
     * @throws IllegalArgumentException if {@link Name} or {@link Uid} is missing from the set.
     */
    public ConnectorObject(ObjectClass objectClass, Set<? extends Attribute> attributes) {
        super(objectClass, attributes);
        if (objectClass == null) {
            throw new IllegalArgumentException("ObjectClass may not be null");
        }
        // make sure the Uid was added..
        if (!this.attributeMap.containsKey(Uid.NAME)) {
            throw new IllegalArgumentException("The Attribute set must contain a 'Uid'.");
        }
        // make sure the Name attribute was added..
        if (!this.attributeMap.containsKey(Name.NAME)) {
            throw new IllegalArgumentException("The Attribute set must contain a 'Name'.");
        }
    }

    /**
     * Get the native identifier for this object.
     */
    public Uid getUid() {
        final Attribute uid = this.attributeMap.get(Uid.NAME);
        if (uid instanceof Uid) {
            return (Uid) uid;
        }
        throw new IllegalArgumentException("__UID__ attribute must be instance of Uid");
    }

    /**
     * Gets the {@link Name} of the object.
     */
    public Name getName() {
        final Attribute name = this.attributeMap.get(Name.NAME);
        if (name instanceof Name) {
            return (Name) name;
        }
        throw new IllegalArgumentException("__NAME__ attribute must be instance of Name");
    }

    @Override
    public ConnectorObjectIdentification getIdentification() {
        return new ConnectorObjectIdentification(
                getObjectClass(),
                Set.of(getUid(), getName()));
    }

    @Override
    public String toString() {
        // poor man's consistent toString()..
        Map<String, Object> map = new HashMap<>();
        map.put("Uid", this.getUid());
        map.put("ObjectClass", this.getObjectClass());
        map.put("Name", this.getName());
        map.put("Attributes", this.getAttributes());
        return map.toString();
    }
}
