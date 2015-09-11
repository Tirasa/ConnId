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
 * Portions Copyrighted 2015 ConnId
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterVisitor;
import org.identityconnectors.framework.spi.SearchResultsHandler;

public final class FilteredResultsHandler implements SearchResultsHandler {

    // =======================================================================
    // Fields
    // =======================================================================
    private final ResultsHandler handler;

    private final Filter filter;

    private final boolean inValidationMode;

    // =======================================================================
    // Constructors
    // =======================================================================
    /**
     * Filter chain for producers.
     *
     * @param handler Producer to filter.
     * @param filter Filter to use to accept objects.
     */
    public FilteredResultsHandler(final ResultsHandler handler, final Filter filter) {
        this(handler, filter, false);
    }

    public FilteredResultsHandler(final ResultsHandler handler, final Filter filter, final boolean inValidationMode) {
        // there must be a producer..
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null!");
        }
        this.handler = handler;
        this.inValidationMode = inValidationMode;
        // use a default pass through filter..
        this.filter = filter == null ? new PassThroughFilter() : filter;
    }

    @Override
    public void handleResult(final SearchResult result) {
        if (handler instanceof SearchResultsHandler) {
            SearchResultsHandler.class.cast(handler).handleResult(result);
        }
    }

    @Override
    public boolean handle(final ConnectorObject object) {
        if (filter.accept(object)) {
            return handler.handle(object);
        } else {
            if (inValidationMode) {
                throw new IllegalStateException("Object " + object
                        + " was returned by the connector but failed to pass "
                        + "the framework filter. This seems like wrong implementation of the filter in the connector.");
            }

            return true;
        }
    }

    /**
     * Use a pass through filter to use if a null filter is provided.
     */
    public static class PassThroughFilter implements Filter {

        @Override
        public boolean accept(final ConnectorObject obj) {
            return true;
        }

        @Override
        public <R, P> R accept(final FilterVisitor<R, P> v, final P p) {
            return v.visitExtendedFilter(p, this);
        }
    }

}
