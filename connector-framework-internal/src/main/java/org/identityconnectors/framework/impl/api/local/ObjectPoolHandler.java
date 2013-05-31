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
 *
 * Portions Copyrighted  2012 ForgeRock Inc.
 */

package org.identityconnectors.framework.impl.api.local;

import org.identityconnectors.common.pooling.ObjectPoolConfiguration;

public interface ObjectPoolHandler<T> {

    /**
     * Validates, copies and updates the original {@code ObjectPoolConfiguration}.
     * <p/>
     * This class can validate and if necessary it changes the {@code original} configuration.
     *
     * @param original
     *         custom configured instance.
     * @return new instance of the {@code original} config.
     */
    public ObjectPoolConfiguration validate(ObjectPoolConfiguration original);

    /**
     * Makes the first instance of the pool.
     * <p/>
     * The pool calls this method when the pool is empty.
     *
     * @return new instance of T.
     */
    public T makeFirstObject();

    /**
     * Makes a new instance of the pooled object.
     * <p/>
     * This method is called whenever a new instance is needed.
     *
     * @return new instance of T.
     */
    public T makeObject();

    /**
     * Tests the borrowed object.
     * <p/>
     * This method is invoked on head instances to make sure they can be
     * borrowed from the pool.
     *
     * @param object
     *            the pooled object.
     */
    public void testObject(T object);

    /**
     * Disposes the object.
     * <p/>
     * This method is invoked on every instance when it is being "dropped" from
     * the pool (whether due to the response from {@link #testObject(Object)},
     * or for reasons specific to the pool implementation.)
     *
     * @param object
     *            The "dropped" object.
     */
    public void disposeObject(T object);

    /**
     * Disposes the last object from the pool.
     * <p/>
     * The pool calls this method when the pool is empty.
     *
     * @param object
     *            The "dropped" object.
     */
    public void disposeLastObject(T object);
}
