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
 * Portions Copyrighted 2014 ForgeRock AS.
 */
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Reflection;
using System.Text;

using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Impl.Api;
using Org.IdentityConnectors.Framework.Impl.Api.Local;
using Org.IdentityConnectors.Framework.Impl.Api.Local.Operations;
using Org.IdentityConnectors.Framework.Common;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
using Org.IdentityConnectors.Test.Common;
using Org.IdentityConnectors.Test.Common.Spi;

namespace Org.IdentityConnectors.Framework.Impl.Test
{
    public class TestHelpersImpl : TestHelpersSpi
    {
        /// <summary>
        /// Method for convenient testing of local connectors.
        /// </summary>
        public APIConfiguration CreateTestConfiguration(SafeType<Connector> clazz,
                Configuration config)
        {
            LocalConnectorInfoImpl info = new LocalConnectorInfoImpl();
            info.ConnectorConfigurationClass = SafeType<Configuration>.Get(config);
            info.ConnectorClass = (clazz);
            info.ConnectorDisplayNameKey = ("DUMMY_DISPLAY_NAME");
            info.ConnectorKey = (
                   new ConnectorKey(clazz.RawType.Name + ".bundle",
                    "1.0",
                    clazz.RawType.Name));
            info.Messages = (this.CreateDummyMessages());
            APIConfigurationImpl rv = new APIConfigurationImpl();
            rv.IsConnectorPoolingSupported = (
                    IsConnectorPoolingSupported(clazz));
            ConfigurationPropertiesImpl properties =
                CSharpClassProperties.CreateConfigurationProperties(config);
            rv.ConfigurationProperties = (properties);
            rv.ConnectorInfo = (info);
            rv.SupportedOperations = (
                    FrameworkUtil.GetDefaultSupportedOperations(clazz));
            info.DefaultAPIConfiguration = (
                    rv);
            return rv;
        }

        /// <summary>
        /// Method for convenient testing of local connectors.
        /// </summary>
        public APIConfiguration CreateTestConfiguration(SafeType<Connector> connectorClass, PropertyBag configData, string prefix)
        {
            Debug.Assert(null != connectorClass);
            Type rawConnectorClass = connectorClass.RawType;

            Object[] attributes = connectorClass.RawType.GetCustomAttributes(
                    typeof(ConnectorClassAttribute),
                    false);
            if (attributes.Length > 0)
            {
                ConnectorClassAttribute attribute =
                    (ConnectorClassAttribute)attributes[0];

                Assembly assembly = IOUtil.GetAssemblyContainingType(rawConnectorClass.FullName);

                String fileName = assembly.Location;
                SafeType<Configuration> connectorConfigurationClass = attribute.ConnectorConfigurationType;
                if (connectorConfigurationClass == null)
                {
                    String MSG = ("File " + fileName +
                                 " contains a ConnectorInfo attribute " +
                                 "with no connector configuration class.");
                    throw new ConfigurationException(MSG);
                }
                String connectorDisplayNameKey =
                    attribute.ConnectorDisplayNameKey;
                if (connectorDisplayNameKey == null)
                {
                    String MSG = ("File " + fileName +
                                  " contains a ConnectorInfo attribute " +
                                  "with no connector display name.");
                    throw new ConfigurationException(MSG);
                }
                LocalConnectorInfoImpl rv = new LocalConnectorInfoImpl();
                rv.ConnectorClass = connectorClass;
                rv.ConnectorConfigurationClass = connectorConfigurationClass;
                rv.ConnectorDisplayNameKey = connectorDisplayNameKey;
                rv.ConnectorCategoryKey = attribute.ConnectorCategoryKey;
                rv.ConnectorKey = (
                   new ConnectorKey(rawConnectorClass.Name + ".bundle",
                    "1.0",
                    rawConnectorClass.Name)); ;
                APIConfigurationImpl impl = LocalConnectorInfoManagerImpl.CreateDefaultAPIConfiguration(rv);
                rv.DefaultAPIConfiguration = impl;
                if (false)
                {
                    rv.Messages = CreateDummyMessages();
                }
                else
                {
                    rv.Messages = LocalConnectorInfoManagerImpl.LoadMessages(assembly, rv, attribute.MessageCatalogPaths);
                }
                ConfigurationPropertiesImpl configProps = (ConfigurationPropertiesImpl)impl.ConfigurationProperties;

                string fullPrefix = StringUtil.IsBlank(prefix) ? null : prefix + ".";

                foreach (ConfigurationPropertyImpl property in configProps.Properties)
                {
                    object value = configData.GetProperty(null != fullPrefix ? fullPrefix + property.Name : property.Name, property.ValueType, property.Value);
                    if (value != null)
                    {
                        property.Value = value;
                    }
                }
                return impl;
            }
            throw new ArgumentException("ConnectorClass does not define ConnectorClassAttribute");
        }

        public void FillConfiguration(Configuration config, IDictionary<string, object> configData)
        {
            IDictionary<string, object> configDataCopy = new Dictionary<string, object>(configData);
            ConfigurationPropertiesImpl configProps =
                CSharpClassProperties.CreateConfigurationProperties(config);
            foreach (string propName in configProps.PropertyNames)
            {
                object value;
                if (configDataCopy.TryGetValue(propName, out value))
                {
                    // Remove the entry from the config map, so that at the end
                    // the map only contains entries that were not assigned to a config property.
                    configDataCopy.Remove(propName);
                    configProps.SetPropertyValue(propName, value);
                }
            }
            // The config map now contains entries that were not assigned to a config property.
            foreach (string propName in configDataCopy.Keys)
            {
                Trace.TraceWarning("Configuration property {0} does not exist!", propName);
            }
            CSharpClassProperties.MergeIntoBean(configProps, config);
        }

        private static bool IsConnectorPoolingSupported(SafeType<Connector> clazz)
        {
            return ReflectionUtil.IsParentTypeOf(typeof(PoolableConnector), clazz.RawType);
        }

        /// <summary>
        /// Performs a raw, unfiltered search at the SPI level,
        /// eliminating duplicates from the result set.
        /// </summary>
        /// <param name="search">The search SPI</param>
        /// <param name="oclass">The object class - passed through to
        /// connector so it may be null if the connecor
        /// allowing it to be null. (This is convenient for
        /// unit tests, but will not be the case in general)</param>
        /// <param name="filter">The filter to search on</param>
        /// <param name="handler">The result handler</param>
        /// <param name="options">The options - may be null - will
        /// be cast to an empty OperationOptions</param>
        public SearchResult Search<T>(SearchOp<T> search,
                ObjectClass objectClass,
                Filter filter,
                ResultsHandler handler,
                OperationOptions options) where T : class
        {
            Assertions.NullCheck(objectClass, "objectClass");
            if (ObjectClass.ALL.Equals(objectClass))
            {
                throw new System.NotSupportedException("Operation is not allowed on __ALL__ object class");
            }
            Assertions.NullCheck(handler, "handler");
            //convert null into empty
            if (options == null)
            {
                options = new OperationOptionsBuilder().Build();
            }

            SearchResult result = null;
            RawSearcherImpl<T>.RawSearch(search, objectClass, filter, new SearchResultsHandler()
            {
                Handle = obj =>
                {
                    return handler.Handle(obj);
                },
                HandleResult = obj =>
                {
                    result = obj;
                }

            }, options);
            return result != null ? result : new SearchResult();
        }

        public ConnectorMessages CreateDummyMessages()
        {
            return new DummyConnectorMessages();
        }

        private class DummyConnectorMessages : ConnectorMessages
        {
            public String Format(String key, String dflt, params Object[] args)
            {
                StringBuilder builder = new StringBuilder();
                builder.Append(key);
                builder.Append(": ");
                String sep = "";
                foreach (Object arg in args)
                {
                    builder.Append(sep);
                    builder.Append(arg);
                    sep = ", ";
                }
                return builder.ToString();
            }
        }
    }
}