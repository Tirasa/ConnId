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
package org.identityconnectors.framework.common.objects;

import org.identityconnectors.framework.api.operations.LiveSyncApiOp;

/**
 * Callback interface that an application implements in order to handle results
 * from {@link LiveSyncApiOp} in a stream-processing fashion.
 */
public interface LiveSyncResultsHandler {

    /**
     * Called to handle a delta in the stream. The Connector framework will call this method multiple times, once for
     * each result. Although this method is callback, the framework will invoke it synchronously. Thus, the framework
     * guarantees that once an application's call to {@link LiveSyncApiOp#sync LiveSyncApiOp#sync()} returns, the
     * framework will no longer call this method to handle results from that {@code sync} operation.
     *
     * @param delta The change
     * @return True if the application wants to continue processing more results.
     * @throws RuntimeException If the application encounters an exception. This will stop iteration and the exception
     * will propagate to the application.
     */
    boolean handle(LiveSyncDelta delta);
}
