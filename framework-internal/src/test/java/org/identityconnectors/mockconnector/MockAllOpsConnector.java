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
package org.identityconnectors.mockconnector;

import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.operations.AuthenticateOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.ResolveUsernameOp;
import org.identityconnectors.framework.spi.operations.ScriptOnConnectorOp;
import org.identityconnectors.framework.spi.operations.ScriptOnResourceOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

public class MockAllOpsConnector extends MockConnector implements CreateOp,
        DeleteOp, UpdateOp, SearchOp<String>, UpdateAttributeValuesOp, AuthenticateOp,
        ResolveUsernameOp, TestOp, ScriptOnConnectorOp, ScriptOnResourceOp {

    @Override
    public Object runScriptOnConnector(ScriptContext request,
            OperationOptions options) {
        assert request != null;
        assert options != null;
        addCall(request, options);
        return null;
    }

    @Override
    public Object runScriptOnResource(ScriptContext request,
            OperationOptions options) {
        assert request != null;
        assert options != null;
        addCall(request, options);
        return null;
    }

    @Override
    public Uid create(final ObjectClass oclass, final Set<Attribute> attrs,
            OperationOptions options) {
        assert attrs != null;
        addCall(attrs);
        return null;
    }

    @Override
    public void delete(final ObjectClass objClass, final Uid uid,
            OperationOptions options) {
        assert uid != null && objClass != null;
        addCall(objClass, uid);
    }

    @Override
    public Uid update(ObjectClass objclass, Uid uid, Set<Attribute> attrs,
            OperationOptions options) {
        assert objclass != null && attrs != null;
        addCall(objclass, attrs);
        return null;
    }

    @Override
    public Uid addAttributeValues(ObjectClass objclass,
            Uid uid,
            Set<Attribute> valuesToAdd,
            OperationOptions options) {
        addCall(objclass, valuesToAdd);
        return null;
    }

    @Override
    public Uid removeAttributeValues(ObjectClass objclass,
            Uid uid,
            Set<Attribute> valuesToRemove,
            OperationOptions options) {
        addCall(objclass, valuesToRemove);
        return null;
    }

    @Override
    public FilterTranslator<String> createFilterTranslator(ObjectClass oclass,
            OperationOptions options) {
        assert oclass != null && options != null;
        addCall(oclass, options);
        // no translation - ok since this is just for tests
        return new AbstractFilterTranslator<String>() {
        };
    }

    @Override
    public void executeQuery(ObjectClass oclass, String query,
            ResultsHandler handler, OperationOptions options) {
        assert oclass != null && handler != null && options != null;
        addCall(oclass, query, handler, options);
    }

    @Override
    public Uid authenticate(ObjectClass objectClass, String username,
            GuardedString password, OperationOptions options) {
        assert username != null && password != null;
        addCall(username, password);
        return null;
    }

    @Override
    public Uid resolveUsername(ObjectClass objectClass, String username,
            OperationOptions options) {

        assert username != null;
        addCall(username);
        return null;
    }

    @Override
    public void test() {
        addCall();
    }
}
