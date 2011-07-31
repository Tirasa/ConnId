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

import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class MyTstConnection {
     private final int _connectionNumber;
    private boolean _isGood = true;

    public MyTstConnection(int connectionNumber) {
        _connectionNumber = connectionNumber;
    }

    public void test() {
        if (!_isGood) {
            throw new ConnectorException("Connection is bad");
        }
    }

    public void dispose() {
        _isGood = false;
    }

    public boolean isGood() {
        return _isGood;
    }

    public int getConnectionNumber() {
        return _connectionNumber;
    }
}

