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
package org.identityconnectors.contract.test;

import java.util.Set;

import org.identityconnectors.contract.data.DataProvider;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.junit.After;
import org.junit.Before;


/**
 * Base class of all contract tests.
 * 
 * @author Zdenek Louzensky
 */
public abstract class ContractTestBase {

    private static DataProvider _dataProvider;
    
    protected ConnectorFacade _connFacade;

    private static void disposeDataProvider() {
    	if(_dataProvider != null) {
	        _dataProvider.dispose();       
    	}
    }
    
    /**
     * Initialize the environment needed to run the test. Called once per test method (@Before).
     */
    @Before
    public void init() {        
        _connFacade = ConnectorHelper.createConnectorFacade(getDataProvider());       
    }

    /**
     * Dispose the test environment, do the cleanup. Called once per test method (@After).
     */
    @After
    public void dispose() {
        _connFacade = null;
        disposeDataProvider();
    }
    
    /**
     * Ask the subclasses for the {@link APIOperation}.
     * Method returns set of required API operations that are prerequisites for
     * running certain contract test.
     */
    public abstract Set<Class<? extends APIOperation>> getAPIOperations();
    
    //=================================================================
    // Helper methods
    //=================================================================
    /**
     * Gets preconfigured {@link DataProvider} instance
     * @return {@link DataProvider}
     */
    public synchronized static DataProvider getDataProvider() {
        if (_dataProvider == null) {
            _dataProvider = ConnectorHelper.createDataProvider();
        }
        return _dataProvider;
    }
    
    /**
     * Always need a {@link ConnectorFacade}.
     */
    public ConnectorFacade getConnectorFacade() {
        return _connFacade;
    }
    
    /**
     * Gets OperationOptions suitable for specified operation.
     * Should be used in all tests requiring OperationOptions unless it's special case.
     * @return {@link OperationOptions}
     */
    public OperationOptions getOperationOptionsByOp(Class<? extends APIOperation> clazz) {
        return null;
    }
}
