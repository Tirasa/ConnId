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
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
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
    public CreateImpl(final ConnectorOperationalContext context,
            final Connector connector) {
        super(context,connector);
    }

    /**
     * Calls the create method on the Connector side.
     * 
     * @see CreateApiOp#create(Set)
     */
    public Uid create(final ObjectClass oclass, 
            final Set<Attribute> attributes,
            OperationOptions options) {
        Assertions.nullCheck(oclass, "oclass");
        Assertions.nullCheck(oclass, "attributes");
        //cast null as empty
        if ( options == null ) {
            options = new OperationOptionsBuilder().build();
        }
        // validate input..
        Set<String> dups = new HashSet<String>();
        for (Attribute attr : attributes) {
            if (dups.contains(attr.getName())) {
                throw new IllegalArgumentException("Duplicated named attributes: " + attr.getName());
            }
            // add for the detection..s
            dups.add(attr.getName());
        }
        
        Connector connector = getConnector();
        final ObjectNormalizerFacade normalizer =
            getNormalizer(oclass);
        // create the object..
        final Set<Attribute> normalizedAttributes =
            normalizer.normalizeAttributes(attributes);
        Uid ret = ((CreateOp) connector).create(oclass,normalizedAttributes,options);
        return (Uid)normalizer.normalizeAttribute(ret);
    }
}
