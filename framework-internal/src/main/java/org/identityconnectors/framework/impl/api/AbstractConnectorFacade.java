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
package org.identityconnectors.framework.impl.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.Set;

import org.identityconnectors.common.Assertions;
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
import org.identityconnectors.framework.api.operations.ValidateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.serializer.SerializerUtil;


/**
 * Implements all the methods of the facade 
 */
public abstract class AbstractConnectorFacade implements ConnectorFacade {

    private final APIConfigurationImpl _configuration;
    

    /**
     * Builds up the maps of supported operations and calls.
     */
    public AbstractConnectorFacade(final APIConfigurationImpl configuration)  {
        Assertions.nullCheck(configuration,"configuration");
        //clone in case application tries to modify
        //after the fact. this is necessary to
        //ensure thread-safety of a ConnectorFacade
        //also, configuration is used as a key in the
        //pool, so it is important that it not be modified.
        _configuration = (APIConfigurationImpl)SerializerUtil.cloneObject(configuration);
        //parent ref not included in the clone
        _configuration.setConnectorInfo(configuration.getConnectorInfo());
    }

    /**
     * Return an instance of an API operation.
     * 
     * @return <code>null</code> if the operation is not support otherwise
     *         return an instance of the operation.
     * @see org.identityconnectors.framework.api.ConnectorFacade#getOperation(java.lang.Class)
     */
    public final APIOperation getOperation(Class<? extends APIOperation> api) {
        if (!getSupportedOperations().contains(api)) {
            return null;
        }
        return getOperationImplementation(api);
    }

    /**
     * {@inheritDoc}
     */
    public final Set<Class<? extends APIOperation>> getSupportedOperations() {
        return _configuration.getSupportedOperations();
    }

    // =======================================================================
    // Operation API Methods
    // =======================================================================
    /**
     * {@inheritDoc}
     */
    public final Schema schema() {
        return ((SchemaApiOp) this.getOperationCheckSupported(SchemaApiOp.class))
                .schema();
    }

    /**
     * {@inheritDoc}
     */
    public final Uid create(final ObjectClass oclass, 
            final Set<Attribute> attrs,
            final OperationOptions options) {
        CreateApiOp op = ((CreateApiOp) getOperationCheckSupported(CreateApiOp.class));
        return op.create(oclass,attrs,options);
    }

    /**
     * {@inheritDoc}
     */
    public final void delete(final ObjectClass objClass, 
            final Uid uid,
            final OperationOptions options) {
        ((DeleteApiOp) this.getOperationCheckSupported(DeleteApiOp.class)).delete(objClass, uid, options);
    }

    /**
     * {@inheritDoc}
     */
    public final void search(final ObjectClass oclass, 
            final Filter filter, 
            final ResultsHandler handler,
            final OperationOptions options) {
        ((SearchApiOp) this.getOperationCheckSupported(SearchApiOp.class)).search(
                oclass,filter, handler,options);
    }

    /**
     * {@inheritDoc}
     */
    public final Uid update(
            final ObjectClass objclass,
            final Uid uid,
            final Set<Attribute> attrs,
            final OperationOptions options) {
        return ((UpdateApiOp) this.getOperationCheckSupported(UpdateApiOp.class))
            .update(objclass, uid, attrs, options);
    }

    /**
     * {@inheritDoc}
     */
    public final Uid addAttributeValues(
            final ObjectClass objclass,
            final Uid uid,
            final Set<Attribute> attrs,
            final OperationOptions options) {
        return ((UpdateApiOp) this.getOperationCheckSupported(UpdateApiOp.class))
            .addAttributeValues(objclass, uid, attrs, options);
    }
    
    /**
     * {@inheritDoc}
     */
    public final Uid removeAttributeValues(
            final ObjectClass objclass,
            final Uid uid,
            final Set<Attribute> attrs,
            final OperationOptions options) {
        return ((UpdateApiOp) this.getOperationCheckSupported(UpdateApiOp.class))
            .removeAttributeValues(objclass, uid, attrs, options);
    }
    
    /**
     * {@inheritDoc}
     */
    public final Uid authenticate(final ObjectClass objectClass, final String username, 
            final GuardedString password,
            final OperationOptions options) {
        return ((AuthenticationApiOp) this
                .getOperationCheckSupported(AuthenticationApiOp.class)).authenticate(
                        objectClass, username, password, options);
    }
    
    /**
     * {@inheritDoc}
     */
    public final Uid resolveUsername(final ObjectClass objectClass, final String username, 
            final OperationOptions options) {
        return ((ResolveUsernameApiOp) this
                .getOperationCheckSupported(ResolveUsernameApiOp.class)).resolveUsername(
                        objectClass, username, options);
    }

    /**
     * {@inheritDoc}
     */
    public final Object runScriptOnConnector(ScriptContext request,
            OperationOptions options) {
        return ((ScriptOnConnectorApiOp) this
                .getOperationCheckSupported(ScriptOnConnectorApiOp.class))
                .runScriptOnConnector(request, options);        
    }
    
    /**
     * {@inheritDoc}
     */
    public final Object runScriptOnResource(ScriptContext request,
            OperationOptions options) {
        return ((ScriptOnResourceApiOp) this
                .getOperationCheckSupported(ScriptOnResourceApiOp.class))
                .runScriptOnResource(request, options);        
    }

    /**
     * {@inheritDoc}
     */
    public final ConnectorObject getObject(ObjectClass objClass, Uid uid, OperationOptions options) {
        return ((GetApiOp) this.getOperationCheckSupported(GetApiOp.class))
                .getObject(objClass, uid, options);
    }

    /**
     * {@inheritDoc}
     */
    public final void test() {
        ((TestApiOp) this.getOperationCheckSupported(TestApiOp.class)).test();
    }

    /**
     * {@inheritDoc}
     */
    public final void validate() {
        ((ValidateApiOp) this.getOperationCheckSupported(ValidateApiOp.class)).validate();        
    }
    
    /**
     * {@inheritDoc}
     */
    public final void sync(ObjectClass objClass, SyncToken token,
            SyncResultsHandler handler,
            OperationOptions options) {
        ((SyncApiOp)this.getOperationCheckSupported(SyncApiOp.class))
        .sync(objClass, token, handler, options);
    }
    
    /**
     * {@inheritDoc}
     */
    public final SyncToken getLatestSyncToken(ObjectClass objectClass) {
        return ((SyncApiOp)this.getOperationCheckSupported(SyncApiOp.class))
        .getLatestSyncToken(objectClass);
    }
    
    private APIOperation getOperationCheckSupported(final Class<? extends APIOperation> api) {
        // check if this operation is supported.
        if (!getSupportedOperations().contains(api)) {
            final String MSG = "Operation ''{0}'' not supported.";
            String str = MessageFormat.format(MSG, api);
            throw new UnsupportedOperationException(str);
        }
        return getOperationImplementation(api);
    }
    
    /**
     * Creates a new {@link APIOperation} proxy given a handler.
     */
    protected APIOperation newAPIOperationProxy(
            final Class<? extends APIOperation> api,
            final InvocationHandler handler) {
        return (APIOperation) Proxy.newProxyInstance(api.getClassLoader(),
                new Class[] { api }, handler);
    }

    /**
     * Gets the implementation of the given operation
     * @param api The operation to implement.
     * @return The implementation
     */
    protected abstract APIOperation getOperationImplementation(final Class<? extends APIOperation> api);

    protected final APIConfigurationImpl getAPIConfiguration() {
        return _configuration;
    }
    
    /**
     * Creates the timeout proxy for the given operation
     * @param api The operation
     * @param target The underlying object
     * @return The proxy
     */
    protected final APIOperation createTimeoutProxy(Class<? extends APIOperation> api,
            APIOperation target) {
        
        int timeout = getAPIConfiguration().getTimeout(api);
        int bufferSize = getAPIConfiguration().getProducerBufferSize();

        DelegatingTimeoutProxy handler = 
            new DelegatingTimeoutProxy(target,timeout,bufferSize);
                
        return newAPIOperationProxy(api, handler);
    }
    
    /**
     * Creates a logging proxy
     * @param api
     * @param target
     * @return
     */
    protected final APIOperation createLoggingProxy(Class<? extends APIOperation> api,
            APIOperation target) {
        return newAPIOperationProxy(api, new LoggingProxy(api, target));
    }
}
