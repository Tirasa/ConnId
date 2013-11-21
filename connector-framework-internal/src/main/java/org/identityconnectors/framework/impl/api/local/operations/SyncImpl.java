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
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.concurrent.atomic.AtomicReference;

import org.identityconnectors.common.Assertions;
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

    public SyncImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    @Override
    public SyncToken sync(final ObjectClass objectClass, final SyncToken token,
            final SyncResultsHandler handler, OperationOptions options) {
        Assertions.nullCheck(objectClass, "objectClass");
        Assertions.nullCheck(handler, "handler");
        // convert null into empty
        if (options == null) {
            options = new OperationOptionsBuilder().build();
        }
        final AtomicReference<SyncToken> result = new AtomicReference<SyncToken>(null);
        SyncTokenResultsHandler handlerChain = new SyncTokenResultsHandler() {
            @Override
            public void handleResult(SyncToken token) {
                result.compareAndSet(null, token);
            }

            @Override
            public boolean handle(final SyncDelta delta) {
                return handler.handle(delta);
            }
        };

        // add a handler in the chain to remove attributes
        String[] attrsToGet = options.getAttributesToGet();
        if (attrsToGet != null && attrsToGet.length > 0) {
            handlerChain = new AttributesToGetSyncResultsHandler(handlerChain, attrsToGet);
        }
        // chain a normalizing results handler
        if (getConnector() instanceof AttributeNormalizer) {
            handlerChain =
                    new NormalizingSyncResultsHandler(handlerChain, getNormalizer(objectClass));
        }

        ((SyncOp) getConnector()).sync(objectClass, token, handlerChain, options);
        return result.get();
    }

    @Override
    public SyncToken getLatestSyncToken(ObjectClass objectClass) {
        return ((SyncOp) getConnector()).getLatestSyncToken(objectClass);
    }

    /**
     * Simple handler to reduce the attributes to only the set of attribute to
     * get.
     */
    public static class AttributesToGetSyncResultsHandler extends AttributesToGetResultsHandler
            implements SyncTokenResultsHandler {

        private final SyncTokenResultsHandler handler;

        public AttributesToGetSyncResultsHandler(final SyncTokenResultsHandler handler,
                String[] attrsToGet) {
            super(attrsToGet);
            this.handler = handler;
        }

        @Override
        public boolean handle(final SyncDelta delta) {
            SyncDeltaBuilder bld = new SyncDeltaBuilder(delta);
            if (delta.getObject() != null) {
                bld.setObject(reduceToAttrsToGet(delta.getObject()));
            }
            return handler.handle(bld.build());
        }

        @Override
        public void handleResult(final SyncToken result) {
            handler.handleResult(result);
        }
    }
}
