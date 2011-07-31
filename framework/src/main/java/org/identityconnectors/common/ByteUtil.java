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
package org.identityconnectors.common;

import java.util.Random;

/**
 * Utility package for byte manipulation.
 * 
 * @author Will Droste
 * @version $Revision $
 * @since 1.0
 */
public final class ByteUtil {
    
    /**
     * Never allow this to be instantiated.
     */
    private ByteUtil() {
        throw new AssertionError();
    }

    /**
     * For those that like the random bytes to be created and returned. Uses the
     * {@link Random#nextBytes(byte[])} method to generate the random data.
     * 
     * @param r
     *            to keep the same randomizer just pass it in..
     * @param length
     *            size of the array of random data returned.
     * @return byte array of random data.
     */
    public static byte[] randomBytes(Random r, int length) {
        byte[] ret = new byte[length];
        r.nextBytes(ret);
        return ret;
    }

    /**
     * Get a random array of bytes with the length specified.
     * 
     * @param length
     *            the size of the byte array returned.
     * @return random array of bytes with the length specified.
     */
    public static byte[] randomBytes(int length) {
        return randomBytes(new Random(), length);
    }

    /**
     * Random array of bytes with a random length. The length should be no
     * greater that 4k.
     * 
     * @param r
     *            uses the randomizer provided to generate the random data.
     * @return a byte array no bigger than 4k filled with random data.
     */
    public static byte[] randomBytes(Random r) {
        return randomBytes(r, r.nextInt(4048));
    }

    /**
     * Random array of bytes with a random length. The length should be no
     * greater that 4k.
     * 
     * @return a byte array no bigger than 4k filled with random data.
     */
    public static byte[] randomBytes() {
        return randomBytes(new Random());
    }
}
