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
 * @version $Revision $
 * @since 1.0
 */
public final class SchemaBuilder {
    
    private final Class<? extends Connector> _connectorClass;
    private final Set<ObjectClassInfo> _declaredObjectClasses
    = new HashSet<ObjectClassInfo>();
    private final Set<OperationOptionInfo> _declaredOperationOptions
    = new HashSet<OperationOptionInfo>();
    
    private final Map<Class<? extends APIOperation>,Set<ObjectClassInfo>>
        _supportedObjectClassesByOperation = 
            new HashMap<Class<? extends APIOperation>,Set<ObjectClassInfo>>();
    private final Map<Class<? extends APIOperation>,Set<OperationOptionInfo>>
    _supportedOptionsByOperation = 
        new HashMap<Class<? extends APIOperation>,Set<OperationOptionInfo>>();
    
    /**
     * Creates a SchemaBuilder for the given connector class
     * @param connectorClass The connector for which we
     * are building the schema.
     */
    public SchemaBuilder(Class<? extends Connector> connectorClass) {
        Assertions.nullCheck(connectorClass, "connectorClass");
        _connectorClass = connectorClass;
    }

    /**
     * Adds another ObjectClassInfo to the schema. Also, adds this
     * to the set of supported classes for every operation defined by
     * the Connector.
     * 
     * @param info
     * @throws IllegalStateException If already defined
     */
    public void defineObjectClass(ObjectClassInfo info) {
        Assertions.nullCheck(info, "info");
        if (_declaredObjectClasses.contains(info)) {
            throw new IllegalStateException("ObjectClass already defined: "+
                    info.getType());
        }
        _declaredObjectClasses.add(info);
        for (Class<? extends APIOperation> op : 
            FrameworkUtil.getDefaultSupportedOperations(_connectorClass)) {
            Set<ObjectClassInfo> oclasses = 
                _supportedObjectClassesByOperation.get(op);
            if (oclasses == null) {
                oclasses = new HashSet<ObjectClassInfo>();
                _supportedObjectClassesByOperation.put(op, oclasses);
            }
            oclasses.add(info);
        }
    }
    
    /**
     * Adds another OperationOptionInfo to the schema. Also, adds this
     * to the set of supported options for every operation defined by
     * the Connector.
     */
    public void defineOperationOption(OperationOptionInfo info) {
        Assertions.nullCheck(info, "info");
        if (_declaredOperationOptions.contains(info)) {
            throw new IllegalStateException("OperationOption already defined: "+
                    info.getName());
        }
        _declaredOperationOptions.add(info);
        for (Class<? extends APIOperation> op : 
            FrameworkUtil.getDefaultSupportedOperations(_connectorClass)) {
            Set<OperationOptionInfo> oclasses = 
                _supportedOptionsByOperation.get(op);
            if (oclasses == null) {
                oclasses = new HashSet<OperationOptionInfo>();
                _supportedOptionsByOperation.put(op, oclasses);
            }
            oclasses.add(info);
        }
    }

    /**
     * Adds another ObjectClassInfo to the schema. Also, adds this
     * to the set of supported classes for every operation defined by
     * the Connector.
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
     * Adds another OperationOptionInfo to the schema. Also, adds this
     * to the set of supported options for every operation defined by
     * the Connector.
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
     * Adds the given ObjectClassInfo as a supported ObjectClass for
     * the given operation. 
     * @param op The SPI operation
     * @param def The ObjectClassInfo
     * @throws IllegalArgumentException If the given ObjectClassInfo was
     *  not already defined using {@link #defineObjectClass(ObjectClassInfo)}.
     */
    public void addSupportedObjectClass(Class<? extends SPIOperation> op,
            ObjectClassInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis =
            FrameworkUtil.spi2apis(op);
        if (!_declaredObjectClasses.contains(def)) {
            throw new IllegalArgumentException("ObjectClass "+def.getType()+
                    " not defined in schema.");
        }
        for (Class<? extends APIOperation> api : apis) {
            Set<ObjectClassInfo> infos = 
                _supportedObjectClassesByOperation.get(api);
            if ( infos == null ) {
                throw new IllegalArgumentException("Operation "+op.getName()+
                        " not implement by connector.");                
            }
            if ( infos.contains(def)) {
                throw new IllegalArgumentException("ObjectClass "+def.getType()+
                        " already supported for operation "+op.getName());
            }
            infos.add(def);
        }
    }
    
    /**
     * Removes the given ObjectClassInfo as a supported ObjectClass for
     * the given operation. 
     * @param op The SPI operation
     * @param def The ObjectClassInfo
     * @throws IllegalArgumentException If the given ObjectClassInfo was
     *  not already defined using {@link #defineObjectClass(ObjectClassInfo)}.
     */
    public void removeSupportedObjectClass(Class<? extends SPIOperation> op,
            ObjectClassInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis =
            FrameworkUtil.spi2apis(op);
        if (!_declaredObjectClasses.contains(def)) {
            throw new IllegalArgumentException("ObjectClass "+def.getType()+
                    " not defined in schema.");
        }
        for (Class<? extends APIOperation> api : apis) {
            Set<ObjectClassInfo> infos = 
                _supportedObjectClassesByOperation.get(api);
            if ( infos == null ) {
                throw new IllegalArgumentException("Operation "+op.getName()+
                        " not implement by connector.");                
            }
            if ( !infos.contains(def)) {
                throw new IllegalArgumentException("ObjectClass "+def.getType()
                        +" already removed for operation "+op.getName());
            }
            infos.remove(def);
        }
    }
    
    /**
     * Adds the given OperationOptionInfo as a supported option for
     * the given operation. 
     * @param op The SPI operation
     * @param def The OperationOptionInfo
     * @throws IllegalArgumentException If the given OperationOptionInfo was
     *  not already defined using {@link #defineOperationOption(OperationOptionInfo)}.
     */
    public void addSupportedOperationOption(Class<? extends SPIOperation> op,
            OperationOptionInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis =
            FrameworkUtil.spi2apis(op);
        if (!_declaredOperationOptions.contains(def)) {
            throw new IllegalArgumentException("OperationOption "+def.getName()+
                    " not defined in schema.");
        }
        for (Class<? extends APIOperation> api : apis) {
            Set<OperationOptionInfo> infos = 
                _supportedOptionsByOperation.get(api);
            if ( infos == null ) {
                throw new IllegalArgumentException("Operation "+op.getName()+
                        " not implement by connector.");                
            }
            if ( infos.contains(def) ) {
                throw new IllegalArgumentException("OperationOption "+def.getName()+
                        " already supported for operation "+op.getName());
            }
            infos.add(def);
        }
    }
    
    /**
     * Removes the given OperationOptionInfo as a supported option for
     * the given operation. 
     * @param op The SPI operation
     * @param def The OperationOptionInfo
     * @throws IllegalArgumentException If the given OperationOptionInfo was
     *  not already defined using {@link #defineOperationOption(OperationOptionInfo)}.
     */
    public void removeSupportedOperationOption(Class<? extends SPIOperation> op,
            OperationOptionInfo def) {
        Assertions.nullCheck(op, "op");
        Assertions.nullCheck(def, "def");
        Set<Class<? extends APIOperation>> apis =
            FrameworkUtil.spi2apis(op);
        if (!_declaredOperationOptions.contains(def)) {
            throw new IllegalArgumentException("OperationOption "+def.getName()+
                    " not defined in schema.");
        }
        for (Class<? extends APIOperation> api : apis) {
            Set<OperationOptionInfo> infos = 
                _supportedOptionsByOperation.get(api);
            if ( infos == null ) {
                throw new IllegalArgumentException("Operation "+op.getName()+
                        " not implement by connector.");                
            }
            if ( !infos.contains(def) ) {
                throw new IllegalArgumentException("OperationOption "+def.getName()+
                        " already removed for operation "+op.getName());
            }
            infos.remove(def);
        }
    }
        
    /**
     * Clears the operation-specific supported classes. Normally, when
     * you add an ObjectClass, using {@link #defineObjectClass(ObjectClassInfo)},
     * it is added to all operations. You may then remove those that you need
     * using {@link #removeSupportedObjectClass(Class, ObjectClassInfo)}. You
     * may wish, as an alternative to clear everything out and instead add using
     * {@link #addSupportedObjectClass(Class, ObjectClassInfo)}. 
     */
    public void clearSupportedObjectClassesByOperation() {
        for (Set<ObjectClassInfo> values : 
            _supportedObjectClassesByOperation.values())
        {
            values.clear();
        }
    }
    
    /**
     * Clears the operation-specific supported options. 
     * Normally, when you add an OperationOptionInfo 
     * using {@link #defineOperationOption(OperationOptionInfo)},
     * this adds the option to all operations. You may then remove the option from any operation
     * that does not support the option
     * using {@link #removeSupportedOperationOption(Class, OperationOptionInfo)}. 
     * An alternative approach is to clear everything out (using this method) 
     * and then add each option to every operation that supports the option 
     * using {@link #addSupportedOperationOption(Class, OperationOptionInfo)}. 
     */
    public void clearSupportedOptionsByOperation() {
        for (Set<OperationOptionInfo> values : 
            _supportedOptionsByOperation.values())
        {
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
        if (_declaredObjectClasses.isEmpty()) {
            final String ERR = "Must be at least one ObjectClassInfo object!";
            throw new IllegalStateException(ERR);
        }
        return new Schema(_declaredObjectClasses,
                _declaredOperationOptions,
                _supportedObjectClassesByOperation,
                _supportedOptionsByOperation);
    }
}
