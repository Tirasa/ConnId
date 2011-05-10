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
using System.Diagnostics;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Common.Proxy;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Impl.Api.Remote;
using Org.IdentityConnectors.Framework.Impl.Api.Local.Operations;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
using System.Collections.Generic;
using System.Globalization;
using System.Reflection;
using System.Resources;
using System.Threading;
using System.IO;
using System.Linq;
namespace Org.IdentityConnectors.Framework.Impl.Api.Local
{
    #region ConnectorPoolManager
    public class ConnectorPoolManager
    {
        private class ConnectorPoolKey
        {
            private readonly ConnectorKey _connectorKey;
            private readonly ConfigurationPropertiesImpl _configProperties;
            private readonly ObjectPoolConfiguration _poolingConfig;
            public ConnectorPoolKey(ConnectorKey connectorKey,
                    ConfigurationPropertiesImpl configProperties,
                    ObjectPoolConfiguration poolingConfig)
            {
                _connectorKey = connectorKey;
                _configProperties = configProperties;
                _poolingConfig = poolingConfig;
            }
            public override int GetHashCode()
            {
                return _connectorKey.GetHashCode();
            }
            public override bool Equals(Object o)
            {
                if (o is ConnectorPoolKey)
                {
                    ConnectorPoolKey other = (ConnectorPoolKey)o;
                    if (!_connectorKey.Equals(other._connectorKey))
                    {
                        return false;
                    }
                    if (!_configProperties.Equals(other._configProperties))
                    {
                        return false;
                    }
                    if (!_poolingConfig.Equals(other._poolingConfig))
                    {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        }

        private class ConnectorPoolHandler : ObjectPoolHandler<PoolableConnector>
        {
            private readonly APIConfigurationImpl _apiConfiguration;
            private readonly LocalConnectorInfoImpl _localInfo;
            public ConnectorPoolHandler(APIConfigurationImpl apiConfiguration,
                    LocalConnectorInfoImpl localInfo)
            {
                _apiConfiguration = apiConfiguration;
                _localInfo = localInfo;
            }
            public PoolableConnector NewObject()
            {
                Configuration config =
                    CSharpClassProperties.CreateBean((ConfigurationPropertiesImpl)_apiConfiguration.ConfigurationProperties,
                        _localInfo.ConnectorConfigurationClass);
                PoolableConnector connector =
                    (PoolableConnector)_localInfo.ConnectorClass.CreateInstance();
                connector.Init(config);
                return connector;
            }
            public void TestObject(PoolableConnector obj)
            {
                obj.CheckAlive();
            }
            public void DisposeObject(PoolableConnector obj)
            {
                obj.Dispose();
            }
        }

        /// <summary>
        /// Cache of the various _pools..
        /// </summary>
        private static readonly IDictionary<ConnectorPoolKey, ObjectPool<PoolableConnector>>
            _pools = new Dictionary<ConnectorPoolKey, ObjectPool<PoolableConnector>>();


        /// <summary>
        /// Get a object pool for this connector if it supports connector pooling.
        /// </summary>
        public static ObjectPool<PoolableConnector> GetPool(APIConfigurationImpl impl,
                LocalConnectorInfoImpl localInfo)
        {
            ObjectPool<PoolableConnector> pool = null;
            // determine if this connector wants generic connector pooling..
            if (impl.IsConnectorPoolingSupported)
            {
                ConnectorPoolKey key =
                    new ConnectorPoolKey(
                            impl.ConnectorInfo.ConnectorKey,
                            (ConfigurationPropertiesImpl)impl.ConfigurationProperties,
                            impl.ConnectorPoolConfiguration);

                lock (_pools)
                {
                    // get the pool associated..
                    pool = CollectionUtil.GetValue(_pools, key, null);
                    // create a new pool if it doesn't exist..
                    if (pool == null)
                    {
                        Trace.TraceInformation("Creating new pool: " +
                                impl.ConnectorInfo.ConnectorKey);
                        // this instance is strictly used for the pool..
                        pool = new ObjectPool<PoolableConnector>(
                                new ConnectorPoolHandler(impl, localInfo),
                                impl.ConnectorPoolConfiguration);
                        // add back to the map of _pools..
                        _pools[key] = pool;
                    }
                }
            }
            return pool;
        }

        public static void Dispose()
        {
            lock (_pools)
            {
                // close each pool..
                foreach (ObjectPool<PoolableConnector> pool in _pools.Values)
                {
                    try
                    {
                        pool.Shutdown();
                    }
                    catch (Exception e)
                    {
                        TraceUtil.TraceException("Failed to close pool", e);
                    }
                }
                // clear the map of all _pools..
                _pools.Clear();
            }
        }
    }
    #endregion

    #region CSharpClassProperties
    internal static class CSharpClassProperties
    {
        public static ConfigurationPropertiesImpl
        CreateConfigurationProperties(Configuration defaultObject)
        {
            SafeType<Configuration> config = SafeType<Configuration>.Get(defaultObject);
            ConfigurationPropertiesImpl properties =
                new ConfigurationPropertiesImpl();
            IList<ConfigurationPropertyImpl> temp =
                new List<ConfigurationPropertyImpl>();
            IDictionary<string, PropertyInfo> descs = GetFilteredProperties(config);

            foreach (PropertyInfo desc in descs.Values)
            {

                String name = desc.Name;

                // get the configuration options..
                ConfigurationPropertyAttribute options =
                    GetPropertyOptions(desc);
                // use the options to set internal properties..
                int order = 0;
                String helpKey = name + ".help";
                String displKey = name + ".display";
                bool confidential = false;
                bool required = false;
                if (options != null)
                {
                    // determine the display and help keys..
                    if (!StringUtil.IsBlank(options.HelpMessageKey))
                    {
                        helpKey = options.HelpMessageKey;
                    }
                    if (!StringUtil.IsBlank(options.DisplayMessageKey))
                    {
                        displKey = options.DisplayMessageKey;
                    }
                    // determine the order..
                    order = options.Order;
                    required = options.Required;
                    confidential = options.Confidential;
                }
                Type type = desc.PropertyType;
                if (!FrameworkUtil.IsSupportedConfigurationType(type))
                {
                    const String MSG = "Property type ''{0}'' is not supported.";
                    throw new ArgumentException(String.Format(MSG, type));
                }

                Object value = desc.GetValue(defaultObject, null);

                ConfigurationPropertyImpl prop = new ConfigurationPropertyImpl();
                prop.IsConfidential = confidential;
                prop.IsRequired = required;
                prop.DisplayMessageKey = displKey;
                prop.HelpMessageKey = helpKey;
                prop.Name = name;
                prop.Order = order;
                prop.Value = value;
                prop.ValueType = type;
                prop.Operations = options == null ? null : TranslateOperations(options.Operations);

                temp.Add(prop);

            }
            properties.Properties = (temp);
            return properties;
        }

        private static ICollection<SafeType<APIOperation>> TranslateOperations(SafeType<SPIOperation>[] ops)
        {
            ICollection<SafeType<APIOperation>> set =
                new HashSet<SafeType<APIOperation>>();
            foreach (SafeType<SPIOperation> spi in ops)
            {
                CollectionUtil.AddAll(set, FrameworkUtil.Spi2Apis(spi));
            }
            return set;
        }

        public static Configuration
        CreateBean(ConfigurationPropertiesImpl properties,
        SafeType<Configuration> configType)
        {
            Configuration rv = configType.CreateInstance();
            rv.ConnectorMessages = properties.Parent.ConnectorInfo.Messages;
            MergeIntoBean(properties, rv);
            return rv;
        }

        public static void
        MergeIntoBean(ConfigurationPropertiesImpl properties,
        Configuration config)
        {
            SafeType<Configuration> configType =
                SafeType<Configuration>.Get(config);
            IDictionary<string, PropertyInfo> descriptors =
                GetFilteredProperties(configType);
            foreach (ConfigurationPropertyImpl property in properties.Properties)
            {
                String name = property.Name;
                PropertyInfo desc =
                    CollectionUtil.GetValue(descriptors, name, null);
                if (desc == null)
                {
                    String FMT =
                        "Class ''{0}'' does not have a property ''{1}''.";
                    String MSG = String.Format(FMT,
                            configType.RawType.Name,
                            name);
                    throw new ArgumentException(MSG);
                }
                object val = property.Value;
                //some value types such as arrays
                //are mutable. make sure the config object
                //has its own copy
                val = SerializerUtil.CloneObject(val);
                desc.SetValue(config, val, null);
            }
        }

        private static IDictionary<string, PropertyInfo>
        GetFilteredProperties(SafeType<Configuration> config)
        {
            IDictionary<string, PropertyInfo> rv =
                new Dictionary<string, PropertyInfo>();
            PropertyInfo[] descriptors = config.RawType.GetProperties();
            foreach (PropertyInfo descriptor in descriptors)
            {
                String propName = descriptor.Name;
                if (!descriptor.CanWrite)
                {
                    //if there's no setter, ignore it
                    continue;
                }
                if ("ConnectorMessages".Equals(propName))
                {
                    continue;
                }
                if (!descriptor.CanRead)
                {
                    const String FMT =
                        "Found setter ''{0}'' but not the corresponding getter.";
                    String MSG = String.Format(FMT, propName);
                    throw new ArgumentException(MSG);
                }
                rv[propName] = descriptor;
            }
            return rv;
        }

        /// <summary>
        /// Get the option from the property.
        /// </summary>
        private static ConfigurationPropertyAttribute GetPropertyOptions(
                PropertyInfo propertyInfo)
        {
            Object[] objs =
                propertyInfo.GetCustomAttributes(
                    typeof(ConfigurationPropertyAttribute), true);
            if (objs.Length == 0)
            {
                return null;
            }
            else
            {
                return (ConfigurationPropertyAttribute)objs[0];
            }
        }

    }
    #endregion

    #region LocalConnectorInfoManagerImpl
    internal class LocalConnectorInfoManagerImpl : ConnectorInfoManager
    {
        private IList<ConnectorInfo> _connectorInfo;

        public LocalConnectorInfoManagerImpl()
        {
            _connectorInfo = new List<ConnectorInfo>();
            Assembly assembly = Assembly.GetExecutingAssembly();
            FileInfo thisAssemblyFile =
               new FileInfo(assembly.Location);
            DirectoryInfo directory =
               thisAssemblyFile.Directory;
            FileInfo[] files =
               directory.GetFiles("*.Connector.dll");
            foreach (FileInfo file in files)
            {
                Assembly lib =
                    Assembly.LoadFrom(file.ToString());
                CollectionUtil.AddAll(_connectorInfo,
                                      ProcessAssembly(lib));
            }
            // also handle connector DLL file names with a version 
            FileInfo[] versionedFiles = directory.GetFiles("*.Connector-*.dll");
            foreach (FileInfo versionedFile in versionedFiles)
            {
                Assembly lib =
                    Assembly.LoadFrom(versionedFile.ToString());
                CollectionUtil.AddAll(_connectorInfo,
                                      ProcessAssembly(lib));
            }
        }

        private IList<ConnectorInfo> ProcessAssembly(Assembly assembly)
        {
            IList<ConnectorInfo> rv = new List<ConnectorInfo>();

            Type[] types = null;
            try
            {
                types = assembly.GetTypes();
            }
            catch (Exception e)
            {
                TraceUtil.TraceException("Unable to load assembly: " + assembly.FullName + ". Assembly will be ignored.", e);
            }

            foreach (Type type in types)
            {
                Object[] attributes = type.GetCustomAttributes(
                    typeof(ConnectorClassAttribute),
                    false);
                if (attributes.Length > 0)
                {
                    ConnectorClassAttribute attribute =
                        (ConnectorClassAttribute)attributes[0];
                    LocalConnectorInfoImpl info =
                        CreateConnectorInfo(assembly, type, attribute);
                    rv.Add(info);
                }
            }
            return rv;
        }

        private LocalConnectorInfoImpl CreateConnectorInfo(Assembly assembly,
                                                           Type rawConnectorClass,
                                                           ConnectorClassAttribute attribute)
        {
            String fileName = assembly.Location;
            if (!typeof(Connector).IsAssignableFrom(rawConnectorClass))
            {
                String MSG = ("File " + fileName +
                               " declares a connector " + rawConnectorClass +
                               " that does not implement Connector.");
                throw new ConfigurationException(MSG);
            }
            SafeType<Connector> connectorClass =
                SafeType<Connector>.ForRawType(rawConnectorClass);
            SafeType<Configuration> connectorConfigurationClass = attribute.ConnectorConfigurationType;
            if (connectorConfigurationClass == null)
            {
                String MSG = ("File " + fileName +
                             " contains a ConnectorInfo attribute " +
                             "with no connector configuration class.");
                throw new ConfigurationException(MSG);
            }
            String connectorDisplayNameKey =
                attribute.ConnectorDisplayNameKey;
            if (connectorDisplayNameKey == null)
            {
                String MSG = ("File " + fileName +
                              " contains a ConnectorInfo attribute " +
                              "with no connector display name.");
                throw new ConfigurationException(MSG);
            }
            ConnectorKey key =
                new ConnectorKey(assembly.GetName().Name,
                                 assembly.GetName().Version.ToString(),
                                 connectorClass.RawType.Namespace + "." + connectorClass.RawType.Name);
            LocalConnectorInfoImpl rv = new LocalConnectorInfoImpl();
            rv.ConnectorClass = connectorClass;
            rv.ConnectorConfigurationClass = connectorConfigurationClass;
            rv.ConnectorDisplayNameKey = connectorDisplayNameKey;
            rv.ConnectorKey = key;
            rv.DefaultAPIConfiguration = CreateDefaultAPIConfiguration(rv);
            rv.Messages = LoadMessages(assembly, rv, attribute.MessageCatalogPaths);
            return rv;
        }

        private APIConfigurationImpl
        CreateDefaultAPIConfiguration(LocalConnectorInfoImpl localInfo)
        {
            SafeType<Connector> connectorClass =
                localInfo.ConnectorClass;
            APIConfigurationImpl rv = new APIConfigurationImpl();
            Configuration config =
                localInfo.ConnectorConfigurationClass.CreateInstance();
            bool pooling = IsPoolingSupported(connectorClass);
            rv.IsConnectorPoolingSupported = pooling;
            rv.ConfigurationProperties = (CSharpClassProperties.CreateConfigurationProperties(config));
            rv.ConnectorInfo = (localInfo);
            rv.SupportedOperations = (FrameworkUtil.GetDefaultSupportedOperations(connectorClass));
            return rv;
        }

        private static bool IsPoolingSupported(SafeType<Connector> clazz)
        {
            return ReflectionUtil.IsParentTypeOf(typeof(PoolableConnector), clazz.RawType);
        }

        /// <summary>
        /// Given an assembly, returns the list of cultures that
        /// it is localized for
        /// </summary>
        /// <param name="assembly"></param>
        /// <returns></returns>
        private CultureInfo[] GetLocalizedCultures(Assembly assembly)
        {
            FileInfo assemblyFile =
               new FileInfo(assembly.Location);
            DirectoryInfo directory =
               assemblyFile.Directory;
            IList<CultureInfo> temp = new List<CultureInfo>();
            DirectoryInfo[] subdirs = directory.GetDirectories();
            foreach (DirectoryInfo subdir in subdirs)
            {
                String name = subdir.Name;
                CultureInfo cultureInfo;
                //get the culture if the directory is the name of the
                //culture
                try
                {
                    cultureInfo = new CultureInfo(name);
                }
                catch (ArgumentException)
                {
                    //invalid culture
                    continue;
                }
                //see if there's a satellite assembly for this
                try
                {
                    assembly.GetSatelliteAssembly(cultureInfo);
                }
                catch (Exception)
                {
                    //invalid assembly
                    continue;
                }
                temp.Add(cultureInfo);
            }
            temp.Add(CultureInfo.InvariantCulture);
            return temp.ToArray();
        }

        private ConnectorMessagesImpl LoadMessages(Assembly assembly,
                                                   LocalConnectorInfoImpl info,
                                                   String[] nameBases)
        {
            if (nameBases == null || nameBases.Length == 0)
            {
                String pkage =
                    info.ConnectorClass.RawType.Namespace;
                nameBases = new String[] { pkage + ".Messages" };
            }
            ConnectorMessagesImpl rv = new ConnectorMessagesImpl();
            CultureInfo[] cultures = GetLocalizedCultures(assembly);
            for (int i = nameBases.Length - 1; i >= 0; i--)
            {
                String nameBase = nameBases[i];
                ResourceManager manager = new ResourceManager(nameBase, assembly);
                foreach (CultureInfo culture in cultures)
                {
                    ResourceSet resourceSet = manager.GetResourceSet(culture, true, false);
                    if (resourceSet != null)
                    {
                        IDictionary<string, string> temp =
                            CollectionUtil.GetValue(rv.Catalogs, culture, null);
                        if (temp == null)
                        {
                            temp = new Dictionary<string, string>();
                            rv.Catalogs[culture] = temp;
                        }
                        foreach (System.Collections.DictionaryEntry entry in resourceSet)
                        {
                            String key = "" + entry.Key;
                            String val = "" + entry.Value;
                            temp[key] = val;
                        }
                    }
                }
            }

            return rv;
        }

        public ConnectorInfo FindConnectorInfo(ConnectorKey key)
        {
            foreach (ConnectorInfo info in _connectorInfo)
            {
                if (info.ConnectorKey.Equals(key))
                {
                    return info;
                }
            }
            return null;
        }
        public IList<ConnectorInfo> ConnectorInfos
        {
            get
            {
                return CollectionUtil.AsReadOnlyList(_connectorInfo);
            }
        }
    }
    #endregion

    #region LocalConnectorInfoImpl
    /// <summary>
    /// Internal class, public only for unit tests
    /// </summary>
    public class LocalConnectorInfoImpl : AbstractConnectorInfo
    {
        public RemoteConnectorInfoImpl ToRemote()
        {
            RemoteConnectorInfoImpl rv = new RemoteConnectorInfoImpl();
            rv.ConnectorDisplayNameKey = ConnectorDisplayNameKey;
            rv.ConnectorKey = ConnectorKey;
            rv.DefaultAPIConfiguration = DefaultAPIConfiguration;
            rv.Messages = Messages;
            return rv;
        }
        public SafeType<Connector> ConnectorClass { get; set; }
        public SafeType<Configuration> ConnectorConfigurationClass { get; set; }
    }
    #endregion

    #region LocalConnectorFacadeImpl
    internal class LocalConnectorFacadeImpl : AbstractConnectorFacade
    {
        // =======================================================================
        // Constants
        // =======================================================================
        /// <summary>
        /// Map the API interfaces to their implementation counterparts.
        /// </summary>
        private static readonly IDictionary<SafeType<APIOperation>, ConstructorInfo> API_TO_IMPL =
            new Dictionary<SafeType<APIOperation>, ConstructorInfo>();

        private static void AddImplementation(SafeType<APIOperation> inter,
                SafeType<APIOperation> impl)
        {
            ConstructorInfo info =
                impl.RawType.GetConstructor(new Type[]{typeof(ConnectorOperationalContext),
                                        typeof(Connector)});
            if (info == null)
            {
                throw new ArgumentException(impl + " does not define the proper constructor");
            }
            API_TO_IMPL[inter] = info;
        }

        static LocalConnectorFacadeImpl()
        {
            AddImplementation(SafeType<APIOperation>.Get<CreateApiOp>(),
                              SafeType<APIOperation>.Get<CreateImpl>());
            AddImplementation(SafeType<APIOperation>.Get<DeleteApiOp>(),
                              SafeType<APIOperation>.Get<DeleteImpl>());
            AddImplementation(SafeType<APIOperation>.Get<SchemaApiOp>(),
                              SafeType<APIOperation>.Get<SchemaImpl>());
            AddImplementation(SafeType<APIOperation>.Get<SearchApiOp>(),
                              SafeType<APIOperation>.Get<SearchImpl>());
            AddImplementation(SafeType<APIOperation>.Get<UpdateApiOp>(),
                              SafeType<APIOperation>.Get<UpdateImpl>());
            AddImplementation(SafeType<APIOperation>.Get<AuthenticationApiOp>(),
                              SafeType<APIOperation>.Get<AuthenticationImpl>());
            AddImplementation(SafeType<APIOperation>.Get<ResolveUsernameApiOp>(),
                              SafeType<APIOperation>.Get<ResolveUsernameImpl>());
            AddImplementation(SafeType<APIOperation>.Get<TestApiOp>(),
                              SafeType<APIOperation>.Get<TestImpl>());
            AddImplementation(SafeType<APIOperation>.Get<ScriptOnConnectorApiOp>(),
                              SafeType<APIOperation>.Get<ScriptOnConnectorImpl>());
            AddImplementation(SafeType<APIOperation>.Get<ScriptOnResourceApiOp>(),
                              SafeType<APIOperation>.Get<ScriptOnResourceImpl>());
            AddImplementation(SafeType<APIOperation>.Get<SyncApiOp>(),
                              SafeType<APIOperation>.Get<SyncImpl>());
        }

        // =======================================================================
        // Fields
        // =======================================================================
        /// <summary>
        /// Pool used to acquire connection from to use during operations.
        /// </summary>

        /// <summary>
        /// The connector info
        /// </summary>
        private readonly LocalConnectorInfoImpl connectorInfo;

        /// <summary>
        /// Builds up the maps of supported operations and calls.
        /// </summary>
        public LocalConnectorFacadeImpl(LocalConnectorInfoImpl connectorInfo,
                APIConfigurationImpl apiConfiguration)
            : base(apiConfiguration)
        {
            this.connectorInfo = connectorInfo;
        }

        // =======================================================================
        // ConnectorFacade Interface
        // =======================================================================

        protected override APIOperation GetOperationImplementation(SafeType<APIOperation> api)
        {
            APIOperation proxy;
            //first create the inner proxy - this is the proxy that obtaining
            //a connection from the pool, etc
            //NOTE: we want to skip this part of the proxy for
            //validate op, but we will want the timeout proxy
            if (api.RawType.Equals(typeof(ValidateApiOp)))
            {
                OperationalContext context =
                    new OperationalContext(connectorInfo, GetAPIConfiguration());
                proxy = new ValidateImpl(context);
            }
            else if (api.RawType.Equals(typeof(GetApiOp)))
            {
                ConstructorInfo constructor =
                    API_TO_IMPL[SafeType<APIOperation>.Get<SearchApiOp>()];
                ConnectorOperationalContext context =
                new ConnectorOperationalContext(connectorInfo,
                        GetAPIConfiguration(),
                        GetPool());

                ConnectorAPIOperationRunnerProxy handler =
                    new ConnectorAPIOperationRunnerProxy(context, constructor);
                proxy =
                    new GetImpl((SearchApiOp)NewAPIOperationProxy(SafeType<APIOperation>.Get<SearchApiOp>(), handler));
            }
            else
            {
                ConstructorInfo constructor =
                    API_TO_IMPL[api];
                ConnectorOperationalContext context =
                new ConnectorOperationalContext(connectorInfo,
                        GetAPIConfiguration(),
                        GetPool());

                ConnectorAPIOperationRunnerProxy handler =
                    new ConnectorAPIOperationRunnerProxy(context, constructor);
                proxy =
                    NewAPIOperationProxy(api, handler);
            }

            //TODO: timeout

            // add logging proxy..
            proxy = CreateLoggingProxy(api, proxy);
            return proxy;
        }
        private ObjectPool<PoolableConnector> GetPool()
        {
            return ConnectorPoolManager.GetPool(GetAPIConfiguration(), connectorInfo);
        }
    }
    #endregion

    #region ObjectPool
    public class ObjectPool<T> where T : class
    {
        /// <summary>
        /// Statistics bean
        /// </summary>
        public sealed class Statistics
        {
            private readonly int _numIdle;
            private readonly int _numActive;

            internal Statistics(int numIdle, int numActive)
            {
                _numIdle = numIdle;
                _numActive = numActive;
            }

            /// <summary>
            /// Returns the number of idle objects
            /// </summary>
            public int NumIdle
            {
                get
                {
                    return _numIdle;
                }
            }

            /// <summary>
            /// Returns the number of active objects
            /// </summary>
            public int NumActive
            {
                get
                {
                    return _numActive;
                }
            }
        }

        /// <summary>
        /// An object plus additional book-keeping
        /// information about the object
        /// </summary>
        private class PooledObject<T2> where T2 : class
        {
            /// <summary>
            /// The underlying object
            /// </summary>
            private readonly T2 _object;

            /// <summary>
            /// True if this is currently active, false if
            /// it is idle
            /// </summary>
            private bool _isActive;

            /// <summary>
            /// Last state change (change from active to
            /// idle or vice-versa)
            /// </summary>
            private long _lastStateChangeTimestamp;

            /// <summary>
            /// Is this a freshly created object (never been pooled)?
            /// </summary>
            private bool _isNew;

            public PooledObject(T2 obj)
            {
                _object = obj;
                _isNew = true;
                Touch();
            }

            public T2 Object
            {
                get
                {
                    return _object;
                }
            }

            public bool IsActive
            {
                get
                {
                    return _isActive;
                }
                set
                {
                    if (_isActive != value)
                    {
                        Touch();
                        _isActive = value;
                    }
                }
            }

            public bool IsNew
            {
                get
                {
                    return _isNew;
                }
                set
                {
                    _isNew = value;
                }
            }


            private void Touch()
            {
                _lastStateChangeTimestamp = DateTimeUtil.GetCurrentUtcTimeMillis();
            }

            public long LastStateChangeTimestamp
            {
                get
                {
                    return _lastStateChangeTimestamp;
                }
            }
        }

        /// <summary>
        /// The lock object we use for everything
        /// </summary>
        private readonly Object LOCK = new Object();

        /// <summary>
        /// Map from the object to the
        /// PooledObject (use IdentityHashMap so it's
        /// always object equality)
        /// </summary>
        private readonly IDictionary<T, PooledObject<T>>
            _activeObjects = CollectionUtil.NewIdentityDictionary<T, PooledObject<T>>();

        /// <summary>
        /// Queue of idle objects.
        /// </summary>
        /// <remarks>
        /// The one that has
        /// been idle for the longest comes first in the queue
        /// </remarks>
        private readonly LinkedList<PooledObject<T>>
            _idleObjects = new LinkedList<PooledObject<T>>();

        /// <summary>
        /// ObjectPoolHandler we use for managing object lifecycle
        /// </summary>
        private readonly ObjectPoolHandler<T> _handler;

        /// <summary>
        /// Configuration for this pool.
        /// </summary>
        private readonly ObjectPoolConfiguration _config;

        /// <summary>
        /// Is the pool shutdown
        /// </summary>
        private bool _isShutdown;

        /// <summary>
        /// Create a new ObjectPool
        /// </summary>
        /// <param name="handler">Handler for objects</param>
        /// <param name="config">Configuration for the pool</param>
        public ObjectPool(ObjectPoolHandler<T> handler,
                ObjectPoolConfiguration config)
        {

            Assertions.NullCheck(handler, "handler");
            Assertions.NullCheck(config, "config");

            _handler = handler;
            //clone it
            _config =
                (ObjectPoolConfiguration)SerializerUtil.CloneObject(config);
            //validate it
            _config.Validate();

        }

        /// <summary>
        /// Return an object to the pool
        /// </summary>
        /// <param name="object"></param>
        public void ReturnObject(T obj)
        {
            Assertions.NullCheck(obj, "object");
            lock (LOCK)
            {
                //remove it from the active list
                PooledObject<T> pooled =
                    CollectionUtil.GetValue(_activeObjects, obj, null);

                //they are attempting to return something
                //we haven't allocated (or that they've
                //already returned)
                if (pooled == null)
                {
                    throw new InvalidOperationException("Attempt to return an object not in the pool: " + obj);
                }
                _activeObjects.Remove(obj);

                //set it to idle and add to idle list
                //(this might get evicted right away
                //by evictIdleObjects if we're over the
                //limit or if we're shutdown)
                pooled.IsActive = (false);
                pooled.IsNew = (false);
                _idleObjects.AddLast(pooled);

                //finally evict idle objects
                EvictIdleObjects();

                //wake anyone up who was waiting on a object
                Monitor.PulseAll(LOCK);
            }
        }

        /// <summary>
        /// Borrow an object from the pool.
        /// </summary>
        /// <returns>An object</returns>
        public T BorrowObject()
        {
            while (true)
            {
                PooledObject<T> rv = BorrowObjectNoTest();
                try
                {
                    //make sure we are testing it outside
                    //of synchronization. otherwise this
                    //can create an IO bottleneck
                    _handler.TestObject(rv.Object);
                    return rv.Object;
                }
                catch (Exception e)
                {
                    //it's bad - remove from active objects
                    lock (LOCK)
                    {
                        _activeObjects.Remove(rv.Object);
                    }
                    DisposeNoException(rv.Object);
                    //if it's a new object, break out of the loop
                    //immediately
                    if (rv.IsNew)
                    {
                        throw e;
                    }
                }
            }
        }

        /// <summary>
        /// Borrow an object from the pool, but don't test
        /// it (it gets tested by the caller *outside* of
        /// synchronization)
        /// </summary>
        /// <returns>the object</returns>
        private PooledObject<T> BorrowObjectNoTest()
        {
            //time when the call began
            long startTime = DateTimeUtil.GetCurrentUtcTimeMillis();

            lock (LOCK)
            {
                EvictIdleObjects();
                while (true)
                {
                    if (_isShutdown)
                    {
                        throw new InvalidOperationException("Object pool already shutdown");
                    }

                    PooledObject<T> pooledConn = null;

                    //first try to recycle an idle object
                    if (_idleObjects.Count > 0)
                    {
                        pooledConn = _idleObjects.First();
                        _idleObjects.RemoveFirst();
                    }
                    //otherwise, allocate a new object if
                    //below the limit
                    else if (_activeObjects.Count < _config.MaxObjects)
                    {
                        pooledConn =
                            new PooledObject<T>(_handler.NewObject());
                    }

                    //if there's an object available, return it
                    //and break out of the loop
                    if (pooledConn != null)
                    {
                        pooledConn.IsActive = (true);
                        _activeObjects[pooledConn.Object] =
                                pooledConn;
                        return pooledConn;
                    }

                    //see if we haven't timed-out yet
                    long elapsed =
                        DateTimeUtil.GetCurrentUtcTimeMillis() - startTime;
                    long remaining = _config.MaxWait - elapsed;

                    //wait if we haven't timed out
                    if (remaining > 0)
                    {
                        Monitor.Wait(LOCK, (int)remaining);
                    }
                    else
                    {
                        //otherwise throw
                        throw new ConnectorException("Max objects exceeded");
                    }
                }
            }
        }

        /// <summary>
        /// Closes any idle objects in the pool.
        /// </summary>
        /// <remarks>
        /// Existing active objects will remain alive and
        /// be allowed to shutdown gracefully, but no more
        /// objects will be allocated.
        /// </remarks>
        public void Shutdown()
        {
            lock (LOCK)
            {
                _isShutdown = true;
                //just evict idle objects
                //if there are any active objects still
                //going, leave them alone so they can return
                //gracefully
                EvictIdleObjects();
                //wake anyone up who was waiting on an object
                Monitor.PulseAll(LOCK);
            }
        }

        /// <summary>
        /// Gets a snapshot of the pool's stats at a point in time.
        /// </summary>
        /// <returns>The statistics</returns>
        public Statistics GetStatistics()
        {
            lock (LOCK)
            {
                return new Statistics(_idleObjects.Count,
                        _activeObjects.Count);
            }
        }

        /// <summary>
        /// Evicts idle objects as needed (evicts
        /// all idle objects if we're shutdown)
        /// </summary>
        private void EvictIdleObjects()
        {
            while (TooManyIdleObjects())
            {
                PooledObject<T> conn = _idleObjects.First();
                _idleObjects.RemoveFirst();
                DisposeNoException(conn.Object);
            }
        }

        /// <summary>
        /// Returns true if any of the following are true:
        /// <list type="number">
        /// <item>
        /// <description>We're shutdown and there are idle objects
        /// </description>
        /// </item>
        /// <item>
        /// <description>Max idle objects exceeded
        /// </description>
        /// </item>
        /// <item>
        /// <description>Min idle objects exceeded and there are old objects
        /// </description>
        /// </item>
        /// </list>
        /// </summary>
        private bool TooManyIdleObjects()
        {

            if (_isShutdown && _idleObjects.Count > 0)
            {
                return true;
            }

            if (_config.MaxIdle < _idleObjects.Count)
            {
                return true;
            }
            if (_config.MinIdle >= _idleObjects.Count)
            {
                return false;
            }

            PooledObject<T> oldest =
                _idleObjects.First();

            long age =
                (DateTimeUtil.GetCurrentUtcTimeMillis() - oldest.LastStateChangeTimestamp);


            return age > _config.MinEvictableIdleTimeMillis;
        }

        /// <summary>
        /// Dispose of an object, but don't throw any exceptions
        /// </summary>
        /// <param name="object"></param>
        private void DisposeNoException(T obj)
        {
            try
            {
                _handler.DisposeObject(obj);
            }
            catch (Exception e)
            {
                TraceUtil.TraceException("disposeObject() is not supposed to throw", e);
            }
        }
    }
    #endregion

    #region ObjectPoolHandler
    public interface ObjectPoolHandler<T> where T : class
    {
        T NewObject();
        void TestObject(T obj);
        void DisposeObject(T obj);
    }
    #endregion
}