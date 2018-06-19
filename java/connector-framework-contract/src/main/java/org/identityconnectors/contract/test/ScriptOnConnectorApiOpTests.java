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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.contract.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.junit.jupiter.api.Test;

/**
 * Contract test of {@link ScriptOnConnectorApiOp} operation.
 *
 * @author Zdenek Louzensky
 */
public class ScriptOnConnectorApiOpTests extends ContractTestBase {

    private static final Log LOG = Log.getLog(ScriptOnConnectorApiOpTests.class);

    public static final String TEST_NAME = "ScriptOnConnector";

    private static final String LANGUAGE_PROP_PREFIX = "language";

    private static final String SCRIPT_PROP_PREFIX = "script";

    private static final String ARGUMENTS_PROP_PREFIX = "arguments";

    private static final String RESULT_PROP_PREFIX = "result";

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> s = new HashSet<>();
        // list of required operations by this test:
        s.add(ScriptOnConnectorApiOp.class);
        return s;
    }

    /**
     * Tests running a script with correct values from property file.
     */
    @Test
    public void testRunScript() {
        // run test only in case operation is supported
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getAPIOperations())) {
            try {
                // get test properties - optional
                // if a property is not found test is skipped
                String language = (String) getDataProvider().getTestSuiteAttribute(
                        LANGUAGE_PROP_PREFIX, TEST_NAME);
                String script = (String) getDataProvider().getTestSuiteAttribute(
                        SCRIPT_PROP_PREFIX, TEST_NAME);
                @SuppressWarnings("unchecked")
                Map<String, Object> arguments = (Map<String, Object>) getDataProvider()
                        .getTestSuiteAttribute(ARGUMENTS_PROP_PREFIX, TEST_NAME);
                Object expResult = getDataProvider().getTestSuiteAttribute(RESULT_PROP_PREFIX,
                        TEST_NAME);

                // run the script
                Object result = getConnectorFacade().runScriptOnConnector(
                        new ScriptContext(language, script, arguments),
                        getOperationOptionsByOp(null, ScriptOnConnectorApiOp.class));

                // check that returned result was expected
                final String msg = "Script result was unexpected, expected: '%s', returned: '%s'.";
                assertEquals(expResult, result, String.format(msg, expResult, result));
            } catch (ObjectNotFoundException ex) {
                // ok - properties were not provided - test is skipped
                LOG.info("Test properties not set, skipping the test " + TEST_NAME);
            }
        } else {
            LOG.info("---------------------------------");
            LOG.info("Skipping test ''testRunScript''.");
            LOG.info("---------------------------------");
        }
    }

    /**
     * Tests running a script with unknown language.
     */
    @Test
    public void testRunScriptFailUnknownLanguage() {
        // run test only in case operation is supported
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getAPIOperations())) {
            try {
                getConnectorFacade().runScriptOnConnector(
                        new ScriptContext("NONEXISTING LANGUAGE", "script", new HashMap<>()), null);
                fail("Script language is not supported, should throw an exception.");
            } catch (RuntimeException ex) {
                // expected
            }
        } else {
            LOG.info("----------------------------------------------------");
            LOG.info("Skipping test ''testRunScriptFailUnknownLanguage''.");
            LOG.info("----------------------------------------------------");
        }
    }

    /**
     * Tests running a script with empty script text.
     */
    @Test
    public void testRunScriptFailEmptyScriptText() {
        // run test only in case operation is supported
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getAPIOperations())) {
            try {
                getConnectorFacade().runScriptOnConnector(
                        new ScriptContext("LANGUAGE", "", new HashMap<String, Object>()), null);
                fail("Script text is empty and script language is not probably supported, should throw an exception.");
            } catch (RuntimeException ex) {
                // expected
            }
        } else {
            LOG.info("----------------------------------------------------");
            LOG.info("Skipping test ''testRunScriptFailEmptyScriptText''.");
            LOG.info("----------------------------------------------------");
        }
    }
}
