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
using System.Reflection;
namespace Org.IdentityConnectors.Common
{
    /// <summary>
    /// Description of FrameworkInternalBridge.
    /// </summary>
    internal static class FrameworkInternalBridge
    {
        private static readonly Object LOCK = new Object();
        private static Assembly _assembly = null;
        /// <summary>
        /// Loads a class from the FrameworkInternal module
        /// </summary>
        /// <param name="typeName"></param>
        /// <returns></returns>
        public static Type LoadType(String typeName)
        {

            Assembly assembly;
            lock (LOCK)
            {
                if (_assembly == null)
                {
                    AssemblyName assemName = new AssemblyName();
                    assemName.Name = "FrameworkInternal";
                    _assembly = Assembly.Load(assemName);
                }
                assembly = _assembly;
            }

            return assembly.GetType(typeName, true);

        }
    }
}
