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
 * Portions Copyrighted 2010-2014 ForgeRock AS.
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;

public class NormalizingSyncResultsHandler implements SyncResultsHandler {

    private final SyncResultsHandler target;

    private final ObjectNormalizerFacade normalizer;

    public NormalizingSyncResultsHandler(
            final SyncResultsHandler target,
            final ObjectNormalizerFacade normalizer) {

        Assertions.nullCheck(target, "target");
        Assertions.nullCheck(normalizer, "normalizer");
        this.target = target;
        this.normalizer = normalizer;
    }

    @Override
    public boolean handle(final SyncDelta delta) {
        SyncDelta normalized = normalizer.normalizeSyncDelta(delta);
        return target.handle(normalized);
    }
}
