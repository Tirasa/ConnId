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

using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Impl.Api.Local;
using Org.IdentityConnectors.Framework.Impl.Api.Remote;
using Org.IdentityConnectors.Framework.Impl.Serializer.Binary;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Common.Proxy;
using Org.IdentityConnectors.Common.Security;
using System.Linq;
using System.Collections.Generic;
using System.Globalization;
using System.Resources;
using System.Reflection;
using System.Reflection.Emit;
using System.Diagnostics;
using System.Text;

namespace Org.IdentityConnectors.Framework.Impl.Api
{
    #region ConfigurationPropertyImpl
    /// <summary>
    /// Internal class, public only for unit tests
    /// </summary>
    public class ConfigurationPropertyImpl : ConfigurationProperty
    {
        private ICollection<SafeType<APIOperation>> _operations;

        public ICollection<SafeType<APIOperation>> Operations
        {
            get
            {
                return _operations;
            }
            set
            {
                _operations = CollectionUtil.NewReadOnlySet(value);
            }
        }

        public ConfigurationPropertiesImpl Parent { get; set; }

        public int Order { get; set; }

        public string Name { get; set; }

        public string HelpMessageKey { get; set; }

        public string DisplayMessageKey { get; set; }

        public string GetHelpMessage(string def)
        {
            return FormatMessage(HelpMessageKey, def);
        }

        public string GetDisplayName(string def)
        {
            return FormatMessage(DisplayMessageKey, def);
        }

        public object Value { get; set; }

        public Type ValueType { get; set; }

        public bool IsConfidential { get; set; }

        public bool IsRequired { get; set; }

        public override int GetHashCode()
        {
            return Name.GetHashCode();
        }

        public override bool Equals(Object o)
        {
            ConfigurationPropertyImpl other = o as ConfigurationPropertyImpl;
            if (other != null)
            {
                if (!Name.Equals(other.Name))
                {
                    return false;
                }
                if (!CollectionUtil.Equals(Value, other.Value))
                {
                    return false;
                }
                if (Order != other.Order)
                {
                    return false;
                }
                if (!CollectionUtil.Equals(HelpMessageKey, other.HelpMessageKey))
                {
                    return false;
                }
                if (!CollectionUtil.Equals(DisplayMessageKey, other.DisplayMessageKey))
                {
                    return false;
                }
                if (IsConfidential != other.IsConfidential)
                {
                    return false;
                }
                if (IsRequired != other.IsRequired)
                {
                    return false;
                }
                if (!CollectionUtil.Equals(ValueType, other.ValueType))
                {
                    return false;
                }
                if (!CollectionUtil.Equals(_operations, other._operations))
                {
                    return false;
                }

                return true;
            }
            return false;
        }
        private String FormatMessage(String key, String dflt, params object[] args)
        {
            APIConfigurationImpl apiConfig = Parent.Parent;
            ConnectorMessages messages =
                apiConfig.ConnectorInfo.Messages;
            return messages.Format(key, dflt, args);
        }
    }
    #endregion

    #region ConfigurationPropertiesImpl
    /// <summary>
    /// Internal class, public only for unit tests
    /// </summary>
    public class ConfigurationPropertiesImpl : ConfigurationProperties
    {
        //properties, sorted by "order"
        private IList<ConfigurationPropertyImpl> _properties;
        //property names, sorted by "order
        private IList<string> _propertyNames;
        private IDictionary<string, ConfigurationPropertyImpl> _propertiesByName;

        public IList<ConfigurationPropertyImpl> Properties
        {
            get
            {
                return _properties;
            }
            set
            {
                ConfigurationPropertyImpl[] arr =
                    value == null ? new ConfigurationPropertyImpl[0] : value.ToArray();

                Array.Sort(arr,
                           (p1, p2) => { return p1.Order.CompareTo(p2.Order); });
                _properties =
                    CollectionUtil.NewReadOnlyList<ConfigurationPropertyImpl>(arr);
                IList<string> propertyNames = new List<string>();
                IDictionary<string, ConfigurationPropertyImpl> propertiesByName =
                    new Dictionary<string, ConfigurationPropertyImpl>();
                foreach (ConfigurationPropertyImpl property in _properties)
                {
                    propertyNames.Add(property.Name);
                    propertiesByName[property.Name] = property;
                    property.Parent = this;
                }
                _propertyNames = CollectionUtil.AsReadOnlyList(propertyNames);
                _propertiesByName = CollectionUtil.AsReadOnlyDictionary(propertiesByName);
            }
        }
        public ConfigurationProperty GetProperty(String name)
        {
            return CollectionUtil.GetValue(_propertiesByName, name, null);
        }
        public IList<string> PropertyNames
        {
            get
            {
                return _propertyNames;
            }
        }
        public APIConfigurationImpl Parent { get; set; }
        public void SetPropertyValue(String name, object val)
        {
            ConfigurationProperty property = GetProperty(name);
            if (property == null)
            {
                String MSG = "Property '" + name + "' does not exist.";
                throw new ArgumentException(MSG);
            }
            property.Value = val;
        }

        public override int GetHashCode()
        {
            return CollectionUtil.GetHashCode(_properties);
        }

        public override bool Equals(object o)
        {
            ConfigurationPropertiesImpl other = o as ConfigurationPropertiesImpl;
            if (other != null)
            {
                return CollectionUtil.SetsEqual(_properties,
                                                other._properties);
            }
            return false;
        }
    }
    #endregion

    #region APIConfigurationImpl
    /// <summary>
    /// Internal class, public only for unit tests
    /// </summary>
    public class APIConfigurationImpl : APIConfiguration
    {
        private ObjectPoolConfiguration _connectorPooling;

        private ConfigurationPropertiesImpl _configurationProperties;
        private ICollection<SafeType<APIOperation>> _supportedOperations =
            CollectionUtil.NewReadOnlySet<SafeType<APIOperation>>(new SafeType<APIOperation>[0]);

        private IDictionary<SafeType<APIOperation>, int> _timeoutMap =
            new Dictionary<SafeType<APIOperation>, int>();

        public ConfigurationProperties ConfigurationProperties
        {
            get
            {
                return _configurationProperties;
            }
            set
            {
                if (_configurationProperties != null)
                {
                    _configurationProperties.Parent = null;
                }
                _configurationProperties = (ConfigurationPropertiesImpl)value;
                if (_configurationProperties != null)
                {
                    _configurationProperties.Parent = this;
                }
            }
        }
        public IDictionary<SafeType<APIOperation>, int> TimeoutMap
        {
            get
            {
                return _timeoutMap;
            }
            set
            {
                _timeoutMap = value;
            }
        }
        public bool IsConnectorPoolingSupported { get; set; }
        public ObjectPoolConfiguration ConnectorPoolConfiguration
        {
            get
            {
                if (_connectorPooling == null)
                {
                    _connectorPooling = new ObjectPoolConfiguration();
                }
                return _connectorPooling;
            }
            set
            {
                _connectorPooling = value;
            }
        }
        public ICollection<SafeType<APIOperation>> SupportedOperations
        {
            get
            {
                return _supportedOperations;
            }
            set
            {
                _supportedOperations = CollectionUtil.NewReadOnlySet<SafeType<APIOperation>>(value);
            }
        }

        public int GetTimeout(SafeType<APIOperation> operation)
        {
            return CollectionUtil.GetValue(_timeoutMap, operation,
                                           APIConstants.NO_TIMEOUT);
        }
        public void SetTimeout(SafeType<APIOperation> operation, int timeout)
        {
            _timeoutMap[operation] = timeout;
        }

        public AbstractConnectorInfo ConnectorInfo { get; set; }

        public int ProducerBufferSize { get; set; }
        public APIConfigurationImpl()
        {
            ProducerBufferSize = 100;
        }
    }
    #endregion

    #region AbstractConnectorInfo
    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class AbstractConnectorInfo : ConnectorInfo
    {
        private APIConfigurationImpl _defaultAPIConfiguration;


        public string GetConnectorDisplayName()
        {
            return Messages.Format(ConnectorDisplayNameKey,
                                   ConnectorKey.ConnectorName);
        }

        public ConnectorKey ConnectorKey { get; set; }

        public string ConnectorDisplayNameKey { get; set; }

        public ConnectorMessages Messages { get; set; }

        public APIConfigurationImpl DefaultAPIConfiguration
        {
            get
            {
                return _defaultAPIConfiguration;
            }
            set
            {
                if (value != null)
                {
                    value.ConnectorInfo = this;
                }
                _defaultAPIConfiguration = value;
            }
        }

        public APIConfiguration CreateDefaultAPIConfiguration()
        {
            APIConfigurationImpl rv =
            (APIConfigurationImpl)
            SerializerUtil.CloneObject(_defaultAPIConfiguration);
            rv.ConnectorInfo = this;
            return rv;
        }
    }
    #endregion

    #region ConnectorMessagesImpl
    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class ConnectorMessagesImpl : ConnectorMessages
    {
        private IDictionary<CultureInfo, IDictionary<string, string>>
            _catalogs = new Dictionary<CultureInfo, IDictionary<String, String>>();

        public String Format(String key, String dflt, params object[] args)
        {
            if (key == null)
            {
                return dflt;
            }
            CultureInfo locale = CultureInfo.CurrentUICulture;
            if (locale == null)
            {
                locale = CultureInfo.CurrentCulture;
            }
            if (dflt == null)
            {
                dflt = key;
            }
            CultureInfo foundCulture = locale;
            String message = GetCatalogMessage(foundCulture, key);
            //check neutral culture
            if (message == null)
            {
                foundCulture = foundCulture.Parent;
                message = GetCatalogMessage(foundCulture, key);
            }
            //check invariant culture
            if (message == null)
            {
                foundCulture = foundCulture.Parent;
                message = GetCatalogMessage(foundCulture, key);
            }
            //and default to framework
            if (message == null)
            {
                message = GetFrameworkMessage(locale, key);
            }
            if (message == null)
            {
                return dflt;
            }
            else
            {
                //TODO: think more about this since the formatting
                //is slightly different than Java
                return String.Format(foundCulture, message, args);
            }
        }

        private String GetCatalogMessage(CultureInfo culture, String key)
        {
            IDictionary<string, string> catalog = CollectionUtil.GetValue(_catalogs, culture, null);
            return catalog != null ? CollectionUtil.GetValue(catalog, key, null) : null;
        }

        private String GetFrameworkMessage(CultureInfo culture, String key)
        {
            ResourceManager manager =
                new ResourceManager("Org.IdentityConnectors.Resources",
                                    typeof(ConnectorMessagesImpl).Assembly);
            String contents = (String)manager.GetObject(key, culture);
            return contents;
        }

        public IDictionary<CultureInfo, IDictionary<string, string>> Catalogs
        {
            get
            {
                return _catalogs;
            }
            set
            {
                if (value == null)
                {
                    _catalogs =
                        new Dictionary<CultureInfo, IDictionary<string, string>>();
                }
                else
                {
                    _catalogs = value;
                }
            }
        }
    }
    #endregion

    #region ConnectorInfoManagerFactoryImpl
    public sealed class ConnectorInfoManagerFactoryImpl :
        ConnectorInfoManagerFactory
    {
        private class RemoteManagerKey
        {
            private readonly String _host;
            private readonly int _port;

            public RemoteManagerKey(RemoteFrameworkConnectionInfo info)
            {
                _host = info.Host;
                _port = info.Port;
            }

            public override bool Equals(Object o)
            {
                if (o is RemoteManagerKey)
                {
                    RemoteManagerKey other = (RemoteManagerKey)o;
                    if (!_host.Equals(other._host))
                    {
                        return false;
                    }
                    if (_port != other._port)
                    {
                        return false;
                    }
                    return true;
                }
                return false;
            }

            public override int GetHashCode()
            {
                return _host.GetHashCode() ^ _port;
            }

        }

        private Object LOCAL_LOCK = new Object();
        private Object REMOTE_LOCK = new Object();

        private ConnectorInfoManager
            _localManagerCache;

        private IDictionary<RemoteManagerKey, RemoteConnectorInfoManagerImpl>
        _remoteManagerCache = new Dictionary<RemoteManagerKey, RemoteConnectorInfoManagerImpl>();

        public ConnectorInfoManagerFactoryImpl()
        {

        }

        public override void ClearRemoteCache()
        {
            lock (REMOTE_LOCK)
            {
                _remoteManagerCache.Clear();
            }
        }

        public override ConnectorInfoManager GetLocalManager()
        {
            lock (LOCAL_LOCK)
            {
                ConnectorInfoManager rv = _localManagerCache;
                if (rv == null)
                {
                    rv = new LocalConnectorInfoManagerImpl();
                }
                _localManagerCache = rv;
                return rv;
            }
        }

        public override ConnectorInfoManager GetRemoteManager(RemoteFrameworkConnectionInfo info)
        {
            RemoteManagerKey key = new RemoteManagerKey(info);
            lock (REMOTE_LOCK)
            {
                RemoteConnectorInfoManagerImpl rv = CollectionUtil.GetValue(_remoteManagerCache, key, null);
                if (rv == null)
                {
                    rv = new RemoteConnectorInfoManagerImpl(info);
                }
                _remoteManagerCache[key] = rv;
                return rv.Derive(info);
            }
        }

    }
    #endregion

    #region AbstractConnectorFacade
    internal abstract class AbstractConnectorFacade : ConnectorFacade
    {
        private readonly APIConfigurationImpl _configuration;

        /// <summary>
        /// Builds up the maps of supported operations and calls.
        /// </summary>
        public AbstractConnectorFacade(APIConfigurationImpl configuration)
        {
            Assertions.NullCheck(configuration, "configuration");
            //clone in case application tries to modify
            //after the fact. this is necessary to
            //ensure thread-safety of a ConnectorFacade
            //also, configuration is used as a key in the
            //pool, so it is important that it not be modified.
            _configuration = (APIConfigurationImpl)SerializerUtil.CloneObject(configuration);
            //parent ref not included in the clone
            _configuration.ConnectorInfo = (configuration.ConnectorInfo);
        }

        /// <summary>
        /// Return an instance of an API operation.
        /// </summary>
        /// <returns>
        /// <code>null</code> if the operation is not support otherwise
        /// return an instance of the operation.</returns>
        /// <seealso cref="Org.IdentityConnectors.Framework.Api.ConnectorFacade.GetOperation(SafeType{APIOperation})" />
        public APIOperation GetOperation(SafeType<APIOperation> api)
        {
            if (!SupportedOperations.Contains(api))
            {
                return null;
            }
            return GetOperationImplementation(api);
        }

        public ICollection<SafeType<APIOperation>> SupportedOperations
        {
            get
            {
                return _configuration.SupportedOperations;
            }
        }

        // =======================================================================
        // Operation API Methods
        // =======================================================================
        public Schema Schema()
        {
            return ((SchemaApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<SchemaApiOp>()))
                    .Schema();
        }

        public Uid Create(ObjectClass oclass, ICollection<ConnectorAttribute> attrs, OperationOptions options)
        {
            CreateApiOp op = ((CreateApiOp)GetOperationCheckSupported(SafeType<APIOperation>.Get<CreateApiOp>()));
            return op.Create(oclass, attrs, options);
        }

        public void Delete(ObjectClass objClass, Uid uid, OperationOptions options)
        {
            ((DeleteApiOp)
             this.GetOperationCheckSupported(SafeType<APIOperation>.Get<DeleteApiOp>()))
                .Delete(objClass, uid, options);
        }

        public void Search(ObjectClass oclass, Filter filter, ResultsHandler handler, OperationOptions options)
        {
            ((SearchApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<SearchApiOp>())).Search(
                    oclass, filter, handler, options);
        }

        public Uid Update(ObjectClass objclass, Uid uid, ICollection<ConnectorAttribute> attrs, OperationOptions options)
        {
            return ((UpdateApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<UpdateApiOp>()))
                    .Update(objclass, uid, attrs, options);
        }

        public Uid AddAttributeValues(
                ObjectClass objclass,
                Uid uid,
                ICollection<ConnectorAttribute> attrs,
                OperationOptions options)
        {
            return ((UpdateApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<UpdateApiOp>()))
                .AddAttributeValues(objclass, uid, attrs, options);
        }

        public Uid RemoveAttributeValues(
                ObjectClass objclass,
                Uid uid,
                ICollection<ConnectorAttribute> attrs,
                OperationOptions options)
        {
            return ((UpdateApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<UpdateApiOp>()))
                .RemoveAttributeValues(objclass, uid, attrs, options);
        }

        public Uid Authenticate(ObjectClass objectClass, String username, GuardedString password, OperationOptions options)
        {
            return ((AuthenticationApiOp)this
             .GetOperationCheckSupported(SafeType<APIOperation>.Get<AuthenticationApiOp>())).Authenticate(
                    objectClass, username, password, options);
        }

        public Uid ResolveUsername(ObjectClass objectClass, String username, OperationOptions options)
        {
            return ((ResolveUsernameApiOp)this
             .GetOperationCheckSupported(SafeType<APIOperation>.Get<ResolveUsernameApiOp>())).ResolveUsername(
                    objectClass, username, options);
        }

        public ConnectorObject GetObject(ObjectClass objClass, Uid uid, OperationOptions options)
        {
            return ((GetApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<GetApiOp>()))
                    .GetObject(objClass, uid, options);
        }

        public Object RunScriptOnConnector(ScriptContext request,
                OperationOptions options)
        {
            return ((ScriptOnConnectorApiOp)this
                    .GetOperationCheckSupported(SafeType<APIOperation>.Get<ScriptOnConnectorApiOp>()))
                    .RunScriptOnConnector(request, options);
        }

        public Object RunScriptOnResource(ScriptContext request,
                OperationOptions options)
        {
            return ((ScriptOnResourceApiOp)this
                    .GetOperationCheckSupported(SafeType<APIOperation>.Get<ScriptOnResourceApiOp>()))
                    .RunScriptOnResource(request, options);
        }

        public void Test()
        {
            ((TestApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<TestApiOp>())).Test();
        }

        public void Validate()
        {
            ((ValidateApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<ValidateApiOp>())).Validate();
        }

        public void Sync(ObjectClass objClass, SyncToken token,
                SyncResultsHandler handler,
                OperationOptions options)
        {
            ((SyncApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<SyncApiOp>()))
            .Sync(objClass, token, handler, options);
        }

        public SyncToken GetLatestSyncToken(ObjectClass objectClass)
        {
            return ((SyncApiOp)this.GetOperationCheckSupported(SafeType<APIOperation>.Get<SyncApiOp>()))
            .GetLatestSyncToken(objectClass);
        }

        private APIOperation GetOperationCheckSupported(SafeType<APIOperation> api)
        {
            // check if this operation is supported.
            if (!SupportedOperations.Contains(api))
            {
                String MSG = "Operation ''{0}'' not supported.";
                String str = String.Format(MSG, api);
                throw new InvalidOperationException(str);
            }
            return GetOperationImplementation(api);
        }

        /// <summary>
        /// Gets the implementation of the given operation
        /// </summary>
        /// <param name="api">The operation to implement.</param>
        /// <returns>The implementation</returns>
        protected abstract APIOperation GetOperationImplementation(SafeType<APIOperation> api);

        protected APIConfigurationImpl GetAPIConfiguration()
        {
            return _configuration;
        }

        /// <summary>
        /// Creates a new <see cref="APIOperation" /> proxy given a handler.
        /// </summary>
        protected APIOperation NewAPIOperationProxy(SafeType<APIOperation> api, InvocationHandler handler)
        {
            return (APIOperation)Proxy.NewProxyInstance(api.RawType, handler);
        }

        private static bool LOGGINGPROXY_ENABLED;
        static AbstractConnectorFacade()
        {
            string enabled = System.Configuration.
                ConfigurationManager.AppSettings.Get("logging.proxy");
            LOGGINGPROXY_ENABLED = StringUtil.IsTrue(enabled);
        }

        protected APIOperation CreateLoggingProxy(SafeType<APIOperation> api, APIOperation target)
        {
            APIOperation ret = target;
            if (LOGGINGPROXY_ENABLED)
            {
                LoggingProxy logging = new LoggingProxy(api, target);
                ret = NewAPIOperationProxy(api, logging);
            }
            return ret;
        }
    }
    #endregion

    #region ConnectorFacadeFactoryImpl
    public class ConnectorFacadeFactoryImpl : ConnectorFacadeFactory
    {
        public override ConnectorFacade NewInstance(APIConfiguration config)
        {
            ConnectorFacade ret = null;
            APIConfigurationImpl impl = (APIConfigurationImpl)config;
            AbstractConnectorInfo connectorInfo = impl.ConnectorInfo;
            if (connectorInfo is LocalConnectorInfoImpl)
            {
                LocalConnectorInfoImpl localInfo =
                    (LocalConnectorInfoImpl)connectorInfo;
                // create a new Provisioner..
                ret = new LocalConnectorFacadeImpl(localInfo, impl);
            }
            else
            {
                ret = new RemoteConnectorFacadeImpl(impl);
            }
            return ret;
        }


        /// <summary>
        /// Dispose of all object pools and other resources associated with this
        /// class.
        /// </summary>
        public override void Dispose()
        {
            ConnectorPoolManager.Dispose();
        }

    }
    #endregion

    #region ObjectStreamHandler
    internal interface ObjectStreamHandler
    {
        bool Handle(Object obj);
    }
    #endregion

    #region StreamHandlerUtil
    internal static class StreamHandlerUtil
    {
        /// <summary>
        /// Adapts from a ObjectStreamHandler to a ResultsHandler
        /// </summary>
        private class ResultsHandlerAdapter
        {
            private readonly ObjectStreamHandler _target;
            public ResultsHandlerAdapter(ObjectStreamHandler target)
            {
                _target = target;
            }
            public bool Handle(ConnectorObject obj)
            {
                return _target.Handle(obj);
            }
        }

        /// <summary>
        /// Adapts from a ObjectStreamHandler to a SyncResultsHandler
        /// </summary>
        private class SyncResultsHandlerAdapter
        {
            private readonly ObjectStreamHandler _target;
            public SyncResultsHandlerAdapter(ObjectStreamHandler target)
            {
                _target = target;
            }
            public bool Handle(SyncDelta obj)
            {
                return _target.Handle(obj);
            }
        }

        /// <summary>
        /// Adapts from a ObjectStreamHandler to a SyncResultsHandler
        /// </summary>
        private class ObjectStreamHandlerAdapter : ObjectStreamHandler
        {
            private readonly Type _targetInterface;
            private readonly Object _target;
            public ObjectStreamHandlerAdapter(Type targetInterface, Object target)
            {
                Assertions.NullCheck(targetInterface, "targetInterface");
                Assertions.NullCheck(target, "target");
                if (!targetInterface.IsAssignableFrom(target.GetType()))
                {
                    throw new ArgumentException("Target" + targetInterface + " " + target);
                }
                if (!IsAdaptableToObjectStreamHandler(targetInterface))
                {
                    throw new ArgumentException("Target interface not supported: " + targetInterface);
                }
                _targetInterface = targetInterface;
                _target = target;
            }
            public bool Handle(Object obj)
            {
                if (_targetInterface.Equals(typeof(ResultsHandler)))
                {
                    return ((ResultsHandler)_target)((ConnectorObject)obj);
                }
                else if (_targetInterface.Equals(typeof(SyncResultsHandler)))
                {
                    return ((SyncResultsHandler)_target)((SyncDelta)obj);
                }
                else
                {
                    throw new InvalidOperationException("Unhandled case: " + _targetInterface);
                }
            }
        }

        public static bool IsAdaptableToObjectStreamHandler(Type clazz)
        {
            return (typeof(ResultsHandler).IsAssignableFrom(clazz) ||
                    typeof(SyncResultsHandler).IsAssignableFrom(clazz));
        }

        public static ObjectStreamHandler AdaptToObjectStreamHandler(Type interfaceType,
                Object target)
        {
            return new ObjectStreamHandlerAdapter(interfaceType, target);
        }
        public static Object AdaptFromObjectStreamHandler(Type interfaceType,
                ObjectStreamHandler target)
        {
            if (interfaceType.Equals(typeof(ResultsHandler)))
            {
                return new ResultsHandler(new ResultsHandlerAdapter(target).Handle);
            }
            else if (interfaceType.Equals(typeof(SyncResultsHandler)))
            {
                return new SyncResultsHandler(new SyncResultsHandlerAdapter(target).Handle);
            }
            else
            {
                throw new InvalidOperationException("Unhandled case: " + interfaceType);
            }
        }
    }
    #endregion

    #region LoggingProxy
    public class LoggingProxy : InvocationHandler
    {
        private readonly SafeType<APIOperation> _op;
        private readonly object _target;

        public LoggingProxy(SafeType<APIOperation> api, object target)
        {
            _op = api;
            _target = target;
        }
        /// <summary>
        /// Log all operations.
        /// </summary>
        public Object Invoke(Object proxy, MethodInfo method, Object[] args)
        {
            //do not proxy equals, hashCode, toString
            if (method.DeclaringType.Equals(typeof(object)))
            {
                return method.Invoke(this, args);
            }
            StringBuilder bld = new StringBuilder();
            bld.Append("Enter: ");
            AddMethodName(bld, method);
            bld.Append('(');
            for (int i = 0; args != null && i < args.Length; i++)
            {
                if (i != 0)
                {
                    bld.Append(", ");
                }
                bld.Append('{').Append(i).Append('}');
            }
            bld.Append(')');
            // write out trace header
            Trace.TraceInformation(bld.ToString(), args);
            // invoke the method
            try
            {
                object ret = method.Invoke(_target, args);
                // clear out buffer.
                bld.Length = 0;
                bld.Append("Return: ");
                AddMethodName(bld, method);
                bld.Append("({0})");
                Trace.TraceInformation(bld.ToString(), ret);
                return ret;
            }
            catch (TargetInvocationException e)
            {
                Exception root = e.InnerException;
                ExceptionUtil.PreserveStackTrace(root);
                throw root;
            }
        }

        private void AddMethodName(StringBuilder bld, MethodInfo method)
        {
            bld.Append(_op.RawType.Name);
            bld.Append('.');
            bld.Append(method.Name);
        }
    }
    #endregion
}