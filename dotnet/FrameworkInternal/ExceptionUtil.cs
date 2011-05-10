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
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;
using System.Diagnostics;
using Org.IdentityConnectors.Common;
using System.Runtime.CompilerServices;

namespace Org.IdentityConnectors.Framework.Impl
{
    /// <summary>
    /// Contains utilities to handle exceptions.
    /// </summary>
    public static class ExceptionUtil
    {
        private const string PreserveStackTraceMethodName = "InternalPreserveStackTrace";

        /// <summary>
        /// Preserves the stack trace of <paramref name="exception"/>.
        /// </summary>
        /// <param name="exception">The exception, the stack trace of which to be preserved.</param>
        /// <remarks>In the .Net Framework the stack trace of an exception starts to get populated when it is thrown,
        /// hence if an exception is re-thrown by a method upper in the call chain the stack trace will reflect that the
        /// exception occurred at that position where the exception was actually re-thrown and the original stack trace will
        /// be lost.
        /// <example>
        /// <code>
        ///     try
        ///     {
        ///         function_that_throws_an_exception_with_a_nested_one();
        ///     }
        ///     catch( Exception ex )
        ///     {
        ///         throw ex.InnerException; //clears the stack trace of the nested exception
        ///     }
        /// </code>
        /// </example>
        /// There is no built-in support in .Net to preserve the stack trace, however, an internal method
        /// of the <see cref="T:System.Exception"/> class called <see cref="F:ExceptionUtil.PreserveStackTraceMethodName"/>
        /// can be used to achieve this. Since it is an internal method it might be subject to change, therefore if it is
        /// not possible to invoke the method by any reason the error cause will be traced, but it will not break the execution.
        /// </remarks>
        public static void PreserveStackTrace(Exception exception)
        {
            Assertions.NullCheck(exception, "exception");

            try
            {
                MethodInfo preserveStackTrace = typeof(Exception).GetMethod(
                    PreserveStackTraceMethodName, BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.InvokeMethod);

                preserveStackTrace.Invoke(exception, null);
            }
            catch (Exception ex)
            {
                //it should not ever happen, but we have to make sure that if a next release of .Net Framework does not
                //include the invoked method it will not break the execution
                TraceUtil.TraceException(string.Format(
                    @"Could not set an exception to preserve its stack trace. Exception: ""{0}""", exception), ex);
                return;
            }
        }
    }
}