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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * The SQL helper/util class.
 */
public final class SQLUtil {

    private static final Log LOG = Log.getLog(SQLUtil.class);

    /**
     * Never allow this to be instantiated.
     */
    private SQLUtil() {
        throw new AssertionError();
    }

    /**
     * Get the connection from the datasource.
     *
     * @param datasourceName datasource JNDI name
     * @param env properties
     * @return the connection get from default jndi context
     */
    public static Connection getDatasourceConnection(final String datasourceName, final Properties env) {
        try {
            LOG.ok("Datasource: {0}", datasourceName);
            LOG.ok("Properties");
            for (final String propertyName : env.stringPropertyNames()) {
                LOG.ok(propertyName + ": {0}", env.getProperty(propertyName));
            }
            final javax.naming.InitialContext ic = getInitialContext(env);
            LOG.ok("Initial context: {0}", ic);
            final DataSource ds = (DataSource) ic.lookup("java:/comp/env/" + datasourceName);
            return ds.getConnection();
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    /**
     * Get the connection from the dataSource with specified user and password.
     *
     * @param datasourceName datasource JNDI name
     * @param user DB user
     * @param password DB password
     * @param env properties
     * @return the connection get from dataSource
     */
    public static Connection getDatasourceConnection(
            final String datasourceName, final String user, GuardedString password, final Properties env) {
        try {
            LOG.ok("Datasource: {0}", datasourceName);
            LOG.ok("User: {0}", user);
            for (final String propertyName : env.stringPropertyNames()) {
                LOG.ok("Properties");
                LOG.ok(propertyName + ": {0}", env.getProperty(propertyName));
            }
            javax.naming.InitialContext ic = getInitialContext(env);
            LOG.ok("Initial context created");
            final DataSource ds = (DataSource) ic.lookup("java:/comp/env/" + datasourceName);
            LOG.ok("Datasource context created");
            final Connection[] ret = new Connection[1];
            password.access(new GuardedString.Accessor() {

                @Override
                public void access(char[] clearChars) {
                    try {
                        ret[0] = ds.getConnection(user, new String(clearChars));
                    } catch (UnsupportedOperationException | SQLFeatureNotSupportedException nse) {
                        try {
                            // some data Sources like HikariCP or Tomcat's BasicDataSource do not support anymore
                            // getConnection(String username, String password)
                            ret[0] = ds.getConnection();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (SQLException e) {
                        // checked exception are not allowed in the access method 
                        // Lets use the exception softening pattern
                        throw new RuntimeException(e);
                    }
                }
            });
            return ret[0];
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    /**
     * Get the connection from the dataSource with specified user and password.
     *
     * @param datasourceName datasource JNDI name
     * @param user DB user
     * @param password DB password
     * @return the connection get from dataSource
     */
    public static Connection getDatasourceConnection(
            final String datasourceName,
            final String user,
            GuardedString password) {

        return getDatasourceConnection(datasourceName, user, password, null);
    }

    /**
     * Get the initial context method.
     *
     * @param env environment hastable is null or empty aware
     * @return The Context
     * @throws NamingException
     */
    private static javax.naming.InitialContext getInitialContext(final Properties env) throws NamingException {
        return (env == null || env.isEmpty())
                ? new InitialContext()
                : new InitialContext(env);
    }

    /**
     * Get the connection from the datasource.
     *
     * @param datasourceName datasource JNDI name
     * @return the connection get from default jndi context
     */
    public static Connection getDatasourceConnection(final String datasourceName) {
        return getDatasourceConnection(datasourceName, null);
    }

    /**
     * Gets a {@link java.sql.Connection} using the basic driver manager.
     *
     * @param driver jdbc driver name
     * @param url jdbc connection url
     * @param login jdbc login name
     * @param password jdbc password
     * @return a valid connection
     */
    public static Connection getDriverMangerConnection(
            final String driver, final String url, final String login, final GuardedString password) {
        // create the connection base on the configuration..
        final Connection[] ret = new Connection[1];
        try {
            // load the driver class..
            Class.forName(driver);
            // get the database URL..
            // check if there is authentication involved.
            if (StringUtil.isNotBlank(login)) {
                password.access(new GuardedString.Accessor() {

                    @Override
                    public void access(char[] clearChars) {
                        try {
                            ret[0] = DriverManager.getConnection(url, login, new String(clearChars));
                        } catch (SQLException e) {
                            // checked exception are not allowed in the access method 
                            // Lets use the exception softening pattern
                            throw new RuntimeException(e);
                        }
                    }
                });
            } else {
                ret[0] = DriverManager.getConnection(url);
            }
            // turn off auto-commit
            try {
                ret[0].setAutoCommit(false);
            } catch (SQLException expected) {
                LOG.error(expected, "setAutoCommit() exception");
            }
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
        return ret[0];
    }

    /**
     * Ignores any exception thrown by the {@link Connection} parameter when closed, it also checks for {@code null}.
     *
     * @param conn JDBC connection to rollback.
     */
    public static void rollbackQuietly(final Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
            }
        } catch (SQLException expected) {
            //expected
        }
    }

    /**
     * Ignores any exception thrown by the {@link DatabaseConnection} parameter when closed, it also checks for
     * {@code null}.
     *
     * @param conn DatabaseConnection to rollback.
     */
    public static void rollbackQuietly(final DatabaseConnection conn) {
        if (conn != null) {
            rollbackQuietly(conn.getConnection());
        }
    }

    /**
     * Ignores any exception thrown by the {@link Connection} parameter when closed, it also checks for {@code null}.
     *
     * @param conn JDBC connection to close.
     */
    public static void closeQuietly(final Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException expected) {
            //expected
        }
    }

    /**
     * Ignores any exception thrown by the {@link Connection} parameter when closed, it also checks for {@code null}.
     *
     * @param conn DatabaseConnection to close.
     */
    public static void closeQuietly(final DatabaseConnection conn) {
        if (conn != null) {
            closeQuietly(conn.getConnection());
        }
    }

    /**
     * Ignores any exception thrown by the {@link Statement#close()} method.
     *
     * @param stmt {@link Statement} to close.
     */
    public static void closeQuietly(final Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException expected) {
            //expected
        }
    }

    /**
     * Closes the {@link ResultSet} and ignores any {@link Exception} that may be thrown by the
     * {@link ResultSet#close()} method.
     *
     * @param rset {@link ResultSet} to close quitely.
     */
    public static void closeQuietly(final ResultSet rset) {
        try {
            if (rset != null) {
                rset.close();
            }
        } catch (SQLException expected) {
            //expected
        }
    }

    /**
     * Date to string.
     *
     * @param value Date value
     * @return String value
     */
    public static String date2String(final Date value) {
        return value.toString();
    }

    /**
     * Time to String format.
     *
     * @param value Time value
     * @return String value
     */
    public static String time2String(final Time value) {
        return value.toString();
    }

    /**
     * Convert timestamp to string.
     *
     * @param value <code>Timestamp</code>
     * @return the string value
     */
    public static String timestamp2String(final Timestamp value) {
        return value.toString();
    }

    /**
     * String to Time.
     *
     * @param param String
     * @return the Time value
     */
    public static Time string2Time(final String param) {
        final DateFormat dfmt = DateFormat.getTimeInstance();
        Time parsedTime;
        try {
            parsedTime = Time.valueOf(param);
        } catch (IllegalArgumentException e) {
            // Locale parsed time, possible lost of precision
            try {
                parsedTime = new Time(dfmt.parse(param).getTime());
            } catch (ParseException pe) {
                throw new IllegalArgumentException(pe);
            }
        }
        return new Time(parsedTime.getTime());
    }

    /**
     * String to Date.
     *
     * @param param the String value
     * @return Date value
     */
    public static Date string2Date(final String param) {
        final DateFormat dfmt = DateFormat.getDateInstance();
        java.sql.Date parsedDate;
        try {
            parsedDate = Date.valueOf(param);
        } catch (IllegalArgumentException e) {
            // Wrong string, cloud be a string number
            try {
                parsedDate = new Date(Long.valueOf(param));
            } catch (NumberFormatException expected) {
                // Locale parsed date, possible lost of precision
                try {
                    parsedDate = new Date(dfmt.parse(param).getTime());
                } catch (ParseException pe) {
                    throw new IllegalArgumentException(pe);
                }
            }
        }
        return parsedDate;
    }

    /**
     * Convert string to Timestamp
     *
     * @param param String value
     * @return Timestamp value
     */
    public static Timestamp string2Timestamp(final String param) {
        final DateFormat dfmt = DateFormat.getDateTimeInstance();
        Timestamp parsedTms;
        try {
            parsedTms = Timestamp.valueOf(param);
        } catch (IllegalArgumentException e) {
            // Wrong string, cloud be a number
            try {
                parsedTms = new Timestamp(Long.valueOf(param));
            } catch (NumberFormatException expected) {
                // Locale parsed date, possible lost of precision
                try {
                    parsedTms = new Timestamp(dfmt.parse(param).getTime());
                } catch (ParseException pe) {
                    throw new IllegalArgumentException(pe);
                }
            }
        }
        return parsedTms;
    }

    /**
     * Convert String to boolean.
     *
     * @param val string value
     * @return Boolean value
     */
    public static Boolean string2Boolean(final String val) {
        if (val == null) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(val);
    }

    /**
     * The null param vlaue normalizator.
     *
     * @param sql SQL query
     * @param params list
     * @param out out param list
     * @return the modified string
     */
    public static String normalizeNullValues(final String sql, final List<SQLParam> params, final List<SQLParam> out) {
        StringBuilder ret = new StringBuilder();
        int size = (params == null) ? 0 : params.size();
        //extend for extra space
        final String sqlext = sql + " ";
        String[] values = sqlext.split("\\?");
        if (values.length != (size + 1)) {
            throw new IllegalStateException("bind.params.count.not.same");
        }
        for (int i = 0; i < values.length; i++) {
            String string = values[i];
            ret.append(string);
            if (params != null && i < params.size()) {
                final SQLParam param = params.get(i);
                if (param == null || (param.getValue() == null && param.getSqlType() == Types.NULL)) {
                    ret.append("null");
                } else {
                    ret.append("?");
                    out.add(param);
                }
            }
        }
        //return sql less the extra space
        return ret.substring(0, ret.length() - 1);
    }

    /**
     * Make a blob conversion.
     *
     * @param blobValue blob
     * @return a converted value
     * @throws SQLException if anything goes wrong
     */
    public static byte[] blob2ByteArray(final Blob blobValue) throws SQLException {
        byte[] newValue = null;
        // convert from Blob to byte[]
        InputStream is = blobValue.getBinaryStream();
        try {
            newValue = IOUtil.readInputStreamBytes(is, true);
        } catch (IOException e) {
            throw ConnectorException.wrap(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return newValue;
    }

    /**
     * Binds the "?" markers in SQL statement with the parameters given as <i>values</i>. It concentrates the
     * replacement of all params.<code>GuardedString</code> are handled so the password is never visible.
     *
     * @param statement SQL statement
     * @param params a <code>List</code> of the object arguments
     * @throws SQLException an exception in statement
     */
    public static void setParams(final PreparedStatement statement, final List<SQLParam> params) throws SQLException {
        if (statement == null || params == null) {
            return;
        }
        for (int i = 0; i < params.size(); i++) {
            final int idx = i + 1;
            final SQLParam parm = params.get(i);
            final int sqlType = parm.getSqlType();
            final SQLParam val = new SQLParam(parm.getName(), attribute2jdbcValue(parm.getValue(), sqlType), sqlType);
            setParam(statement, idx, val);
        }
    }

    /**
     * Binds the "?" markers in SQL statement with the parameters given as <i>values</i>. It concentrates the
     * replacement of all params. <code>GuardedString</code> are handled so the password is never visible.
     *
     * @param statement SQL statement
     * @param params a <code>List</code> of the object arguments
     * @throws SQLException an exception in statement
     */
    public static void setParams(final CallableStatement statement, final List<SQLParam> params) throws SQLException {
        //The same as for prepared statements
        setParams((PreparedStatement) statement, params);
    }

    /**
     * Set the statement parameter. It is ready for overloading if necessary.
     *
     * @param stmt a <code>PreparedStatement</code> to set the params
     * @param idx an index of the parameter
     * @param parm a parameter Value
     * @throws SQLException a SQL exception
     */
    static void setParam(final PreparedStatement stmt, final int idx, final SQLParam parm) throws SQLException {
        // Guarded string conversion
        if (parm.getValue() instanceof GuardedString) {
            setGuardedStringParam(stmt, idx, (GuardedString) parm.getValue());
        } else {
            setSQLParam(stmt, idx, parm);
        }
    }

    /**
     * Read one row from database result set and convert a columns to attribute set.
     *
     * @param resultSet database data
     * @return The transformed attribute set
     * @throws SQLException if anything goes wrong
     */
    public static Map<String, SQLParam> getColumnValues(final ResultSet resultSet) throws SQLException {
        Assertions.nullCheck(resultSet, "resultSet");
        Map<String, SQLParam> ret = CollectionUtil.<SQLParam>newCaseInsensitiveMap();
        final ResultSetMetaData meta = resultSet.getMetaData();
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            final String name = meta.getColumnName(i);
            final int sqlType = meta.getColumnType(i);
            final SQLParam param = getSQLParam(resultSet, i, name, sqlType);
            ret.put(name, param);
        }
        return ret;
    }

    /**
     * Retrieve the SQL value from result set.
     *
     * @param resultSet the result set
     * @param i index
     * @param name param name
     * @param sqlType expected SQL type or Types.NULL for generic
     * @return the object return the retrieved object
     * @throws SQLException any SQL error
     */
    public static SQLParam getSQLParam(
            final ResultSet resultSet, final int i, final String name, final int sqlType) throws SQLException {
        Assertions.nullCheck(resultSet, "resultSet");
        Object object;
        switch (sqlType) {
            //Known conversions
            case Types.NULL:
                object = resultSet.getObject(i);
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                object = resultSet.getBigDecimal(i);
                break;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.INTEGER:
            case Types.BIGINT:
//          object = resultSet.getDouble(i); double does not support update to null
//          object = resultSet.getFloat(i); float does not support update to null
//          object = resultSet.getInt(i); int does not support update to null
                object = resultSet.getObject(i);
                break;
            case Types.TINYINT:
                object = resultSet.getByte(i);
                break;
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                object = resultSet.getObject(i);
                break;
            case Types.TIMESTAMP:
                object = resultSet.getTimestamp(i);
                break;
            case Types.DATE:
                object = resultSet.getDate(i);
                break;
            case Types.TIME:
                object = resultSet.getTime(i);
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                object = resultSet.getBoolean(i);
                break;
            default:
                object = resultSet.getString(i);
        }
        return new SQLParam(name, object, sqlType);
    }

    /**
     * Convert database type to connector supported set of attribute types Can be redefined for different databases.
     *
     * @param sqlType #{@link Types}
     * @return a connector supported class
     */
    public static Class<?> getSQLAttributeType(final int sqlType) {
        Class<?> ret;
        switch (sqlType) {
            //Known conversions
            case Types.DECIMAL:
            case Types.NUMERIC:
                ret = BigDecimal.class;
                break;
            case Types.DOUBLE:
                ret = Double.class;
                break;
            case Types.FLOAT:
            case Types.REAL:
                ret = Float.class;
                break;
            case Types.INTEGER:
                ret = Integer.class;
                break;
            case Types.BIGINT:
                ret = Long.class;
                break;
            case Types.TINYINT:
                ret = Byte.class;
                break;
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                ret = byte[].class;
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                ret = Boolean.class;
                break;
            default:
                ret = String.class;
        }
        return ret;
    }

    /**
     * Set a parameter to statement. The conversion to required database type is expected to be done.
     *
     * @param stmt the statement to set
     * @param idx index of the parameter
     * @param parm the <CODE>SQLParam</CODE> value
     * @throws SQLException something wrong
     */
    public static void setSQLParam(
            final PreparedStatement stmt, final int idx, final SQLParam parm) throws SQLException {
        Assertions.nullCheck(stmt, "statement");
        Assertions.nullCheck(parm, "parm");
        // Handle the null value
        final int sqlType = parm.getSqlType();
        final Object val = parm.getValue();
        //Set the null value
        if (val == null) {
            stmt.setNull(idx, sqlType);
            return;
        }
        //Set the generics 
        if (sqlType == Types.NULL) {
            stmt.setObject(idx, val);
            return;
        }
        //Set specific object
        if (val instanceof BigDecimal) {
            stmt.setBigDecimal(idx, (BigDecimal) val);
        } else if (val instanceof Double) {
            stmt.setDouble(idx, (Double) val);
        } else if (val instanceof Float) {
            stmt.setFloat(idx, (Float) val);
        } else if (val instanceof Integer) {
            stmt.setInt(idx, (Integer) val);
        } else if (val instanceof Long) {
            stmt.setLong(idx, (Long) val);
        } else if (val instanceof BigInteger) {
            stmt.setLong(idx, ((BigInteger) val).longValue());
        } else if (val instanceof Byte) {
            stmt.setByte(idx, (Byte) val);
        } else if (val instanceof Integer) {
            stmt.setInt(idx, (Integer) val);
        } else if (val instanceof InputStream) {
            stmt.setBinaryStream(idx, (InputStream) val, 10000);
        } else if (val instanceof Blob) {
            stmt.setBlob(idx, (Blob) val);
        } else if (val instanceof byte[]) {
            stmt.setBytes(idx, (byte[]) val);
        } else if (val instanceof Timestamp) {
            stmt.setTimestamp(idx, (Timestamp) val);
        } else if (val instanceof java.sql.Date) {
            stmt.setDate(idx, (java.sql.Date) val);
        } else if (val instanceof java.sql.Time) {
            stmt.setTime(idx, (java.sql.Time) val);
        } else if (val instanceof Boolean) {
            stmt.setBoolean(idx, (Boolean) val);
        } else if (val instanceof String) {
            stmt.setString(idx, (String) val);
        } else {
            stmt.setObject(idx, val);
        }
    }

    /**
     * The conversion to required attribute type.
     *
     * @param value to be converted to an attribute
     * @throws SQLException something is not ok
     * @return a attribute's supported object
     */
    public static Object jdbc2AttributeValue(final Object value) throws SQLException {
        Object ret = null;
        if (value == null) {
            return ret;
        }
        if (value instanceof Blob) {
            ret = blob2ByteArray((Blob) value);
        } else if (value instanceof java.sql.Timestamp) {
            ret = timestamp2String((java.sql.Timestamp) value);
        } else if (value instanceof java.sql.Time) {
            ret = time2String((java.sql.Time) value);
        } else if (value instanceof java.sql.Date) {
            ret = date2String((java.sql.Date) value);
        } else if (value instanceof java.util.Date) {
            //convert date to String
            ret = ((java.util.Date) value).toString();
            /* } else if (value instanceof Long) {
             * ret = value;
             * } else if (value instanceof Character) {
             * ret = value;
             * } else if (value instanceof Double) {
             * ret = value;
             * } else if (value instanceof Float) {
             * ret = value;
             * } else if (value instanceof Integer) {
             * ret = value;
             * } else if (value instanceof Boolean) {
             * ret = value;
             * } else if (value instanceof Byte[]) {
             * ret = value;
             * } else if (value instanceof BigDecimal) {
             * ret = value;
             * } else if (value instanceof BigInteger) {
             * ret = value; */
        } else {
            // converted to string leads to error in contract tests
            // TODO figure out, which type fail. It could be Character[] 
            ret = value;
        }
        return ret;
    }

    /**
     * Convert the attribute to expected jdbc type using java conversions Some database strategy sets all attributes as
     * string, other convert them first and than set as native.
     *
     * @param value the value to be converted
     * @param sqlType the target sql type
     * @return the converted object value
     * @throws SQLException any SQL error
     */
    public static Object attribute2jdbcValue(final Object value, int sqlType) throws SQLException {
        if (value == null) {
            return null;
        }
        switch (sqlType) {
            //Known conversions
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.DOUBLE:
                if (value instanceof BigDecimal) {
                    return value;
                } else if (value instanceof Double) {
                    return value;
                } else if (value instanceof Float) {
                    return value;
                } else if (value instanceof String) {
                    return Double.valueOf((String) value);
                } else {
                    return Double.valueOf(value.toString());
                }
            case Types.FLOAT:
            case Types.REAL:
                if (value instanceof BigDecimal) {
                    return value;
                } else if (value instanceof Float) {
                    return value;
                } else if (value instanceof Double) {
                    return value;
                } else if (value instanceof String) {
                    return Float.valueOf((String) value);
                } else {
                    return Float.valueOf(value.toString());
                }
            case Types.INTEGER:
            case Types.BIGINT:
                if (value instanceof BigInteger) {
                    return value;
                } else if (value instanceof Long) {
                    return value;
                } else if (value instanceof Integer) {
                    return value;
                } else if (value instanceof String) {
                    return Long.valueOf((String) value);
                } else {
                    return Long.valueOf(value.toString());
                }
            case Types.TIMESTAMP:
                if (value instanceof String) {
                    return string2Timestamp((String) value);
                }
                break;
            case Types.DATE:
                if (value instanceof String) {
                    return string2Date((String) value);
                }
                break;
            case Types.TIME:
                if (value instanceof String) {
                    return string2Time((String) value);
                }
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                if (value instanceof String) {
                    return string2Boolean((String) value);
                }
                break;
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.CHAR:
                if (value instanceof String) {
                    return value;
                }
                return value.toString();
        }
        return value;
    }

    /**
     * The helper guardedString bind method.
     *
     * @param stmt to bind to
     * @param idx index of the object
     * @param guard a <CODE>GuardedString</CODE> parameter
     * @throws SQLException any SQL error
     */
    public static void setGuardedStringParam(final PreparedStatement stmt, final int idx, final GuardedString guard)
            throws SQLException {
        try {
            guard.access(new GuardedString.Accessor() {

                @Override
                public void access(char[] clearChars) {
                    try {
                        //Never use setString, the DB2 database will fail for secured columns
                        stmt.setObject(idx, new String(clearChars));
                    } catch (SQLException e) {
                        // checked exception are not allowed in the access method 
                        // Lets use the exception softening pattern
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (RuntimeException e) {
            // determine if there's a SQLException and re-throw that..
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Selects single value (first column) from select. It fetches only first row, does not check whether more rows are
     * returned by select. If no row is returned, returns null
     *
     * @param conn JDBC connection
     * @param sql Select statement with or without parameters
     * @param params Parameters to use in statement
     * @return first row and first column value
     * @throws SQLException any SQL error
     */
    public static Object selectSingleValue(final Connection conn, final String sql, final SQLParam... params)
            throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(sql);
            setParams(st, Arrays.asList(params));
            rs = st.executeQuery();
            Object value;
            if (rs.next()) {
                //If needed , switch to getSQLParam
                value = rs.getObject(1);
                return value;
            }
            return null;
        } finally {
            closeQuietly(rs);
            closeQuietly(st);
        }
    }

    /**
     * Selects all rows from select. It uses {@link ResultSet#getMetaData()} to find columns count and use
     * {@link ResultSet#getObject(int)} to retrieve column value.
     *
     * @param conn JDBC connection
     * @param sql SQL select with or without params
     * @param params SQL parameters
     * @return list of selected rows
     * @throws SQLException any SQL error
     */
    public static List<Object[]> selectRows(final Connection conn, final String sql, final SQLParam... params)
            throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        List<Object[]> rows = new ArrayList<Object[]>();
        try {
            st = conn.prepareStatement(sql);
            setParams(st, Arrays.asList(params));
            rs = st.executeQuery();
            final ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Object[] row = new Object[metaData.getColumnCount()];
                for (int i = 0; i < row.length; i++) {
                    final SQLParam param = getSQLParam(rs, i + 1, metaData.getColumnName(i + 1), metaData.getColumnType(
                            i + 1));
                    row[i] = jdbc2AttributeValue(param.getValue());
                }
                rows.add(row);
            }
            return rows;
        } finally {
            closeQuietly(rs);
            closeQuietly(st);
        }
    }

    /**
     * Executes DML sql statement. This can be useful to execute insert/update/delete or some database specific
     * statement in one call
     *
     * @param conn connection
     * @param sql SQL query
     * @param params SQL parameters
     * @return number of rows affected as defined by {@link PreparedStatement#executeUpdate()}
     * @throws SQLException any SQL error
     */
    public static int executeUpdateStatement(final Connection conn, final String sql, final SQLParam... params)
            throws SQLException {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(sql);
            setParams(st, Arrays.asList(params));
            return st.executeUpdate();
        } finally {
            closeQuietly(st);
        }
    }
}
