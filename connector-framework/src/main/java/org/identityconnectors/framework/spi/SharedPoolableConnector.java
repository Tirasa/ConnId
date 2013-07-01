/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
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
 * An SharedPoolableConnector provides ThreadSafe access to shared resources
 * between different connector instances within the same
 * {@link org.identityconnectors.framework.impl.api.local.ObjectPool}.
 * <p/>
 * A connector instance can create a custom connection pool and share it with
 * the other instances withing the same pool.
 *
 * @author Laszlo Hordos
 * @since 1.2
 */
public interface SharedPoolableConnector extends PoolableConnector {

}
