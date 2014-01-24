/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS. All rights reserved.
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
 */

package org.identityconnectors.testconnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.PreconditionFailedException;
import org.identityconnectors.framework.common.exceptions.PreconditionRequiredException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.AttributesAccessor;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.SortKey;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.framework.spi.SyncTokenResultsHandler;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;

public abstract class TstAbstractConnector implements CreateOp, SearchOp<Filter>, SyncOp, DeleteOp {

    private static final class ResourceComparator implements Comparator<ConnectorObject> {
        private final List<SortKey> sortKeys;

        private ResourceComparator(final SortKey... sortKeys) {
            this.sortKeys = Arrays.asList(sortKeys);
        }

        @Override
        public int compare(final ConnectorObject r1, final ConnectorObject r2) {
            for (final SortKey sortKey : sortKeys) {
                final int result = compare(r1, r2, sortKey);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        private int compare(final ConnectorObject r1, final ConnectorObject r2,
                final SortKey sortKey) {
            final List<Object> vs1 = getValuesSorted(r1, sortKey.getField());
            final List<Object> vs2 = getValuesSorted(r2, sortKey.getField());
            if (vs1.isEmpty() && vs2.isEmpty()) {
                return 0;
            } else if (vs1.isEmpty()) {
                // Sort resources with missing attributes last.
                return 1;
            } else if (vs2.isEmpty()) {
                // Sort resources with missing attributes last.
                return -1;
            } else {
                final Object v1 = vs1.get(0);
                final Object v2 = vs2.get(0);
                return sortKey.isAscendingOrder() ? compareValues(v1, v2) : -compareValues(v1, v2);
            }
        }

        private List<Object> getValuesSorted(final ConnectorObject resource, final String field) {
            final Attribute value = AttributeUtil.find(field, resource.getAttributes());
            if (value == null || value.getValue() == null || value.getValue().isEmpty()) {
                return Collections.emptyList();
            } else if (value.getValue().size() > 1) {
                List<Object> results = new ArrayList<Object>(value.getValue());
                Collections.sort(results, VALUE_COMPARATOR);
                return results;
            } else {
                return value.getValue();
            }
        }
    }

    private static final Comparator<Object> VALUE_COMPARATOR = new Comparator<Object>() {
        @Override
        public int compare(final Object o1, final Object o2) {
            return compareValues(o1, o2);
        }
    };

    private static int compareValues(final Object v1, final Object v2) {
        if (v1 instanceof String && v2 instanceof String) {
            final String s1 = (String) v1;
            final String s2 = (String) v2;
            return s1.compareToIgnoreCase(s2);
        } else if (v1 instanceof Number && v2 instanceof Number) {
            final Double n1 = ((Number) v1).doubleValue();
            final Double n2 = ((Number) v2).doubleValue();
            return n1.compareTo(n2);
        } else if (v1 instanceof Boolean && v2 instanceof Boolean) {
            final Boolean b1 = (Boolean) v1;
            final Boolean b2 = (Boolean) v2;
            return b1.compareTo(b2);
        } else {
            return v1.getClass().getName().compareTo(v2.getClass().getName());
        }
    }

    protected TstStatefulConnectorConfig config;

    public void init(Configuration cfg) {
        config = (TstStatefulConnectorConfig) cfg;
        config.getGuid();
    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes,
            OperationOptions options) {
        AttributesAccessor accessor = new AttributesAccessor(createAttributes);
        if (accessor.hasAttribute("fail")) {
            throw new ConnectorException("Test Exception");
        } else if (accessor.hasAttribute("exist") && accessor.findBoolean("exist")) {
            throw new AlreadyExistsException(accessor.getName().getNameValue());
        }
        return new Uid(config.getGuid().toString());
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        if (null == uid.getRevision()) {
            throw new PreconditionRequiredException("Version is required for MVCC");
        } else if (config.getGuid().toString().equals(uid.getRevision())) {
            // Delete
        } else {
            throw new PreconditionFailedException(
                    "Current version of resource is 0 and not match with: " + uid.getRevision());
        }
    }

    @Override
    public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass,
            OperationOptions options) {
        return new FilterTranslator<Filter>() {
            @Override
            public List<Filter> translate(Filter filter) {
                return Collections.singletonList(filter);
            }
        };
    }

    @Override
    public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler,
            OperationOptions options) {

        SortKey[] sortKeys = options.getSortKeys();
        if (null == sortKeys) {
            sortKeys = new SortKey[] { new SortKey(Name.NAME, true) };
        }

        // Rebuild the full result set.
        TreeSet<ConnectorObject> resultSet =
                new TreeSet<ConnectorObject>(new ResourceComparator(sortKeys));

        if (null != query) {
            for (ConnectorObject co : collection.values()) {
                if (query.accept(co)) {
                    resultSet.add(co);
                }
            }
        } else {
            resultSet.addAll(collection.values());
        }

        // Handle the results
        if (null != options.getPageSize()) {
            // Paged Search
            final String pagedResultsCookie = options.getPagedResultsCookie();
            String currentPagedResultsCookie = options.getPagedResultsCookie();
            final Integer pagedResultsOffset =
                    null != options.getPagedResultsOffset() ? Math.max(0, options
                            .getPagedResultsOffset()) : 0;
            final Integer pageSize = options.getPageSize();

            int index = 0;
            int pageStartIndex = null == pagedResultsCookie ? 0 : -1;
            int handled = 0;

            for (ConnectorObject entry : resultSet) {
                if (pageStartIndex < 0 && pagedResultsCookie.equals(entry.getName().getNameValue())) {
                    pageStartIndex = index + 1;
                }

                if (pageStartIndex < 0 || index < pageStartIndex) {
                    index++;
                    continue;
                }

                if (handled >= pageSize) {
                    break;
                }

                if (index >= pagedResultsOffset + pageStartIndex) {
                    if (handler.handle(entry)) {
                        handled++;
                        currentPagedResultsCookie = entry.getName().getNameValue();
                    } else {
                        break;
                    }
                }
                index++;
            }

            if (index == resultSet.size()) {
                currentPagedResultsCookie = null;
            }

            if (handler instanceof SearchResultsHandler) {
                ((SearchResultsHandler) handler).handleResult(new SearchResult(
                        currentPagedResultsCookie, resultSet.size() - index));
            }
        } else {
            // Normal Search
            for (ConnectorObject entry : resultSet) {
                if (!handler.handle(entry)) {
                    break;
                }
            }
            if (handler instanceof SearchResultsHandler) {
                ((SearchResultsHandler) handler).handleResult(new SearchResult());
            }
        }

    }

    @Override
    public void sync(ObjectClass objectClass, SyncToken token, SyncResultsHandler handler,
            OperationOptions options) {
        if (handler instanceof SyncTokenResultsHandler) {
            ((SyncTokenResultsHandler) handler).handleResult(getLatestSyncToken(objectClass));
        }
    }

    @Override
    public SyncToken getLatestSyncToken(ObjectClass objectClass) {
        return new SyncToken(config.getGuid().toString());
    }

    private final static SortedMap<String, ConnectorObject> collection =
            new TreeMap<String, ConnectorObject>(String.CASE_INSENSITIVE_ORDER);
    static {
        boolean enabled = true;
        for (int i = 0; i < 100; i++) {
            ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
            builder.setUid(String.valueOf(i));
            builder.setName(String.format("user%03d", i));
            builder.addAttribute(AttributeBuilder.buildEnabled(enabled));
            ConnectorObject co = builder.build();
            collection.put(co.getName().getNameValue(), co);
            enabled = !enabled;
        }
    }
}
