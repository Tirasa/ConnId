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

import java.util.List;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;

public class ContainsAllValuesFilter extends AttributeFilter {

    private final String name;
    private final List<Object> values;

    /**
     * Public only as an artifact of the implementation. Please use
     * {@link FilterBuilder} to create an instance of
     * {@code ContainsAllValuesFilter}.
     *
     * @param attr
     */
    public ContainsAllValuesFilter(Attribute attr) {
        super(attr);
        name = attr.getName();
        values = attr.getValue();
    }

    /**
     * Determine whether the specified {@link ConnectorObject} contains an
     * {@link Attribute} that has the same name as
     * <em>and contains all of the values of</em> the attribute that
     * {@link FilterBuilder} placed into this filter.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean accept(ConnectorObject obj) {
        Attribute found = obj.getAttributeByName(name);
        if (found != null) {
            // TODO: possible optimization using 'Set'
        	List<Object> value = found.getValue();
        	if (value == null) {
        		throw new IllegalStateException("Null value found in attribute "+name+" of connector object "+obj);
        	}
            return value.containsAll(values);
        }
        return false;
    }

    public <R, P> R accept(FilterVisitor<R, P> v, P p) {
        return v.visitContainsAllValuesFilter(p, this);
    }
    
    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("CONTAINS_ALL_VALUES: ").append(getAttribute());
        return bld.toString();
    }
}
