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

import static org.identityconnectors.framework.common.objects.AttributeUtil.createSpecialName;

/**
 * Defines standard syntax and semantics for common attributes 
 * that are not {@linkplain OperationalAttributes operational in nature}.
 */
public class PredefinedAttributes {
    
    /**
     * Attribute that should hold a reasonable value for an application
     * to display as a label for an object.  
     * If {@code SHORT_NAME} is not present, then the application
     * should use the value of {@link Name the NAME attribute} as a label.
     */
    public static final String SHORT_NAME = createSpecialName("SHORT_NAME");
    
    /**
     * Attribute that should hold the value of the object's description,
     * if one is available.
     */
    public static final String DESCRIPTION = createSpecialName("DESCRIPTION");
    
    /**
     * Read-only attribute that shows the last date/time the password was
     * changed.
     */
    public static final String LAST_PASSWORD_CHANGE_DATE_NAME = createSpecialName("LAST_PASSWORD_CHANGE_DATE");

    /**
     * Common password policy attribute where the password must be changed every
     * so often. The value for this attribute is milliseconds since its the
     * lowest common denominator.
     */
    public static final String PASSWORD_CHANGE_INTERVAL_NAME = createSpecialName("PASSWORD_CHANGE_INTERVAL");
    
    /**
     * Last login date for an account.  This is usually used to determine inactivity.
     */
    public static final String LAST_LOGIN_DATE_NAME = createSpecialName("LAST_LOGIN_DATE");
    
    /**
     * Attribute that refers to groups to which an {@linkplain ConnectorObject object} 
     * (usually an {@linkplain ObjectClass#ACCOUNT account}) belongs.
     * This {@code "GROUPS"} attribute is intended for 
     * {@link ObjectClass#ACCOUNT},
     * but another object-class that can be a member of a group 
     * could support this attribute.
     * <p> 
     * Each (element within the) value of this attribute 
     * is the string value of a Uid.  Each Uid identifies an
     * instance of {@link ObjectClass#GROUP}.
     */
    public static final String GROUPS_NAME = createSpecialName("GROUPS");

}
