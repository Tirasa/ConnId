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

public final class Assertions {
    
    private final static String NULL_FORMAT = "Parameter '%s' must not be null.";
    private static final String BLANK_FORMAT = "Parameter '%s' must not be blank.";

    private Assertions() {
        throw new AssertionError();
    }

    /**
     * Throws {@link NullPointerException} if the parameter <code>o</code> is
     * <code>null</code>.
     * 
     * @param o
     *            check if the object is <code>null</code>.
     * @param param
     *            name of the parameter to check for <code>null</code>.
     * @throws NullPointerException
     *             if <code>o</code> is <code>null</code> and constructs a
     *             message with the name of the parameter.
     */
    public static void nullCheck(Object o, String param) {
        assert StringUtil.isNotBlank(param);
        if (o == null) {
            throw new NullPointerException(String.format(NULL_FORMAT, param));
        }
    }

    /**
     * Throws {@link NullPointerException} if the parameter <code>o</code> is
     * <code>null</code>, otherwise returns the value of the <code>o</code> parameter.
     * 
     * @param o
     *            check if the object is <code>null</code>.
     * @param param
     *            name of the parameter to check for <code>null</code>.
     * @return the value of the <code>o</code> parameter.
     * @throws NullPointerException
     *             if <code>o</code> is <code>null</code> and constructs a
     *             message with the name of the parameter.
     *             
     * @since 1.2
     */
    public static <T> T nullChecked(T o, String param) {
        // Avoid calling nullCheck() here to reuse code: it deepens the stack trace.
        // We want the exception to be thrown as close to the call site as possible.
        assert StringUtil.isNotBlank(param);
        if (o == null) {
            throw new NullPointerException(String.format(NULL_FORMAT, param));
        }
        return o;
    }

    /**
     * Throws {@link IllegalArgumentException} if the parameter <code>o</code>
     * is <code>null</code> or blank.
     * 
     * @param o
     *            value to test for blank.
     * @param param
     *            name of the parameter to check.
     */
    public static void blankCheck(String o, String param) {
        assert StringUtil.isNotBlank(param);
        if (StringUtil.isBlank(o)) {
            throw new IllegalArgumentException(String.format(BLANK_FORMAT, param));
        }
    }

    /**
     * Throws {@link IllegalArgumentException} if the parameter <code>o</code>
     * is <code>null</code> or blank, otherwise returns the value of the
     * <code>o</code> parameter.
     * 
     * @param o
     *            value to test for blank.
     * @param param
     *            name of the parameter to check.
     * @return the value of the <code>o</code> parameter.
     *             
     * @since 1.2
     */
    public static String blankChecked(String o, String param) {
        // Avoid calling blankCheck() here to reuse code: it deepens the stack trace.
        // We want the exception to be thrown as close to the call site as possible.
        assert StringUtil.isNotBlank(param);
        if (StringUtil.isBlank(o)) {
            throw new IllegalArgumentException(String.format(BLANK_FORMAT, param));
        }
        return o;
    }
}
