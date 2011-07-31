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
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * Contract test of {@link UpdateApiOp} 
 */
@RunWith(Parameterized.class)
public class UpdateApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(UpdateApiOpTests.class);
    
    protected static final String MODIFIED = "modified";
    private static final String ADDED = "added";
    private static final String TEST_NAME = "Update";

    private static final String NON_EXISTING_PROP_NAME = "unsupportedAttributeName";

    public UpdateApiOpTests(ObjectClass objectClass) {
        super(objectClass);
    }
    
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
     */
    @Override
    public void testRun() {
        ConnectorObject obj = null;
        Uid uid = null;


        try {
            // create an object to update
            uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                    getObjectClassInfo(), getTestName(), 0, getOperationOptionsByOp(CreateApiOp.class));
            assertNotNull("Create returned null Uid.", uid);

            // get by uid
            obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid, getOperationOptionsByOp(GetApiOp.class));
            assertNotNull("Cannot retrieve created object.", obj);

            Set<Attribute> replaceAttributes = ConnectorHelper.getUpdateableAttributes(
                    getDataProvider(), getObjectClassInfo(), getTestName(), MODIFIED, 0, false,
                    false);

            if (replaceAttributes.size() > 0 || !isObjectClassSupported()) {
                // update only in case there is something to update or when object class is not supported
                replaceAttributes.add(uid);

                assertTrue("no update attributes were found", (replaceAttributes.size() > 0));
                Uid newUid = getConnectorFacade().update(
                        getObjectClass(), uid, AttributeUtil.filterUid(replaceAttributes), getOperationOptionsByOp(UpdateApiOp.class));

                // Update change of Uid must be propagated to replaceAttributes
                // set
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    replaceAttributes.add(newUid);
                    uid = newUid;
                }
            }

            // verify the change
            obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid,
                    getOperationOptionsByOp(GetApiOp.class));
            assertNotNull("Cannot retrieve updated object.", obj);
            ConnectorHelper.checkObject(getObjectClassInfo(), obj, replaceAttributes);

            // ADD and DELETE update test:
            // set of *multivalue* attributes with generated values
            Set<Attribute> addDelAttrs = ConnectorHelper.getUpdateableAttributes(getDataProvider(),
                    getObjectClassInfo(), getTestName(), ADDED, 0, false, true);
            if (addDelAttrs.size() > 0) {
                // uid must be present for update
                addDelAttrs.add(uid);
                Uid newUid = getConnectorFacade().addAttributeValues(getObjectClass(),
                        uid,
                        AttributeUtil.filterUid(addDelAttrs), getOperationOptionsByOp(UpdateApiOp.class));

                // Update change of Uid
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    addDelAttrs.remove(uid);
                    replaceAttributes.add(newUid);
                    addDelAttrs.add(newUid);
                    uid = newUid;
                }

                // verify the change after ADD
                obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid,
                        getOperationOptionsByOp(GetApiOp.class));
                assertNotNull("Cannot retrieve updated object.", obj);
                // don't want to have two same values for UID attribute
                addDelAttrs.remove(uid);
                ConnectorHelper.checkObject(getObjectClassInfo(), obj,
                        mergeAttributeSets(addDelAttrs, replaceAttributes));
                addDelAttrs.add(uid);

                // delete added attribute values
                newUid = getConnectorFacade().removeAttributeValues(getObjectClass(),
                        uid,
                        AttributeUtil.filterUid(addDelAttrs), getOperationOptionsByOp(UpdateApiOp.class));

                // Update change of Uid must be propagated to replaceAttributes
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    addDelAttrs.remove(uid);
                    replaceAttributes.add(newUid);
                    addDelAttrs.add(newUid);
                    uid = newUid;
                }

                // verify the change after DELETE
                obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid,
                        getOperationOptionsByOp(GetApiOp.class));
                assertNotNull("Cannot retrieve updated object.", obj);
                ConnectorHelper.checkObject(getObjectClassInfo(), obj, replaceAttributes);
            }                        
        } finally {
            if (uid != null) {
                // finally ... get rid of the object
                ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid,
                        false, getOperationOptionsByOp(DeleteApiOp.class));
            }
        }
    }   
    
    /**
     * The test verifies that connector doesn't throw NullPointerException or some other unexpected behavior when passed null as 
     * attribute value. Test passes null values only for non-required non-special updateable attributes.
     */
    @Test
    public void testUpdateToNull() {
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(),
                getAPIOperations()) ) {
            ConnectorObject obj = null;
            Uid uid = null;

            try {
                // create an object to update
                uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                        getObjectClassInfo(), getTestName(), 2, getOperationOptionsByOp(CreateApiOp.class));
                assertNotNull("Create returned null Uid.", uid);
                
                Collection<String> skippedAttributesForUpdateToNullValue = getSkippedAttributesForUpdateToNullValue();
                for (AttributeInfo attInfo : getObjectClassInfo().getAttributeInfo()) {
                    if (attInfo.isUpdateable() && !attInfo.isRequired() && !AttributeUtil.isSpecial(attInfo) && !attInfo.getType().isPrimitive()) {
                        if(skippedAttributesForUpdateToNullValue.contains(attInfo.getName())){
                            LOG.info("Attribute '{0}' was skipped in testUpdateToNull", attInfo.getName());
                            continue;
                        }
                        Set<Attribute> nullAttributes = new HashSet<Attribute>();
                        Attribute attr = AttributeBuilder.build(attInfo.getName());
                        nullAttributes.add(attr);
                        
                        try {
                            Uid newUid = getConnectorFacade().update(
                                getObjectClass(), uid, nullAttributes,
                                getOperationOptionsByOp(UpdateApiOp.class));
                            
                            LOG.info("No exception was thrown, attributes should be either removed or their values set to null.");
                            
                            // update uid
                            if (!uid.equals(newUid)) {
                                uid = newUid;
                            }
                            
                            // verify the change
                            obj = getConnectorFacade().getObject(getObjectClass(), uid,
                                    getOperationOptionsByOp(GetApiOp.class));
                            assertNotNull("Cannot retrieve updated object.", obj);
                            
                            // check that nulled attributes were removed or set to null             
                            if (ConnectorHelper.isReadable(getObjectClassInfo(), attr)) {
                                // null in case attribute is not present
                                Attribute checkedAttribute = obj.getAttributeByName(attInfo.getName());
                                final String MSG = "Attribute '%s' was neither removed nor its value set to null or empty list. Updated value is : '%s'";
                                assertTrue(String.format(MSG, attInfo.getName(), checkedAttribute != null ? checkedAttribute.getValue() : null), checkedAttribute == null || checkedAttribute.equals(attr)
                                                                                    || checkedAttribute.getValue().isEmpty());
                            }
                        }
                        catch (RuntimeException ex) {
                            // ok, this option is possible in case connector cannot neither remove the attribute entirely
                            // nor set its value to null
                            // every RuntimeException except for NPE is possible
                            assertFalse(String.format("Update of attribute '%s' to null thrown NullPointerException.",
                                    attInfo.getName()), ex instanceof NullPointerException);
                            LOG.info(String.format("RuntimeException was thrown when trying to update '%s' to null.",
                                                    attInfo.getName()));
                        }  
                    }
                }               
            } finally {
                if (uid != null) {
                    // finally ... get rid of the object
                    ConnectorHelper.deleteObject(getConnectorFacade(), getObjectClass(), uid,
                            false, getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
        } else {
            LOG.info("----------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''testUpdateToNull'' for object class ''{0}''.",
                    getObjectClass());
            LOG.info("----------------------------------------------------------------------------------------");
        }
    }
    
    /**
     * Tests create of two different objects and then update one to the same
     * attributes as the second. Test that updated object did not update uid to the same value as the first object. 
     */
    @Test
    public void testUpdateToSameAttributes() {
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(), getAPIOperations())) {
            Uid uid1 = null;
            Uid uid2 = null;
            
            try {
                // create two new objects
                Set<Attribute> attrs1 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(), getTestName(), 1, true, false);
                uid1 = getConnectorFacade().create(getSupportedObjectClass(), attrs1, null);
                assertNotNull("Create returned null uid.", uid1);
                
                // get the object to make sure it exist now
                ConnectorObject obj1 = getConnectorFacade().getObject(getSupportedObjectClass(),
                        uid1, getOperationOptionsByOp(GetApiOp.class));
                
                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(), obj1, attrs1);
                
                Set<Attribute> attrs2 = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(), getTestName(), 2, true, false);
                uid2 = getConnectorFacade().create(getSupportedObjectClass(), attrs2, null);
                assertNotNull("Create returned null uid.", uid2);
                
                // get the object to make sure it exist now
                ConnectorObject obj2 = getConnectorFacade().getObject(getSupportedObjectClass(),
                        uid2, getOperationOptionsByOp(GetApiOp.class));
                
                // compare requested attributes to retrieved attributes
                ConnectorHelper.checkObject(getObjectClassInfo(), obj2, attrs2);
                                
                // update second object with updateable attributes of first object
                Set<Attribute> replaceAttributes = new HashSet<Attribute>();
                for (Attribute attr : attrs1) {
                    if (ConnectorHelper.isUpdateable(getObjectClassInfo(), attr)) {
                        replaceAttributes.add(attr);
                    }
                }
                replaceAttributes.add(uid2);
                
                try {
                    Uid newUid = getConnectorFacade().update(
                        getSupportedObjectClass(), uid2, AttributeUtil.filterUid(replaceAttributes), getOperationOptionsByOp(UpdateApiOp.class));
                
                    if (!uid2.equals(newUid)) {
                        uid2 = newUid;
                    }

                
                    assertFalse("Update returned the same uid when tried to update to the same " +
                		"attributes as another object.", uid1.equals(uid2));
                }
                catch (RuntimeException ex) {
                    // ok - update could throw this in case __NAME__ and __UID__ are the same attributes
                }
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
            LOG.info("Skipping test ''testUpdateToSameAttributes'' for object class ''{0}''.", getObjectClass());
            LOG.info("----------------------------------------------------------------------------------------");
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
    @Test
    public void testUpdateFailUnsupportedAttribute() {
        // run the contract test only if update is supported by tested object class
        if (ConnectorHelper.operationsSupported(getConnectorFacade(),
                getObjectClass(), getAPIOperations())) {

            Uid uid = null;
            try {
                // create an object to update
                uid = ConnectorHelper.createObject(getConnectorFacade(),
                        getDataProvider(), getObjectClassInfo(), getTestName(),
                        0, getOperationOptionsByOp(CreateApiOp.class));
                assertNotNull("Create returned null Uid.", uid);

                // get by uid
                ConnectorObject obj = getConnectorFacade().getObject(
                        getSupportedObjectClass(), uid,
                        getOperationOptionsByOp(GetApiOp.class));
                assertNotNull("Cannot retrieve created object.", obj);

                Set<Attribute> replaceAttributes = ConnectorHelper
                        .getUpdateableAttributes(getDataProvider(),
                                getObjectClassInfo(), getTestName(), MODIFIED,
                                0, false, false);

                if (replaceAttributes.size() > 0 || !isObjectClassSupported()) {
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

                    assertTrue("no update attributes were found",
                            (replaceAttributes.size() > 0));

                    Uid uidNew = null;
                    try {
                        uidNew = getConnectorFacade().update(getObjectClass(),
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
                                    getSupportedObjectClass(), uidNew, false,
                                    getOperationOptionsByOp(DeleteApiOp.class));
                        }
                    }
                }
            } finally {
                if (uid != null) {
                    // delete the created the object
                    ConnectorHelper.deleteObject(getConnectorFacade(),
                            getSupportedObjectClass(), uid, false,
                            getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
        } else {
            LOG
                    .info("----------------------------------------------------------------------------------------");
            LOG
                    .info(
                            "Skipping test ''testCreateFailUnsupportedAttribute'' for object class ''{0}''.",
                            getObjectClass());
            LOG
                    .info("----------------------------------------------------------------------------------------");
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
