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
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 * Portions Copyrighted 2012-2014 ForgeRock AS.
 */
using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Reflection;
using System.Security.Authentication;
using System.Globalization;
using System.Net.Security;
using System.Net.Sockets;
using System.Security.Cryptography.X509Certificates;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Proxy;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Impl.Api.Remote.Messages;

namespace Org.IdentityConnectors.Framework.Impl.Api.Remote
{
    #region RemoteFrameworkConnection
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
    #endregion

    #region RemoteConnectorInfoImpl
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
    #endregion

    #region RemoteConnectorInfoManagerImpl
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
                connection.WriteObject(new HelloRequest(HelloRequest.CONNECTOR_INFO));
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
    #endregion

    #region RemoteConnectorFacadeImpl
    /// <summary>
    /// Implements all the methods of the facade
    /// </summary>
    public class RemoteConnectorFacadeImpl : AbstractConnectorFacade
    {

        internal readonly string remoteConnectorFacadeKey;

        /// <summary>
        /// Builds up the maps of supported operations and calls.
        /// </summary>
        public RemoteConnectorFacadeImpl(APIConfigurationImpl configuration)
            : base(GenerateRemoteConnectorFacadeKey(configuration), configuration.ConnectorInfo)
        {
            // Restore the original configuration settings
            GetAPIConfiguration().ProducerBufferSize = configuration.ProducerBufferSize;
            GetAPIConfiguration().TimeoutMap = configuration.TimeoutMap;
            remoteConnectorFacadeKey = ConnectorFacadeKey;
        }

        public RemoteConnectorFacadeImpl(RemoteConnectorInfoImpl connectorInfo, string configuration)
            : base(configuration, connectorInfo)
        {
            remoteConnectorFacadeKey = GenerateRemoteConnectorFacadeKey(GetAPIConfiguration());
        }

        private static string GenerateRemoteConnectorFacadeKey(APIConfigurationImpl configuration)
        {
            APIConfigurationImpl copy = new APIConfigurationImpl(configuration);
            copy.ProducerBufferSize = 0;
            copy.TimeoutMap = new Dictionary<SafeType<APIOperation>, int>();
            return SerializerUtil.SerializeBase64Object(copy);
        }

        protected override APIOperation GetOperationImplementation(SafeType<APIOperation> api)
        {
            // add remote proxy
            InvocationHandler handler = new RemoteOperationInvocationHandler(
                (RemoteConnectorInfoImpl)GetAPIConfiguration().ConnectorInfo, remoteConnectorFacadeKey, api);
            APIOperation proxy = NewAPIOperationProxy(api, handler);
            // now wrap the proxy in the appropriate timeout proxy
            proxy = CreateTimeoutProxy(api, proxy);
            // add logging proxy
            proxy = CreateLoggingProxy(api, proxy);

            return proxy;
        }
    }
    #endregion

    #region RemoteOperationInvocationHandler
    /// <summary>
    /// Invocation handler for all of our operations
    /// </summary>
    public class RemoteOperationInvocationHandler : InvocationHandler
    {
        private readonly RemoteConnectorInfoImpl _connectorInfo;
        private readonly String _connectorFacadeKey;
        private readonly SafeType<APIOperation> _operation;

        public RemoteOperationInvocationHandler(RemoteConnectorInfoImpl connectorInfo,
            String connectorFacadeKey,
               SafeType<APIOperation> operation)
        {
            _connectorInfo = connectorInfo;
            _connectorFacadeKey = connectorFacadeKey;
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
            RemoteFrameworkConnectionInfo connectionInfo =
                _connectorInfo.RemoteConnectionInfo;
            OperationRequest request = new OperationRequest(
                    _connectorInfo.ConnectorKey, _connectorFacadeKey,
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
    #endregion

    #region RemoteWrappedException
    /// <summary>
    /// RemoteWrappedException wraps every exception which are received from Remote
    /// Connector Server.
    /// <p/>
    /// <b>This Exception is not allowed to use in Connectors!!!</b>
    /// <p/>
    /// 
    /// 
    /// 
    /// This type of exception is not allowed to be serialise because this exception
    /// represents any after deserialization.
    /// 
    /// This code example show how to get the remote stack trace and how to use the
    /// same catches to handle the exceptions regardless its origin.
    /// 
    /// <pre>
    /// <code>
    ///  String stackTrace = null;
    ///  try {
    ///      try {
    ///          facade.GetObject(ObjectClass.ACCOUNT, uid, null);
    ///      } catch (RemoteWrappedException e) {
    ///          stackTrace = e.StackTrace;
    ///      }
    ///  } catch (Throwable t) {
    ///      
    ///  }
    /// <code>
    /// </pre>
    /// </summary>
    /// <remarks>Since 1.4</remarks>
    public sealed class RemoteWrappedException : ConnectorException
    {
        public const string FIELD_CLASS = "class";
        public const string FIELD_MESSAGE = "message";
        public const string FIELD_CAUSE = "cause";
        public const string FIELD_STACK_TRACE = "stackTrace";

        private readonly string stackTrace;

        /// <summary>
        /// <pre>
        ///     <code>
        ///         {
        ///              "class": "org.identityconnectors.framework.common.exceptions.ConnectorIOException",
        ///              "message": "Sample Error Message",
        ///              "cause": {
        ///                  "class": "java.net.SocketTimeoutException",
        ///                  "message": "Sample Error Message",
        ///                  "cause": {
        ///                      "class": "edu.example.CustomException",
        ///                      "message": "Sample Error Message"
        ///                  }
        ///              },
        ///              "stackTrace": "full stack trace for logging"
        ///          }
        ///     </code>
        /// </pre>
        /// </summary>
        private IDictionary<string, object> exception = null;

        /// <seealso cref= org.identityconnectors.framework.common.exceptions.ConnectorException#ConnectorException(String) </seealso>
        internal RemoteWrappedException(IDictionary<string, object> exception)
            : base((string)exception[FIELD_MESSAGE])
        {
            this.exception = exception;
            this.stackTrace = (string)exception[FIELD_STACK_TRACE];
        }

        public RemoteWrappedException(string throwableClass, string message, RemoteWrappedException cause, string stackTrace)
            : base(message)
        {
            exception = new Dictionary<string, object>(4);
            exception[FIELD_CLASS] = Assertions.BlankChecked(throwableClass, "throwableClass");
            exception[FIELD_MESSAGE] = message;
            if (null != cause)
            {
                exception[FIELD_CAUSE] = cause.exception;
            }
            if (null != stackTrace)
            {
                exception[FIELD_STACK_TRACE] = stackTrace;
            }
            this.stackTrace = stackTrace;
        }

        /// <summary>
        /// Gets the class name of the original exception.
        /// 
        /// This value is constructed by {@code Exception.Type.FullName}.
        /// </summary>
        /// <returns> name of the original exception. </returns>
        public string ExceptionClass
        {
            get
            {
                return (string)exception[FIELD_CLASS];
            }
        }

        /// <summary>
        /// Checks if the exception is the expected class.
        /// </summary>
        /// <param name="expected">
        ///            the expected throwable class. </param>
        /// <returns> {@code true} if the class name are equals. </returns>
        public bool Is(Type expected)
        {
            if (null == expected)
            {
                return false;
            }
            string className = ((string)exception[FIELD_CLASS]);
            //The .NET Type.FullName property will not always yield results identical to the Java Class.getName method:
            string classExpected = expected.FullName;
            return classExpected.Equals(className, StringComparison.CurrentCultureIgnoreCase);
        }

        /// <summary>
        /// Returns the cause of original throwable or {@code null} if the cause is
        /// nonexistent or unknown. (The cause is the throwable that caused the
        /// original throwable to get thrown.)
        /// </summary>
        /// <returns> the cause of this throwable or {@code null} if the cause is
        ///         nonexistent or unknown. </returns>
        public RemoteWrappedException Cause
        {
            get
            {
                object o = exception[FIELD_CAUSE];
                if (o is IDictionary)
                {
                    return new RemoteWrappedException((IDictionary<string, object>)o);
                }
                else
                {
                    return null;
                }
            }
        }

        public override string StackTrace
        {
            get
            {
                if (null == stackTrace)
                {
                    return base.StackTrace;
                }
                else
                {
                    return stackTrace;
                }
            }
        }

        public string ReadStackTrace()
        {
            return stackTrace;
        }

        /// <summary>
        /// Wraps the Throwable into a RemoteWrappedException instance.
        /// </summary>
        /// <param name="ex">
        ///            Exception to wrap or cast and return. </param>
        /// <returns> a <code>RemoteWrappedException</code> that either <i>is</i> the
        ///         specified exception or <i>contains</i> the specified exception. </returns>
        public static RemoteWrappedException Wrap(Exception ex)
        {
            if (null == ex)
            {
                return null;
            }
            // don't bother to wrap a exception that is already a
            // RemoteWrappedException.
            if (ex is RemoteWrappedException)
            {
                return (RemoteWrappedException)ex;
            }
            return new RemoteWrappedException(convert(ex));
        }

        /// <summary>
        /// Converts the throwable object to a new Map object that representing
        /// itself.
        /// </summary>
        /// <param name="throwable">
        ///            the {@code Throwable} to be converted </param>
        /// <returns> the Map representing the throwable. </returns>
        public static Dictionary<string, object> convert(Exception throwable)
        {
            Dictionary<string, object> exception = null;
            if (null != throwable)
            {
                exception = new Dictionary<string, object>(4);
                //The .NET Type.FullName property will not always yield results identical to the Java Class.getName method:
                exception[FIELD_CLASS] = throwable.GetType().FullName;
                exception[FIELD_MESSAGE] = throwable.Message;
                if (null != throwable.InnerException)
                {
                    exception[FIELD_CAUSE] = buildCause(throwable.InnerException);
                }
                exception[FIELD_STACK_TRACE] = throwable.StackTrace;
            }
            return exception;
        }

        private static IDictionary<string, object> buildCause(Exception throwable)
        {
            IDictionary<string, object> cause = new Dictionary<string, object>(null != throwable.InnerException ? 3 : 2);
            //The .NET Type.FullName property will not always yield results identical to the Java Class.getName method:
            cause[FIELD_CLASS] = throwable.GetType().FullName;
            cause[FIELD_MESSAGE] = throwable.Message;
            if (null != throwable.InnerException)
            {
                cause[FIELD_CAUSE] = buildCause(throwable.InnerException);
            }
            return cause;
        }
    }
    #endregion
}