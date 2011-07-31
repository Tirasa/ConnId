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

import org.identityconnectors.framework.api.APIConfiguration;

/**
 * Tests the {@link APIConfiguration configuration} with the connector.
 * 
 * <p>Unlike {@link ValidateApiOp#validate() validation}, testing a configuration should
 * check that any pieces of environment referred by the configuration are available.
 * For example the connector could make a physical connection to a host specified
 * in the configuration to check that it exists and that the credentials
 * specified in the configuration are usable.</p>
 * 
 * <p>Since this operation may connect to the resource, it may be slow. Clients are
 * advised not to invoke this operation often, such as before every provisioning operation.
 * This operation is <strong>not</strong> intended to check that the connector is alive
 * (i.e., that its physical connection to the resource has not timed out).
 * 
 * <p>This operation may be invoked before the configuration has been validated.</p>  
 */ 
public interface TestApiOp extends APIOperation {

    /**
     * Tests the {@link APIConfiguration Configuration} with the connector.
     * 
     * @throws RuntimeException
     *             iff the configuration is not valid or the test failed.
     */
    void test();
}
