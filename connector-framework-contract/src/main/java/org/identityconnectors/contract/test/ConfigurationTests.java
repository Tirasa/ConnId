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
 *
 * Portions Copyrighted 2012 ForgeRock AS
 *
 */
package org.identityconnectors.contract.test;

import java.util.List;

import org.identityconnectors.contract.data.DataProvider;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.spi.Configuration;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.log4testng.Logger;

import static org.testng.Assert.*;

/**
 * Tests for {@link Configuration} of the Connector Under Test
 *
 * @author Tomas Knappek
 */
public final class ConfigurationTests {

    private static final Logger logger = Logger.getLogger(ValidateApiOpTests.class);
    private ConfigurationProperties _configProperties = null;

    /**
     * Initialize the unit test
     */
    @BeforeMethod
    public void init() {
        DataProvider dataProvider = ConnectorHelper.createDataProvider();
        _configProperties = ConnectorHelper.getConfigurationProperties(dataProvider);
    }

    /**
     * Free up the resources
     */
    @AfterMethod
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

        //go through the properties and check the type
        for (String propertyName : propertyNames) {
            ConfigurationProperty property =  _configProperties.getProperty(propertyName);
            assertNotNull(property);

            String typeName = property.getType().getName();
            logger.trace("Property: ''"+property.getName()+"'' type ''"+typeName+"''");
            assertTrue(FrameworkUtil.isSupportedConfigurationType(property.getType()),
                    "Type " + typeName + " not allowed in configuration!");
        }
    }
}
