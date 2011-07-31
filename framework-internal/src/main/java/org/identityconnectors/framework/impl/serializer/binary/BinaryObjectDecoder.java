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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.BinaryObjectDeserializer;
import org.identityconnectors.framework.impl.serializer.ObjectDecoder;
import org.identityconnectors.framework.impl.serializer.ObjectSerializationHandler;
import org.identityconnectors.framework.impl.serializer.ObjectSerializerRegistry;
import org.identityconnectors.framework.impl.serializer.ObjectTypeMapper;


public class BinaryObjectDecoder implements ObjectDecoder, BinaryObjectDeserializer {
    
    private static class ReadState {
        public Map<String,byte[]> objectFields = new HashMap<String,byte[]>();
        public List<byte[]> anonymousFields = new ArrayList<byte[]>();
        public DataInputStream currentInput;
        public ReadState() {
        }
        public boolean startField(String name) {
            currentInput = null;
            byte [] content = objectFields.get(name);
            if ( content == null ) {
                return false;
            }
            else {
                currentInput = new DataInputStream(new ByteArrayInputStream(content));
                return true;
            }
        }
        public void startAnonymousField(int index) {
            if ( index >= anonymousFields.size() ) {
                throw new ConnectorException("Anonymous content not found");
            }
            currentInput = new DataInputStream(new ByteArrayInputStream(anonymousFields.get(index)));            
        }
    }
    
    private static class InternalDecoder {
        
        private boolean _firstObject = true;
        
        private final Map<Integer,String> _constantPool =
            new HashMap<Integer,String>();

        
        private final List<ReadState> _readStateStack = new ArrayList<ReadState>();
        private final DataInputStream _rootInput;
        
        public InternalDecoder(DataInputStream input) {
            _rootInput = input;
        }
                
        public Object readObject(ObjectDecoder decoder) {
            
            if (_firstObject) {
                int magic = readInt();
                if ( magic != BinaryObjectEncoder.OBJECT_MAGIC) {
                    throw new ConnectorException("Bad magic number: "+magic);
                }
                int version = readInt();
                if ( version != BinaryObjectEncoder.ENCODING_VERSION ) {
                    throw new ConnectorException("Unexpected version: "+version);
                }
                _firstObject = false;
            }
            
            //if it's a top-level object, it's proceeded by a constant pool
            if (_readStateStack.size() == 0)
            {
                int size = readInt();
                for ( int i = 0; i < size; i++ ) {
                    String constant = readString(false);
                    int code = readInt();
                    _constantPool.put(code, constant);
                }
            }
                            
            Class<?> clazz = readClass();
            ReadState state = new ReadState();

            while(true) {
                byte type = readByte();
                if ( type == BinaryObjectEncoder.FIELD_TYPE_END_OBJECT ) {
                    break;
                }
                else if ( type == BinaryObjectEncoder.FIELD_TYPE_ANONYMOUS_FIELD ) {
                    byte [] bytes = readByteArray();
                    state.anonymousFields.add(bytes);
                }
                else if ( type == BinaryObjectEncoder.FIELD_TYPE_NAMED_FIELD ) {
                    String fieldName = readString(true);
                    byte [] bytes = readByteArray();
                    state.objectFields.put(fieldName, bytes);                    
                }
                else {
                    throw new ConnectorException("Unknown type: "+type);
                }
            }
            
            _readStateStack.add(state); //push the state on the stack before we read the body
            
            
            Object rv;
            if ( clazz == null ) {
                rv = null;
            }
            else {
                ObjectSerializationHandler handler =
                    ObjectSerializerRegistry.getHandlerByObjectType(clazz);
                if ( handler == null ) {
                    //we may have special handlers for certain types of arrays
                    //if handler is null, treat like any other array
                    if ( clazz.isArray() ) {
                        int length = getNumAnonymousFields();
                        Object array = Array.newInstance(clazz.getComponentType(), 
                                length);
                        for ( int i = 0; i < length; i++) {
                            startAnonymousField(i);
                            Object element = readObject(decoder);
                            Array.set(array, i, element);
                        }
                        rv = array;
                    }
                    else {
                        throw new ConnectorException("No deserializer for type: "+clazz);
                    }
                }
                else {
                    rv = handler.deserialize(decoder);
                }
            }
            _readStateStack.remove(_readStateStack.size()-1); //pop
            return rv;
        }
        
        public Class<?> readClass() {
            int type = readByte();
            if ( type == BinaryObjectEncoder.OBJECT_TYPE_NULL ) {
                return null;
            }
            else if ( type == BinaryObjectEncoder.OBJECT_TYPE_ARRAY ) {
                Class<?> componentClass = readClass();
                return Array.newInstance(componentClass, 0).getClass();
            }
            else if ( type == BinaryObjectEncoder.OBJECT_TYPE_CLASS ) {
                String typeName = readString(true);
                ObjectTypeMapper mapper =
                    ObjectSerializerRegistry.getMapperBySerialType(typeName);
                if ( mapper == null ) {
                    throw new ConnectorException("No deserializer for type: "+typeName);
                }
                return mapper.getHandledObjectType();
            }
            else {
                throw new ConnectorException("Bad type value: "+type);
            }
        }
                
        
        
        public int getNumAnonymousFields() {
            ReadState readState = _readStateStack.get(_readStateStack.size()-1);
            return readState.anonymousFields.size();
        }
        
        public void startAnonymousField(int index) {
            ReadState readState = _readStateStack.get(_readStateStack.size()-1);
            readState.startAnonymousField(index);
        }
        
        public boolean startField(String name) {
            ReadState readState = _readStateStack.get(_readStateStack.size()-1);
            return readState.startField(name);
        }
        
        public int readInt() {
            try {
                return getCurrentInput().readInt();
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }

        public long readLong() {
            try {
                return getCurrentInput().readLong();
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }

        public double readDouble() {
            try {
                return getCurrentInput().readDouble();
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }

        public byte [] readByteArray() {
            try {
                int length = getCurrentInput().readInt();
                byte [] rv = new byte[length];
                getCurrentInput().readFully(rv);
                return rv;
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }        
        }
        
        public byte readByte() {
            try {
                return getCurrentInput().readByte();
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }        
        }

        public boolean readBoolean() {
            try {
                return getCurrentInput().readBoolean();
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);            
            }
        }
                
        public String readString(boolean interned) {
            if (interned) {
                int code = readInt();
                String name = _constantPool.get(code);
                if ( name == null ) {
                    throw new ConnectorException("Undeclared code: "+code);
                }
                return name;
            }

            try {
                byte [] bytes = readByteArray();
                return new String(bytes,"UTF8");
            }
            catch (IOException e) {
                throw ConnectorException.wrap(e);
            }
        }
        
        private DataInputStream getCurrentInput() {
            if (_readStateStack.size() > 0) {
                ReadState state = _readStateStack.get(_readStateStack.size()-1);
                return state.currentInput;
            }
            else {
                return _rootInput;
            }
        }
    }

    
    private InternalDecoder _internalDecoder;    
    
    public BinaryObjectDecoder(InputStream in) {
        _internalDecoder = new InternalDecoder(new DataInputStream(new BufferedInputStream(in,4096)));
    }
        
    public void close() {
        try {
            _internalDecoder._rootInput.close();
        }
        catch (IOException e) {
            throw ConnectorException.wrap(e);
        }
    }
    
    public Object readObject() {
        return _internalDecoder.readObject(this);
    }
    
    public boolean readBooleanContents() {
        _internalDecoder.startAnonymousField(0);
        return _internalDecoder.readBoolean();
    }

    public boolean readBooleanField(String fieldName, boolean dflt) {
        if ( _internalDecoder.startField(fieldName) ) {
            return _internalDecoder.readBoolean();
        }
        else {
            return dflt;
        }
    }

    public byte[] readByteArrayContents() {
        _internalDecoder.startAnonymousField(0);
        return _internalDecoder.readByteArray();
    }
    
    public Class<?> readClassContents() {
        _internalDecoder.startAnonymousField(0);
        return _internalDecoder.readClass();
    }
    
    public Class<?> readClassField(String fieldName, Class<?> dflt) {
        if ( _internalDecoder.startField(fieldName) ) {
            return _internalDecoder.readClass();
        }
        else {
            return dflt;
        }
    }

    public double readDoubleContents() {
        _internalDecoder.startAnonymousField(0);
        return _internalDecoder.readDouble();
    }

    public double readDoubleField(String fieldName, double dflt) {
        if ( _internalDecoder.startField(fieldName) ) {
            return _internalDecoder.readDouble();
        }
        else {
            return dflt;
        }
    }

    public float readFloatContents() {
        _internalDecoder.startAnonymousField(0);
        //read as double since C# only knows how to deal with that
        return (float)_internalDecoder.readDouble();
    }

    public float readFloatField(String fieldName, float dflt) {
        if ( _internalDecoder.startField(fieldName) ) {
            return (float)_internalDecoder.readDouble();
        }
        else {
            return dflt;
        }
    }

    public int readIntContents() {
        _internalDecoder.startAnonymousField(0);
        return _internalDecoder.readInt();
    }

    public int readIntField(String fieldName, int dflt) {
        if ( _internalDecoder.startField(fieldName) ) {
            return _internalDecoder.readInt();
        }
        else {
            return dflt;
        }
    }

    public long readLongContents() {
        _internalDecoder.startAnonymousField(0);
        return _internalDecoder.readLong();
    }

    public long readLongField(String fieldName, long dflt) {
        if ( _internalDecoder.startField(fieldName) ) {
            return _internalDecoder.readLong();
        }
        else {
            return dflt;
        }
    }

    public int getNumSubObjects() {
        return _internalDecoder.getNumAnonymousFields();
    }
    
    public Object readObjectContents(int index) {
        _internalDecoder.startAnonymousField(index);
        return _internalDecoder.readObject(this);
    }
    
    public Object readObjectField(String fieldName, Class<?> expected, Object dflt) {
        if ( _internalDecoder.startField(fieldName) ) {
            return readObject();
        }
        else {
            return dflt;
        }
    }

    public String readStringContents() {
        _internalDecoder.startAnonymousField(0);
        return _internalDecoder.readString(false);
    }
    
    public String readStringField(String fieldName, String dflt) {
        if ( _internalDecoder.startField(fieldName) ) {
            return _internalDecoder.readString(false);
        }
        else {
            return dflt;
        }
    }
    

    
}
