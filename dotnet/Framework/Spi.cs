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
using Org.IdentityConnectors.Common;
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
    /// <description>The attributes passed to <see cref="UpdateOp" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>The <code>Uid</code> returned from <see cref="UpdateOp" />.
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
    /// </para>
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
        private readonly String _connectorCategoryKey;
        private readonly SafeType<Configuration> _connectorConfigurationClass;

        public ConnectorClassAttribute(String connectorDisplayNameKey,
                                      Type connectorConfigurationClass)
        {
            _connectorDisplayNameKey = connectorDisplayNameKey;
            _connectorCategoryKey = string.Empty;
            _connectorConfigurationClass = SafeType<Configuration>.ForRawType(connectorConfigurationClass);
        }

        public ConnectorClassAttribute(String connectorDisplayNameKey, String connectorCategoryKey,
                                      Type connectorConfigurationClass)
        {
            _connectorDisplayNameKey = connectorDisplayNameKey;
            _connectorCategoryKey = connectorCategoryKey;
            _connectorConfigurationClass = SafeType<Configuration>.ForRawType(connectorConfigurationClass);
        }

        /// <summary>
        /// The display name key. This must be a key in the message catalog.
        /// </summary>
        public string ConnectorDisplayNameKey
        {
            get
            {
                return _connectorDisplayNameKey;
            }
        }

        /// <summary>
        /// Category the connector belongs to such as 'LDAP' or 'DB'.
        /// </summary>
        public string ConnectorCategoryKey
        {
            get
            {
                return _connectorCategoryKey;
            }
        }

        public SafeType<Configuration> ConnectorConfigurationType
        {
            get
            {
                return _connectorConfigurationClass;
            }
        }

        /// <summary>
        /// The resource path(s) to the message catalog.
        /// Message catalogs are searched in the order given such 
        /// that the first one wins. By default, if no paths are
        /// specified, we use <code>connector-package.Messages.resx</code>
        /// </summary>
        public string[] MessageCatalogPaths { get; set; }

    }
    #endregion

    #region ConfigurationrClassAttribute
    /// <summary>
    /// The <seealso cref="Org.IdentityConnectors.Framework.Spi.Configuration"/> interface is traversed through reflection. This
    /// annotation provides a way to override the default "add all property" behaviour.
    /// 
    /// @since 1.4
    /// </summary>
    [AttributeUsage(AttributeTargets.Class, AllowMultiple = false)]
    public class ConfigurationClassAttribute : System.Attribute
    {

        private readonly Boolean _skipUnsupported;
        private readonly String[] _ignore;

        public ConfigurationClassAttribute(Boolean skipUnsupported, String[] ignore)
        {
            _skipUnsupported = skipUnsupported;
            _ignore = ignore ?? new string[]{} ;
        }

        /// <summary>
        /// Silently skips properties with unsupported types.
        /// </summary>
        public Boolean SkipUnsupported
        {
            get
            {
                return _skipUnsupported;
            }
        }

        /// <summary>
        /// List of properties which should be excluded from configuration properties..
        /// </summary>
        public string[] Ignore
        {
            get
            {
                return _ignore;
            }
        }
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
        /// Change the default group message key.
        /// </summary>
        public string GroupMessageKey { get; set; }

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
            GroupMessageKey = null;
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
        /// </summary>
        /// <exception cref="System.Exception">if the connector is no longer alive.</exception>
        void CheckAlive();
    }
    #endregion

    #region SearchResultsHandler
    /// <summary>
    /// A SearchResultsHandler is a completion handler for consuming the results of a
    /// search request.
    /// <para>
    /// A search result completion handler may be specified when performing search
    /// requests using a <seealso cref="org.identityconnectors.framework.api.ConnectorFacade"/>
    /// object. The <seealso cref="#handle"/> method is invoked each time a matching
    /// <seealso cref="Org.identityconnectors.framework.common.objects.ConnectorObject"/>
    /// resource is returned, followed by <seealso cref="#handleResult"/> indicating that no
    /// more ConnectorObject resources will be returned.
    /// </para>
    /// <para>
    /// Implementations of these methods should complete in a timely manner so as to
    /// avoid keeping the invoking thread from dispatching to other completion
    /// handlers.
    /// 
    /// </para>
    /// </summary>
    /// <remarks>Since 1.4</remarks>
    public class SearchResultsHandler : ResultsHandler
    {

        /// <summary>
        /// Invoked when the request has completed successfully.
        /// </summary>
        /// <param name="result">
        ///            The query result indicating that no more resources are to be
        ///            returned and, if applicable, including information which
        ///            should be used for subsequent paged results query requests. </param>
        public Action<SearchResult> HandleResult;

    }
    #endregion

    #region StatefulConfiguration
    /// <summary>
    /// A Stateful Configuration interface extends the default <seealso cref="Configuration"/>
    /// and makes the framework keep the same instance.
    /// <p/>
    /// The default Configuration object instance is constructed every single time
    /// before the <seealso cref="Connector#Init(Configuration)"/> is called. If the
    /// configuration class implements this interface then the Framework keeps one
    /// instance of Configuration and the <seealso cref="Connector#Init(Configuration)"/> is
    /// called with the same instance. This requires extra caution because the
    /// framework only guaranties to create one instance and set the properties
    /// before it calls the <seealso cref="Connector#Init(Configuration)"/> on different
    /// connector instances in multiple different threads at the same time. The
    /// Connector developer must quarantine that the necessary resource
    /// initialisation are thread-safe.
    /// 
    /// <p/>
    /// If the connector implements the <seealso cref="PoolableConnector"/> then this
    /// configuration is kept in the
    /// <seealso cref="Org.IdentityConnectors.Framework.Impl.Api.Local.ConnectorPoolManager"/>
    /// and when the
    /// <seealso cref="Org.IdentityConnectors.Framework.Impl.Api.Local.ConnectorPoolManager#Dispose()"/>
    /// calls the <seealso cref="#Release()"/> method. If the connector implements only the
    /// <seealso cref="Connector"/> then this configuration is kept in the
    /// <seealso cref="Org.IdentityConnectors.Framework.Impl.Api.ConnectorFacade"/> and the
    /// application must take care of releasing.
    /// 
    /// </summary>
    public interface StatefulConfiguration : Configuration
    {

        /// <summary>
        /// Release any allocated resources.
        /// </summary>
        void Release();

    }
    #endregion

    #region SyncTokenResultsHandler
    /// <summary>
    /// A SyncTokenResultsHandler is a Callback interface that an application
    /// implements in order to handle results from
    /// <seealso cref="org.identityconnectors.framework.api.operations.SyncApiOp"/> in a
    /// stream-processing fashion.
    /// 
    /// </summary>
    /// <remarks>Since 1.4</remarks>
    public class SyncTokenResultsHandler : SyncResultsHandler
    {

        /// <summary>
        /// Invoked when the request has completed successfully.
        /// </summary>
        /// <param name="result">
        ///            The sync result indicating that no more resources are to be
        ///            returned and, if applicable, including information which
        ///            should be used for next sync requests. </param>
        //void HandleResult(SyncToken result);
        public Action<SyncToken> HandleResult;

    }
    #endregion
}