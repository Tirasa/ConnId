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
package org.identityconnectors.common.l10n;

import java.util.Locale;

/**
 * Thread local variable that impacts localization of all messages in the
 * connector framework. This is roughly equivalent to .Net's
 * Thread.CurrentCulture.
 * <p/>
 * Note that this is an inheritable thread local so it is automatically
 * inherited from parent to child thread. Of course, if the child thread is part
 * of a thread pool, you will still need to manually propagate from parent to
 * child.
 */
public final class CurrentLocale {

    private static final InheritableThreadLocal<Locale> VALUE =
            new InheritableThreadLocal<Locale>();

    private CurrentLocale() {
        // empty constructor for singleton class
    }

    /**
     * Sets the locale for the current thread.
     *
     * @param locale
     *            The locale to use.
     */
    public static void set(final Locale locale) {
        VALUE.set(locale);
    }

    /**
     * Clears the locale for the current thread.
     */
    public static void clear() {
        VALUE.remove();
    }

    /**
     * Gets the locale from the current thread. Returns
     * <code>Locale.getDefault</code> if no locale is specified.
     *
     * @return the locale from the current thread.
     */
    public static Locale get() {
        final Locale rv = VALUE.get();
        return rv == null ? Locale.getDefault() : rv;
    }

    /**
     * Returns true if a thread-local locale is specified on the current thread.
     *
     * @return true if a thread-local locale is specified on the current thread.
     */
    public static boolean isSet() {
        return VALUE.get() != null;
    }
}
