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
package org.identityconnectors.framework.common.objects;

import static org.identityconnectors.framework.common.objects.AttributeBuilder.build;
import static org.identityconnectors.framework.common.objects.AttributeUtil.getAsStringValue;
import static org.identityconnectors.framework.common.objects.AttributeUtil.getIntegerValue;
import static org.identityconnectors.framework.common.objects.AttributeUtil.getStringValue;
import static org.identityconnectors.framework.common.objects.AttributeUtil.isSpecial;
import static org.identityconnectors.framework.common.objects.AttributeUtil.namesEqual;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

/**
 * Tests the {@link AttributeUtil} class.
 */
public class AttributeUtilTests {

    @Test(expectedExceptions = ClassCastException.class)
    public void testGetStringValue() {
        final String TEST_VALUE = "test value";
        // test normal..
        Attribute attr = build("string", TEST_VALUE);
        String value = getStringValue(attr);
        assertEquals(value, TEST_VALUE);
        // test null..
        attr = build("stirng");
        value = getStringValue(attr);
        assertNull(value);
        // test exception
        attr = build("string", 1);
        getStringValue(attr);
    }

    @Test
    public void testGetAsStringValue() {
        final String TEST_VALUE = "test value";
        // test normal
        Attribute attr = build("string", TEST_VALUE);
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

    @Test(expectedExceptions = ClassCastException.class)
    public void testGetIntegerValue() {
        final Integer TEST_VALUE = 1;
        // test normal
        Attribute attr = build("int", TEST_VALUE);
        Integer value = getIntegerValue(attr);
        assertEquals(value, TEST_VALUE);
        // test null
        attr = build("int");
        value = getIntegerValue(attr);
        assertNull(value);
        // test class cast exception
        attr = build("int", "1");
        value = getIntegerValue(attr);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testGetLongValue() {
        final Long TEST_VALUE = 1L;
        // test normal
        Attribute attr = AttributeBuilder.build("long", TEST_VALUE);
        Long value = AttributeUtil.getLongValue(attr);
        assertEquals(value, TEST_VALUE);
        // test null
        attr = AttributeBuilder.build("long");
        value = AttributeUtil.getLongValue(attr);
        assertNull(value);
        // test class cast exception
        attr = AttributeBuilder.build("long", "1");
        value = AttributeUtil.getLongValue(attr);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testBigDecimalValue() {
        final BigDecimal TEST_VALUE = BigDecimal.ONE;
        // test normal
        Attribute attr = AttributeBuilder.build("big", TEST_VALUE);
        BigDecimal value = AttributeUtil.getBigDecimalValue(attr);
        assertEquals(value, TEST_VALUE);
        // test null
        attr = AttributeBuilder.build("big");
        value = AttributeUtil.getBigDecimalValue(attr);
        assertNull(value);
        // test class cast exception
        attr = AttributeBuilder.build("big", "1");
        value = AttributeUtil.getBigDecimalValue(attr);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetSingleValue() {
        final Object TEST_VALUE = 1L;
        // test normal
        Attribute attr = AttributeBuilder.build("long", TEST_VALUE);
        Object value = AttributeUtil.getSingleValue(attr);
        assertEquals(value, TEST_VALUE);
        // test null
        attr = AttributeBuilder.build("long");
        value = AttributeUtil.getSingleValue(attr);
        assertNull(value);
        // test empty
        attr = AttributeBuilder.build("long", Collections.emptyList());
        value = AttributeUtil.getSingleValue(attr);
        assertNull(value);
        // test illegal argument exception
        AttributeUtil.getSingleValue(AttributeBuilder.build("bob", 1, 2, 3));
    }

    @Test
    public void testToMap() {
        Attribute attr;
        Map<String, Attribute> expected = new HashMap<String, Attribute>();
        attr = build("daf", "daf");
        expected.put(attr.getName(), attr);
        attr = build("fasdf", "fadsf3");
        expected.put(attr.getName(), attr);
        Map<String, Attribute> actual = AttributeUtil.toMap(expected.values());
        assertEquals(actual, expected);
    }

    @Test
    public void testGetUidAttribute() {
        Set<Attribute> attrs = new HashSet<Attribute>();
        Uid expected = new Uid("1");
        attrs.add(expected);
        Attribute attr = AttributeBuilder.build("bob");
        attrs.add(attr);
        Uid actual = AttributeUtil.getUidAttribute(attrs);
        assertEquals(actual, expected);
    }

    @Test
    public void testGetBasicAttributes() {
        Set<Attribute> set = new HashSet<Attribute>();
        set.add(new Uid("1"));
        Attribute attr = AttributeBuilder.build("bob");
        set.add(attr);
        Set<Attribute> actual = AttributeUtil.getBasicAttributes(set);
        Set<Attribute> expected = new HashSet<Attribute>();
        expected.add(attr);
        assertEquals(actual, expected);
    }

    @Test
    public void testIsSpecial() {
        assertTrue(isSpecial(new Uid("1")));
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
        Attribute expected = AttributeBuilder.build("FIND_ME");
        Set<Attribute> attrs = new HashSet<Attribute>();
        attrs.add(build("fadsf"));
        attrs.add(build("fadsfadsf"));
        attrs.add(expected);
        assertEquals(AttributeUtil.find("FIND_ME", attrs), expected);
        assertTrue(AttributeUtil.find("Daffff", attrs) == null);
    }

    @Test
    public void testEnableDate() {
        Date expected = new Date();
        Set<Attribute> set = new HashSet<Attribute>();
        set.add(AttributeBuilder.buildEnableDate(expected));
        Date actual = AttributeUtil.getEnableDate(set);
        assertNotNull(actual);
        assertEquals(actual, expected);
    }
}
