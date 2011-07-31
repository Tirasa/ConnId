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
package org.identityconnectors.framework.api.operations;

import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Connector;


public interface CreateApiOp extends APIOperation {
    /**
     * Create a target object based on the specified attributes. 
     * The Connector framework always requires attribute <code>ObjectClass</code>.
     * The <code>Connector</code> itself may require additional attributes.
     * The API will confirm that the set contains the <code>ObjectClass</code> attribute 
     * and that no two attributes in the set have the same {@link Attribute#getName() name}.
     * 
     * @param attrs
     *            includes all the attributes necessary to create the target
     *            object (including the <code>ObjectClass</code> attribute).
     * @param options
     *            additional options that impact the way this operation is run.
     *            May be null.
     * @return the unique id for the object that is created. For instance in
     *         LDAP this would be the 'dn', for a database this would be the
     *         primary key, and for 'ActiveDirectory' this would be the GUID.
     * @throws IllegalArgumentException
     *             iff <code>ObjectClass</code> is missing or elements of the set
     *             produce duplicate values of {@link Attribute#getName()}.
     * @throws NullPointerException
     *             iff the parameter <code>attrs</code> is <code>null</code>.
     * @throws RuntimeException
     *             iff the {@link Connector} SPI throws a native
     *             {@link Exception}.
     */
    public Uid create(final ObjectClass oclass, final Set<Attribute> attrs, final OperationOptions options);
}
