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

using System.Reflection;

namespace Org.IdentityConnectors.Common
{
    /// <summary>
    /// The equivalent of java's Class&lt;? extends...%gt; syntax.
    /// Allows you to restrict a Type to a certain class hierarchy.
    /// </summary>
    public sealed class SafeType<T>
        where T : class
    {
        private readonly Type _rawType;

        /// <summary>
        /// Make private so no one can create directly
        /// </summary>
        private SafeType(Type rawType)
        {
            if (!ReflectionUtil.IsParentTypeOf(typeof(T), rawType))
            {
                throw new ArgumentException("Type: " + rawType + " is not a subclass of" + typeof(T));
            }
            _rawType = rawType;
        }

        /// <summary>
        /// Returns the SafeType for a given raw type. 
        /// NOTE: If possible use Get<U>() instead since it is statically
        /// checked at compile time.
        /// </summary>
        /// <param name="type"></param>
        /// <returns></returns>
        public static SafeType<T> ForRawType(Type type)
        {
            return new SafeType<T>(type);
        }

        /// <summary>
        /// Gets an instance of the safe type in a type-safe fashion.
        /// </summary>
        /// <returns>The instance of the safe type</returns>
        public static SafeType<T> Get<U>()
            where U : T
        {
            return new SafeType<T>(typeof(U));
        }

        /// <summary>
        /// Gets an instance of the safe type in a type-safe fashion from an object.
        /// </summary>
        /// <returns>The instance of the safe type</returns>
        public static SafeType<T> Get(T obj)
        {
            return new SafeType<T>(obj.GetType());
        }

        /// <summary>
        /// Returns the generic type definition corresponding to this type.
        /// Will return the same type if this is already a generic type.
        /// </summary>
        /// <returns></returns>
        public SafeType<T> GetTypeErasure()
        {
            return SafeType<T>.ForRawType(ReflectionUtil.GetTypeErasure(RawType));
        }

        /// <summary>
        /// Returns the underlying C# type
        /// </summary>
        public Type RawType
        {
            get
            {
                return _rawType;
            }
        }

        /// <summary>
        /// Creates a new instance of the given type
        /// </summary>
        /// <returns>The new instance</returns>
        public T CreateInstance()
        {
            return (T)Activator.CreateInstance(RawType);
        }

        /// <summary>
        /// Returns true iff these represent the same underlying type
        /// and the SafeType has the same parent type.
        /// </summary>
        /// <param name="o">The other</param>
        /// <returns>true iff these represent the same underylying type
        /// and the TypeGroup has the same parent type</returns>
        public override bool Equals(object o)
        {
            if (o is SafeType<T>)
            {
                SafeType<T> other = (SafeType<T>)o;
                return RawType.Equals(other.RawType);
            }
            return false;
        }
        /// <summary>
        /// Returns a hash of the type
        /// </summary>
        /// <returns>a hash of the type</returns>
        public override int GetHashCode()
        {
            return RawType.GetHashCode();
        }
        /// <summary>
        /// Returns a string representation of the member type
        /// </summary>
        /// <returns>a string representation of the member type</returns>
        public override string ToString()
        {
            return RawType.ToString();
        }
    }
}