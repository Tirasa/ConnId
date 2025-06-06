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
package org.identityconnectors.test.common;

import java.util.ArrayList;
import java.util.List;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;

/**
 * A {@link ResultsHandler} which stores all connector objects into a list
 * retrievable with {@link #getObjects}.
 */
public final class ToListResultsHandler implements ResultsHandler {

    private final List<ConnectorObject> connectorObjects = new ArrayList<ConnectorObject>();

    public boolean handle(ConnectorObject object) {
        connectorObjects.add(object);
        return true;
    }

    public List<ConnectorObject> getObjects() {
        return connectorObjects;
    }

}
