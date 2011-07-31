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
package org.identityconnectors.contract.data.groovy;

import org.identityconnectors.contract.data.RandomGenerator;

/**
 * <p>
 * Generate random strings based on given pattern
 * </p>
 * <p>
 * This is a Helper class, users of tests will access methods:
 * {@link Lazy#random(Object)} and {@link Lazy#get(Object)})
 * </p>
 * 
 * @author Zdenek Louzensky
 * 
 */
public class Random extends Lazy {

    private Class<?> clazz;

    /**
     * Creates a random string based on given pattern.
     * 
     * @param pattern
     *            format of pattern
     * @see {@link org.identityconnectors.contract.data.RandomGenerator#generate(String)}
     */
    protected Random(Object pattern) {
        this(pattern, String.class);
    }

    /**
     * Creates a random object of given type
     * 
     * @param pattern
     * @see {@link org.identityconnectors.contract.data.RandomGenerator#generate(String)}
     * @param clazz
     *            the class that will be the type of generated object
     */
    protected Random(Object pattern, Class<?> clazz) {
        value = pattern;
        this.clazz = clazz;
    }

    /**
     * create a random value, that is created once. However further queries will
     * return the same random value.
     */
    public Object generate() {
        return RandomGenerator.generate(value.toString(), clazz);
    }

    /**
     * @return the type of generated random class
     */
    public Class<?> getClazz() {
        return clazz;
    }
}
