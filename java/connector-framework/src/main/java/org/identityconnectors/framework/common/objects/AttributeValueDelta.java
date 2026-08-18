/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 ConnId. All rights reserved.
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

import org.identityconnectors.framework.common.objects.filter.Filter;

import java.util.List;

public abstract class AttributeValueDelta {

    public static class Add extends AttributeValueDelta {

        private final List<Object> values;

        public Add(List<Object> values) {
            this.values = List.copyOf(values);
        }

        public List<Object> getValues() {
            return values;
        }

        @Override
        public String toString() {
            return "Add: " + values;
        }
    }

    public static class FilterBased extends AttributeValueDelta {

        private final Filter filter;


        public FilterBased(Filter filter) {
            this.filter = filter;
        }

        public Filter getFilter() {
            return filter;
        }
    }

    public static class Delete extends FilterBased {

        public Delete(Filter filter) {
            super(filter);
        }

        @Override
        public String toString() {
            return "Delete: " + (getFilter() != null ? getFilter().toString() : "*");
        }
    }

    public static class Merge extends FilterBased {

        private final List<ComplexAttributeDelta> deltas;

        public Merge(Filter filter, List<ComplexAttributeDelta> deltas) {
            super(filter);
            if (filter == null) {
                throw new IllegalArgumentException("Filter cannot be null");
            }
            this.deltas = deltas != null ? List.copyOf(deltas) : List.of();
        }

        public List<ComplexAttributeDelta> getDeltas() {
            return deltas;
        }

        @Override
        public String toString() {
            return "Merge: " + getFilter() +
                    "deltas:" + deltas;
        }
    }
}
