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
using System.Collections.Generic;
using System.Linq;
using System.Text;
using NUnit.Framework;
using Org.IdentityConnectors.Test.Common;
using Org.IdentityConnectors.Common;

namespace FrameworkTests
{
    [TestFixture]
    public class PropertyBagTests
    {
        private PropertyBag bag = null;

        [SetUp]
        public void SetUp()
        {
            bag = CreateBag();
        }

        [Test]
        public void TestGetProperty()
        {
            Assert.AreEqual("value1", bag.GetProperty<string>("key1"));
            Assert.IsNull(bag.GetProperty<string>("key2"));

            Assert.AreEqual((int?)1, bag.GetProperty<int?>("key3"));
            Assert.AreEqual((long?)1, bag.GetProperty<long?>("key5"));

            // try not existing
            try
            {
                bag.GetProperty<string>("key4");
                Assert.Fail("Get Property must fail for unexisting property");
            }
            catch (ArgumentException)
            {
            }

            // Try cast
            try
            {
                bag.GetProperty<long?>("key3");
                Assert.Fail("Get Property with incompatible type must fail on InvalidCastException");
            }
            catch (InvalidCastException)
            {
            }
        }

        [Test]
        public void TestGetPropertyWithDef()
        {
            Assert.AreEqual("value1", bag.GetProperty<string>("key1", "def"));
            Assert.IsNull(bag.GetProperty<string>("key2", "def"));
            Assert.AreEqual("def", bag.GetProperty<string>("key4", "def"));
            Assert.IsNull(bag.GetProperty<string>("key4", null));
        }

        [Test]
        public void TestGetStringProperty()
        {
            Assert.AreEqual("value1", bag.GetStringProperty("key1"));
            Assert.IsNull(bag.GetStringProperty("key2"));
            // Try cast
            try
            {
                bag.GetStringProperty("key3");
                Assert.Fail("Get Property with incompatible type must fail on InvalidCastException");
            }
            catch (InvalidCastException)
            {
            }

            // try not existing
            try
            {
                bag.GetStringProperty("key4");
                Assert.Fail("Get String Property must fail for unexisting property");
            }
            catch (ArgumentException)
            {
            }
        }

        [Test]
        public void TestToDictionary()
        {
            var properties = bag.ToDictionary();
            Assert.That(CollectionUtil.DictionariesEqual(properties, CreateDictionary()),
                "ToDictionary must return the same properties as it was created with" );
        }

        private static PropertyBag CreateBag()
        {
            var properties = CreateDictionary();
            return new PropertyBag(properties);
        }

        private static Dictionary<string, object> CreateDictionary()
        {
            var properties = new Dictionary<string, object>
                                 {
                                     {"key1", "value1"},
                                     {"key2", null},
                                     {"key3", (int?) 1},
                                     {"key5", (long?) 1}
                                 };
            return properties;
        }
    }
}
