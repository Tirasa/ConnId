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
package org.identityconnectors.framework.common.objects;

/**
 * Callback interface for operations that are returning one or
 * more results. Currently used only by {@link org.identityconnectors.framework.api.operations.SearchApiOp Search}, but may be used
 * by other operations in the future.
 */
public interface ResultsHandler {
    /**
     * Call-back method to do whatever it is the caller wants to do with
     * each {@link ConnectorObject} that is returned in the result of
     * {@link org.identityconnectors.framework.api.operations.SearchApiOp}.
     * 
     * @param obj
     *            each object return from the search.
     * @return true if we should keep processing else false to cancel.
     * 
     * @throws RuntimeException
     *             the implementor should throw a {@link RuntimeException}
     *             that wraps any native exception (or that describes any other problem
     *             during execution) that is serious enough to stop the iteration.
     */
    boolean handle(final ConnectorObject obj);
}
