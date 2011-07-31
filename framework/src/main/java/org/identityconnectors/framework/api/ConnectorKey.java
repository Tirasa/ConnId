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
package org.identityconnectors.framework.api;

/**
 * Uniquely identifies a connector within an installation.
 * Consists of the triple (bundleName, bundleVersion, connectorName)
 */
public final class ConnectorKey {
    private final String _bundleName;
    private final String _bundleVersion;
    private final String _connectorName;
    
    public ConnectorKey(String bundleName,
            String bundleVersion,
            String connectorName) {
        if (bundleName == null) {
            throw new IllegalArgumentException("bundleName may not be null");
        }
        if (bundleVersion == null) {
            throw new IllegalArgumentException("bundleVersion may not be null");            
        }
        if (connectorName == null) {
            throw new IllegalArgumentException("connectorName may not be null");            
        }
        _bundleName    = bundleName;
        _bundleVersion = bundleVersion;
        _connectorName = connectorName;
    }
    
    public String getBundleName() {
        return _bundleName;
    }
    
    public String getBundleVersion() {
        return _bundleVersion;
    }
    
    public String getConnectorName() {
        return _connectorName;
    }
    
    @Override
    public boolean equals(Object o) {
        if ( o instanceof ConnectorKey ) {
            ConnectorKey other = (ConnectorKey)o;
            if (!_bundleName.equals(other._bundleName)) {
                return false;
            }
            if (!_bundleVersion.equals(other._bundleVersion)) {
                return false;
            }
            if (!_connectorName.equals(other._connectorName)) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int rv = 0;
        rv ^= _connectorName.hashCode();
        return rv;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConnectorKey(");
        builder.append(" bundleName=").append(_bundleName);
        builder.append(" bundleVersion=").append(_bundleVersion);
        builder.append(" connectorName=").append(_connectorName);
        builder.append(" )");
        return builder.toString();
    }
}
