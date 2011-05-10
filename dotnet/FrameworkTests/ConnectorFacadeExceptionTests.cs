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
using System.Diagnostics;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
using Org.IdentityConnectors.Test.Common;
using Org.IdentityConnectors.Common;

namespace FrameworkTests
{
    [TestFixture]
    public class ConnectorFacadeExceptionTests
    {
        /// <summary>
        /// The test specific exception that is intended to be distinguished from the exceptions the framework
        /// might throw.
        /// </summary>
        private class EUTestException : Exception
        {
        }

        private class SpyConnector : Connector, TestOp
        {
            #region Member variables
            private static StackTrace _stackTrace;
            #endregion

            #region Properties
            /// <summary>
            /// Gets the stack trace of the last call to the <see cref="M:SpyConnector.Test"/> method performed on
            /// any instance of this class.
            /// </summary>
            public static StackTrace StackTrace
            {
                get
                {
                    return _stackTrace;
                }
            }
            #endregion

            #region Connector Members
            public void Init(Configuration configuration)
            {
            }
            #endregion

            #region IDisposable Members
            public void Dispose()
            {
            }
            #endregion

            #region TestOp Members
            public void Test()
            {
                //do not insert file info, as the stack trace of the exception and the dumped stack trace
                //will always differ in the line numbers
                _stackTrace = new StackTrace(false);
                throw new EUTestException();
            }
            #endregion
        }

        private class MockConfiguration : AbstractConfiguration
        {
            public override void Validate()
            {
            }
        }

        /// <summary>
        /// Tests whether the <see cref="ConnectorFacade"/> implementation let the exception - thrown by a connector -
        /// propagate to the caller with the call stack representation from the method that throws the exception.
        /// </summary>
        /// <remarks>The current <see cref="ConnectorFacade"/> implementation uses reflection that can hide the original
        /// exception. See <see cref="M:ExceptionUtil.PreserveStackTrace"/> for more information.</remarks>
        [Test]
        public void TestStackTraceOfExceptionThrownByConnectorFacade()
        {
            ConnectorFacadeFactory factory = ConnectorFacadeFactory.GetInstance();
            Configuration config = new MockConfiguration();
            ConnectorFacade facade = factory.NewInstance(
                TestHelpers.CreateTestConfiguration(SafeType<Connector>.Get<SpyConnector>(), config));

            try
            {
                facade.Test();

                Assert.Fail("Exception was not thrown");
            }
            catch (EUTestException eutex)
            {
                ExceptionUtilTestHelpers.AssertStackTrace(eutex, SpyConnector.StackTrace);
            }
        }
    }
}
