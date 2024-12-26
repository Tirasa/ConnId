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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.identityconnectors.common.Pair;
import org.identityconnectors.common.StringUtil;

/**
 * The Database Query builder creates the database query.
 * <p>
 * The main functionality of this helper class is create SQL query statement
 * with bundled object references</p>
 *
 * @version $Revision 1.0$
 * @since 1.0
 */
public class DatabaseQueryBuilder {

    private String selectFrom = null; //Mandatory selectFrom clause

    private String tableName = null; //Mandatory selectFrom clause

    private FilterWhereBuilder where = null;

    private Set<String> columns = new HashSet<String>();

    private List<OrderBy> orderBy = null;

    /**
     * Set the columnNames to get
     *
     * @param columns the required columns in SQL query
     */
    public void setColumns(Set<String> columns) {
        this.columns = columns;
    }

    /**
     * Set selectFrom and from clause
     *
     * @param selectFrom the selectFrom part including the from table
     */
    public void setSelectFrom(String selectFrom) {
        this.selectFrom = selectFrom;
    }

    /**
     * Set the table name
     *
     * @param tableName name of the table
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * set the where builder
     *
     * @param whereBuilder {@link FilterWhereBuilder} the where filer builder
     */
    public void setWhere(FilterWhereBuilder whereBuilder) {
        this.where = whereBuilder;
    }

    /**
     * sET THE ORDER BY CLAUSE
     *
     * @param orderBy a list of {@link Pair} pair as colunName and sort order
     */
    public void setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * DatabaseQuery Constructor, construct selectFrom from table name, columns and where clause
     *
     * @param tableName The name of the database table to selectFrom from
     * @param columns the names of the column to be in the result
     */
    public DatabaseQueryBuilder(String tableName, Set<String> columns) {
        if (StringUtil.isBlank(tableName)) {
            throw new IllegalArgumentException("the tableName must not be null or empty");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("CoulmnNamesToGet must not be empty");
        }
        this.tableName = tableName;
        this.columns = columns;
    }

    /**
     * DatabaseQuery Constructor which takes advantage of prepared selectFrom SQL clause
     *
     * @param selectFrom mandatory selectFrom clause
     */
    public DatabaseQueryBuilder(final String selectFrom) {
        if (StringUtil.isBlank(selectFrom)) {
            throw new IllegalArgumentException("the selectFrom clause must not be empty");
        }
        this.selectFrom = selectFrom;
    }

    /**
     * Return full sql statement string
     *
     * @return Sql query statement to execute
     */
    public String getSQL() {
        if (StringUtil.isBlank(selectFrom)) {
            if (StringUtil.isBlank(tableName)) {
                throw new IllegalArgumentException("the tableName must not be null or empty");
            }
            if (columns != null) {
                selectFrom = createSelect(columns);
            }
            if (StringUtil.isBlank(selectFrom)) {
                throw new IllegalArgumentException("the selectFrom clause must not be empty");
            }
        } else {
            if (!selectFrom.toUpperCase().contains("SELECT")) {
                throw new IllegalArgumentException("the required SELECt clause is missing");
            }
            if (!selectFrom.toUpperCase().contains("FROM")) {
                throw new IllegalArgumentException("the required FROM clause is missing");
            }
        }
        String ret = selectFrom;
        if (where != null) {
            final String whereSql = where.getWhereClause();
            if (!StringUtil.isBlank(whereSql)) {
                ret = whereAnd(selectFrom, whereSql);
            }
        }
        if (this.orderBy != null) {
            StringBuilder obld = new StringBuilder(" ORDER BY ");
            boolean first = true;
            for (OrderBy ord : orderBy) {
                if (!first) {
                    obld.append(", ");
                }
                first = false;
                obld.append(ord.getColumnName());
                obld.append(ord.isAscendent() ? " ASC" : " DESC");
            }
            if (obld.length() != 0) {
                ret += obld.toString();
            }
        }
        return ret;
    }

    /**
     * @param sqlSelect
     * @param whereAnd
     * @return
     */
    private String whereAnd(String sqlSelect, String whereAnd) {
        int iofw = sqlSelect.indexOf("WHERE");
        return (iofw == -1) ? sqlSelect + " WHERE " + whereAnd : sqlSelect.substring(0, iofw) + "WHERE (" + sqlSelect.
                substring(iofw + 5) + ") AND ( " + whereAnd + " )";
    }

    /**
     * @param columns
     * @param columnQuote
     * @return the selectFrom statement
     */
    private String createSelect(final Set<String> columnNamesToGet) {
        if (columnNamesToGet.isEmpty()) {
            throw new IllegalStateException("No coulmnNamesToGet");
        }
        StringBuilder ret = new StringBuilder("SELECT ");
        boolean first = true;
        for (String name : columnNamesToGet) {
            if (!first) {
                ret.append(", ");
            }
            ret.append(name);
            ret.append(" ");
            first = false;
        }
        ret.append("FROM ");
        ret.append(tableName);
        return ret.toString();
    }

    /**
     * Values in wrapped object
     *
     * @return the where values
     */
    public List<SQLParam> getParams() {
        if (where == null) {
            return new ArrayList<SQLParam>();
        }
        return where.getParams();
    }

    /**
     * The Required order by data subclass
     */
    public static class OrderBy extends Pair<String, Boolean> {

        /**
         * One order by column
         *
         * @param columnName column name
         * @param asc true/false for ascendent/descendent
         */
        public OrderBy(String columnName, Boolean asc) {
            super(columnName, asc);
        }

        /**
         * The column name
         *
         * @return a name
         */
        public String getColumnName() {
            return this.first;
        }

        /**
         * The ascendent flag
         *
         * @return a boolean true/false as ascendent/descendent
         */
        public boolean isAscendent() {
            return this.second;
        }
    }
}
