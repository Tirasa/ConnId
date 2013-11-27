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

/**
 * A Stateful Configuration interface extends the default {@link Configuration}
 * and makes the framework keep the same instance.
 * <p/>
 * The default Configuration object instance is constructed every single time
 * before the {@link Connector#init(Configuration)} is called. If the
 * configuration class implements this interface then the Framework keeps one
 * instance of Configuration and the {@link Connector#init(Configuration)} is
 * called with the same instance. This requires extra caution because the
 * framework only guaranties to create one instance and set the properties
 * before it calls the {@link Connector#init(Configuration)} on different
 * connector instances in multiple different threads at the same time. The
 * Connector developer must quarantine that the necessary resource
 * initialisation are thread-safe.
 *
 * <p/>
 * If the connector implements the {@link PoolableConnector} then this
 * configuration is kept in the
 * {@link org.identityconnectors.framework.impl.api.local.ConnectorPoolManager}
 * and when the
 * {@link org.identityconnectors.framework.impl.api.local.ConnectorPoolManager#dispose()}
 * calls the {@link #release()} method. If the connector implements only the
 * {@link Connector} then this configuration is kept in the
 * {@link org.identityconnectors.framework.api.ConnectorFacade} and the
 * application must take care of releasing.
 *
 */
public interface StatefulConfiguration extends Configuration {

    /**
     * Release any allocated resources.
     */
    public void release();

}
