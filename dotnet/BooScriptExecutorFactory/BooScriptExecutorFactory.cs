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
using System.Collections;
using System.Collections.Generic;
using Boo.Lang.Interpreter;
using Boo.Lang.Compiler;

namespace Org.IdentityConnectors.Common.Script.Boo
{
    [ScriptExecutorFactoryClass("Boo")]
    public class BooScriptExecutorFactory : ScriptExecutorFactory
    {
        /// <summary>
        /// Attempt to trigger an exception if the runtime is not present.
        /// </summary>
        public BooScriptExecutorFactory()
        {
            new BooScriptExecutor(new Assembly[0], "1").Execute(null);
        }

        /// <summary>
        /// Creates a script executor give the Boo script.
        /// </summary>
        override
        public ScriptExecutor NewScriptExecutor(Assembly[] referencedAssemblies, string script, bool compile)
        {
            return new BooScriptExecutor(referencedAssemblies, script);
        }

        /// <summary>
        /// Processes the script.
        /// </summary>
        class BooScriptExecutor : ScriptExecutor
        {
            private readonly Assembly[] _referencedAssemblies;
            private readonly string _script;
            private readonly InteractiveInterpreter _engine;

            public BooScriptExecutor(Assembly[] referencedAssemblies, string script)
            {
                _referencedAssemblies = referencedAssemblies;
                _script = script;
                _engine = new InteractiveInterpreter();
                _engine.RememberLastValue = true;
                foreach (Assembly assembly in referencedAssemblies)
                {
                    _engine.References.Add(assembly);
                }
            }
            public object Execute(IDictionary<string, object> arguments)
            {
                // add all the globals
                IDictionary<string, object> args = CollectionUtil.NullAsEmpty(arguments);
                foreach (KeyValuePair<string, object> entry in args)
                {
                    _engine.SetValue(entry.Key, entry.Value);
                }
                CompilerContext context = _engine.Eval(_script);
                if (context.Errors.Count > 0)
                {
                    throw context.Errors[0];
                }
                return _engine.LastValue;
            }
        }
    }
}
