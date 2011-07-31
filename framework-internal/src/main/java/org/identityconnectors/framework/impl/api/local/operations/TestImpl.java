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

import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.TestOp;

/**
 * Provides a method for the API to call the SPI's test method on the
 * connector. The test method is intended to determine if the {@link Connector}
 * is ready to perform the various operations it supports.
 * 
 * @author Will Droste
 * 
 */
public class TestImpl extends ConnectorAPIOperationRunner implements TestApiOp {

    public TestImpl(ConnectorOperationalContext context, Connector connector) {
        super(context,connector);
    }

    /**
     * {@inheritDoc}
     */
    public void test() {
        ((TestOp) getConnector()).test();
    }

}
