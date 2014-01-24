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
 */
package org.identityconnectors.framework.common.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.SPIOperation;

/**
 * Simple builder class to help facilitate creating a {@link Schema} object.
 *
 * @author Will Droste
 * @since 1.0
 */
public final class SchemaBuilder {

    private final Class<? extends Connector> connectorClass;
    private final Set<ObjectClassInfo> declaredObjectClasses = new HashSet<ObjectClassInfo>();
    private final Set<OperationOptionInfo> declaredOperationOptions =
            new HashSet<OperationOptionInfo>();

    private final Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> supportedObjectClassesByOperation =
            new HashMap<Class<? extends APIOperation>, Set<ObjectClassInfo>>();
    private final Map<Class<? extends APIOperation>, Set<OperationOptionInfo>> supportedOptionsByOperation =
            new HashMap<Class<? extends APIOperation>, Set<OperationOptionInfo>>();

    /**
     * Creates a SchemaBuilder for the given connector class
     *
     * @param connectorClass
     *            The connector for which we are building the schema.
     */
    public SchemaBuilder(Class<? extends Connector> connectorClass) {
        Assertions.nullCheck(connectorClass, "connectorClass");
        this.connectorClass = connectorClass;
    }

    /**
     * Adds another ObjectClassInfo to the schema.
     *
     * Also, adds this to the set of supported classes for every operation
     * defined by the Connector.
     *
     * @param info
     * @throws IllegalStateException
     *             If already defined
     */
    public void defineObjectClass(ObjectClassInfo info) {
        Assertions.nullCheck(info, "info");
        if (declaredObjectClasses.contains(info)) {
            throw new IllegalStateException("ObjectClass already defined: " + info.getType());
        }
        declaredObjectClasses.add(info);
        for (Class<? extends APIOperation> op : FrameworkUtil
                .getDefaultSupportedOperations(connectorClass)) {
            Set<ObjectClassInfo> oclasses = supportedObjectClassesByOperation.get(op);
            if (oclasses == null) {
                oclasses = new HashSet<ObjectClassInfo>();
                supportedObjectClassesByOperation.put(op, oclasses);
            }
            oclasses.add(info);
        }
    }

    /**
     * Adds another OperationOptionInfo to the schema. Also, adds this to the
     * set of supported options for every operation defined by the Connector.
     */
    public void defineOperationOption(OperationOptionInfo info) {
        Assertions.nullCheck(info, "info");
        if (declaredOperationOptions.contains(info)) {
            throw new IllegalStateException("OperationOption already defined: " + info.getName());
        }
        declaredOperationOptions.add(info);
        for (Class<? extends APIOperation> op : FrameworkUtil
                .getDefaultSupportedOperations(connectorClass)) {
            Set<OperationOptionInfo> oclasses = supportedOptionsByOperation.get(op);
            if (oclasses == null) {
                oclasses = new HashSet<OperationOptionInfo>();
                supportedOptionsByOperation.put(op, oclasses);
            }
            oclasses.add(info);
        }
    }

    /**
     * Adds another ObjectClassInfo to the schema.
     *
     * Also, adds this to the set of supported classes for every operation
     * defined by the Connector.
     *
     * @throws IllegalStateException
     *             If already defined
     */
    public void defineObjectClass(String type, Set<AttributeInfo> attrInfo) {
        ObjectClassInfoBuilder bld = new ObjectClassInfoBuilder();
        bld.setType(type);
        bld.addAllAttributeInfo(attrInfo);
        ObjectClassInfo obj = bld.build();
        defineObjectClass(obj);
    }

    /**
     * Adds another OperationOptionInfo to the schema.
     *
     * Also, adds this to the set of supported options for every operation
     * defined by the Connector.
     *
     * @throws IllegalStateException
     *             If already defined
     */
    public void defineOperationOption(String optionName, Class<?> type) {
        OperationOptionInfoBuilder bld = new OperationOptionInfoBuilder();
        bld.setName(optionName);
        bld.setType(type);
        OperationOptionInfo info = bld.build();
        defineOperationOption(info);
    }

    /**
     * Adds the given ObjectClassInfo as a supported ObjectClass for the given
     * operation.
     *
     * @param op
     *            The SPI operation
     * @param def
     *            The ObjectClassInfo
     * @throws IllegalArgumentException
     *             If the given ObjectClassInfo was not already defined using
     *             {@link #defineObjectClass(ObjectClassInfo)}.
     */
    public void addSupportedObjectClass(Class<? extends SPIOperation> op, ObjectClassInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis = FrameworkUtil.spi2apis(op);
        if (!declaredObjectClasses.contains(def)) {
            throw new IllegalArgumentException("ObjectClass " + def.getType()
                    + " not defined in schema.");
        }
        for (Class<? extends APIOperation> api : apis) {
            Set<ObjectClassInfo> infos = supportedObjectClassesByOperation.get(api);
            if (infos == null) {
                throw new IllegalArgumentException("Operation " + op.getName()
                        + " not implement by connector.");
            }
            if (infos.contains(def)) {
                throw new IllegalArgumentException("ObjectClass " + def.getType()
                        + " already supported for operation " + op.getName());
            }
            infos.add(def);
        }
    }

    /**
     * Removes the given ObjectClassInfo as a supported ObjectClass for the
     * given operation.
     *
     * @param op
     *            The SPI operation
     * @param def
     *            The ObjectClassInfo
     * @throws IllegalArgumentException
     *             If the given ObjectClassInfo was not already defined using
     *             {@link #defineObjectClass(ObjectClassInfo)}.
     */
    public void removeSupportedObjectClass(Class<? extends SPIOperation> op, ObjectClassInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis = FrameworkUtil.spi2apis(op);
        if (!declaredObjectClasses.contains(def)) {
            throw new IllegalArgumentException("ObjectClass " + def.getType()
                    + " not defined in schema.");
        }
        for (Class<? extends APIOperation> api : apis) {
            Set<ObjectClassInfo> infos = supportedObjectClassesByOperation.get(api);
            if (infos == null) {
                throw new IllegalArgumentException("Operation " + op.getName()
                        + " not implement by connector.");
            }
            if (!infos.contains(def)) {
                throw new IllegalArgumentException("ObjectClass " + def.getType()
                        + " already removed for operation " + op.getName());
            }
            infos.remove(def);
        }
    }

    /**
     * Adds the given OperationOptionInfo as a supported option for the given
     * operation.
     *
     * @param op
     *            The SPI operation
     * @param def
     *            The OperationOptionInfo
     * @throws IllegalArgumentException
     *             If the given OperationOptionInfo was not already defined
     *             using {@link #defineOperationOption(OperationOptionInfo)}.
     */
    public void addSupportedOperationOption(Class<? extends SPIOperation> op,
            OperationOptionInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis = FrameworkUtil.spi2apis(op);
        if (!declaredOperationOptions.contains(def)) {
            throw new IllegalArgumentException("OperationOption " + def.getName()
                    + " not defined in schema.");
        }
        for (Class<? extends APIOperation> api : apis) {
            Set<OperationOptionInfo> infos = supportedOptionsByOperation.get(api);
            if (infos == null) {
                throw new IllegalArgumentException("Operation " + op.getName()
                        + " not implement by connector.");
            }
            if (infos.contains(def)) {
                throw new IllegalArgumentException("OperationOption " + def.getName()
                        + " already supported for operation " + op.getName());
            }
            infos.add(def);
        }
    }

    /**
     * Removes the given OperationOptionInfo as a supported option for the given
     * operation.
     *
     * @param op
     *            The SPI operation
     * @param def
     *            The OperationOptionInfo
     * @throws IllegalArgumentException
     *             If the given OperationOptionInfo was not already defined
     *             using {@link #defineOperationOption(OperationOptionInfo)}.
     */
    public void removeSupportedOperationOption(Class<? extends SPIOperation> op,
            OperationOptionInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis = FrameworkUtil.spi2apis(op);
        if (!declaredOperationOptions.contains(def)) {
            throw new IllegalArgumentException("OperationOption " + def.getName()
                    + " not defined in schema.");
        }
        for (Class<? extends APIOperation> api : apis) {
            Set<OperationOptionInfo> infos = supportedOptionsByOperation.get(api);
            if (infos == null) {
                throw new IllegalArgumentException("Operation " + op.getName()
                        + " not implement by connector.");
            }
            if (!infos.contains(def)) {
                throw new IllegalArgumentException("OperationOption " + def.getName()
                        + " already removed for operation " + op.getName());
            }
            infos.remove(def);
        }
    }

    /**
     * Clears the operation-specific supported classes.
     *
     * Normally, when you add an ObjectClass, using
     * {@link #defineObjectClass(ObjectClassInfo)}, it is added to all
     * operations. You may then remove those that you need using
     * {@link #removeSupportedObjectClass(Class, ObjectClassInfo)}. You may
     * wish, as an alternative to clear everything out and instead add using
     * {@link #addSupportedObjectClass(Class, ObjectClassInfo)}.
     */
    public void clearSupportedObjectClassesByOperation() {
        for (Set<ObjectClassInfo> values : supportedObjectClassesByOperation.values()) {
            values.clear();
        }
    }

    /**
     * Clears the operation-specific supported options.
     *
     * Normally, when you add an OperationOptionInfo using
     * {@link #defineOperationOption(OperationOptionInfo)}, this adds the option
     * to all operations. You may then remove the option from any operation that
     * does not support the option using
     * {@link #removeSupportedOperationOption(Class, OperationOptionInfo)}. An
     * alternative approach is to clear everything out (using this method) and
     * then add each option to every operation that supports the option using
     * {@link #addSupportedOperationOption(Class, OperationOptionInfo)}.
     */
    public void clearSupportedOptionsByOperation() {
        for (Set<OperationOptionInfo> values : supportedOptionsByOperation.values()) {
            values.clear();
        }
    }

    /**
     * Builds the {@link Schema} object based on the {@link ObjectClassInfo}s
     * added so far.
     *
     * @return new Schema object based on the info provided.
     */
    public Schema build() {
        if (declaredObjectClasses.isEmpty()) {
            throw new IllegalStateException("Must be at least one ObjectClassInfo object!");
        }
        return new Schema(declaredObjectClasses, declaredOperationOptions,
                supportedObjectClassesByOperation, supportedOptionsByOperation);
    }
}
