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

using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;

namespace Org.IdentityConnectors.Framework.Spi.Operations
{
    /// <summary>
    /// Authenticate an object based on their unique identifier and password.
    /// </summary>
    public interface AuthenticateOp : SPIOperation
    {
        /// <summary>
        /// Simple authentication with two parameters presumed to be user name and
        /// password.
        /// </summary>
        /// <remarks>
        /// The <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> developer is expected to attempt to
        /// authenticate these credentials natively. If the authentication fails the
        /// developer should throw a type of <see cref="Exception" /> either
        /// <see cref="ArgumentException" /> or if a native exception is available
        /// and if its of type <see cref="Exception" /> simple throw it. If the
        /// native exception is not a <see cref="Exception" /> wrap it in one and
        /// throw it. This will provide the most detail for logging problem and
        /// failed attempts.
        /// <para>
        /// The developer is of course encourage to try and throw the most
        /// informative exception as possible. In that regards there are several
        /// exceptions provided in the exceptions package. For instance one of the
        /// most common is <see cref="Org.IdentityConnectors.Framework.Common.Exceptions.InvalidPasswordException" />.
        /// </para>
        /// </remarks>
        /// <param name="username">the name based credential for authentication.</param>
        /// <param name="password">the password based credential for authentication.</param>
        /// <exception cref="Exception">iff native authentication fails. If a native exception if
        /// available attempt to throw it.</exception>
        Uid Authenticate(ObjectClass objectClass, String username, GuardedString password, OperationOptions options);
    }

    public interface ResolveUsernameOp : SPIOperation
    {
        /// <summary>
        /// This is a companion to the simple <see cref="Org.IdentityConnectors.Framework.Spi.Operations.AuthenticateOp" /> with two parameters
        /// presumed to be user name and password.
        /// </summary>
        /// <remarks>
        /// The difference is that this
        /// method does not try to authenticate the credentials; instead, it
        /// return the <see cref="Uid" /> of the username that would be authenticated.
        /// <para>
        /// If the authentication fails the
        /// developer should throw a type of <see cref="Exception" /> either
        /// <see cref="ArgumentException" /> or if a native exception is available
        /// and if its of type <see cref="Exception" /> simple throw it. If the
        /// native exception is not a <see cref="Exception" /> wrap it in one and
        /// throw it. This will provide the most detail for logging problem and
        /// failed attempts.
        /// </para>
        /// <para>
        /// The developer is of course encourage to try and throw the most
        /// informative exception as possible. In that regards there are several
        /// exceptions provided in the exceptions package. For instance one of the
        /// most common is <see cref="Org.IdentityConnectors.Framework.Common.Exceptions.InvalidPasswordException" />.
        /// </para>
        /// </remarks>
        /// <param name="objectClass">The object class to use for authenticate.
        /// Will typically be an account. Will not be null.</param>
        /// <param name="username">the username that would be authenticated. Will not be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// If the caller passes null, the framework will convert this into
        /// an empty set of options, so SPI need not worry
        /// about this ever being null.</param>
        /// <returns>Uid The uid of the account that would be authenticated.</returns>
        /// <exception cref="Exception">iff native authentication fails. If a native exception is
        /// available attempt to throw it.</exception>
        Uid ResolveUsername(ObjectClass objectClass, String username, OperationOptions options);
    }

    /// <summary>
    /// The <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> developer is responsible for taking the attributes
    /// given (which always includes the <see cref="ObjectClass" />) and create an object
    /// and its <see cref="Uid" />.
    /// </summary>
    /// <remarks>
    /// The <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> developer must return the
    /// <see cref="Uid" /> so that the caller can refer to the created object.
    /// <para>
    /// The <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> developer should make a best effort to create the
    /// object otherwise throw an informative <see cref="Exception" /> telling the
    /// caller why the operation could not be completed. It reasonable to use
    /// defaults for required <see cref="ConnectorAttribute" />s as long as they are documented.
    /// </para>
    /// </remarks>
    /// <author>Will Droste</author>
    /// <version>$Revision $</version>
    /// <since>1.0</since>
    public interface CreateOp : SPIOperation
    {
        /// <summary>
        /// The <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> developer is responsible for taking the attributes
        /// given (which always includes the <see cref="ObjectClass" />) and create an
        /// object and its <see cref="Uid" />.
        /// </summary>
        /// <remarks>
        /// The <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> developer must return
        /// the <see cref="Uid" /> so that the caller can refer to the created object.
        /// </remarks>
        /// <param name="name">specifies the name of the object to create.</param>
        /// <param name="attrs">includes all the attributes necessary to create the resource
        /// object including the <see cref="ObjectClass" /> attribute.</param>
        /// <returns>the unique id for the object that is created. For instance in
        /// LDAP this would be the 'dn', for a database this would be the
        /// primary key, and for 'ActiveDirectory' this would be the GUID.</returns>
        Uid Create(ObjectClass oclass, ICollection<ConnectorAttribute> attrs, OperationOptions options);
    }

    /// <summary>
    /// Deletes an object with the specified Uid and ObjectClass on the
    /// resource.
    /// </summary>
    public interface DeleteOp : SPIOperation
    {
        /// <summary>
        /// Delete the object that the specified Uid identifies (if any).
        /// </summary>
        /// <param name="objectClass">The type of object to delete.</param>
        /// <param name="uid">The unique identitfier for the object to delete.</param>
        /// <exception cref="">Throws UnknowUid if the object does not exist.</exception>
        void Delete(ObjectClass objClass, Uid uid, OperationOptions options);
    }

    public interface SchemaOp : SPIOperation
    {
        /// <summary>
        /// Determines what types of objects this <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> supports.
        /// </summary>
        /// <remarks>
        /// This
        /// method is considered an operation since determining supported objects may
        /// require configuration information and allows this determination to be
        /// dynamic.
        /// </remarks>
        /// <returns>basic schema supported by this <see cref="Org.IdentityConnectors.Framework.Spi.Connector" />.</returns>
        Schema Schema();
    }
    /// <summary>
    /// Operation that runs a script in the environment of the connector.
    /// </summary>
    /// <remarks>
    /// (Compare to <see cref="Org.IdentityConnectors.Framework.Spi.Operations.ScriptOnResourceOp" />, which runs a script
    /// on the target resource that the connector manages.)
    /// A connector that intends to <i>provide to scripts
    /// more than is required by the basic contract</i>
    /// specified in the javadoc for <see cref="Org.IdentityConnectors.Framework.Api.Operations.ScriptOnConnectorApiOp" />
    /// should implement this interface.
    /// <para>
    /// Each connector that implements this interface must support
    /// <em>at least</em> the behavior specified by <see cref="Org.IdentityConnectors.Framework.Api.Operations.ScriptOnConnectorApiOp" />.
    /// A connector also may expose additional variables for use by scripts
    /// and may respond to specific <see cref="Org.IdentityConnectors.Framework.Common.Objects.OperationOptions" />.
    /// Each connector that implements this interface
    /// must describe in its javadoc as available "for use by connector scripts"
    /// any such additional variables or supported options.
    /// </para>
    /// </remarks>
    public interface ScriptOnConnectorOp : SPIOperation
    {

        /// <summary>
        /// Runs the script request.
        /// </summary>
        /// <param name="request">The script and arguments to run.</param>
        /// <param name="options">Additional options that control how the script is
        /// run.</param>
        /// <returns>The result of the script. The return type must be
        /// a type that the framework supports for serialization.
        /// See <see cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" /> for a list of supported types.</returns>
        Object RunScriptOnConnector(ScriptContext request,
                OperationOptions options);
    }
    /// <summary>
    /// Operation that runs a script directly on a target resource.
    /// </summary>
    /// <remarks>
    /// (Compare to <see cref="Org.IdentityConnectors.Framework.Spi.Operations.ScriptOnConnectorOp" />, which runs a script
    /// in the context of a particular connector.)
    /// <para>
    /// A connector that intends to support
    /// <see cref="Org.IdentityConnectors.Framework.Api.Operations.ScriptOnResourceApiOp" />
    /// should implement this interface.  Each connector that implements
    /// this interface must document which script languages the connector supports,
    /// as well as any supported <see cref="OperationOptions" />.
    /// </para>
    /// </remarks>
    public interface ScriptOnResourceOp : SPIOperation
    {
        /// <summary>
        /// Run the specified script <i>on the target resource</i>
        /// that this connector manages.
        /// </summary>
        /// <param name="request">The script and arguments to run.</param>
        /// <param name="options">Additional options that control
        /// how the script is run.</param>
        /// <returns>The result of the script. The return type must be
        /// a type that the framework supports for serialization.
        /// See <see cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" /> for a list of supported types.</returns>
        Object RunScriptOnResource(ScriptContext request,
                OperationOptions options);
    }

    /// <summary>
    /// Implement this interface to allow the Connector to search for resource
    /// objects.
    /// </summary>
    public interface SearchOp<T> : SPIOperation where T : class
    {
        /// <summary>
        /// Creates a filter translator that will translate
        /// a specified filter to the native filter.
        /// </summary>
        /// <remarks>
        /// The
        /// translated filters will be subsequently passed to
        /// <see cref="Org.IdentityConnectors.Framework.Api.Operations.SearchApiOp.Search(ObjectClass, Filter, ResultsHandler, OperationOptions)" />
        /// </remarks>
        /// <param name="oclass">The object class for the search. Will never be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// If the caller passes null, the framework will convert this into
        /// an empty set of options, so SPI need not worry
        /// about this ever being null.</param>
        /// <returns>A filter translator.</returns>
        FilterTranslator<T> CreateFilterTranslator(ObjectClass oclass, OperationOptions options);
        /// <summary>
        /// This will be called by ConnectorFacade, once for each native query produced
        /// by the FilterTranslator.
        /// </summary>
        /// <remarks>
        /// If there is more than one query the results will
        /// automatically be merged together and duplicates eliminated. NOTE
        /// that this implies an in-memory data structure that holds a set of
        /// Uids, so memory usage in the event of multiple queries will be O(N)
        /// where N is the number of results. That is why it is important that
        /// the FilterTranslator implement OR if possible.
        /// </remarks>
        /// <param name="oclass">The object class for the search. Will never be null.</param>
        /// <param name="query">The native query to run. A value of null means 'return everything for the given object class'.</param>
        /// <param name="handler">Results should be returned to this handler</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// If the caller passes null, the framework will convert this into
        /// an empty set of options, so SPI need not worry
        /// about this ever being null.</param>
        void ExecuteQuery(ObjectClass oclass, T query, ResultsHandler handler, OperationOptions options);
    }
    /// <summary>
    /// Receive synchronization events from the resource.
    /// </summary>
    /// <seealso cref="Org.IdentityConnectors.Framework.Api.Operations.SyncApiOp" />
    public interface SyncOp : SPIOperation
    {
        /// <summary>
        /// Perform a synchronization.
        /// </summary>
        /// <param name="objClass">The object class to synchronize. Must not be null.</param>
        /// <param name="token">The token representing the last token from the previous sync.
        /// Should be null if this is the first sync for the given
        /// resource.</param>
        /// <param name="handler">The result handler Must not be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// If the caller passes null, the framework will convert this into
        /// an empty set of options, so SPI need not worry
        /// about this ever being null.</param>
        void Sync(ObjectClass objClass, SyncToken token,
                SyncResultsHandler handler,
                OperationOptions options);
        /// <summary>
        /// Returns the token corresponding to the latest sync delta.
        /// </summary>
        /// <remarks>
        /// This is to support applications that may wish to sync starting
        /// "now".
        /// </remarks>
        /// <returns>The latest token or null if there is no sync data.</returns>
        SyncToken GetLatestSyncToken(ObjectClass objectClass);
    }

    /// <summary>
    /// The developer of a Connector should implement either this interface or the
    /// <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateAttributeValuesOp" /> interface if the Connector will allow an authorized
    /// caller to update (i.e., modify or replace) objects on the target resource.
    /// </summary>
    /// <remarks>
    /// <para>
    /// This update method is simpler to implement than {link UpdateAttributeValuesOp},
    /// which must handle any of several different types of update that the caller
    /// may specify. However a true implementation of {link UpdateAttributeValuesOp}
    /// offers better performance and atomicity semantics.
    /// </para>
    /// </remarks>
    /// <author>Will Droste</author>
    /// <version>$Revision $</version>
    /// <since>1.0</since>
    public interface UpdateOp : SPIOperation
    {
        /// <summary>
        /// Update the object specified by the <see cref="ObjectClass" /> and <see cref="Uid" />,
        /// replacing the current values of each attribute with the values
        /// provided.
        /// </summary>
        /// <remarks>
        /// <para>
        /// For each input attribute, replace
        /// all of the current values of that attribute in the target object with
        /// the values of that attribute.
        /// </para>
        /// <para>
        /// If the target object does not currently contain an attribute that the
        /// input set contains, then add this
        /// attribute (along with the provided values) to the target object.
        /// </para>
        /// <para>
        /// If the value of an attribute in the input set is
        /// <code>null</code>, then do one of the following, depending on
        /// which is most appropriate for the target:
        /// <list type="bullet">
        /// <item>
        /// <description>If possible, <em>remove</em> that attribute from the target
        /// object entirely.
        /// </description>
        /// </item>
        /// <item>
        /// <description>Otherwise, <em>replace all of the current values</em> of that
        /// attribute in the target object with a single value of
        /// <code>null</code>.
        /// </description>
        /// </item>
        /// </list>
        /// </para>
        /// </remarks>
        /// <param name="objclass">the type of object to modify. Will never be null.</param>
        /// <param name="uid">the uid of the object to modify. Will never be null.</param>
        /// <param name="replaceAttributes">set of new <see cref="ConnectorAttribute" />. the values in this set
        /// represent the new, merged values to be applied to the object.
        /// This set may also include <see cref="OperationalAttributes" />.
        /// Will never be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// Will never be null.</param>
        /// <returns>the <see cref="Uid" /> of the updated object in case the update changes
        /// the formation of the unique identifier.</returns>
        /// <exception cref="Org.IdentityConnectors.Framework.Common.Exceptions.UnknownUidException">iff the <see cref="Uid" /> does not exist on the resource.</exception>
        Uid Update(ObjectClass objclass,
                Uid uid,
                ICollection<ConnectorAttribute> replaceAttributes,
                OperationOptions options);
    }

    /// <summary>
    /// More advanced implementation of <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp" /> to be implemented by
    /// connectors that wish to offer better performance and atomicity semantics
    /// for the methods <see cref="Org.IdentityConnectors.Framework.Api.Operations.UpdateApiOp.AddAttributeValues(ObjectClass, Uid, ICollection{ConnectorAttribute}, OperationOptions)" />
    /// and <see cref="Org.IdentityConnectors.Framework.Api.Operations.UpdateApiOp.RemoveAttributeValues(ObjectClass, Uid, ICollection{ConnectorAttribute}, OperationOptions)" />.
    /// </summary>
    public interface UpdateAttributeValuesOp : UpdateOp
    {

        /// <summary>
        /// Update the object specified by the <see cref="ObjectClass" /> and <see cref="Uid" />,
        /// adding to the current values of each attribute the values provided.
        /// </summary>
        /// <remarks>
        /// <para>
        /// For each attribute that the input set contains, add to
        /// the current values of that attribute in the target object all of the
        /// values of that attribute in the input set.
        /// </para>
        /// <para>
        /// NOTE that this does not specify how to handle duplicate values.
        /// The general assumption for an attribute of a <code>ConnectorObject</code>
        /// is that the values for an attribute may contain duplicates.
        /// Therefore, in general simply <em>append</em> the provided values
        /// to the current value for each attribute.
        /// </para>
        /// <para>
        /// </para>
        /// </remarks>
        /// <param name="objclass">the type of object to modify. Will never be null.</param>
        /// <param name="uid">the uid of the object to modify. Will never be null.</param>
        /// <param name="valuesToAdd">set of <see cref="ConnectorAttribute" /> deltas. The values for the attributes
        /// in this set represent the values to add to attributes in the object.
        /// merged. This set will never include <see cref="OperationalAttributes" />.
        /// Will never be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// Will never be null.</param>
        /// <returns>the <see cref="Uid" /> of the updated object in case the update changes
        /// the formation of the unique identifier.</returns>
        /// <exception cref="Org.IdentityConnectors.Framework.Common.Exceptions.UnknownUidException">iff the <see cref="Uid" /> does not exist on the resource.</exception>
        Uid AddAttributeValues(ObjectClass objclass,
                Uid uid,
                ICollection<ConnectorAttribute> valuesToAdd,
                OperationOptions options);

        /// <summary>
        /// Update the object specified by the <see cref="ObjectClass" /> and <see cref="Uid" />,
        /// removing from the current values of each attribute the values provided.
        /// </summary>
        /// <remarks>
        /// <para>
        /// For each attribute that the input set contains,
        /// remove from the current values of that attribute in the target object
        /// any value that matches one of the values of the attribute from the input set.
        /// </para>
        /// <para>
        /// NOTE that this does not specify how to handle unmatched values.
        /// The general assumption for an attribute of a <code>ConnectorObject</code>
        /// is that the values for an attribute are merely <i>representational state</i>.
        /// Therefore, the implementer should simply ignore any provided value
        /// that does not match a current value of that attribute in the target
        /// object. Deleting an unmatched value should always succeed.
        /// </para>
        /// </remarks>
        /// <param name="objclass">the type of object to modify. Will never be null.</param>
        /// <param name="uid">the uid of the object to modify. Will never be null.</param>
        /// <param name="valuesToRemove">set of <see cref="ConnectorAttribute" /> deltas. The values for the attributes
        /// in this set represent the values to remove from attributes in the object.
        /// merged. This set will never include <see cref="OperationalAttributes" />.
        /// Will never be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// Will never be null..</param>
        /// <returns>the <see cref="Uid" /> of the updated object in case the update changes
        /// the formation of the unique identifier.</returns>
        /// <exception cref="Org.IdentityConnectors.Framework.Common.Exceptions.UnknownUidException">iff the <see cref="Uid" /> does not exist on the resource.</exception>
        Uid RemoveAttributeValues(ObjectClass objclass,
                Uid uid,
                ICollection<ConnectorAttribute> valuesToRemove,
                OperationOptions options);
    }

    /// <summary>
    /// Tests the connector <see cref="Configuration"/>.
    /// <para>
    /// Unlike validation performed by <see cref="M:Configuration:Validate"/>, testing a configuration
    /// checks that any pieces of environment referred by the configuration are available.
    /// For example, the connector could make a physical connection to a host specified
    /// in the configuration to check that it exists and that the credentials
    /// specified in the configuration are usable.
    /// </para>
    /// <para>
    /// This operation may be invoked before the configuration has been validated.
    /// An implementation is free to validate the configuration before testing it.
    /// </para>
    public interface TestOp : SPIOperation
    {
        /// <summary>
        /// Tests the <see cref="Configuration"/> with the connector.
        /// </summay>
        /// <exception cref="System.Exception">iff the configuration is not valid or the test failed. Implementations
        /// are encouraged to throw the most specific exception available.
        /// When no specific exception is available, implementations can throw
        /// <see cref="Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException"/>.</exception>
        void Test();
    }

    /// <summary>
    /// Tagging interface for the <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> SPI.
    /// </summary>
    public interface SPIOperation
    {
    }
}