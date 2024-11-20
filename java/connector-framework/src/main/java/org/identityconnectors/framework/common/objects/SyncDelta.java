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
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.common.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.spi.operations.SyncOp;

/**
 * Represents a change to an object in a resource.
 *
 * @see SyncApiOp
 * @see SyncOp
 */
public final class SyncDelta extends LiveSyncDelta {

    private final SyncToken token;

    private final SyncDeltaType deltaType;

    private final Uid previousUid;

    /**
     * Creates a SyncDelta.
     *
     * @param token The token. Must not be null.
     * @param deltaType The delta. Must not be null.
     * @param uid The uid. Must not be null.
     * @param object The object that has changed. May be null for delete.
     */
    SyncDelta(
            final SyncToken token,
            final SyncDeltaType deltaType,
            final Uid previousUid,
            final ObjectClass objectClass,
            final Uid uid,
            final ConnectorObject object) {

        super(objectClass, uid, object);

        Assertions.nullCheck(token, "token");
        Assertions.nullCheck(deltaType, "deltaType");

        // do not allow previous Uid for anything else than create or update
        if (previousUid != null && (deltaType == SyncDeltaType.DELETE || deltaType == SyncDeltaType.CREATE)) {
            throw new IllegalArgumentException(
                    "The previous Uid can only be specified for create_or_update or update.");
        }

        // only allow null object for delete
        if (object == null && deltaType != SyncDeltaType.DELETE) {
            throw new IllegalArgumentException(
                    "ConnectorObject must be specified for anything other than delete.");
        }

        this.token = token;
        this.deltaType = deltaType;
        this.previousUid = previousUid;
    }

    /**
     * If the change described by this <code>SyncDelta</code> modified the object's Uid, this method returns the Uid
     * before the change. Not all resources can determine the previous Uid, so this method can return {@code null}.
     *
     * @return the previous Uid or null if it could not be determined or the change did not modify the Uid.
     */
    public Uid getPreviousUid() {
        return previousUid;
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
        Map<String, Object> values = new HashMap<>();
        values.put("Token", token);
        values.put("DeltaType", deltaType);
        values.put("PreviousUid", previousUid);
        values.put("ObjectClass", getObjectClass());
        values.put("Uid", getUid());
        values.put("Object", getObject());
        return values.toString();
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 89 * hash + Objects.hashCode(this.token);
        hash = 89 * hash + Objects.hashCode(this.deltaType);
        hash = 89 * hash + Objects.hashCode(this.previousUid);
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
        final SyncDelta other = (SyncDelta) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (!Objects.equals(this.token, other.token)) {
            return false;
        }
        if (this.deltaType != other.deltaType) {
            return false;
        }
        return Objects.equals(this.previousUid, other.previousUid);
    }
}
