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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

/**
 * Simple base class that will run through all the {@link ObjectClass}s.
 */
public abstract class ObjectClassRunner extends ContractTestBase {

    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(ObjectClassRunner.class);
    
    private final ObjectClass _objectClass;
    private ObjectClassInfo _objectClassInfo;
    private ObjectClass _supportedObjectClass;    
    private boolean _ocSupported = false;
    

    /**
     * Base class for running {@link ObjectClass} across a test.
     */
    public ObjectClassRunner(ObjectClass oclass) {
        _objectClass = oclass;
    }
    
    /**
     * Initialize the environment needed to run the test
     */
    @Before
    public void init() {
        super.init();

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
        for (Iterator<ObjectClassInfo> it = oinfos.iterator(); it.hasNext();) {
            _objectClassInfo = it.next();
            _supportedObjectClass = ConnectorHelper
                    .getObjectClassFromObjectClassInfo(_objectClassInfo);
            if (_supportedObjectClass.equals(getObjectClass())) {
                _ocSupported = true;
                break;
            }
        }
    }

    /**
     * Dispose the test environment, do the cleanup
     */
    @After
    public void dispose() {        
        _objectClassInfo = null;
        super.dispose();
    }
    
    /**
     * Main contract test entry point, it calls {@link #testRun()} method
     * in configured number of iterations, runs the iteration only if the 
     * operation is supported by the connector
     */
    @Test
    public void testContract() {
        //run the contract test for supported operation only
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), getObjectClass(), 
        		getAPIOperations())) {
            try {
                LOG.info("--------------------------------------------------------------------------------------");
                LOG.info("Running test ''{0}'' for object class ''{1}''.", getTestName(), getObjectClass());
                LOG.info("--------------------------------------------------------------------------------------");
                testRun();
                if (!isObjectClassSupported()) {
                    //should throw RuntimeException
                    fail("ObjectClass " + getObjectClass() + " is not supported, must" +
                            " throw RuntimeException");
                }
            } catch (RuntimeException e) {
                if (isObjectClassSupported()) {
                    throw new ContractException("Unexpected RuntimeException thrown during contract test.", e);
                }
            }
        }
        else {
            LOG.info("--------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''{0}'' for object class ''{1}''.", getTestName(), getObjectClass());
            LOG.info("--------------------------------------------------------------------------------------");
        }
    }

    
    /**
     * This method will be called configured number of times
     */
    public abstract void testRun();
    
    /**
     * Return all the base {@link ObjectClass}s.
     */
    @Parameters
    public static List<Object[]> data() {
        List<Object[]> oclasses = new LinkedList<Object[]>();
        
        List<String> objectClasses = getObjectClassesProperty();
        if (objectClasses != null) {
            for (String objectClass : objectClasses) {
                oclasses.add(new Object[] { new ObjectClass(objectClass) });
            }
        } else {
            Schema schema = ConnectorHelper.createConnectorFacade(getDataProvider()).schema();
            for (ObjectClassInfo ocInfo : schema.getObjectClassInfo()) {
                oclasses.add(new Object[] {ConnectorHelper.getObjectClassFromObjectClassInfo(ocInfo)});
            }
        }
        
        oclasses.add(new Object[] {new ObjectClass("NONEXISTING")});
        
        StringBuilder sb = new StringBuilder();
        for (Object[] oc : oclasses) {
            sb.append(oc[0].toString());
            sb.append(",");
        }
        
        LOG.info("Tested object classes will be: ''{0}''.", sb.toString());
        return oclasses;
    }
    
    private static List<String> getObjectClassesProperty() {
        try {
            @SuppressWarnings("unchecked")
            List<String> objectClasses = (List<String>) getDataProvider().getTestSuiteAttribute("objectClasses");
            return objectClasses;
        } catch (ObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * Returns object class which is currently tested.
     */
    public ObjectClass getObjectClass() {
        return _objectClass;
    }
    
    /**
     * Always returns supported object class by connector operation.
     * If currently tested object class is supported then is returned otherwise not.
     * @return
     */
    public ObjectClass getSupportedObjectClass() {
        return _supportedObjectClass;
    }
    
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
     * Gets {@link ObjectClassInfo} for object class returned by {@link ObjectClassRunner#getSupportedObjectClass}.
     * 
     * @return {@link ObjectClassInfo}
     */
    public ObjectClassInfo getObjectClassInfo() {
        return _objectClassInfo;
    }

    /**
     * Identifier which tells if the tested ObjectClass (get by {@link ObjectClassRunner#getObjectClass() }
     * is supported by connector or not, supported means that the ObjectClass is included in the Schema
     * @return
     */
    public boolean isObjectClassSupported() {
        return _ocSupported;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public OperationOptions getOperationOptionsByOp(Class<? extends APIOperation> clazz) {
        if (clazz.equals(SearchApiOp.class) || clazz.equals(GetApiOp.class) || clazz.equals(SyncApiOp.class)) {
            
            // names of readable attributes
            Set<String> readableAttrs = ConnectorHelper.getReadableAttributesNames(getObjectClassInfo());
            
            // all *readable* object class attributes as attrsToGet
            Collection<String> attrNames = new ArrayList<String>();
            for (AttributeInfo attrInfo : getObjectClassInfo().getAttributeInfo()) {
                
                if (readableAttrs.contains(attrInfo.getName())) {
                    attrNames.add(attrInfo.getName());
                }
                
            }
            
            OperationOptionsBuilder opOptionsBuilder = new OperationOptionsBuilder();
            opOptionsBuilder.setAttributesToGet(attrNames);
            OperationOptions attrsToGet = opOptionsBuilder.build();
            
            return attrsToGet;
        }
        
        return super.getOperationOptionsByOp(clazz);
    }        

}
