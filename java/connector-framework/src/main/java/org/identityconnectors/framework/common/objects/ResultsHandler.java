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
package org.identityconnectors.framework.common.objects;

/**
 * Callback interface for operations that are returning one or more results.
 * Currently used only by
 * {@link org.identityconnectors.framework.api.operations.SearchApiOp Search},
 * but may be used by other operations in the future.
 */
public interface ResultsHandler {

    /**
     * Invoked each time a matching {@link ConnectorObject} is returned from a
     * query request.
     *
     * @param connectorObject
     *            The matching ConnectorObject.
     * @return {@code true} if this handler should continue to be notified of
     *         any remaining matching ConnectorObjects, or {@code false} if the
     *         remaining ConnectorObjects should be skipped for some reason
     *         (e.g. a client side size limit has been reached).
     *
     * @throws RuntimeException
     *             the implementor should throw a {@link RuntimeException} that
     *             wraps any native exception (or that describes any other
     *             problem during execution) that is serious enough to stop the
     *             iteration.
     */
    boolean handle(final ConnectorObject connectorObject);
}
