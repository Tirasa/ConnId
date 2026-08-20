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

import java.sql.Types;

/**
 * The SQL parameter / util class
 *
 * @version $Revision 1.0$
 * @since 1.0
 */
public final class SQLParam {

    private String name;

    private Object value;

    private int sqlType;

    private final String quotedName;

    /**
     * The Sql param is a pair of value and its sqlType
     *
     * @param name name of the attribute
     * @param value value
     * @param sqlType sql type
     */
    public SQLParam(String name, Object value, int sqlType) {
        this(name, value, sqlType, name);
    }

    /**
     * The Sql param is a pair of value and its sqlType
     *
     * @param name name of the attribute
     * @param value value
     * @param sqlType sql type
     * @param quotedName quoted name
     */
    public SQLParam(String name, Object value, int sqlType, String quotedName) {
        if (name == null || name.length() == 0) {
            //TODO localize this
            throw new IllegalArgumentException("SQL param name should be not null");
        }
        this.name = name;
        this.value = value;
        this.sqlType = sqlType;
        this.quotedName = quotedName;
    }

    /**
     * The Sql param is a pair of value and its sqlType
     *
     * @param name name of the attribute
     * @param value value
     */
    public SQLParam(String name, Object value) {
        this(name, value, Types.NULL, name);
    }

    /**
     * Accessor for the quoted name property
     *
     * @return the _name
     */
    public String getQuotedName() {
        return quotedName;
    }

    /**
     * Accessor for the name property
     *
     * @return the _name
     */
    public String getName() {
        return name;
    }

    /**
     * The param value
     *
     * @return a value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sql Type
     *
     * @return a type
     */
    public int getSqlType() {
        return sqlType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        SQLParam other = (SQLParam) obj;
        return ((name == null ? other.name == null : name.equals(other.name))
                || (name != null && name.equals(other.name)))
                && (value == other.value || (value != null && value.equals(other.value)))
                && sqlType == other.sqlType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == name ? 0 : name.hashCode());
        hash = 31 * hash + (null == value ? 0 : value.hashCode());
        hash = 31 * hash + sqlType;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (getName() != null) {
            ret.append(getName());
            ret.append("=");
        }
        ret.append("\"").append(getValue()).append("\"");
        switch (getSqlType()) {
            case Types.ARRAY ->
                ret.append(":[ARRAY]]");
            case Types.BIGINT ->
                ret.append(":[BIGINT]");
            case Types.BINARY ->
                ret.append(":[BINARY]");
            case Types.BIT ->
                ret.append(":[BIT]");
            case Types.BLOB ->
                ret.append(":[BLOB]");
            case Types.BOOLEAN ->
                ret.append(":[BOOLEAN]");
            case Types.CHAR ->
                ret.append(":[CHAR]");
            case Types.CLOB ->
                ret.append(":[CLOB]");
            case Types.DATALINK ->
                ret.append(":[DATALINK]");
            case Types.DATE ->
                ret.append(":[DATE]");
            case Types.DECIMAL ->
                ret.append(":[DECIMAL]");
            case Types.DISTINCT ->
                ret.append(":[DISTINCT]");
            case Types.DOUBLE ->
                ret.append(":[DOUBLE]");
            case Types.FLOAT ->
                ret.append(":[FLOAT]");
            case Types.INTEGER ->
                ret.append(":[INTEGER]");
            case Types.JAVA_OBJECT ->
                ret.append(":[JAVA_OBJECT]");
            case Types.LONGVARBINARY ->
                ret.append(":[LONGVARBINARY]");
            case Types.LONGVARCHAR ->
                ret.append(":[LONGVARCHAR]");
            case Types.NULL -> {
            }
            case Types.NUMERIC ->
                ret.append(":[NUMERIC]");
            case Types.OTHER ->
                ret.append(":[OTHER]");
            case Types.REAL ->
                ret.append(":[REAL]");
            case Types.REF ->
                ret.append(":[REF]");
            case Types.SMALLINT ->
                ret.append(":[SMALLINT]");
            case Types.STRUCT ->
                ret.append(":[STRUCT]");
            case Types.TIME ->
                ret.append(":[TIME]");
            case Types.TIMESTAMP ->
                ret.append(":[TIMESTAMP]");
            case Types.TINYINT ->
                ret.append(":[TINYINT]");
            case Types.VARBINARY ->
                ret.append(":[VARBINARY]");
            case Types.VARCHAR ->
                ret.append(":[VARCHAR]");
            default ->
                ret.append(":[SQL Type:").append(getSqlType()).append("]");
        }
        return ret.toString();
    }
}
