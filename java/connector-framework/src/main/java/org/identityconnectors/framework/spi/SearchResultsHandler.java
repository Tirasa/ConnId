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
 */

package org.identityconnectors.framework.spi;

import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;

/**
 * A SearchResultsHandler is a completion handler for consuming the results of a
 * search request.
 * <p>
 * A search result completion handler may be specified when performing search
 * requests using a {@link org.identityconnectors.framework.api.ConnectorFacade}
 * object. The {@link #handle} method is invoked each time a matching
 * {@link org.identityconnectors.framework.common.objects.ConnectorObject}
 * resource is returned, followed by {@link #handleResult} indicating that no
 * more ConnectorObject resources will be returned.
 * <p>
 * Implementations of these methods should complete in a timely manner so as to
 * avoid keeping the invoking thread from dispatching to other completion
 * handlers.
 *
 * @author Laszlo Hordos
 * @since 1.4
 */
public interface SearchResultsHandler extends ResultsHandler {

    /**
     * Invoked when the request has completed successfully.
     *
     * @param result
     *            The query result indicating that no more resources are to be
     *            returned and, if applicable, including information which
     *            should be used for subsequent paged results query requests.
     */
    public void handleResult(SearchResult result);

}
