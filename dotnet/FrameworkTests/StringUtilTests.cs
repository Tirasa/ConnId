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
 * Portions Copyrighted 2014 ForgeRock AS.
 */

using System;
using System.Collections.Generic;
using System.Reflection;
using System.Text;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Script;
using Org.IdentityConnectors.Framework.Common.Objects;
using NUnit.Framework;

namespace FrameworkTests
{
    /// <summary>
    /// Description of ScriptTests.
    /// </summary>
    [TestFixture]
    public class StringUtilTests
    {
        [Test]
        public virtual void testIndexOfDigit()
        {
            int test = 0;
            const string TEST0 = null;
            const string TEST1 = "fsadlkjffj";
            const string TEST2 = "abac2dafj";
            const string TEST3 = "fa323jf4af";

            test = StringUtil.indexOfDigit(TEST0);
            assertEquals(test, -1);
            test = StringUtil.indexOfDigit(TEST1);
            assertEquals(test, -1);
            test = StringUtil.indexOfDigit(TEST2);
            assertEquals(test, 4);
            test = StringUtil.indexOfDigit(TEST3);
            assertEquals(test, 2);
        }

        [Test]
        public virtual void testIndexOfNonDigit()
        {
            int test = 0;
            const string TEST0 = null;
            const string TEST1 = "2131398750976";
            const string TEST2 = "21312a9320484";
            const string TEST3 = "32323aa323435";

            test = StringUtil.indexOfNonDigit(TEST0);
            assertEquals(test, -1);
            test = StringUtil.indexOfNonDigit(TEST1);
            assertEquals(test, -1);
            test = StringUtil.indexOfNonDigit(TEST2);
            assertEquals(test, 5);
            test = StringUtil.indexOfNonDigit(TEST3);
            assertEquals(test, 5);
        }

        [Test]
        public virtual void testSubDigitString()
        {
        }

        [Test]
        public virtual void testStripXmlAttribute()
        {
            string[][] DATA = new string[][] { new string[] { null, null, null }, new string[] { "attr='fads'", "attr", "" }, new string[] { "at1='fasd' at1=''", "at1", "" } };
            string tst = null;
            for (int i = 0; i < DATA.Length; i++)
            {
                tst = StringUtil.stripXmlAttribute(DATA[i][0], DATA[i][1]);
                assertEquals(tst, DATA[i][2]);
            }
        }

        /// <summary>
        /// Make sure it removes '\n'.
        /// </summary>
        [Test]
        public virtual void testStripNewlines()
        {
            string[][] TESTS = new string[][] { new string[] { null, null }, new string[] { "afdslf\n", "afdslf" }, new string[] { "afds\nfadkfj", "afdsfadkfj" }, new string[] { "afds \nfadkfj", "afds fadkfj" }, new string[] { "afds\n fadkfj", "afds fadkfj" } };
            string tmp;
            foreach (string[] data in TESTS)
            {
                tmp = StringUtil.stripNewlines(data[0]);
                assertEquals(tmp, data[1]);
            }
        }

        [Test]
        public virtual void testStripXmlComments()
        {
            string[][] DATA = new string[][] { new string[] { null, null }, new string[] { "<!--test1-->", "" }, new string[] { "test data", "test data" }, new string[] { "<!--test data", "<!--test data" }, new string[] { "test data-->", "test data-->" }, new string[] { "test data <!-- fasdkfj -->", "test data " }, new string[] { "<!-- fasdkfj --> test data", " test data" }, new string[] { "<!-- fasdkfj --> test data<!-- fadsom-->", " test data" } };

            string tst = null;
            for (int i = 0; i < DATA.Length; i++)
            {
                tst = StringUtil.stripXmlComments(DATA[i][0]);
                assertEquals(tst, DATA[i][1]);
            }
        }

        /// <summary>
        /// Tests the <seealso cref="StringUtil#parseLine(String, char, char)"/> methods on the
        /// arguments provided.
        /// </summary>
        internal static void parseLineTest(char textQ, char fieldD, IList<object> values)
        {
            string csv = createCSVLine(textQ, fieldD, values);
            IList<string> parsedValues = StringUtil.parseLine(csv, fieldD, textQ);
            assertEquals(parsedValues, toStringList(values));
        }

        /// <summary>
        /// Create a CSV line based on the values provided.
        /// </summary>
        internal static string createCSVLine(char textQ, char fieldD, IList<object> values)
        {
            StringBuilder bld = new StringBuilder();
            bool first = true;
            foreach (object o in values)
            {
                // apply field delimiter..
                if (first)
                {
                    first = false;
                }
                else
                {
                    bld.Append(fieldD);
                }
                // if its a string add the text qualifiers..
                // don't bother escape text qualifiers in the string yet..
                if (o is string)
                {
                    bld.Append(textQ);
                }
                bld.Append(o);
                if (o is string)
                {
                    bld.Append(textQ);
                }
            }
            return bld.ToString();
        }

        /// <summary>
        /// Converts a <seealso cref="List"/> of objects to a <seealso cref="List"/> of <seealso cref="String"/>s.
        /// </summary>
        internal static IList<string> toStringList(IList<object> list)
        {
            IList<string> ret = new List<string>();
            foreach (object o in list)
            {
                ret.Add(o.ToString());
            }
            return ret;
        }

        internal static IList<object> randomList(Random r, int size, char[] invalid, char valid)
        {
            IList<object> ret = new List<object>();
            for (int i = 0; i < size; i++)
            {
                object add;
                if (r.nextBoolean())
                {
                    add = r.Next();
                }
                else if (r.nextBoolean())
                {
                    add = r.NextDouble();
                }
                else
                {
                    string str = StringUtil.randomString(r, r.Next(30));
                    foreach (char c in invalid)
                    {
                        // replace all w/ 'a'..
                        str = str.Replace(c, valid);
                    }
                    add = str;
                }
                ret.Add(add);
            }
            return ret;
        }

        [Test]
        public virtual void testRandomString()
        {
            // just execute it because it doesn't really matter..
            string s = StringUtil.randomString();
            assertTrue(s.Length < 257);
        }

        [Test]
        public virtual void testEndsWith()
        {
            assertTrue(StringUtil.EndsWith("afdsf", 'f'));
            assertFalse(StringUtil.EndsWith(null, 'f'));
            assertFalse(StringUtil.EndsWith("fadsfkj", 'f'));
        }

        private static readonly string[] PROP_TEST = new string[] { "# Some comment", "prop1=SomeProp", "prop2=OtherProp" };

        [Test]
        public virtual void testToProperties()
        {
            StringPrintWriter wrt = new StringPrintWriter();
            wrt.println(PROP_TEST);
            wrt.flush();
            string inp = wrt.String;
            Properties prop = StringUtil.toProperties(inp);
            assertEquals(prop.get("prop1"), "SomeProp");
            assertEquals(prop.get("prop2"), "OtherProp");
        }
    }
}