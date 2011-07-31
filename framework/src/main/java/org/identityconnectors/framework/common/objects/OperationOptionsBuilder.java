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
package org.identityconnectors.framework.common.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * Builder for {@link OperationOptions}.
 */
public final class OperationOptionsBuilder {
    private final Map<String, Object> _options;

    /**
     * Create a builder with an empty set of options.
     */
    public OperationOptionsBuilder() {
        _options = new HashMap<String, Object>();
    }
    
    /**
     * Create a builder from an existing set of options.
     * @param options The existing set of options. Must not be null.
     */
    public OperationOptionsBuilder(OperationOptions options) {
        Assertions.nullCheck(options, "options");
        // clone options to do a deep copy in case anything
        // is an array
        @SuppressWarnings("unchecked")
        Map<String, Object> operationOptionsClone = (Map<String, Object>) SerializerUtil
                .cloneObject(options.getOptions());
        _options = operationOptionsClone;
    }

    /**
     * Sets a given option and a value for that option.
     * 
     * @param name
     *            The name of the option
     * @param value
     *            The value of the option. Must be one of the types that we can
     *            serialize. See {@link ObjectSerializerFactory} for a list of
     *            supported types.
     */
    public OperationOptionsBuilder setOption(String name, Object value) {
        Assertions.blankCheck(name, "name");
        // don't validate value here - we do that in
        // the constructor of OperationOptions - that's
        // really the only place we can truly enforce this
        _options.put(name, value);
        return this;
    }

    /**
     * Sets the {@link OperationOptions#OP_ATTRIBUTES_TO_GET} option.
     * 
     * @param attrNames
     *            list of {@link Attribute} names.
     */
    public OperationOptionsBuilder setAttributesToGet(String... attrNames) {
        Assertions.nullCheck(attrNames, "attrNames");
        // don't validate value here - we do that in
        // the constructor of OperationOptions - that's
        // really the only place we can truly enforce this
        _options.put(OperationOptions.OP_ATTRIBUTES_TO_GET, attrNames);
        return this;
    }

    /**
     * Sets the {@link OperationOptions#OP_ATTRIBUTES_TO_GET} option.
     * 
     * @param attrNames
     *            list of {@link Attribute} names.
     */
    public OperationOptionsBuilder setAttributesToGet(
            Collection<String> attrNames) {
        Assertions.nullCheck(attrNames, "attrNames");
        // don't validate value here - we do that in
        // the constructor of OperationOptions - that's
        // really the only place we can truly enforce this
        String[] attrs = new String[attrNames.size()];
        attrs = attrNames.toArray(attrs);
        _options.put(OperationOptions.OP_ATTRIBUTES_TO_GET, attrs);
        return this;
    }

    /**
     * Set the run with password option.
     */
    public OperationOptionsBuilder setRunWithPassword(GuardedString password) {
        Assertions.nullCheck(password, "password");
        _options.put(OperationOptions.OP_RUN_WITH_PASSWORD, password);
        return this;
    }

    /**
     * Set the run as user option.
     */
    public OperationOptionsBuilder setRunAsUser(String user) {
        Assertions.nullCheck(user, "user");
        _options.put(OperationOptions.OP_RUN_AS_USER, user);
        return this;
    }
    
    /**
     * Convenience method to set {@link OperationOptions#OP_SCOPE}
     * @param scope The scope. May not be null.
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setScope(String scope) {
        Assertions.nullCheck(scope, "scope");
        _options.put(OperationOptions.OP_SCOPE, scope);
        return this;
    }
    
    /**
     * Convenience method to set {@link OperationOptions#OP_CONTAINER}
     * @param container The container. May not be null.
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setContainer(QualifiedUid container) {
        Assertions.nullCheck(container, "container");
        _options.put(OperationOptions.OP_CONTAINER, container);
        return this;
    }

    /**
     * Returns a mutable reference of the options map.
     * 
     * @return A mutable reference of the options map.
     */
    public Map<String, Object> getOptions() {
        // might as well be mutable since it's the builder and
        // we don't want to deep copy anyway
        return _options;
    }

    /**
     * Creates the <code>OperationOptions</code>.
     * 
     * @return The newly-created <code>OperationOptions</code>
     */
    public OperationOptions build() {
        return new OperationOptions(_options);
    }
}
