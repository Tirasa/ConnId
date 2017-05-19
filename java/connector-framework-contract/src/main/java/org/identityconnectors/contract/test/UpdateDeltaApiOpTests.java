/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Evolveum. All rights reserved.
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
 */
package org.identityconnectors.contract.test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.UpdateDeltaApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.AttributeDeltaBuilder;
import org.identityconnectors.framework.common.objects.AttributeDeltaUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/**
 * Contract test of {@link UpdateDeltaApiOp}.
 */
@Test(testName = UpdateDeltaApiOpTests.TEST_NAME)
public class UpdateDeltaApiOpTests extends ObjectClassRunner {

    private static final Logger LOG = Logger.getLogger(ValidateApiOpTests.class);

    protected static final String MODIFIED = "modified";

    private static final String ADDED = "added";

    public static final String TEST_NAME = "UpdateDelta";

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        s.add(UpdateDeltaApiOp.class);
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
     * 3) Get a set of new values for updateable attributes - replacing all attributes and add new values
     * 4) Update uid and replaces values and add new values of attributes
     * 5) Verify if the object has new values of attributes
     * 6) Remove added values of attributes
     * 7) Verify if the object hasn't new values of attributes
     * 8) ---- JUMPS TO FINALLY SECTION ---- see in TODOs
     */
    @Override
    protected void testRun(ObjectClass objectClass) {
        ConnectorObject obj = null;
        Uid uid = null;

        try {
            // create an object to update
            uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), 0, getOperationOptionsByOp(objectClass,
                    CreateApiOp.class));
            assertNotNull(uid, "Create returned null Uid.");

            // get by uid
            obj = getConnectorFacade().getObject(objectClass, uid, getOperationOptionsByOp(objectClass, GetApiOp.class));
            assertNotNull(obj, "Cannot retrieve created object.");

            Set<AttributeDelta> replaceAttributesDelta = ConnectorHelper.getUpdateableAttributesDelta(
                    getDataProvider(), getObjectClassInfo(objectClass), getTestName(), MODIFIED, 0, false,
                    false, true);

            Set<AttributeDelta> addAttributesDelta = ConnectorHelper.getUpdateableAttributesDelta(getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), ADDED, 0, false, true, true);

            Set<AttributeDelta> removeAttributesDelta = ConnectorHelper.getUpdateableAttributesDelta(getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), ADDED, 0, false, true, false);

            Set<AttributeDelta> replaceAndAddAttrsDelta = new HashSet<AttributeDelta>();

            replaceAndAddAttrsDelta.addAll(replaceAttributesDelta);
            replaceAndAddAttrsDelta.addAll(addAttributesDelta);

            if (replaceAndAddAttrsDelta.size() > 0 || !isObjectClassSupported(objectClass)) {
                /* TODO when object class is not supported?!
                 */
                // update only in case there is something to update or when object class is not supported
                replaceAndAddAttrsDelta.add(AttributeDeltaBuilder.build(uid.getName(), uid.getValue()));

                assertTrue((replaceAndAddAttrsDelta.size() > 0), "no update attributesDelta were found");
                Set<AttributeDelta> sideEffectModificationAttributesDelta = getConnectorFacade().updateDelta(
                        objectClass, uid, AttributeDeltaUtil.filterUid(replaceAndAddAttrsDelta),
                        getOperationOptionsByOp(objectClass, UpdateDeltaApiOp.class));

                // Update change of Uid must be propagated to replaceAttributes
                // set
                AttributeDelta newUidAttrDelta = AttributeDeltaUtil.getUidAttributeDelta(
                        sideEffectModificationAttributesDelta);
                if (newUidAttrDelta != null) {
                    Attribute newUid = AttributeBuilder.build(newUidAttrDelta.getName(), newUidAttrDelta.
                            getValuesToReplace());
                    uid = (Uid) newUid;
                }
            }

            // verify the change
            obj = getConnectorFacade().getObject(objectClass, uid,
                    getOperationOptionsByOp(objectClass, GetApiOp.class));
            assertNotNull(obj, "Cannot retrieve updated object.");
            ConnectorHelper.checkObjectByAttrDelta(getObjectClassInfo(objectClass), obj, replaceAndAddAttrsDelta);
            // TODO Here it jumps to finally section which is wrong...
            // DELETE update test:

            if (removeAttributesDelta.size() > 0) {
                // uid must be present for update
                removeAttributesDelta.add(AttributeDeltaBuilder.build(uid.getName(), uid.getValue()));

                // delete added attribute values
                Set<AttributeDelta> sideEffectModificationAttributesDelta = getConnectorFacade().
                        updateDelta(objectClass,
                                uid,
                                AttributeDeltaUtil.filterUid(removeAttributesDelta),
                                getOperationOptionsByOp(objectClass, UpdateDeltaApiOp.class));

                AttributeDelta newUidAttrDelta = AttributeDeltaUtil.getUidAttributeDelta(
                        sideEffectModificationAttributesDelta);
                if (newUidAttrDelta != null) {
                    Attribute newUid = AttributeBuilder.build(newUidAttrDelta.getName(), newUidAttrDelta.
                            getValuesToReplace());
                    uid = (Uid) newUid;
                }

                // verify the change after DELETE
                obj = getConnectorFacade().getObject(objectClass, uid,
                        getOperationOptionsByOp(objectClass, GetApiOp.class));
                assertNotNull(obj, "Cannot retrieve updated object.");
                ConnectorHelper.checkObjectByAttrDelta(getObjectClassInfo(objectClass), obj, replaceAttributesDelta);
            }
        } finally {
            if (uid != null) {
                // finally ... get rid of the object
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                        false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        }
    }

    @Override
    public String getTestName() {
        return TEST_NAME;
    }

}
