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
 * Portions Copyrighted 2012-2014 ForgeRock AS.
 */
using System;
using Org.IdentityConnectors.Framework.Common.Objects;
using Org.IdentityConnectors.Common;

namespace Org.IdentityConnectors.Framework.Common.Exceptions
{
    #region AlreadyExistsException
    /// <summary>
    /// AlreadyExistsException is thrown to indicate if
    /// <seealso cref="Org.IdentityConnectors.Framework.Api.Operations.CreateApiOp"/> attempts
    /// to create an object that exists prior to the method execution or
    /// <seealso cref="Org.IdentityConnectors.Framework.Api.Operations.UpdateApiOp"/> attempts
    /// to rename an object to that exists prior to the method execution.
    /// </summary>
    public class AlreadyExistsException : ConnectorException
    {
        [NonSerialized]
        private Uid _uid;

        /// <seealso cref= ConnectorException#ConnectorException() </seealso>
        public AlreadyExistsException()
            : base()
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String) </seealso>
        public AlreadyExistsException(string message)
            : base(message)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(Throwable) </seealso>
        public AlreadyExistsException(Exception ex)
            : base(ex)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String, Throwable) </seealso>
        public AlreadyExistsException(string message, Exception ex)
            : base(message, ex)
        {
        }

        public  Uid Uid
        {
            get
            {
                return _uid;
            }
        }

        /// <summary>
        /// Sets the Uid of existing Object.
        /// 
        /// Connectors who throw this exception from their
        /// <seealso cref="org.identityconnectors.framework.spi.operations.CreateOp"/> or
        /// <seealso cref="org.identityconnectors.framework.spi.operations.UpdateOp"/> should
        /// set the object's Uid if available.
        /// </summary>
        /// <param name="uid">
        ///            The uid. </param>
        /// <returns> A reference to this. </returns>
        public AlreadyExistsException InitUid(Uid uid)
        {
            this._uid = uid;
            return this;
        }
    }
    #endregion

    #region ConfigurationException
    public class ConfigurationException : ConnectorException
    {
        public ConfigurationException()
            : base()
        {
        }

        public ConfigurationException(String message)
            : base(message)
        {
        }

        public ConfigurationException(Exception ex)
            : base(ex)
        {
        }

        public ConfigurationException(String message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region ConnectionBrokenException
    /// <summary>
    /// ConnectionBrokenException is thrown when a connection to a target resource
    /// instance fails during an operation.
    /// 
    /// An instance of <code>ConnectionBrokenException</code> generally wraps the
    /// native exception (or describes the native error) returned by the target
    /// resource.
    /// </summary>
    public class ConnectionBrokenException : ConnectorIOException
    {

        /// <seealso cref= ConnectorException#ConnectorException() </seealso>
        public ConnectionBrokenException()
            : base()
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String) </seealso>
        public ConnectionBrokenException(string msg)
            : base(msg)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(Throwable) </seealso>
        public ConnectionBrokenException(Exception ex)
            : base(ex)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String, Throwable) </seealso>
        public ConnectionBrokenException(string message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region ConnectionFailedException
    /// <summary>
    /// ConnectionFailedException is thrown when a Connector cannot reach the target
    /// resource instance.
    /// 
    /// An instance of <code>ConnectionFailedException</code> generally wraps the
    /// native exception (or describes the native error) returned by the target
    /// resource.
    /// </summary>
    public class ConnectionFailedException : ConnectorIOException
    {

        /// <seealso cref= ConnectorException#ConnectorException() </seealso>
        public ConnectionFailedException()
            : base()
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String) </seealso>
        public ConnectionFailedException(string msg)
            : base(msg)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(Throwable) </seealso>
        public ConnectionFailedException(Exception ex)
            : base(ex)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String, Throwable) </seealso>
        public ConnectionFailedException(string message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region ConnectorException
    public class ConnectorException : ApplicationException
    {
        public ConnectorException()
            : base()
        {
        }

        /// <summary>
        /// Sets a message for the <see cref="Exception" />.
        /// </summary>
        /// <param name="message">passed to the <see cref="Exception" /> message.</param>
        public ConnectorException(String message)
            : base(message)
        {
        }

        /// <summary>
        /// Sets the stack trace to the original exception, so this exception can
        /// masquerade as the original only be a <see cref="Exception" />.
        /// </summary>
        /// <param name="originalException">the original exception adapted to <see cref="Exception" />.</param>
        public ConnectorException(Exception ex)
            : base(ex.Message, ex)
        {
        }

        /// <summary>
        /// Sets the stack trace to the original exception, so this exception can
        /// masquerade as the original only be a <see cref="Exception" />.
        /// </summary>
        /// <param name="message"></param>
        /// <param name="originalException">the original exception adapted to <see cref="Exception" />.</param>
        public ConnectorException(String message, Exception originalException)
            : base(message, originalException)
        {
        }

    }
    #endregion

    #region ConnectorIOException
    public class ConnectorIOException : ConnectorException
    {
        public ConnectorIOException()
            : base()
        {
        }

        public ConnectorIOException(String msg)
            : base(msg)
        {
        }

        public ConnectorIOException(Exception ex)
            : base(ex)
        {
        }

        public ConnectorIOException(String message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region ConnectorSecurityException
    public class ConnectorSecurityException : ConnectorException
    {
        public ConnectorSecurityException()
            : base()
        {
        }

        public ConnectorSecurityException(String message)
            : base(message)
        {
        }

        public ConnectorSecurityException(Exception ex)
            : base(ex)
        {
        }

        public ConnectorSecurityException(String message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region InvalidAttributeValueException
    /// <summary>
    /// InvalidAttributeValueException is thrown when an attempt is made to add to an
    /// attribute a value that conflicts with the attribute's schema definition.
    /// 
    /// This could happen, for example, if attempting to add an attribute with no
    /// value when the attribute is required to have at least one value, or if
    /// attempting to add more than one value to a single valued-attribute, or if
    /// attempting to add a value that conflicts with the type of the attribute or if
    /// attempting to add a value that conflicts with the syntax of the attribute.
    /// </summary>
    /// <remarks>Since 1.4</remarks>
    public class InvalidAttributeValueException : ConnectorException
    {
        /// <summary>
        /// Constructs a new InvalidAttributeValueException exception with
        /// <code>null</code> as its detail message. The cause is not initialized,
        /// and may subsequently be initialized by a call to <seealso cref="#initCause"/>.
        /// </summary>
        public InvalidAttributeValueException()
            : base()
        {
        }

        /// <summary>
        /// Constructs a new InvalidAttributeValueException exception with the specified
        /// detail message. The cause is not initialized, and may subsequently be
        /// initialized by a call to <seealso cref="#initCause"/>.
        /// </summary>
        /// <param name="message">
        ///            the detail message. The detail message is is a String that
        ///            describes this particular exception and saved for later
        ///            retrieval by the <seealso cref="#getMessage()"/> method. </param>
        public InvalidAttributeValueException(string message)
            : base(message)
        {
        }

        /// <summary>
        /// Constructs a new InvalidAttributeValueException exception with the specified
        /// cause and a detail message of
        /// <tt>(cause==null ? null : cause.toString())</tt> (which typically
        /// contains the class and detail message of <tt>cause</tt>). This
        /// constructor is useful for InvalidAccountException exceptions that are
        /// little more than wrappers for other throwables.
        /// </summary>
        /// <param name="cause">
        ///            the cause (which is saved for later retrieval by the
        ///            <seealso cref="#getCause()"/> method). (A <tt>null</tt> value is
        ///            permitted, and indicates that the cause is nonexistent or
        ///            unknown.) </param>
        public InvalidAttributeValueException(Exception cause)
            : base(cause)
        {
        }

        /// <summary>
        /// Constructs a new InvalidAttributeValueException exception with the specified
        /// detail message and cause.
        /// <para>
        /// Note that the detail message associated with <code>cause</code> is
        /// <i>not</i> automatically incorporated in this Connector exception's
        /// detail message.
        /// 
        /// </para>
        /// </summary>
        /// <param name="message">
        ///            the detail message (which is saved for later retrieval by the
        ///            <seealso cref="#getMessage()"/> method). </param>
        /// <param name="cause">
        ///            the cause (which is saved for later retrieval by the
        ///            <seealso cref="#getCause()"/> method). (A <tt>null</tt> value is
        ///            permitted, and indicates that the cause is nonexistent or
        ///            unknown.) </param>
        public InvalidAttributeValueException(string message, Exception cause)
            : base(message, cause)
        {
        }
    }
    #endregion

    #region InvalidCredentialException
    /// <summary>
    /// InvalidCredentialException signals that user authentication failed.
    /// <p/>
    /// This exception is thrown by Connector if authentication failed. For example,
    /// a <code>Connector</code> throws this exception if the user entered an
    /// incorrect password.
    /// </summary>
    public class InvalidCredentialException : ConnectorSecurityException
    {

        /// <seealso cref= ConnectorException#ConnectorException() </seealso>
        public InvalidCredentialException()
            : base()
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String) </seealso>
        public InvalidCredentialException(string message)
            : base(message)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(Throwable) </seealso>
        public InvalidCredentialException(Exception ex)
            : base(ex)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String, Throwable) </seealso>
        public InvalidCredentialException(string message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region InvalidPasswordException
    public class InvalidPasswordException : InvalidCredentialException
    {
        public InvalidPasswordException()
            : base()
        {
        }

        public InvalidPasswordException(String message)
            : base(message)
        {
        }

        public InvalidPasswordException(Exception ex)
            : base(ex)
        {
        }

        public InvalidPasswordException(String message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region OperationTimeoutException
    public class OperationTimeoutException : ConnectorException
    {
        public OperationTimeoutException()
            : base()
        {
        }

        public OperationTimeoutException(String msg)
            : base(msg)
        {
        }

        public OperationTimeoutException(Exception e)
            : base(e)
        {
        }

        public OperationTimeoutException(String msg, Exception e)
            : base(msg, e)
        {
        }

    }
    #endregion

    #region PasswordExpiredException
    /// <summary>
    /// PasswordExpiredException signals that a user password has expired.
    /// <para>
    /// This exception is thrown by Connector when they determine that a password has
    /// expired. For example, a <code>Connector</code>, after successfully
    /// authenticating a user, may determine that the user's password has expired. In
    /// this case the <code>Connector</code> throws this exception to notify the
    /// application. The application can then take the appropriate steps to notify
    /// the user.
    /// 
    /// </para>
    /// </summary>
    public class PasswordExpiredException : InvalidPasswordException
    {
        [NonSerialized]
        private Uid _uid;

        /// <seealso cref= ConnectorException#ConnectorException() </seealso>
        public PasswordExpiredException()
            : base()
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String) </seealso>
        public PasswordExpiredException(string message)
            : base(message)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(Throwable) </seealso>
        public PasswordExpiredException(Exception ex)
            : base(ex)
        {
        }

        /// <seealso cref= ConnectorException#ConnectorException(String, Throwable) </seealso>
        public PasswordExpiredException(string message, Exception ex)
            : base(message, ex)
        {
        }

        public Uid Uid
        {
            get
            {
                return _uid;
            }
            /// <summary>
            /// Sets the Uid. Connectors who throw this exception from their
            /// <seealso cref="AuthenticationApiOp"/> should set the account Uid if available.
            /// </summary>
            /// <param name="uid">
            ///            The uid. </param>
            /// <returns> A reference to this. </returns>
            set
            {
                _uid = value;
            }
        }
    }
    #endregion

    #region PermissionDeniedException
    public class PermissionDeniedException : ConnectorSecurityException
    {
        public PermissionDeniedException()
            : base()
        {
        }

        public PermissionDeniedException(String message)
            : base(message)
        {
        }

        public PermissionDeniedException(Exception ex)
            : base(ex)
        {
        }

        public PermissionDeniedException(String message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region PreconditionFailedException


    /// <summary>
    /// PreconditionFailedException is thrown to indicate that a resource's current
    /// version does not match the version provided.
    /// 
    /// Equivalent to HTTP status: 412 Precondition Failed.
    /// </summary>
    /// <remarks>Since 1.4</remarks>
    public class PreconditionFailedException : ConnectorException
    {
        /// <summary>
        /// Constructs a new PreconditionFailedException exception with
        /// <code>null</code> as its detail message. The cause is not initialized,
        /// and may subsequently be initialized by a call to <seealso cref="#initCause"/>.
        /// </summary>
        public PreconditionFailedException()
            : base()
        {
        }

        /// <summary>
        /// Constructs a new PreconditionFailedException exception with the specified
        /// detail message. The cause is not initialized, and may subsequently be
        /// initialized by a call to <seealso cref="#initCause"/>.
        /// </summary>
        /// <param name="message">
        ///            the detail message. The detail message is is a String that
        ///            describes this particular exception and saved for later
        ///            retrieval by the <seealso cref="#getMessage()"/> method. </param>
        public PreconditionFailedException(string message)
            : base(message)
        {
        }

        /// <summary>
        /// Constructs a new PreconditionFailedException exception with the specified
        /// cause and a detail message of
        /// <tt>(cause==null ? null : cause.toString())</tt> (which typically
        /// contains the class and detail message of <tt>cause</tt>). This
        /// constructor is useful for InvalidAccountException exceptions that are
        /// little more than wrappers for other throwables.
        /// </summary>
        /// <param name="cause">
        ///            the cause (which is saved for later retrieval by the
        ///            <seealso cref="#getCause()"/> method). (A <tt>null</tt> value is
        ///            permitted, and indicates that the cause is nonexistent or
        ///            unknown.) </param>
        public PreconditionFailedException(Exception cause)
            : base(cause)
        {
        }

        /// <summary>
        /// Constructs a new PreconditionFailedException exception with the specified
        /// detail message and cause.
        /// <para>
        /// Note that the detail message associated with <code>cause</code> is
        /// <i>not</i> automatically incorporated in this Connector exception's
        /// detail message.
        /// 
        /// </para>
        /// </summary>
        /// <param name="message">
        ///            the detail message (which is saved for later retrieval by the
        ///            <seealso cref="#getMessage()"/> method). </param>
        /// <param name="cause">
        ///            the cause (which is saved for later retrieval by the
        ///            <seealso cref="#getCause()"/> method). (A <tt>null</tt> value is
        ///            permitted, and indicates that the cause is nonexistent or
        ///            unknown.) </param>
        public PreconditionFailedException(string message, Exception cause)
            : base(message, cause)
        {
        }
    }
    #endregion

    #region PreconditionRequiredException
    /// <summary>
    /// PreconditionRequiredException is thrown to indicate that a resource requires
    /// a version, but no version was supplied in the request.
    /// 
    /// Equivalent to draft-nottingham-http-new-status-03 HTTP status: 428
    /// Precondition Required.
    /// </summary>
    /// <remarks>Since 1.4</remarks>
    public class PreconditionRequiredException : ConnectorException
    {
        /// <summary>
        /// Constructs a new PreconditionRequiredException exception with
        /// <code>null</code> as its detail message. The cause is not initialized,
        /// and may subsequently be initialized by a call to <seealso cref="#initCause"/>.
        /// </summary>
        public PreconditionRequiredException()
            : base()
        {
        }

        /// <summary>
        /// Constructs a new PreconditionRequiredException exception with the
        /// specified detail message. The cause is not initialized, and may
        /// subsequently be initialized by a call to <seealso cref="#initCause"/>.
        /// </summary>
        /// <param name="message">
        ///            the detail message. The detail message is is a String that
        ///            describes this particular exception and saved for later
        ///            retrieval by the <seealso cref="#getMessage()"/> method. </param>
        public PreconditionRequiredException(string message)
            : base(message)
        {
        }

        /// <summary>
        /// Constructs a new PreconditionRequiredException exception with the
        /// specified cause and a detail message of
        /// <tt>(cause==null ? null : cause.toString())</tt> (which typically
        /// contains the class and detail message of <tt>cause</tt>). This
        /// constructor is useful for InvalidAccountException exceptions that are
        /// little more than wrappers for other throwables.
        /// </summary>
        /// <param name="cause">
        ///            the cause (which is saved for later retrieval by the
        ///            <seealso cref="#getCause()"/> method). (A <tt>null</tt> value is
        ///            permitted, and indicates that the cause is nonexistent or
        ///            unknown.) </param>
        public PreconditionRequiredException(Exception cause)
            : base(cause)
        {
        }

        /// <summary>
        /// Constructs a new PreconditionRequiredException exception with the
        /// specified detail message and cause.
        /// <para>
        /// Note that the detail message associated with <code>cause</code> is
        /// <i>not</i> automatically incorporated in this Connector exception's
        /// detail message.
        /// 
        /// </para>
        /// </summary>
        /// <param name="message">
        ///            the detail message (which is saved for later retrieval by the
        ///            <seealso cref="#getMessage()"/> method). </param>
        /// <param name="cause">
        ///            the cause (which is saved for later retrieval by the
        ///            <seealso cref="#getCause()"/> method). (A <tt>null</tt> value is
        ///            permitted, and indicates that the cause is nonexistent or
        ///            unknown.) </param>
        public PreconditionRequiredException(string message, Exception cause)
            : base(message, cause)
        {
        }
    }
    #endregion

    #region RetryableException
    /// <summary>
    /// RetryableException indicates that a failure may be temporary, and that
    /// retrying the same request may be able to succeed in the future.
    /// </summary>
    /// <remarks>Since 1.4</remarks>
    public class RetryableException : ConnectorException
    {
        /// <summary>
        /// Constructs a new RetryableException exception with the specified cause
        /// and a detail message of <tt>(cause==null ? null : cause.toString())</tt>
        /// (which typically contains the class and detail message of <tt>cause</tt>
        /// ). This constructor is useful for InvalidAccountException exceptions that
        /// are little more than wrappers for other throwables.
        /// </summary>
        /// <param name="cause">
        ///            the cause (which is saved for later retrieval by the
        ///            <seealso cref="#getCause()"/> method). (A <tt>null</tt> value is
        ///            permitted, and indicates that the cause is nonexistent or
        ///            unknown.) </param>
        private RetryableException(Exception cause)
            : base(cause)
        {
        }

        /// <summary>
        /// Constructs a new RetryableException exception with the specified detail
        /// message and cause.
        /// <para>
        /// Note that the detail message associated with <code>cause</code> is
        /// <i>not</i> automatically incorporated in this Connector exception's
        /// detail message.
        /// 
        /// </para>
        /// </summary>
        /// <param name="message">
        ///            the detail message (which is saved for later retrieval by the
        ///            <seealso cref="#getMessage()"/> method). </param>
        /// <param name="cause">
        ///            the cause (which is saved for later retrieval by the
        ///            <seealso cref="#getCause()"/> method). (A <tt>null</tt> value is
        ///            permitted, and indicates that the cause is nonexistent or
        ///            unknown.) </param>
        private RetryableException(string message, Exception cause)
            : base(message, cause)
        {
        }

        /// <summary>
        /// If <seealso cref="Exception"/> parameter passed in is a <seealso cref="RetryableException"/>
        /// it is simply returned. Otherwise the <seealso cref="Exception"/> is wrapped in a
        /// <code>RetryableException</code> and returned.
        /// </summary>
        /// <param name="message">
        ///            the detail message (which is saved for later retrieval by the
        ///            <seealso cref="#getMessage()"/> method). </param>
        /// <param name="cause">
        ///            Exception to wrap or cast and return. </param>
        /// <returns> a <code>RuntimeException</code> that either <i>is</i> the
        ///         specified exception or <i>contains</i> the specified exception. </returns>
        public static RetryableException Wrap(string message, Exception cause)
        {
            // don't bother to wrap a exception that is already a
            // RetryableException.
            if (cause is RetryableException)
            {
                return (RetryableException)cause;
            }

            if (null != message)
            {
                return new RetryableException(message, cause);
            }
            else
            {
                return new RetryableException(cause);
            }
        }

        /// <summary>
        /// Constructs a new RetryableException which signals partial success of
        /// <code>create</code> operation.
        /// 
        /// This should be called inside
        /// <seealso cref="Org.IdentityConnectors.Framework.Spi.Operations.CreateOp#create(org.identityconnectors.framework.common.objects.ObjectClass, java.util.Set, org.identityconnectors.framework.common.objects.OperationOptions)"/>
        /// implementation to signal that the create was not completed but the object
        /// was created with <code>Uid</code> and Application should call the
        /// <seealso cref="Org.IdentityConnectors.Framework.Spi.Operations.UpdateOp#update(org.identityconnectors.framework.common.objects.ObjectClass, org.identityconnectors.framework.common.objects.Uid, java.util.Set, org.identityconnectors.framework.common.objects.OperationOptions)"/>
        /// method now.
        /// <p/>
        /// Use this only if the created object can not be deleted. The best-practice
        /// should always be the Connector implementation reverts the changes if the
        /// operation failed.
        /// </summary>
        /// <param name="message">
        ///            the detail message (which is saved for later retrieval by the
        ///            <seealso cref="#getMessage()"/> method).
        /// </param>
        /// <param name="uid">
        ///            the new object's Uid. </param>
        /// <returns> a <code>RetryableException</code> that either <i>is</i> the
        ///         specified exception or <i>contains</i> the specified exception. </returns>
        public static RetryableException Wrap(string message, Uid uid)
        {
            return new RetryableException(message, new AlreadyExistsException().InitUid(Assertions.NullChecked(uid, "Uid")));
        }
    }
    #endregion

    #region UnknownUidException
    public class UnknownUidException : InvalidCredentialException
    {
        const string MSG = "Object with Uid '{0}' and ObjectClass '{1}' does not exist!";

        public UnknownUidException()
            : base()
        {
        }

        public UnknownUidException(Uid uid, ObjectClass objclass) :
            base(String.Format(MSG, uid, objclass))
        {
        }

        public UnknownUidException(String message)
            : base(message)
        {
        }

        public UnknownUidException(Exception ex)
            : base(ex)
        {
        }

        public UnknownUidException(String message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion
}