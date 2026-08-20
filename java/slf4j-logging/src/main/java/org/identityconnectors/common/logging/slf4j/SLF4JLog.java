/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2014 ForgeRock AS. All rights reserved.
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
 * Portions Copyrighted 2026 ConnId
 */
package org.identityconnectors.common.logging.slf4j;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log.Level;
import org.identityconnectors.common.logging.LogSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class SLF4JLog implements LogSpi {

    private static final String CLASS = "Class: ";

    private static final String METHOD = "Method: ";

    private static final String MESSAGE = "Message: ";

    /**
     * Uses the SLF4J logger to log the message.
     *
     * @see LogSpi#log(Class, String,
     * org.identityconnectors.common.logging.Log.Level, String, Throwable)
     */
    @Override
    public void log(final Class<?> clazz, final String methodName, final Level level,
            final String message, final Throwable ex) {
        final String clazzName = clazz.getName();
        final Logger logger = LoggerFactory.getLogger(clazzName);

        if (logger instanceof LocationAwareLogger locationAwareLogger) {
            if (StringUtil.isBlank(methodName)) {
                locationAwareLogger.log(null, clazz.getName(), getLogLevel(level), message, null, ex);
            } else {
                //StringBuilder sb = new StringBuilder(METHOD).append(methodName).append("\t").append(message);
                String sb = null == message ? "" : message + '\t' + METHOD + methodName;
                locationAwareLogger.log(null, clazz.getName(), getLogLevel(level), sb, null, ex);
            }
        } else {
            StringBuilder sb = new StringBuilder(CLASS).append(clazz).append('\t');
            if (StringUtil.isNotBlank(methodName)) {
                sb.append(methodName).append('\t');
            }
            sb.append(MESSAGE);
            if (null != message) {
                sb.append(message);
            }
            if (null != level) {
                // uses different call if the exception is not null..
                switch (level) {
                    case OK -> {
                        if (ex == null) {
                            logger.debug(sb.toString());
                        } else {
                            logger.debug(sb.toString(), ex);
                        }
                    }
                    case INFO -> {
                        if (ex == null) {
                            logger.info(sb.toString());
                        } else {
                            logger.info(sb.toString(), ex);
                        }
                    }
                    case WARN -> {
                        if (ex == null) {
                            logger.warn(sb.toString());
                        } else {
                            logger.warn(sb.toString(), ex);
                        }
                    }
                    case ERROR -> {
                        if (ex == null) {
                            logger.error(sb.toString());
                        } else {
                            logger.error(sb.toString(), ex);
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }

    @Override
    public void log(final Class<?> clazz, final StackTraceElement method, final Level level,
            final String message, final Throwable ex) {
        log(clazz, null != method ? method.getMethodName() : null, level, message, ex);
    }

    /**
     * Use the internal SLF4J logger to determine if the level is worthy of
     * logging.
     */
    @Override
    public boolean isLoggable(Class<?> clazz, Level level) {
        final Logger logger = LoggerFactory.getLogger(clazz);
        boolean ret = true;
        if (Level.OK.equals(level)) {
            ret = logger.isDebugEnabled();
        } else if (Level.INFO.equals(level)) {
            ret = logger.isInfoEnabled();
        } else if (Level.WARN.equals(level)) {
            ret = logger.isWarnEnabled();
        } else if (Level.ERROR.equals(level)) {
            ret = logger.isErrorEnabled();
        }
        return ret;
    }

    /**
     * The caller is extracted only if the Level is OK (Debug).
     */
    @Override
    public boolean needToInferCaller(Class<?> clazz, Level level) {
        return LoggerFactory.getLogger(clazz).isDebugEnabled();
    }

    private int getLogLevel(Level level) {
        int ret = LocationAwareLogger.TRACE_INT;
        if (Level.OK.equals(level)) {
            ret = LocationAwareLogger.DEBUG_INT;
        } else if (Level.INFO.equals(level)) {
            ret = LocationAwareLogger.INFO_INT;
        } else if (Level.WARN.equals(level)) {
            ret = LocationAwareLogger.WARN_INT;
        } else if (Level.ERROR.equals(level)) {
            ret = LocationAwareLogger.ERROR_INT;
        }
        return ret;
    }
}
