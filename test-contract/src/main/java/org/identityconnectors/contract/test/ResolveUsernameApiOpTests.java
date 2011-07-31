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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * tests for {@link ResolveUsernameApiOp}
 * 
 * @author David Adam
 */
@RunWith(Parameterized.class)
public class ResolveUsernameApiOpTests extends ObjectClassRunner {

    private static final String TEST_NAME = "ResolveUsername";
    private static final String WRONG_USERNAME = "wrong.username";

    public ResolveUsernameApiOpTests(ObjectClass oclass) {
        super(oclass);
    }

    @Override
    public String getTestName() {
        return TEST_NAME;
    }

    @Override
    public void testRun() {
        // empty on purpose.
    }

    @Test
    public void testPositive() {
        if (!ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(), getAPIOperations())) {
            return;
        }

        Uid uid = null;
        try {
            /*
             * create a new user
             */
            Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(), AuthenticationApiOpTests.TEST_NAME, 0, true, false);
            uid = getConnectorFacade().create(getObjectClass(), attrs,
                    getOperationOptionsByOp(CreateApiOp.class));

            // get the user to make sure it exists now
            ConnectorObject obj = getConnectorFacade().getObject(getObjectClass(), uid,
                    getOperationOptionsByOp(GetApiOp.class));
            Assert.assertNotNull("Unable to retrieve newly created object", obj);
        
            // compare requested attributes to retrieved attributes
            ConnectorHelper.checkObject(getObjectClassInfo(), obj, attrs);
        
            /*
             * try resolving the new user
             */
            // get username
            String username = (String) getDataProvider().getTestSuiteAttribute(getObjectClass().getObjectClassValue() + "." + AuthenticationApiOpTests.USERNAME_PROP, AuthenticationApiOpTests.TEST_NAME);
            Uid result = getConnectorFacade().resolveUsername(getObjectClass(), username, null);
            Assert.assertEquals(uid, result);
        } finally {
            if (uid != null) {
                // delete the object
                getConnectorFacade().delete(getSupportedObjectClass(), uid,
                        getOperationOptionsByOp(DeleteApiOp.class));
            }
        }
    }

    @Test
    public void testNegative() {
        if (!ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(), getAPIOperations())) {
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
            getConnectorFacade().resolveUsername(getObjectClass(), wrongUsername, null);
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
