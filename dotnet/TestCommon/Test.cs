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
using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Xml;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
using Org.IdentityConnectors.Test.Common.Spi;
using System.Globalization;
using System.Xml.Schema;

namespace Org.IdentityConnectors.Test.Common
{
    /// <summary>    
    /// <see cref="ResultsHandler"/> which stores all connector objects into
    /// list retrievable with <see cref="Objects"/>.
    /// </summary>
    public sealed class ToListResultsHandler
    {
        private IList<ConnectorObject> _objects
            = new List<ConnectorObject>();
        public bool Handle(ConnectorObject obj)
        {
            _objects.Add(obj);
            return true;
        }

        public IList<ConnectorObject> Objects
        {
            get
            {
                return _objects;
            }
        }
    }

    /// <summary>
    /// Bag of utility methods useful to connector tests.
    /// </summary>
    public sealed class TestHelpers
    {

        private TestHelpers()
        {
        }

        /// <summary>
        /// Method for convenient testing of local connectors.
        /// </summary>
        public static APIConfiguration CreateTestConfiguration(SafeType<Connector> clazz,
                Configuration config)
        {
            return GetSpi().CreateTestConfiguration(clazz, config);
        }

        /// <summary>
        /// Fills a configuration bean with data from the given map.
        /// </summary>
        /// <remarks>
        /// The map
        /// keys are configuration property names and the values are
        /// configuration property values.
        /// </remarks>
        /// <param name="config">the configuration bean.</param>
        /// <param name="configData">the map with configuration data.</param>
        public static void FillConfiguration(Configuration config,
                IDictionary<string, object> configData)
        {
            GetSpi().FillConfiguration(config, configData);
        }

        /// <summary>
        /// Creates an dummy message catalog ideal for unit testing.
        /// </summary>
        /// <remarks>
        /// All messages are formatted as follows:
        /// <para>
        /// <code><i>message-key</i>: <i>arg0.toString()</i>, ..., <i>argn.toString</i></code>
        /// </para>
        /// </remarks>
        /// <returns>A dummy message catalog.</returns>
        public static ConnectorMessages CreateDummyMessages()
        {
            return GetSpi().CreateDummyMessages();
        }

        public static IList<ConnectorObject> SearchToList(SearchApiOp search,
                ObjectClass oclass,
                Filter filter)
        {
            return SearchToList(search, oclass, filter, null);
        }

        public static IList<ConnectorObject> SearchToList(SearchApiOp search,
                ObjectClass oclass,
                Filter filter,
                OperationOptions options)
        {
            ToListResultsHandler handler = new
                 ToListResultsHandler();
            search.Search(oclass, filter, handler.Handle, options);
            return handler.Objects;
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
        /// <param name="options">The options - may be null - will
        /// be cast to an empty OperationOptions</param>
        /// <returns>The list of results.</returns>
        public static IList<ConnectorObject> SearchToList<T>(SearchOp<T> search,
                ObjectClass oclass,
                Filter filter) where T : class
        {
            return SearchToList(search, oclass, filter, null);
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
        /// <param name="options">The options - may be null - will
        /// be cast to an empty OperationOptions</param>
        /// <returns>The list of results.</returns>
        public static IList<ConnectorObject> SearchToList<T>(SearchOp<T> search,
                ObjectClass oclass,
                Filter filter,
                OperationOptions options) where T : class
        {
            ToListResultsHandler handler = new
                 ToListResultsHandler();
            Search(search, oclass, filter, handler.Handle, options);
            return handler.Objects;
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
        public static void Search<T>(SearchOp<T> search,
                ObjectClass oclass,
                Filter filter,
                ResultsHandler handler,
                OperationOptions options) where T : class
        {
            GetSpi().Search(search, oclass, filter, handler, options);
        }

        //At some point we might make this pluggable, but for now, hard-code
        private const String IMPL_NAME =
            "Org.IdentityConnectors.Framework.Impl.Test.TestHelpersImpl";
        private static readonly object LOCK = new object();
        private static TestHelpersSpi _instance;

        /// <summary>
        /// Returns the instance of this factory.
        /// </summary>
        /// <returns>The instance of this factory</returns>
        private static TestHelpersSpi GetSpi()
        {
            lock (LOCK)
            {
                if (_instance == null)
                {
                    SafeType<TestHelpersSpi> type = FrameworkInternalBridge.LoadType<TestHelpersSpi>(IMPL_NAME);
                    _instance = type.CreateInstance();
                }
                return _instance;
            }
        }

        private static Dictionary<string, PropertyBag> _propertyBags = new Dictionary<string, PropertyBag>();
        private const string ConfigFileName = "config.xml";
        private static readonly object ConfigurationLock = new object();
        private const string LoadConfigErrorMessage = @"TestHelpers: Unable to load optional XML configuration file ""{0}"". Exception: {1}";

        /// <summary>
        /// The name of the environment variable that contains the name of a certain configuration from which the
        /// test configuration properties to be loaded besides the general properties.
        /// </summary>
        internal const string TestConfigEVName = "TEST_CONFIG";

        /// <summary>
        /// The name of the environment variable that contains the path of the root directory to the private configurations.
        /// </summary>
        internal const string PrivateConfigRootEVName = "PRIVATE_CONFIG_ROOT";

        /// <summary>
        /// The name of the environment variable, the value of which is the location of the current user's profile directory.
        /// </summary>
        internal const string UserProfileEVName = "USERPROFILE";

        /// <overloads>
        /// Gets the configuration properties for a specified type.
        /// </overloads>
        /// 
        /// <summary>
        /// Gets the configuration properties for the specified <paramref name="type"/> from the provided <paramref name="assembly"/>.
        /// </summary>
        /// <param name="type">The type, the fully qualified name (FQN) of which to be used to identify the configuration.</param>
        /// <param name="assembly">The assembly, that contains the public configuration properties. Recommended to be the test project.</param>
        /// <returns>Bag of properties for the specified <paramref name="type"/> and optionally set configuration.</returns>
        /// <remarks>The properties are loaded from public and private configuration files, the former as manifest resources, the
        /// latter as file system entries by using the specified <paramref name="type"/> as root prefix. Optionally, a certain test setup
        /// can be used by defining the "TEST_CONFIG" environment variable that can be used to override the general configuration properties. Both
        /// public and private configurations can be overridden with a certain test setup.
        /// <para>
        /// Public configuration properties are loaded as manifest resources from the specified <paramref name="assembly"/> according
        /// to the following:
        /// <list type="number">
        ///     <item>
        ///         <description>Load the general properties from a resource, the name of which is constructed as follows:
        ///             <code>type.FullName + ".config.config.xml"</code>. To achieve this, you need to create a new folder
        ///             in your test project named as the FQN of the specified <paramref name="type"/>, then create a folder called
        ///             "config", at last add the configuration file called "config.xml" to this folder and set its "Build Action"
        ///             property to "Embedded Resource".
        ///         </description>
        ///     </item>
        ///     <item>
        ///         <description>Load the configuration specific properties from a resource, the name of which is constructed
        ///             as follows:
        ///             <code>type.FullName + ".config."+ Environment.GetEnvironmentVariable("TEST_CONFIG") + ".config.xml"</code>.
        ///             To achieve this, you need to create a new folder underneath the previously created "config" folder, its name
        ///             is defined by the configuration name and add the "config.xml" to this particular folder with "Build Action"
        ///             property set to "Embedded Resource".
        ///         </description>
        ///     </item>
        /// </list>
        /// </para>
        /// <para>
        /// The private configuration properties are loaded from the file system as follows:
        /// <list type="number">
        ///     <item>
        ///         <description>Load the general properties from a file, the path of which is constructed as follows:
        ///             <code>Environment.GetEnvironmentVariable("PRIVATE_CONFIG_ROOT") + "\config\" + type.FullName + "\config-private\config.xml"</code>
        ///         </description>
        ///     </item>
        ///     <item>
        ///         <description>Load the configuration specific properties from a file, the path of which is constructed as follows:
        ///             <code>Environment.GetEnvironmentVariable("PRIVATE_CONFIG_ROOT") + "\config\" + type.FullName + "\config-private\" + 
        ///             Environment.GetEnvironmentVariable("TEST_CONFIG") + "\config.xml"</code>
        ///         </description>
        ///     </item>
        /// </list>
        /// NOTE that if the "PRIVATE_CONFIG_ROOT" environment variable is not defined, it will be replaced in the path with the default root
        /// which points to a directory of the current user's profile container, the path of which is constructed as follows:
        /// <code>Environment.GetEnvironmentVariable("USERPROFILE") + "\.connectors\" + type.Assembly.GetName().Name</code>
        /// <example>For example: c:\Users\Administrator\.connectors\CONNECTOR_CONTAINER_ASSEMBLY_NAME\</example>
        /// </para>
        /// </remarks>
        /// <exception cref="InvalidOperationException">Thrown when the root directory of the private configuration cannot be determined.</exception>
        public static PropertyBag GetProperties(Type type, Assembly assembly)
        {
            lock (ConfigurationLock)
            {
                PropertyBag bag;
                if (_propertyBags.ContainsKey(type.FullName))
                {
                    bag = _propertyBags[type.FullName];
                }
                else
                {
                    bag = LoadProperties(type, assembly);
                    _propertyBags.Add(type.FullName, bag);
                }
                return bag;
            }
        }

        /// <summary>
        /// Gets the configuration properties for the specified <paramref name="type"/> from the calling assembly.
        /// </summary>
        /// <param name="type">The type, the fully qualified name (FQN) of which to be used to identify the configuration.</param>
        /// <returns>Bag of properties for the specified <paramref name="type"/> and optionally set configuration.</returns>
        /// <remarks>See <see cref="M:GetProperties(Type, Assembly)"/> for details of the property loading mechanism.</remarks>
        public static PropertyBag GetProperties(Type type)
        {
            return GetProperties(type, Assembly.GetCallingAssembly());
        }

        /// <summary>
        /// Loads the properties from public and private configurations, optionally from a certain configuration defined by
        /// "TEST_CONFIG" environment variable.
        /// </summary>
        /// <param name="type">The type, the fully qualified name (FQN) of which to be used to identify the configuration.</param>
        /// <param name="assembly">The assembly, that contains the configuration resources.</param>
        /// <returns>Bag of properties for the specified <paramref name="type"/>.</returns>
        /// <exception cref="InvalidOperationException">Thrown when the root directory of the private configuration cannot be determined.</exception>
        private static PropertyBag LoadProperties(Type type, Assembly assembly)
        {
            string bagName = type.FullName;
            string configFilePath = string.Empty;
            IDictionary<string, string> properties = null;
            var ret = new Dictionary<string, object>();

            //load the general public properties file
            configFilePath = string.Format(CultureInfo.InvariantCulture, "{0}.config.{1}", bagName, ConfigFileName);
            properties = LoadConfigurationFromResource(assembly, configFilePath);
            CollectionUtil.AddOrReplaceAll(ret, properties);

            //load the configuration specific public properties file
            string configurationName = Environment.GetEnvironmentVariable(TestConfigEVName);
            if (!StringUtil.IsBlank(configurationName))
            {
                configFilePath = string.Format(CultureInfo.InvariantCulture, "{0}.config.{1}.{2}", bagName,
                                               configurationName, ConfigFileName);
                properties = LoadConfigurationFromResource(assembly, configFilePath);
                CollectionUtil.AddOrReplaceAll(ret, properties);
            }

            //determine the root directory of the private properties files
            string privateConfigRoot = string.Empty;
            if (Environment.GetEnvironmentVariable(PrivateConfigRootEVName) != null)
            {
                privateConfigRoot = Environment.GetEnvironmentVariable(PrivateConfigRootEVName);
            }
            else
            {
                if (Environment.GetEnvironmentVariable(UserProfileEVName) != null)
                {
                    privateConfigRoot = Path.Combine(Environment.GetEnvironmentVariable(UserProfileEVName),
                        Path.Combine(".connectors", type.Assembly.GetName().Name));
                }
                else
                    throw new InvalidOperationException(
                        @"Neither the ""PRIVATE_CONFIG_ROOT"" nor the ""USERPROFILE"" environment variable is defined.");
            }

            privateConfigRoot = Path.Combine(privateConfigRoot, "config");

            //load the general private properties file
            configFilePath = Path.Combine(Path.Combine(Path.Combine(privateConfigRoot, bagName), "config-private"), ConfigFileName);
            properties = LoadConfigurationFromFile(configFilePath);
            CollectionUtil.AddOrReplaceAll(ret, properties);

            // load the configuration specific private properties file
            if (!StringUtil.IsBlank(configurationName))
            {
                configFilePath = Path.Combine(Path.Combine(Path.Combine(Path.Combine(privateConfigRoot, bagName),
                    "config-private"), configurationName), ConfigFileName);
                properties = LoadConfigurationFromFile(configFilePath);
                CollectionUtil.AddOrReplaceAll(ret, properties);
            }
            return new PropertyBag(ret);
        }

        /// <summary>
        /// Loads the configuration properties from the specified <paramref name="filePath"/>.
        /// </summary>
        /// <param name="filePath">The config file path.</param>
        /// <returns>A property name-value pair collection representing the configuration.</returns>
        private static IDictionary<string, string> LoadConfigurationFromFile(string filePath)
        {
            IDictionary<string, string> properties = null;
            try
            {
                if (File.Exists(filePath))
                {
                    using (var stream = new FileStream(filePath, FileMode.Open, FileAccess.Read, FileShare.Read))
                    {
                        properties = ReadConfiguration(stream);
                    }
                }
                else
                {
                    Trace.TraceWarning(@"The configuration file on path ""{0}"" is not found.", filePath);
                }
            }
            catch (Exception e)
            {
                Trace.TraceInformation(LoadConfigErrorMessage, filePath, e);
            }
            return properties;
        }

        /// <summary>
        /// Loads the configuration properties from a resource defined by <paramref name="configResName"/>.
        /// </summary>
        /// <param name="assembly">The assembly, that contains the resource.</param>
        /// <param name="configResName">The name of the resource.</param>
        /// <returns>A property name-value pair collection representing the configuration.</returns>
        private static IDictionary<string, string> LoadConfigurationFromResource(Assembly assembly, string configResName)
        {
            IDictionary<string, string> properties = null;
            try
            {
                //the default namespace with which the resource name starts is not known, therefore
                //it must be looked up in the manifest resource list
                var resourceName = (from name in assembly.GetManifestResourceNames()
                                    where name.EndsWith(configResName, StringComparison.InvariantCultureIgnoreCase)
                                    select name).FirstOrDefault();
                if (!StringUtil.IsBlank(resourceName))
                {
                    using (var stream = assembly.GetManifestResourceStream(resourceName))
                    {
                        if (stream == null)
                        {
                            throw new InvalidOperationException(string.Format(CultureInfo.InvariantCulture,
                                                                              @"Although, the configuration file called ""{0}"" exists, it cannot be accessed.",
                                                                              configResName));
                        }
                        properties = ReadConfiguration(stream);
                    }
                }
                else
                {
                    Trace.TraceWarning(@"The configuration resource called ""{0}"" is not found.", configResName);
                }
            }
            catch (Exception e)
            {
                Trace.TraceInformation(LoadConfigErrorMessage, configResName, e);
            }
            return properties;
        }

        /// <summary>
        /// Reads the configuration properties from the provided <paramref name="configStream"/> stream.
        /// </summary>
        /// <param name="configStream">The stream containing the configuration properties.</param>
        /// <returns>A property name-value pair collection representing the configuration.</returns>
        /// <remarks>The configuration properties are stored in XML format. The stream, that is opened for reading or it can be read,
        /// has to contain the configuration properties in XML that adheres to the config.xsd schema embedded to
        /// the assembly of this project.
        /// 
        /// <example>For example:
        /// <![CDATA[
        /// <?xml version="1.0" encoding="utf-8" ?>
        /// <config>
        ///   <property name="foo" value="bar"/>
        /// <config>
        /// ]]>
        /// </example>
        /// </remarks>
        /// <exception cref="InvalidOperationException">Thrown when the XSD used for validating the configuration is not found in the manifest.</exception>
        /// <exception cref="XmlSchemaValidationException">Thrown when the <paramref name="configStream"/> contains XML that does not adhere to the schema.</exception>
        internal static IDictionary<string, string> ReadConfiguration(Stream configStream)
        {
            var properties = new Dictionary<string, string>();
            //validate the XML configuration against the XSD
            var schemaSet = new XmlSchemaSet();
            var schemaStream = Assembly.GetExecutingAssembly().GetManifestResourceStream("Org.IdentityConnectors.Test.Common.config.xsd");
            if (schemaStream == null)
            {
                throw new InvalidOperationException(@"The schema used for validating of the configuration file is not found.");
            }
            var schemaReader = XmlReader.Create(schemaStream);
            schemaSet.Add(null, schemaReader);

            //load the reader with the data stream and ignore all white space nodes
            var readerSettings = new XmlReaderSettings
                                     {
                                         IgnoreWhitespace = true,
                                         ValidationType = ValidationType.Schema,
                                         Schemas = schemaSet
                                     };

            using (var reader = XmlReader.Create(configStream, readerSettings))
            {
                //read all the nodes and extract the ones that contain properties
                while (reader.Read())
                {
                    if (reader.NodeType == XmlNodeType.Element &&
                        reader.Name.Equals("property"))
                    {
                        string name = reader.GetAttribute("name");
                        string xmlValue = reader.GetAttribute("value");
                        if (!StringUtil.IsBlank(name) && xmlValue != null)
                        {
                            properties[name] = xmlValue;
                        }
                    }
                }
            }
            return properties;
        }
    }
}
