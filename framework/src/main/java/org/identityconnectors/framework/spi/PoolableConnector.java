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
package org.identityconnectors.framework.spi;

import org.identityconnectors.framework.spi.operations.TestOp;

/**
 * To be implemented by connectors that wish to be pooled.
 */
public interface PoolableConnector extends Connector {

    /**
     * Checks if the connector is still alive.
     * 
     * <p>A connector can spend a large amount of time in the pool before
     * being used. This method is intended to check if the connector is
     * alive and operations can be invoked on it (for instance, an implementation
     * would check that the connector's physical connection to the resource
     * has not timed out).</p>
     * 
     * <p>The major difference between this method and {@link TestOp#test()} is that
     * this method must do only the minimum that is necessary to check that the
     * connector is still alive. <code>TestOp.test()</code> does a more thorough
     * check of the environment specified in the Configuration, and can therefore
     * be much slower.</p>
     * 
     * <p>This method can be called often. Implementations should do their
     * best to keep this method fast.</p>
     * 
     * @throws RuntimeException if the connector is no longer alive.
     */
    public void checkAlive();
}
