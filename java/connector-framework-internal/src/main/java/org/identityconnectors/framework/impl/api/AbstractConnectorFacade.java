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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2014-2018 Evolveum
 * Portions Copyrighted 2015 ConnId
 */
package org.identityconnectors.framework.impl.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Set;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.ResolveUsernameApiOp;
import org.identityconnectors.framework.api.operations.SchemaApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.api.operations.UpdateDeltaApiOp;
import org.identityconnectors.framework.api.operations.ValidateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * Implements all the methods of the facade.
 */
public abstract class AbstractConnectorFacade implements ConnectorFacade {
	
	private static final Log LOG = Log.getLog(AbstractConnectorFacade.class);

    private final APIConfigurationImpl configuration;

    private final String connectorFacadeKey;

    /**
     * Builds up the maps of supported operations and calls.
     */
    public AbstractConnectorFacade(final APIConfigurationImpl configuration) {
        Assertions.nullCheck(configuration, "configuration");
        // clone in case application tries to modify
        // after the fact. this is necessary to
        // ensure thread-safety of a ConnectorFacade
        // also, configuration is used as a key in the
        // pool, so it is important that it not be modified.
        byte[] bytes = SerializerUtil.serializeBinaryObject(configuration);
        connectorFacadeKey = Base64.getEncoder().encodeToString(bytes);
        this.configuration = (APIConfigurationImpl) SerializerUtil.deserializeBinaryObject(bytes);
        // parent ref not included in the clone
        this.configuration.setConnectorInfo(configuration.getConnectorInfo());
    }

    /**
     * Builds up the maps of supported operations and calls.
     */
    public AbstractConnectorFacade(final String configuration, final AbstractConnectorInfo connectorInfo) {
        Assertions.nullCheck(configuration, "configuration");
        Assertions.nullCheck(connectorInfo, "connectorInfo");
        this.connectorFacadeKey = configuration;
        this.configuration = (APIConfigurationImpl) SerializerUtil.deserializeBase64Object(configuration);
        // parent ref not included in the clone
        this.configuration.setConnectorInfo(connectorInfo);
    }

    /**
     * Return an instance of an API operation.
     *
     * @return <code>null</code> if the operation is not support otherwise return an instance of the operation.
     * @see org.identityconnectors.framework.api.ConnectorFacade#getOperation(java.lang.Class)
     */
    @Override
    public final APIOperation getOperation(Class<? extends APIOperation> api) {
        if (!configuration.isSupportedOperation(api)) {
            return null;
        }
        return getOperationImplementation(api);
    }

    /**
     * Gets the unique generated identifier of this ConnectorFacade.
     *
     * It's not guaranteed that the equivalent configuration will generate the same configuration key. Always use the
     * generated value and maintain it in the external application.
     *
     * @return identifier of this ConnectorFacade instance.
     */
    @Override
    public final String getConnectorFacadeKey() {
        return connectorFacadeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Class<? extends APIOperation>> getSupportedOperations() {
        return configuration.getSupportedOperations();
    }

    // =======================================================================
    // Operation API Methods
    // =======================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public final Schema schema() {
        return ((SchemaApiOp) this.getOperationCheckSupported(SchemaApiOp.class)).schema();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Uid create(final ObjectClass objectClass, final Set<Attribute> createAttributes,
            final OperationOptions options) {

        return ((CreateApiOp) getOperationCheckSupported(CreateApiOp.class)).
                create(objectClass, createAttributes, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void delete(final ObjectClass objectClass, final Uid uid, final OperationOptions options) {
        ((DeleteApiOp) this.getOperationCheckSupported(DeleteApiOp.class)).delete(objectClass, uid, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SearchResult search(final ObjectClass objectClass, final Filter filter,
            ResultsHandler handler, final OperationOptions options) {

        if (LoggingProxy.isLoggable()) {
            handler = new SearchResultsHandlerLoggingProxy(handler, LOG, null);
        }
        return ((SearchApiOp) this.getOperationCheckSupported(SearchApiOp.class)).
                search(objectClass, filter, handler, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Uid update(final ObjectClass objectClass, final Uid uid,
            final Set<Attribute> attrs, final OperationOptions options) {

        return ((UpdateApiOp) this.getOperationCheckSupported(UpdateApiOp.class)).
                update(objectClass, uid, attrs, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final Set<AttributeDelta> updateDelta(final ObjectClass objectClass, final Uid uid,
            final Set<AttributeDelta> attrsDelta, final OperationOptions options) {

        return ((UpdateDeltaApiOp) this.getDeltaOperationCheckSupported(UpdateDeltaApiOp.class, UpdateApiOp.class)).
                updateDelta(objectClass, uid, attrsDelta, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Uid addAttributeValues(final ObjectClass objclass, final Uid uid,
            final Set<Attribute> attrs, final OperationOptions options) {

        return ((UpdateApiOp) this.getOperationCheckSupported(UpdateApiOp.class)).
                addAttributeValues(objclass, uid, attrs, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Uid removeAttributeValues(final ObjectClass objclass, final Uid uid,
            final Set<Attribute> attrs, final OperationOptions options) {

        return ((UpdateApiOp) this.getOperationCheckSupported(UpdateApiOp.class)).
                removeAttributeValues(objclass, uid, attrs, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Uid authenticate(final ObjectClass objectClass, final String username,
            final GuardedString password, final OperationOptions options) {

        return ((AuthenticationApiOp) this.getOperationCheckSupported(AuthenticationApiOp.class)).
                authenticate(objectClass, username, password, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Uid resolveUsername(final ObjectClass objectClass, final String username,
            final OperationOptions options) {

        return ((ResolveUsernameApiOp) this.getOperationCheckSupported(ResolveUsernameApiOp.class)).
                resolveUsername(objectClass, username, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Object runScriptOnConnector(final ScriptContext request, final OperationOptions options) {
        return ((ScriptOnConnectorApiOp) this.getOperationCheckSupported(ScriptOnConnectorApiOp.class)).
                runScriptOnConnector(request, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Object runScriptOnResource(final ScriptContext request, final OperationOptions options) {
        return ((ScriptOnResourceApiOp) this.getOperationCheckSupported(ScriptOnResourceApiOp.class)).
                runScriptOnResource(request, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ConnectorObject getObject(final ObjectClass objectClass, final Uid uid,
            final OperationOptions options) {

        return ((GetApiOp) this.getOperationCheckSupported(GetApiOp.class)).getObject(objectClass, uid, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void test() {
        ((TestApiOp) this.getOperationCheckSupported(TestApiOp.class)).test();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void validate() {
        ((ValidateApiOp) this.getOperationCheckSupported(ValidateApiOp.class)).validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SyncToken sync(final ObjectClass objectClass, final SyncToken token,
            final SyncResultsHandler handler, final OperationOptions options) {

        return ((SyncApiOp) this.getOperationCheckSupported(SyncApiOp.class)).
                sync(objectClass, token, handler, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SyncToken getLatestSyncToken(final ObjectClass objectClass) {
        return ((SyncApiOp) this.getOperationCheckSupported(SyncApiOp.class)).getLatestSyncToken(objectClass);
    }

    private static final String MSG = "Operation ''{0}'' not supported.";

    private APIOperation getOperationCheckSupported(final Class<? extends APIOperation> api) {
        // check if this operation is supported.
        if (!configuration.isSupportedOperation(api)) {
            String str = MessageFormat.format(MSG, api);
            throw new UnsupportedOperationException(str);
        }
        return getOperationImplementation(api);
    }

    @SafeVarargs
    private final APIOperation getDeltaOperationCheckSupported(final Class<? extends APIOperation>... apis) {
        // check if this operation is supported.
        for (Class<? extends APIOperation> api : apis) {
            if (configuration.isSupportedOperation(api)) {
                return getOperationImplementation(UpdateDeltaApiOp.class);
            }
        }
        String str = MessageFormat.format(MSG, (Object[]) apis);
        throw new UnsupportedOperationException(str);

    }

    /**
     * Creates a new {@link APIOperation} proxy given a handler.
     */
    protected APIOperation newAPIOperationProxy(
            final Class<? extends APIOperation> api, final InvocationHandler handler) {

        return (APIOperation) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[] { api }, handler);
    }

    /**
     * Gets the implementation of the given operation.
     *
     * @param api The operation to implement.
     * @return The implementation
     */
    protected abstract APIOperation getOperationImplementation(final Class<? extends APIOperation> api);

    protected final APIConfigurationImpl getAPIConfiguration() {
        return configuration;
    }

    /**
     * Creates the timeout proxy for the given operation.
     *
     * @param api The operation
     * @param target The underlying object
     * @return The proxy
     */
    protected final APIOperation createTimeoutProxy(
            final Class<? extends APIOperation> api, final APIOperation target) {

        int timeout = getAPIConfiguration().getTimeout(api);
        int bufferSize = getAPIConfiguration().getProducerBufferSize();

        DelegatingTimeoutProxy handler = new DelegatingTimeoutProxy(target, timeout, bufferSize);

        return newAPIOperationProxy(api, handler);
    }

    /**
     * Creates a logging proxy.
     *
     * @param api The operation
     * @param target The underlying object
     * @return The proxy
     */
    protected final APIOperation createLoggingProxy(
            final Class<? extends APIOperation> api, final APIOperation target) {

        return newAPIOperationProxy(api, new LoggingProxy(api, target, getInstanceName()));
    }
    
    protected String getInstanceName() {
    	return getAPIConfiguration().getInstanceName();
    }
}
