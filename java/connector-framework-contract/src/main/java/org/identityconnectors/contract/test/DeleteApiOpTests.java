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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Contract test of {@link DeleteApiOp}
 */
public class DeleteApiOpTests extends ObjectClassRunner {

    private static final Log LOG = Log.getLog(DeleteApiOpTests.class);

    public static final String TEST_NAME = "Delete";

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> requiredOps = new HashSet<>();
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
    protected void testRun(ObjectClass objectClass) {
        ConnectorObject obj = null;
        Uid uid = null;

        try {
            // create something to delete - object class is always supported
            uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), 0, getOperationOptionsByOp(objectClass,
                    DeleteApiOp.class));

            // The object should exist now
            obj = getConnectorFacade().getObject(objectClass, uid, getOperationOptionsByOp(objectClass, GetApiOp.class));
            assertNotNull(obj, "Unable to perform delete test because object to be deleted cannot be created");

            // try to delete object
            getConnectorFacade().delete(objectClass, uid, getOperationOptionsByOp(objectClass, DeleteApiOp.class));

            // Try to find it now, it should be deleted
            obj = getConnectorFacade().getObject(objectClass, uid, getOperationOptionsByOp(objectClass, GetApiOp.class));
            assertNull(obj, "Object wasn't deleted by delete.");

        } finally {
            // try to delete if previous deletes failed
            ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid, false,
                    getOperationOptionsByOp(objectClass, DeleteApiOp.class));
        }
    }

    /**
     * Tests that delete throws {@link UnknownUidException} when object is deleted for the second time.
     */
    @ParameterizedTest
    @MethodSource("objectClasses")
    public void testDeleteThrowUnknownUid(ObjectClass objectClass) {
        // run the contract test only if delete is supported
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())) {
            Uid uid = null;

            try {
                // create something to delete - object class is always supported
                uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 1, getOperationOptionsByOp(objectClass,
                        DeleteApiOp.class));

                // delete for the first time
                getConnectorFacade().delete(objectClass, uid, getOperationOptionsByOp(objectClass, DeleteApiOp.class));

                try {
                    // delete for the second time
                    getConnectorFacade().delete(objectClass, uid,
                            getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                    fail("Delete of previously deleted object should throw UnknownUidException.");
                } catch (UnknownUidException ex) {
                    // ok
                }
            } finally {
                // try to delete if anything failed
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid, false,
                        getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        } else {
            LOG.info("----------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''testDeleteThrowUnknownUid'' for object class ''" + objectClass + "''.");
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
