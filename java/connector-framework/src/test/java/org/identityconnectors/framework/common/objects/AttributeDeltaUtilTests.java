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
package org.identityconnectors.framework.common.objects;

import static org.identityconnectors.framework.common.objects.AttributeDeltaBuilder.build;
import static org.identityconnectors.framework.common.objects.AttributeDeltaUtil.getAsStringValue;
import static org.identityconnectors.framework.common.objects.AttributeDeltaUtil.getIntegerValue;
import static org.identityconnectors.framework.common.objects.AttributeDeltaUtil.getStringValue;
import static org.identityconnectors.framework.common.objects.AttributeDeltaUtil.isSpecial;
import static org.identityconnectors.framework.common.objects.AttributeDeltaUtil.namesEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link AttributeUtil} class.
 */
public class AttributeDeltaUtilTests {

    public void testGetStringValue() {
        assertThrows(ClassCastException.class, () -> {
            final String TEST_VALUE = "test value";
            // test normal..
            AttributeDelta attr = build("string", TEST_VALUE);
            String value = getStringValue(attr);
            assertEquals(value, TEST_VALUE);
            // test null..
            attr = build("stirng");
            value = getStringValue(attr);
            assertNull(value);
            // test exception
            attr = build("string", 1);
            getStringValue(attr);
        });
    }

    @Test
    public void testGetAsStringValue() {
        final String TEST_VALUE = "test value";
        // test normal
        AttributeDelta attr = build("string", TEST_VALUE);
        String value = getAsStringValue(attr);
        assertEquals(value, TEST_VALUE);
        // test null
        attr = build("stirng");
        value = getStringValue(attr);
        assertNull(value);
        // test w/ integer
        attr = build("string", 1);
        value = getAsStringValue(attr);
        assertEquals(value, "1");
    }

    public void testGetIntegerValue() {
        assertThrows(ClassCastException.class, () -> {
            final Integer TEST_VALUE = 1;
            // test normal
            AttributeDelta attr = build("int", TEST_VALUE);
            Integer value = getIntegerValue(attr);
            assertEquals(value, TEST_VALUE);
            // test null
            attr = build("int");
            value = getIntegerValue(attr);
            assertNull(value);
            // test class cast exception
            attr = build("int", "1");
            getIntegerValue(attr);
        });
    }

    public void testGetLongValue() {
        assertThrows(ClassCastException.class, () -> {
            final Long TEST_VALUE = 1L;
            // test normal
            AttributeDelta attr = build("long", TEST_VALUE);
            Long value = AttributeDeltaUtil.getLongValue(attr);
            assertEquals(value, TEST_VALUE);
            // test null
            attr = build("long");
            value = AttributeDeltaUtil.getLongValue(attr);
            assertNull(value);
            // test class cast exception
            attr = build("long", "1");
            AttributeDeltaUtil.getLongValue(attr);
        });
    }

    public void testBigDecimalValue() {
        assertThrows(ClassCastException.class, () -> {
            final BigDecimal TEST_VALUE = BigDecimal.ONE;
            // test normal
            AttributeDelta attr = build("big", TEST_VALUE);
            BigDecimal value = AttributeDeltaUtil.getBigDecimalValue(attr);
            assertEquals(value, TEST_VALUE);
            // test null
            attr = build("big");
            value = AttributeDeltaUtil.getBigDecimalValue(attr);
            assertNull(value);
            // test class cast exception
            attr = build("big", "1");
            AttributeDeltaUtil.getBigDecimalValue(attr);
        });
    }

    public void testGetSingleValue() {
        assertThrows(ClassCastException.class, () -> {
            final Object TEST_VALUE = 1L;
            // test normal
            AttributeDelta attr = build("long", TEST_VALUE);
            Object value = AttributeDeltaUtil.getSingleValue(attr);
            assertEquals(value, TEST_VALUE);
            // test null
            attr = build("long");
            value = AttributeDeltaUtil.getSingleValue(attr);
            assertNull(value);
            // test empty
            attr = build("long", Collections.emptyList());
            value = AttributeDeltaUtil.getSingleValue(attr);
            assertNull(value);
            // test illegal argument exception
            AttributeUtil.getSingleValue(AttributeBuilder.build("bob", 1, 2, 3));
        });
    }

    @Test
    public void testToMap() {
        AttributeDelta attr;
        Map<String, AttributeDelta> expected = new HashMap<>();
        attr = build("daf", "daf");
        expected.put(attr.getName(), attr);
        attr = build("fasdf", "fadsf3");
        expected.put(attr.getName(), attr);
        Map<String, AttributeDelta> actual = AttributeDeltaUtil.toMap(expected.values());
        assertEquals(actual, expected);
    }

    @Test
    public void testGetUidAttribute() {
        Set<AttributeDelta> attrs = new HashSet<>();
        AttributeDelta expected = build(Uid.NAME, "1");
        attrs.add(expected);
        AttributeDelta attr = build("bob");
        attrs.add(attr);
        AttributeDelta actual = AttributeDeltaUtil.getUidAttributeDelta(attrs);
        assertEquals(actual, expected);
    }

    @Test
    public void testGetBasicAttributes() {
        Set<AttributeDelta> set = new HashSet<>();
        set.add(build(Uid.NAME, "1"));
        AttributeDelta attr = AttributeDeltaBuilder.build("bob");
        set.add(attr);
        Set<AttributeDelta> actual = AttributeDeltaUtil.getBasicAttributes(set);
        Set<AttributeDelta> expected = new HashSet<>();
        expected.add(attr);
        assertEquals(actual, expected);
    }

    @Test
    public void testIsSpecial() {
        assertTrue(isSpecial(build(Uid.NAME, "1")));
        assertFalse(isSpecial(build("b")));
    }

    @Test
    public void testNamesEqual() {
        assertTrue(namesEqual("givenName", "givenname"));
    }

    @Test
    public void testIsMethod() {
        assertTrue(build("fad").is("Fad"));
        assertFalse(build("fadsf").is("f"));
    }

    @Test
    public void testFindMethod() {
        AttributeDelta expected = build("FIND_ME");
        Set<AttributeDelta> attrs = new HashSet<>();
        attrs.add(build("fadsf"));
        attrs.add(build("fadsfadsf"));
        attrs.add(expected);
        assertEquals(AttributeDeltaUtil.find("FIND_ME", attrs), expected);
        assertTrue(AttributeDeltaUtil.find("Daffff", attrs) == null);
    }

    @Test
    public void testEnableDate() {
        Date expected = new Date();
        Set<AttributeDelta> set = new HashSet<>();
        set.add(AttributeDeltaBuilder.buildEnableDate(expected));
        Date actual = AttributeDeltaUtil.getEnableDate(set);
        assertNotNull(actual);
        assertEquals(actual, expected);
    }
}
