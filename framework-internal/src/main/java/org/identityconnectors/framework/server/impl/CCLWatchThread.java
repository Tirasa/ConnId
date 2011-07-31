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
package org.identityconnectors.framework.server.impl;

import org.identityconnectors.common.logging.Log;

/**
 * A thread that logs errors when its context class loader becomes null.
 * This is an attempt to provide more information to issue 604.
 */
public class CCLWatchThread extends Thread {

    private static final Log _log = Log.getLog(CCLWatchThread.class);

    public CCLWatchThread(String name) {
        super(name);
        checkCCL();
    }

    public CCLWatchThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
        checkCCL();
    }

    private void checkCCL() {
        if (getContextClassLoader() == null) {
            _log.error(new Throwable(), "The CCL of thread ''{0}'' was null after initialization. The CCL of current thread ''{1}'' is {2}",
                    getName(),
                    Thread.currentThread().getName(),
                    Thread.currentThread().getContextClassLoader());
        } else {
            _log.info("Creating thread ''{0}'' with a non-null CCL", getName());
        }
    }

    @Override
    public void setContextClassLoader(ClassLoader cl) {
        if (cl == null) {
            _log.error(new Throwable(), "Attempting to set the CCL of thread ''{0}'' to null", getName());
        }
        super.setContextClassLoader(cl);
    }
}
