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

import java.util.Objects;

/**
 * Reference to a connector object.
 *
 * It may contain the identifier/identifiers only (e.g. {@link Uid}, {@link Name}, or other attributes), or it may
 * contain the whole object, fetched partially or fully.
 *
 * Typical use case:
 *
 * A user in Active Directory has a multivalued reference attribute {@code group} that points to groups the user is a
 * member of.
 * The attribute is similar to {@code memberOf} attribute, but instead of holding string DNs of the groups, it holds
 * these references as {@link ConnectorObjectReference}s (which may contain DNs, UUIDs, or even partially or fully
 * fetched group objects, depending on the situation).
 *
 * Other use cases:
 *
 * Until ConnId supports structured (complex) attributes, the reference attributes provide an alternative solution.
 * The complex values of such attributes are represented by embedded connector objects.
 *
 * For example, HR system can model contracts of a person as a multivalued reference attribute {@code contract} on
 * object class {@code person} that contains objects of the object class {@code contract}. These objects are provided
 * by value, i.e., being embedded right in the references.
 */
public class ConnectorObjectReference {

    /**
     * The referenced object or its identification.
     */
    private final BaseConnectorObject value;

    public ConnectorObjectReference(final BaseConnectorObject value) {
        if (!(value instanceof ConnectorObject) && !(value instanceof ConnectorObjectIdentification)) {
            throw new IllegalArgumentException(
                    "Referenced object must be either ConnectorObject or ConnectorObjectIdentification");
        }
        this.value = value;
    }

    /**
     * True if the object is present. False if only the identifiers are.
     */
    public boolean hasObject() {
        return value instanceof ConnectorObject;
    }

    /**
     * Returns the value of the reference (the referenced object or its identification)
     */
    public BaseConnectorObject getValue() {
        return Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + value;
    }
}
