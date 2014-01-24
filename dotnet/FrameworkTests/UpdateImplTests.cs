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
using System.Security;
using NUnit.Framework;
using System.Collections.Generic;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Impl.Api.Local.Operations;
using Org.IdentityConnectors.Framework.Common.Objects;
namespace FrameworkTests
{
    /// <summary>
    /// Description of UpdateImplTests.
    /// </summary>
    [TestFixture]
    public class UpdateImplTests
    {
        [Test]
        [ExpectedException(typeof(ArgumentNullException))]
        public void ValidateUidArg()
        {
            UpdateImpl.ValidateInput(ObjectClass.ACCOUNT, null, new HashSet<ConnectorAttribute>(), true);
        }
        [Test]
        [ExpectedException(typeof(ArgumentNullException))]
        public void ValidateObjectClassArg()
        {
            UpdateImpl.ValidateInput(null, new Uid("foo"), new HashSet<ConnectorAttribute>(), true);
        }

        [Test]
        [ExpectedException(typeof(ArgumentNullException))]
        public void ValidateAttrsArg()
        {
            UpdateImpl.ValidateInput(ObjectClass.ACCOUNT, new Uid("foo"), null, true);
        }

        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void ValidateUidAttribute()
        {
            HashSet<ConnectorAttribute> attrs = new HashSet<ConnectorAttribute>();
            attrs.Add(new Uid("foo"));
            UpdateImpl.ValidateInput(ObjectClass.ACCOUNT, new Uid("foo"), attrs, true);
        }

        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void ValidateAddWithNullAttribute()
        {
            ICollection<ConnectorAttribute> attrs = new HashSet<ConnectorAttribute>();
            attrs.Add(ConnectorAttributeBuilder.Build("something"));
            UpdateImpl.ValidateInput(ObjectClass.ACCOUNT, new Uid("foo"), attrs, true);
        }

        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void ValidateAttemptToAddName()
        {
            ICollection<ConnectorAttribute> attrs = new HashSet<ConnectorAttribute>();
            attrs.Add(new Name("fadf"));
            UpdateImpl.ValidateInput(ObjectClass.ACCOUNT, new Uid("foo"), attrs, true);
        }

        [Test]
        public void ValidateAttemptToAddDeleteOperationalAttribute()
        {
            // list of all the operational attributes..
            ICollection<ConnectorAttribute> list = new List<ConnectorAttribute>();
            list.Add(ConnectorAttributeBuilder.BuildEnabled(false));
            list.Add(ConnectorAttributeBuilder.BuildLockOut(true));
            list.Add(ConnectorAttributeBuilder.BuildCurrentPassword(newSecureString("fadsf")));
            list.Add(ConnectorAttributeBuilder.BuildPasswordExpirationDate(DateTime.Now));
            list.Add(ConnectorAttributeBuilder.BuildPassword(newSecureString("fadsf")));
            foreach (ConnectorAttribute attr in list)
            {
                ICollection<ConnectorAttribute> attrs = new HashSet<ConnectorAttribute>();
                attrs.Add(attr);
                try
                {
                    UpdateImpl.ValidateInput(ObjectClass.ACCOUNT, new Uid("1"), attrs, true);
                    Assert.Fail("Failed: " + attr.Name);
                }
                catch (ArgumentException)
                {
                    // this is a good thing..
                }
            }
        }

        private static SecureString newSecureString(string password)
        {
            SecureString rv = new SecureString();
            foreach (char c in password.ToCharArray())
            {
                rv.AppendChar(c);
            }
            return rv;
        }

        /// <summary>
        /// Validate two collections are equal.  (Not fast but effective)
        /// </summary>
        public static bool AreEqual(ICollection<ConnectorAttribute> arg1,
                                    ICollection<ConnectorAttribute> arg2)
        {
            if (arg1.Count != arg2.Count)
            {
                return false;
            }
            foreach (ConnectorAttribute attr in arg1)
            {
                if (!arg2.Contains(attr))
                {
                    return false;
                }
            }
            return true;
        }
        [Test]
        public void MergeAddAttribute()
        {
            UpdateImpl up = new UpdateImpl(null, null);
            ICollection<ConnectorAttribute> actual;
            ICollection<ConnectorAttribute> baseAttrs = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> expected = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> changeset = CollectionUtil.NewSet<ConnectorAttribute>();
            // attempt to add a value to an attribute..
            ConnectorAttribute cattr = ConnectorAttributeBuilder.Build("abc", 2);
            changeset.Add(cattr);
            expected.Add(ConnectorAttributeBuilder.Build("abc", 2));
            actual = up.Merge(changeset, baseAttrs, true);
            Assert.IsTrue(AreEqual(expected, actual));
        }

        [Test]
        public void MergeAddToExistingAttribute()
        {
            UpdateImpl up = new UpdateImpl(null, null);
            ICollection<ConnectorAttribute> actual;
            ICollection<ConnectorAttribute> baseAttrs = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> expected = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> changeset = CollectionUtil.NewSet<ConnectorAttribute>();
            // attempt to add a value to an attribute..
            ConnectorAttribute battr = ConnectorAttributeBuilder.Build("abc", 1);
            ConnectorAttribute cattr = ConnectorAttributeBuilder.Build("abc", 2);
            baseAttrs.Add(battr);
            changeset.Add(cattr);
            expected.Add(ConnectorAttributeBuilder.Build("abc", 1, 2));
            actual = up.Merge(changeset, baseAttrs, true);
            Assert.IsTrue(AreEqual(expected, actual));
        }

        [Test]
        public void MergeDeleteNonExistentAttribute()
        {
            UpdateImpl up = new UpdateImpl(null, null);
            ICollection<ConnectorAttribute> actual;
            ICollection<ConnectorAttribute> baseAttrs = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> expected = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> changeset = CollectionUtil.NewSet<ConnectorAttribute>();
            // attempt to add a value to an attribute..
            ConnectorAttribute cattr = ConnectorAttributeBuilder.Build("abc", 2);
            changeset.Add(cattr);
            actual = up.Merge(changeset, baseAttrs, false);
            Assert.IsTrue(AreEqual(expected, actual));
        }

        [Test]
        public void MergeDeleteToExistingAttribute()
        {
            UpdateImpl up = new UpdateImpl(null, null);
            ICollection<ConnectorAttribute> actual;
            ICollection<ConnectorAttribute> baseAttrs = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> expected = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> changeset = CollectionUtil.NewSet<ConnectorAttribute>();
            // attempt to add a value to an attribute..
            ConnectorAttribute battr = ConnectorAttributeBuilder.Build("abc", 1, 2);
            ConnectorAttribute cattr = ConnectorAttributeBuilder.Build("abc", 2);
            baseAttrs.Add(battr);
            changeset.Add(cattr);
            expected.Add(ConnectorAttributeBuilder.Build("abc", 1));
            actual = up.Merge(changeset, baseAttrs, false);
            Assert.IsTrue(AreEqual(expected, actual));
        }

        [Test]
        public void MergeDeleteToExistingAttributeCompletely()
        {
            UpdateImpl up = new UpdateImpl(null, null);
            ICollection<ConnectorAttribute> actual;
            ICollection<ConnectorAttribute> baseAttrs = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> expected = CollectionUtil.NewSet<ConnectorAttribute>();
            ICollection<ConnectorAttribute> changeset = CollectionUtil.NewSet<ConnectorAttribute>();
            // attempt to add a value to an attribute..
            ConnectorAttribute battr = ConnectorAttributeBuilder.Build("abc", 1, 2);
            ConnectorAttribute cattr = ConnectorAttributeBuilder.Build("abc", 1, 2);
            baseAttrs.Add(battr);
            changeset.Add(cattr);
            expected.Add(ConnectorAttributeBuilder.Build("abc"));
            actual = up.Merge(changeset, baseAttrs, false);
            Assert.IsTrue(AreEqual(expected, actual));
        }
    }
}
