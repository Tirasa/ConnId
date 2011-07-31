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


import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.ValidateApiOp;
import org.junit.Test;

/**
 * Contract test of {@link ValidateApiOp} operation.
 * Positive test for validate() is performed every time connector facade is created.
 */
public class ValidateApiOpTests extends ContractTestBase {
    
    private static final String PROPERTY_NAME_INVALID_CONFIG = "invalidConfig";
    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(TestApiOpTests.class);
    private static final String TEST_NAME = "Validate";
    
    /**
     * Tests validate() with configuration that should NOT be correct.
     */
    @Test
    public void testValidateFail() {
        final String testPropertyName = "testsuite." + TEST_NAME + "."
                + PROPERTY_NAME_INVALID_CONFIG;
       
        // run test only in case operation is supported
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getAPIOperations())) {
            // READ THE TEST PROPERTY WITH WRONG CONFIGURATIONS THAT OVERRIDE THE DEFAULT CONFIGURATION
            Object o = null;
            try {
                 o = getDataProvider().getTestSuiteAttribute(PROPERTY_NAME_INVALID_CONFIG, TEST_NAME);
            } catch (ObjectNotFoundException ex) {
                fail(String.format("Missing test property: '%s'", testPropertyName));
            }
            
            if (!(o instanceof List<?>)) {
                fail(String.format("Test property '%s' should be of type List", testPropertyName));
            }
            
            final List<?> wrongConfigList = (List<?>) o;
            
            for (Object currentWrongConfigMap : wrongConfigList) {
                if (!(currentWrongConfigMap instanceof Map<?,?>)) {
                    fail(String.format("Test property '%s' contains other than Map properties.", testPropertyName));
                }
                Map<?,?> currentWrongMapConfig = (Map<?,?>) currentWrongConfigMap;
                
                _connFacade = ConnectorHelper
                        .createConnectorFacadeWithWrongConfiguration(
                                getDataProvider(), currentWrongMapConfig);
                try {
                    // should throw RuntimeException
                    getConnectorFacade().validate();
                    String msg = String.format("Validate should throw RuntimeException because configuration should be invalid. Wrong properties used: \n%s", currentWrongMapConfig.toString());
                    fail(msg);
                } catch (RuntimeException ex) {
                    // expected
                }
            }
        }
        else {
            LOG.info("--------------------------------");
            LOG.info("Skipping test ''testValidateFail''.");
            LOG.info("--------------------------------");
        }
    }

    /**
     * {@inheritDoc}     
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        s.add(ValidateApiOp.class);
        return s;
    }
}
