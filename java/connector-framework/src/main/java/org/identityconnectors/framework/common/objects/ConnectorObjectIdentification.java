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
 */
package org.identityconnectors.framework.common.objects;

import java.util.Set;

/**
 * Represents an identification of a object (e.g., an Account or a Group) on the target resource.
 * Contains one or more attributes that should identify the object. (E.g. {@link Name} but not necessarily that one.)
 *
 * The developer of a Connector will use a {@link ConnectorObjectBuilder} to 
 * construct instances of ConnectorObjectIdentification.
 *
 * Similar to {@link ConnectorObject} but with no requirements about object class information
 * and the presence of {@link Uid} and {@link Name} attributes.
 */
public final class ConnectorObjectIdentification extends BaseConnectorObject {

    /**
     * Public only for serialization; please use {@link ConnectorObjectBuilder}.
     */
    public ConnectorObjectIdentification(final ObjectClass objectClass, final Set<? extends Attribute> attributes) {
        super(objectClass, attributes);
    }

    @Override
    public ConnectorObjectIdentification getIdentification() {
        return this;
    }
}
