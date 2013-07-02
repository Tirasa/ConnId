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

package org.identityconnectors.framework.impl.api.local;

import java.io.Closeable;
import java.io.IOException;

/**
 * An ObjectPoolEntry is a borrowed object from pool.
 * 
 * @author Laszlo Hordos
 */
public interface ObjectPoolEntry<T> extends Closeable {

    /**
     * Gets the object which is pooled.
     * 
     * @return The pooled object instance.
     */
    public T getPooledObject();

    /**
     * Release the pooled object and puts back to the pool where is was borrowed
     * from.
     */
    @Override
    public void close() throws IOException;

}
