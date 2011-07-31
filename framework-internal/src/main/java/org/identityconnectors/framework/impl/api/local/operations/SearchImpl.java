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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.List;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.SearchOp;


public class SearchImpl extends ConnectorAPIOperationRunner implements SearchApiOp {

    /**
     * Initializes the operation works.
     */
    public SearchImpl(final ConnectorOperationalContext context,
            final Connector connector) {
        super(context,connector);
    }

    /**
     * Call the SPI search routines to return the results to the
     * {@link ResultsHandler}.
     * 
     * @see SearchApiOp#search(Filter, long, long, SearchApiOp.ResultsHandler)
     */
    public void search(final ObjectClass oclass, 
            final Filter originalFilter, 
            final ResultsHandler handler,
            OperationOptions options) {
        Assertions.nullCheck(oclass, "oclass");
        Assertions.nullCheck(handler, "handler");
        //cast null as empty
        if ( options == null ) {
            options = new OperationOptionsBuilder().build();
        }
        SearchOp<?> search =
            ((SearchOp<?>)getConnector());
        final ObjectNormalizerFacade normalizer =
            getNormalizer(oclass);
        //chain a normalizing handler (must come before
        //filter handler)
        NormalizingResultsHandler normalizingHandler =
            new NormalizingResultsHandler(handler,normalizer);
        final Filter normalizedFilter =
            normalizer.normalizeFilter(originalFilter);
        // chain a filter handler..
        ResultsHandler filteredHandler = 
            new FilteredResultsHandler(normalizingHandler, normalizedFilter); 
        // chain an attributes to get handler..
        ResultsHandler attrsToGetHandler = 
            getAttributesToGetResutlsHandler(filteredHandler, options);
        rawSearch(search,oclass,normalizedFilter,attrsToGetHandler,options);
    }
    
    /**
     * Public because it is used by TestHelpersImpl. Raw,
     * SPI-level search. 
     * @param search The underlying implementation of search
     *               (generally the connector itself)
     * @param oclass The object class
     * @param filter The filter
     * @param handler The handler
     * @param options The options
     */
    public static void rawSearch(SearchOp<?> search,
            final ObjectClass oclass, 
            final Filter filter, 
            ResultsHandler handler,
            OperationOptions options) {
        FilterTranslator<?> translator =
            search.createFilterTranslator(oclass, options);
        List<?> queries =
            (List<?>)translator.translate(filter);
        if ( queries.size() == 0) {
            search.executeQuery(oclass, 
                    null, handler, options);
        }
        else {
            //eliminate dups if more than one
            boolean eliminateDups =
                queries.size() > 1;
            if (eliminateDups) {
                handler = new DuplicateFilteringResultsHandler(handler);
            }
            for( Object query : queries ) {
                @SuppressWarnings("unchecked")
                SearchOp<Object> hack = (SearchOp<Object>)search;
                hack.executeQuery(oclass, 
                        query, handler, options);
                //don't run any more queries if the consumer
                //has stopped
                if (handler instanceof DuplicateFilteringResultsHandler) {
                    DuplicateFilteringResultsHandler h =
                        (DuplicateFilteringResultsHandler)handler;
                    if (!h.isStillHandling()) {
                        break;
                    }
                }
            }
        }             
    }
    
    private ResultsHandler getAttributesToGetResutlsHandler(
            ResultsHandler handler, OperationOptions options) {
        ResultsHandler ret = handler;
        String[] attrsToGet = options.getAttributesToGet();
        if (attrsToGet != null && attrsToGet.length > 0) {
            ret = new AttributesToGetSearchResultsHandler(handler, attrsToGet);
        }
        return ret;
    }

    /**
     * Simple results handler that can reduce attributes to only the set of
     * attribute to get..
     * 
     */
    public static class AttributesToGetSearchResultsHandler extends
            AttributesToGetResultsHandler implements ResultsHandler {

        private final ResultsHandler _handler;

        public AttributesToGetSearchResultsHandler(ResultsHandler handler,
                String[] attrsToGet) {
            super(attrsToGet);
            Assertions.nullCheck(handler, "handler");
            _handler = handler;
        }

        /**
         * Handle the object w/ reduced attributes.
         */
        public boolean handle(ConnectorObject obj) {
            obj = reduceToAttrsToGet(obj);
            return _handler.handle(obj);
        }
    }
}
