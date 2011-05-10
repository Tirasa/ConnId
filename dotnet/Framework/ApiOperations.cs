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

using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;

namespace Org.IdentityConnectors.Framework.Api.Operations
{
    /// <summary>
    /// Base interface for all API operations.
    /// </summary>
    public interface APIOperation
    {
    }

    public interface AuthenticationApiOp : APIOperation
    {
        /// <summary>
        /// Most basic authentication available.
        /// </summary>
        /// <param name="username">string that represents the account or user id.</param>
        /// <param name="password">string that represents the password for the account or user.</param>
        /// <exception cref="Exception">iff the credentials do not pass authentication otherwise
        /// nothing.</exception>
        Uid Authenticate(ObjectClass objectClass, string username, GuardedString password, OperationOptions options);
    }

    public interface ResolveUsernameApiOp : APIOperation
    {
        /// <summary>
        /// Resolve the given <see cref="Org.IdentityConnectors.Framework.Api.Operations.AuthenticationApiOp.Authenticate" /> username
        /// to the corresponding <see cref="Uid" />.
        /// </summary>
        /// <remarks>
        /// The <code>Uid</code> is the one
        /// that <see cref="Org.IdentityConnectors.Framework.Api.Operations.AuthenticationApiOp.Authenticate" /> would return
        /// in case of a successful authentication.
        /// </remarks>
        /// <param name="objectClass">The object class to use for authenticate.
        /// Will typically be an account. Must not be null.</param>
        /// <param name="username">string that represents the account or user id.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// May be null.</param>
        /// <returns>Uid The uid of the account that would be used to authenticate.</returns>
        /// <exception cref="Exception">iff the username could not be resolved.</exception>
        Uid ResolveUsername(ObjectClass objectClass, string username, OperationOptions options);
    }

    /// <summary>
    /// Operation to create connector objects based on the attributes provided.
    /// </summary>
    public interface CreateApiOp : APIOperation
    {
        /// <summary>
        /// Creates a user based on the ConnectorAttributes provide. The only
        /// required attribute is the ObjectClass and those required by the
        /// Connector. The API will validate the existence of the
        /// ObjectClass attribute and that there are no duplicate name'd
        /// attributes.
        /// </summary>
        /// <param name="attrs">ConnectorAttribtes to create the object.</param>
        /// <returns>Unique id for the created object.</returns>
        Uid Create(ObjectClass oclass, ICollection<ConnectorAttribute> attrs, OperationOptions options);
    }

    /// <summary>
    /// Deletes an object with the specified Uid and ObjectClass on the
    /// resource.
    /// </summary>
    public interface DeleteApiOp : APIOperation
    {
        /// <summary>
        /// Delete the object that the specified Uid identifies (if any).
        /// </summary>
        /// <param name="objectClass">The type of object to delete.</param>
        /// <param name="uid">The unique identitfier for the object to delete.</param>
        /// <exception cref="">Throws UnknowUid if the object does not exist.</exception>
        void Delete(ObjectClass objectClass, Uid uid, OperationOptions options);
    }

    /// <summary>
    /// Get a particular <see cref="ConnectorObject" /> based on the <see cref="Uid" />.
    /// </summary>
    public interface GetApiOp : APIOperation
    {
        /// <summary>
        /// Get a particular <see cref="ConnectorObject" /> based on the <see cref="Uid" />.
        /// </summary>
        /// <param name="uid">the unique id of the object that to get.</param>
        /// <returns>
        /// <see cref="ConnectorObject" /> based on the <see cref="Uid" /> provided.</returns>
        ConnectorObject GetObject(ObjectClass objClass, Uid uid, OperationOptions options);
    }

    /// <summary>
    /// Get the schema from the <see cref="Org.IdentityConnectors.Framework.Spi.Connector" />.
    /// </summary>
    public interface SchemaApiOp : APIOperation
    {
        /// <summary>
        /// Retrieve the basic schema of this <see cref="Org.IdentityConnectors.Framework.Spi.Connector" />.
        /// </summary>
        Schema Schema();
    }


    public interface SearchApiOp : APIOperation
    {
        /// <summary>
        /// Search the resource for all objects that match the filter.
        /// </summary>
        /// <param name="filter">Reduces the number of entries to only those that match the
        /// <see cref="Filter" /> provided.</param>
        /// <param name="handler">class responsible for working with the objects returned from
        /// the search.</param>
        /// <exception cref="Exception">iff there is problem during the processing of the results.</exception>
        void Search(ObjectClass oclass, Filter filter, ResultsHandler handler, OperationOptions options);
    }

    /// <summary>
    /// Runs a script in the same JVM or .Net Runtime as the <code>Connector</code>.
    /// </summary>
    /// <remarks>
    /// That is, if you are using a <b>local</b> framework, the script will be
    /// run in your JVM. If you are connected to a <b>remote</b> framework, the
    /// script will be run in the remote JVM or .Net Runtime.
    /// <para>
    /// This API allows an application to run a script in the context
    /// of any connector.  (A connector need not implement any particular interface
    /// in order to enable this.)  The <b>minimum contract</b> to which each connector
    /// <b>must</b> adhere is as follows:
    /// <list type="number">
    /// <item>
    /// <description>Script will run in the same classloader/execution environment
    /// as the connector, so the script will have access to all the classes
    /// to which the connector has access.
    /// </description>
    /// </item>
    /// <item>
    /// <description>Script will have access to a <code>"connector"</code> variable
    /// that is equivalent to an initialized instance of a connector.
    /// Thus, at a minimum the script will be able to access
    /// <see cref="Connector.getConfiguration() the configuration of the connector" />.
    /// </description>
    /// </item>
    /// <item>
    /// <description>Script will have access to any
    /// <see cref="ScriptContext.ScriptArguments" />
    /// passed in by the application.
    /// </description>
    /// </item>
    /// </list>
    /// </para>
    /// <para>
    /// A connector that implements <see cref="Org.IdentityConnectors.Framework.Spi.Operations.ScriptOnConnectorOp" />
    /// may provide more variables than what is described above.
    /// A connector also may perform special processing
    /// for <see cref="OperationOptions" /> specific to that connector.
    /// Consult the javadoc of each particular connector to find out what
    /// additional capabilities, if any, that connector exposes for use in scripts.
    /// </para>
    /// <para>
    /// <b>NOTE:</b> A caller who wants to execute scripts on a connector
    /// should assume that <em>a script must not use any method of the connector
    /// beyond the minimum contract described above</em>,
    /// unless the connector explicitly documents that method as
    /// "for use by connector script".  The primary function of a connector
    /// is to implement the SPI in the context of the Connector framework.
    /// In general, no caller should invoke Connector methods directly
    /// --whether by a script or by other means.
    /// </para>
    /// </remarks>
    public interface ScriptOnConnectorApiOp : APIOperation
    {
        /// <summary>
        /// Runs the script.
        /// </summary>
        /// <param name="request">- The script and arguments to run.</param>
        /// <param name="options">- Additional options that control how the script is
        /// run. The framework does not currently recognize any options
        /// but specific connectors might. Consult the documentation
        /// for each connector to identify supported options.</param>
        /// <returns>The result of the script. The return type must be
        /// a type that the framework supports for serialization.</returns>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        Object RunScriptOnConnector(ScriptContext request,
                OperationOptions options);
    }
    /// <summary>
    /// Runs a script on the target resource that a connector manages.
    /// </summary>
    /// <remarks>
    /// This API operation is supported only for a connector that implements
    /// <see cref="Org.IdentityConnectors.Framework.Spi.Operations.ScriptOnResourceOp" />.
    /// <para>
    /// The contract here at the API level is intentionally very loose.
    /// Each connector decides what script languages it supports,
    /// what running a script <b>on</b> a target resource actually means,
    /// and what script options (if any) that connector supports.
    /// Refer to the javadoc of each particular connector for more information.
    /// </para>
    /// </remarks>
    public interface ScriptOnResourceApiOp : APIOperation
    {
        /// <summary>
        /// Runs a script on a specific target resource.
        /// </summary>
        /// <param name="request">The script and arguments to run.</param>
        /// <param name="options">Additional options which control how the script is
        /// run. Please refer to the connector documentation for supported
        /// options.</param>
        /// <returns>The result of the script. The return type must be
        /// a type that the connector framework supports for serialization.
        /// See <see cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" /> for a list of supported return types.</returns>
        Object RunScriptOnResource(ScriptContext request,
                OperationOptions options);
    }
    /// <summary>
    /// Receive synchronization events from the resource.
    /// </summary>
    /// <remarks>
    /// This will be supported by
    /// connectors that implement <see cref="Org.IdentityConnectors.Framework.Spi.Operations.SyncOp" />.
    /// </remarks>
    /// <seealso cref="Org.IdentityConnectors.Framework.Spi.Operations.SyncOp" />
    public interface SyncApiOp : APIOperation
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
        /// May be null.</param>
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
    /// Updates a <see cref="ConnectorObject" />.
    /// </summary>
    /// <remarks>
    /// This operation
    /// is supported for those connectors that implement
    /// either <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp" /> or the more advanced
    /// <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateAttributeValuesOp" />.
    /// </remarks>
    public interface UpdateApiOp : APIOperation
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
        /// <param name="objclass">the type of object to modify. Must not be null.</param>
        /// <param name="uid">the uid of the object to modify. Must not be null.</param>
        /// <param name="replaceAttributes">set of new <see cref="ConnectorAttribute" />. the values in this set
        /// represent the new, merged values to be applied to the object.
        /// This set may also include <see cref="OperationalAttributes" />.
        /// Must not be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// May be null.</param>
        /// <returns>the <see cref="Uid" /> of the updated object in case the update changes
        /// the formation of the unique identifier.</returns>
        /// <exception cref="Org.IdentityConnectors.Framework.Common.Exceptions.UnknownUidException">iff the <see cref="Uid" /> does not exist on the resource.</exception>
        Uid Update(ObjectClass objclass,
                Uid uid,
                ICollection<ConnectorAttribute> replaceAttributes,
                OperationOptions options);

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
        /// IMPLEMENTATION NOTE: for connectors that merely implement <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp" />
        /// and not <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateAttributeValuesOp" /> this method will be simulated by
        /// fetching, merging, and calling
        /// <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp.Update(ObjectClass, Uid, ICollection{ConnectorAttribute}, OperationOptions)" />. Therefore,
        /// connector implementations are encourage to implement <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateAttributeValuesOp" />
        /// from a performance and atomicity standpoint.
        /// </para>
        /// </remarks>
        /// <param name="objclass">the type of object to modify. Must not be null.</param>
        /// <param name="uid">the uid of the object to modify. Must not be null.</param>
        /// <param name="valuesToAdd">set of <see cref="ConnectorAttribute" /> deltas. The values for the attributes
        /// in this set represent the values to add to attributes in the object.
        /// merged. This set must not include <see cref="OperationalAttributes" />.
        /// Must not be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// May be null.</param>
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
        /// <para>
        /// IMPLEMENTATION NOTE: for connectors that merely implement <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp" />
        /// and not <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateAttributeValuesOp" /> this method will be simulated by
        /// fetching, merging, and calling
        /// <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp.Update(ObjectClass, Uid, ICollection{ConnectorAttribute}, OperationOptions)" />. Therefore,
        /// connector implementations are encourage to implement <see cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateAttributeValuesOp" />
        /// from a performance and atomicity standpoint.
        /// </para>
        /// </remarks>
        /// <param name="objclass">the type of object to modify. Must not be null.</param>
        /// <param name="uid">the uid of the object to modify. Must not be null.</param>
        /// <param name="valuesToRemove">set of <see cref="ConnectorAttribute" /> deltas. The values for the attributes
        /// in this set represent the values to remove from attributes in the object.
        /// merged. This set must not include <see cref="OperationalAttributes" />.
        /// Must not be null.</param>
        /// <param name="options">additional options that impact the way this operation is run.
        /// May be null.</param>
        /// <returns>the <see cref="Uid" /> of the updated object in case the update changes
        /// the formation of the unique identifier.</returns>
        /// <exception cref="Org.IdentityConnectors.Framework.Common.Exceptions.UnknownUidException">iff the <see cref="Uid" /> does not exist on the resource.</exception>
        Uid RemoveAttributeValues(ObjectClass objclass,
                Uid uid,
                ICollection<ConnectorAttribute> valuesToRemove,
                OperationOptions options);

    }

    /// <summary>
    /// Validates the <see cref="Org.IdentityConnectors.Framework.Api.APIConfiguration"/>.
    /// <para>
    /// A valid configuration is one that is ready to be used by the connector:
    /// it is complete (all the required properties have been given values) 
    /// and the property values are well-formed (are in the expected range, 
    /// have the expected format, etc.)
    /// </para>
    public interface ValidateApiOp : APIOperation
    {
        /// <summary>
        /// Validates the <see cref="Org.IdentityConnectors.Framework.Api.APIConfiguration"/>.
        /// </summary>
        /// <exception cref="System.Exception">iff the configuration is not valid.</exception>
        void Validate();
    }

    /// <summary>
    /// Tests the <see cref="Org.IdentityConnectors.Framework.Api.APIConfiguration"/> with the connector.
    /// <para>
    /// Unlike validation performed by <see cref="M:Org.IdentityConnectors.Framework.Api.Operations.ValidateApiOp.Validate"/>, testing a configuration should
    /// check that any pieces of environment referred by the configuration are available.
    /// For example the connector could make a physical connection to a host specified
    /// in the configuration to check that it exists and that the credentials
    /// specified in the configuration are usable.
    /// </para>
    /// <para>
    /// Since this operation may connect to the resource, it may be slow. Clients are
    /// advised not to invoke this operation often, such as before every provisioning operation.
    /// This operation is <strong>not</strong> intended to check that the connector is alive
    /// (i.e., its physical connection to the resource has not timed out).
    /// </para>
    /// <para>
    /// This operation may be invoked before the configuration has been validated.
    /// </para>
    /// </summary>
    public interface TestApiOp : APIOperation
    {
        /// <summary>
        /// Tests the current <see cref="Org.IdentityConnectors.Framework.Api.APIConfiguration"/> with the connector.
        /// </summary>
        /// <exception cref="System.Exception">iff the configuration is not valid or the test failed.</exception>
        void Test();
    }
}