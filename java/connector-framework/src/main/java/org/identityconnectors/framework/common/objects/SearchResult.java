/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS. All rights reserved.
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
 * Portions Copyrighted 2014 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import java.io.Serializable;

/**
 * The final result of a query request returned after all connector objects
 * matching the request have been returned. In addition to indicating that no
 * more objects are to be returned by the search, the search result will contain
 * page results state information if result paging has been enabled for the
 * search.
 *
 * @since 1.4
 */
public final class SearchResult implements Serializable {

    private static final long serialVersionUID = 629759587906070073L;

    private final String pagedResultsCookie;

    private final int remainingPagedResults;

    private final boolean allResultsReturned;

    /**
     * Creates a new search result with a {@code null} paged results cookie and no estimate of the total number of
     * remaining results.
     */
    public SearchResult() {
        this(null, -1, true);
    }

    /**
     * Creates a new search result with the provided paged results cookie and estimate of the total number of remaining
     * results.
     *
     * @param pagedResultsCookie The opaque cookie which should be used with the next paged results search request, or
     * {@code null} if paged results were not requested, or if there are not more pages to be returned.
     * @param remainingPagedResults An estimate of the total number of remaining results to be returned in subsequent
     * paged results search requests, or {@code -1} if paged results were not requested, or if the total number of
     * remaining results is unknown.
     */
    public SearchResult(final String pagedResultsCookie, final int remainingPagedResults) {
        this(pagedResultsCookie, remainingPagedResults, true);
    }

    /**
     * Creates a new search result with the provided paged results cookie and estimate of the total number of remaining
     * results.
     *
     * @param pagedResultsCookie The opaque cookie which should be used with the next paged results search request, or
     * {@code null} if paged results were not requested, or if there are not more pages to be returned.
     * @param remainingPagedResults An estimate of the total number of remaining results to be returned in subsequent
     * paged results search requests, or {@code -1} if paged results were not requested, or if the total number of
     * remaining results is unknown.
     * @param allResultsReturned Set to true if the search returned all the results that match the query. Set to false
     * if the returned result is not complete, e.g. if the server returned only part of the results due to server
     * limits, errors, etc.
     */
    public SearchResult(
            final String pagedResultsCookie, final int remainingPagedResults, final boolean allResultsReturned) {

        this.pagedResultsCookie = pagedResultsCookie;
        this.remainingPagedResults = remainingPagedResults;
        this.allResultsReturned = allResultsReturned;
    }

    /**
     * Returns the opaque cookie which should be used with the next paged results search request.
     *
     * @return The opaque cookie which should be used with the next paged results search request, or {@code null} if
     * paged results were not requested, or if there are not more pages to be returned.
     */
    public String getPagedResultsCookie() {
        return pagedResultsCookie;
    }

    /**
     * Returns an estimate of the total number of remaining results to be returned in subsequent paged results search
     * requests.
     *
     * @return An estimate of the total number of remaining results to be returned in subsequent paged results search
     * requests, or {@code -1} if paged results were not requested, or if the total number of remaining results is
     * unknown.
     */
    public int getRemainingPagedResults() {
        return remainingPagedResults;
    }

    /**
     * Returns a flag indicating whether all the results that match a search query were returned.
     *
     * @return Returns true if the search returned all the results that match the query. Returns false if the returned
     * result is not complete, e.g. if the server returned only part of the results due to server limits, errors, etc.
     */
    public boolean isAllResultsReturned() {
        return allResultsReturned;
    }

}
