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

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.ResolveUsernameApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

/**
 * tests for {@link ResolveUsernameApiOp}
 *
 * @author David Adam
 */
@Test(testName =  ResolveUsernameApiOpTests.TEST_NAME)
public class ResolveUsernameApiOpTests extends ObjectClassRunner {

    public static final String TEST_NAME = "ResolveUsername";
    private static final String WRONG_USERNAME = "wrong.username";

    @Override
    public String getTestName() {
        return TEST_NAME;
    }

    @Override
    protected void testRun(ObjectClass objectClass) {
        // empty on purpose.
    }

    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testPositive(ObjectClass objectClass) {
        if (!ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())) {
            return;
        }

        Uid uid = null;
        try {
            /*
             * create a new user
             */
            Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(objectClass), AuthenticationApiOpTests.TEST_NAME, 0, true, false);
            uid = getConnectorFacade().create(objectClass, attrs,
                    getOperationOptionsByOp(objectClass, CreateApiOp.class));

            // get the user to make sure it exists now
            ConnectorObject obj = getConnectorFacade().getObject(objectClass, uid,
                    getOperationOptionsByOp(objectClass, GetApiOp.class));
            Assert.assertNotNull(obj,"Unable to retrieve newly created object");

            // compare requested attributes to retrieved attributes
            ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, attrs);

            /*
             * try resolving the new user
             */
            // get username
            String username = (String) getDataProvider().getTestSuiteAttribute(objectClass.getObjectClassValue() + "." + AuthenticationApiOpTests.USERNAME_PROP, AuthenticationApiOpTests.TEST_NAME);
            Uid result = getConnectorFacade().resolveUsername(objectClass, username, null);
            Assert.assertEquals(uid, result);
        } finally {
            if (uid != null) {
                // delete the object
                getConnectorFacade().delete(objectClass, uid,
                        getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        }
    }

    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testNegative(ObjectClass objectClass) {
        if (!ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())) {
            return;
        }

        String wrongUsername = null;
        try {
            // retrieves an optional value for wrong username
            wrongUsername = (String) getDataProvider().getTestSuiteAttribute(WRONG_USERNAME, AuthenticationApiOpTests.TEST_NAME);
        } catch (Exception ex) {
            wrongUsername = "unresolvableUsername";
        }

        try {
            getConnectorFacade().resolveUsername(objectClass, wrongUsername, null);
            Assert.fail("Runtime exception should be thrown when attempt to resolve non-existing user: '" + wrongUsername + "'");
        } catch (RuntimeException ex) {
            // OK
        }

    }

    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> requiredOps = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        requiredOps.add(CreateApiOp.class);
        requiredOps.add(DeleteApiOp.class);
        requiredOps.add(ResolveUsernameApiOp.class);
        return requiredOps;
    }
}
