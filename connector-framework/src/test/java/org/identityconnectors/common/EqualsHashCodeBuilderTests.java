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
package org.identityconnectors.common;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EqualsHashCodeBuilderTests {

    @Test
    public void testObject() {
        TestObject o1 = new TestObject(2);
        TestObject o2 = new TestObject(2);
        TestObject o3 = new TestObject(3);
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testLong() {
        long o1 = 1L;
        long o2 = 1L;
        long o3 = 2L;
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testInt() {
        int o1 = 1;
        int o2 = 1;
        int o3 = 3;
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testShort() {
        short o1 = 1;
        short o2 = 1;
        short o3 = 3;
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testChar() {
        char o1 = 'a';
        char o2 = 'a';
        char o3 = 'c';
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testByte() {
        byte o1 = 1;
        byte o2 = 1;
        byte o3 = 3;
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testDouble() {
        double o1 = 1;
        double o2 = 1;
        double o3 = 2;
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testFloat() {
        float o1 = 1;
        float o2 = 1;
        float o3 = 2;
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testBoolean() {
        boolean o1 = true;
        boolean o2 = true;
        boolean o3 = false;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testObjectArray() {
        TestObject[] o1 = new TestObject[3];
        o1[0] = new TestObject(4);
        o1[1] = new TestObject(5);
        o1[2] = null;
        TestObject[] o2 = new TestObject[3];
        o2[0] = new TestObject(4);
        o2[1] = new TestObject(5);
        o2[2] = null;
        TestObject[] o3 = new TestObject[3];
        o3[0] = new TestObject(5);
        o3[1] = new TestObject(5);
        o3[2] = null;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testLongArray() {
        long[] o1 = new long[2];
        o1[0] = 5L;
        o1[1] = 6L;
        long[] o2 = new long[2];
        o2[0] = 5L;
        o2[1] = 6L;
        long[] o3 = new long[2];
        o3[0] = 6L;
        o3[1] = 6L;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testIntArray() {
        int[] o1 = new int[2];
        o1[0] = 5;
        o1[1] = 6;
        int[] o2 = new int[2];
        o2[0] = 5;
        o2[1] = 6;
        int[] o3 = new int[2];
        o3[0] = 6;
        o3[1] = 6;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testShortArray() {
        short[] o1 = new short[2];
        o1[0] = 5;
        o1[1] = 6;
        short[] o2 = new short[2];
        o2[0] = 5;
        o2[1] = 6;
        short[] o3 = new short[2];
        o3[0] = 6;
        o3[1] = 6;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testCharArray() {
        char[] o1 = new char[2];
        o1[0] = 5;
        o1[1] = 6;
        char[] o2 = new char[2];
        o2[0] = 5;
        o2[1] = 6;
        char[] o3 = new char[2];
        o3[0] = 6;
        o3[1] = 6;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testByteArray() {
        byte[] o1 = new byte[2];
        o1[0] = 5;
        o1[1] = 6;
        byte[] o2 = new byte[2];
        o2[0] = 5;
        o2[1] = 6;
        byte[] o3 = new byte[2];
        o3[0] = 6;
        o3[1] = 6;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testDoubleArray() {
        double[] o1 = new double[2];
        o1[0] = 5;
        o1[1] = 6;
        double[] o2 = new double[2];
        o2[0] = 5;
        o2[1] = 6;
        double[] o3 = new double[2];
        o3[0] = 6;
        o3[1] = 6;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testFloatArray() {
        float[] o1 = new float[2];
        o1[0] = 5;
        o1[1] = 6;
        float[] o2 = new float[2];
        o2[0] = 5;
        o2[1] = 6;
        float[] o3 = new float[2];
        o3[0] = 6;
        o3[1] = 6;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testBooleanArray() {
        boolean[] o1 = new boolean[2];
        o1[0] = true;
        o1[1] = false;
        boolean[] o2 = new boolean[2];
        o2[0] = true;
        o2[1] = false;
        boolean[] o3 = new boolean[2];
        o3[0] = false;
        o3[1] = false;

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testObjectArrayHiddenByObject() {
        TestObject[] array1 = new TestObject[2];
        array1[0] = new TestObject(4);
        array1[1] = new TestObject(5);
        TestObject[] array2 = new TestObject[2];
        array2[0] = new TestObject(4);
        array2[1] = new TestObject(5);
        TestObject[] array3 = new TestObject[2];
        array3[0] = new TestObject(5);
        array3[1] = new TestObject(5);
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testLongArrayHiddenByObject() {
        long[] array1 = new long[2];
        array1[0] = 5L;
        array1[1] = 6L;
        long[] array2 = new long[2];
        array2[0] = 5L;
        array2[1] = 6L;
        long[] array3 = new long[2];
        array3[0] = 6L;
        array3[1] = 6L;
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testIntArrayHiddenByObject() {
        int[] array1 = new int[2];
        array1[0] = 5;
        array1[1] = 6;
        int[] array2 = new int[2];
        array2[0] = 5;
        array2[1] = 6;
        int[] array3 = new int[2];
        array3[0] = 6;
        array3[1] = 6;
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testShortArrayHiddenByObject() {
        short[] array1 = new short[2];
        array1[0] = 5;
        array1[1] = 6;
        short[] array2 = new short[2];
        array2[0] = 5;
        array2[1] = 6;
        short[] array3 = new short[2];
        array3[0] = 6;
        array3[1] = 6;
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testCharArrayHiddenByObject() {
        char[] array1 = new char[2];
        array1[0] = 5;
        array1[1] = 6;
        char[] array2 = new char[2];
        array2[0] = 5;
        array2[1] = 6;
        char[] array3 = new char[2];
        array3[0] = 6;
        array3[1] = 6;
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testByteArrayHiddenByObject() {
        byte[] array1 = new byte[2];
        array1[0] = 5;
        array1[1] = 6;
        byte[] array2 = new byte[2];
        array2[0] = 5;
        array2[1] = 6;
        byte[] array3 = new byte[2];
        array3[0] = 6;
        array3[1] = 6;
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testDoubleArrayHiddenByObject() {
        double[] array1 = new double[2];
        array1[0] = 5;
        array1[1] = 6;
        double[] array2 = new double[2];
        array2[0] = 5;
        array2[1] = 6;
        double[] array3 = new double[2];
        array3[0] = 6;
        array3[1] = 6;
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testFloatArrayHiddenByObject() {
        float[] array1 = new float[2];
        array1[0] = 5;
        array1[1] = 6;
        float[] array2 = new float[2];
        array2[0] = 5;
        array2[1] = 6;
        float[] array3 = new float[2];
        array3[0] = 6;
        array3[1] = 6;
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testBooleanArrayHiddenByObject() {
        boolean[] array1 = new boolean[2];
        array1[0] = true;
        array1[1] = false;
        boolean[] array2 = new boolean[2];
        array2[0] = true;
        array2[1] = false;
        boolean[] array3 = new boolean[2];
        array3[0] = false;
        array3[1] = false;
        Object o1 = array1;
        Object o2 = array2;
        Object o3 = array3;
        xtestHiddenByObject(o1, o2, o3);
    }

    @Test
    public void testUnrelatedClasses() {
        Object[] x = new Object[] { new TestACanEqualB(1) };
        Object[] y = new Object[] { new TestBCanEqualA(1) };
        Object[] z = new Object[] { new TestBCanEqualA(2) };

        // sanity checks:
        assertTrue(Arrays.equals(x, x));
        assertTrue(Arrays.equals(y, y));
        assertTrue(Arrays.equals(x, y));
        assertTrue(Arrays.equals(y, x));
        // real tests:
        assertTrue(x[0].equals(x[0]));
        assertTrue(y[0].equals(y[0]));
        assertTrue(x[0].equals(y[0]));
        assertTrue(y[0].equals(x[0]));

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(x);
        eq2.append(y);
        eq3.append(z);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
    }

    @Test
    public void testList() {
        List<String> o1 = new ArrayList<String>();
        List<String> o2 = new ArrayList<String>();
        List<String> o3 = new ArrayList<String>();
        o1.add("something");
        o2.add("something");
        o3.add("somethingelse");
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testSet() {
        Set<String> o1 = new HashSet<String>();
        Set<String> o2 = new HashSet<String>();
        Set<String> o3 = new HashSet<String>();
        o1.add("something");
        o2.add("something");
        o3.add("somethingelse");
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testNonCollectionSupport() {
        EqualsHashCodeBuilder eq = new EqualsHashCodeBuilder();
        try {
            eq.append(Collections.unmodifiableCollection(new ArrayList<String>()));
            Assert.fail("Should fail because Collections are not supported!");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    // =======================================================================
    // Added 'x' to helper methods to prevent eclipse from warning..
    // =======================================================================

    //
    // Helper Methods
    //
    void xtestHiddenByObject(Object o1, Object o2, Object o3) {
        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    void xtestHashCode(EqualsHashCodeBuilder eq1, EqualsHashCodeBuilder eq2,
            EqualsHashCodeBuilder eq3) {
        // test hash code part...
        Set<EqualsHashCodeBuilder> set = new HashSet<EqualsHashCodeBuilder>();
        set.add(eq1);
        set.add(eq2);
        set.add(eq3);
        assertTrue(set.size() == 2);
    }

    void xtestEqualsBuilderHashCode() {
        Map<String, String> o1 = new HashMap<String, String>();
        Map<String, String> o2 = new HashMap<String, String>();
        Map<String, String> o3 = new HashMap<String, String>();

        EqualsHashCodeBuilder eq1 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq2 = new EqualsHashCodeBuilder();
        EqualsHashCodeBuilder eq3 = new EqualsHashCodeBuilder();

        eq1.append(o1);
        eq2.append(o2);
        eq3.append(o3);
        assertTrue(eq1.equals(eq1));
        assertTrue(eq1.equals(eq2));
        assertTrue(eq2.equals(eq1));
        assertFalse(eq1.equals(null));
        assertFalse(eq1.equals(eq3));
        assertFalse(eq3.equals(eq1));
        xtestHashCode(eq1, eq2, eq3);
    }

    @Test
    public void testBeanComparison() throws Exception {
        TestBean b1 = new TestBean(new Random());
        TestBean b2 = (TestBean) b1.clone();
        TestBean b3 = new TestBean(new Random());

        EqualsHashCodeBuilder bld1 = new EqualsHashCodeBuilder();
        bld1.appendBean(b1);
        EqualsHashCodeBuilder bld2 = new EqualsHashCodeBuilder();
        bld2.appendBean(b2);
        assertTrue(bld1.equals(bld1));
        assertTrue(bld1.equals(bld2));
        EqualsHashCodeBuilder bld3 = new EqualsHashCodeBuilder();
        bld3.appendBean(b3);
        assertFalse(bld2.equals(bld3));
    }

    // ========================================================================
    // Helper Classes
    // ========================================================================

    public static class TestBean implements Cloneable {
        private String arg1;
        private int arg2;
        private long arg3;
        private double arg4;
        private byte arg5;

        public TestBean(Random r) {
            if (r != null) {
                arg1 = StringUtil.randomString(r, 25);
                arg2 = r.nextInt();
                arg3 = r.nextLong();
                arg4 = r.nextDouble();
                arg5 = (byte) r.nextInt();
            }
        }

        public String getArg1() {
            return arg1;
        }

        public void setArg1(String arg1) {
            this.arg1 = arg1;
        }

        public int getArg2() {
            return arg2;
        }

        public void setArg2(int arg2) {
            this.arg2 = arg2;
        }

        public long getArg3() {
            return arg3;
        }

        public void setArg3(long arg3) {
            this.arg3 = arg3;
        }

        public double getArg4() {
            return arg4;
        }

        public void setArg4(double arg4) {
            this.arg4 = arg4;
        }

        public byte getArg5() {
            return arg5;
        }

        public void setArg5(byte arg5) {
            this.arg5 = arg5;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    static class TestObject {
        static final int SEED = 19;
        private int a;

        public TestObject() {
        }

        public TestObject(int a) {
            this.a = a;
        }

        public boolean equals(Object o) {
            boolean ret = false;
            if (o instanceof TestObject) {
                if (o == this) { // identity check..
                    ret = true;
                } else { // value check..
                    TestObject rhs = (TestObject) o;
                    ret = (a == rhs.a);
                }
            }
            return ret;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getA() {
            return a;
        }

        @Override
        public int hashCode() {
            return a * SEED;
        }
    }

    static class TestSubObject extends TestObject {
        private int b;

        public TestSubObject() {
            super(0);
        }

        public TestSubObject(int a, int b) {
            super(a);
            this.b = b;
        }

        public boolean equals(Object o) {
            boolean ret = false;
            if (o instanceof TestSubObject) {
                if (o == this) { // identity check..
                    ret = true;
                } else { // value check..
                    TestSubObject rhs = (TestSubObject) o;
                    ret = super.equals(o) && (b == rhs.b);
                }
            }
            return ret;
        }

        public void setB(int b) {
            this.b = b;
        }

        public int getB() {
            return b;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + SEED * b;
        }
    }

    static class TestEmptySubObject extends TestObject {
        public TestEmptySubObject(int a) {
            super(a);
        }
    }

    public static class TestACanEqualB {
        private int a;

        public TestACanEqualB(int a) {
            this.a = a;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof TestACanEqualB) {
                return this.a == ((TestACanEqualB) o).getA();
            }
            if (o instanceof TestBCanEqualA) {
                return this.a == ((TestBCanEqualA) o).getB();
            }
            return false;
        }

        public int getA() {
            return this.a;
        }
    }

    public static class TestBCanEqualA {
        private int b;

        public TestBCanEqualA(int b) {
            this.b = b;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof TestACanEqualB) {
                return this.b == ((TestACanEqualB) o).getA();
            }
            if (o instanceof TestBCanEqualA) {
                return this.b == ((TestBCanEqualA) o).getB();
            }
            return false;
        }

        public int getB() {
            return this.b;
        }
    }
}
