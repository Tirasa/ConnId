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
 */
package org.identityconnectors.framework.common.serializer;


/**
 * Interface for writing objects to a stream.
 */
public interface XmlObjectSerializer {
    /**
     * Writes the next object to the stream.
     *
     * @param object
     *            The object to write.
     * @see ObjectSerializerFactory for a list of supported types.
     * @throws org.identityconnectors.framework.common.exceptions.ConnectorException
     *             if there is more than one object and this is not configured
     *             for multi-object document.
     */
    public void writeObject(Object object);

    /**
     * Flushes the underlying stream.
     */
    public void flush();

    /**
     * Adds document end tag and optinally closes the underlying stream
     */
    public void close(boolean closeUnderlyingStream);
}
