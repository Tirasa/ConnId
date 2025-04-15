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

import java.util.HashSet;
import java.util.Set;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * Contract test of {@link GetApiOp}.
 */
public class GetApiOpTests extends ObjectClassRunner {

    public static final String TEST_NAME = "Get";

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        // list of required operations by this test:
        Set<Class<? extends APIOperation>> s = new HashSet<>();
        s.add(GetApiOp.class);
        s.add(CreateApiOp.class);
        s.add(SearchApiOp.class);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void testRun(ObjectClass objectClass) {
        ConnectorObject obj = null;
        Uid uid = null;

        try {
            Set<Attribute> requestedAttributes = ConnectorHelper.getCreateableAttributes(
                    getDataProvider(), getObjectClassInfo(objectClass), getTestName(), 0, true, false);

            // object class is always supported
            uid = getConnectorFacade().create(objectClass,
                    requestedAttributes, getOperationOptionsByOp(objectClass, CreateApiOp.class));
            assertNotNull(uid,
                    "Unable to perform get test because object to be get cannot be created");

            // retrieve by uid
            obj = getConnectorFacade().getObject(objectClass, uid, getOperationOptionsByOp(objectClass, GetApiOp.class));
            assertNotNull(obj, "Unable to get object by uid");

            ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, requestedAttributes);

            // retrieve by name
            Name name = obj.getName();
            obj = ConnectorHelper.findObjectByName(getConnectorFacade(), objectClass,
                    name.getNameValue(), getOperationOptionsByOp(objectClass, SearchApiOp.class));
            assertNotNull(obj, "Unable to get object by name");

            ConnectorHelper.checkObject(getObjectClassInfo(objectClass), obj, requestedAttributes);

            // get by other attributes???
        } finally {
            // finally ... get rid of the object
            ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid, false,
                    getOperationOptionsByOp(objectClass, DeleteApiOp.class));
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
