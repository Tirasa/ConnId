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
    }
}
