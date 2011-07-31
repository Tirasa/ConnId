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
package org.identityconnectors.framework.server;

import java.net.InetAddress;
import java.net.URL;
import java.util.List;

import javax.net.ssl.KeyManager;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;


/**
 * Connector server interface. 
 */
public abstract class ConnectorServer {

    // At some point we might make this pluggable, but for now, hard-code
    private static final String IMPL_NAME 
     = "org.identityconnectors.framework.server.impl.ConnectorServerImpl";


    /**
     * The port to listen on;
     */
    private int _port = 0;
    
    /**
     * The number of connections to queue
     */
    private int _maxConnections = 300;
    
    /**
     * The base 64-encoded hash of the key
     */
    private String _keyHash;
    
    /**
     * The minimum number of worker threads
     */
    private int _minWorkers = 10;
    
    /**
     * The maximum number of worker threads
     */
    private int _maxWorkers = 100;
    
    /**
     * The network interface address to use. 
     */
    private InetAddress _ifAddress = null;
    
    /**
     * Listen on SSL
     */
    private boolean _useSSL = false;
    
    /**
     * The bundle URLs for connectors to be hosted in this server.
     */
    private List<URL> _bundleURLs = null;
    
    /**
     * The class loader that will be used as the parent of the bundle class loaders.
     * May be null. MUST be able to load framework and framework-internal classes.
     */
    private ClassLoader bundleParentClassLoader;
        
    /**
     * The key. managers to use for the connection. If empty,
     * uses JVM defaults. Ignored for non-SSL.
     */
    private List<KeyManager> _keyManagers =
        CollectionUtil.<KeyManager>newReadOnlyList();
        
    /**
     * Get the singleton instance of the {@link ConnectorServer}.
     */
    public static ConnectorServer newInstance() {
        try {
            Class<?> clazz = Class.forName(IMPL_NAME);
            return (ConnectorServer)clazz.newInstance();
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }
    
    private void assertNotStarted() {
        if ( isStarted() ) {
            throw new IllegalStateException("Operation cannot be performed " +
            		"while server is running");
        }
    }
    
    /**
     * Returns the port to listen on.
     * @return The port to listen on.
     */
    public int getPort() {
        return _port;
    }
    
    /**
     * Sets the port to listen on.
     * @param port The port to listen on
     */
    public void setPort(int port) {
        assertNotStarted();
        _port = port;
    }

    /**
     * Returns the max connections to queue
     * @return The max connections to queue
     */
    public int getMaxConnections() {
        return _maxConnections;
    }
    
    /**
     * Sets the max connections to queue.
     * @param max The max connections to queue.
     */
    public void setMaxConnections(int max) {
        assertNotStarted();
        _maxConnections = max;
    }
    
    /**
     * Returns the max worker threads to allow.
     * @return The max worker threads to allow.
     */
    public int getMaxWorkers() {
        return _maxWorkers;
    }
    
    /**
     * Sets the max worker thread to allow.
     * @param maxWorkers The max worker threads to allow.
     */
    public void setMaxWorkers(int maxWorkers) {
        assertNotStarted();
        _maxWorkers = maxWorkers;
    }

    /**
     * Returns the min worker threads to allow.
     * @return The min worker threads to allow.
     */
    public int getMinWorkers() {
        return _minWorkers;
    }
    
    /**
     * Sets the min worker thread to allow.
     * @param minWorkers The min worker threads to allow.
     */
    public void setMinWorkers(int minWorkers) {
        assertNotStarted();
        _minWorkers = minWorkers;
    }

    /**
     * Returns the network interface address to bind to. May be null.
     * @return The network interface address to bind to or null.
     */
    public InetAddress getIfAddress() {
        return _ifAddress;
    }
    
    /**
     * Sets the interface address to bind to.
     * @param addr The network interface address to bind to or null.
     */
    public void setIfAddress(InetAddress addr) {
        assertNotStarted();
        _ifAddress = addr;
    }
    
    /**
     * Returns true iff we are to use SSL.
     * @return true iff we are to use SSL.
     */
    public boolean getUseSSL() {
        return _useSSL;
    }
    
    /**
     * Sets whether we should use ssl.
     * @param ssl true iff we are to use SSL.
     */
    public void setUseSSL(boolean ssl) {
        assertNotStarted();
        _useSSL = ssl;
    }
    
    /**
     * Returns the base-64 encoded SHA1 hash of the key
     * @return the base-64 encoded SHA1 hash of the key
     */
    public String getKeyHash() {
        return _keyHash;
    }
    
    /**
     * Sets the base-64 encoded SHA1 hash of the key
     * @param hash the base-64 encoded SHA1 hash of the key
     */
    public void setKeyHash(String hash) {
        assertNotStarted();
        _keyHash = hash;
    }
        
    /**
     * Returns the key managers to use for the SSL connection.
     * If empty, use the JVM default.
     * @return the key managers to use for the SSL connection.
     */
    public List<KeyManager> getKeyManagers() {
        return _keyManagers;
    }
    
    /**
     * Sets the key managers to use for the SSL connection.
     * @param keyManagers the key managers to use for the SSL connection.
     * If null or empty, uses the JVM default.
     */
    public void setKeyManagers(List<KeyManager> keyManagers) {
        assertNotStarted();
        _keyManagers = CollectionUtil.newReadOnlyList(keyManagers);
    }
    
    /**
     * Gets the bundle URLs for connectors to expose by this server.
     * @return The bundle URLs for connectors to expose by this server.
     */
    public List<URL> getBundleURLs() {
        return _bundleURLs;
    }
    
    /**
     * Sets the bundle URLs for connectors to expose by this server.
     * @param urls The bundle URLs for connectors to expose by this server.
     */
    public void setBundleURLs(List<URL> urls) {
        assertNotStarted();
        _bundleURLs = CollectionUtil.newReadOnlyList(urls);
    }

    /**
     * Gets the class loader that will be used as the parent of the bundle class loaders.
     * @return the class loader that will be used as the parent of the bundle class loaders.
     */
    public ClassLoader getBundleParentClassLoader() {
        return bundleParentClassLoader;
    }

    /**
     * Sets the class loader that will be used as the parent of the bundle class loaders.
     * @param bundleParentClassLoader the class loader that will be used as the parent 
     *              of the bundle class loaders.
     */
    public void setBundleParentClassLoader(ClassLoader bundleParentClassLoader) {
        this.bundleParentClassLoader = bundleParentClassLoader;
    }

    /**
     * Starts the server. All server settings must be configured prior
     * to calling. The following methods are required to be called:
     * <ul>
     *   <li>{@link #setBundleURLs(List)}</li>
     *   <li>{@link #setPort(int)}</li>
     *   <li>{@link #setKeyHash(String)}</li>
     * </ul>
     */
    abstract public void start();
    
    /**
     * Stops the server gracefully. Returns when all in-progress connections
     * have been serviced.
     */
    abstract public void stop();
    
    /**
     * Return true iff the server is started. Note that started is a
     * logical state (start method has been called). It does not necessarily 
     * reflect the health of the server
     * @return true iff the server is started.
     */
    abstract public boolean isStarted();
    
    /**
     * Waits for the server to stop. Similarly to the {@link #isStarted()} method,
     * this method depends on the server's logical state. The trigger that
     * wakes up waiting threads is a call to the {@link #stop()} method, not
     * the health of the server.
     * @throws InterruptedException if the waiting thread is interrupted.
     */
    abstract public void awaitStop() throws InterruptedException;
}
