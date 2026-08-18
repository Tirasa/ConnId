/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2025 Evolveum. All rights reserved.
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

public abstract class BaseObjectClassInfoBuilder<T extends BaseObjectClassInfoBuilder,
        O extends LightweightObjectClassInfo> {

    protected boolean isContainer;

    protected boolean isAuxiliary;

    protected boolean isEmbedded;

    protected String type;

    protected String description;

    public BaseObjectClassInfoBuilder() {
        type = ObjectClass.ACCOUNT_NAME;
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
    public T setType(final String type) {
        this.type = type;
        return getThis();
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

    public T setEmbedded(final boolean embedded) {

        isEmbedded = embedded;
        return getThis();
    }

    public T setDescription(final String description) {
        this.description = description;
        return getThis();
    }

    protected abstract T getThis();

    /**
     * Constructs an instance of an object which extends the {@link LightweightObjectClassInfo} class with any
     * characteristics that were previously specified using this builder.
     *
     * @return an instance of an object which extends {@link LightweightObjectClassInfo} with the characteristics
     * previously specified.
     */
    public abstract O build();
}
