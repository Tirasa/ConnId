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
using NUnit.Framework;
using System.Collections.Generic;
using System.Diagnostics;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Server;
using Org.IdentityConnectors.Framework.Impl.Api;
using Org.IdentityConnectors.Framework.Impl.Api.Local;
using System.Security;
using System.Threading;
using System.Globalization;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
namespace FrameworkTests
{
    [TestFixture]
    public class ConnectorInfoManagerTests
    {
        private static ConnectorInfo FindConnectorInfo
            (ConnectorInfoManager manager,
             string version,
             string connectorName)
        {
            foreach (ConnectorInfo info in manager.ConnectorInfos)
            {
                ConnectorKey key = info.ConnectorKey;
                if (version.Equals(key.BundleVersion) &&
                    connectorName.Equals(key.ConnectorName))
                {
                    //intentionally ineffecient to test
                    //more code
                    return manager.FindConnectorInfo(key);
                }
            }
            return null;
        }

        [TearDown]
        public void TearDown()
        {
            ShutdownConnnectorInfoManager();
        }

        [Test]
        public void TestClassLoading()
        {
            ConnectorInfoManager manager =
                GetConnectorInfoManager();
            ConnectorInfo info1 =
               FindConnectorInfo(manager,
                 "1.0.0.0",
                 "org.identityconnectors.testconnector.TstConnector");
            Assert.IsNotNull(info1);
            ConnectorInfo info2 =
                FindConnectorInfo(manager,
                 "2.0.0.0",
                 "org.identityconnectors.testconnector.TstConnector");

            Assert.IsNotNull(info2);

            ConnectorFacade facade1 =
                ConnectorFacadeFactory.GetInstance().NewInstance(info1.CreateDefaultAPIConfiguration());

            ConnectorFacade facade2 =
                ConnectorFacadeFactory.GetInstance().NewInstance(info2.CreateDefaultAPIConfiguration());

            ICollection<ConnectorAttribute> attrs = new HashSet<ConnectorAttribute>();
            Assert.AreEqual("1.0", facade1.Create(ObjectClass.ACCOUNT, attrs, null).GetUidValue());
            Assert.AreEqual("2.0", facade2.Create(ObjectClass.ACCOUNT, attrs, null).GetUidValue());
        }

        [Test]
        public void TestAPIConfiguration()
        {
            ConnectorInfoManager manager =
                GetConnectorInfoManager();
            ConnectorInfo info =
                 FindConnectorInfo(manager,
                 "1.0.0.0",
                 "org.identityconnectors.testconnector.TstConnector");
            Assert.IsNotNull(info);
            APIConfiguration api = info.CreateDefaultAPIConfiguration();

            ConfigurationProperties props = api.ConfigurationProperties;
            ConfigurationProperty property = props.GetProperty("tstField");

            Assert.IsNotNull(property);
            ICollection<SafeType<APIOperation>> operations =
            property.Operations;
            Assert.AreEqual(1, operations.Count);
            Assert.IsTrue(operations.Contains(SafeType<APIOperation>.Get<SyncApiOp>()));

            Thread.CurrentThread.CurrentUICulture = new CultureInfo("en");
            Assert.AreEqual("Help for test field.", property.GetHelpMessage(null));
            Assert.AreEqual("Display for test field.", property.GetDisplayName(null));
            Assert.AreEqual("Test Framework Value",
                info.Messages.Format("TEST_FRAMEWORK_KEY", "empty"));

            CultureInfo eslocale = new CultureInfo("es");
            Thread.CurrentThread.CurrentUICulture = eslocale;
            Assert.AreEqual("tstField.help_es", property.GetHelpMessage(null));
            Assert.AreEqual("tstField.display_es", property.GetDisplayName(null));

            CultureInfo esESlocale = new CultureInfo("es-ES");
            Thread.CurrentThread.CurrentUICulture = esESlocale;
            Assert.AreEqual("tstField.help_es-ES", property.GetHelpMessage(null));
            Assert.AreEqual("tstField.display_es-ES", property.GetDisplayName(null));

            CultureInfo esARlocale = new CultureInfo("es-AR");
            Thread.CurrentThread.CurrentUICulture = esARlocale;
            Assert.AreEqual("tstField.help_es", property.GetHelpMessage(null));
            Assert.AreEqual("tstField.display_es", property.GetDisplayName(null));

            ConnectorFacadeFactory facf = ConnectorFacadeFactory.GetInstance();
            ConnectorFacade facade = facf.NewInstance(api);
            // call the various create/update/delete commands..
            facade.Schema();
        }

        [Test]
        public void TestValidate()
        {
            ConnectorInfoManager manager =
                GetConnectorInfoManager();
            ConnectorInfo info =
                FindConnectorInfo(manager,
                        "1.0.0.0",
                        "org.identityconnectors.testconnector.TstConnector");

            APIConfiguration api = info.CreateDefaultAPIConfiguration();

            ConfigurationProperties props = api.ConfigurationProperties;
            ConfigurationProperty property = props.GetProperty("failValidation");
            property.Value = false;
            ConnectorFacadeFactory facf = ConnectorFacadeFactory.GetInstance();
            ConnectorFacade facade = facf.NewInstance(api);
            facade.Validate();
            property.Value = true;
            facade = facf.NewInstance(api);
            try
            {
                Thread.CurrentThread.CurrentUICulture = new CultureInfo("en");
                facade.Validate();
                Assert.Fail("exception expected");
            }
            catch (ConnectorException e)
            {
                Assert.AreEqual("validation failed en", e.Message);
            }
            try
            {
                Thread.CurrentThread.CurrentUICulture = new CultureInfo("es");
                facade.Validate();
                Assert.Fail("exception expected");
            }
            catch (ConnectorException e)
            {
                Assert.AreEqual("validation failed es", e.Message);
            }
        }

        /// <summary>
        /// Main purpose of this is to test searching with
        /// many results and that we can properly handle
        /// stopping in the middle of this.
        /// </summary>
        /// <remarks>
        /// There's a bunch of
        /// code in the remote stuff that is there to handle this
        /// in particular that we want to excercise.
        /// </remarks>
        [Test]
        public void TestSearchWithManyResults()
        {
            ConnectorInfoManager manager =
                GetConnectorInfoManager();
            ConnectorInfo info =
                 FindConnectorInfo(manager,
                 "1.0.0.0",
                 "org.identityconnectors.testconnector.TstConnector");

            APIConfiguration api = info.CreateDefaultAPIConfiguration();

            ConfigurationProperties props = api.ConfigurationProperties;
            ConfigurationProperty property = props.GetProperty("numResults");

            //1000 is several times the remote size between pauses
            property.Value = 1000;

            ConnectorFacadeFactory facf = ConnectorFacadeFactory.GetInstance();
            ConnectorFacade facade = facf.NewInstance(api);

            IList<ConnectorObject> results = new List<ConnectorObject>();

            facade.Search(ObjectClass.ACCOUNT, null,
                          obj =>
                          {
                              results.Add(obj);
                              return true;
                          }, null);

            Assert.AreEqual(1000, results.Count);
            for (int i = 0; i < results.Count; i++)
            {
                ConnectorObject obj = results[i];
                Assert.AreEqual(i.ToString(),
                        obj.Uid.GetUidValue());
            }

            results.Clear();

            facade.Search(ObjectClass.ACCOUNT, null,
                          obj =>
                          {
                              if (results.Count < 500)
                              {
                                  results.Add(obj);
                                  return true;
                              }
                              else
                              {
                                  return false;
                              }
                          }, null);

            Assert.AreEqual(500, results.Count);
            for (int i = 0; i < results.Count; i++)
            {
                ConnectorObject obj = results[i];
                Assert.AreEqual(i.ToString(),
                        obj.Uid.GetUidValue());
            }
        }
        /// <summary>
        /// Main purpose of this is to test sync with
        /// many results and that we can properly handle
        /// stopping in the middle of this.
        /// </summary>
        /// <remarks>
        /// There's a bunch of
        /// code in the remote stuff that is there to handle this
        /// in particular that we want to excercise.
        /// </remarks>
        [Test]
        public void TestSyncWithManyResults()
        {
            ConnectorInfoManager manager =
                GetConnectorInfoManager();
            ConnectorInfo info =
                FindConnectorInfo(manager,
                        "1.0.0.0",
                        "org.identityconnectors.testconnector.TstConnector");

            APIConfiguration api = info.CreateDefaultAPIConfiguration();

            ConfigurationProperties props = api.ConfigurationProperties;
            ConfigurationProperty property = props.GetProperty("numResults");

            //1000 is several times the remote size between pauses
            property.Value = (1000);

            ConnectorFacadeFactory facf = ConnectorFacadeFactory.GetInstance();
            ConnectorFacade facade = facf.NewInstance(api);

            SyncToken latest = facade.GetLatestSyncToken(ObjectClass.ACCOUNT);
            Assert.AreEqual("mylatest", latest.Value);
            IList<SyncDelta> results = new List<SyncDelta>();

            facade.Sync(ObjectClass.ACCOUNT, null, obj =>
            {
                results.Add(obj);
                return true;
            }, null);

            Assert.AreEqual(1000, results.Count);
            for (int i = 0; i < results.Count; i++)
            {
                SyncDelta obj = results[i];
                Assert.AreEqual(i.ToString(),
                        obj.Uid.GetUidValue());
            }

            results.Clear();

            facade.Sync(ObjectClass.ACCOUNT,
                    null,
                    obj =>
                    {
                        if (results.Count < 500)
                        {
                            results.Add(obj);
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }, null);

            Assert.AreEqual(500, results.Count);
            for (int i = 0; i < results.Count; i++)
            {
                SyncDelta obj = results[i];
                Assert.AreEqual(i.ToString(),
                        obj.Uid.GetUidValue());
            }
        }

        [Test]
        public void TestConnectionPooling()
        {
            ConnectorPoolManager.Dispose();
            ConnectorInfoManager manager =
                GetConnectorInfoManager();
            ConnectorInfo info1 =
                FindConnectorInfo(manager,
                                  "1.0.0.0",
                                  "org.identityconnectors.testconnector.TstConnector");
            Assert.IsNotNull(info1);
            //reset connection count
            {
                //trigger TstConnection.init to be called
                APIConfiguration config2 =
                    info1.CreateDefaultAPIConfiguration();
                config2.ConfigurationProperties.GetProperty("resetConnectionCount").Value = (true);
                ConnectorFacade facade2 =
                    ConnectorFacadeFactory.GetInstance().NewInstance(config2);
                facade2.Schema(); //force instantiation            
            }

            APIConfiguration config =
                info1.CreateDefaultAPIConfiguration();

            config.ConnectorPoolConfiguration.MinIdle = (0);
            config.ConnectorPoolConfiguration.MaxIdle = (0);

            ConnectorFacade facade1 =
                ConnectorFacadeFactory.GetInstance().NewInstance(config);

            OperationOptionsBuilder builder = new OperationOptionsBuilder();
            builder.SetOption("testPooling", "true");
            OperationOptions options = builder.Build();
            ICollection<ConnectorAttribute> attrs = CollectionUtil.NewReadOnlySet<ConnectorAttribute>();
            Assert.AreEqual("1", facade1.Create(ObjectClass.ACCOUNT, attrs, options).GetUidValue());
            Assert.AreEqual("2", facade1.Create(ObjectClass.ACCOUNT, attrs, options).GetUidValue());
            Assert.AreEqual("3", facade1.Create(ObjectClass.ACCOUNT, attrs, options).GetUidValue());
            Assert.AreEqual("4", facade1.Create(ObjectClass.ACCOUNT, attrs, options).GetUidValue());
            config =
                info1.CreateDefaultAPIConfiguration();
            config.ConnectorPoolConfiguration.MinIdle = (1);
            config.ConnectorPoolConfiguration.MaxIdle = (2);
            facade1 =
                ConnectorFacadeFactory.GetInstance().NewInstance(config);
            Assert.AreEqual("5", facade1.Create(ObjectClass.ACCOUNT, attrs, options).GetUidValue());
            Assert.AreEqual("5", facade1.Create(ObjectClass.ACCOUNT, attrs, options).GetUidValue());
            Assert.AreEqual("5", facade1.Create(ObjectClass.ACCOUNT, attrs, options).GetUidValue());
            Assert.AreEqual("5", facade1.Create(ObjectClass.ACCOUNT, attrs, options).GetUidValue());
        }

        [Test]
        public void TestScripting()
        {
            ConnectorInfoManager manager =
                GetConnectorInfoManager();
            ConnectorInfo info =
                FindConnectorInfo(manager,
                        "1.0.0.0",
                        "org.identityconnectors.testconnector.TstConnector");
            APIConfiguration api = info.CreateDefaultAPIConfiguration();


            ConnectorFacadeFactory facf = ConnectorFacadeFactory.GetInstance();
            ConnectorFacade facade = facf.NewInstance(api);

            ScriptContextBuilder builder = new ScriptContextBuilder();
            builder.AddScriptArgument("arg1", "value1");
            builder.AddScriptArgument("arg2", "value2");
            builder.ScriptLanguage = ("BOO");

            //test that they can run the script and access the
            //connector object
            {
                String SCRIPT =
                    "connector.concat(arg1,arg2)";
                builder.ScriptText = (SCRIPT);
                String result = (String)facade.RunScriptOnConnector(builder.Build(),
                        null);

                Assert.AreEqual("value1value2", result);
            }

            //test that they can access a class in the class loader
            {
                String SCRIPT =
                    "import org.identityconnectors.testconnector\n" +
                    "TstConnector.getVersion()";
                builder.ScriptText = (SCRIPT);
                String result = (String)facade.RunScriptOnConnector(builder.Build(),
                        null);
                Assert.AreEqual("1.0", result);
            }

            //test that they cannot access a class in internal
            {
                Type clazz = typeof(ConfigurationPropertyImpl);

                String SCRIPT =
                    "import " + clazz.Namespace + "\n" +
                    clazz.Name + "()";
                builder.ScriptText = (SCRIPT);
                try
                {
                    facade.RunScriptOnConnector(builder.Build(),
                            null);
                    Assert.Fail("exception expected");
                }
                catch (Exception e)
                {
                    String msg = e.Message;
                    String expectedMessage =
                        "Namespace '" + clazz.Namespace + "' not found";
                    Assert.IsTrue(
                            msg.Contains(expectedMessage),
                            "Unexpected message: " + msg);
                }
            }

            // test that they can access a class in common
            {
                Type clazz = typeof(ConnectorAttributeBuilder);
                String SCRIPT =
                    "import " + clazz.Namespace + "\n" +
                    clazz.Name + ".Build(\"myattr\")";
                builder.ScriptText = (SCRIPT);
                ConnectorAttribute attr = (ConnectorAttribute)facade.RunScriptOnConnector(builder.Build(), null);
                Assert.AreEqual("myattr", attr.Name);
            }
        }

        protected virtual ConnectorInfoManager GetConnectorInfoManager()
        {
            ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.GetInstance();
            ConnectorInfoManager manager = fact.GetLocalManager();
            return manager;
        }

        protected virtual void ShutdownConnnectorInfoManager()
        {
            ConnectorFacadeFactory.GetInstance().Dispose();
        }
    }

    [TestFixture]
    public class RemoteConnectorInfoManagerClearTests : ConnectorInfoManagerTests
    {

        private ConnectorServer _server;

        protected override ConnectorInfoManager GetConnectorInfoManager()
        {
            TestUtil.InitializeLogging();

            GuardedString str = new GuardedString();
            str.AppendChar('c');
            str.AppendChar('h');
            str.AppendChar('a');
            str.AppendChar('n');
            str.AppendChar('g');
            str.AppendChar('e');
            str.AppendChar('i');
            str.AppendChar('t');

#if DEBUG
            const int PORT = 58758;
#else
            const int PORT = 58759;
#endif
            _server = ConnectorServer.NewInstance();
            _server.Port = PORT;
            _server.IfAddress = (IOUtil.GetIPAddress("127.0.0.1"));
            _server.KeyHash = str.GetBase64SHA1Hash();
            _server.Start();
            //while ( true ) {
            //    Thread.Sleep(1000);
            //}
            ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.GetInstance();

            RemoteFrameworkConnectionInfo connInfo = new
            RemoteFrameworkConnectionInfo("127.0.0.1", PORT, str);

            ConnectorInfoManager manager = fact.GetRemoteManager(connInfo);

            return manager;
        }

        protected override void ShutdownConnnectorInfoManager()
        {
            if (_server != null)
            {
                _server.Stop();
                _server = null;
            }
        }
    }

    internal class MyCertificateValidationCallback
    {
        public bool Validate(Object sender,
                     X509Certificate certificate,
                     X509Chain chain,
                     SslPolicyErrors sslPolicyErrors)
        {
            Trace.TraceInformation("validating: " + certificate.Subject);
            return true;
        }
    }

    [TestFixture]
    public class RemoteConnectorInfoManagerSSLTests : ConnectorInfoManagerTests
    {

        //To generate test certificate do the following:
        //
        //makecert -r -pe -n "CN=localhost" -ss TestCertificateStore -sr currentuser -sky exchange 
        //
        //In MMC, go to the certificate, export
        private ConnectorServer _server;
        private const String CERT_PATH = "../../../server.pfx";
        protected override ConnectorInfoManager GetConnectorInfoManager()
        {
            TestUtil.InitializeLogging();

            GuardedString str = new GuardedString();
            str.AppendChar('c');
            str.AppendChar('h');
            str.AppendChar('a');
            str.AppendChar('n');
            str.AppendChar('g');
            str.AppendChar('e');
            str.AppendChar('i');
            str.AppendChar('t');

#if DEBUG
            const int PORT = 58762;
#else
            const int PORT = 58761;
#endif

            /*X509Store store = new X509Store("TestCertificateStore",
                                            StoreLocation.CurrentUser);
            store.Open(OpenFlags.ReadOnly|OpenFlags.OpenExistingOnly);
            X509Certificate certificate = store.Certificates[0];
            store.Close();*/

            X509Certificate2 certificate = new
                X509Certificate2(CERT_PATH,
                                 "changeit");
            //Trace.TraceInformation("certificate: "+certificate);
            _server = ConnectorServer.NewInstance();
            _server.Port = PORT;
            _server.KeyHash = str.GetBase64SHA1Hash();
            _server.IfAddress = (IOUtil.GetIPAddress("localhost"));
            _server.UseSSL = true;
            _server.ServerCertificate = certificate;
            _server.Start();
            //while ( true ) {
            //    Thread.Sleep(1000);
            //}
            ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.GetInstance();
            MyCertificateValidationCallback
                callback = new MyCertificateValidationCallback();
            RemoteFrameworkConnectionInfo connInfo = new
                RemoteFrameworkConnectionInfo("localhost",
                                              PORT,
                                              str,
                                              true,
                                              callback.Validate,
                                              60000);

            ConnectorInfoManager manager = fact.GetRemoteManager(connInfo);

            return manager;
        }

        protected override void ShutdownConnnectorInfoManager()
        {
            if (_server != null)
            {
                _server.Stop();
                _server = null;
            }
        }
    }
}