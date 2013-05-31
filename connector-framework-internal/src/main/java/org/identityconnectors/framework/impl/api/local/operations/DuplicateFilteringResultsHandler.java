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
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;

public final class DuplicateFilteringResultsHandler implements ResultsHandler {

    // =======================================================================
    // Fields
    // =======================================================================
    private final ResultsHandler handler;
    private final Set<String> visitedUIDs = new HashSet<String>();

    private boolean stillHandling;

    // =======================================================================
    // Constructors
    // =======================================================================
    /**
     * Filter chain for producers.
     *
     * @param handler
     *            Producer to filter.
     *
     */
    public DuplicateFilteringResultsHandler(ResultsHandler handler) {
        // there must be a producer..
        if (handler == null) {
            throw new IllegalArgumentException("Handler must not be null!");
        }
        this.handler = handler;
    }

    public boolean handle(ConnectorObject object) {
        String uid = object.getUid().getUidValue();
        if (!visitedUIDs.add(uid)) {
            // we've already seen this - don't pass it
            // throw
            return true;
        }
        stillHandling = handler.handle(object);
        return stillHandling;
    }

    public boolean isStillHandling() {
        return stillHandling;
    }
}
