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

import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;

/**
 * Builds an {@link ScriptContext}.
 */
public final class ScriptContextBuilder {
    private String _scriptLanguage;
    private String _scriptText;
    private final Map<String, Object> _scriptArguments = new HashMap<String, Object>();

    /**
     * Creates an empty builder.
     */
    public ScriptContextBuilder() {

    }

    /**
     * Creates a builder with the required parameters specified.
     * 
     * @param scriptLanguage
     *            a string that identifies the language in which the script is
     *            written (e.g., <code>bash</code>, <code>csh</code>,
     *            <code>Perl4</code> or <code>Python</code>).
     * @param scriptText
     *            The text (i.e., actual characters) of the script.
     */
    public ScriptContextBuilder(String scriptLanguage, String scriptText) {
        _scriptLanguage = scriptLanguage;
        _scriptText = scriptText;
    }

    /**
     * Identifies the language in which the script is written (e.g.,
     * <code>bash</code>, <code>csh</code>, <code>Perl4</code> or
     * <code>Python</code>).
     * 
     * @return The script language.
     */
    public String getScriptLanguage() {
        return _scriptLanguage;
    }

    /**
     * Sets the script language
     * 
     * @param scriptLanguage
     *            The script language
     */
    public ScriptContextBuilder setScriptLanguage(String scriptLanguage) {
        _scriptLanguage = scriptLanguage;
        return this;
    }

    /**
     * Returns the actual characters of the script.
     * 
     * @return the actual characters of the script.
     */
    public String getScriptText() {
        return _scriptText;
    }

    /**
     * Sets the actual characters of the script.
     * 
     * @param scriptText
     *            The actual characters of the script.
     */
    public ScriptContextBuilder setScriptText(String scriptText) {
        _scriptText = scriptText;
        return this;
    }

    /**
     * Adds or sets an argument to pass to the script.
     * 
     * @param name
     *            The name of the argument. Must not be null.
     * @param value
     *            The value of the argument. Must be one of type types that the
     *            framework can serialize.
     * @see ObjectSerializerFactory for a list of supported types.
     */
    public ScriptContextBuilder addScriptArgument(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "Argument 'name' cannot be null.");
        }
        // don't validate value here - we do that implicitly when
        // we clone in the constructor of ScriptRequest
        _scriptArguments.put(name, value);
        return this;
    }

    /**
     * Removes the given script argument.
     * 
     * @param name
     *            The name of the argument. Must not be null.
     */
    public void removeScriptArgument(String name) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "Argument 'name' cannot be null.");
        }
        _scriptArguments.remove(name);
    }

    /**
     * Returns a mutable reference of the script arguments map.
     * 
     * @return A mutable reference of the script arguments map.
     */
    public Map<String, Object> getScriptArguments() {
        // might as well be mutable since it's the builder and
        // we don't want to deep copy anyway
        return _scriptArguments;
    }

    /**
     * Creates a <code>ScriptContext</code>. The <code>scriptLanguage</code>
     * and <code>scriptText</code> must be set prior to calling this.
     * 
     * @return The <code>ScriptContext</code>.
     */
    public ScriptContext build() {
        return new ScriptContext(_scriptLanguage, _scriptText, _scriptArguments);
    }
}
