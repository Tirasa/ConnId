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
 * Portions Copyrighted 2022 ConnId
 */
package org.identityconnectors.common.security;

public abstract class EncryptorFactory {

    // At some point we might make this pluggable, but for now, hard-code
    private static final String IMPL_NAME =
            "org.identityconnectors.common.security.impl.EncryptorFactoryImpl";

    private static EncryptorFactory instance;

    /**
     * Get the singleton instance of the {@link EncryptorFactory}.
     */
    public static synchronized EncryptorFactory getInstance() {
        if (instance == null) {
            try {
                Class<?> clazz = Class.forName(IMPL_NAME);
                Object object = clazz.getDeclaredConstructor().newInstance();
                instance = EncryptorFactory.class.cast(object);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    /**
     * Default encryptor that encrypts/descrypts using a default key.
     */
    public abstract Encryptor getDefaultEncryptor();

    /**
     * Creates a new encryptor initialized with a random encryption key.
     */
    public abstract Encryptor newRandomEncryptor();

}
