/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS. All rights reserved.
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

import org.identityconnectors.common.Assertions;

/**
 * A sort key which can be used to specify the order in which connector objects
 * should be included in the results of a search request.
 *
 * @author Laszlo Hordos
 * @since 1.4
 */
public final class SortKey {

    private final String field;
    private final boolean isAscendingOrder;

    public SortKey(final String field, final boolean isAscendingOrder) {
        this.field = Assertions.blankChecked(field, "field");
        this.isAscendingOrder = isAscendingOrder;
    }

    /**
     * Returns the sort key field.
     *
     * @return The sort key field.
     */
    public String getField() {
        return field;
    }

    /**
     * Returns {@code true} if this sort key is in ascending order, or
     * {@code false} if it is in descending order.
     *
     * @return {@code true} if this sort key is in ascending order, or
     *         {@code false} if it is in descending ord)er.
     */
    public boolean isAscendingOrder() {
        return isAscendingOrder;
    }
}
