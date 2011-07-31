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
package org.identityconnectors.framework.impl.serializer.xml;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.Base64;
import org.identityconnectors.common.XmlUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.serializer.ObjectEncoder;
import org.identityconnectors.framework.impl.serializer.ObjectSerializationHandler;
import org.identityconnectors.framework.impl.serializer.ObjectSerializerRegistry;
import org.identityconnectors.framework.impl.serializer.ObjectTypeMapper;


public class XmlObjectEncoder implements ObjectEncoder {

    private static class OutputElement {
        private final String name;
        private final StringBuilder contents = new StringBuilder();
        private boolean elementData = false;
        public OutputElement(String name) {
            this.name = name;
        }
    }
    
    private List<OutputElement> _outputStack = new ArrayList<OutputElement>();
    
    private StringBuilder _rootBuilder;
    
    public XmlObjectEncoder(StringBuilder builder) {
        Assertions.nullCheck(builder, "builder");
        _rootBuilder = builder;
    }
    
    public String writeObject(Object o) {
        return writeObjectInternal(o,false);
    }
    
    public void writeBooleanContents(boolean v) {
        writeStringContentsInternal(encodeBoolean(v));
    }

    public void writeBooleanField(String fieldName, boolean v) {
        writeAttributeInternal(fieldName,encodeBoolean(v));
    }

    public void writeByteArrayContents(byte[] v) {
        writeStringContentsInternal(encodeByteArray(v));
    }

    public void writeClassContents(Class<?> v) {
        writeStringContentsInternal(encodeClass(v));
    }
    
    public void writeClassField(String name, Class<?> v) {
        if ( v != null ) {
            writeAttributeInternal(name, encodeClass(v));
        }
    }

    public void writeDoubleContents(double v) {
        writeStringContentsInternal(encodeDouble(v));
    }

    public void writeDoubleField(String fieldName, double v) {
        writeAttributeInternal(fieldName,encodeDouble(v));
    }

    public void writeFloatContents(float v) {
        writeStringContentsInternal(encodeFloat(v));
    }

    public void writeFloatField(String fieldName, float v) {
        writeAttributeInternal(fieldName,encodeFloat(v));
    }

    public void writeIntContents(int v) {
        writeStringContentsInternal(encodeInt(v));
    }

    public void writeIntField(String fieldName, int v) {
        writeAttributeInternal(fieldName,encodeInt(v));
    }

    public void writeLongContents(long v) {
        writeStringContentsInternal(encodeLong(v));
    }

    public void writeLongField(String fieldName, long v) {
        writeAttributeInternal(fieldName,encodeLong(v));
    }

    public void writeObjectContents(Object o) {
        if (_outputStack.size() == 0) {
            throw new IllegalStateException("May not write contents on top-level object");
        }
        writeObjectInternal(o,false);
    }

    public void writeObjectField(String fieldName, Object object, boolean inline) {
        if (_outputStack.size() == 0) {
            throw new IllegalStateException("May not write field on top-level object");
        }
        if (inline && object == null)
        {
            return; //don't write anything
        }
        beginElement(fieldName);
        writeObjectInternal(object,inline);
        endElement();
    }

    public void writeStringContents(String str) {
        writeStringContentsInternal(str);
    }
    
    public void writeStringField(String fieldName, String str) {
        if ( str != null ) {
            writeAttributeInternal(fieldName, str);
        }
    }
    
    static String encodeBoolean(boolean b) {
        return String.valueOf(b);
    }
    
    private static String encodeByteArray(byte [] bytes) {
        return Base64.encode(bytes);
    }
        
    private static String encodeClass(Class<?> clazz) {
        ObjectSerializationHandler handler =
            ObjectSerializerRegistry.getHandlerByObjectType(clazz);
        ObjectTypeMapper mapper =
            ObjectSerializerRegistry.getMapperByObjectType(clazz);
        if ( handler == null && clazz.isArray() ) {
            //we may have special handlers for certain types of arrays
            //if handler is null, treat like any other array
            return encodeClass(clazz.getComponentType())+"[]";
        }
        else if ( mapper == null ) {
            throw new ConnectorException("No serializer for class: "+clazz);
        }
        else {
            String typeName = mapper.getHandledSerialType();
            return typeName;
        }
    }
    
    static String encodeDouble(double d) {
        return String.valueOf(d);
    }
    
    static String encodeFloat(float d) {
        return String.valueOf(d);
    }
    
    static String encodeInt(int d) {
        return String.valueOf(d);
    }
    
    static String encodeLong(long d) {
        return String.valueOf(d);
    }
    
    /**
     * Writes the object
     * @param object
     * @param inline
     * @return The type name (regardless of whether it was inlined)
     */
    String writeObjectInternal(Object object, boolean inline) {
        if ( object == null ) {
            if ( inline ) {
                throw new IllegalArgumentException("null cannot be inlined");
            }
            beginElement("null");
            endElement();
            return "null";
        }
        else {
            Class<?> clazz = object.getClass();
            ObjectSerializationHandler handler =
                ObjectSerializerRegistry.getHandlerByObjectType(clazz);
            if ( handler == null ) {
                //we may have special handlers for certain types of arrays
                //if handler is null, treat like any other array
                if ( clazz.isArray() ) {
                    if (!inline) {
                        String componentTypeName = encodeClass(clazz.getComponentType());
                        beginElement("Array");
                        writeAttributeInternal("componentType", componentTypeName);
                    }
                    int length = Array.getLength(object);
                    for ( int i = 0; i < length; i++ ) {
                        Object val = Array.get(object, i);
                        writeObjectInternal(val,false);
                    }
                    if (!inline) {
                        endElement();
                    }
                    return "Array";
                }
                else {
                    throw new ConnectorException("No serializer for class: "+clazz); 
                }
            }
            else {
                String typeName = encodeClass(clazz);
                if (!inline) {
                    beginElement(typeName);
                }
                handler.serialize(object, this);
                if (!inline) {
                    endElement();
                }
                return typeName;
            }
        }

    }
    
    //////////////////////////////////////////////////////////////////
    //
    // xml encoding
    //
    /////////////////////////////////////////////////////////////////

    private OutputElement getCurrentElement() {
        if (_outputStack.size() == 0) {
            return null;
        }
        else {
            return _outputStack.get(_outputStack.size()-1);
        }
    }
    
    private StringBuilder getCurrentBuilder() {
        if (_outputStack.size() == 0) {
            return _rootBuilder;
        }
        else {
            return getCurrentElement().contents;
        }
    }
    
    private StringBuilder getPreviousBuilder() {
        if (_outputStack.size() == 0) {
            return null;
        }
        else if (_outputStack.size() == 1) {
            return _rootBuilder;
        }
        else {
            return _outputStack.get(_outputStack.size()-2).contents;
        }
    }
    
    private void beginElement(String name) {
        indent(getCurrentBuilder(),_outputStack.size());
        OutputElement current = getCurrentElement();
        if ( current != null ) {
            current.elementData = true;
        }
        getCurrentBuilder().append("<"+name);
        _outputStack.add(new OutputElement(name));
    }
        
    private void endElement() {
        OutputElement endedElement = _outputStack.remove(_outputStack.size()-1);
        String contents = endedElement.contents.toString();
        StringBuilder currentBuilder = getCurrentBuilder();
        if ( contents.length() == 0 ) {
            currentBuilder.append("/>\n"); //empty element           		
        }
        else {
            currentBuilder.append(">");
            if ( endedElement.elementData ) {
                currentBuilder.append("\n");
            }
            getCurrentBuilder().append(contents);
            if ( endedElement.elementData ) {
                indent(currentBuilder, _outputStack.size());
            }
            currentBuilder.append("</").append(endedElement.name).append(">\n");
        }
    }
    private void writeAttributeInternal(String fieldName, String str) {
        StringBuilder previousBuilder = getPreviousBuilder();
        previousBuilder.append(" ").append(fieldName).append("='");
        XmlUtil.escape(previousBuilder, str, XmlUtil.SINGLE_QUOTE);
        previousBuilder.append("'");
    }
    private void writeStringContentsInternal(String str) {
        StringBuilder builder = getCurrentBuilder();
        XmlUtil.escape(builder, str, XmlUtil.NO_DELIM);
    }
    private void indent(StringBuilder builder, int level) {
        for (int i = 0; i < level*2; i++ ) {
            builder.append(" ");
        }
    }

}
