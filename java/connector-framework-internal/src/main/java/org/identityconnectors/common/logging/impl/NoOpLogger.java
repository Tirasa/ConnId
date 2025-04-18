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
 */
package org.identityconnectors.common.logging.impl;

import org.identityconnectors.common.logging.Log.Level;
import org.identityconnectors.common.logging.LogSpi;

/**
 * No operation logger. This {@link LogSpi} does nothing it is synonymous with
 * /dev/null.
 *
 * @author Will Droste
 * @version $Revision: 1.4 $
 * @since 1.0
 */
public class NoOpLogger implements LogSpi {
    /**
     * Logs nothing. Its a black hole command.
     */
    @Override
    public void log(Class<?> clazz, String methodName, Level level,
            String message, Throwable ex) {
    }

    public void log(Class<?> clazz, StackTraceElement method, Level level,
                    String message, Throwable ex) {
    }

    /**
     * Always returns <code>false</code> because there nothing to do.
     */
    @Override
    public boolean isLoggable(Class<?> clazz, Level level) {
        return false;
    }

    @Override
    public boolean needToInferCaller(Class<?> clazz, Level level) {
        return false;
    }
}
