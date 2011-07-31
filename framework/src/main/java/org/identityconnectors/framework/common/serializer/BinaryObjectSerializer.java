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
package org.identityconnectors.framework.common.serializer;

/**
 * Interface for writing objects to a stream.
 */
public interface BinaryObjectSerializer {
    /**
     * Writes the next object to the stream.
     * @param object The object to write.
     * @see ObjectSerializerFactory for a list of supported types.
     */
    public void writeObject(Object object);
    
    /**
     * Flushes the underlying stream.
     */
    public void flush();
    
    /**
     * Closes the underylying stream after first flushing it.
     */
    public void close();
}
