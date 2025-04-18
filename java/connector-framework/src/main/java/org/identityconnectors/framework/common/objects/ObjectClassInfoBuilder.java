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
 * Portions Copyrighted 2015 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.identityconnectors.common.CollectionUtil;

/**
 * Simplifies the construction of {@link ObjectClassInfo} instances.
 */
public final class ObjectClassInfoBuilder {

    private boolean isContainer;

    private boolean isAuxiliary;

    private boolean isEmbedded;

    private String type;

    private final Map<String, AttributeInfo> attributeInfoMap;

    public ObjectClassInfoBuilder() {
        type = ObjectClass.ACCOUNT_NAME;
        attributeInfoMap = new HashMap<>();
    }

    /**
     * Sets the specified {@link ObjectClassInfo#getType() type} for the
     * {@link ObjectClassInfo} object that is being built.
     *
     * (If this method is not called, the <code>ObjectClassInfo</code> that is
     * being built will default to {@link ObjectClass#ACCOUNT_NAME} -- that is,
     * its <code>type</code> will default to to a String value of
     * {@link ObjectClass#ACCOUNT_NAME}.)
     *
     * @see ObjectClassInfo#getType()
     * @see ObjectClass#ACCOUNT_NAME
     */
    public ObjectClassInfoBuilder setType(final String type) {
        this.type = type;
        return this;
    }

    private static final String FORMAT = "AttributeInfo of name '%s' already exists!";

    /**
     * Add the specified {@link AttributeInfo} object to the
     * {@link ObjectClassInfo} that is being built.
     */
    public ObjectClassInfoBuilder addAttributeInfo(final AttributeInfo info) {
        if (attributeInfoMap.containsKey(info.getName())) {
            throw new IllegalArgumentException(String.format(FORMAT, info.getName()));
        }
        attributeInfoMap.put(info.getName(), info);
        return this;
    }

    /**
     * Add to the {@link ObjectClassInfo} that is being built each
     * {@link AttributeInfo} in the specified collection.
     */
    public ObjectClassInfoBuilder addAllAttributeInfo(final Collection<AttributeInfo> c) {
        for (AttributeInfo info : c) {
            addAttributeInfo(info);
        }
        return this;
    }

    /**
     * Set to true to indicate this is a container type.
     *
     * @param container True if this is a container type.
     */
    public void setContainer(final boolean container) {
        isContainer = container;
    }

    public void setAuxiliary(final boolean isAuxiliary) {
        this.isAuxiliary = isAuxiliary;
    }

    public ObjectClassInfoBuilder setEmbedded(final boolean embedded) {
        isEmbedded = embedded;
        return this;
    }

    /**
     * Constructs an instance of {@link ObjectClassInfo} with any
     * characteristics that were previously specified using this builder.
     *
     * @return an instance of {@link ObjectClassInfo} with the characteristics
     * previously specified.
     */
    public ObjectClassInfo build() {
        // determine if name is missing and add it by default
        if (!attributeInfoMap.containsKey(Name.NAME)) {
            attributeInfoMap.put(Name.NAME, Name.INFO);
        }
        return new ObjectClassInfo(
                type,
                CollectionUtil.newSet(attributeInfoMap.values()),
                isContainer,
                isAuxiliary,
                isEmbedded);
    }
}
