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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.framework.impl.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoManagerImpl;
import org.identityconnectors.mockconnector.MockConfiguration;
import org.identityconnectors.mockconnector.MockConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ManagedConnectorFacadeFactoryTests {

    private static final String BUNDLE_NAME = "org.identityconnectors.mockconnector.bundle";

    @AfterEach
    public void clearFacadeCache() {
        ConnectorFacadeFactory.getManagedInstance().dispose();
    }

    /**
     * Two versions of one bundle installed side by side, configured identically. They share a connector facade key,
     * because that key is the serialized APIConfiguration and the ConnectorInfo is deliberately left out of it, so the
     * cache has to tell them apart by something else. Handing the second one the facade built for the first means an
     * operation runs the code of a bundle it did not select.
     */
    @Test
    public void versionsOfTheSameBundleDoNotShareAFacade() {
        LocalConnectorInfoImpl olderVersion = connectorInfo("1.0.0.0");
        LocalConnectorInfoImpl newerVersion = connectorInfo("2.0.0.0");

        String sharedFacadeKey = connectorFacadeKeyOf(olderVersion);
        assertEquals(sharedFacadeKey, connectorFacadeKeyOf(newerVersion),
                "the two versions must be indistinguishable by facade key for this test to mean anything");

        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getManagedInstance();
        ConnectorFacade olderFacade = factory.newInstance(olderVersion, sharedFacadeKey);
        ConnectorFacade newerFacade = factory.newInstance(newerVersion, sharedFacadeKey);

        assertNotSame(olderFacade, newerFacade,
                "each bundle version must get its own facade, otherwise the version used first answers for both");
    }

    @Test
    public void oneBundleVersionReusesItsCachedFacade() {
        LocalConnectorInfoImpl connectorInfo = connectorInfo("1.0.0.0");
        String facadeKey = connectorFacadeKeyOf(connectorInfo);

        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getManagedInstance();

        assertSame(factory.newInstance(connectorInfo, facadeKey),
                factory.newInstance(connectorInfo, facadeKey),
                "the same bundle version and configuration should still be served from the cache");
    }

    private static String connectorFacadeKeyOf(LocalConnectorInfoImpl connectorInfo) {
        return ConnectorFacadeFactory.getInstance()
                .newInstance(connectorInfo.createDefaultAPIConfiguration())
                .getConnectorFacadeKey();
    }

    private static LocalConnectorInfoImpl connectorInfo(String bundleVersion) {
        LocalConnectorInfoImpl info = new LocalConnectorInfoImpl();
        info.setConnectorClass(MockConnector.class);
        info.setConnectorConfigurationClass(MockConfiguration.class);
        info.setConnectorDisplayNameKey("DUMMY_DISPLAY_NAME");
        info.setConnectorKey(new ConnectorKey(BUNDLE_NAME, bundleVersion, MockConnector.class.getName()));
        info.setMessages(new ConnectorMessagesImpl());

        info.setDefaultAPIConfiguration(LocalConnectorInfoManagerImpl.createDefaultAPIConfiguration(info));
        return info;
    }
}
