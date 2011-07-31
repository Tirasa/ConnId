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
package org.identityconnectors.framework.spi.operations;

import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Connector;


/**
 * The {@link Connector} developer is responsible for taking the attributes
 * given (which always includes the {@link ObjectClass}) and create an object
 * and its {@link Uid}. The {@link Connector} developer must return the
 * {@link Uid} so that the caller can refer to the created object.
 * <p>
 * The {@link Connector} developer should make a best effort to create the
 * object otherwise throw an informative {@link RuntimeException} telling the
 * caller why the operation could not be completed. It reasonable to use
 * defaults for required {@link Attribute}s as long as they are documented.
 * 
 * @author Will Droste
 * @version $Revision $
 * @since 1.0
 */
public interface CreateOp extends SPIOperation {
    /**
     * The {@link Connector} developer is responsible for taking the attributes
     * given (which always includes the {@link ObjectClass}) and create an
     * object and its {@link Uid}. The {@link Connector} developer must return
     * the {@link Uid} so that the caller can refer to the created object.
     * <p>
     * *Note: There will never be a {@link Uid} passed in with the attribute set for this method.
     * If the resource supports some sort of mutable {@link Uid}, you should create your
     * own resource-specific attribute for it, such as <I>unix_uid</I>.
     * 
     * @param attrs
     *            includes all the attributes necessary to create the resource
     *            object including the {@link ObjectClass} attribute and
     *            {@link Name} attribute.
     * @param options
     *            additional options that impact the way this operation is run.
     *            If the caller passes null, the framework will convert this into
     *            an empty set of options, so SPI need not worry
     *            about this ever being null.
     * @return the unique id for the object that is created. For instance in
     *         LDAP this would be the 'dn', for a database this would be the
     *         primary key, and for 'ActiveDirectory' this would be the GUID.
     */
    Uid create(final ObjectClass oclass, final Set<Attribute> attrs, final OperationOptions options);
}
