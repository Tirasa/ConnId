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
 * Portions Copyrighted 2014 ForgeRock AS.
 * Portions Copyrighted 2022 ConnId
 */
package org.identityconnectors.common.logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.ReflectionUtil;
import org.identityconnectors.common.StringUtil;

/**
 * Yet another logging abstraction.
 */
public final class Log {

    /**
     * Default SPI implementation should all attempts to load the custom logger fail.
     */
    private static final Class<?> DEFAULT_SPI = StdOutLogger.class;

    // Hack for OIM: this ought to be Log.class.getPackage().getName(). However,
    // OIM's
    // tcADPClassLoader does not use ClassLoader.definePackage(), and so
    // Log.class.getPackage()
    // returns null.
    private static final String PACKAGE = ReflectionUtil.getPackage(Log.class);

    private static final String LOGGER_NAME = Log.class.getName();

    private static final Set<String> EXCLUDE_LIST = CollectionUtil.newReadOnlySet(
            "groovy.", "org.codehaus.groovy.", "gjdk.groovy.", "java.", "javax.", "sun.",
            "com.google.apphosting."// for GAE/J
    );

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
    private static Class<?> cacheSPI;

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
         * Maps to java.util.logging.Level.WARNING.
         */
        WARN,

        /**
         * Maps to java.util.logging.Level.SEVERE.
         */
        ERROR;

    }

    /**
     * Class to log.
     */
    private final Class<?> clazz;

    /**
     * Implementation to use for logging.
     */
    private final LogSpi logImpl;

    /**
     * Create an instance of the log based on the SPI in the System properties.
     */
    private Log(final Class<?> clazz, final LogSpi logImpl) {
        this.clazz = clazz;
        this.logImpl = logImpl;
    }

    /**
     * Used for testing..
     */
    static Log getLog(final Class<?> clazz, final LogSpi logImpl) {
        return new Log(clazz, logImpl);
    }

    /**
     * Get the logger for the particular class. <code>
     * private static final Log LOG = Log.getLog(MyClass.class);
     * </code>
     *
     * @param clazz class to log information about.
     * @return logger to use for logging.
     */
    public static Log getLog(final Class<?> clazz) {
        try {
            // check that we're not logging ourselves
            if (LogSpi.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException();
            }
            // attempt to get an instance..
            final LogSpi logImpl = (LogSpi) getSpiClass().getDeclaredConstructor().newInstance();
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
        return logImpl.isLoggable(clazz, level);
    }

    // =======================================================================
    // Helper Methods..
    // =======================================================================
    /**
     * Lowest level logging method.
     *
     * @param clazz Class that is being logged.
     * @param method Method name that is being logged.
     * @param level Logging level.
     * @param message Message about the log.
     * @param ex Exception to use process.
     */
    public void log(
            final Class<?> clazz,
            final String method,
            final Log.Level level,
            final String message,
            final Throwable ex) {

        if (isLoggable(level)) {
            logImpl.log(clazz, method, level, message, ex);
        }
    }

    /**
     * Logs based on the parameters given. Uses the format parameter inside
     * {@link MessageFormat}.
     *
     * @param level the logging level at which to write the message.
     * @param ex [optional] exception stack trace to log.
     * @param format [optional] create a message of a particular format.
     * @param args [optional] parameters to the format string.
     */
    public void log(
            final Level level,
            final Throwable ex,
            final String format,
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

            // To get the StackTrace is expensive. Extract the method name only if it's necessary!!!
            log(level,
                    ex,
                    message,
                    logImpl.needToInferCaller(clazz, level)
                    ? Thread.currentThread().getStackTrace()
                    : null);
        }
    }

    protected void log(
            final Level level,
            final Throwable ex,
            final String message,
            final StackTraceElement[] locations) {

        Optional.ofNullable(extract(locations, EXCLUDE_LIST)).ifPresentOrElse(
                caller -> logImpl.log(clazz, caller, level, message, ex),
                () -> logImpl.log(clazz, (String) null, level, message, ex));
    }

    protected static StackTraceElement extract(
            final StackTraceElement[] steArray,
            final Collection<String> frameworkPackageList) {

        if (steArray == null) {
            return null;
        }

        for (StackTraceElement current : steArray) {
            if (!isInFrameworkPackageList(current.getClassName(), frameworkPackageList)) {
                return current;
            }
        }

        return null;
    }

    protected static boolean isInFrameworkPackageList(
            final String currentClass,
            final Collection<String> frameworkPackageList) {

        if (frameworkPackageList == null) {
            return false;
        }
        if (LOGGER_NAME.equals(currentClass)) {
            return true;
        }
        for (String s : frameworkPackageList) {
            if (currentClass.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public void ok(final Throwable ex, final String format, final Object... args) {
        log(Level.OK, ex, format, args);
    }

    public void info(final Throwable ex, final String format, final Object... args) {
        log(Level.INFO, ex, format, args);
    }

    public void warn(final Throwable ex, final String format, final Object... args) {
        log(Level.WARN, ex, format, args);
    }

    public void error(final Throwable ex, final String format, final Object... args) {
        log(Level.ERROR, ex, format, args);
    }

    public void ok(final String format, final Object... args) {
        log(Level.OK, null, format, args);
    }

    public void info(final String format, final Object... args) {
        log(Level.INFO, null, format, args);
    }

    public void warn(final String format, final Object... args) {
        log(Level.WARN, null, format, args);
    }

    public void error(final String format, final Object... args) {
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
     * <li>read <code>$java.home/lib/<i>connectors.properties</i></code> file</li>
     * <li>read
     * <code>META-INF/services/<i>org.identityconnectors.common.logging.class</i></code>
     * file</li>
     * <li>use {@link org.identityconnectors.common.logging.StdOutLogger}
     * as a fallback.</li>
     * </ol>
     */
    private static Class<?> findSpiClass() {
        // Use the system property first
        final String impl = System.getProperty(LOGSPI_PROP);
        if (StringUtil.isNotBlank(impl)) {
            return forName(impl);
        }
        // attempt to find the properties file..
        File propsFile = Path.of(System.getProperty("java.home")).resolve("lib").resolve(LOGSPI_PROPS_FILE).toFile();
        if (propsFile.isFile() && propsFile.canRead()) {
            try (InputStream fis = Files.newInputStream(propsFile.toPath())) {
                Properties props = new Properties();
                props.load(fis);
                // get the same system property from the properties file..
                String prop = props.getProperty(LOGSPI_PROP);
                if (StringUtil.isNotBlank(prop)) {
                    return forName(prop);
                }
            } catch (IOException e) {
                // throw to alert the caller the file is corrupt
                throw new RuntimeException(e);
            }
        }
        // attempt to find through the jar META-INF/services..
        final String serviceId = "META-INF/services/" + PACKAGE;
        final String clazz = IOUtil.getResourceAsString(Log.class, serviceId);
        if (StringUtil.isNotBlank(clazz)) {
            return forName(clazz.trim());
        }
        // return the default..
        return DEFAULT_SPI;
    }

    /**
     * Simple helper function to prevent duplicate code.
     */
    private static Class<?> forName(final String clazz) {
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
            if (cacheSPI == null) {
                cacheSPI = findSpiClass();
            }
        }
        return cacheSPI;
    }

    /**
     * For <strong>test</strong> only.
     */
    static void setSpiClass(final Class<?> clazz) {
        synchronized (Log.class) {
            cacheSPI = clazz;
        }
    }
}
