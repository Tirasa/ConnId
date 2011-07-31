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
package org.identityconnectors.common.logging.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.identityconnectors.common.logging.LogSpi;
import org.identityconnectors.common.logging.Log.Level;

/**
 * Provider to integrate with the JDK logger.
 * @author Will Droste
 * @version $Revision $
 * @since 1.0
 */
public class JDKLogger implements LogSpi {

    private volatile ConcurrentMap<String, Logger> map = new ConcurrentHashMap<String, Logger>(1);

    /**
     * Uses the JDK logger to log the message.
     * @see LogSpi#log(Class, Level, String, Throwable)
     */
    public void log(Class<?> clazz, String methodName, Level level, String message, Throwable ex) {
        // uses different call if the exception is not null..
        String clazzName = clazz.getName();
        java.util.logging.Level jdkLevel = getJDKLevel(level);
        Logger logger = getJDKLogger(clazzName);
        if (ex == null) {
            logger.logp(jdkLevel, clazzName, methodName, message);
        } else {
            logger.logp(jdkLevel, clazzName, methodName, message, ex);
        }
    }

    /**
     * Use the internal JDK logger to determine if the level is worthy of logging.
     */
    public boolean isLoggable(Class<?> clazz, Level level) {
        return getJDKLogger(clazz.getName()).isLoggable(getJDKLevel(level));
    }

    /**
     * Translate the logging levels.
     */
    java.util.logging.Level getJDKLevel(Level level) {
        java.util.logging.Level ret = java.util.logging.Level.SEVERE;
        if (Level.OK.equals(level)) {
            ret = java.util.logging.Level.FINE;
        } else if (Level.INFO.equals(level)) {
            ret = java.util.logging.Level.INFO;
        } else if (Level.WARN.equals(level)) {
            ret = java.util.logging.Level.WARNING;
        }
        return ret;
    }

    Logger getJDKLogger(String key) {
        Logger aLogger = map.get(key);
        if (aLogger == null) {
            aLogger = Logger.getLogger(key);
            Logger old = map.putIfAbsent(key, aLogger);
            aLogger = old != null ? old : aLogger;
        }
        return aLogger;
    }

    // Method for tests
    ConcurrentMap<String, Logger> getMap() {
        return map;
    }

}
