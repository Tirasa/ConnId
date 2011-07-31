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

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.DeleteOp;

public class DeleteImpl extends ConnectorAPIOperationRunner implements
        org.identityconnectors.framework.api.operations.DeleteApiOp {

    /**
     * Initializes the operation works.
     */
    public DeleteImpl(final ConnectorOperationalContext context,
            final Connector connector) {
        super(context,connector);
    }
    /**
     * Calls the delete method on the Connector side.
     * 
     * @see org.identityconnectors.framework.api.operations.CreateApiOp#create(java.util.Set)
     */
    public void delete(final ObjectClass objClass, 
            final Uid uid,
            OperationOptions options) {
        Assertions.nullCheck(objClass, "objClass");
        Assertions.nullCheck(uid, "uid");
        //cast null as empty
        if ( options == null ) {
            options = new OperationOptionsBuilder().build();
        }
        Connector connector = getConnector();
        final ObjectNormalizerFacade normalizer =
            getNormalizer(objClass);
        ((DeleteOp) connector).delete(objClass, 
                (Uid)normalizer.normalizeAttribute(uid),
                options);
    }
}
