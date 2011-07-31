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

import org.identityconnectors.framework.common.objects.ConnectorObject;

/**
 * Proxy the filter to return the negative of the value.
 */
public final class NotFilter implements Filter {

    private final Filter filter;

    /**
     * Take the value returned from the internal filter and NOT it.
     */
    public NotFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Get the internal filter that is being negated.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Return the opposite the internal filters return value.
     * 
     * @see Filter#accept(ConnectorObject)
     */
    public boolean accept(ConnectorObject obj) {
        return !this.filter.accept(obj);
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("NOT: ").append(getFilter());
        return super.toString();
    }
}
