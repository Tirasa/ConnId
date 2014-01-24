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
package org.identityconnectors.framework.common.objects;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.FrameworkUtil;

/**
 * Abstract "place-holder" for synchronization. The application must not make
 * any attempt to interpret the value of the token. From the standpoint of the
 * application the token is merely a black-box. The application may only persist
 * the value of the token for use on subsequent synchronization requests.
 * <p>
 * What this token represents is entirely connector-specific. On some connectors
 * this might be a last-modified value. On others, it might be a unique ID of a
 * log table entry. On others such as JMS, this might be a dummy value since JMS
 * itself keeps track of the state of the sync.
 */
public final class SyncToken {

    private Object value;

    /**
     * Creates a new
     *
     * @param value
     *            May not be null. TODO: define set of allowed value types
     *            (currently same as set of allowed attribute values).
     */
    public SyncToken(Object value) {
        Assertions.nullCheck(value, "value");
        FrameworkUtil.checkAttributeValue(value);
        this.value = value;
    }

    /**
     * Returns the value for the token.
     *
     * @return The value for the token.
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SyncToken: " + value.toString();
    }

    @Override
    public int hashCode() {
        return CollectionUtil.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SyncToken) {
            SyncToken other = (SyncToken) o;
            return CollectionUtil.equals(value, other.value);
        }
        return false;
    }

}
