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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2014-2018 Evolveum
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.ResultsHandlerConfiguration;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.spi.AttributeNormalizer;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.SyncTokenResultsHandler;
import org.identityconnectors.framework.spi.operations.SyncOp;

public class SyncImpl extends ConnectorAPIOperationRunner implements SyncApiOp {

    // Special logger with SPI operation log name. Used for logging operation entry/exit
    private static final Log OP_LOG = Log.getLog(SyncOp.class);

    private static final Log HANDLER_LOG = Log.getLog(SyncTokenResultsHandler.class);

    public SyncImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    @Override
    public SyncToken sync(
            final ObjectClass objectClass,
            final SyncToken token,
            SyncResultsHandler handler,
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
            handler = new AttributesToGetSyncResultsHandler(handler, attrsToGet);
        }
        // chain a normalizing results handler
        if (getConnector() instanceof AttributeNormalizer && hdlCfg.isEnableNormalizingResultsHandler()) {
            handler = new NormalizingSyncResultsHandler(handler, getNormalizer(objectClass));
        }

        final SyncResultsHandler handlerChain = handler;
        final AtomicReference<SyncToken> result = new AtomicReference<>(null);

        SyncTokenResultsHandler syncHandler = new SyncTokenResultsHandler() {

            @Override
            public void handleResult(SyncToken token) {
                if (HANDLER_LOG.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
                    HANDLER_LOG.log(SyncTokenResultsHandler.class, "handleResult",
                            SpiOperationLoggingUtil.LOG_LEVEL, "Enter: handleResult(" + token + ")", null);
                }
                try {
                    result.compareAndSet(null, token);
                } catch (RuntimeException e) {
                    SpiOperationLoggingUtil.logOpException(HANDLER_LOG, getOperationalContext(),
                            SyncTokenResultsHandler.class, "handleResult", e);
                    throw e;
                }
                if (HANDLER_LOG.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
                    HANDLER_LOG.log(SyncTokenResultsHandler.class, "handleResult",
                            SpiOperationLoggingUtil.LOG_LEVEL, "Return", null);
                }
            }

            @Override
            public boolean handle(final SyncDelta delta) {
                if (HANDLER_LOG.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
                    HANDLER_LOG.log(SyncTokenResultsHandler.class, "handle",
                            SpiOperationLoggingUtil.LOG_LEVEL, "Enter: handle(" + delta + ")", null);
                }
                boolean ret;
                try {
                    ret = handlerChain.handle(delta);
                } catch (RuntimeException e) {
                    SpiOperationLoggingUtil.logOpException(HANDLER_LOG, getOperationalContext(),
                            SyncTokenResultsHandler.class, "handle", e);
                    throw e;
                }
                if (HANDLER_LOG.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
                    HANDLER_LOG.log(SyncTokenResultsHandler.class, "handle",
                            SpiOperationLoggingUtil.LOG_LEVEL, "Return: " + ret, null);
                }
                return ret;
            }
        };

        SpiOperationLoggingUtil.logOpEntry(OP_LOG, getOperationalContext(), SyncOp.class, "sync",
                objectClass, token, syncHandler, options);

        try {
            ((SyncOp) getConnector()).sync(objectClass, token, syncHandler, options);
        } catch (RuntimeException e) {
            SpiOperationLoggingUtil.logOpException(OP_LOG, getOperationalContext(), SyncOp.class, "sync", e);
            throw e;
        }

        SpiOperationLoggingUtil.logOpExit(OP_LOG, getOperationalContext(), SyncOp.class, "sync");

        return result.get();
    }

    @Override
    public SyncToken getLatestSyncToken(ObjectClass objectClass) {
        return ((SyncOp) getConnector()).getLatestSyncToken(objectClass);
    }

    /**
     * Simple handler to reduce the attributes to only the set of attribute to get.
     */
    public static class AttributesToGetSyncResultsHandler
            extends AttributesToGetResultsHandler implements SyncResultsHandler {

        private final SyncResultsHandler handler;

        public AttributesToGetSyncResultsHandler(final SyncResultsHandler handler, final String[] attrsToGet) {
            super(attrsToGet);
            this.handler = handler;
        }

        @Override
        public boolean handle(final SyncDelta delta) {
            SyncDeltaBuilder bld = new SyncDeltaBuilder(delta);
            Optional.ofNullable(delta.getObject()).ifPresent(obj -> bld.setObject(reduceToAttrsToGet(obj)));
            return handler.handle(bld.build());
        }
    }
}
