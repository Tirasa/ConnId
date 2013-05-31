/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
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

package org.identityconnectors.common.event;

/**
 * NOTICE: This package is an early specification of the Events API for 1.2.x.x
 * version. Use carefully, this package may change before the final 1.2.0.0
 * release.
 * <p/>
 *
 * @author Laszlo Hordos
 * @since 1.2
 */
public interface ConnectorEventPublisher {

    /**
     * Adds an observer to the set of observers for this object, provided that
     * it is not the same as some observer already in the set. The order in
     * which notifications will be delivered to multiple observers is not
     * specified. See the class comment.
     *
     * @param handler
     *            an observer to be added.
     * @throws NullPointerException
     *             if the parameter o is null.
     */
    public void addConnectorEventHandler(ConnectorEventHandler handler);

    /**
     * Deletes an observer from the set of observers of this object. Passing
     * {@code null} to this method will have no effect.
     *
     * @param handler
     *            the observer to be deleted.
     */
    public void deleteConnectorEventHandler(ConnectorEventHandler handler);
}
