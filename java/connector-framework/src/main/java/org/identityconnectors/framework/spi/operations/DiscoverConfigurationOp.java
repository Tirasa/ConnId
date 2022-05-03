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
package org.identityconnectors.framework.spi.operations;

import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;

import java.util.Map;

/**
 * <p>
 * Operation interface for configuration discovery, check and recommendation.
 * This interface is used for several purposes.
 * Firstly, it can be used to check whether minimal connector configuration is correct
 * (e.g. that username and password are correct), without the need to have a complete
 * connector configuration.
 * Secondly, the connector can use partial configuration to discover other configuration details,
 * and suggest them to system administrator.
 * E.g. LDAP connector can read root DSE and discover base contexts provided by LDAP server,
 * then suggest the base contexts are values for the baseContext connector configuration property.
 * Overall, this operation can support an interactive way for connector configuration.
 * </p>
 * <p>
 * TODO: describe the process:
 *      create new connector facade with minimal configuration (mind pool size)
 *      invoke testPartialConfiguration(), discoverConfiguration()
 *      interact with user
 *      create new connector facade with full configuration
 *      enjoy
 * </p>
 * TODO: in theory, several iterations can be used.
 *
 * @author Radovan Semancik
 * @since 1.5.2.0
 */
public interface DiscoverConfigurationOp extends SPIOperation {

    /**
     * <p>
     * Tests partial configuration of the connector.
     * It is similar to {@link TestOp}, however, it is supposed to be much more forgiving.
     * While the {@link TestOp#test()} is supposed to test complete connector configuration,
     * making sure that all features of the connector are working, this method does not make
     * such a completeness requirement.
     * The testPartialConfiguration() is supposed to test the very minimal configuration set,
     * which is usually just a set of mandatory configuration properties.
     * For most connectors this will be probably just a hostname, username and password.
     * </p>
     * <p>
     * This method returns successfully if the minimal configuration is correct, i.e. the connector
     * could at least establish a basic connection to the resource.
     * Return from this method does NOT indicate that connector is fully operational.
     * This method will raise an appropriate exception in case that the configuration test fails.
     * </p>
     * <p>
     * Development note: Currently the connector does not have any means to know which configuration
     * properties were explicitly configured, and which were set to default values.
     * Therefore the connector does not know what parts of the configuration should be tested.
     * E.g. CSV connector has "," as default value for separator.
     * Connector has no way to tell whether the "," was explicitly configured by system administrator
     * (hence it should test it), or it was set as a default value (hence it should NOT test it).
     * For now, this is unlikely to be a major problem. There is probably some very basic set of
     * configuration properties that are intuitively understood sa minimal configuration.
     * Later, if that would cause problems, we can add a method to AbstractConfiguration or Configuration
     * that could be used to retrieve a list of properties that were explicitly configured.
     * </p>
     *
     * @throws RuntimeException
     *             if the minimal configuration is not valid or the test failed.
     *             Implementations are encouraged to throw the most specific
     *             exception available. When no specific exception is available,
     *             implementations can throw {@link ConnectorException}.
     */
    void testPartialConfiguration();

    /**
     * <p>
     * Discovers additional configuration properties.
     * The connector is supposed to use minimal configuration to connect to the resource,
     * then use the connection to discover additional configuration properties.
     * Discovered configuration properties are returned from this method (if any).
     * </p>
     * <p>
     * Only discovered values are present in the map.
     * There is no need to add all configuration properties, or even repeat the configured values.
     * Empty map means no suggestions, i.e. the current configuration is complete.
     * Empty list of values in a specific means that there are no valid values.
     * The connector suggests that the property should be configured with no value at all (null).
     * In that case the connector knows that there should be no values.
     * On the other hand, if a suggestion for a particular property is not present, the connector does not make any suggestion.
     * The connector does not know anything about the property.
     * </p>
     * TODO:
     * Note about multi-valued suggestions for single-valued properties?
     * How should multi-valued suggestions for multi-valued attributes be interpreted?
     */
    Map<String, SuggestedValues> discoverConfiguration();

}
