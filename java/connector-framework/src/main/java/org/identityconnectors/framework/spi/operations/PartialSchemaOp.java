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
package org.identityconnectors.framework.spi.operations;

import org.identityconnectors.framework.common.objects.LightweightObjectClassInfo;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.Connector;

/**
 * Implement this interface to allow the Connector to return an array of connector
 * Object Classes (representing the types of object on the target resource)
 * which the connector is capable of managing.
 * Based on this the client is capable of choosing for which type of
 * objects should the connector provide more information (which operations
 * and which options the Connector supports for each type of object).
 */
public interface PartialSchemaOp extends SPIOperation {

    /**
     * Describes a subset of the types of objects this {@link Connector} supports.
     * This method is considered an operation since determining supported
     * objects may require configuration information and allows this
     * determination to be dynamic.
     * <p>
     * The special {@link org.identityconnectors.framework.common.objects.Uid}
     * attribute should never appear in the schema, as it is not a true
     * attribute of an object, rather a reference to it. If your resource
     * object-class has a writable unique id attribute that is different than
     * its {@link org.identityconnectors.framework.common.objects.Name}, then
     * your schema should contain a resource-specific attribute that represents
     * this unique id. For example, a Unix account object might contain
     * <I>unix_uid</I>.
     *
     * @return a subset of the basic schema supported by this {@link Connector}.
     */
    Schema getPartialSchema(LightweightObjectClassInfo... ObjectClassInfo);

    /**
     * Provides the client with an array of {@link LightweightObjectClassInfo} which this {@link Connector} supports.
     * The list can be used in the {@link org.identityconnectors.framework.api.operations.PartialSchemaApiOp} method
     * to request a schema of a subset of the objects which this {@link Connector} is capable of managing.
     *
     * @return an array of the partial Object Class information supported by this {@link Connector}.
     */
    public LightweightObjectClassInfo[] getObjectClassInformation();
}
