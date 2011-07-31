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
package org.identityconnectors.framework.impl.serializer;

/**
 * Interface to be implemented to handle the serialization/
 * deserialization of an object.
 */
public interface ObjectTypeMapper {
    
    /**
     * Returns the type of object being serialized. This is 
     * an abstract type name that is intended to be language
     * neutral.
     */
    public String getHandledSerialType();
    
    /**
     * Returns the java class handled by this handler.
     */
    public Class<?> getHandledObjectType();
    
    /**
     * Should we match subclasses of the given class or only 
     * the exact class?
     */
    public boolean getMatchSubclasses();
    
}
