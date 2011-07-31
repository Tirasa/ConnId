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
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Contract test of {@link SyncApiOp}
 */
@RunWith(Parameterized.class)
public class SyncApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(SyncApiOpTests.class);
    private static final String TEST_NAME = "Sync";
    public static final String MODIFIED = "modified";

    /*
     * Properties' prefixes to disable particular sync change types.
     * (Some connectors are capable to sync only ie. CREATEs)
     */
    private static final String DISABLE = "disable";
    private static final String CREATE_PREFIX = "create";
    private static final String UPDATE_PREFIX = "update";
    private static final String DELETE_PREFIX = "delete";

    public SyncApiOpTests(ObjectClass oclass) {
        super(oclass);
    }
    
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
    public void testRun() {
        Uid uid = null;
        Set<Attribute> attrs = null;
        List<SyncDelta> deltas = null;
        SyncToken token = null;
        String msg = null;

        try {
            // start synchronizing from now
            token = getConnectorFacade().getLatestSyncToken(getObjectClass());

            /* CREATE: */

            // create record
            attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(), getTestName(), 0, true, false);
            uid = getConnectorFacade().create(getSupportedObjectClass(), attrs,
                    getOperationOptionsByOp(CreateApiOp.class));
            assertNotNull("Create returned null uid.", uid);

            if (canSyncAfterOp(CreateApiOp.class)) {
                // sync after create
                deltas = ConnectorHelper.sync(getConnectorFacade(), getObjectClass(), token,
                        getOperationOptionsByOp(SyncApiOp.class));

                // check that returned one delta
                msg = "Sync should have returned one sync delta after creation of one object, but returned: %d";
                assertTrue(String.format(msg, deltas.size()), deltas.size() == 1);

                // check delta
                ConnectorHelper.checkSyncDelta(getObjectClassInfo(), deltas.get(0), uid, attrs,
                        SyncDeltaType.CREATE_OR_UPDATE, true);

                token = deltas.get(0).getToken();
            }

            /* UPDATE: */

            if (ConnectorHelper.operationSupported(getConnectorFacade(), UpdateApiOp.class)
                    && canSyncAfterOp(UpdateApiOp.class)) {

                Set<Attribute> replaceAttributes = ConnectorHelper.getUpdateableAttributes(
                        getDataProvider(), getObjectClassInfo(), getTestName(), MODIFIED, 0, false,
                        false);

                // update only in case there is something to update
                if (replaceAttributes.size() > 0) {
                    replaceAttributes.add(uid);

                    assertTrue("no update attributes were found", (replaceAttributes.size() > 0));
                    Uid newUid = getConnectorFacade().update(
                            getSupportedObjectClass(), uid, AttributeUtil.filterUid(replaceAttributes),
                            getOperationOptionsByOp(UpdateApiOp.class));

                    // Update change of Uid must be propagated to
                    // replaceAttributes
                    if (!newUid.equals(uid)) {
                        replaceAttributes.remove(uid);
                        replaceAttributes.add(newUid);
                        uid = newUid;
                    }

                    // sync after update
                    deltas = ConnectorHelper.sync(getConnectorFacade(), getObjectClass(), token,
                            getOperationOptionsByOp(SyncApiOp.class));

                    // check that returned one delta
                    msg = "Sync should have returned one sync delta after update of one object, but returned: %d";
                    assertTrue(String.format(msg, deltas.size()), deltas.size() == 1);

                    // check delta
                    ConnectorHelper.checkSyncDelta(getObjectClassInfo(), deltas.get(0), uid,
                            replaceAttributes, SyncDeltaType.CREATE_OR_UPDATE, true);

                    token = deltas.get(0).getToken();
                }
            }

            /* DELETE: */

            if (canSyncAfterOp(DeleteApiOp.class)) {
                // delete object
                getConnectorFacade().delete(getObjectClass(), uid,
                        getOperationOptionsByOp(DeleteApiOp.class));

                // sync after delete
                deltas = ConnectorHelper.sync(getConnectorFacade(), getObjectClass(), token,
                        getOperationOptionsByOp(SyncApiOp.class));

                // check that returned one delta
                msg = "Sync should have returned one sync delta after delete of one object, but returned: %d";
                assertTrue(String.format(msg, deltas.size()), deltas.size() == 1);

                // check delta
                ConnectorHelper.checkSyncDelta(getObjectClassInfo(), deltas.get(0), uid, null,
                        SyncDeltaType.DELETE, true);
            }
        } finally {
            // cleanup test data
            ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid,
                    false, getOperationOptionsByOp(DeleteApiOp.class));
        }
    }

    /**
     * Test Sync without attrsToGet.
     */
    @Test
    public void testSyncWithoutAttrsToGet() {
        // run the test only if sync is supported and also object class is
        // supported and connector can sync CREATEs
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(), getAPIOperations())
                && canSyncAfterOp(CreateApiOp.class)) {
            Uid uid = null;
            try {
                // start synchronizing from now
                SyncToken token = getConnectorFacade().getLatestSyncToken(getObjectClass());

                // create record
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(), getTestName(), 1, true, false);
                uid = getConnectorFacade().create(getSupportedObjectClass(), attrs, null);
                assertNotNull("Create returned null uid.", uid);

                List<SyncDelta> deltas = ConnectorHelper.sync(getConnectorFacade(),
                        getSupportedObjectClass(), token, null);

                // check that returned one delta
                final String MSG = "Sync should have returned one sync delta after creation of one object, but returned: %d";
                assertTrue(String.format(MSG, deltas.size()), deltas.size() == 1);

                // check delta, but don't check attributes which are not returned by default
                ConnectorHelper.checkSyncDelta(getObjectClassInfo(), deltas.get(0), uid, attrs,
                        SyncDeltaType.CREATE_OR_UPDATE, false);
            } finally {
                // cleanup
                getConnectorFacade().delete(getSupportedObjectClass(), uid, null);
            }
        } else {
            LOG.info("----------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''testSyncWithoutAttrsToGet'' for object class ''{0}''.", getObjectClass());
            LOG.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Tests that {@link SyncApiOp#getLatestSyncToken()} returns really the latest sync token which is available.
     */
    @Test
    public void testLatestSyncToken() {
        // run the test only if sync is supported by the tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(), getAPIOperations())
                && canSyncAfterOp(CreateApiOp.class)) {
            Uid uid1 = null;
            Uid uid2 = null;
            try {
                // create one new object
                Set<Attribute> attrs1 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(), getTestName(), 2, true, false);
                uid1 = getConnectorFacade().create(getSupportedObjectClass(), attrs1, null);
                assertNotNull("Create returned null uid.", uid1);
                
                // get latest sync token
                SyncToken latestToken = getConnectorFacade().getLatestSyncToken(getObjectClass());
                
                // sync with latest sync token, should return nothing
                final LinkedList<SyncDelta> deltas = new LinkedList<SyncDelta>();
                getConnectorFacade().sync(getObjectClass(), latestToken, new SyncResultsHandler() {
                    public boolean handle(SyncDelta delta) {
                        deltas.add(delta);
                        return true;
                    }
                }, null);
                
                final String MSG1 = "Sync with previously retrieved latest sync token should not return any deltas, but returned: %d.";
                assertTrue(String.format(MSG1, deltas.size()), deltas.size() == 0);
                
                // create another object
                Set<Attribute> attrs2 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(), getTestName(), 3, true, false);
                uid2 = getConnectorFacade().create(getSupportedObjectClass(), attrs2, null);
                assertNotNull("Create returned null uid.", uid2);
                         
                // sync with the same latest sync token as previous sync
                // should return one change this time
                getConnectorFacade().sync(getObjectClass(), latestToken, new SyncResultsHandler() {
                    public boolean handle(SyncDelta delta) {
                        deltas.add(delta);
                        return true;
                    }
                }, null);
                
                final String MSG2 = "Sync with latest sync token retrieved before one create should return one sync delta, but returned: %d";
                assertTrue(String.format(MSG2, deltas.size()), deltas.size() == 1);
                
                ConnectorHelper.checkSyncDelta(getObjectClassInfo(), deltas.get(0), uid2, attrs2,
                        SyncDeltaType.CREATE_OR_UPDATE, false);
            } finally {
                // cleanup
                getConnectorFacade().delete(getSupportedObjectClass(), uid1, null);
                getConnectorFacade().delete(getSupportedObjectClass(), uid2, null);
            }
        } else {
            LOG.info("----------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''testLatestSyncToken'' for object class ''{0}''.", getObjectClass());
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
