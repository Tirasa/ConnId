/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock AS. All rights reserved.
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

package org.identityconnectors.framework.impl.api.osgi.internal;

import java.util.Hashtable;

import org.identityconnectors.common.event.ConnectorEventPublisher;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.ops4j.pax.swissbox.extender.BundleWatcher;
import org.ops4j.pax.swissbox.extender.ManifestEntry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample Class Doc
 *
 * @author Laszlo Hordos
 * @since 1.1
 */
public class Activator implements BundleActivator {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    /**
     * Bundle watcher of ConnectorBundle-.
     */
    private BundleWatcher<ManifestEntry> connectorWatcher;
    /**
     *
     */
    private ServiceRegistration connectorInfoManager;

    @SuppressWarnings("unchecked")
    @Override
    public void start(BundleContext context) throws Exception {
        LOG.debug("OpenICF OSGi Extender - Starting");

        OsgiConnectorInfoManagerImpl manager = new OsgiConnectorInfoManagerImpl();
        connectorWatcher =
                new BundleWatcher<ManifestEntry>(context, new ConnectorManifestScanner(
                        FrameworkUtil.getFrameworkVersion()), manager);
        connectorWatcher.start();

        Hashtable<String, String> prop = new Hashtable<String, String>();
        prop.put("ConnectorBundle-FrameworkVersion", FrameworkUtil.getFrameworkVersion()
                .getVersion());

        connectorInfoManager =
                context.registerService(new String[] { ConnectorInfoManager.class.getName(),
                    ConnectorEventPublisher.class.getName() }, manager, prop);

        LOG.debug("OpenICF OSGi Extender - Started");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOG.debug("OpenICF OSGi Extender - Stopping");
        connectorInfoManager.unregister();
        // Stop the bundle watcher.
        // This will result in un-publish of each web application that was
        // registered during the lifetime of
        // bundle watcher.
        if (connectorWatcher != null) {
            connectorWatcher.stop();
            connectorWatcher = null;
        }
        LOG.debug("OpenICF OSGi Extender - Stopped");
    }
}
