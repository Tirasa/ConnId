/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 ConnId. All rights reserved.
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

import org.identityconnectors.common.StringUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.identityconnectors.framework.common.objects.NameUtil.nameHashCode;
import static org.identityconnectors.framework.common.objects.NameUtil.namesEqual;

public abstract class BaseAttributeDelta {
    /**
     * Name of the attribute
     */
    private final String name;


    public BaseAttributeDelta(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Name must not be blank!");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean is(String name) {
        return namesEqual(this.name, name);
    }

    @Override
    public int hashCode() {
        return nameHashCode(name);
    }


    @Override
    public String toString() {
        // poor man's consistent toString impl..
        StringBuilder bld = new StringBuilder();
        bld.append("Attribute: ");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("Name", getName());
        extendToStringMap(map);
        bld.append(map);
        return bld.toString();
    }

    protected void extendToStringMap(Map<String, Object> map) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseAttributeDelta other = (BaseAttributeDelta) obj;
        return is(other.name);
    }

    public abstract Attribute applyTo(Attribute attr);
}
