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

import org.identityconnectors.common.Assertions;

/**
 * Represents a script in a scripting language.
 * 
 * @since 1.1
 */
public final class Script {

    private final String scriptLanguage;
    private final String scriptText;

    Script(String scriptLanguage, String scriptText) {
        Assertions.blankCheck(scriptLanguage, "scriptLanguage");
        Assertions.nullCheck(scriptText, "scriptText"); // Allow empty text.
        this.scriptLanguage = scriptLanguage;
        this.scriptText = scriptText;
    }

    /**
     * Returns the language of this script.
     * 
     * @return the script language; never null.
     */
    public String getScriptLanguage() {
        return scriptLanguage;
    }

    /**
     * Returns the text of this script.
     * 
     * @return the script text; never null.
     */
    public String getScriptText() {
        return scriptText;
    }

    @Override
    public int hashCode() {
        return scriptLanguage.hashCode() ^ scriptText.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Script) {
            Script other = (Script) obj;
            if (!scriptLanguage.equals(other.scriptLanguage)) {
                return false;
            }
            if (!scriptText.equals(other.scriptText)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        // Text can be large, probably should not be included.
        return "Script: " + scriptLanguage;
    }
}
