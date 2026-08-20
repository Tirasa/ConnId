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
 * Portions Copyrighted 2026 ConnId
 */
package org.identityconnectors.framework.impl.serializer;

/**
 * Interface to abstract away the difference between deserializing xml and binary
 */
public interface ObjectDecoder {

    /**
     * Reads an object using the appropriate serializer for that object
     *
     * @param fieldName A hint of the field name. Ignored for binary
     * serialization. The subelement name for xml serialization
     * @expectedType Ignored for binary serialization. For xml serialization,
     * this must be specified if it was written in-line.
     * @dflt The default value if there is no value.
     */
    Object readObjectField(String fieldName, Class<?> expectedType, Object dflt);

    /**
     * Reads a boolean.
     *
     * @param fieldName A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value.
     */
    boolean readBooleanField(String fieldName, boolean dflt);

    /**
     * Reads an int.
     *
     * @param fieldName A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value.
     */
    int readIntField(String fieldName, int dflt);

    /**
     * Reads a long.
     *
     * @param fieldName A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value.
     */
    long readLongField(String fieldName, long dflt);

    /**
     * Reads a float.
     *
     * @param fieldName A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value.
     */
    float readFloatField(String fieldName, float dflt);

    /**
     * Reads a Class.
     *
     * @param fieldName A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value.
     */
    Class<?> readClassField(String fieldName, Class<?> dflt);

    /**
     * Reads a String.
     *
     * @param fieldName A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value.
     */
    String readStringField(String fieldName, String dflt);

    /**
     * Reads a double.
     *
     * @param fieldName A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value.
     */
    double readDoubleField(String fieldName, double dflt);

    /**
     * Reads the value in-line.
     */
    String readStringContents();

    /**
     * Reads the value in-line.
     */
    boolean readBooleanContents();

    /**
     * Reads the value in-line.
     */
    int readIntContents();

    /**
     * reads the value in-line.
     */
    long readLongContents();

    /**
     * Reads the value in-line.
     */
    float readFloatContents();

    /**
     * reads the value in-line.
     */
    double readDoubleContents();

    /**
     * reads the value in-line.
     */
    byte readByteContents();

    /**
     * reads the value in-line.
     */
    byte[] readByteArrayContents();

    /**
     * reads the value in-line.
     */
    Class<?> readClassContents();

    /**
     * Returns the number of anonymous sub-objects.
     *
     * @return
     */
    int getNumSubObjects();

    /**
     * Reads a sub-object
     */
    Object readObjectContents(int index);
}
