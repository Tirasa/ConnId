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
 * Portions Copyrighted 2010-2014 ForgeRock AS.
 * Portions Copyrighted 2014 Evolveum
 * Portions Copyrighted 2015 ConnId
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.List;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.ResultsHandlerConfiguration;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.impl.api.SearchResultsHandlerLoggingProxy;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.framework.spi.operations.SearchOp;

public class SearchImpl extends ConnectorAPIOperationRunner implements SearchApiOp {

    private static final Log LOG = Log.getLog(SearchImpl.class);

    /**
     * Special logger with SPI operation log name. Used for logging operation entry/exit
     */
    private static final Log OP_LOG = Log.getLog(SearchOp.class);

    /**
     * Initializes the operation works.
     */
    public SearchImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    /**
     * Call the SPI search routines to return the results to the {@link ResultsHandler}.
     *
     * @see SearchOp#executeQuery
     */
    @Override
    public SearchResult search(final ObjectClass objectClass, final Filter originalFilter,
            ResultsHandler handler, OperationOptions options) {

        Assertions.nullCheck(objectClass, "objectClass");
        if (ObjectClass.ALL.equals(objectClass)) {
            throw new UnsupportedOperationException("Operation is not allowed on __ALL__ object class");
        }
        Assertions.nullCheck(handler, "handler");
        // cast null as empty
        if (options == null) {
            options = new OperationOptionsBuilder().build();
        }

        ResultsHandlerConfiguration hdlCfg =
                null != getOperationalContext() ? getOperationalContext()
                        .getResultsHandlerConfiguration() : new ResultsHandlerConfiguration();

        // actualFilter is used for chaining filters - it points to the filter where new filters should be chained
        Filter actualFilter = originalFilter;

        if (hdlCfg.isEnableFilteredResultsHandler() && hdlCfg.isEnableCaseInsensitiveFilter() && actualFilter != null) {
            LOG.ok("Creating case insensitive filter");
            ObjectNormalizerFacade normalizer = new ObjectNormalizerFacade(objectClass, new CaseNormalizer());
            actualFilter = new NormalizingFilter(actualFilter, normalizer);
        }

        if (hdlCfg.isEnableFilteredResultsHandler() && !hdlCfg.isFilteredResultsHandlerInValidationMode()
                && options.getPageSize() != null && options.getPageSize() > 0) {

            throw new IllegalArgumentException("Paged search is requested, but the filtered results handler is enabled "
                    + "in effective (i.e. non-validation) mode. This is not supported.");
        }

        if (hdlCfg.isEnableNormalizingResultsHandler()) {
            ObjectNormalizerFacade normalizer = getNormalizer(objectClass);
            // chain a normalizing handler (must come before
            // filter handler)
            NormalizingResultsHandler normalizingHandler = new NormalizingResultsHandler(handler, normalizer);
            // chain a filter handler..
            if (hdlCfg.isEnableFilteredResultsHandler()) {
                // chain a filter handler..
                Filter normalizedFilter = normalizer.normalizeFilter(actualFilter);
                handler = new FilteredResultsHandler(
                        normalizingHandler, normalizedFilter, hdlCfg.isFilteredResultsHandlerInValidationMode());
                actualFilter = normalizedFilter;
            } else {
                handler = normalizingHandler;
            }
        } else if (hdlCfg.isEnableFilteredResultsHandler()) {
            // chain a filter handler..
            handler = new FilteredResultsHandler(
                    handler, actualFilter, hdlCfg.isFilteredResultsHandlerInValidationMode());
        }
        // chain an attributes to get handler..
        String[] attrsToGet = options.getAttributesToGet();
        if (attrsToGet != null && attrsToGet.length > 0 && hdlCfg.isEnableAttributesToGetSearchResultsHandler()) {
            handler = getAttributesToGetResultsHandler(handler, options);
        }

        SearchOp<?> search = ((SearchOp<?>) getConnector());
        final SearchResult[] result = new SearchResult[] { null };
        final ResultsHandler handlerChain = handler;
        rawSearch(search, objectClass, actualFilter, new SearchResultsHandler() {

            @Override
            public void handleResult(final SearchResult searchResult) {
                if (handlerChain instanceof SearchResultsHandler) {
                    SearchResultsHandler.class.cast(handlerChain).handleResult(searchResult);
                }
                result[0] = searchResult;
            }

            @Override
            public boolean handle(final ConnectorObject connectorObject) {
                return handlerChain.handle(connectorObject);
            }
        }, options);

        return result[0];
    }

    /**
     * Public because it is used by TestHelpersImpl. Raw, SPI-level search.
     *
     * @param search The underlying implementation of search (generally the connector itself)
     * @param objectClass The object class
     * @param filter The filter
     * @param handler The handler
     * @param options The options
     */
    public static void rawSearch(final SearchOp<?> search, final ObjectClass objectClass, final Filter filter,
            SearchResultsHandler handler, final OperationOptions options) {

        FilterTranslator<?> translator = search.createFilterTranslator(objectClass, options);
        List<?> queries = translator.translate(filter);

        if (isLoggable()) {
            handler = new SearchResultsHandlerLoggingProxy(handler);
        }

        if (queries.isEmpty()) {
            logOpEntry(objectClass, null, handler, options);
            try {
                search.executeQuery(objectClass, null, handler, options);
                logOpExit();
            } catch (RuntimeException e) {
                SpiOperationLoggingUtil.logOpException(OP_LOG, SearchOp.class, "executeQuery", e);
                throw e;
            }
        } else {
            // eliminate dups if more than one
            boolean eliminateDups = queries.size() > 1;
            if (eliminateDups) {
                handler = new DuplicateFilteringResultsHandler(handler);
                if (options.getPageSize() != null && options.getPageSize() > 0) {
                    throw new IllegalArgumentException(
                            "Paged search is requested, but the filter was translated into more than one query."
                            + "This is not supported. Queries = " + queries);
                }
            }
            for (Object query : queries) {
                @SuppressWarnings("unchecked")
                SearchOp<Object> hack = (SearchOp<Object>) search;
                logOpEntry(objectClass, query, handler, options);
                try {
                    hack.executeQuery(objectClass, query, handler, options);
                    logOpExit();
                } catch (RuntimeException e) {
                    SpiOperationLoggingUtil.logOpException(OP_LOG, SearchOp.class, "executeQuery", e);
                    throw e;
                }
                // don't run any more queries if the consumer has stopped
                if (handler instanceof DuplicateFilteringResultsHandler) {
                    DuplicateFilteringResultsHandler h = (DuplicateFilteringResultsHandler) handler;
                    if (!h.isStillHandling()) {
                        break;
                    }
                }
            }
        }
    }

    private static boolean isLoggable() {
        return OP_LOG.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL);
    }

    private static void logOpEntry(ObjectClass objectClass, Object object, SearchResultsHandler handler,
            OperationOptions options) {
        if (!isLoggable()) {
            return;
        }
        StringBuilder bld = new StringBuilder();
        bld.append("Enter: executeQuery(");
        bld.append(objectClass).append(", ");
        bld.append(object).append(", ");
        if (handler instanceof SearchResultsHandlerLoggingProxy) {
            bld.append(((SearchResultsHandlerLoggingProxy) handler).getOrigHandler()).append(", ");
        } else {
            bld.append(handler).append(", ");
        }
        bld.append(options).append(")");
        final String msg = bld.toString();
        OP_LOG.log(SearchOp.class, "executeQuery", SpiOperationLoggingUtil.LOG_LEVEL, msg, null);
    }

    private static void logOpExit() {
        if (!isLoggable()) {
            return;
        }
        OP_LOG.log(SearchOp.class, "executeQuery", SpiOperationLoggingUtil.LOG_LEVEL, "Return", null);
    }

    private ResultsHandler getAttributesToGetResultsHandler(
            final ResultsHandler handler, final OperationOptions options) {

        ResultsHandler ret = handler;
        String[] attrsToGet = options.getAttributesToGet();
        if (attrsToGet != null && attrsToGet.length > 0) {
            ret = new AttributesToGetSearchResultsHandler(handler, attrsToGet);
        }
        return ret;
    }

    /**
     * Simple results handler that can reduce attributes to only the set of attribute to get.
     */
    public static class AttributesToGetSearchResultsHandler
            extends AttributesToGetResultsHandler implements SearchResultsHandler {

        private final ResultsHandler handler;

        public AttributesToGetSearchResultsHandler(final ResultsHandler handler, final String[] attrsToGet) {
            super(attrsToGet);
            Assertions.nullCheck(handler, "handler");
            this.handler = handler;
        }

        @Override
        public void handleResult(final SearchResult result) {
            if (handler instanceof SearchResultsHandler) {
                SearchResultsHandler.class.cast(handler).handleResult(result);
            }
        }

        /**
         * Handle the object w/ reduced attributes.
         */
        @Override
        public boolean handle(ConnectorObject obj) {
            obj = reduceToAttrsToGet(obj);
            return handler.handle(obj);
        }
    }
}
