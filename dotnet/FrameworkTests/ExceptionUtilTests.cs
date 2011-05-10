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
using NUnit.Framework;
using Org.IdentityConnectors.Test.Common;
using System.Diagnostics;
using Org.IdentityConnectors.Framework.Impl;

namespace FrameworkTests
{
    /// <summary>
    /// Contains helper function to verify the output of <see cref="ExceptionUtil"/>.
    /// </summary>
    internal class ExceptionUtilTestHelpers
    {
        /// <summary>
        /// Tests whether the <paramref name="exception"/> is originated from the same function as in which the
        /// <paramref name="stackTrace"/> was captured.
        /// </summary>
        /// <param name="exception">The exception to test.</param>
        /// <param name="stackTrace">The captured stack trace to test against.</param>
        public static void AssertStackTrace(Exception exception, StackTrace stackTrace)
        {
            Trace.TraceInformation("Stack trace from the failing method: {0}", stackTrace.ToString());
            Trace.TraceInformation("Exception stack trace: {0}", exception.StackTrace);

            string fullST = stackTrace.ToString();
            int newLinePos = fullST.IndexOf(Environment.NewLine);
            string failingMethodExpected = fullST.Substring(0, (newLinePos == -1) ? fullST.Length : newLinePos);

            newLinePos = exception.StackTrace.IndexOf(Environment.NewLine);
            string failingMethodActual = exception.StackTrace.Substring(0, (newLinePos == -1) ? fullST.Length : newLinePos);

            //check if the first line of the stack trace fetched from the failing method is contained in the
            //first line of the exception's stack trace, i.e. the method which threw the exception is in the
            //stack trace of the exception
            Assert.That(failingMethodActual.Contains(failingMethodExpected));
        }
    }

    [TestFixture]
    public class ExceptionUtilTests
    {
        [Test]
        public void TestPreserveStackTrace()
        {
            StackTrace stackTrace = null;
            try
            {
                try
                {
                    Action bar = () =>
                    {
                        try
                        {
                            //delegate to the failing method
                            Action foo = () =>
                            {
                                stackTrace = new StackTrace(false);
                                throw new InvalidOperationException();
                            };

                            foo();

                            Assert.Fail("Exception was not thrown - 1");
                        }
                        catch (InvalidOperationException iopex)
                        {
                            //wrap the exception to make sure that there is an
                            //inner exception that can be re-thrown later on
                            throw new ArgumentException(string.Empty, iopex);
                        };
                    };

                    bar();

                    Assert.Fail("Exception was not thrown - 2");
                }
                catch (ArgumentException aex)
                {
                    //preserve the stack trace of the nested exception and re-throw it
                    ExceptionUtil.PreserveStackTrace(aex.InnerException);
                    throw aex.InnerException;
                }

                Assert.Fail("Exception was not thrown - 3");
            }
            catch (InvalidOperationException iopex)
            {
                ExceptionUtilTestHelpers.AssertStackTrace(iopex, stackTrace);
            }
        }
    }
}
