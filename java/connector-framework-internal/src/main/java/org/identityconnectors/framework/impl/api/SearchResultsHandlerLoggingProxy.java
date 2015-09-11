/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Evolveum. All rights reserved.
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
 * Portions Copyrighted 2015 ConnId
 */
package org.identityconnectors.framework.impl.api;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.spi.SearchResultsHandler;

public class SearchResultsHandlerLoggingProxy implements SearchResultsHandler {

    private static final Log LOG = Log.getLog(SearchResultsHandlerLoggingProxy.class);

    private final ResultsHandler origHandler;

    public SearchResultsHandlerLoggingProxy(final ResultsHandler origHandler) {
        this.origHandler = origHandler;
    }

    public ResultsHandler getOrigHandler() {
        return origHandler;
    }

    @Override
    public void handleResult(final SearchResult result) {
        if (origHandler instanceof SearchResultsHandler) {
            LOG.log(SearchResultsHandler.class, "handleResult", LoggingProxy.LOG_LEVEL, "Enter: " + result, null);
            try {
                SearchResultsHandler.class.cast(origHandler).handleResult(result);
                LOG.log(SearchResultsHandler.class, "handleResult", LoggingProxy.LOG_LEVEL, "Return: ", null);
            } catch (RuntimeException e) {
                LOG.log(SearchResultsHandler.class, "handleResult", LoggingProxy.LOG_LEVEL, "Exception: ", e);
                throw e;
            }
        }
    }

    @Override
    public boolean handle(final ConnectorObject connectorObject) {
        LOG.log(ResultsHandler.class, "handle", LoggingProxy.LOG_LEVEL, "Enter: " + connectorObject, null);
        try {
            boolean ret = origHandler.handle(connectorObject);
            LOG.log(ResultsHandler.class, "handle", LoggingProxy.LOG_LEVEL, "Return: " + ret, null);
            return ret;
        } catch (RuntimeException e) {
            LOG.log(ResultsHandler.class, "handle", LoggingProxy.LOG_LEVEL, "Exception: ", e);
            throw e;
        }
    }

}
