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
using System.Security;
using System.Runtime.InteropServices;
using System.Text;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Common.Serializer;
using NUnit.Framework;

namespace FrameworkTests
{
    [TestFixture]
    public class GuardedByteArrayTests
    {
        [Test]
        public void TestBasics()
        {
            GuardedByteArray ss = new GuardedByteArray();
            ss.AppendByte(0x00);
            ss.AppendByte(0x01);
            ss.AppendByte(0x02);
            byte[] decrypted = DecryptToByteArray(ss);
            Assert.AreEqual(new byte[] { 0x00, 0x01, 0x02 }, decrypted);
            String hash = ss.GetBase64SHA1Hash();
            Assert.IsTrue(ss.VerifyBase64SHA1Hash(hash));
            ss.AppendByte(0x03);
            Assert.IsFalse(ss.VerifyBase64SHA1Hash(hash));
        }
        [Test]
        public void TestRange()
        {

            for (byte i = 0; i < 0xFF; i++)
            {
                byte expected = i;
                GuardedByteArray gba = new GuardedByteArray();
                gba = (GuardedByteArray)SerializerUtil.CloneObject(gba);
                gba.AppendByte(i);
                gba.Access(clearChars =>
                {
                    int v = (byte)clearChars[0];
                    Assert.AreEqual(expected, v);
                });

            }
        }

        [Test]
        public void TestEquals()
        {
            GuardedByteArray arr1 = new GuardedByteArray();
            GuardedByteArray arr2 = new GuardedByteArray();
            Assert.AreEqual(arr1, arr2);
            arr2.AppendByte(0x02);
            Assert.AreNotEqual(arr1, arr2);
            arr1.AppendByte(0x02);
            Assert.AreEqual(arr1, arr2);
        }


        /// <summary>
        /// Highly insecure method! Do not do this in production
        /// code.
        /// </summary>
        /// <remarks>
        /// This is only for test purposes
        /// </remarks>
        private byte[] DecryptToByteArray(GuardedByteArray str)
        {
            byte[] result = null;
            str.Access(
                                            array =>
                                            {
                                                result = new byte[array.Length];
                                                for (int i = 0; i < array.Length; i++)
                                                {
                                                    result[i] = array[i];
                                                }
                                            });
            return result;
        }
    }
}