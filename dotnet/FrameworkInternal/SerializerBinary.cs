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
using System;
using System.Collections.Generic;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Impl.Serializer;
using System.IO;
using System.Text;
namespace Org.IdentityConnectors.Framework.Impl.Serializer.Binary
{
    internal class InternalEncoder
    {
        /// <summary>
        /// Mapping from type name to the ID we serialize so we only have to
        /// </summary>
        private IDictionary<String, int> _constantPool =
            new Dictionary<String, int>();

        private IList<String> _constantBuffer = new List<String>();

        private IList<MemoryStream> _outputBufferStack = new List<MemoryStream>();
        internal Stream _rootOutput;
        private bool _firstObject = true;

        public InternalEncoder(Stream output)
        {
            _rootOutput = output;
        }

        public void WriteObject(ObjectEncoder encoder, Object obj)
        {

            if (_firstObject)
            {
                WriteInt(BinaryObjectEncoder.OBJECT_MAGIC);
                WriteInt(BinaryObjectEncoder.ENCODING_VERSION);
                _firstObject = false;
            }

            MemoryStream objectBuffer = new MemoryStream();
            _outputBufferStack.Add(objectBuffer);

            if (obj == null)
            {
                WriteByte(BinaryObjectEncoder.OBJECT_TYPE_NULL);
            }
            else
            {
                Type clazz = obj.GetType();
                WriteClass(clazz);
                ObjectSerializationHandler handler =
                    ObjectSerializerRegistry.GetHandlerByObjectType(clazz);
                if (handler == null)
                {
                    //we may have special handlers for certain types of arrays
                    //if handler is null, treat like any other array
                    if (obj is Array)
                    {
                        Array array = (Array)obj;
                        int length = array.Length;
                        for (int i = 0; i < length; i++)
                        {
                            Object val = array.GetValue(i);
                            StartAnonymousField();
                            WriteObject(encoder, val);
                            EndField();
                        }
                    }
                    else
                    {
                        throw new ConnectorException("No serializer for class: " + clazz);
                    }
                }
                else
                {
                    handler.Serialize(obj, encoder);
                }
            }

            //write end-object into the current obj buffer
            WriteByte(BinaryObjectEncoder.FIELD_TYPE_END_OBJECT);

            //pop the stack
            _outputBufferStack.RemoveAt(_outputBufferStack.Count - 1);

            //it's a top-level object, flush the constant pool
            if (_outputBufferStack.Count == 0)
            {
                WriteInt(_constantBuffer.Count);
                foreach (String constant in _constantBuffer)
                {
                    WriteString(constant, false);
                    WriteInt(_constantPool[constant]);
                }
                _constantBuffer.Clear();
            }

            //now write the actual object
            objectBuffer.Close();
            byte[] bytes = objectBuffer.ToArray();
            WriteBytes(bytes);
        }

        public void WriteClass(Type clazz)
        {
            ObjectSerializationHandler handler =
                ObjectSerializerRegistry.GetHandlerByObjectType(clazz);
            ObjectTypeMapper mapper =
                ObjectSerializerRegistry.GetMapperByObjectType(clazz);
            if (handler == null && clazz.IsArray)
            {
                //we may have special handlers for certain types of arrays
                //if handler is null, treat like any other array
                WriteByte(BinaryObjectEncoder.OBJECT_TYPE_ARRAY);
                WriteClass(clazz.GetElementType());
            }
            else if (mapper == null)
            {
                throw new ConnectorException("No serializer for class: " + clazz);
            }
            else
            {
                String typeName = mapper.HandledSerialType;
                WriteByte(BinaryObjectEncoder.OBJECT_TYPE_CLASS);
                WriteString(typeName, true);
            }
        }

        public void StartAnonymousField()
        {
            WriteByte(BinaryObjectEncoder.FIELD_TYPE_ANONYMOUS_FIELD);
            MemoryStream buf = new MemoryStream();
            _outputBufferStack.Add(buf);
        }

        public void StartField(String name)
        {
            WriteByte(BinaryObjectEncoder.FIELD_TYPE_NAMED_FIELD);
            WriteString(name, true);
            MemoryStream buf = new MemoryStream();
            _outputBufferStack.Add(buf);
        }

        public void EndField()
        {
            MemoryStream buf = _outputBufferStack[_outputBufferStack.Count - 1];
            _outputBufferStack.RemoveAt(_outputBufferStack.Count - 1);
            buf.Close();
            byte[] bytes = buf.ToArray();
            WriteByteArray(bytes);
        }

        public void WriteInt(int v)
        {
            Stream output = GetCurrentOutput();
            output.WriteByte((byte)(0xff & (v >> 24)));
            output.WriteByte((byte)(0xff & (v >> 16)));
            output.WriteByte((byte)(0xff & (v >> 8)));
            output.WriteByte((byte)(0xff & v));
        }

        public void WriteLong(long v)
        {
            Stream output = GetCurrentOutput();
            output.WriteByte((byte)(0xff & (v >> 56)));
            output.WriteByte((byte)(0xff & (v >> 48)));
            output.WriteByte((byte)(0xff & (v >> 40)));
            output.WriteByte((byte)(0xff & (v >> 32)));
            output.WriteByte((byte)(0xff & (v >> 24)));
            output.WriteByte((byte)(0xff & (v >> 16)));
            output.WriteByte((byte)(0xff & (v >> 8)));
            output.WriteByte((byte)(0xff & v));
        }

        public void WriteDouble(double l)
        {
            long val = BitConverter.DoubleToInt64Bits(l);
            WriteLong(val);
        }

        public void WriteByteArray(byte[] v)
        {
            WriteInt(v.Length);
            WriteBytes(v);
        }

        public void WriteByte(byte b)
        {
            GetCurrentOutput().WriteByte(b);
        }

        public void WriteBoolean(bool b)
        {
            WriteByte(b ? (byte)1 : (byte)0);
        }

        public void WriteString(String str, bool intern)
        {
            if (intern)
            {
                int code = InternIdentifier(str);
                WriteInt(code);
                return;
            }
            byte[] bytes = Encoding.UTF8.GetBytes(str);
            WriteByteArray(bytes);
        }

        private int InternIdentifier(String name)
        {
            int code = CollectionUtil.GetValue(_constantPool, name, -1);
            if (code == -1)
            {
                code = _constantPool.Count;
                _constantPool[name] = code;
                _constantBuffer.Add(name);
            }
            return code;
        }

        private void WriteBytes(byte[] v)
        {
            //only write if length > 0 - C# seems to have a problem with
            //zero-length byte arrays
            if (v.Length > 0)
            {
                GetCurrentOutput().Write(v, 0, v.Length);
            }
        }

        private Stream GetCurrentOutput()
        {
            if (_outputBufferStack.Count == 0)
            {
                return _rootOutput;
            }
            else
            {
                MemoryStream buf = _outputBufferStack[_outputBufferStack.Count - 1];
                return buf;
            }
        }
    }

    internal class BinaryObjectEncoder : ObjectEncoder, BinaryObjectSerializer
    {
        public const int ENCODING_VERSION = 1;

        public const int OBJECT_MAGIC = 0xFAFB;

        public const byte OBJECT_TYPE_NULL = 60;
        public const byte OBJECT_TYPE_CLASS = 61;
        public const byte OBJECT_TYPE_ARRAY = 62;

        public const byte FIELD_TYPE_ANONYMOUS_FIELD = 70;
        public const byte FIELD_TYPE_NAMED_FIELD = 71;
        public const byte FIELD_TYPE_END_OBJECT = 72;


        private InternalEncoder _internalEncoder;

        public BinaryObjectEncoder(Stream output)
        {
            _internalEncoder = new InternalEncoder(new BufferedStream(output, 4096));
        }

        public void Flush()
        {
            _internalEncoder._rootOutput.Flush();
        }

        public void Close()
        {
            _internalEncoder._rootOutput.Close();
        }

        public void WriteObject(Object o)
        {
            _internalEncoder.WriteObject(this, o);
        }

        public void WriteBooleanContents(bool v)
        {
            _internalEncoder.StartAnonymousField();
            _internalEncoder.WriteBoolean(v);
            _internalEncoder.EndField();
        }

        public void WriteBooleanField(String fieldName, bool v)
        {
            _internalEncoder.StartField(fieldName);
            _internalEncoder.WriteBoolean(v);
            _internalEncoder.EndField();
        }

        public void WriteByteArrayContents(byte[] v)
        {
            _internalEncoder.StartAnonymousField();
            _internalEncoder.WriteByteArray(v);
            _internalEncoder.EndField();
        }

        public void WriteClassContents(Type v)
        {
            _internalEncoder.StartAnonymousField();
            _internalEncoder.WriteClass(v);
            _internalEncoder.EndField();
        }

        public void WriteClassField(string fieldName, Type v)
        {
            if (v != null)
            {
                _internalEncoder.StartField(fieldName);
                _internalEncoder.WriteClass(v);
                _internalEncoder.EndField();
            }
        }

        public void WriteDoubleContents(double v)
        {
            _internalEncoder.StartAnonymousField();
            _internalEncoder.WriteDouble(v);
            _internalEncoder.EndField();
        }

        public void WriteDoubleField(String fieldName, double v)
        {
            _internalEncoder.StartField(fieldName);
            _internalEncoder.WriteDouble(v);
            _internalEncoder.EndField();
        }

        public void WriteFloatContents(float v)
        {
            _internalEncoder.StartAnonymousField();
            //write as double since C# only knows how to deal with that
            _internalEncoder.WriteDouble((double)v);
            _internalEncoder.EndField();
        }

        public void WriteFloatField(String fieldName, float v)
        {
            _internalEncoder.StartField(fieldName);
            //write as double since C# only knows how to deal with that
            _internalEncoder.WriteDouble((double)v);
            _internalEncoder.EndField();
        }

        public void WriteIntContents(int v)
        {
            _internalEncoder.StartAnonymousField();
            _internalEncoder.WriteInt(v);
            _internalEncoder.EndField();
        }

        public void WriteIntField(String fieldName, int v)
        {
            _internalEncoder.StartField(fieldName);
            _internalEncoder.WriteInt(v);
            _internalEncoder.EndField();
        }

        public void WriteLongContents(long v)
        {
            _internalEncoder.StartAnonymousField();
            _internalEncoder.WriteLong(v);
            _internalEncoder.EndField();
        }

        public void WriteLongField(String fieldName, long v)
        {
            _internalEncoder.StartField(fieldName);
            _internalEncoder.WriteLong(v);
            _internalEncoder.EndField();
        }

        public void WriteObjectContents(Object obj)
        {
            _internalEncoder.StartAnonymousField();
            _internalEncoder.WriteObject(this, obj);
            _internalEncoder.EndField();
        }

        public void WriteObjectField(String fieldName, Object obj, bool inline)
        {
            _internalEncoder.StartField(fieldName);
            _internalEncoder.WriteObject(this, obj);
            _internalEncoder.EndField();
        }

        public void WriteStringContents(String str)
        {
            _internalEncoder.StartAnonymousField();
            _internalEncoder.WriteString(str, false);
            _internalEncoder.EndField();
        }

        public void WriteStringField(String fieldName, String val)
        {
            if (val != null)
            {
                _internalEncoder.StartField(fieldName);
                _internalEncoder.WriteString(val, false);
                _internalEncoder.EndField();
            }
        }
    }

    internal class ReadState
    {
        public IDictionary<String, byte[]> objectFields = new Dictionary<String, byte[]>();
        public IList<byte[]> anonymousFields = new List<byte[]>();
        public Stream currentInput;
        public ReadState()
        {
        }
        public bool StartField(String name)
        {
            currentInput = null;
            byte[] content = CollectionUtil.GetValue(objectFields, name, null);
            if (content == null)
            {
                return false;
            }
            else
            {
                currentInput = new MemoryStream(content);
                return true;
            }
        }
        public void StartAnonymousField(int index)
        {
            if (index >= anonymousFields.Count)
            {
                throw new ConnectorException("Anonymous content not found");
            }
            currentInput = new MemoryStream(anonymousFields[index]);
        }
    }

    internal class InternalDecoder
    {
        private readonly byte[] _int_buf = new byte[4];
        private readonly byte[] _long_buf = new byte[8];
        private readonly byte[] _byte_buf = new byte[1];

        private bool _firstObject = true;

        private readonly IDictionary<int, string> _constantPool =
            new Dictionary<int, string>();

        private readonly IList<ReadState> _readStateStack = new List<ReadState>();
        internal readonly Stream _rootInput;

        public InternalDecoder(Stream input)
        {
            _rootInput = input;
        }

        public Object ReadObject(ObjectDecoder decoder)
        {

            if (_firstObject)
            {
                int magic = ReadInt();
                if (magic != BinaryObjectEncoder.OBJECT_MAGIC)
                {
                    throw new ConnectorException("Bad magic number: " + magic);
                }
                int version = ReadInt();
                if (version != BinaryObjectEncoder.ENCODING_VERSION)
                {
                    throw new ConnectorException("Unexpected version: " + version);
                }
                _firstObject = false;
            }

            //if it's a top-level object, it's proceeded by a constant pool
            if (_readStateStack.Count == 0)
            {
                int size = ReadInt();
                for (int i = 0; i < size; i++)
                {
                    String constant = ReadString(false);
                    int code = ReadInt();
                    _constantPool[code] = constant;
                }
            }

            Type clazz = ReadClass();

            ReadState state = new ReadState();
            while (true)
            {
                byte type = ReadByte();
                if (type == BinaryObjectEncoder.FIELD_TYPE_END_OBJECT)
                {
                    break;
                }
                else if (type == BinaryObjectEncoder.FIELD_TYPE_ANONYMOUS_FIELD)
                {
                    byte[] bytes = ReadByteArray();
                    state.anonymousFields.Add(bytes);
                }
                else if (type == BinaryObjectEncoder.FIELD_TYPE_NAMED_FIELD)
                {
                    String fieldName = ReadString(true);
                    byte[] bytes = ReadByteArray();
                    state.objectFields[fieldName] = bytes;
                }
                else
                {
                    throw new ConnectorException("Unknown type: " + type);
                }
            }
            _readStateStack.Add(state);

            Object rv;
            if (clazz == null)
            {
                rv = null;
            }
            else
            {
                ObjectSerializationHandler handler =
                    ObjectSerializerRegistry.GetHandlerByObjectType(clazz);
                if (handler == null)
                {
                    //we may have special handlers for certain types of arrays
                    //if handler is null, treat like any other array
                    if (clazz.IsArray)
                    {
                        int length = GetNumAnonymousFields();
                        Array array = Array.CreateInstance(clazz.GetElementType(),
                                length);
                        for (int i = 0; i < length; i++)
                        {
                            StartAnonymousField(i);
                            Object element = ReadObject(decoder);
                            array.SetValue(element, i);
                        }
                        rv = array;
                    }
                    else
                    {
                        throw new ConnectorException("No deserializer for type: " + clazz);
                    }
                }
                else
                {
                    rv = handler.Deserialize(decoder);
                }
            }
            _readStateStack.RemoveAt(_readStateStack.Count - 1);
            return rv;
        }

        public Type ReadClass()
        {
            int type = ReadByte();
            if (type == BinaryObjectEncoder.OBJECT_TYPE_NULL)
            {
                return null;
            }
            else if (type == BinaryObjectEncoder.OBJECT_TYPE_ARRAY)
            {
                Type componentClass = ReadClass();
                return componentClass.MakeArrayType();
            }
            else if (type == BinaryObjectEncoder.OBJECT_TYPE_CLASS)
            {
                String typeName = ReadString(true);
                ObjectTypeMapper mapper =
                    ObjectSerializerRegistry.GetMapperBySerialType(typeName);
                if (mapper == null)
                {
                    throw new ConnectorException("No deserializer for type: " + typeName);
                }
                return mapper.HandledObjectType;
            }
            else
            {
                throw new ConnectorException("Bad type value: " + type);
            }
        }

        public int GetNumAnonymousFields()
        {
            ReadState readState = _readStateStack[_readStateStack.Count - 1];
            return readState.anonymousFields.Count;
        }

        public void StartAnonymousField(int index)
        {
            ReadState readState = _readStateStack[_readStateStack.Count - 1];
            readState.StartAnonymousField(index);
        }

        public bool StartField(String name)
        {
            ReadState readState = _readStateStack[_readStateStack.Count - 1];
            return readState.StartField(name);
        }

        public int ReadInt()
        {
            ReadByteArrayFully(_int_buf);
            return (((_int_buf[0] & 0xff) << 24) | ((_int_buf[1] & 0xff) << 16) |
                    ((_int_buf[2] & 0xff) << 8) | (_int_buf[3] & 0xff));
        }

        public long ReadLong()
        {
            ReadByteArrayFully(_long_buf);
            return (((long)(_long_buf[0] & 0xff) << 56) |
                      ((long)(_long_buf[1] & 0xff) << 48) |
                      ((long)(_long_buf[2] & 0xff) << 40) |
                      ((long)(_long_buf[3] & 0xff) << 32) |
                      ((long)(_long_buf[4] & 0xff) << 24) |
                      ((long)(_long_buf[5] & 0xff) << 16) |
                      ((long)(_long_buf[6] & 0xff) << 8) |
                      ((long)(_long_buf[7] & 0xff)));
        }

        public double ReadDouble()
        {
            long v = ReadLong();
            return BitConverter.Int64BitsToDouble(v);
        }

        public byte[] ReadByteArray()
        {
            int len = ReadInt();
            byte[] bytes = new byte[len];
            ReadByteArrayFully(bytes);
            return bytes;
        }

        public byte ReadByte()
        {
            ReadByteArrayFully(_byte_buf);
            return _byte_buf[0];
        }

        public bool ReadBoolean()
        {
            byte b = ReadByte();
            return b != 0;
        }

        public String ReadString(bool interned)
        {
            if (interned)
            {
                int code = ReadInt();
                String name = CollectionUtil.GetValue(_constantPool, code, null);
                if (name == null)
                {
                    throw new ConnectorException("Undeclared code: " + code);
                }
                return name;
            }
            byte[] bytes = ReadByteArray();
            return Encoding.UTF8.GetString(bytes);
        }

        private Stream GetCurrentInput()
        {
            if (_readStateStack.Count > 0)
            {
                ReadState state = _readStateStack[_readStateStack.Count - 1];
                return state.currentInput;
            }
            else
            {
                return _rootInput;
            }
        }

        private void ReadByteArrayFully(byte[] bytes)
        {
            int pos = 0;
            while (pos < bytes.Length)
            {
                int count = GetCurrentInput().Read(bytes, pos, bytes.Length - pos);
                if (count <= 0)
                {
                    throw new EndOfStreamException();
                }
                pos += count;
            }
        }
    }

    internal class BinaryObjectDecoder : ObjectDecoder, BinaryObjectDeserializer
    {

        private InternalDecoder _internalDecoder;

        public BinaryObjectDecoder(Stream inp)
        {
            _internalDecoder = new InternalDecoder(new BufferedStream(inp, 4096));
        }

        public void Close()
        {
            _internalDecoder._rootInput.Close();
        }

        public Object ReadObject()
        {
            return _internalDecoder.ReadObject(this);
        }

        public bool ReadBooleanContents()
        {
            _internalDecoder.StartAnonymousField(0);
            return _internalDecoder.ReadBoolean();
        }

        public bool ReadBooleanField(String fieldName, bool dflt)
        {
            if (_internalDecoder.StartField(fieldName))
            {
                return _internalDecoder.ReadBoolean();
            }
            else
            {
                return dflt;
            }
        }

        public byte[] ReadByteArrayContents()
        {
            _internalDecoder.StartAnonymousField(0);
            return _internalDecoder.ReadByteArray();
        }

        public Type ReadClassContents()
        {
            _internalDecoder.StartAnonymousField(0);
            return _internalDecoder.ReadClass();
        }

        public Type ReadClassField(string fieldName, Type dflt)
        {
            if (_internalDecoder.StartField(fieldName))
            {
                return _internalDecoder.ReadClass();
            }
            else
            {
                return dflt;
            }
        }

        public double ReadDoubleContents()
        {
            _internalDecoder.StartAnonymousField(0);
            return _internalDecoder.ReadDouble();
        }

        public double ReadDoubleField(String fieldName, double dflt)
        {
            if (_internalDecoder.StartField(fieldName))
            {
                return ReadDoubleContents();
            }
            else
            {
                return dflt;
            }
        }

        public float ReadFloatContents()
        {
            _internalDecoder.StartAnonymousField(0);
            //read as double since C# only knows how to deal with that
            return (float)_internalDecoder.ReadDouble();
        }

        public float ReadFloatField(String fieldName, float dflt)
        {
            if (_internalDecoder.StartField(fieldName))
            {
                //read as double since C# only knows how to deal with that
                return (float)_internalDecoder.ReadDouble();
            }
            else
            {
                return dflt;
            }
        }

        public int ReadIntContents()
        {
            _internalDecoder.StartAnonymousField(0);
            return _internalDecoder.ReadInt();
        }

        public int ReadIntField(String fieldName, int dflt)
        {
            if (_internalDecoder.StartField(fieldName))
            {
                return _internalDecoder.ReadInt();
            }
            else
            {
                return dflt;
            }
        }

        public long ReadLongContents()
        {
            _internalDecoder.StartAnonymousField(0);
            return _internalDecoder.ReadLong();
        }

        public long ReadLongField(String fieldName, long dflt)
        {
            if (_internalDecoder.StartField(fieldName))
            {
                return _internalDecoder.ReadLong();
            }
            else
            {
                return dflt;
            }
        }

        public int GetNumSubObjects()
        {
            return _internalDecoder.GetNumAnonymousFields();
        }

        public Object ReadObjectContents(int index)
        {
            _internalDecoder.StartAnonymousField(index);
            return _internalDecoder.ReadObject(this);
        }

        public Object ReadObjectField(String fieldName, Type expected, Object dflt)
        {
            if (_internalDecoder.StartField(fieldName))
            {
                return _internalDecoder.ReadObject(this);
            }
            else
            {
                return dflt;
            }
        }

        public String ReadStringContents()
        {
            _internalDecoder.StartAnonymousField(0);
            return _internalDecoder.ReadString(false);
        }

        public String ReadStringField(String fieldName, String dflt)
        {
            if (_internalDecoder.StartField(fieldName))
            {
                return _internalDecoder.ReadString(false);
            }
            else
            {
                return dflt;
            }
        }
    }
}