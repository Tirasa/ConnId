/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Tirasa. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://github.com/Tirasa/ConnId/blob/master/legal/license.txt
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at https://github.com/Tirasa/ConnId/blob/master/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.common;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class PrettyStringBuilder {

    private final int maxArrayLen;

    private final int maxDeep;

    private final String nullValue;

    private int deep;

    public PrettyStringBuilder() {
        this.maxArrayLen = 10;
        this.maxDeep = 3;
        this.nullValue = "<null>";
    }

    public PrettyStringBuilder(final int maxArrayLen, final int maxDeep, final String nullValue) {
        this.maxArrayLen = maxArrayLen;
        this.maxDeep = maxDeep;
        this.nullValue = nullValue;
    }

    /**
     * Returns pretty value from object value.
     */
    protected String toPrettyString(final Object obj) {
        deep++;
        if (obj == null) {
            deep--;
            return nullValue;
        }
        if (deep == maxDeep) {
            deep--;
            return obj.toString();
        }
        final StringBuilder s = new StringBuilder();
        final Class c = obj.getClass();
        if (c.isArray()) {
            final int arrayLen = Array.getLength(obj);
            final int len = Math.min(arrayLen, maxArrayLen);
            s.append('[');
            for (int i = 0; i < len; i++) {
                s.append(toPrettyString(Array.get(obj, i)));
                if (i != len - 1) {
                    s.append(',');
                }
            }
            if (len < arrayLen) {
                s.append("...");
            }
            s.append(']');
        } else if (obj instanceof Collection) {
            final Collection coll = (Collection) obj;
            final Iterator it = coll.iterator();
            int i = 0;
            s.append('(');
            while ((it.hasNext() && (i < maxArrayLen))) {
                s.append(toPrettyString(it.next()));
                i++;
            }
            if (i < coll.size()) {
                s.append("...");
            }
            s.append(')');
        } else if (obj instanceof Map) {
            final Map map = (Map) obj;
            final Iterator it = map.keySet().iterator();
            int i = 0;
            s.append('{');
            while ((it.hasNext() && (i < maxArrayLen))) {
                final Object key = it.next();
                s.append(key).append(':');
                s.append(toPrettyString(map.get(key)));
                i++;
            }
            if (i < map.size()) {
                s.append("...");
            }
            s.append('}');
        } else {
            s.append(obj.toString());
        }
        deep--;
        return s.toString();
    }

    /**
     * Returns pretty string representation of the object.
     */
    public String toString(final Object value) {
        return toPrettyString(value);
    }
}
