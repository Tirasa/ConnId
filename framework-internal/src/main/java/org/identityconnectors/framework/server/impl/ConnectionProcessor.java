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
package org.identityconnectors.framework.server.impl;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.AbstractConnectorInfo;
import org.identityconnectors.framework.impl.api.ConnectorInfoManagerFactoryImpl;
import org.identityconnectors.framework.impl.api.ObjectStreamHandler;
import org.identityconnectors.framework.impl.api.StreamHandlerUtil;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteFrameworkConnection;
import org.identityconnectors.framework.impl.api.remote.messages.EchoMessage;
import org.identityconnectors.framework.impl.api.remote.messages.HelloRequest;
import org.identityconnectors.framework.impl.api.remote.messages.HelloResponse;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequest;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequestMoreData;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponseEnd;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePart;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePause;
import org.identityconnectors.framework.server.ConnectorServer;


public class ConnectionProcessor implements Runnable {

    private static final Log _log = Log.getLog(ConnectionListener.class);

    private static class RemoteResultsHandler 
    implements ObjectStreamHandler {
        private static final int PAUSE_INTERVAL = 200;

        private final RemoteFrameworkConnection _connection;
        private long _count = 0;
        public RemoteResultsHandler(RemoteFrameworkConnection conn) {
            _connection = conn;
        }
                
        public boolean handle(Object obj) {
            try {
                OperationResponsePart part = 
                    new OperationResponsePart(null,obj);
                _connection.writeObject(part);
                _count++;
                if ( _count % PAUSE_INTERVAL == 0 ) {
                    _connection.writeObject(new OperationResponsePause());
                    Object message = 
                        _connection.readObject();
                    return message instanceof OperationRequestMoreData;      
                }
                else {
                    return true;
                }
            }
            catch (RuntimeException e) {
                if ( e.getCause() instanceof IOException ) {
                    throw new BrokenConnectionException((IOException)e.getCause());
                }
                else {
                    throw e;
                }
            }
        }

    }
    
    private final ConnectorServer _server;
    private final RemoteFrameworkConnection _connection;
    
    public ConnectionProcessor(ConnectorServer server,
            Socket socket) {
        _server = server;
        _connection = new RemoteFrameworkConnection(socket);
    }
    
    public void run() {
        try {
            try {
                while ( true ) {
                    boolean keepGoing = processRequest();
                    if (!keepGoing) {
                        break;
                    }
                }
            }
            finally {
                try {
                    _connection.close();
                }
                catch (Exception e) {
                    _log.error(e, null);
                }
            }
        }
        catch (Throwable e) {
            _log.error(e, null);
        }
    }
    
    private boolean processRequest() 
        throws Exception {
        Locale locale;
        try {
            locale = (Locale)_connection.readObject();
        }
        catch (RuntimeException e) {
            if ( e.getCause() instanceof EOFException ) {
                return false;
            }
            throw e;
        }
        CurrentLocale.set(locale);
        GuardedString key = (GuardedString)_connection.readObject();
        
        boolean authorized;
        try {
            authorized = key.verifyBase64SHA1Hash(_server.getKeyHash());
        }
        finally {
            key.dispose();
        }
        InvalidCredentialException authException = null;
        if (!authorized) {
            authException =
                new InvalidCredentialException("Remote framework key is invalid");

        }
        Object requestObject = _connection.readObject();
        if ( requestObject instanceof HelloRequest ) {
            if (authException != null) {
                HelloResponse response =
                    new HelloResponse(authException,null);
                _connection.writeObject(response);
            }
            else {
                HelloResponse response = 
                    processHelloRequest((HelloRequest)requestObject);
                _connection.writeObject(response);
            }
        }
        else if ( requestObject instanceof OperationRequest ) {
            if ( authException != null ) {
                OperationResponsePart part =
                    new OperationResponsePart(authException,null);
                _connection.writeObject(part);
            }
            else {
                OperationRequest opRequest =
                    (OperationRequest)requestObject;
                OperationResponsePart part =
                    processOperationRequest(opRequest);
                _connection.writeObject(part);
            }
        }
        else if (requestObject instanceof EchoMessage) {
            if ( authException != null ) {
                //echo message probably doesn't need auth, but
                //it couldn't hurt - actually it does for test connection
                EchoMessage part =
                    new EchoMessage(authException,null);
                _connection.writeObject(part);
            }
            else {                    
                EchoMessage message = (EchoMessage)requestObject;
                Object obj = message.getObject();
                String xml = message.getXml();
                if ( xml != null ) {
                    Object xmlClone =
                        SerializerUtil.deserializeXmlObject(xml,true);
                    xml =
                        SerializerUtil.serializeXmlObject(xmlClone,true);                    
                }
                EchoMessage message2 = new EchoMessage(obj,xml);
                _connection.writeObject(message2);
            }
        }
        else {
            throw new ConnectorException("Unexpected request: "+requestObject);
        }
        return true;
    }
    
    private ConnectorInfoManager getConnectorInfoManager() {
        ConnectorInfoManagerFactoryImpl factory = (ConnectorInfoManagerFactoryImpl) ConnectorInfoManagerFactory.getInstance();
        return factory.getLocalManager(_server.getBundleURLs(), _server.getBundleParentClassLoader());
    }
    
    private HelloResponse processHelloRequest(HelloRequest request) {
        List<RemoteConnectorInfoImpl> connectorInfo;
        Exception exception = null;
        try {
            ConnectorInfoManager manager =
                getConnectorInfoManager();
            
            List<ConnectorInfo> localInfos =
                manager.getConnectorInfos();
            connectorInfo = new ArrayList<RemoteConnectorInfoImpl>();
            for (ConnectorInfo localInfo : localInfos) {
                LocalConnectorInfoImpl localInfoImpl = 
                    (LocalConnectorInfoImpl)localInfo;
                RemoteConnectorInfoImpl remoteInfo =
                    localInfoImpl.toRemote();
                connectorInfo.add(remoteInfo);
            }
        }
        catch (Exception e) {
            exception = e;
            connectorInfo = null;
        }
        return new HelloResponse(exception,connectorInfo);
    }
        
    private Method getOperationMethod(OperationRequest request) {
        Method [] methods = 
            request.getOperation().getDeclaredMethods();
        Method found = null;
        for (Method m : methods ) {
            if ( m.getName().equalsIgnoreCase(request.getOperationMethodName())) {
                if ( found != null) {
                    throw new ConnectorException("APIOperations are expected "
                            +"to have exactly one method of a given name: "+request.getOperation());
                }
                found = m;
            }
        }
        if ( found == null) {
            throw new ConnectorException("APIOperations are expected "
                    +"to have exactly one method of a given name: "+request.getOperation());
        }
        return found;
    }
    
    private OperationResponsePart 
    processOperationRequest(OperationRequest request) 
    throws IOException {
        Object result;
        Throwable exception = null;
        try {
            Method method = getOperationMethod(request);
            APIOperation operation = getAPIOperation(request);
            List<Object> arguments = request.getArguments();
            List<Object> argumentsAndStreamHandlers =
                populateStreamHandlers(method.getParameterTypes(),
                        arguments);
            try {
                result = method.invoke(operation, argumentsAndStreamHandlers.toArray());
            }
            catch (InvocationTargetException e) {
                throw e.getCause();
            }
            boolean anyStreams =
                argumentsAndStreamHandlers.size() > arguments.size();
            if ( anyStreams ) {
                try {
                    _connection.writeObject(new OperationResponseEnd());
                }
                catch (RuntimeException e) {
                    if ( e.getCause() instanceof IOException ) {
                        throw new BrokenConnectionException((IOException)e.getCause());
                    }
                    else {
                        throw e;
                    }
                }
            }
        }
        catch (BrokenConnectionException w) {
            //at this point the stream is broken - just give up
            throw w.getIOException();
        }
        catch (Throwable e) {
            _log.error(e, null);
            exception = e;
            result = null;
        }
        return new OperationResponsePart(exception,result);
    }
    
    private List<Object> populateStreamHandlers(Class<?> [] paramTypes, List<Object> arguments) {
        List<Object> rv = new ArrayList<Object>();
        boolean firstStream = true;
        Iterator<Object> argIt = arguments.iterator();
        for (Class<?> paramType : paramTypes) {
            if ( StreamHandlerUtil.isAdaptableToObjectStreamHandler(paramType) ) {
                if (!firstStream) {
                    throw new UnsupportedOperationException("At most one stream handler is supported");
                }
                ObjectStreamHandler osh =
                    new RemoteResultsHandler(_connection);
                rv.add(StreamHandlerUtil.adaptFromObjectStreamHandler(paramType, osh));
                firstStream = false;
            }
            else {
                rv.add(argIt.next());
            }
        }
        return rv;
    }
        
    private APIOperation getAPIOperation(OperationRequest request)
        throws Exception {
        ConnectorInfoManager manager =
            getConnectorInfoManager();
        ConnectorInfo info = manager.findConnectorInfo(
                request.getConnectorKey());
        if ( info == null ) {
            throw new ConnectorException("No such connector: "
                    +request.getConnectorKey()+" ");
        }
        APIConfigurationImpl config = 
            request.getConfiguration();
        
        //re-wire the configuration with its connector info
        config.setConnectorInfo((AbstractConnectorInfo)info);
        
        ConnectorFacade facade = 
            ConnectorFacadeFactory.getInstance().newInstance(config);
        
        return facade.getOperation(request.getOperation());
    }
    
    private static class BrokenConnectionException extends ConnectorException {
        
        static final long serialVersionUID = 0L;
        
        public BrokenConnectionException(IOException ex) {
            super(ex);
        }
        
        public IOException getIOException() {
            return (IOException)getCause();
        }
    }

}
