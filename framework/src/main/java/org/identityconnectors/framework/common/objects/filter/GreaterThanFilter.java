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

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;

public final class GreaterThanFilter extends ComparableAttributeFilter {

    /**
     * Determine if the {@link ConnectorObject} {@link Attribute} value is
     * greater than the one provided in the filter.
     */
    public GreaterThanFilter(Attribute attr) {
        super(attr);
    }

    /**
     * Determine if the {@link ConnectorObject} {@link Attribute} value is
     * greater than the one provided in the filter.
     * 
     * @see org.identityconnectors.framework.common.objects.filter.Filter#accept(ConnectorObject)
     */
    public boolean accept(ConnectorObject obj) {
        return isPresent(obj) && this.compare(obj) > 0;
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("GREATERTHAN: ").append(getAttribute());
        return bld.toString();
    }
}
