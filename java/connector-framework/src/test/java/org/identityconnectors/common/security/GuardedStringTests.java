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
 * Portions Copyrighted 2022 Evolveum
 */
package org.identityconnectors.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GuardedStringTests {

    @BeforeEach
    public void setUp() {
        GuardedString.setEncryptor(new SimpleEncryptor());
    }

    @AfterEach
    public void tearDown() {
        GuardedString.setEncryptor(null);
    }

    @Test
    public void testBasics() {
        GuardedString str = new GuardedString("foo".toCharArray());
        assertEquals(decryptToString(str), "foo");
        str.appendChar('2');
        assertEquals(decryptToString(str), "foo2");
        assertFalse(str.verifyBase64SHA1Hash(SecurityUtil
                .computeBase64SHA1Hash("foo".toCharArray())));
        assertTrue(str.verifyBase64SHA1Hash(SecurityUtil
                .computeBase64SHA1Hash("foo2".toCharArray())));
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
        assertFalse(str.isReadOnly());
        str.makeReadOnly();
        assertTrue(str.isReadOnly());
        assertEquals(decryptToString(str), "foo");
        try {
            str.appendChar('2');
            fail("expected exception");
        } catch (IllegalStateException e) {
            /* ignore */
        }
        str = str.copy();
        assertEquals(decryptToString(str), "foo");
        str.appendChar('2');
        assertEquals(decryptToString(str), "foo2");
    }

    @Test
    public void testDispose() {
        GuardedString str = new GuardedString("foo".toCharArray());
        str.dispose();
        try {
            decryptToString(str);
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
            str.appendChar('c');
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
    public void testUnicode() {

        for (int i = 0; i < 0xFFFF; i++) {
            final int expected = i;
            char c = (char) i;
            GuardedString gs = new GuardedString(new char[] { c });
            gs.access((char[] clearChars) -> {
                int v = (int) clearChars[0];
                assertEquals(v, expected);
            });
        }
    }

    /**
     * Highly insecure method! Do not do this in production code. This is only
     * for test purposes
     */
    private String decryptToString(GuardedString string) {
        final StringBuilder buf = new StringBuilder();
        string.access((char[] chars) -> {
            buf.append(chars);
        });
        return buf.toString();
    }

    // This test must be here, even though it would be better to have it in AttributeBuilderTests
    // GuardedString.setEncryptor() method is package-private, therefore it cannot be initialized in AttributeBuilderTests.
    @Test
    public void passwordAttribute() {
        AttributeBuilder bld = new AttributeBuilder();
        bld.setName(OperationalAttributes.PASSWORD_NAME);
        bld.addValue(new GuardedString("foobar".toCharArray()));
        Attribute attr = bld.build();
        assertEquals(OperationalAttributes.PASSWORD_NAME, attr.getName());
        assertEquals(1, attr.getValue().size());
        assertTrue(attr.getValue().get(0) instanceof GuardedString);
        assertEquals("foobar", decryptToString((GuardedString)attr.getValue().get(0)));
    }
}
