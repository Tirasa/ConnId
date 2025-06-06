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
 * Portions Copyrighted 2016 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * Builder for {@link OperationOptions}.
 */
public final class OperationOptionsBuilder {

    private final Map<String, Object> options;

    /**
     * Create a builder with an empty set of options.
     */
    public OperationOptionsBuilder() {
        options = new HashMap<String, Object>();
    }

    /**
     * Create a builder from an existing set of options.
     *
     * @param options The existing set of options. Must not be null.
     */
    public OperationOptionsBuilder(final OperationOptions options) {
        Assertions.nullCheck(options, "options");
        // clone options to do a deep copy in case anything
        // is an array
        @SuppressWarnings("unchecked")
        Map<String, Object> operationOptionsClone =
                (Map<String, Object>) SerializerUtil.cloneObject(options.getOptions());
        this.options = operationOptionsClone;
    }

    /**
     * Sets a given option and a value for that option.
     *
     * @param name The name of the option
     * @param value The value of the option. Must be one of the types that we can serialize. See
     * {@link ObjectSerializerFactory} for a list of supported types.
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setOption(final String name, final Object value) {
        Assertions.blankCheck(name, "name");
        // don't validate value here - we do that in
        // the constructor of OperationOptions - that's
        // really the only place we can truly enforce this
        options.put(name, value);
        return this;
    }

    /**
     * Sets the {@link OperationOptions#OP_ATTRIBUTES_TO_GET} option.
     *
     * @param attrNames list of {@link Attribute} names.
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setAttributesToGet(final String... attrNames) {
        Assertions.nullCheck(attrNames, "attrNames");
        // don't validate value here - we do that in
        // the constructor of OperationOptions - that's
        // really the only place we can truly enforce this
        options.put(OperationOptions.OP_ATTRIBUTES_TO_GET, attrNames);
        return this;
    }

    /**
     * Sets the {@link OperationOptions#OP_ATTRIBUTES_TO_GET} option.
     *
     * @param attrNames list of {@link Attribute} names.
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setAttributesToGet(final Collection<String> attrNames) {
        Assertions.nullCheck(attrNames, "attrNames");
        // don't validate value here - we do that in
        // the constructor of OperationOptions - that's
        // really the only place we can truly enforce this
        String[] attrs = new String[attrNames.size()];
        attrs = attrNames.toArray(attrs);
        options.put(OperationOptions.OP_ATTRIBUTES_TO_GET, attrs);
        return this;
    }

    /**
     * Sets the {@link OperationOptions#OP_RETURN_DEFAULT_ATTRIBUTES} option.
     *
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setReturnDefaultAttributes(final Boolean flag) {
        Assertions.nullCheck(flag, "flag");
        options.put(OperationOptions.OP_RETURN_DEFAULT_ATTRIBUTES, flag);
        return this;
    }

    /**
     * Set the run with password option.
     *
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setRunWithPassword(final GuardedString password) {
        Assertions.nullCheck(password, "password");
        options.put(OperationOptions.OP_RUN_WITH_PASSWORD, password);
        return this;
    }

    /**
     * Set the run as user option.
     *
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setRunAsUser(final String user) {
        Assertions.nullCheck(user, "user");
        options.put(OperationOptions.OP_RUN_AS_USER, user);
        return this;
    }

    /**
     * Convenience method to set {@link OperationOptions#OP_SCOPE}.
     *
     * @param scope The scope. May not be null.
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setScope(final String scope) {
        Assertions.nullCheck(scope, "scope");
        options.put(OperationOptions.OP_SCOPE, scope);
        return this;
    }

    /**
     * Convenience method to set {@link OperationOptions#OP_CONTAINER}.
     *
     * @param container The container. May not be null.
     * @return A this reference to allow chaining
     */
    public OperationOptionsBuilder setContainer(final QualifiedUid container) {
        Assertions.nullCheck(container, "container");
        options.put(OperationOptions.OP_CONTAINER, container);
        return this;
    }

    /**
     * Convenience method to set {@link OperationOptions#OP_PAGED_RESULTS_COOKIE}.
     *
     * @param pagedResultsCookie The pagedResultsCookie. May not be null.
     * @return A this reference to allow chaining
     * @since 1.4
     */
    public OperationOptionsBuilder setPagedResultsCookie(final String pagedResultsCookie) {
        Assertions.nullCheck(pagedResultsCookie, "pagedResultsCookie");
        options.put(OperationOptions.OP_PAGED_RESULTS_COOKIE, pagedResultsCookie);
        return this;
    }

    /**
     * Convenience method to set {@link OperationOptions#OP_PAGED_RESULTS_OFFSET}.
     *
     * @param pagedResultsOffset The pagedResultsOffset. May not be null.
     * @return A this reference to allow chaining
     * @since 1.4
     */
    public OperationOptionsBuilder setPagedResultsOffset(final Integer pagedResultsOffset) {
        Assertions.nullCheck(pagedResultsOffset, "pagedResultsOffset");
        options.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, pagedResultsOffset);
        return this;
    }

    /**
     * Convenience method to set {@link OperationOptions#OP_PAGE_SIZE}.
     *
     * @param pageSize The pageSize. May not be null.
     * @return A this reference to allow chaining
     * @since 1.4
     */
    public OperationOptionsBuilder setPageSize(final Integer pageSize) {
        Assertions.nullCheck(pageSize, "pageSize");
        options.put(OperationOptions.OP_PAGE_SIZE, pageSize);
        return this;
    }

    /**
     * Convenience method to set {@link OperationOptions#OP_SORT_KEYS}.
     *
     * @param sortKeys The sort keys. May not be null.
     * @return A this reference to allow chaining
     * @since 1.4
     */
    public OperationOptionsBuilder setSortKeys(final List<SortKey> sortKeys) {
        Assertions.nullCheck(sortKeys, "sortKeys");
        options.put(OperationOptions.OP_SORT_KEYS, sortKeys.toArray(new SortKey[sortKeys.size()]));
        return this;
    }

    /**
     * Convenience method to set {@link OperationOptions#OP_SORT_KEYS}.
     *
     * @param sortKeys The sort keys. May not be null.
     * @return A this reference to allow chaining
     * @since 1.4
     */
    public OperationOptionsBuilder setSortKeys(final SortKey... sortKeys) {
        Assertions.nullCheck(sortKeys, "sortKeys");
        options.put(OperationOptions.OP_SORT_KEYS, sortKeys);
        return this;
    }

    /**
     * Convenience method to set {@link OperationOptions#OP_ALLOW_PARTIAL_RESULTS}.
     *
     * @param allowPartialResults Flag indicating whether partial results are allowed.
     * @return A this reference to allow chaining
     * @since 1.4.2
     */
    public OperationOptionsBuilder setAllowPartialResults(final boolean allowPartialResults) {
        options.put(OperationOptions.OP_ALLOW_PARTIAL_RESULTS, allowPartialResults);
        return this;
    }
    
    /**
     * Convenience method to set {@link OperationOptions#OP_ALLOW_PARTIAL_ATTRIBUTE_VALUES}.
     *
     * @param allowPartialResults Flag indicating whether partial attribute values are allowed.
     * @return A this reference to allow chaining
     * @since 1.4.3
     */
    public OperationOptionsBuilder setAllowPartialAttributeValues(final boolean allowPartialAttributeValues) {
        options.put(OperationOptions.OP_ALLOW_PARTIAL_ATTRIBUTE_VALUES, allowPartialAttributeValues);
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
        return options;
    }

    /**
     * Creates the <code>OperationOptions</code>.
     *
     * @return The newly-created <code>OperationOptions</code>
     */
    public OperationOptions build() {
        return new OperationOptions(options);
    }
}
