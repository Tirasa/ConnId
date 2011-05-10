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
using System.Globalization;
using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
namespace org.identityconnectors.testconnector
{
    public class TstConnectorConfig : AbstractConfiguration
    {
        public override void Validate()
        {
        }
    }

    [ConnectorClass("TestConnector",
                      typeof(TstConnectorConfig),
                      MessageCatalogPaths = new String[] { "TestBundleV2.Messages" }
                        )]
    public class TstConnector : CreateOp, Connector, SchemaOp
    {
        public Uid Create(ObjectClass oclass, ICollection<ConnectorAttribute> attrs, OperationOptions options)
        {
            String version = "2.0";
            return new Uid(version);
        }
        public void Init(Configuration cfg)
        {
        }

        public void Dispose()
        {

        }

        public Schema Schema()
        {
            return null;
        }
    }
}
