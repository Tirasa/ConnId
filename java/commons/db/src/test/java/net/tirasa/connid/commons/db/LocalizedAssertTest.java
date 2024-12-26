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
 * Portions Copyrighted 2011 ConnId.
 */
package net.tirasa.connid.commons.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Tests for LocalizedAssert */
public class LocalizedAssertTest {

    private static class TestConnectorMessages implements ConnectorMessages {

        private Properties properties;

        TestConnectorMessages() {
            try {
                properties = IOUtil.getResourceAsProperties(getClass()
                        .getClassLoader(), LocalizedAssertTest.class
                                .getPackage().getName().replace('.', '/')
                        + "/Messages.properties");
                assertNotNull(properties);
            } catch (IOException e) {
                fail("Cannot load Messages.properties" + e.getMessage());
            }
        }

        @Override
        public String format(String key, String dflt, Object... args) {
            String value = properties.getProperty(key);
            if (value == null) {
                return dflt != null ? dflt : key;
            }
            if (args == null) {
                return value;
            }
            return MessageFormat.format(value, args);
        }
    }

    static LocalizedAssert testee;

    @BeforeAll
    public static void setup() {
        testee = new LocalizedAssert(new TestConnectorMessages());
    }

    /**
     * Test method for
     * {@link org.identityconnectors.dbcommon.LocalizedAssert#assertNotNull(java.lang.Object, java.lang.String)}.
     */
    @Test
    public final void testAssertNotNull() {
        Integer i = testee.assertNotNull(1, "i");
        assertEquals(1, i);
        try {
            testee.assertNotNull(null, "i");
            fail("Must fail for null argument");
        } catch (RuntimeException e) {
            assertEquals("Argument [i] cannot be null", e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.identityconnectors.dbcommon.LocalizedAssert#assertNull(java.lang.Object, java.lang.String)}.
     */
    @Test
    public final void testAssertNull() {
        Integer i = testee.assertNull(null, "i");
        assertNull(i);
        try {
            testee.assertNull(1, "i");
            fail("Must fail for not null argument");
        } catch (RuntimeException e) {
            assertEquals("Argument [i] must be null", e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.identityconnectors.dbcommon.LocalizedAssert#assertNotBlank(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testAssertNotBlank() {
        String os = testee.assertNotBlank("Linux", "os");
        assertEquals("Linux", os);
        try {
            testee.assertNotBlank(null, "os");
            fail("Must fail for null argument");
        } catch (RuntimeException e) {
            assertEquals("Argument [os] cannot be blank", e.getMessage());
        }
        try {
            testee.assertNotBlank("", "os");
            fail("Must fail for blank argument");
        } catch (RuntimeException e) {
            assertEquals("Argument [os] cannot be blank", e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.identityconnectors.dbcommon.LocalizedAssert#assertBlank(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testAsserBlank() {
        String os = testee.assertBlank(null, "os");
        assertNull(os);
        os = testee.assertBlank("", "os");
        assertEquals("", os);
        try {
            testee.assertBlank("Some os", "os");
            fail("Must fail for non blank argument");
        } catch (RuntimeException e) {
            assertEquals("Argument [os] must be blank", e.getMessage());
        }
    }

    /** Test of {@link LocalizedAssert#LocalizedAssert(ConnectorMessages)} */
    @Test
    public void testCreate() {
        new LocalizedAssert(new TestConnectorMessages());
        try {
            new LocalizedAssert(null);
            fail("Must fail for null ConnectorMessages");
        } catch (RuntimeException e) {
            //emptyS
        }
    }

    /**
     * test method
     */
    @Test
    public void testLocalizeArgumentNames() {
        LocalizedAssert la = new LocalizedAssert(new TestConnectorMessages(), true);
        try {
            //Small hack, we do not create new TestMessages with dummy argument name, use assert.blank as argument name
            la.assertNotBlank("", "assert.blank");
            fail("Must fail for blank String");
        } catch (RuntimeException e) {
            assertEquals("Argument [Argument [{0}] must be blank] cannot be blank", e.getMessage());
        }
    }
}
