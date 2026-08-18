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
 * Portions Copyrighted 2026 ConnId
 */
package org.identityconnectors.framework.common.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.identityconnectors.common.CollectionUtil;

/**
 * Simplifies the construction of {@link ObjectClassInfo} instances.
 */
public final class ObjectClassInfoBuilder extends BaseObjectClassInfoBuilder<ObjectClassInfoBuilder, ObjectClassInfo> {

    private final Map<String, AttributeInfo> attributeInfoMap;

    public ObjectClassInfoBuilder() {
        super();
        attributeInfoMap = new HashMap<>();
    }

    private static final String FORMAT = "AttributeInfo of name '%s' already exists!";

    /**
     * Add the specified {@link AttributeInfo} object to the
     * {@link ObjectClassInfo} that is being built.
     */
    public ObjectClassInfoBuilder addAttributeInfo(final AttributeInfo info) {
        if (attributeInfoMap.containsKey(info.getName())) {
            throw new IllegalArgumentException(FORMAT.formatted(info.getName()));
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

    @Override
    protected ObjectClassInfoBuilder getThis() {
        return this;
    }

    /**
     * Constructs an instance of {@link ObjectClassInfo} with any
     * characteristics that were previously specified using this builder.
     *
     * @return an instance of {@link ObjectClassInfo} with the characteristics
     * previously specified.
     */
    @Override
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
                isEmbedded,
                description);
    }

    // Binary level backwards compatibility
    // Moving method to superclass is source-level backwards compatible change, but not binary-level backwards
    // compatible change, so we need to override this methods in order to provide backwards compatibility for connectors
    // built with previous version of APIs.
    @Override
    public ObjectClassInfoBuilder setDescription(String description) {
        return super.setDescription(description);
    }

    @Override
    public ObjectClassInfoBuilder setEmbedded(boolean embedded) {
        return super.setEmbedded(embedded);
    }

    @Override
    public ObjectClassInfoBuilder setType(String type) {
        return super.setType(type);
    }

    @Override
    public void setAuxiliary(boolean isAuxiliary) {
        super.setAuxiliary(isAuxiliary);
    }

    @Override
    public void setContainer(boolean container) {
        super.setContainer(container);
    }
}
