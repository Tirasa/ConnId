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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.framework.common.objects.filter;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;

/**
 * Filter based on strings.
 */
public abstract class StringFilter extends SingleValueAttributeFilter {

    /**
     * Attempts to get a string from the attribute.
     */
    StringFilter(Attribute attr) {
        super(attr);
        Object val = super.getValue();
        if (!(val instanceof String)) {
            throw new IllegalArgumentException("Value must be a string!");
        }
    }

    /**
     * Get the string value from the afore mentioned attribute.
     *
     * @see SingleValueAttributeFilter#getValue()
     */
    @Override
    public String getValue() {
        return (String) super.getValue();
    }

    /**
     * @throws IllegalArgumentException if the value from the {@link ConnectorObject}'s attribute of the same name as
     * provided is not a string.
     * @see org.identityconnectors.framework.common.objects.filter.Filter#accept(ConnectorObject)
     */
    @Override
    public boolean accept(ConnectorObject obj) {
        boolean ret = false;
        Attribute attr = obj.getAttributeByName(getName());
        if (attr != null && attr.getValue() != null) {
            if (!(attr.getValue().get(0) instanceof String)) {
                throw new IllegalArgumentException("Value must be a string!");
            }
            ret = accept((String) attr.getValue().get(0));
        }
        return ret;
    }

    public abstract boolean accept(String value);
}
