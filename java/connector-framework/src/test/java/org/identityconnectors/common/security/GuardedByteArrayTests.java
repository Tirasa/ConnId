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
package org.identityconnectors.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GuardedByteArrayTests {

    @BeforeEach
    public void setUp() {
        GuardedByteArray.setEncryptor(new SimpleEncryptor());
    }

    @AfterEach
    public void tearDown() {
        GuardedByteArray.setEncryptor(null);
    }

    @Test
    public void testBasics() {
        GuardedByteArray bytes = new GuardedByteArray(new byte[] { 0x00, 0x01, 0x02 });
        assertTrue(Arrays.equals(decryptToBytes(bytes), new byte[] { 0x00, 0x01, 0x02 }));
        assertTrue(Arrays.equals(new byte[] { 0x00, 0x01, 0x02 }, decryptToBytes(bytes)));
        bytes.appendByte((byte) 0x03);
        assertTrue(Arrays.equals(new byte[] { 0x00, 0x01, 0x02, 0x03 }, decryptToBytes(bytes)));
        assertFalse(bytes.verifyBase64SHA1Hash(
                SecurityUtil.computeBase64SHA1Hash(new byte[] { 0x00, 0x01, 0x02 })));
        assertTrue(bytes.verifyBase64SHA1Hash(
                SecurityUtil.computeBase64SHA1Hash(new byte[] { 0x00, 0x01, 0x02, 0x03 })));
    }

    @Test
    public void testEquals() {
        GuardedByteArray bytes1 = new GuardedByteArray();
        GuardedByteArray bytes2 = new GuardedByteArray();
        assertEquals(bytes1, bytes2);
        bytes2.appendByte((byte) 0x03);
        assertFalse(bytes1.equals(bytes2));
        bytes1.appendByte((byte) 0x03);
        assertEquals(bytes1, bytes2);
    }

    @Test
    public void testReadOnly() {
        GuardedByteArray bytes = new GuardedByteArray(new byte[] { 0x00, 0x01, 0x02 });
        assertFalse(bytes.isReadOnly());
        bytes.makeReadOnly();
        assertTrue(bytes.isReadOnly());
        assertTrue(Arrays.equals(new byte[] { 0x00, 0x01, 0x02 }, decryptToBytes(bytes)));
        try {
            bytes.appendByte((byte) 0x03);
            fail("expected exception");
        } catch (IllegalStateException e) {
            /* ignore */
        }
        bytes = bytes.copy();
        assertTrue(Arrays.equals(new byte[] { 0x00, 0x01, 0x02 }, decryptToBytes(bytes)));
        bytes.appendByte((byte) 0x03);
        assertTrue(Arrays.equals(new byte[] { 0x00, 0x01, 0x02, 0x03 }, decryptToBytes(bytes)));
    }

    @Test
    public void testDispose() {
        GuardedByteArray str = new GuardedByteArray(new byte[] { 0x00, 0x01, 0x02 });
        str.dispose();
        try {
            decryptToBytes(str);
            fail("expected exception");
        } catch (IllegalStateException e) {
            /* ignore */
        }
        try {
            str.isReadOnly();
            fail("expected exception");
        } catch (IllegalStateException e) {
            /* ignore */
        }
        try {
            str.appendByte((byte) 0x03);
            fail("expected exception");
        } catch (IllegalStateException e) {
            /* ignore */
        }
        try {
            str.copy();
            fail("expected exception");
        } catch (IllegalStateException e) {
            /* ignore */
        }
        try {
            str.verifyBase64SHA1Hash("foo");
            fail("expected exception");
        } catch (IllegalStateException e) {
            /* ignore */
        }
    }

    @Test
    public void testRange() {
        for (int i = -128; i < 128; i++) {
            final byte expected = (byte) i;
            GuardedByteArray bytes = new GuardedByteArray(new byte[] { (byte) i });
            bytes.access((byte[] clearBytes) -> {
                byte v = clearBytes[0];
                assertEquals(v, expected);
            });
        }
    }

    /**
     * Highly insecure method! Do not do this in production
     * code. This is only for test purposes
     */
    private byte[] decryptToBytes(GuardedByteArray bytes) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        bytes.access((byte[] bytes1) -> {
            out.write(bytes1, 0, bytes1.length);
        });
        return out.toByteArray();
    }
}
