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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.api.remote;

import java.util.List;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.impl.api.remote.messages.HelloRequest;
import org.identityconnectors.framework.impl.api.remote.messages.HelloResponse;


public class RemoteConnectorInfoManagerImpl implements ConnectorInfoManager {

    
    private List<ConnectorInfo> _connectorInfo;
    
    private RemoteConnectorInfoManagerImpl() {
        
    }
    
    public RemoteConnectorInfoManagerImpl(RemoteFrameworkConnectionInfo info) 
        throws RuntimeException {
        RemoteFrameworkConnection connection = new RemoteFrameworkConnection(info);
        try {
            connection.writeObject(CurrentLocale.get());
            connection.writeObject(info.getKey());
            connection.writeObject(new HelloRequest());
            HelloResponse response = (HelloResponse)connection.readObject();
            if ( response.getException() != null ) {
                throw ConnectorException.wrap(response.getException());
            }
            List<RemoteConnectorInfoImpl> remoteInfos = 
            response.getConnectorInfos();
            //populate transient fields not serialized
            for (RemoteConnectorInfoImpl remoteInfo : remoteInfos ) {
                remoteInfo.setRemoteConnectionInfo(info);
            }
            _connectorInfo = 
                CollectionUtil.<ConnectorInfo>newReadOnlyList(remoteInfos);
        } 
        finally {
            connection.close();
        }
        
    }
    
    /**
     * Derives another RemoteConnectorInfoManagerImpl with
     * a different RemoteFrameworkConnectionInfo but with the
     * same metadata
     * @param info
     * @return
     */
    public RemoteConnectorInfoManagerImpl derive(RemoteFrameworkConnectionInfo info) {
        RemoteConnectorInfoManagerImpl rv = new RemoteConnectorInfoManagerImpl();
        @SuppressWarnings("unchecked")
        List<RemoteConnectorInfoImpl> remoteInfos = 
            (List<RemoteConnectorInfoImpl>)SerializerUtil.cloneObject(_connectorInfo);
        for (RemoteConnectorInfoImpl remoteInfo : remoteInfos) {
            remoteInfo.setRemoteConnectionInfo(info);
        }
        rv._connectorInfo = CollectionUtil.<ConnectorInfo>newReadOnlyList(remoteInfos);
        return rv;
    }
    
    public ConnectorInfo findConnectorInfo(ConnectorKey key) {
        for (ConnectorInfo info : _connectorInfo) {
            if ( info.getConnectorKey().equals(key) ) {
                return info;
            }
        }
        return null;
    }

    public List<ConnectorInfo> getConnectorInfos() {
        return _connectorInfo;
    }

}
