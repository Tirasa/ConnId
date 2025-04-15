/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2024 ConnId. All rights reserved.
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

import java.util.Optional;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.ResultsHandlerConfiguration;
import org.identityconnectors.framework.api.operations.LiveSyncApiOp;
import org.identityconnectors.framework.common.objects.LiveSyncDelta;
import org.identityconnectors.framework.common.objects.LiveSyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.LiveSyncResultsHandler;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.spi.AttributeNormalizer;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.LiveSyncOp;

public class LiveSyncImpl extends ConnectorAPIOperationRunner implements LiveSyncApiOp {

    // Special logger with SPI operation log name. Used for logging operation entry/exit
    private static final Log OP_LOG = Log.getLog(LiveSyncOp.class);

    public LiveSyncImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    @Override
    public void livesync(
            final ObjectClass objectClass,
            LiveSyncResultsHandler handler,
            OperationOptions options) {

        Assertions.nullCheck(objectClass, "objectClass");
        Assertions.nullCheck(handler, "handler");
        // convert null into empty
        if (options == null) {
            options = new OperationOptionsBuilder().build();
        }

        ResultsHandlerConfiguration hdlCfg = null != getOperationalContext()
                ? getOperationalContext().getResultsHandlerConfiguration()
                : new ResultsHandlerConfiguration();

        // add a handler in the chain to remove attributes
        String[] attrsToGet = options.getAttributesToGet();
        if (attrsToGet != null && attrsToGet.length > 0 && hdlCfg.isEnableAttributesToGetSearchResultsHandler()) {
            handler = new AttributesToGetLiveSyncResultsHandler(handler, attrsToGet);
        }
        // chain a normalizing results handler
        if (getConnector() instanceof AttributeNormalizer && hdlCfg.isEnableNormalizingResultsHandler()) {
            handler = new NormalizingLiveSyncResultsHandler(handler, getNormalizer(objectClass));
        }

        SpiOperationLoggingUtil.logOpEntry(
                OP_LOG, getOperationalContext(), LiveSyncOp.class, "livesync", objectClass, options);

        try {
            ((LiveSyncOp) getConnector()).livesync(objectClass, handler, options);
        } catch (RuntimeException e) {
            SpiOperationLoggingUtil.logOpException(OP_LOG, getOperationalContext(), LiveSyncOp.class, "livesync", e);
            throw e;
        }

        SpiOperationLoggingUtil.logOpExit(OP_LOG, getOperationalContext(), LiveSyncOp.class, "livesync");
    }

    /**
     * Simple handler to reduce the attributes to only the set of attribute to get.
     */
    public static class AttributesToGetLiveSyncResultsHandler
            extends AttributesToGetResultsHandler implements LiveSyncResultsHandler {

        private final LiveSyncResultsHandler handler;

        public AttributesToGetLiveSyncResultsHandler(final LiveSyncResultsHandler handler, final String[] attrsToGet) {
            super(attrsToGet);
            this.handler = handler;
        }

        @Override
        public boolean handle(final LiveSyncDelta delta) {
            LiveSyncDeltaBuilder bld = new LiveSyncDeltaBuilder(delta);
            Optional.ofNullable(delta.getObject()).ifPresent(obj -> bld.setObject(reduceToAttrsToGet(obj)));
            return handler.handle(bld.build());
        }
    }
}
