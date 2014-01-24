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
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
using System;
using System.Collections.Generic;
using System.Reflection;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Script;
using Org.IdentityConnectors.Framework.Common.Objects;
using NUnit.Framework;

namespace FrameworkTests
{
    /// <summary>
    /// Description of ScriptTests.
    /// </summary>
    [TestFixture]
    public class ScriptTests
    {
        [Test]
        public void testBooScripting()
        {
            ScriptExecutorFactory factory = ScriptExecutorFactory.NewInstance("BOO");
            ScriptExecutor exe = factory.NewScriptExecutor(new Assembly[0], "x", false);
            IDictionary<string, object> vals = new Dictionary<string, object>();
            vals["x"] = 1;
            Assert.AreEqual(1, exe.Execute(vals));
            vals["x"] = 2;
            Assert.AreEqual(2, exe.Execute(vals));
        }
        [Test]
        public void testShellScripting()
        {
            ScriptExecutorFactory factory = ScriptExecutorFactory.NewInstance("Shell");
            ScriptExecutor exe = factory.NewScriptExecutor(new Assembly[0], "echo bob", false);
            IDictionary<string, object> vals = new Dictionary<string, object>();
            Assert.AreEqual(0, exe.Execute(vals));
        }
        [Test]
        [ExpectedException(typeof(ArgumentException))]
        public void testUnsupported()
        {
            ScriptExecutorFactory.NewInstance("fadsflkj");
        }

        [Test]
        public void testBasic()
        {
            ScriptBuilder builder = new ScriptBuilder();
            builder.ScriptLanguage = "Groovy";
            builder.ScriptText = "print 'foo'";
            Script s1 = builder.Build();
            Assert.AreEqual("Groovy", s1.ScriptLanguage);
            Assert.AreEqual("print 'foo'", s1.ScriptText);
            builder = new ScriptBuilder();
            builder.ScriptLanguage = "Groovy";
            builder.ScriptText = "print 'foo'";
            Script s2 = builder.Build();
            Assert.AreEqual(s1, s2);
            Assert.AreEqual(s1.GetHashCode(), s2.GetHashCode());
        }

        [Test]
        public void testLanguageNotBlank()
        {
            try
            {
                ScriptBuilder builder = new ScriptBuilder();
                builder.ScriptText = "print 'foo'";
                builder.Build();
                Assert.Fail();
            }
            catch (ArgumentException)
            {
                // OK.
            }

            try
            {
                ScriptBuilder builder = new ScriptBuilder();
                builder.ScriptText = "print 'foo'";
                builder.ScriptLanguage = "";
                builder.Build();
                Assert.Fail();
            }
            catch (ArgumentException)
            {
                // OK.
            }

            try
            {
                ScriptBuilder builder = new ScriptBuilder();
                builder.ScriptText = "print 'foo'";
                builder.ScriptLanguage = " ";
                builder.Build();
                Assert.Fail();
            }
            catch (ArgumentException)
            {
                // OK.
            }
        }

        [Test]
        public void testTextNotNull()
        {
            ScriptBuilder builder = new ScriptBuilder();
            try
            {
                builder.ScriptLanguage = "Groovy";
                builder.Build();
                Assert.Fail();
            }
            catch (ArgumentNullException)
            {
                // OK.
            }

            // The text can be empty.
            builder = new ScriptBuilder();
            builder.ScriptLanguage = "Groovy";
            builder.ScriptText = "";
            builder.Build();
        }
    }
}
