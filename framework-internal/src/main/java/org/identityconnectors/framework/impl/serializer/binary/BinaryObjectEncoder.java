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
package org.identityconnectors.framework.impl.serializer.binary;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.identityconnectors.common.Pair;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.BinaryObjectSerializer;
import org.identityconnectors.framework.impl.serializer.ObjectEncoder;
import org.identityconnectors.framework.impl.serializer.ObjectSerializationHandler;
import org.identityconnectors.framework.impl.serializer.ObjectSerializerRegistry;
import org.identityconnectors.framework.impl.serializer.ObjectTypeMapper;


public class BinaryObjectEncoder implements ObjectEncoder, BinaryObjectSerializer {
    
    /**
     * Version for the overall encoding - if we need
     * to change anything in the encoder, we will need
     * to bump this and handle appropriately
     */
    public static final int ENCODING_VERSION = 1;
    
    public static final int OBJECT_MAGIC = 0xFAFB;
    
    public static final byte OBJECT_TYPE_NULL                 = 60;
    public static final byte OBJECT_TYPE_CLASS                = 61;
    public static final byte OBJECT_TYPE_ARRAY                = 62;
    
    public static final byte FIELD_TYPE_ANONYMOUS_FIELD      = 70;
    public static final byte FIELD_TYPE_NAMED_FIELD          = 71;
    public static final byte FIELD_TYPE_END_OBJECT           = 72;
    
    private static class OutputBuffer extends Pair<ByteArrayOutputStream,DataOutputStream> {
        public OutputBuffer(ByteArrayOutputStream buf,
                DataOutputStream data) {
            super(buf,data);
        }
    }
    
    private static class InternalEncoder {

        /**
         * Mapping from type name to the ID we serialize so we only have to 
         */
        private Map<String,Integer> _constantPool =
            new HashMap<String,Integer>();
        
        private List<String> _constantBuffer = new ArrayList<String>();
        
        private List<OutputBuffer> _outputBufferStack = new ArrayList<OutputBuffer>();
        private DataOutputStream _rootOutput;
        private boolean _firstObject = true;
        
        public InternalEncoder(DataOutputStream output) {
            _rootOutput = output;
        }
                
        public void writeObject(ObjectEncoder encoder, Object object) {
            
            if (_firstObject) {
                writeInt(OBJECT_MAGIC);
                writeInt(ENCODING_VERSION);
                _firstObject = false;
            }
            
            //push the stack
            OutputBuffer objectBuffer;
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream data = new DataOutputStream(baos);
                objectBuffer = new OutputBuffer(baos,data);
                _outputBufferStack.add(objectBuffer);
            }
            
            if ( object == null ) {
                writeByte(OBJECT_TYPE_NULL);
            }
            else {
                Class<?> clazz = object.getClass();
                writeClass(clazz); 
                ObjectSerializationHandler handler =
                    ObjectSerializerRegistry.getHandlerByObjectType(clazz);
                if ( handler == null ) {
                    //we may have special handlers for certain types of arrays
                    //if handler is null, treat like any other array
                    if ( clazz.isArray() ) {
                        int length = Array.getLength(object);
                        for ( int i = 0; i < length; i++ ) {
                            Object val = Array.get(object, i);
                            startAnonymousField();
                            writeObject(encoder,val);
                            endField();
                        }
                    }
                    else {
                        throw new ConnectorException("No serializer for class: "+clazz); 
                    }
                }
                else {
                    handler.serialize(object, encoder);
                }
            }
            writeByte(FIELD_TYPE_END_OBJECT); //write end-object into the current obj buffer
            
            //pop the stack
            _outputBufferStack.remove(_outputBufferStack.size()-1);
            
            //it's a top-level object, flush the constant pool
            if (_outputBufferStack.size() == 0) {
                writeInt(_constantBuffer.size());
                for (String constant : _constantBuffer) {
                    writeString(constant,false);
                    writeInt(_constantPool.get(constant));
                }                
                _constantBuffer.clear();
            }
            
            //now write the actual object
            try {
                objectBuffer.second.close();
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
            byte [] bytes = objectBuffer.first.toByteArray();
            writeBytes(bytes);            
        }
        
        public void writeClass(Class<?> clazz) 
        {
            ObjectSerializationHandler handler =
                ObjectSerializerRegistry.getHandlerByObjectType(clazz);
            ObjectTypeMapper mapper =
                ObjectSerializerRegistry.getMapperByObjectType(clazz);
            if ( handler == null && clazz.isArray() ) {
                //we may have special handlers for certain types of arrays
                //if handler is null, treat like any other array
                writeByte(OBJECT_TYPE_ARRAY);
                writeClass(clazz.getComponentType());
            }
            else if ( mapper == null ) {
                throw new ConnectorException("No serializer for class: "+clazz);
            }
            else {
                String typeName = mapper.getHandledSerialType();
                writeByte(OBJECT_TYPE_CLASS);
                writeString(typeName,true);
            }
        }
        
        public void startAnonymousField() {
            writeByte(FIELD_TYPE_ANONYMOUS_FIELD);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(buf);
            _outputBufferStack.add(new OutputBuffer(buf,data));            
        }
        
        
        public void startField(String name) {
            writeByte(FIELD_TYPE_NAMED_FIELD);
            writeString(name,true);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(buf);
            _outputBufferStack.add(new OutputBuffer(buf,data));            
        }
        
        public void endField() {
            OutputBuffer buf = _outputBufferStack.remove(_outputBufferStack.size()-1);
            try {
                buf.second.close();
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
            byte [] bytes = buf.first.toByteArray();
            writeByteArray(bytes);
        }
                
        public void writeInt(int v) 

        {
            try {
                getCurrentOutput().writeInt(v);
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }
        
        public void writeLong(long v) 

        {
            try {
                getCurrentOutput().writeLong(v);
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }

        public void writeDouble(double l) 

        {
            try {
                getCurrentOutput().writeDouble(l);
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }
                
        public void writeByteArray(byte[] v) 

        {
            try {
                getCurrentOutput().writeInt(v.length);
                getCurrentOutput().write(v);      
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }
        
        public void writeByte(byte b) 

        {
            try {
                getCurrentOutput().writeByte(b);
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }

        public void writeBoolean(boolean b)

        {
            try {
                getCurrentOutput().writeBoolean(b);
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);            
            }
        }
        
        public void writeString(String str, boolean intern) 

        {
            if ( intern ) {
                int code = internIdentifier(str);
                writeInt(code);
                return;
            }
            try {
                byte [] bytes = str.getBytes("UTF8");
                writeByteArray(bytes);
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }
        
        private void writeBytes( byte [] v ) 
        {
            try {
                getCurrentOutput().write(v);  
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }
        
        private int internIdentifier(String name) {
            Integer code = _constantPool.get(name);
            if ( code == null ) {
                code = _constantPool.size();
                _constantPool.put(name,code);
                _constantBuffer.add(name);
            }
            return code;
        }
        
        private DataOutputStream getCurrentOutput() {
            if (_outputBufferStack.size() == 0) {
                return _rootOutput;
            }
            else {
                OutputBuffer buf = _outputBufferStack.get(_outputBufferStack.size()-1);
                return buf.second;
            }
        }
    }
    
        
    private InternalEncoder _internalEncoder;
            
    
    public BinaryObjectEncoder(OutputStream output) {
        _internalEncoder = new InternalEncoder(new DataOutputStream(new BufferedOutputStream(output,4096)));
    }
    
        
    public void flush() {
        try {
            _internalEncoder._rootOutput.flush();
        }
        catch (IOException e) {
            throw ConnectorException.wrap(e);
        }
    }
    
    public void close() {
        flush();
        try {
            _internalEncoder._rootOutput.close();
        }
        catch (IOException e) {
            throw ConnectorException.wrap(e);
        }
        
    }
    
    public void writeObject(Object o) {
        _internalEncoder.writeObject(this, o);
    }

    public void writeBooleanContents(boolean v) {
        _internalEncoder.startAnonymousField();
        _internalEncoder.writeBoolean(v);
        _internalEncoder.endField();
    }

    public void writeBooleanField(String fieldName, boolean v) {
        _internalEncoder.startField(fieldName);
        _internalEncoder.writeBoolean(v);
        _internalEncoder.endField();
    }

    public void writeByteArrayContents(byte[] v) {
        _internalEncoder.startAnonymousField();
        _internalEncoder.writeByteArray(v);
        _internalEncoder.endField();
    }
    
    public void writeClassContents(Class<?> v) {
        _internalEncoder.startAnonymousField();
        _internalEncoder.writeClass(v);
        _internalEncoder.endField();
    }
    
    public void writeClassField(String fieldName, Class<?> v) {
        if ( v != null ) {
            _internalEncoder.startField(fieldName);
            _internalEncoder.writeClass(v);
            _internalEncoder.endField();
        }
    }
    
    public void writeDoubleContents(double v) {
        _internalEncoder.startAnonymousField();
        _internalEncoder.writeDouble(v);
        _internalEncoder.endField();
    }

    public void writeDoubleField(String fieldName, double v) {
        _internalEncoder.startField(fieldName);
        _internalEncoder.writeDouble(v);
        _internalEncoder.endField();
    }

    public void writeFloatContents(float v) {
        _internalEncoder.startAnonymousField();
        //write as double since C# only knows how to deal with that
        _internalEncoder.writeDouble((double)v);
        _internalEncoder.endField();
    }

    public void writeFloatField(String fieldName, float v) {
        _internalEncoder.startField(fieldName);
        //write as double since C# only knows how to deal with that
        _internalEncoder.writeDouble((double)v);
        _internalEncoder.endField();
    }

    public void writeIntContents(int v) {
        _internalEncoder.startAnonymousField();
        _internalEncoder.writeInt(v);
        _internalEncoder.endField();
    }

    public void writeIntField(String fieldName, int v) {
        _internalEncoder.startField(fieldName);
        _internalEncoder.writeInt(v);
        _internalEncoder.endField();
    }

    public void writeLongContents(long v) {
        _internalEncoder.startAnonymousField();
        _internalEncoder.writeLong(v);
        _internalEncoder.endField();
    }

    public void writeLongField(String fieldName, long v) {
        _internalEncoder.startField(fieldName);
        _internalEncoder.writeLong(v);
        _internalEncoder.endField();
    }
    
    public void writeObjectContents(Object object) {
        _internalEncoder.startAnonymousField();
        _internalEncoder.writeObject(this,object);
        _internalEncoder.endField();
    }

    public void writeObjectField(String fieldName, Object object, boolean inline) {
        _internalEncoder.startField(fieldName);
        _internalEncoder.writeObject(this,object);
        _internalEncoder.endField();
    }

    public void writeStringContents(String str) {
        _internalEncoder.startAnonymousField();
        _internalEncoder.writeString(str,false);
        _internalEncoder.endField();
    }
    
    public void writeStringField(String fieldName, String v) {
        if ( v != null ) {
            _internalEncoder.startField(fieldName);
            _internalEncoder.writeString(v,false);
            _internalEncoder.endField();
        }
    }

}
