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
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Framework.Api;
using Org.IdentityConnectors.Framework.Api.Operations;
namespace Org.IdentityConnectors.Framework.Impl.Api.Remote.Messages
{
    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class HelloRequest : Message
    {

        public HelloRequest()
        {
        }
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class HelloResponse : Message
    {
        /// <summary>
        /// The exception
        /// </summary>
        private Exception _exception;

        /// <summary>
        /// List of connector infos, containing infos for all the connectors
        /// on the server.
        /// </summary>
        private IList<RemoteConnectorInfoImpl> _connectorInfos;

        public HelloResponse(Exception exception,
                IList<RemoteConnectorInfoImpl> connectorInfos)
        {
            _exception = exception;
            _connectorInfos = CollectionUtil.NewReadOnlyList<RemoteConnectorInfoImpl>(connectorInfos);
        }

        public Exception Exception
        {
            get
            {
                return _exception;
            }
        }

        public IList<RemoteConnectorInfoImpl> ConnectorInfos
        {
            get
            {
                return _connectorInfos;
            }
        }
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public interface Message
    {
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class OperationRequest : Message
    {
        /// <summary>
        /// The key of the connector to operate on.
        /// </summary>
        private readonly ConnectorKey _connectorKey;

        /// <summary>
        /// The configuration information to use.
        /// </summary>
        private readonly APIConfigurationImpl _configuration;

        /// <summary>
        /// The operation to perform.
        /// </summary>
        private readonly SafeType<APIOperation> _operation;

        /// <summary>
        /// The name of the method since operations can have more
        /// than one method.
        /// </summary>
        /// <remarks>
        /// NOTE: this is case-insensitive
        /// </remarks>
        private readonly String _operationMethodName;

        /// <summary>
        /// The arguments to the operation.
        /// </summary>
        /// <remarks>
        /// In general, these correspond
        /// to the actual arguments of the method. The one exception is
        /// search - in this case, the callback is not passed.
        /// </remarks>
        private readonly IList<object> _arguments;

        public OperationRequest(ConnectorKey key,
                APIConfigurationImpl apiConfiguration,
                SafeType<APIOperation> operation,
                string operationMethodName,
                IList<Object> arguments)
        {
            _connectorKey = key;
            _configuration = apiConfiguration;
            _operation = operation;
            _operationMethodName = operationMethodName;
            _arguments = CollectionUtil.NewReadOnlyList<object>(arguments);
        }

        public ConnectorKey ConnectorKey
        {
            get
            {
                return _connectorKey;
            }
        }

        public APIConfigurationImpl Configuration
        {
            get
            {
                return _configuration;
            }
        }

        public SafeType<APIOperation> Operation
        {
            get
            {
                return _operation;
            }
        }

        public string OperationMethodName
        {
            get
            {
                return _operationMethodName;
            }
        }

        public IList<Object> Arguments
        {
            get
            {
                return _arguments;
            }
        }
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class OperationRequestMoreData : Message
    {
        public OperationRequestMoreData()
        {
        }
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class OperationRequestStopData : Message
    {
        public OperationRequestStopData()
        {
        }
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class OperationResponseEnd : Message
    {
        public OperationResponseEnd()
        {
        }
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class OperationResponsePart : Message
    {
        private Exception _exception;
        private Object _result;

        public OperationResponsePart(Exception ex, Object result)
        {
            _exception = ex;
            _result = result;
        }

        public Exception Exception
        {
            get
            {
                return _exception;
            }
        }

        public Object Result
        {
            get
            {
                return _result;
            }
        }
    }

    /// <summary>
    /// internal class, public only for unit tests
    /// </summary>
    public class OperationResponsePause : Message
    {
        public OperationResponsePause()
        {
        }
    }

    public class EchoMessage : Message
    {
        private object _object;
        private string _objectXml;
        public EchoMessage(object obj, string xml)
        {
            _object = obj;
            _objectXml = xml;
        }
        public object Object
        {
            get
            {
                return _object;
            }
        }
        public string ObjectXml
        {
            get
            {
                return _objectXml;
            }
        }
    }
}