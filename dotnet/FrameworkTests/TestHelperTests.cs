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
using System.IO;
using System.Xml;
using System.Collections.Generic;
using System.Linq;

using NUnit.Framework;

using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Test.Common;
using System.Text;
using System.Diagnostics;
using Org.IdentityConnectors.Common;

namespace FrameworkTests
{
    /// <summary>
    /// Description of TestHelperTests.
    /// </summary>
    [TestFixture]
    public class TestHelperTests
    {
        [Test]
        public void TestReadConfiguration()
        {
            using (var memoryStream = new MemoryStream())
            {
                var properties = new Dictionary<string, string>()
                                     {
                                         {"bob", "bobsValue"},
                                         {"bob2", "bob2sValue"}
                                     };

                CreateXmlConfiguration(memoryStream, properties);

                memoryStream.Seek(0, SeekOrigin.Begin);
                // load the properties files
                var dict = TestHelpers.ReadConfiguration(memoryStream);
                foreach (var property in properties)
                {
                    Assert.AreEqual(dict[property.Key], property.Value);
                }
            }
        }

        /// <summary>
        /// Creates an XML configuration in the specified stream based on the <paramref name="properties"/>.
        /// </summary>
        /// <param name="stream">The output stream.</param>
        /// <param name="properties">The properties to be stored.</param>
        /// <remarks>The caller is responsible for closing the stream.</remarks>
        private static void CreateXmlConfiguration(Stream stream, IDictionary<string, string> properties)
        {
            var settings = new XmlWriterSettings
                               {
                                   Encoding = Encoding.UTF8,
                                   CloseOutput = false
                               };
            using (var writer = XmlWriter.Create(stream, settings))
            {
                writer.WriteStartDocument();
                writer.WriteStartElement("config");
                foreach (var property in properties)
                {
                    writer.WriteStartElement("property");
                    writer.WriteAttributeString("name", property.Key);
                    writer.WriteAttributeString("value", property.Value);
                    writer.WriteEndElement();
                }
                writer.WriteEndDocument();
                writer.Flush();
            }
        }

        [Test]
        public void TestGetProperties()
        {
            const string testConfigName = "myconfig";
            Type connectorType = typeof(Org.IdentityConnectors.TestConnector.FakeConnector);

            //store environment variables, they must be restored at the end
            var oldTestConfig = Environment.GetEnvironmentVariable(TestHelpers.TestConfigEVName);
            var oldPrivateConfigRoot = Environment.GetEnvironmentVariable(TestHelpers.PrivateConfigRootEVName);
            Environment.SetEnvironmentVariable(TestHelpers.TestConfigEVName, testConfigName);

            var privateConfigRoot = Path.GetTempPath();

            try
            {
                //set the TestHelpers.PrivateConfigRootEVName environment variable used by this test
                Environment.SetEnvironmentVariable(TestHelpers.PrivateConfigRootEVName, privateConfigRoot);

                //at the end the created dir structure must be deleted, hence we need to store this
                privateConfigRoot = Path.Combine(privateConfigRoot, "config");

                var privateConfigPath = Path.Combine(Path.Combine(privateConfigRoot, connectorType.FullName),
                                                     "config-private");
                //create the directory structure for the general and the specific "testConfigName" private config
                Directory.CreateDirectory(Path.Combine(privateConfigPath, testConfigName));

                //create general private config file
                using (var configFile = File.Create(Path.Combine(privateConfigPath, "config.xml")))
                {
                    CreateXmlConfiguration(configFile, new Dictionary<string, string>() {{"privatekey", "value"}});
                }

                //create specific private config file
                using (var configFile = File.Create(Path.Combine(Path.Combine(privateConfigPath, testConfigName), "config.xml")))
                {
                    CreateXmlConfiguration(configFile, new Dictionary<string, string>() {{"myconfig.privatekey", "value"}});
                }

                PropertyBag bag1 = TestHelpers.GetProperties(connectorType);
                CheckProperties(bag1);
                PropertyBag bag2 = TestHelpers.GetProperties(connectorType);
                Assert.AreSame(bag1, bag2, "TestHepers must create the same PropertyBag for the same connector");
            }
            finally
            {
                if (oldTestConfig != null)
                {
                    Environment.SetEnvironmentVariable(TestHelpers.TestConfigEVName, oldTestConfig);
                }
                if (oldPrivateConfigRoot != null)
                {
                    Environment.SetEnvironmentVariable(TestHelpers.PrivateConfigRootEVName, oldPrivateConfigRoot);
                }

                try
                {
                    if (Directory.Exists(privateConfigRoot))
                    {
                        Directory.Delete(privateConfigRoot, true);
                    }
                }
                catch (Exception ex)
                {
                    //although, something bad happened there is no need to fail the test since the next time it will overwrite
                    //the temporary files if any exists
                    Trace.TraceWarning(ex.ToString());
                }
            }
        }

        private static void CheckProperties(PropertyBag bag)
        {
            var properties = new Dictionary<string, object>
                                 {
                                     {"Help", "Me"},
                                     {"publickey", "value"},
                                     {"myconfig.publickey", "value"},
                                     {"privatekey", "value"},
                                     {"myconfig.privatekey", "value"}
                                 };
            
            var bagElements = bag.ToDictionary();
            Assert.That(
                CollectionUtil.DictionariesEqual(properties, bagElements) && CollectionUtil.DictionariesEqual(bagElements, properties),
                "Expected test properties not equal");
        }

        [Test]
        public void testFillConfiguration()
        {
            TestConfiguration testConfig = new TestConfiguration();
            // There is no "Foo" property in the config bean. We want to ensure
            // that TestHelpers.FillConfiguration() does not fail for unknown properties.
            IDictionary<string, object> configData = new Dictionary<string, object>();
            configData["Host"] = "example.com";
            configData["Port"] = 1234;
            configData["Foo"] = "bar";
            TestHelpers.FillConfiguration(testConfig, configData);

            Assert.AreEqual("example.com", testConfig.Host);
            Assert.AreEqual(1234, testConfig.Port);
        }

        public class TestConfiguration : Configuration
        {

            public ConnectorMessages ConnectorMessages
            {
                get
                {
                    return null;
                }
                set
                {
                    Assert.Fail("Should not set ConnectorMessages");
                }
            }

            public void Validate()
            {
                Assert.Fail("Should not call Validate()");
            }

            public String Host { get; set; }

            public int Port { get; set; }

            public String Unused { get; set; }
        }
    }
}

namespace Org.IdentityConnectors.TestConnector
{
    public class FakeConnector : Connector
    {
        #region Connector Members
        public void Init(Configuration configuration)
        {
            throw new NotImplementedException();
        }
        #endregion

        #region IDisposable Members
        public void Dispose()
        {
            throw new NotImplementedException();
        }
        #endregion
    }
}
