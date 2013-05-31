/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.identityconnectors.framework.common.objects.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;

/**
 * A filter which can be used to select which ConnectorObject resources should be included
 * in the results of a query request. A query string has the following string
 * representation:
 *
 * <pre>
 * Expr           = OrExpr
 * OrExpr         = AndExpr ( 'or' AndExpr ) *
 * AndExpr        = NotExpr ( 'and' NotExpr ) *
 * NotExpr        = '!' PrimaryExpr | PrimaryExpr
 * PrimaryExpr    = '(' Expr ')' | ComparisonExpr | PresenceExpr | LiteralExpr
 * ComparisonExpr = Pointer OpName Value
 * PresenceExpr   = Pointer 'pr'
 * LiteralExpr    = 'true' | 'false'
 * Pointer        = Pointer
 * OpName         = 'eq' |  # equal to
 *                  'co' |  # contains
 *                  'sw' |  # starts with
 *                  'lt' |  # less than
 *                  'le' |  # less than or equal to
 *                  'gt' |  # greater than
 *                  'ge' |  # greater than or equal to
 *                  STRING  # extended operator
 * Value      = NUMBER | BOOLEAN | '"' UTF8STRING '"'
 * STRING         = ASCII string not containing white-space
 * UTF8STRING     = UTF-8 string possibly containing white-space
 * </pre>
 *
 * Note that white space, parentheses, and exclamation characters need URL
 * encoding in HTTP query strings.
 *
 * @since 2.0
 */
public final class QueryFilter {

    private static final class AndImpl extends Impl {
        private final List<QueryFilter> subFilters;

        private AndImpl(final List<QueryFilter> subFilters) {
            this.subFilters = subFilters;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof AndImpl) {
                return subFilters.equals(((AndImpl) obj).subFilters);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return subFilters.hashCode();
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitAndFilter(p, subFilters);
        }

        @Override
        protected void toString(final StringBuilder builder) {
            builder.append('(');
            boolean isFirst = true;
            for (final QueryFilter subFilter : subFilters) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(" and ");
                }
                subFilter.pimpl.toString(builder);
            }
            builder.append(')');
        }
    }

    private static final class BooleanLiteralImpl extends Impl {
        private final boolean value;

        private BooleanLiteralImpl(final boolean value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof BooleanLiteralImpl) {
                return value == ((BooleanLiteralImpl) obj).value;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Boolean.valueOf(value).hashCode();
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitBooleanLiteralFilter(p, value);
        }

        @Override
        protected void toString(final StringBuilder builder) {
            builder.append(value);
        }
    }

    /*
     * TODO: should value assertions be Objects or Strings? Objects allows use
     * of numbers, during construction, but visitors may need to handle
     * different types (e.g. Date or String representation of a date).
     */

    private static abstract class ComparatorImpl extends Impl {
        protected final AttributeInfo field;
        protected final Object valueAssertion;

        protected ComparatorImpl(final AttributeInfo field, final Object valueAssertion) {
            this.field = field;
            this.valueAssertion = valueAssertion;
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof ComparatorImpl) {
                final ComparatorImpl o = (ComparatorImpl) obj;
                return field.equals(o.field) && getOperator().equals(o.getOperator())
                        && valueAssertion.equals(o.valueAssertion);
            } else {
                return false;
            }
        }

        @Override
        public final int hashCode() {
            return (field.hashCode() * 31 + getOperator().hashCode()) * 31
                    + valueAssertion.hashCode();
        }

        protected abstract String getOperator();

        @Override
        protected final void toString(final StringBuilder builder) {
            builder.append(field.toString());
            builder.append(' ');
            builder.append(getOperator());
            builder.append(' ');
            if (valueAssertion instanceof Boolean || valueAssertion instanceof Number) {
                // No need for quotes.
                builder.append(valueAssertion);
            } else {
                builder.append('"');
                builder.append(valueAssertion);
                builder.append('"');
            }
        }
    }

    private static final class ContainsImpl extends ComparatorImpl {
        private ContainsImpl(final AttributeInfo field, final Object valueAssertion) {
            super(field, valueAssertion);
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitContainsFilter(p, field, valueAssertion);
        }

        @Override
        protected String getOperator() {
            return "co";
        }
    }

    private static final class EqualsImpl extends ComparatorImpl {
        private EqualsImpl(final AttributeInfo field, final Object valueAssertion) {
            super(field, valueAssertion);
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitEqualsFilter(p, field, valueAssertion);
        }

        @Override
        protected String getOperator() {
            return "eq";
        }
    }

    private static final class ExtendedMatchImpl extends ComparatorImpl {
        private final String operator;

        private ExtendedMatchImpl(final AttributeInfo field, final String operator,
                final Object valueAssertion) {
            super(field, valueAssertion);
            this.operator = operator;
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitExtendedMatchFilter(p, field, operator, valueAssertion);
        }

        @Override
        protected String getOperator() {
            return operator;
        }

    }

    private static final class FilterTokenizer implements Iterator<String> {
        private static final int NEED_END_STRING = 2;
        private static final int NEED_START_STRING = 1;
        private static final int NEED_TOKEN = 0;

        private final String filterString;
        private String nextToken;
        private int pos;
        private int state;

        private FilterTokenizer(final String filterString) {
            this.filterString = filterString;
            this.pos = 0;
            this.state = NEED_TOKEN;
            readNextToken();
        }

        public boolean hasNext() {
            return nextToken != null;
        }

        public String next() {
            final String next = peek();
            readNextToken();
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return filterString;
        }

        private String peek() {
            if (nextToken == null) {
                throw new NoSuchElementException();
            }
            return nextToken;
        }

        private void readNextToken() {
            switch (state) {
            case NEED_START_STRING:
                final int stringStart = pos;
                for (; pos < filterString.length() && filterString.charAt(pos) != '"'; pos++) {
                    // Do nothing
                }
                nextToken = filterString.substring(stringStart, pos);
                state = NEED_END_STRING;
                break;
            case NEED_END_STRING:
                // NEED_START_STRING guarantees that we are either at the end of
                // the string
                // or the next character is a quote.
                if (pos < filterString.length()) {
                    nextToken = filterString.substring(pos, ++pos);
                } else {
                    nextToken = null;
                }
                state = NEED_TOKEN;
                break;
            default: // NEED_TOKEN:
                if (!skipWhiteSpace()) {
                    nextToken = null;
                } else {
                    final int tokenStart = pos;
                    switch (filterString.charAt(pos++)) {
                    case '(':
                    case ')':
                        break;
                    case '"':
                        state = NEED_START_STRING;
                        break;
                    default:
                        for (; pos < filterString.length(); pos++) {
                            final char c = filterString.charAt(pos);
                            if (c == '(' || c == ')' || c == ' ') {
                                break;
                            }
                        }
                        break;
                    }
                    nextToken = filterString.substring(tokenStart, pos);
                }
            }
        }

        private boolean skipWhiteSpace() {
            for (; pos < filterString.length() && filterString.charAt(pos) == ' '; pos++) {
                // Do nothing
            }
            return pos < filterString.length();
        }
    }

    private static final class GreaterThanImpl extends ComparatorImpl {
        private GreaterThanImpl(final AttributeInfo field, final Object valueAssertion) {
            super(field, valueAssertion);
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitGreaterThanFilter(p, field, valueAssertion);
        }

        @Override
        protected String getOperator() {
            return "gt";
        }
    }

    private static final class GreaterThanOrEqualToImpl extends ComparatorImpl {
        private GreaterThanOrEqualToImpl(final AttributeInfo field, final Object valueAssertion) {
            super(field, valueAssertion);
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitGreaterThanOrEqualToFilter(p, field, valueAssertion);
        }

        @Override
        protected String getOperator() {
            return "ge";
        }
    }

    private static abstract class Impl {
        protected Impl() {
            // Nothing to do.
        }

        @Override
        public abstract boolean equals(Object obj);

        @Override
        public abstract int hashCode();

        protected abstract <R, P> R accept(QueryFilterVisitor<R, P> v, P p);

        protected abstract void toString(StringBuilder builder);
    }

    private static final class LessThanImpl extends ComparatorImpl {
        private LessThanImpl(final AttributeInfo field, final Object valueAssertion) {
            super(field, valueAssertion);
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitLessThanFilter(p, field, valueAssertion);
        }

        @Override
        protected String getOperator() {
            return "lt";
        }
    }

    private static final class LessThanOrEqualToImpl extends ComparatorImpl {
        private LessThanOrEqualToImpl(final AttributeInfo field, final Object valueAssertion) {
            super(field, valueAssertion);
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitLessThanOrEqualToFilter(p, field, valueAssertion);
        }

        @Override
        protected String getOperator() {
            return "le";
        }
    }

    private static final class NotImpl extends Impl {
        private final QueryFilter subFilter;

        private NotImpl(final QueryFilter subFilter) {
            this.subFilter = subFilter;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof NotImpl) {
                return subFilter.equals(((NotImpl) obj).subFilter);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return subFilter.hashCode();
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitNotFilter(p, subFilter);
        }

        @Override
        protected void toString(final StringBuilder builder) {
            // This is not officially supported in SCIM.
            builder.append("! (");
            subFilter.pimpl.toString(builder);
            builder.append(')');
        }
    }

    private static final class OrImpl extends Impl {
        private final List<QueryFilter> subFilters;

        private OrImpl(final List<QueryFilter> subFilters) {
            this.subFilters = subFilters;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof OrImpl) {
                return subFilters.equals(((OrImpl) obj).subFilters);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return subFilters.hashCode();
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitOrFilter(p, subFilters);
        }

        @Override
        protected void toString(final StringBuilder builder) {
            builder.append('(');
            boolean isFirst = true;
            for (final QueryFilter subFilter : subFilters) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(" or ");
                }
                subFilter.pimpl.toString(builder);
            }
            builder.append(')');
        }
    }

    private static final class PresentImpl extends Impl {
        private final AttributeInfo field;

        private PresentImpl(final AttributeInfo field) {
            this.field = field;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof PresentImpl) {
                final PresentImpl o = (PresentImpl) obj;
                return field.equals(o.field);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return field.hashCode();
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitPresentFilter(p, field);
        }

        @Override
        protected void toString(final StringBuilder builder) {
            builder.append(field.toString());
            builder.append(' ');
            builder.append("pr");
        }
    }

    private static final class StartsWithImpl extends ComparatorImpl {
        private StartsWithImpl(final AttributeInfo field, final Object valueAssertion) {
            super(field, valueAssertion);
        }

        @Override
        protected <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
            return v.visitStartsWithFilter(p, field, valueAssertion);
        }

        @Override
        protected String getOperator() {
            return "sw";
        }
    }

    private static final QueryFilter ALWAYS_FALSE = new QueryFilter(new BooleanLiteralImpl(false));
    private static final QueryFilter ALWAYS_TRUE = new QueryFilter(new BooleanLiteralImpl(true));

    // Maximum permitted query filter nesting depth.
    private static final int VALUE_OF_MAX_DEPTH = 256;

    /**
     * Returns a filter which does not match any resources.
     *
     * @return A filter which does not match any resources.
     */
    public static QueryFilter alwaysFalse() {
        return ALWAYS_FALSE;
    }

    /**
     * Returns a filter which matches all resources.
     *
     * @return A filter which matches all resources.
     */
    public static QueryFilter alwaysTrue() {
        return ALWAYS_TRUE;
    }

    /**
     * Creates a new {@code and} filter using the provided list of sub-filters.
     * <p>
     * Creating a new {@code and} filter with a {@code null} or empty list of
     * sub-filters is equivalent to calling {@link #alwaysTrue()}.
     *
     * @param subFilters
     *            The list of sub-filters, may be empty or {@code null}.
     * @return The newly created {@code and} filter.
     */
    public static QueryFilter and(final Collection<QueryFilter> subFilters) {
        switch (subFilters.size()) {
        case 0:
            return alwaysTrue();
        case 1:
            return subFilters.iterator().next();
        default:
            return new QueryFilter(new AndImpl(Collections
                    .unmodifiableList(new ArrayList<QueryFilter>(subFilters))));
        }
    }

    /**
     * Creates a new {@code and} filter using the provided list of sub-filters.
     * <p>
     * Creating a new {@code and} filter with a {@code null} or empty list of
     * sub-filters is equivalent to calling {@link #alwaysTrue()}.
     *
     * @param subFilters
     *            The list of sub-filters, may be empty or {@code null}.
     * @return The newly created {@code and} filter.
     */
    public static QueryFilter and(final QueryFilter... subFilters) {
        return and(Arrays.asList(subFilters));
    }

    /**
     * Creates a new generic comparison filter using the provided field name,
     * operator, and value assertion. When the provided operator name represents
     * a core operator, e.g. "eq", then this method is equivalent to calling the
     * equivalent constructor, e.g. {@link #equalTo(AttributeInfo, Object)}.
     * Otherwise, when the operator name does not correspond to a core operator,
     * an extended comparison filter will be returned.
     *
     * @param field
     *            The name of field within the ConnectorObject to be compared.
     * @param operator
     *            The operator to use for the comparison, which must be one of
     *            the core operator names, or a string matching the regular
     *            expression {@code [a-zA-Z_0-9.]+}.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created generic comparison filter.
     * @throws IllegalArgumentException
     *             If {@code operator} is not a valid operator name.
     */
    public static QueryFilter comparisonFilter(final AttributeInfo field, final String operator,
            final Object valueAssertion) {
        if (operator.equalsIgnoreCase("eq")) {
            return QueryFilter.equalTo(field, valueAssertion);
        } else if (operator.equalsIgnoreCase("gt")) {
            return QueryFilter.greaterThan(field, valueAssertion);
        } else if (operator.equalsIgnoreCase("ge")) {
            return QueryFilter.greaterThanOrEqualTo(field, valueAssertion);
        } else if (operator.equalsIgnoreCase("lt")) {
            return QueryFilter.lessThan(field, valueAssertion);
        } else if (operator.equalsIgnoreCase("le")) {
            return QueryFilter.lessThanOrEqualTo(field, valueAssertion);
        } else if (operator.equalsIgnoreCase("co")) {
            return QueryFilter.contains(field, valueAssertion);
        } else if (operator.equalsIgnoreCase("sw")) {
            return QueryFilter.startsWith(field, valueAssertion);
        } else if (operator.matches("[a-zA-Z_0-9.]+")) {
            return new QueryFilter(new ExtendedMatchImpl(field, operator, valueAssertion));
        } else {
            throw new IllegalArgumentException("\"" + operator
                    + "\" is not a valid filter operator");
        }
    }

    /**
     * Creates a new generic comparison filter using the provided field name,
     * operator, and value assertion. When the provided operator name represents
     * a core operator, e.g. "eq", then this method is equivalent to calling the
     * equivalent constructor, e.g. {@link #equalTo(String, Object)}. Otherwise,
     * when the operator name does not correspond to a core operator, an
     * extended comparison filter will be returned.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param operator
     *            The operator to use for the comparison, which must be one of
     *            the core operator names, or a string matching the regular
     *            expression {@code [a-zA-Z_0-9.]+}.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created generic comparison filter.
     * @throws IllegalArgumentException
     *             If {@code operator} is not a valid operator name.
     */
    public static QueryFilter comparisonFilter(final String field, final String operator,
            final Object valueAssertion) {
        return comparisonFilter(AttributeInfoBuilder.build(field), operator, valueAssertion);
    }

    /**
     * Creates a new {@code contains} filter using the provided field name and
     * value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code contains} filter.
     */
    public static QueryFilter contains(final AttributeInfo field, final Object valueAssertion) {
        return new QueryFilter(new ContainsImpl(field, valueAssertion));
    }

    /**
     * Creates a new {@code contains} filter using the provided field name and
     * value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code contains} filter.
     */
    public static QueryFilter contains(final String field, final Object valueAssertion) {
        return contains(AttributeInfoBuilder.build(field), valueAssertion);
    }

    /**
     * Creates a new {@code equality} filter using the provided field name and
     * value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code equality} filter.
     */
    public static QueryFilter equalTo(final AttributeInfo field, final Object valueAssertion) {
        return new QueryFilter(new EqualsImpl(field, valueAssertion));
    }

    /**
     * Creates a new {@code equality} filter using the provided field name and
     * value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code equality} filter.
     */
    public static QueryFilter equalTo(final String field, final Object valueAssertion) {
        return equalTo(AttributeInfoBuilder.build(field), valueAssertion);
    }

    /**
     * Creates a new {@code greater than} filter using the provided field name
     * and value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code greater than} filter.
     */
    public static QueryFilter greaterThan(final AttributeInfo field, final Object valueAssertion) {
        return new QueryFilter(new GreaterThanImpl(field, valueAssertion));
    }

    /**
     * Creates a new {@code greater than} filter using the provided field name
     * and value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code greater than} filter.
     */
    public static QueryFilter greaterThan(final String field, final Object valueAssertion) {
        return greaterThan(AttributeInfoBuilder.build(field), valueAssertion);
    }

    /**
     * Creates a new {@code greater than or equal to} filter using the provided
     * field name and value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code greater than or equal to} filter.
     */
    public static QueryFilter greaterThanOrEqualTo(final AttributeInfo field,
            final Object valueAssertion) {
        return new QueryFilter(new GreaterThanOrEqualToImpl(field, valueAssertion));
    }

    /**
     * Creates a new {@code greater than or equal to} filter using the provided
     * field name and value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code greater than or equal to} filter.
     */
    public static QueryFilter greaterThanOrEqualTo(final String field, final Object valueAssertion) {
        return greaterThanOrEqualTo(AttributeInfoBuilder.build(field), valueAssertion);
    }

    /**
     * Creates a new {@code less than} filter using the provided field name and
     * value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code less than} filter.
     */
    public static QueryFilter lessThan(final AttributeInfo field, final Object valueAssertion) {
        return new QueryFilter(new LessThanImpl(field, valueAssertion));
    }

    /**
     * Creates a new {@code less than} filter using the provided field name and
     * value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code less than} filter.
     */
    public static QueryFilter lessThan(final String field, final Object valueAssertion) {
        return lessThan(AttributeInfoBuilder.build(field), valueAssertion);
    }

    /**
     * Creates a new {@code less than or equal to} filter using the provided
     * field name and value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code less than or equal to} filter.
     */
    public static QueryFilter lessThanOrEqualTo(final AttributeInfo field,
            final Object valueAssertion) {
        return new QueryFilter(new LessThanOrEqualToImpl(field, valueAssertion));
    }

    /**
     * Creates a new {@code less than or equal to} filter using the provided
     * field name and value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code less than or equal to} filter.
     */
    public static QueryFilter lessThanOrEqualTo(final String field, final Object valueAssertion) {
        return lessThanOrEqualTo(AttributeInfoBuilder.build(field), valueAssertion);
    }

    /**
     * Creates a new {@code not} filter using the provided sub-filter.
     *
     * @param subFilter
     *            The sub-filter.
     * @return The newly created {@code not} filter.
     */
    public static QueryFilter not(final QueryFilter subFilter) {
        return new QueryFilter(new NotImpl(subFilter));
    }

    /**
     * Creates a new {@code or} filter using the provided list of sub-filters.
     * <p>
     * Creating a new {@code or} filter with a {@code null} or empty list of
     * sub-filters is equivalent to calling {@link #alwaysFalse()}.
     *
     * @param subFilters
     *            The list of sub-filters, may be empty or {@code null}.
     * @return The newly created {@code or} filter.
     */
    public static QueryFilter or(final Collection<QueryFilter> subFilters) {
        switch (subFilters.size()) {
        case 0:
            return alwaysFalse();
        case 1:
            return subFilters.iterator().next();
        default:
            return new QueryFilter(new OrImpl(Collections
                    .unmodifiableList(new ArrayList<QueryFilter>(subFilters))));
        }
    }

    /**
     * Creates a new {@code or} filter using the provided list of sub-filters.
     * <p>
     * Creating a new {@code or} filter with a {@code null} or empty list of
     * sub-filters is equivalent to calling {@link #alwaysFalse()}.
     *
     * @param subFilters
     *            The list of sub-filters, may be empty or {@code null}.
     * @return The newly created {@code or} filter.
     */
    public static QueryFilter or(final QueryFilter... subFilters) {
        return or(Arrays.asList(subFilters));
    }

    /**
     * Creates a new {@code presence} filter using the provided field name.
     *
     * @param field
     *            The name of field within the JSON resource which must be
     *            present.
     * @return The newly created {@code presence} filter.
     */
    public static QueryFilter present(final AttributeInfo field) {
        return new QueryFilter(new PresentImpl(field));
    }

    /**
     * Creates a new {@code presence} filter using the provided field name.
     *
     * @param field
     *            The name of field within the JSON resource which must be
     *            present.
     * @return The newly created {@code presence} filter.
     */
    public static QueryFilter present(final String field) {
        return present(AttributeInfoBuilder.build(field));
    }

    /**
     * Creates a new {@code starts with} filter using the provided field name
     * and value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code starts with} filter.
     */
    public static QueryFilter startsWith(final AttributeInfo field, final Object valueAssertion) {
        return new QueryFilter(new StartsWithImpl(field, valueAssertion));
    }

    /**
     * Creates a new {@code starts with} filter using the provided field name
     * and value assertion.
     *
     * @param field
     *            The name of field within the JSON resource to be compared.
     * @param valueAssertion
     *            The assertion value.
     * @return The newly created {@code starts with} filter.
     */
    public static QueryFilter startsWith(final String field, final Object valueAssertion) {
        return startsWith(AttributeInfoBuilder.build(field), valueAssertion);
    }

    /**
     * Parses the provided string representation of a query filter as a
     * {@code QueryFilter}.
     *
     * @param string
     *            The string representation of a query filter .
     * @return The parsed {@code QueryFilter}.
     * @throws IllegalArgumentException
     *             If {@code string} is not a valid string representation of a
     *             query filter.
     */
    public static QueryFilter valueOf(final String string) {
        // Use recursive descent of grammar described in class Javadoc.
        final FilterTokenizer tokenizer = new FilterTokenizer(string);
        final QueryFilter filter = valueOfOrExpr(tokenizer, 0);
        if (tokenizer.hasNext()) {
            return valueOfIllegalArgument(tokenizer);
        } else {
            return filter;
        }
    }

    private static void checkDepth(final FilterTokenizer tokenizer, final int depth) {
        if (depth > VALUE_OF_MAX_DEPTH) {
            throw new IllegalArgumentException("The query filter '" + tokenizer
                    + "' cannot be parsed because it contains more than " + VALUE_OF_MAX_DEPTH
                    + " nexted expressions");
        }
    }

    private static QueryFilter valueOfAndExpr(final FilterTokenizer tokenizer, final int depth) {
        checkDepth(tokenizer, depth);
        QueryFilter filter = valueOfNotExpr(tokenizer, depth + 1);
        List<QueryFilter> subFilters = null;
        while (tokenizer.hasNext() && tokenizer.peek().equalsIgnoreCase("and")) {
            tokenizer.next();
            if (subFilters == null) {
                subFilters = new LinkedList<QueryFilter>();
                subFilters.add(filter);
            }
            subFilters.add(valueOfNotExpr(tokenizer, depth + 1));
        }
        if (subFilters != null) {
            filter = QueryFilter.and(subFilters);
        }
        return filter;
    }

    private static QueryFilter valueOfIllegalArgument(final FilterTokenizer tokenizer) {
        throw new IllegalArgumentException("Invalid query filter '" + tokenizer + "'");
    }

    private static QueryFilter valueOfNotExpr(final FilterTokenizer tokenizer, final int depth) {
        checkDepth(tokenizer, depth);
        if (tokenizer.hasNext() && tokenizer.peek().equalsIgnoreCase("!")) {
            tokenizer.next();
            final QueryFilter rhs = valueOfPrimaryExpr(tokenizer, depth + 1);
            return QueryFilter.not(rhs);
        } else {
            return valueOfPrimaryExpr(tokenizer, depth + 1);
        }
    }

    private static QueryFilter valueOfOrExpr(final FilterTokenizer tokenizer, final int depth) {
        checkDepth(tokenizer, depth);
        QueryFilter filter = valueOfAndExpr(tokenizer, depth + 1);
        List<QueryFilter> subFilters = null;
        while (tokenizer.hasNext() && tokenizer.peek().equalsIgnoreCase("or")) {
            tokenizer.next();
            if (subFilters == null) {
                subFilters = new LinkedList<QueryFilter>();
                subFilters.add(filter);
            }
            subFilters.add(valueOfAndExpr(tokenizer, depth + 1));
        }
        if (subFilters != null) {
            filter = QueryFilter.or(subFilters);
        }
        return filter;
    }

    private static QueryFilter valueOfPrimaryExpr(final FilterTokenizer tokenizer, final int depth) {
        checkDepth(tokenizer, depth);
        if (!tokenizer.hasNext()) {
            return valueOfIllegalArgument(tokenizer);
        }
        String nextToken = tokenizer.next();
        if (nextToken.equals("(")) {
            // Nested expression.
            final QueryFilter filter = valueOfOrExpr(tokenizer, depth + 1);
            if (!tokenizer.hasNext() || !tokenizer.next().equals(")")) {
                return valueOfIllegalArgument(tokenizer);
            }
            return filter;
        } else if (nextToken.equalsIgnoreCase("true")) {
            return alwaysTrue();
        } else if (nextToken.equalsIgnoreCase("false")) {
            return alwaysFalse();
        } else if (nextToken.equals("\"")) {
            return valueOfIllegalArgument(tokenizer);
        } else {
            // Assertion.
            final AttributeInfo pointer = AttributeInfoBuilder.build(nextToken);
            if (!tokenizer.hasNext()) {
                return valueOfIllegalArgument(tokenizer);
            }
            final String operator = tokenizer.next();
            if (operator.equalsIgnoreCase("pr")) {
                return QueryFilter.present(pointer);
            } else {
                // Read assertion value: NUMBER | BOOLEAN | '"' UTF8STRING '"'
                if (!tokenizer.hasNext()) {
                    return valueOfIllegalArgument(tokenizer);
                }
                final Object assertionValue;
                nextToken = tokenizer.next();
                if (nextToken.equals("\"")) {
                    // UTFSTRING
                    if (!tokenizer.hasNext()) {
                        return valueOfIllegalArgument(tokenizer);
                    }
                    assertionValue = tokenizer.next();
                    if (!tokenizer.hasNext() || !tokenizer.next().equals("\"")) {
                        return valueOfIllegalArgument(tokenizer);
                    }
                } else if (nextToken.equalsIgnoreCase("true")
                        || nextToken.equalsIgnoreCase("false")) {
                    assertionValue = Boolean.parseBoolean(nextToken);
                } else if (nextToken.indexOf('.') >= 0) {
                    // Floating point number.
                    assertionValue = Double.parseDouble(nextToken);
                } else {
                    // Must be an integer.
                    assertionValue = Long.parseLong(nextToken);
                }
                try {
                    return QueryFilter.comparisonFilter(pointer, operator, assertionValue);
                } catch (final IllegalArgumentException e) {
                    return valueOfIllegalArgument(tokenizer);
                }
            }
        }
    }

    private final Impl pimpl;

    private QueryFilter(final Impl pimpl) {
        this.pimpl = pimpl;
    }

    /**
     * Applies a {@code QueryFilterVisitor} to this {@code QueryFilter}.
     *
     * @param <R>
     *            The return type of the visitor's methods.
     * @param <P>
     *            The type of the additional parameters to the visitor's
     *            methods.
     * @param v
     *            The filter visitor.
     * @param p
     *            Optional additional visitor parameter.
     * @return A result as specified by the visitor.
     */
    public <R, P> R accept(final QueryFilterVisitor<R, P> v, final P p) {
        return pimpl.accept(v, p);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof QueryFilter) {
            return pimpl.equals(((QueryFilter) obj).pimpl);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return pimpl.hashCode();
    }

    /**
     * Returns the string representation of this query filter. The string
     * representation is defined to be similar to that of SCIM's, with the
     * following differences:
     * <ul>
     * <li>field references are JSON pointers
     * <li>support for boolean literal expressions, e.g. {@code (true)}
     * <li>support for the logical not operator, e.g.
     * {@code (! /role eq "user")}
     * </ul>
     *
     * @return The string representation of this query filter.
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        pimpl.toString(builder);
        return builder.toString();
    }

}
