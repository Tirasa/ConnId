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

import org.identityconnectors.common.CollectionUtil;

/**
 * A single-valued attribute that represents the <i>user-friendly identifier</i> 
 * of an object on a target resource.
 * For instance, the name of an <code>Account</code> will most often be its loginName. 
 * The value of <code>Name</code> need not be unique within <code>ObjectClass</code>.
 * In LDAP, for example, the <code>Name</code> could be the <code>Common Name (CN)</code>.
 * Contrast this with {@link Uid}, which is intended to be a unique identifier
 * (and, if possible, immutable):
 * <ul>
 * <li>When an application creates an object, the application uses the <code>Name</code>
 * attribute to supply the user-friendly identifier for the object.
 * (Because the create operation returns the <code>Uid</code> as its result,
 * the application cannot know the <code>Uid</code> value beforehand.)
 * </li>
 * <li>When an application renames an object, this changes the <code>Name</code> of the object.
 * (For some target resources that do not have a separate internal identifier,
 * this might also change the <code>Uid</code>.  
 * However, the application would never attempt to change the <code>Uid</code> directly.)
 * </li>
 * </ul>
 * <b>NOTE:</b> For some connectors, <code>Name</code> and <code>Uid</code> will be equivalent.
 * If a target resource does not support a separate, internal identifier
 * for an object, then the create() method can simply return a <code>Uid</code> 
 * that has the same string value as the <code>Name</code> attribute.  
 * The DatabaseTable connector is an example of a connector 
 * that might use the same value for both <code>Name</code> and <code>Uid</code>.   
 */
public final class Name extends Attribute {
    public static final String NAME = AttributeUtil.createSpecialName("NAME");
    public static final AttributeInfo INFO =
        new AttributeInfoBuilder(NAME)
        .setRequired(true)
        .build();
    
    public Name(String value) {
        super(NAME, CollectionUtil.<Object>newReadOnlyList(value));
    }

    /**
     * The single value of the attribute that is the unique id of an object.
     * 
     * @return value that identifies an object.
     */
    public String getNameValue() {
        return AttributeUtil.getStringValue(this);
    }
}
