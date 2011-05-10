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
using Org.IdentityConnectors.Common;
namespace Org.IdentityConnectors.Framework.Common.Serializer
{
    /// <summary>
    /// Interface for reading objects from a stream.
    /// </summary>
    public interface BinaryObjectDeserializer
    {
        /// <summary>
        /// Reads the next object from the stream.
        /// </summary>
        /// <remarks>
        /// Throws
        /// a wrapped <see cref="EndOfStreamException" /> if end of stream is reached.
        /// </remarks>
        /// <returns>The next object from the stream.</returns>
        object ReadObject();

        /// <summary>
        /// Closes the underlying stream
        /// </summary>
        void Close();
    }
    /// <summary>
    /// Interface for writing objects to a stream.
    /// </summary>
    public interface BinaryObjectSerializer
    {
        /// <summary>
        /// Writes the next object to the stream.
        /// </summary>
        /// <param name="obj">The object to write.</param>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        void WriteObject(object obj);

        /// <summary>
        /// Flushes the underlying stream.
        /// </summary>
        void Flush();

        /// <summary>
        /// Closes the underylying stream after first flushing it.
        /// </summary>
        void Close();
    }

    /// <summary>
    /// Serializer factory for serializing connector objects.
    /// </summary>
    /// <remarks>
    /// The list of
    /// supported types are as follows:
    /// TODO: list supported types
    /// <list type="bullet">
    /// </list>
    /// </remarks>
    /// <seealso cref="SerializerUtil" />
    public abstract class ObjectSerializerFactory
    {
        // At some point we might make this pluggable, but for now, hard-code
        private const string IMPL_NAME = "Org.IdentityConnectors.Framework.Impl.Serializer.ObjectSerializerFactoryImpl";

        private static ObjectSerializerFactory _instance;

        private static readonly Object LOCK = new Object();


        /// <summary>
        /// Get the singleton instance of the <see cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />.
        /// </summary>
        public static ObjectSerializerFactory GetInstance()
        {
            lock (LOCK)
            {
                if (_instance == null)
                {
                    SafeType<ObjectSerializerFactory> t =
                        FrameworkInternalBridge.LoadType<ObjectSerializerFactory>(IMPL_NAME);

                    _instance = t.CreateInstance();
                }
                return _instance;
            }
        }

        /// <summary>
        /// Creates a <code>BinaryObjectSerializer</code> for writing objects to
        /// the given stream.
        /// </summary>
        /// <remarks>
        /// NOTE: consider using <see cref="SerializerUtil.SerializeBinaryObject(Object)" />
        /// for convenience serializing a single object.
        /// 
        /// NOTE2: do not mix and match <see cref="SerializerUtil.SerializeBinaryObject(Object)" />
        /// with {<see cref="NewBinaryDeserializer(Stream)" />. This is unsafe since there
        /// is header information and state associated with the stream. Objects written
        /// using one method must be read using the proper corresponding method.
        /// </remarks>
        /// <param name="os">The stream</param>
        /// <returns>The serializer</returns>
        public abstract BinaryObjectSerializer NewBinarySerializer(Stream os);

        /// <summary>
        /// Creates a <code>BinaryObjectDeserializer</code> for reading objects from
        /// the given stream.
        /// </summary>
        /// <remarks>
        /// NOTE: consider using <see cref="SerializerUtil.DeserializeBinaryObject(byte[])" />
        /// for convenience deserializing a single object.
        /// NOTE2: do not mix and match <see cref="SerializerUtil.DeserializeBinaryObject(byte[])" />
        /// with {<see cref="NewBinarySerializer(Stream)" />. This is unsafe since there
        /// is header information and state associated with the stream. Objects written
        /// using one method must be read using the proper corresponding method.
        /// </remarks>
        /// <param name="os">The stream</param>
        /// <returns>The deserializer</returns>
        public abstract BinaryObjectDeserializer NewBinaryDeserializer(Stream i);

        /// <summary>
        /// Creates a <code>BinaryObjectSerializer</code> for writing objects to
        /// the given stream.
        /// </summary>
        /// <remarks>
        /// NOTE: consider using <see cref="SerializerUtil.SerializeXmlObject(Object,bool)" />
        /// for convenience serializing a single object.
        /// 
        /// NOTE2: do not mix and match <see cref="SerializerUtil.SerializeXmlObject(Object,bool)" />
        /// with {<see cref="DeserializeXmlStream(TextReader, XmlObjectResultsHandler, bool)" />.
        /// </remarks>
        /// <param name="w">The writer</param>
        /// <param name="includeHeader">True to include the xml header</param>
        /// <param name="multiObject">Is this to produce a multi-object document. If false, only
        /// a single object may be written.</param>
        /// <returns>The serializer</returns>
        public abstract XmlObjectSerializer NewXmlSerializer(TextWriter w,
                bool includeHeader,
                bool multiObject);

        /// <summary>
        /// Deserializes XML objects from a stream
        /// NOTE: Consider using <see cref="SerializerUtil.DeserializeXmlObject(String,bool)" />
        /// for convenience deserializing a single object.
        /// </summary>
        /// <remarks>
        /// NOTE2: Do not mix and match <see cref="SerializerUtil.DeserializeXmlObject(String,bool)" />
        /// with {<see cref="NewXmlSerializer(TextWriter, bool, bool)" />.
        /// </remarks>
        /// <param name="is">The input source</param>
        /// <param name="handler">The callback to receive objects from the stream</param>
        /// <param name="validate">True iff we are to validate</param>
        public abstract void DeserializeXmlStream(TextReader reader,
                XmlObjectResultsHandler handler,
                bool validate);

    }

    /// <summary>
    /// Bag of utilities for serialization
    /// </summary>
    public static class SerializerUtil
    {
        /// <summary>
        /// Serializes the given object to bytes
        /// </summary>
        /// <param name="object">The object to serialize</param>
        /// <returns>The bytes</returns>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        public static byte[] SerializeBinaryObject(object obj)
        {
            ObjectSerializerFactory fact = ObjectSerializerFactory.GetInstance();
            MemoryStream mem = new MemoryStream();
            BinaryObjectSerializer ser = fact.NewBinarySerializer(mem);
            ser.WriteObject(obj);
            ser.Close();
            return mem.ToArray();
        }

        /// <summary>
        /// Deserializes the given object from bytes
        /// </summary>
        /// <param name="bytes">The bytes to deserialize</param>
        /// <returns>The object</returns>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        public static object DeserializeBinaryObject(byte[] bytes)
        {
            ObjectSerializerFactory fact = ObjectSerializerFactory.GetInstance();
            MemoryStream mem = new MemoryStream(bytes);
            BinaryObjectDeserializer des = fact.NewBinaryDeserializer(mem);
            return des.ReadObject();
        }

        /// <summary>
        /// Serializes the given object to xml
        /// </summary>
        /// <param name="object">The object to serialize</param>
        /// <param name="includeHeader">True if we are to include the xml header.</param>
        /// <returns>The xml</returns>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        public static String SerializeXmlObject(Object obj, bool includeHeader)
        {
            ObjectSerializerFactory fact = ObjectSerializerFactory.GetInstance();
            StringWriter w = new StringWriter();
            XmlObjectSerializer ser = fact.NewXmlSerializer(w, includeHeader, false);
            ser.WriteObject(obj);
            ser.Close(true);
            return w.ToString();
        }

        /// <summary>
        /// Deserializes the given object from xml
        /// </summary>
        /// <param name="bytes">The xml to deserialize</param>
        /// <param name="validate">True if we are to validate the xml</param>
        /// <returns>The object</returns>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        public static Object DeserializeXmlObject(String str, bool validate)
        {
            ObjectSerializerFactory fact = ObjectSerializerFactory.GetInstance();
            StringReader source = new StringReader(str);
            IList<Object> rv = new List<Object>();
            fact.DeserializeXmlStream(source,
                    obj =>
                    {
                        rv.Add(obj);
                        return true;
                    }, validate);
            if (rv.Count > 0)
            {
                return rv[0];
            }
            else
            {
                return null;
            }
        }

        /// <summary>
        /// Clones the given object by serializing it to bytes and then
        /// deserializing it.
        /// </summary>
        /// <param name="object">The object.</param>
        /// <returns>A clone of the object</returns>
        public static object CloneObject(Object obj)
        {
            byte[] bytes = SerializeBinaryObject(obj);
            return DeserializeBinaryObject(bytes);
        }

    }

    /// <summary>
    /// Callback interface to receive xml objects from a stream of objects.
    /// </summary>
    public delegate bool XmlObjectResultsHandler(Object obj);

    /// <summary>
    /// Interface for writing objects to a stream.
    /// </summary>
    public interface XmlObjectSerializer
    {
        /// <summary>
        /// Writes the next object to the stream.
        /// </summary>
        /// <param name="object">The object to write.</param>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        /// <exception cref="Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException">if there is more than one object
        /// and this is not configured for multi-object document.</exception>
        void WriteObject(Object obj);

        /// <summary>
        /// Flushes the underlying stream.
        /// </summary>
        void Flush();

        /// <summary>
        /// Adds document end tag and optinally closes the underlying stream
        /// </summary>
        void Close(bool closeUnderlyingStream);
    }
}