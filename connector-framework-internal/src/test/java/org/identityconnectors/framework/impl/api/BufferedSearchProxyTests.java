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
package org.identityconnectors.framework.impl.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.testng.annotations.Test;

public class BufferedSearchProxyTests {

    private static class ExpectedTestResults implements ResultsHandler
    {
        private int _count;
        private List<ResultsHandler> _resultsHandlers
                = new ArrayList<ResultsHandler>();
        public ExpectedTestResults()
        {
        }
        public void addExpectedResult(ResultsHandler handler)
        {
            _resultsHandlers.add(handler);
        }
        public void addExpectedRange(int start, int size)
        {
            for ( int i = 0; i < size; i++ ) {
                addExpectedResult(new CheckCountHandler(i+start));
            }
        }

        public boolean handle(ConnectorObject object) {
            if (_count >= _resultsHandlers.size()) {
                fail("Unpextected number of results: " + _count);
            }
            boolean rv = _resultsHandlers.get(_count).handle(object);
            _count++;
            return rv;
        }
        public void assertFinished() {
            assertEquals(_resultsHandlers.size(), _count);
        }
    }

    private static class StopResultsHandler implements ResultsHandler {
        private ResultsHandler _target;
        public StopResultsHandler(ResultsHandler target) {
            _target = target;
        }
        public boolean handle(ConnectorObject object) {
            if (_target != null)
                _target.handle(object);
            return false;
        }
    }

    private static class CheckCountHandler implements ResultsHandler {
        private final int _expectedCount;
        public CheckCountHandler(int expectedCount) {
            _expectedCount = expectedCount;
        }
        public boolean handle(ConnectorObject object) {
            assertEquals(object.getAttributeByName("count").getValue().get(0), _expectedCount);
            return true;
        }
    }

    @Test
    public void withBuffer() {
        // test the limit on a range..
        for (int i = 0; i < 200; i++) {

            ExpectedTestResults expected = new ExpectedTestResults();
            expected.addExpectedRange(0, i);

            SearchApiOp search = new Searches.ConnectorObjectSearch(i);
            SearchApiOp proxy = createSearchProxy(search,i+1,50000);
            proxy.search(ObjectClass.ACCOUNT, null, expected,null);
            expected.assertFinished();
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidSearch() {
        new BufferedResultsProxy(null, 1, 2);
    }

    @Test
    public void throwTimeoutException() {
        ExpectedTestResults expected = new ExpectedTestResults();
        SearchApiOp search = new Searches.WaitObjectSearch(10,1000);
        SearchApiOp proxy = createSearchProxy(search,10+1,20);

        try {
            proxy.search(ObjectClass.ACCOUNT, null, expected,null);
            fail("Should throw a IllegalState/TimeoutException??");
        } catch (OperationTimeoutException e) {
        }
        expected.assertFinished();
    }

    @Test
    public void testCancel() {
        ExpectedTestResults expected = new ExpectedTestResults();
        expected.addExpectedRange(0, 5);
        expected.addExpectedResult(new StopResultsHandler(new CheckCountHandler(5)));
        SearchApiOp search = new Searches.WaitObjectSearch(10,10);
        SearchApiOp proxy = createSearchProxy(search,10+1,20000);

        proxy.search(ObjectClass.ACCOUNT, null, expected, null);
        expected.assertFinished();
    }

    @Test
    public void passException() {
        ExpectedTestResults expected = new ExpectedTestResults();
        expected.addExpectedRange(0, 5);
        SearchApiOp search = new Searches.ThrowsExceptionSearch(10,5,new IllegalArgumentException());
        SearchApiOp proxy = createSearchProxy(search,10+1,20000);
        try {
            proxy.search(ObjectClass.ACCOUNT, null, expected,null);
            fail("expected exception");
        }
        catch (IllegalArgumentException e) {

        }
        expected.assertFinished();
    }

    @Test
    public void slowProducer() {
        // test the limit on a range..
        for (int i = 0; i < 5; i++) {
            // confirm the correct count..
            ExpectedTestResults expected = new ExpectedTestResults();
            expected.addExpectedRange(0, i);
            SearchApiOp search = new Searches.WaitObjectSearch(i, 100);
            SearchApiOp proxy = createSearchProxy(search, i + 1, 1000);
            proxy.search(ObjectClass.ACCOUNT, null, expected, null);
            expected.assertFinished();
        }
    }

    private static SearchApiOp createSearchProxy(SearchApiOp search, int bufSize, long timeout) {
        BufferedResultsProxy timeoutHandler = new BufferedResultsProxy(search, bufSize, timeout);
        return (SearchApiOp)Proxy.newProxyInstance(SearchApiOp.class.getClassLoader(),
                new Class[]{SearchApiOp.class},
                timeoutHandler);

    }
}
