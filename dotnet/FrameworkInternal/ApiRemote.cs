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
using System.Net;
using System.Reflection;
using System.Security.Authentication;
using System.Globalization;
using System.Net.Security;
using System.Net.Sockets;
using System.Security.Cryptography.X509Certificates;
using System.Threading;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Proxy;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Impl.Api;
using Org.IdentityConnectors.Framework.Impl.Api.Local.Operations;
using Org.IdentityConnectors.Framework.Impl.Api.Remote.Messages;
using System.Diagnostics;
namespace Org.IdentityConnectors.Framework.Impl.Api.Remote
{
    public class RemoteFrameworkConnection : IDisposable
    {
        private TcpClient _socket;
        private Stream _stream;

        private BinaryObjectSerializer _encoder;
        private BinaryObjectDeserializer _decoder;

        public RemoteFrameworkConnection(RemoteFrameworkConnectionInfo info)
        {
            Init(info);
        }

        public RemoteFrameworkConnection(TcpClient socket, Stream stream)
        {
            Init(socket, stream);
        }

        private void Init(RemoteFrameworkConnectionInfo connectionInfo)
        {
            IPAddress[] addresses =
                Dns.GetHostAddresses(connectionInfo.Host);
            TcpClient client = new TcpClient(addresses[0].AddressFamily);
            client.SendTimeout = connectionInfo.Timeout;
            client.ReceiveTimeout = connectionInfo.Timeout;
            client.Connect(addresses[0], connectionInfo.Port);
            Stream stream;
            try
            {
                stream = client.GetStream();
            }
            catch (Exception)
            {
                try { client.Close(); }
                catch (Exception) { }
                throw;
            }
            try
            {
                if (connectionInfo.UseSSL)
                {
                    if (connectionInfo.CertificateValidationCallback != null)
                    {
                        RemoteCertificateValidationCallback callback =
                            connectionInfo.CertificateValidationCallback;

                        stream = new SslStream(
                            stream, false, callback);
                    }
                    else
                    {
                        stream = new SslStream(stream,
                                               false);
                    }
                    ((SslStream)stream).AuthenticateAsClient(connectionInfo.Host,
                                                                 new X509CertificateCollection(new X509Certificate[0]),
                                                            SslProtocols.Tls,
                                                            false);
                }
            }
            catch (Exception)
            {
                try { stream.Close(); }
                catch (Exception) { }
                try { client.Close(); }
                catch (Exception) { }
                throw;
            }
            Init(client, stream);
        }

        private void Init(TcpClient socket, Stream stream)
        {
            _socket = socket;
            _stream = stream;
            ObjectSerializerFactory fact =
                ObjectSerializerFactory.GetInstance();
            _encoder = fact.NewBinarySerializer(_stream);
            _decoder = fact.NewBinaryDeserializer(_stream);
        }

        public void Dispose()
        {
            Flush();
            _stream.Close();
            _socket.Close();
        }

        public void Flush()
        {
            _encoder.Flush();
        }

        public void WriteObject(object obj)
        {
            _encoder.WriteObject(obj);
        }

        public object ReadObject()
        {
            //flush first in case there is any data in the
            //output buffer
            Flush();
            return _decoder.ReadObject();
        }
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public sealed class RemoteConnectorInfoImpl : AbstractConnectorInfo
    {


        public RemoteConnectorInfoImpl()
        {

        }

        //transient field, not serialized
        public RemoteFrameworkConnectionInfo RemoteConnectionInfo { get; set; }

    }

    public class RemoteConnectorInfoManagerImpl : ConnectorInfoManager
    {
        private IList<ConnectorInfo> _connectorInfo;

        private RemoteConnectorInfoManagerImpl()
        {

        }

        public RemoteConnectorInfoManagerImpl(RemoteFrameworkConnectionInfo info)
        {
            using (RemoteFrameworkConnection connection =
                   new RemoteFrameworkConnection(info))
            {
                connection.WriteObject(CultureInfo.CurrentUICulture);
                connection.WriteObject(info.Key);
                connection.WriteObject(new HelloRequest());
                HelloResponse response = (HelloResponse)connection.ReadObject();
                if (response.Exception != null)
                {
                    throw response.Exception;
                }
                IList<RemoteConnectorInfoImpl> remoteInfos =
                response.ConnectorInfos;
                //populate transient fields not serialized
                foreach (RemoteConnectorInfoImpl remoteInfo in remoteInfos)
                {
                    remoteInfo.RemoteConnectionInfo = info;
                }
                _connectorInfo =
         CollectionUtil.NewReadOnlyList<RemoteConnectorInfoImpl, ConnectorInfo>(remoteInfos);
            }

        }

        /// <summary>
        /// Derives another RemoteConnectorInfoManagerImpl with
        /// a different RemoteFrameworkConnectionInfo but with the
        /// same metadata
        /// </summary>
        /// <param name="info"></param>
        public RemoteConnectorInfoManagerImpl Derive(RemoteFrameworkConnectionInfo info)
        {
            RemoteConnectorInfoManagerImpl rv = new RemoteConnectorInfoManagerImpl();
            IList<Object> remoteInfosObj =
                (IList<Object>)SerializerUtil.CloneObject(_connectorInfo);
            IList<ConnectorInfo> remoteInfos =
                CollectionUtil.NewList<object, ConnectorInfo>(remoteInfosObj);
            foreach (ConnectorInfo remoteInfo in remoteInfos)
            {
                ((RemoteConnectorInfoImpl)remoteInfo).RemoteConnectionInfo = (info);
            }
            rv._connectorInfo =
                CollectionUtil.AsReadOnlyList(remoteInfos);
            return rv;
        }

        public ConnectorInfo FindConnectorInfo(ConnectorKey key)
        {
            foreach (ConnectorInfo info in _connectorInfo)
            {
                if (info.ConnectorKey.Equals(key))
                {
                    return info;
                }
            }
            return null;
        }

        public IList<ConnectorInfo> ConnectorInfos
        {
            get
            {
                return _connectorInfo;
            }
        }
    }

    internal class RemoteConnectorFacadeImpl : AbstractConnectorFacade
    {
        /// <summary>
        /// Builds up the maps of supported operations and calls.
        /// </summary>
        public RemoteConnectorFacadeImpl(APIConfigurationImpl configuration)
            : base(configuration)
        {
        }

        protected override APIOperation GetOperationImplementation(SafeType<APIOperation> api)
        {
            InvocationHandler handler = new RemoteOperationInvocationHandler(
                    GetAPIConfiguration(),
                    api);
            APIOperation proxy = NewAPIOperationProxy(api, handler);
            // add logging..
            proxy = CreateLoggingProxy(api, proxy);
            return proxy;
        }
    }

    /// <summary>
    /// Invocation handler for all of our operations
    /// </summary>
    public class RemoteOperationInvocationHandler : InvocationHandler
    {
        private readonly APIConfigurationImpl _configuration;
        private readonly SafeType<APIOperation> _operation;

        public RemoteOperationInvocationHandler(APIConfigurationImpl configuration,
               SafeType<APIOperation> operation)
        {
            _configuration = configuration;
            _operation = operation;
        }


        public Object Invoke(Object proxy, MethodInfo method, Object[] args)
        {
            //don't proxy toString, hashCode, or equals
            if (method.DeclaringType.Equals(typeof(object)))
            {
                return method.Invoke(this, args);
            }

            //partition arguments into arguments that can
            //be simply marshalled as part of the request and
            //those that are response handlers
            IList<Object> simpleMarshallArgs =
                CollectionUtil.NewList(args);
            ObjectStreamHandler streamHandlerArg =
                ExtractStreamHandler(ReflectionUtil.GetParameterTypes(method), simpleMarshallArgs);

            //build the request object
            RemoteConnectorInfoImpl connectorInfo =
                (RemoteConnectorInfoImpl)_configuration.ConnectorInfo;
            RemoteFrameworkConnectionInfo connectionInfo =
                connectorInfo.RemoteConnectionInfo;
            OperationRequest request = new OperationRequest(
                    connectorInfo.ConnectorKey,
                    _configuration,
                    _operation,
                    method.Name,
                    simpleMarshallArgs);

            //create the connection
            RemoteFrameworkConnection connection =
                new RemoteFrameworkConnection(connectionInfo);
            try
            {
                connection.WriteObject(CultureInfo.CurrentUICulture);
                connection.WriteObject(connectionInfo.Key);
                //send the request
                connection.WriteObject(request);

                //now process each response stream (if any)
                if (streamHandlerArg != null)
                {
                    HandleStreamResponse(connection, streamHandlerArg);
                }

                //finally return the actual return value
                OperationResponsePart response =
                    (OperationResponsePart)connection.ReadObject();
                if (response.Exception != null)
                {
                    throw response.Exception;
                }
                return response.Result;
            }
            finally
            {
                connection.Dispose();
            }
        }
        /// <summary>
        /// Handles a stream response until the end of the stream
        /// </summary>
        private static void HandleStreamResponse(RemoteFrameworkConnection connection, ObjectStreamHandler streamHandler)
        {
            Object response;
            bool handleMore = true;
            while (true)
            {
                response = connection.ReadObject();
                if (response is OperationResponsePart)
                {
                    OperationResponsePart part = (OperationResponsePart)response;
                    if (part.Exception != null)
                    {
                        throw part.Exception;
                    }
                    object obj =
                        part.Result;
                    if (handleMore)
                    {
                        handleMore = streamHandler.Handle(obj);
                    }
                }
                else if (response is OperationResponsePause)
                {
                    if (handleMore)
                    {
                        connection.WriteObject(new OperationRequestMoreData());
                    }
                    else
                    {
                        connection.WriteObject(new OperationRequestStopData());
                    }
                }
                else if (response is OperationResponseEnd)
                {
                    break;
                }
                else
                {
                    throw new ConnectorException("Unexpected response: " + response);
                }
            }
        }

        /// <summary>
        /// Partitions arguments into regular arguments and
        /// stream arguments.
        /// </summary>
        /// <param name="paramTypes">The param types of the method</param>
        /// <param name="arguments">The passed-in arguments. As a
        /// side-effect will be set to just the regular arguments.</param>
        /// <returns>The stream handler arguments.</returns>
        private static ObjectStreamHandler ExtractStreamHandler(Type[] paramTypes,
                IList<Object> arguments)
        {
            ObjectStreamHandler rv = null;
            IList<Object> filteredArguments = new List<Object>();
            for (int i = 0; i < paramTypes.Length; i++)
            {
                Type paramType = paramTypes[i];
                object arg = arguments[i];
                if (StreamHandlerUtil.IsAdaptableToObjectStreamHandler(paramType))
                {
                    ObjectStreamHandler handler = StreamHandlerUtil.AdaptToObjectStreamHandler(paramType, arg);
                    if (rv != null)
                    {
                        throw new InvalidOperationException("Multiple stream handlers not supported");
                    }
                    rv = handler;
                }
                else
                {
                    filteredArguments.Add(arg);
                }
            }
            arguments.Clear();
            CollectionUtil.AddAll(arguments, filteredArguments);
            return rv;
        }
    }
}