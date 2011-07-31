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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.BinaryObjectDeserializer;
import org.identityconnectors.framework.common.serializer.BinaryObjectSerializer;
import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;


public class RemoteFrameworkConnection {
    
    private Socket _socket;
    private BinaryObjectSerializer _encoder;
    private BinaryObjectDeserializer _decoder;
    
    public RemoteFrameworkConnection(RemoteFrameworkConnectionInfo info) {
        try {
            init(info);
        }
        catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }
    
    public RemoteFrameworkConnection(Socket socket) {
        try {
            init(socket);
        }
        catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }
    
    private void init(RemoteFrameworkConnectionInfo connectionInfo)
        throws Exception {
        Socket socket = new Socket();
        socket.setSoTimeout(connectionInfo.getTimeout());
        socket.connect(new InetSocketAddress(connectionInfo.getHost(),
                connectionInfo.getPort()),
                connectionInfo.getTimeout());
        try {
            if (connectionInfo.getUseSSL()) {
                List<TrustManager> trustManagers = connectionInfo.getTrustManagers();
                TrustManager [] trustManagerArr = null;
                if ( trustManagers.size() > 0 ) { //convert empty to null
                    trustManagerArr = trustManagers.toArray(new TrustManager[trustManagers.size()]);
                }
                SSLSocketFactory factory;
                //the only way to get the default keystore is this way
                if (trustManagers == null) {
                    factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
                }
                else {
                    SSLContext context = SSLContext.getInstance("TLS");        
                    context.init(null, trustManagerArr, null);
                    factory = context.getSocketFactory();
                }        
                
                socket = factory.createSocket(socket,
                        connectionInfo.getHost(),
                        connectionInfo.getPort(),
                        true);
                ((SSLSocket)socket).startHandshake();
            }
        }
        catch (Exception e) {
            try {
                socket.close();
            }
            catch (Exception e2) {
                
            }
            throw e;
        }
        init(socket);
    }
    
    private void init(Socket socket)
        throws Exception
    {
        _socket = socket;
        InputStream inputStream = _socket.getInputStream();
        OutputStream outputStream = _socket.getOutputStream();
        ObjectSerializerFactory factory =
            ObjectSerializerFactory.getInstance();
        _encoder = factory.newBinarySerializer(outputStream);
        _decoder = factory.newBinaryDeserializer(inputStream);
    }
    
    public void close() {
        flush();
        try {
            if (_socket instanceof SSLSocket) {
                //SSLSocket doesn't like shutdownOutput/shutdownInput
                _socket.close();
            }
            else {
                _socket.shutdownOutput();
                _socket.shutdownInput();
                _socket.close();
            }
        }
        catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }
    
    public void flush() {
        _encoder.flush();
    }
    
    public void writeObject(Object object) {
        _encoder.writeObject(object);
    }
    
    public Object readObject() {
        //flush first in case there is any data in the
        //output buffer
        flush();
        return _decoder.readObject();
    }
}
