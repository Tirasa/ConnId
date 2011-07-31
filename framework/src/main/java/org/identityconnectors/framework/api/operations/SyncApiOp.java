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
package org.identityconnectors.framework.api.operations;

import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.spi.operations.SyncOp;


/**
 * Poll for synchronization events--i.e., native changes to target objects. 
 * <p>
 * Connectors that implement {@linkplain SyncOp the SyncOp SPI} will support this.
 * @see SyncOp
 */
public interface SyncApiOp extends APIOperation {
    /**
     * Request synchronization events--i.e., native changes to target objects.
     * <p>
     * This method will call the specified {@linkplain SyncResultsHandler#handle handler} 
     * once to pass back each matching {@linkplain SyncDelta synchronization event}.
     * Once this method returns, this method will no longer invoke the specified handler.
     * <p>
     * Each {@linkplain SyncDelta#getToken() synchronization event contains a token}
     * that can be used to resume reading events <i>starting from that point in the event stream</i>.
     * In typical usage, a client will save the token from the final synchronization event
     * that was received from one invocation of this {@code sync()} method
     * and then pass that token into that client's next call to this {@code sync()} method.
     * This allows a client to "pick up where he left off" in receiving synchronization events.
     * However, a client can pass the token from <i>any</i> synchronization event
     * into a subsequent invocation of this {@code sync()} method.
     * This will return synchronization events (that represent native changes that
     * occurred) immediately subsequent to the event from which the client obtained the token.
     * <p>
     * A client that wants to read synchronization events "starting now"
     * can call {@link #getLatestSyncToken} and then pass that token 
     * into this {@code sync()} method.
     * 
     * @param objClass
     *            The class of object for which to return synchronization events. Must not be null.
     * @param token
     *            The token representing the last token from the previous sync.
     *            The {@code SyncResultsHandler} will return any number of
     *            {@linkplain SyncDelta} objects, each of which contains a token.
     *            Should be {@code null} if this is the client's first call 
     *            to the {@code sync()} method for this connector.
     * @param handler
     *            The result handler. Must not be null.
     * @param options
     *            Options that affect the way this operation is run.
     *            May be null.
     * @throws IllegalArgumentException if {@code objClass} or {@code handler} is null 
     *            or if any argument is invalid.
     */
    public void sync(ObjectClass objClass, SyncToken token,
            SyncResultsHandler handler,
            OperationOptions options);
    
    /**
     * Returns the token corresponding to the most recent synchronization event
     * for any instance of the specified object class.
     * <p>
     * An application that wants to receive synchronization events "starting now"
     * --i.e., wants to receive only native changes that occur after this method is called--
     * should call this method and then pass the resulting token 
     * into {@linkplain #sync the sync() method}.
     *  
     * @param objClass the class of object for which to find the most recent
     *          synchronization event (if any).
     * @return A token if synchronization events exist; otherwise {@code null}.
     */
    public SyncToken getLatestSyncToken(ObjectClass objClass);
}
