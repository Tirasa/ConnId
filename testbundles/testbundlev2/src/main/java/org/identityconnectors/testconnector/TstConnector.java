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
package org.identityconnectors.testconnector;

import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.AuthenticateOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.testcommon.TstCommon;

@ConnectorClass(configurationClass=TstConnectorConfig.class,
        displayNameKey="TestConnector")
public class TstConnector implements CreateOp, AuthenticateOp, Connector {

    private Configuration _config;

    public Uid create(ObjectClass oclass, Set<Attribute> attrs, OperationOptions options) {
        String version = TstCommon.getVersion();
        return new Uid(version);
    }
    
    public Uid authenticate(ObjectClass oclass, String username, GuardedString password, OperationOptions options) {
        // The native library is an empty file, so this should fail (and tests expect it).
        System.loadLibrary("native");
        throw new AssertionError("The loadLibrary call did not fail");
    }
    
    public void init(Configuration cfg) {
        _config = cfg;
    }

    public Configuration getConfiguration() {
        return _config;
    }

    public void dispose() {
        
    }

    public Schema getSchema() {
        return null;
    }
}
