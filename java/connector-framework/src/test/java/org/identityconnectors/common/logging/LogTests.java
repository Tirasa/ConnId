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
 * Portions Copyrighted 2014 ForgeRock AS.
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.identityconnectors.common.logging.Log.Level;
import org.junit.jupiter.api.Test;

public class LogTests {

    public static class MockLogSpi implements LogSpi {

        private Log.Level level = null;

        private Class<?> clazz = null;

        private String message = null;

        private String methodName = null;

        private Throwable exception = null;

        private boolean isloggable = false;

        @Override
        public boolean isLoggable(Class<?> clazz, Level level) {
            this.clazz = clazz;
            this.level = level;
            return isloggable;
        }

        @Override
        public boolean needToInferCaller(Class<?> clazz, Level level) {
            return true;
        }

        @Override
        public void log(Class<?> clazz, String methodName, Level level, String message, Throwable ex) {
            this.clazz = clazz;
            this.level = level;
            exception = ex;
            this.message = message;
            this.methodName = methodName;
        }

        @Override
        public void log(Class<?> clazz, StackTraceElement caller, Level level, String message, Throwable ex) {
            this.clazz = clazz;
            this.level = level;
            exception = ex;
            this.message = message;
            methodName = null != caller ? caller.getMethodName() : "unknown";
        }
    }

    @Test
    public void checkIsLoggableMethods() {
        // create log w/ Mock..
        MockLogSpi spi = new MockLogSpi();
        Log log = Log.getLog(String.class, spi);
        // try each of the is log methods..
        // ERROR
        spi.isloggable = false;
        assertFalse(log.isError());
        assertEquals(spi.level, Log.Level.ERROR);
        spi.isloggable = true;
        assertTrue(log.isError());
        assertEquals(spi.level, Log.Level.ERROR);
        assertNull(spi.methodName);
        // INFO
        spi.isloggable = false;
        assertFalse(log.isInfo());
        assertEquals(spi.level, Log.Level.INFO);
        spi.isloggable = true;
        assertTrue(log.isInfo());
        assertEquals(spi.level, Log.Level.INFO);
        assertNull(spi.methodName);
        // OK
        spi.isloggable = false;
        assertFalse(log.isOk());
        assertEquals(spi.level, Log.Level.OK);
        spi.isloggable = true;
        assertTrue(log.isOk());
        assertEquals(spi.level, Log.Level.OK);
        assertNull(spi.methodName);
        // WARN
        spi.isloggable = false;
        assertFalse(log.isWarning());
        assertEquals(spi.level, Log.Level.WARN);
        spi.isloggable = true;
        assertTrue(log.isWarning());
        assertEquals(spi.level, Log.Level.WARN);
        // loop through all the levels..
        for (Level level : Level.values()) {
            spi.isloggable = false;
            assertFalse(log.isLoggable(level));
            assertEquals(spi.level, level);
            spi.isloggable = true;
            assertTrue(log.isLoggable(level));
            assertEquals(spi.level, level);
            // make sure the rest are the ok..
            assertEquals(spi.clazz, String.class);
            assertNull(spi.methodName);
        }
    }

    @Test
    public void checkMessageLog() {
        final String METHOD = "checkMessageLog";
        final String EXPECTED_MSG = "any old message will do";
        // create log w/ Mock..
        MockLogSpi spi = new MockLogSpi();
        Log log = Log.getLog(String.class, spi);
        // try each of the is log methods..
        // attempt to check the message methods...
        spi.message = null;
        spi.isloggable = false;
        log.error(EXPECTED_MSG);
        assertNull(spi.message);
        assertEquals(spi.clazz, String.class);
        spi.isloggable = true;
        log.error(EXPECTED_MSG);
        assertEquals(spi.level, Log.Level.ERROR);
        assertEquals(spi.message, EXPECTED_MSG);
        assertEquals(spi.clazz, String.class);
        assertEquals(spi.methodName, METHOD);
    }

    @Test
    public void checkFullLogMessage() {
        final String EXPECTED = "some message: {0}";
        MockLogSpi spi = new MockLogSpi();
        Log log = Log.getLog(String.class, spi);
        // test that it doesn't log..
        spi.isloggable = false;
        log.log(Log.Level.INFO, new Exception(), EXPECTED, 1);
        assertEquals(spi.level, Log.Level.INFO);
        assertEquals(spi.clazz, String.class);
        assertNull(spi.message);
        assertNull(spi.exception);
        assertFalse(spi.isloggable);
        // test that it does log..
        spi = new MockLogSpi();
        log = Log.getLog(String.class, spi);
        spi.isloggable = true;
        final Exception EX = new Exception();
        log.log(Log.Level.ERROR, EX, EXPECTED, 1);
        assertEquals(spi.message, "some message: 1");
        assertEquals(spi.level, Log.Level.ERROR);
        assertEquals(spi.clazz, String.class);
        assertEquals(spi.exception, EX);
        assertTrue(spi.isloggable);
        // check that is goes through the condition..
        spi = new MockLogSpi();
        log = Log.getLog(String.class, spi);
        spi.isloggable = true;
        final String EX_MSG = "dafdslfkj";
        log.log(Log.Level.ERROR, new Exception(EX_MSG), null);
        assertEquals(spi.level, Log.Level.ERROR);
        assertEquals(spi.clazz, String.class);
        assertEquals(spi.message, EX_MSG);
    }

    @Test
    public void checkBasicLogging() {
        for (Log.Level level : Log.Level.values()) {
            final String EXPECTED_MSG = "Message: " + level;
            MockLogSpi spi = new MockLogSpi();
            spi.isloggable = true;
            Log log = Log.getLog(String.class, spi);
            if (Log.Level.ERROR.equals(level)) {
                log.error(EXPECTED_MSG);
            } else if (Log.Level.INFO.equals(level)) {
                log.info(EXPECTED_MSG);
            } else if (Log.Level.OK.equals(level)) {
                log.ok(EXPECTED_MSG);
            } else if (Log.Level.WARN.equals(level)) {
                log.warn(EXPECTED_MSG);
            }
            assertNull(spi.exception);
            assertTrue(spi.isloggable);
            assertEquals(spi.level, level);
            assertEquals(spi.clazz, String.class);
            assertEquals(spi.message, EXPECTED_MSG);
        }
    }

    @Test
    public void checkBasicLoggingOff() {
        for (Log.Level level : Log.Level.values()) {
            final String EXPECTED_MSG = "Message: " + level;
            MockLogSpi spi = new MockLogSpi();
            spi.isloggable = false;
            Log log = Log.getLog(String.class, spi);
            if (null != level) {
                switch (level) {
                    case ERROR ->
                        log.error(EXPECTED_MSG);
                    case INFO ->
                        log.info(EXPECTED_MSG);
                    case OK ->
                        log.ok(EXPECTED_MSG);
                    case WARN ->
                        log.warn(EXPECTED_MSG);
                    default -> {
                    }
                }
            }
            assertNull(spi.message);
            assertNull(spi.exception);
            assertFalse(spi.isloggable);
            assertEquals(spi.level, level);
            assertEquals(spi.clazz, String.class);
        }
    }

    @Test
    public void checkBasicLoggingWithFormating() {
        for (Log.Level level : Log.Level.values()) {
            final String FORMAT = "Message: {0}";
            final String EXPECTED_MSG = "Message: " + level;
            MockLogSpi spi = new MockLogSpi();
            spi.isloggable = true;
            Log log = Log.getLog(String.class, spi);
            if (null != level) {
                switch (level) {
                    case ERROR ->
                        log.error(FORMAT, level);
                    case INFO ->
                        log.info(FORMAT, level);
                    case OK ->
                        log.ok(FORMAT, level);
                    case WARN ->
                        log.warn(FORMAT, level);
                    default -> {
                    }
                }
            }
            assertEquals(spi.message, EXPECTED_MSG);
            assertNull(spi.exception);
            assertTrue(spi.isloggable);
            assertEquals(level, spi.level);
            assertEquals(spi.clazz, String.class);
        }
    }

    @Test
    public void checkBasicLoggingWithExceptionAndFormating() {
        for (Log.Level level : Log.Level.values()) {
            final String FORMAT = "Message: {0}";
            final String EXPECTED_MSG = "Message: " + level;
            final Exception EXPECTED_EX = new Exception(level.toString());
            MockLogSpi spi = new MockLogSpi();
            spi.isloggable = true;
            Log log = Log.getLog(String.class, spi);
            switch (level) {
                case ERROR ->
                    log.error(EXPECTED_EX, FORMAT, level);
                case INFO ->
                    log.info(EXPECTED_EX, FORMAT, level);
                case OK ->
                    log.ok(EXPECTED_EX, FORMAT, level);
                case WARN ->
                    log.warn(EXPECTED_EX, FORMAT, level);
                default -> {
                }
            }
            assertEquals(spi.message, EXPECTED_MSG);
            assertTrue(spi.isloggable);
            assertEquals(level, spi.level);
            assertEquals(spi.clazz, String.class);
            assertEquals(spi.exception, EXPECTED_EX);
        }
    }

    @Test
    public void checkSystemProperty() {
        // don't mess up other tests w/ changing out logging..
        synchronized (Log.class) {
            // save the original..
            Class<?> orig = Log.getSpiClass();
            try {
                // check the default..
                Log.getLog(String.class);
                assertEquals(Log.getSpiClass(), StdOutLogger.class);
                // attempt to get the mock logger
                Log.setSpiClass(null);
                System.setProperty(Log.LOGSPI_PROP, MockLogSpi.class.getName());
                Log.getLog(String.class);
                assertEquals(Log.getSpiClass(), MockLogSpi.class);
                // attempt to change it, so make sure its cached..
                System.setProperty(Log.LOGSPI_PROP, StdOutLogger.class.getName());
                assertEquals(Log.getSpiClass(), MockLogSpi.class);
            } finally {
                // restore logger to original state..
                Log.setSpiClass(orig);
                System.clearProperty(Log.LOGSPI_PROP);
            }
        }
    }
}
