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
using System.Reflection;
using System.Globalization;
using System.Collections.Generic;
using System.Net.Security;
using System.Security;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common;
using Org.IdentityConnectors.Framework.Common.Objects;

namespace Org.IdentityConnectors.Framework.Api
{
    public static class APIConstants
    {
        public const int NO_TIMEOUT = -1;
    }

    public interface APIConfiguration
    {
        ConfigurationProperties ConfigurationProperties { get; }
        bool IsConnectorPoolingSupported { get; }
        ObjectPoolConfiguration ConnectorPoolConfiguration { get; }
        ICollection<SafeType<APIOperation>> SupportedOperations { get; }

        int GetTimeout(SafeType<APIOperation> operation);
        void SetTimeout(SafeType<APIOperation> operation, int timeout);

        int ProducerBufferSize { get; set; }
    }

    /// <summary>
    /// Configuration properties encapsulates the <see cref="Org.IdentityConnectors.Framework.Spi.Configuration" /> and uses
    /// <see cref="System.Reflection" /> to determine the properties available for manipulation.
    /// </summary>
    public interface ConfigurationProperties
    {
        /// <summary>
        /// Get the list of properties names for this <see cref="Org.IdentityConnectors.Framework.Spi.Configuration" />.
        /// </summary>
        /// <returns>get the list of properties names.</returns>
        IList<string> PropertyNames { get; }

        /// <summary>
        /// Get a particular <see cref="ConfigurationProperty" /> by name.
        /// </summary>
        /// <param name="name">the unique name of the property.</param>
        /// <returns>a <see cref="ConfigurationProperty" /> if it exists otherwise null.</returns>
        ConfigurationProperty GetProperty(string name);

        /// <summary>
        /// Set the value of the <see cref="Org.IdentityConnectors.Framework.Spi.Configuration" /> property by name.
        /// </summary>
        /// <param name="name">Name of the property to set the value against.</param>
        /// <param name="value">Value to set on the configuration property.</param>
        /// <exception cref="ArgumentException">iff the property name does not exist.</exception>
        void SetPropertyValue(string name, Object value);

    }

    /// <summary>
    /// Translation from <see cref="Org.IdentityConnectors.Framework.Spi.Configuration" /> at the SPI layer to the API.
    /// </summary>
    public interface ConfigurationProperty
    {
        int Order { get; }

        /// <summary>
        /// Get the unique name of the configuration property.
        /// </summary>
        string Name { get; }

        /// <summary>
        /// Get the help message from the message catalog.
        /// </summary>
        string GetHelpMessage(string def);

        /// <summary>
        /// Get the display name for the is configuration
        /// </summary>
        string GetDisplayName(string def);

        /// <summary>
        /// Get the value from the property.
        /// </summary>
        /// <remarks>
        /// This should be the default value.
        /// </remarks>
        object Value { get; set; }

        /// <summary>
        /// Get the type of the property.
        /// </summary>
        Type ValueType { get; }

        /// <summary>
        /// Is this a confidential property whose value should be encrypted by
        /// the application when persisted?
        /// </summary>
        bool IsConfidential { get; }

        /// <summary>
        /// Is this a required property
        /// </summary>
        /// <returns>True if the property is required</returns>
        bool IsRequired { get; }

        /// <summary>
        /// Set of operations for which this property must be specified.
        /// </summary>
        /// <remarks>
        /// This is used for the case where a connector may or may not
        /// implement certain operations depending in the configuration.
        /// The default value of "empty array" is special in that
        /// it means that this property is applicable to all operations.
        /// </remarks>
        /// 
        ICollection<SafeType<APIOperation>> Operations { get; }
    }

    /// <summary>
    /// Main interface for which consumers call the Connector API logic.
    /// </summary>
    public interface ConnectorFacade : CreateApiOp, DeleteApiOp,
            SearchApiOp, UpdateApiOp, SchemaApiOp, AuthenticationApiOp, ResolveUsernameApiOp,
            GetApiOp, ValidateApiOp, TestApiOp, ScriptOnConnectorApiOp, ScriptOnResourceApiOp,
            SyncApiOp
    {
        /// <summary>
        /// Get the set of operations that this <see cref="ConnectorFacade" /> will support.
        /// </summary>
        ICollection<SafeType<APIOperation>> SupportedOperations { get; }

        /// <summary>
        /// Get an instance of an operation that this facade supports.
        /// </summary>
        APIOperation GetOperation(SafeType<APIOperation> type);

    }

    /// <summary>
    /// Manages a pool of connectors for use by a provisioner.
    /// </summary>
    public abstract class ConnectorFacadeFactory
    {
        // At some point we might make this pluggable, but for now, hard-code
        private const string IMPL_NAME =
            "Org.IdentityConnectors.Framework.Impl.Api.ConnectorFacadeFactoryImpl";
        private static ConnectorFacadeFactory _instance;
        private static object LOCK = new Object();

        /// <summary>
        /// Get the singleton instance of the <see cref="ConnectorFacadeFactory" />.
        /// </summary>
        public static ConnectorFacadeFactory GetInstance()
        {
            lock (LOCK)
            {
                if (_instance == null)
                {
                    SafeType<ConnectorFacadeFactory> t = FrameworkInternalBridge.LoadType<ConnectorFacadeFactory>(IMPL_NAME);
                    _instance = t.CreateInstance();
                }
            }
            return _instance;
        }

        /// <summary>
        /// Get a new instance of <see cref="ConnectorFacade" />.
        /// </summary>
        /// <param name="config">all the configuration that the framework, connector, and
        /// pooling needs.</param>
        /// <returns>
        /// <see cref="ConnectorFacade" /> to call API operations against.</returns>
        /// <exception cref="ClassNotFoundException"></exception>
        public abstract ConnectorFacade NewInstance(APIConfiguration config);


        /// <summary>
        /// Dispose of all connection pools, resources, etc.
        /// </summary>
        public abstract void Dispose();
    }

    /// <summary>
    /// The connector meta-data for a given connector.
    /// </summary>
    public interface ConnectorInfo
    {
        /// <summary>
        /// Returns a friendly name suitable for display in the UI.
        /// </summary>
        /// <returns>The friendly name</returns>
        string GetConnectorDisplayName();

        ConnectorMessages Messages { get; }

        ConnectorKey ConnectorKey { get; }

        /// <summary>
        /// Loads the <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> and <see cref="Org.IdentityConnectors.Framework.Spi.Configuration" /> class in order to
        /// determine the proper default configuration parameters.
        /// </summary>
        APIConfiguration CreateDefaultAPIConfiguration();
    }
    /// <summary>
    /// Class responsible for maintaing a list of <code>ConnectorInfo</code>
    /// associated with a set of connector bundles.
    /// </summary>
    public interface ConnectorInfoManager
    {
        /// <summary>
        /// Returns the list of <code>ConnectorInfo</code>
        /// </summary>
        /// <returns>the list of <code>ConnectorInfo</code></returns>
        IList<ConnectorInfo> ConnectorInfos { get; }

        /// <summary>
        /// Given a connectorName and connectorVersion, returns the
        /// associated <code>ConnectorInfo</code>.
        /// </summary>
        /// <param name="key">The connector key.</param>
        /// <returns>The <code>ConnectorInfo</code> or null if it couldn't
        /// be found.</returns>
        ConnectorInfo FindConnectorInfo(ConnectorKey key);
    }
    /// <summary>
    /// The main entry point into connectors.
    /// </summary>
    /// <remarks>
    /// This allows you
    /// to load the connector classes from a set of bundles.
    /// </remarks>
    public abstract class ConnectorInfoManagerFactory
    {
        //At some point we might make this pluggable, but for now, hard-code
        private const string IMPL_NAME =
            "Org.IdentityConnectors.Framework.Impl.Api.ConnectorInfoManagerFactoryImpl";
        private static ConnectorInfoManagerFactory _instance;
        private static object LOCK = new Object();
        /// <summary>
        /// Singleton pattern for getting an instance of the 
        /// ConnectorInfoManagerFactory.
        /// </summary>
        /// <returns></returns>
        public static ConnectorInfoManagerFactory GetInstance()
        {
            lock (LOCK)
            {
                if (_instance == null)
                {
                    SafeType<ConnectorInfoManagerFactory> t =
                        FrameworkInternalBridge.LoadType<ConnectorInfoManagerFactory>(IMPL_NAME);
                    _instance = t.CreateInstance();
                }
            }
            return _instance;
        }
        public abstract ConnectorInfoManager GetLocalManager();
        public abstract ConnectorInfoManager GetRemoteManager(RemoteFrameworkConnectionInfo info);

        /// <summary>
        /// Clears the bundle manager cache.
        /// </summary>
        /// <remarks>
        /// Generally intended for unit testing
        /// </remarks>
        public abstract void ClearRemoteCache();
    }

    /// <summary>
    /// Uniquely identifies a connector within an installation.
    /// </summary>
    /// <remarks>
    /// Consists of the triple (bundleName, bundleVersion, connectorName)
    /// </remarks>
    public sealed class ConnectorKey
    {
        private readonly string _bundleName;
        private readonly string _bundleVersion;
        private readonly string _connectorName;

        public ConnectorKey(String bundleName,
                String bundleVersion,
                String connectorName)
        {
            if (bundleName == null)
            {
                throw new ArgumentException("bundleName may not be null");
            }
            if (bundleVersion == null)
            {
                throw new ArgumentException("bundleVersion may not be null");
            }
            if (connectorName == null)
            {
                throw new ArgumentException("connectorName may not be null");
            }
            _bundleName = bundleName;
            _bundleVersion = bundleVersion;
            _connectorName = connectorName;
        }

        public string BundleName
        {
            get
            {
                return _bundleName;
            }
        }

        public string BundleVersion
        {
            get
            {
                return _bundleVersion;
            }
        }

        public string ConnectorName
        {
            get
            {
                return _connectorName;
            }
        }

        public override bool Equals(object o)
        {
            if (o is ConnectorKey)
            {
                ConnectorKey other = (ConnectorKey)o;
                if (!_bundleName.Equals(other._bundleName))
                {
                    return false;
                }
                if (!_bundleVersion.Equals(other._bundleVersion))
                {
                    return false;
                }
                if (!_connectorName.Equals(other._connectorName))
                {
                    return false;
                }
                return true;
            }
            return false;
        }

        public override int GetHashCode()
        {
            int rv = 0;
            rv ^= _connectorName.GetHashCode();
            return rv;
        }

        public override string ToString()
        {
            StringBuilder builder = new StringBuilder();
            builder.Append("ConnectorKey(");
            builder.Append(" bundleName=").Append(_bundleName);
            builder.Append(" bundleVersion=").Append(_bundleVersion);
            builder.Append(" connectorName=").Append(_connectorName);
            builder.Append(" )");
            return builder.ToString();
        }
    }



    public sealed class RemoteFrameworkConnectionInfo
    {
        private readonly String _host;
        private readonly int _port;
        private readonly GuardedString _key;
        private readonly bool _useSSL;
        private readonly RemoteCertificateValidationCallback _certificateValidationCallback;
        private readonly int _timeout;

        /// <summary>
        /// Creates a new instance of RemoteFrameworkConnectionInfo, using
        /// a clear (non-ssl) connection and a 60-second timeout.
        /// </summary>
        /// <param name="host">The host to connect to</param>
        /// <param name="port">The port to connect to</param>
        public RemoteFrameworkConnectionInfo(String host,
                int port,
                GuardedString key)
            : this(host, port, key, false, null, 60 * 1000)
        {
        }

        /// <summary>
        /// Creates a new instance of RemoteFrameworkConnectionInfo.
        /// </summary>
        /// <param name="host">The host to connect to</param>
        /// <param name="port">The port to connect to</param>
        /// <param name="useSSL">Set to true if we are to connect via SSL.</param>
        /// <param name="certificateValidationCallback">to use
        /// for establising the SSL connection. May be null or empty,
        /// in which case the default installed providers for the JVM will
        /// be used. Ignored if 'useSSL' is false.</param>
        /// <param name="timeout">The timeout to use (in milliseconds). A value of 0
        /// means infinite timeout;</param>
        public RemoteFrameworkConnectionInfo(String host,
                int port,
                GuardedString key,
                bool useSSL,
                RemoteCertificateValidationCallback certificateValidationCallback,
                int timeout)
        {

            if (host == null)
            {
                throw new ArgumentException("Parameter 'host' is null.");
            }
            if (key == null)
            {
                throw new ArgumentException("Parameter 'key' is null.");
            }

            _host = host;
            _port = port;
            _key = key;
            _useSSL = useSSL;
            _certificateValidationCallback = certificateValidationCallback;
            _timeout = timeout;
        }

        /// <summary>
        /// Returns the host to connect to.
        /// </summary>
        /// <returns>The host to connect to.</returns>
        public String Host
        {
            get
            {
                return _host;
            }
        }

        /// <summary>
        /// Returns the port to connect to
        /// </summary>
        /// <returns>The port to connect to</returns>
        public int Port
        {
            get
            {
                return _port;
            }
        }

        public GuardedString Key
        {
            get
            {
                return _key;
            }
        }

        /// <summary>
        /// Returns true iff we are to use SSL to connect.
        /// </summary>
        /// <returns>true iff we are to use SSL to connect.</returns>
        public bool UseSSL
        {
            get
            {
                return _useSSL;
            }
        }

        /// <summary>
        /// Returns the list of <see cref="TrustManager" />'s.
        /// </summary>
        /// <remarks>
        /// to use when establishing
        /// the connection.
        /// </remarks>
        /// <returns>The list of <see cref="TrustManager" />'s.</returns>
        public RemoteCertificateValidationCallback CertificateValidationCallback
        {
            get
            {
                return _certificateValidationCallback;
            }
        }

        /// <summary>
        /// Returns the timeout (in milliseconds) to use for the connection.
        /// </summary>
        /// <remarks>
        /// A value of zero means infinite timeout.
        /// </remarks>
        /// <returns>the timeout (in milliseconds) to use for the connection.</returns>
        public int Timeout
        {
            get
            {
                return _timeout;
            }
        }

        public override bool Equals(Object o)
        {
            if (o is RemoteFrameworkConnectionInfo)
            {
                RemoteFrameworkConnectionInfo other =
                    (RemoteFrameworkConnectionInfo)o;
                if (!Object.Equals(Host, other.Host))
                {
                    return false;
                }
                if (Port != other.Port)
                {
                    return false;
                }
                if (UseSSL != other.UseSSL)
                {
                    return false;
                }
                if (CertificateValidationCallback == null ||
                    other.CertificateValidationCallback == null)
                {
                    if (CertificateValidationCallback != null ||
                        other.CertificateValidationCallback != null)
                    {
                        return false;
                    }
                }
                else
                {
                    if (!CertificateValidationCallback.Equals
                        (other.CertificateValidationCallback))
                    {
                        return false;
                    }
                }

                if (!Key.Equals(other.Key))
                {
                    return false;
                }

                if (Timeout != other.Timeout)
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

        public override String ToString()
        {
            return "{host=" + _host + ", port=" + _port + "}";
        }
    }
}
