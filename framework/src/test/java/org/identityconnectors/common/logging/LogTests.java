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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.common.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.identityconnectors.common.logging.Log.Level;
import org.junit.Test;

public class LogTests {

    public static class MockLogSpi implements LogSpi {

        public Log.Level _level = null;

        public Class<?> _class = null;

        public String _message = null;

        public String _methodName = null;

        public Throwable _exception = null;

        public boolean _isloggable = false;

        public boolean isLoggable(Class<?> clazz, Level level) {
            _class = clazz;
            _level = level;
            return _isloggable;
        }

        public void log(Class<?> clazz, String methodName, Level level,
                String message, Throwable ex) {
            _class = clazz;
            _level = level;
            _exception = ex;
            _message = message;
            _methodName = methodName;
        }
    }

    @Test
    public void checkIsLoggableMethods() {
        // create log w/ Mock..
        MockLogSpi spi = new MockLogSpi();
        Log log = Log.getLog(String.class, spi);
        // try each of the is log methods..
        // ERROR
        spi._isloggable = false;
        assertFalse(log.isError());
        assertEquals(Log.Level.ERROR, spi._level);
        spi._isloggable = true;
        assertTrue(log.isError());
        assertEquals(Log.Level.ERROR, spi._level);
        assertNull(spi._methodName);
        // INFO
        spi._isloggable = false;
        assertFalse(log.isInfo());
        assertEquals(Log.Level.INFO, spi._level);
        spi._isloggable = true;
        assertTrue(log.isInfo());
        assertEquals(Log.Level.INFO, spi._level);
        assertNull(spi._methodName);
        // OK
        spi._isloggable = false;
        assertFalse(log.isOk());
        assertEquals(Log.Level.OK, spi._level);
        spi._isloggable = true;
        assertTrue(log.isOk());
        assertEquals(Log.Level.OK, spi._level);
        assertNull(spi._methodName);
        // WARN
        spi._isloggable = false;
        assertFalse(log.isWarning());
        assertEquals(Log.Level.WARN, spi._level);
        spi._isloggable = true;
        assertTrue(log.isWarning());
        assertEquals(Log.Level.WARN, spi._level);
        // loop through all the levels..
        for (Level level : Level.values()) {
            spi._isloggable = false;
            assertFalse(log.isLoggable(level));
            assertEquals(level, spi._level);
            spi._isloggable = true;
            assertTrue(log.isLoggable(level));
            assertEquals(level, spi._level);
            // make sure the rest are the ok..
            assertEquals(String.class, spi._class);
            assertNull(spi._methodName);
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
        spi._message = null;
        spi._isloggable = false;
        log.error(EXPECTED_MSG);
        assertNull(spi._message);
        assertEquals(String.class, spi._class);
        spi._isloggable = true;
        log.error(EXPECTED_MSG);
        assertEquals(Log.Level.ERROR, spi._level);
        assertEquals(EXPECTED_MSG, spi._message);
        assertEquals(String.class, spi._class);
        assertEquals(METHOD, spi._methodName);
    }

    @Test
    public void checkFullLogMessage() {
        final String EXPECTED = "some message: {0}";
        MockLogSpi spi = new MockLogSpi();
        Log log = Log.getLog(String.class, spi);
        // test that it doesn't log..
        spi._isloggable = false;
        log.log(Log.Level.INFO, new Exception(), EXPECTED, 1);
        assertEquals(Log.Level.INFO, spi._level);
        assertEquals(String.class, spi._class);
        assertNull(spi._message);
        assertNull(spi._exception);
        assertFalse(spi._isloggable);
        // test that it does log..
        spi = new MockLogSpi();
        log = Log.getLog(String.class, spi);
        spi._isloggable = true;
        final Exception EX = new Exception();
        log.log(Log.Level.ERROR, EX, EXPECTED, 1);
        assertEquals("some message: 1", spi._message);
        assertEquals(Log.Level.ERROR, spi._level);
        assertEquals(String.class, spi._class);
        assertEquals(EX, spi._exception);
        assertTrue(spi._isloggable);
        // check that is goes through the condition..
        spi = new MockLogSpi();
        log = Log.getLog(String.class, spi);
        spi._isloggable = true;
        final String EX_MSG = "dafdslfkj";
        log.log(Log.Level.ERROR, new Exception(EX_MSG), null);
        assertEquals(Log.Level.ERROR, spi._level);
        assertEquals(String.class, spi._class);
        assertEquals(EX_MSG, spi._message);
    }

    @Test
    public void checkBasicLogging() {
        for (Log.Level level : Log.Level.values()) {
            final String EXPECTED_MSG = "Message: " + level;
            MockLogSpi spi = new MockLogSpi();
            spi._isloggable = true;
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
            assertNull(spi._exception);
            assertTrue(spi._isloggable);
            assertEquals(level, spi._level);
            assertEquals(String.class, spi._class);
            assertEquals(EXPECTED_MSG, spi._message);
        }
    }

    @Test
    public void checkBasicLoggingOff() {
        for (Log.Level level : Log.Level.values()) {
            final String EXPECTED_MSG = "Message: " + level;
            MockLogSpi spi = new MockLogSpi();
            spi._isloggable = false;
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
            assertNull(spi._message);
            assertNull(spi._exception);
            assertFalse(spi._isloggable);
            assertEquals(level, spi._level);
            assertEquals(String.class, spi._class);
        }
    }

    @Test
    public void checkBasicLoggingWithFormating() {
        for (Log.Level level : Log.Level.values()) {
            final String FORMAT = "Message: {0}";
            final String EXPECTED_MSG = "Message: " + level;
            MockLogSpi spi = new MockLogSpi();
            spi._isloggable = true;
            Log log = Log.getLog(String.class, spi);
            if (Log.Level.ERROR.equals(level)) {
                log.error(FORMAT, level);
            } else if (Log.Level.INFO.equals(level)) {
                log.info(FORMAT, level);
            } else if (Log.Level.OK.equals(level)) {
                log.ok(FORMAT, level);
            } else if (Log.Level.WARN.equals(level)) {
                log.warn(FORMAT, level);
            }
            assertEquals(EXPECTED_MSG, spi._message);
            assertNull(spi._exception);
            assertTrue(spi._isloggable);
            assertEquals(level, spi._level);
            assertEquals(String.class, spi._class);
        }
    }

    @Test
    public void checkBasicLoggingWithExceptionAndFormating() {
        for (Log.Level level : Log.Level.values()) {
            final String FORMAT = "Message: {0}";
            final String EXPECTED_MSG = "Message: " + level;
            final Exception EXPECTED_EX = new Exception(level.toString());
            MockLogSpi spi = new MockLogSpi();
            spi._isloggable = true;
            Log log = Log.getLog(String.class, spi);
            if (Log.Level.ERROR.equals(level)) {
                log.error(EXPECTED_EX, FORMAT, level);
            } else if (Log.Level.INFO.equals(level)) {
                log.info(EXPECTED_EX, FORMAT, level);
            } else if (Log.Level.OK.equals(level)) {
                log.ok(EXPECTED_EX, FORMAT, level);
            } else if (Log.Level.WARN.equals(level)) {
                log.warn(EXPECTED_EX, FORMAT, level);
            }
            assertEquals(EXPECTED_MSG, spi._message);
            assertTrue(spi._isloggable);
            assertEquals(level, spi._level);
            assertEquals(String.class, spi._class);
            assertEquals(EXPECTED_EX, spi._exception);
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
                assertEquals(StdOutLogger.class, Log.getSpiClass());
                // attempt to get the mock logger
                Log.setSpiClass(null);
                System.setProperty(Log.LOGSPI_PROP, MockLogSpi.class.getName());
                Log.getLog(String.class);
                assertEquals(MockLogSpi.class, Log.getSpiClass());
                // attempt to change it, so make sure its cached..
                System.setProperty(Log.LOGSPI_PROP, StdOutLogger.class.getName());
                assertEquals(MockLogSpi.class, Log.getSpiClass());
            } finally {
                // restore logger to original state..
                Log.setSpiClass(orig);
                System.clearProperty(Log.LOGSPI_PROP);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkGetLog() {
        Log.getLog(MockLogSpi.class);
    }
}
