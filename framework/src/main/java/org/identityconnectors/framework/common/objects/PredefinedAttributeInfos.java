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
package org.identityconnectors.framework.common.objects;

import java.util.EnumSet;

import org.identityconnectors.framework.common.objects.AttributeInfo.Flags;

public class PredefinedAttributeInfos {
    
    /**
     * Attribute that should hold a reasonable value to
     * display for the value of an object.  If this is not present, then the
     * application will have to use the NAME to show the value.
     */
    public static final AttributeInfo SHORT_NAME = 
        AttributeInfoBuilder.build(PredefinedAttributes.SHORT_NAME);
    
    /**
     * Attribute that should hold the value of the object's description,
     * if one is available.
     */
    public static final AttributeInfo DESCRIPTION = 
        AttributeInfoBuilder.build(PredefinedAttributes.DESCRIPTION);
    
    /**
     * Read-only attribute that shows the last date/time the password was
     * changed.
     */
    public static final AttributeInfo LAST_PASSWORD_CHANGE_DATE = 
         AttributeInfoBuilder.build(PredefinedAttributes.LAST_PASSWORD_CHANGE_DATE_NAME, 
                 long.class,
                 EnumSet.of(Flags.NOT_CREATABLE,
                            Flags.NOT_UPDATEABLE));

    /**
     * Common password policy attribute where the password must be changed every
     * so often. The value for this attribute is milliseconds since its the
     * lowest common denominator.
     */
    public static final AttributeInfo PASSWORD_CHANGE_INTERVAL = 
        AttributeInfoBuilder.build(PredefinedAttributes.PASSWORD_CHANGE_INTERVAL_NAME, 
                long.class);

    /**
     * Last login date for an account. This is usually used to determine
     * inactivity.
     */
    public static final AttributeInfo LAST_LOGIN_DATE = 
        AttributeInfoBuilder.build(PredefinedAttributes.LAST_LOGIN_DATE_NAME, 
                long.class,
                EnumSet.of(Flags.NOT_CREATABLE,
                        Flags.NOT_UPDATEABLE));
                

    /**
     * Groups that an account belongs to. The Attribute values are the
     * UID value of each group that an account has membership in.
     */
    public static final AttributeInfo GROUPS =
        AttributeInfoBuilder.build(PredefinedAttributes.GROUPS_NAME,
                String.class,
                EnumSet.of(Flags.MULTIVALUED,
                        Flags.NOT_RETURNED_BY_DEFAULT));
        
}
