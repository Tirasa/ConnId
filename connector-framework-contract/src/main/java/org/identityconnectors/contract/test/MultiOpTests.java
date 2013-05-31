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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.api.operations.ValidateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.PredefinedAttributes;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/**
 * Tests which use many APIOperations to do the test scenario
 *
 * @author Tomas Knappek
 * @author Zdenek Louzensky
 */
@Test(testName =  MultiOpTests.TEST_NAME)
public class MultiOpTests extends ObjectClassRunner {

    /**
     * Logging..
     */
    private static final Logger logger = Logger.getLogger(ValidateApiOpTests.class);

    public static final String TEST_NAME = "Multi";
    private static final String MODIFIED = "modified";
    private static final String LOCKOUT_PREFIX = "lockout";
    private static final String SKIP = "skip";

    /**
     * Scenario test - test positive cases. {@inheritDoc} Test assumes that
     * Schema,Create,Search and Delete are supported operations.
     *
     */
    @Override
    protected void testRun(ObjectClass objectClass) {
        // initial number of objects to be created
        final int recordCount = 10;

        List<Uid> uids = new ArrayList<Uid>();
        List<Set<Attribute>> attrs = new ArrayList<Set<Attribute>>();

        // objects stored in connector resource before test
        Map<Uid, ConnectorObject> coBeforeTest = null;

        // sync variables
        SyncToken token = null;
        List<SyncDelta> deltas = null;

        // variable for assert messages
        String msg = null;

        try {
            /* SearchApiOp - get objects stored in connector resource before test */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, SearchApiOp.class)) {
                // null filter
                coBeforeTest = ConnectorHelper.search2Map(getConnectorFacade(), objectClass,
                        null, getOperationOptionsByOp(objectClass, SearchApiOp.class));
            }

            /* SyncApiOp - start synchronizing from now */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, SyncApiOp.class)) {
                // start synchronizing from now
                token = getConnectorFacade().getLatestSyncToken(objectClass);
            }

            /* CreateApiOp - create initial objects */
            for (int i = 0; i < recordCount; i++) {
                Set<Attribute> attr = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), i, true, false);
                Uid luid = getConnectorFacade().create(objectClass, attr, getOperationOptionsByOp(objectClass, CreateApiOp.class));
                assertNotNull(luid,"Create returned null uid.");
                attrs.add(attr);
                uids.add(luid);
            }

            /* GetApiOp - check that objects were created with attributes as requested */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, GetApiOp.class)) {
                for (int i = 0; i < recordCount; i++) {
                    ConnectorObject obj = getConnectorFacade().getObject(objectClass,
                            uids.get(i), getOperationOptionsByOp(objectClass, GetApiOp.class));
                    assertNotNull(obj,"Unable to retrieve newly created object");

                    ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, attrs.get(i));
                }
            }

            /* TestApiOp */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, TestApiOp.class)) {
                // should NOT throw
                getConnectorFacade().test();
            }

            /* SyncApiOp - check sync of created objects */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, SyncApiOp.class)) {
                if (SyncApiOpTests.canSyncAfterOp(CreateApiOp.class)) {
                    // sync after create
                    deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token,
                            getOperationOptionsByOp(objectClass, SyncApiOp.class));

                    msg = "Sync after %d creates returned %d deltas.";
                    assertTrue(deltas.size() == recordCount,String.format(msg, recordCount, deltas.size()));

                    // check all deltas
                    for (int i = 0; i < recordCount; i++) {
                        ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), deltas.get(i), uids
                                .get(i), attrs.get(i), SyncDeltaType.CREATE_OR_UPDATE, true);
                    }

                    token = deltas.get(recordCount - 1).getToken();
                }
            }

            /* DeleteApiOp - delete one object */
            Uid deleteUid = uids.remove(0);
            attrs.remove(0);

            // delete it and check that it was really deleted
            ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, deleteUid, true,
                    getOperationOptionsByOp(objectClass, DeleteApiOp.class));

            /* SearchApiOp - search with null filter */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, SearchApiOp.class)) {
                List<ConnectorObject> coFound = ConnectorHelper.search(getConnectorFacade(),
                        objectClass, null, getOperationOptionsByOp(objectClass, SearchApiOp.class));
                assertTrue(coFound.size() == uids.size() + coBeforeTest.size(),
                        "Search with null filter returned different count of results. Expected: "
                                + uids.size() + coBeforeTest.size() + ", but returned: "
                                + coFound.size());
                // check all objects
                for (ConnectorObject obj : coFound) {
                    if (uids.contains((obj.getUid()))) {
                        int index = uids.indexOf(obj.getUid());
                        ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, attrs.get(index));
                    } else {
                        if (SearchApiOpTests.compareExistingObjectsByUidOnly()) {
                            assertTrue(coBeforeTest.containsKey(obj.getUid()),
                                    "Search with null filter returned unexpected object " + obj + ", objects were compared by Uid.");
                        }
                        else {
                            assertTrue(coBeforeTest.containsValue(obj),
                                    "Search with null filter returned unexpected object " + obj + ", objects were compared by Uid.");
                        }
                    }
                }
            }

            /* UpdateApiOp - update one object */
            Uid updateUid = null;
            Set<Attribute> replaceAttributes = null;
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, UpdateApiOp.class)) {
                updateUid = uids.remove(0);
                attrs.remove(0);
                replaceAttributes = ConnectorHelper.getUpdateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), MODIFIED, 0, false, false);

                // update only in case there is something to update
                if (replaceAttributes.size() > 0) {
                    // Uid must be present in attributes
                    replaceAttributes.add(updateUid);
                    Uid newUid = getConnectorFacade().update(
                            objectClass,
                            updateUid,
                            AttributeUtil.filterUid(replaceAttributes), getOperationOptionsByOp(objectClass, UpdateApiOp.class));
                    replaceAttributes.remove(updateUid);

                    if (!updateUid.equals(newUid)) {
                        updateUid = newUid;
                    }

                    attrs.add(replaceAttributes);
                    uids.add(updateUid);
                }

                /* SearchApiOp - search with Uid filter */
                if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, SearchApiOp.class)) {
                    // search by Uid
                    Filter fltUid = FilterBuilder.equalTo(updateUid);
                    List<ConnectorObject> coFound = ConnectorHelper.search(getConnectorFacade(), objectClass,
                            fltUid, getOperationOptionsByOp(objectClass, SearchApiOp.class));
                    assertTrue(coFound.size() == 1,"Search with Uid filter returned unexpected number of objects. Expected: 1, but returned: "
                                    + coFound.size());
                    ConnectorHelper.checkObject(getObjectClassInfo(objectClass), coFound.get(0),
                            replaceAttributes);
                }
            }

            /* SyncApiOp - sync after one delete and one possible update */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, SyncApiOp.class)) {
                if (SyncApiOpTests.canSyncAfterOp(DeleteApiOp.class)
                        || SyncApiOpTests.canSyncAfterOp(UpdateApiOp.class)) {

                    deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass,
                            token, getOperationOptionsByOp(objectClass, SyncApiOp.class));
                    // one deleted, one updated (if existed attributes to
                    // update)
                    assertTrue(((deltas.size() <= 2) && (deltas.size() > 0)),"Sync returned unexpected number of deltas. Exptected: max 2, but returned: "
                                    + deltas.size());

                    for (int i = 0; i < deltas.size(); i++) {
                        SyncDelta delta = deltas.get(i);

                        if (SyncDeltaType.CREATE_OR_UPDATE.equals(delta.getDeltaType())) {
                            ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), delta,
                                    updateUid, replaceAttributes, SyncDeltaType.CREATE_OR_UPDATE, true);
                        }
                        else {
                            ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), delta,
                                    deleteUid, null, SyncDeltaType.DELETE, true);
                        }

                        // remember last token
                        token = delta.getToken();
                    }
                }
            }

            /* ValidateApiOp */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, ValidateApiOp.class)) {
                // should NOT throw
                getConnectorFacade().validate();
            }

            /* CreateApiOp - create one last object */
            Set<Attribute> attrs11 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), recordCount + 1, true, false);
            Uid createUid = getConnectorFacade().create(objectClass, attrs11, getOperationOptionsByOp(objectClass, CreateApiOp.class));
            uids.add(createUid);
            attrs.add(attrs11);
            assertNotNull(createUid,"Create returned null Uid.");

            /* GetApiOp */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, GetApiOp.class)) {
                // get the object to make sure it exist now
                ConnectorObject obj = getConnectorFacade().getObject(objectClass, createUid,
                        getOperationOptionsByOp(objectClass, GetApiOp.class));
                assertNotNull(obj,"Unable to retrieve newly created object");

                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, attrs11);
            }

            /* DeleteApiOp - delete one object */
            deleteUid = uids.remove(0);
            attrs.remove(0);
            // delete it and check that it was really deleted
            ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, deleteUid, true,
                    getOperationOptionsByOp(objectClass, DeleteApiOp.class));

            /* SyncApiOp - after create, delete */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, SyncApiOp.class)) {
                if (SyncApiOpTests.canSyncAfterOp(DeleteApiOp.class)
                        || SyncApiOpTests.canSyncAfterOp(CreateApiOp.class)) {
                    deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token,
                            getOperationOptionsByOp(objectClass, SyncApiOp.class));
                    // one deleted, one created
                    assertTrue(deltas.size() <= 2,"Sync returned unexpected number of deltas. Exptected: max 2, but returned: "
                                    + deltas.size());

                    for (int i = 0; i < deltas.size(); i++) {
                        SyncDelta delta = deltas.get(i);

                        if (SyncDeltaType.CREATE_OR_UPDATE.equals(delta.getDeltaType())) {
                            ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), delta,
                                    createUid, attrs11, SyncDeltaType.CREATE_OR_UPDATE, true);
                        }
                        else {
                            ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), delta,
                                    deleteUid, null, SyncDeltaType.DELETE, true);
                        }

                        // remember last token
                        token = delta.getToken();
                    }
                }
            }

            /* DeleteApiOp - delete all objects */
            for (int i = 0; i < uids.size(); i++) {
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uids.get(i),
                        true, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }

            /* SyncApiOp - all objects were deleted */
            if (ConnectorHelper.operationSupported(getConnectorFacade(), objectClass, SyncApiOp.class)
                    && SyncApiOpTests.canSyncAfterOp(DeleteApiOp.class)) {
                deltas = ConnectorHelper.sync(getConnectorFacade(), objectClass, token,
                        getOperationOptionsByOp(objectClass, SyncApiOp.class))
                        ;
                msg = "Sync returned unexpected number of deltas. Exptected: %d, but returned: %d";
                assertTrue(deltas.size() == uids.size(),String.format(msg, uids.size(), deltas.size()));

                for (int i = 0; i < uids.size(); i++) {
                    ConnectorHelper.checkSyncDelta(getObjectClassInfo(objectClass), deltas.get(i),
                            uids.get(i), null, SyncDeltaType.DELETE, true);
                }
            }

        } finally {
            // cleanup
            for (Uid deluid : uids) {
                try {
                    ConnectorHelper.deleteObject(getConnectorFacade(), objectClass,
                            deluid, false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                } catch (Exception e) {
                    // ok
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        s.add(CreateApiOp.class);
        s.add(DeleteApiOp.class);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestName() {
        return TEST_NAME;
    }

    /*
     * *****************************
     * OPERATIONAL ATTRIBUTES TESTS:
     * *****************************
     */

    /**
     * Tests ENABLE attribute contract
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testEnableOpAttribute(ObjectClass objectClass) {
        if (isObjectClassSupported(objectClass)
                && ConnectorHelper.isCRU(getObjectClassInfo(objectClass), OperationalAttributes.ENABLE_NAME)) {

            // check ENABLE for true
            checkOpAttribute(objectClass, OperationalAttributes.ENABLE_NAME, true, false, Boolean.class);

            // check ENABLE for false
            checkOpAttribute(objectClass, OperationalAttributes.ENABLE_NAME, false, true, Boolean.class);
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testEnableOpAttribute'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Tests ENABLE_DATE attribute contract
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testEnableDateOpAttribute(ObjectClass objectClass) {
        if (isObjectClassSupported(objectClass)
                && ConnectorHelper.isCRU(getObjectClassInfo(objectClass), OperationalAttributes.ENABLE_DATE_NAME)) {

        	// try to retrieve the optional contract tests property for setting the dates, otherwise use the default values
        	//"now"
            final long createValue = getDateProperty(OperationalAttributes.ENABLE_DATE_NAME, (new Date()).getTime(), false);
            //"1.1.1970"
            final long updateValue = getDateProperty(OperationalAttributes.ENABLE_DATE_NAME, (new Date(0)).getTime(), true);

            // check ENABLE_DATE for "now" and "1.1.1970"
            checkOpAttribute(objectClass, OperationalAttributes.ENABLE_DATE_NAME, createValue,
                    updateValue, Long.class);
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testEnableDateOpAttribute'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Tests DISABLE_DATE attribute contract
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testDisableDateOpAttribute(ObjectClass objectClass) {
        if (isObjectClassSupported(objectClass)
                && ConnectorHelper.isCRU(getObjectClassInfo(objectClass), OperationalAttributes.DISABLE_DATE_NAME)) {


        	// try to retrieve the optional contract tests property for setting the dates, otherwise use the default values
        	//"now"
            final long createValue = getDateProperty(OperationalAttributes.DISABLE_DATE_NAME, (new Date()).getTime(), false);
            //"1.1.1970"
            final long updateValue = getDateProperty(OperationalAttributes.DISABLE_DATE_NAME, (new Date(0)).getTime(), true);

			// check DISABLE_DATE for "now" and "1.1.1970"
            checkOpAttribute(objectClass, OperationalAttributes.DISABLE_DATE_NAME, createValue ,
                    updateValue, Long.class);
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testDisableDateOpAttribute'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
	 * helper method for {@link MultiOpTests#testDisableDateOpAttribute(ObjectClass)}
	 *
	 * @param defaultValue
	 *            in case property is not found, this value is used
	 * @param isModified
	 *            if the modified keyword is used
	 */
	private long getDateProperty(String propName, long defaultValue, boolean isModified) {
		try {
			if (isModified) {
				propName = String.format("%s.%s", MODIFIED, propName);
			}

			Object obj = getDataProvider().getTestSuiteAttribute(propName, getTestName());
			if (obj instanceof Long) {
				return (Long) obj;
			} else {
				Assert.fail(String.format("Property 'testsuite.%s.%s' should be of type *long*", getTestName(), propName));
			}
		} catch (ObjectNotFoundException onfe) {
			// use the default value, OK
		}
		return defaultValue;
	}



	/**
     * Tests LOCK_OUT attribute contract
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testLockOutOpAttribute(ObjectClass objectClass) {
        if (isObjectClassSupported(objectClass)
                && ConnectorHelper.isCRU(getObjectClassInfo(objectClass), OperationalAttributes.LOCK_OUT_NAME) && canLockOut()) {

            // check: setting LOCKOUT from true to false
            checkOpAttribute(objectClass, OperationalAttributes.LOCK_OUT_NAME, true, false, Boolean.class);

            // check: setting LOCKOUT from false for true
            checkOpAttribute(objectClass, OperationalAttributes.LOCK_OUT_NAME, false, true, Boolean.class);
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testLockOutOpAttribute'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Tests PASSWORD_EXPIRATION_DATE attribute contract
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testPasswordExpirationDateOpAttribute(ObjectClass objectClass) {
        if (isObjectClassSupported(objectClass)
                && ConnectorHelper.isCRU(getObjectClassInfo(objectClass), OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME)) {

            // check PASSWORD_EXPIRATION_DATE for "now" and "1.1.1970"
            checkOpAttribute(objectClass, OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME, (new Date()).getTime(),
                    (new Date(0)).getTime(), Long.class);
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testPasswordExpirationDateOpAttribute'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Tests PASSWORD_EXPIRED attribute contract
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testPasswordExpiredOpAttribute(ObjectClass objectClass) {
        if (isObjectClassSupported(objectClass)
                && ConnectorHelper.isCRU(getObjectClassInfo(objectClass), OperationalAttributes.PASSWORD_EXPIRED_NAME)) {

            // check PASSWORD_EXPIRED for false
            checkOpAttribute(objectClass, OperationalAttributes.PASSWORD_EXPIRED_NAME, false, true, Boolean.class, true);
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testPasswordExpiredOpAttribute'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Tests PASSWORD_CHANGE_INTERVAL attribute contract
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testPasswordChangeIntervalPredAttribute(ObjectClass objectClass) {
        if (isObjectClassSupported(objectClass)
                && ConnectorHelper.isCRU(getObjectClassInfo(objectClass), PredefinedAttributes.PASSWORD_CHANGE_INTERVAL_NAME)) {

            // check PASSWORD_CHANGE_INTERVAL for 120 days and 30 days
            checkOpAttribute(objectClass, PredefinedAttributes.PASSWORD_CHANGE_INTERVAL_NAME, 10368000000L, 2592000000L, Long.class);
        }
        else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testPasswordChangeIntervalPredAttribute'' for object class ''"+objectClass+"''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }

    /**
     * Method to check the attrName's attribute contract
     *
     * @param attrName attribute to be checked
     * @param createValue value used for create
     * @param updateValue value used for update
     * @param type expected type of the value
     * @param addPassword, add password to attributes in the update
     */
    private void checkOpAttribute(ObjectClass objectClass, String attrName, Object createValue, Object updateValue, Class<?> type, boolean addPassword) {
        final int iteration = 0;
        Set<Attribute> attrs = null;

        attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(), getObjectClassInfo(objectClass),
                getTestName(), iteration, true, false);

        //remove attrName if present
        for (Attribute attribute : attrs) {
            if (attribute.is(attrName)) {
                attrs.remove(attribute);
                break;
            }
        }

        //add attrName with create value
        attrs.add(AttributeBuilder.build(attrName, createValue));

        Uid uid = null;

        try {
            //create
            uid = getConnectorFacade().create(objectClass, attrs, null);

            // check value of attribute with create value
            checkAttribute(objectClass, attrName, uid, createValue, type);

            // clear attrs
            attrs.clear();

            //add update value
            attrs.add(AttributeBuilder.build(attrName, updateValue));

            if (addPassword) {
                // add the same password, that was created before in the following update
                GuardedString password = (GuardedString) ConnectorHelper.get(getDataProvider(),
                        getTestName(), GuardedString.class, OperationalAttributes.PASSWORD_NAME,
                        getObjectClassInfo(objectClass).getType(), iteration, false);
                Attribute attrPasswd = AttributeBuilder.build(OperationalAttributes.PASSWORD_NAME,
                        password);
                attrs.add(attrPasswd);
            }

            // add uid for update
            attrs.add(uid);

            //update
            uid = getConnectorFacade().update(objectClass, uid,
                    AttributeUtil.filterUid(attrs), null);

            //check again with update value
            checkAttribute(objectClass, attrName, uid, updateValue, type);

        } finally {
            ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid, false, null);
        }
    }

    /**
     * {@link MultiOpTests#checkOpAttribute(ObjectClass, String, Object, Object, Class, boolean)}
     */
    private void checkOpAttribute(ObjectClass objectClass, String attrName, Object createValue, Object updateValue, Class<?> type) {
        checkOpAttribute(objectClass, attrName, createValue, updateValue, type, false);
    }

    /**
     * Gets the ConnectorObject and check the value of attribute is as
     * expected
     *
     * @param uid Uid of object to get
     * @param expValue expected value of the attribute
     * @param type expected type of the attribute
     */
    private void checkAttribute(ObjectClass objectClass, String attrName, Uid uid, Object expValue, Class<?> type) {
        //get the object
        ConnectorObject obj = getConnectorFacade().getObject(objectClass, uid, null);

        //check we have the correct value
        for (Attribute attribute : obj.getAttributes()) {
            if (attribute.is(attrName)) {
                List<Object> vals = attribute.getValue();
                assertTrue(vals.size() == 1,String.format("Operational attribute %s must contain exactly one value.",
                        attrName));
                Object val = vals.get(0);
                assertEquals(type, val.getClass(),String.format(
                        "Operational attribute %s value type must be %s, but is %s.", attrName,
                        type.getSimpleName(), val.getClass().getSimpleName()));

                assertEquals(expValue, val,String.format("Operational attribute %s value is different, expected: %s, returned: %s",
                        attrName, expValue, val));
            }
        }
    }

    /**
     * Tests GROUPS attribute contract
     */
    @Test
    public void testGroupsPredAttribute() {
        final ObjectClassInfo accountInfo = findOInfo(ObjectClass.ACCOUNT);
        final ObjectClassInfo groupInfo = findOInfo(ObjectClass.GROUP);

        // run test only in case ACCOUNT and GROUP are supported and GROUPS is supported for ACCOUNT
        if (accountInfo != null && groupInfo != null
                && ConnectorHelper.isCRU(accountInfo, PredefinedAttributes.GROUPS_NAME)) {

            Uid groupUid1 = null;
            Uid groupUid2 = null;
            Uid accountUid1 = null;
            try {
                // create 1st group
                Set<Attribute> groupAttrs1 = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), groupInfo, getTestName(), 0, true, false);
                groupUid1 = getConnectorFacade().create(ObjectClass.GROUP, groupAttrs1, null);

                // create an account with GROUPS set to created GROUP
                Set<Attribute> accountAttrs1 = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), accountInfo, getTestName(), 0, true, false);
                for (Attribute attr : accountAttrs1) {
                    if (attr.is(PredefinedAttributes.GROUPS_NAME)) {
                        accountAttrs1.remove(attr);
                        break;
                    }
                }
                accountAttrs1.add(AttributeBuilder.build(PredefinedAttributes.GROUPS_NAME,
                        AttributeUtil.getStringValue(groupUid1)));

                accountUid1 = getConnectorFacade().create(ObjectClass.ACCOUNT, accountAttrs1, null);

                // build attributes to get
                OperationOptionsBuilder oob = new OperationOptionsBuilder();
                oob.setAttributesToGet(ConnectorHelper.getReadableAttributesNames(accountInfo));
                OperationOptions attrsToGet = oob.build();

                // get the account to make sure it exists now
                ConnectorObject obj = getConnectorFacade().getObject(ObjectClass.ACCOUNT,
                        accountUid1, attrsToGet);

                // check that object was created properly
                ConnectorHelper.checkObject(accountInfo, obj, accountAttrs1);

                // continue test only if update is supported for account and GROUPS is multiValue
                if (ConnectorHelper.operationSupported(getConnectorFacade(), ObjectClass.ACCOUNT,
                        UpdateApiOp.class) && ConnectorHelper.isMultiValue(accountInfo, PredefinedAttributes.GROUPS_NAME)) {
                    // create another group
                    Set<Attribute> groupAttrs2 = ConnectorHelper.getCreateableAttributes(
                            getDataProvider(), groupInfo, getTestName(), 1, true, false);
                    groupUid2 = getConnectorFacade().create(ObjectClass.GROUP, groupAttrs2, null);

                    // update account to contain both groups
                    Set<Attribute> accountAttrs2 = new HashSet<Attribute>();
                    accountAttrs2.add(AttributeBuilder.build(PredefinedAttributes.GROUPS_NAME,
                    		AttributeUtil.getStringValue(groupUid2)));
                    accountAttrs2.add(accountUid1);
                    accountUid1 = getConnectorFacade().addAttributeValues(
                            ObjectClass.ACCOUNT, accountUid1, AttributeUtil.filterUid(accountAttrs2), null);

                    // get the account to make sure it exists now and values are correct
                    obj = getConnectorFacade().getObject(ObjectClass.ACCOUNT, accountUid1,
                            attrsToGet);

                    // check that object was created properly
                    ConnectorHelper.checkObject(accountInfo, obj, UpdateApiOpTests
                            .mergeAttributeSets(accountAttrs1, accountAttrs2));
                }
            } finally {
                ConnectorHelper.deleteObject(getConnectorFacade(), ObjectClass.GROUP, groupUid1,
                        false, null);
                ConnectorHelper.deleteObject(getConnectorFacade(), ObjectClass.GROUP, groupUid2,
                        false, null);
                ConnectorHelper.deleteObject(getConnectorFacade(), ObjectClass.ACCOUNT,
                        accountUid1, false, null);
            }

        } else {
            logger.info("----------------------------------------------------------------------------------------");
            logger.info("Skipping test ''testGroupsPredAttribute''.");
            logger.info("----------------------------------------------------------------------------------------");
        }
    }


    /**
     * Returns ObjectClassInfo stored in connector schema for object class.
     */
    private ObjectClassInfo findOInfo(ObjectClass oclass) {
        Schema schema = getConnectorFacade().schema();
        for (ObjectClassInfo oinfo : schema.getObjectClassInfo()) {
            if (oinfo.is(oclass.getObjectClassValue())) {
                return oinfo;
            }
        }

        return null;
    }

    /**
     * <p>
     * Returns true if tests are configured to lockout tests
     * {@link MultiOpTests#testLockOutOpAttribute(ObjectClass)}.
     * </p>
     *
     * <p>
     * Returns true if tests are configured to test connector's lockout
     * operation. Some connectors implement lockout but are capable
     * to unlock but not lock.
     * </p>
     */
    private static boolean canLockOut() {
        // by default it's supposed that case insensitive search is disabled.
        Boolean canLockout = true;
        try {
            canLockout = !(Boolean) getDataProvider().getTestSuiteAttribute(
                    SKIP + "." + LOCKOUT_PREFIX,
                    TEST_NAME);

        } catch (ObjectNotFoundException ex) {
            // exceptions is throw in case property definition is not found
            // ok -- indicates enabling the property
        }

        return canLockout;
    }


}
