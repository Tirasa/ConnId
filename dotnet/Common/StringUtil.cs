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
using System;
using Org.IdentityConnectors.Common.Security;
namespace Org.IdentityConnectors.Common
{
    public static class StringUtil
    {
        /// <summary>
        /// Determines if a string is empty.
        /// </summary>
        /// <remarks>
        /// Empty is defined as null or empty
        /// string.
        /// <pre>
        /// StringUtil.isEmpty(null)               = true
        /// StringUtil.isEmpty("")       = true
        /// StringUtil.isEmpty(" ")      = false
        /// StringUtil.isEmpty("bob")    = false
        /// StringUtil.isEmpty(" bob ")  = false
        /// </pre>
        /// </remarks>
        /// <param name="val">string to evaluate as empty.</param>
        /// <returns>true if the string is empty else false.</returns>
        public static bool IsEmpty(String val)
        {
            return (val == null) ? true : val.Length == 0;
        }

        /// <summary>
        /// <pre>
        /// StringUtil.isBlank(null)                = true
        /// StringUtil.isBlank("")        = true
        /// StringUtil.isBlank(" ")       = true
        /// StringUtil.isBlank("bob")     = false
        /// StringUtil.isBlank("  bob  ") = false
        /// </pre>
        /// </summary>
        public static bool IsBlank(String val)
        {
            return (val == null) ? true : IsEmpty(val.Trim());
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