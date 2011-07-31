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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Contract test of {@link SearchApiOp} 
 */
@RunWith(Parameterized.class)
public class SearchApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(SearchApiOpTests.class);
    private static final String TEST_NAME = "Search";
    
    /**
     * Properties' prefixes to enable case insensitive search tests
     * (Connectors by default are not capable of this.)
     */
    private static final String CASE_INSENSITIVE_PREFIX = "caseinsensitive";
    private static final String DISABLE = "disable";
    
    private static final String COMPARE_BY_UID_ONLY = "compareExistingObjectsByUidOnly";
    
    public SearchApiOpTests(ObjectClass oclass) {
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
        requiredOps.add(SearchApiOp.class);
        requiredOps.add(GetApiOp.class);
        return requiredOps;
    }
    
    /**
     * {@inheritDoc}      
     */
    @Override
    public void testRun() {
        Uid uid = null;
        List<Uid> uids = new ArrayList<Uid>();
        List<Set<Attribute>> attrs = new ArrayList<Set<Attribute>>();
        ConnectorObject coFound = null;
        final int recordCount = 10;


        try {
            // obtain objects stored in connector resource, before test inserts
            // own test data
            // should throw if object class is not supported and test ends
            Map<Uid, ConnectorObject> coBeforeTest = ConnectorHelper.search2Map(getConnectorFacade(),
                    getObjectClass(), null, getOperationOptionsByOp(SearchApiOp.class));
            
            //prepare the data
            for (int i = 0; i < recordCount; i++) {
                //create objects
                Set<Attribute> attr = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(), getTestName(), i, true, false);

                Uid luid = getConnectorFacade().create(getSupportedObjectClass(), attr, getOperationOptionsByOp(CreateApiOp.class));
                assertNotNull("Create returned null uid.", luid);
                attrs.add(attr);
                uids.add(luid);
            }
            
            // retrieve all objects including newly created
            List<ConnectorObject> coAll = ConnectorHelper.search(getConnectorFacade(),
                    getObjectClass(), null, getOperationOptionsByOp(SearchApiOp.class));

            //search by id 
            uid = uids.get(0);
            Filter fltUid = FilterBuilder.equalTo(uid);
            List<ConnectorObject> coObjects = ConnectorHelper.search(getConnectorFacade(),
                    getObjectClass(), fltUid, getOperationOptionsByOp(SearchApiOp.class));
            assertTrue("Search filter by uid failed, expected to return one object, but returned "
                    + coObjects.size(), coObjects.size() == 1);
            coFound = coObjects.get(0);

            final Set<Attribute> searchBy = attrs.get(0);
            ConnectorHelper.checkObject(getObjectClassInfo(), coFound, searchBy);

            //get name
            Attribute attName = coFound.getAttributeByName(Name.NAME);
            assertTrue("Special attribute NAME is expected to have exactly one value.", attName
                    .getValue().size() == 1);
            String attNameValue = attName.getValue().get(0).toString();

            //search by name
            coFound = ConnectorHelper.findObjectByName(getConnectorFacade(), getObjectClass(),
                    attNameValue, getOperationOptionsByOp(SearchApiOp.class));

            ConnectorHelper.checkObject(getObjectClassInfo(), coFound, searchBy);
            
            //search by all non special readable attributes
            Filter fltAllAtts = null;
            // attributes which are used in filter must be the same for all filtered objects
            Set<Attribute> filteredAttrs = new HashSet<Attribute>();
            
            for (Attribute attribute : searchBy) {
                if (!AttributeUtil.isSpecial(attribute) && ConnectorHelper.isReadable(getObjectClassInfo(), attribute)) {                    
                    if (fltAllAtts == null) {
                        fltAllAtts = FilterBuilder.equalTo(attribute);
                    } else {
                        fltAllAtts = FilterBuilder.and(fltAllAtts, FilterBuilder.equalTo(attribute));
                    }
                    filteredAttrs.add(attribute);
                }
            }            
            // skip test when there are no non-special readable attributes 
            // (results in null filter - tested explicitly)
            if (fltAllAtts != null) {
                // find how many object should pass filter
                int count = 0;
                for (ConnectorObject co : coAll) {
                    if (fltAllAtts.accept(co)) {
                        count++;
                    }
                }
                coObjects = ConnectorHelper.search(getConnectorFacade(),
                        getObjectClass(), fltAllAtts,
                        getOperationOptionsByOp(SearchApiOp.class));

                assertEquals("Search by all non-special attributes returned "
                        + coObjects.size() + " objects, but expected was "
                        + count + " .", count, coObjects.size());
                 
                for (ConnectorObject coChecked : coObjects) {
                    ConnectorHelper.checkObject(getObjectClassInfo(), coChecked,
                        filteredAttrs);
                }
            }
            
            //check null filter
            coObjects = ConnectorHelper.search(getConnectorFacade(), getObjectClass(), null, getOperationOptionsByOp(SearchApiOp.class));
            assertTrue("Null-filter search failed, wrong number of objects returned, expected: "
                            + (uids.size() + coBeforeTest.size()) + " but found: "
                            + coObjects.size(), 
                            coObjects.size() == uids.size() + coBeforeTest.size());
            
            List<Uid> tempUids = new ArrayList<Uid>(uids);
            
            for (ConnectorObject cObject : coObjects) {
                // check if the uid is in list of objects created by test
                int idx = uids.indexOf(cObject.getUid());
                if (idx > -1) {
                    // is in list
                    // remove it from temp list
                    assertTrue(tempUids.remove(cObject.getUid()));
                    // compare the attributes
                    ConnectorHelper.checkObject(getObjectClassInfo(), cObject, attrs.get(idx));
                } else {
                    if (compareExistingObjectsByUidOnly()) {
                        assertTrue("Object returned by null-filter search is neither in list of objects created by test nor in list of objects that were in connector resource before test. Objects were compared by Uid only.",
                                coBeforeTest.containsKey(cObject.getUid()));
                    }
                    else {
                        assertTrue("Object returned by null-filter search is neither in list of objects created by test nor in list of objects that were in connector resource before test. Objects were compared by all attributes.",
                                coBeforeTest.containsValue(cObject));
                    }                    
                }
            }
            assertTrue("Null-filter search didn't return all created objects by search test.",
                    tempUids.size() == 0);                        
            
   
        } finally {
            // remove objects created by test
            for (Uid deluid : uids) {
                try {
                    ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(),
                            deluid, false, getOperationOptionsByOp(DeleteApiOp.class));
                } catch (Exception e) {
                    // ok
                    // note: this is thrown, when we delete the same object twice.
                }
            }
        }
        
    }
    
    /**
     * Test Search without specified OperationOptions attrsToGet which is the default for all other tests.
     * All the other tests contain explicit attrsToGet.
     */
    @Test
    public void testSearchWithoutAttrsToGet() {
        // run the contract test only if search is supported by tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(),
                getAPIOperations())) {
            Uid uid = null;

            try {
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(), getTestName(), 0, true, false);

                uid = getConnectorFacade().create(getSupportedObjectClass(), attrs, null);
                assertNotNull("Create returned null uid.", uid);

                // get the user to make sure it exists now
                Filter fltUid = FilterBuilder.equalTo(uid);
                List<ConnectorObject> coObjects = ConnectorHelper.search(getConnectorFacade(),
                        getSupportedObjectClass(), fltUid, null);
                assertTrue(
                        "Search filter by uid with no OperationOptions failed, expected to return one object, but returned "
                                + coObjects.size(), coObjects.size() == 1);

                assertNotNull("Unable to retrieve newly created object", coObjects.get(0));

                // compare requested attributes to retrieved attributes, but
                // don't compare attrs which
                // are not returned by default
                ConnectorHelper.checkObject(getObjectClassInfo(), coObjects.get(0), attrs, false);
            } finally {
                if (uid != null) {
                    // delete the object
                    getConnectorFacade().delete(getSupportedObjectClass(), uid, null);
                }
            }
        }
        else {
            LOG.info("----------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''testSearchWithoutAttrsToGet'' for object class ''{0}''.", getObjectClass());
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
    
    /* ***************** CASE INSENSITIVE SEARCH ********************* */
    /**
     * Test case <strong>insensitive</strong> search for UID attribute.
     * 
     * There is twice Search performed, once with changed case. The results should be identical.
     */
    @Test
    public void testCaseInsensitiveSearch() {
        // run the contract test only if search is supported by tested object
        // class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(),
                getObjectClass(), getAPIOperations())
                && canSearchCaseInsensitive()) {
            Uid uid = null;

            try {
                // create a new dummy object
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(
                        getDataProvider(), getObjectClassInfo(), getTestName(),
                        0, true, false);

                uid = getConnectorFacade().create(getSupportedObjectClass(),
                        attrs, null);
                assertNotNull("Create returned null uid.", uid);

                // 1st search, contains the original object
                ConnectorObject searchResult = searchForUid(uid.getUidValue(), "[query by original uid]");
                
                // get created uid and change its case, than perform 2nd search for uid with changed case
                String uidStr = uid.getUidValue();

                
                // change the case in UID
                String caseChngd_uidStr = changeCase(uidStr); // inverts the case of the original uid (example: BvZAO96 --> bVzao96)
                
                // change the case in NAME
                String name = getName(uid);
                String caseChngd_NAME = changeCase(name);
                
                
                //perform search with changed case UID
                ConnectorObject searchWithChngdCaseResult = searchForUid(
                        caseChngd_uidStr, "[query by changed case uid]");

                assertTrue(
                        "The search responses differ for changed case query [UID] and simple query.",
                        searchWithChngdCaseResult.equals(searchResult));
                
                //perform search with changed case NAME
                searchWithChngdCaseResult = searchForName(
                        caseChngd_NAME, "[query by changed case name]");

                assertTrue(
                        "The search responses differ for changed case query [NAME] and simple query.",
                        searchWithChngdCaseResult.equals(searchResult));
            } finally {
                if (uid != null) {
                    // delete the dummy object
                    getConnectorFacade().delete(getSupportedObjectClass(), uid,
                            null);
                }
            }
        } else {
            LOG
                    .info("----------------------------------------------------------------------------------------");
            LOG
                    .info(
                            "Skipping test ''testCaseInsensitiveSearch'' for object class ''{0}''.",
                            getObjectClass());
            LOG
                    .info("----------------------------------------------------------------------------------------");
        }
    }

    private ConnectorObject searchForName(String caseChngd_NAME, String msg) {
        // get the user to make sure it exists now
        Filter fltUid = FilterBuilder.equalTo(AttributeBuilder.build(Name.NAME, caseChngd_NAME));
        List<ConnectorObject> coObjects = ConnectorHelper.search(
                getConnectorFacade(), getSupportedObjectClass(), fltUid, null);
        assertTrue(
                msg
                        + " Search filter by uid with no OperationOptions failed, expected to return one object, but returned "
                        + coObjects.size(), coObjects.size() == 1);

        assertNotNull("Unable to retrieve newly created object", coObjects
                .get(0));
        return coObjects.get(0);
    }

    /** get the name for uid */
    private String getName(Uid uid) {
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        oob.setAttributesToGet(Name.NAME);
        ConnectorObject o = getConnectorFacade().getObject(getObjectClass(), uid, oob.build()); // get the name
        Attribute attrName = o.getAttributeByName(Name.NAME);
        return attrName.getValue().get(0).toString();
    }

    /** search for given Uid and return first item in the response list */
    private ConnectorObject searchForUid(String uidValue, String msg) {
        // get the user to make sure it exists now
        Filter fltUid = FilterBuilder.equalTo(AttributeBuilder.build(Uid.NAME, uidValue));
        List<ConnectorObject> coObjects = ConnectorHelper.search(
                getConnectorFacade(), getSupportedObjectClass(), fltUid, null);
        assertTrue(
                msg
                        + " Search filter by uid with no OperationOptions failed, expected to return one object, but returned "
                        + coObjects.size(), coObjects.size() == 1);

        assertNotNull("Unable to retrieve newly created object", coObjects
                .get(0));
        return coObjects.get(0);
    }

    /** replace upper and lowercase letters */
    static String changeCase(String str_uid) {
        char[] result = new char[str_uid.length()];
        for (int i = 0; i < str_uid.length(); i++) {
            if (Character.isLowerCase(str_uid.charAt(i))) {
                result[i] = Character.toUpperCase(str_uid.charAt(i));
            } else {
                result[i] = Character.toLowerCase(str_uid.charAt(i));
            }
        }
        
        return new String(result);
    }
    
    /**
     * <p>
     * Returns true if tests are configured to enable case insensitive tests
     * {@link SearchApiOpTests#testCaseInsensitiveSearch()}.
     * </p>
     * 
     * <p>
     * Returns true if tests are configured to test connector's sync after
     * specified operation. Some connectors implement sync but are not capable
     * to sync all changes' types.
     * </p>
     */
    protected static boolean canSearchCaseInsensitive() {
        // by default it's supposed that case insensitive search is disabled.
        Boolean canSearchCIns = true;
        try {
            canSearchCIns = !(Boolean) getDataProvider().getTestSuiteAttribute(
                    DISABLE + "." + CASE_INSENSITIVE_PREFIX,
                    TEST_NAME);

        } catch (ObjectNotFoundException ex) {
            // exceptions is throw in case property definition is not found
            // ok -- indicates enabling the property
        }

        return canSearchCIns;
    }

    /**
     * Returns true if tests should compare already existing objects by uid only. 
     */
    protected static boolean compareExistingObjectsByUidOnly() {
        // by default it's supposed that all attributes all compared
        boolean compareByUidOnly = false;
        try {
            compareByUidOnly = (Boolean) getDataProvider().getTestSuiteAttribute(
                    COMPARE_BY_UID_ONLY, TEST_NAME);

        } catch (ObjectNotFoundException ex) {
            // exceptions is throw in case property definition is not found
            // ok -- indicates enabling the property
        }

        return compareByUidOnly;
    }


}
