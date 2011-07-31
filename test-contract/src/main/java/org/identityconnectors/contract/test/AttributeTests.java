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
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SchemaApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * <p>
 * Test that attributes satisfy contract.
 * </p>
 * Tests check:
 * <ul>
 * <li>non-readable attributes are not returnedByDefault</li>
 * <li>attributes which are not returnedByDefault really are not returned
 * unless</li>
 * specified in attrsToGet </li>
 * <li>update of non-updateable attribute will fail</li>
 * <li>required attributes must be creatable</li>
 * </ul>
 * 
 * @author David Adam
 */
@RunWith(Parameterized.class)
public class AttributeTests extends ObjectClassRunner {

    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(AttributeTests.class);
    private static final String TEST_NAME = "Attribute";

    public AttributeTests(ObjectClass oclass) {
        super(oclass);
    }
    
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> res = new HashSet<Class<? extends APIOperation>>();
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
    public void testRun() {
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
    @Test
    public void testNonReadable() {
        if (ConnectorHelper.operationsSupported(getConnectorFacade(),
                getObjectClass(), getAPIOperations())) {
            Uid uid = null;
            try {
                ObjectClassInfo oci = getObjectClassInfo();

                // create a new user
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), oci, getTestName(), 0, true, false);
                // should throw UnsupportedObjectClass if not supported
                uid = getConnectorFacade().create(getSupportedObjectClass(),
                        attrs, getOperationOptionsByOp(CreateApiOp.class));

                // get the user to make sure it exists now
                ConnectorObject obj = getConnectorFacade().getObject(
                        getObjectClass(), uid,
                        null/* GET returned by default attributes*/);

                assertNotNull("Unable to retrieve newly created object", obj);

                // check: non readable attributes should not be returned by
                // default
                for (Attribute attr : obj.getAttributes()) {
                    if (!ConnectorHelper.isReadable(oci, attr)) {
                        String msg = String
                                .format(
                                        "Non-readable attribute should not be returned by default: %s",
                                        attr.getName());
                        assertTrue(msg, !ConnectorHelper.isReturnedByDefault(
                                oci, attr));
                    }
                }
            } finally {
                if (uid != null) {
                    // delete the object
                    getConnectorFacade().delete(getSupportedObjectClass(), uid,
                            getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
        } else {
            printSkipTestMsg("testNonReadable");
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
    @Test
    public void testReturnedByDefault() {
        if (ConnectorHelper.operationSupported(getConnectorFacade(),
                getObjectClass(), CreateApiOp.class)) {
            // run the test for GetApiOp, SearchApiOp and SyncApiOp
            for (ApiOperations apiop : ApiOperations.values()) {
                testReturnedByDefault(apiop);
            }
        } else {
            printSkipTestMsg("testReturnedByDefault");
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
    @Test
    public void testNonUpdateable() {
        boolean exceptionCaught = false;
        /** is there any non updateable item? (if not skip this test) */
        boolean isChanged = false;
        /** logging info bean */
        LogInfo logInfo = null;

        if (ConnectorHelper.operationsSupported(getConnectorFacade(),
                getObjectClass(), getAPIOperations())) {
            ConnectorObject obj = null;
            Uid uid = null;

            try {
                // create an object to update
                uid = ConnectorHelper.createObject(getConnectorFacade(),
                        getDataProvider(), getObjectClassInfo(), getTestName(),
                        1, getOperationOptionsByOp(CreateApiOp.class));
                assertNotNull("Create returned null Uid.", uid);

                // get by uid
                obj = getConnectorFacade().getObject(getSupportedObjectClass(),
                        uid, getOperationOptionsByOp(GetApiOp.class));
                assertNotNull("Cannot retrieve created object.", obj);

                // ******************************
                // Acquire updateable attributes
                Schema schema = getConnectorFacade().schema();
                Set<Attribute> nonUpdateableAttrs = getNonUpdateableAttributes(schema, getObjectClass()); 

                // null indicates an empty set ==> no non-updateable attributes
                isChanged = (nonUpdateableAttrs != null)? true : false;

                if (isChanged) {
                    //keep logging info
                    logInfo = new LogInfo(getObjectClass(), nonUpdateableAttrs);

                    assertTrue("no update attributes were found",
                            (nonUpdateableAttrs.size() > 0));
                    Uid newUid = getConnectorFacade().update(
                            getObjectClass(),
                            uid,
                            AttributeUtil.filterUid(nonUpdateableAttrs),
                            getOperationOptionsByOp(UpdateApiOp.class));

                    // Update change of Uid must be propagated to
                    // replaceAttributes
                    // set
                    if (!newUid.equals(uid)) {
                        nonUpdateableAttrs.remove(uid);
                        nonUpdateableAttrs.add(newUid);
                        uid = newUid;
                    }

                    // verify the change
                    obj = getConnectorFacade().getObject(
                            getSupportedObjectClass(), uid,
                            getOperationOptionsByOp(GetApiOp.class));
                    assertNotNull("Cannot retrieve updated object.", obj);
                    ConnectorHelper.checkObject(getObjectClassInfo(), obj,
                            nonUpdateableAttrs);
                } else {
                    /*
                     * SKIPPING THE TEST
                     * no non-updateable attribute present
                     */
                    printSkipNonUpdateableTestMsg();
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
                            getSupportedObjectClass(), uid, false,
                            getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
            
            // in case no exception is thrown:
            if (!exceptionCaught) {
                fail(String.format("No exception thrown when update is performed on non-updateable attribute(s). (hint: throw a RuntimeException) %s", ((logInfo != null)?logInfo.toString():"")));
            }
        } else {
            printSkipTestMsg("testNonUpdateable");
        }
        
    }
    
    /**
     * return the Non-Updateable attributes for currently tested objectclass
     * @param schema the schema of currently tested connector
     * @param objectClass the currently tested objectclass
     * @return
     */
    private Set<Attribute> getNonUpdateableAttributes(Schema schema,
            ObjectClass objectClass) {
        Set<Attribute> result = new HashSet<Attribute>();
        
        //objectClass that we search for
        ObjectClassInfoBuilder oib = new ObjectClassInfoBuilder();
        oib.setType(objectClass.getObjectClassValue());
        ObjectClassInfo ocToFind = oib.build();
        
        Set<ObjectClassInfo> oci = schema.getObjectClassInfo();
        for (ObjectClassInfo objectClassInfo : oci) {
            if (objectClassInfo.getType().equals(ocToFind.getType())) {
                // we found the currently tested object class

                // do a scan through attributes and look for non-updateable ones.
                Set<AttributeInfo> attrInfo = objectClassInfo.getAttributeInfo();
                for (AttributeInfo attributeInfo : attrInfo) {
                    if (!attributeInfo.isUpdateable()) {
                        // create an empty list value for update
                        Attribute attr = AttributeBuilder.build(attributeInfo.getName());
                        // TODO might add some reasonable value for the attribute to update
                        result.add(attr);
                    }
                }
            }
        }
        
        return (result.size() == 0)? null : result;
    }

    /**
     * prints log message when skipping
     * {@link AttributeTests#testNonUpdateable()} test
     */
    private void printSkipNonUpdateableTestMsg() {
        printSkipTestMsg("testNonUpdateable");
    }
    
    /**
     * prints log message when skipping
     * {@link AttributeTests#testNonUpdateable()} test
     * 
     * @param testName the name of the test to print
     */
    private void printSkipTestMsg(String testName) {
        LOG
                .info("----------------------------------------------------------------------------------------");
        LOG
                .info(
                        "Skipping test ''{0}'' for object class ''{1}''. (Reason: non-updateable attrs. are missing)",
                        testName, getObjectClass());
        LOG
                .info("----------------------------------------------------------------------------------------");

    }

    /**
     * Required attributes must be creatable. It is a fialure if a required
     * attribute is not creatable.
     */
    @Test
    public void testRequirableIsCreatable() {
        if (ConnectorHelper.operationSupported(getConnectorFacade(),
                getObjectClass(), CreateApiOp.class)
                && ConnectorHelper.operationSupported(getConnectorFacade(),
                        getObjectClass(), GetApiOp.class)) {
            Uid uid = null;
            try {
                ObjectClassInfo oci = getObjectClassInfo();

                // create a new user
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), oci, getTestName(), 2, true, false);
                // should throw UnsupportedObjectClass if not supported
                uid = getConnectorFacade().create(getSupportedObjectClass(),
                        attrs, getOperationOptionsByOp(CreateApiOp.class));

                // get the user to make sure it exists now
                ConnectorObject obj = getConnectorFacade().getObject(
                        getObjectClass(), uid,
                        getOperationOptionsByOp(GetApiOp.class));

                assertNotNull("Unable to retrieve newly created object", obj);

                // check: Required attributes must be createable.
                for (Attribute attr : obj.getAttributes()) {
                    if (ConnectorHelper.isRequired(oci, attr)) {
                        if (!ConnectorHelper.isCreateable(oci, attr)) {
                            //WARN
                            String msg = String.format("Required attribute is not createable. Attribute name: %s", attr.getName());
                            fail(msg);
                        }
                    }
                }
            } finally {
                if (uid != null) {
                    // delete the object
                    getConnectorFacade().delete(getSupportedObjectClass(), uid,
                            getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
        } else {
            LOG
                    .info("----------------------------------------------------------------------------------------");
            LOG
                    .info(
                            "Skipping test ''testNonReadable'' for object class ''{0}''.",
                            getObjectClass());
            LOG
                    .info("----------------------------------------------------------------------------------------");
        }
    }

    /* ******************** HELPER METHODS ******************** */
    /**
     * {@link AttributeTests#testReturnedByDefault()}
     * 
     * @param apiOp
     *            the type of ApiOperation, that shall be tested.
     */
    private void testReturnedByDefault(ApiOperations apiOp) {
        /** marker in front of every assert message */
        String testMarkMsg = String.format("[testReturnedByDefault/%s]", apiOp);
        
        // run the contract test only if <strong>apiOp</strong> APIOperation is
        // supported
        if (ConnectorHelper.operationSupported(getConnectorFacade(),
                getObjectClass(), apiOp.getClazz())) {
            
            // start synchronizing from now
            SyncToken token = null;
            if (apiOp.equals(ApiOperations.SYNC)) { // just for SyncApiOp test
                token = getConnectorFacade().getLatestSyncToken(getObjectClass());
            }

            Uid uid = null;
            try {
                ObjectClassInfo oci = getObjectClassInfo();

                /*
                 * CREATE a new user
                 */ 
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), oci, getTestName(), 3, true, false);
                // should throw UnsupportedObjectClass if not supported
                uid = getConnectorFacade().create(getObjectClass(), attrs,
                        null);
                assertNotNull(testMarkMsg + " Create returned null uid.", uid);

                /*
                 * ************ GetApiOp ************
                 */
                // get the user to make sure it exists now
                ConnectorObject obj = null;
                switch (apiOp) {
                case GET:
                    /* last _null_ param - no operation option, response contains just attributes returned by default*/
                    obj = getConnectorFacade().getObject(getObjectClass(), uid, null);
                    break;// GET

                case SEARCH:
                    Filter fltUid = FilterBuilder.equalTo(AttributeBuilder
                            .build(Uid.NAME, uid.getUidValue()));

                    assertTrue(testMarkMsg + " filterUid is null", fltUid != null);

                    List<ConnectorObject> coObjects = ConnectorHelper.search(
                            getConnectorFacade(), getSupportedObjectClass(),
                            fltUid, null);

                    assertTrue(testMarkMsg + 
                            " Search filter by uid with no OperationOptions failed, expected to return one object, but returned "
                                    + coObjects.size(), coObjects.size() == 1);

                    assertNotNull(testMarkMsg + " Unable to retrieve newly created object",
                            coObjects.get(0));

                    obj = coObjects.get(0);
                    break;// SEARCH

                case SYNC:
                    uid = testSync(uid, token, attrs, oci, testMarkMsg);
                    break;// SYNC
                }//switch

                /*
                 * Check if attribute set contains non-returned by default
                 * Attributes. This is specific for AttributeTests
                 */
                if (!apiOp.equals(ApiOperations.SYNC)) {
                    assertNotNull("Unable to retrieve newly created object", obj);
                    // obj is null for sync tests
                    checkAttributes(obj, oci, apiOp);
                }

            } finally {
                if (uid != null) {
                    // cleanup test data
                    ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid,
                            false, getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
        } else {
            LOG
                    .info("----------------------------------------------------------------------------------------");
            LOG
                    .info(
                            "Skipping test ''testReturnedByDefault'' for object class ''{0}''.",
                            getObjectClass());
            LOG
                    .info("----------------------------------------------------------------------------------------");
        }
    }

    /** Main checking of "no returned by default" attributes 
     * @param testName 
     * @param apiOp */
    private void checkAttributes(ConnectorObject obj, ObjectClassInfo oci, ApiOperations apiOp) {
        // Check if attribute set contains non-returned by default
        // Attributes.
        for (Attribute attr : obj.getAttributes()) {
            String msg = String
                    .format(
                            "[testReturnedByDefault / %s]Attribute %s returned. However it is _not_ returned by default.",
                            apiOp, attr.getName());
            /*
             * this is a hack that skips control of UID, as it is presently 
             * non returned by default, however it is automatically returned.
             * see discussion in Issue mailing list -- Issue #334
             * future TODO: after joining UID to schema, erase the condition.
             */
            if (!attr.getName().equals(Uid.NAME)) {
                assertTrue(msg, ConnectorHelper.isReturnedByDefault(oci, attr));
            }
        }
    }

    /**
     * test sync
     * 
     * @param token
     *            initialized token
     * @param attrs
     *            newly created attributes
     * @param uid
     *            the newly created object
     * @param oci
     *            object class info
     * @param testMarkMsg test marker
     * @return the updated Uid
     */
    private Uid testSync(Uid uid, SyncToken token,
            Set<Attribute> attrs, ObjectClassInfo oci, String testMarkMsg) {
        List<SyncDelta> deltas = null;
        String msg = null;

        /*
         * CREATE: (was handled in the calling method, result of create is in
         * param uid, cleanup is also in caller method.)
         */

        if (SyncApiOpTests.canSyncAfterOp(CreateApiOp.class)) {
            // sync after create
            deltas = ConnectorHelper.sync(getConnectorFacade(),
                    getObjectClass(), token,
                    null);

            // check that returned one delta
            msg = "%s Sync should have returned one sync delta after creation of one object, but returned: %d";
            assertTrue(String.format(msg, testMarkMsg, deltas.size()), deltas.size() == 1);

            // check delta
            ConnectorHelper.checkSyncDelta(getObjectClassInfo(), deltas.get(0),
                    uid, attrs, SyncDeltaType.CREATE_OR_UPDATE, false);

            /*
             * check the attributes inside delta This is specific for
             * AttributeTests
             */
            ConnectorObject obj = deltas.get(0).getObject();
            checkAttributes(obj, oci, ApiOperations.SYNC);

            token = deltas.get(0).getToken();
        }

        /* UPDATE: */

        if (ConnectorHelper.operationSupported(getConnectorFacade(),
                UpdateApiOp.class)
                && SyncApiOpTests.canSyncAfterOp(UpdateApiOp.class)) {

            Set<Attribute> replaceAttributes = ConnectorHelper
                    .getUpdateableAttributes(getDataProvider(),
                            getObjectClassInfo(), getTestName(),
                            SyncApiOpTests.MODIFIED, 0, false, false);

            // update only in case there is something to update
            if (replaceAttributes.size() > 0) {
                replaceAttributes.add(uid);

                assertTrue(testMarkMsg + " no update attributes were found",
                        (replaceAttributes.size() > 0));
                Uid newUid = getConnectorFacade().update(
                        getSupportedObjectClass(),
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
                deltas = ConnectorHelper.sync(getConnectorFacade(),
                        getObjectClass(), token,
                        null);

                // check that returned one delta
                msg = "%s Sync should have returned one sync delta after update of one object, but returned: %d";
                assertTrue(String.format(msg, testMarkMsg, deltas.size()),
                        deltas.size() == 1);

                // check delta
                ConnectorHelper.checkSyncDelta(getObjectClassInfo(), deltas
                        .get(0), uid, replaceAttributes,
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
            getConnectorFacade().delete(getObjectClass(), uid,
                    null);

            // sync after delete
            deltas = ConnectorHelper.sync(getConnectorFacade(),
                    getObjectClass(), token,
                    null);

            // check that returned one delta
            msg = "%s Sync should have returned one sync delta after delete of one object, but returned: %d";
            assertTrue(String.format(msg, testMarkMsg, deltas.size()), deltas.size() == 1);

            // check delta
            ConnectorHelper.checkSyncDelta(getObjectClassInfo(), deltas.get(0),
                    uid, null, SyncDeltaType.DELETE, false);

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
    SEARCH(SearchApiOp.class), GET(GetApiOp.class), SYNC(SyncApiOp.class);
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
}// end of enum ApiOperations

/** helper inner class for saving log information */
class LogInfo {
    /** attribute set */
    private Set<Attribute> attrSet;

    /** object class */
    private ObjectClass oc;
    
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
    
    public String toString() {
        return " \n ObjectClass: " + oc.toString() + "\n AttributeSet: " + attrSet.toString();
    }
}// end of LogInfo
