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

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * Contract test of {@link GetApiOp} 
 */
@RunWith(Parameterized.class)
public class GetApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    @SuppressWarnings("unused")
    private static final Log LOG = Log.getLog(GetApiOpTests.class);
    private static final String TEST_NAME = "Get";

    public GetApiOpTests(ObjectClass oclass) {
        super(oclass);
    }

    
    /**
     * {@inheritDoc}     
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        // list of required operations by this test:
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        s.add(GetApiOp.class);
        s.add(CreateApiOp.class);
        s.add(SearchApiOp.class);
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
            Set<Attribute> requestedAttributes = ConnectorHelper.getCreateableAttributes(
                    getDataProvider(), getObjectClassInfo(), getTestName(), 0, true, false);

            // object class is always supported
            uid = getConnectorFacade().create(getSupportedObjectClass(),
                    requestedAttributes, getOperationOptionsByOp(CreateApiOp.class));
            assertNotNull(
                    "Unable to perform get test because object to be get cannot be created",
                    uid);

            // retrieve by uid
            obj = getConnectorFacade().getObject(getObjectClass(), uid, getOperationOptionsByOp(GetApiOp.class));
            assertNotNull("Unable to get object by uid", obj);

            ConnectorHelper.checkObject(getObjectClassInfo(), obj, requestedAttributes);

            // retrieve by name
            Name name = obj.getName();
            obj = ConnectorHelper.findObjectByName(getConnectorFacade(), getObjectClass(),
                    name.getNameValue(), getOperationOptionsByOp(SearchApiOp.class));
            assertNotNull("Unable to get object by name", obj);

            ConnectorHelper.checkObject(getObjectClassInfo(), obj, requestedAttributes);

            // get by other attributes???

        } finally {
            // finally ... get rid of the object
            ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid, false,
                    getOperationOptionsByOp(DeleteApiOp.class));
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
