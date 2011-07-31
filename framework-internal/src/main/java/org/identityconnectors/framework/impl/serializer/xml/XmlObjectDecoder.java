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

import org.identityconnectors.common.Base64;
import org.identityconnectors.common.XmlUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.serializer.ObjectDecoder;
import org.identityconnectors.framework.impl.serializer.ObjectSerializationHandler;
import org.identityconnectors.framework.impl.serializer.ObjectSerializerRegistry;
import org.identityconnectors.framework.impl.serializer.ObjectTypeMapper;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;


public class XmlObjectDecoder implements ObjectDecoder {

    private final Element _node;
    private final Class<?> _expectedClass;
    
    public XmlObjectDecoder(Element node, Class<?> expectedClass) {
        _node = node;
        _expectedClass = expectedClass;
    }
    
    public Object readObject() {
        return readObjectInternal();
    }
    
    public boolean readBooleanContents() {
        return decodeBoolean(readStringContentsInternal());
    }

    public boolean readBooleanField(String fieldName, boolean dflt) {
        return decodeBoolean(readStringAttributeInternal(fieldName,XmlObjectEncoder.encodeBoolean(dflt)));
    }

    public byte[] readByteArrayContents() {
        return decodeByteArray(readStringContentsInternal());
    }

    public Class<?> readClassContents() {
        return decodeClass(readStringContentsInternal());
    }
    
    public Class<?> readClassField(String name, Class<?> dflt) {
        String val = readStringAttributeInternal(name, null);
        if ( val == null ) {
            return dflt;
        }
        else {
            return decodeClass(val);
        }
    }

    public double readDoubleContents() {
        return decodeDouble(readStringContentsInternal());
    }

    public double readDoubleField(String fieldName, double dflt) {
        return decodeDouble(readStringAttributeInternal(fieldName,XmlObjectEncoder.encodeDouble(dflt)));
    }

    public float readFloatContents() {
        return decodeFloat(readStringContentsInternal());
    }

    public float readFloatField(String fieldName, float dflt) {
        return decodeFloat(readStringAttributeInternal(fieldName,XmlObjectEncoder.encodeFloat(dflt)));
    }

    public int readIntContents() {
        return decodeInt(readStringContentsInternal());
    }

    public int readIntField(String fieldName, int dflt) {
        return decodeInt(readStringAttributeInternal(fieldName,XmlObjectEncoder.encodeInt(dflt)));
    }

    public long readLongContents() {
        return decodeLong(readStringContentsInternal());
    }

    public long readLongField(String fieldName, long dflt) {
        return decodeLong(readStringAttributeInternal(fieldName,XmlObjectEncoder.encodeLong(dflt)));
    }
    
    public int getNumSubObjects() {
        int count = 0;
        for (Element subElement = XmlUtil.getFirstChildElement(_node);
             subElement != null;
             subElement = XmlUtil.getNextElement(subElement)) {
            count++;
        }
        return count;
    }

    public Object readObjectContents(int index) {
        
        Element subElement = XmlUtil.getFirstChildElement(_node);
        for ( int i = 0; i < index; i++) {
            subElement = XmlUtil.getNextElement(subElement);
        }
        
        if ( subElement == null ) {
            throw new ConnectorException("Missing subelement number: "+index);
        }
        
        return new XmlObjectDecoder(subElement,null).readObject();
    }

    public Object readObjectField(String fieldName, Class<?> expected, Object dflt) {
        Element child = XmlUtil.findImmediateChildElement(_node, fieldName);
        if ( child == null ) {
            return dflt;
        }
        if ( expected != null ) {
            return new XmlObjectDecoder(child,expected).readObject();
        }
        Element subElement = XmlUtil.getFirstChildElement(child);
        if ( subElement == null ) {
            return dflt;
        }
        //if they specify null, don't apply defaults
        return new XmlObjectDecoder(subElement,null).readObject();    
    }

    public String readStringContents() {
        String rv = readStringContentsInternal();
        return rv == null ? "" : rv;
    }
    
    public String readStringField(String fieldName, String dflt) {
        return readStringAttributeInternal(fieldName, dflt);
    }
    
    private String readStringContentsInternal() {
        String xml = XmlUtil.getContent(_node);
        return xml; 
    }
    
    private String readStringAttributeInternal(String name, String dflt) {
        Attr attr = _node.getAttributeNode(name);
        if ( attr == null ) {
            return dflt;
        }
        return attr.getValue();
    }
    
    private boolean decodeBoolean(String v) {
        return Boolean.parseBoolean(v);
    }
    
    private byte [] decodeByteArray(String base64) {
        return Base64.decode(base64);
    }
        
    private Class<?> decodeClass(String type) {
        if ( type.endsWith("[]") ) {
            String componentName = type.substring(0,type.length()-"[]".length());
            Class<?> componentClass =
                decodeClass(componentName);
            Class<?> arrayClass =
                Array.newInstance(componentClass, 0).getClass();
            return arrayClass;            
        }
        else {            
            ObjectTypeMapper mapper =
                ObjectSerializerRegistry.getMapperBySerialType(type);
            if ( mapper == null ) {
                throw new ConnectorException("No deserializer for type: "+type);
            }
            Class<?> clazz = mapper.getHandledObjectType();
            return clazz;
        }
    }
    
    private double decodeDouble(String val) {
        return Double.parseDouble(val);
    }
    
    private float decodeFloat(String val) {
        return Float.parseFloat(val);
    }
    
    private int decodeInt(String val) {
        return Integer.parseInt(val);
    }
    
    private long decodeLong(String val) {
        return Long.parseLong(val);
    }
    
    private Object readObjectInternal() {
        if (_expectedClass != null) {
            ObjectSerializationHandler handler =
                ObjectSerializerRegistry.getHandlerByObjectType(_expectedClass);
            if ( handler == null ) {
                if (_expectedClass.isArray()) {
                    List<Object> temp = new ArrayList<Object>();
                    for (Element child = XmlUtil.getFirstChildElement(_node); child != null;
                         child = XmlUtil.getNextElement(child)) {
                        XmlObjectDecoder sub = new XmlObjectDecoder(child,null);
                        Object obj = sub.readObject();
                        temp.add(obj);
                    }
                    int length = temp.size();
                    Object array = Array.newInstance(_expectedClass.getComponentType(), 
                            length);
                    for ( int i = 0; i < length; i++) {
                        Object element = temp.get(i);
                        Array.set(array, i, element);
                    }
                    return array;                    
                }
                else {
                    throw new ConnectorException("No deserializer for type: "+_expectedClass);
                }
            }
            else {
                return handler.deserialize(this);
            }            
        }
        else if ( _node.getTagName().equals("null") ) {
            return null;
        }
        else if (_node.getTagName().equals("Array")) {
            String componentType = XmlUtil.getAttribute(_node, "componentType");
            if ( componentType == null ) {
                componentType = "Object";
            }
            Class<?> componentClass = decodeClass(componentType);
            List<Object> temp = new ArrayList<Object>();
            for (Element child = XmlUtil.getFirstChildElement(_node); child != null;
                 child = XmlUtil.getNextElement(child)) {
                XmlObjectDecoder sub = new XmlObjectDecoder(child,null);
                Object obj = sub.readObject();
                temp.add(obj);
            }
            int length = temp.size();
            Object array = Array.newInstance(componentClass, 
                    length);
            for ( int i = 0; i < length; i++) {
                Object element = temp.get(i);
                Array.set(array, i, element);
            }
            return array;
        }
        else {
            Class<?> clazz =
                decodeClass(_node.getTagName());
            ObjectSerializationHandler handler =
                ObjectSerializerRegistry.getHandlerByObjectType(clazz);
            if ( handler == null ) {
                throw new ConnectorException("No deserializer for type: "+clazz);
            }
            else {
                return handler.deserialize(this);
            }
        }
    }

}
