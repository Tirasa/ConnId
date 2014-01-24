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
package org.identityconnectors.framework.common.objects;

import static org.identityconnectors.framework.common.objects.AttributeInfoBuilder.build;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.testng.annotations.Test;

public class AttributeInfoUtilTests {

    @Test
    public void testIsMethod() {
        assertTrue(build("fad").is("Fad"));
        assertFalse(build("fadsf").is("f"));
    }

    @Test
    public void testFindMethod() {
        AttributeInfo expected = build("FIND_ME");
        Set<AttributeInfo> attrs = new HashSet<AttributeInfo>();
        attrs.add(build("fadsf"));
        attrs.add(build("fadsfadsf"));
        attrs.add(expected);
        assertEquals(AttributeInfoUtil.find("FIND_ME", attrs), expected);
        assertTrue(AttributeInfoUtil.find("Daffff", attrs) == null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testPasswordBuild() {
        AttributeInfoBuilder.build(OperationalAttributes.PASSWORD_NAME);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCurrentPasswordBuild() {
        AttributeInfoBuilder.build(OperationalAttributes.CURRENT_PASSWORD_NAME);
    }

    @Test
    public void testRegularPassword() {
        AttributeInfoBuilder.build(OperationalAttributes.PASSWORD_NAME, GuardedString.class);
    }
}
