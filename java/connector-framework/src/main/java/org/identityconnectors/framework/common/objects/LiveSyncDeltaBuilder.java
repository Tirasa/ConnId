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

/**
 * Builder for {@link LiveSyncDelta}.
 */
public final class LiveSyncDeltaBuilder {

    private ObjectClass objectClass;

    private Uid uid;

    private ConnectorObject connectorObject;

    /**
     * Create a new <code>LiveSyncDeltaBuilder</code>
     */
    public LiveSyncDeltaBuilder() {
    }

    /**
     * Creates a new <code>LiveSyncDeltaBuilder</code> whose values are initialized to those of the delta.
     *
     * @param delta the original delta.
     */
    public LiveSyncDeltaBuilder(final LiveSyncDelta delta) {
        connectorObject = delta.getObject();
        objectClass = delta.getObjectClass();
        uid = delta.getUid();
    }

    /**
     * Gets the ObjectClass of the object that deleted.
     *
     * @return The ObjectClass of the object that deleted.
     */
    public ObjectClass getObjectClass() {
        return objectClass;
    }

    /**
     * Sets the ObjectClass of the object that deleted. Note that this is
     * implicitly set when you call {@link #setObject(ConnectorObject)}.
     *
     * @param objectClass The ObjectClass of the object that changed.
     */
    public LiveSyncDeltaBuilder setObjectClass(final ObjectClass objectClass) {
        this.objectClass = objectClass;
        return this;
    }

    /**
     * Gets the Uid of the object that changed.
     *
     * @return The Uid of the object that changed.
     */
    public Uid getUid() {
        return uid;
    }

    /**
     * Sets the Uid of the object that changed. Note that this is implicitly set
     * when you call {@link #setObject(ConnectorObject)}.
     *
     * @param uid The Uid of the object that changed.
     */
    public LiveSyncDeltaBuilder setUid(final Uid uid) {
        this.uid = uid;
        return this;
    }

    /**
     * Returns the object that changed.
     *
     * @return The object that changed. May be null for deletes.
     */
    public ConnectorObject getObject() {
        return connectorObject;
    }

    /**
     * Sets the object that changed and implicitly sets Uid if object is not null.
     *
     * @param object The object that changed. May be null for deletes.
     */
    public LiveSyncDeltaBuilder setObject(final ConnectorObject object) {
        connectorObject = object;
        if (object != null) {
            uid = object.getUid();
            objectClass = object.getObjectClass();
        }
        return this;
    }

    /**
     * Creates a LiveSyncDelta. Prior to calling the following must be specified:
     * <ol>
     * <li>{@link #setObject(ConnectorObject) Object}</li>
     * <li>{@link #setUid(Uid) Uid} (this is implictly set when calling {@link #setObject(ConnectorObject)})</li>
     * </ol>
     */
    public LiveSyncDelta build() {
        return new LiveSyncDelta(objectClass, uid, connectorObject);
    }
}
