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
 * Portions Copyrighted 2015 ConnId
 */
package org.identityconnectors.framework.impl.api;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.spi.SearchResultsHandler;

public class StreamHandlerUtil {

    /**
     * Adapts from a ObjectStreamHandler to a ResultsHandler (or SearchResultsHandler).
     */
    private static class SearchResultsHandlerAdapter implements SearchResultsHandler {

        private final ObjectStreamHandler target;

        public SearchResultsHandlerAdapter(final ObjectStreamHandler target) {
            this.target = target;
        }

        @Override
        public void handleResult(final SearchResult result) {
            target.handle(result);
        }

        @Override
        public boolean handle(final ConnectorObject obj) {
            return target.handle(obj);
        }
    }

    /**
     * Adapts from a ObjectStreamHandler to a SyncResultsHandler.
     */
    private static class SyncResultsHandlerAdapter implements SyncResultsHandler {

        private final ObjectStreamHandler target;

        public SyncResultsHandlerAdapter(final ObjectStreamHandler target) {
            this.target = target;
        }

        @Override
        public boolean handle(final SyncDelta obj) {
            return target.handle(obj);
        }
    }

    /**
     * Adapts from a ObjectStreamHandler to a SyncResultsHandler.
     */
    private static class ObjectStreamHandlerAdapter implements ObjectStreamHandler {

        private final Class<?> targetInterface;

        private final Object target;

        public ObjectStreamHandlerAdapter(final Class<?> targetInterface, final Object target) {
            Assertions.nullCheck(targetInterface, "targetInterface");
            Assertions.nullCheck(target, "target");
            if (!targetInterface.isInstance(target)) {
                throw new IllegalArgumentException("Target" + targetInterface + " " + target);
            }
            if (!isAdaptableToObjectStreamHandler(targetInterface)) {
                throw new IllegalArgumentException("Target interface not supported: " + targetInterface);
            }
            this.targetInterface = targetInterface;
            this.target = target;
        }

        @Override
        public boolean handle(final Object obj) {
            if (targetInterface == ResultsHandler.class || targetInterface == SearchResultsHandler.class) {
                if (obj instanceof ConnectorObject) {
                    return ((ResultsHandler) target).handle((ConnectorObject) obj);
                } else if (obj instanceof SearchResult) {
                    ((SearchResultsHandler) target).handleResult((SearchResult) obj);
                    return true;
                }
            } else if (targetInterface == SyncResultsHandler.class) {
                return ((SyncResultsHandler) target).handle((SyncDelta) obj);
            }

            throw new UnsupportedOperationException("Unhandled case: " + targetInterface);
        }
    }

    public static boolean isAdaptableToObjectStreamHandler(final Class<?> clazz) {
        return (ResultsHandler.class.isAssignableFrom(clazz) || SyncResultsHandler.class.isAssignableFrom(clazz));
    }

    public static ObjectStreamHandler adaptToObjectStreamHandler(final Class<?> interfaceType, final Object target) {
        return new ObjectStreamHandlerAdapter(interfaceType, target);
    }

    public static Object adaptFromObjectStreamHandler(final Class<?> interfaceType, final ObjectStreamHandler target) {
        if (interfaceType == ResultsHandler.class || interfaceType == SearchResultsHandler.class) {
            return new SearchResultsHandlerAdapter(target);
        } else if (interfaceType == SyncResultsHandler.class) {
            return new SyncResultsHandlerAdapter(target);
        }

        throw new UnsupportedOperationException("Unhandled case: " + interfaceType);
    }

}
