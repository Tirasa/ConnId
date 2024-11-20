/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2024 ConnId. All rights reserved.
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
package org.identityconnectors.framework.api.operations;

import org.identityconnectors.framework.common.objects.LiveSyncDelta;
import org.identityconnectors.framework.common.objects.LiveSyncResultsHandler;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.spi.operations.LiveSyncOp;

/**
 * Poll for synchronization events--i.e., native changes to target objects.
 * <p>
 * Connectors that implement {@linkplain LiveSyncOp the LiveSyncOp SPI} will support
 * this.
 *
 * @see LiveSyncOp
 */
public interface LiveSyncApiOp extends APIOperation {

    /**
     * Request synchronization events--i.e., native changes to target objects.
     * <p>
     * This method will call the specified
     * {@linkplain LiveSyncResultsHandler#handle handler} once to pass back each
     * matching {@linkplain LiveSyncDelta synchronization event}. Once this method
     * returns, this method will no longer invoke the specified handler.
     *
     * @param objectClass The class of object for which to return synchronization events. Must not be null.
     * @param handler The result handler. Must not be null.
     * @param options Options that affect the way this operation is run. May be null.
     * @return The sync token or {@code null}.
     * @throws IllegalArgumentException if {@code objectClass} or {@code handler} is null
     * or if any argument is invalid.
     */
    void livesync(ObjectClass objectClass, LiveSyncResultsHandler handler, OperationOptions options);
}
