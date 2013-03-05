/**
 * ====================
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2011-2013 Tirasa. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License"). You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at https://oss.oracle.com/licenses/CDDL.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.common;

import org.junit.Assert;
import org.junit.Test;

public class UtilTests {

    @Test
    public void testForceCompare() {
        Integer i1 = 1, i2 = 2;
        int cmp = CollectionUtil.forceCompare(i1, i2);
        Assert.assertEquals(-1, cmp);
    }

    @Test
    public void testGetMethodName() {
        String expected = ReflectionUtil.getMethodName(1);
        Assert.assertEquals(expected, "testGetMethodName");
    }
}
