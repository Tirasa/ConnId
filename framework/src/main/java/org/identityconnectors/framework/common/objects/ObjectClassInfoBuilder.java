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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.common.CollectionUtil;


/**
 * Simplifies the construction of {@link ObjectClassInfo} instances.
 */
public final class ObjectClassInfoBuilder {

    private boolean _isContainer;
    private String _type;
    private Map<String, AttributeInfo> _info;

    public ObjectClassInfoBuilder() {
        _type = ObjectClass.ACCOUNT_NAME;
        _info = new HashMap<String, AttributeInfo>();
    }

    /**
     * Sets the specified {@link ObjectClassInfo#getType() type} for the
     * {@link ObjectClassInfo} object that is being built. (If this method is
     * not called, the <code>ObjectClassInfo</code> that is being built will
     * default to {@link ObjectClass#ACCOUNT_NAME} -- that is, its
     * <code>type</code> will default to to a String value of
     * {@link ObjectClass#ACCOUNT_NAME}.)
     * 
     * @see ObjectClassInfo#getType()
     * @see ObjectClass#ACCOUNT_NAME
     */
    public ObjectClassInfoBuilder setType(String type) {
        _type = type;
        return this;
    }

    /**
     * Add the specified {@link AttributeInfo} object to the
     * {@link ObjectClassInfo} that is being built.
     */
    public ObjectClassInfoBuilder addAttributeInfo(AttributeInfo info) {
        if (_info.containsKey(info.getName())) {
            final String MSG = "AttributeInfo of name '%s' already exists!";
            throw new IllegalArgumentException(String.format(MSG, info.getName()));
        }
        _info.put(info.getName(), info);
        return this;
    }

    /**
     * Add to the {@link ObjectClassInfo} that is being built each
     * {@link AttributeInfo} in the specified collection.
     */
    public ObjectClassInfoBuilder addAllAttributeInfo(Collection<AttributeInfo> c) {
        for (AttributeInfo info : c) {
            addAttributeInfo(info);
        }
        return this;
    }
    
    /**
     * Set to true to indicate this is a container type.
     * @param container True iff this is a container type.
     */
    public void setContainer(boolean container) {
        _isContainer = container;
    }

    /**
     * Constructs an instance of {@link ObjectClassInfo} with any
     * characteristics that were previously specified using this builder.
     * 
     * @return an instance of {@link ObjectClassInfo} with the characteristics
     *         previously specified.
     */
    public ObjectClassInfo build() {
        // determine if name is missing and add it by default
        if (!_info.containsKey(Name.NAME)) {
            _info.put(Name.NAME, Name.INFO);
        }
        return new ObjectClassInfo(_type, CollectionUtil.newSet(_info.values()),_isContainer);
    }
}
