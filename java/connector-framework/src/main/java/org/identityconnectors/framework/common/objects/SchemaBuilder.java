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
 * Portions Copyrighted 2014 ForgeRock AS.
 * Portions Copyrighted 2018 ConnId
 * Portions Copyrighted 2018 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.ResolveUsernameApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.api.operations.UpdateDeltaApiOp;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.SPIOperation;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.ScriptOnConnectorOp;
import org.identityconnectors.framework.spi.operations.ScriptOnResourceOp;
import org.identityconnectors.framework.spi.operations.TestOp;

/**
 * Simple builder class to help facilitate creating a {@link Schema} object.
 *
 * @author Will Droste
 * @since 1.0
 */
public final class SchemaBuilder {

    private final Set<ObjectClassInfo> declaredObjectClasses = new HashSet<>();

    private final Set<OperationOptionInfo> declaredOperationOptions = new HashSet<>();

    private final Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> supportedObjectClassesByOperation =
            new HashMap<>();

    private final Map<Class<? extends APIOperation>, Set<OperationOptionInfo>> supportedOptionsByOperation =
            new HashMap<>();

    private final Set<Class<? extends APIOperation>> defaultSupportedOperations;

    /**
     * Creates a SchemaBuilder for the given connector class
     *
     * @param connectorClass The connector for which we are building the schema.
     */
    public SchemaBuilder(Class<? extends Connector> connectorClass) {
        Assertions.nullCheck(connectorClass, "connectorClass");
        this.defaultSupportedOperations = FrameworkUtil.getDefaultSupportedOperations(connectorClass);
    }

    private boolean objectClassOperation(Class<? extends APIOperation> op) {
        return AuthenticationApiOp.class.equals(op) || CreateApiOp.class.equals(op)
                || DeleteApiOp.class.equals(op) || GetApiOp.class.equals(op)
                || ResolveUsernameApiOp.class.equals(op) || SearchApiOp.class.equals(op)
                || SyncApiOp.class.equals(op) || UpdateApiOp.class.equals(op)
                || UpdateDeltaApiOp.class.equals(op);
    }

    private boolean operationOptionOperation(Class<? extends APIOperation> op) {
        return AuthenticationApiOp.class.equals(op) || CreateApiOp.class.equals(op)
                || DeleteApiOp.class.equals(op) || GetApiOp.class.equals(op)
                || ResolveUsernameApiOp.class.equals(op) || ScriptOnConnectorApiOp.class.equals(op)
                || ScriptOnResourceApiOp.class.equals(op) || SearchApiOp.class.equals(op)
                || SyncApiOp.class.equals(op) || UpdateApiOp.class.equals(op)
                || UpdateDeltaApiOp.class.equals(op);
    }

    /**
     * Adds another ObjectClassInfo to the schema.
     *
     * Also, adds this to the set of supported classes for every operation defined by the Connector.
     *
     * @param info
     * @throws IllegalStateException If already defined
     */
    public void defineObjectClass(ObjectClassInfo info) {
        Assertions.nullCheck(info, "info");
        if (declaredObjectClasses.contains(info)) {
            throw new IllegalStateException("ObjectClass already defined: " + info.getType());
        }
        declaredObjectClasses.add(info);
        defaultSupportedOperations.stream().
                filter((op) -> (objectClassOperation(op))).
                map((op) -> {
                    Set<ObjectClassInfo> oclasses = supportedObjectClassesByOperation.get(op);
                    if (oclasses == null) {
                        oclasses = new HashSet<>();
                        supportedObjectClassesByOperation.put(op, oclasses);
                    }
                    return oclasses;
                }).forEachOrdered(oclasses -> {
            oclasses.add(info);
        });
    }

    /**
     * Adds another ObjectClassInfo to the schema.
     *
     * Also, adds this to the set of supported classes for every operation
     * defined by the Connector.
     *
     * @param objectClassInfo
     * @param operations The SPI operation which use supports this {@code objectClassInfo}
     *
     * @throws IllegalStateException If already defined
     */
    @SafeVarargs
    public final void defineObjectClass(ObjectClassInfo objectClassInfo, Class<? extends SPIOperation>... operations) {
        if (operations.length > 0) {
            Assertions.nullCheck(objectClassInfo, "objectClassInfo");
            if (declaredObjectClasses.contains(objectClassInfo)) {
                throw new IllegalStateException("ObjectClass already defined: "
                        + objectClassInfo.getType());
            }
            declaredObjectClasses.add(objectClassInfo);
            for (Class<? extends SPIOperation> spi : operations) {
                if (SchemaOp.class.equals(spi) || ScriptOnConnectorOp.class.equals(spi)
                        || ScriptOnResourceOp.class.equals(spi) || TestOp.class.equals(spi)) {
                    continue;
                }
                Set<Class<? extends APIOperation>> apiOperations = FrameworkUtil.spi2apis(spi);
                apiOperations.retainAll(defaultSupportedOperations);
                apiOperations.stream().
                        filter((api) -> (objectClassOperation(api))).
                        map((api) -> {
                            Set<ObjectClassInfo> oclasses = supportedObjectClassesByOperation.get(api);
                            if (oclasses == null) {
                                oclasses = new HashSet<>();
                                supportedObjectClassesByOperation.put(api, oclasses);
                            }
                            return oclasses;
                        }).forEachOrdered((oclasses) -> {
                    oclasses.add(objectClassInfo);
                });
            }

        } else {
            defineObjectClass(objectClassInfo);
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
        defaultSupportedOperations.stream().
                filter((op) -> (operationOptionOperation(op))).
                map((op) -> {
                    Set<OperationOptionInfo> oclasses = supportedOptionsByOperation.get(op);
                    if (oclasses == null) {
                        oclasses = new HashSet<>();
                        supportedOptionsByOperation.put(op, oclasses);
                    }
                    return oclasses;
                }).forEachOrdered((oclasses) -> {
            oclasses.add(info);
        });
    }

    /**
     * Adds another OperationOptionInfo to the schema. Also, adds this to the
     * set of supported options for operation defined.
     *
     * @param operationOptionInfo
     * @param operations
     *
     * @throws IllegalStateException If already defined
     */
    @SafeVarargs
    public final void defineOperationOption(
            OperationOptionInfo operationOptionInfo,
            Class<? extends SPIOperation>... operations) {

        if (operations.length > 0) {
            Assertions.nullCheck(operationOptionInfo, "info");
            if (declaredOperationOptions.contains(operationOptionInfo)) {
                throw new IllegalStateException("OperationOption already defined: "
                        + operationOptionInfo.getName());
            }
            declaredOperationOptions.add(operationOptionInfo);
            for (Class<? extends SPIOperation> spi : operations) {
                if (SchemaOp.class.equals(spi) || TestOp.class.equals(spi)) {
                    continue;
                }
                Set<Class<? extends APIOperation>> apiOperations = FrameworkUtil.spi2apis(spi);
                apiOperations.retainAll(defaultSupportedOperations);
                apiOperations.stream().
                        filter((api) -> (operationOptionOperation(api))).
                        map((api) -> {
                            Set<OperationOptionInfo> oclasses = supportedOptionsByOperation.get(api);
                            if (oclasses == null) {
                                oclasses = new HashSet<>();
                                supportedOptionsByOperation.put(api, oclasses);
                            }
                            return oclasses;
                        }).forEachOrdered((oclasses) -> {
                    oclasses.add(operationOptionInfo);
                });
            }

        } else {
            defineOperationOption(operationOptionInfo);
        }
    }

    /**
     * Adds another ObjectClassInfo to the schema.
     *
     * Also, adds this to the set of supported classes for every operation
     * defined by the Connector.
     *
     * @throws IllegalStateException If already defined
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
     * @throws IllegalStateException If already defined
     */
    public void defineOperationOption(String optionName, Class<?> type) {
        OperationOptionInfoBuilder bld = new OperationOptionInfoBuilder();
        bld.setName(optionName);
        bld.setType(type);
        OperationOptionInfo info = bld.build();
        defineOperationOption(info);
    }

    /**
     * Adds the given ObjectClassInfo as a supported ObjectClass for the given operation.
     *
     * @param op The SPI operation
     * @param def The ObjectClassInfo
     * @throws IllegalArgumentException If the given ObjectClassInfo was not already defined using
     * {@link #defineObjectClass(ObjectClassInfo)}.
     */
    public void addSupportedObjectClass(Class<? extends SPIOperation> op, ObjectClassInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis = FrameworkUtil.spi2apis(op);
        apis.retainAll(defaultSupportedOperations);
        if (!declaredObjectClasses.contains(def)) {
            throw new IllegalArgumentException("ObjectClass " + def.getType()
                    + " not defined in schema.");
        }
        apis.stream().
                filter((api) -> (objectClassOperation(api))).
                map((api) -> supportedObjectClassesByOperation.get(api)).
                map((infos) -> {
                    if (infos == null) {
                        throw new IllegalArgumentException("Operation " + op.getName()
                                + " not implement by connector.");
                    }
                    return infos;
                }).map((infos) -> {
            if (infos.contains(def)) {
                throw new IllegalArgumentException("ObjectClass " + def.getType()
                        + " already supported for operation " + op.getName());
            }
            return infos;
        }).forEachOrdered((infos) -> {
            infos.add(def);
        });
    }

    /**
     * Removes the given ObjectClassInfo as a supported ObjectClass for the given operation.
     *
     * @param op The SPI operation
     * @param def The ObjectClassInfo
     * @throws IllegalArgumentException If the given ObjectClassInfo was not already defined using
     * {@link #defineObjectClass(ObjectClassInfo)}.
     */
    public void removeSupportedObjectClass(Class<? extends SPIOperation> op, ObjectClassInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis = FrameworkUtil.spi2apis(op);
        if (!declaredObjectClasses.contains(def)) {
            throw new IllegalArgumentException("ObjectClass " + def.getType()
                    + " not defined in schema.");
        }
        apis.stream().
                filter((api) -> (objectClassOperation(api))).
                forEachOrdered((api) -> {
                    if (defaultSupportedOperations.contains(api)) {
                        Set<ObjectClassInfo> infos = supportedObjectClassesByOperation.get(api);
                        if (null == infos || !infos.contains(def)) {
                            throw new IllegalArgumentException("ObjectClass " + def.getType()
                                    + " already removed for operation " + op.getName());
                        }
                        infos.remove(def);
                    } else {
                        throw new IllegalArgumentException("Operation " + op.getName()
                                + " not implement by connector.");
                    }
                });
    }

    /**
     * Adds the given OperationOptionInfo as a supported option for the given operation.
     *
     * @param op the SPI operation
     * @param def The OperationOptionInfo
     * @throws IllegalArgumentException If the given OperationOptionInfo was not already defined
     * using {@link #defineOperationOption(OperationOptionInfo)}.
     */
    public void addSupportedOperationOption(Class<? extends SPIOperation> op,
            OperationOptionInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis = FrameworkUtil.spi2apis(op);
        apis.retainAll(defaultSupportedOperations);
        if (!declaredOperationOptions.contains(def)) {
            throw new IllegalArgumentException("OperationOption " + def.getName()
                    + " not defined in schema.");
        }
        apis.stream().
                filter((api) -> (operationOptionOperation(api))).
                map((api) -> supportedOptionsByOperation.get(api)).
                map((infos) -> {
                    if (infos == null) {
                        throw new IllegalArgumentException("Operation " + op.getName()
                                + " not implement by connector.");
                    }
                    return infos;
                }).map((infos) -> {
            if (infos.contains(def)) {
                throw new IllegalArgumentException("OperationOption " + def.getName()
                        + " already supported for operation " + op.getName());
            }
            return infos;
        }).forEachOrdered((infos) -> {
            infos.add(def);
        });
    }

    /**
     * Removes the given OperationOptionInfo as a supported option for the given operation.
     *
     * @param op The SPI operation
     * @param def The OperationOptionInfo
     * @throws IllegalArgumentException If the given OperationOptionInfo was not already defined
     * using {@link #defineOperationOption(OperationOptionInfo)}.
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
        apis.stream().
                filter((api) -> (operationOptionOperation(api))).
                forEachOrdered((api) -> {
                    if (defaultSupportedOperations.contains(api)) {
                        Set<OperationOptionInfo> infos = supportedOptionsByOperation.get(api);
                        if (null == infos || !infos.contains(def)) {
                            throw new IllegalArgumentException("OperationOption " + def.getName()
                                    + " already removed for operation " + op.getName());
                        }
                        infos.remove(def);
                    } else {
                        throw new IllegalArgumentException("Operation " + op.getName()
                                + " not implement by connector.");
                    }
                });
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
        supportedObjectClassesByOperation.values().forEach((values) -> {
            values.clear();
        });
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
        supportedOptionsByOperation.values().forEach((values) -> {
            values.clear();
        });
    }

    /**
     * Builds the {@link Schema} object based on the {@link ObjectClassInfo}s added so far.
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
