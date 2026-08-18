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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ComplexValueDelta {

    public abstract void applyTo(List<Object> values);

    public static class Add extends ComplexValueDelta {

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

        @Override
        public void applyTo(List<Object> values) {
            values.addAll(this.values);
        }
    }

    public static abstract class FilterBased extends ComplexValueDelta {

        private final Filter filter;


        public FilterBased(Filter filter) {
            this.filter = filter;
        }

        public Filter getFilter() {
            return filter;
        }

        protected boolean filterMatches(Object value) {
            if (filter == null) {
                return true;
            }
            if (!(value instanceof BaseObject)) {
                return false;
            }
            return filter.accept((BaseObject) value);
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

        @Override
        public void applyTo(List<Object> values) {
            var iter = values.iterator();
            while (iter.hasNext()) {
                var current = iter.next();
                if (filterMatches(current)) {
                    iter.remove();
                }
            }
        }
    }

    public static class Merge extends FilterBased {

        private final Set<BaseAttributeDelta> deltas;

        public Merge(Filter filter, Set<BaseAttributeDelta> deltas) {
            super(filter);
            if (filter == null) {
                throw new IllegalArgumentException("Filter cannot be null");
            }
            this.deltas = deltas != null ? Set.copyOf(deltas) : Set.of();
        }

        public Set<BaseAttributeDelta> getDeltas() {
            return deltas;
        }

        @Override
        public String toString() {
            return "Merge: " + getFilter() +
                    "deltas:" + deltas;
        }

        @Override
        public void applyTo(List<Object> values) {
            values.replaceAll(this::applyToSingleObject);
        }

        private Object applyToSingleObject(Object value) {
            if (value instanceof BaseObject) {
                var object = (BaseObject) value;
                if (!filterMatches(object)) {
                    // Filter did not matched, we are reusing original object
                    return object;
                }

                var originalAttrs = object.getAttributes();
                var modified = AttributeDeltaUtil.applyDeltas(originalAttrs, deltas);
                return new EmbeddedObject(object.getObjectClass(), Set.copyOf(modified.values()));
            }
            return value;
        }
    }
}
