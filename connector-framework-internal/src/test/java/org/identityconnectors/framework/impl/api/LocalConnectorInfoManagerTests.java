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
package org.identityconnectors.framework.impl.api;

import org.testng.annotations.Test;
import org.testng.Assert;
import java.net.URL;
import java.util.List;

import org.identityconnectors.common.Version;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.common.FrameworkUtilTestHelpers;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;


public class LocalConnectorInfoManagerTests extends ConnectorInfoManagerTestBase {

    /**
     * Tests that the framework refuses to load a bundle that requests
     * a framework version newer than the one present.
     */
    @Test(priority = -1)
    public void testCheckVersion() throws Exception {
        // The test bundles require framework 1.0, so pretend the framework is older.
        FrameworkUtilTestHelpers.setFrameworkVersion(Version.parse("0.5"));
        try {
            getConnectorInfoManager();
            Assert.fail();
        } catch (ConfigurationException e) {
            if (!e.getMessage().contains("unrecognized framework version")) {
                Assert.fail();
            }
        }
    }

    /**
     * To be overridden by subclasses to get different ConnectorInfoManagers
     * @return
     * @throws Exception
     */
    protected ConnectorInfoManager getConnectorInfoManager() throws Exception {
        List<URL> urls = getTestBundles();
        ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
        ConnectorInfoManager manager = fact.getLocalManager(urls.toArray(new URL[0]));
        return manager;
    }

    protected void shutdownConnnectorInfoManager() {
        ConnectorFacadeFactory.getInstance().dispose();
        ConnectorInfoManagerFactory.getInstance().clearLocalCache();
    }
}
