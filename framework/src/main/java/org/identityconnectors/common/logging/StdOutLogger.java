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

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.identityconnectors.common.StringPrintWriter;
import org.identityconnectors.common.logging.LogSpi;
import org.identityconnectors.common.logging.Log.Level;

/**
 * Standard out logger. It logs all messages to STDOUT. The method
 * {@link LogSpi#isLoggable(Class, Level)} will always return true so currently
 * logging is not filtered.
 * 
 * @author Will Droste
 * @version $Revision: 1.5 $
 * @since 1.0
 */
class StdOutLogger implements LogSpi {

    private static final String PATTERN = "Thread Id: {0}\tTime: {1}\tClass: {2}\tMethod: {3}\tLevel: {4}\tMessage: {5}";
    /**
     * Insures there is only one MessageFormat per thread since MessageFormat is
     * not thread safe.
     */
    private static final ThreadLocal<MessageFormat> _messageFormatHandler = new ThreadLocal<MessageFormat>() {
        @Override
        protected MessageFormat initialValue() {
            return new MessageFormat(PATTERN);
        }
    };
    
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final ThreadLocal<DateFormat> _dateFormatHandler = new ThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(DATE_PATTERN);
        }
    };

    /**
     * Logs the thread id, date, class, level, message, and optionally exception
     * stack trace to standard out.
     * 
     * @see LogSpi#log(Class, String, Level, String, Throwable)
     */
    public void log(Class<?> clazz, String methodName, Level level,
            String message, Throwable ex) {
        Object[] args = new Object[] { Thread.currentThread().getId(),
                _dateFormatHandler.get().format(new Date()), clazz.getName(), methodName, level, message };
        PrintStream out = Level.ERROR.equals(level) ? System.err : System.out;
        String msg = _messageFormatHandler.get().format(args);
        out.println(msg);
        if (ex != null) {
            StringPrintWriter wrt = new StringPrintWriter();
            ex.printStackTrace(wrt);
            out.print(wrt.getString());
        }
    }

    /**
     * Always returns true.
     */
    public boolean isLoggable(Class<?> clazz, Level level) {
        return true;
    }
}
