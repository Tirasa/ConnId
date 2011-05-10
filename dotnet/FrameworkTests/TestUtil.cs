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

namespace FrameworkTests
{
    /// <summary>
    /// Description of TestUtil.
    /// </summary>
    public static class TestUtil
    {
        private static readonly object LOCK = new Object();
        private static bool _initialized;


        public static void InitializeLogging()
        {
            lock (LOCK)
            {
                if (!_initialized)
                {
                    ConsoleTraceListener listener =
                        new ConsoleTraceListener();
                    listener.TraceOutputOptions =
                        TraceOptions.ThreadId | TraceOptions.Timestamp;
                    Trace.Listeners.Add(listener);
                    _initialized = true;
                }
            }
        }
    }
}
