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
 * Portions Copyrighted 2012-2013 ForgeRock AS
 */
using System;
using System.IO;
using System.Text;
using NUnit.Framework;
using System.Collections.Generic;
using System.Globalization;
using System.Security;
using System.Linq;
using System.Xml;
using Org.IdentityConnectors.Framework.Impl.Serializer.Binary;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Impl.Api;
using Org.IdentityConnectors.Framework.Impl.Api.Remote;
using Org.IdentityConnectors.Framework.Impl.Api.Remote.Messages;
using Org.IdentityConnectors.Framework.Impl.Serializer.Xml;
namespace FrameworkTests
{
    [TestFixture]
    public class ObjectSerializationTests
    {
        [Test]
        public void TestBoolean()
        {
            bool v1 = true;
            bool v2 = (bool)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = false;
            v2 = (bool)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }
        [Test]
        public void TestCharacter()
        {
            char v1 = 'h';
            char v2 = (char)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }
        [Test]
        public void TestInteger()
        {
            int v1 = 12345;
            int v2 = (int)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Int32.MinValue;
            v2 = (int)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Int32.MaxValue;
            v2 = (int)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = -1;
            v2 = (int)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestLong()
        {
            long v1 = 12345;
            long v2 = (long)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Int64.MinValue;
            v2 = (long)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Int64.MaxValue;
            v2 = (long)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = -1;
            v2 = (long)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestFloat()
        {
            float v1 = 1.1F;
            float v2 = (float)CloneObject(v1);
            Assert.IsTrue(!Object.ReferenceEquals(v1, v2));
            Assert.AreEqual(v1, v2);

            v1 = Single.Epsilon;
            v2 = (float)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Single.NaN;
            v2 = (float)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Single.NegativeInfinity;
            v2 = (float)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Single.PositiveInfinity;
            v2 = (float)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Single.MinValue;
            v2 = (float)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Single.MaxValue;
            v2 = (float)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestDouble()
        {
            double v1 = 1.1;
            double v2 = (double)CloneObject(v1);
            Assert.IsTrue(!Object.ReferenceEquals(v1, v2));
            Assert.AreEqual(v1, v2);

            v1 = Double.Epsilon;
            v2 = (double)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Double.NaN;
            v2 = (double)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Double.NegativeInfinity;
            v2 = (double)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Double.PositiveInfinity;
            v2 = (double)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Double.MaxValue;
            v2 = (double)CloneObject(v1);
            Assert.AreEqual(v1, v2);

            v1 = Double.MinValue;
            v2 = (double)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestString()
        {
            string v1 = "abcd";
            string v2 = (string)CloneObject(v1);
            Assert.IsTrue(!Object.ReferenceEquals(v1, v2));
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestURI()
        {
            Uri v1 = new Uri("mailto:java-net@java.sun.com");
            Uri v2 = (Uri)CloneObject(v1);
            Assert.IsTrue(!Object.ReferenceEquals(v1, v2));
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestFile()
        {
            FileName v1 = new FileName("c:/foo.txt");
            FileName v2 = (FileName)CloneObject(v1);
            Assert.IsTrue(!Object.ReferenceEquals(v1, v2));
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestBigInteger()
        {
            BigInteger v1 = new BigInteger("983423442347324324324");
            BigInteger v2 = (BigInteger)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestBigDecimal()
        {
            BigDecimal v1 = new BigDecimal(new BigInteger("9847324324324"), 45);
            BigDecimal v2 = (BigDecimal)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestByteArray()
        {
            byte[] v1 = { 1, 2, 3 };
            byte[] v2 = (byte[])CloneObject(v1);
            Assert.AreEqual(3, v2.Length);
            Assert.AreEqual(1, v2[0]);
            Assert.AreEqual(2, v2[1]);
            Assert.AreEqual(3, v2[2]);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestClasses()
        {
            Assert.AreEqual(typeof(bool),
                            CloneObject(typeof(bool)));
            Assert.AreEqual(typeof(bool?),
                            CloneObject(typeof(bool?)));
            Assert.AreEqual(typeof(bool[][]),
                            CloneObject(typeof(bool[][])));
            Assert.AreEqual(typeof(bool?[][]),
                            CloneObject(typeof(bool?[][])));
            Assert.AreEqual(typeof(char),
                            CloneObject(typeof(char)));
            Assert.AreEqual(typeof(char?),
                            CloneObject(typeof(char?)));
            //if this fails, we have added a new type and we need to add
            //a serializer for it
            Assert.IsTrue(
                CollectionUtil.SetsEqual(
                CollectionUtil.NewSet<Type, object>(FrameworkUtil.GetAllSupportedConfigTypes()),
                (ICollection<object>)CloneObject(FrameworkUtil.GetAllSupportedConfigTypes())));
            //if this fails, we have added a new type and we need to add
            //a serializer for it
            Assert.IsTrue(
                CollectionUtil.SetsEqual(
                CollectionUtil.NewSet<Type, object>(FrameworkUtil.GetAllSupportedAttributeTypes()),
                (ICollection<object>)CloneObject(FrameworkUtil.GetAllSupportedAttributeTypes())));
            ICollection<Type> apiOperations =
                new HashSet<Type>();
            foreach (SafeType<APIOperation> op in FrameworkUtil.AllAPIOperations())
            {
                apiOperations.Add(op.RawType);
            }
            //if this fails, need to add to the OperationMappings class
            Assert.IsTrue(
                CollectionUtil.SetsEqual(
                CollectionUtil.NewSet<Type, object>(apiOperations),
                (ICollection<object>)CloneObject(apiOperations)));

        }

        [Test]
        public void TestArrays()
        {
            int[] v1 = { 1, 2, 3 };
            int[] v2 = (int[])CloneObject(v1);
            Assert.AreEqual(3, v2.Length);
            Assert.AreEqual(1, v2[0]);
            Assert.AreEqual(2, v2[1]);
            Assert.AreEqual(3, v2[2]);
        }


        [Test]
        public void TestObjectArrays()
        {
            object[] v1 = { "1", "2", "3" };
            object[] v2 = (object[])CloneObject(v1);
            Assert.AreEqual(3, v2.Length);
            Assert.AreEqual("1", v2[0]);
            Assert.AreEqual("2", v2[1]);
            Assert.AreEqual("3", v2[2]);
        }

        [Test]
        public void TestDictionary()
        {
            IDictionary<object, object> map = new Dictionary<object, object>();
            map["key1"] = "val1";
            map["key2"] = "val2";
            IDictionary<object, object> map2 = (IDictionary<object, object>)CloneObject(map);
            Assert.AreEqual("val1", map["key1"]);
            Assert.AreEqual("val2", map["key2"]);

            IDictionary<string, string> map3 = new Dictionary<string, string>();
            map3["key1"] = "val1";
            map3["key2"] = "val2";
            IDictionary<object, object> map4 = (IDictionary<object, object>)CloneObject(map3);
            Assert.AreEqual("val1", map4["key1"]);
            Assert.AreEqual("val2", map4["key2"]);
        }
        [Test]
        public void TestCaseInsensitiveMap()
        {
            HashSet<ConnectorAttribute> set = new HashSet<ConnectorAttribute>();
            set.Add(ConnectorAttributeBuilder.Build("foo1"));
            set.Add(ConnectorAttributeBuilder.Build("foo2"));
            IDictionary<String, ConnectorAttribute> map = ConnectorAttributeUtil.ToMap(set);
            Assert.IsTrue(map.ContainsKey("Foo1"));
            Assert.IsTrue(map.ContainsKey("Foo2"));
            IDictionary<String, object> map2 = (IDictionary<String, object>)CloneObject(map);
            Assert.IsTrue(map2.ContainsKey("Foo1"));
            Assert.IsTrue(map2.ContainsKey("Foo2"));
        }

        [Test]
        public void TestList()
        {
            IList<Object> v1 = new List<Object>();
            v1.Add("val1");
            v1.Add("val2");
            IList<Object> v2 = (IList<Object>)CloneObject(v1);
            Assert.AreEqual(2, v2.Count);
            Assert.AreEqual("val1", v2[0]);
            Assert.AreEqual("val2", v2[1]);

            IList<string> v3 = new List<string>();
            v3.Add("val1");
            v3.Add("val2");

            IList<object> v4 = (IList<Object>)CloneObject(v3);
            Assert.AreEqual(2, v4.Count);
            Assert.AreEqual("val1", v4[0]);
            Assert.AreEqual("val2", v4[1]);
        }

        [Test]
        public void TestSet()
        {
            //underneath the covers, this creates an
            //ICollection - we need to test that ICollection
            //becomes a Set
            ICollection<Object> v1 =
                CollectionUtil.NewReadOnlySet<object>("val1", "val2");
            HashSet<Object> v2 = (HashSet<Object>)CloneObject(v1);
            Assert.AreEqual(2, v2.Count);
            Assert.IsTrue(v2.Contains("val1"));
            Assert.IsTrue(v2.Contains("val2"));

            HashSet<string> v3 = new HashSet<string>();
            v3.Add("val1");
            v3.Add("val2");

            HashSet<object> v4 = (HashSet<Object>)CloneObject(v3);
            Assert.AreEqual(2, v4.Count);
            Assert.IsTrue(v4.Contains("val1"));
            Assert.IsTrue(v4.Contains("val2"));
        }

        [Test]
        public void TestCaseInsensitiveSet()
        {
            ICollection<String> v1 = CollectionUtil.NewCaseInsensitiveSet();
            v1.Add("foo");
            v1.Add("foo2");
            ICollection<String> v2 = (ICollection<String>)CloneObject(v1);
            Assert.IsTrue(v2.Contains("Foo"));
            Assert.IsTrue(v2.Contains("Foo2"));
        }

        [Test]
        public void TestCultureInfo()
        {
            //use this one since it uses all 3 fields of locale
            CultureInfo v1 = new CultureInfo("nn-NO");
            CultureInfo v2 = (CultureInfo)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestObjectPoolConfiguration()
        {
            ObjectPoolConfiguration v1 = new ObjectPoolConfiguration();
            v1.MaxObjects = 1;
            v1.MaxIdle = 2;
            v1.MaxWait = 3;
            v1.MinEvictableIdleTimeMillis = 4;
            v1.MinIdle = 5;
            ObjectPoolConfiguration v2 =
                (ObjectPoolConfiguration)CloneObject(v1);
            Assert.IsTrue(!Object.ReferenceEquals(v1, v2));

            Assert.AreEqual(v1, v2);

            Assert.AreEqual(1, v2.MaxObjects);
            Assert.AreEqual(2, v2.MaxIdle);
            Assert.AreEqual(3, v2.MaxWait);
            Assert.AreEqual(4, v2.MinEvictableIdleTimeMillis);
            Assert.AreEqual(5, v2.MinIdle);
        }
        [Test]
        public void TestConfigurationProperty()
        {
            ConfigurationPropertyImpl v1 = new ConfigurationPropertyImpl();
            v1.Order = (1);
            v1.IsConfidential = (true);
            v1.IsRequired = true;
            v1.Name = ("foo");
            v1.HelpMessageKey = ("help key");
            v1.DisplayMessageKey = ("display key");
            v1.GroupMessageKey = ("group key");
            v1.Value = ("bar");
            v1.ValueType = (typeof(string));
            v1.Operations = FrameworkUtil.AllAPIOperations();

            ConfigurationPropertyImpl v2 = (ConfigurationPropertyImpl)
                CloneObject(v1);
            Assert.AreEqual(1, v2.Order);
            Assert.IsTrue(v2.IsConfidential);
            Assert.IsTrue(v2.IsRequired);
            Assert.AreEqual("foo", v2.Name);
            Assert.AreEqual("help key", v2.HelpMessageKey);
            Assert.AreEqual("display key", v2.DisplayMessageKey);
            Assert.AreEqual("group key", v2.GroupMessageKey);
            Assert.AreEqual("bar", v2.Value);
            Assert.AreEqual(typeof(string), v2.ValueType);
            Assert.IsTrue(CollectionUtil.Equals(
                FrameworkUtil.AllAPIOperations(), v2.Operations));
        }

        [Test]
        public void TestConfigurationProperties()
        {
            ConfigurationPropertyImpl prop1 = new ConfigurationPropertyImpl();
            prop1.Order = (1);
            prop1.IsConfidential = (true);
            prop1.Name = ("foo");
            prop1.HelpMessageKey = ("help key");
            prop1.DisplayMessageKey = ("display key");
            prop1.GroupMessageKey = ("group key");
            prop1.Value = ("bar");
            prop1.ValueType = (typeof(string));
            prop1.Operations = null;

            ConfigurationPropertiesImpl v1 = new ConfigurationPropertiesImpl();
            v1.Properties = (CollectionUtil.NewReadOnlyList<ConfigurationPropertyImpl>(prop1));
            v1.SetPropertyValue("foo", "bar");

            ConfigurationPropertiesImpl v2 = (ConfigurationPropertiesImpl)
                CloneObject(v1);
            Assert.AreEqual("bar", v2.GetProperty("foo").Value);
        }

        [Test]
        public void TestAPIConfiguration()
        {
            ConfigurationPropertyImpl prop1 = new ConfigurationPropertyImpl();
            prop1.Order = (1);
            prop1.IsConfidential = (true);
            prop1.Name = ("foo");
            prop1.HelpMessageKey = ("help key");
            prop1.DisplayMessageKey = ("display key");
            prop1.GroupMessageKey = ("group key");
            prop1.Value = ("bar");
            prop1.ValueType = (typeof(string));
            prop1.Operations = null;

            ConfigurationPropertiesImpl props1 = new ConfigurationPropertiesImpl();
            props1.Properties = (CollectionUtil.NewReadOnlyList<ConfigurationPropertyImpl>(prop1));

            APIConfigurationImpl v1 = new APIConfigurationImpl();
            v1.ConnectorPoolConfiguration = (new ObjectPoolConfiguration());
            v1.ConfigurationProperties = (props1);
            v1.IsConnectorPoolingSupported = (true);
            v1.ProducerBufferSize = (200);
            v1.SupportedOperations = (FrameworkUtil.AllAPIOperations());
            IDictionary<SafeType<APIOperation>, int> map =
                CollectionUtil.NewDictionary<SafeType<APIOperation>, int>(SafeType<APIOperation>.Get<CreateApiOp>(), 6);
            v1.TimeoutMap = (map);

            APIConfigurationImpl v2 = (APIConfigurationImpl)
                CloneObject(v1);
            Assert.IsTrue(!Object.ReferenceEquals(v1, v2));
            Assert.IsNotNull(v2.ConnectorPoolConfiguration);
            Assert.IsNotNull(v2.ConfigurationProperties);
            Assert.AreEqual(v1.ConnectorPoolConfiguration, v2.ConnectorPoolConfiguration);
            Assert.AreEqual(v1.ConfigurationProperties, v2.ConfigurationProperties);
            Assert.IsTrue(v2.IsConnectorPoolingSupported);
            Assert.AreEqual(200, v2.ProducerBufferSize);
            Assert.IsTrue(CollectionUtil.SetsEqual(
                FrameworkUtil.AllAPIOperations(),
                v2.SupportedOperations));
            Assert.AreEqual(map, v2.TimeoutMap);
        }

        [Test]
        public void TestConnectorMessages()
        {
            ConnectorMessagesImpl v1 = new ConnectorMessagesImpl();
            IDictionary<String, String> defaultMap = new Dictionary<String, String>();
            defaultMap["key1"] = "val1";
            IDictionary<CultureInfo, IDictionary<String, String>> messages =
                new Dictionary<CultureInfo, IDictionary<String, String>>();
            messages[new CultureInfo("en")] = defaultMap;
            messages[new CultureInfo("")] = defaultMap;
            v1.Catalogs = (messages);

            ConnectorMessagesImpl v2 = (ConnectorMessagesImpl)
                CloneObject(v1);
            Assert.IsTrue(
                CollectionUtil.SetsEqual(messages[new CultureInfo("")],
                                         v2.Catalogs[new CultureInfo("")]));
            Assert.IsTrue(
                CollectionUtil.SetsEqual(messages[new CultureInfo("en")],
                                         v2.Catalogs[new CultureInfo("en")]));
        }

        [Test]
        public void TestRemoteConnectorInfo()
        {
            RemoteConnectorInfoImpl v1 = new RemoteConnectorInfoImpl();
            v1.Messages = (new ConnectorMessagesImpl());
            v1.ConnectorKey = (new ConnectorKey("my bundle",
                "my version",
            "my connector"));
            ConfigurationPropertiesImpl configProperties = new ConfigurationPropertiesImpl();
            configProperties.Properties = (new List<ConfigurationPropertyImpl>());
            APIConfigurationImpl apiImpl = new APIConfigurationImpl();
            apiImpl.ConfigurationProperties = (configProperties);
            v1.DefaultAPIConfiguration = (apiImpl);
            v1.ConnectorDisplayNameKey = ("mykey");
            v1.ConnectorCategoryKey = ("LDAP");

            RemoteConnectorInfoImpl v2 = (RemoteConnectorInfoImpl)
                CloneObject(v1);

            Assert.IsNotNull(v2.Messages);
            Assert.AreEqual("my bundle", v2.ConnectorKey.BundleName);
            Assert.AreEqual("my version", v2.ConnectorKey.BundleVersion);
            Assert.AreEqual("my connector", v2.ConnectorKey.ConnectorName);
            Assert.AreEqual("mykey", v2.ConnectorDisplayNameKey);
            Assert.AreEqual("LDAP", v2.ConnectorCategoryKey);
            Assert.IsNotNull(v2.DefaultAPIConfiguration);
        }

        [Test]
        public void TestAttribute()
        {
            ConnectorAttribute v1 = ConnectorAttributeBuilder.Build("foo", "val1", "val2");
            ConnectorAttribute v2 = (ConnectorAttribute)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestAttributeInfo()
        {

            ConnectorAttributeInfoBuilder builder = new ConnectorAttributeInfoBuilder();
            builder.Name = ("foo");
            builder.ValueType = (typeof(String));
            builder.Required = (true);
            builder.Readable = (true);
            builder.Creatable = (true);
            builder.Updateable = (true);
            builder.MultiValued = (true);
            builder.ReturnedByDefault = false;
            ConnectorAttributeInfo v1 = builder.Build();
            ConnectorAttributeInfo v2 = (ConnectorAttributeInfo)CloneObject(v1);
            Assert.AreEqual(v1, v2);
            Assert.AreEqual("foo", v2.Name);
            Assert.AreEqual(typeof(String), v2.ValueType);
            Assert.IsTrue(v2.IsMultiValued);
            Assert.IsTrue(v2.IsReadable);
            Assert.IsTrue(v2.IsRequired);
            Assert.IsTrue(v2.IsUpdateable);
            Assert.IsTrue(v2.IsCreatable);
            Assert.IsFalse(v2.IsReturnedByDefault);

            builder.InfoFlags = AllFlags();
            v1 = builder.Build();
            v2 = (ConnectorAttributeInfo)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        private ConnectorAttributeInfo.Flags AllFlags()
        {
            ConnectorAttributeInfo.Flags flags =
                ConnectorAttributeInfo.Flags.NONE;
            foreach (Enum e in Enum.GetValues(typeof(ConnectorAttributeInfo.Flags)))
            {
                ConnectorAttributeInfo.Flags f =
                    (ConnectorAttributeInfo.Flags)e;
                flags |= f;
            }
            return flags;
        }

        [Test]
        public void TestConnectorObject()
        {
            ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
            bld.SetUid("foo");
            bld.SetName("fooToo");

            ConnectorObject v1 = bld.Build();
            ConnectorObject v2 = (ConnectorObject)CloneObject(v1);

            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestName()
        {
            Name v1 = new Name("Test");
            Name v2 = (Name)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestObjectClass()
        {
            ObjectClass v1 = new ObjectClass("test");
            ObjectClass v2 = (ObjectClass)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestObjectClassInfo()
        {
            ConnectorAttributeInfoBuilder builder = new ConnectorAttributeInfoBuilder();
            builder.Name = ("foo");
            builder.ValueType = (typeof(String));
            builder.Required = (true);
            builder.Readable = (true);
            builder.Updateable = (true);
            builder.MultiValued = (true);
            ObjectClassInfoBuilder obld = new ObjectClassInfoBuilder();
            obld.ObjectType = ObjectClass.ACCOUNT_NAME;
            obld.IsContainer = true;
            obld.AddAttributeInfo(builder.Build());
            ObjectClassInfo v1 = obld.Build();
            ObjectClassInfo v2 = (ObjectClassInfo)CloneObject(v1);
            Assert.AreEqual(v1, v2);
            Assert.IsTrue(v2.IsContainer);
        }

        [Test]
        public void TestUid()
        {
            Uid v1 = new Uid("test");
            Uid v2 = (Uid)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void testOperationOptionInfo()
        {
            OperationOptionInfo v1 =
                new OperationOptionInfo("name", typeof(int?));
            OperationOptionInfo v2 =
                (OperationOptionInfo)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestSchema()
        {
            OperationOptionInfo opInfo =
                new OperationOptionInfo("name", typeof(int?));
            ObjectClassInfoBuilder bld = new ObjectClassInfoBuilder();
            bld.ObjectType = ObjectClass.ACCOUNT_NAME;
            ObjectClassInfo info = bld.Build();
            ICollection<ObjectClassInfo> temp = CollectionUtil.NewSet(info);
            IDictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>> map =
                new Dictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>>();
            map[SafeType<APIOperation>.Get<CreateApiOp>()] = temp;
            ICollection<OperationOptionInfo> temp2 = CollectionUtil.NewSet(opInfo);
            IDictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>> map2 =
                new Dictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>>();
            map2[SafeType<APIOperation>.Get<CreateApiOp>()] = temp2;
            Schema v1 = new Schema(CollectionUtil.NewSet(info),
                    CollectionUtil.NewSet(opInfo),
                    map,
                    map2);
            Schema v2 = (Schema)CloneObject(v1);
            Assert.AreEqual(v1, v2);
            Assert.AreEqual(info, v2.ObjectClassInfo.First());
            Assert.AreEqual(1, v2.SupportedObjectClassesByOperation.Count);
            Assert.AreEqual(1, v2.SupportedOptionsByOperation.Count);
            Assert.AreEqual(1, v2.OperationOptionInfo.Count);
        }

        [Test]
        public void TestContainsFilter()
        {
            ContainsFilter v1 = new ContainsFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            ContainsFilter v2 = (ContainsFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestAndFilter()
        {
            ContainsFilter left1 = new ContainsFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            ContainsFilter right1 = new ContainsFilter(ConnectorAttributeBuilder.Build("foo2", "bar2"));
            AndFilter v1 = new AndFilter(left1, right1);
            AndFilter v2 = (AndFilter)CloneObject(v1);
            ContainsFilter left2 = (ContainsFilter)v2.Left;
            ContainsFilter right2 = (ContainsFilter)v2.Right;
            Assert.AreEqual(left1.GetAttribute(), left2.GetAttribute());
            Assert.AreEqual(right1.GetAttribute(), right2.GetAttribute());
        }

        [Test]
        public void TestEndsWithFilter()
        {
            EndsWithFilter v1 = new EndsWithFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            EndsWithFilter v2 = (EndsWithFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestEqualsFilter()
        {
            EqualsFilter v1 = new EqualsFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            EqualsFilter v2 = (EqualsFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestGreaterThanFilter()
        {
            GreaterThanFilter v1 = new GreaterThanFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            GreaterThanFilter v2 = (GreaterThanFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestGreaterThanOrEqualFilter()
        {
            GreaterThanOrEqualFilter v1 = new GreaterThanOrEqualFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            GreaterThanOrEqualFilter v2 = (GreaterThanOrEqualFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestLessThanFilter()
        {
            LessThanFilter v1 = new LessThanFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            LessThanFilter v2 = (LessThanFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestLessThanOrEqualFilter()
        {
            LessThanOrEqualFilter v1 = new LessThanOrEqualFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            LessThanOrEqualFilter v2 = (LessThanOrEqualFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestNotFilter()
        {
            ContainsFilter left1 = new ContainsFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            NotFilter v1 = new NotFilter(left1);
            NotFilter v2 = (NotFilter)CloneObject(v1);
            ContainsFilter left2 = (ContainsFilter)v2.Filter;
            Assert.AreEqual(left1.GetAttribute(), left2.GetAttribute());
        }

        [Test]
        public void TestOrFilter()
        {
            ContainsFilter left1 = new ContainsFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            ContainsFilter right1 = new ContainsFilter(ConnectorAttributeBuilder.Build("foo2", "bar2"));
            OrFilter v1 = new OrFilter(left1, right1);
            OrFilter v2 = (OrFilter)CloneObject(v1);
            ContainsFilter left2 = (ContainsFilter)v2.Left;
            ContainsFilter right2 = (ContainsFilter)v2.Right;
            Assert.AreEqual(left1.GetAttribute(), left2.GetAttribute());
            Assert.AreEqual(right1.GetAttribute(), right2.GetAttribute());
        }

        [Test]
        public void TestStartsWithFilter()
        {
            StartsWithFilter v1 = new StartsWithFilter(ConnectorAttributeBuilder.Build("foo", "bar"));
            StartsWithFilter v2 = (StartsWithFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestContainsAllValuesFilter()
        {
            ContainsAllValuesFilter v1 = new ContainsAllValuesFilter(ConnectorAttributeBuilder.Build("foo", "bar", "foo"));
            ContainsAllValuesFilter v2 = (ContainsAllValuesFilter)CloneObject(v1);
            Assert.AreEqual(v1.GetAttribute(), v2.GetAttribute());
        }

        [Test]
        public void TestExceptions()
        {
            {
                AlreadyExistsException v1 = new AlreadyExistsException("ex");
                AlreadyExistsException v2 = (AlreadyExistsException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                ConfigurationException v1 = new ConfigurationException("ex");
                ConfigurationException v2 = (ConfigurationException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                ConnectionBrokenException v1 = new ConnectionBrokenException("ex");
                ConnectionBrokenException v2 = (ConnectionBrokenException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                ConnectionFailedException v1 = new ConnectionFailedException("ex");
                ConnectionFailedException v2 = (ConnectionFailedException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                ConnectorException v1 = new ConnectorException("ex");
                ConnectorException v2 = (ConnectorException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }
            {
                ConnectorIOException v1 = new ConnectorIOException("ex");
                ConnectorIOException v2 = (ConnectorIOException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }
            {
                ConnectorSecurityException v1 = new ConnectorSecurityException("ex");
                ConnectorSecurityException v2 = (ConnectorSecurityException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                InvalidCredentialException v1 = new InvalidCredentialException("ex");
                InvalidCredentialException v2 = (InvalidCredentialException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                InvalidPasswordException v1 = new InvalidPasswordException("ex");
                InvalidPasswordException v2 = (InvalidPasswordException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                PasswordExpiredException v1 = new PasswordExpiredException("ex");
                v1.Uid = (new Uid("myuid"));
                PasswordExpiredException v2 = (PasswordExpiredException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
                Assert.AreEqual("myuid", v2.Uid.GetUidValue());
            }

            {
                OperationTimeoutException v1 = new OperationTimeoutException("ex");
                OperationTimeoutException v2 = (OperationTimeoutException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                PermissionDeniedException v1 = new PermissionDeniedException("ex");
                PermissionDeniedException v2 = (PermissionDeniedException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                UnknownUidException v1 = new UnknownUidException("ex");
                UnknownUidException v2 = (UnknownUidException)CloneObject(v1);
                Assert.AreEqual("ex", v2.Message);
            }

            {
                ArgumentException v1 = new ArgumentException("my msg");
                ArgumentException v2 = (ArgumentException)CloneObject(v1);
                Assert.AreEqual("my msg", v2.Message);
            }

            {
                ArgumentNullException v1 = new ArgumentNullException(null, "my msg 1");
                ArgumentException v2 = (ArgumentException)CloneObject(v1);
                Assert.AreEqual("my msg 1", v2.Message);
            }

            {
                Exception v1 = new Exception("my msg2");
                Exception v2 = (Exception)CloneObject(v1);
                Assert.AreEqual("my msg2", v2.Message);
            }

        }

        [Test]
        public void TestHelloRequest()
        {
            HelloRequest v1 = new HelloRequest(HelloRequest.CONNECTOR_INFO);
            HelloRequest v2 = (HelloRequest)CloneObject(v1);
            Assert.IsNotNull(v2);
            Assert.AreEqual(HelloRequest.CONNECTOR_INFO, v2.GetInfoLevel());
        }

        [Test]
        public void TestHelloResponse()
        {
            Exception ex = new Exception("foo");
            IDictionary<string,object> serverInfo = new Dictionary<string, object>(1);
            serverInfo.Add(HelloResponse.SERVER_START_TIME, DateTimeUtil.GetCurrentUtcTimeMillis());
 	 	 	ConnectorKey key = new ConnectorKey("my bundle", "my version", "my connector");
            RemoteConnectorInfoImpl info = new RemoteConnectorInfoImpl();
            info.Messages = (new ConnectorMessagesImpl());
            info.ConnectorKey = (key);
            ConfigurationPropertiesImpl configProperties = new ConfigurationPropertiesImpl();
            configProperties.Properties = (new List<ConfigurationPropertyImpl>());
            APIConfigurationImpl apiImpl = new APIConfigurationImpl();
            apiImpl.ConfigurationProperties = (configProperties);
            info.DefaultAPIConfiguration = (apiImpl);
            info.ConnectorDisplayNameKey = ("mykey");
            info.ConnectorCategoryKey = ("");


            HelloResponse v1 = new HelloResponse(ex, serverInfo, CollectionUtil.NewReadOnlyList<ConnectorKey>(key), CollectionUtil.NewReadOnlyList<RemoteConnectorInfoImpl>(info));
            HelloResponse v2 = (HelloResponse)CloneObject(v1);
            Assert.IsNotNull(v2.Exception);
            Assert.IsNotNull(v2.ServerInfo[HelloResponse.SERVER_START_TIME]);
            Assert.IsNotNull(v2.ConnectorKeys.First());
            Assert.IsNotNull(v2.ConnectorInfos.First());
        }

        [Test]
        public void TestOperationRequest()
        {
            ConfigurationPropertiesImpl configProperties = new ConfigurationPropertiesImpl();
            configProperties.Properties = (new List<ConfigurationPropertyImpl>());
            APIConfigurationImpl apiImpl = new APIConfigurationImpl();
            apiImpl.ConfigurationProperties = (configProperties);

            IList<object> args = new List<object>();
            args.Add("my arg");
            OperationRequest v1 = new
                OperationRequest(new ConnectorKey("my bundle",
                    "my version",
                "my connector"),
                    apiImpl,
                    SafeType<APIOperation>.Get<CreateApiOp>(),
                    "mymethodName",
                    args);
            OperationRequest v2 = (OperationRequest)CloneObject(v1);
            Assert.AreEqual("my bundle", v2.ConnectorKey.BundleName);
            Assert.AreEqual("my version", v2.ConnectorKey.BundleVersion);
            Assert.AreEqual("my connector", v2.ConnectorKey.ConnectorName);
            Assert.IsNotNull(v2.Configuration);
            Assert.AreEqual(SafeType<APIOperation>.Get<CreateApiOp>(), v2.Operation);
            Assert.AreEqual("mymethodName", v2.OperationMethodName);
            Assert.IsTrue(
                CollectionUtil.Equals(
                    args, v2.Arguments));
        }

        [Test]
        public void TestOperationResponseEnd()
        {
            OperationResponseEnd v1 = new OperationResponseEnd();
            OperationResponseEnd v2 = (OperationResponseEnd)CloneObject(v1);
            Assert.IsNotNull(v2);
        }
        [Test]
        public void TestOperationResponsePart()
        {
            Exception ex = new Exception("foo");
            OperationResponsePart v1 = new OperationResponsePart(ex, "bar");
            OperationResponsePart v2 = (OperationResponsePart)CloneObject(v1);
            Assert.IsNotNull(v2.Exception);
            Assert.AreEqual("bar", v2.Result);
        }

        [Test]
        public void TestOperationResponsePause()
        {
            OperationResponsePause v1 = new OperationResponsePause();
            OperationResponsePause v2 = (OperationResponsePause)CloneObject(v1);
            Assert.IsNotNull(v2);
        }

        [Test]
        public void TestOperationRequestMoreData()
        {
            OperationRequestMoreData v1 = new OperationRequestMoreData();
            OperationRequestMoreData v2 = (OperationRequestMoreData)CloneObject(v1);
            Assert.IsNotNull(v2);
        }

        [Test]
        public void TestOperationRequestStopData()
        {
            OperationRequestStopData v1 = new OperationRequestStopData();
            OperationRequestStopData v2 = (OperationRequestStopData)CloneObject(v1);
            Assert.IsNotNull(v2);
        }

        [Test]
        public void TestEchoMessage()
        {
            EchoMessage v1 = new EchoMessage("test", "xml");
            EchoMessage v2 = (EchoMessage)CloneObject(v1);
            Assert.AreEqual("test", v2.Object);
            Assert.AreEqual("xml", v2.ObjectXml);
        }

        [Test]
        public void TestOperationOptions()
        {
            OperationOptionsBuilder builder = new OperationOptionsBuilder();
            builder.SetOption("foo", "bar");
            builder.SetOption("foo2", "bar2");
            OperationOptions v1 = builder.Build();
            OperationOptions v2 = (OperationOptions)CloneObject(v1);
            Assert.AreEqual(2, v2.Options.Count);
            Assert.AreEqual("bar", v2.Options["foo"]);
            Assert.AreEqual("bar2", v2.Options["foo2"]);
        }

        [Test]
        public void TestScript()
        {
            ScriptBuilder builder = new ScriptBuilder();
            builder.ScriptLanguage = "language";
            builder.ScriptText = "text";
            Script v1 = builder.Build();
            Script v2 = (Script)CloneObject(v1);
            Assert.AreEqual("language", v2.ScriptLanguage);
            Assert.AreEqual("text", v2.ScriptText);
        }

        [Test]
        public void TestScriptContext()
        {
            ScriptContextBuilder builder = new ScriptContextBuilder();
            builder.ScriptLanguage = ("language");
            builder.ScriptText = ("text");
            builder.AddScriptArgument("foo", "bar");
            builder.AddScriptArgument("foo2", "bar2");
            ScriptContext v1 = builder.Build();
            ScriptContext v2 = (ScriptContext)CloneObject(v1);
            Assert.AreEqual(2, v2.ScriptArguments.Count);
            Assert.AreEqual("bar", v2.ScriptArguments["foo"]);
            Assert.AreEqual("bar2", v2.ScriptArguments["foo2"]);
            Assert.AreEqual("language", v2.ScriptLanguage);
            Assert.AreEqual("text", v2.ScriptText);
        }
        [Test]
        public void TestSyncDeltaType()
        {
            SyncDeltaType v1 = SyncDeltaType.DELETE;
            SyncDeltaType v2 = (SyncDeltaType)CloneObject(v1);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestSyncToken()
        {
            SyncToken v1 = new SyncToken("mytoken");
            SyncToken v2 = (SyncToken)CloneObject(v1);
            Assert.AreEqual(v1.Value, v2.Value);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestSyncDelta()
        {
            ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
            bld.SetUid("foo");
            bld.SetName("name");
            SyncDeltaBuilder builder = new SyncDeltaBuilder();
            builder.PreviousUid = (new Uid("mypreviousuid"));
            builder.Uid = (new Uid("myuid"));
            builder.DeltaType = (SyncDeltaType.CREATE_OR_UPDATE);
            builder.Token = (new SyncToken("mytoken"));
            builder.Object = (bld.Build());
            SyncDelta v1 = builder.Build();
            SyncDelta v2 = (SyncDelta)CloneObject(v1);
            Assert.AreEqual(new Uid("mypreviousuid"), v2.PreviousUid);
            Assert.AreEqual(new Uid("foo"), v2.Uid);
            Assert.AreEqual(new SyncToken("mytoken"), v2.Token);
            Assert.AreEqual(SyncDeltaType.CREATE_OR_UPDATE, v2.DeltaType);
            Assert.AreEqual(v1, v2);
        }

        [Test]
        public void TestNull()
        {
            Object v1 = null;
            Object v2 = CloneObject(v1);
            Assert.IsNull(v2);
        }

        [Test]
        public void TestGuardedByteArray()
        {
            GuardedByteArray v1 = new GuardedByteArray();
            v1.AppendByte(0x00);
            v1.AppendByte(0x01);
            v1.AppendByte(0x02);
            GuardedByteArray v2 = (GuardedByteArray)CloneObject(v1);
            Assert.AreEqual(new byte[] { 0x00, 0x01, 0x02 }, DecryptToByteArray(v2));
        }

        [Test]
        public void TestGuardedString()
        {
            GuardedString v1 = new GuardedString();
            v1.AppendChar('f');
            v1.AppendChar('o');
            v1.AppendChar('o');
            v1.AppendChar('b');
            v1.AppendChar('a');
            v1.AppendChar('r');
            GuardedString v2 = (GuardedString)CloneObject(v1);
            Assert.AreEqual("foobar", DecryptToString(v2));
        }

        [Test]
        public void TestQualifiedUid()
        {
            QualifiedUid v1 = new QualifiedUid(new ObjectClass("myclass"),
                    new Uid("myuid"));
            QualifiedUid v2 = (QualifiedUid)CloneObject(v1);
            Assert.AreEqual(v1, v2);
            Assert.AreEqual("myclass", v2.ObjectClass.GetObjectClassValue());
            Assert.AreEqual("myuid", v2.Uid.GetUidValue());
        }

        /// <summary>
        /// Highly insecure method! Do not do this in production
        /// code.
        /// </summary>
        /// <remarks>
        /// This is only for test purposes
        /// </remarks>
        private String DecryptToString(GuardedString str)
        {
            StringBuilder buf = new StringBuilder();
            str.Access(
                                            array =>
                                            {
                                                for (int i = 0; i < array.Length; i++)
                                                {
                                                    buf.Append(array[i]);
                                                }
                                            });
            return buf.ToString();
        }

        private byte[] DecryptToByteArray(GuardedByteArray bytes)
        {
            byte[] result = null;
            bytes.Access(
                                            array =>
                                            {
                                                result = new byte[array.Length];
                                                for (int i = 0; i < array.Length; i++)
                                                {
                                                    result[i] = array[i];
                                                }
                                            });
            return result;
        }

        protected virtual Object CloneObject(Object o)
        {
            return SerializerUtil.CloneObject(o);
        }
    }
    [TestFixture]
    public class XmlSerializationTests : ObjectSerializationTests
    {
        protected override Object CloneObject(Object o)
        {
            String xml = SerializerUtil.SerializeXmlObject(o, true);
            Console.WriteLine(xml);
            o = SerializerUtil.DeserializeXmlObject(xml, true);

            //pass through a list to make sure dtd correctly defines all xml objects
            List<Object> list = new List<Object>();
            list.Add(o);
            xml = SerializerUtil.SerializeXmlObject(list, true);
            Console.WriteLine(xml);
            IList<Object> rv = (IList<Object>)SerializerUtil.DeserializeXmlObject(xml, true);
            return rv[0];
        }

        [Test]
        public void TestMultiObject()
        {
            ObjectSerializerFactory factory = ObjectSerializerFactory.GetInstance();
            StringWriter sw = new StringWriter();
            XmlObjectSerializer ser = factory.NewXmlSerializer(sw, true, true);
            ser.WriteObject("foo");
            ser.WriteObject("bar");
            ser.Close(true);
            String xml = sw.ToString();
            Console.WriteLine(xml);
            IList<Object> results = new List<Object>();
            factory.DeserializeXmlStream(new StringReader(xml),
                    o =>
                    {
                        results.Add(o);
                        return true;
                    },
                    true);
            Assert.AreEqual(2, results.Count);
            Assert.AreEqual("foo", results[0]);
            Assert.AreEqual("bar", results[1]);
        }

        [Test]
        public void TestWriter()
        {
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Indent = true;
            settings.OmitXmlDeclaration = true;
            XmlWriter writer = XmlWriter.Create(Console.Out, settings);

            // Write the book element.
            writer.WriteStartElement("book");
            writer.WriteEndElement();
            writer.Close();
            // Write the title element.
            //writer.WriteStartElement("title");
            //writer.WriteString("Pride And Prejudice<<");
            //writer.WriteEndElement();

            // Write the close tag for the root element.
            //writer.WriteEndElement();

            // Write the XML and close the writer.
            //writer.Close();  

        }
    }
}