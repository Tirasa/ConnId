/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2022 Evolveum. All rights reserved.
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
package org.identityconnectors.contract.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.DiscoverConfigurationApiOp;
import org.identityconnectors.framework.common.objects.SuggestedValues;
import org.junit.jupiter.api.Test;

/**
 * Contract test of {@link DiscoverConfigurationApiOp}.
 */
public class DiscoverConfigurationApiOpTests extends ContractTestBase {

    public static final String TEST_NAME = "DiscoverConfiguration";

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        // list of required operations by this test:
        Set<Class<? extends APIOperation>> s = new HashSet<>();
        s.add(DiscoverConfigurationApiOp.class);
        return s;
    }

    /**
     * Basic positive test case.
     * Basically, it checks whether there are no exceptions, and basic sanity of returned values.
     */
    @Test
    protected void testBasic() {
        // No exception means success. Nothing to assert.
        getConnectorFacade().testPartialConfiguration();

        Map<String, SuggestedValues> discoveredConfiguration = getConnectorFacade().discoverConfiguration();
        assertNotNull(discoveredConfiguration, "Null configuration discovered");

        // Not much to assert here. We do not know what the connector can discover.
        // However, we can at least check if the returned properties (if any) have legal names.
        ConfigurationProperties configurationProperties = ConnectorHelper.getConfigurationProperties(getDataProvider());
        for (Map.Entry<String, SuggestedValues> discoveredConfigurationEntry : discoveredConfiguration.entrySet()) {
            assertTrue(configurationProperties.getPropertyNames().contains(discoveredConfigurationEntry.getKey()),
                    "Discovered property " + discoveredConfigurationEntry.getKey() + " is not legal configuration property");
        }

    }

}
