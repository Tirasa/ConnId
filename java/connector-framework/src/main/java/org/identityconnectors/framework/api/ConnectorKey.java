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
package org.identityconnectors.framework.api;

/**
 * Uniquely identifies a connector within an installation. Consists of the
 * triple (bundleName, bundleVersion, connectorName)
 */
public final class ConnectorKey {
    private final String bundleName;
    private final String bundleVersion;
    private final String connectorName;

    public ConnectorKey(String bundleName, String bundleVersion, String connectorName) {
        if (bundleName == null) {
            throw new IllegalArgumentException("bundleName may not be null");
        }
        if (bundleVersion == null) {
            throw new IllegalArgumentException("bundleVersion may not be null");
        }
        if (connectorName == null) {
            throw new IllegalArgumentException("connectorName may not be null");
        }
        this.bundleName = bundleName;
        this.bundleVersion = bundleVersion;
        this.connectorName = connectorName;
    }

    public String getBundleName() {
        return bundleName;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    public String getConnectorName() {
        return connectorName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConnectorKey) {
            ConnectorKey other = (ConnectorKey) o;
            if (!bundleName.equals(other.bundleName)) {
                return false;
            }
            if (!bundleVersion.equals(other.bundleVersion)) {
                return false;
            }
            if (!connectorName.equals(other.connectorName)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int rv = 0;
        rv ^= connectorName.hashCode();
        return rv;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConnectorKey(");
        builder.append(" bundleName=").append(bundleName);
        builder.append(" bundleVersion=").append(bundleVersion);
        builder.append(" connectorName=").append(connectorName);
        builder.append(" )");
        return builder.toString();
    }
}
