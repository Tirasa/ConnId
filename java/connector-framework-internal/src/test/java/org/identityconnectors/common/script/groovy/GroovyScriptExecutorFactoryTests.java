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

import static org.testng.Assert.assertEquals;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.script.ScriptExecutor;
import org.identityconnectors.common.script.ScriptExecutorFactory;
import org.testng.annotations.Test;

public class GroovyScriptExecutorFactoryTests {

    @Test
    public void testValidScript() throws Exception {
        ScriptExecutor ex = getScriptExecutor("print 'Hello World\\n'");
        ex.execute(null);
        ex.execute(null);
        ex.execute(null);
        ex.execute(null);
    }

    @Test
    public void testWithVariables() throws Exception {
        Object actual;
        ScriptExecutor ex = getScriptExecutor("return y != null ? y : x;");
        actual = ex.execute(CollectionUtil.<String, Object> newMap("x", 1, "y", null));
        assertEquals(actual, 1);
        actual = ex.execute(CollectionUtil.<String, Object> newMap("x", 1, "y", 2));
        assertEquals(actual, 2);
        actual = ex.execute(CollectionUtil.<String, Object> newMap("x", 3, "y", null));
        assertEquals(actual, 3);
    }

    private ScriptExecutor getScriptExecutor(String script) {
        ClassLoader loader = getClass().getClassLoader();
        return ScriptExecutorFactory.newInstance("GROOVY").newScriptExecutor(loader, script, false);
    }
}
