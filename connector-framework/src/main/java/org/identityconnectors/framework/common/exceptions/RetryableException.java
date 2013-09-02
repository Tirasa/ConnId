/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock Inc. All rights reserved.
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

package org.identityconnectors.framework.common.exceptions;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * RetryableException indicates that a failure may be temporary, and that
 * retrying the same request may be able to succeed in the future.
 * <p/>
 *
 *
 * @author Laszlo Hordos
 * @since 1.4
 */
public class RetryableException extends ConnectorException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new RetryableException exception with the specified cause
     * and a detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of <tt>cause</tt>
     * ). This constructor is useful for InvalidAccountException exceptions that
     * are little more than wrappers for other throwables.
     *
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <tt>null</tt> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    private RetryableException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new RetryableException exception with the specified detail
     * message and cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this Connector exception's
     * detail message.
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <tt>null</tt> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    private RetryableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * If {@link Exception} parameter passed in is a {@link RetryableException}
     * it is simply returned. Otherwise the {@link Exception} is wrapped in a
     * <code>RetryableException</code> and returned.
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     * @param cause
     *            Exception to wrap or cast and return.
     * @return a <code>RuntimeException</code> that either <i>is</i> the
     *         specified exception or <i>contains</i> the specified exception.
     */
    public static RetryableException wrap(String message, Throwable cause) {
        // don't bother to wrap a exception that is already a
        // RetryableException.
        if (cause instanceof RetryableException) {
            return (RetryableException) cause;
        }

        if (null != message) {
            return new RetryableException(message, cause);
        } else {
            return new RetryableException(cause);
        }
    }

    /**
     * Constructs a new RetryableException which signals partial success of
     * <code>create</code> operation.
     *
     * This should be called inside
     * {@link org.identityconnectors.framework.spi.operations.CreateOp#create(org.identityconnectors.framework.common.objects.ObjectClass, java.util.Set, org.identityconnectors.framework.common.objects.OperationOptions)}
     * implementation to signal that the create was not completed but the object
     * was created with <code>Uid</code> and Application should call the
     * {@link org.identityconnectors.framework.spi.operations.UpdateOp#update(org.identityconnectors.framework.common.objects.ObjectClass, org.identityconnectors.framework.common.objects.Uid, java.util.Set, org.identityconnectors.framework.common.objects.OperationOptions)}
     * method now.
     * <p/>
     * Use this only if the created object can not be deleted. The best-practice
     * should always be the Connector implementation reverts the changes if the
     * operation failed.
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     *
     * @param uid
     *            the new object's Uid.
     * @return a <code>RetryableException</code> that either <i>is</i> the
     *         specified exception or <i>contains</i> the specified exception.
     */
    public static RetryableException wrap(final String message, final Uid uid) {
        return new RetryableException(message, new AlreadyExistsException().initUid(Assertions
                .nullChecked(uid, "Uid")));
    }
}
