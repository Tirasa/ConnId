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

/**
 * Logging 'Service Provider Interface'.
 */
public interface LogSpi {
    /**
     * Log given the class, level, message, and exception.
     * 
     * @param clazz
     *            Class that logging.
     * @param level
     *            the level at which to log.
     * 
     * @param message
     *            optional message to send to the log.
     * @param ex
     *            optional exception at logging point.
     */
    public void log(Class<?> clazz, String method, Log.Level level, String message, Throwable ex);

    /**
     * Determines if the it should be logged based on Class and Level.
     */
    public boolean isLoggable(Class<?> clazz, Log.Level level);
}
