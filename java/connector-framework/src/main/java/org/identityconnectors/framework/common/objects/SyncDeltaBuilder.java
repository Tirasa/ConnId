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

/**
 * Builder for {@link SyncDelta}.
 */
public final class SyncDeltaBuilder {
    private SyncToken syncToken;
    private SyncDeltaType deltaType;
    private Uid previousUid;
    private Uid uid;
    private ConnectorObject connectorObject;

    /**
     * Create a new <code>SyncDeltaBuilder</code>
     */
    public SyncDeltaBuilder() {

    }

    /**
     * Creates a new <code>SyncDeltaBuilder</code> whose values are initialized
     * to those of the delta.
     *
     * @param delta
     *            The original delta.
     */
    public SyncDeltaBuilder(SyncDelta delta) {
        syncToken = delta.getToken();
        deltaType = delta.getDeltaType();
        connectorObject = delta.getObject();
        previousUid = delta.getPreviousUid();
        uid = delta.getUid();
    }

    /**
     * Returns the <code>SyncToken</code> of the object that changed.
     *
     * @return the <code>SyncToken</code> of the object that changed.
     */
    public SyncToken getToken() {
        return syncToken;
    }

    /**
     * Sets the <code>SyncToken</code> of the object that changed.
     *
     * @param token
     *            the <code>SyncToken</code> of the object that changed.
     */
    public SyncDeltaBuilder setToken(SyncToken token) {
        syncToken = token;
        return this;
    }

    /**
     * Returns the type of the change that occurred.
     *
     * @return The type of change that occurred.
     */
    public SyncDeltaType getDeltaType() {
        return deltaType;
    }

    /**
     * Sets the type of the change that occurred.
     *
     * @param type
     *            The type of change that occurred.
     */
    public SyncDeltaBuilder setDeltaType(SyncDeltaType type) {
        deltaType = type;
        return this;
    }

    /**
     * Gets the Uid of the object before the change.
     *
     * @return The Uid of the object before the change.
     */
    public Uid getPreviousUid() {
        return previousUid;
    }

    /**
     * Sets the Uid of the object before the change.
     *
     * @param previousUid
     *            The Uid of the object before the change.
     */
    public void setPreviousUid(Uid previousUid) {
        this.previousUid = previousUid;
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
     * @param uid
     *            The Uid of the object that changed.
     */
    public SyncDeltaBuilder setUid(Uid uid) {
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
     * Sets the object that changed and implicitly sets Uid if object is not
     * null.
     *
     * @param object
     *            The object that changed. May be null for deletes.
     */
    public SyncDeltaBuilder setObject(ConnectorObject object) {
        connectorObject = object;
        if (object != null) {
            uid = object.getUid();
        }
        return this;
    }

    /**
     * Creates a SyncDelta. Prior to calling the following must be specified:
     * <ol>
     * <li>{@link #setObject(ConnectorObject) Object} (for anything other than
     * delete)</li>
     * <li>{@link #setUid(Uid) Uid} (this is implictly set when calling
     * {@link #setObject(ConnectorObject)})</li>
     * <li>{@link #setToken(SyncToken) Token}</li>
     * <li>{@link #setDeltaType(SyncDeltaType) DeltaType}</li>
     * </ol>
     */
    public SyncDelta build() {
        return new SyncDelta(syncToken, deltaType, previousUid, uid, connectorObject);
    }
}
