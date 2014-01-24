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
 */

package org.identityconnectors.framework.common.objects;

import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.PrettyStringBuilder;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * Arbitrary options to be passed into various operations. This serves as a
 * catch-all for extra options.
 */
public final class OperationOptions {

    /**
     * An option to use with {@link SearchApiOp} (in conjunction with
     * {@link #OP_CONTAINER}) that specifies how far beneath the
     * {@linkplain #OP_CONTAINER specified container} to search. Must be one of
     * the following values:
     * <ol>
     * <li>{@link #SCOPE_OBJECT}</li>
     * <li>{@link #SCOPE_ONE_LEVEL}</li>
     * <li>{@link #SCOPE_SUBTREE}</li>
     * </ol>
     */
    public static final String OP_SCOPE = "SCOPE";

    /**
     * A value of {@link #OP_SCOPE} that indicates to search for the
     * {@linkplain #OP_CONTAINER specified container} <em>object itself</em>.
     */
    public static final String SCOPE_OBJECT = "object";

    /**
     * A value of {@link #OP_SCOPE} that indicates to search for objects that
     * the {@linkplain #OP_CONTAINER specified container}
     * <em>directly contains</em>.
     */
    public static final String SCOPE_ONE_LEVEL = "onelevel";

    /**
     * A value of {@link #OP_SCOPE} that indicates to search for objects that
     * the {@linkplain #OP_CONTAINER specified container}
     * <em>directly or indirectly contains</em>.
     */
    public static final String SCOPE_SUBTREE = "subtree";

    /**
     * An option to use with {@link SearchApiOp} that specifies the container
     * under which to perform the search. Must be of type {@link QualifiedUid}.
     * Should be implemented for those object classes whose
     * {@link ObjectClassInfo#isContainer()} returns true.
     */
    public static final String OP_CONTAINER = "CONTAINER";

    /**
     * An option to use with {@link ScriptOnResourceApiOp} and possibly others
     * that specifies an account under which to execute the script/operation.
     * The specified account will appear to have performed any action that the
     * script/operation performs.
     * <p>
     * Check the javadoc for a particular connector to see whether that
     * connector supports this option.
     */
    public static final String OP_RUN_AS_USER = "RUN_AS_USER";

    /**
     * An option to use with {@link ScriptOnResourceApiOp} and possibly others
     * that specifies a password under which to execute the script/operation.
     */
    public static final String OP_RUN_WITH_PASSWORD = "RUN_WITH_PASSWORD";

    /**
     * Determines which attributes to retrieve during {@link SearchApiOp} and
     * {@link SyncApiOp}.
     * <p>
     * This option overrides the default behavior, which is for the connector to
     * return exactly the set of attributes that are identified as
     * {@link AttributeInfo#isReturnedByDefault() returned by default} in the
     * schema for that connector.
     * <p>
     * This option allows a client application to request <i>additional
     * attributes</i> that would not otherwise not be returned (generally
     * because such attributes are more expensive for a connector to fetch and
     * to format) and/or to request only a <i>subset of the attributes</i> that
     * would normally be returned.
     */
    public static final String OP_ATTRIBUTES_TO_GET = "ATTRS_TO_GET";

    /**
     * An option to use with {@link SearchApiOp} that specifies an opaque cookie
     * which is used by the connector to track its position in the set of query
     * results.
     */
    public static final String OP_PAGED_RESULTS_COOKIE = "PAGED_RESULTS_COOKIE";

    /**
     * An option to use with {@link SearchApiOp} that specifies the index within
     * the result set of the first result which should be returned.
     */
    public static final String OP_PAGED_RESULTS_OFFSET = "PAGED_RESULTS_OFFSET";

    /**
     * An option to use with {@link SearchApiOp} that specifies the requested
     * page results page size.
     */
    public static final String OP_PAGE_SIZE = "PAGE_SIZE";

    /**
     * An option to use with {@link SearchApiOp} that specifies the sort keys
     * which should be used for ordering the {@link ConnectorObject} returned by
     * search request.
     */
    public static final String OP_SORT_KEYS = "SORT_KEYS";

    private final Map<String, Object> operationOptions;

    /**
     * Public only for serialization; please use {@link OperationOptionsBuilder}
     * .
     *
     * @param operationOptions
     *            The options.
     */
    public OperationOptions(Map<String, Object> operationOptions) {
        for (Object value : operationOptions.values()) {
            FrameworkUtil.checkOperationOptionValue(value);
        }
        // clone options to do a deep copy in case anything
        // is an array
        @SuppressWarnings("unchecked")
        Map<String, Object> operationOptionsClone =
                (Map<String, Object>) SerializerUtil.cloneObject(operationOptions);
        this.operationOptions = CollectionUtil.asReadOnlyMap(operationOptionsClone);
    }

    // NOTE: this method makes a heavy assumption that in OpenICF only arrays
    // occur as mutable values in operation options, and that there is a single
    // level of array/nesting.
    // Really would be better if OpenICF switched to List and not array
    // To more easily return immutable views
    private Map<String, Object> copyMutables(Map<String, Object> operationOptions) {
        Map<String, Object> operationOptionsCopy = new HashMap<String, Object>(operationOptions);
        for (Map.Entry<String, Object> entry : operationOptionsCopy.entrySet()) {
            if (entry.getValue() instanceof Object[]) {
                entry.setValue(((Object[]) entry.getValue()).clone());
            }
        }
        return operationOptionsCopy;
    }

    /**
     * Returns a map of options. Each value in the map must be of a type that
     * the framework can serialize. See {@link ObjectSerializerFactory} for a
     * list of supported types.
     *
     * @return A map of options.
     */
    public Map<String, Object> getOptions() {
        return operationOptions;
    }

    /**
     * Add basic debugging of internal data. {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("OperationOptions: ").append(new PrettyStringBuilder().toString(getOptions()));
        return bld.toString();
    }

    /**
     * Convenience method that returns {@link #OP_SCOPE}.
     *
     * @return The value for {@link #OP_SCOPE}.
     */
    public String getScope() {
        return (String) operationOptions.get(OP_SCOPE);
    }

    /**
     * Convenience method that returns {@link #OP_CONTAINER}.
     *
     * @return The value for {@link #OP_CONTAINER}.
     */
    public QualifiedUid getContainer() {
        return (QualifiedUid) operationOptions.get(OP_CONTAINER);
    }

    /**
     * Get the string array of attribute names to return in the object.
     */
    public String[] getAttributesToGet() {
        return (String[]) operationOptions.get(OP_ATTRIBUTES_TO_GET);
    }

    /**
     * Get the account to run the operation as..
     */
    public String getRunAsUser() {
        return (String) operationOptions.get(OP_RUN_AS_USER);
    }

    /**
     * Get the password to run the operation as..
     */
    public GuardedString getRunWithPassword() {
        return (GuardedString) operationOptions.get(OP_RUN_WITH_PASSWORD);
    }

    /**
     * Returns the opaque cookie which is used by the Connector to track its
     * position in the set of query results. Paged results will be enabled if
     * and only if the page size is non-zero.
     * <p>
     * The cookie must be {@code null} in the initial search request sent by the
     * client. For subsequent search requests the client must include the cookie
     * returned with the previous search result, until the resource provider
     * returns a {@code null} cookie indicating that the final page of results
     * has been returned.
     *
     * @return The opaque cookie which is used by the Connector to track its
     *         position in the set of search results, or {@code null} if paged
     *         results are not requested (when the page size is 0), or if the
     *         first page of results is being requested (when the page size is
     *         non-zero).
     * @see #getPageSize()
     * @see #getPagedResultsOffset()
     * @since 1.4
     */
    public String getPagedResultsCookie() {
        return (String) operationOptions.get(OP_PAGED_RESULTS_COOKIE);
    };

    /**
     * Returns the index within the result set of the first result which should
     * be returned. Paged results will be enabled if and only if the page size
     * is non-zero. If the parameter is not present or a value less than 1 is
     * specified then the page following the previous page returned will be
     * returned. A value equal to or greater than 1 indicates that a specific
     * page should be returned starting from the position specified.
     *
     * @return The index within the result set of the first result which should
     *         be returned.
     * @see #getPageSize()
     * @see #getPagedResultsCookie()
     * @since 1.4
     */
    public Integer getPagedResultsOffset() {
        return (Integer) operationOptions.get(OP_PAGED_RESULTS_OFFSET);
    };

    /**
     * Returns the requested page results page size or {@code 0} if paged
     * results are not required. For all paged result requests other than the
     * initial request, a cookie should be provided with the search request. See
     * {@link #getPagedResultsCookie()} for more information.
     *
     * @return The requested page results page size or {@code 0} if paged
     *         results are not required.
     * @see #getPagedResultsCookie()
     * @see #getPagedResultsOffset()
     * @since 1.4
     */
    public Integer getPageSize() {
        return (Integer) operationOptions.get(OP_PAGE_SIZE);
    };

    /**
     * Returns the sort keys which should be used for ordering the
     * {@link ConnectorObject}s returned by this search request.
     *
     * @return The sort keys which should be used for ordering the
     *         {@link ConnectorObject}s returned by this search request (never
     *         {@code null}).
     * @since 1.4
     */
    @SuppressWarnings("unchecked")
    public SortKey[] getSortKeys() {
        return (SortKey[]) operationOptions.get(OP_SORT_KEYS);
    };
}
