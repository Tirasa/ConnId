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
 * Portions Copyrighted 2011 ConnId.
 */
package net.tirasa.connid.commons.db;

import java.util.*;

/**
 * Resolver of properties in UNIX/ant style.
 * Example of usage :
 * <code>
 * Properties p = new Properties();
 * p.setProperty("p1","value1");
 * p.setProperty("p2","Value of p1 is ${p1}");
 * p = PropertiesResolver.resolveProperties(p);
 * </code>
 *
 * It is shield against recursion.
 *
 * @author kitko
 *
 */
public class PropertiesResolver {

    private PropertiesResolver() {
        //empty
    }

    /** Resolves properties
     *
     * @param properties properties we want to resolve
     * @param resolvedProperties already known properties
     * @return resolved properties
     */
    public static Properties resolveProperties(Properties properties, Properties resolvedProperties) {
        if (properties == null) {
            return null;
        }
        return resolveProperties(copyProperties(properties), copyProperties(resolvedProperties), new HashSet<String>(5));
    }

    /**
     * Resolve properties containing already known values
     *
     * @param properties properties
     * @return resolved properties
     */
    public static Properties resolveProperties(Properties properties) {
        if (properties == null) {
            return null;
        }
        Properties copy = copyProperties(properties);
        return resolveProperties(copy, new Properties(), new HashSet<String>(5));
    }

    private static Properties resolveProperties(Properties properties, Properties resolvedProperties,
            Set<String> justResolving) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String value = (String) entry.getValue();
            if (value.contains("$")) {
                value = resolveName((String) entry.getKey(), properties, resolvedProperties, justResolving);
                entry.setValue(value);
            }
        }
        return properties;
    }

    private static String resolveName(String name, Properties properties, Properties resolvedProperties,
            Set<String> justResolving) {
        String value = null;
        if (!justResolving.isEmpty()) {
            value = resolvedProperties.getProperty(name);
        }
        if (value != null) {
            return value;
        }
        if (justResolving.contains(name)) {
            value = "RECURSION";
            return value;
        }
        value = properties.getProperty(name);
        if (value == null) {
            value = "NOT_RESOLVED";
            return value;
        } else {
            justResolving.add(name);
            value = resolveValue(value, properties, resolvedProperties, justResolving);
            justResolving.remove(name);
            resolvedProperties.setProperty(name, value);
            return value;
        }
    }

    private static String resolveValue(String value, Properties properties, Properties resolvedProperties,
            Set<String> justResolving) {
        if (value == null) {
            return null;
        }
        int index = 0;
        int length = value.length();
        StringBuilder result = new StringBuilder();
        while (index >= 0 && index < length) {
            int varStart = value.indexOf("${", index);
            if (varStart >= 0) {
                int varEnd = value.indexOf('}', varStart);
                if (varEnd >= 0) {
                    String varSubstring = value.substring(varStart, varEnd + 1);
                    String varName = varSubstring.substring(2, varSubstring.length() - 1);
                    String varValue = resolveName(varName, properties, resolvedProperties, justResolving);
                    if ("NOT_RESOLVED".equals(varValue)) {
                        varValue = varSubstring;
                    } else if ("RECUSRION".equals(varValue)) {
                        varValue = "${RECURSION!!!_" + varName + "}";
                    }
                    result.append(value.substring(index, varStart));
                    result.append(varValue);
                    index = varEnd + 1;
                } else {
                    result.append(value.substring(index, value.length()));
                    index = value.length();
                }
            } else {
                result.append(value.substring(index, value.length()));
                index = value.length();
            }
        }
        return result.toString();
    }

    private static Properties copyProperties(Properties properties) {
        Properties result = new Properties();
        if (properties == null) {
            return result;
        }
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
