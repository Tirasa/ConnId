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
 */

package org.identityconnectors.framework.common.objects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.GuardedByteArray;
import org.identityconnectors.common.security.GuardedString;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AttributesAccessorTest {


    @Test
    public void testGetUid() throws Exception {
        Assert.assertEquals(getTestable().getUid(), new Uid("UID001"));
    }

    @Test
    public void testGetName() throws Exception {
        Assert.assertEquals(getTestable().getName(), new Name("NAME001"));
    }

    @Test
    public void testGetEnabled() throws Exception {
        Assert.assertFalse(getTestable().getEnabled(true));
    }

    @Test
    public void testGetPassword() throws Exception {
        Assert.assertEquals(getTestable().getPassword(),
                new GuardedString("Passw0rd".toCharArray()));
    }

    @Test
    public void testFindList() throws Exception {
        Assert.assertEquals(getTestable().findList("attributeFloatMultivalue"), CollectionUtil
                .newList(24F, 25F, null));
    }

    @Test
    public void testListAttributeNames() throws Exception {
        Assert.assertTrue(getTestable().listAttributeNames().contains("attributeboolean"));
    }

    @Test
    public void testHasAttribute() throws Exception {
        Assert.assertTrue(getTestable().hasAttribute("attributeboolean"));
    }

    @Test
    public void testFindString() throws Exception {
        Assert.assertEquals(getTestable().findString("attributeString"), "retipipiter");
    }

    @Test
    public void testFindStringList() throws Exception {
        Assert.assertEquals(getTestable().findStringList("attributeStringMultivalue"),
                CollectionUtil.newList("value1", "value2"));
    }

    @Test
    public void testFindCharacter() throws Exception {
        Assert.assertEquals((char) getTestable().findCharacter("attributechar"), 'a');
        Assert.assertEquals((char) getTestable().findCharacter("attributecharacter"), 'd');
    }

    @Test
    public void testFindInteger() throws Exception {
        Assert.assertEquals((int) getTestable().findInteger("attributeint"), 26);
        Assert.assertEquals((int) getTestable().findInteger("attributeinteger"), 29);
    }

    @Test
    public void testFindLong() throws Exception {
        Assert.assertEquals((long) getTestable().findLong("attributelongp"), 11L);
        Assert.assertEquals((long) getTestable().findLong("attributelong"), 14L);
    }

    @Test
    public void testFindDate() throws Exception {

    }

    @Test
    public void testFindDouble() throws Exception {
        Assert.assertEquals(getTestable().findDouble("attributedoublep"), Double.MIN_NORMAL);
        Assert.assertEquals(getTestable().findDouble("attributedouble"), 17D);
    }

    @Test
    public void testFindFloat() throws Exception {
        Assert.assertEquals( getTestable().findFloat("attributefloatp"), 20F);
        Assert.assertEquals( getTestable().findFloat("attributefloat"), 23F);
    }

    @Test
    public void testFindBoolean() throws Exception {
        Assert.assertTrue( getTestable().findBoolean("attributebooleanp"));
        Assert.assertFalse( getTestable().findBoolean("attributeboolean"));
    }

    @Test
    public void testFindByte() throws Exception {
        Assert.assertEquals((byte) getTestable().findByte("attributebytep"), (byte)48);
        Assert.assertEquals((byte) getTestable().findByte("attributebyte"), (byte)51);
    }

    @Test
    public void testFindByteArray() throws Exception {
        Assert.assertEquals( getTestable().findByteArray("attributeByteArray"), "array".getBytes(Charset.forName("UTF-8")));
    }

    @Test
    public void testFindBigDecimal() throws Exception {
        Assert.assertEquals( getTestable().findBigDecimal("attributeBigDecimal"), BigDecimal.ONE);
    }

    @Test
    public void testFindBigInteger() throws Exception {
        Assert.assertEquals( getTestable().findBigInteger("attributeBigInteger"), BigInteger.ONE);
    }

    @Test
    public void testFindGuardedByteArray() throws Exception {
        Assert.assertEquals( getTestable().findGuardedByteArray("attributeGuardedByteArray"), new GuardedByteArray("array".getBytes(Charset.forName("UTF-8"))));
    }

    @Test
    public void testFindGuardedString() throws Exception {
        Assert.assertEquals( getTestable().findGuardedString("attributeGuardedString"), new GuardedString("secret".toCharArray()));
    }

    @Test
    public void testFindMap() throws Exception {
        Assert.assertEquals( getTestable().findMap("attributeMap"), getSampleMap());
    }

    private AttributesAccessor getTestable() {
        Set<Attribute> attributes = new HashSet<Attribute>();
        attributes.add(new Uid("UID001"));
        attributes.add(new Name("NAME001"));
        attributes.add(AttributeBuilder.buildEnabled(false));
        attributes.add(AttributeBuilder.buildPassword("Passw0rd".toCharArray()));

        attributes.add(AttributeBuilder.build("attributeString", "retipipiter"));
        attributes.add(AttributeBuilder.build("attributeStringMultivalue", "value1", "value2"));

        attributes.add(AttributeBuilder.build("attributelongp", 11L));
        attributes.add(AttributeBuilder.build("attributelongpMultivalue", 12L, 13L));

        attributes.add(AttributeBuilder.build("attributeLong", new Long(14L)));
        attributes.add(AttributeBuilder.build("attributeLongMultivalue", Long.valueOf(15), Long
                .valueOf(16), null));

        attributes.add(AttributeBuilder.build("attributechar", 'a'));
        attributes.add(AttributeBuilder.build("attributecharMultivalue", 'b', 'c'));

        attributes.add(AttributeBuilder.build("attributeCharacter", new Character('d')));
        attributes.add(AttributeBuilder.build("attributeCharacterMultivalue", new Character('e'),
                new Character('f'), null));

        attributes.add(AttributeBuilder.build("attributedoublep", Double.MIN_NORMAL));
        attributes.add(AttributeBuilder.build("attributedoublepMultivalue", Double.MIN_VALUE,
                Double.MAX_VALUE));

        attributes.add(AttributeBuilder.build("attributeDouble", new Double(17D)));
        attributes.add(AttributeBuilder.build("attributeDoubleMultivalue", new Double(18D),
                new Double(19D), null));

        attributes.add(AttributeBuilder.build("attributefloatp", 20F));
        attributes.add(AttributeBuilder.build("attributefloatpMultivalue", 21F, 22F));

        attributes.add(AttributeBuilder.build("attributeFloat", new Float(23F)));
        attributes.add(AttributeBuilder.build("attributeFloatMultivalue", new Float(24F),
                new Float(25F), null));

        attributes.add(AttributeBuilder.build("attributeint", 26));
        attributes.add(AttributeBuilder.build("attributeintMultivalue", 27, 28));

        attributes.add(AttributeBuilder.build("attributeInteger", new Integer(29)));
        attributes.add(AttributeBuilder.build("attributeIntegerMultivalue", new Integer(30),
                new Integer(31), null));

        attributes.add(AttributeBuilder.build("attributebooleanp", true));
        attributes.add(AttributeBuilder.build("attributebooleanpMultivalue", true, false));

        attributes.add(AttributeBuilder.build("attributeBoolean", Boolean.valueOf(false)));
        attributes.add(AttributeBuilder.build("attributeBooleanMultivalue", Boolean.valueOf(true),
                Boolean.valueOf(false), null));

        attributes.add(AttributeBuilder.build("attributebytep", (byte) 48));
        attributes.add(AttributeBuilder.build("attributebytepMultivalue", (byte) 49, (byte) 50));

        attributes.add(AttributeBuilder.build("attributeByte", new Byte((byte) 51)));
        attributes.add(AttributeBuilder.build("attributeByteMultivalue", new Byte((byte) 52),
                new Byte((byte) 53), null));

        attributes.add(AttributeBuilder.build("attributeByteArray", "array".getBytes(Charset
                .forName("UTF-8"))));
        attributes.add(AttributeBuilder.build("attributeByteArrayMultivalue", "item1"
                .getBytes(Charset.forName("UTF-8")), "item2".getBytes(Charset.forName("UTF-8"))));

        attributes.add(AttributeBuilder.build("attributeBigDecimal", BigDecimal.ONE));
        attributes.add(AttributeBuilder.build("attributeBigDecimalMultivalue", BigDecimal.ZERO,
                BigDecimal.TEN));

        attributes.add(AttributeBuilder.build("attributeBigInteger", BigInteger.ONE));
        attributes.add(AttributeBuilder.build("attributeBigIntegerMultivalue", BigInteger.ZERO,
                BigInteger.TEN));

        attributes.add(AttributeBuilder.build("attributeGuardedByteArray", new GuardedByteArray(
                "array".getBytes(Charset.forName("UTF-8")))));
        attributes.add(AttributeBuilder.build("attributeGuardedByteArrayMultivalue",
                new GuardedByteArray("item1".getBytes(Charset.forName("UTF-8"))),
                new GuardedByteArray("item2".getBytes(Charset.forName("UTF-8")))));

        attributes.add(AttributeBuilder.build("attributeGuardedString", new GuardedString("secret"
                .toCharArray())));
        attributes.add(AttributeBuilder.build("attributeGuardedStringMultivalue",
                new GuardedString("secret1".toCharArray()), new GuardedString("secret2"
                        .toCharArray())));

        attributes.add(AttributeBuilder.build("attributeMap", getSampleMap()));
        attributes.add(AttributeBuilder.build("attributeMapMultivalue", getSampleMap(),
                getSampleMap()));
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
