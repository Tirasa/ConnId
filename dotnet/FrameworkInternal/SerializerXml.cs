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
using System.IO;
using System.Resources;
using System.Text;
using System.Net;
using System.Xml;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Serializer;
namespace Org.IdentityConnectors.Framework.Impl.Serializer.Xml
{
    public class XmlObjectEncoder : ObjectEncoder
    {
        private StringBuilder _rootBuilder;
        private XmlWriter _writer;

        public XmlObjectEncoder(StringBuilder builder)
        {
            Assertions.NullCheck(builder, "builder");
            _rootBuilder = builder;
        }

        public String WriteObject(Object o)
        {
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Indent = true;
            settings.OmitXmlDeclaration = true;
            _writer = XmlWriter.Create(_rootBuilder, settings);
            String rv = WriteObjectInternal(o, false);
            _writer.Close();
            return rv;
        }

        public void WriteBooleanContents(bool v)
        {
            WriteStringContentsInternal(EncodeBoolean(v));
        }

        public void WriteBooleanField(String fieldName, bool v)
        {
            WriteAttributeInternal(fieldName, EncodeBoolean(v));
        }

        public void WriteByteArrayContents(byte[] v)
        {
            WriteStringContentsInternal(EncodeByteArray(v));
        }

        public void WriteClassContents(Type v)
        {
            WriteStringContentsInternal(EncodeClass(v));
        }

        public void WriteClassField(String name, Type v)
        {
            if (v != null)
            {
                WriteAttributeInternal(name, EncodeClass(v));
            }
        }

        public void WriteDoubleContents(double v)
        {
            WriteStringContentsInternal(EncodeDouble(v));
        }

        public void WriteDoubleField(String fieldName, double v)
        {
            WriteAttributeInternal(fieldName, EncodeDouble(v));
        }

        public void WriteFloatContents(float v)
        {
            WriteStringContentsInternal(EncodeFloat(v));
        }

        public void WriteFloatField(String fieldName, float v)
        {
            WriteAttributeInternal(fieldName, EncodeFloat(v));
        }

        public void WriteIntContents(int v)
        {
            WriteStringContentsInternal(EncodeInt(v));
        }

        public void WriteIntField(String fieldName, int v)
        {
            WriteAttributeInternal(fieldName, EncodeInt(v));
        }

        public void WriteLongContents(long v)
        {
            WriteStringContentsInternal(EncodeLong(v));
        }

        public void WriteLongField(String fieldName, long v)
        {
            WriteAttributeInternal(fieldName, EncodeLong(v));
        }

        public void WriteObjectContents(Object o)
        {
            WriteObjectInternal(o, false);
        }

        public void WriteObjectField(String fieldName, Object obj, bool inline)
        {
            if (inline && obj == null)
            {
                return; //don't write anything
            }
            BeginElement(fieldName);
            WriteObjectInternal(obj, inline);
            EndElement();
        }

        public void WriteStringContents(String str)
        {
            WriteStringContentsInternal(str);
        }

        public void WriteStringField(String fieldName, String str)
        {
            if (str != null)
            {
                WriteAttributeInternal(fieldName, str);
            }
        }

        internal static String EncodeBoolean(bool b)
        {
            return b.ToString();
        }

        private static String EncodeByteArray(byte[] bytes)
        {
            return Convert.ToBase64String(bytes);
        }

        private static String EncodeClass(Type clazz)
        {
            ObjectSerializationHandler handler =
                ObjectSerializerRegistry.GetHandlerByObjectType(clazz);
            ObjectTypeMapper mapper =
                ObjectSerializerRegistry.GetMapperByObjectType(clazz);
            if (handler == null && clazz.IsArray)
            {
                //we may have special handlers for certain types of arrays
                //if handler is null, treat like any other array
                return EncodeClass(clazz.GetElementType()) + "[]";
            }
            else if (mapper == null)
            {
                throw new ConnectorException("No serializer for class: " + clazz);
            }
            else
            {
                String typeName = mapper.HandledSerialType;
                return typeName;
            }
        }

        internal static String EncodeDouble(double d)
        {
            return d.ToString("R");
        }

        internal static String EncodeFloat(float d)
        {
            return d.ToString("R");
        }

        internal static String EncodeInt(int d)
        {
            return d.ToString();
        }

        internal static String EncodeLong(long d)
        {
            return d.ToString();
        }

        /// <summary>
        /// Writes the object
        /// </summary>
        /// <param name="object"></param>
        /// <param name="inline"></param>
        /// <returns>The type name (regardless of whether it was inlined)</returns>
        String WriteObjectInternal(Object obj, bool inline)
        {
            if (obj == null)
            {
                if (inline)
                {
                    throw new ArgumentException("null cannot be inlined");
                }
                BeginElement("null");
                EndElement();
                return "null";
            }
            else
            {
                Type clazz = obj.GetType();
                ObjectSerializationHandler handler =
                    ObjectSerializerRegistry.GetHandlerByObjectType(clazz);
                if (handler == null)
                {
                    //we may have special handlers for certain types of arrays
                    //if handler is null, treat like any other array
                    if (clazz.IsArray)
                    {
                        if (!inline)
                        {
                            String componentTypeName = EncodeClass(clazz.GetElementType());
                            BeginElement("Array");
                            WriteAttributeInternal("componentType", componentTypeName);
                        }
                        Array array = (Array)obj;
                        int length = array.Length;
                        for (int i = 0; i < length; i++)
                        {
                            Object val = array.GetValue(i);
                            WriteObjectInternal(val, false);
                        }
                        if (!inline)
                        {
                            EndElement();
                        }
                        return "Array";
                    }
                    else
                    {
                        throw new ConnectorException("No serializer for class: " + clazz);
                    }
                }
                else
                {
                    String typeName = EncodeClass(clazz);
                    if (!inline)
                    {
                        BeginElement(typeName);
                    }
                    handler.Serialize(obj, this);
                    if (!inline)
                    {
                        EndElement();
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

        private void BeginElement(String name)
        {
            _writer.WriteStartElement(name);
        }

        private void EndElement()
        {
            _writer.WriteEndElement();
        }
        private void WriteAttributeInternal(String fieldName, String str)
        {
            _writer.WriteAttributeString(fieldName, str);
        }
        private void WriteStringContentsInternal(String str)
        {
            _writer.WriteString(str);
        }
    }

    public class XmlObjectDecoder : ObjectDecoder
    {

        private readonly XmlElement _node;
        private readonly Type _expectedClass;

        public XmlObjectDecoder(XmlElement node, Type expectedClass)
        {
            _node = node;
            _expectedClass = expectedClass;
        }

        public Object ReadObject()
        {
            return ReadObjectInternal();
        }

        public bool ReadBooleanContents()
        {
            return DecodeBoolean(ReadStringContentsInternal());
        }

        public bool ReadBooleanField(String fieldName, bool dflt)
        {
            return DecodeBoolean(ReadStringAttributeInternal(fieldName, XmlObjectEncoder.EncodeBoolean(dflt)));
        }

        public byte[] ReadByteArrayContents()
        {
            return DecodeByteArray(ReadStringContentsInternal());
        }

        public Type ReadClassContents()
        {
            return DecodeClass(ReadStringContentsInternal());
        }

        public Type ReadClassField(String name, Type dflt)
        {
            String val = ReadStringAttributeInternal(name, null);
            if (val == null)
            {
                return dflt;
            }
            else
            {
                return DecodeClass(val);
            }
        }

        public double ReadDoubleContents()
        {
            return DecodeDouble(ReadStringContentsInternal());
        }

        public double ReadDoubleField(String fieldName, double dflt)
        {
            return DecodeDouble(ReadStringAttributeInternal(fieldName, XmlObjectEncoder.EncodeDouble(dflt)));
        }

        public float ReadFloatContents()
        {
            return DecodeFloat(ReadStringContentsInternal());
        }

        public float ReadFloatField(String fieldName, float dflt)
        {
            return DecodeFloat(ReadStringAttributeInternal(fieldName, XmlObjectEncoder.EncodeFloat(dflt)));
        }

        public int ReadIntContents()
        {
            return DecodeInt(ReadStringContentsInternal());
        }

        public int ReadIntField(String fieldName, int dflt)
        {
            return DecodeInt(ReadStringAttributeInternal(fieldName, XmlObjectEncoder.EncodeInt(dflt)));
        }

        public long ReadLongContents()
        {
            return DecodeLong(ReadStringContentsInternal());
        }

        public long ReadLongField(String fieldName, long dflt)
        {
            return DecodeLong(ReadStringAttributeInternal(fieldName, XmlObjectEncoder.EncodeLong(dflt)));
        }

        public int GetNumSubObjects()
        {
            int count = 0;
            for (XmlElement subElement = XmlUtil.GetFirstChildElement(_node);
                 subElement != null;
                 subElement = XmlUtil.GetNextElement(subElement))
            {
                count++;
            }
            return count;
        }

        public Object ReadObjectContents(int index)
        {
            XmlElement subElement = XmlUtil.GetFirstChildElement(_node);
            for (int i = 0; i < index; i++)
            {
                subElement = XmlUtil.GetNextElement(subElement);
            }

            if (subElement == null)
            {
                throw new ConnectorException("Missing subelement number: " + index);
            }

            return new XmlObjectDecoder(subElement, null).ReadObject();
        }

        public Object ReadObjectField(String fieldName, Type expected, Object dflt)
        {
            XmlElement child = XmlUtil.FindImmediateChildElement(_node, fieldName);
            if (child == null)
            {
                return dflt;
            }
            if (expected != null)
            {
                return new XmlObjectDecoder(child, expected).ReadObject();
            }
            XmlElement subElement = XmlUtil.GetFirstChildElement(child);
            if (subElement == null)
            {
                return dflt;
            }
            //if they specify null, don't apply defaults
            return new XmlObjectDecoder(subElement, null).ReadObject();
        }

        public String ReadStringContents()
        {
            String rv = ReadStringContentsInternal();
            return rv == null ? "" : rv;
        }

        public String ReadStringField(String fieldName, String dflt)
        {
            return ReadStringAttributeInternal(fieldName, dflt);
        }

        private String ReadStringContentsInternal()
        {
            String xml = XmlUtil.GetContent(_node);
            return xml;
        }

        private String ReadStringAttributeInternal(String name, String dflt)
        {
            XmlAttribute attr = _node.GetAttributeNode(name);
            if (attr == null)
            {
                return dflt;
            }
            return attr.Value;
        }

        private bool DecodeBoolean(String v)
        {
            return Boolean.Parse(v);
        }

        private byte[] DecodeByteArray(String base64)
        {
            return Convert.FromBase64String(base64);
        }

        private Type DecodeClass(String type)
        {
            if (type.EndsWith("[]"))
            {
                String componentName = type.Substring(0, type.Length - "[]".Length);
                Type componentClass =
                    DecodeClass(componentName);
                Type arrayClass =
                    componentClass.MakeArrayType();
                return arrayClass;
            }
            else
            {
                ObjectTypeMapper mapper =
                    ObjectSerializerRegistry.GetMapperBySerialType(type);
                if (mapper == null)
                {
                    throw new ConnectorException("No deserializer for type: " + type);
                }
                Type clazz = mapper.HandledObjectType;
                return clazz;
            }
        }

        private double DecodeDouble(String val)
        {
            return Double.Parse(val);
        }

        private float DecodeFloat(String val)
        {
            return Single.Parse(val);
        }

        private int DecodeInt(String val)
        {
            return Int32.Parse(val);
        }

        private long DecodeLong(String val)
        {
            return Int64.Parse(val);
        }

        private Object ReadObjectInternal()
        {
            if (_expectedClass != null)
            {
                ObjectSerializationHandler handler =
                    ObjectSerializerRegistry.GetHandlerByObjectType(_expectedClass);
                if (handler == null)
                {
                    if (_expectedClass.IsArray)
                    {
                        IList<Object> temp = new List<Object>();
                        for (XmlElement child = XmlUtil.GetFirstChildElement(_node); child != null;
                             child = XmlUtil.GetNextElement(child))
                        {
                            XmlObjectDecoder sub = new XmlObjectDecoder(child, null);
                            Object obj = sub.ReadObject();
                            temp.Add(obj);
                        }
                        int length = temp.Count;
                        Array array = Array.CreateInstance(_expectedClass.GetElementType(), length);
                        for (int i = 0; i < length; i++)
                        {
                            Object element = temp[i];
                            array.SetValue(element, i);
                        }
                        return array;
                    }
                    else
                    {
                        throw new ConnectorException("No deserializer for type: " + _expectedClass);
                    }
                }
                else
                {
                    return handler.Deserialize(this);
                }
            }
            else if (_node.LocalName.Equals("null"))
            {
                return null;
            }
            else if (_node.LocalName.Equals("Array"))
            {
                String componentType = XmlUtil.GetAttribute(_node, "componentType");
                if (componentType == null)
                {
                    componentType = "Object";
                }
                Type componentClass = DecodeClass(componentType);
                IList<Object> temp = new List<Object>();
                for (XmlElement child = XmlUtil.GetFirstChildElement(_node); child != null;
                     child = XmlUtil.GetNextElement(child))
                {
                    XmlObjectDecoder sub = new XmlObjectDecoder(child, null);
                    Object obj = sub.ReadObject();
                    temp.Add(obj);
                }
                int length = temp.Count;
                Array array = Array.CreateInstance(componentClass,
                        length);
                for (int i = 0; i < length; i++)
                {
                    Object element = temp[i];
                    array.SetValue(element, i);
                }
                return array;
            }
            else
            {
                Type clazz =
                    DecodeClass(_node.LocalName);
                ObjectSerializationHandler handler =
                    ObjectSerializerRegistry.GetHandlerByObjectType(clazz);
                if (handler == null)
                {
                    throw new ConnectorException("No deserializer for type: " + clazz);
                }
                else
                {
                    return handler.Deserialize(this);
                }
            }
        }
    }

    public class XmlObjectParser
    {
        public static void parse(TextReader inputSource,
                XmlObjectResultsHandler handler,
                bool validate)
        {
            XmlReaderSettings mySettings =
                new XmlReaderSettings();
            if (validate)
            {
                mySettings.ValidationType = ValidationType.DTD;
            }
            mySettings.ProhibitDtd = false;
            mySettings.XmlResolver = new MyEntityResolver(validate);
            XmlReader reader = XmlReader.Create(inputSource, mySettings);
            MyParser parser = new MyParser(handler);
            parser.Parse(reader);
        }

        private class MyEntityResolver : XmlResolver
        {
            private readonly bool _validate;

            public MyEntityResolver(bool validate)
            {
                _validate = validate;
            }

            public override Object GetEntity(Uri absoluteUri, string role, Type ofObject)
            {
                String text = null;
                if (absoluteUri.AbsolutePath.EndsWith(XmlObjectSerializerImpl.CONNECTORS_DTD))
                {
                    if (!_validate)
                    {
                        text = "<?xml version='1.0' encoding='UTF-8'?>";
                    }
                    else
                    {
                        text = GetDTD();
                    }
                }
                if (text != null)
                {
                    byte[] bytes = Encoding.UTF8.GetBytes(text);
                    return new MemoryStream(bytes);
                }
                return null;
            }

            public override ICredentials Credentials
            {
                set
                {

                }
            }
            private static String GetDTD()
            {
                ResourceManager manager =
                    new ResourceManager("Org.IdentityConnectors.Resources",
                                        typeof(XmlObjectParser).Assembly);
                String contents = (String)manager.GetObject(XmlObjectSerializerImpl.CONNECTORS_DTD);
                return contents;
            }
        }

        private class MyParser
        {
            /// <summary>
            /// The document for the current top-level element.
            /// </summary>
            /// <remarks>
            /// with each top-level element,
            /// we discard the previous to avoid accumulating memory
            /// </remarks>
            private XmlDocument _currentTopLevelElementDocument;


            /// <summary>
            /// Stack of elements we are creating
            /// </summary>
            private IList<XmlElement> _elementStack = new List<XmlElement>(10);

            /// <summary>
            /// Results handler that we write our objects to
            /// </summary>
            private readonly XmlObjectResultsHandler _handler;

            /// <summary>
            /// Is the handler still handing
            /// </summary>
            private bool _stillHandling = true;


            public MyParser(XmlObjectResultsHandler handler)
            {
                _handler = handler;
            }

            public void Parse(XmlReader reader)
            {
                while (_stillHandling && reader.Read())
                {
                    XmlNodeType nodeType = reader.NodeType;
                    switch (nodeType)
                    {
                        case XmlNodeType.Element:
                            StartElement(reader.LocalName);
                            bool empty = reader.IsEmptyElement;
                            if (reader.MoveToFirstAttribute())
                            {
                                AddAttribute(reader.LocalName, reader.Value);
                                while (reader.MoveToNextAttribute())
                                {
                                    AddAttribute(reader.LocalName, reader.Value);
                                }
                            }
                            if (empty)
                            {
                                EndElement();
                            }
                            break;
                        case XmlNodeType.Text:
                        case XmlNodeType.CDATA:
                        case XmlNodeType.Whitespace:
                        case XmlNodeType.SignificantWhitespace:
                            AddText(reader.Value);
                            break;
                        case XmlNodeType.EndElement:
                            EndElement();
                            break;
                    }
                }
            }

            private XmlElement GetCurrentElement()
            {
                if (_elementStack.Count > 0)
                {
                    return _elementStack[_elementStack.Count - 1];
                }
                else
                {
                    return null;
                }
            }

            private void AddText(String text)
            {
                XmlElement currentElement = GetCurrentElement();
                if (currentElement != null)
                {
                    currentElement.AppendChild(_currentTopLevelElementDocument.CreateTextNode(text));
                }
            }

            private void EndElement()
            {
                if (_elementStack.Count > 0) //we don't push the top-level MULTI_OBJECT_ELEMENT on the stack
                {
                    XmlElement element = _elementStack[_elementStack.Count - 1];
                    _elementStack.RemoveAt(_elementStack.Count - 1);
                    if (_elementStack.Count == 0)
                    {
                        _currentTopLevelElementDocument = null;
                        if (_stillHandling)
                        {
                            XmlObjectDecoder decoder = new XmlObjectDecoder(element, null);
                            Object obj = decoder.ReadObject();
                            _stillHandling = _handler(obj);
                        }
                    }
                }
            }


            private void StartElement(String localName)
            {
                XmlElement element = null;
                if (_elementStack.Count == 0)
                {
                    if (!XmlObjectSerializerImpl.MULTI_OBJECT_ELEMENT.Equals(localName))
                    {
                        _currentTopLevelElementDocument = new XmlDocument();
                        element = _currentTopLevelElementDocument.CreateElement(localName);
                    }
                }
                else
                {
                    element =
                        _currentTopLevelElementDocument.CreateElement(localName);
                    GetCurrentElement().AppendChild(element);
                }
                if (element != null)
                {
                    _elementStack.Add(element);
                }
            }

            private void AddAttribute(String name, String val)
            {
                XmlElement element = GetCurrentElement();
                if (element != null)
                {
                    element.SetAttribute(name, val);
                }
            }
        }
    }

    public class XmlObjectSerializerImpl : XmlObjectSerializer
    {

        public const String MULTI_OBJECT_ELEMENT = "MultiObject";
        public const String CONNECTORS_DTD = "connectors.dtd";

        private readonly TextWriter _output;

        private readonly bool _multiObject;

        private readonly bool _includeHeader;

        private bool _firstObjectWritten;

        private bool _documentEnded;

        public XmlObjectSerializerImpl(TextWriter output, bool includeHeader, bool multiObject)
        {
            _output = output;
            _includeHeader = includeHeader;
            _multiObject = multiObject;
        }


        /// <summary>
        /// Writes the next object to the stream.
        /// </summary>
        /// <param name="object">The object to write.</param>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        /// <exception cref="Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException">if there is more than one object
        /// and this is not configured for multi-object document.</exception>
        public void WriteObject(Object obj)
        {
            if (_documentEnded)
            {
                throw new InvalidOperationException("Attempt to writeObject after the document is already closed");
            }
            StringBuilder buf = new StringBuilder();
            XmlObjectEncoder encoder = new XmlObjectEncoder(buf);
            String elementName = encoder.WriteObject(obj);
            if (!_firstObjectWritten)
            {
                StartDocument(elementName);
            }
            else
            {
                if (!_multiObject)
                {
                    throw new InvalidOperationException("Attempt to write multiple objects on a single-object document");
                }
            }
            Write(buf.ToString());
            _firstObjectWritten = true;
        }

        public void Flush()
        {
            _output.Flush();
        }

        public void Close(bool closeStream)
        {
            if (!_documentEnded)
            {
                if (!_firstObjectWritten)
                {
                    if (!_multiObject)
                    {
                        throw new InvalidOperationException("Attempt to write zero objects on a single-object document");
                    }
                    StartDocument(null);
                }
                WriteEndDocument();
                _documentEnded = true;
            }
            if (closeStream)
            {
                _output.Close();
            }
        }

        private void StartDocument(String firstElement)
        {
            if (_includeHeader)
            {
                String docType = _multiObject ? MULTI_OBJECT_ELEMENT : firstElement;
                String line1 = "<?xml version='1.0' encoding='UTF-8'?>\n";
                String line2 = "<!DOCTYPE " + docType + " PUBLIC '" + CONNECTORS_DTD + "' '" + CONNECTORS_DTD + "'>\n";
                Write(line1);
                Write(line2);
            }
            if (_multiObject)
            {
                String line3 = "<" + MULTI_OBJECT_ELEMENT + ">\n";
                Write(line3);
            }
        }

        private void WriteEndDocument()
        {
            if (_multiObject)
            {
                String line1 = "</" + MULTI_OBJECT_ELEMENT + ">\n";
                Write(line1);
            }
        }

        private void Write(String str)
        {
            _output.Write(str);
        }
    }
}