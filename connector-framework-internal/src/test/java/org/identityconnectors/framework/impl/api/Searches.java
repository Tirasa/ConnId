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

import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.filter.Filter;

public class Searches {

    static class EmptySearch implements SearchApiOp {

        public void search(final ObjectClass objectClass, final Filter filter,
                final ResultsHandler handler, final OperationOptions options) {

        }
    }

    public static class ConnectorObjectSearch implements SearchApiOp {

        /**
         * Amount of data to produce.
         */
        final int limit;

        public ConnectorObjectSearch(int limit) {
            this.limit = limit;
        }

        public void search(final ObjectClass objectClass, final Filter filter,
                final ResultsHandler handler, OperationOptions options) {
            for (int i = 0; i < limit; i++) {
                beforeObject(i);
                ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
                bld.setUid(Integer.toString(i));
                bld.setObjectClass(objectClass);
                bld.setName(Integer.toString(i));
                bld.addAttribute("count", i);
                ConnectorObject obj = bld.build();
                if (!handler.handle(obj)) {
                    break;
                }
            }
        }

        protected void beforeObject(int count) {

        }
    }

    public static class WaitObjectSearch extends ConnectorObjectSearch {

        /**
         * Time to wait between objects or 0 for no wait
         */
        final long wait;

        public WaitObjectSearch(int limit, long wait) {
            super(limit);
            this.wait = wait;
        }

        @Override
        protected void beforeObject(int count) {
            long wait = getCurrentWait(count);
            if (wait != 0) {
                try {
                    Thread.sleep(wait);
                } catch (Exception e) {
                    /* ignore */
                }
            }
        }

        protected long getCurrentWait(int count) {
            return wait;
        }
    }

    public static class WaitListObjectSearch extends WaitObjectSearch {

        private long[] waitList;

        public WaitListObjectSearch(long... waitList) {
            super(waitList.length, 0);
            this.waitList = waitList;
        }

        @Override
        protected long getCurrentWait(int count) {
            return waitList[count];
        }
    }

    public static class ThrowsExceptionSearch extends ConnectorObjectSearch {
        final int idx;
        final RuntimeException ex;

        public ThrowsExceptionSearch(final int limit, final int idx, final RuntimeException ex) {
            super(limit);
            this.ex = ex;
            this.idx = idx;
        }

        @Override
        protected void beforeObject(int count) {
            if (count == idx) {
                throw this.ex;
            }
        }
    }
}
