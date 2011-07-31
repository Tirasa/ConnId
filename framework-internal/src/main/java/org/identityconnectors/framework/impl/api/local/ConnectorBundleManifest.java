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

/**
 * Model for the ConnectorBundleManifest. 
 * @see
 * <a href="https://identityconnectors.dev.java.net/connector_bundles.html">
 * https://identityconnectors.dev.java.net/connector_bundles.html
 * </a>
 */
public final class ConnectorBundleManifest {
    private String _frameworkVersion;
    private String _bundleName;
    private String _bundleVersion;
    
    public String getFrameworkVersion()
    {
        return _frameworkVersion;
    }
    
    public void setFrameworkVersion(String v)
    {
        _frameworkVersion = v;
    }
    
    public String getBundleName()
    {
        return _bundleName;
    }
    
    public void setBundleName(String name)
    {
        _bundleName = name;
    }
    
    public String getBundleVersion()
    {
        return _bundleVersion;
    }
    
    public void setBundleVersion(String ver)
    {
        _bundleVersion = ver;
    }    
   
}
