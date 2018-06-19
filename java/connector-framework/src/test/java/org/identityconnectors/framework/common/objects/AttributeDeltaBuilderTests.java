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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class AttributeDeltaBuilderTests {

    @Test
    public void nullAttributeDelta() {
        AttributeDeltaBuilder bld = new AttributeDeltaBuilder();
        bld.setName("ljhgf");
        assertNull(bld.build().getValuesToReplace());
        assertNull(bld.build().getValuesToAdd());
        assertNull(bld.build().getValuesToRemove());
        assertNull(AttributeDeltaBuilder.build("fadsfd").getValuesToReplace());
        assertNull(AttributeDeltaBuilder.build("fadsfd").getValuesToAdd());
        assertNull(AttributeDeltaBuilder.build("fadsfd").getValuesToRemove());
    }

    @Test
    public void buildBoolean() {
        AttributeDeltaBuilder bld = new AttributeDeltaBuilder();
        bld.setName("Taumatawha-katangihangako-auauotamateaturi-pukakapikimaun-gahoronuku-pokaiwhenu-akitanatahu");
        bld.addValueToReplace(true, false);
        AttributeDelta attrDelta1 = bld.build();
        AttributeDelta attrDelta2 = bld.build();
        bld.addValueToReplace(false);
        AttributeDelta attrDelta3 = bld.build();

        List<Object> expected = new ArrayList<>();
        expected.add(true);
        expected.add(false);
        assertEquals(attrDelta1.getValuesToReplace(), expected);

        testAttributesDelta(attrDelta1, attrDelta2, attrDelta3);
    }

    void testAttributesDelta(AttributeDelta attrDelta1, AttributeDelta attrDelta2, AttributeDelta attrDelta3) {
        assertEquals(attrDelta1, attrDelta2);
        assertEquals(attrDelta1, attrDelta1);
        assertNotNull(attrDelta1);

        Set<AttributeDelta> set = new HashSet<>();
        set.add(attrDelta1);
        set.add(attrDelta2);
        set.add(attrDelta3);
        assertEquals(2, set.size());
    }

    public void uidFromBuilderInteger() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeDeltaBuilder.build(Uid.NAME, 1);
        });
    }

    public void uidFromBuilderLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeDeltaBuilder.build(Uid.NAME, 1L);
        });
    }

    public void uidFromBuilderDouble() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeDeltaBuilder.build(Uid.NAME, 1.0);
        });
    }

    public void mapNullAttributeDelta() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<Object, Object> map = new HashMap<>();
            map.put(null, "NOK");

            AttributeDeltaBuilder.build("map", map);
        });
    }

    public void mapIntegerAttributeDelta() {
        assertThrows(IllegalArgumentException.class, () -> {
            Map<Object, Object> map = new HashMap<>();
            map.put(1, "NOK");

            AttributeDeltaBuilder bld = new AttributeDeltaBuilder();
            bld.addValueToReplace(map);
        });
    }

    @SuppressWarnings("unchecked")
    public void mapShortAttributeDelta() {
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

            AttributeDeltaBuilder.build("map", map4);
        });
    }

    @Test
    public void mapAttributeDelta() {
        Map<Object, Object> map1 = new HashMap<>();
        map1.put("string", "OK");

        Map<Object, Object> map2 = new HashMap<>();
        map2.put("map1", map1);
        map2.put("list", Arrays.asList(1, 2, 3));

        Map<Object, Object> map3 = new HashMap<>();
        map3.put("map2", map2);

        Map<Object, Object> map4 = new HashMap<>();
        map4.put("map3", map3);

        AttributeDeltaBuilder.build("map", map4);
    }

    public void conflictReplaceAndAddValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
            attrDelta1.setName("test");
            attrDelta1.addValueToReplace("jgs");
            attrDelta1.addValueToAdd("fda");
        });
    }

    public void conflictReplaceAndRemoveValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
            attrDelta1.setName("test");
            attrDelta1.addValueToReplace("jgs");
            attrDelta1.addValueToRemove("fda");
        });
    }

    public void conflictAddAndReplaceValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
            attrDelta1.setName("test");
            attrDelta1.addValueToAdd("fda");
            attrDelta1.addValueToReplace("jgs");
        });
    }

    public void conflictRemoveAndReplaceValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
            attrDelta1.setName("test");
            attrDelta1.addValueToRemove("fda");
            attrDelta1.addValueToReplace("jgs");
        });
    }

    @Test
    public void buildAttrDeltaWithRemoveAndAddValues() {
        AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
        attrDelta1.setName("test");
        attrDelta1.addValueToRemove("fda");
        attrDelta1.addValueToAdd("jgs");
    }
}
