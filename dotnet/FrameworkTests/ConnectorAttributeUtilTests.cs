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
using System.Linq;
using System.Security;
using NUnit.Framework;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Common.Objects;

namespace FrameworkTests
{
    #region ConnectorAttributesAccessorTest
    [TestFixture]
    public class ConnectorAttributesAccessorTest
    {
        [Test]
        public virtual void TestGetUid()
        {
            Assert.AreEqual(Testable.GetUid(), new Uid("UID001"));
        }

        [Test]
        public virtual void TestGetName()
        {
            Assert.AreEqual(Testable.GetName(), new Name("NAME001"));
        }

        [Test]
        public virtual void TestGetEnabled()
        {
            Assert.IsFalse(Testable.GetEnabled(true));
        }

        [Test]
        public virtual void testGetPassword()
        {
            SecureString secret = new SecureString();
            "Passw0rd".ToCharArray().ToList().ForEach(secret.AppendChar);
            Assert.AreEqual(new GuardedString(secret), Testable.GetPassword());
        }

        [Test]
        public virtual void TestFindList()
        {
            Assert.AreEqual(Testable.FindList("attributeFloatMultivalue"), CollectionUtil.NewList(new float?[3] { 24F, 25F, null }));
        }

        [Test]
        public virtual void TestListAttributeNames()
        {
            Assert.IsTrue(Testable.ListAttributeNames().Contains("attributeboolean"));
        }

        [Test]
        public virtual void TestHasAttribute()
        {
            Assert.IsTrue(Testable.HasAttribute("attributeboolean"));
        }

        [Test]
        public virtual void TestFindString()
        {
            Assert.AreEqual(Testable.FindString("attributeString"), "retipipiter");
        }

        [Test]
        public virtual void TestFindStringList()
        {
            Assert.AreEqual(Testable.FindStringList("attributeStringMultivalue"), CollectionUtil.NewList(new String[2] { "value1", "value2" }));
        }

        [Test]
        public virtual void TestFindCharacter()
        {
            Assert.AreEqual((char)Testable.FindCharacter("attributechar"), 'a');
            Assert.AreEqual((char)Testable.FindCharacter("attributecharacter"), 'd');
        }

        [Test]
        public virtual void TestFindInteger()
        {
            Assert.AreEqual((int)Testable.FindInteger("attributeint"), 26);
            Assert.AreEqual((int)Testable.FindInteger("attributeinteger"), 29);
        }

        [Test]
        public virtual void TestFindLong()
        {
            Assert.AreEqual((long)Testable.FindLong("attributelongp"), 11L);
            Assert.AreEqual((long)Testable.FindLong("attributelong"), 14L);
        }

        [Test]
        public virtual void TestFindDouble()
        {
            Assert.AreEqual(Testable.FindDouble("attributedoublep"), double.MinValue);
            Assert.AreEqual(Testable.FindDouble("attributedouble"), 17D);
        }

        [Test]
        public virtual void TestFindFloat()
        {
            Assert.AreEqual(Testable.FindFloat("attributefloatp"), 20F);
            Assert.AreEqual(Testable.FindFloat("attributefloat"), 23F);
        }

        [Test]
        public virtual void TestFindBoolean()
        {
            var findBoolean = Testable.FindBoolean("attributebooleanp");
            Assert.IsTrue(findBoolean != null && (bool)findBoolean);
            var boolean = Testable.FindBoolean("attributeboolean");
            Assert.IsFalse(boolean != null && (bool)boolean);
        }

        [Test]
        public virtual void TestFindByte()
        {
            Assert.AreEqual((byte)Testable.FindByte("attributebytep"), (byte)48);
            Assert.AreEqual((byte)Testable.FindByte("attributebyte"), (byte)51);
        }

        [Test]
        public virtual void TestFindByteArray()
        {
            Assert.AreEqual(Testable.FindByteArray("attributeByteArray"), System.Text.Encoding.UTF8.GetBytes("array"));
        }

        [Test]
        public virtual void TestFindBigDecimal()
        {
            Assert.AreEqual(Testable.FindBigDecimal("attributeBigDecimal"), new BigDecimal(new BigInteger("1"), 0));
        }

        [Test]
        public virtual void FestFindBigInteger()
        {
            Assert.AreEqual(Testable.FindBigInteger("attributeBigInteger"), new BigInteger("1"));
        }

        [Test]
        public virtual void TestFindGuardedByteArray()
        {
            var expected = new GuardedByteArray();
            System.Text.Encoding.UTF8.GetBytes("array").ToList().ForEach(expected.AppendByte);
            Assert.AreEqual(expected, Testable.FindGuardedByteArray("attributeGuardedByteArray"));
        }

        [Test]
        public virtual void TestFindGuardedString()
        {
            SecureString secret = new SecureString();
            "secret".ToCharArray().ToList().ForEach(secret.AppendChar);
            Assert.AreEqual(new GuardedString(secret), Testable.FindGuardedString("attributeGuardedString"));
        }

        [Test]
        public void TestFindDictionary()
        {
            Assert.AreEqual(SampleMap,Testable.FindDictionary("attributeMap"));
        }


        private ConnectorAttributesAccessor Testable
        {
            get
            {
                ICollection<ConnectorAttribute> attributes = new HashSet<ConnectorAttribute>();
                attributes.Add(new Uid("UID001"));
                attributes.Add(new Name("NAME001"));
                attributes.Add(ConnectorAttributeBuilder.BuildEnabled(false));
                SecureString password = new SecureString();
                "Passw0rd".ToCharArray().ToList().ForEach(p => password.AppendChar(p));
                attributes.Add(ConnectorAttributeBuilder.BuildPassword(password));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeString", "retipipiter"));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeStringMultivalue", "value1", "value2"));

                attributes.Add(ConnectorAttributeBuilder.Build("attributelongp", 11L));
                attributes.Add(ConnectorAttributeBuilder.Build("attributelongpMultivalue", 12L, 13L));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeLong", new long?(14L)));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeLongMultivalue", Convert.ToInt64(15), Convert.ToInt64(16), null));

                attributes.Add(ConnectorAttributeBuilder.Build("attributechar", 'a'));
                attributes.Add(ConnectorAttributeBuilder.Build("attributecharMultivalue", 'b', 'c'));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeCharacter", new char?('d')));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeCharacterMultivalue", new char?('e'), new char?('f'), null));

                attributes.Add(ConnectorAttributeBuilder.Build("attributedoublep", double.MinValue));
                attributes.Add(ConnectorAttributeBuilder.Build("attributedoublepMultivalue", double.Epsilon, double.MaxValue));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeDouble", new double?(17D)));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeDoubleMultivalue", new double?(18D), new double?(19D), null));

                attributes.Add(ConnectorAttributeBuilder.Build("attributefloatp", 20F));
                attributes.Add(ConnectorAttributeBuilder.Build("attributefloatpMultivalue", 21F, 22F));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeFloat", new float?(23F)));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeFloatMultivalue", new float?(24F), new float?(25F), null));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeint", 26));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeintMultivalue", 27, 28));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeInteger", new int?(29)));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeIntegerMultivalue", new int?(30), new int?(31), null));

                attributes.Add(ConnectorAttributeBuilder.Build("attributebooleanp", true));
                attributes.Add(ConnectorAttributeBuilder.Build("attributebooleanpMultivalue", true, false));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeBoolean", Convert.ToBoolean(false)));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeBooleanMultivalue", Convert.ToBoolean(true), Convert.ToBoolean(false), null));

                attributes.Add(ConnectorAttributeBuilder.Build("attributebytep", (byte)48));
                attributes.Add(ConnectorAttributeBuilder.Build("attributebytepMultivalue", (byte)49, (byte)50));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeByte", new byte?((byte)51)));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeByteMultivalue", new byte?((byte)52), new byte?((byte)53), null));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeByteArray", System.Text.Encoding.UTF8.GetBytes("array")));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeByteArrayMultivalue", System.Text.Encoding.UTF8.GetBytes("item1"), System.Text.Encoding.UTF8.GetBytes("item2")));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeBigDecimal", new BigDecimal(new BigInteger("1"), 0)));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeBigDecimalMultivalue", new BigDecimal(new BigInteger("0"), 0), new BigDecimal(new BigInteger("10"), 0)));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeBigInteger", new BigInteger("1")));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeBigIntegerMultivalue", new BigInteger("0"), new BigInteger("10")));

                GuardedByteArray array = new GuardedByteArray();
                GuardedByteArray item1 = new GuardedByteArray();
                GuardedByteArray item2 = new GuardedByteArray();
                System.Text.Encoding.UTF8.GetBytes("array").ToList().ForEach(p => array.AppendByte(p));
                System.Text.Encoding.UTF8.GetBytes("item1").ToList().ForEach(p => item1.AppendByte(p));
                System.Text.Encoding.UTF8.GetBytes("item2").ToList().ForEach(p => item2.AppendByte(p));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeGuardedByteArray", array));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeGuardedByteArrayMultivalue", item1, item2));

                SecureString secret = new SecureString();
                SecureString secret1 = new SecureString();
                SecureString secret2 = new SecureString();
                "secret".ToCharArray().ToList().ForEach(p => secret.AppendChar(p));
                "secret1".ToCharArray().ToList().ForEach(p => secret1.AppendChar(p));
                "secret2".ToCharArray().ToList().ForEach(p => secret2.AppendChar(p));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeGuardedString", new GuardedString(secret)));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeGuardedStringMultivalue", new GuardedString(secret1), new GuardedString(secret2)));

                attributes.Add(ConnectorAttributeBuilder.Build("attributeMap", SampleMap));
                attributes.Add(ConnectorAttributeBuilder.Build("attributeMapMultivalue", SampleMap, SampleMap));
                return new ConnectorAttributesAccessor(attributes);
            }
        }

        private IDictionary<object, object> SampleMap
        {
            get
            {
                IDictionary<object, object> ret = CollectionUtil.NewDictionary<object, object>("string", "String", "number", 43, "trueOrFalse", true, "nullValue", null, "collection", CollectionUtil.NewList(new string[2] { "item1", "item2" }));
                ret["object"] = CollectionUtil.NewDictionary("key1", "value1", "key2", "value2");
                return ret;
            }
        }
    }
    #endregion
    #region ConnectorAttributeUtilTests
    [TestFixture]
    public class ConnectorAttributeUtilTests
    {

        [Test]
        public void TestNamesEqual()
        {
            Assert.IsTrue(ConnectorAttributeUtil.NamesEqual("givenName", "givenname"));
        }

        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void TestGetSingleValue()
        {
            object TEST_VALUE = 1L;
            ConnectorAttribute attr = ConnectorAttributeBuilder.Build("long", TEST_VALUE);
            object value = ConnectorAttributeUtil.GetSingleValue(attr);
            Assert.AreEqual(TEST_VALUE, value);

            // test null
            attr = ConnectorAttributeBuilder.Build("long");
            value = ConnectorAttributeUtil.GetSingleValue(attr);
            Assert.IsNull(value);
            // test empty
            attr = ConnectorAttributeBuilder.Build("long", new List<object>());
            value = ConnectorAttributeUtil.GetSingleValue(attr);
            Assert.IsNull(value);
            // test illegal argument exception
            ConnectorAttributeUtil.GetSingleValue(ConnectorAttributeBuilder.Build("bob", 1, 2, 3));
        }

        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void TestDictionaryIntegerAttribute()
        {
            Dictionary<object, object> map = new Dictionary<object, object>();
            map[1] = "NOK";

            ConnectorAttributeBuilder bld = new ConnectorAttributeBuilder();
            bld.AddValue(map);
        }

        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void TestDictionaryShortAttribute()
        {
            Dictionary<object, object> map1 = new Dictionary<object, object>();
            map1["string"] = "NOK";

            Dictionary<object, object> map2 = new Dictionary<object, object>();
            map2["map1"] = map1;
            map2["list"] = new List<object> { 1, 2, 3, (short)5 };

            Dictionary<object, object> map3 = new Dictionary<object, object>();
            map3["map2"] = map2;

            Dictionary<object, object> map4 = new Dictionary<object, object>();
            map4["map3"] = map3;

            ConnectorAttributeBuilder.Build("map", map4);
        }

        [Test]
        public void TestDictionaryAttribute()
        {
            Dictionary<object, object> map1 = new Dictionary<object, object>();
            map1["string"] = "OK";

            Dictionary<object, object> map2 = new Dictionary<object, object>();
            map2["map1"] = map1;
            map2["list"] = new List<int> { 1, 2, 3 };

            Dictionary<object, object> map3 = new Dictionary<object, object>();
            map3["map2"] = map2;

            Dictionary<object, object> map4 = new Dictionary<object, object>();
            map4["map3"] = map3;

            ConnectorAttributeBuilder.Build("map", map4);
        }
    }
    #endregion
}
