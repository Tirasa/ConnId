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
package org.identityconnectors.framework.impl.api.local;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

import org.identityconnectors.common.Version;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;


public final class ConnectorBundleManifestParser {

    private static final String BUNDLE_PREFIX = "ConnectorBundle-";
    
    private static final String ATT_FRAMEWORK_VERSION = BUNDLE_PREFIX + "FrameworkVersion";
    
    private static final String ATT_BUNDLE_NAME = BUNDLE_PREFIX + "Name";
    
    private static final String ATT_BUNDLE_VERSION = BUNDLE_PREFIX + "Version";

    private final String _fileName;
    private final Map<String, String> _attributes;
    
    public ConnectorBundleManifestParser(String fileName, Manifest manifest) {
        _fileName = fileName;
        _attributes = getAttributes(manifest);
    }
    
    private static Map<String, String> getAttributes(Manifest manifest) {
        HashMap<String, String> rv = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : manifest.getMainAttributes().entrySet()) {
            rv.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(rv);
    }

    /**
     * Parses the manifest.
     * @return The manifest. Note that the classes/classloaders will
     * not be populated yet. That is to be done at a higher-level.
     * @throws ConfigurationException If there were any structural problems.
     */
    public ConnectorBundleManifest parse() throws ConfigurationException {
        String frameworkVersion = getRequiredAttribute(ATT_FRAMEWORK_VERSION);
        String bundleName = getRequiredAttribute(ATT_BUNDLE_NAME);
        String bundleVersion =getRequiredAttribute(ATT_BUNDLE_VERSION);

        if (FrameworkUtil.getFrameworkVersion().compareTo(Version.parse(frameworkVersion)) < 0) {
            String message = "Bundle " + _fileName + " requests an unrecognized " +
                    "framework version " + frameworkVersion + " but available is " +
                    FrameworkUtil.getFrameworkVersion().getVersion();
            throw new ConfigurationException(message);
        }

        ConnectorBundleManifest rv = new ConnectorBundleManifest();

        rv.setFrameworkVersion(frameworkVersion);
        rv.setBundleName(bundleName);
        rv.setBundleVersion(bundleVersion);

        return rv;
    }

    private String getRequiredAttribute(String name) throws ConfigurationException {
        String rv = getAttribute(name);
        if ( rv == null ) {
            String msg = "Bundle " + _fileName + " is missing required attribute '" + name + "'.";
            throw new ConfigurationException(msg);
        }
        return rv;
    }

    private String getAttribute(String name) {
        return _attributes.get(name);
    }
}
