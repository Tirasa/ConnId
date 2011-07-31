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
package org.identityconnectors.framework.impl.serializer;

import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.ResolveUsernameApiOp;
import org.identityconnectors.framework.api.operations.SchemaApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.api.operations.ValidateApiOp;


class OperationMappings {
    
    public static final List<ObjectTypeMapper> MAPPINGS =
        new ArrayList<ObjectTypeMapper>();
    
    static {
        MAPPINGS.add(new ObjectTypeMapperImpl(AuthenticationApiOp.class,
                "AuthenticationApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(ResolveUsernameApiOp.class,
                "ResolveUsernameApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(SearchApiOp.class,
                "SearchApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(ValidateApiOp.class,
                "ValidateApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(CreateApiOp.class,
                "CreateApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(SchemaApiOp.class,
                "SchemaApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(UpdateApiOp.class,
                "UpdateApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(DeleteApiOp.class,
                "DeleteApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(GetApiOp.class,
                "GetApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(TestApiOp.class, 
                "TestApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(ScriptOnResourceApiOp.class, 
        "ScriptOnResourceApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(ScriptOnConnectorApiOp.class, 
        "ScriptOnConnectorApiOp"));
        MAPPINGS.add(new ObjectTypeMapperImpl(SyncApiOp.class, 
        "SyncApiOp"));
    }
}
