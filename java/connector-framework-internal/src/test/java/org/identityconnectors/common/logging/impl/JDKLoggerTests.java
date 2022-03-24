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
package org.identityconnectors.common.logging.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformLoggingMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.identityconnectors.common.logging.Log.Level;
import org.junit.jupiter.api.Test;

public class JDKLoggerTests {

    @Test
    public void testLog() {
        JDKLogger logger = new JDKLogger();
        Logger jdkLogger = logger.getJDKLogger(JDKLoggerTests.class.getName());
        assertNotNull(jdkLogger);
        // We do not want to have output in stderr
        jdkLogger.setUseParentHandlers(false);
        final List<LogRecord> records = new ArrayList<>();
        jdkLogger.addHandler(new Handler() {

            @Override
            public void close() throws SecurityException {
            }

            @Override
            public void flush() {
            }

            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }
        });
        logger.log(JDKLoggerTests.class, "method1", Level.ERROR, "Msg", new Exception());
        assertEquals(records.size(), 1, "Must log one record");
        assertEquals(records.get(0).getSourceMethodName(), "method1");
        assertEquals(records.get(0).getMessage(), "Msg");
        assertNotNull(records.get(0).getThrown());
    }

    @Test
    public void testIsLoggable() {
        JDKLogger logger = new JDKLogger();
        Logger jdkLogger = logger.getJDKLogger(JDKLoggerTests.class.getName());

        ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class).
                setLoggerLevel(JDKLoggerTests.class.getName(), java.util.logging.Level.INFO.getName());
        assertEquals(jdkLogger.getLevel(), java.util.logging.Level.INFO);
        assertTrue(logger.isLoggable(JDKLoggerTests.class, Level.INFO));

        ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class).
                setLoggerLevel(JDKLoggerTests.class.getName(), java.util.logging.Level.WARNING.getName());
        assertEquals(jdkLogger.getLevel(), java.util.logging.Level.WARNING);
        assertTrue(logger.isLoggable(JDKLoggerTests.class, Level.WARN));

        ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class).
                setLoggerLevel(JDKLoggerTests.class.getName(), java.util.logging.Level.INFO.getName());
        assertEquals(jdkLogger.getLevel(), java.util.logging.Level.INFO);
        assertFalse(logger.isLoggable(JDKLoggerTests.class, Level.OK));

        ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class).
                setLoggerLevel(JDKLoggerTests.class.getName(), java.util.logging.Level.OFF.getName());
        assertFalse(logger.isLoggable(JDKLoggerTests.class, Level.WARN));

        ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class).
                setLoggerLevel(JDKLoggerTests.class.getName(), java.util.logging.Level.ALL.getName());
        assertTrue(logger.isLoggable(JDKLoggerTests.class, Level.OK));
    }

    @Test
    public void testCreateJDKLogger() {
        JDKLogger logger = new JDKLogger();
        Logger jdkLogger1 = logger.getJDKLogger(JDKLoggerTests.class.getName());
        assertSame(Logger.getLogger(JDKLoggerTests.class.getName()), jdkLogger1);
        Logger jdkLogger2 = logger.getJDKLogger(JDKLoggerTests.class.getName());
        assertSame(jdkLogger1, jdkLogger2);
        Logger jdkLogger3 = logger.getJDKLogger(Integer.class.getName());
        Logger jdkLogger4 = logger.getJDKLogger(Long.class.getName());
        Logger jdkLogger5 = logger.getJDKLogger(Byte.class.getName());
        Logger jdkLogger6 = logger.getJDKLogger(Byte.class.getName());
        assertNotSame(jdkLogger3, jdkLogger4);
        assertNotSame(jdkLogger3, jdkLogger5);
        assertSame(jdkLogger5, jdkLogger6);
        Logger jdkLogger7 = logger.getJDKLogger(JDKLoggerTests.class.getName());
        assertSame(jdkLogger1, jdkLogger7);
    }

    @Test
    public void testMultithreaded() {
        final JDKLogger logger = new JDKLogger();
        int size = 100;
        CyclicBarrier barier = new CyclicBarrier(size);
        List<Thread> threads = new ArrayList<>(size);
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String key = "" + i % 10;
            keys.add(key);
            Thread thread = new Thread(new CreateLogger(barier, logger, key));
            threads.add(thread);
            thread.start();
        }
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        });
        assertEquals(keys, logger.getMap().keySet());
    }

    private static class CreateLogger implements Runnable {

        final CyclicBarrier barier;

        final JDKLogger logger;

        final String key;

        private CreateLogger(CyclicBarrier barier, JDKLogger logger, String key) {
            this.barier = barier;
            this.logger = logger;
            this.key = key;
        }

        @Override
        public void run() {
            try {
                barier.await();
            } catch (Exception e) {
                // ignore
            }
            logger.getJDKLogger(key);
        }
    }
}
