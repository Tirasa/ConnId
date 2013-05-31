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
 *
 * Portions Copyrighted 2011-2013 ForgeRock
 */
package org.identityconnectors.framework.impl.api.remote.messages;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoImpl;

/**
 * Sent in response to a {@link HelloRequest}.
 */
public class HelloResponse implements Message {

    public static final String SERVER_START_TIME = "SERVER_START_TIME";
    /**
     * The exception
     */
    private Throwable exception;

    private Map<String, Object> serverInfo;

    /**
     * List of connector infos, containing infos for all the connectors on the
     * server.
     */
    private List<RemoteConnectorInfoImpl> connectorInfos;

    /**
     * List of connector keys, containing the keys of all the connectors on the
     * server.
     */
    private List<ConnectorKey> connectorKeys;

    public HelloResponse(Throwable exception, Map<String, Object> serverInfo,
            List<ConnectorKey> connectorKeys, List<RemoteConnectorInfoImpl> connectorInfos) {
        this.exception = exception;
        this.serverInfo = CollectionUtil.asReadOnlyMap(serverInfo);
        this.connectorKeys = CollectionUtil.newReadOnlyList(connectorKeys);
        this.connectorInfos = CollectionUtil.newReadOnlyList(connectorInfos);
    }

    public Throwable getException() {
        return exception;
    }

    public List<RemoteConnectorInfoImpl> getConnectorInfos() {
        return connectorInfos;
    }

    public List<ConnectorKey> getConnectorKeys() {
        return connectorKeys;
    }

    public Map<String, Object> getServerInfo() {
        return serverInfo;
    }

    public Date getStartTime() {
        Object time = getServerInfo().get(SERVER_START_TIME);
        if (time instanceof Long) {
            return new Date((Long) time);
        }
        return null;
    }
}
