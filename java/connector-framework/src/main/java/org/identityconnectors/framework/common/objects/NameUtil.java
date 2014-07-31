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

import org.identityconnectors.common.StringUtil;

final class NameUtil {

    private NameUtil() {
    }

    public static boolean isSpecialName(final String name) {
        return name.startsWith("__") && name.endsWith("__");
    }

    public static String createSpecialName(final String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Name parameter must not be blank!");
        }
        final StringBuilder bld = new StringBuilder();
        bld.append("__").append(name).append("__");
        return bld.toString();
    }

    public static boolean namesEqual(final String name1, final String name2) {
        return name1.toUpperCase(LocaleCache.getInstance()).equals(
                name2.toUpperCase(LocaleCache.getInstance()));
    }

    public static int nameHashCode(final String name) {
        return name.toUpperCase(LocaleCache.getInstance()).hashCode();
    }
}
