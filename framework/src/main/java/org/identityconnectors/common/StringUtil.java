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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.common;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;

public class StringUtil {

    /**
     * Never allow this to be instantiated.
     */
    private StringUtil() {
        throw new AssertionError();
    }

    /**
     * Finds the index of the first digit and starts from the index specified.
     * 
     * @param str
     *            String to search for a digit.
     * @param startidx
     *            Starting index from which to search
     * @return -1 if not found otherwise the index.
     */
    public static int indexOfDigit(String str, int startidx) {
        int ret = -1;
        if (str != null) {
            for (int i = startidx; i < str.length(); i++) {
                // get the first digit..
                if (Character.isDigit(str.charAt(i))) {
                    ret = i;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Finds the index of the first digit.
     * 
     * @param str
     *            String to seach for a digit.
     * @return -1 if not found otherwise the index.
     */
    public static int indexOfDigit(String str) {
        return indexOfDigit(str, 0);
    }

    /**
     * Finds the index of the first non digit and starts from the index
     * specified.
     * 
     * @param str
     *            String to seach for a non digit.
     * @param startidx
     *            Starting index from which to search.
     * @return -1 if not found otherwise the index.
     */
    public static int indexOfNonDigit(String str, int startidx) {
        int ret = -1;
        if (str != null) {
            for (int i = startidx; i < str.length(); i++) {
                // get the first digit..
                if (!Character.isDigit(str.charAt(i))) {
                    ret = i;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Finds the index of the first non digit.
     * 
     * @param str
     *            String to seach for a non digit.
     * @return -1 if not found otherwise the index.
     */
    public static int indexOfNonDigit(String str) {
        return indexOfNonDigit(str, 0);
    }

    /**
     * Return the string of digits from string.
     * 
     * @param str
     *            Source string to search.
     */
    public static String subDigitString(String str) {
        return subDigitString(str, 0);
    }

    /**
     * Return the string of digits from string.
     * 
     * @param str
     *            Source string to search.
     * @param idx
     *            Start index from which to search.
     */
    public static String subDigitString(String str, int idx) {
        String ret = null;
        int sidx = indexOfDigit(str, idx);
        if (sidx != -1) {
            int eidx = indexOfNonDigit(str, sidx);
            ret = (eidx == -1) ? str.substring(sidx) : str
                    .substring(sidx, eidx);
        }
        return ret;
    }

    /**
     * Removes the attribute from the source string and returns.
     */
    public static String stripXmlAttribute(String src, String attrName) {
        String ret = null;
        // quick exit..
        if (src == null) {
            return null;
        }
        // find the attribute and remove all occurances of it..
        final char[] QUOTE = new char[] { '\'', '"' };
        ret = src;
        while (true) {
            int start = ret.indexOf(attrName);
            // no more attributes
            if (start == -1) {
                break;
            }
            // find the end of the attribute
            int openQuote = indexOf(ret, QUOTE, start);
            // there a problem because there's no open quote..
            if (openQuote == -1) {
                break;
            }
            // look for the closed quote
            int closeQuote = indexOf(ret, QUOTE, openQuote + 1);
            if (closeQuote == -1) {
                break;
            }
            // remove the space either before or after the attribute
            if (start - 1 >= 0 && ret.charAt(start - 1) == ' ') {
                start -= 1;
            } else if (closeQuote + 1 < ret.length()
                    && ret.charAt(closeQuote + 1) == ' ') {
                closeQuote += 1;
            }
            // construct new string from parts..
            StringBuffer buf = new StringBuffer();
            buf.append(ret.substring(0, start));
            buf.append(ret.substring(closeQuote + 1));
            ret = buf.toString();
        }
        return ret;
    }

    /**
     * Removes newline characters (0x0a and 0x0d) from a string.
     */
    public static String stripNewlines(String src) {
        String dest = null;
        if (src != null) {
            StringBuffer b = new StringBuffer();
            int max = src.length();
            for (int i = 0; i < max; i++) {
                char c = src.charAt(i);
                if (c != 0x0a && c != 0x0d)
                    b.append(c);
            }
            dest = b.toString();
        }
        return dest;
    }

    /**
     * Finds the start index of the comparison string regards of case.
     * 
     * @param src
     *            String to search.
     * @param cmp
     *            Comparsion string to find.
     * @return -1 if not found otherwise the index of the starting character.
     */
    public static int indexOfIgnoreCase(String src, String cmp) {
        // quick check exit...
        if (src == null || cmp == null) {
            return -1;
        }
        String isrc = src.toUpperCase();
        String icmp = cmp.toUpperCase();
        return isrc.indexOf(icmp);
    }

    /**
     * Strip XML comments
     */
    public static String stripXmlComments(String src) {
        final String END_XMLCOMMENT = "-->";
        final String START_XMLCOMMENT = "<!--";
        // quick exit for invalid data
        if (src == null) {
            return null;
        }
        // loop until all comments are removed..
        String ret = src;
        while (true) {
            int start = ret.indexOf(START_XMLCOMMENT);
            // no xml comment
            if (start == -1) {
                break;
            }
            int end = ret.indexOf(END_XMLCOMMENT, start);
            // exit invalid xml..
            if (end == -1) {
                break;
            }
            // construct new string from parts..
            StringBuffer buf = new StringBuffer();
            buf.append(ret.substring(0, start));
            buf.append(ret.substring(end + END_XMLCOMMENT.length()));
            ret = buf.toString();
        }
        return ret;
    }

    public static int indexOf(String src, char[] ch) {
        return indexOf(src, ch, 0);
    }

    public static int indexOf(String src, char[] ch, int idx) {
        int ret = Integer.MAX_VALUE;
        for (int i = 0; i < ch.length; i++) {
            int tmp = src.indexOf(ch[i], idx);
            if (tmp != -1 && tmp < ret) {
                ret = tmp;
            }
        }
        return (ret == Integer.MAX_VALUE) ? -1 : ret;
    }

    /**
     * Determines if a string is empty. Empty is defined as null or empty
     * string.
     * 
     * <pre>
     *  StringUtil.isEmpty(null)               = true
     *  StringUtil.isEmpty(&quot;&quot;)       = true
     *  StringUtil.isEmpty(&quot; &quot;)      = false
     *  StringUtil.isEmpty(&quot;bob&quot;)    = false
     *  StringUtil.isEmpty(&quot; bob &quot;)  = false
     * </pre>
     * 
     * @param val
     *            string to evaluate as empty.
     * @return true if the string is empty else false.
     */
    public static boolean isEmpty(String val) {
        return (val == null) ? true : "".equals(val) ? true : false;
    }

    /**
     * Determines if a string is not empty. Its the exact opposite for
     * {@link #isEmpty(String)}.
     * 
     * @param val
     *            string to evaluate.
     * @return true if the string is not empty
     */
    public static boolean isNotEmpty(String val) {
        return !isEmpty(val);
    }

    /**
     * <pre>
     *      StringUtil.isBlank(null)                = true
     *      StringUtil.isBlank(&quot;&quot;)        = true
     *      StringUtil.isBlank(&quot; &quot;)       = true
     *      StringUtil.isBlank(&quot;bob&quot;)     = false
     *      StringUtil.isBlank(&quot;  bob  &quot;) = false
     * </pre>
     */
    public static boolean isBlank(String val) {
        return (val == null) ? true : isEmpty(val.trim());
    }

    public static boolean isNotBlank(String val) {
        return !isBlank(val);
    }

    /**
     * Returns a properties object w/ the key/value pairs parsed from the string
     * passed in.
     */
    public static Properties toProperties(String value) {
        Properties ret = new Properties();
        // make sure there's a value present..
        if (isNotBlank(value)) {
            try {
                // get the bytes..
                byte[] bytes = value.getBytes("ISO-8859-1");
                // load into the properties object..
                ret.load(new ByteArrayInputStream(bytes));
            } catch (RuntimeException ex) {
                // don't stop the runtime exception
                throw ex;
            } catch (Exception ex) {
                // throw the error..
                throw new IllegalStateException(ex);
            }
        }
        return ret;
    }

    /**
     * Simple variable replacement internally using regular expressions.
     * 
     * <pre>
     * String o = &quot;Some string with a ${variable} in it.&quot;;
     * String n = replaceVariable(o, &quot;variable&quot;, &quot;something&quot;);
     * String r = &quot;Some string with a something in it&quot;;
     * assert r.equals(n);
     * </pre>
     * 
     * @param o
     *            Original string to do the replacement on.
     * @param var
     *            String representation of the variable to replace.
     * @param val
     *            Value to replace the variable with.
     * @return String will all the variables replaced with the value.
     * 
     * @throws IllegalArgumentException
     *             iff o is null, var is blank, or val is null.
     */
    public static String replaceVariable(String o, String var, String val) {
        try {
            if (o == null || isBlank(var) || val == null) {
                throw new IllegalArgumentException();
            }
            String regex = VAR_REG_EX_START + var + VAR_REG_EX_END;
            String value = Matcher.quoteReplacement(val);
            return o.replaceAll(regex, value);
        } catch (RuntimeException e) {
            // catch from reqex too..
            StringBuilder bld = new StringBuilder();
            bld.append(" var: ").append(var);
            bld.append(" val: ").append(val);
            bld.append(" o: ").append(o);
            throw new IllegalArgumentException(bld.toString());
        }
    }

    private static final String VAR_REG_EX_START = "\\$\\{";

    private static final String VAR_REG_EX_END = "\\}";

    /**
     * Determines if the string parameter 'str' ends with the character value.
     * 
     * @param str
     *            String to check for the character at the end.
     * @param value
     *            The character to look for at the end of the string.
     * @return true iff character parameter is found at the end of the string
     *         parameter otherwise false.
     */
    public static boolean endsWith(final String str, final char value) {
        return StringUtil.isBlank(str) ? false
                : str.charAt(str.length() - 1) == value;
    }

    /**
     * Parses a line into a List of strings.
     * 
     * @param line
     *            String to parse.
     * @param fsep
     *            Field separator
     * @param tqul
     *            Text qualifier.
     * @return list of string separated by a delimiter passed in by 'fsep' and
     *         text is qualified by the parameter 'tqul'.
     */
    public static List<String> parseLine(final String line, final char fsep,
            final char tqul) {
        assert isNotBlank(line);
        List<String> fields = new ArrayList<String>();
        // sometimes, a line will end with the delimiter; make sure we do
        // not create a blank key/value pair for it
        int length = endsWith(line, fsep) ? line.length() - 1 : line.length();
        int j = 0;
        char ch, next_ch;
        int whitespace = 0;
        boolean inQuotes = false;
        boolean fieldStarted = false;
        boolean fieldFinished = false;
        StringBuilder field = new StringBuilder();
        while (j < length) {
            ch = line.charAt(j);
            if (isWhitespace(ch)) {
                if (fieldStarted) {
                    whitespace++;
                    field.append(ch);
                }
            } else {
                fieldStarted = true;
                if (ch == tqul) {
                    whitespace = 0;
                    if (inQuotes) {
                        if (j + 1 < length) {
                            next_ch = line.charAt(j + 1);
                            if (next_ch == tqul) {
                                field.append(ch);
                                j++; // skip the extra double quote
                            } else {
                                inQuotes = false;
                            }
                        } else {
                            inQuotes = false;
                        }
                    } else {
                        inQuotes = true;
                    }
                } else if (ch == fsep) {
                    if (inQuotes) {
                        whitespace = 0;
                        field.append(ch);
                    } else {
                        fieldFinished = true;
                    }
                } else {
                    whitespace = 0;
                    field.append(ch);
                }
            } // else (not whitespace)
            if (fieldFinished) {
                String f = field.toString();
                // Trim any white space that occurred at the end of the
                // field.
                // We can't just use trim() because there may have been
                // double quotes around leading and/or trailing whitespace
                // that the user wants to keep, and we've filtered out
                // the double quotes at this point.
                if (whitespace > 0) {
                    f = f.substring(0, f.length() - whitespace);
                }
                fields.add(f);
                field.setLength(0);
                whitespace = 0;
                fieldStarted = false;
                fieldFinished = false;
            }
            j++;
        }
        if (inQuotes) {
            fields = null;
            throw new IllegalStateException(
                    "Unterminated quotation mark detected.");
        } else {
            // Either we were at the end of a field when we reached
            // the end of the line or we ended with a comma, so there
            // is one more empty field after the comma. Parse the
            // field in either case.
            String f = field.toString();
            // Trim any white space that occurred at the end of the field.
            // We can't just use trim() because there may have been
            // double quotes around leading and/or trailing whitespace
            // that the user wants to keep, and we've filtered out
            // the double quotes at this point.
            if (whitespace > 0) {
                f = f.substring(0, f.length() - whitespace);
            }
            fields.add(f);
        }
        return fields;
    }

    /**
     * Determine if this is a white space character. Whitespace characters are
     * defined as the character ' ' and the tab character.
     */
    public static boolean isWhitespace(final char ch) {
        return (ch == ' ' || ch == '\t');
    }

    /**
     * Create a random Unicode string.
     */
    public static String randomString() {
        return randomString(new Random());
    }

    /**
     * Create a random length Unicode string based on the {@link Random} object
     * passed in.
     */
    public static String randomString(Random r) {
        return randomString(r, Math.abs(r.nextInt(257)));
    }

    /**
     * Create a random string of fixed length based on the {@link Random} object
     * passed in.  Insure that the string is built w/ Unicode characters.
     * 
     * @param r
     *            used to get random unicode characters.
     * @param length
     *            fixed length of string.
     * @return a randomly generated string based on the parameters.
     */
    public static String randomString(Random r, int length) {
        StringBuilder bld = new StringBuilder(length);
        while (bld.length() < length) {
            // get a random 16 bit number..
            int rnd = r.nextInt() & 0x0000ffff;
            if (Character.isLetter(rnd)) {
                bld.append((char) rnd);
            }
        }
        return bld.toString();
    }
}
