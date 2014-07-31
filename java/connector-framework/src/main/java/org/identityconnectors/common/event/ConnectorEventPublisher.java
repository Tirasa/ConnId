/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 ForgeRock AS. All rights reserved.
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

package org.identityconnectors.common.event;

/**
 * The ConnectorEventPublisher is used by Connector Info Manager to publish
 * events to the Framework.
 *
 * @author Laszlo Hordos
 * @since 1.4
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
