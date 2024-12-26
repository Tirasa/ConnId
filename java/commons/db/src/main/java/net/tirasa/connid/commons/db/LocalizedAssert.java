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

import org.identityconnectors.framework.common.objects.ConnectorMessages;

/**
 * Localized asserts is a set of localized asserts utility method that throws localized
 * exception when assert is not true.
 * Argument names passed into assert methods can also be localized
 *
 * @author kitko
 *
 */
public class LocalizedAssert {

    private ConnectorMessages cm;

    private boolean localizeArguments;

    /**
     * Creates asserts with messages
     *
     * @param cm connector messages
     * @throws IllegalArgumentException if cm param is null
     */
    public LocalizedAssert(ConnectorMessages cm) {
        if (cm == null) {
            throw new IllegalArgumentException("ConnectorMessages argument is null");
        }
        this.cm = cm;
    }

    /**
     * Creates asserts with messages with flag whether to localize argument names
     *
     * @param cm connector messages
     * @param localizeArguments the arg
     * @throws IllegalArgumentException if cm param is null
     */
    public LocalizedAssert(ConnectorMessages cm, boolean localizeArguments) {
        if (cm == null) {
            throw new IllegalArgumentException("ConnectorMessages argument is null");
        }
        this.cm = cm;
        this.localizeArguments = localizeArguments;
    }

    private void throwException(String locKey, String argument) {
        if (localizeArguments) {
            argument = cm.format(argument, argument);
        }
        String msg = cm.format(locKey, null, argument);
        throw new IllegalArgumentException(msg);
    }

    /**
     * Asserts the argument is not null. If argument is null, throws localized IllegalArgumentException.
     *
     * @param <T> type of the object to check
     * @param o object to check
     * @param argument exception message
     * @return original obkect
     */
    public <T> T assertNotNull(T o, String argument) {
        if (o == null) {
            throwException(DBMessages.ASSERT_NOT_NULL, argument);
        }
        return o;
    }

    /**
     * Asserts the argument is null. If argument is not null, throws localized IllegalArgumentException.
     *
     * @param <T> type of the object to check
     * @param o object to check
     * @param argument exception message
     * @return original obkect
     */
    public <T> T assertNull(T o, String argument) {
        if (o != null) {
            throwException(DBMessages.ASSERT_NULL, argument);
        }
        return o;
    }

    /**
     * Asserts that argument string is not blank. If argument is null or blank, throws localized
     * IllegalArgumentException
     *
     * @param o object to check
     * @param argument exception message
     * @return original obkect
     */
    public String assertNotBlank(String o, String argument) {
        if (o == null || o.length() == 0) {
            throwException(DBMessages.ASSERT_NOT_BLANK, argument);
        }
        return o;
    }

    /**
     * Asserts that argument string is blank. If argument is not blank, throws localized
     * IllegalArgumentException
     *
     * @param s string to check
     * @param argument exception message
     * @return original obkect
     */
    public String assertBlank(String s, String argument) {
        if (s != null && s.length() > 0) {
            throwException(DBMessages.ASSERT_BLANK, argument);
        }
        return s;
    }
}
