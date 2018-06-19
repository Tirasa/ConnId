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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.contract.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.contract.data.DataProvider;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.spi.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Configuration} of the Connector Under Test
 *
 * @author Tomas Knappek
 */
public final class ConfigurationTests {

    private static final Log LOG = Log.getLog(ConfigurationTests.class);

    private ConfigurationProperties _configProperties = null;

    /**
     * Initialize the unit test
     */
    @BeforeEach
    public void init() {
        DataProvider dataProvider = ConnectorHelper.createDataProvider();
        _configProperties = ConnectorHelper.getConfigurationProperties(dataProvider);
    }

    /**
     * Free up the resources
     */
    @AfterEach
    public void dispose() {
        _configProperties = null;
    }

    /**
     * Unit test for checking if the {@link Configuration} property type is supported
     */
    @Test
    public void testPropertiesType() {
        assertNotNull(_configProperties);

        List<String> propertyNames = _configProperties.getPropertyNames();
        assertNotNull(propertyNames);

        // go through the properties and check the type
        propertyNames.forEach(propertyName -> {
            ConfigurationProperty property = _configProperties.getProperty(propertyName);
            assertNotNull(property);

            String typeName = property.getType().getName();
            LOG.ok("Property: ''" + property.getName() + "'' type ''" + typeName + "''");
            assertTrue(FrameworkUtil.isSupportedConfigurationType(property.getType()),
                    "Type " + typeName + " not allowed in configuration!");
        });
    }
}
