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
package org.identityconnectors.common.script.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Map;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.script.ScriptExecutor;
import org.identityconnectors.common.script.ScriptExecutorFactory;


/**
 * Creates a new ScriptExecutorFactory for executing Groovy scripts. Scripts are
 * compiled at the creation of a new instance of {@link ScriptExecutor}.
 */
public class GroovyScriptExecutorFactory extends ScriptExecutorFactory {

    /**
     * Make sure we blow up if Groovy does not exist.
     */
    public GroovyScriptExecutorFactory() {
        new GroovyShell();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always compile the script.
     */
    @Override
    public ScriptExecutor newScriptExecutor(ClassLoader loader, String script,
            boolean compile) {
        return new GroovyScriptExecutor(loader, script);
    }

    private static class GroovyScriptExecutor implements ScriptExecutor {
        private final Script _groovyScript;

        public GroovyScriptExecutor(ClassLoader loader, String script) {
            _groovyScript = new GroovyShell(loader).parse(script);
        }

        public Object execute(Map<String, Object> arguments) throws Exception {
            Map<String, Object> args = CollectionUtil.nullAsEmpty(arguments);
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                _groovyScript.setProperty(entry.getKey(), entry.getValue());
            }
            return _groovyScript.run();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLanguageName() {
        return "Groovy";
    }
}
