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
using System.Diagnostics;
using System.Globalization;
using System.Net;
using System.Net.Security;
using System.Security;
using System.Security.Cryptography.X509Certificates;
using System.Net.Sockets;
using System.IO;
using System.Linq;
using System.Threading;
using System.Reflection;
using System.Security.Authentication;
using System.Text;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Server;
using Org.IdentityConnectors.Framework.Impl.Api;
using Org.IdentityConnectors.Framework.Impl.Api.Remote.Messages;
using Org.IdentityConnectors.Framework.Impl.Api.Local;
using Org.IdentityConnectors.Framework.Impl.Api.Local.Operations;
using Org.IdentityConnectors.Framework.Impl.Api.Remote;

namespace Org.IdentityConnectors.Framework.Server
{
    /// <summary>
    /// Connector server interface.
    /// </summary>
    public abstract class ConnectorServer
    {
        // At some point we might make this pluggable, but for now, hard-code
        private const String IMPL_NAME
         = "Org.IdentityConnectors.Framework.Impl.Server.ConnectorServerImpl";

        /// <summary>
        /// The port to listen on;
        /// </summary>
        private int _port = 0;

        /// <summary>
        /// Base 64 sha1 hash of the connector server key
        /// </summary>
        /// 
        private String _keyHash;

        /// <summary>
        /// The number of connections to queue
        /// </summary>
        private int _maxConnections = 300;

        /// <summary>
        /// The minimum number of worker threads
        /// </summary>
        private int _minWorkers = 10;

        /// <summary>
        /// The maximum number of worker threads
        /// </summary>
        private int _maxWorkers = 100;

        /// <summary>
        /// The network interface address to use.
        /// </summary>
        private IPAddress _ifAddress = null;

        /// <summary>
        /// Listen on SSL
        /// </summary>
        private bool _useSSL = false;

        /// <summary>
        /// The server certificate to use
        /// </summary>
        private X509Certificate _serverCertificate = null;

        /// <summary>
        /// Get the singleton instance of the <see cref="ConnectorServer" />.
        /// </summary>
        public static ConnectorServer NewInstance()
        {
            SafeType<ConnectorServer> type =
                SafeType<ConnectorServer>.ForRawType(Type.GetType(IMPL_NAME, true));
            return type.CreateInstance();
        }

        private void AssertNotStarted()
        {
            if (IsStarted())
            {
                throw new InvalidOperationException("Operation cannot be performed " +
                        "while server is running");
            }
        }

        /// <summary>
        /// Returns the port to listen on.
        /// </summary>
        /// <returns>The port to listen on.</returns>
        public int Port
        {
            get
            {
                return _port;
            }
            set
            {
                AssertNotStarted();
                _port = value;
            }
        }

        /// <summary>
        /// Returns the max connections to queue
        /// </summary>
        /// <returns>The max connections to queue</returns>
        public int MaxConnections
        {
            get
            {
                return _maxConnections;
            }
            set
            {
                AssertNotStarted();
                _maxConnections = value;
            }
        }

        /// <summary>
        /// Returns the max worker threads to allow.
        /// </summary>
        /// <returns>The max worker threads to allow.</returns>
        public int MaxWorkers
        {
            get
            {
                return _maxWorkers;
            }
            set
            {
                AssertNotStarted();
                _maxWorkers = value;
            }
        }

        /// <summary>
        /// Returns the min worker threads to allow.
        /// </summary>
        /// <returns>The min worker threads to allow.</returns>
        public int MinWorkers
        {
            get
            {
                return _minWorkers;
            }
            set
            {
                AssertNotStarted();
                _minWorkers = value;
            }
        }

        /// <summary>
        /// Returns the network interface address to bind to.
        /// </summary>
        /// <remarks>
        /// May be null.
        /// </remarks>
        /// <returns>The network interface address to bind to or null.</returns>
        public IPAddress IfAddress
        {
            get
            {
                return _ifAddress;
            }
            set
            {
                AssertNotStarted();
                _ifAddress = value;
            }
        }

        /// <summary>
        /// Returns true iff we are to use SSL.
        /// </summary>
        /// <returns>true iff we are to use SSL.</returns>
        public bool UseSSL
        {
            get
            {
                return _useSSL;
            }
            set
            {
                AssertNotStarted();
                _useSSL = value;
            }
        }

        /// <summary>
        /// Returns the certificate to use for the SSL connection.
        /// </summary>
        public X509Certificate ServerCertificate
        {
            get
            {
                return _serverCertificate;
            }
            set
            {
                AssertNotStarted();
                _serverCertificate = value;
            }
        }

        public String KeyHash
        {
            get
            {
                return _keyHash;
            }
            set
            {
                AssertNotStarted();
                _keyHash = value;
            }
        }

        /// <summary>
        /// Produces a thread dump of all pending requests
        /// </summary>
        abstract public void DumpRequests();

        /// <summary>
        /// Starts the server.
        /// </summary>
        /// <remarks>
        /// All server settings must be configured prior
        /// to calling. The following methods are required to be called:
        /// <list type="bullet">
        /// <item>
        /// <description><see cref="Port" />
        /// </description>
        /// </item>
        /// <item>
        /// <description><see cref="KeyHash" />
        /// </description>
        /// </item>
        /// </list>
        /// </remarks>
        abstract public void Start();

        /// <summary>
        /// Stops the server gracefully.
        /// </summary>
        /// <remarks>
        /// Returns when all in-progress connections
        /// have been serviced.
        /// </remarks>
        abstract public void Stop();

        /// <summary>
        /// Return true iff the server is started.
        /// </summary>
        /// <remarks>
        /// Note that started is a
        /// logical state (start method has been called). It does not necessarily
        /// reflect the health of the server
        /// </remarks>
        /// <returns>true iff the server is started.</returns>
        abstract public bool IsStarted();
    }
}

namespace Org.IdentityConnectors.Framework.Impl.Server
{
    public class ConnectionProcessor
    {
        private class RemoteResultsHandler : ObjectStreamHandler
        {
            private const int PAUSE_INTERVAL = 200;

            private readonly RemoteFrameworkConnection _connection;
            private long _count = 0;
            public RemoteResultsHandler(RemoteFrameworkConnection conn)
            {
                _connection = conn;
            }

            public bool Handle(Object obj)
            {
                try
                {
                    OperationResponsePart part =
                        new OperationResponsePart(null, obj);
                    _connection.WriteObject(part);
                    _count++;
                    if (_count % PAUSE_INTERVAL == 0)
                    {
                        _connection.WriteObject(new OperationResponsePause());
                        Object message =
                            _connection.ReadObject();
                        return message is OperationRequestMoreData;
                    }
                    else
                    {
                        return true;
                    }
                }
                catch (IOException e)
                {
                    throw new BrokenConnectionException(e);
                }
                catch (Exception)
                {
                    throw;
                }
            }
        }

        private readonly ConnectorServerImpl _server;
        private readonly RemoteFrameworkConnection _connection;

        public ConnectionProcessor(ConnectorServerImpl server,
                RemoteFrameworkConnection connection)
        {
            _server = server;
            _connection = connection;
        }

        public void Run()
        {
            try
            {
                _server.BeginRequest();
                try
                {
                    while (true)
                    {
                        bool keepGoing = ProcessRequest();
                        if (!keepGoing)
                        {
                            break;
                        }
                    }
                }
                finally
                {
                    try
                    {
                        _connection.Dispose();
                    }
                    catch (Exception e)
                    {
                        TraceUtil.TraceException(null, e);
                    }
                }
            }
            catch (Exception e)
            {
                TraceUtil.TraceException(null, e);
            }
            finally
            {
                _server.EndRequest();
            }
        }

        private bool ProcessRequest()
        {
            CultureInfo locale;
            try
            {
                locale = (CultureInfo)_connection.ReadObject();
            }
            catch (EndOfStreamException)
            {
                return false;
            }

            //We can't set this because C# does not like language-neutral
            //cultures for CurrentCulture - this tends to blow up
            //TODO: think more about this...
            //Thread.CurrentThread.CurrentCulture = locale;
            Thread.CurrentThread.CurrentUICulture = locale;

            GuardedString key = (GuardedString)_connection.ReadObject();

            bool authorized;
            try
            {
                authorized = key.VerifyBase64SHA1Hash(_server.KeyHash);
            }
            finally
            {
                key.Dispose();
            }
            Org.IdentityConnectors.Framework.Common.Exceptions.InvalidCredentialException authException = null;
            if (!authorized)
            {
                authException = new Org.IdentityConnectors.Framework.Common.Exceptions.InvalidCredentialException("Remote framework key is invalid");
            }
            Object requestObject = _connection.ReadObject();
            if (requestObject is HelloRequest)
            {
                if (authException != null)
                {
                    HelloResponse response =
                        new HelloResponse(authException, null);
                    _connection.WriteObject(response);
                }
                else
                {
                    HelloResponse response =
                        ProcessHelloRequest((HelloRequest)requestObject);
                    _connection.WriteObject(response);
                }
            }
            else if (requestObject is OperationRequest)
            {
                if (authException != null)
                {
                    OperationResponsePart part =
                        new OperationResponsePart(authException, null);
                    _connection.WriteObject(part);
                }
                else
                {
                    OperationRequest opRequest =
                        (OperationRequest)requestObject;
                    OperationResponsePart part =
                        ProcessOperationRequest(opRequest);
                    _connection.WriteObject(part);
                }
            }
            else if (requestObject is EchoMessage)
            {
                if (authException != null)
                {
                    //echo message probably doesn't need auth, but
                    //it couldn't hurt - actually it does for test connection
                    EchoMessage part =
                        new EchoMessage(authException, null);
                    _connection.WriteObject(part);
                }
                else
                {
                    EchoMessage message = (EchoMessage)requestObject;
                    Object obj = message.Object;
                    String xml = message.ObjectXml;
                    if (xml != null)
                    {
                        Console.WriteLine("xml: \n" + xml);
                        Object xmlClone =
                            SerializerUtil.DeserializeXmlObject(xml, true);
                        xml =
                            SerializerUtil.SerializeXmlObject(xmlClone, true);
                    }
                    EchoMessage message2 = new EchoMessage(obj, xml);
                    _connection.WriteObject(message2);
                }
            }
            else
            {
                throw new Exception("Unexpected request: " + requestObject);
            }
            return true;
        }

        private ConnectorInfoManager GetConnectorInfoManager()
        {
            return ConnectorInfoManagerFactory.GetInstance().GetLocalManager();
        }

        private HelloResponse ProcessHelloRequest(HelloRequest request)
        {
            IList<RemoteConnectorInfoImpl> connectorInfo;
            Exception exception = null;
            try
            {
                ConnectorInfoManager manager =
                    GetConnectorInfoManager();
                IList<ConnectorInfo> localInfos =
                    manager.ConnectorInfos;
                connectorInfo = new List<RemoteConnectorInfoImpl>();
                foreach (ConnectorInfo localInfo in localInfos)
                {
                    LocalConnectorInfoImpl localInfoImpl =
                        (LocalConnectorInfoImpl)localInfo;
                    RemoteConnectorInfoImpl remoteInfo =
                        localInfoImpl.ToRemote();
                    connectorInfo.Add(remoteInfo);
                }
            }
            catch (Exception e)
            {
                TraceUtil.TraceException(null, e);
                exception = e;
                connectorInfo = null;
            }
            return new HelloResponse(exception, connectorInfo);
        }

        private MethodInfo GetOperationMethod(OperationRequest request)
        {
            MethodInfo[] methods =
                request.Operation.RawType.GetMethods();
            MethodInfo found = null;
            foreach (MethodInfo m in methods)
            {
                if (m.Name.ToUpper().Equals(request.OperationMethodName.ToUpper()))
                {
                    if (found != null)
                    {
                        throw new ConnectorException("APIOperations are expected "
                                + "to have exactly one method of a given name: " + request.Operation);
                    }
                    found = m;
                }
            }

            if (found == null)
            {
                throw new ConnectorException("APIOperations are expected "
                        + "to have exactly one method of a given name: " + request.Operation + " " + methods.Length);
            }
            return found;
        }

        private OperationResponsePart
        ProcessOperationRequest(OperationRequest request)
        {
            Object result;
            Exception exception = null;
            try
            {
                MethodInfo method = GetOperationMethod(request);
                APIOperation operation = GetAPIOperation(request);
                IList<Object> arguments = request.Arguments;
                IList<Object> argumentsAndStreamHandlers =
                    PopulateStreamHandlers(ReflectionUtil.GetParameterTypes(method),
                            arguments);
                try
                {
                    Object[] args = argumentsAndStreamHandlers.ToArray();
                    FixupArguments(method, args);
                    result = method.Invoke(operation, args);
                }
                catch (TargetInvocationException e)
                {
                    Exception root = e.InnerException;
                    ExceptionUtil.PreserveStackTrace(root);
                    throw root;
                }
                bool anyStreams =
                    argumentsAndStreamHandlers.Count > arguments.Count;
                if (anyStreams)
                {
                    try
                    {
                        _connection.WriteObject(new OperationResponseEnd());
                    }
                    catch (IOException e)
                    {
                        throw new BrokenConnectionException(e);
                    }
                }
            }
            catch (BrokenConnectionException w)
            {
                //at this point the stream is broken - just give up
                throw w.GetIOException();
            }
            catch (Exception e)
            {
                TraceUtil.TraceException(null, e);
                exception = e;
                result = null;
            }
            return new OperationResponsePart(exception, result);
        }

        private IList<Object> PopulateStreamHandlers(Type[] paramTypes, IList<Object> arguments)
        {
            IList<Object> rv = new List<Object>();
            bool firstStream = true;
            IEnumerator<Object> argIt = arguments.GetEnumerator();
            foreach (Type paramType in paramTypes)
            {
                if (StreamHandlerUtil.IsAdaptableToObjectStreamHandler(paramType))
                {
                    if (!firstStream)
                    {
                        throw new InvalidOperationException("At most one stream handler is supported");
                    }
                    ObjectStreamHandler osh =
                        new RemoteResultsHandler(_connection);
                    rv.Add(StreamHandlerUtil.AdaptFromObjectStreamHandler(paramType, osh));
                    firstStream = false;
                }
                else
                {
                    argIt.MoveNext();
                    rv.Add(argIt.Current);
                }
            }
            return rv;
        }

        /// <summary>
        /// When arguments are serialized, we loose the
        /// generic-type of collections. We must fix
        /// the arguments 
        /// </summary>
        /// <param name="method"></param>
        /// <param name="args"></param>
        private void FixupArguments(MethodInfo method,
                                    object[] args)
        {
            Type[] paramTypes =
                ReflectionUtil.GetParameterTypes(method);
            if (paramTypes.Length != args.Length)
            {
                throw new ArgumentException("Number of arguments does not match for method: " + method);
            }
            for (int i = 0; i < args.Length; i++)
            {
                args[i] = FixupArgument(paramTypes[i],
                                         args[i]);
            }
        }

        private object FixupArgument(Type expectedType,
                                     object argument)
        {
            //at some point, we might want this to be more general-purpose
            //for now we just handle those cases that we need to
            if (typeof(ICollection<ConnectorAttribute>).Equals(expectedType))
            {
                ICollection<object> val =
                    (ICollection<object>)argument;
                return CollectionUtil.NewSet<object, ConnectorAttribute>(val);
            }
            else
            {
                return argument;
            }
        }

        private APIOperation GetAPIOperation(OperationRequest request)
        {
            ConnectorInfoManager manager =
                GetConnectorInfoManager();
            ConnectorInfo info = manager.FindConnectorInfo(
                    request.ConnectorKey);
            if (info == null)
            {
                throw new Exception("No such connector: "
                                    + request.ConnectorKey);
            }
            APIConfigurationImpl config =
                request.Configuration;

            //re-wire the configuration with its connector info
            config.ConnectorInfo = (AbstractConnectorInfo)info;

            ConnectorFacade facade =
                ConnectorFacadeFactory.GetInstance().NewInstance(config);

            return facade.GetOperation(request.Operation);
        }

        private class BrokenConnectionException : Exception
        {


            public BrokenConnectionException(IOException ex)
                : base("", ex)
            {
            }

            public IOException GetIOException()
            {
                return (IOException)InnerException;
            }
        }

    }

    class ConnectionListener
    {
        /// <summary>
        /// This is the size of our internal queue.
        /// </summary>
        /// <remarks>
        /// For now I have this
        /// relatively small because I want the OS to manage the connect
        /// queue coming in. That way it can properly turn away excessive
        /// requests
        /// </remarks>
        private const int INTERNAL_QUEUE_SIZE = 2;


        /// <summary>
        /// The server object that we are using
        /// </summary>
        private readonly ConnectorServerImpl _server;

        /// <summary>
        /// The server socket.
        /// </summary>
        /// <remarks>
        /// This must be bound at the time
        /// of creation.
        /// </remarks>
        private readonly TcpListener _socket;

        /// <summary>
        /// Pool of executors
        /// </summary>
        //TODO: add a thread pool
        //private readonly ExecutorService _threadPool;

        /// <summary>
        /// Set to indicated we need to start shutting down
        /// </summary>
        private bool _stopped = false;

        private Thread _thisThread;

        private readonly Object MUTEX = new Object();

        /// <summary>
        /// Creates the listener thread
        /// </summary>
        /// <param name="server">The server object</param>
        /// <param name="socket">The socket (should already be bound)</param>
        public ConnectionListener(ConnectorServerImpl server,
                TcpListener socket)
        {
            _server = server;
            _socket = socket;
            _thisThread = new Thread(Run) { Name = "ConnectionListener", IsBackground = false };
            //TODO: thread pool
            /*            _threadPool = 
                            new ThreadPoolExecutor
                            (server.getMinWorkers(),
                             server.getMaxWorkers(),
                             30, //idle time timeout
                             TimeUnit.SECONDS,
                             new ArrayBlockingQueue<Runnable>(
                                     INTERNAL_QUEUE_SIZE,
                                     true)); //fair*/
        }

        public void Start()
        {
            _thisThread.Start();
        }

        public void Run()
        {
            Trace.TraceInformation("Server started on port: " + _server.Port);
            while (!IsStopped())
            {
                try
                {
                    TcpClient connection = null;
                    Stream stream = null;
                    try
                    {
                        connection = _socket.AcceptTcpClient();
                        stream = connection.GetStream();
                        if (_server.UseSSL)
                        {
                            SslStream sslStream = new SslStream(stream, false);
                            stream = sslStream;
                            sslStream.AuthenticateAsServer(_server.ServerCertificate,
                                                           false,
                                                           SslProtocols.Tls,
                                                           false);
                        }

                        ConnectionProcessor processor =
                            new ConnectionProcessor(_server,
                                                    new RemoteFrameworkConnection(connection, stream));
                        Thread thread = new Thread(processor.Run);
                        thread.IsBackground = false;
                        thread.Start();
                    }
                    catch (Exception)
                    {
                        if (stream != null)
                        {
                            try { stream.Close(); }
                            catch (Exception) { }
                        }
                        if (connection != null)
                        {
                            try { connection.Close(); }
                            catch (Exception) { }
                        }
                        throw;
                    }
                }
                catch (Exception e)
                {
                    //log the error unless it's because we've stopped
                    if (!IsStopped() || !(e is SocketException))
                    {
                        TraceUtil.TraceException("Error processing request", e);
                    }
                    //wait a second before trying again
                    if (!IsStopped())
                    {
                        Thread.Sleep(1000);
                    }
                }
            }
        }

        private void MarkStopped()
        {
            lock (MUTEX)
            {
                _stopped = true;
            }
        }

        private bool IsStopped()
        {
            lock (MUTEX)
            {
                return _stopped;
            }
        }

        public void Shutdown()
        {
            if (Object.ReferenceEquals(Thread.CurrentThread, _thisThread))
            {
                throw new ArgumentException("Shutdown may not be called from this thread");
            }
            if (!IsStopped())
            {
                //set the stopped flag so we no its a normal
                //shutdown and don't log the SocketException
                MarkStopped();
                //close the socket - this causes accept to throw an exception
                _socket.Stop();
                //wait for the main listener thread to die so we don't
                //get any new requests
                _thisThread.Join();
                //TODO: shutdown thread pool
                //wait for all in-progress requests to finish
                //_threadPool.shutdown();
            }
        }
    }

    internal class RequestStats
    {
        public RequestStats()
        {
        }
        public Thread RequestThread { get; set; }
        public long StartTimeMillis { get; set; }
        public long RequestID { get; set; }
    }

    public class ConnectorServerImpl : ConnectorServer
    {

        private readonly IDictionary<Thread, RequestStats>
            _pendingRequests = CollectionUtil.NewIdentityDictionary<Thread, RequestStats>();
        private ConnectionListener _listener;
        private Object COUNT_LOCK = new Object();
        private long _requestCount = 0;

        public override bool IsStarted()
        {
            return _listener != null;
        }

        public void BeginRequest()
        {
            long requestID;
            lock (COUNT_LOCK)
            {
                requestID = _requestCount++;
            }
            Thread requestThread = Thread.CurrentThread;
            RequestStats stats = new RequestStats();
            stats.StartTimeMillis =
                DateTimeUtil.GetCurrentUtcTimeMillis();
            stats.RequestThread = Thread.CurrentThread;
            stats.RequestID = requestID;
            lock (_pendingRequests)
            {
                _pendingRequests[stats.RequestThread]
                    = stats;
            }
        }

        public void EndRequest()
        {
            lock (_pendingRequests)
            {
                _pendingRequests.Remove(Thread.CurrentThread);
            }
        }
        public override void DumpRequests()
        {
            long currentTime = DateTimeUtil.GetCurrentUtcTimeMillis();
            IDictionary<Thread, RequestStats>
                pending;
            lock (_pendingRequests)
            {
                pending = new Dictionary<Thread, RequestStats>(_pendingRequests);
            }
            StringBuilder builder = new StringBuilder();
            builder.Append("****Pending Requests Summary*****");
            foreach (RequestStats stats in pending.Values)
            {
                DumpStats(stats, builder, currentTime);
            }
            //here we purposefully use write line since
            //we always want to see it. in general, don't
            //use this method
            Trace.WriteLine(builder.ToString());
        }

        private void DumpStats(RequestStats stats,
                               StringBuilder builder,
                               long currentTime)
        {
            builder.AppendLine("**Request #" + stats.RequestID + " pending for " + (currentTime - stats.StartTimeMillis) + " millis.");
            StackTrace stackTrace = GetStackTrace(stats.RequestThread);
            if (stackTrace == null)
            {
                builder.AppendLine("    <stack trace unavailable>");
            }
            else
            {
                builder.AppendLine(stackTrace.ToString());
            }
        }

        private static StackTrace GetStackTrace(Thread thread)
        {
            bool suspended = false;
            try
            {
                thread.Suspend();
                suspended = true;
                return new StackTrace(thread, true);
            }
            catch (ThreadStateException)
            {
                return null; //we missed this one
            }
            finally
            {
                if (suspended)
                {
                    thread.Resume();
                }
            }
        }

        public override void Start()
        {
            if (IsStarted())
            {
                throw new InvalidOperationException("Server is already running.");
            }
            if (Port == 0)
            {
                throw new InvalidOperationException("Port must be set prior to starting server.");
            }
            if (KeyHash == null)
            {
                throw new InvalidOperationException("Key hash must be set prior to starting server.");
            }
            if (UseSSL && ServerCertificate == null)
            {
                throw new InvalidOperationException("ServerCertificate must be set if using SSL.");
            }
            //make sure we are configured properly
            ConnectorInfoManagerFactory.GetInstance().GetLocalManager();
            _requestCount = 0;
            _pendingRequests.Clear();
            TcpListener socket =
                CreateServerSocket();
            ConnectionListener listener = new ConnectionListener(this, socket);
            listener.Start();
            _listener = listener;
        }

        private TcpListener CreateServerSocket()
        {
            IPAddress addr = IfAddress;

            if (addr == null)
            {
                addr = IOUtil.GetIPAddress("0.0.0.0");
            }
            TcpListener rv = new TcpListener(addr, Port);
            //TODO: specify accept count
            rv.Start();
            return rv;
        }


        public override void Stop()
        {
            if (_listener != null)
            {
                _listener.Shutdown();
                _listener = null;
            }
            ConnectorFacadeFactory.GetInstance().Dispose();
        }
    }
}