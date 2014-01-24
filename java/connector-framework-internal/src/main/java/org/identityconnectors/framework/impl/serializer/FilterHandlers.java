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
 */
package org.identityconnectors.framework.impl.serializer;

import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.filter.AndFilter;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.CompositeFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.NotFilter;
import org.identityconnectors.framework.common.objects.filter.OrFilter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;

/**
 * Serialization handles for APIConfiguration and dependencies.
 */
class FilterHandlers {

    public static final List<ObjectTypeMapper> HANDLERS = new ArrayList<ObjectTypeMapper>();

    private static abstract class CompositeFilterHandler<T extends CompositeFilter> extends
            AbstractObjectSerializationHandler {

        protected CompositeFilterHandler(final Class<T> clazz, final String typeName) {
            super(clazz, typeName);
        }

        public final Object deserialize(final ObjectDecoder decoder) {
            final Filter left = (Filter) decoder.readObjectContents(0);
            final Filter right = (Filter) decoder.readObjectContents(1);
            return createFilter(left, right);
        }

        public final void serialize(final Object object, final ObjectEncoder encoder) {
            final CompositeFilter val = (CompositeFilter) object;
            encoder.writeObjectContents(val.getLeft());
            encoder.writeObjectContents(val.getRight());
        }

        protected abstract T createFilter(Filter left, Filter right);
    }

    private static abstract class AttributeFilterHandler<T extends AttributeFilter> extends
            AbstractObjectSerializationHandler {

        protected AttributeFilterHandler(final Class<T> clazz, final String typeName) {
            super(clazz, typeName);
        }

        public final Object deserialize(final ObjectDecoder decoder) {
            final Attribute attribute =
                    (Attribute) decoder.readObjectField("attribute", null, null);
            return createFilter(attribute);
        }

        public final void serialize(final Object object, final ObjectEncoder encoder) {
            final AttributeFilter val = (AttributeFilter) object;
            encoder.writeObjectField("attribute", val.getAttribute(), false);
        }

        protected abstract T createFilter(Attribute attribute);
    }

    static {
        HANDLERS.add(new CompositeFilterHandler<AndFilter>(AndFilter.class, "AndFilter") {

            @Override
            protected AndFilter createFilter(final Filter left, final Filter right) {
                return new AndFilter(left, right);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<ContainsFilter>(ContainsFilter.class,
                "ContainsFilter") {

            @Override
            protected ContainsFilter createFilter(final Attribute attribute) {
                return new ContainsFilter(attribute);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<EndsWithFilter>(EndsWithFilter.class,
                "EndsWithFilter") {

            @Override
            protected EndsWithFilter createFilter(final Attribute attribute) {
                return new EndsWithFilter(attribute);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<EqualsFilter>(EqualsFilter.class, "EqualsFilter") {

            @Override
            protected EqualsFilter createFilter(final Attribute attribute) {
                return new EqualsFilter(attribute);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<GreaterThanFilter>(GreaterThanFilter.class,
                "GreaterThanFilter") {

            @Override
            protected GreaterThanFilter createFilter(final Attribute attribute) {
                return new GreaterThanFilter(attribute);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<GreaterThanOrEqualFilter>(
                GreaterThanOrEqualFilter.class, "GreaterThanOrEqualFilter") {

            @Override
            protected GreaterThanOrEqualFilter createFilter(final Attribute attribute) {
                return new GreaterThanOrEqualFilter(attribute);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<LessThanFilter>(LessThanFilter.class,
                "LessThanFilter") {

            @Override
            protected LessThanFilter createFilter(final Attribute attribute) {
                return new LessThanFilter(attribute);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<LessThanOrEqualFilter>(LessThanOrEqualFilter.class,
                "LessThanOrEqualFilter") {

            @Override
            protected LessThanOrEqualFilter createFilter(final Attribute attribute) {
                return new LessThanOrEqualFilter(attribute);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(NotFilter.class, "NotFilter") {

            public Object deserialize(final ObjectDecoder decoder) {
                final Filter filter = (Filter) decoder.readObjectContents(0);
                return new NotFilter(filter);
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final NotFilter val = (NotFilter) object;
                encoder.writeObjectContents(val.getFilter());
            }
        });

        HANDLERS.add(new CompositeFilterHandler<OrFilter>(OrFilter.class, "OrFilter") {

            @Override
            protected OrFilter createFilter(final Filter left, final Filter right) {
                return new OrFilter(left, right);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<StartsWithFilter>(StartsWithFilter.class,
                "StartsWithFilter") {

            @Override
            protected StartsWithFilter createFilter(final Attribute attribute) {
                return new StartsWithFilter(attribute);
            }
        });

        HANDLERS.add(new AttributeFilterHandler<ContainsAllValuesFilter>(
                ContainsAllValuesFilter.class, "ContainsAllValuesFilter") {

            @Override
            protected ContainsAllValuesFilter createFilter(final Attribute attribute) {
                return new ContainsAllValuesFilter(attribute);
            }
        });
    }
}
