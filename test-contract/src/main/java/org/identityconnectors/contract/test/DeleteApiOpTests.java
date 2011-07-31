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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Contract test of {@link DeleteApiOp}
 */
@RunWith(Parameterized.class)
public class DeleteApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(DeleteApiOpTests.class);
    private static final String TEST_NAME = "Delete";

    public DeleteApiOpTests(ObjectClass oclass) {
        super(oclass);
    }
    
    /**
     * {@inheritDoc}     
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> requiredOps = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        requiredOps.add(DeleteApiOp.class);
        requiredOps.add(CreateApiOp.class);
        requiredOps.add(GetApiOp.class);
        return requiredOps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testRun() {      
        ConnectorObject obj = null;
        Uid uid = null;
        
        try {
            // create something to delete - object class is always supported
            uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                    getObjectClassInfo(), getTestName(), 0, getOperationOptionsByOp(DeleteApiOp.class));
            
            // The object should exist now
            obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid, getOperationOptionsByOp(GetApiOp.class));
            assertNotNull("Unable to perform delete test because object to be deleted cannot be created", obj);

            // try to delete object
            getConnectorFacade().delete(getObjectClass(), uid, getOperationOptionsByOp(DeleteApiOp.class));

            // Try to find it now, it should be deleted
            obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid, getOperationOptionsByOp(GetApiOp.class));
            assertNull("Object wasn't deleted by delete.", obj);
            
        } finally {
            // try to delete if previous deletes failed
            ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid, false,
                    getOperationOptionsByOp(DeleteApiOp.class));
        }
    }
    
    /**
     * Tests that delete throws {@link UnknownUidException} when object is deleted for the second time.
     */
    @Test
    public void testDeleteThrowUnknownUid() {
        // run the contract test only if delete is supported
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(), getAPIOperations())) {
            Uid uid = null;
            
            try {
                // create something to delete - object class is always supported
                uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                        getObjectClassInfo(), getTestName(), 1, getOperationOptionsByOp(DeleteApiOp.class));
                
                // delete for the first time
                getConnectorFacade().delete(getSupportedObjectClass(), uid, getOperationOptionsByOp(DeleteApiOp.class));
                
                try {
                    // delete for the second time
                    getConnectorFacade().delete(getSupportedObjectClass(), uid, getOperationOptionsByOp(DeleteApiOp.class));
                    fail("Delete of previously deleted object should throw UnknownUidException.");
                }
                catch (UnknownUidException ex) {
                    // ok
                }
            }
            finally {
                // try to delete if anything failed
                ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid, false,
                        getOperationOptionsByOp(DeleteApiOp.class));
            }
        }
        else {
            LOG.info("----------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''testDeleteThrowUnknownUid'' for object class ''{0}''.", getObjectClass());
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
