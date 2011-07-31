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

public abstract class AttributeFilter implements Filter {

    private final Attribute attr;

    /**
     * Root filter for Attribute testing..
     */
    AttributeFilter(Attribute attr) {
        this.attr = attr;
        if (attr == null) {
            throw new IllegalArgumentException("Attribute not be null!");
        }
    }

    /**
     * Get the internal attribute.
     */
    public Attribute getAttribute() {
        return this.attr;
    }

    /**
     * Name of the attribute to find in the {@link ConnectorObject}.
     */
    public String getName() {
        return getAttribute().getName();
    }
    
    /**
     * Determines if the attribute provided is present in the
     * {@link ConnectorObject}.
     */
    public boolean isPresent(ConnectorObject obj) {
        return obj.getAttributeByName(this.attr.getName()) != null;
    }
}
