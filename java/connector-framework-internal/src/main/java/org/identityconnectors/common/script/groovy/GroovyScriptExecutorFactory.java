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
package org.identityconnectors.common.script.groovy;

import groovy.grape.GrabAnnotationTransformation;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.script.ScriptExecutor;
import org.identityconnectors.common.script.ScriptExecutorFactory;
import org.jenkinsci.plugins.scriptsecurity.sandbox.Whitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.blacklists.Blacklist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.RejectASTTransformsCustomizer;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SandboxInterceptor;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

/**
 * Creates a new ScriptExecutorFactory for executing Groovy scripts.
 * Scripts are compiled at the creation of a new instance of {@link ScriptExecutor}.
 */
public class GroovyScriptExecutorFactory extends ScriptExecutorFactory {

    private static final Log LOG = Log.getLog(GroovyScriptExecutorFactory.class);

    private static final CompilerConfiguration CC;

    private static Object BLACKLIST;

    static {
        CC = new CompilerConfiguration();

        try {
            CC.addCompilationCustomizers(new RejectASTTransformsCustomizer(), new SandboxTransformer());
            CC.setDisabledGlobalASTTransformations(Set.of(GrabAnnotationTransformation.class.getName()));

            try (InputStream in = GroovyScriptExecutorFactory.class.
                    getClassLoader().getResourceAsStream("META-INF/groovy.blacklist");
                    Reader reader = new InputStreamReader(in)) {

                BLACKLIST = new Blacklist(reader);
            } catch (IOException e) {
                throw new IllegalStateException("Could not load META-INF/groovy.blacklist", e);
            }
        } catch (NoClassDefFoundError noClassDefFound) {
            LOG.warn(noClassDefFound, "Groovy Sandbox runtime not found, disabling");
            BLACKLIST = null;
        }
    }

    private static class GroovyScriptExecutor implements ScriptExecutor {

        private final Script groovyScript;

        public GroovyScriptExecutor(final ClassLoader loader, final String script) {
            groovyScript = new GroovyShell(loader, CC).parse(script);
        }

        @Override
        public Object execute(final Map<String, Object> arguments) throws Exception {
            Object interceptor = null;
            try {
                interceptor = new SandboxInterceptor((Whitelist) BLACKLIST);
                ((GroovyInterceptor) interceptor).register();
            } catch (NoClassDefFoundError noClassDefFound) {
                // ignore
            }

            try {
                groovyScript.setBinding(new Binding(CollectionUtil.nullAsEmpty(arguments)));
                return groovyScript.run();
            } finally {
                if (interceptor != null) {
                    try {
                        ((GroovyInterceptor) interceptor).unregister();
                    } catch (NoClassDefFoundError noClassDefFound) {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always compile the script.
     */
    @Override
    public ScriptExecutor newScriptExecutor(
            final ClassLoader loader,
            final String script,
            final boolean compile) {

        return new GroovyScriptExecutor(loader, script);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLanguageName() {
        return "Groovy";
    }
}
