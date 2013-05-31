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
package org.identityconnectors.framework.common.objects.filter;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.atIndex;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.testng.annotations.Test;

public class FilterTranslatorTests {

    private static class AllFiltersTranslator extends AbstractFilterTranslator<String> {

        @Override
        protected String createAndExpression(String leftExpression, String rightExpression) {
            return "( & " + leftExpression + " " + rightExpression + " )";
        }

        @Override
        protected String createOrExpression(String leftExpression, String rightExpression) {
            return "( | " + leftExpression + " " + rightExpression + " )";
        }

        @Override
        protected String createContainsExpression(ContainsFilter filter, boolean not) {
            String rv = "( CONTAINS " + filter.getName() + " " + filter.getValue() + " )";
            return not(rv, not);
        }

        @Override
        protected String createEndsWithExpression(EndsWithFilter filter, boolean not) {
            String rv = "( ENDS-WITH " + filter.getName() + " " + filter.getValue() + " )";
            return not(rv, not);
        }

        @Override
        protected String createEqualsExpression(EqualsFilter filter, boolean not) {
            String rv =
                    "( = " + filter.getAttribute().getName() + " "
                            + filter.getAttribute().getValue() + " )";
            return not(rv, not);
        }

        @Override
        protected String createGreaterThanExpression(GreaterThanFilter filter, boolean not) {
            String rv = "( > " + filter.getName() + " " + filter.getValue() + " )";
            return not(rv, not);
        }

        @Override
        protected String createGreaterThanOrEqualExpression(GreaterThanOrEqualFilter filter,
                boolean not) {
            String rv = "( >= " + filter.getName() + " " + filter.getValue() + " )";
            return not(rv, not);
        }

        @Override
        protected String createLessThanExpression(LessThanFilter filter, boolean not) {
            String rv = "( < " + filter.getName() + " " + filter.getValue() + " )";
            return not(rv, not);
        }

        @Override
        protected String createLessThanOrEqualExpression(LessThanOrEqualFilter filter, boolean not) {
            String rv = "( <= " + filter.getName() + " " + filter.getValue() + " )";
            return not(rv, not);
        }

        @Override
        protected String createStartsWithExpression(StartsWithFilter filter, boolean not) {
            String rv = "( STARTS-WITH " + filter.getName() + " " + filter.getValue() + " )";
            return not(rv, not);
        }

        @Override
        protected String createContainsAllValuesExpression(ContainsAllValuesFilter filter,
                boolean not) {
            String rv = "( CONTAINS-ALL-VALUES " + filter.getAttribute() + " )";
            return not(rv, not);
        }

        private String not(String orig, boolean not) {
            if (not) {
                return "( ! " + orig + " )";
            } else {
                return orig;
            }
        }
    }

    /**
     * Everything but Or
     */
    private static class NoOrTranslator extends AllFiltersTranslator {

        @Override
        protected String createOrExpression(String leftExpression, String rightExpression) {
            return null;
        }
    }

    /**
     * Everything but EndsWith
     */
    private static class NoEndsWithTranslator extends AllFiltersTranslator {

        @Override
        protected String createEndsWithExpression(EndsWithFilter filter, boolean not) {
            return null;
        }
    }

    /**
     * Everything but EndsWith,Or
     */
    private static class NoEndsWithNoOrTranslator extends AllFiltersTranslator {

        @Override
        protected String createOrExpression(String leftExpression, String rightExpression) {
            return null;
        }

        @Override
        protected String createEndsWithExpression(EndsWithFilter filter, boolean not) {
            return null;
        }
    }

    /**
     * Everything but And
     */
    private static class NoAndTranslator extends AllFiltersTranslator {

        @Override
        protected String createAndExpression(String leftExpression, String rightExpression) {
            return null;
        }
    }

    /**
     * Everything but And
     */
    private static class NoAndNoEndsWithTranslator extends AllFiltersTranslator {

        @Override
        protected String createAndExpression(String leftExpression, String rightExpression) {
            return null;
        }

        @Override
        protected String createEndsWithExpression(EndsWithFilter filter, boolean not) {
            return null;
        }
    }

    /**
     * Everything but And
     */
    private static class NoAndNoOrNoEndsWithTranslator extends AllFiltersTranslator {

        @Override
        protected String createAndExpression(String leftExpression, String rightExpression) {
            return null;
        }

        @Override
        protected String createEndsWithExpression(EndsWithFilter filter, boolean not) {
            return null;
        }

        @Override
        protected String createOrExpression(String leftExpression, String rightExpression) {
            return null;
        }
    }

    /**
     * Test all operations when everything is fully implemented. Test not
     * normalization as well.
     */
    @Test
    public void testBasics() {
        Attribute attribute = AttributeBuilder.build("att-name", "att-value");
        Attribute attribute2 = AttributeBuilder.build("att-name2", "att-value2");
        AllFiltersTranslator translator = new AllFiltersTranslator();

        {
            Filter filter = FilterBuilder.contains(attribute);
            String expected = "( CONTAINS att-name att-value )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        {
            Filter filter = FilterBuilder.endsWith(attribute);
            String expected = "( ENDS-WITH att-name att-value )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        {
            Filter filter = FilterBuilder.equalTo(attribute);
            String expected = "( = att-name [att-value] )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        {
            Filter filter = FilterBuilder.greaterThan(attribute);
            String expected = "( > att-name att-value )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        {
            Filter filter = FilterBuilder.greaterThanOrEqualTo(attribute);
            String expected = "( >= att-name att-value )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        {
            Filter filter = FilterBuilder.lessThan(attribute);
            String expected = "( < att-name att-value )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        {
            Filter filter = FilterBuilder.lessThanOrEqualTo(attribute);
            String expected = "( <= att-name att-value )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        {
            Filter filter = FilterBuilder.startsWith(attribute);
            String expected = "( STARTS-WITH att-name att-value )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        {
            Filter filter = FilterBuilder.containsAllValues(attribute);
            String expected = "( CONTAINS-ALL-VALUES " + attribute + " )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expected = "( ! " + expected + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }
        // and
        {
            Filter left = FilterBuilder.contains(attribute);
            Filter right = FilterBuilder.contains(attribute2);
            String expectedLeft = "( CONTAINS att-name att-value )";
            String expectedRight = "( CONTAINS att-name2 att-value2 )";
            Filter filter = FilterBuilder.and(left, right);
            String expected = "( & " + expectedLeft + " " + expectedRight + " )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expectedLeft = "( ! " + expectedLeft + " )";
            expectedRight = "( ! " + expectedRight + " )";
            expected = "( | " + expectedLeft + " " + expectedRight + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

        }

        // or
        {
            Filter left = FilterBuilder.contains(attribute);
            Filter right = FilterBuilder.contains(attribute2);
            String expectedLeft = "( CONTAINS att-name att-value )";
            String expectedRight = "( CONTAINS att-name2 att-value2 )";
            Filter filter = FilterBuilder.or(left, right);
            String expected = "( | " + expectedLeft + " " + expectedRight + " )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);

            filter = FilterBuilder.not(filter);
            expectedLeft = "( ! " + expectedLeft + " )";
            expectedRight = "( ! " + expectedRight + " )";
            expected = "( & " + expectedLeft + " " + expectedRight + " )";
            actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

        // double-negative
        {
            Filter filter = FilterBuilder.contains(attribute);
            filter = FilterBuilder.not(filter);
            filter = FilterBuilder.not(filter);
            String expected = "( CONTAINS att-name att-value )";
            String actual = translateSingle(translator, filter);
            assertEquals(actual, expected);
        }

    }

    /**
     * (a OR b) AND ( c OR d) needs to become (a AND c) OR ( a AND d) OR (b AND
     * c) OR (b AND d) is OR is not implemented. Otherwise it should stay as-is.
     */
    @Test
    public void testDistribution() {
        Filter a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        Filter b = FilterBuilder.contains(AttributeBuilder.build("b", "b"));
        Filter c = FilterBuilder.contains(AttributeBuilder.build("c", "c"));
        Filter d = FilterBuilder.contains(AttributeBuilder.build("d", "d"));

        Filter filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        String expected =
                "( & ( | ( CONTAINS a a ) ( CONTAINS b b ) ) ( | ( CONTAINS c c ) ( CONTAINS d d ) ) )";
        String actual = translateSingle(new AllFiltersTranslator(), filter);

        assertEquals(actual, expected);

        List<String> results = new NoOrTranslator().translate(filter);
        assertThat(results).hasSize(4).contains("( & ( CONTAINS a a ) ( CONTAINS c c ) )",
                atIndex(0)).contains("( & ( CONTAINS a a ) ( CONTAINS d d ) )", atIndex(1))
                .contains("( & ( CONTAINS b b ) ( CONTAINS c c ) )", atIndex(2)).contains(
                        "( & ( CONTAINS b b ) ( CONTAINS d d ) )", atIndex(3));
    }

    // test simplification
    // -no leaf
    @Test
    public void testSimplifyNoLeaf() {
        Filter a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        Filter b = FilterBuilder.contains(AttributeBuilder.build("b", "b"));
        Filter c = FilterBuilder.endsWith(AttributeBuilder.build("c", "c"));
        Filter d = FilterBuilder.contains(AttributeBuilder.build("d", "d"));

        Filter filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        String expected = "( | ( CONTAINS a a ) ( CONTAINS b b ) )";
        String actual = translateSingle(new NoEndsWithTranslator(), filter);
        assertEquals(actual, expected);

    }

    // -no leaf + no or
    @Test
    public void testSimplifyNoLeafNoOr() {
        Filter a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        Filter b = FilterBuilder.contains(AttributeBuilder.build("b", "b"));
        Filter c = FilterBuilder.endsWith(AttributeBuilder.build("c", "c"));
        Filter d = FilterBuilder.contains(AttributeBuilder.build("d", "d"));

        Filter filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        List<String> results = new NoEndsWithNoOrTranslator().translate(filter);
        assertThat(results).hasSize(2).contains("( CONTAINS a a )", atIndex(0)).contains(
                "( CONTAINS b b )", atIndex(1));
    }

    // -no and
    @Test
    public void testSimplifyNoAnd() {
        Filter a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        Filter b = FilterBuilder.contains(AttributeBuilder.build("b", "b"));
        Filter c = FilterBuilder.endsWith(AttributeBuilder.build("c", "c"));
        Filter d = FilterBuilder.contains(AttributeBuilder.build("d", "d"));

        Filter filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        String expected = "( | ( CONTAINS a a ) ( CONTAINS b b ) )";
        String actual = translateSingle(new NoAndTranslator(), filter);
        assertEquals(actual, expected);
    }

    // -no and+no leaf
    @Test
    public void testSimplifyNoAndNoLeaf() {
        Filter a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        Filter b = FilterBuilder.contains(AttributeBuilder.build("b", "b"));
        Filter c = FilterBuilder.endsWith(AttributeBuilder.build("c", "c"));
        Filter d = FilterBuilder.contains(AttributeBuilder.build("d", "d"));

        Filter filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        String expected = "( | ( CONTAINS a a ) ( CONTAINS b b ) )";
        String actual = translateSingle(new NoAndNoEndsWithTranslator(), filter);
        assertEquals(actual, expected);

        a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        b = FilterBuilder.endsWith(AttributeBuilder.build("b", "b"));
        c = FilterBuilder.contains(AttributeBuilder.build("c", "c"));
        d = FilterBuilder.contains(AttributeBuilder.build("d", "d"));

        filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        expected = "( | ( CONTAINS c c ) ( CONTAINS d d ) )";
        actual = translateSingle(new NoAndNoEndsWithTranslator(), filter);
        assertEquals(actual, expected);

        a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        b = FilterBuilder.endsWith(AttributeBuilder.build("b", "b"));
        c = FilterBuilder.contains(AttributeBuilder.build("c", "c"));
        d = FilterBuilder.endsWith(AttributeBuilder.build("d", "d"));

        filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        List<String> results = new NoAndNoEndsWithTranslator().translate(filter);
        assertThat(results).hasSize(0);
    }

    // -no and, no or, no leaf
    @Test
    public void testSimplifyNoAndNoOrNoLeaf() {
        Filter a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        Filter b = FilterBuilder.contains(AttributeBuilder.build("b", "b"));
        Filter c = FilterBuilder.endsWith(AttributeBuilder.build("c", "c"));
        Filter d = FilterBuilder.contains(AttributeBuilder.build("d", "d"));

        Filter filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        List<String> results = new NoAndNoOrNoEndsWithTranslator().translate(filter);
        assertThat(results).hasSize(2).contains("( CONTAINS a a )", atIndex(0)).contains(
                "( CONTAINS b b )", atIndex(1));

        a = FilterBuilder.contains(AttributeBuilder.build("a", "a"));
        b = FilterBuilder.endsWith(AttributeBuilder.build("b", "b"));
        c = FilterBuilder.contains(AttributeBuilder.build("c", "c"));
        d = FilterBuilder.contains(AttributeBuilder.build("d", "d"));
        filter = FilterBuilder.and(FilterBuilder.or(a, b), FilterBuilder.or(c, d));
        results = new NoAndNoOrNoEndsWithTranslator().translate(filter);
        assertThat(results).hasSize(2).contains("( CONTAINS c c )", atIndex(0)).contains(
                "( CONTAINS d d )", atIndex(1));
    }

    private static String translateSingle(AbstractFilterTranslator<String> translator, Filter filter) {
        List<String> translated = translator.translate(filter);
        assertEquals(translated.size(), 1);
        return translated.get(0);
    }
}
