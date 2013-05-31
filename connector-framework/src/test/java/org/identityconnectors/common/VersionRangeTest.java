/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.identityconnectors.common;

import org.testng.Assert;
import org.testng.annotations.Test;

public class VersionRangeTest {

    @Test
    public void testIsInRange() throws Exception {
        Version reference0 = new Version(1, 1, 0, 0);
        Version reference1 = new Version(1, 1, 0, 1);
        Version reference2 = new Version(1, 1, 0, 2);
        Version reference3 = new Version(1, 1, 0, 3);
        Version reference4 = new Version(1, 1, 0, 4);
        VersionRange range = VersionRange.parse("[1.1.0.1,1.1.0.3)");

        Assert.assertFalse(range.isInRange(reference0));
        Assert.assertTrue(range.isInRange(reference1));
        Assert.assertTrue(range.isInRange(reference2));
        Assert.assertFalse(range.isInRange(reference3));
        Assert.assertFalse(range.isInRange(reference4));
    }

    @Test
    public void testIsExact() throws Exception {
        Assert.assertTrue(VersionRange.parse("1.1.0.0").isExact());
        Assert.assertTrue(VersionRange.parse("  [  1 , 1 ]  ").isExact());
        Assert.assertTrue(VersionRange.parse("[  1.1 , 1.1 ]").isExact());
        Assert.assertTrue(VersionRange.parse("  [1.1.1 , 1.1.1]  ").isExact());
        Assert.assertTrue(VersionRange.parse("[1.1.0.0,1.1.0.0]").isExact());
        Assert.assertTrue(VersionRange.parse("(1.1.0.0,1.1.0.2)").isExact());
    }

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertTrue(VersionRange.parse("(1.1.0.0,1.1.0.0)").isEmpty());
        Assert.assertTrue(VersionRange.parse("(1.2.0.0,1.1.0.0]").isEmpty());
    }

    @Test
    public void testValidSyntax() throws Exception {
        try {
            VersionRange.parse("(1.1.0.0)");
            Assert.fail("Invalid syntax not failed");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            VersionRange.parse("1.1.0.0,1.1)]");
            Assert.fail("Invalid syntax not failed");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            VersionRange.parse("(1.1.0.0-1.1)");
            Assert.fail("Invalid syntax not failed");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            VersionRange.parse("1.1.0.0,1.1");
            Assert.fail("Invalid syntax not failed");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            VersionRange.parse("( , 1.1)");
            Assert.fail("Invalid syntax not failed");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void testIsEqual() throws Exception {
        VersionRange range1 = VersionRange.parse("[1.1.0.1,1.1.0.3)");
        VersionRange range2 = VersionRange.parse(range1.toString());
        Assert.assertTrue(range1.equals(range2));
    }
}
