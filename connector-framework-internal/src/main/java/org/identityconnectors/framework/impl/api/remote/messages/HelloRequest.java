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
 *
 * Portions Copyrighted 2011-2013 ForgeRock
 */

package org.identityconnectors.framework.impl.api.remote.messages;

/**
 * Sent the first time we connect to a given server. The server
 * will respond with a {@link HelloResponse}.
 */
public class HelloRequest implements Message {

    public static final int SERVER_INFO = 4;
    public static final int CONNECTOR_KEY_LIST = 16;
    //public static final int DEFAULT_CONFIG = 32;
    public static final int CONNECTOR_INFO = CONNECTOR_KEY_LIST | SERVER_INFO;

    private final int level;

    public HelloRequest(int infoLevel) {
        level = infoLevel;
    }

    public int getInfoLevel() {
        return level;
    }

    private boolean checkInfoLevel(int info) {
        return ((level & info) == info);
    }

    public boolean isServerInfo() {
        return checkInfoLevel(SERVER_INFO);
    }

    public boolean isConnectorKeys() {
        return checkInfoLevel(CONNECTOR_KEY_LIST);
    }

    public boolean isConnectorInfo() {
        return checkInfoLevel(CONNECTOR_INFO);
    }
}
