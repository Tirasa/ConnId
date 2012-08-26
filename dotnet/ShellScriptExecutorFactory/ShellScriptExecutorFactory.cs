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
 * Portions Copyrighted 2012 ForgeRock AS
 */
using System;
using System.IO;
using System.Security;
using System.Diagnostics;
using System.Reflection;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using Org.IdentityConnectors.Common.Security;
using System.Threading;

namespace Org.IdentityConnectors.Common.Script.Shell
{

    /// <summary>
    /// Process shell scripts.  Valid arguments are below, the rest will be
    /// used as environment variables.  The return is the exit code of the
    /// process.
    /// <ul>
    /// <li>USERNAME - name of the user to run this script as..</li>
    /// <li>PASSWORD - (GuardedString or SecureString) password for the user to run this script as..</li>
    /// <li>WORKINGDIR - working directory run this script in..</li>
    /// <li>TIMEOUT - timeout waiting for script to finish in ms (default: 30 secs)</li>
    /// </ul>
    /// </summary>
    [ScriptExecutorFactoryClass("Shell")]
    public class ShellScriptExecutorFactory : ScriptExecutorFactory
    {

        /// <summary>
        /// Creates a script executor give the Shell script.
        /// </summary>
        override
        public ScriptExecutor NewScriptExecutor(Assembly[] refs, string script, bool compile)
        {
            return new ShellScriptExecutor(script);
        }

        /// <summary>
        /// Processes the script.
        /// </summary>
        class ShellScriptExecutor : ScriptExecutor
        {
            private readonly string _script;

            public ShellScriptExecutor(string script)
            {
                _script = script;
            }
            public object Execute(IDictionary<string, object> arguments)
            {
                //
                string fn = String.Empty;
                // create the process info..
                Process process = new Process();
                // set the defaults..
                process.StartInfo.CreateNoWindow = true;
                process.StartInfo.UseShellExecute = true;
                process.StartInfo.RedirectStandardOutput = true;
                process.StartInfo.RedirectStandardError = true;
                // set the default timeout..
                int timeout = 1000 * 30; // 30 secss
                IDictionary<string, object> result = new Dictionary<string, object>();
                int exitCode = 1;
                try
                {
                    // if there are any environment varibles set to false..
                    process.StartInfo.UseShellExecute = arguments.Count == 0;
                    // take out username and password if they're in the options.
                    foreach (KeyValuePair<string, object> kv in arguments)
                    {
                        if (kv.Key.ToUpper().Equals("USERNAME"))
                        {
                            string domainUser = kv.Value.ToString();
                            string[] split = domainUser.Split(new char[] { '\\' });
                            if (split.Length == 1)
                            {
                                process.StartInfo.UserName = split[0];
                            }
                            else
                            {
                                process.StartInfo.Domain = split[0];
                                process.StartInfo.UserName = split[1];
                            }
                        }
                        else if (kv.Key.ToUpper().Equals("PASSWORD"))
                        {
                            if (kv.Value is SecureString)
                            {
                                process.StartInfo.Password = (SecureString)kv.Value;
                            }
                            else if (kv.Value is GuardedString)
                            {
                                process.StartInfo.Password = ((GuardedString)kv.Value).ToSecureString();
                            }
                            else
                            {
                                throw new ArgumentException("Invalid type for password.");
                            }
                        }
                        else if (kv.Key.ToUpper().Equals("WORKINGDIR"))
                        {
                            process.StartInfo.WorkingDirectory = kv.Value.ToString();
                        }
                        else if (kv.Key.ToUpper().Equals("TIMEOUT"))
                        {
                            timeout = Int32.Parse(kv.Value.ToString());
                        }
                        else
                        {
                            process.StartInfo.EnvironmentVariables[kv.Key] = kv.Value.ToString();
                        }
                    }
                    // write out the script..
                    fn = Path.GetTempFileName() + ".cmd";
                    StreamWriter sw = null;
                    try
                    {
                        sw = new StreamWriter(fn);
                        sw.Write(_script);
                    }
                    finally
                    {
                        sw.Close();
                        sw.Dispose();
                    }
                    // set temp file..
                    process.StartInfo.FileName = fn;
                    // execute script..
                    process.Start();
                    string stdout = process.StandardOutput.ReadToEnd();
                    // http://msdn.microsoft.com/en-us/library/system.diagnostics.process.standardoutput.aspx
                    // Use asynchronous read operations on at least one of the streams.
                    AsynchronousReader msr_stderr = new AsynchronousReader(process.StandardError);
                    // Create the thread objects to run the code asynchronously
                    Thread t_stderr = new Thread(msr_stderr.Go);
                    t_stderr.Start();
                    t_stderr.Join();
                    // wait for the process to exit..
                    if (!process.WaitForExit(timeout))
                    {
                        throw new TimeoutException("Script failed to exit in time!");
                    }
                    exitCode = process.ExitCode;
                    result.Add("stdout", stdout);
                    result.Add("stderr", msr_stderr.Text);
                }
                catch (Exception e)
                {
                    Trace.TraceError("Failed to execute script with exception {0}", e.Message);
                }
                finally
                {
                    // close up the process
                    process.Close();
                    process.Dispose();
                }
                // clean up temp file..
                try
                {
                    File.Delete(fn);
                }
                catch (Exception e)
                {

                }
                result.Add("exitCode", exitCode);
                return result;
            }
        }
    }
    internal class AsynchronousReader
    {
        StreamReader _sr = null;
        string _text = null;
        public string Text { get { return _text; } }

        public AsynchronousReader(StreamReader sr)
        {
            _sr = sr;
        }

        public void Go()
        {
            _text = _sr.ReadToEnd();
        }
    }
}
