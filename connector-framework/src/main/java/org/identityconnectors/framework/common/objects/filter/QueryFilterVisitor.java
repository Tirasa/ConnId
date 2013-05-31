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

import java.util.List;

import org.identityconnectors.framework.common.objects.AttributeInfo;

/**
 * A visitor of {@code QueryFilter}s, in the style of the visitor design
 * pattern.
 * <p>
 * Classes implementing this interface can query filters in a type-safe manner.
 * When a visitor is passed to a filter's accept method, the corresponding visit
 * method most applicable to that filter is invoked.
 *
 * @param <R>
 *            The return type of this visitor's methods. Use
 *            {@link java.lang.Void} for visitors that do not need to return
 *            results.
 * @param <P>
 *            The type of the additional parameter to this visitor's methods.
 *            Use {@link java.lang.Void} for visitors that do not need an
 *            additional parameter.
 * @since 2.0
 */
public interface QueryFilterVisitor<R, P> {

    /**
     * Visits an {@code and} filter.
     * <p>
     * <b>Implementation note</b>: for the purposes of matching, an empty
     * sub-filter list should always evaluate to {@code true}.
     *
     * @param p
     *            A visitor specified parameter.
     * @param subFilters
     *            The unmodifiable list of sub-filters.
     * @return Returns a visitor specified result.
     */
    R visitAndFilter(P p, List<QueryFilter> subFilters);

    /**
     * Visits a boolean literal filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param value
     *            The boolean literal value.
     * @return Returns a visitor specified result.
     */
    R visitBooleanLiteralFilter(P p, boolean value);

    /**
     * Visits a {@code contains} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @param valueAssertion
     *            The value assertion.
     * @return Returns a visitor specified result.
     */
    R visitContainsFilter(P p, AttributeInfo field, Object valueAssertion);

    /**
     * Visits a {@code equality} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @param valueAssertion
     *            The value assertion.
     * @return Returns a visitor specified result.
     */
    R visitEqualsFilter(P p, AttributeInfo field, Object valueAssertion);

    /**
     * Visits a {@code comparison} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @param operator
     *            The operator to use for the comparison, which will not be one
     *            of the core operator names.
     * @param valueAssertion
     *            The value assertion.
     * @return Returns a visitor specified result.
     */
    R visitExtendedMatchFilter(P p, AttributeInfo field, String operator, Object valueAssertion);

    /**
     * Visits a {@code greater than} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @param valueAssertion
     *            The value assertion.
     * @return Returns a visitor specified result.
     */
    R visitGreaterThanFilter(P p, AttributeInfo field, Object valueAssertion);

    /**
     * Visits a {@code greater than or equal to} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @param valueAssertion
     *            The value assertion.
     * @return Returns a visitor specified result.
     */
    R visitGreaterThanOrEqualToFilter(P p, AttributeInfo field, Object valueAssertion);

    /**
     * Visits a {@code less than} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @param valueAssertion
     *            The value assertion.
     * @return Returns a visitor specified result.
     */
    R visitLessThanFilter(P p, AttributeInfo field, Object valueAssertion);

    /**
     * Visits a {@code less than or equal to} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @param valueAssertion
     *            The value assertion.
     * @return Returns a visitor specified result.
     */
    R visitLessThanOrEqualToFilter(P p, AttributeInfo field, Object valueAssertion);

    /**
     * Visits a {@code not} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param subFilter
     *            The sub-filter.
     * @return Returns a visitor specified result.
     */
    R visitNotFilter(P p, QueryFilter subFilter);

    /**
     * Visits an {@code or} filter.
     * <p>
     * <b>Implementation note</b>: for the purposes of matching, an empty
     * sub-filter list should always evaluate to {@code false}.
     *
     * @param p
     *            A visitor specified parameter.
     * @param subFilters
     *            The unmodifiable list of sub-filters.
     * @return Returns a visitor specified result.
     */
    R visitOrFilter(P p, List<QueryFilter> subFilters);

    /**
     * Visits a {@code present} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @return Returns a visitor specified result.
     */
    R visitPresentFilter(P p, AttributeInfo field);

    /**
     * Visits a {@code starts with} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param field
     *            A pointer to the field within JSON resource to be compared.
     * @param valueAssertion
     *            The value assertion.
     * @return Returns a visitor specified result.
     */
    R visitStartsWithFilter(P p, AttributeInfo field, Object valueAssertion);

}
