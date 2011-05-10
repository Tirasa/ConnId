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
using System.Collections.Specialized;
using System.ComponentModel;
using System.Configuration;
using System.Data;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Security.Cryptography.X509Certificates;
using System.ServiceProcess;
using System.Text;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Server;

namespace Org.IdentityConnectors.Framework.Service
{
	public class Service : ServiceBase
	{		
        private const string PROP_PORT = "connectorserver.port";
        private const string PROP_SSL  = "connectorserver.usessl";
        private const string PROP_CERTSTORE = "connectorserver.certificatestorename";
        private const string PROP_IFADDRESS = "connectorserver.ifaddress";
        public const string PROP_KEY = "connectorserver.key";
	    
	    private ConnectorServer _server;
	    
		public Service()
		{
		}
		
		public void DumpRequests()
		{
		    _server.DumpRequests();
		}
		
		
		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose(bool disposing)
		{
			base.Dispose(disposing);
		}
		
		private void initializeCurrentDirectory()
		{
	        Assembly assembly = 
	            Assembly.GetExecutingAssembly();
            FileInfo thisAssemblyFile =
               new FileInfo(assembly.Location);
            DirectoryInfo directory =
               thisAssemblyFile.Directory;
	        Environment.CurrentDirectory =
	            directory.FullName;
		    
		}
		
		private NameValueCollection GetApplicationSettings()
		{
		    return ConfigurationManager.AppSettings;
		}
		
		private X509Certificate GetCertificate()
		{
		    NameValueCollection settings =
		        GetApplicationSettings();
		    String storeName = settings.Get(PROP_CERTSTORE);
		    if (storeName == null) {
		        throw new Org.IdentityConnectors.Framework.Common.Exceptions.ConfigurationException("Missing required configuration setting: "+PROP_CERTSTORE);
		    }
		        
    		X509Store store = new X509Store(storeName,
                                           StoreLocation.LocalMachine);
		   
            store.Open(OpenFlags.ReadOnly|OpenFlags.OpenExistingOnly);
            X509CertificateCollection certificates = store.Certificates;
            if ( certificates.Count != 1 ) {
		        throw new Org.IdentityConnectors.Framework.Common.Exceptions.ConfigurationException("There is supported to be exactly one certificate in the store: "+storeName);                
            }
            X509Certificate certificate = store.Certificates[0];
            store.Close();
            return certificate;
		}
		
		public void StartService(string [] args)
		{
		    OnStart(args);
		}
		
		/// <summary>
		/// Start this service.
		/// </summary>
		protected override void OnStart(string[] args)
		{
		    try {		        
		        initializeCurrentDirectory();
    		    Trace.TraceInformation("Starting connector server: "+Environment.CurrentDirectory);
    		    NameValueCollection settings =
    		        GetApplicationSettings();
    		    String portStr =
    		        settings.Get(PROP_PORT);
    		    if ( portStr == null ) {
    		        throw new Org.IdentityConnectors.Framework.Common.Exceptions.ConfigurationException("Missing required configuration property: "+PROP_PORT);
    		    }
    		    String keyHash = settings.Get(PROP_KEY);
    		    if ( keyHash == null ) {
    		        throw new Org.IdentityConnectors.Framework.Common.Exceptions.ConfigurationException("Missing required configuration property: "+PROP_KEY);
    		    }
    		    
    		    int port = Int32.Parse(portStr);
    		    bool useSSL = Boolean.Parse(settings.Get(PROP_SSL)??"false");
    		    _server = ConnectorServer.NewInstance();    		        
    		    _server.Port = port;
    		    _server.UseSSL = useSSL;
    		    _server.KeyHash = keyHash;
    		    if (useSSL) {
    		        _server.ServerCertificate =
    		            GetCertificate();
    		    }
    		    String ifaddress = settings.Get(PROP_IFADDRESS);
    		    if ( ifaddress != null ) {
    		        _server.IfAddress =
    		            IOUtil.GetIPAddress(ifaddress);
    		    }
    		    _server.Start();
    		    Trace.TraceInformation("Started connector server");
		    }
		    catch (Exception e) {
		        TraceUtil.TraceException("Exception occured starting connector server",
		                                 e);
		        throw;
		    }
		}
		
		public void StopService()
		{
		    OnStop();
		}
		
		
		/// <summary>
		/// Stop this service.
		/// </summary>
		protected override void OnStop()
		{
		    try {
    		    Trace.TraceInformation("Stopping connector server");
    		    if (_server != null) {
    		        _server.Stop();
    		    }
    		    Trace.TraceInformation("Stopped connector server");
		    }
		    catch (Exception e) {
		        TraceUtil.TraceException("Exception occured stopping connector server",
		                                 e);
		    }
		}
	}
}
