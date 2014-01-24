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
using System;
using System.Collections.Generic;
using NUnit.Framework;
using Org.IdentityConnectors.Common;
namespace FrameworkTests
{
    [TestFixture]
    public class CollectionUtilTests
    {
        [Test]
        public void TestEquals()
        {
            Assert.IsTrue(CollectionUtil.Equals(null, null));
            Assert.IsFalse(CollectionUtil.Equals(null, "str"));
            Assert.IsTrue(CollectionUtil.Equals("str", "str"));

            byte[] arr1 = new byte[] { 1, 2, 3 };
            byte[] arr2 = new byte[] { 1, 2, 3 };
            byte[] arr3 = new byte[] { 1, 2, 4 };
            byte[] arr4 = new byte[] { 1, 2 };
            int[] arr5 = new int[] { 1, 2, 3 };

            Assert.IsTrue(CollectionUtil.Equals(arr1, arr2));
            Assert.IsFalse(CollectionUtil.Equals(arr2, arr3));
            Assert.IsFalse(CollectionUtil.Equals(arr2, arr4));
            Assert.IsFalse(CollectionUtil.Equals(arr2, arr5));

            IList<byte[]> list1 = new List<byte[]>();
            IList<byte[]> list2 = new List<byte[]>();
            list1.Add(arr1);
            list2.Add(arr2);

            Assert.IsTrue(CollectionUtil.Equals(list1, list2));

            list2.Add(arr2);
            Assert.IsFalse(CollectionUtil.Equals(list1, list2));

            list1.Add(arr1);
            Assert.IsTrue(CollectionUtil.Equals(list1, list2));

            list1.Add(arr1);
            list2.Add(arr3);
            Assert.IsFalse(CollectionUtil.Equals(list1, list2));

            IDictionary<String, byte[]> map1 = new Dictionary<String, byte[]>();
            IDictionary<String, byte[]> map2 = new Dictionary<String, byte[]>();
            map1["key1"] = arr1;
            map2["key1"] = arr2;
            Assert.IsTrue(CollectionUtil.Equals(map1, map2));
            map2["key2"] = arr2;
            Assert.IsFalse(CollectionUtil.Equals(map1, map2));
            map1["key2"] = arr1;
            Assert.IsTrue(CollectionUtil.Equals(map1, map2));
            map1["key2"] = arr3;
            Assert.IsFalse(CollectionUtil.Equals(map1, map2));

            ICollection<String> set1 = new HashSet<String>();
            ICollection<String> set2 = new HashSet<String>();
            set1.Add("val");
            set2.Add("val");
            Assert.IsTrue(CollectionUtil.Equals(set1, set2));
            set2.Add("val2");
            Assert.IsFalse(CollectionUtil.Equals(set1, set2));
            set1.Add("val2");
            Assert.IsTrue(CollectionUtil.Equals(set1, set2));
        }
        [Test]
        public void TestHashCode()
        {
            Assert.AreEqual(0, CollectionUtil.GetHashCode(null));
            Assert.AreEqual("str".GetHashCode(), CollectionUtil.GetHashCode("str"));

            byte[] arr1 = new byte[] { 1, 2, 3 };
            byte[] arr2 = new byte[] { 1, 2, 3 };
            byte[] arr3 = new byte[] { 1, 2, 4 };
            byte[] arr4 = new byte[] { 1, 2 };
            int[] arr5 = new int[] { 1, 2, 3 };

            Assert.AreEqual(CollectionUtil.GetHashCode(arr1),
                    CollectionUtil.GetHashCode(arr2));
            Assert.IsFalse(CollectionUtil.GetHashCode(arr2) ==
                    CollectionUtil.GetHashCode(arr3));
            Assert.IsFalse(CollectionUtil.GetHashCode(arr2) ==
                CollectionUtil.GetHashCode(arr4));
            Assert.IsTrue(CollectionUtil.GetHashCode(arr2) ==
                CollectionUtil.GetHashCode(arr5));

            List<byte[]> list1 = new List<byte[]>();
            List<byte[]> list2 = new List<byte[]>();
            list1.Add(arr1);
            list2.Add(arr2);

            Assert.IsTrue(CollectionUtil.GetHashCode(list1) ==
                CollectionUtil.GetHashCode(list2));

            list2.Add(arr2);
            Assert.IsFalse(CollectionUtil.GetHashCode(list1) ==
                CollectionUtil.GetHashCode(list2));

            list1.Add(arr1);
            Assert.IsTrue(CollectionUtil.GetHashCode(list1) ==
                CollectionUtil.GetHashCode(list2));

            list1.Add(arr1);
            list2.Add(arr3);
            Assert.IsFalse(CollectionUtil.GetHashCode(list1) ==
                CollectionUtil.GetHashCode(list2));

            Dictionary<String, byte[]> map1 = new Dictionary<String, byte[]>();
            Dictionary<String, byte[]> map2 = new Dictionary<String, byte[]>();
            map1["key1"] = arr1;
            map2["key1"] = arr2;
            Assert.IsTrue(CollectionUtil.GetHashCode(map1) ==
                CollectionUtil.GetHashCode(map2));
            map2["key2"] = arr2;
            Assert.IsFalse(CollectionUtil.GetHashCode(map1) ==
                CollectionUtil.GetHashCode(map2));
            map1["key2"] = arr1;
            Assert.IsTrue(CollectionUtil.GetHashCode(map1) ==
                CollectionUtil.GetHashCode(map2));
            map1["key2"] = arr3;
            Assert.IsFalse(CollectionUtil.GetHashCode(map1) ==
                CollectionUtil.GetHashCode(map2));

            HashSet<String> set1 = new HashSet<String>();
            HashSet<String> set2 = new HashSet<String>();
            set1.Add("val");
            set2.Add("val");
            Assert.IsTrue(CollectionUtil.GetHashCode(set1) ==
                CollectionUtil.GetHashCode(set2));
            set2.Add("val2");
            Assert.IsFalse(CollectionUtil.GetHashCode(set1) ==
                CollectionUtil.GetHashCode(set2));
            set1.Add("val2");
            Assert.IsTrue(CollectionUtil.GetHashCode(set1) ==
                CollectionUtil.GetHashCode(set2));
        }
    }
}