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
 * Builder for instances of {@link SuggestedValues}.
 *
 * @author Radovan Semancik
 * @since 1.5.2.0
 */
public final class SuggestedValuesBuilder {

    private List<Object> values = new ArrayList<Object>();
    
    private ValueListOpenness openness = ValueListOpenness.CLOSED;

    /**
     * Creates closed suggested values with the specified values.
     *
     * @param values
     *            variable number of arguments that are used as values for the
     *            attribute.
     * @return instance of {@code SuggestedValues} with the specified values
     *         that includes the arguments provided.
     */
    public static SuggestedValues build(final Object... values) {
        SuggestedValuesBuilder bld = new SuggestedValuesBuilder();
        bld.addValues(values);
        return bld.build();
    }

    /**
     * Creates open suggested values with the specified values.
     *
     * @param values
     *            variable number of arguments that are used as values for the
     *            attribute.
     * @return instance of {@code SuggestedValues} with the specified values
     *         that includes the arguments provided.
     */
    public static SuggestedValues buildOpen(final Object... values) {
        SuggestedValuesBuilder bld = new SuggestedValuesBuilder();
        bld.setOpenness(ValueListOpenness.OPEN);
        bld.addValues(values);
        return bld.build();
    }

    /**
     * Creates closed suggested values with the specified values from a collection.
     *
     * @param collection
     *            a collection of objects that are used as suggested values.
     * @return instance of {@code SuggestedValues} with values
     *         from the provided collection.
     */
    public static SuggestedValues build(final Collection<?> collection) {
        SuggestedValuesBuilder bld = new SuggestedValuesBuilder();
        bld.addValues(collection);
        return bld.build();
    }

    /**
     * Return current values of the SuggestedValues instance that is being built.
     *
     * @return current values of the SuggestedValues instance that is being built.
     */
    public List<Object> getValues() {
        return values == null ? null : CollectionUtil.asReadOnlyList(values);
    }

    /**
     * Add each of the specified objects as a value for SuggestedValues instance that is being built.
     *
     * @param objs
     *            the values to add
     * @throws NullPointerException
     *             if any of the values is null.
     */
    public SuggestedValuesBuilder addValues(final Object... objs) {
        if (objs != null) {
            addValuesInternal(Arrays.asList(objs));
        }
        return this;
    }

    /**
     * Adds each object in the collection as a value for SuggestedValues instance that is being built.
     *
     * @param obj
     *            the values to add
     * @throws NullPointerException
     *             if any of the values is null.
     */
    public SuggestedValuesBuilder addValues(final Collection<?> obj) {
        addValuesInternal(obj);
        return this;
    }

    /**
     * @return a new SuggestedValues instance with the values that have been
     *         provided to the builder.
     */
    public SuggestedValues build() {
        return new SuggestedValues(values, openness);
    }

    private void addValuesInternal(final Iterable<?> newValues) {
        if (newValues != null) {
            for (Object v : newValues) {
                this.values.add(v);
            }
        }
    }

    /**
     * Returns openness of value list. Closed lists (the default) can accept only specified values.
     * Open lists can accept any value.
     * @see ValueListOpenness
     */
    public ValueListOpenness getOpenness() {
        return openness;
    }

    /**
     * Sets openness of value list. Closed lists (the default) can accept only specified values.
     * Open lists can accept any value.
     * @see ValueListOpenness
     */
    public void setOpenness(ValueListOpenness openness) {
        this.openness = openness;
    }

}
