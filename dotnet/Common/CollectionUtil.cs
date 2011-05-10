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
using System;
using System.Runtime.CompilerServices;
using System.Reflection;
using System.Collections.Generic;

namespace Org.IdentityConnectors.Common
{
    internal class IdentityEqualityComparer<T> : IEqualityComparer<T> where T : class
    {
        public bool Equals(T x, T y)
        {
            return Object.ReferenceEquals(x, y);
        }
        public int GetHashCode(T o)
        {
            return RuntimeHelpers.GetHashCode(o);
        }
    }

    internal class ReadOnlyCollection<T> : ICollection<T>
    {
        private readonly ICollection<T> _target;
        public ReadOnlyCollection(ICollection<T> target)
        {
            _target = target;
        }
        public void Add(T item)
        {
            throw new NotSupportedException();
        }
        public void Clear()
        {
            throw new NotSupportedException();
        }
        public bool Contains(T item)
        {
            return _target.Contains(item);
        }
        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return _target.GetEnumerator();
        }
        public IEnumerator<T> GetEnumerator()
        {
            return _target.GetEnumerator();
        }
        public bool IsReadOnly
        {
            get
            {
                return true;
            }
        }
        public int Count
        {
            get
            {
                return _target.Count;
            }
        }
        public bool Remove(T item)
        {
            throw new NotSupportedException();
        }
        public void CopyTo(T[] array,
                           int arrayIndex)
        {
            _target.CopyTo(array, arrayIndex);
        }

        public ICollection<T> GetTarget()
        {
            return _target;
        }

    }


    internal class ReadOnlyList<T> : ReadOnlyCollection<T>, IList<T>
    {

        public ReadOnlyList(IList<T> target)
            : base(target)
        {

        }
        public int IndexOf(T item)
        {
            return GetTarget().IndexOf(item);
        }
        public void Insert(int index, T item)
        {
            throw new NotSupportedException();
        }
        public void RemoveAt(int index)
        {
            throw new NotSupportedException();
        }

        public T this[int index]
        {
            get
            {
                return GetTarget()[index];
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        protected new IList<T> GetTarget()
        {
            return (IList<T>)base.GetTarget();
        }
    }

    internal class ReadOnlyDictionary<TKey, TValue> :
        ReadOnlyCollection<KeyValuePair<TKey, TValue>>,
    IDictionary<TKey, TValue>
    {
        public ReadOnlyDictionary(IDictionary<TKey, TValue> target)
            : base(target)
        {

        }
        public void Add(TKey key, TValue val)
        {
            throw new NotSupportedException();
        }
        protected new IDictionary<TKey, TValue> GetTarget()
        {
            return (IDictionary<TKey, TValue>)base.GetTarget();
        }
        public ICollection<TKey> Keys
        {
            get
            {
                return new ReadOnlyCollection<TKey>(GetTarget().Keys);
            }
        }
        public ICollection<TValue> Values
        {
            get
            {
                return new ReadOnlyCollection<TValue>(GetTarget().Values);
            }
        }
        public bool Remove(TKey key)
        {
            throw new NotSupportedException();
        }
        public bool ContainsKey(TKey key)
        {
            return GetTarget().ContainsKey(key);
        }
        public bool TryGetValue(TKey key, out TValue value)
        {
            return GetTarget().TryGetValue(key, out value);
        }
        public TValue this[TKey key]
        {
            get
            {
                return GetTarget()[key];
            }
            set
            {
                throw new NotSupportedException();
            }
        }
    }




    /// <summary>
    /// Delegate that returns the Key, given a value
    /// </summary>
    public delegate TKey KeyFunction<TKey, TValue>(TValue value);


    /// <summary>
    /// Description of CollectionUtil.
    /// </summary>
    public static class CollectionUtil
    {
        /// <summary>
        /// Creates a case-insensitive set
        /// </summary>
        /// <returns>An empty case-insensitive set</returns>
        public static ICollection<String> NewCaseInsensitiveSet()
        {
            HashSet<string> rv = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
            return rv;
        }

        /// <summary>
        /// Returns true iff the collection is a case-insensitive set
        /// </summary>
        /// <param name="collection">The collection. May be null.</param>
        /// <returns>true iff the collection is a case-insensitive set</returns>
        public static bool IsCaseInsensitiveSet<T>(ICollection<T> collection)
        {
            if (collection is ReadOnlyCollection<T>)
            {
                ReadOnlyCollection<T> roc =
                    (ReadOnlyCollection<T>)collection;
                return IsCaseInsensitiveSet(roc.GetTarget());
            }
            else if (collection is HashSet<string>)
            {
                HashSet<string> set = (HashSet<string>)collection;
                return StringComparer.OrdinalIgnoreCase.Equals(set.Comparer);
            }
            else
            {
                return false;
            }
        }
        /// <summary>
        /// Creates a case-insensitive map
        /// </summary>
        /// <returns>An empty case-insensitive map</returns>
        public static IDictionary<String, T> NewCaseInsensitiveDictionary<T>()
        {
            Dictionary<string, T> rv = new Dictionary<string, T>(StringComparer.OrdinalIgnoreCase);
            return rv;
        }

        /// <summary>
        /// Returns true iff the collection is a case-insensitive map
        /// </summary>
        /// <param name="map">The map. May be null.</param>
        /// <returns>true iff the collection is a case-insensitive map</returns>
        public static bool IsCaseInsensitiveDictionary<K, V>(IDictionary<K, V> map)
        {
            if (map is ReadOnlyDictionary<K, V>)
            {
                ReadOnlyDictionary<K, V> roc =
                    (ReadOnlyDictionary<K, V>)map;
                return IsCaseInsensitiveDictionary((IDictionary<K, V>)roc.GetTarget());
            }
            else if (map is Dictionary<string, V>)
            {
                Dictionary<string, V> d = (Dictionary<string, V>)map;
                return StringComparer.OrdinalIgnoreCase.Equals(d.Comparer);
            }
            else
            {
                return false;
            }
        }

        /// <summary>
        /// Creates a dictionary where the keys are looked up using
        /// reference equality
        /// </summary>
        /// <returns></returns>
        public static IDictionary<K, V> NewIdentityDictionary<K, V>()
            where K : class
        {
            IdentityEqualityComparer<K> comp = new IdentityEqualityComparer<K>();
            return new Dictionary<K, V>(comp);
        }

        /// <summary>
        /// Computes a hashCode over the enumeration. The hashCode is computed
        /// such that it doesn't matter what order the elements are listed in. Thus, it
        /// is suitable for arrays, lists, sets, and dictionaries
        /// </summary>
        /// <param name="enum1">The enumerable</param>
        /// <returns>The hashcode</returns>
        public static int GetEnumerableHashCode<T>(IEnumerable<T> enum1)
        {
            if (enum1 == null)
            {
                return 0;
            }
            int rv = 0;
            foreach (T val1 in enum1)
            {
                unchecked
                {
                    rv += CollectionUtil.GetHashCode(val1);
                }
            }
            return rv;
        }

        /// <summary>
        /// Computes a hashCode for a key value pair.
        /// </summary>
        /// <param name="pair">The pair</param>
        /// <returns>The hashcode</returns>
        public static int GetKeyValuePairHashCode<K, V>(KeyValuePair<K, V> pair)
        {
            int rv = 0;
            unchecked
            {
                rv += CollectionUtil.GetHashCode(pair.Key);
                rv += CollectionUtil.GetHashCode(pair.Value);
            }
            return rv;
        }

        /// <summary>
        /// Returns true iff the two sets contain the same elements. This is only for
        /// sets and dictionaries. Does not work for Lists or Arrays.
        /// </summary>
        /// <param name="collection1">The first collection</param>
        /// <param name="collection2">The second collection</param>
        /// <returns></returns>
        public static bool SetsEqual<T>(ICollection<T> collection1,
                                        ICollection<T> collection2)
        {
            if (collection1 == null || collection1 == null)
            {
                return collection1 == null && collection1 == null;
            }
            if (collection1.Count != collection2.Count)
            {
                return false;
            }
            foreach (T val1 in collection1)
            {
                if (!collection2.Contains(val1))
                {
                    return false;
                }
            }
            return true;
        }

        /// <summary>
        /// Gets the given value from the map or default if not exists. Always
        /// use this rather than map[] since map[] throws an exception if not
        /// exists.
        /// </summary>
        /// <param name="map">The map</param>
        /// <param name="key">The key</param>
        /// <param name="def">The default value</param>
        /// <returns></returns>
        public static TValue GetValue<TKey, TValue>(IDictionary<TKey, TValue> map,
                                                   TKey key,
                                                   TValue def)
        {
            TValue rv;
            bool present = map.TryGetValue(key, out rv);
            if (present)
            {
                return rv;
            }
            else
            {
                return def;
            }
        }

        /// <summary>
        /// Adds all the elements from the given enumerable to the given collection.
        /// </summary>
        /// <param name="collection">The collection to add to</param>
        /// <param name="enumerable">The enumerable to get from</param>
        public static void AddAll<T, U>(ICollection<T> collection,
                                       IEnumerable<U> enumerable)
            where U : T
        {
            if (enumerable != null)
            {
                foreach (U obj in enumerable)
                {
                    collection.Add(obj);
                }
            }
        }

        /// <summary>
        /// Adds all the elements from the given enumerable to the given collection.
        /// Replace the element value if already stored in the collection.
        /// </summary>
        /// <typeparam name="TKey">IDictionary key type</typeparam>
        /// <typeparam name="TValue">IDictionary value type</typeparam>
        /// <typeparam name="UKey">Enumeration key type, has to extend IDictionary key type</typeparam>
        /// <typeparam name="UValue">Enumeration value type, has to extend IDictionary value type</typeparam>
        /// <param name="collection">The collection to add to</param>
        /// <param name="enumerable">The enumerable to get from</param>
        public static void AddOrReplaceAll<TKey, TValue, UKey, UValue>(IDictionary<TKey, TValue> collection,
                                       IEnumerable<KeyValuePair<UKey, UValue>> enumerable)
            where UKey : TKey
            where UValue : TValue
        {
            if (enumerable != null)
            {
                foreach (KeyValuePair<UKey, UValue> obj in enumerable)
                {
                    collection[obj.Key] = obj.Value;
                }
            }
        }

        /// <summary>
        /// Adds all the elements from the given enumerable to the given collection.
        /// </summary>
        /// <param name="collection">The collection to add to</param>
        /// <param name="enumerable">The enumerable to get from</param>
        public static void RemoveAll<T, U>(ICollection<T> collection,
                                       IEnumerable<U> enumerable)
            where U : T
        {
            if (enumerable != null)
            {
                foreach (U obj in enumerable)
                {
                    collection.Remove(obj);
                }
            }
        }

        /// <summary>
        /// Adds all the elements from the given enumerable to the given collection.
        /// </summary>
        /// <param name="collection">The collection to add to</param>
        /// <param name="enumerable">The enumerable to get from</param>
        public static void RemovalAll<T, U>(ICollection<T> collection,
                                       IEnumerable<U> enumerable)
            where U : T
        {
            if (enumerable != null)
            {
                foreach (U obj in enumerable)
                {
                    collection.Remove(obj);
                }
            }
        }


        /// <summary>
        /// Returns c or an empty collection iff c is null.
        /// </summary>
        /// <param name="c">The collection</param>
        /// <returns>c or an empty collection iff c is null.</returns>
        public static ICollection<T> NullAsEmpty<T>(ICollection<T> c)
        {
            return c ?? new HashSet<T>();
        }

        /// <summary>
        /// Returns c or an empty collection iff c is null.
        /// </summary>
        /// <param name="c">The collection</param>
        /// <returns>c or an empty collection iff c is null.</returns>
        public static IDictionary<K, V> NullAsEmpty<K, V>(IDictionary<K, V> c)
        {
            return c ?? new Dictionary<K, V>();
        }

        /// <summary>
        /// Returns c or an empty collection iff c is null.
        /// </summary>
        /// <param name="c">The collection</param>
        /// <returns>c or an empty collection iff c is null.</returns>
        public static IList<T> NullAsEmpty<T>(IList<T> c)
        {
            return c ?? new List<T>();
        }

        /// <summary>
        /// Returns c or an empty array iff c is null.
        /// </summary>
        /// <param name="c">The array</param>
        /// <returns>c or an empty collection iff c is null.</returns>
        public static T[] NullAsEmpty<T>(T[] c)
        {
            return c ?? new T[0];
        }

        /// <summary>
        /// Given a collection of values a key function, builds a dictionary
        /// </summary>
        /// <param name="values">List of values</param>
        /// <param name="keyFunction">Key function, mapping from key to value</param>
        /// <returns>The dictionay</returns>
        public static IDictionary<TKey, TValue> NewDictionary<TKey, TValue>(
            TKey k1,
            TValue v1)
        {
            IDictionary<TKey, TValue> rv = new Dictionary<TKey, TValue>();
            rv[k1] = v1;
            return rv;
        }

        /// <summary>
        /// Given a collection of values a key function, builds a dictionary
        /// </summary>
        /// <param name="values">List of values</param>
        /// <param name="keyFunction">Key function, mapping from key to value</param>
        /// <returns>The dictionay</returns>
        public static IDictionary<TKey, TValue> NewDictionary<TKey, TValue>(
            IEnumerable<TValue> values,
            KeyFunction<TKey, TValue> keyFunction)
        {
            IDictionary<TKey, TValue> rv = new Dictionary<TKey, TValue>();
            if (values != null)
            {
                foreach (TValue value in values)
                {
                    TKey key = keyFunction(value);
                    //DONT use Add - it throws exceptions if already there
                    rv[key] = value;
                }
            }
            return rv;
        }

        /// <summary>
        /// Given a collection of values a key function, builds a dictionary
        /// </summary>
        /// <param name="values">List of values</param>
        /// <param name="keyFunction">Key function, mapping from key to value</param>
        /// <returns>The dictionay</returns>
        public static IDictionary<TKey, TValue> NewReadOnlyDictionary<TKey, TValue>(
            IEnumerable<TValue> values,
            KeyFunction<TKey, TValue> keyFunction)
        {
            IDictionary<TKey, TValue> rv =
                NewDictionary<TKey, TValue>(values, keyFunction);
            return new ReadOnlyDictionary<TKey, TValue>(rv);
        }

        /// <summary>
        /// Given a collection of values a key function, builds a dictionary
        /// </summary>
        /// <param name="values">List of values</param>
        /// <param name="keyFunction">Key function, mapping from key to value</param>
        /// <returns>The dictionay</returns>
        public static IDictionary<TKey, TValue> NewDictionary<TKey, TValue>(
            IDictionary<TKey, TValue> original)
        {
            return NewDictionary<TKey, TValue, TKey, TValue>(original);
        }

        /// <summary>
        /// Given a collection of values a key function, builds a dictionary
        /// </summary>
        /// <param name="values">List of values</param>
        /// <param name="keyFunction">Key function, mapping from key to value</param>
        /// <returns>The dictionay</returns>
        public static IDictionary<TKey2, TValue2> NewDictionary<TKey1, TValue1, TKey2, TValue2>(
            IDictionary<TKey1, TValue1> original)
        {
            IDictionary<TKey2, TValue2> rv = new Dictionary<TKey2, TValue2>();
            if (original != null)
            {
                foreach (KeyValuePair<TKey1, TValue1> entry in original)
                {
                    //DONT use Add - it throws exceptions if already there
                    rv[(TKey2)(object)entry.Key] = (TValue2)(object)entry.Value;
                }
            }
            return rv;
        }

        /// <summary>
        /// Given a collection of values a key function, builds a dictionary
        /// </summary>
        /// <param name="values">List of values</param>
        /// <param name="keyFunction">Key function, mapping from key to value</param>
        /// <returns>The dictionay</returns>
        public static IDictionary<TKey, TValue> NewReadOnlyDictionary<TKey, TValue>(
            IDictionary<TKey, TValue> original)
        {
            return NewReadOnlyDictionary<TKey, TValue, TKey, TValue>(original);
        }

        /// <summary>
        /// Given a collection of values a key function, builds a dictionary
        /// </summary>
        /// <param name="values">List of values</param>
        /// <param name="keyFunction">Key function, mapping from key to value</param>
        /// <returns>The dictionay</returns>
        public static IDictionary<TKey2, TValue2> NewReadOnlyDictionary<TKey1,
        TValue1, TKey2, TValue2>(
            IDictionary<TKey1, TValue1> original)
        {
            IDictionary<TKey2, TValue2> rv = NewDictionary<TKey1, TValue1, TKey2, TValue2>(original);
            return new ReadOnlyDictionary<TKey2, TValue2>(rv);
        }


        /// <summary>
        /// Returns a modifiable list, after first copying the collection.
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>a modifiable list, after first copying the collection.</returns>
        public static IList<T> NewList<T>(IEnumerable<T> collection)
        {
            return NewList<T, T>(collection);
        }

        /// <summary>
        /// Returns a modifiable list, after first copying the collection.
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>a modifiable list, after first copying the collection.</returns>
        public static IList<U> NewList<T, U>(IEnumerable<T> collection)
        {
            IList<U> rv = new List<U>();
            if (collection != null)
            {
                foreach (T element in collection)
                {
                    rv.Add((U)(object)element);
                }
            }
            return rv;
        }

        /// <summary>
        /// Returns a modifiable set, after first copying the collection.
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>a modifiable set, after first copying the collection.</returns>
        public static ICollection<T> NewSet<T>(IEnumerable<T> collection)
        {
            return NewSet<T, T>(collection);
        }

        /// <summary>
        /// Returns a modifiable set, after first copying the array.
        /// </summary>
        /// <param name="collection">An array maybe null.</param>
        /// <returns>a modifiable set, after first copying the array.</returns>
        public static ICollection<T> NewSet<T>(params T[] items)
        {
            return NewSet<T>((IEnumerable<T>)items);
        }

        /// <summary>
        /// Returns a modifiable set, after first copying the collection.
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>a modifiable set, after first copying the collection.</returns>
        public static ICollection<U> NewSet<T, U>(IEnumerable<T> collection)
        {
            ICollection<U> rv = new HashSet<U>();
            if (collection != null)
            {
                foreach (T element in collection)
                {
                    rv.Add((U)(object)element);
                }
            }
            return rv;
        }


        /// <summary>
        /// Returns an unmodifiable list, after first copying the collection.
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>an unmodifiable list, after first copying the collection.</returns>
        public static IList<T> NewReadOnlyList<T>(ICollection<T> collection)
        {
            return NewReadOnlyList<T, T>(collection);
        }

        /// <summary>
        /// Returns an unmodifiable list, after first copying the collection.
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>an unmodifiable list, after first copying the collection.</returns>
        public static IList<U> NewReadOnlyList<T, U>(ICollection<T> collection)
        {
            IList<U> list = NewList<T, U>(collection);
            return new ReadOnlyList<U>(list);
        }

        /// <summary>
        /// Returns an unmodifiable list, after first copying the collection.
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>an unmodifiable list, after first copying the collection.</returns>
        public static ICollection<T> NewReadOnlySet<T>(ICollection<T> collection)
        {
            return NewReadOnlySet<T, T>(collection);
        }

        /// <summary>
        /// Returns an unmodifiable list, after first copying the collection.
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>an unmodifiable list, after first copying the collection.</returns>
        private static ICollection<U> NewReadOnlySet<T, U>(ICollection<T> collection)
        {
            ICollection<U> list = NewSet<T, U>(collection);
            return new ReadOnlyCollection<U>(list);
        }

        /// <summary>
        /// Returns an unmodifiable set, backed by the original
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>an unmodifiable list, after first copying the collection.</returns>
        public static ICollection<T> AsReadOnlySet<T>(ICollection<T> collection)
        {
            ICollection<T> list = NullAsEmpty(collection);
            return new ReadOnlyCollection<T>(list);
        }

        /// <summary>
        /// Returns an unmodifiable list, backed by the original
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>an unmodifiable list, after first copying the collection.</returns>
        public static IList<T> AsReadOnlyList<T>(IList<T> collection)
        {
            IList<T> list = NullAsEmpty(collection);
            return new ReadOnlyList<T>(list);
        }

        /// <summary>
        /// Returns an unmodifiable list, backed by the original
        /// </summary>
        /// <param name="collection">A collection. May be null.</param>
        /// <returns>an unmodifiable list, after first copying the collection.</returns>
        public static IDictionary<K, V> AsReadOnlyDictionary<K, V>(IDictionary<K, V> d)
        {
            d = NullAsEmpty(d);
            return new ReadOnlyDictionary<K, V>(d);
        }

        /// <summary>
        /// Creates a new read-only list from an array.
        /// </summary>
        /// <param name="args"></param>
        /// <returns></returns>
        public static IList<T> NewReadOnlyList<T>(params T[] args)
        {
            return NewReadOnlyList<T, T>(args);
        }

        /// <summary>
        /// Creates a new read-only list from an array.
        /// </summary>
        /// <param name="args"></param>
        /// <returns></returns>
        private static IList<U> NewReadOnlyList<T, U>(params T[] args)
        {
            IList<U> list = CollectionUtil.NewList<T, U>(args);
            return new ReadOnlyList<U>(list);
        }

        /// <summary>
        /// Creates a new read-only set from an array.
        /// </summary>
        /// <param name="args"></param>
        /// <returns></returns>
        public static ICollection<T> NewReadOnlySet<T>(params T[] args)
        {
            return NewReadOnlySet<T, T>(args);
        }
        /// <summary>
        /// Creates a new read-only set from an array.
        /// </summary>
        /// <param name="args"></param>
        /// <returns></returns>
        private static ICollection<U> NewReadOnlySet<T, U>(params T[] args)
        {
            ICollection<U> list = CollectionUtil.NewSet<T, U>(args);
            return new ReadOnlyCollection<U>(list);
        }

        public static bool DictionariesEqual<K, V>(IDictionary<K, V> m1,
                                                  IDictionary<K, V> m2)
        {
            if (m1.Count != m2.Count)
            {
                return false;
            }
            foreach (KeyValuePair<K, V> entry1 in m1)
            {
                K key1 = entry1.Key;
                V val1 = entry1.Value;
                if (!m2.ContainsKey(key1))
                {
                    return false;
                }
                Object val2 = m2[key1];
                if (!CollectionUtil.Equals(val1, val2))
                {
                    return false;
                }
            }
            return true;
        }

        public static bool ListsEqual<T>(IList<T> v1,
                                         IList<T> v2)
        {
            if (v1.Count != v2.Count)
            {
                return false;
            }
            for (int i = 0; i < v1.Count; i++)
            {
                if (!CollectionUtil.Equals(v1[i], v2[i]))
                {
                    return false;
                }
            }
            return true;
        }

        /// <summary>
        /// hashCode function that properly handles arrays,
        /// collections, maps, collections of arrays, and maps of arrays.
        /// </summary>
        /// <param name="o">The object. May be null.</param>
        /// <returns>the hashCode</returns>
        public static int GetHashCode(Object o)
        {
            if (o == null)
            {
                return 0;
            }
            else if (o is Array)
            {
                Array array = (Array)o;
                int length = array.Length;
                int rv = 0;
                for (int i = 0; i < length; i++)
                {
                    Object el = array.GetValue(i);
                    unchecked
                    {
                        rv += CollectionUtil.GetHashCode(el);
                    }
                }
                return rv;
            }
            else if (ReflectionUtil.IsParentTypeOf(typeof(KeyValuePair<,>), o.GetType()))
            {
                Type parent = ReflectionUtil.FindInHierarchyOf(typeof(KeyValuePair<,>), o.GetType());
                Type[] genericArguments =
                    parent.GetGenericArguments();

                Type collectionUtil = typeof(CollectionUtil);
                MethodInfo info = collectionUtil.GetMethod("GetKeyValuePairHashCode");

                info = info.MakeGenericMethod(genericArguments);

                Object rv = info.Invoke(null, new object[] { o });
                return (int)rv;
            }
            else if (ReflectionUtil.IsParentTypeOf(typeof(ICollection<>), o.GetType()))
            {
                Type parent = ReflectionUtil.FindInHierarchyOf(typeof(ICollection<>), o.GetType());

                Type[] genericArguments =
                    parent.GetGenericArguments();


                Type collectionUtil = typeof(CollectionUtil);
                MethodInfo info = collectionUtil.GetMethod("GetEnumerableHashCode");

                info = info.MakeGenericMethod(genericArguments);

                Object rv = info.Invoke(null, new object[] { o });
                return (int)rv;
            }
            else
            {
                return o.GetHashCode();
            }
        }

        /// <summary>
        /// Equality function that properly handles arrays,
        /// lists, maps, lists of arrays, and maps of arrays.
        /// </summary>
        /// <remarks>
        /// <para>
        /// NOTE: For Sets, this relies on the equals method
        /// of the Set to do the right thing. This is a reasonable
        /// assumption since, in order for Sets to behave
        /// properly as Sets, their values must already have
        /// a proper implementation of equals. (Or they must
        /// be specialized Sets that define a custom comparator that
        /// knows how to do the right thing). The same holds true for Map keys.
        /// Map values, on the other hand, are compared (so Map values
        /// can be arrays).
        /// </para>
        /// </remarks>
        /// <param name="o1">The first object. May be null.</param>
        /// <param name="o2">The second object. May be null.</param>
        /// <returns>true iff the two objects are equal.</returns>
        public new static bool Equals(Object o1, Object o2)
        {
            if (o1 == o2)
            { //same object or both null
                return true;
            }
            else if (o1 == null)
            {
                return false;
            }
            else if (o2 == null)
            {
                return false;
            }
            else if (o1 is Array)
            {
                Type clazz1 = o1.GetType();
                Type clazz2 = o2.GetType();
                if (!clazz1.Equals(clazz2))
                {
                    return false;
                }
                Array array1 = (Array)o1;
                Array array2 = (Array)o2;
                int length1 = array1.Length;
                int length2 = array2.Length;
                if (length1 != length2)
                {
                    return false;
                }
                for (int i = 0; i < length1; i++)
                {
                    Object el1 = array1.GetValue(i);
                    Object el2 = array2.GetValue(i);
                    if (!CollectionUtil.Equals(el1, el2))
                    {
                        return false;
                    }
                }
                return true;
            }
            else if (ReflectionUtil.IsParentTypeOf(typeof(IList<>), o1.GetType()))
            {
                Type parent1 = ReflectionUtil.FindInHierarchyOf(typeof(IList<>), o1.GetType());
                Type parent2 = ReflectionUtil.FindInHierarchyOf(typeof(IList<>), o2.GetType());
                if (!parent1.Equals(parent2))
                {
                    return false;
                }
                Type[] genericArguments =
                    parent1.GetGenericArguments();


                Type collectionUtil = typeof(CollectionUtil);
                MethodInfo info = collectionUtil.GetMethod("ListsEqual");

                info = info.MakeGenericMethod(genericArguments);

                Object rv = info.Invoke(null, new object[] { o1, o2 });
                return (bool)rv;
            }
            else if (ReflectionUtil.IsParentTypeOf(typeof(IDictionary<,>), o1.GetType()))
            {
                Type parent1 = ReflectionUtil.FindInHierarchyOf(typeof(IDictionary<,>), o1.GetType());
                Type parent2 = ReflectionUtil.FindInHierarchyOf(typeof(IDictionary<,>), o2.GetType());
                if (!parent1.Equals(parent2))
                {
                    return false;
                }
                Type[] genericArguments =
                    parent1.GetGenericArguments();


                Type collectionUtil = typeof(CollectionUtil);
                MethodInfo info = collectionUtil.GetMethod("DictionariesEqual");

                info = info.MakeGenericMethod(genericArguments);

                Object rv = info.Invoke(null, new object[] { o1, o2 });
                return (bool)rv;
            }
            else if (ReflectionUtil.IsParentTypeOf(typeof(ICollection<>), o1.GetType()))
            {
                Type parent1 = ReflectionUtil.FindInHierarchyOf(typeof(ICollection<>), o1.GetType());
                Type parent2 = ReflectionUtil.FindInHierarchyOf(typeof(ICollection<>), o2.GetType());
                if (!parent1.Equals(parent2))
                {
                    return false;
                }
                Type[] genericArguments =
                    parent1.GetGenericArguments();


                Type collectionUtil = typeof(CollectionUtil);
                MethodInfo info = collectionUtil.GetMethod("SetsEqual");

                info = info.MakeGenericMethod(genericArguments);

                Object rv = info.Invoke(null, new object[] { o1, o2 });
                return (bool)rv;
            }
            else
            {
                return o1.Equals(o2);
            }
        }

    }

}
