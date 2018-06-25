/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014-2018 Evolveum, Inc. All rights reserved.
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
 * Portions Copyrighted 2015-2018 ConnId
 */
package org.identityconnectors.framework.impl.api;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.impl.api.local.operations.ConnectorOperationalContext;
import org.identityconnectors.framework.impl.api.local.operations.SpiOperationLoggingUtil;
import org.identityconnectors.framework.spi.SearchResultsHandler;

public class SearchResultsHandlerLoggingProxy implements SearchResultsHandler {

    private final Log log;

    private final ResultsHandler origHandler;

    private final ConnectorOperationalContext operationalContext;

    public SearchResultsHandlerLoggingProxy(
            final ResultsHandler origHandler, final Log log, ConnectorOperationalContext operationalContext) {

        this.origHandler = origHandler;
        this.log = log;
        this.operationalContext = operationalContext;
    }

    public ResultsHandler getOrigHandler() {
        return origHandler;
    }

    @Override
    public void handleResult(final SearchResult result) {
        if (origHandler instanceof SearchResultsHandler) {
            SpiOperationLoggingUtil.logOpEntry(
                    log, operationalContext, SearchResultsHandler.class, "handleResult", result);
            try {
                SearchResultsHandler.class.cast(origHandler).handleResult(result);
                SpiOperationLoggingUtil.logOpExit(
                        log, operationalContext, SearchResultsHandler.class, "handleResult");
            } catch (RuntimeException e) {
                SpiOperationLoggingUtil.logOpException(
                        log, operationalContext, SearchResultsHandler.class, "handleResult", e);
                throw e;
            }
        }
    }

    @Override
    public boolean handle(final ConnectorObject connectorObject) {
        SpiOperationLoggingUtil.logOpEntry(log, operationalContext, ResultsHandler.class, "handle", connectorObject);
        try {
            boolean ret = origHandler.handle(connectorObject);
            SpiOperationLoggingUtil.logOpExit(
                    log, operationalContext, ResultsHandler.class, "handle", ret);
            return ret;
        } catch (RuntimeException e) {
            SpiOperationLoggingUtil.logOpException(
                    log, operationalContext, ResultsHandler.class, "handle", e);
            throw e;
        }
    }
}
