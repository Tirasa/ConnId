/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.identityconnectors.framework.common.objects;

import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * A completion handler for consuming the result of an asynchronous operation or
 * connection attempts.
 * <p>
 * A result completion handler may be specified when performing asynchronous
 * requests using a {@link org.identityconnectors.framework.api.ConnectorFacade}
 * object. The {@link #handleResult} method is invoked when the request
 * completes successfully. The {@link #handleError} method is invoked if the
 * request fails.
 * <p>
 * Implementations of these methods should complete in a timely manner so as to
 * avoid keeping the invoking thread from dispatching to other completion
 * handlers.
 *
 * @param <V>
 *            The type of result handled by this result handler.
 * @since 2.0
 */
public interface ResultHandler<V> {

    /**
     * Invoked when the asynchronous request has failed.
     *
     * @param error
     *            The resource exception indicating why the asynchronous request
     *            has failed.
     */
    void handleError(ConnectorException error);

    /**
     * Invoked when the asynchronous request has completed successfully.
     *
     * @param result
     *            The result of the asynchronous request.
     */
    void handleResult(V result);
}
