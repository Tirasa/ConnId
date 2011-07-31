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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.common.objects.filter;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;

/**
 * Filter for an attribute value that is comparable.
 */
public abstract class ComparableAttributeFilter extends
        SingleValueAttributeFilter {

    /**
     * Attempt compare attribute values.
     */
    ComparableAttributeFilter(Attribute attr) {
        super(attr);
        // determine if this attribute value is comparable..
        if (!(getValue() instanceof Comparable)) {
            final String ERR = "Must be a comparable value!";
            throw new IllegalArgumentException(ERR);
        }
    }

    /**
     * Call compareTo on the attribute values. If the attribute is not present
     * in the {@link ConnectorObject} return -1.
     */
    public int compare(ConnectorObject obj) {
        int ret = -1;
        Attribute attr = obj.getAttributeByName(getName());
        if (attr != null && attr.getValue().size() == 1) {
            // it must be a comparable because that's were testing against
            if (!(attr.getValue().get(0) instanceof Comparable)) {
                final String ERR = "Attribute value must be comparable!";
                throw new IllegalArgumentException(ERR);
            }
            // grab this value and the on from the attribute an compare..
            Object o1 = attr.getValue().get(0);
            Object o2 = getValue();
            ret = CollectionUtil.forceCompare(o1, o2);
        }
        return ret;
    }
}
