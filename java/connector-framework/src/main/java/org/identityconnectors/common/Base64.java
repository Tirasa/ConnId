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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.common;

/**
 * Utility package for base64 encoding and decoding.
 *
 * @author Will Droste
 * @since 1.0
 * @deprecated Use {@link java.util.Base64} instead.
 */
public final class Base64 {

    /**
     * Never allow this to be instantiated.
     */
    private Base64() {
    }

    /**
     * Returns a String of base64-encoded characters to represent the specified data array.
     *
     * @param data The array of bytes to encode.
     * @return A String containing base64-encoded characters.
     * @deprecated Use {@code java.util.Base64.getEncoder().encodeToString(data)} instead
     */
    public static String encode(byte[] data) {
        return java.util.Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decodes a specified base64-encoded String and returns the resulting bytes.
     *
     * @param encdata A String containing base64-encoded characters.
     * @return The base64-decoded array of bytes.
     * @deprecated Use {@code java.util.Base64.getDecoder().decode(encdata)} instead
     */
    public static byte[] decode(String encdata) {
        return java.util.Base64.getDecoder().decode(encdata);
    }
}
