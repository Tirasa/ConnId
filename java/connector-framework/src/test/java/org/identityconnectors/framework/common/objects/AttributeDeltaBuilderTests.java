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
 */
package org.identityconnectors.framework.common.objects;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        List<Object> expected = new ArrayList<Object>();
        expected.add(true);
        expected.add(false);
        assertEquals(attrDelta1.getValuesToReplace(), expected);

        testAttributesDelta(attrDelta1, attrDelta2, attrDelta3);
    }

    void testAttributesDelta(AttributeDelta attrDelta1, AttributeDelta attrDelta2, AttributeDelta attrDelta3) {
        assertEquals(attrDelta1, attrDelta2);
        assertEquals(attrDelta1, attrDelta1);
        assertNotNull(attrDelta1);

        Set<AttributeDelta> set = new HashSet<AttributeDelta>();
        set.add(attrDelta1);
        set.add(attrDelta2);
        set.add(attrDelta3);
        assertTrue(set.size() == 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void uidFromBuilderInteger() {
        AttributeDeltaBuilder.build(Uid.NAME, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void uidFromBuilderLong() {
        AttributeDeltaBuilder.build(Uid.NAME, 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void uidFromBuilderDouble() {
        AttributeDeltaBuilder.build(Uid.NAME, 1.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void mapNullAttributeDelta() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put(null, "NOK");

        AttributeDeltaBuilder.build("map", map);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void mapIntegerAttributeDelta() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "NOK");

        AttributeDeltaBuilder bld = new AttributeDeltaBuilder();
        bld.addValueToReplace(map);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("unchecked")
    public void mapShortAttributeDelta() {
        HashMap<Object, Object> map1 = new HashMap<Object, Object>();
        map1.put("string", "NOK");

        HashMap<Object, Object> map2 = new HashMap<Object, Object>();
        map2.put("map1", map1);
        map2.put("list", Arrays.asList(1, 2, 3, new Short("5")));

        HashMap<Object, Object> map3 = new HashMap<Object, Object>();
        map3.put("map2", map2);

        HashMap<Object, Object> map4 = new HashMap<Object, Object>();
        map4.put("map3", map3);

        AttributeDeltaBuilder.build("map", map4);
    }

    @Test
    public void mapAttributeDelta() {
        HashMap<Object, Object> map1 = new HashMap<Object, Object>();
        map1.put("string", "OK");

        HashMap<Object, Object> map2 = new HashMap<Object, Object>();
        map2.put("map1", map1);
        map2.put("list", Arrays.asList(1, 2, 3));

        HashMap<Object, Object> map3 = new HashMap<Object, Object>();
        map3.put("map2", map2);

        HashMap<Object, Object> map4 = new HashMap<Object, Object>();
        map4.put("map3", map3);

        AttributeDeltaBuilder.build("map", map4);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void conflictReplaceAndAddValues() {
        AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
        attrDelta1.setName("test");
        attrDelta1.addValueToReplace("jgs");
        attrDelta1.addValueToAdd("fda");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void conflictReplaceAndRemoveValues() {
        AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
        attrDelta1.setName("test");
        attrDelta1.addValueToReplace("jgs");
        attrDelta1.addValueToRemove("fda");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void conflictAddAndReplaceValues() {
        AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
        attrDelta1.setName("test");
        attrDelta1.addValueToAdd("fda");
        attrDelta1.addValueToReplace("jgs");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void conflictRemoveAndReplaceValues() {
        AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
        attrDelta1.setName("test");
        attrDelta1.addValueToRemove("fda");
        attrDelta1.addValueToReplace("jgs");
    }

    @Test
    public void buildAttrDeltaWithRemoveAndAddValues() {
        AttributeDeltaBuilder attrDelta1 = new AttributeDeltaBuilder();
        attrDelta1.setName("test");
        attrDelta1.addValueToRemove("fda");
        attrDelta1.addValueToAdd("jgs");
    }
}
