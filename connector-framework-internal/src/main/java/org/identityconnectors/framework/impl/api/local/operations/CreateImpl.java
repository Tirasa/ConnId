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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.CreateOp;

public class CreateImpl extends ConnectorAPIOperationRunner implements
        org.identityconnectors.framework.api.operations.CreateApiOp {

    /**
     * Initializes the operation works.
     */
    public CreateImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    /**
     * Calls the create method on the Connector side.
     *
     * @see CreateOp#create(org.identityconnectors.framework.common.objects.ObjectClass,
     *      java.util.Set,
     *      org.identityconnectors.framework.common.objects.OperationOptions)
     */
    @Override
    public Uid create(final ObjectClass objectClass, final Set<Attribute> createAttributes,
            OperationOptions options) {
        Assertions.nullCheck(objectClass, "objectClass");
        Assertions.nullCheck(createAttributes, "createAttributes");
        // check to make sure there's not a uid..
        if (AttributeUtil.getUidAttribute(createAttributes) != null) {
            throw new InvalidAttributeValueException("Parameter 'createAttributes' contains a uid.");
        }
        // cast null as empty
        if (options == null) {
            options = new OperationOptionsBuilder().build();
        }
        // validate input..
        final Set<String> dups = new HashSet<String>();
        for (Attribute attr : createAttributes) {
            if (dups.contains(attr.getName())) {
                throw new InvalidAttributeValueException("Duplicated named attributes: " + attr.getName());
            }
            // add for the detection..s
            dups.add(attr.getName());
        }

        final Connector connector = getConnector();
        final ObjectNormalizerFacade normalizer = getNormalizer(objectClass);
        // create the object..
        final Set<Attribute> normalizedAttributes =
                normalizer.normalizeAttributes(createAttributes);
        final Uid ret = ((CreateOp) connector).create(objectClass, normalizedAttributes, options);
        return (Uid) normalizer.normalizeAttribute(ret);
    }
}
