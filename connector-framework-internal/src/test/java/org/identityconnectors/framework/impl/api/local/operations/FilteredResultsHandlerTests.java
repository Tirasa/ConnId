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

import static org.testng.Assert.assertEquals;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.impl.api.Searches.ConnectorObjectSearch;
import org.identityconnectors.test.common.ToListResultsHandler;
import org.testng.annotations.Test;

public class FilteredResultsHandlerTests {
    @Test
    public void withPassThruFilter() {

        final int EXPECTED = 1000;
        ConnectorObjectSearch data = new ConnectorObjectSearch(EXPECTED);
        ToListResultsHandler results = new ToListResultsHandler();
        data.search(ObjectClass.ACCOUNT, null, new FilteredResultsHandler(results,
                new FilteredResultsHandler.PassThruFilter()), null);
        int actual = 0;
        for (ConnectorObject obj : results.getObjects()) {
            // check that we limit expected values..
            for (Attribute attr : obj.getAttributes()) {
                if (attr.is("count")) {
                    int idx = (Integer) attr.getValue().get(0);
                    assertEquals(actual, idx);
                }
            }
            actual++;
        }
        assertEquals(actual, EXPECTED);
    }

    @Test
    public void withNullFilter() {
        final int EXPECTED = 1000;
        ConnectorObjectSearch data = new ConnectorObjectSearch(EXPECTED);
        ToListResultsHandler results = new ToListResultsHandler();
        data.search(ObjectClass.ACCOUNT, null, new FilteredResultsHandler(results, null), null);
        int actual = 0;
        for (ConnectorObject obj : results.getObjects()) {
            // check that we limit expected values..
            for (Attribute attr : obj.getAttributes()) {
                if (attr.is("count")) {
                    int idx = (Integer) attr.getValue().get(0);
                    assertEquals(actual, idx);
                }
            }
            actual++;
        }
        assertEquals(actual, EXPECTED);
    }

    @Test
    public void withRangeFilter() {
        final int DATA = 1000;
        final long EXPECTED_LOW = 100;
        final long EXPECTED_HIGH = 200;
        ConnectorObjectSearch data = new ConnectorObjectSearch(DATA);
        ToListResultsHandler results = new ToListResultsHandler();
        data.search(ObjectClass.ACCOUNT, null, new FilteredResultsHandler(results, new RangeFilter(
                EXPECTED_LOW, EXPECTED_HIGH)), null);
        long actual = EXPECTED_LOW;
        for (ConnectorObject obj : results.getObjects()) {
            // check that we limit expected values..
            for (Attribute attr : obj.getAttributes()) {
                if (attr.is("count")) {
                    int idx = (Integer) attr.getValue().get(0);
                    assertEquals(actual, idx);
                }
            }
            actual++;
        }
        assertEquals(actual - EXPECTED_LOW, EXPECTED_HIGH - EXPECTED_LOW);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullProducer() {
        new FilteredResultsHandler(null, new RangeFilter(0, 100));

    }

    /**
     * Basic filter depends on the 'count' attribute. We're testing that we can
     * filter the producer output.
     */
    static class RangeFilter implements Filter {
        final long low, high;

        public RangeFilter(long low, long high) {
            this.low = low;
            this.high = high;
        }

        public boolean accept(ConnectorObject obj) {
            boolean ret = false;
            Attribute attr = obj.getAttributeByName("count");
            if (attr != null) {
                int value = (Integer) attr.getValue().get(0);
                if (value >= this.low && value < this.high) {
                    ret = true;
                }
            }
            return ret;
        }
    }
}
