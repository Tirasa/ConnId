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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/**
 * Contract test of {@link SyncApiOp}
 */
@Test(testName =  SyncApiOpTests.TEST_NAME)
public class SyncApiOpTests extends ObjectClassRunner {

    /**
     * Logging..
     */
    private static final Logger logger = Logger.getLogger(ValidateApiOpTests.class);
    public static final String TEST_NAME = "Sync";
    public static final String MODIFIED = "modified";

    /*
     * Properties' prefixes to disable particular sync change types.
     * (Some connectors are capable to sync only ie. CREATEs)
     */
    private static final String DISABLE = "disable";
    private static final String CREATE_PREFIX = "create";
    private static final String UPDATE_PREFIX = "update";
    private static final String DELETE_PREFIX = "delete";

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        s.add(SyncApiOp.class);
        s.add(CreateApiOp.class);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void testRun(ObjectClass objectClass) {
        Uid uid = null;
        Set<Attribute> attrs = null;
        List<SyncDelta> deltas = null;
        SyncToken token = null;
        String msg = null;

        try {
            // start synchronizing from now
            token = getConnectorFacade().getLatestSyncToken(objectClass);

            /* CREATE: */

            // create record
            attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), 0, true, false);
            uid = getConnectorFacade().create(objectClass, attrs,
                    getOperationOptionsByOp(objectClass, CreateApiOp.class));
            assertNotNull(uid, "Create returned null uid.");

            if (canSyncAfterOp(CreateApiOp.class)) {
                // sync after create
                deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token,
                        getOperationOptionsByOp(objectClass, SyncApiOp.class));

                // check that returned one delta
                msg = "Sync should have returned one sync delta after creation of one object, but returned: %d";
                assertTrue(deltas.size() == 1, String.format(msg, deltas.size()));

                // check delta
                ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), deltas.get(0), uid, attrs,
                        SyncDeltaType.CREATE_OR_UPDATE, true);

                token = deltas.get(0).getToken();
            }

            /* UPDATE: */

            if (ConnectorHelper.operationSupported(getConnectorFacade(), UpdateApiOp.class)
                    && canSyncAfterOp(UpdateApiOp.class)) {

                Set<Attribute> replaceAttributes = ConnectorHelper.getUpdateableAttributes(
                        getDataProvider(), getObjectClassInfo(objectClass), getTestName(), MODIFIED, 0, false,
                        false);

                // update only in case there is something to update
                if (replaceAttributes.size() > 0) {
                    replaceAttributes.add(uid);

                    assertTrue((replaceAttributes.size() > 0), "no update attributes were found");
                    Uid newUid = getConnectorFacade().update(
                            objectClass, uid, AttributeUtil.filterUid(replaceAttributes),
                            getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                    // Update change of Uid must be propagated to
                    // replaceAttributes
                    if (!newUid.equals(uid)) {
                        replaceAttributes.remove(uid);
                        replaceAttributes.add(newUid);
                        uid = newUid;
                    }

                    // sync after update
                    deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token,
                            getOperationOptionsByOp(objectClass, SyncApiOp.class));

                    // check that returned one delta
                    msg = "Sync should have returned one sync delta after update of one object, but returned: %d";
                    assertTrue(deltas.size() == 1, String.format(msg, deltas.size()));

                    // check delta
                    ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), deltas.get(0), uid,
                            replaceAttributes, SyncDeltaType.CREATE_OR_UPDATE, true);

                    token = deltas.get(0).getToken();
                }
            }

            /* DELETE: */

            if (canSyncAfterOp(DeleteApiOp.class)) {
                // delete object
                getConnectorFacade().delete(objectClass, uid,
                        getOperationOptionsByOp(objectClass, DeleteApiOp.class));

                // sync after delete
                deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token,
                        getOperationOptionsByOp(objectClass, SyncApiOp.class));

                // check that returned one delta
                msg = "Sync should have returned one sync delta after delete of one object, but returned: %d";
                assertTrue(deltas.size() == 1, String.format(msg, deltas.size()));

                // check delta
                ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), deltas.get(0), uid, null,
                        SyncDeltaType.DELETE, true);
            }
        } finally {
            // cleanup test data
            ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                    false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
        }
    }

    /**
     * Test Sync without attrsToGet.
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testSyncWithoutAttrsToGet(ObjectClass objectClass) {
        // run the test only if sync is supported and also object class is
        // supported and connector can sync CREATEs
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())
                && canSyncAfterOp(CreateApiOp.class)) {
            Uid uid = null;
            try {
                // start synchronizing from now
                SyncToken token = getConnectorFacade().getLatestSyncToken(objectClass);

                // create record
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 1, true, false);
                uid = getConnectorFacade().create(objectClass, attrs, null);
                assertNotNull(uid, "Create returned null uid.");

                List<SyncDelta> deltas = ConnectorHelper.sync(getConnectorFacade(),
                        objectClass, token, null);

                // check that returned one delta
                final String MSG = "Sync should have returned one sync delta after creation of one object, but returned: %d";
                assertTrue(deltas.size() == 1,String.format(MSG, deltas.size()));

                // check delta, but don't check attributes which are not returned by default
                ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), deltas.get(0), uid, attrs,
                        SyncDeltaType.CREATE_OR_UPDATE, false);
            } finally {
                // cleanup
                getConnectorFacade().delete(objectClass, uid, null);
            }
        } else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testSyncWithoutAttrsToGet'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Tests that {@link SyncApiOp#getLatestSyncToken(ObjectClass)} returns really the latest sync token which is available.
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testLatestSyncToken(ObjectClass objectClass) {
        // run the test only if sync is supported by the tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())
                && canSyncAfterOp(CreateApiOp.class)) {
            Uid uid1 = null;
            Uid uid2 = null;
            try {
                // create one new object
                Set<Attribute> attrs1 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 2, true, false);
                uid1 = getConnectorFacade().create(objectClass, attrs1, null);
                assertNotNull(uid1,"Create returned null uid.");

                // get latest sync token
                SyncToken latestToken = getConnectorFacade().getLatestSyncToken(objectClass);

                // sync with latest sync token, should return nothing
                final LinkedList<SyncDelta> deltas = new LinkedList<SyncDelta>();
                getConnectorFacade().sync(objectClass, latestToken, new SyncResultsHandler() {

                    public boolean handle(SyncDelta delta) {
                        deltas.add(delta);
                        return true;
                    }
                }, null);

                final String MSG1 = "Sync with previously retrieved latest sync token should not return any deltas, but returned: %d.";
                assertTrue(deltas.size() == 0,String.format(MSG1, deltas.size()));

                // create another object
                Set<Attribute> attrs2 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 3, true, false);
                uid2 = getConnectorFacade().create(objectClass, attrs2, null);
                assertNotNull(uid2,"Create returned null uid.");

                // sync with the same latest sync token as previous sync
                // should return one change this time
                getConnectorFacade().sync(objectClass, latestToken, new SyncResultsHandler() {

                    public boolean handle(SyncDelta delta) {
                        deltas.add(delta);
                        return true;
                    }
                }, null);

                final String MSG2 = "Sync with latest sync token retrieved before one create should return one sync delta, but returned: %d";
                assertTrue(deltas.size() == 1,String.format(MSG2, deltas.size()));

                ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), deltas.get(0), uid2, attrs2,
                        SyncDeltaType.CREATE_OR_UPDATE, false);
            } finally {
                // cleanup
                getConnectorFacade().delete(objectClass, uid1, null);
                getConnectorFacade().delete(objectClass, uid2, null);
            }
        } else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testLatestSyncToken'' for object class ''"+objectClass+"''.");
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

    /**
     * Returns true if tests are configured to test connector's sync after specified operation.
     * Some connectors implement sync but are not capable to sync all changes' types.
     */
    protected static boolean canSyncAfterOp(Class<? extends APIOperation> operation) {
        // by default it's supposed that sync works for all change types
        Boolean canSync = true;
        try {
            if (operation.equals(CreateApiOp.class)) {
                canSync = !(Boolean) getDataProvider().getTestSuiteAttribute(
                        DISABLE + "." + CREATE_PREFIX, TEST_NAME);
            } else if (operation.equals(UpdateApiOp.class)) {
                canSync = !(Boolean) getDataProvider().getTestSuiteAttribute(
                        DISABLE + "." + UPDATE_PREFIX, TEST_NAME);
            } else if (operation.equals(DeleteApiOp.class)) {
                canSync = !(Boolean) getDataProvider().getTestSuiteAttribute(
                        DISABLE + "." + DELETE_PREFIX, TEST_NAME);
            }
        } catch (ObjectNotFoundException ex) {
            // exceptions is throw in case property definition is not found
            // ok
        }

        return canSync;
    }
}
