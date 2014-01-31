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
using System;
using System.Collections.Generic;
using NUnit.Framework;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
namespace FrameworkTests
{

    internal class AllFiltersTranslator : AbstractFilterTranslator<String>
    {
        protected override String CreateAndExpression(String leftExpression, String rightExpression)
        {
            return "( & " + leftExpression + " " + rightExpression + " )";
        }

        protected override String CreateOrExpression(String leftExpression, String rightExpression)
        {
            return "( | " + leftExpression + " " + rightExpression + " )";
        }

        protected override String CreateContainsExpression(ContainsFilter filter, bool not)
        {
            String rv = "( CONTAINS " + filter.GetName() + " " + filter.GetValue() + " )";
            return Not(rv, not);
        }

        protected override String CreateEndsWithExpression(EndsWithFilter filter, bool not)
        {
            String rv = "( ENDS-WITH " + filter.GetName() + " " + filter.GetValue() + " )";
            return Not(rv, not);
        }

        protected override String CreateEqualsExpression(EqualsFilter filter, bool not)
        {
            String rv = "( = " + filter.GetAttribute().Name + " [" + filter.GetAttribute().Value[0] + "] )";
            return Not(rv, not);
        }

        protected override String CreateGreaterThanExpression(GreaterThanFilter filter, bool not)
        {
            String rv = "( > " + filter.GetName() + " " + filter.GetValue() + " )";
            return Not(rv, not);
        }

        protected override String CreateGreaterThanOrEqualExpression(GreaterThanOrEqualFilter filter, bool not)
        {
            String rv = "( >= " + filter.GetName() + " " + filter.GetValue() + " )";
            return Not(rv, not);
        }

        protected override String CreateLessThanExpression(LessThanFilter filter, bool not)
        {
            String rv = "( < " + filter.GetName() + " " + filter.GetValue() + " )";
            return Not(rv, not);
        }

        protected override String CreateLessThanOrEqualExpression(LessThanOrEqualFilter filter, bool not)
        {
            String rv = "( <= " + filter.GetName() + " " + filter.GetValue() + " )";
            return Not(rv, not);
        }

        protected override String CreateStartsWithExpression(StartsWithFilter filter, bool not)
        {
            String rv = "( STARTS-WITH " + filter.GetName() + " " + filter.GetValue() + " )";
            return Not(rv, not);
        }
        protected override String CreateContainsAllValuesExpression(ContainsAllValuesFilter filter, bool not)
        {
            String rv = "( CONTAINS-ALL-VALUES " + filter.GetAttribute() + " )";
            return Not(rv, not);
        }
        private String Not(String orig, bool not)
        {
            if (not)
            {
                return "( ! " + orig + " )";
            }
            else
            {
                return orig;
            }
        }
    }

    /// <summary>
    /// Everything but Or
    /// </summary>
    internal class NoOrTranslator : AllFiltersTranslator
    {
        protected override String CreateOrExpression(String leftExpression, String rightExpression)
        {
            return null;
        }
    }
    /// <summary>
    /// Everything but EndsWith
    /// </summary>
    internal class NoEndsWithTranslator : AllFiltersTranslator
    {
        protected override String CreateEndsWithExpression(EndsWithFilter filter, bool not)
        {
            return null;
        }
    }
    /// <summary>
    /// Everything but EndsWith,Or
    /// </summary>
    internal class NoEndsWithNoOrTranslator : AllFiltersTranslator
    {
        protected override String CreateOrExpression(String leftExpression, String rightExpression)
        {
            return null;
        }
        protected override String CreateEndsWithExpression(EndsWithFilter filter, bool not)
        {
            return null;
        }
    }

    /// <summary>
    /// Everything but And
    /// </summary>
    internal class NoAndTranslator : AllFiltersTranslator
    {
        protected override String CreateAndExpression(String leftExpression, String rightExpression)
        {
            return null;
        }
    }
    /// <summary>
    /// Everything but And
    /// </summary>
    internal class NoAndNoEndsWithTranslator : AllFiltersTranslator
    {
        protected override String CreateAndExpression(String leftExpression, String rightExpression)
        {
            return null;
        }
        protected override String CreateEndsWithExpression(EndsWithFilter filter, bool not)
        {
            return null;
        }

    }
    /// <summary>
    /// Everything but And
    /// </summary>
    internal class NoAndNoOrNoEndsWithTranslator : AllFiltersTranslator
    {
        protected override String CreateAndExpression(String leftExpression, String rightExpression)
        {
            return null;
        }
        protected override String CreateEndsWithExpression(EndsWithFilter filter, bool not)
        {
            return null;
        }
        protected override String CreateOrExpression(String leftExpression, String rightExpression)
        {
            return null;
        }

    }
    [TestFixture]
    public class FilterTranslatorTests
    {

        /// <summary>
        /// Test all operations when everything is fully implemented.
        /// </summary>
        /// <remarks>
        /// Test not normalization as well.
        /// </remarks>
        [Test]
        public void TestBasics()
        {
            ConnectorAttribute attribute =
                ConnectorAttributeBuilder.Build("att-name", "att-value");
            ConnectorAttribute attribute2 =
                ConnectorAttributeBuilder.Build("att-name2", "att-value2");
            AllFiltersTranslator translator = new
            AllFiltersTranslator();

            {
                Filter filter =
                    FilterBuilder.Contains(attribute);
                String expected = "( CONTAINS att-name att-value )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            {
                Filter filter =
                    FilterBuilder.EndsWith(attribute);
                String expected = "( ENDS-WITH att-name att-value )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            {
                Filter filter =
                    FilterBuilder.EqualTo(attribute);
                String expected = "( = att-name [att-value] )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            {
                Filter filter =
                    FilterBuilder.GreaterThan(attribute);
                String expected = "( > att-name att-value )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            {
                Filter filter =
                    FilterBuilder.GreaterThanOrEqualTo(attribute);
                String expected = "( >= att-name att-value )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            {
                Filter filter =
                    FilterBuilder.LessThan(attribute);
                String expected = "( < att-name att-value )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            {
                Filter filter =
                    FilterBuilder.LessThanOrEqualTo(attribute);
                String expected = "( <= att-name att-value )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            {
                Filter filter =
                    FilterBuilder.StartsWith(attribute);
                String expected = "( STARTS-WITH att-name att-value )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            {
                Filter filter =
                    FilterBuilder.ContainsAllValues(attribute);
                String expected = "( CONTAINS-ALL-VALUES " + attribute + " )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expected = "( ! " + expected + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            //and
            {
                Filter left =
                    FilterBuilder.Contains(attribute);
                Filter right =
                    FilterBuilder.Contains(attribute2);
                String expectedLeft = "( CONTAINS att-name att-value )";
                String expectedRight = "( CONTAINS att-name2 att-value2 )";
                Filter filter =
                    FilterBuilder.And(left, right);
                String expected =
                    "( & " + expectedLeft + " " + expectedRight + " )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expectedLeft = "( ! " + expectedLeft + " )";
                expectedRight = "( ! " + expectedRight + " )";
                expected =
                    "( | " + expectedLeft + " " + expectedRight + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

            }

            //or
            {
                Filter left =
                    FilterBuilder.Contains(attribute);
                Filter right =
                    FilterBuilder.Contains(attribute2);
                String expectedLeft = "( CONTAINS att-name att-value )";
                String expectedRight = "( CONTAINS att-name2 att-value2 )";
                Filter filter =
                    FilterBuilder.Or(left, right);
                String expected =
                    "( | " + expectedLeft + " " + expectedRight + " )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);

                filter = FilterBuilder.Not(filter);
                expectedLeft = "( ! " + expectedLeft + " )";
                expectedRight = "( ! " + expectedRight + " )";
                expected =
                    "( & " + expectedLeft + " " + expectedRight + " )";
                actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

            //double-negative
            {
                Filter filter =
                    FilterBuilder.Contains(attribute);
                filter = FilterBuilder.Not(filter);
                filter = FilterBuilder.Not(filter);
                String expected = "( CONTAINS att-name att-value )";
                String actual =
                    TranslateSingle(translator, filter);
                Assert.AreEqual(expected, actual);
            }

        }

        /// <summary>
        /// (a OR b) AND ( c OR d) needs to become
        /// (a AND c) OR ( a AND d) OR (b AND c) OR (b AND d) is
        /// OR is not implemented.
        /// </summary>
        /// <remarks>
        /// Otherwise it should stay
        /// as-is.
        /// </remarks>
        [Test]
        public void TestDistribution()
        {
            Filter a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            Filter b =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("b", "b"));
            Filter c =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("c", "c"));
            Filter d =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("d", "d"));

            Filter filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            String expected = "( & ( | ( CONTAINS a a ) ( CONTAINS b b ) ) ( | ( CONTAINS c c ) ( CONTAINS d d ) ) )";
            String actual =
                TranslateSingle(new AllFiltersTranslator(), filter);

            Assert.AreEqual(expected, actual);

            IList<String> results =
                new NoOrTranslator().Translate(filter);
            Assert.AreEqual(4, results.Count);

            Assert.AreEqual("( & ( CONTAINS a a ) ( CONTAINS c c ) )",
                            results[0]);
            Assert.AreEqual("( & ( CONTAINS a a ) ( CONTAINS d d ) )",
                            results[1]);
            Assert.AreEqual("( & ( CONTAINS b b ) ( CONTAINS c c ) )",
                            results[2]);
            Assert.AreEqual("( & ( CONTAINS b b ) ( CONTAINS d d ) )",
                            results[3]);
        }

        //test simplification
        //-no leaf
        [Test]
        public void TestSimplifyNoLeaf()
        {
            Filter a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            Filter b =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("b", "b"));
            Filter c =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("c", "c"));
            Filter d =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("d", "d"));

            Filter filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            String expected = "( | ( CONTAINS a a ) ( CONTAINS b b ) )";
            String actual =
                TranslateSingle(new NoEndsWithTranslator(), filter);
            Assert.AreEqual(expected, actual);

        }
        //-no leaf + no or
        [Test]
        public void TestSimplifyNoLeafNoOr()
        {
            Filter a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            Filter b =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("b", "b"));
            Filter c =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("c", "c"));
            Filter d =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("d", "d"));

            Filter filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            IList<String> results =
                new NoEndsWithNoOrTranslator().Translate(filter);
            Assert.AreEqual(2, results.Count);
            Assert.AreEqual("( CONTAINS a a )",
                            results[0]);
            Assert.AreEqual("( CONTAINS b b )",
                            results[1]);

        }

        //-no and
        [Test]
        public void TestSimplifyNoAnd()
        {
            Filter a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            Filter b =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("b", "b"));
            Filter c =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("c", "c"));
            Filter d =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("d", "d"));

            Filter filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            String expected = "( | ( CONTAINS a a ) ( CONTAINS b b ) )";
            String actual =
                TranslateSingle(new NoAndTranslator(), filter);
            Assert.AreEqual(expected, actual);
        }

        //-no and+no leaf
        [Test]
        public void TestSimplifyNoAndNoLeaf()
        {
            Filter a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            Filter b =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("b", "b"));
            Filter c =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("c", "c"));
            Filter d =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("d", "d"));

            Filter filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            String expected = "( | ( CONTAINS a a ) ( CONTAINS b b ) )";
            String actual =
                TranslateSingle(new NoAndNoEndsWithTranslator(), filter);
            Assert.AreEqual(expected, actual);

            a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            b =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("b", "b"));
            c =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("c", "c"));
            d =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("d", "d"));

            filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            expected = "( | ( CONTAINS c c ) ( CONTAINS d d ) )";
            actual =
                TranslateSingle(new NoAndNoEndsWithTranslator(), filter);
            Assert.AreEqual(expected, actual);

            a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            b =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("b", "b"));
            c =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("c", "c"));
            d =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("d", "d"));

            filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            IList<String> results =
                new NoAndNoEndsWithTranslator().Translate(filter);
            Assert.AreEqual(0, results.Count);
        }

        //-no and, no or, no leaf
        [Test]
        public void TestSimplifyNoAndNoOrNoLeaf()
        {
            Filter a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            Filter b =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("b", "b"));
            Filter c =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("c", "c"));
            Filter d =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("d", "d"));

            Filter filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            IList<String> results =
                new NoAndNoOrNoEndsWithTranslator().Translate(filter);
            Assert.AreEqual(2, results.Count);
            Assert.AreEqual("( CONTAINS a a )",
                            results[0]);
            Assert.AreEqual("( CONTAINS b b )",
                            results[1]);

            a =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("a", "a"));
            b =
                FilterBuilder.EndsWith(ConnectorAttributeBuilder.Build("b", "b"));
            c =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("c", "c"));
            d =
                FilterBuilder.Contains(ConnectorAttributeBuilder.Build("d", "d"));
            filter =
                FilterBuilder.And(
                        FilterBuilder.Or(a, b),
                        FilterBuilder.Or(c, d));
            results =
                new NoAndNoOrNoEndsWithTranslator().Translate(filter);
            Assert.AreEqual(2, results.Count);
            Assert.AreEqual("( CONTAINS c c )",
                            results[0]);
            Assert.AreEqual("( CONTAINS d d )",
                            results[1]);
        }

        private static String TranslateSingle(AbstractFilterTranslator<String> translator,
                Filter filter)
        {
            IList<String> translated =
                translator.Translate(filter);
            Assert.AreEqual(1, translated.Count);
            return translated[0];
        }
    }
}