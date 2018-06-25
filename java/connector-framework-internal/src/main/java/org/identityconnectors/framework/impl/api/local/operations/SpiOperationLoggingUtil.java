/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014-2018 Evolveum. All rights reserved.
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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.logging.Log;

public class SpiOperationLoggingUtil {

    public static final Log.Level LOG_LEVEL = Log.Level.OK;

    public static void logOpEntry(
            Log opLog,
            ConnectorOperationalContext opContext,
            Class<?> opClass,
            String methodName,
            Object... parameters) {

        if (!opLog.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        appendInstanceName(sb, opContext);

        sb.append("Enter: ").append(methodName).append("(");
        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i]);
            if (i < parameters.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        opLog.log(opClass, methodName, SpiOperationLoggingUtil.LOG_LEVEL, sb.toString(), null);
    }

    public static void logOpExit(
            Log opLog,
            ConnectorOperationalContext opContext,
            Class<?> opClass,
            String methodName,
            Object returnValue) {

        if (!opLog.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        appendInstanceName(sb, opContext);
        sb.append("Return: ").append(returnValue);
        opLog.log(opClass, methodName, SpiOperationLoggingUtil.LOG_LEVEL, sb.toString(), null);
    }

    public static void logOpExit(
            Log opLog,
            ConnectorOperationalContext opContext,
            Class<?> opClass,
            String methodName) {

        if (!opLog.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        appendInstanceName(sb, opContext);
        sb.append("Return");
        opLog.log(opClass, methodName, SpiOperationLoggingUtil.LOG_LEVEL, sb.toString(), null);
    }

    public static void logOpException(
            Log opLog,
            ConnectorOperationalContext opContext,
            Class<?> opClass,
            String methodName,
            RuntimeException e) {

        if (!opLog.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        appendInstanceName(sb, opContext);
        sb.append("Exception: ");
        opLog.log(opClass, methodName, SpiOperationLoggingUtil.LOG_LEVEL, sb.toString(), e);
    }

    private static void appendInstanceName(StringBuilder sb, ConnectorOperationalContext opContext) {
        if (opContext == null) {
            return;
        }
        if (opContext.getInstanceName() != null) {
            sb.append("instance='").append(opContext.getInstanceName()).append("' ");
        }
    }
}
