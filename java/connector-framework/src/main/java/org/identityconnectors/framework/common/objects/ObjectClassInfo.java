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
 * Portions Copyrighted 2015 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import static org.identityconnectors.framework.common.objects.NameUtil.nameHashCode;
import static org.identityconnectors.framework.common.objects.NameUtil.namesEqual;

import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * Definition of an object class.
 *
 * @author Will Droste
 * @since 1.0
 */
public final class ObjectClassInfo {

    private final String type;

    private final Set<AttributeInfo> attributeInfos;

    private final boolean isContainer;

    private final boolean isAuxiliary;

    private final boolean isEmbedded;

    /**
     * Public only for serialization; Use ObjectClassInfoBuilder instead.
     *
     * @param type The name of the object class
     * @param attrInfo The attributes of the object class.
     * @param isContainer True if this can contain other object classes.
     */
    public ObjectClassInfo(
            final String type,
            final Set<AttributeInfo> attrInfo,
            final boolean isContainer,
            final boolean isAuxiliary,
            final boolean isEmbedded) {

        Assertions.nullCheck(type, "type");
        this.type = type;
        this.attributeInfos = CollectionUtil.newReadOnlySet(attrInfo);
        this.isContainer = isContainer;
        this.isAuxiliary = isAuxiliary;
        this.isEmbedded = isEmbedded;
        // check to make sure name exists and if not throw
        Map<String, AttributeInfo> map = AttributeInfoUtil.toMap(attrInfo);
        if (!map.containsKey(Name.NAME)) {
            throw new IllegalArgumentException("Missing 'Name' attribute info.");
        }
    }

    public boolean isContainer() {
        return isContainer;
    }

    /**
     * Returns flag indicating whether this is a definition of auxiliary object class.
     * Auxiliary object classes define additional characteristics of the object.
     */
    public boolean isAuxiliary() {
        return isAuxiliary;
    }

    /**
     * If {@code true}, objects of this class are meant to be embedded in other objects.
     * (They may or may not be queryable or updatable directly.)
     *
     * Currently, this information serves just as a hint for the client code. In the future,
     * we may relax some of requirements on embedded objects, for example, they may not need to have
     * the {@link Name} and/or {@link Uid} attributes.
     */
    public boolean isEmbedded() {
        return isEmbedded;
    }

    public Set<AttributeInfo> getAttributeInfo() {
        return CollectionUtil.newReadOnlySet(attributeInfos);
    }

    public String getType() {
        return type;
    }

    /**
     * Determines if the 'name' matches this {@link ObjectClassInfo}.
     *
     * @param name case-insensitive string representation of the ObjectClassInfo's type.
     * @return <code>true</code> if the case insensitive type is equal to that of the one in this
     * {@link ObjectClassInfo}.
     */
    public boolean is(final String name) {
        return namesEqual(type, name);
    }

    @Override
    public boolean equals(final Object obj) {
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

        ObjectClassInfo other = (ObjectClassInfo) obj;

        if (!is(other.getType())) {
            return false;
        }
        if (!CollectionUtil.equals(getAttributeInfo(), other.getAttributeInfo())) {
            return false;
        }
        if (!isContainer == other.isContainer) {
            return false;
        }
        if (!isAuxiliary == other.isAuxiliary) {
            return false;
        }
        return !isEmbedded != other.isEmbedded;
    }

    @Override
    public int hashCode() {
        return nameHashCode(type);
    }

    @Override
    public String toString() {
        return SerializerUtil.serializeXmlObject(this, false);
    }
}
