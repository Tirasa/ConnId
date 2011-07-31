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

import static org.identityconnectors.framework.common.objects.NameUtil.nameHashCode;
import static org.identityconnectors.framework.common.objects.NameUtil.namesEqual;
import static org.identityconnectors.framework.common.objects.ObjectClassUtil.createSpecialName;

import java.util.Locale;

/**
 * An instance of <code>ObjectClass</code> 
 * specifies a <i>category or type</i> of {@link ConnectorObject}.
 * This class predefines some common object-classes,
 * such as <code>ACCOUNT</code> and <code>GROUP</code>.
 * 
 * @author Will Droste
 * @version $Revision: 1.3 $
 * @since 1.0
 */
public final class ObjectClass {
    
    // =======================================================================
    // Basic Types--i.e., common values of the ObjectClass attribute.
    // =======================================================================    

    /**
     * This constant defines a specific 
     * {@linkplain #getObjectClassValue value of ObjectClass} 
     * that is reserved for {@link ObjectClass#ACCOUNT}.
     */
    public static final String ACCOUNT_NAME = createSpecialName("ACCOUNT");

    /**
     * This constant defines a specific 
     * {@linkplain #getObjectClassValue value of ObjectClass} 
     * that is reserved for {@link ObjectClass#GROUP}.
     */
    public static final String GROUP_NAME = createSpecialName("GROUP");
    
    // =======================================================================
    // Create only after all other static initializers
    // =======================================================================
    
    /**
     * Represents a human being <i>in the context of a specific system or application</i>.
     * <p>
     * When an attribute matching this constant is found within a <code>ConnectorObject</code>,
     * this indicates that the <code>ConnectorObject</code> represents a human being
     * (actual or fictional) within the context of a specific system or application.
     * <p>
     * Generally, an Account object records characteristics of a human user
     * (such as loginName, password, user preferences or access privileges)
     * that are relevant only to (or primarily to) a specific system or application.
     */
    public static final ObjectClass ACCOUNT = new ObjectClass(ACCOUNT_NAME);

    /**
     * Represents a collection that contains an object (such as an account).
     * <p>
     * When an attribute matching this constant is found within a <code>ConnectorObject</code>,
     * this indicates that the <code>ConnectorObject</code> represents a group.
     */
    public static final ObjectClass GROUP = new ObjectClass(GROUP_NAME);

    private final String _type;
    
    /**
     * Create a custom object class.
     * 
     * @param type
     *            string representation for the name of the object class.
     */
    public ObjectClass(String type) {
        if ( type == null ) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        _type = type;
    }

    /**
     * Get the name of the object class.
     * (For example, the name of {@link ObjectClass#ACCOUNT}
     * is the value defined by {@link ObjectClass#ACCOUNT_NAME},
     * which is <code>"__ACCOUNT__"</code>.)
     */
    public String getObjectClassValue() {
        return _type;
    }
    
    /**
     * Convenience method to build the display name key for
     * an object class.  
     * 
     * @return The display name key.
     */
    public String getDisplayNameKey() {
        return "MESSAGE_OBJECT_CLASS_"+_type.toUpperCase(Locale.US);
    }
    
    /**
     * Determines if the 'name' matches this {@link ObjectClass}.
     * 
     * @param name
     *            case-insensitive string representation of the ObjectClass's
     *            type.
     * @return <code>true</code> if the case-insensitive name is equal to
     *         that of the one in this {@link ObjectClass}.
     */
    public boolean is(String name) {
        return namesEqual(_type, name);
    }
    
    @Override
    public int hashCode() {
        return nameHashCode(_type);
    }
    
    @Override
    public final boolean equals(Object obj) {
        // test identity
        if (this == obj) {
            return true;
        }
        // test for null..
        if (obj == null) {
            return false;
        }
        // test that the exact class matches
        if (!(getClass().equals(obj.getClass()))) {
            return false;
        }
        
        ObjectClass other = (ObjectClass)obj;
        
        if(!is(other.getObjectClassValue())) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "ObjectClass: "+_type;
    }

}
