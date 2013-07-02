/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.Version;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.swissbox.extender.BundleScanner;
import org.ops4j.pax.swissbox.extender.ManifestEntry;
import org.osgi.framework.Bundle;

/**
 * The ConnectorManifestScanner ...
 * 
 * @author Laszlo Hordos
 * @since 1.1
 */
public class ConnectorManifestScanner implements BundleScanner<ManifestEntry> {

    private static final String BUNDLE_PREFIX = "ConnectorBundle-";
    public static final String ATT_FRAMEWORK_VERSION = BUNDLE_PREFIX + "FrameworkVersion";
    public static final String ATT_BUNDLE_NAME = BUNDLE_PREFIX + "Name";
    public static final String ATT_BUNDLE_VERSION = BUNDLE_PREFIX + "Version";
    private final Version version;

    public ConnectorManifestScanner(Version version) {
        NullArgumentException.validateNotNull(version, "Version");
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ManifestEntry> scan(Bundle bundle) {
        NullArgumentException.validateNotNull(bundle, "Bundle");

        String frameworkVersion = null;
        String bundleName = null;
        String bundleVersion = null;

        final Dictionary<?,?> bundleHeaders = bundle.getHeaders();
        if (bundleHeaders != null && !bundleHeaders.isEmpty()) {
            final Enumeration<?> keys = bundleHeaders.keys();
            while (keys.hasMoreElements()) {
                final String key = (String) keys.nextElement();
                if (ATT_FRAMEWORK_VERSION.equals(key)) {
                    frameworkVersion = (String) bundleHeaders.get(key);
                    if (version.compareTo(Version.parse(frameworkVersion)) < 0) {
                        // Framework is incompatible
                        frameworkVersion = null;
                    }
                } else if (ATT_BUNDLE_NAME.equals(key)) {
                    bundleName = (String) bundleHeaders.get(key);
                } else if (ATT_BUNDLE_VERSION.equals(key)) {
                    bundleVersion = (String) bundleHeaders.get(key);
                }
            }
        }
        if (!StringUtil.isBlank(frameworkVersion) && !StringUtil.isBlank(bundleName)
                && !StringUtil.isBlank(bundleVersion)) {
            List<ManifestEntry> result = new ArrayList<ManifestEntry>(3);
            result.add(new ManifestEntry(ATT_FRAMEWORK_VERSION, frameworkVersion));
            result.add(new ManifestEntry(ATT_BUNDLE_NAME, bundleName));
            result.add(new ManifestEntry(ATT_BUNDLE_VERSION, bundleVersion));
            return result;
        } else {
            return Collections.emptyList();
        }
    }
}
