/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright (c) 2012-2014 ForgeRock AS. All Rights Reserved
*
* The contents of this file are subject to the terms
* of the Common Development and Distribution License
* (the License). You may not use this file except in
* compliance with the License.
*
* You can obtain a copy of the License at
* http://forgerock.org/license/CDDLv1.0.html
* See the License for the specific language governing
* permission and limitations under the License.
*
* When distributing Covered Code, include this CDDL
* Header Notice in each file and include the License file
* at http://forgerock.org/license/CDDLv1.0.html
* If applicable, add the following below the CDDL Header,
* with the fields enclosed by brackets [] replaced by
* your own identifying information:
* "Portions Copyrighted [year] [name of copyright owner]"
*
* 
* Author: Gael Allioux
*/

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Reflection;
using System.Management.Automation;
using System.Management.Automation.Runspaces;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Script;


// namespace Org.IdentityConnectors.Common.Script.PowerShell
namespace Org.ForgeRock.OpenICF.Framework.Common.Script.PowerShell 
{
    [ScriptExecutorFactoryClass("PowerShell")]
    public class PowerShellScriptExecutorFactory : ScriptExecutorFactory
    {

        /// <summary>
        /// Creates a script executor given the PowerShell script.
        /// </summary>
        override
        public ScriptExecutor NewScriptExecutor(Assembly[] referencedAssemblies, string script, bool compile)
        {
            return new PowerShellScriptExecutor(script);
        }

        /// <summary>
        /// Processes the script.
        /// </summary>
        class PowerShellScriptExecutor : ScriptExecutor
        {
            private readonly string _script;

            public PowerShellScriptExecutor()
            {
            }

            public PowerShellScriptExecutor(string script)
            {
                _script = script;
            }

            public PowerShellScriptExecutor(Assembly[] referencedAssemblies, string script)
            {
                _script = script;
            }

            public object Execute(IDictionary<string, object> arguments)
            {
                Command myCommand = new Command(_script,true);
                Runspace runspace = RunspaceFactory.CreateRunspace();
                Pipeline pipeline = null;
                Collection<PSObject> results = null;

                //foreach (String argumentName in arguments.Keys)
                //{
                //    CommandParameter param = new CommandParameter(argumentName, arguments[argumentName]);
                //    myCommand.Parameters.Add(param);
                //}
                
                try
                {
                    runspace.Open();
                    // create a pipeline and give it the command
                    pipeline = runspace.CreatePipeline();
                    pipeline.Commands.Add(myCommand);
                    //pipeline.Commands.Add("Out-String");
                    foreach (String argumentName in arguments.Keys)
                    {
                        runspace.SessionStateProxy.SetVariable(argumentName, arguments[argumentName]);
                    }
                    // execute the script
                    results = pipeline.Invoke();
                }
                catch (Exception e)
                {
                    TraceUtil.TraceException("Unable to run PowerShell script on resource.", e);
                    throw;
                }
                finally
                {
                    pipeline.Dispose();
                    // close & dispose the runspace
                    runspace.Close();
                    runspace.Dispose();
                }

                // return the script result as a single string
                IDictionary<string, string> result = new Dictionary<string, string>();
                int index = 0;
                foreach (PSObject obj in results)                   
                    {
                        result.Add(index.ToString(),obj.ToString());
                        index++;
                    }
                return result;
            }
        }
    }
}
