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

import static org.identityconnectors.framework.common.objects.LocaleTestUtil.resetLocaleCache;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ObjectClassInfoTests {

    @BeforeMethod
    public void before() {
        resetLocaleCache();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNoName() {
        new ObjectClassInfo(ObjectClass.ACCOUNT_NAME, new HashSet<AttributeInfo>(), false);
    }

    @Test
    public void testBuilderAddsName() {
        ObjectClassInfo o = new ObjectClassInfoBuilder().build();
        Map<String, AttributeInfo> map = AttributeInfoUtil.toMap(o.getAttributeInfo());
        assertTrue(map.containsKey(Name.NAME));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDuplicate() {
        ObjectClassInfoBuilder bld = new ObjectClassInfoBuilder();
        bld.addAttributeInfo(AttributeInfoBuilder.build("bob"));
        bld.addAttributeInfo(AttributeInfoBuilder.build("bob"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAllDuplicate() {
        ObjectClassInfoBuilder bld = new ObjectClassInfoBuilder();
        Set<AttributeInfo> set = new HashSet<AttributeInfo>();
        set.add(AttributeInfoBuilder.build("bob"));
        set.add(AttributeInfoBuilder.build("bob", int.class));
        bld.addAllAttributeInfo(set);
    }

    @Test
    public void testIs() {
        // Test type case-insensitivity
        ObjectClassInfoBuilder bld = new ObjectClassInfoBuilder();
        bld.addAttributeInfo(AttributeInfoBuilder.build("bob"));
        bld.setType("group");
        ObjectClassInfo oci = bld.build();

        assertTrue(oci.is("group"));
        assertTrue(oci.is("Group"));
        assertFalse(oci.is("admin"));
    }

    @Test
    public void testEquals() {
        // Test type case-insensitivity
        ObjectClassInfo oci_lower = build("group");
        ObjectClassInfo oci_upper = build("Group");

        assertEquals(oci_lower, oci_upper);
    }

    @Test
    public void testHashCode() {
        // Test type case-insensitivity
        ObjectClassInfo oci_lower = build("group");
        ObjectClassInfo oci_upper = build("Group");

        assertEquals(oci_lower.hashCode(), oci_upper.hashCode());
    }

    @Test
    public void testEqualsObservesLocale() {
        Locale defLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            ObjectClassInfo oci1 = build("i");
            ObjectClassInfo oci2 = build("I");
            assertFalse(oci1.equals(oci2));
        } finally {
            Locale.setDefault(defLocale);
        }
    }

    @Test
    public void testHashCodeIndependentOnLocale() {
        Locale defLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            final ObjectClassInfo attribute = build("i");
            final int hash1 = attribute.hashCode();
            Locale.setDefault(new Locale("tr"));
            int hash2 = attribute.hashCode();
            assertEquals(hash1, hash2);
        } finally {
            Locale.setDefault(defLocale);
        }
    }

    private static ObjectClassInfo build(String name) {
        ObjectClassInfoBuilder bld = new ObjectClassInfoBuilder();
        bld.addAttributeInfo(AttributeInfoBuilder.build("bob"));
        bld.setType(name);
        return bld.build();
    }
}
