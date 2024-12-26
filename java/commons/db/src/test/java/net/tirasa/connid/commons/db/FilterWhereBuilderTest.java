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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * FilterWhereBuilder test class
 *
 * @version $Revision 1.0$
 * @since 1.0
 */
public class FilterWhereBuilderTest {

    private static final SQLParam VALUE = new SQLParam("value", "value");

    private static final String NAME = "name";

    private static final String OPERATOR = "=";

    /**
     * Test method for {@link FilterWhereBuilder#FilterWhereBuilder(String, java.util.Map)}.
     */
    @Test
    public void testFilterQueryBuilder() {
        FilterWhereBuilder actual = new FilterWhereBuilder();
        assertNotNull(actual);
    }

    /**
     * Test method for {@link FilterWhereBuilder#join(String, FilterWhereBuilder, FilterWhereBuilder)}.
     */
    @Test
    public void testJoin() {
        FilterWhereBuilder l = new FilterWhereBuilder();
        final SQLParam param = new SQLParam(NAME, VALUE);
        l.addBind(param, OPERATOR, false);
        FilterWhereBuilder r = new FilterWhereBuilder();
        r.addBind(param, OPERATOR, false);
        FilterWhereBuilder actual = new FilterWhereBuilder();
        actual.join("AND", l, r);
        assertNotNull(actual);
        assertNotNull(actual.getParams());
        assertTrue(actual.getParams().contains(param));
        assertEquals(2, actual.getParams().size());
        assertEquals("name = ? AND name = ?", actual.getWhereClause());
    }

    /**
     * Test method for {@link FilterWhereBuilder#getNames()}.
     */
    @Test
    public void testGetNamesAndValues() {
        FilterWhereBuilder actual = new FilterWhereBuilder();
        assertNotNull(actual);
        assertNotNull(actual.getParams());
        final SQLParam param = new SQLParam(NAME, VALUE);
        actual.addBind(param, OPERATOR, false);
        assertTrue(actual.getParams().contains(param));
    }

    /**
     * Test method for {@link FilterWhereBuilder#getWhere()}.
     */
    @Test
    public void testGetWhere() {
        FilterWhereBuilder actual = new FilterWhereBuilder();
        assertNotNull(actual);
        final SQLParam param = new SQLParam(NAME, VALUE);
        actual.addBind(param, OPERATOR, false);
        assertNotNull(actual.getWhere());
        assertEquals("name = ?", actual.getWhere().toString());
    }

    /**
     * Test method for {@link FilterWhereBuilder#getWhere()}.
     */
    @Test
    public void testEmptyWhere() {
        FilterWhereBuilder actual = new FilterWhereBuilder();
        assertNotNull(actual);
        assertNotNull(actual.getWhere());
        assertEquals("", actual.getWhere().toString());
    }

    /**
     * Test method for {@link FilterWhereBuilder#addBind(String, String, Object)}.
     */
    @Test
    public void testAddBind() {
        FilterWhereBuilder actual = new FilterWhereBuilder();
        assertNotNull(actual);
        assertNotNull(actual.getParams());
        final SQLParam param = new SQLParam(NAME, VALUE);
        actual.addBind(param, OPERATOR, false);
        assertTrue(actual.getParams().contains(param));
        assertEquals("name = ?", actual.getWhereClause());
    }

    /**
     * Test method for {@link FilterWhereBuilder#getWhereClause()}.
     */
    @Test
    public void testGetWhereClause() {
        FilterWhereBuilder actual = new FilterWhereBuilder();
        assertNotNull(actual);
        final SQLParam param = new SQLParam(NAME, VALUE);
        actual.addBind(param, OPERATOR, false);
        assertEquals("name = ?", actual.getWhereClause());
        assertEquals(1, actual.getParams().size());
    }

    /**
     * Test method for {@link FilterWhereBuilder#getWhereClause()}.
     */
    @Test
    public void testGetWhereClauseWithWhere() {
        FilterWhereBuilder actual = new FilterWhereBuilder();
        assertNotNull(actual);
        final SQLParam param = new SQLParam(NAME, VALUE);
        actual.addBind(param, OPERATOR, false);
        assertEquals("name = ?", actual.getWhereClause());
        assertEquals(1, actual.getParams().size());
    }
}
