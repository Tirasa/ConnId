/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Evolveum, Inc. All rights reserved.
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
package org.identityconnectors.framework.impl.api.local;

import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.InstanceNameAware;

/**
 * Utility methods regarding connector lifecycle (creation, disposal).
 * Not very clean. But the connector is created at two different places
 * (ConnectorAPIOperationRunnerProxy and connector pool) without using
 * common code. Therefore this util is still better than copying the
 * code to two places or a big code refactoring.
 */
public class ConnectorLifecycleUtil {

    public static void setConnectorInstanceName(Connector connector, String instanceName) {
        if (connector instanceof InstanceNameAware) {
            ((InstanceNameAware) connector).setInstanceName(instanceName);
        }
    }
}
