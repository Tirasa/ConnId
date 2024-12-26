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
import java.util.List;
import org.identityconnectors.common.CollectionUtil;

/**
 * The update set builder create the database update statement.
 * <p>
 * The main functionality is create set part of update statement from Attribute set</p>
 *
 * @version $Revision 1.0$
 * @since 1.0
 */
public class InsertIntoBuilder extends OperationBuilder {

    private final List<SQLParam> params = new ArrayList<SQLParam>();

    private final StringBuilder into = new StringBuilder();

    private final StringBuilder values = new StringBuilder();

    private boolean first = true;

    /**
     * Add column name and value pair
     *
     * @param param parameter
     * @return self, builder pattern
     */
    @Override
    public InsertIntoBuilder addBind(final SQLParam param) {
        if (!first) {
            into.append(", ");
            values.append(", ");
        }
        into.append(param.getName());
        values.append("?");
        params.add(param);
        first = false;
        return this;
    }

    /**
     * Build the into
     *
     * @return The SQL part
     */
    public String getInto() {
        return into.toString();
    }

    /**
     * Build the values
     *
     * @return The SQL part
     */
    public String getValues() {
        return values.toString();
    }

    /**
     * @return the param values
     */
    public List<SQLParam> getParams() {
        return CollectionUtil.asReadOnlyList(params);
    }
}
