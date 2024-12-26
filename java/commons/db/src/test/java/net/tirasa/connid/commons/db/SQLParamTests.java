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

import java.sql.Types;
import org.junit.jupiter.api.Test;

/**
 * The SQL util tests
 *
 * @version $Revision 1.0$
 * @since 1.0
 */
public class SQLParamTests {

    /**
     * Test method
     */
    @Test
    public void paramCreateValue() {
        SQLParam a = new SQLParam("A", "B", 5);
        assertEquals("A", a.getName());
        assertEquals("B", a.getValue());
        assertEquals(5, a.getSqlType());
    }

    /**
     * Test method
     */
    @Test
    public void paramCreateNoSQLType() {
        SQLParam a = new SQLParam("A", "B");
        assertEquals("A", a.getName());
        assertEquals("B", a.getValue());
    }

    /**
     * Test method
     */
    @Test
    public void paramTestHashEqual2() {
        SQLParam a = new SQLParam("A", "B");
        SQLParam b = new SQLParam("A", "B");
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, b);
    }

    /**
     * Test method
     */
    @Test
    public void paramTestHashEqual3() {
        SQLParam a = new SQLParam("A", "B", Types.VARCHAR);
        SQLParam b = new SQLParam("A", "B", Types.VARCHAR);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, b);
    }

    /**
     * Test method
     */
    @Test
    public void toStringTest() {
        SQLParam a = new SQLParam("A", "B", Types.VARCHAR);
        assertEquals("A=\"B\":[VARCHAR]", a.toString());
    }
}
