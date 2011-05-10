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
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;

namespace FrameworkTests
{

    public class MockConnector : Connector, SchemaOp
    {

        /// <summary>
        /// Represents a call to a connector method.
        /// </summary>
        public class Call
        {

            private readonly string methodName;
            private readonly object[] args;

            public Call(string methodName, params object[] args)
            {
                this.methodName = methodName;
                this.args = args;
            }

            public string MethodName
            {
                get
                {
                    return methodName;
                }
            }

            public object[] Arguments
            {
                get
                {
                    return this.args;
                }
            }
        }

        // need to keep track of when methods are called an their parameters..
        private static IList<Call> callPattern = new List<Call>();

        private Configuration _config;

        public void Dispose()
        {
            AddCall("Dispose");
        }

        public Schema Schema()
        {
            AddCall("Schema");
            return null;
        }

        public void Init(Configuration cfg)
        {
            _config = cfg;
            AddCall("Init", cfg);
        }

        public Configuration getConfiguration()
        {
            return _config;
        }

        /// <summary>
        /// Clear the call pattern.
        /// </summary>
        public static void Reset()
        {
            callPattern.Clear();
        }

        /// <summary>
        /// Get the current call pattern.
        /// </summary>
        public static IList<Call> GetCallPattern()
        {
            return CollectionUtil.NewList(callPattern);
        }

        /// <summary>
        /// Adds the call to the internal call pattern.
        /// </summary>
        public static void AddCall(string methodName, params object[] args)
        {
            callPattern.Add(new Call(methodName, args));
        }
    }

    public class MockAllOpsConnector : MockConnector, CreateOp,
            DeleteOp, UpdateOp, SearchOp<string>, UpdateAttributeValuesOp, AuthenticateOp,
            ResolveUsernameOp, TestOp, ScriptOnConnectorOp, ScriptOnResourceOp
    {

        public object RunScriptOnConnector(ScriptContext request,
                OperationOptions options)
        {
            Assert.IsNotNull(request);
            Assert.IsNotNull(options);
            AddCall("RunScriptOnConnector", request, options);
            return null;
        }

        public object RunScriptOnResource(ScriptContext request,
                OperationOptions options)
        {
            Assert.IsNotNull(request);
            Assert.IsNotNull(options);
            AddCall("RunScriptOnResource", request, options);
            return null;
        }

        public Uid Create(ObjectClass oclass, ICollection<ConnectorAttribute> attrs,
                OperationOptions options)
        {
            Assert.IsNotNull(attrs);
            AddCall("Create", attrs);
            return null;
        }

        public void Delete(ObjectClass objClass, Uid uid,
                OperationOptions options)
        {
            Assert.IsNotNull(uid);
            Assert.IsNotNull(objClass);
            AddCall("Delete", objClass, uid);
        }

        public Uid Update(ObjectClass objclass, Uid uid, ICollection<ConnectorAttribute> attrs,
                OperationOptions options)
        {
            Assert.IsNotNull(objclass);
            Assert.IsNotNull(attrs);
            AddCall("Update", objclass, attrs);
            return null;
        }

        public Uid AddAttributeValues(ObjectClass objclass, Uid uid,
                ICollection<ConnectorAttribute> valuesToAdd, OperationOptions options)
        {
            AddCall("AddAttributeValues", objclass, valuesToAdd);
            return null;
        }

        public Uid RemoveAttributeValues(ObjectClass objclass, Uid uid,
                ICollection<ConnectorAttribute> valuesToRemove, OperationOptions options)
        {
            AddCall("RemoveAttributeValues", objclass, valuesToRemove);
            return null;
        }

        public FilterTranslator<string> CreateFilterTranslator(ObjectClass oclass,
                OperationOptions options)
        {
            Assert.IsNotNull(oclass);
            Assert.IsNotNull(options);
            AddCall("CreateFilterTranslator", oclass, options);
            // no translation - ok since this is just for tests
            return new MockFilterTranslator();
        }

        public void ExecuteQuery(ObjectClass oclass, string query,
                ResultsHandler handler, OperationOptions options)
        {
            Assert.IsNotNull(oclass);
            Assert.IsNotNull(handler);
            Assert.IsNotNull(options);
            AddCall("ExecuteQuery", oclass, query, handler, options);
        }

        public Uid Authenticate(ObjectClass objectClass, string username, GuardedString password,
                OperationOptions options)
        {
            Assert.IsNotNull(username);
            Assert.IsNotNull(password);
            AddCall("Authenticate", username, password);
            return null;
        }

        public Uid ResolveUsername(ObjectClass objectClass, string username, OperationOptions options)
        {
            Assert.IsNotNull(username);
            AddCall("ResolveUsername", username);
            return null;
        }

        public void Test()
        {
            AddCall("Test");
        }
    }

    public class MockUpdateConnector : Connector, UpdateOp, SearchOp<string>
    {

        private Configuration _cfg;

        public void Dispose()
        {
            // nothing to do this is a mock connector..
        }

        public void Init(Configuration cfg)
        {
            _cfg = cfg;
        }

        public Configuration GetConfiguration()
        {
            return _cfg;
        }

        private static IList<ConnectorObject> objects = new List<ConnectorObject>();

        static MockUpdateConnector()
        {
            ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
            for (int i = 0; i < 100; i++)
            {
                bld.SetUid(Convert.ToString(i));
                bld.SetName(Convert.ToString(i));
                objects.Add(bld.Build());
            }
        }

        /// <summary>
        /// This will do a basic replace.
        /// </summary>
        /// 
        /// <seealso cref="UpdateOp.Update"/>
        ///
        public Uid Update(ObjectClass objclass, Uid uid, ICollection<ConnectorAttribute> attrs, OperationOptions options)
        {
            string val = ConnectorAttributeUtil.GetAsStringValue(uid);
            int idx = Convert.ToInt32(val);
            //.Get out the object..
            ConnectorObject baseObject = objects[idx];
            ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
            bld.Add(baseObject);
            bld.AddAttributes(attrs);
            ConnectorObject obj = bld.Build();
            objects[idx] = obj;
            return obj.Uid;
        }

        public FilterTranslator<string> CreateFilterTranslator(ObjectClass oclass, OperationOptions options)
        {
            //no translation - ok since this is just for tests
            return new MockFilterTranslator();
        }

        /// <summary>
        /// Simply return everything don't bother optimizing.
        /// </summary>
        ///
        /// <seealso cref="SearchOp.Search"/>
        public void ExecuteQuery(ObjectClass oclass, string query, ResultsHandler handler, OperationOptions options)
        {
            foreach (ConnectorObject obj in objects)
            {
                if (!handler(obj))
                {
                    break;
                }
            }
        }
    }

    class MockFilterTranslator : AbstractFilterTranslator<string>
    {
    }

    public class MockConfiguration : AbstractConfiguration
    {

        private readonly bool fail;

        public MockConfiguration()
        {
        }

        /// <summary>
        /// Determines if this configuration will fail validation.
        /// </summary>
        public MockConfiguration(bool failvalidation)
        {
            this.fail = failvalidation;
        }

        public bool Fail
        {
            get;
            set;
        }

        public override void Validate()
        {
            if (fail)
            {
                throw new ConfigurationException();
            }
        }

    }
}