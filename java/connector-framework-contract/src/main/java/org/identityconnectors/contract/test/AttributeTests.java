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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.LiveSyncApiOp;
import org.identityconnectors.framework.api.operations.SchemaApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.LiveSyncDelta;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * <p>
 * Test that attributes satisfy contract.
 * </p>
 * Tests check:
 * <ul>
 * <li>non-readable attributes are not returnedByDefault</li>
 * <li>attributes which are not returnedByDefault really are not returned unless specified in attrsToGet </li>
 * <li>update of non-updateable attribute will fail</li>
 * <li>required attributes must be creatable</li>
 * </ul>
 *
 * @author David Adam
 */
public class AttributeTests extends ObjectClassRunner {

    private static final Log LOG = Log.getLog(AttributeTests.class);

    private static final String TEST_NAME = "Attribute";

    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> res = new HashSet<>();
        // list of required operations by this test:
        res.add(CreateApiOp.class);
        res.add(UpdateApiOp.class);
        res.add(GetApiOp.class);
        res.add(SchemaApiOp.class);
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void testRun(ObjectClass objectClass) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestName() {
        return TEST_NAME;
    }

    /* ******************** TEST METHODS ******************** */
    /**
     * <p>
     * Non readable attributes should _not_ be returned by default
     * </p>
     * <p>
     * API operations for acquiring attributes: <code>GetApiOp</code>
     * </p>
     */
    @ParameterizedTest
    @MethodSource("objectClasses")
    public void testNonReadable(final ObjectClass objectClass) {
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())) {
            Uid uid = null;
            try {
                ObjectClassInfo oci = getObjectClassInfo(objectClass);

                // create a new user
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), oci, getTestName(), 0, true, false);
                // should throw UnsupportedObjectClass if not supported
                uid = getConnectorFacade().create(objectClass,
                        attrs, getOperationOptionsByOp(objectClass, CreateApiOp.class));

                // get the user to make sure it exists now
                ConnectorObject obj = getConnectorFacade().getObject(
                        objectClass, uid,
                        null/* GET returned by default attributes */);

                assertNotNull(obj, "Unable to retrieve newly created object");

                // check: non readable attributes should not be returned by
                // default
                obj.getAttributes().stream().
                        filter((attr) -> (!ConnectorHelper.isReadable(oci, attr))).
                        forEachOrdered((attr) -> {
                            String msg = String.format(
                                    "Non-readable attribute should not be returned by default: %s", attr.getName());
                            assertTrue(!ConnectorHelper.isReturnedByDefault(oci, attr), msg);
                        });
            } finally {
                if (uid != null) {
                    // delete the object
                    getConnectorFacade().delete(objectClass, uid,
                            getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
            }
        } else {
            printSkipTestMsg("testNonReadable", objectClass);
        }
    }

    /**
     * <p>
     * Not returned by default attributes should not be returned, unless
     * specified in attributesToGet ({@link OperationOptions})
     * </p>
     * <p>
     * API operations for acquiring attributes:
     * </p>
     * <ul>
     * <li>{@link GetApiOp}</li>
     * <li>{@link SearchApiOp}</li>
     * <li>{@link SyncApiOp}</li>
     * </ul>
     */
    @ParameterizedTest
    @MethodSource("objectClasses")
    public void testReturnedByDefault(final ObjectClass objectClass) {
        if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, CreateApiOp.class)) {
            // run the test for GetApiOp, SearchApiOp and SyncApiOp
            for (ApiOperations apiop : ApiOperations.values()) {
                testReturnedByDefault(objectClass, apiop);
            }
        } else {
            printSkipTestMsg("testReturnedByDefault", objectClass);
        }
    }

    /**
     * Update of non-updateable attribute is not acceptable.
     * Connector should throw a RuntimeException.
     *
     * <p>
     * API operations for acquiring attributes: {@link GetApiOp}
     * </p>
     */
    @ParameterizedTest
    @MethodSource("objectClasses")
    public void testNonUpdateable(final ObjectClass objectClass) {
        boolean exceptionCaught = false;
        /** logging info bean */
        LogInfo logInfo = null;

        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())) {
            Uid uid = null;

            try {
                // create an object to update
                uid = ConnectorHelper.createObject(getConnectorFacade(),
                        getDataProvider(), getObjectClassInfo(objectClass), getTestName(),
                        1, getOperationOptionsByOp(objectClass, CreateApiOp.class));
                assertNotNull(uid, "Create returned null Uid.");

                // get by uid
                ConnectorObject obj = getConnectorFacade().getObject(objectClass,
                        uid, getOperationOptionsByOp(objectClass, GetApiOp.class));
                assertNotNull(obj, "Cannot retrieve created object.");

                // ******************************
                // Acquire updateable attributes
                Schema schema = getConnectorFacade().schema();
                Set<Attribute> nonUpdateableAttrs = getNonUpdateableAttributes(schema, objectClass);

                // null indicates an empty set ==> no non-updateable attributes
                boolean isChanged = (nonUpdateableAttrs != null);

                if (isChanged) {
                    //keep logging info
                    logInfo = new LogInfo(objectClass, nonUpdateableAttrs);

                    assertTrue(!nonUpdateableAttrs.isEmpty(), "no update attributes were found");
                    Uid newUid = getConnectorFacade().update(
                            objectClass,
                            uid,
                            AttributeUtil.filterUid(nonUpdateableAttrs),
                            getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                    // Update change of Uid must be propagated to replaceAttributes set
                    if (!newUid.equals(uid)) {
                        nonUpdateableAttrs.remove(uid);
                        nonUpdateableAttrs.add(newUid);
                        uid = newUid;
                    }

                    // verify the change
                    obj = getConnectorFacade().getObject(
                            objectClass, uid,
                            getOperationOptionsByOp(objectClass, GetApiOp.class));
                    assertNotNull(obj, "Cannot retrieve updated object.");
                    ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj,
                            nonUpdateableAttrs);
                } else {
                    /*
                     * SKIPPING THE TEST
                     * no non-updateable attribute present
                     */
                    printSkipNonUpdateableTestMsg(objectClass);
                    return;
                }

            } catch (RuntimeException ex) {
                /*
                 * Expected behavior:
                 * in case non-updateable attribute is updated, Runtime exception
                 * should be thrown.
                 */
                exceptionCaught = true;
            } finally {
                if (uid != null) {
                    // finally ... get rid of the object
                    ConnectorHelper.deleteObject(getConnectorFacade(),
                            objectClass, uid, false,
                            getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
            }

            // in case no exception is thrown:
            if (!exceptionCaught) {
                fail(String.format("No exception thrown when update is performed on non-updateable attribute(s). "
                        + "(hint: throw a RuntimeException) %s", ((logInfo != null) ? logInfo.toString() : "")));
            }
        } else {
            printSkipTestMsg("testNonUpdateable", objectClass);
        }
    }

    /**
     * return the Non-Updateable attributes for currently tested objectclass
     *
     * @param schema the schema of currently tested connector
     * @param objectClass the currently tested objectclass
     * @return
     */
    private Set<Attribute> getNonUpdateableAttributes(Schema schema, ObjectClass objectClass) {
        Set<Attribute> result = new HashSet<>();

        //objectClass that we search for
        ObjectClassInfoBuilder oib = new ObjectClassInfoBuilder();
        oib.setType(objectClass.getObjectClassValue());
        ObjectClassInfo ocToFind = oib.build();

        Set<ObjectClassInfo> oci = schema.getObjectClassInfo();
        oci.forEach(objectClassInfo -> {
            if (objectClassInfo.getType().equals(ocToFind.getType())) {
                // we found the currently tested object class

                // do a scan through attributes and look for non-updateable ones.
                Set<AttributeInfo> attrInfo = objectClassInfo.getAttributeInfo();
                attrInfo.stream().
                        filter(attributeInfo -> !attributeInfo.isUpdateable()).
                        map(attributeInfo -> AttributeBuilder.build(attributeInfo.getName())).
                        forEachOrdered((attr) -> {
                            // create an empty list value for update
                            // TODO might add some reasonable value for the attribute to update
                            result.add(attr);
                        });
            }
        });

        return result.isEmpty() ? null : result;
    }

    /**
     * prints log message when skipping
     * {@link AttributeTests#testNonUpdateable()} test
     */
    private void printSkipNonUpdateableTestMsg(ObjectClass objectClass) {
        printSkipTestMsg("testNonUpdateable", objectClass);
    }

    /**
     * prints log message when skipping
     * {@link AttributeTests#testNonUpdateable()} test
     *
     * @param testName the name of the test to print
     */
    private void printSkipTestMsg(String testName, ObjectClass objectClass) {
        LOG.info(LOG_SEPARATOR);
        LOG.info("Skipping test ''" + testName + "'' for object class ''" + objectClass + "''. "
                + "(Reason: non-updateable attrs. are missing)");
        LOG.info(LOG_SEPARATOR);
    }

    /**
     * Required attributes must be creatable. It is a fialure if a required
     * attribute is not creatable.
     */
    @ParameterizedTest
    @MethodSource("objectClasses")
    public void testRequirableIsCreatable(final ObjectClass objectClass) {
        if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, CreateApiOp.class)
                && ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, GetApiOp.class)) {
            Uid uid = null;
            try {
                ObjectClassInfo oci = getObjectClassInfo(objectClass);

                // create a new user
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), oci, getTestName(), 2, true, false);
                // should throw UnsupportedObjectClass if not supported
                uid = getConnectorFacade().create(objectClass,
                        attrs, getOperationOptionsByOp(objectClass, CreateApiOp.class));

                // get the user to make sure it exists now
                ConnectorObject obj = getConnectorFacade().getObject(
                        objectClass, uid,
                        getOperationOptionsByOp(objectClass, GetApiOp.class));

                assertNotNull(obj, "Unable to retrieve newly created object");

                // check: Required attributes must be createable.
                obj.getAttributes().stream().
                        filter((attr) -> (ConnectorHelper.isRequired(oci, attr))).
                        filter((attr) -> (!ConnectorHelper.isCreateable(oci, attr))).
                        map((attr) -> String.format("Required attribute is not createable. Attribute name: %s",
                        attr.getName())).
                        forEachOrdered((msg) -> {
                            //WARN
                            fail(msg);
                        });
            } finally {
                if (uid != null) {
                    // delete the object
                    getConnectorFacade().delete(objectClass, uid,
                            getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
            }
        } else {
            LOG.info(LOG_SEPARATOR);
            LOG.info("Skipping test ''testNonReadable'' for object class ''{0}''.", objectClass);
            LOG.info(LOG_SEPARATOR);
        }
    }

    /* ******************** HELPER METHODS ******************** */
    /**
     * {@link AttributeTests#testReturnedByDefault()}
     *
     * @param apiOp the type of ApiOperation, that shall be tested.
     */
    private void testReturnedByDefault(final ObjectClass objectClass, final ApiOperations apiOp) {
        /** marker in front of every assert message */
        String testMarkMsg = String.format("[testReturnedByDefault/%s]", apiOp);

        // run the contract test only if <strong>apiOp</strong> APIOperation is
        // supported
        if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, apiOp.getClazz())) {
            // start synchronizing from now
            SyncToken token = null;
            if (apiOp.equals(ApiOperations.SYNC)) { // just for SyncApiOp test
                token = getConnectorFacade().getLatestSyncToken(objectClass);
            }

            Uid uid = null;
            try {
                ObjectClassInfo oci = getObjectClassInfo(objectClass);

                /*
                 * CREATE a new user
                 */
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), oci, getTestName(), 3, true, false);
                // should throw UnsupportedObjectClass if not supported
                uid = getConnectorFacade().create(objectClass, attrs, null);
                assertNotNull(uid, testMarkMsg + " Create returned null uid.");

                /*
                 * ************ GetApiOp ************
                 */
                // get the user to make sure it exists now
                ConnectorObject obj = null;
                switch (apiOp) {
                    case GET:
                        /* last _null_ param - no operation option, response contains just attributes returned by
                         * default */
                        obj = getConnectorFacade().getObject(objectClass, uid, null);
                        break;// GET

                    case SEARCH:
                        Filter fltUid = FilterBuilder.equalTo(AttributeBuilder
                                .build(Uid.NAME, uid.getUidValue()));

                        assertNotNull(fltUid, testMarkMsg + " filterUid is null");

                        List<ConnectorObject> coObjects = ConnectorHelper.search(
                                getConnectorFacade(), objectClass, fltUid, null);

                        assertTrue(coObjects.size() == 1, testMarkMsg
                                + " Search filter by uid with no OperationOptions failed, expected to return one object,"
                                + " but returned " + coObjects.size());

                        assertNotNull(coObjects.get(0), testMarkMsg + " Unable to retrieve newly created object");

                        obj = coObjects.get(0);
                        break;

                    case LIVE_SYNC:
                        uid = testLivesync(objectClass, uid, attrs, oci, testMarkMsg);
                        break;

                    case SYNC:
                        uid = testSync(objectClass, uid, token, attrs, oci, testMarkMsg);
                        break;
                }

                /*
                 * Check if attribute set contains non-returned by default
                 * Attributes. This is specific for AttributeTests
                 */
                if (apiOp != ApiOperations.SYNC && apiOp != ApiOperations.LIVE_SYNC) {
                    assertNotNull(obj, "Unable to retrieve newly created object");
                    // obj is null for sync tests
                    checkAttributes(obj, oci, apiOp);
                }

            } finally {
                if (uid != null) {
                    // cleanup test data
                    ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                            false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
            }
        } else {
            LOG.info(LOG_SEPARATOR);
            LOG.info("Skipping test ''testReturnedByDefault'' for object class ''{0}''.", objectClass);
            LOG.info(LOG_SEPARATOR);
        }
    }

    /**
     * Main checking of "no returned by default" attributes
     *
     * @param obj
     * @param oci
     * @param apiOp
     */
    private void checkAttributes(ConnectorObject obj, ObjectClassInfo oci, ApiOperations apiOp) {
        // Check if attribute set contains non-returned by default
        // Attributes.
        for (Attribute attr : obj.getAttributes()) {
            String msg = String.format(
                    "[testReturnedByDefault / %s]Attribute %s returned. However it is _not_ returned by default.",
                    apiOp, attr.getName());
            /*
             * this is a hack that skips control of UID, as it is presently
             * non returned by default, however it is automatically returned.
             * see discussion in Issue mailing list -- Issue #334
             * future TODO: after joining UID to schema, erase the condition.
             */
            if (!attr.getName().equals(Uid.NAME)) {
                assertTrue(ConnectorHelper.isReturnedByDefault(oci, attr), msg);
            }
        }
    }

    /**
     * test sync
     *
     * @param attrs newly created attributes
     * @param uid the newly created object
     * @param oci object class info
     * @param testMarkMsg test marker
     * @return the updated Uid
     */
    private Uid testLivesync(
            ObjectClass objectClass,
            Uid uid,
            Set<Attribute> attrs,
            ObjectClassInfo oci,
            String testMarkMsg) {

        /*
         * CREATE: (was handled in the calling method, result of create is in
         * param uid, cleanup is also in caller method.)
         */
        if (SyncApiOpTests.canSyncAfterOp(CreateApiOp.class)) {
            // sync after create
            List<LiveSyncDelta> deltas = ConnectorHelper.livesync(getConnectorFacade(), objectClass, null);

            // check that returned one delta
            String msg = "%s Sync should have returned one sync delta after creation of one object, but returned: %d";
            assertTrue(deltas.size() == 1, String.format(msg, testMarkMsg, deltas.size()));

            // check delta
            ConnectorHelper.checkLiveSyncDelta(getObjectClassInfo(objectClass), deltas.get(0), uid, attrs, false);

            /*
             * check the attributes inside delta This is specific for
             * AttributeTests
             */
            ConnectorObject obj = deltas.get(0).getObject();
            checkAttributes(obj, oci, ApiOperations.LIVE_SYNC);
        }

        /* UPDATE: */
        if (ConnectorHelper.operationSupported(getConnectorFacade(), UpdateApiOp.class)
                && SyncApiOpTests.canSyncAfterOp(UpdateApiOp.class)) {

            Set<Attribute> replaceAttributes = ConnectorHelper.getUpdateableAttributes(
                    getDataProvider(),
                    getObjectClassInfo(objectClass),
                    getTestName(),
                    SyncApiOpTests.MODIFIED,
                    0,
                    false,
                    false);

            // update only in case there is something to update
            if (!replaceAttributes.isEmpty()) {
                replaceAttributes.add(uid);

                assertTrue(!replaceAttributes.isEmpty(), testMarkMsg + " no update attributes were found");
                Uid newUid = getConnectorFacade().update(
                        objectClass,
                        uid,
                        AttributeUtil.filterUid(replaceAttributes),
                        null);

                // Update change of Uid must be propagated to
                // replaceAttributes
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    replaceAttributes.add(newUid);
                    uid = newUid;
                }

                // sync after update
                List<LiveSyncDelta> deltas = ConnectorHelper.livesync(getConnectorFacade(), objectClass, null);

                // check that returned one delta
                String msg = "%s Sync should have returned one sync delta after update of one object, but returned: %d";
                assertTrue(deltas.size() == 1, String.format(msg, testMarkMsg, deltas.size()));

                // check delta
                ConnectorHelper.checkLiveSyncDelta(
                        getObjectClassInfo(objectClass), deltas.get(0), uid, replaceAttributes, false);

                /*
                 * check the attributes inside delta This is specific for
                 * AttributeTests
                 */
                ConnectorObject obj = deltas.get(0).getObject();
                checkAttributes(obj, oci, ApiOperations.LIVE_SYNC);
            }
        }

        /* DELETE: */
        if (SyncApiOpTests.canSyncAfterOp(DeleteApiOp.class)) {
            // delete object
            getConnectorFacade().delete(objectClass, uid, null);

            // sync after delete
            List<LiveSyncDelta> deltas = ConnectorHelper.livesync(getConnectorFacade(), objectClass, null);

            // check that returned one delta
            String msg = "%s Sync should have returned one sync delta after delete of one object, but returned: %d";
            assertTrue(deltas.size() == 1, String.format(msg, testMarkMsg, deltas.size()));

            // check delta
            ConnectorHelper.checkLiveSyncDelta(getObjectClassInfo(objectClass), deltas.get(0), uid, null, false);

            /*
             * check the attributes inside delta This is specific for
             * AttributeTests
             */
            ConnectorObject obj = deltas.get(0).getObject();
            checkAttributes(obj, oci, ApiOperations.LIVE_SYNC);
        }
        return uid;
    }

    /**
     * test sync
     *
     * @param token initialized token
     * @param attrs newly created attributes
     * @param uid the newly created object
     * @param oci object class info
     * @param testMarkMsg test marker
     * @return the updated Uid
     */
    private Uid testSync(
            ObjectClass objectClass,
            Uid uid,
            SyncToken token,
            Set<Attribute> attrs,
            ObjectClassInfo oci,
            String testMarkMsg) {

        /*
         * CREATE: (was handled in the calling method, result of create is in
         * param uid, cleanup is also in caller method.)
         */
        if (SyncApiOpTests.canSyncAfterOp(CreateApiOp.class)) {
            // sync after create
            List<SyncDelta> deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token, null);

            // check that returned one delta
            String msg = "%s Sync should have returned one sync delta after creation of one object, but returned: %d";
            assertTrue(deltas.size() == 1, String.format(msg, testMarkMsg, deltas.size()));

            // check delta
            ConnectorHelper.checkSyncDelta(
                    getObjectClassInfo(objectClass), deltas.get(0), uid, attrs, SyncDeltaType.CREATE_OR_UPDATE, false);

            /*
             * check the attributes inside delta This is specific for
             * AttributeTests
             */
            ConnectorObject obj = deltas.get(0).getObject();
            checkAttributes(obj, oci, ApiOperations.SYNC);

            token = deltas.get(0).getToken();
        }

        /* UPDATE: */
        if (ConnectorHelper.operationSupported(getConnectorFacade(), UpdateApiOp.class)
                && SyncApiOpTests.canSyncAfterOp(UpdateApiOp.class)) {

            Set<Attribute> replaceAttributes = ConnectorHelper
                    .getUpdateableAttributes(getDataProvider(),
                            getObjectClassInfo(objectClass), getTestName(),
                            SyncApiOpTests.MODIFIED, 0, false, false);

            // update only in case there is something to update
            if (!replaceAttributes.isEmpty()) {
                replaceAttributes.add(uid);

                assertTrue(!replaceAttributes.isEmpty(), testMarkMsg + " no update attributes were found");
                Uid newUid = getConnectorFacade().update(
                        objectClass,
                        uid,
                        AttributeUtil.filterUid(replaceAttributes),
                        null);

                // Update change of Uid must be propagated to
                // replaceAttributes
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    replaceAttributes.add(newUid);
                    uid = newUid;
                }

                // sync after update
                List<SyncDelta> deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token, null);

                // check that returned one delta
                String msg = "%s Sync should have returned one sync delta after update of one object, but returned: %d";
                assertTrue(deltas.size() == 1, String.format(msg, testMarkMsg, deltas.size()));

                // check delta
                ConnectorHelper.checkSyncDelta(
                        getObjectClassInfo(objectClass), deltas.get(0), uid, replaceAttributes,
                        SyncDeltaType.CREATE_OR_UPDATE, false);

                /*
                 * check the attributes inside delta This is specific for
                 * AttributeTests
                 */
                ConnectorObject obj = deltas.get(0).getObject();
                checkAttributes(obj, oci, ApiOperations.SYNC);

                token = deltas.get(0).getToken();
            }
        }

        /* DELETE: */
        if (SyncApiOpTests.canSyncAfterOp(DeleteApiOp.class)) {
            // delete object
            getConnectorFacade().delete(objectClass, uid, null);

            // sync after delete
            List<SyncDelta> deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token, null);

            // check that returned one delta
            String msg = "%s Sync should have returned one sync delta after delete of one object, but returned: %d";
            assertTrue(deltas.size() == 1, String.format(msg, testMarkMsg, deltas.size()));

            // check delta
            ConnectorHelper.checkSyncDelta(
                    getObjectClassInfo(objectClass), deltas.get(0), uid, null, SyncDeltaType.DELETE, false);

            /*
             * check the attributes inside delta This is specific for
             * AttributeTests
             */
            ConnectorObject obj = deltas.get(0).getObject();
            checkAttributes(obj, oci, ApiOperations.SYNC);
        }
        return uid;
    }

}// end of class AttributeTests

/** helper inner class for passing the type of tested operations */
enum ApiOperations {
    SEARCH(SearchApiOp.class),
    GET(GetApiOp.class),
    LIVE_SYNC(LiveSyncApiOp.class),
    SYNC(SyncApiOp.class);

    private final String s;

    private final Class<? extends APIOperation> clazz;

    private ApiOperations(Class<? extends APIOperation> c) {
        this.s = c.getName();
        this.clazz = c;
    }

    @Override
    public String toString() {
        return s;
    }

    public Class<? extends APIOperation> getClazz() {
        return clazz;
    }
}

/** helper inner class for saving log information */
class LogInfo {

    /** attribute set */
    private final Set<Attribute> attrSet;

    /** object class */
    private final ObjectClass oc;

    public LogInfo(ObjectClass oc, Set<Attribute> attrSet) {
        this.oc = oc;
        this.attrSet = attrSet;
    }

    public Set<Attribute> getAttrSet() {
        return attrSet;
    }

    public ObjectClass getOc() {
        return oc;
    }

    @Override
    public String toString() {
        return " \n ObjectClass: " + oc.toString() + "\n AttributeSet: " + attrSet.toString();
    }
}
