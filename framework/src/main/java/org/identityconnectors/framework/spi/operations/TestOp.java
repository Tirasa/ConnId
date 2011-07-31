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

import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.Configuration;

/**
 * Tests the connector {@link Configuration}.
 * 
 * <p>Unlike {@link Configuration#validate() validation}, testing a configuration
 * checks that any pieces of environment referred by the configuration are available.
 * For example, the connector could make a physical connection to a host specified
 * in the configuration to check that it exists and that the credentials
 * specified in the configuration are usable.</p>
 * 
 * <p>This operation may be invoked before the configuration has been validated.
 * An implementation is free to validate the configuration before testing it.</p>
 */
public interface TestOp extends SPIOperation {

    /**
     * Tests the {@link Configuration} with the connector.
     * 
     * @throws RuntimeException
     *             iff the configuration is not valid or the test failed. Implementations
     *             are encouraged to throw the most specific exception available.
     *             When no specific exception is available, implementations can throw
     *             {@link ConnectorException}.
     */
    void test();
}
