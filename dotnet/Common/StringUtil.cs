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
using System.Text;
using System.Text.RegularExpressions;
using System.Diagnostics;
using System.Collections.Generic;
using System.Linq;
using Org.IdentityConnectors.Common.Security;

namespace Org.IdentityConnectors.Common
{
    public static class StringUtil
    {

        /// <summary>
        /// Finds the index of the first digit and starts from the index specified.
        /// </summary>
        /// <param name="str">
        ///            String to search for a digit. </param>
        /// <param name="startidx">
        ///            Starting index from which to search </param>
        /// <returns> -1 if not found otherwise the index. </returns>
        public static int IndexOfDigit(string str, int startidx)
        {
            int ret = -1;
            if (str != null)
            {
                for (int i = startidx; i < str.Length; i++)
                {
                    // get the first digit..
                    if (char.IsDigit(str[i]))
                    {
                        ret = i;
                        break;
                    }
                }
            }
            return ret;
        }

        /// <summary>
        /// Finds the index of the first digit.
        /// </summary>
        /// <param name="str">
        ///            String to seach for a digit. </param>
        /// <returns> -1 if not found otherwise the index. </returns>
        public static int IndexOfDigit(string str)
        {
            return IndexOfDigit(str, 0);
        }

        /// <summary>
        /// Finds the index of the first non digit and starts from the index
        /// specified.
        /// </summary>
        /// <param name="str">
        ///            String to seach for a non digit. </param>
        /// <param name="startidx">
        ///            Starting index from which to search. </param>
        /// <returns> -1 if not found otherwise the index. </returns>
        public static int IndexOfNonDigit(string str, int startidx)
        {
            int ret = -1;
            if (str != null)
            {
                for (int i = startidx; i < str.Length; i++)
                {
                    // get the first digit..
                    if (!char.IsDigit(str[i]))
                    {
                        ret = i;
                        break;
                    }
                }
            }
            return ret;
        }

        /// <summary>
        /// Finds the index of the first non digit.
        /// </summary>
        /// <param name="str">
        ///            String to seach for a non digit. </param>
        /// <returns> -1 if not found otherwise the index. </returns>
        public static int IndexOfNonDigit(string str)
        {
            return IndexOfNonDigit(str, 0);
        }

        /// <summary>
        /// Return the string of digits from string.
        /// </summary>
        /// <param name="str">
        ///            Source string to search. </param>
        public static string SubDigitString(string str)
        {
            return SubDigitString(str, 0);
        }


        /// <summary>
        /// Return the string of digits from string.
        /// </summary>
        /// <param name="str">
        ///            Source string to search. </param>
        /// <param name="idx">
        ///            Start index from which to search. </param>
        public static string SubDigitString(string str, int idx)
        {
            string ret = null;
            int sidx = IndexOfDigit(str, idx);
            if (sidx != -1)
            {
                int eidx = IndexOfNonDigit(str, sidx);
                ret = (eidx == -1) ? str.Substring(sidx) : str.Substring(sidx, eidx - sidx);
            }
            return ret;
        }

        /// <summary>
        /// Removes the attribute from the source string and returns.
        /// </summary>
        public static string StripXmlAttribute(string src, string attrName)
        {
            string ret = null;
            // quick exit..
            if (src == null)
            {
                return null;
            }
            // find the attribute and remove all occurances of it..
            char[] quote = new char[] { '\'', '"' };
            ret = src;
            while (true)
            {
                int start = ret.IndexOf(attrName);
                // no more attributes
                if (start == -1)
                {
                    break;
                }
                // find the end of the attribute
                int openQuote = IndexOf(ret, quote, start);
                // there a problem because there's no open quote..
                if (openQuote == -1)
                {
                    break;
                }
                // look for the closed quote
                int closeQuote = IndexOf(ret, quote, openQuote + 1);
                if (closeQuote == -1)
                {
                    break;
                }
                // remove the space either before or after the attribute
                if (start - 1 >= 0 && ret[start - 1] == ' ')
                {
                    start -= 1;
                }
                else if (closeQuote + 1 < ret.Length && ret[closeQuote + 1] == ' ')
                {
                    closeQuote += 1;
                }
                // construct new string from parts..
                StringBuilder builder = new StringBuilder();
                builder.Append(ret.Substring(0, start));
                builder.Append(ret.Substring(closeQuote + 1));
                ret = builder.ToString();
            }
            return ret;
        }

        /// <summary>
        /// Removes newline characters (0x0a and 0x0d) from a string.
        /// </summary>
        public static string StripNewlines(string src)
        {
            String dest = null;
            if (src != null)
            {
                StringBuilder b = new StringBuilder();
                int max = src.Length;
                for (int i = 0; i < max; i++)
                {
                    char c = src[i];
                    if (c != 0x0a && c != 0x0d)
                    {
                        b.Append(c);
                    }
                }
                dest = b.ToString();
            }
            return dest;
        }

        /// <summary>
        /// Finds the start index of the comparison string regards of case.
        /// </summary>
        /// <param name="src">
        ///            String to search. </param>
        /// <param name="cmp">
        ///            Comparsion string to find. </param>
        /// <returns> -1 if not found otherwise the index of the starting character. </returns>
        public static int IndexOfIgnoreCase(string src, string cmp)
        {
            // quick check exit...
            if (src == null || cmp == null)
            {
                return -1;
            }
            string isrc = src.ToUpper();
            string icmp = cmp.ToUpper();
            return isrc.IndexOf(icmp);
        }



        private const string END_XMLCOMMENT = "-->";
        private const string START_XMLCOMMENT = "<!--";

        /// <summary>
        /// Strip XML comments.
        /// </summary>
        public static string StripXmlComments(string src)
        {
            // quick exit for invalid data
            if (src == null)
            {
                return null;
            }
            // loop until all comments are removed..
            string ret = src;
            while (true)
            {
                int start = ret.IndexOf(START_XMLCOMMENT);
                // no xml comment
                if (start == -1)
                {
                    break;
                }
                int end = ret.IndexOf(END_XMLCOMMENT, start);
                // exit invalid xml..
                if (end == -1)
                {
                    break;
                }
                // construct new string from parts..
                StringBuilder builder = new StringBuilder();
                builder.Append(ret.Substring(0, start));
                builder.Append(ret.Substring(end + END_XMLCOMMENT.Length));
                ret = builder.ToString();
            }
            return ret;
        }

        public static int IndexOf(string src, char[] ch)
        {
            return IndexOf(src, ch, 0);
        }

        public static int IndexOf(string src, char[] ch, int idx)
        {
            int ret = int.MaxValue;
            for (int i = 0; i < ch.Length; i++)
            {
                int tmp = src.IndexOf(ch[i], idx);
                if (tmp != -1 && tmp < ret)
                {
                    ret = tmp;
                }
            }
            return (ret == int.MaxValue) ? -1 : ret;
        }

        /// <summary>
        /// Determines if a string is empty. Empty is defined as null or empty
        /// string.
        /// 
        /// <pre>
        /// StringUtil.IsEmpty(null)     = true
        /// StringUtil.IsEmpty("")       = true
        /// StringUtil.IsEmpty(" ")      = false
        /// StringUtil.IsEmpty("bob")    = false
        /// StringUtil.IsEmpty(" bob ")  = false
        /// </pre>
        /// </summary>
        /// <param name="val">
        ///            string to evaluate as empty. </param>
        /// <returns> true if the string is empty else false. </returns>
        public static bool IsEmpty(string value)
        {
            return String.IsNullOrEmpty(value) || String.Empty == value;
            //return (val == null) ? true : "".Equals(val) ? true : false;
        }

        /// <summary>
        /// Determines if a string is not empty. Its the exact opposite for
        /// <seealso cref="#IsEmpty(String)"/>.
        /// </summary>
        /// <param name="val">
        ///            string to evaluate. </param>
        /// <returns> true if the string is not empty </returns>
        public static bool IsNotEmpty(string val)
        {
            return !IsEmpty(val);
        }

        /// <summary>
        /// Checks if a String is whitespace, empty ("") or null.
        /// 
        /// <pre>
        /// StringUtil.IsBlank(null)      = true
        /// StringUtil.IsBlank("")        = true
        /// StringUtil.IsBlank(" ")       = true
        /// StringUtil.IsBlank("bob")     = false
        /// StringUtil.IsBlank("  bob  ") = false
        /// </pre>
        /// </summary>
        /// <param name="val">
        ///            the String to check, may be null
        /// </param>
        /// <returns> {@code true} if the String is null, empty or whitespace </returns>
        public static bool IsBlank(string val)
        {
            return String.IsNullOrEmpty(val) || val.Trim().Length == 0;
        }


        /// <summary>
        /// Checks if a String is not empty (""), not null and not whitespace only.
        ///     
        /// <pre>
        /// StringUtil.IsNotBlank(null)      = false
        /// StringUtil.IsNotBlank("")        = false
        /// StringUtil.IsNotBlank(" ")       = false
        /// StringUtil.IsNotBlank("bob")     = true
        /// StringUtil.IsNotBlank("  bob  ") = true
        /// </pre>
        /// </summary>
        /// <param name="val">
        ///            the String to check, may be null
        /// </param>
        /// <returns> {@code true} if the String is not empty and not null and not
        ///         whitespace </returns>
        public static bool IsNotBlank(string val)
        {
            return !IsBlank(val);
        }

        /// <summary>
        /// Returns a properties object w/ the key/value pairs parsed from the string
        /// passed in.
        /// </summary>
        public static Dictionary<string, string> ToProperties(String input)
        {
            Dictionary<string, string> ret = new Dictionary<string, string>(0);
            // make sure there's a value present..
            if (IsNotBlank(input))
            {
                try
                {
                    ret = input.Split(new string[] { Environment.NewLine }, StringSplitOptions.None).Select(value => value.Split('=')).ToDictionary(pair => pair[0], pair => pair[1]);
                }
                catch (IndexOutOfRangeException ex)
                {
                    // don't stop the runtime exception
                    // if the text isn't in the right format
                    throw ex;
                }
                catch (ArgumentException ex)
                {
                    // if there are duplicate keys
                    // throw the error..
                    throw ex;
                }
            }
            return ret;
        }

        /// <summary>
        /// Simple variable replacement internally using regular expressions.
        /// 
        /// <pre>
        /// String o = &quot;Some string with a ${variable} in it.&quot;
        /// String n = replaceVariable(o, &quot;variable&quot;, &quot;something&quot;);
        /// String r = &quot;Some string with a something in it&quot;
        /// assert r.equals(n);
        /// </pre>
        /// </summary>
        /// <param name="o">
        ///            Original string to do the replacement on. </param>
        /// <param name="variable">
        ///            String representation of the variable to replace. </param>
        /// <param name="val">
        ///            Value to replace the variable with. </param>
        /// <returns> String will all the variables replaced with the value.
        /// </returns>
        /// <exception cref="IllegalArgumentException">
        ///             if o is null, var is blank, or val is null. </exception>
        public static string ReplaceVariable(string o, string variable, string val)
        {
            try
            {
                if (o == null || IsBlank(variable) || val == null)
                {
                    throw new System.ArgumentException();
                }
                string regex = VAR_REG_EX_START + variable + VAR_REG_EX_END;
                string value = Regex.Escape(val);
                return o.Replace(regex, value);
            }
            catch (Exception)
            {
                // catch from reqex too..
                StringBuilder bld = new StringBuilder();
                bld.Append(" var: ").Append(variable);
                bld.Append(" val: ").Append(val);
                bld.Append(" o: ").Append(o);
                throw new System.ArgumentException(bld.ToString());
            }
        }
        private const string VAR_REG_EX_START = "\\$\\{";
        private const string VAR_REG_EX_END = "\\}";

        /// <summary>
        /// Determines if the string parameter 'str' ends with the character value.
        /// </summary>
        /// <param name="str">
        ///            String to check for the character at the end. </param>
        /// <param name="value">
        ///            The character to look for at the end of the string. </param>
        /// <returns> true if character parameter is found at the end of the string
        ///         parameter otherwise false. </returns>
        public static bool EndsWith(string str, char value)
        {
            return StringUtil.IsBlank(str) ? false : str[str.Length - 1] == value;
        }

        /// <summary>
        /// Parses a line into a List of strings.
        /// </summary>
        /// <param name="line">
        ///            String to parse. </param>
        /// <param name="fsep">
        ///            Field separator </param>
        /// <param name="tqul">
        ///            Text qualifier. </param>
        /// <returns> list of string separated by a delimiter passed in by 'fsep' and
        ///         text is qualified by the parameter 'tqul'. </returns>
        public static IList<string> ParseLine(string line, char fsep, char tqul)
        {
            Debug.Assert(IsNotBlank(line));
            IList<string> fields = new List<string>();
            // sometimes, a line will end with the delimiter; make sure we do
            // not create a blank key/value pair for it
            int length = EndsWith(line, fsep) ? line.Length - 1 : line.Length;
            int j = 0;
            char ch, nextCh;
            int whitespace = 0;
            bool inQuotes = false;
            bool fieldStarted = false;
            bool fieldFinished = false;
            StringBuilder field = new StringBuilder();
            while (j < length)
            {
                ch = line[j];
                if (IsWhitespace(ch))
                {
                    if (fieldStarted)
                    {
                        whitespace++;
                        field.Append(ch);
                    }
                }
                else
                {
                    fieldStarted = true;
                    if (ch == tqul)
                    {
                        whitespace = 0;
                        if (inQuotes)
                        {
                            if (j + 1 < length)
                            {
                                nextCh = line[j + 1];
                                if (nextCh == tqul)
                                {
                                    field.Append(ch);
                                    j++; // skip the extra double quote
                                }
                                else
                                {
                                    inQuotes = false;
                                }
                            }
                            else
                            {
                                inQuotes = false;
                            }
                        }
                        else
                        {
                            inQuotes = true;
                        }
                    }
                    else if (ch == fsep)
                    {
                        if (inQuotes)
                        {
                            whitespace = 0;
                            field.Append(ch);
                        }
                        else
                        {
                            fieldFinished = true;
                        }
                    }
                    else
                    {
                        whitespace = 0;
                        field.Append(ch);
                    }
                } // else (not whitespace)
                if (fieldFinished)
                {
                    string f = field.ToString();
                    // Trim any white space that occurred at the end of the
                    // field.
                    // We can't just use trim() because there may have been
                    // double quotes around leading and/or trailing whitespace
                    // that the user wants to keep, and we've filtered out
                    // the double quotes at this point.
                    if (whitespace > 0)
                    {
                        f = f.Substring(0, f.Length - whitespace);
                    }
                    fields.Add(f);
                    field.Length = 0;
                    whitespace = 0;
                    fieldStarted = false;
                    fieldFinished = false;
                }
                j++;
            }
            if (inQuotes)
            {
                fields = null;
                throw new InvalidOperationException("Unterminated quotation mark detected.");
            }
            else
            {
                // Either we were at the end of a field when we reached
                // the end of the line or we ended with a comma, so there
                // is one more empty field after the comma. Parse the
                // field in either case.
                string f = field.ToString();
                // Trim any white space that occurred at the end of the field.
                // We can't just use trim() because there may have been
                // double quotes around leading and/or trailing whitespace
                // that the user wants to keep, and we've filtered out
                // the double quotes at this point.
                if (whitespace > 0)
                {
                    f = f.Substring(0, f.Length - whitespace);
                }
                fields.Add(f);
            }
            return fields;
        }

        /// <summary>
        /// Determine if this is a white space character. Whitespace characters are
        /// defined as the character ' ' and the tab character.
        /// </summary>
        public static bool IsWhitespace(char ch)
        {
            return Char.IsWhiteSpace(ch);// (ch == ' ' || ch == '\t');
        }

        /// <summary>
        /// Create a random Unicode string.
        /// </summary>
        public static string RandomString()
        {
            return RandomString(new Random((int)DateTime.Now.Ticks));
        }

        /// <summary>
        /// Create a random length Unicode string based on the <seealso cref="Random"/> object
        /// passed in.
        /// </summary>
        public static string RandomString(Random r)
        {
            return RandomString(r, Math.Abs(r.Next(257)));
        }

        /// <summary>
        /// Create a random string of fixed length based on the <seealso cref="Random"/> object
        /// passed in. Insure that the string is built w/ Unicode characters.
        /// </summary>
        /// <param name="random">
        ///            used to get random unicode characters. </param>
        /// <param name="length">
        ///            fixed length of string. </param>
        /// <returns> a randomly generated string based on the parameters. </returns>
        public static string RandomString(Random random, int length)
        {
            StringBuilder bld = new StringBuilder(length);

            while (bld.Length < length)
            {
                //char ch = (char)random.Next('A', 'Z' + 1);
                char ch = Convert.ToChar(Convert.ToInt32(Math.Floor(26 * random.NextDouble() + 65)));
                bld.Append(ch);
            }
            return bld.ToString();
        }

        /// <summary>
        /// Constructs a secure string from a char []. The char[] will
        /// be cleared out when finished.
        /// </summary>
        /// <param name="val">The characters to use. Will be cleared
        /// out.</param>
        /// <returns>A secure string representation</returns>
        public static GuardedString NewGuardedString(char[] val)
        {
            GuardedString rv = new GuardedString();
            for (int i = 0; i < val.Length; i++)
            {
                rv.AppendChar(val[i]);
                val[i] = (char)0;
            }
            return rv;
        }


        public static bool IsTrue(string val)
        {
            if (!IsBlank(val))
            {
                // clean up the value..
                val = val.Trim().ToLower();
                if (val.Equals("1") || val.Equals("on") || val.Equals("true"))
                {
                    return true;
                }
            }
            return false;
        }
    }
}