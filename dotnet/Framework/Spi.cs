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
using System.Globalization;
using System.Collections.Generic;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Spi.Operations;
namespace Org.IdentityConnectors.Framework.Spi
{
    #region AttributeNormalizer
    /// <summary>
    /// Interface to be implemented by connectors that need
    /// to normalize certain attributes.
    /// </summary>
    /// <remarks>
    /// This might, for
    /// example, be used to normalize whitespace within
    /// DN's to ensure consistent filtering whether that
    /// filtering is natively on the resource or by the
    /// connector framework. For connectors implementing
    /// this interface, the method <see cref="NormalizeAttribute(ObjectClass, ConnectorAttribute)" />
    /// will be applied to each of the following:
    /// <list type="number">
    /// <item>
    /// <description>The filter passed to <see cref="Org.IdentityConnectors.Framework.Spi.Operations.SearchOp{T}" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The results returned from <see cref="Org.IdentityConnectors.Framework.Spi.Operations.SearchOp{T}" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The results returned from <see cref="Org.IdentityConnectors.Framework.Spi.Operations.SyncOp" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The attributes passed to <see cref="AdvancedUpdateOp" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The <code>Uid</code> returned from <see cref="AdvancedUpdateOp" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The attributes passed to <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The <code>Uid</code> returned from <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The attributes passed to <see cref="CreateOp" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The <code>Uid</code> returned from <see cref="CreateOp" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The <code>Uid</code> passed to <see cref="DeleteOp" />.
    /// </description>
    /// </item>
    /// </list>
    /// </remarks>
    public interface AttributeNormalizer
    {
        ConnectorAttribute NormalizeAttribute(ObjectClass oclass, ConnectorAttribute attribute);
    }
    #endregion

    #region Configuration
    /// <summary>
    /// Encapsulates the configuration of a connector.
    /// <para>
    /// Implementations of the <code>Configuration</code> interface must have a default
    /// constructor. All properties are considered configuration for the connector.
    /// The initial value of the property is
    /// considered the default value of the property. The types of the properties
    /// can be only those returned by <see cref="M:Org.IdentityConnectors.Framework.Common.FrameworkUtil.GetAllSupportedConfigTypes"/> and
    /// multi-dimensional arrays thereof. The properties are not required by default,
    /// but a property can be marked as required through use of the <see cref="ConfigurationPropertyAttribute"/>.
    /// </para>
    /// <para>  
    /// Each property corresponds to two entries in a resource named <code>Messages</code>:
    /// <code>[Property].display</code> and <code>[Property].help</code>. For example,
    /// <code>hostname.help</code> and <code>hostname.display</code> would be the keys
    /// corresponding to a <code>hostname</code> property. The <code>display</code> message is the display
    /// name of the property and can be used to display the property in a view. The <code>help</code>
    /// message holds the description of the property. The names of the two keys can be overridden
    /// through the <code>ConfigurationProperty</code> annotation.
    /// <para>
    /// </summary>
    public interface Configuration
    {
        /// <summary>
        /// Gets or sets the {@link ConnectorMessages message catalog} instance that allows the Connector
        /// to localize messages. The setter is called before any bean property setter,
        /// the <see cref="M:Validate"/> method or this property getter.
        /// </summary>
        ConnectorMessages ConnectorMessages { get; set; }

        /// <summary>
        /// Determines if the configuration is valid.
        /// <para>
        /// A valid configuration is one that is ready to be used by the connector:
        /// it is complete (all the required properties have been given values) 
        /// and the property values are well-formed (are in the expected range, 
        /// have the expected format, etc.)
        /// </para>
        /// <para>
        /// Implementations of this method <strong>should not</strong> connect to the resource
        /// in an attempt to validate the configuration. For example, implementations
        /// should not attempt to check that a host of the specified name exists
        /// by making a connection to it. Such checks can be performed in the implementation
        /// of the <see cref="M:Org.IdentityConnectors.Framework.Spi.Operations.TestOp.Test"/> method.
        /// </para>
        /// </summary>
        /// <exception cref="System.Exception">iff the configuration is not valid. Implementations
        /// are encouraged to throw the most specific exception available.
        /// When no specific exception is available, implementations can throw
        /// <see cref="Org.IdentityConnectors.Framework.Common.Exceptions.ConfigurationException"/>.</exception>
        void Validate();
    }
    #endregion

    #region AbstractConfiguration
    public abstract class AbstractConfiguration : Configuration
    {

        public ConnectorMessages ConnectorMessages { get; set; }

        public abstract void Validate();
    }
    #endregion

    #region ConnectorClassAttribute
    [AttributeUsage(AttributeTargets.Class, AllowMultiple = false)]
    public class ConnectorClassAttribute : System.Attribute
    {

        private readonly String _connectorDisplayNameKey;
        private readonly SafeType<Configuration> _connectorConfigurationClass;

        public ConnectorClassAttribute(String connectorDisplayNameKey,
                                      Type connectorConfigurationClass)
        {
            _connectorDisplayNameKey = connectorDisplayNameKey;
            _connectorConfigurationClass = SafeType<Configuration>.ForRawType(connectorConfigurationClass);
        }

        public string ConnectorDisplayNameKey
        {
            get
            {
                return _connectorDisplayNameKey;
            }
        }

        public SafeType<Configuration> ConnectorConfigurationType
        {
            get
            {
                return _connectorConfigurationClass;
            }
        }

        public string[] MessageCatalogPaths { get; set; }

    }
    #endregion

    #region ConfigurationPropertyAttribute
    /// <summary>
    /// The <see cref="Configuration"/> interface is traversed through reflection. This
    /// annotation provides a way to override the default configuration operation for
    /// each property.
    /// </summary>
    /// <example>
    /// <code>
    ///     public class MyClass : Configuration {
    ///         [ConfigurationPropertionOptions(Confidential=true)]
    ///         public string MyProperty {get ; set;}
    ///     }
    /// </code>
    /// </example>
    [AttributeUsage(AttributeTargets.Property)]
    public class ConfigurationPropertyAttribute : System.Attribute
    {

        /// <summary>
        /// Order in which this property is displayed.
        /// </summary>
        public int Order { get; set; }
        /// <summary>
        /// Is this a confidential property whose value should be 
        /// encrypted by the application when persisted?
        /// </summary>
        public bool Confidential { get; set; }
        /// <summary>
        /// Is this a required property?
        /// </summary>
        public bool Required { get; set; }
        /// <summary>
        /// Change the default help message key.
        /// </summary>
        public string HelpMessageKey { get; set; }
        /// <summary>
        /// Change the default display message key.
        /// </summary>
        public string DisplayMessageKey { get; set; }

        /// <summary>
        /// List of operations for which this property must be specified.
        /// </summary>
        /// <remarks>
        /// This is used for the case where a connector may or may not
        /// implement certain operations depending in the configuration.
        /// The default value of "empty array" is special in that
        /// it means that this property is applicable to all operations.
        /// MUST be SPI operations
        /// </remarks>
        public Type[] OperationTypes { get; set; }

        /// <summary>
        /// List of operations for which this property must be specified.
        /// </summary>
        /// <remarks>
        /// This is used for the case where a connector may or may not
        /// implement certain operations depending in the configuration.
        /// The default value of "empty array" is special in that
        /// it means that this property is applicable to all operations.
        /// </remarks>
        public SafeType<SPIOperation>[] Operations
        {
            get
            {
                Type[] types = OperationTypes;
                SafeType<SPIOperation>[] rv = new SafeType<SPIOperation>[types.Length];
                for (int i = 0; i < types.Length; i++)
                {
                    rv[i] =
                        SafeType<SPIOperation>.ForRawType(types[i]);
                }
                return rv;
            }
        }

        /// <summary>
        /// Default constructor 
        /// </summary>
        public ConfigurationPropertyAttribute()
        {
            Order = 1;
            Confidential = false;
            Required = false;
            HelpMessageKey = null;
            DisplayMessageKey = null;
            OperationTypes = new Type[0];
        }
    }
    #endregion

    #region Connector
    /// <summary>
    /// This is the main interface to declare a connector. Developers must implement
    /// this interface. The life-cycle for a <see cref="Connector"/> is as follows
    /// <see cref="Connector.Init(Configuration)"/> is called then any of the operations implemented
    /// in the Connector and finally dispose. The <see cref="Connector.Init(Configuration)"/> and
    /// <see cref="IDisposable.Dispose()"/> allow for block operations. For instance bulk creates or
    /// deletes and the use of before and after actions. Once <see cref="IDisposable.Dispose()"/> is
    /// called the <see cref="Connector"/> object is discarded.
    /// </summary>
    public interface Connector : IDisposable
    {

        /// <summary>
        /// Initialize the connector with its configuration. For instance in a JDBC
        /// Connector this would include the database URL, password, and user.
        /// </summary>
        /// <param name="configuration">instance of the <see cref="Configuration"/> object implemented by
        /// the <see cref="Connector"/> developer and populated with information
        /// in order to initialize the <see cref="Connector"/>.</param>
        void Init(Configuration configuration);
    }
    #endregion

    #region PoolableConnector
    /// <summary>
    /// To be implemented by Connectors that wish to be pooled.
    /// </summary>
    public interface PoolableConnector : Connector
    {
        /// <summary>
        /// Checks if the connector is still alive.
        /// <para>
        /// A connector can spend a large amount of time in the pool before
        /// being used. This method is intended to check if the connector is
        /// alive and operations can be invoked on it (for instance, an implementation
        /// would check that the connector's physical connection to the resource
        /// has not timed out).
        /// </para>
        /// <para>
        /// The major difference between this method and <see cref="M:TestOp.Test"/> is that
        /// this method must do only the minimum that is necessary to check that the
        /// connector is still alive. <code>TestOp.Test()</code> does a more thorough
        /// check of the environment specified in the Configuration, and can therefore
        /// be much slower.
        /// </para>
        /// <para>
        /// This method can be called often. Implementations should do their
        /// best to keep this method fast.
        /// </para>
        /// <exception cref="System.Exception">if the connector is no longer alive.</exception>
        void CheckAlive();
    }
    #endregion
}