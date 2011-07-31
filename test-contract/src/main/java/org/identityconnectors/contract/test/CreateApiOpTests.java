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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Contract test of {@link CreateApiOp} operation.
 */
@RunWith(Parameterized.class)
public class CreateApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(CreateApiOpTests.class);
    private static final String TEST_NAME = "Create";
    private static final String NON_EXISTING_PROP_NAME = "unsupportedAttributeName";

    public CreateApiOpTests(ObjectClass oclass) {
        super(oclass);
    }

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
    public void testRun() {
        
        Uid uid = null;
        
        try {
            Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(), getTestName(), 0, true, false);
            
            // should throw UnsupportedObjectClass if not supported
            uid = getConnectorFacade().create(getObjectClass(), attrs,
                    getOperationOptionsByOp(CreateApiOp.class));            

            // get the user to make sure it exists now
            ConnectorObject obj = getConnectorFacade().getObject(getObjectClass(), uid,
                    getOperationOptionsByOp(GetApiOp.class));

            assertNotNull("Unable to retrieve newly created object", obj);

            // compare requested attributes to retrieved attributes
            ConnectorHelper.checkObject(getObjectClassInfo(), obj, attrs);
        } finally {
            if (uid != null) {
                // delete the object
                getConnectorFacade().delete(getSupportedObjectClass(), uid,
                        getOperationOptionsByOp(DeleteApiOp.class));
            }
        }
    }
    
    /**
     * Tests create method with invalid Attribute, RuntimeException is expected
     * 
     * connector developers can set the value of unsupported attribute
     * using test property: <code>testsuite.Create.unsupportedAttributeName</code>
     */
    @Test
    public void testCreateFailUnsupportedAttribute() {
        // run the contract test only if create is supported by tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(),
                getAPIOperations())) {
            // create not supported Attribute Set
            Set<Attribute> attrs = new HashSet<Attribute>();
            
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
                uid = getConnectorFacade().create(getObjectClass(), attrs, null);
                Assert.fail("'testCreateFailUnsupportedAttribute': NONEXISTING attribute accepted without throwing a RuntimeException.");
            }
            catch (RuntimeException ex) {
                // ok
            } 
            finally {
                if (uid != null) {
                    // delete the created the object
                    ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid,
                            false, getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
        }
        else {
            LOG.info("----------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''testCreateFailUnsupportedAttribute'' for object class ''{0}''.", getObjectClass());
            LOG.info("----------------------------------------------------------------------------------------");
        }
    }
    
    /**
     * Tests create twice with the same attributes. It should return different
     * Uids.
     */
    @Test
    public void testCreateWithSameAttributes() {
        // run the contract test only if create is supported by tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(), getAPIOperations())) {
            Uid uid1 = null;
            Uid uid2 = null;

            try {
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(), getTestName(), 1, true, false);

                // ObjectClassInfo is always supported
                uid1 = getConnectorFacade().create(getSupportedObjectClass(), attrs,
                        getOperationOptionsByOp(CreateApiOp.class));

                // get the object to make sure it exist now
                ConnectorObject obj1 = getConnectorFacade().getObject(getSupportedObjectClass(),
                        uid1, getOperationOptionsByOp(GetApiOp.class));
                assertNotNull("Unable to retrieve newly created object", obj1);

                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(), obj1, attrs);

                /* SECOND CREATE: */

                // should return different uid or throw
                uid2 = getConnectorFacade().create(getSupportedObjectClass(), attrs, getOperationOptionsByOp(CreateApiOp.class));
                assertFalse("Create returned the same Uid as by previous create.", uid1
                        .equals(uid2));

                // get the object to make sure it exists now
                ConnectorObject obj2 = getConnectorFacade().getObject(getSupportedObjectClass(),
                        uid2, getOperationOptionsByOp(GetApiOp.class));
                assertNotNull("Unable to retrieve newly created object", obj2);

                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(), obj2, attrs);
            } catch (RuntimeException ex) {
                // ok - second create could throw this exception
            } finally {
                if (uid1 != null) {
                    // delete the object
                    ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid1,
                            false, getOperationOptionsByOp(DeleteApiOp.class));
                }
                if (uid2 != null) {
                    // delete the object
                    ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid2,
                            false, getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
        }
        else {
            LOG.info("----------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''testCreateWithSameAttributes'' for object class ''{0}''.", getObjectClass());
            LOG.info("----------------------------------------------------------------------------------------");
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
