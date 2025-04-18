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
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.common.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;

/**
 * Builder class to create a {@link ConnectorObject}.
 *
 * The developer of a Connector will construct a ConnectorObjectBuilder, and then call the ConnectorObjectBuilder to set
 * a {@link Uid}, add attributes, and then finally to {@link #build()} the actual {@link ConnectorObject}.
 */
public final class ConnectorObjectBuilder {

    private ObjectClass objectClass = ObjectClass.ACCOUNT;

    private final Map<String, Attribute> attributeMap = new HashMap<>();

    // =======================================================================
    // Uid Setters
    // =======================================================================
    public ConnectorObjectBuilder setUid(final String uid) {
        addAttribute(new Uid(uid));
        return this;
    }

    public ConnectorObjectBuilder setUid(final Uid uid) {
        addAttribute(uid);
        return this;
    }

    // =======================================================================
    // Name Setter
    // =======================================================================
    public ConnectorObjectBuilder setName(final String name) {
        addAttribute(new Name(name));
        return this;
    }

    public ConnectorObjectBuilder setName(final Name name) {
        addAttribute(name);
        return this;
    }

    // =======================================================================
    // ObjectClass Setter
    // =======================================================================
    public ConnectorObjectBuilder setObjectClass(final ObjectClass oclass) {
        if (ObjectClass.ALL.equals(oclass)) {
            throw new IllegalArgumentException("Connector object class can not be type of __ALL__");
        }
        objectClass = oclass;
        return this;
    }

    // =======================================================================
    // Clone basically..
    // =======================================================================
    /**
     * Takes all the attribute from a {@link ConnectorObject} and add/overwrite
     * the current attributes.
     */
    public ConnectorObjectBuilder add(final ConnectorObject obj) {
        // simply add all the attributes
        for (Attribute attr : obj.getAttributes()) {
            addAttribute(attr);
        }
        setObjectClass(obj.getObjectClass());
        return this;
    }

    // =======================================================================
    // Attribute based methods..
    // =======================================================================
    /**
     * Adds one or many attributes to the {@link ConnectorObject}.
     */
    public ConnectorObjectBuilder addAttribute(final Attribute... attrs) {
        Assertions.nullCheck(attrs, "attrs");
        for (Attribute a : attrs) {
            attributeMap.put(a.getName(), a);
        }
        return this;
    }

    /**
     * Add all the {@link Attribute}s of a {@link Collection}.
     */
    public ConnectorObjectBuilder addAttributes(final Collection<Attribute> attrs) {
        Assertions.nullCheck(attrs, "attrs");
        for (Attribute a : attrs) {
            attributeMap.put(a.getName(), a);
        }
        return this;
    }

    /**
     * Adds values to the attribute.
     */
    public ConnectorObjectBuilder addAttribute(final String name, final Object... objs) {
        addAttribute(AttributeBuilder.build(name, objs));
        return this;
    }

    /**
     * Adds each object in the collection.
     */
    public ConnectorObjectBuilder addAttribute(final String name, final Collection<?> obj) {
        addAttribute(AttributeBuilder.build(name, obj));
        return this;
    }

    // =======================================================================
    // Build Out..
    // =======================================================================
    /**
     * Builds a 'ConnectorObject' based on the attributes and Uid provided.
     */
    public ConnectorObject build() {
        // check that there are attributes to return..
        if (attributeMap.isEmpty()) {
            throw new IllegalStateException("No attributes set!");
        }
        Set<Attribute> attrs = CollectionUtil.newReadOnlySet(attributeMap.values());
        return new ConnectorObject(objectClass, attrs);
    }

    public ConnectorObjectIdentification buildIdentification() {
        return new ConnectorObjectIdentification(objectClass, CollectionUtil.newReadOnlySet(attributeMap.values()));
    }
}
