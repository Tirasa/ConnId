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
using System.Diagnostics;
using System.Text;
namespace Org.IdentityConnectors.Common
{
    /// <summary>
    /// Description of TraceUtil.
    /// </summary>
    public static class TraceUtil
    {
        /// <summary>
        /// Traces an exception with its stack trace
        /// </summary>
        /// <param name="msg">An optional error message to display in addition to the exception</param>
        /// <param name="e">The exception</param>
        public static void TraceException(String msg, Exception e)
        {
            StringBuilder builder = new StringBuilder();
            if (msg != null)
            {
                builder.AppendLine(msg);
            }
            if (e != null)
            {
                builder.AppendLine(e.ToString());
            }
            Trace.TraceError(builder.ToString());
        }
    }
}