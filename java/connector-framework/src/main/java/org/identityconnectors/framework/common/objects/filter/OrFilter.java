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
 * Portions Copyrighted 2014 ForgeRock AS.
 */
package org.identityconnectors.framework.common.objects.filter;

import java.util.Collection;
import java.util.LinkedList;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;

public final class OrFilter extends CompositeFilter {

    /**
     * Left side of a composite based filter.
     */
    private LinkedList<Filter> subFilters;

    /**
     * Takes the result of the left and right filter and ORs them.
     */
    public OrFilter(Filter left, Filter right) {
        this(CollectionUtil.newList(left, right));
    }

    public OrFilter(Collection<Filter> filters) {
        super(null, null);
        subFilters = new LinkedList(filters);
    }

    /**
     * Takes the result from the left and ORs it w/ the right filter.
     *
     * @see Filter#accept(ConnectorObject)
     */
    @Override
    public boolean accept(final ConnectorObject obj) {
        boolean result = false;
        for (final Filter subFilter : subFilters) {
            result = subFilter.accept(obj);
            if (result) {
                break;
            }
        }
        return result;
    }

    public <R, P> R accept(FilterVisitor<R, P> v, P p) {
        return v.visitOrFilter(p, this);
    }

    @Override
    public Filter getLeft() {
        return subFilters.getFirst();
    }

    @Override
    public Filter getRight() {
        if (subFilters.size() > 2) {
            LinkedList<Filter> right = new LinkedList<Filter>(subFilters);
            right.removeFirst();
            return new AndFilter(right);
        } else if (subFilters.size() == 2 ){
            return subFilters.getLast();
        } else {
            return null;
        }
    }

    public Collection<Filter> getFilters() {
        return CollectionUtil.asReadOnlyList(subFilters);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append('(');
        boolean isFirst = true;
        for (final Filter subFilter : subFilters) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(" or ");
            }
            builder.append(subFilter);
        }
        return builder.append(')').toString();
    }
}
