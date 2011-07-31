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

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.common.exceptions.ConnectorException;


public final class Main {
    
    private static final String PROP_PORT = "connectorserver.port";
    private static final String PROP_BUNDLE_DIR = "connectorserver.bundleDir";
    private static final String PROP_LIB_DIR = "connectorserver.libDir";
    private static final String PROP_SSL  = "connectorserver.usessl";
    private static final String PROP_IFADDRESS = "connectorserver.ifaddress";
    private static final String PROP_KEY = "connectorserver.key";
    private static final String PROP_LOGGER_CLASS = "connectorserver.loggerClass";
    
    private static final String DEFAULT_LOG_SPI = "org.identityconnectors.common.logging.StdOutLogger";
    
    private static ConnectorServer _server;
    private static Log _log; // Initialized lazily to avoid early initialization.

    private static void usage() {
        System.out.println("Usage: Main -run -properties <connectorserver.properties>");
        System.out.println("       Main -setKey -key <key> -properties <connectorserver.properties>");
        System.out.println("       Main -setDefaults -properties <connectorserver.properties>");
        System.out.println("NOTE: If using SSL, you must specify the system config");
        System.out.println("    properties: ");
        System.out.println("        -Djavax.net.ssl.keyStore");
        System.out.println("        -Djavax.net.ssl.keyStoreType (optional)");
        System.out.println("        -Djavax.net.ssl.keyStorePassword");
    }
    
    public static void main(String [] args) 
        throws Exception {
        if ( args.length == 0 || args.length % 2 != 1) {
            usage();
            return;
        }
        String propertiesFileName = null;
        String key = null;
        for ( int i = 1; i < args.length; i+=2 ) {
            String name = args[i];
            String value = args[i+1];
            if (name.equalsIgnoreCase("-properties")) {
                propertiesFileName = value;
            }
            else if (name.equalsIgnoreCase("-key")) {
                key = value;
            }
            else {
                usage();
                return;
            }
        }
        String cmd = args[0];
        if ( cmd.equalsIgnoreCase("-run")) {
            if ( propertiesFileName == null || key != null ) {
                usage();
                return;
            }
            Properties properties = 
                IOUtil.loadPropertiesFile(propertiesFileName);
            run(properties);
        }
        else if (cmd.equalsIgnoreCase("-setkey")) {
            if ( propertiesFileName == null || key == null ) {
                usage();
                return;
            }
            Properties properties = IOUtil.loadPropertiesFile(propertiesFileName);
            properties.put(PROP_KEY, SecurityUtil.computeBase64SHA1Hash(key.toCharArray()));
            IOUtil.storePropertiesFile(new File(propertiesFileName),properties);
        }
        else if (cmd.equalsIgnoreCase("-setDefaults")) {
            if ( propertiesFileName == null || key != null ) {
                usage();
                return;
            }
            IOUtil.extractResourceToFile(Main.class, 
                    "connectorserver.properties", 
                    new File(propertiesFileName));
        }
        else {
            usage();
            return;
        }
    }
    
    private static void run(Properties properties) throws Exception {
        if ( _server != null ) {
            // Procrun called main() without calling stop().
            // Do not use a logging statement here to avoid initializing logging 
            // too early just because a bug in procrun.
            System.err.println("Server has already been started");
        }
        
        String portStr = properties.getProperty(PROP_PORT);
        String bundleDirStr = properties.getProperty(PROP_BUNDLE_DIR);
        String libDirStr = properties.getProperty(PROP_LIB_DIR);
        String useSSLStr = properties.getProperty(PROP_SSL);
        String ifAddress = properties.getProperty(PROP_IFADDRESS);
        String keyHash = properties.getProperty(PROP_KEY);
        String loggerClass = properties.getProperty(PROP_LOGGER_CLASS);
        if ( portStr == null ) {
            throw new ConnectorException("connectorserver.properties is missing "+PROP_PORT);
        }
        if ( bundleDirStr == null ) {
            throw new ConnectorException("connectorserver.properties is missing "+PROP_BUNDLE_DIR);
        }
        if ( keyHash == null ) {
            throw new ConnectorException("connectorserver.properties is missing "+PROP_KEY);
        }
        
        if ( loggerClass == null ) {
            loggerClass = DEFAULT_LOG_SPI;
        }
        ensureLoggingNotInitialized();
        System.setProperty(Log.LOGSPI_PROP, loggerClass);
        
        int port = Integer.parseInt(portStr);
        
        // Work around issue 604. It seems that sometimes procrun will run
        // the start method in a thread with a null context class loader.
        if (Thread.currentThread().getContextClassLoader() == null) {
            getLog().warn("Context class loader is null, working around");
            Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
        }
        
        _server = ConnectorServer.newInstance();
        _server.setPort(port);
        _server.setBundleURLs(buildBundleURLs(new File(bundleDirStr)));
        if ( libDirStr != null ) {
            _server.setBundleParentClassLoader(buildLibClassLoader(new File(libDirStr)));
        }
        _server.setKeyHash(keyHash);
        if (useSSLStr != null) {
            boolean useSSL = Boolean.parseBoolean(useSSLStr);
            _server.setUseSSL(useSSL);
        }
        if (ifAddress != null) {
            _server.setIfAddress(InetAddress.getByName(ifAddress));
        }
        _server.start();
        getLog().info("Connector server listening on port "+port);
        _server.awaitStop();
    }
    
    public static void stop(String [] args) {
        if ( _server == null ) {
            // Procrun called stop() without calling main().
            // Do not use a logging statement here to avoid initializing logging 
            // too early just because a bug in procrun.
            System.err.println("Server has not been started yet");
            return;
        }
        
        // Work around issue 604. It seems that sometimes procrun will run
        // the start method in a thread with a null context class loader.
        if (Thread.currentThread().getContextClassLoader() == null) {
            getLog().warn("Context class loader is null, working around");
            Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
        }

        _server.stop();
        // Do not set _server to null, because that way the check in run() fails
        // and we ensure that the server cannot be started twice in the same JVM.
        getLog().info("Connector server stopped");
        // LogManager installs a shutdown hook to reset the handlers (which includes
        // closing any files opened by FileHandler-s). Procrun does not call 
        // JNI_DestroyJavaVM(), so shutdown hooks do not run. We reset the LM here. 
        LogManager.getLogManager().reset();
    }
    
    private static void ensureLoggingNotInitialized() throws Exception {
        Field field = Log.class.getDeclaredField("_cacheSpi");
        field.setAccessible(true);
        if ( field.get(null) != null ) {
            throw new IllegalStateException("Logging has already been initialized");
        }
    }

    private static List<URL> buildBundleURLs(File dir) throws MalformedURLException {
        List<URL> rv = getJarFiles(dir);
        if (rv.isEmpty()) {
            getLog().warn("No bundles found in the bundles directory");
        }
        return rv;
    }

    private static ClassLoader buildLibClassLoader(File dir) throws MalformedURLException {
        List<URL> jars = getJarFiles(dir);
        if (!jars.isEmpty()) {
            return new URLClassLoader(jars.toArray(new URL[jars.size()]), ConnectorInfoManagerFactory.class.getClassLoader());
        }
        return null;

    }

    private static List<URL> getJarFiles(File dir) throws MalformedURLException {
        if (!dir.isDirectory()) {
            throw new ConnectorException(dir.getPath()+" does not exist");
        }
        List<URL> rv = new ArrayList<URL>();
        for (File bundle : dir.listFiles()) {
            if ( bundle.getName().endsWith(".jar")) {
                rv.add(bundle.toURL());
            }
        }
        return rv;
    }
    
    private synchronized static Log getLog() {
        if ( _log == null ) {
            _log = Log.getLog(Main.class);
        }
        return _log;
    }
}
