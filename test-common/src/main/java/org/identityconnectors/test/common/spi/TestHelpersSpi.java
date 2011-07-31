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
package org.identityconnectors.test.common.spi;

import java.util.Map;

import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.SearchOp;

/**
 * Private use only, do not implement! Use the methods in 
 * {@link org.identityconnectors.test.common.TestHelpers} instead.
 */
public interface TestHelpersSpi {

    public APIConfiguration createTestConfiguration(
            Class<? extends Connector> clazz, Configuration config);

    public void fillConfiguration(Configuration config, Map<String, ? extends Object> configData);

    public void search(SearchOp<?> search,
            final ObjectClass oclass, final Filter filter,
            ResultsHandler handler, OperationOptions options);

    public ConnectorMessages createDummyMessages();

}
