/*
 *  ====================
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  
 *  Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.     
 *  
 *  The contents of this file are subject to the terms of the Common Development 
 *  and Distribution License("CDDL") (the "License").  You may not use this file 
 *  except in compliance with the License.
 *  
 *  You can obtain a copy of the License at 
 *  http://IdentityConnectors.dev.java.net/legal/license.txt
 *  See the License for the specific language governing permissions and limitations 
 *  under the License. 
 *  
 *  When distributing the Covered Code, include this CDDL Header Notice in each file
 *  and include the License file at identityconnectors/legal/license.txt.
 *  If applicable, add the following below this CDDL Header, with the fields 
 *  enclosed by brackets [] replaced by your own identifying information: 
 *  "Portions Copyrighted [year] [name of copyright owner]"
 *  ====================
 */

using System;
using System.Collections.Generic;

using NUnit.Framework;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
using Org.IdentityConnectors.Test.Common;

namespace FrameworkTests
{

    [TestFixture]
    public class ConnectorFacadeTests
    {

        [SetUp]
        public void setup()
        {
            // always reset the call patterns..
            MockConnector.Reset();
        }

        private class TestOperationPattern
        {
            /// <summary>
            /// Simple call back to make the 'facade' calls.
            /// </summary> 
            public Action<ConnectorFacade> MakeCall;

            /// <summary>
            /// Given the list of calls determine if they match expected values based
            /// on the calls made in the <see cref="TestOperationPattern.MakeCall"/> method.
            /// </summary>
            public Action<IList<MockConnector.Call>> CheckCalls;
        }

        /// <summary>
        /// Test the pattern of the common operations.
        /// </summary>
        private void TestCallPattern(TestOperationPattern pattern)
        {
            TestCallPattern(pattern, SafeType<Connector>.Get<MockAllOpsConnector>());
        }

        private void TestCallPattern(TestOperationPattern pattern,
                SafeType<Connector> clazz)
        {
            Configuration config = new MockConfiguration(false);
            ConnectorFacadeFactory factory = ConnectorFacadeFactory.GetInstance();
            // **test only**
            APIConfiguration impl = TestHelpers.CreateTestConfiguration(clazz, config);
            ConnectorFacade facade;
            facade = factory.NewInstance(impl);
            // make the call on the connector facade..
            pattern.MakeCall(facade);
            // check the call structure..
            IList<MockConnector.Call> calls = MockConnector.GetCallPattern();
            // check the call pattern..
            Assert.AreEqual("Init", calls[0].MethodName);
            calls.RemoveAt(0);
            pattern.CheckCalls(calls);
            Assert.AreEqual("Dispose", calls[0].MethodName);
            calls.RemoveAt(0);
            Assert.IsTrue(calls.Count == 0);
        }

        /// <summary>
        /// Tests that if an SPI operation is not implemented that the API will throw
        /// an <see cref="InvalidOperationException"/>.
        /// </summary>
        [Test]
        [ExpectedException(typeof(InvalidOperationException))]
        public void UnsupportedOperationTest()
        {
            Configuration config = new MockConfiguration(false);
            SafeType<Connector> clazz = SafeType<Connector>.Get<MockConnector>();
            ConnectorFacadeFactory factory = ConnectorFacadeFactory.GetInstance();
            APIConfiguration impl = TestHelpers.CreateTestConfiguration(clazz,
                    config);
            ConnectorFacade facade;
            facade = factory.NewInstance(impl);
            facade.Authenticate(ObjectClass.ACCOUNT, "fadf", new GuardedString(), null);
        }

        [Test]
        public void RunScriptOnConnectorCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    facade.RunScriptOnConnector(
                            new ScriptContextBuilder("lang", "script").Build(),
                            null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("RunScriptOnConnector", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void RunScriptOnResourceCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    facade.RunScriptOnResource(
                            new ScriptContextBuilder("lang", "script").Build(),
                            null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("RunScriptOnResource", GetAndRemoveMethodName(calls));
                }
            });
        }

        /// <summary>
        /// Test the call pattern to get the schema.
        /// </summary>
        [Test]
        public void SchemaCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    facade.Schema();
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("Schema", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void AuthenticateCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    facade.Authenticate(ObjectClass.ACCOUNT, "dfadf", new GuardedString(), null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("Authenticate", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void ResolveUsernameCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    facade.ResolveUsername(ObjectClass.ACCOUNT, "dfadf", null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("ResolveUsername", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void CreateCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    ICollection<ConnectorAttribute> attrs = CollectionUtil.NewReadOnlySet<ConnectorAttribute>();
                    facade.Create(ObjectClass.ACCOUNT, attrs, null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("Create", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        [ExpectedException(typeof(ArgumentNullException))]
        public void CreateWithOutObjectClassPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    ICollection<ConnectorAttribute> attrs = new HashSet<ConnectorAttribute>();
                    facade.Create(null, attrs, null);
                },
                CheckCalls = calls =>
                {
                    Assert.Fail("Should not get here..");
                }
            });
        }

        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void createDuplicatConnectorAttributesPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    ICollection<ConnectorAttribute> attrs = new HashSet<ConnectorAttribute>();
                    attrs.Add(ConnectorAttributeBuilder.Build("abc", 1));
                    attrs.Add(ConnectorAttributeBuilder.Build("abc", 2));
                    facade.Create(ObjectClass.ACCOUNT, attrs, null);
                },
                CheckCalls = calls =>
                {
                    Assert.Fail("Should not get here..");
                }
            });
        }

        [Test]
        public void UpdateCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    ICollection<ConnectorAttribute> attrs = new HashSet<ConnectorAttribute>();
                    attrs.Add(ConnectorAttributeBuilder.Build("accountid"));
                    facade.Update(ObjectClass.ACCOUNT, NewUid(0), attrs, null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("Update", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void DeleteCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    facade.Delete(ObjectClass.ACCOUNT, NewUid(0), null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("Delete", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void SearchCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    // create an empty results handler..
                    ResultsHandler rh = obj =>
                    {
                        return true;
                    };
                    // call the search method..
                    facade.Search(ObjectClass.ACCOUNT, null, rh, null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("CreateFilterTranslator", GetAndRemoveMethodName(calls));
                    Assert.AreEqual("ExecuteQuery", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void GetCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    // create an empty results handler..
                    // call the search method..
                    facade.GetObject(ObjectClass.ACCOUNT, NewUid(0), null);
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("CreateFilterTranslator", GetAndRemoveMethodName(calls));
                    Assert.AreEqual("ExecuteQuery", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void TestOpCallPattern()
        {
            TestCallPattern(new TestOperationPattern()
            {
                MakeCall = facade =>
                {
                    facade.Test();
                },
                CheckCalls = calls =>
                {
                    Assert.AreEqual("Test", GetAndRemoveMethodName(calls));
                }
            });
        }

        [Test]
        public void UpdateMergeTests()
        {
            ConnectorAttribute expected, actual;
            Configuration config = new MockConfiguration(false);
            ConnectorFacadeFactory factory = ConnectorFacadeFactory.GetInstance();
            SafeType<Connector> clazz = SafeType<Connector>.Get<MockUpdateConnector>();
            // **test only**
            APIConfiguration impl = TestHelpers.CreateTestConfiguration(clazz, config);
            impl.SetTimeout(SafeType<APIOperation>.Get<GetApiOp>(), APIConstants.NO_TIMEOUT);
            impl.SetTimeout(SafeType<APIOperation>.Get<UpdateApiOp>(), APIConstants.NO_TIMEOUT);
            impl.SetTimeout(SafeType<APIOperation>.Get<SearchApiOp>(), APIConstants.NO_TIMEOUT);
            ConnectorFacade facade = factory.NewInstance(impl);
            // sniff test to make sure we can get an object..
            ConnectorObject obj = facade.GetObject(ObjectClass.ACCOUNT, NewUid(1), null);
            Assert.AreEqual(NewUid(1), obj.Uid);
            // ok lets add an attribute that doesn't exist..
            String ADDED = "somthing to add to the object";
            String ATTR_NAME = "added";
            ICollection<ConnectorAttribute> addAttrSet;
            addAttrSet = CollectionUtil.NewSet((IEnumerable<ConnectorAttribute>)obj.GetAttributes());
            addAttrSet.Add(ConnectorAttributeBuilder.Build(ATTR_NAME, ADDED));
            Name name = obj.Name;
            addAttrSet.Remove(name);
            Uid uid = facade.AddAttributeValues(ObjectClass.ACCOUNT, obj.Uid, ConnectorAttributeUtil.FilterUid(addAttrSet), null);
            // get back the object and see if there are the same..
            addAttrSet.Add(name);
            ConnectorObject addO = new ConnectorObject(ObjectClass.ACCOUNT, addAttrSet);
            obj = facade.GetObject(ObjectClass.ACCOUNT, NewUid(1), null);
            Assert.AreEqual(addO, obj);
            // attempt to add on to an existing attribute..
            addAttrSet.Remove(name);
            uid = facade.AddAttributeValues(ObjectClass.ACCOUNT, obj.Uid, ConnectorAttributeUtil.FilterUid(addAttrSet), null);
            // get the object back out and check on it..
            obj = facade.GetObject(ObjectClass.ACCOUNT, uid, null);
            expected = ConnectorAttributeBuilder.Build(ATTR_NAME, ADDED, ADDED);
            actual = obj.GetAttributeByName(ATTR_NAME);
            Assert.AreEqual(expected, actual);
            // attempt to delete a value from an attribute..
            ICollection<ConnectorAttribute> deleteAttrs = CollectionUtil.NewSet((IEnumerable<ConnectorAttribute>)addO.GetAttributes());
            deleteAttrs.Remove(name);
            uid = facade.RemoveAttributeValues(ObjectClass.ACCOUNT, addO.Uid, ConnectorAttributeUtil.FilterUid(deleteAttrs), null);
            obj = facade.GetObject(ObjectClass.ACCOUNT, uid, null);
            expected = ConnectorAttributeBuilder.Build(ATTR_NAME, ADDED);
            actual = obj.GetAttributeByName(ATTR_NAME);
            Assert.AreEqual(expected, actual);
            // attempt to delete an attribute that doesn't exist..
            ICollection<ConnectorAttribute> nonExist = new HashSet<ConnectorAttribute>();
            nonExist.Add(NewUid(1));
            nonExist.Add(ConnectorAttributeBuilder.Build("does not exist", "asdfe"));
            uid = facade.RemoveAttributeValues(ObjectClass.ACCOUNT, addO.Uid, ConnectorAttributeUtil.FilterUid(nonExist), null);
            obj = facade.GetObject(ObjectClass.ACCOUNT, NewUid(1), null);
            Assert.IsTrue(obj.GetAttributeByName("does not exist") == null);
        }

        static Uid NewUid(int id)
        {
            return new Uid(Convert.ToString(id));
        }

        static string GetAndRemoveMethodName(IList<MockConnector.Call> calls)
        {
            string result = calls[0].MethodName;
            calls.RemoveAt(0);
            return result;
        }
    }
}