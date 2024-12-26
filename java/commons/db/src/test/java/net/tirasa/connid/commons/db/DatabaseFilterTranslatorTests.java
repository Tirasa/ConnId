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

import static org.identityconnectors.framework.common.objects.AttributeBuilder.build;
import static org.identityconnectors.framework.common.objects.filter.FilterBuilder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.junit.jupiter.api.Test;

class DatabaseFilterTranslatorTests {

    private static DatabaseFilterTranslator getDatabaseFilterTranslator() {
        return new DatabaseFilterTranslator(ObjectClass.ACCOUNT, null) {

            @Override
            protected SQLParam getSQLParam(
                    final Attribute attribute,
                    final ObjectClass oclass,
                    final OperationOptions options) {

                return new SQLParam(attribute.getName(), AttributeUtil.getSingleValue(attribute), Types.NULL);
            }
        };
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     *
     * @throws Exception
     */
    @Test
    void unaryFilters() throws Exception {
        Attribute attr = build("count", 2);
        Filter filters[] = new Filter[] {
            equalTo(attr), greaterThan(attr), greaterThanOrEqualTo(attr), lessThan(attr), lessThanOrEqualTo(attr) };
        String ops[] = new String[] { "=", ">", ">=", "<", "<=" };
        List<SQLParam> expected = new ArrayList<>();
        expected.add(new SQLParam("count", 2, Types.INTEGER));
        for (int i = 0; i < filters.length; i++) {
            DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
            List<FilterWhereBuilder> blist = tr.translate(filters[i]);
            assertEquals(1, blist.size());
            final FilterWhereBuilder b = blist.get(0);
            assertEquals("count " + ops[i] + " ?", b.getWhereClause());
            assertEquals(expected.size(), b.getParams().size());
        }
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     *
     * @throws Exception
     */
    @Test
    void compositeFilters() throws Exception {
        Filter lf = greaterThan(build("count", 4));
        Filter rf = lessThan(build("count", 20));
        List<SQLParam> expected = new ArrayList<>();
        expected.add(new SQLParam("count", 4, Types.INTEGER));
        expected.add(new SQLParam("count", 20, Types.INTEGER));
        // test and
        Filter f = FilterBuilder.and(lf, rf);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        FilterWhereBuilder b = blist.get(0);
        assertEquals("count > ? AND count < ?", b.getWhereClause());
        // test or
        assertEquals(expected.size(), b.getParams().size());
        f = FilterBuilder.or(lf, rf);
        DatabaseFilterTranslator tr2 = getDatabaseFilterTranslator();
        blist = tr2.translate(f);
        assertEquals(1, blist.size());
        b = blist.get(0);
        assertEquals("count > ? OR count < ?", b.getWhereClause());
        assertEquals(expected.size(), b.getParams().size());
        // test xor
        // assertEquals(expected, actual);
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     *
     * @throws Exception
     */
    @Test
    void compositeFilterChainNotOr() throws Exception {
        Filter lf = greaterThan(build("count", 4));
        Filter rf = lessThan(build("count", 20));
        List<SQLParam> expected = new ArrayList<>();
        expected.add(new SQLParam("count", 4, Types.INTEGER));
        expected.add(new SQLParam("count", 20, Types.INTEGER));
        // test and
        Filter f = FilterBuilder.or(lf, rf);
        Filter not = FilterBuilder.not(f);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
        List<FilterWhereBuilder> blist = tr.translate(not);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("count <= ? AND count >= ?", b.getWhereClause());
        assertEquals(expected.size(), b.getParams().size());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     *
     * @throws Exception
     */
    @Test
    void compositeFilterChainOrAnd() throws Exception {
        Filter f1 = greaterThan(build("count", 4));
        Filter f2 = lessThan(build("count", 20));
        Filter f3 = equalTo(build("count", 10));
        List<SQLParam> expected = new ArrayList<>();
        expected.add(new SQLParam("count", 4, Types.INTEGER));
        expected.add(new SQLParam("count", 20, Types.INTEGER));
        expected.add(new SQLParam("count", 10, Types.INTEGER));
        // test and
        Filter f12 = FilterBuilder.or(f1, f2);
        Filter f = FilterBuilder.and(f12, f3);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("( count > ? OR count < ? ) AND count = ?", b.getWhereClause());
        assertEquals(expected.size(), b.getParams().size());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     *
     * @throws Exception
     */
    @Test
    void compositeFilterChainAndOrAndOrAnd() throws Exception {
        Filter f1 = equalTo(build("a", 1));
        Filter f2 = equalTo(build("b", 1));
        Filter f3 = equalTo(build("c", 1));
        Filter f4 = equalTo(build("d", 1));
        Filter f5 = equalTo(build("e", 1));
        Filter f6 = equalTo(build("f", 1));
        List<SQLParam> expected = new ArrayList<>();
        expected.add(new SQLParam("a", 1));
        expected.add(new SQLParam("b", 1));
        expected.add(new SQLParam("c", 1));
        expected.add(new SQLParam("d", 1));
        expected.add(new SQLParam("e", 1));
        expected.add(new SQLParam("f", 1));
        // test and
        Filter f12 = FilterBuilder.or(f1, f2);
        Filter f34 = FilterBuilder.and(f3, f4);
        Filter f56 = FilterBuilder.or(f5, f6);
        Filter f1234 = FilterBuilder.and(f12, f34);
        Filter f = FilterBuilder.or(f1234, f56);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals(
                "( ( a = ? OR b = ? ) AND ( c = ? AND d = ? ) ) OR ( e = ? OR f = ? )", b
                        .getWhereClause());
        assertEquals(expected, b.getParams());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     *
     * @throws Exception
     */
    @Test
    void compositeFilterChainOrAndNot() throws Exception {
        Filter f1 = greaterThan(build("count", 4));
        Filter f2 = lessThan(build("count", 20));
        Filter f3 = equalTo(build("count", 10));
        List<SQLParam> expected = new ArrayList<>();
        expected.add(new SQLParam("count", 4, Types.INTEGER));
        expected.add(new SQLParam("count", 20, Types.INTEGER));
        expected.add(new SQLParam("count", 10, Types.INTEGER));
        // test and
        Filter f1o2 = FilterBuilder.or(f1, f2);
        Filter fn3 = FilterBuilder.not(f3);
        Filter f = FilterBuilder.and(f1o2, fn3);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("( count > ? OR count < ? ) AND NOT count = ?", b.getWhereClause());
        assertEquals(expected.size(), b.getParams().size());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.DatabaseFilterTranslator}.
     *
     * @throws Exception
     */
    @Test
    void notfilter() throws Exception {
        Filter gt = greaterThan(build("count", 4));
        Filter f = FilterBuilder.not(gt);
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("count <= ?", b.getWhereClause());
        List<SQLParam> expected = new ArrayList<>();
        expected.add(new SQLParam("count", 4, Types.INTEGER));
        assertEquals(expected.size(), b.getParams().size());
    }

    @Test
    void equalsIgnoreCase() {
        Filter f = FilterBuilder.equalsIgnoreCase(AttributeBuilder.build("name", "John"));
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("LOWER(name) = LOWER( ? )", b.getWhereClause());
    }

    @Test
    void issueCOMMONS13() {
        Filter f = FilterBuilder.equalTo(AttributeBuilder.build("last_name"));
        DatabaseFilterTranslator tr = getDatabaseFilterTranslator();
        List<FilterWhereBuilder> blist = tr.translate(f);
        assertEquals(1, blist.size());
        final FilterWhereBuilder b = blist.get(0);
        assertEquals("last_name IS NULL", b.getWhereClause());
    }
}
