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
import org.identityconnectors.framework.common.objects.Uid;

/**
 * Get a single value out of the attribute to test w/.
 */
public abstract class SingleValueAttributeFilter extends AttributeFilter {
    
    /**
     * Attempt to single out the value for comparison.
     */
    SingleValueAttributeFilter(Attribute attr) {
        super(attr);
        // make sure this is not a Uid..
        if (Uid.NAME.equals(attr.getName())) {
            final String MSG = "Uid can only be used for equals comparison.";
            throw new IllegalArgumentException(MSG);
        }
        // actual runtime..
        if (attr.getValue().size() != 1) {
            final String ERR = "Must only be one value!";
            throw new IllegalArgumentException(ERR);
        }
    }

    /**
     * Value to test against.
     */
    public Object getValue() {
        return getAttribute().getValue().get(0);
    }
}
