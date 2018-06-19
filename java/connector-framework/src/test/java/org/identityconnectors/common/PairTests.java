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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests the pair object.
 */
public class PairTests {

    @Test
    public void equals() {
        Pair<String, String> a = new Pair<>("a", "b");
        Pair<String, String> b = new Pair<>("a", "b");
        assertTrue(a.equals(b));
        assertFalse(a == null);
        assertFalse(b == null);
        assertFalse(a.equals("f"));
    }

    @Test
    public void hash() {
        Set<Pair<Integer, Integer>> set = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            Pair<Integer, Integer> pair = new Pair<>(i, i + 1);
            Pair<Integer, Integer> tst = new Pair<>(i, i + 1);
            set.add(pair);
            assertTrue(set.contains(tst));
        }
        // check that each pair is unique..
        assertEquals(20, set.size());
    }
}
