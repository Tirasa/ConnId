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

public final class OperationOptionInfoBuilder {
    private String _name;
    private Class<?> _type;
    
    public OperationOptionInfoBuilder() {
    }
       
    public OperationOptionInfoBuilder(String name,
            Class<?> type) {
        _name = name;
        _type = type;
    }
    
    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        _name = name;
    }
    
    public Class<?> getType() {
        return _type;
    }
    
    public void setType(Class<?> type) {
        _type = type;
    }
    
    public OperationOptionInfo build() {
        return new OperationOptionInfo(_name,_type);
    }
    
    public static OperationOptionInfo build(String name, Class<?> type) {
        return new OperationOptionInfoBuilder(name,type).build();
    }

    public static OperationOptionInfo build(String name) {
        return new OperationOptionInfoBuilder(name, String.class).build();
    }
    
    /**
     * Builds an {@link OperationOptionInfo} for the attribute to get option.
     */
    public static OperationOptionInfo buildAttributesToGet() {
        return build(OperationOptions.OP_ATTRIBUTES_TO_GET, String[].class);
    }
    
    public static OperationOptionInfo buildRunWithPassword() {
        return build(OperationOptions.OP_RUN_WITH_PASSWORD);
    }
    
    public static OperationOptionInfo buildRunWithUser() {
        return build(OperationOptions.OP_RUN_AS_USER);
    }
    
    public static OperationOptionInfo buildScope() {
        return build(OperationOptions.OP_SCOPE);
    }
    
    public static OperationOptionInfo buildContainer() {
        return build(OperationOptions.OP_CONTAINER,QualifiedUid.class);
    }
}
