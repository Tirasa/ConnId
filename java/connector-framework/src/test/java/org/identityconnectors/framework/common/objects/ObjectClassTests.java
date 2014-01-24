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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.identityconnectors.framework.common.objects.LocaleTestUtil.resetLocaleCache;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Make sure to test various methods of the object class.
 */
public class ObjectClassTests {

    @BeforeMethod
    public void before() {
        resetLocaleCache();
    }

    @Test
    public void testIs() {
        ObjectClass actual = new ObjectClass("group");

        assertTrue(actual.is("group"));
        assertTrue(actual.is("Group"));
        assertFalse(actual.is("admin"));
    }

    @Test
    public void testEquals() {
        Object actual = new ObjectClass(ObjectClass.ACCOUNT_NAME);
        assertEquals(actual, ObjectClass.ACCOUNT);
        actual = new ObjectClass("babbo");
        assertFalse(actual.equals(ObjectClass.ACCOUNT));
        ObjectClass expected = new ObjectClass("babbo");
        assertEquals(actual, expected);

        // Test case-insensitivity
        ObjectClass lower = new ObjectClass("group");
        ObjectClass mixed = new ObjectClass("Group");
        assertEquals(lower, mixed);
    }

    @Test
    public void testHashCode() {
        Set<ObjectClass> set = new HashSet<ObjectClass>();
        set.add(ObjectClass.ACCOUNT);
        set.add(ObjectClass.GROUP);
        set.add(ObjectClass.ACCOUNT);
        assertThat(set).hasSize(2).contains(ObjectClass.ACCOUNT, ObjectClass.GROUP);

        // Test case-insensitivity
        set = new HashSet<ObjectClass>();
        set.add(new ObjectClass("group"));
        set.add(new ObjectClass("Group"));
        assertThat(set).hasSize(1);
    }

    @Test
    public void testEqualsObservesLocale() {
        Locale defLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            ObjectClass oc1 = new ObjectClass("i");
            ObjectClass oc2 = new ObjectClass("I");
            assertFalse(oc1.equals(oc2));
        } finally {
            Locale.setDefault(defLocale);
        }
    }

    @Test
    public void testHashCodeIndependentOnLocale() {
        Locale defLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            final ObjectClass attribute = new ObjectClass("i");
            final int hash1 = attribute.hashCode();
            Locale.setDefault(new Locale("tr"));
            int hash2 = attribute.hashCode();
            assertEquals(hash1, hash2);
        } finally {
            Locale.setDefault(defLocale);
        }
    }
}
