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

public class MockAllOpsConnector extends MockConnector implements CreateOp, DeleteOp, UpdateOp,
        SearchOp<String>, UpdateAttributeValuesOp, AuthenticateOp, ResolveUsernameOp, TestOp,
        ScriptOnConnectorOp, ScriptOnResourceOp {

    public Object runScriptOnConnector(ScriptContext request, OperationOptions options) {
        assert request != null;
        assert options != null;
        addCall(request, options);
        return null;
    }

    public Object runScriptOnResource(ScriptContext request, OperationOptions options) {
        assert request != null;
        assert options != null;
        addCall(request, options);
        return null;
    }

    public Uid create(final ObjectClass objectClass, final Set<Attribute> createAttributes,
            OperationOptions options) {
        assert createAttributes != null;
        addCall(createAttributes);
        return null;
    }

    public void delete(final ObjectClass objectClass, final Uid uid, OperationOptions options) {
        assert uid != null && objectClass != null;
        addCall(objectClass, uid);
    }

    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attrs,
            OperationOptions options) {
        assert objectClass != null && attrs != null;
        addCall(objectClass, attrs);
        return null;
    }

    public Uid addAttributeValues(ObjectClass objclass, Uid uid, Set<Attribute> valuesToAdd,
            OperationOptions options) {
        addCall(objclass, valuesToAdd);
        return null;
    }

    public Uid removeAttributeValues(ObjectClass objclass, Uid uid, Set<Attribute> valuesToRemove,
            OperationOptions options) {
        addCall(objclass, valuesToRemove);
        return null;
    }

    public FilterTranslator<String> createFilterTranslator(ObjectClass objectClass,
            OperationOptions options) {
        assert objectClass != null && options != null;
        addCall(objectClass, options);
        // no translation - ok since this is just for tests
        return new AbstractFilterTranslator<String>() {
        };
    }

    public void executeQuery(ObjectClass objectClass, String query, ResultsHandler handler,
            OperationOptions options) {
        assert objectClass != null && handler != null && options != null;
        addCall(objectClass, query, handler, options);
    }

    public Uid authenticate(ObjectClass objectClass, String username, GuardedString password,
            OperationOptions options) {
        assert username != null && password != null;
        addCall(username, password);
        return null;
    }

    public Uid resolveUsername(ObjectClass objectClass, String username, OperationOptions options) {
        assert username != null;
        addCall(username);
        return null;
    }

    public void test() {
        addCall();
    }
}
