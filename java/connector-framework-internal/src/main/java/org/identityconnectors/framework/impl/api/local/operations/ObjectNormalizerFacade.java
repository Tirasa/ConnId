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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.LiveSyncDelta;
import org.identityconnectors.framework.common.objects.LiveSyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.filter.AndFilter;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
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
import org.identityconnectors.framework.spi.AttributeNormalizer;

public final class ObjectNormalizerFacade {

    /**
     * The (non-null) object class
     */
    private final ObjectClass objectClass;

    /**
     * The (possibly null) attribute normalizer
     */
    private final AttributeNormalizer normalizer;

    /**
     * Create a new ObjectNormalizer.
     *
     * @param objectClass The object class
     * @param normalizer The normalizer. May be null.
     */
    public ObjectNormalizerFacade(final ObjectClass objectClass, final AttributeNormalizer normalizer) {
        Assertions.nullCheck(objectClass, "objectClass");
        this.objectClass = objectClass;
        this.normalizer = normalizer;
    }

    /**
     * Returns the normalized value of the attribute.
     *
     * If no normalizer is specified, returns the original attribute.
     *
     * @param attribute The attribute to normalize.
     * @return The normalized attribute
     */
    public Attribute normalizeAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        } else if (normalizer != null) {
            return normalizer.normalizeAttribute(objectClass, attribute);
        } else {
            return attribute;
        }
    }

    /**
     * Returns the normalized set of attributes or null if the original set is null.
     *
     * @param attributes The original attributes.
     * @return The normalized attributes or null if the original set is null.
     */
    public Set<Attribute> normalizeAttributes(Set<Attribute> attributes) {
        if (attributes == null) {
            return null;
        }
        Set<Attribute> temp = new HashSet<>();
        for (Attribute attribute : attributes) {
            temp.add(normalizeAttribute(attribute));
        }
        return Collections.unmodifiableSet(temp);
    }

    /**
     * Returns the normalized object.
     *
     * @param orig The original object
     * @return The normalized object.
     */
    public ConnectorObject normalizeObject(ConnectorObject orig) {
        return new ConnectorObject(orig.getObjectClass(), normalizeAttributes(orig.getAttributes()));
    }

    /**
     * Returns the normalized sync delta.
     *
     * @param delta The original delta.
     * @return The normalized delta.
     */
    public SyncDelta normalizeSyncDelta(SyncDelta delta) {
        SyncDeltaBuilder builder = new SyncDeltaBuilder(delta);
        Optional.ofNullable(delta.getObject()).ifPresent(o -> builder.setObject(normalizeObject(o)));
        return builder.build();
    }

    /**
     * Returns the normalized live sync delta.
     *
     * @param delta The original delta.
     * @return The normalized delta.
     */
    public LiveSyncDelta normalizeLiveSyncDelta(LiveSyncDelta delta) {
        LiveSyncDeltaBuilder builder = new LiveSyncDeltaBuilder(delta);
        Optional.ofNullable(delta.getObject()).ifPresent(o -> builder.setObject(normalizeObject(o)));
        return builder.build();
    }

    /**
     * Returns a filter consisting of the original with all attributes
     * normalized.
     *
     * @param filter The original.
     * @return The normalized filter.
     */
    public Filter normalizeFilter(Filter filter) {
        if (filter instanceof ContainsFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new ContainsFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof EndsWithFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new EndsWithFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof EqualsFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new EqualsFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof GreaterThanFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new GreaterThanFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof GreaterThanOrEqualFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new GreaterThanOrEqualFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof LessThanFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new LessThanFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof LessThanOrEqualFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new LessThanOrEqualFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof StartsWithFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new StartsWithFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof ContainsAllValuesFilter) {
            AttributeFilter afilter = (AttributeFilter) filter;
            return new ContainsAllValuesFilter(normalizeAttribute(afilter.getAttribute()));
        } else if (filter instanceof NotFilter) {
            NotFilter notFilter = (NotFilter) filter;
            return new NotFilter(normalizeFilter(notFilter.getFilter()));
        } else if (filter instanceof AndFilter) {
            AndFilter andFilter = (AndFilter) filter;
            return new AndFilter(normalizeFilter(andFilter.getLeft()), normalizeFilter(andFilter.getRight()));
        } else if (filter instanceof OrFilter) {
            OrFilter orFilter = (OrFilter) filter;
            return new OrFilter(normalizeFilter(orFilter.getLeft()), normalizeFilter(orFilter.getRight()));
        } else {
            return filter;
        }
    }
}
