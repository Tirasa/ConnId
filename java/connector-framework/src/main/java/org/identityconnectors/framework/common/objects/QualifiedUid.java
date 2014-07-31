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

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * A fully-qualified uid. That is, a pair of {@link ObjectClass} and
 * {@link Uid}.
 */
public final class QualifiedUid {
    private final ObjectClass objectClass;
    private final Uid uid;

    /**
     * Create a QualifiedUid.
     *
     * @param objectClass
     *            The object class. May not be null.
     * @param uid
     *            The uid. May not be null.
     */
    public QualifiedUid(ObjectClass objectClass, Uid uid) {
        Assertions.nullCheck(objectClass, "objectClass");
        Assertions.nullCheck(uid, "uid");
        this.objectClass = objectClass;
        this.uid = uid;
    }

    /**
     * Returns the object class.
     *
     * @return The object class.
     */
    public ObjectClass getObjectClass() {
        return objectClass;
    }

    /**
     * Returns the uid.
     *
     * @return The uid.
     */
    public Uid getUid() {
        return uid;
    }

    /**
     * Returns true if o is a QualifiedUid and the object class and uid match.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof QualifiedUid) {
            QualifiedUid other = (QualifiedUid) o;
            return (objectClass.equals(other.objectClass) && uid.equals(other.uid));
        }
        return false;
    }

    /**
     * Returns a hash code based on uid
     */
    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    /**
     * Returns a string representation acceptible for debugging.
     */
    @Override
    public String toString() {
        return SerializerUtil.serializeXmlObject(this, false);
    }

}
