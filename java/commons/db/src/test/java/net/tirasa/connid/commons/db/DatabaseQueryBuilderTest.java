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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.tirasa.connid.commons.db.DatabaseQueryBuilder.OrderBy;
import org.junit.jupiter.api.Test;

/**
 * DatabaseQueryBuilder test Class
 *
 * @version $Revision 1.0$
 * @since 1.0
 */
public class DatabaseQueryBuilderTest {

    private static final String SELECT = "SELECT * FROM Users";

    private static final String SELECT_WITH_WHERE = "SELECT * FROM Users WHERE test = 1";

    private static final SQLParam VALUE = new SQLParam("value", "value");

    private static final String NAME = "name";

    private static final String OPERATOR = "=";

    /**
     * Test method for {@link DatabaseQueryBuilder#DatabaseQueryBuilder(String, Set)}.
     */
    @Test
    public void testFilterQueryBuilderTableMissing() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseQueryBuilder("", null).getSQL());
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#DatabaseQueryBuilder(String, Set)}.
     */
    @Test
    public void testFilterQueryBuilderColumnMissing() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseQueryBuilder("table", null).getSQL());
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#DatabaseQueryBuilder(String, Set)}.
     */
    @Test
    public void testFilterQueryBuilderColumnEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseQueryBuilder("table", new HashSet<>()).getSQL());
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#DatabaseQueryBuilder(String)}.
     */
    @Test
    public void testFilterQueryBuilderSelectMissing() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseQueryBuilder("").getSQL());
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#DatabaseQueryBuilder(String)}.
     */
    @Test
    public void testFilterQueryBuilderWhereMissing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseQueryBuilder(SELECT.substring(0, 7), null).getSQL());
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#DatabaseQueryBuilder(String)}.
     */
    @Test
    public void testFilterQueryBuilder() {
        DatabaseQueryBuilder actual = new DatabaseQueryBuilder(SELECT);
        assertNotNull(actual);
        assertNotNull(actual.getSQL());
        assertEquals(SELECT, actual.getSQL());
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#getSQL()}.
     */
    @Test
    public void testGetSql() {
        FilterWhereBuilder where = new FilterWhereBuilder();
        final SQLParam param = new SQLParam(NAME, VALUE);
        where.addBind(param, OPERATOR, false);
        DatabaseQueryBuilder actual = new DatabaseQueryBuilder(SELECT);
        actual.setWhere(where);
        assertNotNull(actual);
        assertEquals(SELECT + " WHERE name = ?", actual.getSQL());
        assertEquals(1, actual.getParams().size());
        assertEquals(param, actual.getParams().get(0));
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#getSQL()}.
     */
    @Test
    public void testGetSqlWithWhere() {
        FilterWhereBuilder where = new FilterWhereBuilder();
        final SQLParam param = new SQLParam(NAME, VALUE);
        where.addBind(param, OPERATOR, false);
        DatabaseQueryBuilder actual = new DatabaseQueryBuilder(SELECT_WITH_WHERE);
        actual.setWhere(where);
        assertNotNull(actual);
        assertEquals("SELECT * FROM Users WHERE ( test = 1) AND ( name = ? )", actual.getSQL());
        assertEquals(1, actual.getParams().size());
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#getSQL()}.
     */
    @Test
    public void testGetSqlWithEmptyWhere() {
        FilterWhereBuilder where = new FilterWhereBuilder();
        DatabaseQueryBuilder actual = new DatabaseQueryBuilder(SELECT_WITH_WHERE);
        actual.setWhere(where);
        assertNotNull(actual);
        assertEquals(SELECT_WITH_WHERE, actual.getSQL());
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#getSQL()}.
     */
    @Test
    public void testGetSqlWithAttributesToGet() {
        Set<String> attributesToGet = new LinkedHashSet<>();
        attributesToGet.add("test1");
        attributesToGet.add("test2");
        FilterWhereBuilder where = new FilterWhereBuilder();
        final SQLParam param = new SQLParam(NAME, VALUE);
        where.addBind(param, OPERATOR, false);
        DatabaseQueryBuilder actual = new DatabaseQueryBuilder("table", attributesToGet);
        actual.setWhere(where);
        assertEquals("SELECT test1 , test2 FROM table WHERE name = ?", actual.getSQL());
        assertEquals(1, actual.getParams().size());
        assertEquals(param, actual.getParams().get(0));
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#getSQL()}.
     */
    @Test
    public void testGetSqlWithAttributesToGetDifferentQuoting() {
        Set<String> attributesToGet = new LinkedHashSet<>();
        attributesToGet.add("test1");
        attributesToGet.add("test2");
        FilterWhereBuilder where = new FilterWhereBuilder();
        final SQLParam param = new SQLParam(NAME, VALUE);
        where.addBind(param, OPERATOR, false);
        DatabaseQueryBuilder actual = new DatabaseQueryBuilder("table", attributesToGet);
        actual.setWhere(where);
        actual.setTableName("table");
        assertNotNull(actual);
        assertEquals("SELECT test1 , test2 FROM table WHERE name = ?", actual.getSQL());
        assertEquals(1, actual.getParams().size());
        assertEquals(param, actual.getParams().get(0));
    }

    /**
     * Test method for {@link DatabaseQueryBuilder#getSQL()}.
     */
    @Test
    public void testGetSqlWithAttributesToGetAndOrderBy() {
        Set<String> attributesToGet = new LinkedHashSet<>();
        List<OrderBy> orderBy = new ArrayList<>();
        attributesToGet.add("test1");
        attributesToGet.add("test2");
        orderBy.add(new OrderBy("test1", true));
        orderBy.add(new OrderBy("test2", false));
        FilterWhereBuilder where = new FilterWhereBuilder();
        final SQLParam param = new SQLParam(NAME, VALUE);
        where.addBind(param, OPERATOR, false);
        DatabaseQueryBuilder actual = new DatabaseQueryBuilder("table", attributesToGet);
        actual.setWhere(where);
        actual.setOrderBy(orderBy);
        assertNotNull(actual);
        assertEquals("SELECT test1 , test2 FROM table WHERE name = ? ORDER BY test1 ASC, test2 DESC", actual.getSQL());
        assertEquals(1, actual.getParams().size());
        assertEquals(param, actual.getParams().get(0));
    }
}
