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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 */

package org.identityconnectors.framework.server.impl;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.impl.api.ConnectorInfoManagerFactoryImpl;
import org.identityconnectors.framework.impl.api.ObjectStreamHandler;
import org.identityconnectors.framework.impl.api.StreamHandlerUtil;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteFrameworkConnection;
import org.identityconnectors.framework.impl.api.remote.messages.EchoMessage;
import org.identityconnectors.framework.impl.api.remote.messages.ErrorResponse;
import org.identityconnectors.framework.impl.api.remote.messages.HelloRequest;
import org.identityconnectors.framework.impl.api.remote.messages.HelloResponse;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequest;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequestMoreData;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponseEnd;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePart;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePause;
import org.identityconnectors.framework.server.ConnectorServer;

public class ConnectionProcessor implements Runnable {

    private static final Log LOG = Log.getLog(ConnectionListener.class);

    private static class RemoteResultsHandler implements ObjectStreamHandler {
        private static final int PAUSE_INTERVAL = 200;

        private final RemoteFrameworkConnection connection;
        private long count = 0;

        public RemoteResultsHandler(RemoteFrameworkConnection conn) {
            connection = conn;
        }

        @Override
        public boolean handle(Object obj) {
            try {
                OperationResponsePart part = new OperationResponsePart(null, obj);
                connection.writeObject(part);
                count++;
                if (count % PAUSE_INTERVAL == 0) {
                    connection.writeObject(new OperationResponsePause());
                    Object message = connection.readObject();
                    return message instanceof OperationRequestMoreData;
                } else {
                    return true;
                }
            } catch (RuntimeException e) {
                if (e.getCause() instanceof IOException) {
                    throw new BrokenConnectionException((IOException) e.getCause());
                } else {
                    throw e;
                }
            }
        }

    }

    private final ConnectorServer connectorServer;
    private final RemoteFrameworkConnection connection;

    public ConnectionProcessor(ConnectorServer server, Socket socket) {
        connectorServer = server;
        connection = new RemoteFrameworkConnection(socket);
    }

    @Override
    public void run() {
        try {
            try {
                while (true) {
                    boolean keepGoing = processRequest();
                    if (!keepGoing) {
                        break;
                    }
                }
            } finally {
                try {

                    LOG.ok("Attempting to close the current connection chanel");
                    connection.close();
                } catch (Exception e) {

                    LOG.error(e, "Exception occurred during connection termination: {0}", e.getLocalizedMessage());
                }
            }
        } catch (Throwable e) {

            LOG.error(e, "The following exception occurred during the processing of the current request: {0}", e.getLocalizedMessage());
        }
    }

    private boolean processRequest() throws Exception {
        Locale locale;
        try {
            locale = (Locale) connection.readObject();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof EOFException) {

                LOG.warn("Handled exception occurred while processing request: {0}", e.getLocalizedMessage());

                return false;
            }
            throw e;
        }
        CurrentLocale.set(locale);

        LOG.ok("The request locale: {0}", locale.toString());
        GuardedString key = (GuardedString) connection.readObject();

        boolean authorized;
        try {
            authorized = key.verifyBase64SHA1Hash(connectorServer.getKeyHash());
        } finally {
            key.dispose();
        }
        InvalidCredentialException authException = null;
        if (!authorized) {
            authException = new InvalidCredentialException("Remote framework key is invalid.");

            LOG.ok("Un-authorized, remote framework key is invalid");
        } else {

            LOG.ok("Request authorized, remote framework key is valid");
        }

        Object requestObject;
        try {
            requestObject = connection.readObject();
        } catch (Exception e) {

                LOG.error(e,"An exception was thrown from initial connection object read: {0}",e.getLocalizedMessage());
            ErrorResponse errorMessage = new ErrorResponse(e);
            connection.writeObject(errorMessage);
            throw e;
        }

        if (requestObject instanceof HelloRequest) {

            LOG.ok("Processing HelloRequest");
            if (authException != null) {
                HelloResponse response = new HelloResponse(authException, null, null, null);
                connection.writeObject(response);
            } else {
                HelloResponse response = processHelloRequest((HelloRequest) requestObject);
                connection.writeObject(response);
            }
        } else if (requestObject instanceof OperationRequest) {

            LOG.ok("Processing operation request");
            if (authException != null) {
                OperationResponsePart part = new OperationResponsePart(authException, null);
                connection.writeObject(part);
            } else {
                OperationRequest opRequest = (OperationRequest) requestObject;
                OperationResponsePart part = processOperationRequest(opRequest);
                connection.writeObject(part);
            }
        } else if (requestObject instanceof EchoMessage) {

            LOG.ok("Processing EchoMessage");
            if (authException != null) {
                // echo message probably doesn't need auth, but
                // it couldn't hurt - actually it does for test connection
                EchoMessage part = new EchoMessage(authException, null);
                connection.writeObject(part);
            } else {
                EchoMessage message = (EchoMessage) requestObject;
                Object obj = message.getObject();
                String xml = message.getXml();
                if (xml != null) {
                    Object xmlClone = SerializerUtil.deserializeXmlObject(xml, true);
                    xml = SerializerUtil.serializeXmlObject(xmlClone, true);
                }
                EchoMessage message2 = new EchoMessage(obj, xml);
                connection.writeObject(message2);
            }
        } else {
            throw new ConnectorException("Unexpected request: " + requestObject);
        }
        return true;
    }

    private ConnectorInfoManager getConnectorInfoManager() {
        ConnectorInfoManagerFactoryImpl factory =
                (ConnectorInfoManagerFactoryImpl) ConnectorInfoManagerFactory.getInstance();
        return factory.getLocalManager(connectorServer.getBundleURLs(), connectorServer
                .getBundleParentClassLoader());
    }

    private HelloResponse processHelloRequest(HelloRequest request) {
        List<RemoteConnectorInfoImpl> connectorInfo = null;
        List<ConnectorKey> connectorKeys = null;
        Map<String, Object> serverInfo = null;
        Exception exception = null;
        try {
            serverInfo = new HashMap<String, Object>(1);
            if (request.isServerInfo()) {
                serverInfo.put(HelloResponse.SERVER_START_TIME, connectorServer.getStartTime());

                LOG.ok("Appended Server information to the Hello Request response");
            }
            if (request.isConnectorKeys()) {
                ConnectorInfoManager manager = getConnectorInfoManager();
                List<ConnectorInfo> localInfos = manager.getConnectorInfos();
                connectorKeys = new ArrayList<ConnectorKey>();
                for (ConnectorInfo localInfo : localInfos) {
                    ConnectorKey connectorKey = localInfo.getConnectorKey();
                    connectorKeys.add(connectorKey);

                    LOG.ok("Appended connector name {0}, connector version {1} to server hello response", connectorKey.getBundleName(), connectorKey.getBundleVersion());
                }
                if (!connectorKeys.isEmpty()) {

                    LOG.ok("Appended Connector Keys information to the Hello Request response");
                }

                if (request.isConnectorInfo()) {
                    connectorInfo = new ArrayList<RemoteConnectorInfoImpl>();
                    for (ConnectorInfo localInfo : localInfos) {
                        LocalConnectorInfoImpl localInfoImpl = (LocalConnectorInfoImpl) localInfo;
                        RemoteConnectorInfoImpl remoteInfo = localInfoImpl.toRemote();
                        connectorInfo.add(remoteInfo);
                    }
                    if (!connectorInfo.isEmpty()) {

                        LOG.ok("Finished adding Connector information to the Hello Request response.");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Exception occurred during the processing of the HelloRequest: {0} ", e.getLocalizedMessage());
            exception = e;
            connectorInfo = null;
        }
        return new HelloResponse(exception, serverInfo, connectorKeys, connectorInfo);
    }

    private Method getOperationMethod(OperationRequest request) {
        Class<? extends APIOperation> operation = request.getOperation();
        Method[] methods = operation.getDeclaredMethods();
        Method found = null;
        for (Method m : methods) {
            if (m.getName().equalsIgnoreCase(request.getOperationMethodName())) {
                if (found != null) {
                    throw new ConnectorException("APIOperations are expected "
                            + "to have exactly one method of a given name: "
                            + operation);
                }
                found = m;
            }
        }
        if (found == null) {
            throw new ConnectorException("APIOperations are expected "
                    + "to have exactly one method of a given name: " + operation);
        }
        return found;
    }

    private OperationResponsePart processOperationRequest(OperationRequest request)
            throws IOException {
        Object result;
        Throwable exception = null;
        try {
            Method method = getOperationMethod(request);
            APIOperation operation = getAPIOperation(request);
            List<Object> arguments = request.getArguments();
            List<Object> argumentsAndStreamHandlers =
                    populateStreamHandlers(method.getParameterTypes(), arguments);

            try {

                LOG.ok("Request of the API Operation: {0}, invoking the API Method: {1}",
                        operation.getClass().getSimpleName(), method.getName());

                if (LOG.isOk()) {
                    if (arguments != null && !arguments.isEmpty()) {
                    } else {
                        LOG.ok("Request being processed does not contain any arguments");
                    }
                }

                result = method.invoke(operation, argumentsAndStreamHandlers.toArray());
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            boolean anyStreams = argumentsAndStreamHandlers.size() > arguments.size();
            if (anyStreams) {
                try {
                    LOG.ok("Writing blank operation response");
                    connection.writeObject(new OperationResponseEnd());
                } catch (RuntimeException e) {
                    if (e.getCause() instanceof IOException) {
                        throw new BrokenConnectionException((IOException) e.getCause());
                    } else {
                        throw e;
                    }
                }
            }
        } catch (BrokenConnectionException w) {
            // at this point the stream is broken - just give up
            throw w.getIOException();
        } catch (Throwable e) {
            LOG.error(e, "Exception occurred during the processing of an operation request: {0}", e.getLocalizedMessage());
            exception = e;
            result = null;
        }
        LOG.ok("Writing the processed request result out as operation response");
        return new OperationResponsePart(exception, result);
    }

    private List<Object> populateStreamHandlers(Class<?>[] paramTypes, List<Object> arguments) {
        List<Object> rv = new ArrayList<Object>();
        boolean firstStream = true;
        Iterator<Object> argIt = arguments.iterator();
        for (Class<?> paramType : paramTypes) {
            if (StreamHandlerUtil.isAdaptableToObjectStreamHandler(paramType)) {
                if (!firstStream) {
                    throw new UnsupportedOperationException(
                            "At most one stream handler is supported");
                }
                ObjectStreamHandler osh = new RemoteResultsHandler(connection);
                rv.add(StreamHandlerUtil.adaptFromObjectStreamHandler(paramType, osh));
                firstStream = false;
            } else {
                rv.add(argIt.next());
            }
        }
        return rv;
    }

    private APIOperation getAPIOperation(OperationRequest request) throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = manager.findConnectorInfo(request.getConnectorKey());
        Class<? extends APIOperation> requestedOperation = request.getOperation();

        LOG.ok("About to get the operation {0}", requestedOperation.getSimpleName());
        if (info == null) {
            throw new ConnectorException("No such connector: " + request.getConnectorKey() + " ");
        }
        String connectorFacadeKey = request.getConnectorFacadeKey();

        ConnectorFacade facade =
                ConnectorFacadeFactory.getManagedInstance().newInstance(info, connectorFacadeKey);

        return facade.getOperation(requestedOperation);
    }

    private static class BrokenConnectionException extends ConnectorException {

        static final long serialVersionUID = 0L;

        public BrokenConnectionException(IOException ex) {
            super(ex);
        }

        public IOException getIOException() {
            return (IOException) getCause();
        }
    }

}
