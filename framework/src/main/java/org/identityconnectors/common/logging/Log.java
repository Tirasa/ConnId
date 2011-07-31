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
package org.identityconnectors.common.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.ReflectionUtil;
import org.identityconnectors.common.StringUtil;


/**
 * Yet another logging abstraction.
 */
public final class Log {

    /**
     * Default SPI implementation should all attempts to load the custom logger
     * fail.
     */
    private static Class<?> DEFAULT_SPI = StdOutLogger.class;

    // Hack for OIM: this ought to be Log.class.getPackage().getName(). However, OIM's
    // tcADPClassLoader does not use ClassLoader.definePackage(), and so Log.class.getPackage()
    // returns null.
    private static final String PACKAGE = ReflectionUtil.getPackage(Log.class);

    /**
     * System property to set the logger class that is most appropriate.
     */
    public static final String LOGSPI_PROP = PACKAGE + ".class";

    /**
     * Filename 'connectors.properties' used to for SPI class in the
     * '$(java.home)/lib/' directory.
     */
    public static final String LOGSPI_PROPS_FILE = "connectors.properties";

    /**
     * Cache the SPI class so we one search for it once.
     */
    private static Class<?> _cacheSpi;

    /**
     * Basic logging levels.
     */
    public static enum Level {
        /**
         * Maps to java.util.logging.Level.FINE.
         */
        OK,

        /**
         * Maps to java.util.logging.Level.INFO.
         */
        INFO,

        /**
         * Maps to java.util.logging.Level.WARNING
         */
        WARN,

        /**
         * Maps to java.util.logging.Level.SEVERE
         */
        ERROR;
    }

    /**
     * Class to log.
     */
    private Class<?> _clazz;

    /**
     * Implementation to use for logging.
     */
    private LogSpi _logImpl;

    /**
     * Create an instance of the log based on the SPI in the System properties.
     */
    private Log(Class<?> clazz, LogSpi logImpl) {
        _clazz = clazz;
        _logImpl = logImpl;
    }

    /**
     * Used for testing..
     */
    static Log getLog(final Class<?> clazz, LogSpi logImpl) {
        return new Log(clazz, logImpl);
    }

    /**
     * Get the logger for the particular class. <code>
     * private static final Log LOG = Log.getLog(MyClass.class);
     * </code>
     * 
     * @param clazz
     *            class to log information about.
     * @return logger to use for logging.
     */
    public static Log getLog(final Class<?> clazz) {
        try {
            // check that we're not logging ourselves
            if (LogSpi.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException();
            }
            // attempt to get an instance..
            LogSpi logImpl = (LogSpi) getSpiClass().newInstance();
            return new Log(clazz, logImpl);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determine if its log-able at this level within this class.
     */
    public boolean isLoggable(final Level level) {
        return _logImpl.isLoggable(_clazz, level);
    }

    // =======================================================================
    // Helper Methods..
    // =======================================================================
    /**
     * Lowest level logging method.
     * 
     * @param clazz
     *            Class that is being logged.
     * @param method
     *            Method name that is being logged.
     * @param level
     *            Logging level.
     * @param message
     *            Message about the log.
     * @param ex
     *            Exception to use process.
     */
    public void log(Class<?> clazz, String method, Log.Level level,
            String message, Throwable ex) {
        if (isLoggable(level)) {
            _logImpl.log(clazz, method, level, message, ex);
        }
    }

    /**
     * Logs based on the parameters given. Uses the format parameter inside
     * {@link MessageFormat}.
     * 
     * @param level
     *            the logging level at which to write the message.
     * @param ex
     *            [optional] exception stack trace to log.
     * @param format
     *            [optional] create a message of a particular format.
     * @param args
     *            [optional] parameters to the format string.
     */
    public void log(final Level level, final Throwable ex, final String format,
            final Object... args) {
        if (isLoggable(level)) {
            String message = format;
            if (format != null && args != null) {
                // consider using thread local pattern to cache these for
                // performance the pattern will always may always changed.
                message = MessageFormat.format(format, args);
            } else if (format == null && ex != null) {
                message = ex.getLocalizedMessage();
            }
            String methodName = ReflectionUtil.getMethodName(3);
            log(_clazz, methodName, level, message, ex);
        }
    }

    public void ok(Throwable ex, String format, Object... args) {
        log(Level.OK, ex, format, args);
    }

    public void info(Throwable ex, String format, Object... args) {
        log(Level.INFO, ex, format, args);
    }

    public void warn(Throwable ex, String format, Object... args) {
        log(Level.WARN, ex, format, args);
    }

    public void error(Throwable ex, String format, Object... args) {
        log(Level.ERROR, ex, format, args);
    }

    public void ok(String format, Object... args) {
        log(Level.OK, null, format, args);
    }

    public void info(String format, Object... args) {
        log(Level.INFO, null, format, args);
    }

    public void warn(String format, Object... args) {
        log(Level.WARN, null, format, args);
    }

    public void error(String format, Object... args) {
        log(Level.ERROR, null, format, args);
    }

    public boolean isOk() {
        return isLoggable(Level.OK);
    }

    public boolean isInfo() {
        return isLoggable(Level.INFO);
    }

    public boolean isWarning() {
        return isLoggable(Level.WARN);
    }

    public boolean isError() {
        return isLoggable(Level.ERROR);
    }

    /**
     * Finds the logging implementation Class object in the specified order:
     * <ol>
     * <li>query the system property using <code>System.getProperty</code></li>
     * <li>read <code>$java.home/lib/<i>connectors.properties</i></code>
     * file</li>
     * <li>read
     * <code>META-INF/services/<i>org.identityconnectors.common.logging.class</i></code>
     * file</li>
     * <li>use {@link org.identityconnectors.common.logging.impl.StdOutLogger}
     * as a fallback.</li>
     * </ol>
     */
    private static Class<?> findSpiClass() {
        // Use the system property first
        String impl = System.getProperty(LOGSPI_PROP);
        if (StringUtil.isNotBlank(impl)) {
            return forName(impl);
        }
        // attempt to find the properties file..
        File javaHome = new File(System.getProperty("java.home"));
        File javaHomeLib = new File(javaHome, "lib");
        File propsFile = new File(javaHomeLib, LOGSPI_PROPS_FILE);
        if (propsFile.isFile() && propsFile.canRead()) {
            FileInputStream fis = null;
            try {
                Properties props = new Properties();
                fis = new FileInputStream(propsFile);
                props.load(fis);
                // get the same system property from the properties file..
                String prop = props.getProperty(LOGSPI_PROP);
                if (StringUtil.isNotBlank(prop)) {
                    return forName(prop);
                }
            } catch (IOException e) {
                // throw to alert the caller the file is corrupt
                throw new RuntimeException(e);
            } finally {
                IOUtil.quietClose(fis);
            }
        }
        // attempt to find through the jar META-INF/services..
        String serviceId = "META-INF/services/" + PACKAGE;
        String clazz = IOUtil.getResourceAsString(Log.class, serviceId);
        if (StringUtil.isNotBlank(clazz)) {
            return forName(clazz.trim());
        }
        // return the default..
        return DEFAULT_SPI;
    }

    /**
     * Simple helper function to prevent duplicate code.
     */
    private static Class<?> forName(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cache the search for the SPI class.
     */
    static Class<?> getSpiClass() {
        // initialize the SPI class cache object
        synchronized (Log.class) {
            if (_cacheSpi == null) {
                _cacheSpi = findSpiClass();
            }
        }
        return _cacheSpi;
    }

    /**
     * For <strong>test</strong> only.
     */
    static void setSpiClass(Class<?> clazz) {
        synchronized (Log.class) {
            _cacheSpi = clazz;
        }
    }
}
