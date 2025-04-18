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
 * Portions Copyrighted 2014 ForgeRock AS.
 * Portions Copyrighted 2018 ConnId
 * Portions Copyrighted 2022 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class AttributeBuilderTests {

    @Test
    public void nullAttribute() {
        AttributeBuilder bld = new AttributeBuilder();
        bld.setName("adfaljk");
        assertNull(bld.build().getValue());
        assertNull(AttributeBuilder.build("fadsfd").getValue());
    }

    @Test
    public void buildBoolean() {
        AttributeBuilder bld = new AttributeBuilder();
        bld.setName("somename");
        bld.addValue(true, false);
        Attribute attr1 = bld.build();
        Attribute attr2 = bld.build();
        bld.addValue(false);
        Attribute attr3 = bld.build();

        List<Object> expected = new ArrayList<>();
        expected.add(true);
        expected.add(false);
        assertEquals(attr1.getValue(), expected);

        testAttributes(attr1, attr2, attr3);
    }

    void testAttributes(Attribute attr1, Attribute attr2, Attribute attr3) {
        assertEquals(attr1, attr2);
        assertEquals(attr1, attr1);
        assertFalse(attr1 == null);

        Set<Attribute> set = new HashSet<>();
        set.add(attr1);
        set.add(attr2);
        set.add(attr3);
        assertTrue(set.size() == 2);
    }

    public void uidFromBuilderInteger() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeBuilder.build(Uid.NAME, 1);
        });
    }

    public void uidFromBuilderLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeBuilder.build(Uid.NAME, 1L);
        });
    }

    public void uidFromBuilderDouble() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeBuilder.build(Uid.NAME, 1.0);
        });
    }

    public void nameFromBuilder() {
        assertThrows(IllegalArgumentException.class, () -> {
            // basic name tests..
            Name actual = (Name) AttributeBuilder.build(Name.NAME, "daf");
            assertEquals(actual, new Name("daf"));
            AttributeBuilder bld = new AttributeBuilder();
            bld.setName(Name.NAME);
            bld.addValue("stuff");
            actual = (Name) bld.build();
            assertEquals(actual, new Name("stuff"));
            // throw the exception at the end..
            AttributeBuilder.build(Name.NAME);
        });
    }

    public void mapNullAttribute() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<Object, Object> map = new HashMap<>();
            map.put(null, "NOK");

            AttributeBuilder.build("map", map);
        });
    }

    public void mapIntegerAttribute() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<Object, Object> map = new HashMap<>();
            map.put(1, "NOK");

            AttributeBuilder bld = new AttributeBuilder();
            bld.addValue(map);
        });
    }

    @SuppressWarnings("unchecked")
    public void mapShortAttribute() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<Object, Object> map1 = new HashMap<>();
            map1.put("string", "NOK");

            Map<Object, Object> map2 = new HashMap<>();
            map2.put("map1", map1);
            map2.put("list", Arrays.asList(1, 2, 3, Short.valueOf("5")));

            Map<Object, Object> map3 = new HashMap<>();
            map3.put("map2", map2);

            Map<Object, Object> map4 = new HashMap<>();
            map4.put("map3", map3);

            AttributeBuilder.build("map", map4);
        });
    }

    @Test
    public void mapAttribute() {
        Map<Object, Object> map1 = new HashMap<>();
        map1.put("string", "OK");

        Map<Object, Object> map2 = new HashMap<>();
        map2.put("map1", map1);
        map2.put("list", Arrays.asList(1, 2, 3));

        Map<Object, Object> map3 = new HashMap<>();
        map3.put("map2", map2);

        Map<Object, Object> map4 = new HashMap<>();
        map4.put("map3", map3);

        AttributeBuilder.build("map", map4);
    }

    // BASE-78
    @Test
    public void nullPasswordAttribute() {
        AttributeBuilder bld = new AttributeBuilder();
        bld.setName(OperationalAttributes.PASSWORD_NAME);
        Attribute attr = bld.build();
        assertEquals(OperationalAttributes.PASSWORD_NAME, attr.getName());
        assertNull(attr.getValue());
        assertNull(AttributeBuilder.build(OperationalAttributes.PASSWORD_NAME).getValue());
    }

    // Note: test for password attribute with actual GuardedString value has to be in GuardedStringTests
    // GuardedString.setEncryptor() method is package-private, therefore it cannot be initialized in this class.
}
