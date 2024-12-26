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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.Configuration;

/**
 * The DatabaseConnection wraps the JDBC connection.
 *
 * Define the test method meaning the wrapped connection is still valid
 *
 * Defines come useful method to work with prepared statements *
 */
public class DatabaseConnection {

    /**
     * Setup logging for the {@link DatabaseConnection}.
     */
    private static final Log LOG = Log.getLog(DatabaseConnection.class);

    /**
     * Internal JDBC connection.
     */
    private Connection nativeConn = null;

    /**
     *
     * Test constructor *
     */
    DatabaseConnection() {
        //No body
    }

    /**
     * Use the {@link Configuration} passed in to immediately connect to a database. If the {@link Connection} fails a
     * {@link RuntimeException} will be thrown.
     *
     * @param conn a connection
     * @throws RuntimeException if there is a problem creating a {@link java.sql.Connection}.
     */
    public DatabaseConnection(final Connection conn) {
        this.nativeConn = conn;
    }

    /**
     * Closes the internal {@link java.sql.Connection}.
     */
    public void dispose() {
        SQLUtil.closeQuietly(nativeConn);
    }

    /**
     * Determines if the underlying JDBC {@link java.sql.Connection} is valid.
     *
     * @throws RuntimeException if the underlying JDBC {@link java.sql.Connection} is not valid otherwise do nothing.
     */
    public void test() {
        try {
            // setAutoCommit() requires a connection to the server
            // in most cases. But some drivers may cache the autoCommit
            // value and only connect to the server if the value changes.
            // (namely DB2). So we have to actually change the value twice
            // and then set it back to the original value if the connection
            // is still valid. setAutoCommit() is very quick so 2 round
            // trips shouldn't be that bad.
            // This has the BAD side effect of actually causing preceding
            // partial transactions to be committed at this point. Also,
            // PostgreSQL apparently caches BOTH operations, so this still
            // does not work against that DB.
            getConnection().setAutoCommit(!getConnection().getAutoCommit());
            getConnection().setAutoCommit(!getConnection().getAutoCommit());
            commit();
            LOG.ok("connection tested");
        } catch (Exception e) {
            // anything, not just SQLException
            // if the connection is not valid anymore,
            // a new one will be created, so there is no
            // need to set auto commit back to its original value
            SQLUtil.rollbackQuietly(getConnection());
            throw ConnectorException.wrap(e);
        }
    }

    /**
     * Get the internal JDBC connection.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return this.nativeConn;
    }

    /**
     * Set the internal JDBC connection.
     *
     * @param connection new connection
     */
    public void setConnection(final Connection connection) {
        this.nativeConn = connection;
    }

    /**
     * Indirect call of prepare statement with mapped prepare statement parameters
     *
     * @param sql a <CODE>String</CODE> sql statement definition
     * @param params the bind parameter values
     * @return return a prepared statement
     * @throws SQLException an exception in statement
     */
    public PreparedStatement prepareStatement(final String sql, final List<SQLParam> params) throws SQLException {
        LOG.ok("prepareStatement: statement {0}", sql);
        final List<SQLParam> out = new ArrayList<SQLParam>();
        final String nomalized = SQLUtil.normalizeNullValues(sql, params, out);
        final PreparedStatement prepareStatement = getConnection().prepareStatement(nomalized);
        SQLUtil.setParams(prepareStatement, out);
        LOG.ok("prepareStatement: normalizzed statement {0} prepared", nomalized);
        return prepareStatement;
    }

    /**
     *
     * Indirect call of prepare statement using the query builder object
     *
     * @param query DatabaseQueryBuilder query
     *
     * @return return a prepared statement
     *
     * @throws SQLException an exception in statement
     *
     */
    public PreparedStatement prepareStatement(final DatabaseQueryBuilder query) throws SQLException {
        final String sql = query.getSQL();
        LOG.ok("prepareStatement {0}", sql);
        return prepareStatement(sql, query.getParams());
    }

    /**
     *
     * Indirect call of prepareCall statement with mapped callable statement parameters
     *
     * @param sql a <CODE>String</CODE> sql statement definition
     *
     * @param params the bind parameter values
     *
     * @return return a callable statement
     *
     * @throws SQLException an exception in statement
     *
     */
    public CallableStatement prepareCall(final String sql, final List<SQLParam> params) throws SQLException {
        LOG.ok("normalize call statement {0}", sql);
        final List<SQLParam> out = new ArrayList<SQLParam>();
        final String nomalized = SQLUtil.normalizeNullValues(sql, params, out);
        final CallableStatement prepareCall = getConnection().prepareCall(nomalized);
        SQLUtil.setParams(prepareCall, out);
        LOG.ok("call statement {0} normalizead and prepared", nomalized);
        return prepareCall;
    }

    /**
     *
     * commit transaction
     *
     */
    public void commit() {
        try {
            getConnection().commit();
        } catch (SQLException e) {
            SQLUtil.rollbackQuietly(getConnection());
            LOG.error(e, "error in commit");
            throw ConnectorException.wrap(e);
        }
    }
}
