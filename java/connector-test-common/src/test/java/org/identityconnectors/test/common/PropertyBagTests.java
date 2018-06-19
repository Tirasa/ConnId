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
package org.identityconnectors.test.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PropertyBagTests {

    private final PropertyBag bag;

    public PropertyBagTests() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", null);
        map.put("key3", 1);
        map.put("key5", 1L);
        bag = new PropertyBag(map);
    }

    @Test
    public void testGetProperty() {
        assertEquals(bag.getProperty("key1", String.class), "value1");
        assertNull(bag.getProperty("key2", String.class));
        assertEquals(bag.getProperty("key3", Integer.class), Integer.valueOf(1));
        assertEquals(bag.getProperty("key5", Long.class), Long.valueOf(1));

        // try not existing
        try {
            bag.getProperty("key4", String.class);
            fail("Get Property must fail for unexisting property");
        } catch (IllegalArgumentException e) {
            /* ignore */
        }

        // Try cast
        try {
            bag.getProperty("key3", Long.class);
            fail("Get Property with incompatible type must fail on ClassCastException");
        } catch (ClassCastException e) {
            /* ignore */
        }

    }

    @Test
    public void testGetPropertyWithDef() {
        assertEquals(bag.getProperty("key1", String.class, "def"), "value1");
        assertNull(bag.getProperty("key2", String.class, "def"));
        assertEquals(bag.getProperty("key4", String.class, "def"), "def");
        assertNull(bag.getProperty("key4", String.class, null));
    }

    @Test
    public void testGetStringProperty() {
        assertEquals(bag.getStringProperty("key1"), "value1");
        assertNull(bag.getStringProperty("key2"));
        // Try cast
        try {
            bag.getStringProperty("key3");
            fail("Get Property with incompatible type must fail on ClassCastException");
        } catch (ClassCastException e) {
            /* ignore */
        }

    }
}
