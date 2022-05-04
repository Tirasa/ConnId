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
 * The discovery process is supposed to work as follows:
 * <ol>
 *     <li>Create new connector facade with minimal configuration.
 *     This usually means just hostname, username and password.
 *     Note: Mind the connector pool size when creating this configuration.
 *     We really want minimal pool size set to zero. This connector instance is supposed to be
 *     very short-lived. We do not want it to stay in the pool for a long time.</li>
 *
 *     <li>Invoke testPartialConfiguration() method on the facade.
 *     If the method returns without an exception the provided configuration is correct,
 *     e.g. connection to the server was established, the username/password is correct.</li>
 *
 *     <li>Invoke discoverConfiguration() method on the facade.
 *     The connector will try to use the connection to discover additional configuration options,
 *     suggesting values for configuration properties.
 *     </li>
 *
 *     <li>Interact with user.
 *     Present the suggested values to the user, let user choose the right values, add additional configuration, etc.</li>
 *
 *     <li>Apply the values chosen by the user to a new APIConfiguration, creating a new connector facade.
 *     This is supposed to be a fully-configured, production-ready facade.</li>
 *
 *     <li>Use the new facade as usual.</li>
 * </ol>
 * The discovery process is usually used only once. Once the correct connector configuration is determined,
 * the discovery is no longer needed.
 * </p>
 * <p>
 * In theory, the discovery process can be iterative. The configuration suggested by discoverConfiguration()
 * can be used to create another temporary facade, on which the discoverConfiguration() is called again.
 * This process can be repeated as long as the discoverConfiguration() returns empty collection.
 * However, we expect that a single-iteration processes will be the usual case.
 * </p>
 * <p>
 * Tips for creating connectors with discoverable configuration:
 * <ul>
 *     <li>Allow minimal configuration for the connector.
 *     Define only a very small set of mandatory configuration properties.
 *     Validate only those properties for presence in validate() method of connector configuration object.
 *     (Of course, it is perfectly OK validating proper formats of any property that has a value.)
 *     Keep the validations simple.</li>
 *
 *     <li>Make a more in-depth validations in test() and testPartialConfiguration() methods.
 *     These methods are supposed to make a connection to the resource.
 *     Therefore, they can validate much more that just data formats.
 *     They can validate that a particular value is valid for that particular system.</li>
 * </ul>
 * </p>
 *
 * @author Radovan Semancik
 * @since 1.5.2.0
 */
public interface DiscoverConfigurationOp extends SPIOperation {

    // Maintenance note: Do not forget to update documentation in DiscoverConfigurationApiOp, when updating this interface.

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
     * On the other hand, if a suggestion for a particular property is not present,
     * the connector does not make any suggestion.
     * The connector does not know anything about the property.
     * Note: It may be difficult to distinguish explicitly configured properties and default values.
     * Please see note in testPartialConfiguration() description.
     * </p>
     * <p>
     * Single-valued configuration properties can have multiple suggestions,
     * e.i a list of suggested values can be returned.
     * Individual suggested values should be considered to be options.
     * One of them (or none at all) should be selected by the user.
     * Similar approach applies to multi-valued configuration properties.
     * However, in that case more than one of the values can be selected.
     * I.e. the user can choose any combination of the suggested values (or no value at all).
     * </p>
     * <p>
     * Note: So far there is no support for suggesting several combinations of multi-valued configuration properties.
     * This can be added later, by allowing suggested values to be collections (lists).
     * However, this is not supported yet.
     * For now the suggested values must be primitive (i.e. non-complex, non-collection) data types.
     * We do not want to support it now, as it can be confusing in case that the value itself is complex (e.g a map).
     * Therefore we leave this decision for the future when the design for complex values is more mature.
     * </p>
     */
    Map<String, SuggestedValues> discoverConfiguration();

}
