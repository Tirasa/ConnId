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
 */
package org.identityconnectors.framework.impl.api.remote;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.serializer.BinaryObjectDeserializer;
import org.identityconnectors.framework.common.serializer.BinaryObjectSerializer;
import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;

public class RemoteFrameworkConnection implements Closeable {

    private static final Log LOG = Log.getLog(RemoteFrameworkConnection.class);
    private Socket socket;
    private BinaryObjectSerializer encoder;
    private BinaryObjectDeserializer decoder;

    public RemoteFrameworkConnection(RemoteFrameworkConnectionInfo info) {
        try {
            init(info);
        } catch (SocketException e) {
            throw new ConnectorIOException("Failed to init remote connection to "
                    + (null != info ? info.toString() : "null"), e);
        } catch (Exception e) {
            throw new ConnectorException("Failed to init remote connection to "
                    + (null != info ? info.toString() : "null"), e);
        }
    }

    public RemoteFrameworkConnection(Socket socket) {
        try {
            init(socket);
        } catch (SocketException e) {
            throw new ConnectorIOException("Failed to init remote connection to "
                    + (null != socket ? socket.toString() : "null"), e);
        } catch (Exception e) {
            throw new ConnectorException("Failed to init remote connection to "
                    + (null != socket ? socket.toString() : "null"), e);
        }
    }

    private void init(RemoteFrameworkConnectionInfo connectionInfo) throws Exception {
        Socket socket = new Socket();
        socket.setSoTimeout(connectionInfo.getTimeout());
        socket.connect(new InetSocketAddress(connectionInfo.getHost(), connectionInfo.getPort()),
                connectionInfo.getTimeout());
        try {
            if (connectionInfo.getUseSSL()) {
                List<TrustManager> trustManagers = connectionInfo.getTrustManagers();
                TrustManager[] trustManagerArr = null;
                if (null != trustManagers && trustManagers.size() > 0) {
                    // convert empty to null
                    trustManagerArr = trustManagers.toArray(new TrustManager[trustManagers.size()]);
                }
                SSLSocketFactory factory;
                // the only way to get the default keystore is this way
                if (trustManagers == null) {
                    factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                } else {
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, trustManagerArr, null);
                    factory = context.getSocketFactory();
                }

                socket =
                        factory.createSocket(socket, connectionInfo.getHost(), connectionInfo
                                .getPort(), true);
                ((SSLSocket) socket).startHandshake();
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception e2) {
                /* ignore */
            }
            throw e;
        }
        init(socket);
    }

    private void init(Socket socket) throws Exception {
        this.socket = socket;
        InputStream inputStream = this.socket.getInputStream();
        OutputStream outputStream = this.socket.getOutputStream();
        ObjectSerializerFactory factory = ObjectSerializerFactory.getInstance();
        encoder = factory.newBinarySerializer(outputStream);
        decoder = factory.newBinaryDeserializer(inputStream);
    }

    public void close() {
        flush();
        try {
            if (socket instanceof SSLSocket) {
                // SSLSocket doesn't like shutdownOutput/shutdownInput
                socket.close();
            } else {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        } catch (Exception e) {
            LOG.info(e, "Failed to close connection.");
            throw ConnectorException.wrap(e);
        }
    }

    public void flush() {
        encoder.flush();
    }

    public void writeObject(Object object) {
        encoder.writeObject(object);
    }

    public Object readObject() {
        // flush first in case there is any data in the
        // output buffer
        flush();
        return decoder.readObject();
    }
}
