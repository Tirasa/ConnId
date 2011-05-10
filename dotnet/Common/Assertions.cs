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

namespace Org.IdentityConnectors.Common
{
    /// <summary>
    /// Description of Assertions.
    /// </summary>
    public static class Assertions
    {
        private const string NULL_FORMAT = "Parameter '{0}' must not be null.";
        private const string BLANK_FORMAT = "Parameter '{0}' must not be blank.";

        /// <summary>
        /// Throws <see cref="ArgumentNullException" /> if the parameter <paramref name="o"/>
        /// is <code>null</code>.
        /// </summary>
        /// <param name="o">check if the object is <code>null</code>.</param>
        /// <param name="param">name of the parameter to check for <code>null</code>.</param>
        /// <exception cref="ArgumentNullException">if <paramref name="o"/> is <code>null</code> and constructs a
        /// message with the name of the parameter.</exception>
        public static void NullCheck(Object o, String param)
        {
            if (o == null)
            {
                throw new ArgumentNullException(String.Format(NULL_FORMAT, param));
            }
        }

        /// <summary>
        /// Throws <see cref="ArgumentNullException" /> if the parameter <paramref name="o"/>
        /// is <code>null</code>, otherwise returns its value.
        /// </summary>
        /// <typeparam name="T">the type of the parameter to check for <code>null</code>. Must be a reference type.</typeparam>
        /// <param name="o">check if the object is <code>null</code>.</param>
        /// <param name="param">name of the parameter to check for <code>null</code>.</param>
        /// <returns>the value of the parameter <paramref name="o"/>.</returns>
        /// <exception cref="ArgumentNullException">if <paramref name="o"/> is <code>null</code> and constructs a
        /// message with the name of the parameter.</exception>
        public static T NullChecked<T>(T o, String param) where T : class
        {
            // Avoid calling NullCheck() here to reuse code: it deepens the stack trace.
            // We want the exception to be thrown as close to the call site as possible.
            if (o == null)
            {
                throw new ArgumentNullException(String.Format(NULL_FORMAT, param));
            }
            return o;
        }

        /// <summary>
        /// Throws <see cref="ArgumentException" /> if the parameter <paramref name="o"/>
        /// is <code>null</code> or blank.
        /// </summary>
        /// <param name="o">value to test for blank.</param>
        /// <param name="param">name of the parameter to check.</param>
        /// <exception cref="ArgumentException">if <paramref name="o"/> is <code>null</code> or  blank and constructs a
        /// message with the name of the parameter.</exception>
        public static void BlankCheck(String o, String param)
        {
            if (StringUtil.IsBlank(o))
            {
                throw new ArgumentException(String.Format(BLANK_FORMAT, param));
            }
        }

        /// <summary>
        /// Throws <see cref="ArgumentException" /> if the parameter <paramref name="o"/>
        /// is <code>null</code> or blank, otherwise returns its value.
        /// </summary>
        /// <param name="o">value to test for blank.</param>
        /// <param name="param">name of the parameter to check.</param>
        /// <returns>the value of the parameter <paramref name="o"/>.</returns>
        /// <exception cref="ArgumentException">if <paramref name="o"/> is <code>null</code> or  blank and constructs a
        /// message with the name of the parameter.</exception>
        public static String BlankChecked(String o, String param)
        {
            // Avoid calling BlankCheck() here to reuse code: it deepens the stack trace.
            // We want the exception to be thrown as close to the call site as possible.
            if (StringUtil.IsBlank(o))
            {
                throw new ArgumentException(String.Format(BLANK_FORMAT, param));
            }
            return o;
        }
    }
}
