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
 * Portions Copyrighted 2012-2014 ForgeRock AS.
 */
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Threading;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Common.Objects;
using ICF = Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
namespace org.identityconnectors.testconnector
{
    #region MyTstConnection
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
    #endregion

    #region TstAbstractConnector
    public abstract class TstAbstractConnector : CreateOp, SearchOp<Filter>, SyncOp, DeleteOp
    {

        internal sealed class ResourceComparator : IComparer<ConnectorObject>
        {
            private readonly IList<ICF.SortKey> sortKeys;

            public ResourceComparator(ICF.SortKey[] sortKeys)
            {
                this.sortKeys = sortKeys;
            }


            public int Compare(ConnectorObject r1, ConnectorObject r2)
            {
                foreach (ICF.SortKey sortKey in sortKeys)
                {
                    int result = Compare(r1, r2, sortKey);
                    if (result != 0)
                    {
                        return result;
                    }
                }
                return 0;
            }

            private int Compare(ConnectorObject r1, ConnectorObject r2, ICF.SortKey sortKey)
            {
                IList<object> vs1 = ValuesSorted(r1, sortKey.Field);
                IList<object> vs2 = ValuesSorted(r2, sortKey.Field);
                if (vs1.Count == 0 && vs2.Count == 0)
                {
                    return 0;
                }
                else if (vs1.Count == 0)
                {
                    // Sort resources with missing attributes last.
                    return 1;
                }
                else if (vs2.Count == 0)
                {
                    // Sort resources with missing attributes last.
                    return -1;
                }
                else
                {
                    object v1 = vs1[0];
                    object v2 = vs2[0];
                    return sortKey.AscendingOrder ? CompareValues(v1, v2) : -CompareValues(v1, v2);
                }
            }

            private IList<object> ValuesSorted(ConnectorObject resource, string field)
            {
                ConnectorAttribute value = resource.GetAttributeByName(field);
                if (value == null || value.Value == null || value.Value.Count == 0)
                {
                    return new List<object>();
                }
                else if (value.Value.Count > 1)
                {
                    List<object> results = new List<object>(value.Value);
                    results.Sort(VALUE_COMPARATOR);
                    return results;
                }
                else
                {
                    return value.Value;
                }
            }
        }

        private static readonly IComparer<object> VALUE_COMPARATOR = new ComparatorAnonymousInnerClassHelper();

        private class ComparatorAnonymousInnerClassHelper : IComparer<object>
        {
            public ComparatorAnonymousInnerClassHelper()
            {
            }

            public virtual int Compare(object o1, object o2)
            {
                return CompareValues(o1, o2);
            }
        }

        private static int CompareValues(object v1, object v2)
        {
            if (v1 is string && v2 is string)
            {
                string s1 = (string)v1;
                string s2 = (string)v2;
                return StringComparer.OrdinalIgnoreCase.Compare(s1, s2);
            }
            else if (v1 is double && v2 is double)
            {
                double n1 = (double)v1;
                double n2 = (double)v2;
                return n1.CompareTo(n2);
            }
            else if (v1 is int && v2 is int)
            {
                int n1 = (int)v1;
                int n2 = (int)v2;
                return n1.CompareTo(n2);
            }
            else if (v1 is bool && v2 is bool)
            {
                bool b1 = (bool)v1;
                bool b2 = (bool)v2;
                return b1.CompareTo(b2);
            }
            else
            {
                return v1.GetType().FullName.CompareTo(v2.GetType().FullName);
            }
        }

        protected TstStatefulConnectorConfig _config;

        public void Init(Configuration cfg)
        {
            _config = (TstStatefulConnectorConfig)cfg;
            Guid g = _config.Guid;
        }

        public Uid Create(ObjectClass objectClass, ICollection<ConnectorAttribute> createAttributes, OperationOptions options)
        {
            ConnectorAttributesAccessor accessor = new ConnectorAttributesAccessor(createAttributes);
            if (accessor.HasAttribute("fail"))
            {
                throw new ConnectorException("Test Exception");
            }
            else if (accessor.HasAttribute("exist") && accessor.FindBoolean("exist") == true)
            {
                throw new AlreadyExistsException(accessor.GetName().GetNameValue());
            }
            return new Uid(_config.Guid.ToString());
        }

        public void Delete(ObjectClass objectClass, Uid uid, OperationOptions options)
        {
            if (null == uid.Revision)
            {
                throw new PreconditionRequiredException("Version is required for MVCC");
            }
            else if (_config.Guid.ToString().Equals(uid.Revision))
            {
                // Delete
                String a = _config.Guid.ToString();
                String b = _config.Guid.ToString();
                String c = _config.Guid.ToString();

            }
            else
            {
                throw new PreconditionFailedException("Current version of resource is 0 and not match with: " + uid.Revision);
            }
        }

        public FilterTranslator<Filter> CreateFilterTranslator(ObjectClass objectClass, OperationOptions options)
        {
            return new FilterTranslatorAnonymousInnerClassHelper();
        }

        private class FilterTranslatorAnonymousInnerClassHelper : FilterTranslator<Filter>
        {

            public FilterTranslatorAnonymousInnerClassHelper()
            {
            }

            public IList<Filter> Translate(Filter filter)
            {
                List<Filter> filters = new List<Filter>(1);
                filters.Add(filter);
                return filters;
            }
        }
        public void ExecuteQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options)
        {

            ICF.SortKey[] sortKeys = options.SortKeys;
            if (null == sortKeys)
            {
                sortKeys = new ICF.SortKey[] { new ICF.SortKey(Name.NAME, true) };
            }

            // Rebuild the full result set.
            SortedSet<ConnectorObject> resultSet = new SortedSet<ConnectorObject>(new ResourceComparator(sortKeys));

            if (null != query)
            {
                foreach (ConnectorObject co in collection.Values)
                {
                    if (query.Accept(co))
                    {
                        resultSet.Add(co);
                    }
                }
            }
            else
            {
                resultSet.UnionWith(collection.Values);
            }
            // Handle the results
            if (null != options.PageSize)
            {
                // Paged Search
                string pagedResultsCookie = options.PagedResultsCookie;
                string currentPagedResultsCookie = options.PagedResultsCookie;
                int? pagedResultsOffset = null != options.PagedResultsOffset ? Math.Max(0, (int)options.PagedResultsOffset) : 0;
                int? pageSize = options.PageSize;
                int index = 0;
                int pageStartIndex = null == pagedResultsCookie ? 0 : -1;
                int handled = 0;
                foreach (ConnectorObject entry in resultSet)
                {
                    if (pageStartIndex < 0 && pagedResultsCookie.Equals(entry.Name.GetNameValue()))
                    {
                        pageStartIndex = index + 1;
                    }
                    if (pageStartIndex < 0 || index < pageStartIndex)
                    {
                        index++;
                        continue;
                    }
                    if (handled >= pageSize)
                    {
                        break;
                    }
                    if (index >= pagedResultsOffset + pageStartIndex)
                    {
                        if (handler.Handle(entry))
                        {
                            handled++;
                            currentPagedResultsCookie = entry.Name.GetNameValue();
                        }
                        else
                        {
                            break;
                        }
                    }
                    index++;
                }

                if (index == resultSet.Count)
                {
                    currentPagedResultsCookie = null;
                }

                if (handler is SearchResultsHandler)
                {
                    ((SearchResultsHandler)handler).HandleResult(new SearchResult(currentPagedResultsCookie, resultSet.Count - index));
                }
            }
            else
            {
                // Normal Search
                foreach (ConnectorObject entry in resultSet)
                {
                    if (!handler.Handle(entry))
                    {
                        break;
                    }
                }
                if (handler is SearchResultsHandler)
                {
                    ((SearchResultsHandler)handler).HandleResult(new SearchResult());
                }
            }
        }

        public void Sync(ObjectClass objectClass, SyncToken token, SyncResultsHandler handler, OperationOptions options)
        {
            if (handler is SyncTokenResultsHandler)
            {
                ((SyncTokenResultsHandler)handler).HandleResult(GetLatestSyncToken(objectClass));
            }
        }

        public SyncToken GetLatestSyncToken(ObjectClass objectClass)
        {
            return new SyncToken(_config.Guid.ToString());
        }

        private static readonly SortedDictionary<string, ConnectorObject> collection = new SortedDictionary<string, ConnectorObject>(StringComparer.InvariantCultureIgnoreCase);
        static TstAbstractConnector()
        {
            bool enabled = true;
            for (int i = 0; i < 100; i++)
            {
                ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
                builder.SetUid(Convert.ToString(i));
                builder.SetName(string.Format("user{0:D3}", i));
                builder.AddAttribute(ConnectorAttributeBuilder.BuildEnabled(enabled));
                ConnectorObject co = builder.Build();
                collection[co.Name.GetNameValue()] = co;
                enabled = !enabled;
            }
        }

    }
    #endregion

    #region TstConnector
    [ConnectorClass("TestConnector",
                    "TestConnector.category",
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
                String version = GetVersion();
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

        public static String GetVersion()
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
            int remaining = _config.numResults;
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
                if (handler.Handle(rv))
                {
                    remaining--;
                }
                else
                {
                    break;
                }
            }

            if (handler is SearchResultsHandler)
            {
                ((SearchResultsHandler)handler).HandleResult(new SearchResult("", remaining));
            }
        }
        public void Sync(ObjectClass objClass, SyncToken token,
                         SyncResultsHandler handler,
                         OperationOptions options)
        {
            int remaining = _config.numResults;
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
                if (handler.Handle(rv))
                {
                    remaining--;
                }
                else
                {
                    break; ;
                }
            }
            if (handler is SyncTokenResultsHandler)
            {
                ((SyncTokenResultsHandler)handler).HandleResult(new SyncToken(remaining));
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
    #endregion

    #region TstConnectorConfig
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
    #endregion

    #region TstStatefulConnector
    [ConnectorClass("TestStatefulConnector",
                "TestStatefulConnector.category",
                typeof(TstStatefulConnectorConfig),
                MessageCatalogPaths = new String[] { "TestBundleV1.Messages" }
                    )]
    public class TstStatefulConnector : TstAbstractConnector, Connector
    {

        //public void Init(Configuration cfg)
        //{
        //    base.Init(cfg);
        //}

        public Configuration Configuration
        {
            get
            {
                return _config;
            }
        }

        public void Dispose()
        {
            _config = null;
        }
    }
    #endregion

    #region TstStatefulConnectorConfig
    public class TstStatefulConnectorConfig : TstConnectorConfig, StatefulConfiguration
    {

        private Guid? guid;

        public Guid Guid
        {
            get
            {
                lock (this)
                {
                    if (null == guid)
                    {
                        guid = Guid.NewGuid();
                    }
                    return (Guid)guid;
                }
            }
        }

        public void Release()
        {
            guid = null;
        }
    }
    #endregion

    #region TstStatefulPoolableConnector
    [ConnectorClass("TestStatefulPoolableConnector",
                "TestStatefulPoolableConnector.category",
                typeof(TstStatefulConnectorConfig),
                MessageCatalogPaths = new String[] { "TestBundleV1.Messages" }
                    )]
    public class TstStatefulPoolableConnector : TstAbstractConnector, PoolableConnector
    {

        //public void Init(Configuration cfg)
        //{
        //    base.Init(cfg);
        //}

        public Configuration Configuration
        {
            get
            {
                return _config;
            }
        }

        public void Dispose()
        {
            _config = null;
        }

        public void CheckAlive()
        {
        }

    }
    #endregion
}
