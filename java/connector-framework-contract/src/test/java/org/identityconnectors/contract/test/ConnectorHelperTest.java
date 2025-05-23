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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.contract.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.identityconnectors.common.CollectionUtil;
import org.junit.jupiter.api.Test;

public class ConnectorHelperTest {

    @Test
    public void testCheckValue() {
        // no exception should be thrown
        assertTrue(ConnectorHelper.checkValue(CollectionUtil.newList("foo", "bar", "baz"),
                CollectionUtil.newList("foo", "bar")));
        assertFalse(ConnectorHelper.checkValue(CollectionUtil.newList("foo", "baz"), CollectionUtil
                .newList("foo", "bar")));

        // byte array comparison
        byte[] barr1 = { 10, 11, 12 };
        byte[] barr2 = { 10, 10, 12 };
        byte[] barr3 = { 10, 10, 10 };
        List<Object> fetchedValue = CollectionUtil.<Object>newList(barr1, barr2, barr3);
        List<Object> requestedValue = CollectionUtil.<Object>newList(barr1, barr3);
        assertTrue(ConnectorHelper.checkValue(fetchedValue, requestedValue));

        // Collections in value with duplicate values shouldn't be equal
        // For example ['a','a','b'] != ['a','b']
        assertFalse(ConnectorHelper.checkValue(CollectionUtil.newList("foo", "bar"), // fetched
                CollectionUtil.newList("foo", "bar", "bar") // requested
        ));

        assertTrue(ConnectorHelper.checkValue(CollectionUtil.newList("foo", "bar", "bar"), // fetched
                CollectionUtil.newList("foo", "bar") // requested
        ));

        // match should be indifferent for order of values
        assertTrue(ConnectorHelper.checkValue(CollectionUtil.newList("baz", "bar", "foo"), // fetched
                CollectionUtil.newList("foo", "bar") // requested
        ));

        // identical lists should pass:
        List<String> sameList = CollectionUtil.newList("a", "b", "b");
        assertTrue(ConnectorHelper.checkValue(sameList, sameList));
        assertTrue(ConnectorHelper.checkValue(sameList, CollectionUtil.newList("b", "a", "b")));
    }
}
