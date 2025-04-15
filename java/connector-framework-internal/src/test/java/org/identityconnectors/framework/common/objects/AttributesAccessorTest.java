/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 ForgeRock AS. All rights reserved.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.GuardedByteArray;
import org.identityconnectors.common.security.GuardedString;
import org.junit.jupiter.api.Test;

public class AttributesAccessorTest {

    @Test
    public void testGetUid() throws Exception {
        assertEquals(getTestable().getUid(), new Uid("UID001"));
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(getTestable().getName(), new Name("NAME001"));
    }

    @Test
    public void testGetEnabled() throws Exception {
        assertFalse(getTestable().getEnabled(true));
    }

    @Test
    public void testGetPassword() throws Exception {
        assertEquals(getTestable().getPassword(),
                new GuardedString("Passw0rd".toCharArray()));
    }

    @Test
    public void testFindList() throws Exception {
        assertEquals(getTestable().findList("attributeFloatMultivalue"), CollectionUtil
                .newList(24F, 25F, null));
    }

    @Test
    public void testListAttributeNames() throws Exception {
        assertTrue(getTestable().listAttributeNames().contains("attributeboolean"));
    }

    @Test
    public void testHasAttribute() throws Exception {
        assertTrue(getTestable().hasAttribute("attributeboolean"));
    }

    @Test
    public void testFindString() throws Exception {
        assertEquals(getTestable().findString("attributeString"), "retipipiter");
    }

    @Test
    public void testFindStringList() throws Exception {
        assertEquals(getTestable().findStringList("attributeStringMultivalue"),
                CollectionUtil.newList("value1", "value2"));
    }

    @Test
    public void testFindCharacter() throws Exception {
        assertEquals((char) getTestable().findCharacter("attributechar"), 'a');
        assertEquals((char) getTestable().findCharacter("attributecharacter"), 'd');
    }

    @Test
    public void testFindInteger() throws Exception {
        assertEquals((int) getTestable().findInteger("attributeint"), 26);
        assertEquals((int) getTestable().findInteger("attributeinteger"), 29);
    }

    @Test
    public void testFindLong() throws Exception {
        assertEquals((long) getTestable().findLong("attributelongp"), 11L);
        assertEquals((long) getTestable().findLong("attributelong"), 14L);
    }

    @Test
    public void testFindDate() throws Exception {

    }

    @Test
    public void testFindDouble() throws Exception {
        assertEquals(getTestable().findDouble("attributedoublep"), Double.MIN_NORMAL, 0);
        assertEquals(getTestable().findDouble("attributedouble"), 17D, 0);
    }

    @Test
    public void testFindFloat() throws Exception {
        assertEquals(getTestable().findFloat("attributefloatp"), 20F, 0);
        assertEquals(getTestable().findFloat("attributefloat"), 23F, 0);
    }

    @Test
    public void testFindBoolean() throws Exception {
        assertTrue(getTestable().findBoolean("attributebooleanp"));
        assertFalse(getTestable().findBoolean("attributeboolean"));
    }

    @Test
    public void testFindByte() throws Exception {
        assertEquals((byte) getTestable().findByte("attributebytep"), (byte) 48);
        assertEquals((byte) getTestable().findByte("attributebyte"), (byte) 51);
    }

    @Test
    public void testFindByteArray() throws Exception {
        assertEquals(getTestable().findByteArray("attributeByteArray"), "array".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testFindBigDecimal() throws Exception {
        assertEquals(getTestable().findBigDecimal("attributeBigDecimal"), BigDecimal.ONE);
    }

    @Test
    public void testFindBigInteger() throws Exception {
        assertEquals(getTestable().findBigInteger("attributeBigInteger"), BigInteger.ONE);
    }

    @Test
    public void testFindGuardedByteArray() throws Exception {
        assertEquals(getTestable().findGuardedByteArray("attributeGuardedByteArray"),
                new GuardedByteArray("array".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testFindGuardedString() throws Exception {
        assertEquals(getTestable().findGuardedString("attributeGuardedString"),
                new GuardedString("secret".toCharArray()));
    }

    @Test
    public void testFindMap() throws Exception {
        assertEquals(getTestable().findMap("attributeMap"), getSampleMap());
    }

    private AttributesAccessor getTestable() {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new Uid("UID001"));
        attributes.add(new Name("NAME001"));
        attributes.add(AttributeBuilder.buildEnabled(false));
        attributes.add(AttributeBuilder.buildPassword("Passw0rd".toCharArray()));

        attributes.add(AttributeBuilder.build("attributeString", "retipipiter"));
        attributes.add(AttributeBuilder.build("attributeStringMultivalue", "value1", "value2"));

        attributes.add(AttributeBuilder.build("attributelongp", 11L));
        attributes.add(AttributeBuilder.build("attributelongpMultivalue", 12L, 13L));

        attributes.add(AttributeBuilder.build("attributeLong", 14L));
        attributes.add(AttributeBuilder.build("attributeLongMultivalue", 15L, 16L, null));

        attributes.add(AttributeBuilder.build("attributechar", 'a'));
        attributes.add(AttributeBuilder.build("attributecharMultivalue", 'b', 'c'));

        attributes.add(AttributeBuilder.build("attributeCharacter", 'd'));
        attributes.add(AttributeBuilder.build("attributeCharacterMultivalue", 'e', 'f', null));

        attributes.add(AttributeBuilder.build("attributedoublep", Double.MIN_NORMAL));
        attributes.add(AttributeBuilder.build("attributedoublepMultivalue", Double.MIN_VALUE, Double.MAX_VALUE));

        attributes.add(AttributeBuilder.build("attributeDouble", 17D));
        attributes.add(AttributeBuilder.build("attributeDoubleMultivalue", 18D, 19D, null));

        attributes.add(AttributeBuilder.build("attributefloatp", 20F));
        attributes.add(AttributeBuilder.build("attributefloatpMultivalue", 21F, 22F));

        attributes.add(AttributeBuilder.build("attributeFloat", 23F));
        attributes.add(AttributeBuilder.build("attributeFloatMultivalue", 24F, 25F, null));

        attributes.add(AttributeBuilder.build("attributeint", 26));
        attributes.add(AttributeBuilder.build("attributeintMultivalue", 27, 28));

        attributes.add(AttributeBuilder.build("attributeInteger", 29));
        attributes.add(AttributeBuilder.build("attributeIntegerMultivalue", 30, 31, null));

        attributes.add(AttributeBuilder.build("attributebooleanp", true));
        attributes.add(AttributeBuilder.build("attributebooleanpMultivalue", true, false));

        attributes.add(AttributeBuilder.build("attributeBoolean", false));
        attributes.add(AttributeBuilder.build("attributeBooleanMultivalue", true, false, null));

        attributes.add(AttributeBuilder.build("attributebytep", (byte) 48));
        attributes.add(AttributeBuilder.build("attributebytepMultivalue", (byte) 49, (byte) 50));

        attributes.add(AttributeBuilder.build("attributeByte", (byte) 0x51));
        attributes.add(AttributeBuilder.build("attributeByteMultivalue", (byte) 0x52, (byte) 0x53, null));

        attributes.add(AttributeBuilder.build("attributeByteArray", "array".getBytes(StandardCharsets.UTF_8)));
        attributes.add(AttributeBuilder.build("attributeByteArrayMultivalue", "item1"
                .getBytes(StandardCharsets.UTF_8), "item2".getBytes(StandardCharsets.UTF_8)));

        attributes.add(AttributeBuilder.build("attributeBigDecimal", BigDecimal.ONE));
        attributes.add(AttributeBuilder.build("attributeBigDecimalMultivalue", BigDecimal.ZERO,
                BigDecimal.TEN));

        attributes.add(AttributeBuilder.build("attributeBigInteger", BigInteger.ONE));
        attributes.add(AttributeBuilder.build("attributeBigIntegerMultivalue", BigInteger.ZERO,
                BigInteger.TEN));

        attributes.add(AttributeBuilder.build("attributeGuardedByteArray", new GuardedByteArray(
                "array".getBytes(StandardCharsets.UTF_8))));
        attributes.add(AttributeBuilder.build("attributeGuardedByteArrayMultivalue",
                new GuardedByteArray("item1".getBytes(StandardCharsets.UTF_8)),
                new GuardedByteArray("item2".getBytes(StandardCharsets.UTF_8))));

        attributes.add(AttributeBuilder.build("attributeGuardedString", new GuardedString("secret".toCharArray())));
        attributes.add(AttributeBuilder.build("attributeGuardedStringMultivalue",
                new GuardedString("secret1".toCharArray()), new GuardedString("secret2".toCharArray())));

        attributes.add(AttributeBuilder.build("attributeMap", getSampleMap()));
        attributes.add(AttributeBuilder.build("attributeMapMultivalue", getSampleMap(), getSampleMap()));
        return new AttributesAccessor(attributes);
    }

    private Map<String, Object> getSampleMap() {
        Map<String, Object> ret =
                CollectionUtil.newMap("string", "String", "number", 43, "trueOrFalse", true,
                        "nullValue", null, "collection", CollectionUtil.newList("item1", "item2"));
        ret.put("object", CollectionUtil.newMap("key1", "value1", "key2", "value2"));
        return ret;
    }
}
