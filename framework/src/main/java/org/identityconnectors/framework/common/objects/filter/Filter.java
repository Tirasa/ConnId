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
 * Basic interface to match a {@link ConnectorObject}.
 * 
 * @author Will Droste
 * @version $Revision: 1.3 $
 * @since 1.0
 */
public interface Filter {
    /**
     * Determines whether the specified {@link ConnectorObject} 
     * matches this filter.
     * @param obj - The specified ConnectorObject.
     * @return {@code true} if the object matches
     *  (that is, satisfies all selection criteria of) this filter;
     *  otherwise {@code false}.
     */
    boolean accept(ConnectorObject obj);
}
