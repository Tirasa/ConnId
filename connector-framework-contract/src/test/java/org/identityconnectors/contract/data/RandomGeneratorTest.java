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
package org.identityconnectors.contract.data;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.identityconnectors.common.security.GuardedString;
import org.testng.annotations.Test;

/**
 * JUnit test class for RandomGenerator
 *
 * @author David Adam
 *
 */
public class RandomGeneratorTest {

    @Test
    public void testRandomLongGenerator() {
        {
            Object o = RandomGenerator.generate("#####", Long.class);
            assertNotNull(o);
            assertTrue(o instanceof Long);
            System.out.println(o.toString());
        }

    }

    @Test
    public void testRgen2() {
        {
            Object o = RandomGenerator.generate("###X##");
            assertNotNull(o);
            assertTrue(o.toString().contains("X"));
            System.out.println(o.toString());
        }
    }

    @Test
    public void testRgen3() {
        {
            Object o = RandomGenerator.generate("###\\.##", Float.class); // this
                                                                          // means
            // ###\.##
            assertNotNull(o);
            assertTrue(o instanceof Float);
            assertTrue(o.toString().contains("."));
            System.out.println(o.toString());
        }
    }

    @Test
    public void testUnique() {
        {
            Object o = RandomGenerator.generate("###X##");
            Object o2 = RandomGenerator.generate("###X##");
            assertNotNull(o);
            assertNotNull(o2);
            assertTrue(o.toString().contains("X"));
            assertTrue(o2.toString().contains("X"));
            assertTrue(!o2.equals(o));
            System.out.println(o.toString() + "\n" + o2.toString());
        }
    }

    @Test
    public void testGuardedStr() {
        Object o = RandomGenerator.generate("\\a\\h###\\s\\h", GuardedString.class);
        assertTrue(o instanceof GuardedString);
        GuardedString pass = (GuardedString) o;
        pass.access(new GuardedString.Accessor() {
            public void access(char[] clearChars) {
                final String result = new String(clearChars);
                assertTrue(result.startsWith("ah"));
                assertTrue(result.endsWith("sh"));
            }
        });
    }

    @Test
    public void testChar() {
        Object o = RandomGenerator.generate("A", Character.class);
        assertNotNull(o);
        assertTrue(o instanceof Character);
    }

}
