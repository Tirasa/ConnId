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
using System.Collections.Generic;
using System.Globalization;
using Org.IdentityConnectors.Common;

namespace Org.IdentityConnectors.Test.Common
{
    /// <summary>
    /// Encapsulates a read-only bag of properties, which can be accessed in a type-safe manner.
    /// </summary>
    /// <remarks>
    /// The simplest way to obtain a required (i.e., the property must be in the bag, otherwise an exception is thrown)
    /// property value is <see cref="PropertyBag.GetProperty{T}(string)"/>.
    /// If the property is not a required one, the <see cref="PropertyBag.GetProperty{T}(string, T)"/> method can be used,
    /// which also takes a default value which is returned when the property is not present in the bag.
    /// </remarks>
    public sealed class PropertyBag
    {
        #region Member variables
        private readonly Dictionary<string, object> _bag;
        #endregion

        #region Constructors
        /// <summary>
        /// Initializes a new instance of the <see cref="PropertyBag"/> class with the properties.
        /// </summary>
        /// <param name="bag">The properties contained in the bag.</param>
        internal PropertyBag(IDictionary<string, object> bag)
        {
            _bag = new Dictionary<string, object>(bag);
        }
        #endregion

        #region Methods
        /// <summary>
        /// Gets the value of a required property defined by <paramref name="name"/> in a type-safe manner.
        /// </summary>
        /// <typeparam name="T">The type of the property to get.</typeparam>
        /// <param name="name">The name of the property.</param>
        /// <returns>The value of the property in bag; the value might be <c>null</c>.</returns>
        /// <exception cref="ArgumentException">Thrown when no property found defined by <paramref name="name"/></exception>
        /// <exception cref="InvalidCastException">Thrown when the property exists, but its value is not of type <typeparamref name="T"/>.</exception>
        /// <remarks>See <see cref="PropertyBag.CastValue{T}(string)"/> for details on the types that can be defined in <typeparamref name="T"/>.</remarks>
        public T GetProperty<T>(string name)
        {
            if (!_bag.ContainsKey(name))
            {
                throw new ArgumentException(string.Format(CultureInfo.InvariantCulture, @"Property named ""{0}"" not found in bag.", name));
            }

            return CastValue<T>(name);
        }

        /// <summary>
        /// Casts the value of a property defined by <paramref name="name"/> to the type <typeparamref name="T"/>.
        /// </summary>
        /// <typeparam name="T">The type, the property must be cast to.</typeparam>
        /// <param name="name">The name of the property.</param>
        /// <returns>The value of the property in bag cast to type <typeparamref name="T"/>; the value might be <c>null</c>.</returns>
        /// <exception cref="InvalidCastException">Thrown when the value of the property is not of type <typeparamref name="T"/>.</exception>
        /// <remarks>The property must be in the bag!
        /// <para>
        /// The .Net BCL does not provide wrappers for primitive classes, hence to make sure that a property containing a <c>null</c> value
        /// cannot break your code use a nullable primitive type.
        /// <example>For example:
        ///     <code>
        ///         PropertyBag bag = TestHelpers.GetProperties(typeof(yourConnector));
        ///         int? mightBeNull = bag.GetProperty<int?>("property name");
        ///     </code>
        /// </example>
        /// </para>
        /// </remarks>
        private T CastValue<T>(string name)
        {
            object value = _bag[name];

            //if the value of the property is null then just return null, there is no need for type checking
            if ((value != null) &&
                !(value is T))      //otherwise check if value is an instance of T
            {
                throw new InvalidCastException(string.Format(CultureInfo.InvariantCulture,
                                                             @"Property named ""{0}"" is of type ""{1}"" but expected type was ""{2}""",
                                                             name, value.GetType(), typeof (T)));
            }

            return (T)value;
        }

        /// <summary>
        /// Gets a property value, returning a default value when no property with the specified name exists in the bag.
        /// </summary>
        /// <typeparam name="T">The type of the property to get.</typeparam>
        /// <param name="name">The name of the property.</param>
        /// <param name="def">The default value returned when no property with the specified name exists in the bag.</param>
        /// <returns>The value of the property in bag cast to type <typeparamref name="T"/> or the default value <paramref name="def"/>;
        /// the value might be <c>null</c>.</returns>
        /// <exception cref="InvalidCastException">Thrown when the property exists, but its value is not of type <typeparamref name="T"/>.</exception>
        /// <remarks>See <see cref="PropertyBag.CastValue{T}(string)"/> for details on the types that can be defined in <typeparamref name="T"/>.</remarks>
        public T GetProperty<T>(string name, T def)
        {
            if (!_bag.ContainsKey(name))
            {
                return def;
            }
            return CastValue<T>(name);
        }

        /// <summary>
        /// Gets a required property value known to be of type <see cref="String"/>.
        /// </summary>
        /// <param name="name">The name of the property.</param>
        /// <returns>The value of the property in bag; the value might be <c>null</c>.</returns>
        /// <exception cref="ArgumentException">Thrown when no property found defined by <paramref name="name"/></exception>
        /// <exception cref="InvalidCastException">Thrown when the property exists, but its value is not of type <see cref="String"/>.</exception>
        /// <remarks> The method expects that the value is an instance of <see cref="String"/>. It does not attempt to
        /// call <see cref="Object.ToString()"/> on the value.</remarks>
        public string GetStringProperty(string name)
        {
            return GetProperty<string>(name);
        }

        /// <summary>
        /// Returns a key-value pair collection that represents the current <see cref="T:PropertyBag"/>.
        /// </summary>
        /// <returns>A <see cref="IDictionary{T,U}"/> instance that represents the current <see cref="T:PropertyBag"/>.</returns>
        internal IDictionary<string, object> ToDictionary()
        {
            return CollectionUtil.NewDictionary(_bag);
        }
        #endregion
    }
}
