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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.contract.exceptions.ContractException;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Simple base class that will run through all the {@link ObjectClass}s.
 */
public abstract class ObjectClassRunner extends ContractTestBase {

    private static final Log LOG = Log.getLog(ObjectClassRunner.class);

    /**
     * Main contract test entry point, it calls {@link #testRun()} method
     * in configured number of iterations, runs the iteration only if the
     * operation is supported by the connector
     */
    @ParameterizedTest
    @MethodSource("objectClasses")
    public void testContract(final ObjectClass objectClass) {
        //run the contract test for supported operation only
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())) {
            boolean supported = isObjectClassSupported(objectClass);
            try {
                LOG.info("--------------------------------------------------------------------------------------");
                LOG.info("Running test ''{0}'' for object class ''{1}''.", getTestName(), objectClass);
                LOG.info("--------------------------------------------------------------------------------------");
                testRun(objectClass);
                if (!supported) {
                    //should throw RuntimeException
                    fail("ObjectClass " + objectClass + " is not supported, must" + " throw RuntimeException");
                }
            } catch (RuntimeException e) {
                if (supported) {
                    throw new ContractException("Unexpected RuntimeException thrown during contract test.", e);
                }
            }
        } else {
            LOG.info("--------------------------------------------------------------------------------------");
            LOG.info("Skipping test ''{0}'' for object class ''{1}''.", getTestName(), objectClass);
            LOG.info("--------------------------------------------------------------------------------------");
        }
    }

    /**
     * This method will be called configured number of times
     */
    protected abstract void testRun(ObjectClass objectClass);

    /**
     * Return all the base {@link ObjectClass}s.
     */
    protected static Stream<Arguments> objectClasses() {
        List<Object[]> oclasses = new LinkedList<>();

        List<String> objectClasses = getObjectClassesProperty();
        if (objectClasses != null) {
            objectClasses.forEach((objectClass) -> {
                oclasses.add(new Object[] { new ObjectClass(objectClass) });
            });
        } else {
            Schema schema = ConnectorHelper.createConnectorFacade(getDataProvider()).schema();
            schema.getObjectClassInfo().forEach((ocInfo) -> {
                oclasses.add(new Object[] { ConnectorHelper.getObjectClassFromObjectClassInfo(ocInfo) });
            });
        }

        oclasses.add(new Object[] { new ObjectClass("NONEXISTING") });

        StringBuilder sb = new StringBuilder();
        oclasses.forEach(oc -> {
            sb.append(oc[0].toString());
            sb.append(',');
        });

        LOG.info("Tested object classes will be: ''{0}''.", sb.toString());

        return Stream.of(Arguments.of(oclasses));
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
     *
     * @return Test Name
     */
    public abstract String getTestName();

    /**
     * Gets {@link ObjectClassInfo} for object class returned by {@link ObjectClassRunner#getSupportedObjectClass}.
     *
     * @return {@link ObjectClassInfo}
     */
    public ObjectClassInfo getObjectClassInfo(ObjectClass objectClass) {
        return getConnectorFacade().schema().findObjectClassInfo(objectClass.getObjectClassValue());
    }

    /**
     * Identifier which tells if the tested ObjectClass (get by {@link ObjectClassRunner#objectClass }
     * is supported by connector or not, supported means that the ObjectClass is included in the Schema
     */
    public boolean isObjectClassSupported(final ObjectClass objectClass) {
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
            Set<String> readableAttrs = ConnectorHelper.getReadableAttributesNames(getObjectClassInfo(objectClass));

            // all *readable* object class attributes as attrsToGet
            Collection<String> attrNames = new ArrayList<>();
            getObjectClassInfo(objectClass).getAttributeInfo().stream().
                    filter(attrInfo -> readableAttrs.contains(attrInfo.getName())).
                    forEachOrdered(attrInfo -> {
                        attrNames.add(attrInfo.getName());
                    });

            OperationOptionsBuilder opOptionsBuilder = new OperationOptionsBuilder();
            opOptionsBuilder.setAttributesToGet(attrNames);
            OperationOptions attrsToGet = opOptionsBuilder.build();

            return attrsToGet;
        }

        return super.getOperationOptionsByOp(objectClass, clazz);
    }
}
