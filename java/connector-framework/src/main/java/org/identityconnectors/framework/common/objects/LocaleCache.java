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

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable reference to a {@link Locale } instance. Can be used as an
 * immutable replacement for {@link Locale#getDefault()}.
 *
 * @author Andrei Badea
 */
final class LocaleCache {

    private static final AtomicReference<Locale> INSTANCE = new AtomicReference<Locale>();

    public static Locale getInstance() {
        INSTANCE.compareAndSet(null, Locale.getDefault());
        return INSTANCE.get();
    }

    // For tests only!
    static synchronized void reset() {
        INSTANCE.set(null);
    }

    private LocaleCache() {
    }
}
