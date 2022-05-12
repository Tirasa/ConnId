/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2022 Evolveum. All rights reserved.
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
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.DiscoverConfigurationApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles both version of update this include simple replace and the advance update.
 */
public class DiscoverConfigurationImpl extends ConnectorAPIOperationRunner implements DiscoverConfigurationApiOp {

    // Special logger with SPI operation log name. Used for logging operation entry/exit
    private static final Log OP_LOG = Log.getLog(DiscoverConfigurationOp.class);

    public DiscoverConfigurationImpl(ConnectorOperationalContext context, Connector connector) {
        super(context, connector);
    }

    @Override
    public void testPartialConfiguration() {
        SpiOperationLoggingUtil.logOpEntry(OP_LOG, getOperationalContext(), DiscoverConfigurationOp.class, "testPartialConfiguration");

        try {
            ((DiscoverConfigurationOp) getConnector()).testPartialConfiguration();
        } catch (RuntimeException e) {
            SpiOperationLoggingUtil.logOpException(OP_LOG, getOperationalContext(), DiscoverConfigurationOp.class, "testPartialConfiguration", e);
            throw e;
        }
        SpiOperationLoggingUtil.logOpExit(OP_LOG, getOperationalContext(), DiscoverConfigurationOp.class, "testPartialConfiguration");
    }

    @Override
    public Map<String, SuggestedValues> discoverConfiguration() {
        SpiOperationLoggingUtil.logOpEntry(OP_LOG, getOperationalContext(), DiscoverConfigurationOp.class, "discoverConfiguration");

        Map<String, SuggestedValues> ret;
        try {
            ret = ((DiscoverConfigurationOp) getConnector()).discoverConfiguration();
        } catch (RuntimeException e) {
            SpiOperationLoggingUtil.logOpException(OP_LOG, getOperationalContext(), DiscoverConfigurationOp.class, "discoverConfiguration", e);
            throw e;
        }
        SpiOperationLoggingUtil.logOpExit(OP_LOG, getOperationalContext(), DiscoverConfigurationOp.class, "discoverConfiguration", ret);
        return ret;
    }
}
