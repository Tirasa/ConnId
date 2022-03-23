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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.framework.impl.api.remote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.event.ConnectorEvent;
import org.identityconnectors.common.event.ConnectorEventHandler;
import org.identityconnectors.common.event.ConnectorEventPublisher;
import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.impl.api.remote.messages.ErrorResponse;
import org.identityconnectors.framework.impl.api.remote.messages.HelloRequest;
import org.identityconnectors.framework.impl.api.remote.messages.HelloResponse;

public class RemoteConnectorInfoManagerImpl implements ConnectorInfoManager,
        ConnectorEventPublisher, Runnable {

    /**
     * Logger.
     */
    private static final Log LOG = Log.getLog(RemoteConnectorInfoManagerImpl.class);

    private final RemoteFrameworkConnectionInfo frameworkConnectionInfo;

    private List<ConnectorInfo> connectorInfoList;

    private Long serverStartTime = null;

    private final List<ConnectorEventHandler> eventHandlers = new ArrayList<>();

    private RemoteConnectorInfoManagerImpl() {
        frameworkConnectionInfo = null;
    }

    public RemoteConnectorInfoManagerImpl(RemoteFrameworkConnectionInfo info)
            throws RuntimeException {
        this(info, true);
    }

    public RemoteConnectorInfoManagerImpl(RemoteFrameworkConnectionInfo info, boolean loadConnectorInfo) {
        frameworkConnectionInfo = info;
        if (loadConnectorInfo) {
            init();
        } else {
            connectorInfoList = null;
        }
    }

    private void init() {
        RemoteFrameworkConnection connection = new RemoteFrameworkConnection(frameworkConnectionInfo);
        HelloResponse response = null;
        try {
            connection.writeObject(CurrentLocale.get());
            connection.writeObject(frameworkConnectionInfo.getKey());
            connection.writeObject(new HelloRequest(HelloRequest.CONNECTOR_INFO));
            response = fetchHelloResponse(connection);
        } catch (Throwable e) {

            throw ConnectorException.wrap(e);
        } finally {
            connection.close();
        }
        if (null == response) {
            LOG.error("HelloResponse is null from {0}", frameworkConnectionInfo);
            throw new ConnectorIOException("HelloResponse is null from "
                    + frameworkConnectionInfo.toString());
        }
        if (response.getException() != null) {
            throw ConnectorException.wrap(response.getException());
        }

        List<RemoteConnectorInfoImpl> remoteInfos = response.getConnectorInfos();
        // populate transient fields not serialized
        remoteInfos.forEach((remoteInfo) -> {
            remoteInfo.setRemoteConnectionInfo(frameworkConnectionInfo);
        });

        List<ConnectorInfo> connectorInfoBefore = connectorInfoList;
        connectorInfoList = CollectionUtil.<ConnectorInfo>newReadOnlyList(remoteInfos);
        Object o = response.getServerInfo().get(HelloResponse.SERVER_START_TIME);
        if (o instanceof Long) {
            serverStartTime = (Long) o;
        } else {
            serverStartTime = System.currentTimeMillis();
        }

        // Notify all the listeners
        List<ConnectorInfo> unchanged = new ArrayList<>(connectorInfoList.size());
        if (null != connectorInfoBefore) {
            for (ConnectorInfo oldCi : connectorInfoBefore) {
                boolean unregistered = true;
                for (ConnectorInfo newCi : connectorInfoList) {
                    if (oldCi.getConnectorKey().equals(newCi.getConnectorKey())) {
                        unregistered = false;
                        unchanged.add(newCi);
                        break;
                    }
                }
                if (unregistered) {
                    notifyListeners(new ConnectorEvent(ConnectorEvent.CONNECTOR_UNREGISTERING,
                            oldCi.getConnectorKey()));
                }
            }
        }
        connectorInfoList.stream().
                filter((newCi) -> (!unchanged.contains(newCi))).
                forEachOrdered((newCi) -> {
                    notifyListeners(
                            new ConnectorEvent(ConnectorEvent.CONNECTOR_REGISTERED, newCi.getConnectorKey()));
                });
    }

    private HelloResponse fetchHelloResponse(RemoteFrameworkConnection connection) throws Throwable {

        Object response = connection.readObject();
        if (response instanceof HelloResponse) {

            return (HelloResponse) response;
        } else if (response instanceof ErrorResponse) {

            ErrorResponse error = (ErrorResponse) response;
            if (error.getException() != null) {
                throw error.getException();
            } else {

                throw new ConnectorException("Received an invalid Error response object, exception parameter missing");
            }
        } else {

            throw new ConnectorException("Received unknown response object type: " + response.getClass().getCanonicalName());
        }

    }

    /**
     * Derives another RemoteConnectorInfoManagerImpl with a different
     * RemoteFrameworkConnectionInfo but with the same metadata.
     *
     * @param info
     * @return
     */
    public RemoteConnectorInfoManagerImpl derive(RemoteFrameworkConnectionInfo info) {
        RemoteConnectorInfoManagerImpl rv = new RemoteConnectorInfoManagerImpl();
        if (null == connectorInfoList || connectorInfoList.isEmpty()) {
            rv.connectorInfoList = Collections.emptyList();
        } else {
            @SuppressWarnings("unchecked")
            List<RemoteConnectorInfoImpl> remoteInfos =
                    (List<RemoteConnectorInfoImpl>) SerializerUtil.cloneObject(connectorInfoList);
            remoteInfos.forEach((remoteInfo) -> {
                remoteInfo.setRemoteConnectionInfo(info);
            });
            rv.connectorInfoList = CollectionUtil.<ConnectorInfo>newReadOnlyList(remoteInfos);
        }
        return rv;
    }

    @Override
    public ConnectorInfo findConnectorInfo(ConnectorKey key) {
        for (ConnectorInfo info : getConnectorInfos()) {
            if (info.getConnectorKey().equals(key)) {
                return info;
            }
        }
        return null;
    }

    @Override
    public List<ConnectorInfo> getConnectorInfos() {
        List<ConnectorInfo> result = connectorInfoList;
        if (null == result) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            Map<String, Object> serverInfo = getServerInfo();
            Object o = serverInfo.get(HelloResponse.SERVER_START_TIME);
            if (o instanceof Long) {
                if (null == serverStartTime || ((Long) o) > serverStartTime) {
                    if (LOG.isOk()) {
                        if (null != serverStartTime) {
                            LOG.ok("Connector server has been restarted since {0}, new start time: {1}",
                                    new Date(serverStartTime), new Date((Long) o));
                        } else {
                            LOG.ok("First connection to connector server has been established, new start time: {0}",
                                    new Date((Long) o));
                        }
                    }
                    init();
                }
            }
        } catch (ConnectorIOException e) {
            // Server is unreachable and we notify all listeners
            if (null != connectorInfoList) {
                connectorInfoList.forEach((connectorInfo) -> {
                    notifyListeners(new ConnectorEvent(ConnectorEvent.CONNECTOR_UNREGISTERING,
                            connectorInfo.getConnectorKey()));
                });
            }
            connectorInfoList = null;
            LOG.error("Failed to connect to remote connector server {0}", frameworkConnectionInfo);
        } catch (Exception e) {
            LOG.error(e, "Failed to update the ConnectorInfo from remote connector server");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConnectorEventHandler(ConnectorEventHandler hook) {
        if (hook == null) {
            throw new NullPointerException();
        }
        if (!eventHandlers.contains(hook)) {
            if (null != connectorInfoList) {
                connectorInfoList.forEach((connectorInfo) -> {
                    hook.handleEvent(new ConnectorEvent(ConnectorEvent.CONNECTOR_REGISTERED,
                            connectorInfo.getConnectorKey()));
                });
            }
            eventHandlers.add(hook);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteConnectorEventHandler(ConnectorEventHandler hook) {
        eventHandlers.remove(hook);
    }

    public Map<String, Object> getServerInfo() throws RuntimeException {
        try (RemoteFrameworkConnection connection = new RemoteFrameworkConnection(frameworkConnectionInfo)) {
            connection.writeObject(CurrentLocale.get());
            connection.writeObject(frameworkConnectionInfo.getKey());
            connection.writeObject(new HelloRequest(HelloRequest.SERVER_INFO));
            HelloResponse response = (HelloResponse) connection.readObject();
            if (response.getException() instanceof ConnectorException) {
                throw (ConnectorException) response.getException();
            } else if (response.getException() != null) {
                throw ConnectorException.wrap(response.getException());
            }
            return response.getServerInfo();
        }
    }

    public List<ConnectorKey> getConnectorKeys() throws RuntimeException {
        try (RemoteFrameworkConnection connection = new RemoteFrameworkConnection(frameworkConnectionInfo)) {
            connection.writeObject(CurrentLocale.get());
            connection.writeObject(frameworkConnectionInfo.getKey());
            connection.writeObject(new HelloRequest(HelloRequest.CONNECTOR_KEY_LIST));
            HelloResponse response = (HelloResponse) connection.readObject();
            if (response.getException() instanceof ConnectorException) {
                throw (ConnectorException) response.getException();
            } else if (response.getException() != null) {
                throw ConnectorException.wrap(response.getException());
            }
            return response.getConnectorKeys();
        }
    }

    private void notifyListeners(ConnectorEvent event) {
        /*
         * a temporary array buffer, used as a snapshot of the state of current
         * Handlers.
         */
        Object[] arrLocal = eventHandlers.toArray();

        for (int i = arrLocal.length - 1; i >= 0; i--) {
            try {
                ((ConnectorEventHandler) arrLocal[i]).handleEvent(new ConnectorEvent(event));
            } catch (Throwable t) {
                /* ignore */
            }
        }
    }
}
