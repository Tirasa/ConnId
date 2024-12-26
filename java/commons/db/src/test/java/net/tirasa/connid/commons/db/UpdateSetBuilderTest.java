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
 * Portions Copyrighted 2011 ConnId.
 */
package net.tirasa.connid.commons.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Types;
import org.junit.jupiter.api.Test;

/**
 * Tests
 *
 * @version $Revision 1.0$
 * @since 1.0
 */
public class UpdateSetBuilderTest {

    private static final String MYSQL_USER_COLUMN = "User";

    private static final SQLParam VALUE = new SQLParam(MYSQL_USER_COLUMN, "name", Types.VARCHAR);

    /**
     * Test method for {@link org.identityconnectors.dbcommon.UpdateSetBuilder#UpdateSetBuilder()}.
     */
    @Test
    public void testUpdateSetBuilder() {
        UpdateSetBuilder actual = new UpdateSetBuilder();
        assertNotNull(actual);
        assertNotNull(actual.getParams());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.UpdateSetBuilder#addBind(String, String, Object)}.
     */
    @Test
    public void testAddBindExpression() {
        UpdateSetBuilder actual = new UpdateSetBuilder();
        assertNotNull(actual);
        // do the update
        actual.addBind(new SQLParam("test1", "val1"), "password(?)");
        actual.addBind(new SQLParam("test2", "val2"), "max(?)");
        assertNotNull(actual.getSQL());
        assertEquals("test1 = password(?) , test2 = max(?)", actual.getSQL());
        assertNotNull(actual.getParams());
        assertEquals(2, actual.getParams().size());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.UpdateSetBuilder#getParams()}.
     */
    @Test
    public void testGetValues() {
        UpdateSetBuilder actual = new UpdateSetBuilder();
        assertNotNull(actual);
        // do the update
        actual.addBind(VALUE);
        assertNotNull(actual.getSQL());
        assertEquals("User = ?", actual.getSQL());
        assertNotNull(actual.getParams());
        assertNotNull(actual.getParams().get(0));
        assertEquals(VALUE, actual.getParams().get(0));
        assertEquals(Types.VARCHAR, actual.getParams().get(0).getSqlType());
    }

    /**
     * Test method for {@link org.identityconnectors.dbcommon.UpdateSetBuilder#addBind(String, Object)}
     */
    @Test
    public void testAddBind() {
        UpdateSetBuilder actual = new UpdateSetBuilder();
        assertNotNull(actual);
        // do the update
        actual.addBind(VALUE);
        assertNotNull(actual.getParams());
        assertEquals(1, actual.getParams().size());
        assertNotNull(actual.getParams().get(0));
        assertEquals(VALUE, actual.getParams().get(0));
        assertEquals(MYSQL_USER_COLUMN + " = ?", actual.getSQL());
    }
}
