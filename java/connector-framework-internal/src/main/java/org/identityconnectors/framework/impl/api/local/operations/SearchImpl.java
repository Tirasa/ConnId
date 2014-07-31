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
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.framework.spi.operations.SearchOp;

public class SearchImpl extends ConnectorAPIOperationRunner implements SearchApiOp {

    private static final Log logger = Log.getLog(SearchImpl.class);

    /**
     * Initializes the operation works.
     */
    public SearchImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    /**
     * Call the SPI search routines to return the results to the
     * {@link ResultsHandler}.
     *
     * @see SearchOp#executeQuery(org.identityconnectors.framework.common.objects.ObjectClass,
     *      Object,
     *      org.identityconnectors.framework.common.objects.ResultsHandler,
     *      org.identityconnectors.framework.common.objects.OperationOptions)
     */
    @Override
    public SearchResult search(ObjectClass objectClass, Filter originalFilter,
            ResultsHandler handler, OperationOptions options) {
        Assertions.nullCheck(objectClass, "objectClass");
        if (ObjectClass.ALL.equals(objectClass)) {
            throw new UnsupportedOperationException(
                    "Operation is not allowed on __ALL__ object class");
        }
        Assertions.nullCheck(handler, "handler");
        // cast null as empty
        if (options == null) {
            options = new OperationOptionsBuilder().build();
        }
        SearchOp<?> search = ((SearchOp<?>) getConnector());

        ResultsHandlerConfiguration hdlCfg =
                null != getOperationalContext() ? getOperationalContext()
                        .getResultsHandlerConfiguration() : new ResultsHandlerConfiguration();

        Filter actualFilter = originalFilter;               // actualFilter is used for chaining filters - it points to the filter where new filters should be chained

        if (hdlCfg.isEnableFilteredResultsHandler() && hdlCfg.isEnableCaseInsensitiveFilter() && actualFilter != null) {
            logger.ok("Creating case insensitive filter");
            ObjectNormalizerFacade normalizer = new ObjectNormalizerFacade(objectClass, new CaseNormalizer());
            actualFilter = new NormalizingFilter(actualFilter, normalizer);
        }

        if (hdlCfg.isEnableNormalizingResultsHandler()) {
            final ObjectNormalizerFacade normalizer = getNormalizer(objectClass);
            // chain a normalizing handler (must come before
            // filter handler)
            NormalizingResultsHandler normalizingHandler =
                    new NormalizingResultsHandler(handler, normalizer);
            // chain a filter handler..
            if (hdlCfg.isEnableFilteredResultsHandler()) {
                // chain a filter handler..
                final Filter normalizedFilter = normalizer.normalizeFilter(actualFilter);
                handler = new FilteredResultsHandler(normalizingHandler, normalizedFilter);
                actualFilter = normalizedFilter;
            } else {
                handler = normalizingHandler;
            }
        } else if (hdlCfg.isEnableFilteredResultsHandler()) {
            // chain a filter handler..
            handler = new FilteredResultsHandler(handler, actualFilter);
        }
        // chain an attributes to get handler..
        String[] attrsToGet = options.getAttributesToGet();
        if (attrsToGet != null && attrsToGet.length > 0 && hdlCfg.isEnableAttributesToGetSearchResultsHandler()) {
            handler = getAttributesToGetResutlsHandler(handler, options);
        }

        final ResultsHandler handlerChain = handler;

        final AtomicReference<SearchResult> result = new AtomicReference<SearchResult>(null);
        rawSearch(search, objectClass, actualFilter, new SearchResultsHandler() {
            @Override
            public void handleResult(final SearchResult searchResult) {
                result.set(searchResult);
            }

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                return handlerChain.handle(connectorObject);
            }
        }, options);
        return result.get();
    }

    /**
     * Public because it is used by TestHelpersImpl. Raw, SPI-level search.
     *
     * @param search
     *            The underlying implementation of search (generally the
     *            connector itself)
     * @param objectClass
     *            The object class
     * @param filter
     *            The filter
     * @param handler
     *            The handler
     * @param options
     *            The options
     */
    public static void rawSearch(SearchOp<?> search, ObjectClass objectClass, Filter filter,
            SearchResultsHandler handler, OperationOptions options) {
        FilterTranslator<?> translator = search.createFilterTranslator(objectClass, options);
        List<?> queries = translator.translate(filter);

        if (queries.size() == 0) {
            search.executeQuery(objectClass, null, handler, options);
        } else {
            // eliminate dups if more than one
            boolean eliminateDups = queries.size() > 1;
            if (eliminateDups) {
                handler = new DuplicateFilteringResultsHandler(handler);
            }
            for (Object query : queries) {
                @SuppressWarnings("unchecked")
                SearchOp<Object> hack = (SearchOp<Object>) search;
                hack.executeQuery(objectClass, query, handler, options);
                // don't run any more queries if the consumer
                // has stopped
                if (handler instanceof DuplicateFilteringResultsHandler) {
                    DuplicateFilteringResultsHandler h = (DuplicateFilteringResultsHandler) handler;
                    if (!h.isStillHandling()) {
                        break;
                    }
                }
            }
        }
    }

    private ResultsHandler getAttributesToGetResutlsHandler(ResultsHandler handler,
            OperationOptions options) {
        ResultsHandler ret = handler;
        String[] attrsToGet = options.getAttributesToGet();
        if (attrsToGet != null && attrsToGet.length > 0) {
            ret = new AttributesToGetSearchResultsHandler(handler, attrsToGet);
        }
        return ret;
    }

    /**
     * Simple results handler that can reduce attributes to only the set of
     * attribute to get.
     *
     */
    public static class AttributesToGetSearchResultsHandler extends AttributesToGetResultsHandler
            implements ResultsHandler {

        private final ResultsHandler handler;

        public AttributesToGetSearchResultsHandler(final ResultsHandler handler, String[] attrsToGet) {
            super(attrsToGet);
            Assertions.nullCheck(handler, "handler");
            this.handler = handler;
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
