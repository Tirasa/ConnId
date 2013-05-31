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
import static org.testng.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;


/**
 * Contract test of {@link UpdateApiOp}
 */
@Test(testName =  UpdateApiOpTests.TEST_NAME)
public class UpdateApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    private static final Logger logger = Logger.getLogger(ValidateApiOpTests.class);

    protected static final String MODIFIED = "modified";
    private static final String ADDED = "added";
    public static final String TEST_NAME = "Update";

    private static final String NON_EXISTING_PROP_NAME = "unsupportedAttributeName";

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        s.add(UpdateApiOp.class);
        s.add(CreateApiOp.class);
        s.add(GetApiOp.class);
        return s;
    }

    /**
     * {@inheritDoc}
     *
     * This tests an proper updating of attributes. All new attributes' values in given set for update are valid.
     *
     * Test procedure:
     * 1) Create object.
     * 2) Get this created object + fill it with values
     * 3) Get a set of new values for updateable attributes - replacing all attributes' values  (no add or delete)
     * 4) Update uid and replaces values of attributes
     * 5) Verify if the object has new values of attributes
     * 6) ---- JUMPS TO FINALLY SECTION ---- see in TODOs
     */
    @Override
    protected void testRun(ObjectClass objectClass) {
        ConnectorObject obj = null;
        Uid uid = null;


        try {
            // create an object to update
            uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), 0, getOperationOptionsByOp(objectClass, CreateApiOp.class));
            assertNotNull(uid,"Create returned null Uid.");

            // get by uid
            obj = getConnectorFacade().getObject(objectClass, uid, getOperationOptionsByOp(objectClass, GetApiOp.class));
            assertNotNull(obj,"Cannot retrieve created object.");

            Set<Attribute> replaceAttributes = ConnectorHelper.getUpdateableAttributes(
                    getDataProvider(), getObjectClassInfo(objectClass), getTestName(), MODIFIED, 0, false,
                    false);

            if (replaceAttributes.size() > 0 || !isObjectClassSupported(objectClass)) {
                /* TODO when object class is not supported?!
                 */
                // update only in case there is something to update or when object class is not supported
                replaceAttributes.add(uid);

                assertTrue((replaceAttributes.size() > 0),"no update attributes were found");
                Uid newUid = getConnectorFacade().update(
                        objectClass, uid, AttributeUtil.filterUid(replaceAttributes), getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                // Update change of Uid must be propagated to replaceAttributes
                // set
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    replaceAttributes.add(newUid);
                    uid = newUid;
                }
            }

            // verify the change
            obj = getConnectorFacade().getObject(objectClass, uid,
                    getOperationOptionsByOp(objectClass, GetApiOp.class));
            assertNotNull(obj,"Cannot retrieve updated object.");
            ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, replaceAttributes);
            // TODO Here it jumps to finally section which is wrong...
            // ADD and DELETE update test:
            // set of *multivalue* attributes with generated values
            Set<Attribute> addDelAttrs = ConnectorHelper.getUpdateableAttributes(getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), ADDED, 0, false, true);
            if (addDelAttrs.size() > 0) {
                // uid must be present for update
                addDelAttrs.add(uid);
                Uid newUid = getConnectorFacade().addAttributeValues(objectClass,
                        uid,
                        AttributeUtil.filterUid(addDelAttrs), getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                // Update change of Uid
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    addDelAttrs.remove(uid);
                    replaceAttributes.add(newUid);
                    addDelAttrs.add(newUid);
                    uid = newUid;
                }

                // verify the change after ADD
                obj = getConnectorFacade().getObject(objectClass, uid,
                        getOperationOptionsByOp(objectClass, GetApiOp.class));
                assertNotNull(obj,"Cannot retrieve updated object.");
                // don't want to have two same values for UID attribute
                addDelAttrs.remove(uid);
                ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj,
                        mergeAttributeSets(addDelAttrs, replaceAttributes));
                addDelAttrs.add(uid);

                // delete added attribute values
                newUid = getConnectorFacade().removeAttributeValues(objectClass,
                        uid,
                        AttributeUtil.filterUid(addDelAttrs), getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                // Update change of Uid must be propagated to replaceAttributes
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    addDelAttrs.remove(uid);
                    replaceAttributes.add(newUid);
                    addDelAttrs.add(newUid);
                    uid = newUid;
                }

                // verify the change after DELETE
                obj = getConnectorFacade().getObject(objectClass, uid,
                        getOperationOptionsByOp(objectClass, GetApiOp.class));
                assertNotNull(obj,"Cannot retrieve updated object.");
                ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, replaceAttributes);
            }
        } finally {
            if (uid != null) {
                // finally ... get rid of the object
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                        false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        }
    }

    /**
     * The test verifies that connector doesn't throw NullPointerException or some other unexpected behavior when passed null as
     * attribute value. Test passes null values only for non-required non-special updateable attributes.
     *
     * Test procedure:
     * 1) Test if the required API operaions are supported by given objectClass.
     * 2) Set all attributes' values to null
     *    If you want to skip some attributes from being set to null list them in Update.updateToNullValue.skippedAttributes property
     *    in groovy.config
     * 3) Try to update the object with property's value = null -> IF no exception, property has been removed or set to null
     * 4) Check if the property has been really removed or set to null
     * 5) TODO something went wrong because the value has not been set to null value or removed. The value of nulled property pesisted.
     *
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testUpdateToNull(ObjectClass objectClass) {
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass,
                getAPIOperations()) ) {
            ConnectorObject obj = null;
            Uid uid = null;

            try {
                // create an object to update
                uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 2, getOperationOptionsByOp(objectClass, CreateApiOp.class));
                assertNotNull(uid,"Create returned null Uid.");

                ConnectorObject originalObject = getConnectorFacade().getObject(objectClass, uid,
                        getOperationOptionsByOp(objectClass, GetApiOp.class));

                Collection<String> skippedAttributesForUpdateToNullValue = getSkippedAttributesForUpdateToNullValue();
                for (AttributeInfo attInfo : getObjectClassInfo(objectClass).getAttributeInfo()) {
                    if (attInfo.isUpdateable() && !attInfo.isRequired() && !AttributeUtil.isSpecial(attInfo) && !attInfo.getType().isPrimitive()) {
                        if(skippedAttributesForUpdateToNullValue.contains(attInfo.getName())){
                            logger.info("Attribute '"+attInfo.getName()+"' was skipped in testUpdateToNull");
                            continue;
                        }
                        Set<Attribute> nullAttributes = new HashSet<Attribute>();
                        Attribute attr = AttributeBuilder.build(attInfo.getName());
                        nullAttributes.add(attr);

                        try {
                            Uid newUid = getConnectorFacade().update(
                                objectClass, uid, nullAttributes,
                                getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                            logger.info(
                                    "No exception was thrown, attributes should be either removed or their values set to null.");

                            // update uid
                            if (!uid.equals(newUid)) {
                                uid = newUid;
                            }

                            // verify the change
                            obj = getConnectorFacade().getObject(objectClass, uid,
                                    getOperationOptionsByOp(objectClass, GetApiOp.class));
                            assertNotNull(obj,"Cannot retrieve updated object.");

                            // check that nulled attributes were removed or set to null
                            if (ConnectorHelper.isReadable(getObjectClassInfo(objectClass), attr)) {
                                // null in case attribute is not present
                                Attribute checkedAttribute = obj.getAttributeByName(attInfo.getName());
                                final String MSG = "Attribute '%s' was neither removed nor its value set to null or empty list. Updated value is : '%s'";
                                assertTrue(checkedAttribute == null || checkedAttribute.equals(attr) || checkedAttribute.getValue().isEmpty(),
                                        String.format(MSG, attInfo.getName(), checkedAttribute != null ? checkedAttribute.getValue() : null));
                            }
                        }
                        catch (RuntimeException ex) {
                            // ok, this option is possible in case connector cannot neither remove the attribute entirely
                            // nor set its value to null
                            // every RuntimeException except for NPE is possible
                            assertFalse(ex instanceof NullPointerException,String.format("Update of attribute '%s' to null thrown NullPointerException.",
                                    attInfo.getName()));
                            logger.info(String.format("RuntimeException was thrown when trying to update '%s' to null.",
                                    attInfo.getName()));
                        }
                    }
                }
            } finally {
                if (uid != null) {
                    // finally ... get rid of the object
                    ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                            false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
            }
        } else {
            logger.info(LOG_SEPARATOR);
            logger.info("Skipping test ''testUpdateToNull'' for object class ''"+objectClass+"''.");
            logger.info(LOG_SEPARATOR);
        }
    }

    /**
     * Tests create of two different objects and then update one to the same
     * attributes as the second. Test that updated object did not update uid to the same value as the first object.
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testUpdateToSameAttributes(ObjectClass objectClass) {
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())) {
            Uid uid1 = null;
            Uid uid2 = null;

            try {
                // create two new objects
                Set<Attribute> attrs1 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 1, true, false);
                uid1 = getConnectorFacade().create(objectClass, attrs1, null);
                assertNotNull(uid1,"Create returned null uid.");

                // get the object to make sure it exist now
                ConnectorObject obj1 = getConnectorFacade().getObject(objectClass,
                        uid1, getOperationOptionsByOp(objectClass, GetApiOp.class));

                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj1, attrs1);

                Set<Attribute> attrs2 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 2, true, false);
                uid2 = getConnectorFacade().create(objectClass, attrs2, null);
                assertNotNull(uid2,"Create returned null uid.");

                // get the object to make sure it exist now
                ConnectorObject obj2 = getConnectorFacade().getObject(objectClass,
                        uid2, getOperationOptionsByOp(objectClass, GetApiOp.class));

                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj2, attrs2);

                // update second object with updateable attributes of first object
                Set<Attribute> replaceAttributes = new HashSet<Attribute>();
                for (Attribute attr : attrs1) {
                    if (ConnectorHelper.isUpdateable(getObjectClassInfo(objectClass), attr)) {
                        replaceAttributes.add(attr);
                    }
                }
                replaceAttributes.add(uid2);

                try {
                    Uid newUid = getConnectorFacade().update(
                        objectClass, uid2, AttributeUtil.filterUid(replaceAttributes), getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                    if (!uid2.equals(newUid)) {
                        uid2 = newUid;
                    }


                    assertFalse(uid1.equals(uid2),"Update returned the same uid when tried to update to the same " +
                		"attributes as another object.");
                }
                catch (RuntimeException ex) {
                    // ok - update could throw this in case __NAME__ and __UID__ are the same attributes
                }
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
            logger.info(LOG_SEPARATOR);
            logger.info("Skipping test ''testUpdateToSameAttributes'' for object class ''"+objectClass+"''.");
            logger.info(LOG_SEPARATOR);
        }
    }

    @Override
    public String getTestName() {
        return TEST_NAME;
    }

    /**
     * Tests update method with invalid Attribute, RuntimeException is expected
     *
     * connector developers can set the value of unsupported attribute
     * using test property: <code>testsuite.Create.unsupportedAttributeName</code>
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testUpdateFailUnsupportedAttribute(ObjectClass objectClass) {
        // run the contract test only if update is supported by tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(),
                objectClass, getAPIOperations())) {

            Uid uid = null;
            try {
                // create an object to update
                uid = ConnectorHelper.createObject(getConnectorFacade(),
                        getDataProvider(), getObjectClassInfo(objectClass), getTestName(),
                        0, getOperationOptionsByOp(objectClass, CreateApiOp.class));
                assertNotNull(uid,"Create returned null Uid.");

                // get by uid
                ConnectorObject obj = getConnectorFacade().getObject(
                        objectClass, uid,
                        getOperationOptionsByOp(objectClass, GetApiOp.class));
                assertNotNull(obj,"Cannot retrieve created object.");

                Set<Attribute> replaceAttributes = ConnectorHelper
                        .getUpdateableAttributes(getDataProvider(),
                                getObjectClassInfo(objectClass), getTestName(), MODIFIED,
                                0, false, false);

                if (replaceAttributes.size() > 0 || !isObjectClassSupported(objectClass)) {
                    // update only in case there is something to update or when
                    // object class is not supported
                    replaceAttributes.add(uid);

                    String unsupportedAttribute = null;
                    try {
                        unsupportedAttribute = (String) getDataProvider()
                                .getTestSuiteAttribute(NON_EXISTING_PROP_NAME,
                                        TEST_NAME);
                    } catch (ObjectNotFoundException ex) {
                        unsupportedAttribute = "nonExistingAndUnlikelyAttrName";
                    }
                    // + add one non-existing attribute
                    replaceAttributes.add(AttributeBuilder
                            .build(unsupportedAttribute));

                    assertTrue((replaceAttributes.size() > 0),"no update attributes were found");

                    Uid uidNew = null;
                    try {
                        uidNew = getConnectorFacade().update(objectClass,
                                uid,
                                AttributeUtil.filterUid(replaceAttributes),
                                null);
                        Assert
                                .fail("'testUpdateFailUnsupportedAttribute': NONEXISTING attribute accepted without throwing a RuntimeException.");
                    } catch (RuntimeException ex) {
                        // ok
                    } finally {
                        if (uidNew != null) {
                            // delete the created the object
                            ConnectorHelper.deleteObject(getConnectorFacade(),
                                    objectClass, uidNew, false,
                                    getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                        }
                    }
                }
            } finally {
                if (uid != null) {
                    // delete the created the object
                    ConnectorHelper.deleteObject(getConnectorFacade(),
                            objectClass, uid, false,
                            getOperationOptionsByOp(objectClass, DeleteApiOp.class));
                }
            }
        } else {
            String msg = "Skipping test ''testUpdateFailUnsupportedAttribute'' for object class ''"+objectClass+"''.";
            logger.info(LOG_SEPARATOR);
            logger.info(msg);
            logger.info(LOG_SEPARATOR);
        }
    }

    /**
     * Returns new attribute set which contains all attributes from both sets. If attribute with the same name is present
     * in both sets then its values are merged.
     */
    protected static Set<Attribute> mergeAttributeSets(Set<Attribute> attrSet1, Set<Attribute> attrSet2) {
        Set<Attribute> attrs = new HashSet<Attribute>();
        Map<String, Attribute> attrMap2 = new HashMap<String, Attribute>();
        for (Attribute attr : attrSet2) {
            attrMap2.put(attr.getName(), attr);
        }

        for (Attribute attr1 : attrSet1) {
            Attribute attr2 = attrMap2.remove(attr1.getName());
            // if attribute is present in both sets then merge its values
            if (attr2 != null) {
                AttributeBuilder attrBuilder = new AttributeBuilder();
                attrBuilder.setName(attr1.getName());
                attrBuilder.addValue(attr1.getValue());
                attrBuilder.addValue(attr2.getValue());
                attrs.add(attrBuilder.build());
            } else {
                attrs.add(attr1);
            }
        }

        // add remaining attributes from second set
        for (Attribute attr2 : attrMap2.values()) {
            attrs.add(attr2);
        }

        return attrs;
    }


    @SuppressWarnings("unchecked")
    protected static Collection<String> getSkippedAttributesForUpdateToNullValue(){
        Object skippedAttributes = null;
        try{
            skippedAttributes = getDataProvider().getTestSuiteAttribute("updateToNullValue.skippedAttributes",TEST_NAME);
        }
        catch(ObjectNotFoundException e){
        }
        if(skippedAttributes == null){
            return Collections.emptyList();
        }
        if(!(skippedAttributes instanceof Collection<?>)){
            throw new RuntimeException(MessageFormat.format(
                    "Testsuite Property '{0}' must be of type Collection , but was of type {1}", "testsuite." + TEST_NAME
                            + "." + "updateToNullValue.skippedAttributes", skippedAttributes.getClass()));
        }
        return (Collection<String>)(skippedAttributes);
    }
}
