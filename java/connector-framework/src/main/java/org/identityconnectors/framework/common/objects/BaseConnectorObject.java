/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2024 Evolveum. All rights reserved.
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
 * Portions Copyrighted 2026 ConnId
 */
package org.identityconnectors.framework.common.objects;

import java.util.Set;

/**
 * Helps implementing {@link ConnectorObject} and {@link ConnectorObjectIdentification}.
 */
public abstract class BaseConnectorObject extends BaseObject {

    public BaseConnectorObject(ObjectClass objectClass, Set<? extends Attribute> attributes) {
        super(objectClass, attributes);
    }

    /**
     * Returns the identification of this object. For full objects it means providing {@link Name} and {@link Uid}
     * attributes.
     * (We have no other way of telling what attributes are the identifiers.)
     */
    public abstract ConnectorObjectIdentification getIdentification();
}
