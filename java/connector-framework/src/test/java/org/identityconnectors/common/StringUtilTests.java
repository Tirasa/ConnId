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
package org.identityconnectors.common;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.testng.annotations.Test;

public class StringUtilTests {
    // ========================================================================
    // JUnit Tests
    // ========================================================================
    /**
     */
    @Test
    public void testIndexOfDigit() {
        int test = 0;
        final String TEST0 = null;
        final String TEST1 = "fsadlkjffj";
        final String TEST2 = "abac2dafj";
        final String TEST3 = "fa323jf4af";

        test = StringUtil.indexOfDigit(TEST0);
        assertEquals(test, -1);
        test = StringUtil.indexOfDigit(TEST1);
        assertEquals(test, -1);
        test = StringUtil.indexOfDigit(TEST2);
        assertEquals(test, 4);
        test = StringUtil.indexOfDigit(TEST3);
        assertEquals(test, 2);
    }

    /**
     */
    @Test
    public void testIndexOfNonDigit() {
        int test = 0;
        final String TEST0 = null;
        final String TEST1 = "2131398750976";
        final String TEST2 = "21312a9320484";
        final String TEST3 = "32323aa323435";

        test = StringUtil.indexOfNonDigit(TEST0);
        assertEquals(test, -1);
        test = StringUtil.indexOfNonDigit(TEST1);
        assertEquals(test, -1);
        test = StringUtil.indexOfNonDigit(TEST2);
        assertEquals(test, 5);
        test = StringUtil.indexOfNonDigit(TEST3);
        assertEquals(test, 5);
    }

    /**
     */
    @Test
    public void testSubDigitString() {
    }

    /**
     */
    @Test
    public void testStripXmlAttribute() {
        final String DATA[][] = {
                // source, attr, result
                { null, null, null },
                { "attr='fads'", "attr", "" },
                { "at1='fasd' at1=''", "at1", "" }
        };
        String tst = null;
        for (int i = 0; i < DATA.length; i++) {
            tst = StringUtil.stripXmlAttribute(DATA[i][0], DATA[i][1]);
            assertEquals(tst, DATA[i][2]);
        }
    }

    /**
     * Make sure it removes '\n'.
     */
    @Test
    public void testStripNewlines() {
        final String[][] TESTS = new String[][] { { null, null },
                { "afdslf\n", "afdslf" }, { "afds\nfadkfj", "afdsfadkfj" },
                { "afds \nfadkfj", "afds fadkfj" },
                { "afds\n fadkfj", "afds fadkfj" } };
        String tmp;
        for (String[] data : TESTS) {
            tmp = StringUtil.stripNewlines(data[0]);
            assertEquals(tmp, data[1]);
        }
    }

    /**
     */
    @Test
    public void testStripXmlComments() {
        final String DATA[][] = {
                // test data -> result
                { null, null }, { "<!--test1-->", "" },
                { "test data", "test data" },
                { "<!--test data", "<!--test data" },
                { "test data-->", "test data-->" },
                { "test data <!-- fasdkfj -->", "test data " },
                { "<!-- fasdkfj --> test data", " test data" },
                { "<!-- fasdkfj --> test data<!-- fadsom-->", " test data" } };

        String tst = null;
        for (int i = 0; i < DATA.length; i++) {
            tst = StringUtil.stripXmlComments(DATA[i][0]);
            assertEquals(tst, DATA[i][1]);
        }
    }

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty(" "));
        assertFalse(StringUtil.isEmpty("bob"));
        assertFalse(StringUtil.isEmpty("  bob  "));
    }

    @Test
    public void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank(" "));
        assertFalse(StringUtil.isBlank("bob"));
        assertFalse(StringUtil.isBlank("  bob  "));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(StringUtil.isNotEmpty(null));
        assertFalse(StringUtil.isNotEmpty(""));
        assertTrue(StringUtil.isNotEmpty(" "));
        assertTrue(StringUtil.isNotEmpty("bob"));
        assertTrue(StringUtil.isNotEmpty("  bob  "));
    }

    @Test
    public void testIsNotBlank() {
        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank(""));
        assertFalse(StringUtil.isNotBlank(" "));
        assertTrue(StringUtil.isNotBlank("bob"));
        assertTrue(StringUtil.isNotBlank("  bob  "));
    }

    private static final String TEMPLATE = "StringUtilTests_template.js";

    private static final String PAUSE_TEXT = "pause.text";

    private static final String RESUME_TEXT = "resume.text";

    private static final String REFRESH_TIME = "refresh.time";

    private static final String GRAPH_IDS = "graphs";

    @Test
    public void testReplaceVariables() {
        // test using the following template..
        Map<String, String> vars = new HashMap<String, String>();
        vars.put(PAUSE_TEXT, "PAUSE");
        vars.put(REFRESH_TIME, "5");
        vars.put(RESUME_TEXT, "RESUME");
        vars.put(GRAPH_IDS, "1,2,4345");

        String tmpl = IOUtil.getResourceAsString(getClass(), TEMPLATE);
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            tmpl = StringUtil.replaceVariable(tmpl, key, value);
            assertTrue(tmpl.indexOf(value) != -1);
        }
    }

    /**
     * Tests the {@link StringUtil#isWhitespace(char)} method.
     */
    @Test
    public void testWhtiespace() {
        assertTrue(StringUtil.isWhitespace(' '));
        assertTrue(StringUtil.isWhitespace('\t'));
        assertFalse(StringUtil.isWhitespace('\n'));
        assertFalse(StringUtil.isWhitespace('\r'));
        for (char i = 'a'; i < 'Z'; i++) {
            assertFalse(StringUtil.isWhitespace(i));
        }
    }

    final char TEXTQ = '"';

    final char FEILDD = ',';

    /**
     * Tests the {@link StringUtil#parseLine(String, char, char)} method.
     */
    @Test
    public void testParseLine() {
        List<Object> values;
        values = CollectionUtil.<Object> newReadOnlyList("bob", "george", 4, 23, 230948);
        parseLineTest(TEXTQ, FEILDD, values);

    }

    @Test
    public void testRandomParseLine() {
        // try random stuff..
        final Random r = new Random(17);
        final char[] replace = new char[] { TEXTQ, FEILDD };
        for (int i = 0; i < 100; i++) {
            final List<Object> values = randomList(r, 10, replace, 'a');
            parseLineTest(TEXTQ, FEILDD, values);
        }
    }

    /**
     * Tests the {@link StringUtil#parseLine(String, char, char)} methods on the
     * arguments provided.
     */
    static void parseLineTest(final char textQ, final char fieldD, final List<Object> values) {
        String csv = createCSVLine(textQ, fieldD, values);
        List<String> parsedValues = StringUtil.parseLine(csv, fieldD, textQ);
        assertEquals(parsedValues, toStringList(values));
    }

    /**
     * Create a CSV line based on the values provided.
     */
    static String createCSVLine(final char textQ, final char fieldD, final List<Object> values) {
        StringBuilder bld = new StringBuilder();
        boolean first = true;
        for (Object o : values) {
            // apply field delimiter..
            if (first) {
                first = false;
            } else {
                bld.append(fieldD);
            }
            // if its a string add the text qualifiers..
            // don't bother escape text qualifiers in the string yet..
            if (o instanceof String) {
                bld.append(textQ);
            }
            bld.append(o);
            if (o instanceof String) {
                bld.append(textQ);
            }
        }
        return bld.toString();
    }

    /**
     * Converts a {@link List} of objects to a {@link List} of {@link String}s.
     */
    static List<String> toStringList(final List<Object> list) {
        List<String> ret = new ArrayList<String>();
        for (Object o : list) {
            ret.add(o.toString());
        }
        return ret;
    }

    static List<Object> randomList(final Random r, final int size, final char[] invalid,
            final char valid) {
        List<Object> ret = new ArrayList<Object>();
        for (int i = 0; i < size; i++) {
            final Object add;
            if (r.nextBoolean()) {
                add = r.nextInt();
            } else if (r.nextBoolean()) {
                add = r.nextDouble();
            } else {
                String str = StringUtil.randomString(r, r.nextInt(30));
                for (char c : invalid) {
                    // replace all w/ 'a'..
                    str = str.replace(c, valid);
                }
                add = str;
            }
            ret.add(add);
        }
        return ret;
    }

    @Test
    public void testRandomString() {
        // just execute it because it doesn't really matter..
        String s = StringUtil.randomString();
        assertTrue(s.length() < 257);
    }

    @Test
    public void testEndsWith() {
        assertTrue(StringUtil.endsWith("afdsf", 'f'));
        assertFalse(StringUtil.endsWith(null, 'f'));
        assertFalse(StringUtil.endsWith("fadsfkj", 'f'));
    }

    private static final String PROP_TEST[] = {
      "# Some comment",
      "prop1=SomeProp",
      "prop2=OtherProp"
    };

    @Test
    public void testToProperties() {
        StringPrintWriter wrt = new StringPrintWriter();
        wrt.println(PROP_TEST);
        wrt.flush();
        String inp = wrt.getString();
        Properties prop = StringUtil.toProperties(inp);
        assertEquals(prop.get("prop1"), "SomeProp");
        assertEquals(prop.get("prop2"), "OtherProp");
    }
}
