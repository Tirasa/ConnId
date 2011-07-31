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
package org.identityconnectors.framework.impl.api.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ObjectStreamHandler;
import org.identityconnectors.framework.impl.api.StreamHandlerUtil;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequest;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequestMoreData;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequestStopData;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponseEnd;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePart;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePause;


/**
 * Invocation handler for all of our operations
 */
public class RemoteOperationInvocationHandler implements InvocationHandler {
    private final APIConfigurationImpl _configuration;
    private final Class<? extends APIOperation> _operation;
    
    public RemoteOperationInvocationHandler(APIConfigurationImpl configuration,
            Class<? extends APIOperation> operation) {
        _configuration = configuration;
        _operation = operation;
    }
        
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        //don't proxy toString, hashCode, or equals
        if ( method.getDeclaringClass() == Object.class ) {
            return method.invoke(this, args);
        }
        //partition arguments into arguments that can
        //be simply marshalled as part of the request and
        //those that are response handlers
        List<Object> simpleMarshallArgs =
            CollectionUtil.newList(args);
        ObjectStreamHandler streamHandlerArg =
            extractStreamHandler(method.getParameterTypes(),simpleMarshallArgs);
        
        //build the request object
        RemoteConnectorInfoImpl connectorInfo = 
            (RemoteConnectorInfoImpl)_configuration.getConnectorInfo();
        RemoteFrameworkConnectionInfo connectionInfo = 
            connectorInfo.getRemoteConnectionInfo();
        OperationRequest request = new OperationRequest(
                connectorInfo.getConnectorKey(),
                _configuration,
                _operation,
                method.getName(),
                simpleMarshallArgs);
        
        //create the connection
        RemoteFrameworkConnection connection = 
            new RemoteFrameworkConnection(connectionInfo);
            
        try {
            connection.writeObject(CurrentLocale.get());
            connection.writeObject(connectionInfo.getKey());
            //send the request
            connection.writeObject(request);
            
            //now process the response stream (if any)
            if ( streamHandlerArg != null ) {
                handleStreamResponse(connection,streamHandlerArg);
            }
            
            //finally return the actual return value
            OperationResponsePart response = (OperationResponsePart) connection.readObject();
            if (response.getException() != null) {
                throw ConnectorException.wrap(response.getException());
            }
            return response.getResult();
        } finally {
            connection.close();
        }
        
    }
    
    /**
     * Handles a stream response until the end of the stream
     */
    private static void handleStreamResponse(RemoteFrameworkConnection connection, ObjectStreamHandler streamHandler) throws ConnectorException {
        Object response; 
        boolean handleMore = true;
        while ( true ) {
            response = connection.readObject();
            if ( response instanceof OperationResponsePart ) {
                OperationResponsePart part = (OperationResponsePart)response;
                if ( part.getException() != null ) {
                    throw ConnectorException.wrap(part.getException());
                }
                Object object =
                    part.getResult();
                if ( handleMore ) {
                    handleMore = streamHandler.handle(object);
                }
            }
            else if ( response instanceof OperationResponsePause ) {
                if ( handleMore ) {
                    connection.writeObject(new OperationRequestMoreData());
                }
                else {
                    connection.writeObject(new OperationRequestStopData());
                }
            }
            else if ( response instanceof OperationResponseEnd ) {
                break;
            }
            else {
                throw new ConnectorException("Unexpected response: "+response);
            }
        }            
    }
    
    
    /**
     * Partitions arguments into regular arguments and
     * stream arguments.
     * @param paramTypes The param types of the method
     * @param arguments The passed-in arguments. As a
     * side-effect will be set to just the regular arguments.
     * @return The stream handler arguments.
     */
    private static ObjectStreamHandler extractStreamHandler(Class<?> [] paramTypes,
            List<Object> arguments) {
        ObjectStreamHandler rv = null; 
        List<Object> filteredArguments = new ArrayList<Object>();
        for ( int i = 0; i < paramTypes.length; i++ ) {
            Class<?> paramType = paramTypes[i];
            Object arg = arguments.get(i);
            if (StreamHandlerUtil.isAdaptableToObjectStreamHandler(paramType)) {                
                ObjectStreamHandler handler = StreamHandlerUtil.adaptToObjectStreamHandler(paramType, arg);
                if ( rv != null ) {
                    throw new UnsupportedOperationException("Multiple stream handlers not supported");
                }
                rv = handler;
            }
            else {
                filteredArguments.add(arg);
            }
        }
        arguments.clear();
        arguments.addAll(filteredArguments);
        return rv;
    }

}
