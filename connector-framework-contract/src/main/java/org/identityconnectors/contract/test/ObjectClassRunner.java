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

import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.contract.data.DataProvider;
import org.identityconnectors.contract.exceptions.ContractException;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
import com.google.inject.Inject;

/**
 * Simple base class that will run through all the {@link ObjectClass}s.
 */
public abstract class ObjectClassRunner extends ContractTestBase {

    /**
     * Logging..
     */
    private static final Logger logger = Logger.getLogger(ValidateApiOpTests.class);

    private ObjectClassInfo _objectClassInfo;
    private ObjectClass _supportedObjectClass;
    private boolean _ocSupported = false;

    @Inject
    protected DataProvider dataProvider;

    /**
     * Dispose the test environment, do the cleanup
     */
    @AfterTest
    public void dispose() {
        _objectClassInfo = null;
        super.dispose();
    }

    /**
     * Main contract test entry point, it calls {@link #testRun(ObjectClass)} method
     * in configured number of iterations, runs the iteration only if the
     * operation is supported by the connector
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testContract(ObjectClass objectClass) {
        //run the contract test for supported operation only
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass,
        		getAPIOperations())) {
            boolean supported = isObjectClassSupported(objectClass);
            try {
                logger.info("--------------------------------------------------------------------------------------");
                logger.info("Running test ''"+getTestName()+"'' for object class ''"+objectClass+"''.");
                logger.info("--------------------------------------------------------------------------------------");
                testRun(objectClass);
                if (!supported) {
                    //should throw RuntimeException
                    fail("ObjectClass " + objectClass + " is not supported, must" +
                            " throw RuntimeException");
                }
            } catch (RuntimeException e) {
                if (supported) {
                    throw new ContractException("Unexpected RuntimeException thrown during contract test.", e);
                }
            }
        }
        else {
            String msg = "Skipping test ''"+getTestName()+"'' for object class ''"+objectClass+"''.";
            logger.info("--------------------------------------------------------------------------------------");
            logger.info(msg);
            logger.info("--------------------------------------------------------------------------------------");
            throw new SkipException(msg);
        }
    }


    /**
     * This method will be called configured number of times
     */
    protected abstract void testRun(ObjectClass objectClass);

    /**
     * Return all the base {@link ObjectClass}s.
     */

    @org.testng.annotations.DataProvider(name = OBJECTCLASS_DATAPROVIDER)
    public Iterator<Object[]> data(ITestContext context) {
        List<Object[]> oclasses = new LinkedList<Object[]>();

        List<String> objectClasses = getObjectClassesProperty(dataProvider);
        if (objectClasses != null) {
            for (String objectClass : objectClasses) {
                oclasses.add(new Object[] { new ObjectClass(objectClass) });
            }
        } else {
            Schema schema = getConnectorFacade().schema();
            for (ObjectClassInfo ocInfo : schema.getObjectClassInfo()) {
                oclasses.add(new Object[] {ConnectorHelper.getObjectClassFromObjectClassInfo(ocInfo)});
            }
        }

        oclasses.add(new Object[] {new ObjectClass("NONEXISTING")});

        StringBuilder sb = new StringBuilder("Tested object classes will be: ''");
        for (Object[] oc : oclasses) {
            sb.append(oc[0].toString());
            sb.append(", ");
        }

        logger.info(sb.append("''.").toString());
        return oclasses.iterator();
    }

    private static List<String> getObjectClassesProperty(DataProvider dataProvider) {
        try {
            @SuppressWarnings("unchecked")
            List<String> objectClasses = (List<String>) dataProvider.getTestSuiteAttribute("objectClasses");
            return objectClasses;
        } catch (ObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * Always returns supported object class by connector operation.
     * If currently tested object class is supported then is returned otherwise not.
     * @return
     */
    /*public ObjectClass getSupportedObjectClass(ObjectClass objectClass) {
        return _supportedObjectClass;
    }*/

    //=================================================================
    // Helper methods
    //=================================================================

    /**
     * Need a schema
     */
    public Schema getSchema() {
        return getConnectorFacade().schema();
    }

    /**
     * Gets Test name
     * @return Test Name
     */
    public abstract String getTestName();

    /**
     * Gets {@link ObjectClassInfo} for object class returned by {@link ObjectClassRunner}.
     *
     * @return {@link ObjectClassInfo}
     */
    public ObjectClassInfo getObjectClassInfo(ObjectClass objectClass) {
        return getConnectorFacade().schema().findObjectClassInfo(objectClass.getObjectClassValue());
    }

    /**
     * Identifier which tells if the tested ObjectClass
     * is supported by connector or not, supported means that the ObjectClass is included in the Schema
     * @return
     */
    public boolean isObjectClassSupported(ObjectClass objectClass) {
        // get all the required operations for current contract test
        Set<Class<? extends APIOperation>> apiOps = getAPIOperations();
        /** set of objectclasses that support all apiOps required by current test */
        Set<ObjectClassInfo> oinfos = null;

        // Create an intersection of supported objectclasses by the connector.
        // These objectclasses should support all apioperations.
        for (Class<? extends APIOperation> apiOperation : apiOps) {
            if (oinfos == null) {
                oinfos = getSchema().getSupportedObjectClassesByOperation(
                        apiOperation);
            } else {
                Set<ObjectClassInfo> currOinfos = getSchema()
                        .getSupportedObjectClassesByOperation(apiOperation);
                Set<ObjectClassInfo> tmp = CollectionUtil.intersection(oinfos, currOinfos);
                oinfos = tmp;
            }
        }

        // Find the objectclass in set of supported objectclasses (oinfos),
        // that is currently tested. If it is present set the indicator _ocSupported accordingly.
        for (ObjectClassInfo oci : oinfos) {
            if (oci.is(objectClass.getObjectClassValue())) {
                _objectClassInfo = oci;
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationOptions getOperationOptionsByOp(ObjectClass objectClass, Class<? extends APIOperation> clazz) {
        if (clazz.equals(SearchApiOp.class) || clazz.equals(GetApiOp.class) || clazz.equals(SyncApiOp.class)) {

            // names of readable attributes
            ObjectClassInfo info = getObjectClassInfo(objectClass);
            Set<String> readableAttrs = ConnectorHelper.getReadableAttributesNames(info);

            // all *readable* object class attributes as attrsToGet
            Collection<String> attrNames = new ArrayList<String>();
            for (AttributeInfo attrInfo : info.getAttributeInfo()) {

                if (readableAttrs.contains(attrInfo.getName())) {
                    attrNames.add(attrInfo.getName());
                }

            }

            OperationOptionsBuilder opOptionsBuilder = new OperationOptionsBuilder();
            opOptionsBuilder.setAttributesToGet(attrNames);
            OperationOptions attrsToGet = opOptionsBuilder.build();

            return attrsToGet;
        }

        return super.getOperationOptionsByOp(objectClass, clazz);
    }

}
