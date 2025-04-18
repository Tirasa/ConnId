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
 * Portions Copyrighted 2014 ForgeRock AS.
 */
package org.identityconnectors.framework.common.objects.filter;

import java.util.Collection;
import org.identityconnectors.common.CollectionUtil;

/**
 * Useful for the AND, OR, XOR, etc..
 */
public abstract class CompositeFilter implements Filter {

    /**
     * Left side of a composite based filter.
     */
    private final Filter left;

    /**
     * Right side of a composite based filter.
     */
    private final Filter right;

    /**
     * Create a composite filter w/ the left and right filters provided.
     *
     * @param left
     *            the left side of the composite.
     * @param right
     *            the right side of the composite.
     */
    CompositeFilter(Filter left, Filter right) {
        this.left = left;
        this.right = right;
    }

    /**
     * @return the left side of the composite.
     */
    public Filter getLeft() {
        return left;
    }

    /**
     * @return the right side of the composite.
     */
    public Filter getRight() {
        return right;
    }

    public Collection<Filter> getFilters() {
        return CollectionUtil.newReadOnlyList(getLeft(), getRight());
    }
}
