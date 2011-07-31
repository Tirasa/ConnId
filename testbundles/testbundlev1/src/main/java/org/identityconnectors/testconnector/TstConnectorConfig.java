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

import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.operations.SyncOp;

public class TstConnectorConfig extends AbstractConfiguration {
    private String _tstField;

    private int _numResults;
    
    private boolean _failValidation;

    private boolean _resetConnectionCount;

    public TstConnectorConfig() {
        TstConnector.checkClassLoader();
    }

    
    public boolean getResetConnectionCount() {
        TstConnector.checkClassLoader();
        return _resetConnectionCount;
    }

    public void setResetConnectionCount( boolean count ) {
        TstConnector.checkClassLoader();
        _resetConnectionCount = count;
    }

    @ConfigurationProperty(operations={SyncOp.class})
    public String getTstField() {
        TstConnector.checkClassLoader();
        return _tstField;
    }

    public void setTstField(String value) {
        TstConnector.checkClassLoader();
        _tstField = value;
    }

    public int getNumResults() {
        TstConnector.checkClassLoader();
        return _numResults;
    }
    
    public void setNumResults(int numResults) {
        TstConnector.checkClassLoader();
        _numResults = numResults;
    }

    public boolean getFailValidation() {
        TstConnector.checkClassLoader();
        return _failValidation;
    }
    
    public void setFailValidation(boolean fail) {
        TstConnector.checkClassLoader();
        _failValidation = fail;
    }
    
    public void validate() {
        TstConnector.checkClassLoader();
        if (_failValidation) {
            throw new ConnectorException("validation failed "+CurrentLocale.get().getLanguage());
        }
    }

}
