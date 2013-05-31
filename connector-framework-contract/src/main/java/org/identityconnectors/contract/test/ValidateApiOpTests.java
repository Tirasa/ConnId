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
 *
 * Portions Copyrighted 2012 ForgeRock AS
 *
 */
package org.identityconnectors.contract.test;


import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.ValidateApiOp;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/**
 * Contract test of {@link ValidateApiOp} operation.
 * Positive test for validate() is performed every time connector facade is created.
 */
@Test(testName = ValidateApiOpTests.TEST_NAME)
public class ValidateApiOpTests extends ContractTestBase {

    private static final String PROPERTY_NAME_INVALID_CONFIG = "invalidConfig";
    /**
     * Logging..
     */
    private static final Logger logger = Logger.getLogger(ValidateApiOpTests.class);
    public static final String TEST_NAME = "Validate";

    /**
     * Tests validate() with configuration that should NOT be correct.
     */
    @Test(dataProvider = "invalidConfig")
    public void testValidateFail(Object currentWrongConfigMap) {
        if (!(currentWrongConfigMap instanceof Map<?, ?>)) {
            fail(String.format("Test property '%s.%s.%s' contains other than Map properties.", TESTSUITE, TEST_NAME,
                    PROPERTY_NAME_INVALID_CONFIG));
        }
        Map<?, ?> currentWrongMapConfig = (Map<?, ?>) currentWrongConfigMap;

        _connFacade = ConnectorHelper
                .createConnectorFacadeWithWrongConfiguration(
                        getDataProvider(), currentWrongMapConfig);
        try {
            // should throw RuntimeException
            getConnectorFacade().validate();
            String msg = String
                    .format("Validate should throw RuntimeException because configuration should be invalid. Wrong properties used: \n%s",
                            currentWrongMapConfig.toString());
            fail(msg);
        } catch (RuntimeException ex) {
            // expected
        }
    }

    @DataProvider(name = "invalidConfig")
    public Iterator<Object[]> createData(final ITestContext context, Method method) {
        // READ THE TEST PROPERTY WITH WRONG CONFIGURATIONS THAT OVERRIDE THE DEFAULT CONFIGURATION
        Object o = null;
        try {
            o = getDataProvider().getTestSuiteAttribute(PROPERTY_NAME_INVALID_CONFIG, TEST_NAME);
        } catch (ObjectNotFoundException ex) {
            fail(String.format("Missing test property: '%s.%s.%s'", TESTSUITE,TEST_NAME,PROPERTY_NAME_INVALID_CONFIG));
        }

        if (!(o instanceof List<?>)) {
            fail(String.format("Test property '%s.%s.%s' should be of type List", TESTSUITE,TEST_NAME,PROPERTY_NAME_INVALID_CONFIG));
        }

        final Iterator<?> wrongConfigList = ((List<?>) o).iterator();

        return new Iterator<Object[]>() {
            public boolean hasNext() {
                return wrongConfigList.hasNext();
            }

            public Object[] next() {
                return new Object[]{wrongConfigList.next()};
            }

            public void remove() {
                wrongConfigList.remove();
            }
        };
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
