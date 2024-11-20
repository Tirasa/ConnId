/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2024 ConnId. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.api.operations.LiveSyncApiOp;
import org.identityconnectors.framework.spi.operations.LiveSyncOp;

/**
 * Represents a change to an object in a resource.
 *
 * @see LiveSyncApiOp
 * @see LiveSyncOp
 */
public class LiveSyncDelta {

    private final ObjectClass objectClass;

    private final Uid uid;

    private final ConnectorObject object;

    LiveSyncDelta(
            final ObjectClass objectClass,
            final Uid uid,
            final ConnectorObject object) {

        Assertions.nullCheck(uid, "uid");

        // if object not null, make sure its Uid matches
        if (object != null && !uid.attributeEquals(object.getUid())) {
            throw new IllegalArgumentException("Uid does not match that of the object.");
        }
        if (object != null && !objectClass.equals(object.getObjectClass())) {
            throw new IllegalArgumentException("ObjectClass does not match that of the object.");
        }

        this.objectClass = objectClass;
        this.uid = uid;
        this.object = object;
    }

    /**
     * If the change described by this <code>SyncDelta.DELETE</code> and the
     * deleted object value is <code>null</code>, this method returns the
     * ObjectClass of the deleted object. If operation syncs
     * {@link org.identityconnectors.framework.common.objects.ObjectClass#ALL}
     * this must be set, otherwise this method can return <code>null</code>.
     *
     * @return the ObjectClass of the deleted object.
     */
    public ObjectClass getObjectClass() {
        return objectClass;
    }

    /**
     * Returns the Uid of the connector object that changed.
     *
     * @return The Uid.
     */
    public Uid getUid() {
        return uid;
    }

    /**
     * Returns the connector object that changed. This may be null in the case of delete.
     *
     * @return The object or possibly null if this represents a delete.
     */
    public ConnectorObject getObject() {
        return object;
    }

    @Override
    public String toString() {
        Map<String, Object> values = new HashMap<>();
        values.put("ObjectClass", objectClass);
        values.put("Uid", uid);
        values.put("Object", object);
        return values.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.objectClass);
        hash = 67 * hash + Objects.hashCode(this.uid);
        hash = 67 * hash + Objects.hashCode(this.object);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LiveSyncDelta other = (LiveSyncDelta) obj;
        if (!Objects.equals(this.objectClass, other.objectClass)) {
            return false;
        }
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        return Objects.equals(this.object, other.object);
    }
}
