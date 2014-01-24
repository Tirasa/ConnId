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
import org.identityconnectors.framework.common.FrameworkUtil;

public final class OperationOptionInfo {
    private final String name;
    private final Class<?> type;

    public OperationOptionInfo(String name, Class<?> type) {
        Assertions.nullCheck(name, "name");
        Assertions.nullCheck(type, "type");
        FrameworkUtil.checkOperationOptionType(type);
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof OperationOptionInfo) {
            OperationOptionInfo other = (OperationOptionInfo) o;
            if (!name.equals(other.name)) {
                return false;
            }
            if (!type.equals(other.type)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OperationOptionInfo( ");
        builder.append(name);
        builder.append(type.toString());
        builder.append(") ");
        return builder.toString();
    }
}
