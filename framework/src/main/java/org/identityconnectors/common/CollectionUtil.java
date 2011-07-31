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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public final class CollectionUtil {

    /**
     * Never allow this to be instantiated.
     */
    private CollectionUtil() {
        throw new AssertionError();
    }
    
    /**
     * Creates a case-insensitive set
     * @return An empty case-insensitive set
     */
    public static SortedSet<String> newCaseInsensitiveSet() {
        TreeSet<String> rv = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        return rv;
    }
    
    /**
     * Returns true if the given set is a case-insensitive set
     * @param set The set. May be null.
     * @return true iff the given set is a case-insensitive set
     */
    public static boolean isCaseInsensitiveSet(Set<?> set) {
        if ( set instanceof SortedSet ) {
            SortedSet<?> sortedSet =
                (SortedSet<?>)set;
            Comparator<?> comp = sortedSet.comparator();
            if ( comp.equals(String.CASE_INSENSITIVE_ORDER) ) {
                return true;
            }
        }
        return false;
    }
    /**
     * Creates a case-insenstive map
     * @param <T> The object type of the map
     * @return An empty case-insensitive map
     */
    public static <T> SortedMap<String,T> newCaseInsensitiveMap() {
        TreeMap<String,T> rv = new TreeMap<String,T>(String.CASE_INSENSITIVE_ORDER);
        return rv;
    }
    
    /**
     * Returns true if the given map is a case-insensitive map
     * @param map The map. May be null.
     * @return true iff the given map is a case-insensitive map
     */
    public static boolean isCaseInsensitiveMap(Map<?,?> map) {
        if ( map instanceof SortedMap ) {
            SortedMap<?,?> sortedMap =
                (SortedMap<?,?>)map;
            Comparator<?> comp = sortedMap.comparator();
            if ( comp.equals(String.CASE_INSENSITIVE_ORDER) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * Protects from <code>null</code> and returns a new instance of
     * {@link ArrayList} if the parameter <strong>c</strong> is <strong>null</strong>.
     * Otherwise return the parameter that was passed in.
     */
    public static <T> Collection<T> nullAsEmpty(Collection<T> c) {
        return c == null ? new HashSet<T>() : c;
    }

    /**
     * Protects from <code>null</code> and returns a new instance of
     * {@link HashMap} if the parameter <code>map</code> is <code>null</code>.
     * Otherwise return the parameter that was passed in.
     */
    public static <T, K> Map<T, K> nullAsEmpty(Map<T, K> map) {
        return (map == null) ? new HashMap<T, K>() : map;
    }

    /**
     * Protects from <code>null</code> and returns a new instance of
     * {@link HashSet} if the parameter <code>set</code> is <code>null</code>.
     * Otherwise return the parameter that was passed in.
     */
    public static <T> Set<T> nullAsEmpty(Set<T> set) {
        return (set == null) ? new HashSet<T>() : set;
    }

    /**
     * Protects from <code>null</code> and returns a new instance of
     * {@link ArrayList} if the parameter <code>list</code> is
     * <code>null</code>. Otherwise return the parameter that was passed in.
     */
    public static <T> List<T> nullAsEmpty(final List<T> list) {
        return (list == null) ? new ArrayList<T>() : list;
    }

    /**
     * Determine if {@link Collection} is empty or not, protects against null
     * being passed in.
     */
    public static <T> boolean isEmpty(Collection<T> c) {
        return c == null || c.isEmpty();
    }

    /**
     * Use {@link HashSet} to create a unique {@link Collection} based on the
     * one passed in. The method protects against <strong>null</strong>. The
     * returned {@link Collection} is unmodifiable.
     */
    public static <T> Collection<T> unique(final Collection<T> c) {
        return Collections.<T> unmodifiableSet(newSet(c));
    }

    public static <T, K> Map<T, K> newReadOnlyMap(Map<T, K> map) {
        return Collections.unmodifiableMap(new HashMap<T, K>(nullAsEmpty(map)));
    }
    
    public static <T, K> Map<T, K> asReadOnlyMap(Map<T, K> map) {
        if ( map instanceof SortedMap ) {
            @SuppressWarnings("unchecked")
            SortedMap<T,K> sortedMap =
                (SortedMap)map;
            return Collections.unmodifiableSortedMap(sortedMap);
        }
        else {
            return Collections.unmodifiableMap(nullAsEmpty(map));
        }
    }

    public static <T> Map<T, T> newReadOnlyMap(T[][] kv) {
        Map<T, T> map = new HashMap<T, T>();
        for (int i = 0; kv != null && i < kv.length; i++) {
            T key = kv[i][0];
            T value = kv[i][1];
            map.put(key, value);
        }
        return Collections.<T, T> unmodifiableMap(map);
    }

    /**
     * Converts two {@link List} to a map. The order is important here because
     * each key will map to one value.
     */
    public static <T, K> Map<T, K> newMapFromLists(List<T> keys, List<K> values) {
        // throw if there's invalid input..
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException();
        }
        Map<T, K> map = new HashMap<T, K>(keys.size());
        Iterator<T> keyIter = keys.iterator();
        Iterator<K> valueIter = values.iterator();
        while (keyIter.hasNext() && valueIter.hasNext()) {
            T key = keyIter.next();
            K value = valueIter.next();
            map.put(key, value);
        }
        return map;
    }

    public static Map<String,String> newMap(Properties properties) {
        Map<String,String> rv = new HashMap<String,String>();
        for (Map.Entry<Object,Object> entry : properties.entrySet()) {
            rv.put((String)entry.getKey(),
                   (String)entry.getValue());
        }
        return rv;
    }
    
    public static <T, K> Map<T, K> newMap(T k0, K v0) {
        Map<T, K> map = new HashMap<T, K>();
        map.put(k0, v0);
        return map;
    }

    public static <T, K> Map<T, K> newMap(T k0, K v0, T k1, K v1) {
        Map<T, K> map = newMap(k0, v0);
        map.put(k1, v1);
        return map;
    }

    public static <T, K> Map<T, K> newMap(T k0, K v0, T k1, K v1, T k2, K v2) {
        Map<T, K> map = newMap(k0, v0, k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <T, K> Map<T, K> newMap(T k0, K v0, T k1, K v1, T k2, K v2,
            T k3, K v3) {
        Map<T, K> map = newMap(k0, v0, k1, v1, k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static <T, K> Map<T, K> newMap(T k0, K v0, T k1, K v1, T k2, K v2,
            T k3, K v3, T k4, K v4) {
        Map<T, K> map = newMap(k0, v0, k1, v1, k2, v2, k3, v3);
        map.put(k4, v4);
        return map;
    }

    public static <T, K> Map<T, K> newMap(T k0, K v0, T k1, K v1, T k2, K v2,
            T k3, K v3, T k4, K v4, T k5, K v5) {
        Map<T, K> map = newMap(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4);
        map.put(k5, v5);
        return map;
    }

    /**
     * Builds a map from two arrays.
     * 
     * @param k
     *            Array of keys.
     * @param v
     *            Array of values.
     * @return a map based on the two arrays.
     */
    public static <T, K> Map<T, K> newMap(T[] k, K[] v) {
        // throw if there's invalid input..
        if (k.length != v.length) {
            throw new IllegalArgumentException();
        }
        Map<T, K> map = new HashMap<T, K>(k.length);
        for (int i = 0; i < k.length; i++) {
            T key = k[i];
            K value = v[i];
            map.put(key, value);
        }
        return map;
    }

    /**
     * Creates a set that can be modified from the {@link Collection} provided.
     */
    public static <T> Set<T> newSet(Collection<T> c) {
        return new HashSet<T>(CollectionUtil.nullAsEmpty(c));
    }

    /**
     * Creates a set that can be modified from the arguments.
     */
    public static <T> Set<T> newSet(T... arr) {
        // default to empty..
        Set<T> ret = new HashSet<T>();
        if (arr != null && arr.length != 0) {
            // not empty populate the set..
            for (T t : arr) {
                ret.add(t);
            }
        }
        return ret;
    }

    /**
     * Creates an unmodifiable set from a variable number arguments.
     */
    public static <T> Set<T> newReadOnlySet(T... arr) {
        return Collections.unmodifiableSet(newSet(arr));
    }

    /**
     * Creates an unmodifiable set from a {@link Collection}.
     */
    public static <T> Set<T> newReadOnlySet(Collection<T> c) {
        return Collections.unmodifiableSet(newSet(c));
    }

    /**
     * Returns the union of two {@link Collection}s as an unmodifiable set.
     */
    public static <T> Set<T> union(Collection<T> c1, Collection<T> c2) {
        Set<T> union = newSet(c1);
        union.addAll(c2);
        return Collections.<T> unmodifiableSet(union);
    }

    /**
     * Returns the intersection of two {@link Collection}s as an unmodifiable
     * set.
     */
    public static <T> Set<T> intersection(Collection<T> c1, Collection<T> c2) {
        Set<T> intersection = newSet(c1);
        intersection.retainAll(c2);
        return Collections.<T> unmodifiableSet(intersection);
    }

    /**
     * Create a modifiable sorted {@link List} based on the {@link Collection}
     * provided.
     */
    public static <T extends Object & Comparable<? super T>> List<T> newSortedList(
            final Collection<? extends T> col) {
        List<T> list = newList(col);
        Collections.sort(list);
        return list;
    }

    /**
     * Create a modifiable list from the {@link Collection} provided. The return
     * value is backed by an {@link ArrayList}.
     */
    public static <T> List<T> newList(Collection<? extends T> c) {
        return new ArrayList<T>(CollectionUtil.nullAsEmpty(c));
    }

    /**
     * Create a modifiable list from the arguments. The return value is backed
     * by an {@link ArrayList}.
     */
    public static <T> List<T> newList(T... arr) {
        List<T> ret = new ArrayList<T>();
        if (arr != null && arr.length != 0) {
            for (T t : arr) {
                ret.add(t);
            }
        }
        return ret;
    }

    /**
     * Create an unmodifiable {@link List} based on the {@link List} passed in
     * checks for null and returns an empty list if null is passed in. This one
     * insures that the order is maintained between lists.
     */
    public static <T> List<T> newReadOnlyList(final List<? extends T> list) {
        List<T> l = new ArrayList<T>(nullAsEmpty(list));
        return Collections.unmodifiableList(l);
    }

    /**
     * Creates an unmodifiable {@link List} from a variable number arguments.
     */
    public static <T> List<T> newReadOnlyList(T... obj) {
        return Collections.unmodifiableList(newList(obj));
    }

    /**
     * Creates an unmodifiable {@link List} from a collection.
     */
    public static <T> List<T> newReadOnlyList(final Collection<? extends T> c) {
        return Collections.unmodifiableList(newList(c));
    }
    
    /**
     * Returns a read-only list. The list is backed by the original
     * so no copy is made.
     * @param <T> The type of the list
     * @param list The list or null.
     * @return A read-only proxy on the original list.
     */
    public static <T> List<T> asReadOnlyList(List<T> list) {
        if ( list == null ) {
            list = new ArrayList<T>();
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Forces the compare of two comparable objects and removes any warnings
     * generated by the compiler.
     * 
     * @return {@link Comparable} the integer value of o1.compareTo(o2).
     */
    public static <T> int forceCompare(Object o1, Object o2) {
        @SuppressWarnings("unchecked")
        Comparable<T> t1 = (Comparable<T>) o1;
        @SuppressWarnings("unchecked")
        T t2 = (T) o2;
        return t1.compareTo(t2);
    }
    
    /**
     * hashCode function that properly handles arrays,
     * collections, maps, collections of arrays, and maps of arrays.
     * @param o The object. May be null.
     * @return the hashCode
     */
    public static int hashCode(Object o) {
        if ( o == null ) {
            return 0;
        }
        else if ( o.getClass().isArray() ) {
            int length = Array.getLength(o);
            int rv = 0;
            for ( int i = 0; i < length; i++ ) {
                Object el = Array.get(o, i);
                rv += CollectionUtil.hashCode(el);
            }
            return rv;
        }
        else if ( o instanceof Collection ) {
            Collection<?> l = (Collection<?>)o;
            int rv = 0;
            for ( Object el : l) {
                rv += CollectionUtil.hashCode(el);
            }
            return rv;
        }
        else if ( o instanceof Map ) {
            Map<?,?> map = (Map<?,?>)o;
            return CollectionUtil.hashCode(map.values());
        }
        else {
            return o.hashCode();
        }
    }
    
    /**
     * Equality function that properly handles arrays,
     * lists, maps, lists of arrays, and maps of arrays.
     * <p>
     * NOTE: For Sets, this relies on the equals method
     * of the Set to do the right thing. This is a reasonable
     * assumption since, in order for Sets to behave
     * properly as Sets, their values must already have
     * a proper implementation of equals. (Or they must
     * be specialized Sets that define a custom comparator that
     * knows how to do the right thing). The same holds true for Map keys.
     * Map values, on the other hand, are compared (so Map values
     * can be arrays). 
     * @param o1 The first object. May be null.
     * @param o2 The second object. May be null.
     * @return true iff the two objects are equal.
     */
    public static boolean equals(Object o1, Object o2) {
        if ( o1 == o2 ) { //same object or both null
            return true; 
        }
        else if ( o1 == null ) {
            return false;
        }
        else if ( o2 == null ) {
            return false;
        }
        else if ( o1.getClass().isArray() ) {
            Class<?> clazz1 = o1.getClass();
            Class<?> clazz2 = o2.getClass();
            if ( !clazz1.equals(clazz2)) {
                return false;
            }
            int length1 = Array.getLength(o1);
            int length2 = Array.getLength(o2);
            if ( length1 != length2 ) {
                return false;
            }
            for ( int i = 0; i < length1; i++ ) {
                Object el1 = Array.get(o1, i);
                Object el2 = Array.get(o2, i);
                if (!CollectionUtil.equals(el1,el2)) {
                    return false;
                }
            }
            return true;
        }
        else if ( o1 instanceof List ) {
            if ( o2 instanceof List ) {
                List<?> l1 = (List<?>)o1;
                List<?> l2 = (List<?>)o2;
                if ( l1.size() != l2.size() ) {
                    return false;
                }
                for ( int i = 0; i < l1.size(); i++) {
                    Object el1 = l1.get(i);
                    Object el2 = l2.get(i);
                    if (!CollectionUtil.equals(el1, el2)) {
                        return false;
                    }
                }
                return true;
             }
            else {
                return false;
            }
        }
        else if ( o1 instanceof Set ) {
            if ( o2 instanceof Set ) {
                //rely on Set equality. this does not
                //handle the case of arrays within sets,
                //but arrays should not be placed within sets
                //unless the set is a specialized set that
                //knows how to compare arrays
                return o1.equals(o2);
            }
            else {
                return false;
            }
        }
        else if ( o1 instanceof Map ) {
            if ( o2 instanceof Map ) {
                Map<?,?> m1 = (Map<?,?>)o1;
                Map<?,?> m2 = (Map<?,?>)o2;
                if ( m1.size() != m2.size() ) {
                    return false;
                }
                for (Map.Entry<?, ?> entry1 : m1.entrySet()) {
                    Object key1 = entry1.getKey();
                    Object val1 = entry1.getValue();
                    if (!m2.containsKey(key1)) {
                        return false;
                    }
                    Object val2 = m2.get(key1);
                    if (!CollectionUtil.equals(val1,val2)) {
                        return false;
                    }
                }
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return o1.equals(o2);
        }
    }
}
