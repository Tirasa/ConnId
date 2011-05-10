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
using System.IO;
using System.Reflection;
using System.Collections.Generic;
using System.Diagnostics;

namespace Org.IdentityConnectors.Common.Script
{
    public interface ScriptExecutor
    {
        /// <summary>
        /// Executes the script with the given arguments.
        /// </summary>
        /// <param name="arguments">key/value set of variables to 
        /// pass to the script.</param>
        /// <returns></returns>
        object Execute(IDictionary<string, object> arguments);
    }
    public abstract class ScriptExecutorFactory
    {

        private static readonly object LOCK = new object();

        /// <summary>
        /// Loaded w/ all supported languages.
        /// </summary>
        private static IDictionary<string, Type> _supportedLanguages = null;

        /// <summary>
        /// Load all script executor factory assemblies in the same directory
        /// the 'Common' assembly.
        /// </summary>
        private static IDictionary<string, Type> LoadSupportedLanguages()
        {
            // attempt to process all assemblies..
            IDictionary<string, Type> ret = new Dictionary<string, Type>();
            Assembly assembly = Assembly.GetExecutingAssembly();
            FileInfo thisAssemblyFile = new FileInfo(assembly.Location);
            DirectoryInfo directory = thisAssemblyFile.Directory;
            // get all *ScriptExecutorFactory assmebly from the current directory
            FileInfo[] files = directory.GetFiles("*.ScriptExecutorFactory.dll");
            Type t = typeof(ScriptExecutorFactoryClassAttribute);
            foreach (FileInfo file in files)
            {
                try
                {
                    Assembly lib = Assembly.LoadFrom(file.ToString());
                    foreach (Type type in lib.GetTypes())
                    {
                        Object[] attributes = type.GetCustomAttributes(t, false);
                        if (attributes.Length > 0)
                        {
                            ScriptExecutorFactoryClassAttribute attribute =
                                (ScriptExecutorFactoryClassAttribute)attributes[0];
                            // attempt to test assembly..
                            Activator.CreateInstance(type);
                            // if we made it this far its okay
                            ret[attribute.Language.ToUpper()] = type;
                        }
                    }
                }
                catch (Exception e)
                {
                    TraceUtil.TraceException("Unable to load assembly: " +
                        assembly.FullName + ". This is a fatal exception: ", e);
                    throw;
                }
            }
            return ret;
        }

        private static IDictionary<string, Type> GetSupportedLanguages()
        {
            lock (LOCK)
            {
                if (_supportedLanguages == null)
                {
                    _supportedLanguages = LoadSupportedLanguages();
                }
                return _supportedLanguages;
            }
        }

        /// <summary>
        /// Returns the set of supported languages.
        /// </summary>
        /// <returns>The set of supported languages.</returns>
        public static ICollection<String> SupportedLanguages
        {
            get
            {
                IDictionary<string, Type> map =
                    GetSupportedLanguages();
                return CollectionUtil.AsReadOnlySet(map.Keys);
            }
        }

        /// <summary>
        /// Creates a ScriptExecutorFactory for the given language
        /// </summary>
        /// <param name="language">The name of the language</param>
        /// <returns>The script executor factory</returns>
        /// <exception cref="ArgumentException">If the given language is not
        /// supported.</exception>
        public static ScriptExecutorFactory NewInstance(String language)
        {
            if (language == null)
            {
                throw new ArgumentException("Language must be specified");
            }
            Type type = CollectionUtil.GetValue(GetSupportedLanguages(), language.ToUpper(), null);
            if (type == null)
            {
                throw new ArgumentException("Language not supported: " + language);
            }
            return (ScriptExecutorFactory)Activator.CreateInstance(type);
        }


        /// <summary>
        /// Creates a script executor for the given script.
        /// </summary>
        /// <param name="loader">The classloader that contains the java classes
        /// that the script should have access to.</param>
        /// <param name="script">The script text.</param>
        /// <param name="compile">A hint to tell the script executor whether or
        /// not to compile the given script. This need not be implemented
        /// by all script executors. If true, the caller is saying that
        /// they intend to call the script multiple times with different
        /// arguments, so compile if possible.</param>
        /// <returns>A script executor.</returns>
        public abstract ScriptExecutor NewScriptExecutor(
                Assembly[] referencedAssemblies,
                String script,
                bool compile);
    }

    [AttributeUsage(AttributeTargets.Class, AllowMultiple = false)]
    public class ScriptExecutorFactoryClassAttribute : System.Attribute
    {
        private readonly string _lang;

        /// <summary>
        /// Determine the language supported by the factory.
        /// </summary>
        /// <param name="lang"></param>
        public ScriptExecutorFactoryClassAttribute(string lang)
        {
            _lang = lang;
        }

        public string Language
        {
            get
            {
                return _lang;
            }
        }
    }
}