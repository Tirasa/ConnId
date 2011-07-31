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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates a version number. A version number is composed of up to four components:
 * major, minor, micro and specification number.
 * 
 * @since 1.1
 */
public final class Version implements Comparable<Version> {

    private static final Pattern PATTERN = Pattern.compile("(\\d+)(\\.(\\d+))?(\\.(\\d+))?(\\.(\\d+))?(-\\w+)?");
    // The indexes of the version component groups in the above pattern.
    private static final int[] GROUPS = { 1, 3, 5, 7 };

    private static final int MAJOR = 0;
    private static final int MINOR = MAJOR + 1;
    private static final int MICRO = MINOR + 1;
    private static final int REVISION = MICRO + 1;

    private static final int MAX_COMPONENTS = REVISION + 1;

    private final Integer[] components;

    /**
     * Parses the passed version string. The string can contain up to
     * four numeric component separated by a dot, followed by an alphanumberic
     * qualifier prepended by a dash. For example, the following are valid
     * versions:
     * <ul>
     *   <li>1</li>
     *   <li>1.1</li>
     *   <li>1.1.0</li>
     *   <li>1.2.3-alpha</li>
     *   <li>1.2.3.4-SNAPSHOT</li>
     * </ul>
     *
     * @param version the version string.
     */
    public static Version parse(String version) {
        return new Version(parseInternal(version));
    }

    /**
     * Creates a new version from components.
     *
     * @param components the components
     */
    public static Version create(Integer... components) {
        return new Version(components);
    }

    private static Integer[] parseInternal(String version) {
        Assertions.nullCheck(version, "version");        
        Matcher matcher = PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Invalid version number %s", version));
        }
        List<Integer> components = new ArrayList<Integer>(MAX_COMPONENTS);
        for (int group : GROUPS) {
            String text = matcher.group(group);
            if (text != null && !text.startsWith("-")) { // That would be the qualifier.
                try {
                    components.add(Integer.valueOf(text));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("Non-numeric version component %s", text));
                }
            }
        }
        return components.toArray(new Integer[components.size()]);
    }

    Version(Integer... components) {
        Assertions.nullCheck(components, "components");
        for (int i = 0; i < components.length; i++) {
            if (components[i] == null || components[i] < 0) {
                throw new IllegalArgumentException("Illegal version number");
            }
        }
        if (components.length == 0) {
            throw new IllegalArgumentException("Too few version components");
        }
        if (components.length > MAX_COMPONENTS) {
            throw new IllegalArgumentException("Too many version components");
        }
        this.components = new Integer[components.length];
        System.arraycopy(components, 0, this.components, 0, components.length);
    }

    /**
     * Returns this version number's major component.
     *
     * @return the major component; never null.
     */
    public Integer getMajor() {
        return getComponent(MAJOR);
    }

    /**
     * Returns this version number's minor component.
     *
     * @return the minor component or <code>null</code> if this version
     *         number doesn't have a minor component.
     */
    public Integer getMinor() {
        return getComponent(MINOR);
    }

    /**
     * Returns this version number's minor component.
     *
     * @return the minor component or <code>null</code> if this version
     *         number doesn't have a minor component.
     */
    public Integer getMicro() {
        return getComponent(MICRO);
    }

    /**
     * Returns this version number's minor component.
     *
     * @return the minor component or <code>null</code> if this version
     *         number doesn't have a revision component.
     */
    public Integer getRevision() {
        return getComponent(REVISION);
    }

    private Integer getComponent(int index) {
        return index < components.length ? components[index] : null;
    }

    /**
     * Returns this version as a string.
     *
     * @return this version as a string.
     */
    public String getVersion() {
        StringBuffer buffer = new StringBuffer();
        appendTo(buffer);
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        int result = 12345;
        for (int i = 0; i < components.length; i++) {
            result ^= (components[i] != null ? components[i] : 0) << i;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Version) {
            return compareTo((Version) o) == 0;
        }
        return false;
    }

    public int compareTo(Version that) {
        for (int i = 0; i < Math.max(this.components.length, that.components.length); i++) {
            Integer c1 = i < this.components.length ? this.components[i] : Integer.valueOf(0);
            Integer c2 = i < that.components.length ? that.components[i] : Integer.valueOf(0);
            int result = c1.compareTo(c2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Version[");
        appendTo(buf);
        buf.append(']');
        return buf.toString();
    }

    private void appendTo(StringBuffer buffer) {
        for (int i = 0; i < components.length; i++) {
            buffer.append(components[i]);
            if (i < components.length - 1) {
                buffer.append('.');
            }
        }
    }
}
