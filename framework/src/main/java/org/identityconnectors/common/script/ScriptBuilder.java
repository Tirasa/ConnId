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

/**
 * Builder for {@link Script}.
 */
public class ScriptBuilder {

    private String scriptLanguage;
    private String scriptText;

    /**
     * Creates a new <code>ScriptBuilder</code>.
     */
    public ScriptBuilder() {
    }

    /**
     * Returns the language of the script.
     * 
     * @return the script language.
     */
    public String getScriptLanguage() {
        return scriptLanguage;
    }

    /**
     * Sets the language of the script.
     * 
     * @param scriptLanguage the script language.
     * @return this builder.
     */
    public ScriptBuilder setScriptLanguage(String scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
        return this;
    }

    /**
     * Returns the text of the script.
     * 
     * @return the script text.
     */
    public String getScriptText() {
        return scriptText;
    }

    /**
     * Sets the text of the script.
     * 
     * @param scriptText the script text.
     * @return this builder.
     */
    public ScriptBuilder setScriptText(String scriptText) {
        this.scriptText = scriptText;
        return this;
    }

    /**
     * Creates a <code>Script</code>. Prior to calling this method the language
     * and the text should have been set.
     * 
     * @return a new script; never null.
     */
    public Script build() {
        return new Script(scriptLanguage, scriptText);
    }
}
