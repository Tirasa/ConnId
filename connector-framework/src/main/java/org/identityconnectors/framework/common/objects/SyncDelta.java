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

import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.spi.operations.SyncOp;

/**
 * Represents a change to an object in a resource.
 *
 * @see SyncApiOp
 * @see SyncOp
 */
public final class SyncDelta {
    private final SyncToken token;
    private final SyncDeltaType deltaType;
    private final Uid previousUid;
    private final Uid uid;
    private final ConnectorObject connectorObject;

    /**
     * Creates a SyncDelta.
     *
     * @param token
     *            The token. Must not be null.
     * @param deltaType
     *            The delta. Must not be null.
     * @param uid
     *            The uid. Must not be null.
     * @param object
     *            The object that has changed. May be null for delete.
     */
    SyncDelta(SyncToken token, SyncDeltaType deltaType, Uid previousUid, Uid uid,
            ConnectorObject object) {
        Assertions.nullCheck(token, "token");
        Assertions.nullCheck(deltaType, "deltaType");
        Assertions.nullCheck(uid, "uid");

        // do not allow previous Uid for anything else than create or update
        if (previousUid != null && deltaType == SyncDeltaType.DELETE) {
            throw new IllegalArgumentException(
                    "The previous Uid can only be specified for create or update.");
        }

        // only allow null object for delete
        if (object == null && deltaType != SyncDeltaType.DELETE) {
            throw new IllegalArgumentException(
                    "ConnectorObject must be specified for anything other than delete.");
        }

        // if object not null, make sure its Uid matches
        if (object != null && !uid.equals(object.getUid())) {
            throw new IllegalArgumentException("Uid does not match that of the object.");
        }

        this.token = token;
        this.deltaType = deltaType;
        this.previousUid = previousUid;
        this.uid = uid;
        connectorObject = object;
    }

    /**
     * If the change described by this <code>SyncDelta</code> modified the
     * object's Uid, this method returns the Uid before the change. Not all
     * resources can determine the previous Uid, so this method can return
     * <code>null</code>.
     *
     * @return the previous Uid or null if it could not be determined or the
     *         change did not modify the Uid.
     */
    public Uid getPreviousUid() {
        return previousUid;
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
     * Returns the connector object that changed. This may be null in the case
     * of delete.
     *
     * @return The object or possibly null if this represents a delete.
     */
    public ConnectorObject getObject() {
        return connectorObject;
    }

    /**
     * Returns the <code>SyncToken</code> of the object that changed.
     *
     * @return the <code>SyncToken</code> of the object that changed.
     */
    public SyncToken getToken() {
        return token;
    }

    /**
     * Returns the type of the change the occured.
     *
     * @return The type of change that occured.
     */
    public SyncDeltaType getDeltaType() {
        return deltaType;
    }

    @Override
    public String toString() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("Token", token);
        values.put("DeltaType", deltaType);
        values.put("PreviousUid", previousUid);
        values.put("Uid", uid);
        values.put("Object", connectorObject);
        return values.toString();
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SyncDelta) {
            SyncDelta other = (SyncDelta) o;
            if (!token.equals(other.token)) {
                return false;
            }
            if (!deltaType.equals(other.deltaType)) {
                return false;
            }
            if (previousUid == null) {
                if (other.previousUid != null) {
                    return false;
                }
            } else if (!previousUid.equals(other.previousUid)) {
                return false;
            }
            if (!uid.equals(other.uid)) {
                return false;
            }
            if (connectorObject == null && other.connectorObject != null) {
                return false;
            } else if (!connectorObject.equals(other.connectorObject)) {
                return false;
            }
            return true;
        }
        return false;
    }
}
