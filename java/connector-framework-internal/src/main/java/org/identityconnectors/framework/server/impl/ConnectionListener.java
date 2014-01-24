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
package org.identityconnectors.framework.server.impl;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.server.ConnectorServer;

class ConnectionListener extends CCLWatchThread {

    /**
     * This is the size of our internal queue. For now I have this relatively
     * small because I want the OS to manage the connect queue coming in. That
     * way it can properly turn away excessive requests
     */
    private final static int INTERNAL_QUEUE_SIZE = 2;

    private static final Log LOG = Log.getLog(ConnectionListener.class);

    /**
     * The server object that we are using
     */
    private final ConnectorServer connectorServer;

    /**
     * The server socket. This must be bound at the time of creation.
     */
    private final ServerSocket socket;

    /**
     * Pool of executors
     */
    private final ExecutorService threadPool;

    /**
     * Set to indicated we need to start shutting down
     */
    private boolean stopped = false;

    /**
     * Creates the listener thread
     *
     * @param server
     *            The server object
     * @param socket
     *            The socket (should already be bound)
     */
    public ConnectionListener(ConnectorServer server, ServerSocket socket) {
        super("ConnectionListener");
        connectorServer = server;
        this.socket = socket;
        // idle time timeout
        threadPool =
                new ThreadPoolExecutor(server.getMinWorkers(), server.getMaxWorkers(), 30,
                        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(INTERNAL_QUEUE_SIZE,
                                true), // fair
                        new CCLWatchThreadFactory());
    }

    @Override
    public void run() {
        while (!isStopped()) {
            try {
                Socket connection = socket.accept();
                ConnectionProcessor processor =
                        new ConnectionProcessor(connectorServer, connection);
                // this really sucks - ideally, execute would block
                // if the queue is full. now we have to do a busy wait
                // the effect is that eventually our socket's accept
                // queue will fill up and start rejecting requests
                // at the connection (which is what we want)
                while (true) {
                    try {
                        threadPool.execute(processor);
                        break;
                    } catch (RejectedExecutionException e) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e2) {
                            /* ignore */
                        }
                    }
                }
            } catch (Throwable e) {
                // log the error unless it's because we've stopped
                if (!isStopped() || !(e instanceof SocketException)) {
                    LOG.error(e, "Error processing request");
                }
                // wait a second before trying again
                if (!isStopped()) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e2) {
                        /* ignore */
                    }
                }
            }
        }
    }

    private synchronized void markStopped() {
        stopped = true;
    }

    private synchronized boolean isStopped() {
        return stopped;
    }

    public void shutdown() {
        if (Thread.currentThread() == this) {
            throw new IllegalArgumentException("Shutdown may not be called from this thread");
        }
        if (!isStopped()) {
            try {
                // set the stopped flag so we no its a normal
                // shutdown and don't log the SocketException
                markStopped();
                // close the socket - this causes accept to throw an exception
                socket.close();
                // wait for the main listener thread to die so we don't
                // get any new requests
                join();
                // wait for all in-progress requests to finish
                threadPool.shutdown();
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }
    }
}
