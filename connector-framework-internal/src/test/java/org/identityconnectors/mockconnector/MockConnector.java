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
package org.identityconnectors.mockconnector;

import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.ReflectionUtil;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.SchemaOp;

public class MockConnector implements Connector, SchemaOp {

    /**
     * Represents a call to a connector method.
     */
    public static class Call {
        final Object[] args;
        final String methodName;

        public Call(String methodName, Object... args) {
            this.args = args;
            this.methodName = methodName;
        }

        public String getMethodName() {
            return this.methodName;
        }

        public Object[] getArguments() {
            return this.args;
        }
    }

    // need to keep track of when methods are called an their parameters..
    private static List<Call> callPattern = new ArrayList<Call>();

    private Configuration configuration;

    public void dispose() {
        addCall();
    }

    public Schema schema() {
        addCall();
        return null;
    }

    public void init(Configuration cfg) {
        configuration = cfg;
        addCall(cfg);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Clear the call pattern.
     */
    public static void reset() {
        callPattern.clear();
    }

    /**
     * Get the current call pattern.
     */
    public static List<Call> getCallPattern() {
        return CollectionUtil.newList(callPattern);
    }

    /**
     * Adds the call to the internal call pattern.
     */
    public static void addCall(Object... args) {
        String methodName = ReflectionUtil.getMethodName(2);
        callPattern.add(new Call(methodName, args));
    }
}
