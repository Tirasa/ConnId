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
package org.identityconnectors.framework.impl.api.local.operations;

import static org.identityconnectors.framework.common.objects.AttributeBuilder.build;
import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.testng.annotations.Test;

public class AttributesToGetResultsHandlerTests {

    @Test(expectedExceptions = NullPointerException.class)
    public void testAttrsToGet() throws Exception {
        new TestHandler((String[]) null);
    }

    @Test
    public void testReduceAttributes() throws Exception {
        String[] attrsToGet = { "a", "b" };
        TestHandler tst = new TestHandler(attrsToGet);
        Set<Attribute> expected = CollectionUtil.newSet(build("a"), build("b"));
        Set<Attribute> testAttrs = CollectionUtil.newSet(expected);
        testAttrs.add(build("c"));
        Set<Attribute> actual = tst.reduceToAttrsToGet(testAttrs);
        assertEquals(actual, expected);
    }

    @Test
    public void testIgnoreMissing() throws Exception {
        String[] attrsToGet = { "a", "b", "c", "d" };
        TestHandler tst = new TestHandler(attrsToGet);
        Set<Attribute> expected = CollectionUtil.newSet(build("a"), build("b"));
        Set<Attribute> testAttrs = CollectionUtil.newSet(expected);
        testAttrs.add(build("g"));
        Set<Attribute> actual = tst.reduceToAttrsToGet(testAttrs);
        assertEquals(actual, expected);
    }

    @Test
    public void testWithConnectorObject() throws Exception {
        String[] attrsToGet = { "a", "b" };
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid("1");
        bld.setName("aname");
        bld.addAttribute(AttributeBuilder.build("a", 1));
        bld.addAttribute(AttributeBuilder.build("b", 1));
        ConnectorObject expected = bld.build();
        bld.addAttribute(AttributeBuilder.build("c", 1));
        TestHandler tst = new TestHandler(attrsToGet);
        ConnectorObject actual = tst.reduceToAttrsToGet(bld.build());
        assertEquals(actual, expected);
    }

    static class TestHandler extends AttributesToGetResultsHandler {

        public TestHandler(String[] attrsToGet) {
            super(attrsToGet);
        }
    }
}
