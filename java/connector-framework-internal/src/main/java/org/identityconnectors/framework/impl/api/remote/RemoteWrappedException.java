/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS. All rights reserved.
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

package org.identityconnectors.framework.impl.api.remote;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * RemoteWrappedException wraps every exception which are received from Remote
 * Connector Server.
 * <p/>
 * <b>This Exception is not allowed to use in Connectors!!!</b>
 * <p/>
 *
 *
 *
 * This type of exception is not allowed to be serialise because this exception
 * represents any after deserialization.
 *
 * This code example show how to get the remote stack trace and how to use the
 * same catches to handle the exceptions regardless its origin.
 *
 * <pre>
 * <code>
 *  String stackTrace = null;
 *  try {
 *      try {
 *          facade.getObject(ObjectClass.ACCOUNT, uid, null);
 *      } catch (RemoteWrappedException e) {
 *          stackTrace = e.getStackTrace()
 *      }
 *  } catch (Throwable t) {
 *      if (null != stackTrace) {
 *          System.err.println(stackTrace);
 *      } else {
 *          t.printStackTrace();
 *      }
 *  }
 * <code>
 * </pre>
 *
 * @author Laszlo Hordos
 * @since 1.4
 */
public final class RemoteWrappedException extends ConnectorException {

    private static final long serialVersionUID = 1L;
    public static final String FIELD_CLASS = "class";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_CAUSE = "cause";
    public static final String FIELD_STACK_TRACE = "stackTrace";

    private final String stackTrace;

    /**
     * <pre>
     *     <code>
     *         {
     *              "class": "org.identityconnectors.framework.common.exceptions.ConnectorIOException",
     *              "message": "Sample Error Message",
     *              "cause": {
     *                  "class": "java.net.SocketTimeoutException",
     *                  "message": "Sample Error Message",
     *                  "cause": {
     *                      "class": "edu.example.CustomException",
     *                      "message": "Sample Error Message"
     *                  }
     *              },
     *              "stackTrace": "full stack trace for logging"
     *          }
     *     </code>
     * </pre>
     */
    private Map<String, Object> exception = null;

    /**
     * @see org.identityconnectors.framework.common.exceptions.ConnectorException#ConnectorException(String)
     */
    RemoteWrappedException(final Map<String, Object> exception) {
        super((String) exception.get(FIELD_MESSAGE));
        this.exception = exception;
        this.stackTrace = (String) exception.get(FIELD_STACK_TRACE);
    }

    public RemoteWrappedException(final String throwableClass, final String message,
            final RemoteWrappedException cause, final String stackTrace) {
        super(message);
        exception = new HashMap<String, Object>(4);
        exception.put(FIELD_CLASS, Assertions.blankChecked(throwableClass, "throwableClass"));
        exception.put(FIELD_MESSAGE, message);
        if (null != cause) {
            exception.put(FIELD_CAUSE, cause.exception);
        }
        if (null != stackTrace) {
            exception.put(FIELD_STACK_TRACE, stackTrace);
        }
        this.stackTrace = stackTrace;
    }

    /**
     * Gets the class name of the original exception.
     *
     * This value is constructed by {@code throwable.getClass().getName()}.
     *
     * @return name of the original exception.
     */
    public String getExceptionClass() {
        return (String) exception.get(FIELD_CLASS);
    }

    /**
     * Checks if the exception is the expected class.
     *
     * @param expected
     *            the expected throwable class.
     * @return {@code true} if the class name are equals.
     */
    public boolean is(Class<? extends Throwable> expected) {
        if (null == expected) {
            return false;
        }
        String className = ((String) exception.get(FIELD_CLASS));
        String classExpected = expected.getName();
        return classExpected.equalsIgnoreCase(className);
    }

    /**
     * Returns the cause of original throwable or {@code null} if the cause is
     * nonexistent or unknown. (The cause is the throwable that caused the
     * original throwable to get thrown.)
     *
     * @return the cause of this throwable or {@code null} if the cause is
     *         nonexistent or unknown.
     */
    @Override
    @SuppressWarnings("unchecked")
    public RemoteWrappedException getCause() {
        Object o = exception.get(FIELD_CAUSE);
        if (o instanceof Map) {
            return new RemoteWrappedException((Map<String, Object>) o);
        } else {
            return null;
        }
    }

    @Override
    public void printStackTrace(PrintStream s) {
        if (null == stackTrace) {
            super.printStackTrace(s);
        } else {
            s.println(stackTrace);
        }
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        if (null == stackTrace) {
            super.printStackTrace(s);
        } else {
            s.println(stackTrace);
        }
    }

    public String readStackTrace() {
        return stackTrace;
    }

    /**
     * Gets the stack trace from a Throwable as a String.
     *
     *
     * @param throwable
     *            the {@code Throwable} to be examined
     * @return the stack trace as generated by the exception's
     *         {@code printStackTrace(PrintWriter)} method
     */
    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }

    /**
     * Wraps the Throwable into a RemoteWrappedException instance.
     *
     * @param ex
     *            Exception to wrap or cast and return.
     * @return a <code>RemoteWrappedException</code> that either <i>is</i> the
     *         specified exception or <i>contains</i> the specified exception.
     */
    public static RemoteWrappedException wrap(Throwable ex) {
        if (null == ex) {
            return null;
        }
        // don't bother to wrap a exception that is already a
        // RemoteWrappedException.
        if (ex instanceof RemoteWrappedException) {
            return (RemoteWrappedException) ex;
        }
        return new RemoteWrappedException(convert(ex));
    }

    /**
     * Converts the throwable object to a new Map object that representing
     * itself.
     *
     * @param throwable
     *            the {@code Throwable} to be converted
     * @return the Map representing the throwable.
     */
    public static HashMap<String, Object> convert(Throwable throwable) {
        HashMap<String, Object> exception = null;
        if (null != throwable) {
            exception = new HashMap<String, Object>(4);
            exception.put(FIELD_CLASS, throwable.getClass().getName());
            exception.put(FIELD_MESSAGE, throwable.getMessage());
            if (null != throwable.getCause()) {
                exception.put(FIELD_CAUSE, buildCause(throwable.getCause()));
            }
            exception.put(FIELD_STACK_TRACE, getStackTrace(throwable));
        }
        return exception;
    }

    private static Map<String, Object> buildCause(Throwable throwable) {
        Map<String, Object> cause =
                new HashMap<String, Object>(null != throwable.getCause() ? 3 : 2);
        cause.put(FIELD_CLASS, throwable.getClass().getName());
        cause.put(FIELD_MESSAGE, throwable.getMessage());
        if (null != throwable.getCause()) {
            cause.put(FIELD_CAUSE, buildCause(throwable.getCause()));
        }
        return cause;
    }
}
