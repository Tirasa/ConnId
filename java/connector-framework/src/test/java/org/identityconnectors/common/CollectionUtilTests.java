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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

public class CollectionUtilTests {

    @Test
    public void testNullAsEmptyList() {
        // test w/ null
        List<String> l = CollectionUtil.nullAsEmpty((List<String>) null);
        assertNotNull(l);
        assertEquals(l.size(), 0);
        // test w/ regular
        l.add(" adf");
        List<String> t = CollectionUtil.nullAsEmpty(l);
        assertEquals(l, t);
    }

    @Test
    public void testNullAsEmptyMap() {
        // test w/ null
        Map<String, String> m = CollectionUtil.nullAsEmpty((Map<String, String>) null);
        assertNotNull(m);
        assertEquals(m.size(), 0);
        // test w/ regular
        m.put(" adf", "fdf");
        Map<String, String> t = CollectionUtil.nullAsEmpty(m);
        assertEquals(m, t);
    }

    @Test
    public void testNullAsEmptySet() {
        // test w/ null
        Set<String> l = CollectionUtil.nullAsEmpty((Set<String>) null);
        assertNotNull(l);
        assertEquals(l.size(), 0);
        // test w/ regular
        l.add(" adf");
        Set<String> t = CollectionUtil.nullAsEmpty(l);
        assertEquals(l, t);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(CollectionUtil.isEmpty((Collection<String>) null));
        Collection<String> c = new ArrayList<String>();
        assertTrue(CollectionUtil.isEmpty(c));
        c.add("dfa");
        assertFalse(CollectionUtil.isEmpty(c));
    }

    @Test
    public void testUnique() {
        List<String> list = new ArrayList<String>();
        list.add("test");
        list.add("test");
        Collection<String> u = CollectionUtil.unique(list);
        assertEquals(u.size(), 1);
    }

    // <T, K> Map<T, K> newUnmodifiableMap(Map<T, K> map) {
    // <T> Map<T, T> map(T[][] kv)
    // <T, K> Map<T, K> mapFromLists(Collection<T> keys, Collection<K> values)

    // <T, K> Map<T, K> asMap(T k0, K v0) {

    // <T, K> Map<T, K> asMap(T k0, K v0, T k1, K v1) {

    // <T, K> Map<T, K> asMap(T k0, K v0, T k1, K v1, T k2, K v2) {

    // <T, K> Map<T, K> asMap(T[] k, K[] v) {

    @Test
    public void testAsList() {
        Collection<Integer> c = new HashSet<Integer>();
        c.add(1);
        c.add(2);
        List<Integer> list = CollectionUtil.newList(c);
        assertTrue(list.remove(Integer.valueOf(1)));
        assertTrue(list.remove(Integer.valueOf(2)));
        // make sure it can be modified..
        list.add(2);
        // make sure asset can handle null..
        c = null;
        CollectionUtil.newList(c);
        // test array..
        list = CollectionUtil.newList(1, 2);
        assertTrue(list.remove(Integer.valueOf(1)));
        assertTrue(list.remove(Integer.valueOf(2)));
    }

    @Test
    public void testAsSet() {
        Collection<Integer> c = new ArrayList<Integer>();
        c.add(1);
        c.add(2);
        Set<Integer> set = CollectionUtil.newSet(c);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        // make sure it can be modified..
        set.add(2);
        // make sure asset can handle null..
        c = null;
        CollectionUtil.newSet(c);
        // test vargs..
        set = CollectionUtil.newSet(1, 2);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testList() {
        Collection<Integer> c = new HashSet<Integer>();
        c.add(1);
        c.add(2);
        List<Integer> list = CollectionUtil.newReadOnlyList(c);
        assertTrue(list.remove(Integer.valueOf(1)));
        assertTrue(list.remove(Integer.valueOf(2)));
        // make sure asset can handle null..
        c = null;
        CollectionUtil.newReadOnlyList(c);
        // test array..
        list = CollectionUtil.newReadOnlyList(1, 2);
        assertTrue(list.contains(1));
        assertTrue(list.contains(2));
        // make sure it can *not* be modified..
        list.add(2);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testSet() {
        Collection<Integer> c = new ArrayList<Integer>();
        c.add(1);
        c.add(2);
        Set<Integer> set = CollectionUtil.newReadOnlySet(c);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        // make sure asset can handle null..
        c = null;
        CollectionUtil.newReadOnlySet(c);
        // test vargs..
        set = CollectionUtil.newReadOnlySet(1, 2);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        // make sure it can *not* be modified..
        set.add(2);
    }

    // <T> Set<T> union(Collection<T> c1, Collection<T> c2) {
    // <T> Set<T> intersection(Collection<T> c1, Collection<T> c2) {
    // <T extends Object & Comparable<? super T>> List<T> asSortedList(final
    // Collection<? extends T> col) {
    // <T> List<T> list(final List<T> list) {

    @Test
    public void testReadonlyList() {
        String[] data = { "a", "b", "c" };
        List<String> expected = CollectionUtil.newReadOnlyList("a", "b", "c");
        List<String> actual = CollectionUtil.newReadOnlyList(data);
        assertEquals(actual, expected);
    }

    @Test
    public void testReadonlySet() {
        String[] data = { "a", "b", "c" };
        Set<String> expected = CollectionUtil.newReadOnlySet("a", "b", "c");
        Set<String> actual = CollectionUtil.newReadOnlySet(data);
        assertEquals(actual, expected);
    }

    @Test
    public void testEquals() {
        assertTrue(CollectionUtil.equals(null, null));
        assertFalse(CollectionUtil.equals(null, "str"));
        assertTrue(CollectionUtil.equals("str", "str"));

        byte[] arr1 = new byte[] { 1, 2, 3 };
        byte[] arr2 = new byte[] { 1, 2, 3 };
        byte[] arr3 = new byte[] { 1, 2, 4 };
        byte[] arr4 = new byte[] { 1, 2 };
        int[] arr5 = new int[] { 1, 2, 3 };

        assertTrue(CollectionUtil.equals(arr1, arr2));
        assertFalse(CollectionUtil.equals(arr2, arr3));
        assertFalse(CollectionUtil.equals(arr2, arr4));
        assertFalse(CollectionUtil.equals(arr2, arr5));

        List<byte[]> list1 = new ArrayList<byte[]>();
        List<byte[]> list2 = new ArrayList<byte[]>();
        list1.add(arr1);
        list2.add(arr2);

        assertTrue(CollectionUtil.equals(list1, list2));

        list2.add(arr2);
        assertFalse(CollectionUtil.equals(list1, list2));

        list1.add(arr1);
        assertTrue(CollectionUtil.equals(list1, list2));

        list1.add(arr1);
        list2.add(arr3);
        assertFalse(CollectionUtil.equals(list1, list2));

        Map<String, byte[]> map1 = new HashMap<String, byte[]>();
        Map<String, byte[]> map2 = new HashMap<String, byte[]>();
        map1.put("key1", arr1);
        map2.put("key1", arr2);
        assertTrue(CollectionUtil.equals(map1, map2));
        map2.put("key2", arr2);
        assertFalse(CollectionUtil.equals(map1, map2));
        map1.put("key2", arr1);
        assertTrue(CollectionUtil.equals(map1, map2));
        map1.put("key2", arr3);
        assertFalse(CollectionUtil.equals(map1, map2));

        Set<String> set1 = new HashSet<String>();
        Set<String> set2 = new HashSet<String>();
        set1.add("val");
        set2.add("val");
        assertTrue(CollectionUtil.equals(set1, set2));
        set2.add("val2");
        assertFalse(CollectionUtil.equals(set1, set2));
        set1.add("val2");
        assertTrue(CollectionUtil.equals(set1, set2));
    }

    @Test
    public void testHashCode() {
        assertEquals(CollectionUtil.hashCode(null), 0);
        assertEquals(CollectionUtil.hashCode("str"), "str".hashCode());

        byte[] arr1 = new byte[] { 1, 2, 3 };
        byte[] arr2 = new byte[] { 1, 2, 3 };
        byte[] arr3 = new byte[] { 1, 2, 4 };
        byte[] arr4 = new byte[] { 1, 2 };
        int[] arr5 = new int[] { 1, 2, 3 };

        assertEquals(CollectionUtil.hashCode(arr1), CollectionUtil.hashCode(arr2));
        assertFalse(CollectionUtil.hashCode(arr2) == CollectionUtil.hashCode(arr3));
        assertFalse(CollectionUtil.hashCode(arr2) == CollectionUtil.hashCode(arr4));
        assertTrue(CollectionUtil.hashCode(arr2) == CollectionUtil.hashCode(arr5));

        List<byte[]> list1 = new ArrayList<byte[]>();
        List<byte[]> list2 = new ArrayList<byte[]>();
        list1.add(arr1);
        list2.add(arr2);

        assertTrue(CollectionUtil.hashCode(list1) == CollectionUtil.hashCode(list2));

        list2.add(arr2);
        assertFalse(CollectionUtil.hashCode(list1) == CollectionUtil.hashCode(list2));

        list1.add(arr1);
        assertTrue(CollectionUtil.hashCode(list1) == CollectionUtil.hashCode(list2));

        list1.add(arr1);
        list2.add(arr3);
        assertFalse(CollectionUtil.hashCode(list1) == CollectionUtil.hashCode(list2));

        Map<String, byte[]> map1 = new HashMap<String, byte[]>();
        Map<String, byte[]> map2 = new HashMap<String, byte[]>();
        map1.put("key1", arr1);
        map2.put("key1", arr2);
        assertTrue(CollectionUtil.hashCode(map1) == CollectionUtil.hashCode(map2));
        map2.put("key2", arr2);
        assertFalse(CollectionUtil.hashCode(map1) == CollectionUtil.hashCode(map2));
        map1.put("key2", arr1);
        assertTrue(CollectionUtil.hashCode(map1) == CollectionUtil.hashCode(map2));
        map1.put("key2", arr3);
        assertFalse(CollectionUtil.hashCode(map1) == CollectionUtil.hashCode(map2));

        Set<String> set1 = new HashSet<String>();
        Set<String> set2 = new HashSet<String>();
        set1.add("val");
        set2.add("val");
        assertTrue(CollectionUtil.hashCode(set1) == CollectionUtil.hashCode(set2));
        set2.add("val2");
        assertFalse(CollectionUtil.hashCode(set1) == CollectionUtil.hashCode(set2));
        set1.add("val2");
        assertTrue(CollectionUtil.hashCode(set1) ==             CollectionUtil.hashCode(set2));
    }
}
