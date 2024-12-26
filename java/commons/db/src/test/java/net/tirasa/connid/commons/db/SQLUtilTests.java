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

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import org.identityconnectors.common.security.GuardedString;
import org.junit.jupiter.api.Test;

/**
 *
 * The SQL util tests.
 */
public class SQLUtilTests {

    /**
     * Test method for {@link SQLUtil#closeQuietly(Connection)}.
     *
     * @throws Exception
     */
    @Test
    public void quietConnectionClose() throws Exception {
        ExpectProxy<Connection> tp = new ExpectProxy<>();
        tp.expectAndReturn("isClosed", Boolean.FALSE);
        tp.expectAndThrow("close", new SQLException("expected"));
        Connection c = tp.getProxy(Connection.class);
        DatabaseConnection dbc = new DatabaseConnection(c);
        SQLUtil.closeQuietly(dbc);
        assertTrue(tp.isDone());
        tp = new ExpectProxy<>();
        tp.expectAndReturn("isClosed", Boolean.TRUE);
        c = tp.getProxy(Connection.class);
        dbc = new DatabaseConnection(c);
        SQLUtil.closeQuietly(dbc);
        assertTrue(tp.isDone());
        //null tests
        dbc = null;
        SQLUtil.closeQuietly(dbc);
    }

    /**
     * Test method for {@link SQLUtil#rollbackQuietly(Connection)}.
     *
     * @throws Exception
     */
    @Test
    public void quietConnectionRolback() throws Exception {
        ExpectProxy<Connection> tp = new ExpectProxy<>();
        tp.expectAndReturn("isClosed", Boolean.FALSE);
        tp.expectAndThrow("rollback", new SQLException("expected"));
        Connection s = tp.getProxy(Connection.class);
        DatabaseConnection dbc = new DatabaseConnection(s);
        SQLUtil.rollbackQuietly(dbc);
        assertTrue(tp.isDone());
        tp = new ExpectProxy<>();
        tp.expectAndReturn("isClosed", Boolean.TRUE);
        s = tp.getProxy(Connection.class);
        dbc = new DatabaseConnection(s);
        SQLUtil.rollbackQuietly(dbc);
        assertTrue(tp.isDone());
        //null tests
        dbc = null;
        SQLUtil.rollbackQuietly(dbc);
    }

    /**
     * Test method for {@link SQLUtil#closeQuietly(Statement)}.
     */
    @Test
    public void quietStatementClose() {
        ExpectProxy<Statement> tp = new ExpectProxy<Statement>().expectAndThrow("close", new SQLException("expected"));
        Statement s = tp.getProxy(Statement.class);
        SQLUtil.closeQuietly(s);
        assertTrue(tp.isDone());
    }

    /**
     * Test method for {@link SQLUtil#closeQuietly(ResultSet)}.
     */
    @Test
    public void quietResultSetClose() {
        ExpectProxy<ResultSet> tp = new ExpectProxy<ResultSet>().expectAndThrow("close", new SQLException("expected"));
        ResultSet c = tp.getProxy(ResultSet.class);
        SQLUtil.closeQuietly(c);
        assertTrue(tp.isDone());
    }

    /**
     * Test method for {@link SQLUtil#closeQuietly(Connection)}.
     */
    @Test
    public void withNull() {
        // attempt to close on a null..
        SQLUtil.closeQuietly((Connection) null);
        SQLUtil.closeQuietly((ResultSet) null);
        SQLUtil.closeQuietly((Statement) null);
    }

    @Test
    public void testBuildConnectorObjectBuilderNull() throws SQLException {
        assertThrows(NullPointerException.class, () -> SQLUtil.getColumnValues(null));
    }

    @Test
    public void testConvertBlob() throws SQLException {
        ExpectProxy<Blob> tb = new ExpectProxy<>();
        byte[] expected = new byte[] { 'a', 'h', 'o', 'j' };
        final ByteArrayInputStream is = new ByteArrayInputStream(expected);
        tb.expectAndReturn("getBinaryStream", is);
        Blob blob = tb.getProxy(Blob.class);
        final Object object = SQLUtil.jdbc2AttributeValue(blob);
        assertEquals(expected[0], ((byte[]) object)[0]);
        assertEquals(expected[3], ((byte[]) object)[3]);
        assertTrue(tb.isDone());
    }

    @Test
    public void testConvertBooleanToAttribute() throws SQLException {
        String expected = "test";
        final Object object = SQLUtil.jdbc2AttributeValue(expected);
        assertEquals(expected, object);
    }

    @Test
    public void testConvertDateToAttribute() throws SQLException {
        java.sql.Date src = new java.sql.Date(System.currentTimeMillis());
        final Object object = SQLUtil.jdbc2AttributeValue(src);
        assertEquals(src.toString(), object);
    }

    @Test
    public void testString2Timestamp() throws SQLException {
        final String src = "2008-12-31 23:59:59.999999999";
        final Timestamp timestamp = SQLUtil.string2Timestamp(src);
        assertEquals(src, SQLUtil.timestamp2String(timestamp));
    }

    @Test
    public void testString2Date() throws SQLException {
        final String src = "2008-12-31";
        final Date date = SQLUtil.string2Date(src);
        assertEquals(src, SQLUtil.date2String(date));
    }

    @Test
    public void testString2Time() throws SQLException {
        final String src = "23:59:59";
        final Time time = SQLUtil.string2Time(src);
        assertEquals(src, SQLUtil.time2String(time));
    }

    @Test
    public void testConvertTimestampToAttribute() throws SQLException {
        Timestamp src = new Timestamp(System.currentTimeMillis());
        final Object object = SQLUtil.jdbc2AttributeValue(src);
        assertEquals(src.toString(), object);
    }

    @Test
    public void testConvertTimestampToJDBC() {
        Timestamp expected = new Timestamp(System.currentTimeMillis());
        String src = SQLUtil.timestamp2String(expected);
        Object actual = SQLUtil.string2Timestamp(src);
        assertNotNull(actual);
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected, actual);
    }

    @Test
    public void testConvertSqlDateToJDBC() {
        Date expected = new Date(System.currentTimeMillis());
        String src = SQLUtil.date2String(expected);
        Object actual = SQLUtil.string2Date(src);
        assertNotNull(actual);
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testNormalizeNullValues() {
        final String sql = "insert into table values(?, ?, ?)";
        final List<SQLParam> params = new ArrayList<>();
        params.add(new SQLParam("test", "test"));
        params.add(new SQLParam("null", null)); //Null unspecified should be normalized
        params.add(new SQLParam("null2", null, Types.VARCHAR)); //Null typed should remain
        final List<SQLParam> out = new ArrayList<>();
        String actual = SQLUtil.normalizeNullValues(sql, params, out);
        assertNotNull(actual);
        assertEquals("insert into table values(?, null, ?)", actual);
        assertEquals(2, out.size());
    }

    @Test
    public void testNormalizeNullValuesSame() {
        final String sql = "insert into table values(?, ?, ?)";
        final List<SQLParam> params = new ArrayList<>();
        params.add(new SQLParam("test1", "test"));
        params.add(new SQLParam("test2", 1));
        params.add(new SQLParam("test3", 5, Types.VARCHAR));
        final List<SQLParam> out = new ArrayList<>();
        String actual = SQLUtil.normalizeNullValues(sql, params, out);
        assertNotNull(actual);
        assertEquals("insert into table values(?, ?, ?)", actual);
        assertEquals(3, out.size());
    }

    @Test
    public void testNormalizeNullValuesLess() {
        final String sql = "insert into table values(?, ?, ?)";
        final List<SQLParam> params = new ArrayList<>();
        params.add(new SQLParam("test1", "test"));
        params.add(new SQLParam("test2", 3));
        final List<SQLParam> out = new ArrayList<>();
        try {
            SQLUtil.normalizeNullValues(sql, params, out);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            // expected
        }
        params.add(new SQLParam("test3", 3));
        params.add(new SQLParam("test4", 3));
        try {
            SQLUtil.normalizeNullValues(sql, params, out);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            // expected
        }
    }

    /**
     * GetAttributeSet test method.
     *
     * @throws SQLException *
     */
    @Test
    public void testGetAttributeSet() throws SQLException {
        final String TEST1 = "test1";
        final String TEST_VAL1 = "testValue1";
        final String TEST2 = "test2";
        final String TEST_VAL2 = "testValue2";
        //Resultset
        final ExpectProxy<ResultSet> trs = new ExpectProxy<>();
        ResultSet resultSetProxy = trs.getProxy(ResultSet.class);
        //Metadata
        final ExpectProxy<ResultSetMetaData> trsmd = new ExpectProxy<>();
        ResultSetMetaData metaDataProxy = trsmd.getProxy(ResultSetMetaData.class);
        trs.expectAndReturn("getMetaData", metaDataProxy);
        trsmd.expectAndReturn("getColumnCount", 2);
        trsmd.expectAndReturn("getColumnName", TEST1);
        trsmd.expectAndReturn("getColumnType", Types.VARCHAR);
        trs.expectAndReturn("getString", TEST_VAL1);
        trsmd.expectAndReturn("getColumnName", TEST2);
        trsmd.expectAndReturn("getColumnType", Types.VARCHAR);
        trs.expectAndReturn("getString", TEST_VAL2);
        final Map<String, SQLParam> actual = SQLUtil.getColumnValues(resultSetProxy);
        assertTrue(trs.isDone());
        assertTrue(trsmd.isDone());
        assertEquals(2, actual.size());
        assertNotNull(actual.get(TEST1));
        assertNotNull(actual.get(TEST2));
        assertEquals(TEST_VAL1, actual.get(TEST1).getValue());
        assertEquals(TEST_VAL2, actual.get(TEST2).getValue());
    }

    /**
     * GetAttributeSet test method.
     *
     * @throws SQLException *
     */
    @Test
    public void testGetSQLParam() throws SQLException {
        final String TEST_STR = "testValue1";
        final Timestamp TEST_TMS = new Timestamp(System.currentTimeMillis());
        final Date TEST_DATE = new Date(System.currentTimeMillis());
        final Time TEST_TIME = new Time(System.currentTimeMillis());
        //Resultset
        final ExpectProxy<ResultSet> trs = new ExpectProxy<>();
        ResultSet resultSetProxy = trs.getProxy(ResultSet.class);
        trs.expectAndReturn("getObject", TEST_STR);
        SQLParam actual = SQLUtil.getSQLParam(resultSetProxy, 0, TEST_STR, Types.NULL);
        assertTrue(trs.isDone());
        assertNotNull(actual);
        assertEquals(TEST_STR, actual.getValue());
        trs.expectAndReturn("getString", TEST_STR);
        actual = SQLUtil.getSQLParam(resultSetProxy, 0, TEST_STR, Types.VARCHAR);
        assertTrue(trs.isDone());
        assertNotNull(actual);
        assertEquals(TEST_STR, actual.getValue());
        trs.expectAndReturn("getObject", TEST_STR);
        actual = SQLUtil.getSQLParam(resultSetProxy, 0, TEST_STR, Types.DOUBLE);
        assertTrue(trs.isDone());
        assertNotNull(actual);
        assertEquals(TEST_STR, actual.getValue());
        trs.expectAndReturn("getObject", TEST_STR);
        actual = SQLUtil.getSQLParam(resultSetProxy, 0, TEST_STR, Types.BLOB);
        assertTrue(trs.isDone());
        assertNotNull(actual);
        assertEquals(TEST_STR, actual.getValue());
        trs.expectAndReturn("getTimestamp", TEST_TMS);
        actual = SQLUtil.getSQLParam(resultSetProxy, 0, TEST_STR, Types.TIMESTAMP);
        assertTrue(trs.isDone());
        assertNotNull(actual);
        assertEquals(TEST_TMS, actual.getValue());
        trs.expectAndReturn("getDate", TEST_DATE);
        actual = SQLUtil.getSQLParam(resultSetProxy, 0, TEST_STR, Types.DATE);
        assertTrue(trs.isDone());
        assertNotNull(actual);
        assertEquals(TEST_DATE, actual.getValue());
        trs.expectAndReturn("getTime", TEST_TIME);
        actual = SQLUtil.getSQLParam(resultSetProxy, 0, TEST_STR, Types.TIME);
        assertTrue(trs.isDone());
        assertNotNull(actual);
        assertEquals(TEST_TIME, actual.getValue());
        trs.expectAndReturn("getBoolean", Boolean.TRUE);
        actual = SQLUtil.getSQLParam(resultSetProxy, 0, TEST_STR, Types.BOOLEAN);
        assertTrue(trs.isDone());
        assertNotNull(actual);
        assertEquals(Boolean.TRUE, actual.getValue());
    }

    /**
     * GetAttributeSet test method.
     *
     * @throws SQLException *
     */
    @Test
    public void testSetSQLParam() throws SQLException {
        final String TEST_STR = "testValue1";
        final Timestamp TEST_TMS = new Timestamp(System.currentTimeMillis());
        final Date TEST_DATE = new Date(System.currentTimeMillis());
        final Time TEST_TIME = new Time(System.currentTimeMillis());
        //Resultset
        final ExpectProxy<PreparedStatement> trs = new ExpectProxy<>();
        PreparedStatement resultSetProxy = trs.getProxy(PreparedStatement.class);
        trs.expect("setNull");
        SQLUtil.setSQLParam(resultSetProxy, 0, new SQLParam(TEST_STR, null, Types.CHAR));
        assertTrue(trs.isDone());
        trs.expect("setObject");
        SQLUtil.setSQLParam(resultSetProxy, 0, new SQLParam(TEST_STR, TEST_STR));
        assertTrue(trs.isDone());
        trs.expect("setString");
        SQLUtil.setSQLParam(resultSetProxy, 0, new SQLParam(TEST_STR, TEST_STR, Types.CHAR));
        assertTrue(trs.isDone());
        trs.expect("setBoolean");
        SQLUtil.setSQLParam(resultSetProxy, 0, new SQLParam(TEST_STR, Boolean.TRUE, Types.BOOLEAN));
        assertTrue(trs.isDone());
        trs.expect("setTimestamp");
        SQLUtil.setSQLParam(resultSetProxy, 0, new SQLParam(TEST_STR, TEST_TMS, Types.TIMESTAMP));
        assertTrue(trs.isDone());
        trs.expect("setTime");
        SQLUtil.setSQLParam(resultSetProxy, 0, new SQLParam(TEST_STR, TEST_TIME, Types.TIME));
        assertTrue(trs.isDone());
        trs.expect("setDate");
        SQLUtil.setSQLParam(resultSetProxy, 0, new SQLParam(TEST_STR, TEST_DATE, Types.DATE));
        assertTrue(trs.isDone());
    }

    /**
     * GetAttributeSet test method.
     */
    @Test
    public void testGetSQLAttributeType() {
        assertEquals(BigDecimal.class, SQLUtil.getSQLAttributeType(Types.DECIMAL));
        assertEquals(BigDecimal.class, SQLUtil.getSQLAttributeType(Types.NUMERIC));
        assertEquals(Double.class, SQLUtil.getSQLAttributeType(Types.DOUBLE));
        assertEquals(BigDecimal.class, SQLUtil.getSQLAttributeType(Types.NUMERIC));
        assertEquals(Float.class, SQLUtil.getSQLAttributeType(Types.FLOAT));
        assertEquals(Float.class, SQLUtil.getSQLAttributeType(Types.REAL));
        assertEquals(Integer.class, SQLUtil.getSQLAttributeType(Types.INTEGER));
        assertEquals(Long.class, SQLUtil.getSQLAttributeType(Types.BIGINT));
        assertEquals(Byte.class, SQLUtil.getSQLAttributeType(Types.TINYINT));
        assertEquals(byte[].class, SQLUtil.getSQLAttributeType(Types.BLOB));
        assertEquals(byte[].class, SQLUtil.getSQLAttributeType(Types.BINARY));
        assertEquals(byte[].class, SQLUtil.getSQLAttributeType(Types.LONGVARBINARY));
        assertEquals(byte[].class, SQLUtil.getSQLAttributeType(Types.VARBINARY));
        assertEquals(Boolean.class, SQLUtil.getSQLAttributeType(Types.BIT));
        assertEquals(Boolean.class, SQLUtil.getSQLAttributeType(Types.BOOLEAN));
        assertEquals(String.class, SQLUtil.getSQLAttributeType(Types.CHAR));
        assertEquals(String.class, SQLUtil.getSQLAttributeType(Types.CLOB));
        assertEquals(String.class, SQLUtil.getSQLAttributeType(Types.VARCHAR));
    }

    /**
     * GetAttributeSet test method.
     *
     * @throws SQLException *
     */
    @Test
    public void testJdbc2Attribute() throws SQLException {
        final String TEST_STR = "testValue1";
        final Timestamp TEST_TMS = new Timestamp(System.currentTimeMillis());
        final Date TEST_DATE = new Date(System.currentTimeMillis());
        final Time TEST_TIME = new Time(System.currentTimeMillis());
        Object actual = SQLUtil.jdbc2AttributeValue(TEST_STR);
        assertEquals(TEST_STR, actual);
        actual = SQLUtil.jdbc2AttributeValue(TEST_TMS);
        assertEquals(TEST_TMS.toString(), actual);
        actual = SQLUtil.jdbc2AttributeValue(TEST_DATE);
        assertEquals(TEST_DATE.toString(), actual);
        actual = SQLUtil.jdbc2AttributeValue(TEST_TIME);
        assertEquals(TEST_TIME.toString(), actual);
        actual = SQLUtil.jdbc2AttributeValue(1);
        assertEquals(1, actual);
        actual = SQLUtil.jdbc2AttributeValue(1L);
        assertEquals(1L, actual);
        actual = SQLUtil.jdbc2AttributeValue(1d);
        assertEquals(1d, actual);
        actual = SQLUtil.jdbc2AttributeValue(1f);
        assertEquals(1f, actual);
        actual = SQLUtil.jdbc2AttributeValue(true);
        assertEquals(true, actual);
    }

    /**
     * GetAttributeSet test method.
     *
     * @throws SQLException *
     */
    @Test
    public void testAttribute2JdbcValue() throws SQLException {
        final String TEST_STR = "testValue1";
        final Timestamp TEST_TMS = new Timestamp(System.currentTimeMillis());
        final Date TEST_DATE = new Date(System.currentTimeMillis());
        final Time TEST_TIME = new Time(System.currentTimeMillis());
        Object actual = SQLUtil.attribute2jdbcValue(TEST_STR, Types.CHAR);
        assertEquals(TEST_STR, actual);
        actual = SQLUtil.attribute2jdbcValue(TEST_TMS.toString(), Types.TIMESTAMP);
        assertEquals(TEST_TMS, actual);
        actual = SQLUtil.attribute2jdbcValue(TEST_TIME.toString(), Types.TIME);
        assertEquals(TEST_TIME.toString(), actual.toString());
        actual = SQLUtil.attribute2jdbcValue(TEST_DATE.toString(), Types.DATE);
        assertEquals(TEST_DATE.toString(), actual.toString());
        actual = SQLUtil.attribute2jdbcValue("55.55", Types.DOUBLE);
        assertEquals(55.55d, actual);
        actual = SQLUtil.attribute2jdbcValue("true", Types.BIT);
        assertEquals(true, actual);
    }

    /**
     * We need this helper class as InitialContextFactory class name value to Hashtable into InitialContext.
     *
     * We must use instantiable classname and class must be accessible
     */
    public static class MockContextFactory implements InitialContextFactory {

        @Override
        public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {
            ExpectProxy<DataSource> dsProxy = new ExpectProxy<>();
            dsProxy.expectAndReturn("getConnection", new ExpectProxy<Connection>().getProxy(Connection.class));
            ExpectProxy<Context> ctxProxy = new ExpectProxy<>();
            ctxProxy.expectAndReturn("lookup", dsProxy.getProxy(DataSource.class));
            return ctxProxy.getProxy(Context.class);
        }
    }

    /**
     * Tests getting connection from dataSource.
     */
    @Test
    public void testGetConnectionFromDS() {
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", MockContextFactory.class.getName());
        assertNotNull(SQLUtil.getDatasourceConnection(
                "", "user", new GuardedString("password".toCharArray()), properties));
        assertNotNull(SQLUtil.getDatasourceConnection("", properties));
    }
}
