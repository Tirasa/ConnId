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
using System.IO;
using System.Collections.Generic;
using System.Security;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Impl.Api;
using Org.IdentityConnectors.Framework.Impl.Api.Remote;
using Org.IdentityConnectors.Framework.Impl.Api.Remote.Messages;
using Org.IdentityConnectors.Framework.Impl.Serializer.Binary;
using Org.IdentityConnectors.Framework.Impl.Serializer.Xml;
using System.Linq;
using System.Globalization;
namespace Org.IdentityConnectors.Framework.Impl.Serializer
{
    #region Serialization Framework
    internal abstract class AbstractObjectSerializationHandler
            : ObjectTypeMapperImpl, ObjectSerializationHandler
    {

        protected AbstractObjectSerializationHandler(Type handledClass,
                String type)
            : base(handledClass, type)
        {
        }
        /// <summary>
        /// Called to serialize the object.
        /// </summary>
        abstract public void Serialize(Object obj, ObjectEncoder encoder);

        /// <summary>
        /// Called to deserialize the object.
        /// </summary>
        abstract public Object Deserialize(ObjectDecoder decoder);
    }


    internal class EnumSerializationHandler :
            AbstractObjectSerializationHandler
    {
        public EnumSerializationHandler(Type clazz, String name)
            : base(clazz, name)
        {
        }

        public override object Deserialize(ObjectDecoder decoder)
        {
            String val = decoder.ReadStringField("value", null);
            Type enumClass = HandledObjectType;
            Object rv = Enum.Parse(enumClass, val);
            return rv;
        }

        public override void Serialize(object obj, ObjectEncoder encoder)
        {
            Enum e = (Enum)obj;
            encoder.WriteStringField("value", Enum.GetName(e.GetType(), e));
        }
    }

    /// <summary>
    /// Interface to abstract away the difference between deserializing
    /// xml and binary
    /// </summary>
    internal interface ObjectDecoder
    {
        /// <summary>
        /// Reads an object using the appropriate serializer for that object
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. The subelement name for xml serialization</param>
        Object ReadObjectField(String fieldName,
             Type expectedType,
             Object dflt);

        /// <summary>
        /// Reads a bool.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. The subelement name for xml serialization</param>
        bool ReadBooleanField(String fieldName, bool dflt);

        /// <summary>
        /// Reads an int.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. The subelement name for xml serialization</param>
        int ReadIntField(String fieldName, int dflt);

        /// <summary>
        /// Reads a long.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. The subelement name for xml serialization</param>
        long ReadLongField(String fieldName, long dflt);

        /// <summary>
        /// Reads a float.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. The subelement name for xml serialization</param>
        float ReadFloatField(String fieldName, float dflt);

        /// <summary>
        /// Reads a double.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. The subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        double ReadDoubleField(String fieldName, double dflt);

        /// <summary>
        /// Reads a double.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. The subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        string ReadStringField(String fieldName, string dflt);

        /// <summary>
        /// Reads a double.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. The subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        Type ReadClassField(String fieldName, Type dflt);

        /// <summary>
        /// Reads the value in-line.
        /// </summary>
        String ReadStringContents();

        /// <summary>
        /// Reads the value in-line.
        /// </summary>
        bool ReadBooleanContents();

        /// <summary>
        /// Reads the value in-line.
        /// </summary>
        int ReadIntContents();

        /// <summary>
        /// reads the value in-line.
        /// </summary>
        long ReadLongContents();

        /// <summary>
        /// Reads the value in-line.
        /// </summary>
        float ReadFloatContents();

        /// <summary>
        /// reads the value in-line.
        /// </summary>
        double ReadDoubleContents();

        /// <summary>
        /// reads the value in-line.
        /// </summary>
        byte[] ReadByteArrayContents();

        /// <summary>
        /// reads the value in-line.
        /// </summary>
        Type ReadClassContents();

        /// <summary>
        /// Returns the number of anonymous sub-objects.
        /// </summary>
        int GetNumSubObjects();

        /// <summary>
        /// Reads a sub-object
        /// </summary>
        Object ReadObjectContents(int index);
    }

    /// <summary>
    /// Interface to abstract away the difference between serializing
    /// xml and binary
    /// </summary>
    internal interface ObjectEncoder
    {
        /// <summary>
        /// Writes an object using the appropriate serializer for that object
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. Becomes the subelement name for xml serialization</param>
        /// <param name="object">The object to serialize</param>
        void WriteObjectField(String fieldName, Object obj, bool inline);

        /// <summary>
        /// Writes a boolean.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. Becomes the subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        void WriteBooleanField(String fieldName, bool v);

        /// <summary>
        /// Writes an int.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. Becomes the subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        void WriteIntField(String fieldName, int v);

        /// <summary>
        /// Writes a long.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. Becomes the subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        void WriteLongField(String fieldName, long v);

        /// <summary>
        /// Writes a float.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. Becomes the subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        void WriteFloatField(String fieldName, float v);

        /// <summary>
        /// Writes a double.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. Becomes the subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        void WriteDoubleField(String fieldName, double v);

        /// <summary>
        /// Writes a double.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. Becomes the subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        void WriteStringField(String fieldName, string v);

        /// <summary>
        /// Writes a double.
        /// </summary>
        /// <param name="fieldName">A hint of the field name. Ignored for binary
        /// serialization. Becomes the subelement name for xml serialization</param>
        /// <param name="v">The value to serialize</param>
        void WriteClassField(String fieldName, Type v);

        /// <summary>
        /// Writes the value in-line.
        /// </summary>
        void WriteStringContents(String str);

        /// <summary>
        /// Writes the value in-line.
        /// </summary>
        void WriteBooleanContents(bool v);

        /// <summary>
        /// Writes the value in-line.
        /// </summary>
        void WriteIntContents(int v);

        /// <summary>
        /// Writes the value in-line.
        /// </summary>
        void WriteLongContents(long v);

        /// <summary>
        /// Writes the value in-line.
        /// </summary>
        void WriteFloatContents(float v);

        /// <summary>
        /// Writes the value in-line.
        /// </summary>
        void WriteDoubleContents(double v);

        /// <summary>
        /// Special case for byte [] that uses base64 encoding for XML
        /// </summary>
        void WriteByteArrayContents(byte[] v);

        /// <summary>
        /// Writes the value in-line.
        /// </summary>
        void WriteClassContents(Type v);

        /// <summary>
        /// Writes a sub-object
        /// </summary>
        void WriteObjectContents(object o);
    }

    /// <summary>
    /// Interface to be implemented to handle the serialization/
    /// deserialization of an object.
    /// </summary>
    internal interface ObjectSerializationHandler : ObjectTypeMapper
    {
        /// <summary>
        /// Called to serialize the object.
        /// </summary>
        void Serialize(Object obj, ObjectEncoder encoder);

        /// <summary>
        /// Called to deserialize the object.
        /// </summary>
        Object Deserialize(ObjectDecoder decoder);
    }

    internal static class ObjectSerializerRegistry
    {
        private static readonly IList<ObjectTypeMapper> HANDLERS =
            new List<ObjectTypeMapper>();

        private static readonly IDictionary<String, ObjectTypeMapper>
        HANDLERS_BY_SERIAL_TYPE = new Dictionary<String, ObjectTypeMapper>();

        static ObjectSerializerRegistry()
        {
            CollectionUtil.AddAll(HANDLERS, Primitives.HANDLERS);
            CollectionUtil.AddAll(HANDLERS, OperationMappings.MAPPINGS);
            CollectionUtil.AddAll(HANDLERS, APIConfigurationHandlers.HANDLERS);
            CollectionUtil.AddAll(HANDLERS, FilterHandlers.HANDLERS);
            CollectionUtil.AddAll(HANDLERS, CommonObjectHandlers.HANDLERS);
            CollectionUtil.AddAll(HANDLERS, MessageHandlers.HANDLERS);
            //object is special - just map the type, but don't actually
            //serialize
            HANDLERS.Add(new ObjectTypeMapperImpl(typeof(object), "Object"));

            foreach (ObjectTypeMapper handler in HANDLERS)
            {
                if (HANDLERS_BY_SERIAL_TYPE.ContainsKey(handler.HandledSerialType))
                {
                    throw new Exception("More than one handler of the" +
                            " same type: " + handler.HandledSerialType);
                }
                HANDLERS_BY_SERIAL_TYPE[handler.HandledSerialType] =
                        handler;
            }
        }

        /// <summary>
        /// Mapping by class.
        /// </summary>
        /// <remarks>
        /// Dynamically built since actual class may be
        /// a subclass.
        /// </remarks>
        private static readonly IDictionary<Type, ObjectTypeMapper>
        HANDLERS_BY_OBJECT_TYPE =
            new Dictionary<Type, ObjectTypeMapper>();

        public static ObjectTypeMapper GetMapperBySerialType(String type)
        {
            return CollectionUtil.GetValue(HANDLERS_BY_SERIAL_TYPE, type, null);
        }

        public static ObjectTypeMapper GetMapperByObjectType(Type clazz)
        {
            lock (HANDLERS_BY_OBJECT_TYPE)
            {
                ObjectTypeMapper rv =
                    CollectionUtil.GetValue(HANDLERS_BY_OBJECT_TYPE, clazz, null);
                if (rv == null)
                {
                    foreach (ObjectTypeMapper handler in HANDLERS)
                    {
                        IDelegatingObjectTypeMapper delegator =
                            handler as IDelegatingObjectTypeMapper;
                        ObjectTypeMapper effectiveHandler;
                        if (delegator != null)
                        {
                            effectiveHandler = delegator.FindMapperDelegate(clazz);
                        }
                        else
                        {
                            effectiveHandler = handler;
                        }
                        if (effectiveHandler != null)
                        {
                            Type handledClass =
                                effectiveHandler.HandledObjectType;
                            if (effectiveHandler.MatchSubclasses)
                            {
                                if (handledClass.IsAssignableFrom(clazz))
                                {
                                    rv = effectiveHandler;
                                    break;
                                }
                            }
                            else if (handledClass.Equals(clazz))
                            {
                                rv = effectiveHandler;
                                break;
                            }
                        }
                    }
                    HANDLERS_BY_OBJECT_TYPE[clazz] = rv;
                }
                return rv;
            }
        }
        public static ObjectSerializationHandler GetHandlerBySerialType(String type)
        {
            ObjectTypeMapper rv = GetMapperBySerialType(type);
            return rv as ObjectSerializationHandler;
        }

        public static ObjectSerializationHandler GetHandlerByObjectType(Type clazz)
        {
            ObjectTypeMapper rv = GetMapperByObjectType(clazz);
            return rv as ObjectSerializationHandler;
        }
    }

    /// <summary>
    /// ObjectTypeMappers can implement this interface as well. If
    /// they do, they can handle specific generic types as well.
    /// </summary>
    internal interface IDelegatingObjectTypeMapper
    {
        ObjectTypeMapper FindMapperDelegate(Type type);
    }

    /// <summary>
    /// Interface to be implemented to handle the serialization/
    /// deserialization of an object.
    /// </summary>
    internal interface ObjectTypeMapper
    {
        /// <summary>
        /// Returns the type of object being serialized.
        /// </summary>
        /// <remarks>
        /// This is
        /// an abstract type name that is intended to be language
        /// neutral.
        /// </remarks>
        String HandledSerialType { get; }

        /// <summary>
        /// Returns the java class handled by this handler.
        /// </summary>
        Type HandledObjectType { get; }

        /// <summary>
        /// Should we match subclasses of the given class or only
        /// the exact class?
        /// </summary>
        bool MatchSubclasses { get; }
    }

    internal class ObjectTypeMapperImpl : ObjectTypeMapper
    {
        private Type _handledClass;
        private String _handledType;

        public ObjectTypeMapperImpl(Type handledClass, String handledType)
        {
            _handledClass = handledClass;
            _handledType = handledType;
        }

        public Type HandledObjectType
        {
            get
            {
                return _handledClass;
            }
        }

        public String HandledSerialType
        {
            get
            {
                return _handledType;
            }
        }

        public virtual bool MatchSubclasses
        {
            get
            {
                return false;
            }
        }
    }

    internal abstract class AbstractExceptionHandler<T> : AbstractObjectSerializationHandler
        where T : Exception
    {
        protected AbstractExceptionHandler(String typeName)
            : base(typeof(T), typeName)
        {
        }

        public override Object Deserialize(ObjectDecoder decoder)
        {
            String message = decoder.ReadStringField("message", null);
            return CreateException(message);
        }

        public override void Serialize(Object obj, ObjectEncoder encoder)
        {
            Exception val = (Exception)obj;
            encoder.WriteStringField("message", val.Message);
        }
        public override bool MatchSubclasses
        {
            get { return true; }
        }
        protected abstract T CreateException(String message);
    }


    #endregion

    #region Primitives

    internal static class Primitives
    {
        public static readonly IList<ObjectTypeMapper> HANDLERS =
        new List<ObjectTypeMapper>();
        static Primitives()
        {
            HANDLERS.Add(new BooleanHandler(typeof(bool?), "Boolean"));
            HANDLERS.Add(new BooleanHandler(typeof(bool), "boolean"));
            HANDLERS.Add(new CharacterHandler(typeof(char?), "Character"));
            HANDLERS.Add(new CharacterHandler(typeof(char), "char"));
            HANDLERS.Add(new IntegerHandler(typeof(int?), "Integer"));
            HANDLERS.Add(new IntegerHandler(typeof(int), "int"));
            HANDLERS.Add(new LongHandler(typeof(long?), "Long"));
            HANDLERS.Add(new LongHandler(typeof(long), "long"));
            HANDLERS.Add(new FloatHandler(typeof(float?), "Float"));
            HANDLERS.Add(new FloatHandler(typeof(float), "float"));
            HANDLERS.Add(new DoubleHandler(typeof(double?), "Double"));
            HANDLERS.Add(new DoubleHandler(typeof(double), "double"));
            HANDLERS.Add(new StringHandler());
            HANDLERS.Add(new URIHandler());
            HANDLERS.Add(new FileHandler());
            HANDLERS.Add(new BigIntegerHandler());
            HANDLERS.Add(new BigDecimalHandler());
            HANDLERS.Add(new ByteArrayHandler());
            HANDLERS.Add(new ClassHandler());
            HANDLERS.Add(new MapEntryHandler());
            HANDLERS.Add(new MapHandler<object, object>());
            HANDLERS.Add(new ListHandler<object>());
            HANDLERS.Add(new SetHandler<object>());
            HANDLERS.Add(new LocaleHandler());
            HANDLERS.Add(new GuardedByteArrayHandler());
            HANDLERS.Add(new GuardedStringHandler());
        }
        private class BooleanHandler : AbstractObjectSerializationHandler
        {
            public BooleanHandler(Type objectType, String serialType)
                : base(objectType, serialType)
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                bool val = decoder.ReadBooleanContents();
                return val;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                bool val = (bool)obj;
                encoder.WriteBooleanContents(val);
            }
        }
        private class CharacterHandler : AbstractObjectSerializationHandler
        {
            public CharacterHandler(Type objectType, String serialType)
                : base(objectType, serialType)
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                String val = decoder.ReadStringContents();
                return val.ToCharArray()[0];
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                char val = (char)obj;
                encoder.WriteStringContents(val.ToString());
            }
        }
        private class IntegerHandler : AbstractObjectSerializationHandler
        {
            public IntegerHandler(Type objectType, String serialType)
                : base(objectType, serialType)
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                int val = decoder.ReadIntContents();
                return val;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                int val = (int)obj;
                encoder.WriteIntContents(val);
            }
        }
        private class LongHandler : AbstractObjectSerializationHandler
        {
            public LongHandler(Type objectType, String serialType)
                : base(objectType, serialType)
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                long val = decoder.ReadLongContents();
                return val;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                long val = (long)obj;
                encoder.WriteLongContents(val);
            }
        }
        private class FloatHandler : AbstractObjectSerializationHandler
        {
            public FloatHandler(Type objectType, String serialType)
                : base(objectType, serialType)
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                float val = decoder.ReadFloatContents();
                return val;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                float val = (float)obj;
                encoder.WriteFloatContents(val);
            }
        }
        private class DoubleHandler : AbstractObjectSerializationHandler
        {
            public DoubleHandler(Type objectType, String serialType)
                : base(objectType, serialType)
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                double val = decoder.ReadDoubleContents();
                return val;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                double val = (double)obj;
                encoder.WriteDoubleContents(val);
            }
        }
        private class StringHandler : AbstractObjectSerializationHandler
        {
            public StringHandler()
                : base(typeof(string), "String")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                string val = decoder.ReadStringContents();
                return val;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                string val = (string)obj;
                encoder.WriteStringContents(val);
            }
        }
        private class URIHandler : AbstractObjectSerializationHandler
        {
            public URIHandler()
                : base(typeof(Uri), "URI")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                string val = decoder.ReadStringContents();
                return new Uri(val);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                Uri val = (Uri)obj;
                encoder.WriteStringContents(val.ToString());
            }
        }
        private class FileHandler : AbstractObjectSerializationHandler
        {
            public FileHandler()
                : base(typeof(FileName), "File")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                string val = decoder.ReadStringContents();
                return new FileName(val);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                FileName val = (FileName)obj;
                encoder.WriteStringContents(val.Path);
            }
        }
        private class BigDecimalHandler : AbstractObjectSerializationHandler
        {
            public BigDecimalHandler()
                : base(typeof(BigDecimal), "BigDecimal")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                BigInteger unscaled =
                    new BigInteger(decoder.ReadStringField("unscaled", null));
                int scale = decoder.ReadIntField("scale", 0);
                return new BigDecimal(unscaled, scale);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                BigDecimal val = (BigDecimal)obj;
                encoder.WriteStringField("unscaled", val.UnscaledValue.Value);
                encoder.WriteIntField("scale", val.Scale);
            }
        }
        private class BigIntegerHandler : AbstractObjectSerializationHandler
        {
            public BigIntegerHandler()
                : base(typeof(BigInteger), "BigInteger")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                string val = decoder.ReadStringContents();
                return new BigInteger(val);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                BigInteger val = (BigInteger)obj;
                encoder.WriteStringContents(val.Value);
            }
        }
        private class ByteArrayHandler : AbstractObjectSerializationHandler
        {
            public ByteArrayHandler()
                : base(typeof(byte[]), "ByteArray")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                return decoder.ReadByteArrayContents();
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                byte[] val = (byte[])obj;
                encoder.WriteByteArrayContents(val);
            }
        }
        private class ClassHandler : AbstractObjectSerializationHandler
        {
            public ClassHandler()
                : base(typeof(Type), "Class")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                return decoder.ReadClassContents();
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                Type val = (Type)obj;
                encoder.WriteClassContents(val);
            }

            /// <summary>
            /// In C#, the actual Type of Type is RuntimeType
            /// </summary>
            public override bool MatchSubclasses
            {
                get { return true; }
            }
        }

        private class MapEntry
        {
            internal object key;
            internal object val;
            public MapEntry(object key, object val)
            {
                this.key = key;
                this.val = val;
            }
        }
        private class MapEntryHandler :
            AbstractObjectSerializationHandler
        {

            public MapEntryHandler()
                : base(typeof(MapEntry), "MapEntry")
            {
            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                Object key = decoder.ReadObjectContents(0);
                Object val = decoder.ReadObjectContents(1);
                return new MapEntry(key, val);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                MapEntry entry = (MapEntry)obj;
                encoder.WriteObjectContents(entry.key);
                encoder.WriteObjectContents(entry.val);
            }
        }
        private class MapHandler<TKey, TValue> :
            AbstractObjectSerializationHandler,
        IDelegatingObjectTypeMapper
        {
            public MapHandler()
                : base(typeof(IDictionary<TKey, TValue>), "Map")
            {
            }
            public ObjectTypeMapper FindMapperDelegate(Type type)
            {
                //get the IDictionary interface that this type implements
                Type interfaceType = ReflectionUtil.FindInHierarchyOf
                    (typeof(IDictionary<,>), type);
                if (interfaceType != null)
                {
                    Type[] keyAndValue = interfaceType.GetGenericArguments();
                    if (keyAndValue.Length != 2)
                    {
                        throw new Exception("Cannot serialize type: " + type);
                    }
                    Type mapHandlerRawType = typeof(MapHandler<,>);
                    Type mapHandlerType =
                        mapHandlerRawType.MakeGenericType(keyAndValue);
                    return (ObjectTypeMapper)Activator.CreateInstance(mapHandlerType);
                }
                return null;
            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                bool caseInsensitive =
                    decoder.ReadBooleanField("caseInsensitive", false);
                if (caseInsensitive)
                {
                    IDictionary<string, object> rv =
                        CollectionUtil.NewCaseInsensitiveDictionary<object>();
                    int count = decoder.GetNumSubObjects();
                    for (int i = 0; i < count; i++)
                    {
                        MapEntry entry = (MapEntry)decoder.ReadObjectContents(i);
                        rv["" + entry.key] = entry.val;
                    }
                    return rv;
                }
                else
                {
                    IDictionary<object, object> rv =
                        new Dictionary<object, object>();
                    int count = decoder.GetNumSubObjects();
                    for (int i = 0; i < count; i++)
                    {
                        MapEntry entry = (MapEntry)decoder.ReadObjectContents(i);
                        rv[entry.key] = entry.val;
                    }
                    return rv;
                }
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                IDictionary<TKey, TValue> map = (IDictionary<TKey, TValue>)obj;
                if (CollectionUtil.IsCaseInsensitiveDictionary(map))
                {
                    encoder.WriteBooleanField("caseInsensitive", true);
                }
                else if (map is SortedDictionary<TKey, TValue>)
                {
                    throw new Exception("Serialization of SortedDictionary not supported");
                }
                foreach (KeyValuePair<TKey, TValue> entry in map)
                {
                    MapEntry myEntry = new MapEntry(entry.Key, entry.Value);
                    encoder.WriteObjectContents(myEntry);
                }
            }

            public override bool MatchSubclasses
            {
                get { return true; }
            }
        }
        private class ListHandler<T> :
            AbstractObjectSerializationHandler,
        IDelegatingObjectTypeMapper
        {

            public ListHandler()
                : base(typeof(IList<T>), "List")
            {
            }
            public ObjectTypeMapper FindMapperDelegate(Type type)
            {
                //in C#, arrays implement IList
                if (type.IsArray)
                {
                    return null;
                }
                //get the IList interface that this type implements
                Type interfaceType = ReflectionUtil.FindInHierarchyOf
                    (typeof(IList<>), type);
                if (interfaceType != null)
                {
                    Type[] val = interfaceType.GetGenericArguments();
                    if (val.Length != 1)
                    {
                        throw new Exception("Cannot serialize type: " + type);
                    }
                    Type listHandlerRawType = typeof(ListHandler<>);
                    Type listHandlerType =
                        listHandlerRawType.MakeGenericType(val);
                    return (ObjectTypeMapper)Activator.CreateInstance(listHandlerType);
                }
                return null;
            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                IList<object> rv =
                    new List<object>();
                int count = decoder.GetNumSubObjects();
                for (int i = 0; i < count; i++)
                {
                    rv.Add(decoder.ReadObjectContents(i));
                }
                return rv;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                IList<T> list = (IList<T>)obj;
                foreach (T o in list)
                {
                    encoder.WriteObjectContents(o);
                }
            }

            public override bool MatchSubclasses
            {
                get { return true; }
            }
        }
        private class SetHandler<T> :
            AbstractObjectSerializationHandler,
        IDelegatingObjectTypeMapper
        {

            public SetHandler()
                : base(typeof(ICollection<T>), "Set")
            {
            }
            public ObjectTypeMapper FindMapperDelegate(Type type)
            {
                //in C#, arrays implement IList
                if (type.IsArray)
                {
                    return null;
                }

                //get the IList interface that this type implements
                Type interfaceType = ReflectionUtil.FindInHierarchyOf
                    (typeof(ICollection<>), type);
                if (interfaceType != null)
                {
                    Type[] val = interfaceType.GetGenericArguments();
                    if (val.Length != 1)
                    {
                        throw new Exception("Cannot serialize type: " + type);
                    }
                    Type setHandlerRawType = typeof(SetHandler<>);
                    Type setHandlerType =
                        setHandlerRawType.MakeGenericType(val);
                    return (ObjectTypeMapper)Activator.CreateInstance(setHandlerType);
                }
                return null;
            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                bool caseInsensitive =
                    decoder.ReadBooleanField("caseInsensitive", false);
                if (caseInsensitive)
                {
                    ICollection<string> rv =
                        CollectionUtil.NewCaseInsensitiveSet();
                    int count = decoder.GetNumSubObjects();
                    for (int i = 0; i < count; i++)
                    {
                        rv.Add("" + decoder.ReadObjectContents(i));
                    }
                    return rv;
                }
                else
                {
                    ICollection<object> rv =
                        new HashSet<object>();
                    int count = decoder.GetNumSubObjects();
                    for (int i = 0; i < count; i++)
                    {
                        rv.Add(decoder.ReadObjectContents(i));
                    }
                    return rv;
                }
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ICollection<T> list = (ICollection<T>)obj;
                if (CollectionUtil.IsCaseInsensitiveSet(list))
                {
                    encoder.WriteBooleanField("caseInsensitive", true);
                }
                foreach (T o in list)
                {
                    encoder.WriteObjectContents(o);
                }
            }

            public override bool MatchSubclasses
            {
                get { return true; }
            }
        }
        private class LocaleHandler : AbstractObjectSerializationHandler
        {
            public LocaleHandler()
                : base(typeof(CultureInfo), "Locale")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                string language = decoder.ReadStringField("language", "");
                string country = decoder.ReadStringField("country", "");
                string variant = decoder.ReadStringField("variant", "");
                Locale locale = new Locale(language, country, variant);
                return locale.ToCultureInfo();
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                CultureInfo cultureInfo = (CultureInfo)obj;
                Locale locale = Locale.FindLocale(cultureInfo);
                encoder.WriteStringField("language", locale.Language);
                encoder.WriteStringField("country", locale.Country);
                encoder.WriteStringField("variant", locale.Variant);

            }
        }

        private class GuardedByteArrayHandler : AbstractObjectSerializationHandler
        {
            public GuardedByteArrayHandler()
                : base(typeof(GuardedByteArray), "GuardedByteArray")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                byte[] encryptedBytes = null;
                UnmanagedArray<byte> clearBytes = null;
                try
                {
                    encryptedBytes = decoder.ReadByteArrayContents();
                    clearBytes = EncryptorFactory.GetInstance().GetDefaultEncryptor().Decrypt(encryptedBytes);
                    GuardedByteArray rv = new GuardedByteArray();
                    for (int i = 0; i < clearBytes.Length; i++)
                    {
                        rv.AppendByte(clearBytes[i]);
                    }
                    return rv;
                }
                finally
                {
                    if (clearBytes != null)
                    {
                        clearBytes.Dispose();
                    }
                    SecurityUtil.Clear(encryptedBytes);
                }
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                GuardedByteArray str = (GuardedByteArray)obj;
                str.Access(
                     clearBytes =>
                     {
                         byte[] encryptedBytes = null;
                         try
                         {
                             encryptedBytes = EncryptorFactory.GetInstance().GetDefaultEncryptor().Encrypt(clearBytes);
                             encoder.WriteByteArrayContents(encryptedBytes);
                         }
                         finally
                         {
                             SecurityUtil.Clear(encryptedBytes);
                         }
                     });
            }
        }

        private class GuardedStringHandler : AbstractObjectSerializationHandler
        {
            public GuardedStringHandler()
                : base(typeof(GuardedString), "GuardedString")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                byte[] encryptedBytes = null;
                UnmanagedArray<byte> clearBytes = null;
                UnmanagedArray<char> clearChars = null;
                try
                {
                    encryptedBytes = decoder.ReadByteArrayContents();
                    clearBytes = EncryptorFactory.GetInstance().GetDefaultEncryptor().Decrypt(encryptedBytes);
                    clearChars = SecurityUtil.BytesToChars(clearBytes);
                    GuardedString rv = new GuardedString();
                    for (int i = 0; i < clearChars.Length; i++)
                    {
                        rv.AppendChar(clearChars[i]);
                    }
                    return rv;
                }
                finally
                {
                    if (clearBytes != null)
                    {
                        clearBytes.Dispose();
                    }
                    if (clearChars != null)
                    {
                        clearChars.Dispose();
                    }
                    SecurityUtil.Clear(encryptedBytes);
                }
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                GuardedString str = (GuardedString)obj;
                str.Access(
                     clearChars =>
                     {
                         UnmanagedArray<byte> clearBytes = null;
                         byte[] encryptedBytes = null;
                         try
                         {
                             clearBytes = SecurityUtil.CharsToBytes(clearChars);
                             encryptedBytes = EncryptorFactory.GetInstance().GetDefaultEncryptor().Encrypt(clearBytes);
                             encoder.WriteByteArrayContents(encryptedBytes);
                         }
                         finally
                         {
                             if (clearBytes != null)
                             {
                                 clearBytes.Dispose();
                             }
                             SecurityUtil.Clear(encryptedBytes);
                         }
                     });
            }
        }
    }
    #endregion

    #region APIConfigurationHandlers
    internal static class APIConfigurationHandlers
    {
        public static readonly IList<ObjectTypeMapper> HANDLERS =
        new List<ObjectTypeMapper>();
        static APIConfigurationHandlers()
        {
            HANDLERS.Add(new ConnectionPoolingConfigurationHandler());
            HANDLERS.Add(new ConfigurationPropertyHandler());
            HANDLERS.Add(new ConfigurationPropertiesHandler());
            HANDLERS.Add(new APIConfigurationHandler());
            HANDLERS.Add(new ConnectorMessagesHandler());
            HANDLERS.Add(new ConnectorKeyHandler());
            HANDLERS.Add(new ConnectorInfoHandler());
        }
        private class ConnectionPoolingConfigurationHandler : AbstractObjectSerializationHandler
        {
            public ConnectionPoolingConfigurationHandler()
                : base(typeof(ObjectPoolConfiguration), "ObjectPoolConfiguration")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                ObjectPoolConfiguration rv =
                    new ObjectPoolConfiguration();
                rv.MaxObjects = (decoder.ReadIntField("maxObjects", rv.MaxObjects));
                rv.MaxIdle = (decoder.ReadIntField("maxIdle", rv.MaxIdle));
                rv.MaxWait = (decoder.ReadLongField("maxWait", rv.MaxWait));
                rv.MinEvictableIdleTimeMillis = (
                        decoder.ReadLongField("minEvictableIdleTimeMillis", rv.MinEvictableIdleTimeMillis));
                rv.MinIdle = (
                        decoder.ReadIntField("minIdle", rv.MinIdle));
                return rv;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ObjectPoolConfiguration val =
                    (ObjectPoolConfiguration)obj;
                encoder.WriteIntField("maxObjects",
                        val.MaxObjects);
                encoder.WriteIntField("maxIdle",
                        val.MaxIdle);
                encoder.WriteLongField("maxWait",
                        val.MaxWait);
                encoder.WriteLongField("minEvictableIdleTimeMillis",
                        val.MinEvictableIdleTimeMillis);
                encoder.WriteIntField("minIdle",
                        val.MinIdle);
            }
        }
        private class ConfigurationPropertyHandler : AbstractObjectSerializationHandler
        {
            public ConfigurationPropertyHandler()
                : base(typeof(ConfigurationPropertyImpl), "ConfigurationProperty")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                ConfigurationPropertyImpl rv = new ConfigurationPropertyImpl();
                rv.Order = (decoder.ReadIntField("order", 0));
                rv.IsConfidential = (decoder.ReadBooleanField("confidential", false));
                rv.IsRequired = decoder.ReadBooleanField("required", false);
                rv.Name = (decoder.ReadStringField("name", null));
                rv.HelpMessageKey = (
                    decoder.ReadStringField("helpMessageKey", null));
                rv.DisplayMessageKey = (
                    decoder.ReadStringField("displayMessageKey", null));
                rv.ValueType = (
                        decoder.ReadClassField("type", null));
                rv.Value = (
                        decoder.ReadObjectField("value", null, null));
                ICollection<object> operationsObj =
                    (ICollection<object>)decoder.ReadObjectField("operations", typeof(ICollection<object>), null);
                ICollection<SafeType<APIOperation>> operations =
                    new HashSet<SafeType<APIOperation>>();
                foreach (object o in operationsObj)
                {
                    Type type = (Type)o;
                    operations.Add(SafeType<APIOperation>.ForRawType(type));
                }
                rv.Operations = operations;

                return rv;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ConfigurationPropertyImpl val =
                    (ConfigurationPropertyImpl)obj;
                encoder.WriteIntField("order",
                        val.Order);
                encoder.WriteBooleanField("confidential",
                        val.IsConfidential);
                encoder.WriteBooleanField("required",
                        val.IsRequired);
                encoder.WriteStringField("name",
                        val.Name);
                encoder.WriteStringField("helpMessageKey",
                        val.HelpMessageKey);
                encoder.WriteStringField("displayMessageKey",
                        val.DisplayMessageKey);
                encoder.WriteClassField("type",
                        val.ValueType);
                encoder.WriteObjectField("value",
                        val.Value,
                        false);
                ICollection<Type> operationsObj =
                    new HashSet<Type>();
                foreach (SafeType<APIOperation> op in val.Operations)
                {
                    operationsObj.Add(op.RawType);
                }
                encoder.WriteObjectField("operations", operationsObj, true);
            }
        }
        private class ConfigurationPropertiesHandler : AbstractObjectSerializationHandler
        {
            public ConfigurationPropertiesHandler()
                : base(typeof(ConfigurationPropertiesImpl), "ConfigurationProperties")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                ConfigurationPropertiesImpl rv =
                    new ConfigurationPropertiesImpl();
                IList<ConfigurationPropertyImpl> props =
                    new List<ConfigurationPropertyImpl>
                    ();
                int count = decoder.GetNumSubObjects();
                for (int i = 0; i < count; i++)
                {
                    ConfigurationPropertyImpl prop =
                        (ConfigurationPropertyImpl)decoder.ReadObjectContents(i);
                    props.Add(prop);
                }
                rv.Properties = props;
                return rv;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ConfigurationPropertiesImpl val =
                    (ConfigurationPropertiesImpl)obj;
                IList<ConfigurationPropertyImpl> props =
                    val.Properties;
                foreach (ConfigurationPropertyImpl prop in props)
                {
                    encoder.WriteObjectContents(prop);
                }
            }
        }
        private class APIConfigurationHandler : AbstractObjectSerializationHandler
        {
            public APIConfigurationHandler()
                : base(typeof(APIConfigurationImpl), "APIConfiguration")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                APIConfigurationImpl rv = new APIConfigurationImpl();
                rv.IsConnectorPoolingSupported = (
                        decoder.ReadBooleanField("connectorPoolingSupported", false));
                rv.ConnectorPoolConfiguration = (
                        (ObjectPoolConfiguration)
                        decoder.ReadObjectField("connectorPoolConfiguration", null, null));
                rv.ConfigurationProperties = ((ConfigurationPropertiesImpl)
                                            decoder.ReadObjectField("ConfigurationProperties", typeof(ConfigurationPropertiesImpl), null));
                IDictionary<object, object> timeoutMapObj =
                    (IDictionary<object, object>)decoder.ReadObjectField("timeoutMap", null, null);
                IDictionary<SafeType<APIOperation>, int> timeoutMap =
                    new Dictionary<SafeType<APIOperation>, int>();
                foreach (KeyValuePair<object, object> entry in timeoutMapObj)
                {
                    Type type = (Type)entry.Key;
                    int val = (int)entry.Value;
                    timeoutMap[SafeType<APIOperation>.ForRawType(type)] = val;
                }
                rv.TimeoutMap = timeoutMap;
                ICollection<Object> supportedOperationsObj =
                    (ICollection<object>)decoder.ReadObjectField("SupportedOperations", typeof(ICollection<object>), null);
                ICollection<SafeType<APIOperation>> supportedOperations =
                    new HashSet<SafeType<APIOperation>>();
                foreach (object obj in supportedOperationsObj)
                {
                    Type type = (Type)obj;
                    supportedOperations.Add(SafeType<APIOperation>.ForRawType(type));
                }
                rv.SupportedOperations = supportedOperations;
                rv.ProducerBufferSize = (decoder.ReadIntField("producerBufferSize", 0));
                return rv;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                APIConfigurationImpl val =
                    (APIConfigurationImpl)obj;

                ICollection<Type> supportedOperations =
                    new HashSet<Type>();
                if (val.SupportedOperations != null)
                {
                    foreach (SafeType<APIOperation> op in val.SupportedOperations)
                    {
                        supportedOperations.Add(op.RawType);
                    }
                }
                IDictionary<Type, int> timeoutMap =
                    new Dictionary<Type, int>();
                if (val.TimeoutMap != null)
                {
                    foreach (KeyValuePair<SafeType<APIOperation>, int> entry in val.TimeoutMap)
                    {
                        timeoutMap[entry.Key.RawType] = entry.Value;
                    }
                }

                encoder.WriteIntField("producerBufferSize",
                        val.ProducerBufferSize);
                encoder.WriteBooleanField("connectorPoolingSupported",
                        val.IsConnectorPoolingSupported);
                encoder.WriteObjectField("connectorPoolConfiguration",
                        val.ConnectorPoolConfiguration, false);
                encoder.WriteObjectField("ConfigurationProperties",
                        val.ConfigurationProperties, true);
                encoder.WriteObjectField("timeoutMap",
                        timeoutMap, false);
                encoder.WriteObjectField("SupportedOperations",
                        supportedOperations, true);
            }
        }
        private class ConnectorMessagesHandler : AbstractObjectSerializationHandler
        {
            public ConnectorMessagesHandler()
                : base(typeof(ConnectorMessagesImpl), "ConnectorMessages")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                ConnectorMessagesImpl rv = new ConnectorMessagesImpl();
                IDictionary<object, object> catalogsObj =
                    (IDictionary<object, object>)decoder.ReadObjectField("catalogs", null, null);
                IDictionary<CultureInfo, IDictionary<string, string>>
                    catalogs = new Dictionary<CultureInfo, IDictionary<string, string>>();
                foreach (KeyValuePair<object, object> entry in catalogsObj)
                {
                    CultureInfo key = (CultureInfo)entry.Key;
                    IDictionary<object, object> valObj = (IDictionary<object, object>)entry.Value;
                    IDictionary<string, string> val =
                        CollectionUtil.NewDictionary<object, object, string, string>(valObj);
                    catalogs[key] = val;
                }
                rv.Catalogs = (catalogs);
                return rv;

            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ConnectorMessagesImpl val =
                    (ConnectorMessagesImpl)obj;
                encoder.WriteObjectField("catalogs",
                        val.Catalogs, false);
            }
        }

        private class ConnectorKeyHandler : AbstractObjectSerializationHandler
        {
            public ConnectorKeyHandler()
                : base(typeof(ConnectorKey), "ConnectorKey")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                String bundleName =
                    decoder.ReadStringField("bundleName", null);
                String bundleVersion =
                    decoder.ReadStringField("bundleVersion", null);
                String connectorName =
                    decoder.ReadStringField("connectorName", null);
                return new ConnectorKey(bundleName, bundleVersion, connectorName);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ConnectorKey val = (ConnectorKey)obj;
                encoder.WriteStringField("bundleName",
                        val.BundleName);
                encoder.WriteStringField("bundleVersion",
                        val.BundleVersion);
                encoder.WriteStringField("connectorName",
                        val.ConnectorName);
            }
        }

        private class ConnectorInfoHandler : AbstractObjectSerializationHandler
        {
            public ConnectorInfoHandler()
                : base(typeof(RemoteConnectorInfoImpl), "ConnectorInfo")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                RemoteConnectorInfoImpl rv = new RemoteConnectorInfoImpl();
                rv.ConnectorDisplayNameKey = (
                                   decoder.ReadStringField("connectorDisplayNameKey", null));
                rv.ConnectorKey = ((ConnectorKey)
                    decoder.ReadObjectField("ConnectorKey", typeof(ConnectorKey), null));
                rv.Messages = ((ConnectorMessagesImpl)
                    decoder.ReadObjectField("ConnectorMessages", typeof(ConnectorMessagesImpl), null));
                rv.DefaultAPIConfiguration = ((APIConfigurationImpl)
                                            decoder.ReadObjectField("APIConfiguration", typeof(APIConfigurationImpl), null));
                return rv;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                RemoteConnectorInfoImpl val =
                    (RemoteConnectorInfoImpl)obj;
                encoder.WriteStringField("connectorDisplayNameKey",
                        val.ConnectorDisplayNameKey);
                encoder.WriteObjectField("ConnectorKey",
                        val.ConnectorKey, true);
                encoder.WriteObjectField("ConnectorMessages",
                        val.Messages, true);
                encoder.WriteObjectField("APIConfiguration",
                        val.DefaultAPIConfiguration, true);
            }
        }

    }
    #endregion

    #region ObjectSerializerFactoryImpl
    public class ObjectSerializerFactoryImpl : ObjectSerializerFactory
    {
        public override BinaryObjectDeserializer NewBinaryDeserializer(Stream i)
        {
            return new BinaryObjectDecoder(i);
        }

        public override BinaryObjectSerializer NewBinarySerializer(Stream os)
        {
            return new BinaryObjectEncoder(os);
        }
        public override XmlObjectSerializer NewXmlSerializer(TextWriter w,
                bool includeHeader,
                bool multiObject)
        {
            return new XmlObjectSerializerImpl(w, includeHeader, multiObject);
        }

        public override void DeserializeXmlStream(TextReader reader,
                XmlObjectResultsHandler handler,
                bool validate)
        {
            XmlObjectParser.parse(reader, handler, validate);
        }

    }
    #endregion

    #region OperationMappings
    internal static class OperationMappings
    {
        public static readonly IList<ObjectTypeMapper> MAPPINGS =
            new List<ObjectTypeMapper>();

        static OperationMappings()
        {
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(AuthenticationApiOp),
                    "AuthenticationApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(ResolveUsernameApiOp),
                    "ResolveUsernameApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(SearchApiOp),
                    "SearchApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(ValidateApiOp),
                    "ValidateApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(CreateApiOp),
                    "CreateApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(SchemaApiOp),
                    "SchemaApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(UpdateApiOp),
                    "UpdateApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(DeleteApiOp),
                    "DeleteApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(GetApiOp),
            "GetApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(TestApiOp),
            "TestApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(ScriptOnConnectorApiOp),
            "ScriptOnConnectorApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(ScriptOnResourceApiOp),
            "ScriptOnResourceApiOp"));
            MAPPINGS.Add(new ObjectTypeMapperImpl(typeof(SyncApiOp),
            "SyncApiOp"));
        }

    }
    #endregion

    #region CommonObjectHandlers
    internal static class CommonObjectHandlers
    {
        public static readonly IList<ObjectTypeMapper> HANDLERS =
        new List<ObjectTypeMapper>();
        static CommonObjectHandlers()
        {
            HANDLERS.Add(new AlreadyExistsExceptionHandler());
            HANDLERS.Add(new ConfigurationExceptionHandler());
            HANDLERS.Add(new ConnectionBrokenExceptionHandler());
            HANDLERS.Add(new ConnectionFailedExceptionHandler());
            HANDLERS.Add(new ConnectorIOExceptionHandler());
            HANDLERS.Add(new PasswordExpiredExceptionHandler());
            HANDLERS.Add(new InvalidPasswordExceptionHandler());
            HANDLERS.Add(new UnknownUidExceptionHandler());
            HANDLERS.Add(new InvalidCredentialExceptionHandler());
            HANDLERS.Add(new PermissionDeniedExceptionHandler());
            HANDLERS.Add(new ConnectorSecurityExceptionHandler());
            HANDLERS.Add(new OperationTimeoutExceptionHandler());
            HANDLERS.Add(new ConnectorExceptionHandler());
            HANDLERS.Add(new ArgumentExceptionHandler());
            HANDLERS.Add(new RuntimeExceptionHandler());
            HANDLERS.Add(new ExceptionHandler());
            HANDLERS.Add(new ThrowableHandler());
            HANDLERS.Add(new AttributeHandler());
            HANDLERS.Add(new AttributeInfoHandler());
            HANDLERS.Add(new ConnectorObjectHandler());
            HANDLERS.Add(new NameHandler());
            HANDLERS.Add(new ObjectClassHandler());
            HANDLERS.Add(new ObjectClassInfoHandler());
            HANDLERS.Add(new SchemaHandler());
            HANDLERS.Add(new UidHandler());
            HANDLERS.Add(new ScriptHandler());
            HANDLERS.Add(new ScriptContextHandler());
            HANDLERS.Add(new OperationOptionsHandler());
            HANDLERS.Add(new OperationOptionInfoHandler());
            HANDLERS.Add(new EnumSerializationHandler(typeof(ConnectorAttributeInfo.Flags),
                                                       "AttributeInfoFlag"));
            HANDLERS.Add(new EnumSerializationHandler(typeof(SyncDeltaType),
                                                       "SyncDeltaType"));
            HANDLERS.Add(new SyncTokenHandler());
            HANDLERS.Add(new SyncDeltaHandler());
            HANDLERS.Add(new QualifiedUidHandler());
        }

        private class AlreadyExistsExceptionHandler : AbstractExceptionHandler<AlreadyExistsException>
        {
            public AlreadyExistsExceptionHandler()
                : base("AlreadyExistsException")
            {

            }
            protected override AlreadyExistsException CreateException(String msg)
            {
                return new AlreadyExistsException(msg);
            }
        }
        private class ConfigurationExceptionHandler : AbstractExceptionHandler<ConfigurationException>
        {
            public ConfigurationExceptionHandler()
                : base("ConfigurationException")
            {

            }
            protected override ConfigurationException CreateException(String msg)
            {
                return new ConfigurationException(msg);
            }
        }
        private class ConnectionBrokenExceptionHandler : AbstractExceptionHandler<ConnectionBrokenException>
        {
            public ConnectionBrokenExceptionHandler()
                : base("ConnectionBrokenException")
            {

            }
            protected override ConnectionBrokenException CreateException(String msg)
            {
                return new ConnectionBrokenException(msg);
            }
        }
        private class ConnectionFailedExceptionHandler : AbstractExceptionHandler<ConnectionFailedException>
        {
            public ConnectionFailedExceptionHandler()
                : base("ConnectionFailedException")
            {

            }
            protected override ConnectionFailedException CreateException(String msg)
            {
                return new ConnectionFailedException(msg);
            }
        }
        private class ConnectorIOExceptionHandler : AbstractExceptionHandler<ConnectorIOException>
        {
            public ConnectorIOExceptionHandler()
                : base("ConnectorIOException")
            {

            }
            protected override ConnectorIOException CreateException(String msg)
            {
                return new ConnectorIOException(msg);
            }
        }
        private class PasswordExpiredExceptionHandler : AbstractExceptionHandler<PasswordExpiredException>
        {
            public PasswordExpiredExceptionHandler()
                : base("PasswordExpiredException")
            {

            }

            public override Object Deserialize(ObjectDecoder decoder)
            {
                Uid uid = (Uid)decoder.ReadObjectField("Uid", typeof(Uid), null);
                PasswordExpiredException ex =
                    (PasswordExpiredException)base.Deserialize(decoder);
                ex.Uid = uid;
                return ex;
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                base.Serialize(obj, encoder);
                PasswordExpiredException val = (PasswordExpiredException)obj;
                encoder.WriteObjectField("Uid", val.Uid, true);
            }
            protected override PasswordExpiredException CreateException(String msg)
            {
                return new PasswordExpiredException(msg);
            }
        }
        private class InvalidPasswordExceptionHandler : AbstractExceptionHandler<InvalidPasswordException>
        {
            public InvalidPasswordExceptionHandler()
                : base("InvalidPasswordException")
            {

            }
            protected override InvalidPasswordException CreateException(String msg)
            {
                return new InvalidPasswordException(msg);
            }
        }
        private class UnknownUidExceptionHandler : AbstractExceptionHandler<UnknownUidException>
        {
            public UnknownUidExceptionHandler()
                : base("UnknownUidException")
            {

            }
            protected override UnknownUidException CreateException(String msg)
            {
                return new UnknownUidException(msg);
            }
        }
        private class InvalidCredentialExceptionHandler : AbstractExceptionHandler<InvalidCredentialException>
        {
            public InvalidCredentialExceptionHandler()
                : base("InvalidCredentialException")
            {

            }
            protected override InvalidCredentialException CreateException(String msg)
            {
                return new InvalidCredentialException(msg);
            }
        }
        private class PermissionDeniedExceptionHandler : AbstractExceptionHandler<PermissionDeniedException>
        {
            public PermissionDeniedExceptionHandler()
                : base("PermissionDeniedException")
            {

            }
            protected override PermissionDeniedException CreateException(String msg)
            {
                return new PermissionDeniedException(msg);
            }
        }
        private class ConnectorSecurityExceptionHandler : AbstractExceptionHandler<ConnectorSecurityException>
        {
            public ConnectorSecurityExceptionHandler()
                : base("ConnectorSecurityException")
            {

            }
            protected override ConnectorSecurityException CreateException(String msg)
            {
                return new ConnectorSecurityException(msg);
            }
        }
        private class OperationTimeoutExceptionHandler : AbstractExceptionHandler<OperationTimeoutException>
        {
            public OperationTimeoutExceptionHandler()
                : base("OperationTimeoutException")
            {

            }
            protected override OperationTimeoutException CreateException(String msg)
            {
                return new OperationTimeoutException(msg);
            }
        }
        private class ConnectorExceptionHandler : AbstractExceptionHandler<ConnectorException>
        {
            public ConnectorExceptionHandler()
                : base("ConnectorException")
            {

            }
            protected override ConnectorException CreateException(String msg)
            {
                return new ConnectorException(msg);
            }
        }
        //RuntimeException, Exception, and Throwable
        //when going from Java to C#, these always become
        //Exception.
        //when going from C# to Java, these always become
        //RuntimeException
        private class RuntimeExceptionHandler : AbstractExceptionHandler<Exception>
        {
            public RuntimeExceptionHandler()
                : base("RuntimeException")
            {

            }
            protected override Exception CreateException(String msg)
            {
                return new Exception(msg);
            }
        }

        private class ArgumentExceptionHandler : AbstractExceptionHandler<ArgumentException>
        {
            public ArgumentExceptionHandler()
                : base("IllegalArgumentException")
            {

            }
            protected override ArgumentException CreateException(string msg)
            {
                return new ArgumentException(msg);
            }
        }

        private class ExceptionHandler : AbstractExceptionHandler<Exception>
        {
            public ExceptionHandler()
                : base("Exception")
            {

            }
            protected override Exception CreateException(String msg)
            {
                return new Exception(msg);
            }
        }
        private class ThrowableHandler : AbstractExceptionHandler<Exception>
        {
            public ThrowableHandler()
                : base("Throwable")
            {

            }
            protected override Exception CreateException(String msg)
            {
                return new Exception(msg);
            }
        }

        private abstract class AbstractAttributeHandler<T>
            : AbstractObjectSerializationHandler
            where T : ConnectorAttribute
        {

            protected AbstractAttributeHandler(String typeName)
                : base(typeof(T), typeName)
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                String name = decoder.ReadStringField("name", null);
                IList<Object> val = (IList<Object>)decoder.ReadObjectField("Values", typeof(IList<object>), null);
                return CreateAttribute(name, val);
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
                ConnectorAttribute val = (ConnectorAttribute)obj;
                encoder.WriteStringField("name", val.Name);
                encoder.WriteObjectField("Values", val.Value, true);
            }

            protected abstract T CreateAttribute(String name, IList<Object> val);
        }

        private class AttributeHandler
            : AbstractAttributeHandler<ConnectorAttribute>
        {
            public AttributeHandler()
                : base("Attribute")
            {

            }
            protected override ConnectorAttribute CreateAttribute(String name, IList<Object> value)
            {
                return ConnectorAttributeBuilder.Build(name, value);
            }
        }

        private class AttributeInfoHandler : AbstractObjectSerializationHandler
        {
            public AttributeInfoHandler()
                : base(typeof(ConnectorAttributeInfo), "AttributeInfo")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                ConnectorAttributeInfoBuilder builder = new ConnectorAttributeInfoBuilder();
                builder.Name = (
                              decoder.ReadStringField("name", null));
                builder.ValueType = (
                                   decoder.ReadClassField("type", null));
                ConnectorAttributeInfo.Flags flags = ConnectorAttributeInfo.Flags.NONE;
                int count = decoder.GetNumSubObjects();
                for (int i = 0; i < count; i++)
                {
                    object o = decoder.ReadObjectContents(i);
                    if (o is ConnectorAttributeInfo.Flags)
                    {
                        ConnectorAttributeInfo.Flags f =
                            (ConnectorAttributeInfo.Flags)o;
                        flags |= f;
                    }
                }
                builder.InfoFlags = flags;
                return builder.Build();
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ConnectorAttributeInfo val = (ConnectorAttributeInfo)obj;
                encoder.WriteStringField("name", val.Name);
                encoder.WriteClassField("type", val.ValueType);
                ConnectorAttributeInfo.Flags flags = val.InfoFlags;
                foreach (Enum e in Enum.GetValues(typeof(ConnectorAttributeInfo.Flags)))
                {
                    ConnectorAttributeInfo.Flags flag =
                        (ConnectorAttributeInfo.Flags)e;
                    if ((flags & flag) != 0)
                    {
                        encoder.WriteObjectContents(flag);
                    }
                }
            }
        }

        private class ConnectorObjectHandler : AbstractObjectSerializationHandler
        {
            public ConnectorObjectHandler()
                : base(typeof(ConnectorObject), "ConnectorObject")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                ObjectClass oclass =
                    (ObjectClass)decoder.ReadObjectField("ObjectClass", typeof(ObjectClass), null);
                ICollection<Object> attsObj =
                    (ICollection<Object>)decoder.ReadObjectField("Attributes", typeof(ICollection<object>), null);
                ICollection<ConnectorAttribute> atts =
                    CollectionUtil.NewSet<Object, ConnectorAttribute>(attsObj);
                return new ConnectorObject(oclass, atts);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ConnectorObject val = (ConnectorObject)obj;
                encoder.WriteObjectField("ObjectClass", val.ObjectClass, true);
                encoder.WriteObjectField("Attributes", val.GetAttributes(), true);
            }
        }
        private class NameHandler
            : AbstractObjectSerializationHandler
        {
            public NameHandler()
                : base(typeof(Name), "Name")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                String str = decoder.ReadStringContents();
                return new Name(str);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                Name val = (Name)obj;
                encoder.WriteStringContents(val.GetNameValue());
            }
        }
        private class ObjectClassHandler : AbstractObjectSerializationHandler
        {
            public ObjectClassHandler()
                : base(typeof(ObjectClass), "ObjectClass")
            {

            }
            public override object Deserialize(ObjectDecoder decoder)
            {
                string type = decoder.ReadStringField("type", null);
                return new ObjectClass(type);
            }

            public override void Serialize(object obj, ObjectEncoder encoder)
            {
                ObjectClass val = (ObjectClass)obj;
                encoder.WriteStringField("type", val.GetObjectClassValue());
            }
        }

        private class ObjectClassInfoHandler : AbstractObjectSerializationHandler
        {
            public ObjectClassInfoHandler()
                : base(typeof(ObjectClassInfo), "ObjectClassInfo")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                string type =
                    decoder.ReadStringField("type", null);
                bool container =
                    decoder.ReadBooleanField("container", false);

                ICollection<object> attrInfoObj =
                    (ICollection<object>)decoder.ReadObjectField("AttributeInfos", typeof(ICollection<object>), null);

                ICollection<ConnectorAttributeInfo> attrInfo =
                    CollectionUtil.NewSet<object, ConnectorAttributeInfo>(attrInfoObj);

                return new ObjectClassInfo(type, attrInfo, container);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ObjectClassInfo val = (ObjectClassInfo)obj;
                encoder.WriteStringField("type", val.ObjectType);
                encoder.WriteBooleanField("container", val.IsContainer);
                encoder.WriteObjectField("AttributeInfos", val.ConnectorAttributeInfos, true);
            }
        }
        private class SchemaHandler : AbstractObjectSerializationHandler
        {
            public SchemaHandler()
                : base(typeof(Schema), "Schema")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {

                ICollection<object> objectClassesObj =
                    (ICollection<object>)decoder.ReadObjectField("ObjectClassInfos", typeof(ICollection<object>), null);
                ICollection<ObjectClassInfo> objectClasses =
                    CollectionUtil.NewSet<object, ObjectClassInfo>(objectClassesObj);
                IDictionary<String, ObjectClassInfo> objectClassesByName = new Dictionary<String, ObjectClassInfo>();
                foreach (ObjectClassInfo info in objectClasses)
                {
                    objectClassesByName[info.ObjectType] = info;
                }
                ICollection<object> operationOptionsObj =
                    (ICollection<object>)decoder.ReadObjectField("OperationOptionInfos", typeof(ICollection<object>), null);
                ICollection<OperationOptionInfo> operationOptions =
                    CollectionUtil.NewSet<object, OperationOptionInfo>(operationOptionsObj);
                IDictionary<String, OperationOptionInfo> optionsByName = new Dictionary<String, OperationOptionInfo>();
                foreach (OperationOptionInfo info in operationOptions)
                {
                    optionsByName[info.Name] = info;
                }
                IDictionary<object, object> objectClassNamesByOperationObj =
                    (IDictionary<object, object>)decoder.ReadObjectField("objectClassesByOperation", null, null);
                IDictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>>
                   objectClassesByOperation =
                    new Dictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>>();
                foreach (KeyValuePair<object, object> entry in objectClassNamesByOperationObj)
                {
                    SafeType<APIOperation> op = SafeType<APIOperation>.ForRawType((Type)entry.Key);
                    ICollection<object> namesObj =
                        (ICollection<object>)entry.Value;
                    ICollection<ObjectClassInfo> infos =
                        new HashSet<ObjectClassInfo>();
                    foreach (object name in namesObj)
                    {
                        ObjectClassInfo objectClass = CollectionUtil.GetValue(objectClassesByName, (string)name, null);
                        if (objectClass != null)
                        {
                            infos.Add(objectClass);
                        }
                    }
                    objectClassesByOperation[op] = infos;
                }
                IDictionary<object, object> optionsByOperationObj =
                       (IDictionary<object, object>)decoder.ReadObjectField("optionsByOperation", null, null);
                IDictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>>
                   optionsByOperation =
                    new Dictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>>();
                foreach (KeyValuePair<object, object> entry in optionsByOperationObj)
                {
                    SafeType<APIOperation> op = SafeType<APIOperation>.ForRawType((Type)entry.Key);
                    ICollection<object> namesObj =
                        (ICollection<object>)entry.Value;
                    ICollection<OperationOptionInfo> infos =
                        new HashSet<OperationOptionInfo>();
                    foreach (object name in namesObj)
                    {
                        OperationOptionInfo info = CollectionUtil.GetValue(optionsByName, (string)name, null);
                        if (info != null)
                        {
                            infos.Add(info);
                        }
                    }
                    optionsByOperation[op] = infos;
                }
                return new Schema(objectClasses,
                                  operationOptions,
                                  objectClassesByOperation,
                                  optionsByOperation);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                Schema val = (Schema)obj;
                encoder.WriteObjectField("ObjectClassInfos", val.ObjectClassInfo, true);
                encoder.WriteObjectField("OperationOptionInfos", val.OperationOptionInfo, true);
                IDictionary<Type, ICollection<String>>
                objectClassNamesByOperation = new Dictionary<Type, ICollection<String>>();
                IDictionary<Type, ICollection<String>>
                optionNamesByOperation = new Dictionary<Type, ICollection<String>>();


                foreach (KeyValuePair<SafeType<APIOperation>, ICollection<ObjectClassInfo>>
                entry in val.SupportedObjectClassesByOperation)
                {
                    ICollection<ObjectClassInfo> value = entry.Value;
                    ICollection<String> names = new HashSet<String>();
                    foreach (ObjectClassInfo info in value)
                    {
                        names.Add(info.ObjectType);
                    }
                    objectClassNamesByOperation[entry.Key.RawType] = names;
                }
                foreach (KeyValuePair<SafeType<APIOperation>, ICollection<OperationOptionInfo>>
                entry in val.SupportedOptionsByOperation)
                {
                    ICollection<OperationOptionInfo> value = entry.Value;
                    ICollection<String> names = new HashSet<String>();
                    foreach (OperationOptionInfo info in value)
                    {
                        names.Add(info.Name);
                    }
                    optionNamesByOperation[entry.Key.RawType] = names;
                }
                encoder.WriteObjectField("objectClassesByOperation", objectClassNamesByOperation, false);
                encoder.WriteObjectField("optionsByOperation", optionNamesByOperation, false);
            }
        }
        private class UidHandler
            : AbstractObjectSerializationHandler
        {
            public UidHandler()
                : base(typeof(Uid), "Uid")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                String str = decoder.ReadStringContents();
                return new Uid(str);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                Uid val = (Uid)obj;
                encoder.WriteStringContents(val.GetUidValue());
            }
        }
        private class ScriptHandler : AbstractObjectSerializationHandler
        {
            public ScriptHandler()
                : base(typeof(Script), "Script")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                ScriptBuilder builder = new ScriptBuilder();
                builder.ScriptLanguage = decoder.ReadStringField("scriptLanguage", null);
                builder.ScriptText = (String)decoder.ReadObjectField("scriptText", typeof(string), null);
                return builder.Build();
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                Script val = (Script)obj;
                encoder.WriteStringField("scriptLanguage", val.ScriptLanguage);
                encoder.WriteObjectField("scriptText", val.ScriptText, true);
            }
        }
        private class ScriptContextHandler : AbstractObjectSerializationHandler
        {
            public ScriptContextHandler()
                : base(typeof(ScriptContext), "ScriptContext")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                String scriptLanguage =
                    decoder.ReadStringField("scriptLanguage", null);
                IDictionary<Object, Object> arguments =
                    (IDictionary<Object, Object>)decoder.ReadObjectField("scriptArguments", null, null);
                String scriptText =
                    (String)decoder.ReadObjectField("scriptText", typeof(string), null);
                IDictionary<String, Object> arguments2 =
                    CollectionUtil.NewDictionary<object, object, string, object>(arguments);
                return new ScriptContext(scriptLanguage, scriptText, arguments2);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                ScriptContext val = (ScriptContext)obj;
                encoder.WriteStringField("scriptLanguage", val.ScriptLanguage);
                encoder.WriteObjectField("scriptArguments", val.ScriptArguments, false);
                encoder.WriteObjectField("scriptText", val.ScriptText, true);
            }
        }
        private class OperationOptionsHandler : AbstractObjectSerializationHandler
        {
            public OperationOptionsHandler()
                : base(typeof(OperationOptions), "OperationOptions")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                IDictionary<Object, Object> options =
                    (IDictionary<Object, Object>)decoder.ReadObjectField("options", null, null);
                IDictionary<string, object> options2 =
                    CollectionUtil.NewDictionary<object, object, string, object>(options);
                return new OperationOptions(options2);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                OperationOptions val = (OperationOptions)obj;
                encoder.WriteObjectField("options", val.Options, false);
            }
        }
        private class OperationOptionInfoHandler : AbstractObjectSerializationHandler
        {
            public OperationOptionInfoHandler()
                : base(typeof(OperationOptionInfo), "OperationOptionInfo")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                String name = decoder.ReadStringField("name", null);
                Type type = decoder.ReadClassField("type", null);
                return new OperationOptionInfo(name, type);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                OperationOptionInfo val = (OperationOptionInfo)obj;
                encoder.WriteStringField("name", val.Name);
                encoder.WriteClassField("type", val.OptionType);
            }
        }
        private class SyncTokenHandler : AbstractObjectSerializationHandler
        {
            public SyncTokenHandler()
                : base(typeof(SyncToken), "SyncToken")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                Object value = decoder.ReadObjectField("value", null, null);
                return new SyncToken(value);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                SyncToken val = (SyncToken)obj;
                encoder.WriteObjectField("value", val.Value, false);
            }
        }
        private class SyncDeltaHandler : AbstractObjectSerializationHandler
        {
            public SyncDeltaHandler()
                : base(typeof(SyncDelta), "SyncDelta")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                SyncDeltaBuilder builder = new SyncDeltaBuilder();
                builder.DeltaType = ((SyncDeltaType)decoder.ReadObjectField("SyncDeltaType", typeof(SyncDeltaType), null));
                builder.Token = ((SyncToken)decoder.ReadObjectField("SyncToken", typeof(SyncToken), null));
                builder.PreviousUid = ((Uid)decoder.ReadObjectField("PreviousUid", typeof(Uid), null));
                builder.Uid = ((Uid)decoder.ReadObjectField("Uid", typeof(Uid), null));
                builder.Object = ((ConnectorObject)decoder.ReadObjectField("ConnectorObject", typeof(ConnectorObject), null));
                return builder.Build();
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                SyncDelta val = (SyncDelta)obj;
                encoder.WriteObjectField("SyncDeltaType", val.DeltaType, true);
                encoder.WriteObjectField("SyncToken", val.Token, true);
                encoder.WriteObjectField("PreviousUid", val.PreviousUid, true);
                encoder.WriteObjectField("Uid", val.Uid, true);
                encoder.WriteObjectField("ConnectorObject", val.Object, true);
            }
        }
        private class QualifiedUidHandler : AbstractObjectSerializationHandler
        {
            public QualifiedUidHandler()
                : base(typeof(QualifiedUid), "QualifiedUid")
            {

            }
            public override Object Deserialize(ObjectDecoder decoder)
            {
                ObjectClass objectClass = (ObjectClass)decoder.ReadObjectField("ObjectClass", typeof(ObjectClass), null);
                Uid uid = (Uid)decoder.ReadObjectField("Uid", typeof(Uid), null);
                return new QualifiedUid(objectClass, uid);
            }

            public override void Serialize(Object obj, ObjectEncoder encoder)
            {
                QualifiedUid val = (QualifiedUid)obj;
                encoder.WriteObjectField("ObjectClass", val.ObjectClass, true);
                encoder.WriteObjectField("Uid", val.Uid, true);
            }
        }
    }
    #endregion

    #region FilterHandlers
    internal static class FilterHandlers
    {
        public static readonly IList<ObjectTypeMapper> HANDLERS =
        new List<ObjectTypeMapper>();
        static FilterHandlers()
        {
            HANDLERS.Add(new AndFilterHandler());
            HANDLERS.Add(new ContainsFilterHandler());
            HANDLERS.Add(new EndsWithFilterHandler());
            HANDLERS.Add(new EqualsFilterHandler());
            HANDLERS.Add(new GreaterThanFilterHandler());
            HANDLERS.Add(new GreaterThanOrEqualFilterHandler());
            HANDLERS.Add(new LessThanFilterHandler());
            HANDLERS.Add(new LessThanOrEqualFilterHandler());
            HANDLERS.Add(new NotFilterHandler());
            HANDLERS.Add(new OrFilterHandler());
            HANDLERS.Add(new StartsWithFilterHandler());
            HANDLERS.Add(new ContainsAllValuesFilterHandler());
        }

        private abstract class CompositeFilterHandler<T>
            : AbstractObjectSerializationHandler
            where T : CompositeFilter
        {

            protected CompositeFilterHandler(String typeName)
                : base(typeof(T), typeName)
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                Filter left = (Filter)decoder.ReadObjectContents(0);
                Filter right = (Filter)decoder.ReadObjectContents(1);
                return CreateFilter(left, right);
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
                CompositeFilter val = (CompositeFilter)obj;
                encoder.WriteObjectContents(val.Left);
                encoder.WriteObjectContents(val.Right);
            }

            protected abstract T CreateFilter(Filter left, Filter right);
        }

        private abstract class AttributeFilterHandler<T>
            : AbstractObjectSerializationHandler
            where T : AttributeFilter
        {

            protected AttributeFilterHandler(String typeName)
                : base(typeof(T), typeName)
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                ConnectorAttribute attribute = (ConnectorAttribute)decoder.ReadObjectField("attribute", null, null);
                return CreateFilter(attribute);
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
                AttributeFilter val = (AttributeFilter)obj;
                encoder.WriteObjectField("attribute", val.GetAttribute(), false);
            }

            protected abstract T CreateFilter(ConnectorAttribute attribute);
        }




        private class AndFilterHandler : CompositeFilterHandler<AndFilter>
        {
            public AndFilterHandler()
                : base("AndFilter")
            {
            }
            protected override AndFilter CreateFilter(Filter left, Filter right)
            {
                return new AndFilter(left, right);
            }
        }

        private class ContainsFilterHandler : AttributeFilterHandler<ContainsFilter>
        {
            public ContainsFilterHandler()
                : base("ContainsFilter")
            {
            }
            protected override ContainsFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new ContainsFilter(attribute);
            }
        }

        private class EndsWithFilterHandler : AttributeFilterHandler<EndsWithFilter>
        {
            public EndsWithFilterHandler()
                : base("EndsWithFilter")
            {
            }
            protected override EndsWithFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new EndsWithFilter(attribute);
            }
        }

        private class EqualsFilterHandler : AttributeFilterHandler<EqualsFilter>
        {
            public EqualsFilterHandler()
                : base("EqualsFilter")
            {
            }
            protected override EqualsFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new EqualsFilter(attribute);
            }
        }

        private class GreaterThanFilterHandler : AttributeFilterHandler<GreaterThanFilter>
        {
            public GreaterThanFilterHandler()
                : base("GreaterThanFilter")
            {
            }
            protected override GreaterThanFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new GreaterThanFilter(attribute);
            }
        }

        private class GreaterThanOrEqualFilterHandler : AttributeFilterHandler<GreaterThanOrEqualFilter>
        {
            public GreaterThanOrEqualFilterHandler()
                : base("GreaterThanOrEqualFilter")
            {
            }
            protected override GreaterThanOrEqualFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new GreaterThanOrEqualFilter(attribute);
            }
        }
        private class LessThanFilterHandler : AttributeFilterHandler<LessThanFilter>
        {
            public LessThanFilterHandler()
                : base("LessThanFilter")
            {
            }
            protected override LessThanFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new LessThanFilter(attribute);
            }
        }
        private class LessThanOrEqualFilterHandler : AttributeFilterHandler<LessThanOrEqualFilter>
        {
            public LessThanOrEqualFilterHandler()
                : base("LessThanOrEqualFilter")
            {
            }
            protected override LessThanOrEqualFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new LessThanOrEqualFilter(attribute);
            }
        }
        private class NotFilterHandler
            : AbstractObjectSerializationHandler
        {

            public NotFilterHandler()
                : base(typeof(NotFilter), "NotFilter")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                Filter filter =
                    (Filter)decoder.ReadObjectContents(0);
                return new NotFilter(filter);
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
                NotFilter val = (NotFilter)obj;
                encoder.WriteObjectContents(val.Filter);
            }

        }
        private class OrFilterHandler : CompositeFilterHandler<OrFilter>
        {
            public OrFilterHandler()
                : base("OrFilter")
            {
            }
            protected override OrFilter CreateFilter(Filter left, Filter right)
            {
                return new OrFilter(left, right);
            }
        }
        private class StartsWithFilterHandler : AttributeFilterHandler<StartsWithFilter>
        {
            public StartsWithFilterHandler()
                : base("StartsWithFilter")
            {
            }
            protected override StartsWithFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new StartsWithFilter(attribute);
            }
        }

        private class ContainsAllValuesFilterHandler : AttributeFilterHandler<ContainsAllValuesFilter>
        {
            public ContainsAllValuesFilterHandler()
                : base("ContainsAllValuesFilter")
            {
            }
            protected override ContainsAllValuesFilter CreateFilter(ConnectorAttribute attribute)
            {
                return new ContainsAllValuesFilter(attribute);
            }
        }
    }
    #endregion

    #region MessageHandlers
    internal static class MessageHandlers
    {
        public static readonly IList<ObjectTypeMapper> HANDLERS =
        new List<ObjectTypeMapper>();
        static MessageHandlers()
        {
            HANDLERS.Add(new HelloRequestHandler());
            HANDLERS.Add(new HelloResponseHandler());
            HANDLERS.Add(new OperationRequestHandler());
            HANDLERS.Add(new OperationResponseEndHandler());
            HANDLERS.Add(new OperationResponsePartHandler());
            HANDLERS.Add(new OperationRequestMoreDataHandler());
            HANDLERS.Add(new OperationRequestStopDataHandler());
            HANDLERS.Add(new OperationResponsePauseHandler());
            HANDLERS.Add(new EchoMessageHandler());
        }
        private class HelloRequestHandler
            : AbstractObjectSerializationHandler
        {

            public HelloRequestHandler()
                : base(typeof(HelloRequest), "HelloRequest")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                return new HelloRequest();
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
            }

        }
        private class HelloResponseHandler
            : AbstractObjectSerializationHandler
        {

            public HelloResponseHandler()
                : base(typeof(HelloResponse), "HelloResponse")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                Exception exception =
                    (Exception)decoder.ReadObjectField("exception", null, null);
                IList<object> connectorInfosObj =
                    (IList<object>)decoder.ReadObjectField("ConnectorInfos", typeof(IList<object>), null);
                IList<RemoteConnectorInfoImpl> connectorInfos =
                    CollectionUtil.NewList<object, RemoteConnectorInfoImpl>(connectorInfosObj);
                return new HelloResponse(exception, connectorInfos);
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
                HelloResponse val = (HelloResponse)obj;
                encoder.WriteObjectField("exception", val.Exception, false);
                encoder.WriteObjectField("ConnectorInfos", val.ConnectorInfos, true);
            }

        }
        private class OperationRequestHandler
            : AbstractObjectSerializationHandler
        {

            public OperationRequestHandler()
                : base(typeof(OperationRequest), "OperationRequest")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                ConnectorKey connectorKey =
                    (ConnectorKey)decoder.ReadObjectField("ConnectorKey", typeof(ConnectorKey), null);
                APIConfigurationImpl configuration =
                    (APIConfigurationImpl)decoder.ReadObjectField("APIConfiguration", typeof(APIConfigurationImpl), null);
                Type operation =
                    decoder.ReadClassField("operation", null);
                string operationMethodName =
                    decoder.ReadStringField("operationMethodName", null);
                IList<object> arguments = (IList<object>)
                    decoder.ReadObjectField("Arguments", typeof(IList<object>), null);
                return new OperationRequest(connectorKey,
                        configuration,
                        SafeType<APIOperation>.ForRawType(operation),
                        operationMethodName,
                        arguments);
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
                OperationRequest val =
                    (OperationRequest)obj;
                encoder.WriteClassField("operation",
                        val.Operation.RawType);
                encoder.WriteStringField("operationMethodName",
                                         val.OperationMethodName);
                encoder.WriteObjectField("ConnectorKey",
                        val.ConnectorKey, true);
                encoder.WriteObjectField("APIConfiguration",
                        val.Configuration, true);
                encoder.WriteObjectField("Arguments",
                        val.Arguments, true);
            }

        }
        private class OperationResponseEndHandler
            : AbstractObjectSerializationHandler
        {

            public OperationResponseEndHandler()
                : base(typeof(OperationResponseEnd), "OperationResponseEnd")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                return new OperationResponseEnd();
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
            }
        }
        private class OperationResponsePartHandler
            : AbstractObjectSerializationHandler
        {

            public OperationResponsePartHandler()
                : base(typeof(OperationResponsePart), "OperationResponsePart")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                Exception exception =
                    (Exception)decoder.ReadObjectField("exception", null, null);
                Object result =
                    decoder.ReadObjectField("result", null, null);

                return new OperationResponsePart(exception, result);
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
                OperationResponsePart val = (OperationResponsePart)obj;
                encoder.WriteObjectField("exception", val.Exception, false);
                encoder.WriteObjectField("result", val.Result, false);
            }
        }
        private class OperationRequestMoreDataHandler
            : AbstractObjectSerializationHandler
        {

            public OperationRequestMoreDataHandler()
                : base(typeof(OperationRequestMoreData), "OperationRequestMoreData")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                return new OperationRequestMoreData();
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
            }
        }
        private class OperationRequestStopDataHandler
            : AbstractObjectSerializationHandler
        {

            public OperationRequestStopDataHandler()
                : base(typeof(OperationRequestStopData), "OperationRequestStopData")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                return new OperationRequestStopData();
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
            }
        }
        private class OperationResponsePauseHandler
            : AbstractObjectSerializationHandler
        {

            public OperationResponsePauseHandler()
                : base(typeof(OperationResponsePause), "OperationResponsePause")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                return new OperationResponsePause();
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
            }
        }
        private class EchoMessageHandler
            : AbstractObjectSerializationHandler
        {

            public EchoMessageHandler()
                : base(typeof(EchoMessage), "EchoMessage")
            {
            }


            public override sealed Object Deserialize(ObjectDecoder decoder)
            {
                return new EchoMessage(decoder.ReadObjectField("value", null, null),
                                       (String)decoder.ReadObjectField("objectXml", typeof(string), null));
            }

            public override sealed void Serialize(Object obj, ObjectEncoder encoder)
            {
                EchoMessage val = (EchoMessage)obj;
                encoder.WriteObjectField("value", val.Object, false);
                encoder.WriteObjectField("objectXml", val.ObjectXml, true);
            }
        }
    }
    #endregion
}