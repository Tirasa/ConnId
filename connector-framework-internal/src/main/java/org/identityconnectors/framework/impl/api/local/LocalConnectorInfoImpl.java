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
package org.identityconnectors.framework.impl.api.local;

import org.identityconnectors.framework.impl.api.AbstractConnectorInfo;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoImpl;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;

public class LocalConnectorInfoImpl extends AbstractConnectorInfo {

    private Class<? extends Connector> connectorClass;
    private Class<? extends Configuration> connectorConfigurationClass;

    public LocalConnectorInfoImpl() {

    }

    public RemoteConnectorInfoImpl toRemote() {
        RemoteConnectorInfoImpl rv = new RemoteConnectorInfoImpl();
        rv.setConnectorDisplayNameKey(getConnectorDisplayNameKey());
        rv.setConnectorKey(getConnectorKey());
        rv.setDefaultAPIConfiguration(getDefaultAPIConfiguration());
        rv.setMessages(getMessages());
        return rv;
    }

    public Class<? extends Configuration> getConnectorConfigurationClass() {
        return connectorConfigurationClass;
    }

    public void setConnectorConfigurationClass(Class<? extends Configuration> c) {
        connectorConfigurationClass = c;
    }

    public Class<? extends Connector> getConnectorClass() {
        return connectorClass;
    }

    public void setConnectorClass(Class<? extends Connector> clazz) {
        connectorClass = clazz;
    }
}
