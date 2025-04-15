/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2022 Evolveum. All rights reserved.
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
package org.identityconnectors.framework.common.objects;

import java.util.*;
import org.identityconnectors.common.CollectionUtil;

/**
 * List of suggested values, with openness specification.
 * Can be used to list suggested or discovered values for configuration properties.
 *
 * @author Radovan Semancik
 * @since 1.5.2.0
 */
public class SuggestedValues {

    /**
     * List of suggested values.
     * Values are ordered in the list in the same way as they should be presented.
     * E.g. a most popular or probable value should be the first.
     */
    private final List<Object> values;

    /**
     * Openness of value list. Closed lists (the default) can accept only specified values.
     * Open lists can accept any value.
     * @see ValueListOpenness
     */
    private final ValueListOpenness openness;

    SuggestedValues(List<Object> values) {
        this(values, ValueListOpenness.CLOSED);
    }

    SuggestedValues(List<Object> values, ValueListOpenness openness) {
        if (values == null) {
            throw new IllegalArgumentException("List of suggested values cannot be null, use empty list to indicate no values");
        }
        // copy to prevent corruption..
        this.values =  CollectionUtil.newReadOnlyList(values);
        this.openness = openness;
    }

    /**
     * Returns list of suggested values.
     * Values are ordered in the list in the same way as they should be presented.
     * E.g. a most popular or probable value should be the first.
     */
    public List<Object> getValues() {
        return values;
    }

    /**
     * Returns openness of value list. Closed lists (the default) can accept only specified values.
     * Open lists can accept any value.
     * @see ValueListOpenness
     */
    public ValueListOpenness getOpenness() {
        return openness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuggestedValues that = (SuggestedValues) o;
        return values.equals(that.values) && openness == that.openness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, openness);
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("Suggested values: ");
        bld.append(values);
        bld.append(" (").append(openness).append(")");
        return bld.toString();
    }

}
