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
 * Portions Copyrighted 2010-2014 ForgeRock AS.
 * Portions Copyrighted 2014 Evolveum
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.ArrayList;
import java.util.List;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.operations.SearchOp;

/**
 * Uses {@link SearchOp} to find the object that is referenced by the
 * {@link Uid} provided.
 */
public class GetImpl implements GetApiOp {

    final SearchApiOp op;

    public GetImpl(SearchApiOp search) {
        this.op = search;
    }

    @Override
    public ConnectorObject getObject(ObjectClass objectClass, Uid uid, OperationOptions options) {
        Assertions.nullCheck(objectClass, "objectClass");
        if (ObjectClass.ALL.equals(objectClass)) {
            throw new UnsupportedOperationException("Operation is not allowed on __ALL__ object class");
        }
        Assertions.nullCheck(uid, "uid");
        // cast null as empty
        if (options == null) {
            options = new OperationOptionsBuilder().build();
        }
        final List<ConnectorObject> list = new ArrayList<ConnectorObject>();
        // No need to log entry/exit here. SeachImpl will do it.
        op.search(objectClass, FilterBuilder.equalTo(uid), new ResultsHandler() {

            @Override
            public boolean handle(ConnectorObject obj) {
                list.add(obj);
                return false;
            }
        }, options);
        return list.isEmpty() ? null : list.get(0);
    }
}
