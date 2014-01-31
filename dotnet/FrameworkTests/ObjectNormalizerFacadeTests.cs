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
 */
using System;
using NUnit.Framework;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Impl.Api.Local.Operations;
namespace FrameworkTests
{
    [TestFixture]
    public class ObjectNormalizerFacadeTests
    {
        public class MyAttributeNormalizer : AttributeNormalizer
        {
            public ConnectorAttribute NormalizeAttribute(ObjectClass oclass, ConnectorAttribute attribute)
            {
                if (attribute.Is("foo"))
                {
                    String val = ConnectorAttributeUtil.GetStringValue(attribute);
                    return ConnectorAttributeBuilder.Build("foo", val.Trim());
                }
                else
                {
                    return attribute;
                }
            }
        }

        private ConnectorAttribute CreateTestAttribute()
        {
            return ConnectorAttributeBuilder.Build("foo", " bar ");
        }

        private ConnectorAttribute CreateNormalizedTestAttribute()
        {
            return ConnectorAttributeBuilder.Build("foo", "bar");
        }

        private ObjectNormalizerFacade CreateTestNormalizer()
        {
            ObjectNormalizerFacade facade = new
            ObjectNormalizerFacade(ObjectClass.ACCOUNT,
                    new MyAttributeNormalizer());
            return facade;
        }

        private void AssertNormalizedFilter(Filter expectedNormalizedFilter,
                Filter filter)
        {
            ObjectNormalizerFacade facade =
                CreateTestNormalizer();
            filter = facade.NormalizeFilter(filter);
            String expectedXml = SerializerUtil.SerializeXmlObject(expectedNormalizedFilter, false);
            String actualXml = SerializerUtil.SerializeXmlObject(filter, false);
            Assert.AreEqual(expectedXml, actualXml);
        }



        [Test]
        public void TestEndsWith()
        {
            Filter expected =
                FilterBuilder.EndsWith(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.EndsWith(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestStartsWith()
        {
            Filter expected =
                FilterBuilder.StartsWith(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.StartsWith(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestContains()
        {
            Filter expected =
                FilterBuilder.Contains(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.Contains(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestEqualTo()
        {
            Filter expected =
                FilterBuilder.EqualTo(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.EqualTo(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestGreaterThanOrEqualTo()
        {
            Filter expected =
                FilterBuilder.GreaterThanOrEqualTo(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.GreaterThanOrEqualTo(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestLessThanOrEqualTo()
        {
            Filter expected =
                FilterBuilder.LessThanOrEqualTo(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.LessThanOrEqualTo(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestLessThan()
        {
            Filter expected =
                FilterBuilder.LessThan(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.LessThan(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestGreaterThan()
        {
            Filter expected =
                FilterBuilder.GreaterThan(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.GreaterThan(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestAnd()
        {
            Filter expected =
                FilterBuilder.And(FilterBuilder.Contains(CreateNormalizedTestAttribute()),
                        FilterBuilder.Contains(CreateNormalizedTestAttribute()));
            Filter filter =
                FilterBuilder.And(FilterBuilder.Contains(CreateTestAttribute()),
                        FilterBuilder.Contains(CreateTestAttribute()));
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestOr()
        {
            Filter expected =
                FilterBuilder.Or(FilterBuilder.Contains(CreateNormalizedTestAttribute()),
                        FilterBuilder.Contains(CreateNormalizedTestAttribute()));
            Filter filter =
                FilterBuilder.Or(FilterBuilder.Contains(CreateTestAttribute()),
                        FilterBuilder.Contains(CreateTestAttribute()));
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestNot()
        {
            Filter expected =
                FilterBuilder.Not(FilterBuilder.Contains(CreateNormalizedTestAttribute()));
            Filter filter =
                FilterBuilder.Not(FilterBuilder.Contains(CreateTestAttribute()));
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestContainsAllValues()
        {
            Filter expected =
                FilterBuilder.ContainsAllValues(CreateNormalizedTestAttribute());
            Filter filter =
                FilterBuilder.ContainsAllValues(CreateTestAttribute());
            AssertNormalizedFilter(expected, filter);
        }
        [Test]
        public void TestConnectorObject()
        {
            ConnectorObjectBuilder builder =
               new ConnectorObjectBuilder();
            builder.SetName("myname");
            builder.SetUid("myuid");
            builder.AddAttribute(CreateTestAttribute());
            ConnectorObject v1 = builder.Build();
            ConnectorObject v2 = CreateTestNormalizer().NormalizeObject(v1);
            builder =
                new ConnectorObjectBuilder();
            builder.SetName("myname");
            builder.SetUid("myuid");
            builder.AddAttribute(CreateNormalizedTestAttribute());
            ConnectorObject expected = builder.Build();
            Assert.AreEqual(expected, v2);
            Assert.IsFalse(expected.Equals(v1));
        }

        [Test]
        public void TestSyncDelta()
        {
            ConnectorObjectBuilder objbuilder =
                new ConnectorObjectBuilder();
            objbuilder.SetName("myname");
            objbuilder.SetUid("myuid");
            objbuilder.AddAttribute(CreateTestAttribute());
            ConnectorObject obj = objbuilder.Build();

            SyncDeltaBuilder builder =
                new SyncDeltaBuilder();
            builder.DeltaType = (SyncDeltaType.DELETE);
            builder.Token = (new SyncToken("mytoken"));
            builder.Uid = (new Uid("myuid"));
            builder.Object = (obj);
            SyncDelta v1 = builder.Build();
            SyncDelta v2 = CreateTestNormalizer().NormalizeSyncDelta(v1);
            builder =
                new SyncDeltaBuilder();
            builder.DeltaType = (SyncDeltaType.DELETE);
            builder.Token = (new SyncToken("mytoken"));
            builder.Uid = (new Uid("myuid"));
            objbuilder =
                new ConnectorObjectBuilder();
            objbuilder.SetName("myname");
            objbuilder.SetUid("myuid");
            objbuilder.AddAttribute(CreateNormalizedTestAttribute());
            builder.Object = objbuilder.Build();
            SyncDelta expected = builder.Build();
            Assert.AreEqual(expected, v2);
            Assert.IsFalse(expected.Equals(v1));

        }

    }
}