/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Evolveum. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttributeDeltaTests {

    @BeforeEach
    public void before() {
        LocaleTestUtil.resetLocaleCache();
    }

    /**
     * Test the case insensitivity.
     */
    @Test
    void name() {
        AttributeDelta actual = AttributeDeltaBuilder.build("Jack");
        assertEquals(actual, AttributeDeltaBuilder.build("jacK"));
        assertTrue(actual.is("JacK"));
    }

    @Test
    void arrays() {
        List<byte[]> values1 = new ArrayList<>();
        values1.add(new byte[] { 0, 1 });
        List<byte[]> values2 = new ArrayList<>();
        values2.add(new byte[] { 0, 1 });
        //test build attributeDelta with valuesToReplace
        AttributeDelta attribute1 = AttributeDeltaBuilder.build("test", values1);
        AttributeDelta attribute2 = AttributeDeltaBuilder.build("test", values2);
        assertEquals(attribute1, attribute2, "test failed with valuesToReplace");
        //test build attributeDelta with valuesToAdd and null for valueToRemove
        attribute1 = AttributeDeltaBuilder.build("test", values1, null);
        attribute2 = AttributeDeltaBuilder.build("test", values2, null);
        assertEquals(attribute1, attribute2, "test failed with valuesToAdd");
        //test build attributeDelta with valueToRemove and null for valueToAdd
        attribute1 = AttributeDeltaBuilder.build("test", null, values1);
        attribute2 = AttributeDeltaBuilder.build("test", null, values2);
        assertEquals(attribute1, attribute2, "test failed with valueToRemove");
        //test build attributeDelta with valueToAdd and valueToRemove
        attribute1 = AttributeDeltaBuilder.build("test", values1, values1);
        attribute2 = AttributeDeltaBuilder.build("test", values2, values2);
        assertEquals(attribute1, attribute2, "test failed with valueToAdd and valueToRemove");
    }

    @Test
    void normal() {
        assertEquals(AttributeDeltaBuilder.build("test", 1, 2, 4), AttributeDeltaBuilder.build("test", 1, 2, 4));
        assertFalse(AttributeDeltaBuilder.build("test", 1, 2, 4).equals(AttributeDeltaBuilder.build("test", 2, 4)));
    }

    @Test
    void equalsObservesLocale() {
        Locale defLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.of("tr"));
            AttributeDelta attribute1 = AttributeDeltaBuilder.build("i");
            AttributeDelta attribute2 = AttributeDeltaBuilder.build("I");
            assertFalse(attribute1.equals(attribute2));
        } finally {
            Locale.setDefault(defLocale);
        }
    }

    @Test
    void hashCodeIndependentOnLocale() {
        Locale defLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            AttributeDelta attribute = AttributeDeltaBuilder.build("i");
            int hash1 = attribute.hashCode();
            Locale.setDefault(Locale.of("tr"));
            int hash2 = attribute.hashCode();
            assertEquals(hash1, hash2);
        } finally {
            Locale.setDefault(defLocale);
        }
    }

    @Test
    void isseBASE104() {
        Attribute current = AttributeBuilder.build("description");
        AttributeDelta delta = AttributeDeltaBuilder.build("description", "new");

        Attribute updated = delta.applyTo(current);
        assertEquals("description", updated.getName());
        assertEquals(List.of("new"), updated.getValue());
    }
}
