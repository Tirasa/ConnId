/**
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2011-2013 Tirasa. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License"). You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at https://oss.oracle.com/licenses/CDDL.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GuardedStringTests {

    @Before
    public void setUp() {
        GuardedString.setEncryptor(new SimpleEncryptor());
    }

    @After
    public void tearDown() {
        GuardedString.setEncryptor(null);
    }

    @Test
    public void testBasics() {
        GuardedString str = new GuardedString("foo".toCharArray());
        assertEquals("foo", decryptToString(str));
        str.appendChar('2');
        assertEquals("foo2", decryptToString(str));
        assertFalse(str.verifyBase64SHA1Hash(SecurityUtil.
                computeBase64SHA1Hash("foo".toCharArray())));
        assertTrue(str.verifyBase64SHA1Hash(SecurityUtil.
                computeBase64SHA1Hash("foo2".toCharArray())));
    }

    @Test
    public void testEquals() {
        GuardedString str1 = new GuardedString();
        GuardedString str2 = new GuardedString();
        assertEquals(str1, str2);
        str2.appendChar('2');
        assertFalse(str1.equals(str2));
        str1.appendChar('2');
        assertEquals(str1, str2);
    }

    @Test
    public void testReadOnly() {
        GuardedString str = new GuardedString("foo".toCharArray());
        assertEquals(false, str.isReadOnly());
        str.makeReadOnly();
        assertEquals(true, str.isReadOnly());
        assertEquals("foo", decryptToString(str));
        try {
            str.appendChar('2');
            fail("expected exception");
        } catch (IllegalStateException e) {
        }
        str = str.copy();
        assertEquals("foo", decryptToString(str));
        str.appendChar('2');
        assertEquals("foo2", decryptToString(str));
    }

    @Test
    public void testDispose() {
        GuardedString str = new GuardedString("foo".toCharArray());
        str.dispose();
        try {
            decryptToString(str);
            fail("expected exception");
        } catch (IllegalStateException e) {
        }
        try {
            str.isReadOnly();
            fail("expected exception");
        } catch (IllegalStateException e) {
        }
        try {
            str.appendChar('c');
            fail("expected exception");
        } catch (IllegalStateException e) {
        }
        try {
            str.copy();
            fail("expected exception");
        } catch (IllegalStateException e) {
        }
        try {
            str.verifyBase64SHA1Hash("foo");
            fail("expected exception");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testUnicode() {

        for (int i = 0; i < 0xFFFF; i++) {
            final int expected = i;
            char c = (char) i;
            GuardedString gs = new GuardedString(new char[]{c});
            gs.access(new GuardedString.Accessor() {

                public void access(char[] clearChars) {
                    int v = (int) clearChars[0];
                    assertEquals(expected, v);
                }
            });

        }
    }

    /**
     * Highly insecure method! Do not do this in production code. This is only for test purposes
     */
    private String decryptToString(GuardedString string) {
        final StringBuilder buf = new StringBuilder();
        string.access(
                new GuardedString.Accessor() {

                    public void access(char[] chars) {
                        buf.append(chars);
                    }
                });
        return buf.toString();
    }
}
