/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 ForgeRock AS. All rights reserved.
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
 * Portions Copyrighted 2018 ConnId.
 */

package org.identityconnectors.framework.common.objects.filter;

/**
 * A visitor of {@code Filter}s, in the style of the visitor design pattern.
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
 * @since 1.4
 */
public interface FilterVisitor<R, P> {

    /**
     * Visits an {@code and} filter.
     * <p>
     * <b>Implementation note</b>: for the purposes of matching, an empty
     * sub-filters should always evaluate to {@code true}.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitAndFilter(P p, AndFilter filter);

    /**
     * Visits a {@code contains} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitContainsFilter(P p, ContainsFilter filter);

    /**
     * Visits a {@code containsAll} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitContainsAllValuesFilter(P p, ContainsAllValuesFilter filter);

    /**
     * Visits a {@code equality} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitEqualsFilter(P p, EqualsFilter filter);

    /**
     * Visits a {@code comparison} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitExtendedFilter(P p, Filter filter);

    /**
     * Visits a {@code greater than} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitGreaterThanFilter(P p, GreaterThanFilter filter);

    /**
     * Visits a {@code greater than or equal to} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitGreaterThanOrEqualFilter(P p, GreaterThanOrEqualFilter filter);

    /**
     * Visits a {@code less than} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitLessThanFilter(P p, LessThanFilter filter);

    /**
     * Visits a {@code less than or equal to} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitLessThanOrEqualFilter(P p, LessThanOrEqualFilter filter);

    /**
     * Visits a {@code not} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitNotFilter(P p, NotFilter filter);

    /**
     * Visits an {@code or} filter.
     * <p>
     * <b>Implementation note</b>: for the purposes of matching, an empty
     * sub-filters should always evaluate to {@code false}.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitOrFilter(P p, OrFilter filter);

    /**
     * Visits a {@code starts with} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitStartsWithFilter(P p, StartsWithFilter filter);

    /**
     * Visits a {@code ends with} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitEndsWithFilter(P p, EndsWithFilter filter);
    
    /**
     * Visits a {@code equals ignoreCase} filter.
     *
     * @param p
     *            A visitor specified parameter.
     * @param filter
     *            The visited filter.
     * @return Returns a visitor specified result.
     */
    R visitEqualsIgnoreCaseFilter(P p, EqualsIgnoreCaseFilter filter);

}
