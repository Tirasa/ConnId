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
 * Portions Copyrighted 2011 ConnId.
 */
package net.tirasa.connid.commons.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author kitko
 *
 */
public class JNDIUtilTest {

    @Test
    public void testArrayToHashtableSuc() {
        String[] entries1 = { "a=A", "b=B" };
        Map<String, String> res1 = new HashMap<>();
        res1.put("a", "A");
        res1.put("b", "B");
        assertEquals(res1, JNDIUtil.arrayToProperties(entries1, null));
    }

    /**
     * test for testArrayToHashtable fail
     */
    @Test
    public void testArrayToHashtableFail() {
        try {
            String[] entries2 = { "a=A", "b=" };
            JNDIUtil.arrayToProperties(entries2, null);
            fail();
        } catch (RuntimeException e) {
            //expected
        }
        try {
            String[] entries2 = { "a=A", "=" };
            JNDIUtil.arrayToProperties(entries2, null);
            fail();
        } catch (RuntimeException e) {
            //expected
        }
        try {
            String[] entries2 = { "a=A", "=B" };
            JNDIUtil.arrayToProperties(entries2, null);
            fail();
        } catch (RuntimeException e) {
            //expected
        }
    }

    /**
     * test for testArrayToHashtable fail
     */
    @Test
    public void testArrayToHashtableNull() {
        JNDIUtil.arrayToProperties(null, null);
        String[] entries2 = {};
        JNDIUtil.arrayToProperties(entries2, null);
        String[] entries3 = { null, null };
        JNDIUtil.arrayToProperties(entries3, null);
        String[] entries4 = { "", " " };
        JNDIUtil.arrayToProperties(entries4, null);
    }
}
