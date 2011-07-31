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
package org.identityconnectors.common.script;

import java.util.Map;

/**
 * Represents a (possibly compiled) script. It can be invoked many
 * times with many arguments:
 * TODO: Specify thread safety. Can this be called from multiple
 * threads or not? Need empirical data from a few scripting engines to
 * determine which is most appropriate.
 */
public interface ScriptExecutor {
    /**
     * Executes the script with the given arguments.
     * @param arguments Map of arguments to pass to the script.
     * @return A result, if any.
     * @throws Exception Whatever native exception the script engine
     * produces. (When called from the connector framework, this will
     * subsequently be wrapped in a ConnectorException)
     */
    public Object execute(Map<String,Object> arguments) throws Exception;
}
