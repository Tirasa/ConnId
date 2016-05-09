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
 * Portions Copyrighted 2016 Evolveum
 */
package org.identityconnectors.common.security.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.Encryptor;

public class EncryptorImpl implements Encryptor {

    private static final String ALGORITHM = "AES";

    private static final String FULL_ALGORITHM = "AES/CBC/PKCS5Padding";

    private final static byte[] DEFAULT_KEY_BYTES = {
        (byte) 0x23, (byte) 0x65, (byte) 0x87, (byte) 0x22,
        (byte) 0x59, (byte) 0x78, (byte) 0x54, (byte) 0x43,
        (byte) 0x64, (byte) 0x05, (byte) 0x6A, (byte) 0xBD,
        (byte) 0x34, (byte) 0xA2, (byte) 0x34, (byte) 0x57, };

    private final static byte[] DEFAULT_IV_BYTES = {
        (byte) 0x51, (byte) 0x65, (byte) 0x22, (byte) 0x23,
        (byte) 0x64, (byte) 0x05, (byte) 0x6A, (byte) 0xBE,
        (byte) 0x51, (byte) 0x65, (byte) 0x22, (byte) 0x23,
        (byte) 0x64, (byte) 0x05, (byte) 0x6A, (byte) 0xBE, };

    private static final int FALLBACK_KEY_SIZE = 128;

    private static final Log LOG = Log.getLog(EncryptorImpl.class);

    private Key key;

    private IvParameterSpec iv;

    private boolean defaultKey;

    public EncryptorImpl(boolean defaultKey) {
        this.defaultKey = defaultKey;
        if (defaultKey) {
            key = new SecretKeySpec(DEFAULT_KEY_BYTES, ALGORITHM);
            iv = new IvParameterSpec(DEFAULT_IV_BYTES);
        } else {
            try {
                key = KeyGenerator.getInstance(ALGORITHM).generateKey();
                iv = new IvParameterSpec(DEFAULT_IV_BYTES);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Do a test encryption. It happens sometimes that the key generator
            // generates a key that is not supported by the cipher.
            try {
                Cipher cipher = Cipher.getInstance(FULL_ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
                cipher.doFinal("A".getBytes("UTF-8"));
            } catch (InvalidKeyException e) {

                LOG.warn("Error in self-test with default generated key (algorithm={0}, keySize(encoded)={1}), "
                        + "will fall back to default key size. Original error: {2}",
                        ALGORITHM, key.getEncoded().length * 8, e.getMessage());

                // Fall back to known key size supported by all JCE implementations
                KeyGenerator keyGenerator;
                try {
                    keyGenerator = KeyGenerator.getInstance(ALGORITHM);
                } catch (NoSuchAlgorithmException e1) {
                    throw new RuntimeException(e1.getMessage() + " (algorithm=" + ALGORITHM + ")", e1);
                }
                keyGenerator.init(FALLBACK_KEY_SIZE);
                key = keyGenerator.generateKey();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            LOG.ok("Using cipher algorithm={0}, keySize(encoded)={1}, defaultKey={2}",
                    FULL_ALGORITHM, key.getEncoded().length * 8, defaultKey);
        }
    }

    @Override
    public byte[] decrypt(byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(FULL_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(bytes);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e.getMessage() + " (algorithm=" + FULL_ALGORITHM
                    + ", keySize(encoded)=" + key.getEncoded().length * 8 + ", defaultKey=" + defaultKey + ")", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encrypt(byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(FULL_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return cipher.doFinal(bytes);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e.getMessage() + " (algorithm=" + FULL_ALGORITHM
                    + ", keySize(encoded)=" + key.getEncoded().length * 8 + ", defaultKey=" + defaultKey + ")", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
