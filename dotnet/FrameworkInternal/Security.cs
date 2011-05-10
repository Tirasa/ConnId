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

using Org.IdentityConnectors.Common.Security;
using System.Text;
using System.Security.Cryptography;

namespace Org.IdentityConnectors.Common.Security.Impl
{
    public class EncryptorFactoryImpl : EncryptorFactory
    {
        private readonly Encryptor _defaultEncryptor;

        public EncryptorFactoryImpl()
        {
            _defaultEncryptor = new EncryptorImpl();
        }

        public override Encryptor GetDefaultEncryptor()
        {
            return _defaultEncryptor;
        }
    }

    public class EncryptorImpl : Encryptor
    {
        private readonly static byte[] _defaultKeyBytes =
        {
            (byte) 0x23,(byte) 0x65,(byte) 0x87,(byte) 0x22,
            (byte) 0x59,(byte) 0x78,(byte) 0x54,(byte) 0x43,
            (byte) 0x64,(byte) 0x05,(byte) 0x6A,(byte) 0xBD,
            (byte) 0x34,(byte) 0xA2,(byte) 0x34,(byte) 0x57,
        };
        private readonly static byte[] _defaultIvBytes =
        {
            (byte) 0x51,(byte) 0x65,(byte) 0x22,(byte) 0x23,
            (byte) 0x64,(byte) 0x05,(byte) 0x6A,(byte) 0xBE,
            (byte) 0x51,(byte) 0x65,(byte) 0x22,(byte) 0x23,
            (byte) 0x64,(byte) 0x05,(byte) 0x6A,(byte) 0xBE,
        };

        public EncryptorImpl()
        {
        }

        public UnmanagedArray<byte> Decrypt(byte[] bytes)
        {
            using (SymmetricAlgorithm algo = Aes.Create())
            {
                algo.Padding = PaddingMode.PKCS7;
                algo.Mode = CipherMode.CBC;
                algo.Key = _defaultKeyBytes;
                algo.IV = _defaultIvBytes;
                using (ICryptoTransform transform = algo.CreateDecryptor())
                {
                    return Decrypt2(bytes, transform);
                }
            }
        }

        private unsafe UnmanagedArray<byte> Decrypt2(byte[] bytes, ICryptoTransform transform)
        {
            byte[] managedBytes = transform.TransformFinalBlock(bytes, 0, bytes.Length);
            //pin it ASAP. this is a small race condition that is unavoidable due
            //to the way the crypto API's return byte[]. 
            fixed (byte* dummy = managedBytes)
            {
                try
                {
                    UnmanagedByteArray rv = new UnmanagedByteArray(managedBytes.Length);
                    for (int i = 0; i < rv.Length; i++)
                    {
                        rv[i] = managedBytes[i];
                    }
                    return rv;
                }
                finally
                {
                    SecurityUtil.Clear(managedBytes);
                }
            }
        }

        public byte[] Encrypt(UnmanagedArray<byte> bytes)
        {
            using (SymmetricAlgorithm algo = Aes.Create())
            {
                algo.Padding = PaddingMode.PKCS7;
                algo.Mode = CipherMode.CBC;
                algo.Key = _defaultKeyBytes;
                algo.IV = _defaultIvBytes;
                using (ICryptoTransform transform = algo.CreateEncryptor())
                {
                    return Encrypt2(bytes, transform);
                }
            }
        }

        private unsafe byte[] Encrypt2(UnmanagedArray<byte> bytes, ICryptoTransform transform)
        {
            byte[] managedBytes = new byte[bytes.Length];
            fixed (byte* dummy = managedBytes)
            {
                try
                {
                    SecurityUtil.UnmanagedBytesToManagedBytes(bytes, managedBytes);
                    byte[] rv = transform.TransformFinalBlock(managedBytes, 0, managedBytes.Length);
                    return rv;
                }
                finally
                {
                    SecurityUtil.Clear(managedBytes);
                }
            }
        }
    }
}