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

using NUnit.Framework;
using Org.IdentityConnectors.Framework.Common.Objects;

namespace FrameworkTests
{
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
            map2["list"] = new List<object> { 1, 2, 3, (short)5};

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
            map2["list"] = new List<int>{ 1, 2, 3};

            Dictionary<object, object> map3 = new Dictionary<object, object>();
            map3["map2"] = map2;

            Dictionary<object, object> map4 = new Dictionary<object, object>();
            map4["map3"] = map3;

            ConnectorAttributeBuilder.Build("map", map4);
        }
    }
}
