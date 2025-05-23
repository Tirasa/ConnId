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
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.serializer;

/**
 * Interface to be implemented to handle the serialization/deserialization of an object.
 */
public interface ObjectTypeMapper {

    /**
     * Returns the type of object being serialized.
     *
     * @return an abstract type name that is intended to be language neutral
     */
    String getHandledSerialType();

    /**
     * Returns the java class handled by this handler.
     *
     * @return the java class handled by this handler
     */
    Class<?> getHandledObjectType();

    /**
     * Should we match subclasses of the given class or only the exact class?
     *
     * @return whether subclasses of the given class or only the exact class should be matched
     */
    boolean isMatchSubclasses();
}
