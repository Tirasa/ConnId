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
package org.identityconnectors.framework.common.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.InputSource;

/**
 * Bag of utilities for serialization
 */
public final class SerializerUtil {

    private SerializerUtil() {
        
    }
        
    /**
     * Serializes the given object to bytes
     * @param object The object to serialize
     * @return The bytes
     * @see ObjectSerializerFactory for a list of supported types
     */
    public static byte [] serializeBinaryObject(Object object) {
        ObjectSerializerFactory fact = ObjectSerializerFactory.getInstance();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryObjectSerializer ser = fact.newBinarySerializer(baos);
        ser.writeObject(object);
        ser.close();
        return baos.toByteArray();
    }

    /**
     * Deserializes the given object from bytes
     * @param bytes The bytes to deserialize
     * @return The object
     * @see ObjectSerializerFactory for a list of supported types
     */
    public static Object deserializeBinaryObject(byte [] bytes) {
        ObjectSerializerFactory fact = ObjectSerializerFactory.getInstance();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BinaryObjectDeserializer des = fact.newBinaryDeserializer(in);
        return des.readObject();
    }
    
    /**
     * Serializes the given object to xml
     * @param object The object to serialize
     * @param includeHeader True if we are to include the xml header.
     * @return The xml
     * @see ObjectSerializerFactory for a list of supported types
     */
    public static String serializeXmlObject(Object object, boolean includeHeader) {
        ObjectSerializerFactory fact = ObjectSerializerFactory.getInstance();
        StringWriter w = new StringWriter();
        XmlObjectSerializer ser = fact.newXmlSerializer(w, includeHeader, false);
        ser.writeObject(object);
        ser.close(true);
        return w.toString();
    }

    /**
     * Deserializes the given object from xml
     * @param str The xml to deserialize
     * @param validate True if we are to validate the xml
     * @return The object
     * @see ObjectSerializerFactory for a list of supported types
     */
    public static Object deserializeXmlObject(String str, boolean validate) {
        ObjectSerializerFactory fact = ObjectSerializerFactory.getInstance();
        InputSource source = new InputSource(new StringReader(str));
        final List<Object> rv = new ArrayList<Object>();
        fact.deserializeXmlStream(source, 
                new XmlObjectResultsHandler() {
                public boolean handle(Object o) {
                    rv.add(o);
                    return false;
                }
        },validate);
        if ( rv.size() > 0 ) {
            return rv.get(0);
        }
        else {
            return null;
        }
    }
    
    /**
     * Clones the given object by serializing it to bytes and then
     * deserializing it.
     * @param object The object.
     * @return A clone of the object
     */
    public static Object cloneObject(Object object) {
        byte [] bytes = serializeBinaryObject(object);
        return deserializeBinaryObject(bytes);
    }

}
