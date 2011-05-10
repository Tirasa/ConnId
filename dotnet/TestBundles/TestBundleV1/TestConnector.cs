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
using System.Globalization;
using System.Threading;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
namespace org.identityconnectors.testconnector
{
    public class TstConnectorConfig : AbstractConfiguration
    {
        /// <summary>
        /// keep lower case for consistent unit tests
        /// </summary>
        [ConfigurationProperty(OperationTypes = new Type[] { typeof(SyncOp) })]
        public string tstField { get; set; }

        /// <summary>
        /// keep lower case for consistent unit tests
        /// </summary>
        public int numResults { get; set; }

        /// <summary>
        /// keep lower case for consistent unit tests
        /// </summary>
        public bool failValidation { get; set; }

        /// <summary>
        /// keep lower case for consistent unit tests
        /// </summary>
        public bool resetConnectionCount { get; set; }

        public override void Validate()
        {
            if (failValidation)
            {
                throw new ConnectorException("validation failed " + CultureInfo.CurrentUICulture.TwoLetterISOLanguageName);
            }
        }

    }

    public class MyTstConnection
    {
        private readonly int _connectionNumber;
        private bool _isGood = true;

        public MyTstConnection(int connectionNumber)
        {
            _connectionNumber = connectionNumber;
        }

        public void Test()
        {
            if (!_isGood)
            {
                throw new ConnectorException("Connection is bad");
            }
        }

        public void Dispose()
        {
            _isGood = false;
        }

        public bool IsGood()
        {
            return _isGood;
        }

        public int GetConnectionNumber()
        {
            return _connectionNumber;
        }
    }

    [ConnectorClass("TestConnector",
                    typeof(TstConnectorConfig),
                    MessageCatalogPaths = new String[] { "TestBundleV1.Messages" }
                        )]
    public class TstConnector : CreateOp, PoolableConnector, SchemaOp, SearchOp<String>, SyncOp
    {
        private static int _connectionCount = 0;
        private MyTstConnection _myConnection;
        private TstConnectorConfig _config;

        public Uid Create(ObjectClass oclass, ICollection<ConnectorAttribute> attrs, OperationOptions options)
        {
            int? delay = (int?)CollectionUtil.GetValue(options.Options, "delay", null);
            if (delay != null)
            {
                Thread.Sleep((int)delay);
            }

            if (options.Options.ContainsKey("testPooling"))
            {
                return new Uid(_myConnection.GetConnectionNumber().ToString());
            }
            else
            {
                String version = "1.0";
                return new Uid(version);
            }
        }
        public void Init(Configuration cfg)
        {
            _config = (TstConnectorConfig)cfg;
            if (_config.resetConnectionCount)
            {
                _connectionCount = 0;
            }
            _myConnection = new MyTstConnection(_connectionCount++);
        }

        public static String getVersion()
        {
            return "1.0";
        }

        public void Dispose()
        {
            if (_myConnection != null)
            {
                _myConnection.Dispose();
                _myConnection = null;
            }
        }

        /// <summary>
        /// Used by the script tests
        /// </summary>
        public String concat(String s1, String s2)
        {
            return s1 + s2;
        }

        public void CheckAlive()
        {
            _myConnection.Test();
        }

        private class MyTranslator : AbstractFilterTranslator<String>
        {

        }
        public FilterTranslator<String> CreateFilterTranslator(ObjectClass oclass, OperationOptions options)
        {
            return new MyTranslator();
        }
        public void ExecuteQuery(ObjectClass oclass, String query, ResultsHandler handler, OperationOptions options)
        {

            for (int i = 0; i < _config.numResults; i++)
            {
                int? delay = (int?)CollectionUtil.GetValue(options.Options, "delay", null);
                if (delay != null)
                {
                    Thread.Sleep((int)delay);
                }
                ConnectorObjectBuilder builder =
                    new ConnectorObjectBuilder();
                builder.SetUid("" + i);
                builder.SetName(i.ToString());
                builder.ObjectClass = oclass;
                for (int j = 0; j < 50; j++)
                {
                    builder.AddAttribute("myattribute" + j, "myvaluevaluevalue" + j);
                }
                ConnectorObject rv = builder.Build();
                if (!handler(rv))
                {
                    break;
                }
            }
        }
        public void Sync(ObjectClass objClass, SyncToken token,
                         SyncResultsHandler handler,
                         OperationOptions options)
        {
            for (int i = 0; i < _config.numResults; i++)
            {
                ConnectorObjectBuilder obuilder =
                    new ConnectorObjectBuilder();
                obuilder.SetUid(i.ToString());
                obuilder.SetName(i.ToString());
                obuilder.ObjectClass = (objClass);

                SyncDeltaBuilder builder =
                    new SyncDeltaBuilder();
                builder.Object = (obuilder.Build());
                builder.DeltaType = (SyncDeltaType.CREATE_OR_UPDATE);
                builder.Token = (new SyncToken("mytoken"));
                SyncDelta rv = builder.Build();
                if (!handler(rv))
                {
                    break;
                }
            }
        }

        public SyncToken GetLatestSyncToken(ObjectClass objectClass)
        {
            return new SyncToken("mylatest");
        }

        public Schema Schema()
        {
            SchemaBuilder builder = new SchemaBuilder(SafeType<Connector>.Get<TstConnector>());
            for (int i = 0; i < 2; i++)
            {
                ObjectClassInfoBuilder classBuilder = new ObjectClassInfoBuilder();
                classBuilder.ObjectType = ("class" + i);
                for (int j = 0; j < 200; j++)
                {
                    classBuilder.AddAttributeInfo(ConnectorAttributeInfoBuilder.Build("attributename" + j, typeof(String)));
                }
                builder.DefineObjectClass(classBuilder.Build());
            }
            return builder.Build();
        }
    }
}
