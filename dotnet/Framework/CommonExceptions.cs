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
using System;
using Org.IdentityConnectors.Framework.Common.Objects;

namespace Org.IdentityConnectors.Framework.Common.Exceptions
{
    #region AlreadyExistsException
    public class AlreadyExistsException : ConnectorException
    {
        public AlreadyExistsException()
            : base()
        {
        }

        public AlreadyExistsException(String message)
            : base(message)
        {

        }

        public AlreadyExistsException(Exception ex)
            : base(ex)
        {
        }

        public AlreadyExistsException(String message, Exception ex)
            : base(message, ex)
        {
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
    public class ConnectionBrokenException : ConnectorIOException
    {
        public ConnectionBrokenException()
            : base()
        {
        }

        public ConnectionBrokenException(String msg)
            : base(msg)
        {
        }

        public ConnectionBrokenException(Exception ex)
            : base(ex)
        {
        }

        public ConnectionBrokenException(String message, Exception ex)
            : base(message, ex)
        {
        }
    }
    #endregion

    #region ConnectionFailedException
    public class ConnectionFailedException : ConnectorIOException
    {
        public ConnectionFailedException()
            : base()
        {
        }

        public ConnectionFailedException(String msg)
            : base(msg)
        {
        }

        public ConnectionFailedException(Exception ex)
            : base(ex)
        {
        }

        public ConnectionFailedException(String message, Exception ex)
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

    #region InvalidCredentialException
    public class InvalidCredentialException : ConnectorSecurityException
    {
        public InvalidCredentialException()
            : base()
        {
        }

        public InvalidCredentialException(String message)
            : base(message)
        {
        }

        public InvalidCredentialException(Exception ex)
            : base(ex)
        {
        }

        public InvalidCredentialException(String message, Exception ex)
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
    public class PasswordExpiredException : InvalidPasswordException
    {
        private Uid _uid;

        public PasswordExpiredException()
            : base()
        {
        }

        public PasswordExpiredException(String message)
            : base(message)
        {
        }

        public PasswordExpiredException(Exception ex)
            : base(ex)
        {
        }

        public PasswordExpiredException(String message, Exception ex)
            : base(message, ex)
        {
        }

        public Uid Uid
        {
            get
            {
                return _uid;
            }
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