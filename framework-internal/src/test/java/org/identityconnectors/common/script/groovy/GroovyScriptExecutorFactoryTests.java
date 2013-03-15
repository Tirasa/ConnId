/**
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2011-2013 Tirasa. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License"). You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at https://oss.oracle.com/licenses/CDDL.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.common.script.groovy;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.script.ScriptExecutor;
import org.identityconnectors.common.script.ScriptExecutorFactory;
import org.junit.Assert;
import org.junit.Test;

public class GroovyScriptExecutorFactoryTests {

    @Test
    public void testValidScript()
            throws Exception {
        ScriptExecutor ex = getScriptExecutor("print 'Hello World\\n'");
        ex.execute(null);
        ex.execute(null);
        ex.execute(null);
        ex.execute(null);
    }

    @Test
    public void testWithVariables()
            throws Exception {
        Object actual;
        ScriptExecutor ex = getScriptExecutor("return x;");
        actual = ex.execute(CollectionUtil.<String, Object>newMap("x", 1));
        Assert.assertEquals(1, actual);
        actual = ex.execute(CollectionUtil.<String, Object>newMap("x", 2));
        Assert.assertEquals(2, actual);
    }

    private ScriptExecutor getScriptExecutor(String script) {
        ClassLoader loader = getClass().getClassLoader();
        return ScriptExecutorFactory.newInstance("GROOVY").newScriptExecutor(
                loader, script, false);
    }
}
