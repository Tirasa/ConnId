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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/**
 * Contract test of {@link CreateApiOp} operation.
 */
@Test(testName = CreateApiOpTests.TEST_NAME)
public class CreateApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    private static final Logger logger = Logger.getLogger(ValidateApiOpTests.class);
    public static final String TEST_NAME = "Create";
    private static final String NON_EXISTING_PROP_NAME = "unsupportedAttributeName";


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> requiredOps = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        requiredOps.add(CreateApiOp.class);
        requiredOps.add(GetApiOp.class);
        return requiredOps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void testRun(ObjectClass objectClass) {

        Uid uid = null;

        try {
            Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), 0, true, false);

            // should throw UnsupportedObjectClass if not supported
            uid = getConnectorFacade().create(objectClass, attrs,
                    getOperationOptionsByOp(objectClass, CreateApiOp.class));

            // get the user to make sure it exists now
            ConnectorObject obj = getConnectorFacade().getObject(objectClass, uid,
                    getOperationOptionsByOp(objectClass, GetApiOp.class));

            assertNotNull(obj,"Unable to retrieve newly created object");

            // compare requested attributes to retrieved attributes
            ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, attrs);
        } finally {
            if (uid != null) {
                // delete the object
                getConnectorFacade().delete(objectClass, uid,
                        getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        }
    }

    /**
     * Tests create method with invalid Attribute, RuntimeException is expected
     *
     * connector developers can set the value of unsupported attribute
     * using test property: <code>testsuite.Create.unsupportedAttributeName</code>
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testCreateFailUnsupportedAttribute(ObjectClass objectClass) {
        // run the contract test only if create is supported by tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass,
                getAPIOperations())) {
            // create not supported Attribute Set
            Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), 0, true, false);

            String unsupportedAttribute = null;
            try{
                unsupportedAttribute = (String) getDataProvider().getTestSuiteAttribute(NON_EXISTING_PROP_NAME, TEST_NAME);
            } catch (ObjectNotFoundException ex) {
                unsupportedAttribute = "nonExistingAndUnlikelyAttrName";
            }

            attrs.add(AttributeBuilder.build(unsupportedAttribute));

            Uid uid = null;
            try {
                // do the create call
                // note - the ObjectClassInfo is always supported
                uid = getConnectorFacade().create(objectClass, attrs, null);
                Assert.fail("'testCreateFailUnsupportedAttribute': NONEXISTING attribute accepted without throwing a RuntimeException.");
            }
            catch (RuntimeException ex) {
                // ok
            }
            finally {
                if (uid != null) {
                    // delete the created the object
                    ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                            false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
            }
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testCreateFailUnsupportedAttribute'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Tests create twice with the same attributes. It should return different
     * Uids.
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testCreateWithSameAttributes(ObjectClass objectClass) {
        // run the contract test only if create is supported by tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())) {
            Uid uid1 = null;
            Uid uid2 = null;

            try {
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 1, true, false);

                // ObjectClassInfo is always supported
                uid1 = getConnectorFacade().create(objectClass, attrs,
                        getOperationOptionsByOp(objectClass, CreateApiOp.class));

                // get the object to make sure it exist now
                ConnectorObject obj1 = getConnectorFacade().getObject(objectClass,
                        uid1, getOperationOptionsByOp(objectClass, GetApiOp.class));
                assertNotNull(obj1,"Unable to retrieve newly created object");

                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj1, attrs);

                /* SECOND CREATE: */

                // should return different uid or throw
                uid2 = getConnectorFacade().create(objectClass, attrs, getOperationOptionsByOp(objectClass, CreateApiOp.class));
                assertFalse(uid1
                        .equals(uid2),"Create returned the same Uid as by previous create.");

                // get the object to make sure it exists now
                ConnectorObject obj2 = getConnectorFacade().getObject(objectClass,
                        uid2, getOperationOptionsByOp(objectClass, GetApiOp.class));
                assertNotNull(obj2,"Unable to retrieve newly created object");

                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj2, attrs);
            } catch (RuntimeException ex) {
                // ok - second create could throw this exception
            } finally {
                if (uid1 != null) {
                    // delete the object
                    ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid1,
                            false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
                if (uid2 != null) {
                    // delete the object
                    ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid2,
                            false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
            }
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testCreateWithSameAttributes'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestName() {
        return TEST_NAME;
    }

}
