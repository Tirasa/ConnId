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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * Determines the objects supported by a
 * {@link org.identityconnectors.framework.spi.Connector}.
 *
 * The {@link Schema} object is used to represent the basic objects that a
 * connector supports. This does not prevent a connector from supporting more.
 * Rather, this is informational for the caller of the connector to understand a
 * minimum support level. The schema defines 4 primary data structures
 * <ol>
 * <li>Declared ObjectClasses ({@link #getObjectClassInfo()}).</li>
 * <li>Declared OperationOptionInfo ({@link #getOperationOptionInfo()}).</li>
 * <li>Supported ObjectClasses by operation (
 * {@link #getSupportedObjectClassesByOperation()}).</li>
 * <li>Supported OperationOptionInfo by operation(
 * {@link #getSupportedOptionsByOperation()()}).</li>
 * </ol>
 *
 * TODO: add more to describe and what is expected from this call and how it is
 * used.. based on OperationalAttribute etc..
 */
public final class Schema {
    /**
     *
     */
    private final Set<ObjectClassInfo> declaredObjectClasses;
    private final Set<OperationOptionInfo> declaredOperationOptions;
    private final Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> supportedObjectClassesByOperation;
    private final Map<Class<? extends APIOperation>, Set<OperationOptionInfo>> supportedOptionsByOperation;

    /**
     * Public only for serialization; please use SchemaBuilder instead.
     *
     * @param info
     * @param supportedObjectClassesByOperation
     */
    public Schema(
            Set<ObjectClassInfo> info,
            Set<OperationOptionInfo> options,
            Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> supportedObjectClassesByOperation,
            Map<Class<? extends APIOperation>, Set<OperationOptionInfo>> supportedOptionsByOperation) {
        declaredObjectClasses = CollectionUtil.newReadOnlySet(info);
        declaredOperationOptions = CollectionUtil.newReadOnlySet(options);
        // make read-only
        {
            Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> temp =
                    new HashMap<Class<? extends APIOperation>, Set<ObjectClassInfo>>();
            for (Map.Entry<Class<? extends APIOperation>, Set<ObjectClassInfo>> entry : supportedObjectClassesByOperation
                    .entrySet()) {
                Class<? extends APIOperation> op = entry.getKey();
                Set<ObjectClassInfo> resolvedClasses =
                        CollectionUtil.newReadOnlySet(entry.getValue());
                temp.put(op, resolvedClasses);
            }
            this.supportedObjectClassesByOperation = CollectionUtil.asReadOnlyMap(temp);
        }
        // make read-only
        {
            Map<Class<? extends APIOperation>, Set<OperationOptionInfo>> temp =
                    new HashMap<Class<? extends APIOperation>, Set<OperationOptionInfo>>();
            for (Map.Entry<Class<? extends APIOperation>, Set<OperationOptionInfo>> entry : supportedOptionsByOperation
                    .entrySet()) {
                Class<? extends APIOperation> op = entry.getKey();
                Set<OperationOptionInfo> resolvedClasses =
                        CollectionUtil.newReadOnlySet(entry.getValue());
                temp.put(op, resolvedClasses);
            }
            this.supportedOptionsByOperation = CollectionUtil.asReadOnlyMap(temp);
        }

    }

    /**
     * Returns the set of object classes that are defined in the schema,
     * regardless of which operations support them.
     */
    public Set<ObjectClassInfo> getObjectClassInfo() {
        return declaredObjectClasses;
    }

    /**
     * Returns the ObjectClassInfo for the given type.
     *
     * @param type
     *            The type to find.
     * @return the ObjectClassInfo for the given type or null if not found.
     */
    public ObjectClassInfo findObjectClassInfo(String type) {
        Assertions.nullCheck(type, "type");
        for (ObjectClassInfo info : declaredObjectClasses) {
            if (info.is(type)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Returns the set of operation options that are defined in the schema,
     * regardless of which operations support them.
     *
     * @return The options defined in this schema.
     */
    public Set<OperationOptionInfo> getOperationOptionInfo() {
        return declaredOperationOptions;
    }

    /**
     * Returns the OperationOptionInfo for the given name.
     *
     * @param name
     *            The name to find.
     * @return the OperationOptionInfo for the given name or null if not found.
     */
    public OperationOptionInfo findOperationOptionInfo(String name) {
        Assertions.nullCheck(name, "name");
        for (OperationOptionInfo info : declaredOperationOptions) {
            if (info.getName().equals(name)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Returns the supported object classes for the given operation.
     *
     * @param apiop
     *            The operation.
     * @return the supported object classes for the given operation.
     */
    public Set<ObjectClassInfo> getSupportedObjectClassesByOperation(
            Class<? extends APIOperation> apiop) {
        Set<ObjectClassInfo> rv = supportedObjectClassesByOperation.get(apiop);
        if (rv == null) {
            @SuppressWarnings("unchecked")
            Set<ObjectClassInfo> empty = Collections.EMPTY_SET;
            return empty;
        } else {
            return rv;
        }
    }

    /**
     * Returns the supported options for the given operation.
     *
     * @param apiop
     *            The operation.
     * @return the supported options for the given operation.
     */
    public Set<OperationOptionInfo> getSupportedOptionsByOperation(
            Class<? extends APIOperation> apiop) {
        Set<OperationOptionInfo> rv = supportedOptionsByOperation.get(apiop);
        if (rv == null) {
            @SuppressWarnings("unchecked")
            Set<OperationOptionInfo> empty = Collections.EMPTY_SET;
            return empty;
        } else {
            return rv;
        }
    }

    /**
     * Returns the set of object classes that apply to a particular operation.
     *
     * @return the set of object classes that apply to a particular operation.
     */
    public Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> getSupportedObjectClassesByOperation() {
        return supportedObjectClassesByOperation;
    }

    /**
     * Returns the set of operation options that apply to a particular
     * operation.
     *
     * @return the set of operation options that apply to a particular
     *         operation.
     */
    public Map<Class<? extends APIOperation>, Set<OperationOptionInfo>> getSupportedOptionsByOperation() {
        return supportedOptionsByOperation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return SerializerUtil.serializeXmlObject(this, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Schema) {
            Schema other = (Schema) obj;
            if (!CollectionUtil.equals(getObjectClassInfo(), other.getObjectClassInfo())) {
                return false;
            }
            if (!CollectionUtil.equals(getOperationOptionInfo(), other.getOperationOptionInfo())) {
                return false;
            }
            if (!CollectionUtil.equals(supportedObjectClassesByOperation,
                    other.supportedObjectClassesByOperation)) {
                return false;
            }
            if (!CollectionUtil.equals(supportedOptionsByOperation,
                    other.supportedOptionsByOperation)) {
                return false;
            }
            return true;
        }
        return false;

    }

    /**
     * Create a hash code from all the object info objects.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return declaredObjectClasses.hashCode();
    }

}
