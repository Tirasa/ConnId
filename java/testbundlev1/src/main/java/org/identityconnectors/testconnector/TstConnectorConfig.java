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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 */
package org.identityconnectors.testconnector;

import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.operations.SyncOp;

public class TstConnectorConfig extends AbstractConfiguration {
    private String tstField;
    private String tst1Field;

    private int numResults;

    private boolean failValidation;

    private boolean resetConnectionCount;

    public TstConnectorConfig() {
        TstConnector.checkClassLoader();
    }


    public boolean getResetConnectionCount() {
        TstConnector.checkClassLoader();
        return resetConnectionCount;
    }

    public void setResetConnectionCount( boolean count ) {
        TstConnector.checkClassLoader();
        resetConnectionCount = count;
    }

    @ConfigurationProperty(operations={SyncOp.class})
    public String getTstField() {
        TstConnector.checkClassLoader();
        return tstField;
    }

    public void setTstField(String value) {
        TstConnector.checkClassLoader();
        tstField = value;
    }

    public String getTst1Field() {
        TstConnector.checkClassLoader();
        return tst1Field;
    }

    public void setTst1Field(String value) {
        TstConnector.checkClassLoader();
        tst1Field = value;
    }

    public int getNumResults() {
        TstConnector.checkClassLoader();
        return numResults;
    }

    public void setNumResults(int numResults) {
        TstConnector.checkClassLoader();
        this.numResults = numResults;
    }

    public boolean getFailValidation() {
        TstConnector.checkClassLoader();
        return failValidation;
    }

    public void setFailValidation(boolean fail) {
        TstConnector.checkClassLoader();
        failValidation = fail;
    }

    @Override
    public void validate() {
        TstConnector.checkClassLoader();
        if (failValidation) {
            throw new ConnectorException("validation failed "+CurrentLocale.get().getLanguage());
        }
    }

}
