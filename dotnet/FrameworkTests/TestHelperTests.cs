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
using System.IO;
using System.Linq;
using System.Reflection;
using System.Xml;
using System.Collections.Generic;
using NUnit.Framework;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Test.Common;
using System.Text;
using System.Diagnostics;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.TestConnector;

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
                    CreateXmlConfiguration(configFile, new Dictionary<string, string>() { { "privatekey", "value" } });
                }

                //create specific private config file
                using (var configFile = File.Create(Path.Combine(Path.Combine(privateConfigPath, testConfigName), "config.xml")))
                {
                    CreateXmlConfiguration(configFile, new Dictionary<string, string>() { { "myconfig.privatekey", "value" } });
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
        public void TestFillConfiguration()
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

        [Test]
        public void TestCreateTestConfiguration()
        {           
            IDictionary<string, object> expectedData = new Dictionary<string, object>();
            expectedData["String"] = "retipipiter";
            expectedData["StringArray"] = new [] { "value1", "value2", "value3" };
            expectedData["Long"] = 11L;
            expectedData["LongArray"] = new []{12L, 13L};
            expectedData["LongObject"] = 14L;
            expectedData["LongObjectArray"] = new long?[]{15, null};
            expectedData["Char"] = 'a';
            expectedData["CharArray"] = new []{'b','c'};
            expectedData["Character"] = 'd';
            expectedData["CharacterArray"] = new char?[]{'e','f'};
            expectedData["Double"] = 0D;
            expectedData["DoubleArray"] = new []{0D, 100D};
            expectedData["DoubleObject"] = 0d;
            expectedData["DoubleObjectArray"] = new double?[] { 0D, 100D };
            expectedData["Float"] = 0F;
            expectedData["FloatArray"] = new[] { 0F, 100F };
            expectedData["FloatObject"] = null;
            expectedData["FloatObjectArray"] = new float?[] { 0F, 100F };
            expectedData["Int"] = 0;
            expectedData["IntArray"] = new[] { 0, 100 };
            expectedData["Integer"] = 0;
            expectedData["IntegerArray"] = new int?[] { 0, 100 };
            expectedData["Boolean"] = true;
            expectedData["BooleanArray"] = new[]{true, false};
            expectedData["BooleanObject"] = false;
            expectedData["BooleanObjectArray"] = new bool?[] { true, false };
            expectedData["URI"] = new Uri("http://localhost:8080");            expectedData["URIArray"] = "";
            expectedData["URIArray"] = new[] { new Uri("http://localhost:8080"), new Uri("http://localhost:8443") };
            expectedData["File"] = new FileName("c:\\Users\\Admin");
            expectedData["FileArray"] = new[] {new FileName("c:\\Users\\Admin\\Documents"), new FileName("c:\\Users\\Admin\\Settings")};
            var array = new GuardedByteArray();               
            Encoding.UTF8.GetBytes("array").ToList().ForEach(array.AppendByte);                
            expectedData["GuardedByteArray"] = array;

            array = new GuardedByteArray();               
            Encoding.UTF8.GetBytes("item1").ToList().ForEach(array.AppendByte);                            
            var array2 = new GuardedByteArray();               
            Encoding.UTF8.GetBytes("item2").ToList().ForEach(array2.AppendByte); 
            expectedData["GuardedByteArrayArray"] = new []{array, array2};
            
            var secret = new GuardedString();
            "secret".ToCharArray().ToList().ForEach(secret.AppendChar);                
            expectedData["GuardedString"] = secret;
            
            secret = new GuardedString();
            "secret1".ToCharArray().ToList().ForEach(secret.AppendChar);                
            var secret2 = new GuardedString();
            "secret2".ToCharArray().ToList().ForEach(secret2.AppendChar);                
            
            expectedData["GuardedStringArray"] = new[]{secret, secret2};
            expectedData["Script"] = new ScriptBuilder { ScriptLanguage = "PowerShell", ScriptText = "echo 'Hello OpenICF Developer'" }.Build();              
            expectedData["ScriptArray"] = new[]{new ScriptBuilder { ScriptLanguage = "Groovy", ScriptText = "println 'Hello'" }.Build(),new ScriptBuilder { ScriptLanguage = "Groovy", ScriptText = "println 'OpenICF Developer'" }.Build()};

            Environment.SetEnvironmentVariable(TestHelpers.TestConfigEVName, "converter");

            FieldInfo info = typeof (TestHelpers).GetField("_propertyBags", BindingFlags.NonPublic | BindingFlags.Static);
            (info.GetValue(null) as Dictionary<string, PropertyBag>).Clear();

            PropertyBag propertyBag =
                TestHelpers.GetProperties(typeof(Org.IdentityConnectors.TestConnector.FakeConnector));
            (info.GetValue(null) as Dictionary<string, PropertyBag>).Clear();

            APIConfiguration testable = TestHelpers.CreateTestConfiguration(SafeType<Connector>.Get<Org.IdentityConnectors.TestConnector.FakeConnector>(), propertyBag, null);

            foreach (KeyValuePair<string, object> entry in expectedData)
            {
                Assert.AreEqual(entry.Value, testable.ConfigurationProperties.GetProperty(entry.Key).Value, "Configuration property: " + entry.Key + " has different value");
            }
        }

    }
}

namespace Org.IdentityConnectors.TestConnector
{


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


        //string

        [ConfigurationProperty(Order = 1)]
        public string String { get; set; }

        [ConfigurationProperty(Order = 2)]
        public string[] StringArray { get; set; }


        // long

        [ConfigurationProperty(Order = 3)]
        public long Long { get; set; }

        [ConfigurationProperty(Order = 4)]
        public virtual long[] LongArray { get; set; }


        // Long

        [ConfigurationProperty(Order = 5)]
        public long? LongObject { get; set; }

        [ConfigurationProperty(Order = 6)]
        public long?[] LongObjectArray { get; set; }


        // char

        [ConfigurationProperty(Order = 7)]
        public char Char { get; set; }

        [ConfigurationProperty(Order = 8)]
        public char[] CharArray { get; set; }


        // Character

        [ConfigurationProperty(Order = 9)]
        public char? Character { get; set; }

        [ConfigurationProperty(Order = 10)]
        public char?[] CharacterArray { get; set; }


        // double

        [ConfigurationProperty(Order = 11)]
        public double Double { get; set; }

        [ConfigurationProperty(Order = 12)]
        public double[] DoubleArray { get; set; }


        // Double

        [ConfigurationProperty(Order = 13)]
        public double? DoubleObject { get; set; }

        [ConfigurationProperty(Order = 14)]
        public double?[] DoubleObjectArray { get; set; }


        // float

        [ConfigurationProperty(Order = 15)]
        public float Float { get; set; }

        [ConfigurationProperty(Order = 16)]
        public float[] FloatArray { get; set; }


        // Float

        [ConfigurationProperty(Order = 17)]
        public float? FloatObject { get; set; }

        [ConfigurationProperty(Order = 18)]
        public float?[] FloatObjectArray { get; set; }


        // int

        [ConfigurationProperty(Order = 19)]
        public int Int { get; set; }

        [ConfigurationProperty(Order = 20)]
        public int[] IntArray { get; set; }


        // Integer

        [ConfigurationProperty(Order = 21)]
        public int? Integer { get; set; }

        [ConfigurationProperty(Order = 22)]
        public int?[] IntegerArray { get; set; }


        // boolean

        [ConfigurationProperty(Order = 23)]
        public bool Boolean { get; set; }

        [ConfigurationProperty(Order = 24)]
        public bool[] BooleanArray { get; set; }


        // Boolean

        [ConfigurationProperty(Order = 25)]
        public bool? BooleanObject { get; set; }

        [ConfigurationProperty(Order = 26)]
        public bool?[] BooleanObjectArray { get; set; }


        // URI

        [ConfigurationProperty(Order = 27)]
        public Uri URI { get; set; }

        [ConfigurationProperty(Order = 28)]
        public Uri[] URIArray { get; set; }


        // File

        [ConfigurationProperty(Order = 29)]
        public FileName File { get; set; }

        [ConfigurationProperty(Order = 30)]
        public virtual FileName[] FileArray { get; set; }


        // GuardedByteArray

        [ConfigurationProperty(Order = 31)]
        public GuardedByteArray GuardedByteArray { get; set; }

        [ConfigurationProperty(Order = 32)]
        public GuardedByteArray[] GuardedByteArrayArray { get; set; }


        // GuardedString

        [ConfigurationProperty(Order = 33)]
        public GuardedString GuardedString { get; set; }

        [ConfigurationProperty(Order = 34)]
        public GuardedString[] GuardedStringArray { get; set; }


        // Script

        [ConfigurationProperty(Order = 35)]
        public Script Script { get; set; }

        [ConfigurationProperty(Order = 36)]
        public Script[] ScriptArray { get; set; }
    }

    [ConnectorClass("FakeConnector",
                    "FakeConnector.category",
                    typeof(TestConfiguration)
                        )]
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
