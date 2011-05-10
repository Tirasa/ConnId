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
using System.Security;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.Text;

using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Security;

using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Spi.Operations;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Common.Serializer;
using Org.IdentityConnectors.Framework.Common.Objects.Filters;
namespace Org.IdentityConnectors.Framework.Common.Objects
{
    #region NameUtil
    internal static class NameUtil
    {
        public static bool IsSpecialName(String name)
        {
            return (name.StartsWith("__") && name.EndsWith("__"));
        }

        public static string CreateSpecialName(string name)
        {
            if (StringUtil.IsBlank(name))
            {
                const string ERR = "Name parameter must not be blank!";
                throw new ArgumentException(ERR);
            }
            return "__" + name + "__";
        }

        public static bool NamesEqual(string name1, string name2)
        {
            return name1.ToUpper(CultureInfoCache.Instance).Equals(
                name2.ToUpper(CultureInfoCache.Instance));
        }

        public static int GetNameHashCode(string name)
        {
            return name.ToUpper(CultureInfoCache.Instance).GetHashCode();
        }
    }
    #endregion

    #region ConnectorAttributeUtil
    public static class ConnectorAttributeUtil
    {
        /// <summary>
        /// Gets the string value from the single value attribute.
        /// </summary>
        /// <param name="attr">ConnectorAttribute to retrieve the string value from.</param>
        /// <returns>null if the value is null otherwise the string value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an string.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi valued instead of single valued.</exception>
        public static string GetStringValue(ConnectorAttribute attr)
        {
            return (string)GetSingleValue(attr);
        }

        /// <summary>
        /// Gets the string value from the single value attribute.
        /// </summary>
        /// <param name="attr">ConnectorAttribute to retrieve the string value from.</param>
        /// <returns>null if the value is null otherwise the string value for the
        /// attribute.</returns>
        /// <exception cref="ArgumentException">iff the attribute is a multi valued instead of single valued.</exception>
        public static string GetAsStringValue(ConnectorAttribute attr)
        {
            object obj = GetSingleValue(attr);
            return obj != null ? obj.ToString() : null;
        }

        public static GuardedString GetGuardedStringValue(ConnectorAttribute attr)
        {
            object obj = GetSingleValue(attr);
            return obj != null ? (GuardedString)obj : null;
        }
        /// <summary>
        /// Gets the integer value from the single value attribute.
        /// </summary>
        /// <param name="attr">ConnectorAttribute to retrieve the integer value from.</param>
        /// <returns>null if the value is null otherwise the integer value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an integer.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi valued instead of single valued.</exception>
        public static int? GetIntegerValue(ConnectorAttribute attr)
        {
            object obj = GetSingleValue(attr);
            return obj != null ? (int?)obj : null;
        }

        /// <summary>
        /// Gets the long value from the single value attribute.
        /// </summary>
        /// <param name="attr">ConnectorAttribute to retrieve the long value from.</param>
        /// <returns>null if the value is null otherwise the long value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an long.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi valued instead of single valued.</exception>
        public static long? GetLongValue(ConnectorAttribute attr)
        {
            Object obj = GetSingleValue(attr);
            return obj != null ? (long?)obj : null;
        }

        /// <summary>
        /// Gets the date value from the single value attribute.
        /// </summary>
        /// <param name="attr">ConnectorAttribute to retrieve the date value from.</param>
        /// <returns>null if the value is null otherwise the date value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an long.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi valued instead of single valued.</exception>
        public static DateTime? GetDateTimeValue(ConnectorAttribute attr)
        {
            long? val = GetLongValue(attr);
            if (val != null)
            {
                return DateTimeUtil.GetDateTimeFromUtcMillis(val.Value);
            }
            return null;
        }

        /// <summary>
        /// Gets the integer value from the single value attribute.
        /// </summary>
        /// <param name="attr">ConnectorAttribute to retrieve the integer value from.</param>
        /// <returns>null if the value is null otherwise the integer value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an integer.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi valued instead of single valued.</exception>
        public static double? GetDoubleValue(ConnectorAttribute attr)
        {
            Object obj = GetSingleValue(attr);
            return obj != null ? (double?)obj : null;
        }

        public static bool? GetBooleanValue(ConnectorAttribute attr)
        {
            object obj = GetSingleValue(attr);
            return obj != null ? (bool?)obj : null;
        }

        /// <summary>
        /// Get the single value from the ConnectorAttribute.
        /// </summary>
        /// <remarks>
        /// Return
        /// null if the attribute's list of values is null or empty.
        /// </remarks>
        public static object GetSingleValue(ConnectorAttribute attr)
        {
            Object ret = null;
            IList<Object> val = attr.Value;
            if (val != null && val.Count > 0)
            {
                // make sure this only called for single value..
                if (val.Count > 1)
                {
                    const string MSG = "The method is only for single value attributes.";
                    throw new ArgumentException(MSG);
                }
                ret = val[0];
            }
            return ret;
        }

        /// <summary>
        /// Transform a <code>Collection</code> of <code></code>ConnectorAttribute} instances into a {@link Map}.
        /// The key to each element in the map is the <i>name</i> of an <code>ConnectorAttribute</code>.
        /// The value of each element in the map is the <code>ConnectorAttribute</code> instance with that name.
        /// </summary>
        /// <param name="attributes"></param>
        /// <returns></returns>
        public static IDictionary<string, ConnectorAttribute> ToMap(
            ICollection<ConnectorAttribute> attributes)
        {
            IDictionary<string, ConnectorAttribute> ret =
                new Dictionary<string, ConnectorAttribute>(
                    StringComparer.OrdinalIgnoreCase);
            foreach (ConnectorAttribute attr in attributes)
            {
                ret[attr.Name] = attr;
            }
            return ret;
        }

        /// <summary>
        /// Get the <see cref="Uid" /> from the attribute set.
        /// </summary>
        /// <param name="attrs">set of <see cref="ConnectorAttribute" />s that may contain a <see cref="Uid" />.</param>
        /// <returns>null if the set does not contain a <see cref="Uid" /> object the first
        /// one found.</returns>
        public static Uid GetUidAttribute(ICollection<ConnectorAttribute> attrs)
        {
            return (Uid)Find(Uid.NAME, attrs);
        }

        /// <summary>
        /// Filters out all special attributes from the set.
        /// </summary>
        /// <remarks>
        /// These special attributes
        /// include <see cref="Password" />, <see cref="Uid" /> etc..
        /// </remarks>
        /// <param name="attrs">set of <see cref="ConnectorAttribute" />s to filter out the operational and
        /// default attributes.</param>
        /// <returns>a set that only contains plain attributes or empty.</returns>
        public static ICollection<ConnectorAttribute> GetBasicAttributes(ICollection<ConnectorAttribute> attrs)
        {
            ICollection<ConnectorAttribute> ret = new HashSet<ConnectorAttribute>();
            foreach (ConnectorAttribute attr in attrs)
            {
                // note this is dangerous because we need to be consistent
                // in the naming of special attributes.
                if (!IsSpecial(attr))
                {
                    ret.Add(attr);
                }
            }
            return ret;
        }

        /// <summary>
        /// Filter out any basic attributes from the specified set, leaving only
        /// special attributes.
        /// </summary>
        /// <remarks>
        /// Special attributes include <see cref="Name" />, <see cref="Uid" />,
        /// and <see cref="OperationalAttributes" />.
        /// </remarks>
        /// <param name="attrs">set of <see cref="ConnectorAttribute" />s to filter out the basic attributes</param>
        /// <returns>a set that only contains special attributes or an empty set if
        /// there are none.</returns>
        public static ICollection<ConnectorAttribute> GetSpecialAttributes(ICollection<ConnectorAttribute> attrs)
        {
            ICollection<ConnectorAttribute> ret = new HashSet<ConnectorAttribute>();
            foreach (ConnectorAttribute attr in attrs)
            {
                if (IsSpecial(attr))
                {
                    ret.Add(attr);
                }
            }
            return ret;
        }

        /// <summary>
        /// Returns a mutable copy of the original set with the uid attribute removed.
        /// </summary>
        /// <param name="attrs">The original set. Must not be null.</param>
        /// <returns>A mutable copy of the original set with the uid attribute removed.</returns>
        public static ICollection<ConnectorAttribute> FilterUid(ICollection<ConnectorAttribute> attrs)
        {
            Assertions.NullCheck(attrs, "attrs");
            HashSet<ConnectorAttribute> ret = new HashSet<ConnectorAttribute>();
            foreach (ConnectorAttribute attr in attrs)
            {
                if (!(attr is Uid))
                {
                    ret.Add(attr);
                }
            }
            return ret;
        }

        /// <summary>
        /// Returns a mutable copy of the original set with the uid attribute added.
        /// </summary>
        /// <param name="attrs">The original set. Must not be null.</param>
        /// <param name="uid">The uid. Must not be null.</param>
        /// <returns>A mutable copy of the original set with the uid attribute added.</returns>
        public static ICollection<ConnectorAttribute> AddUid(ICollection<ConnectorAttribute> attrs, Uid uid)
        {
            Assertions.NullCheck(attrs, "attrs");
            Assertions.NullCheck(uid, "uid");
            HashSet<ConnectorAttribute> ret = new HashSet<ConnectorAttribute>(attrs);
            ret.Add(uid);
            return ret;
        }

        /// <summary>
        /// Determines if this attribute is a special attribute.
        /// </summary>
        /// <param name="attr">
        /// <see cref="ConnectorAttribute" /> to test for against.</param>
        /// <returns>true iff the attribute value is a <see cref="Uid" />,
        /// <see cref="ObjectClass" />, <see cref="Password" />, or
        /// <see cref="OperationalAttributes" />.</returns>
        /// <exception cref="NullReferenceException">iff the attribute parameter is null.</exception>
        public static bool IsSpecial(ConnectorAttribute attr)
        {
            // note this is dangerous because we need to be consistent
            // in the naming of special attributes.
            String name = attr.Name;
            return IsSpecialName(name);
        }

        /// <summary>
        /// Determines if this attribute is a special attribute.
        /// </summary>
        /// <param name="attr">
        /// <see cref="ConnectorAttribute" /> to test for against.</param>
        /// <returns>true iff the attribute value is a <see cref="Uid" />,
        /// <see cref="ObjectClass" />, <see cref="Password" />, or
        /// <see cref="OperationalAttributes" />.</returns>
        /// <exception cref="NullReferenceException">iff the attribute parameter is null.</exception>
        public static bool IsSpecial(ConnectorAttributeInfo attr)
        {
            String name = attr.Name;
            return IsSpecialName(name);
        }

        /// <summary>
        /// Determines whether the specified attribute name is special in the
        /// sense of <see cref="ConnectorAttributeUtil.CreateSpecialName"/>.
        /// </summary>
        /// <param name="name">the name of the attribute to test</param>
        /// <returns>true iff the attribute name is special</returns>
        public static bool IsSpecialName(String name)
        {
            return NameUtil.IsSpecialName(name);
        }

        /// <summary>
        /// Creates the special naming for operational type attributes.
        /// </summary>
        /// <param name="name">string to make special</param>
        /// <returns>name constructed for use as an operational attribute.</returns>
        public static string CreateSpecialName(string name)
        {
            return NameUtil.CreateSpecialName(name);
        }

        /// <summary>
        /// Compares two attribute names for equality.
        /// </summary>
        /// <param name="name1">the first attribute name</param>
        /// <param name="name2">the second attribute name</param>
        /// <returns>true iff the two attribute names are equal</returns>
        public static bool NamesEqual(string name1, string name2)
        {
            return NameUtil.NamesEqual(name2, name2);
        }

        /// <summary>
        /// Gets the 'Name' attribute from a set of ConnectorAttributes.
        /// </summary>
        /// <param name="attrs">set of attributes to search against.</param>
        /// <returns>the 'Name' attribute it if exsist otherwise<code>null</code></returns>
        public static Name GetNameFromAttributes(ICollection<ConnectorAttribute> attrs)
        {
            return (Name)Find(Name.NAME, attrs);
        }


        /// <summary>
        /// Find the <see cref="ConnectorAttribute" /> of the given name in the <see cref="Set" />.
        /// </summary>
        /// <param name="name">
        /// <see cref="ConnectorAttribute" />'s name to search for.</param>
        /// <param name="attrs">
        /// <see cref="Set" /> of attribute to search.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" /> with the specified otherwise <code>null</code>.</returns>
        public static ConnectorAttribute Find(string name, ICollection<ConnectorAttribute> attrs)
        {
            Assertions.NullCheck(name, "name");
            ICollection<ConnectorAttribute> attributes = CollectionUtil.NullAsEmpty(attrs);
            foreach (ConnectorAttribute attr in attributes)
            {
                if (attr.Is(name))
                {
                    return attr;
                }
            }
            return null;
        }
        /// <summary>
        /// Get the password value from the provided set of <see cref="ConnectorAttribute" />s.
        /// </summary>
        public static GuardedString GetPasswordValue(ICollection<ConnectorAttribute> attrs)
        {
            ConnectorAttribute pwd = Find(OperationalAttributes.PASSWORD_NAME, attrs);
            return (pwd == null) ? null : GetGuardedStringValue(pwd);
        }

        /// <summary>
        /// Get the current password value from the provided set of <see cref="ConnectorAttribute" />s.
        /// </summary>
        /// <param name="attrs">Set of <see cref="ConnectorAttribute" />s that may contain the current password
        /// <see cref="OperationalAttributes.CURRENT_PASSWORD_NAME" />
        /// <see cref="ConnectorAttribute" />.</param>
        /// <returns>
        /// <code>null</code> if it does not exist in the <see cref="Set" /> else
        /// the value.</returns>
        public static GuardedString GetCurrentPasswordValue(ICollection<ConnectorAttribute> attrs)
        {
            ConnectorAttribute pwd = Find(OperationalAttributes.CURRENT_PASSWORD_NAME, attrs);
            return (pwd == null) ? null : GetGuardedStringValue(pwd);
        }
        /// <summary>
        /// Determine if the <see cref="ConnectorObject" /> is locked out.
        /// </summary>
        /// <remarks>
        /// By getting the
        /// value of the <see cref="OperationalAttributes.LOCK_OUT_NAME" />.
        /// </remarks>
        /// <param name="obj">
        /// <see cref="ConnectorObject" /> object to inspect.</param>
        /// <exception cref="NullReferenceException">iff the parameter 'obj' is <code>null</code>.</exception>
        /// <returns>
        /// <code>null</code> if the attribute does not exist otherwise to
        /// value of the <see cref="ConnectorAttribute" />.</returns>
        public static bool? IsLockedOut(ConnectorObject obj)
        {
            ConnectorAttribute attr = obj.GetAttributeByName(OperationalAttributes.LOCK_OUT_NAME);
            return (attr == null) ? null : GetBooleanValue(attr);
        }

        /// <summary>
        /// Determine if the <see cref="ConnectorObject" /> is enable.
        /// </summary>
        /// <remarks>
        /// By getting the value
        /// of the <see cref="OperationalAttributes.ENABLE_NAME" />.
        /// </remarks>
        /// <param name="obj">
        /// <see cref="ConnectorObject" /> object to inspect.</param>
        /// <exception cref="IllegalStateException">if the object does not contain attribute in question.</exception>
        /// <exception cref="NullReferenceException">iff the parameter 'obj' is <code>null</code>.</exception>
        /// <returns>
        /// <code>null</code> if the attribute does not exist otherwise to
        /// value of the <see cref="ConnectorAttribute" />.</returns>
        public static bool? IsEnabled(ConnectorObject obj)
        {
            ConnectorAttribute attr = obj.GetAttributeByName(OperationalAttributes.ENABLE_NAME);
            return (attr == null) ? null : GetBooleanValue(attr);
        }

        /// <summary>
        /// Retrieve the password expiration date from the <see cref="ConnectorObject" />.
        /// </summary>
        /// <param name="obj">
        /// <see cref="ConnectorObject" /> object to inspect.</param>
        /// <exception cref="IllegalStateException">if the object does not contain attribute in question.</exception>
        /// <exception cref="NullReferenceException">iff the parameter 'obj' is <code>null</code>.</exception>
        /// <returns>
        /// <code>null</code> if the <see cref="ConnectorAttribute" /> does not exist
        /// otherwise the value of the <see cref="ConnectorAttribute" />.</returns>
        public static DateTime? GetPasswordExpirationDate(ConnectorObject obj)
        {
            DateTime? ret = null;
            ConnectorAttribute attr = obj.GetAttributeByName(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME);
            if (attr != null)
            {
                long? date = GetLongValue(attr);
                if (date != null)
                {
                    ret = DateTime.FromFileTimeUtc(date.Value);
                }
            }
            return ret;
        }
        /// <summary>
        /// Get the password expired attribute from a <see cref="Collection" /> of
        /// <see cref="ConnectorAttribute" />s.
        /// </summary>
        /// <param name="attrs">set of attribute to find the expired password
        /// <see cref="ConnectorAttribute" />.</param>
        /// <returns>
        /// <code>null</code> if the attribute does not exist and the value
        /// of the <see cref="ConnectorAttribute" /> if it does.</returns>
        public static bool? GetPasswordExpired(ICollection<ConnectorAttribute> attrs)
        {
            ConnectorAttribute pwd = Find(OperationalAttributes.PASSWORD_EXPIRED_NAME, attrs);
            return (pwd == null) ? null : GetBooleanValue(pwd);
        }

        /// <summary>
        /// Determine if the password is expired for this object.
        /// </summary>
        /// <param name="obj">
        /// <see cref="ConnectorObject" /> that should contain a password expired
        /// attribute.</param>
        /// <returns>
        /// <code>null</code> if the attribute does not exist and the value
        /// of the <see cref="ConnectorAttribute" /> if it does.</returns>
        public static bool? IsPasswordExpired(ConnectorObject obj)
        {
            ConnectorAttribute pwd = obj.GetAttributeByName(OperationalAttributes.PASSWORD_EXPIRED_NAME);
            return (pwd == null) ? null : GetBooleanValue(pwd);
        }

        /// <summary>
        /// Get the enable date from the set of attributes.
        /// </summary>
        /// <param name="attrs">set of attribute to find the enable date
        /// <see cref="ConnectorAttribute" />.</param>
        /// <returns>
        /// <code>null</code> if the attribute does not exist and the value
        /// of the <see cref="ConnectorAttribute" /> if it does.</returns>
        public static DateTime? GetEnableDate(ICollection<ConnectorAttribute> attrs)
        {
            ConnectorAttribute attr = Find(OperationalAttributes.ENABLE_DATE_NAME, attrs);
            return (attr == null) ? null : GetDateTimeValue(attr);
        }
    }
    #endregion

    #region ConnectorAttributeInfoUtil
    public static class ConnectorAttributeInfoUtil
    {
        /// <summary>
        /// Transform a <code>Collection</code> of <see cref="ConnectorAttributeInfo" /> instances into
        /// a <see cref="Map" />.
        /// </summary>
        /// <remarks>
        /// The key to each element in the map is the <i>name</i> of
        /// an <code>AttributeInfo</code>. The value of each element in the map is the
        /// <code>AttributeInfo</code> instance with that name.
        /// </remarks>
        /// <param name="attributes">set of AttributeInfo to transform to a map.</param>
        /// <returns>a map of string and AttributeInfo.</returns>
        /// <exception cref="NullReferenceException">iff the parameter <strong>attributes</strong> is
        /// <strong>null</strong>.</exception>
        public static IDictionary<string, ConnectorAttributeInfo> ToMap(
                ICollection<ConnectorAttributeInfo> attributes)
        {
            IDictionary<string, ConnectorAttributeInfo>
                ret = new Dictionary<string, ConnectorAttributeInfo>(
                    StringComparer.OrdinalIgnoreCase);
            foreach (ConnectorAttributeInfo attr in attributes)
            {
                ret[attr.Name] = attr;
            }
            return ret;
        }

        /// <summary>
        /// Find the <see cref="ConnectorAttributeInfo" /> of the given name in the <see cref="Set" />.
        /// </summary>
        /// <param name="name">
        /// <see cref="ConnectorAttributeInfo" />'s name to search for.</param>
        /// <param name="attrs">
        /// <see cref="Set" /> of AttributeInfo to search.</param>
        /// <returns>
        /// <see cref="ConnectorAttributeInfo" /> with the specified otherwise <code>null</code>.</returns>
        public static ConnectorAttributeInfo Find(string name, ICollection<ConnectorAttributeInfo> attrs)
        {
            Assertions.NullCheck(name, "name");
            ICollection<ConnectorAttributeInfo> attributes = CollectionUtil.NullAsEmpty(attrs);
            foreach (ConnectorAttributeInfo attr in attributes)
            {
                if (attr.Is(name))
                {
                    return attr;
                }
            }
            return null;
        }
    }
    #endregion

    #region BigDecimal
    /// <summary>
    /// Placeholder since C# doesn't have a BigInteger
    /// </summary>
    public sealed class BigDecimal
    {
        private BigInteger _unscaledVal;
        private int _scale;
        public BigDecimal(BigInteger unscaledVal,
                         int scale)
        {
            if (unscaledVal == null)
            {
                throw new ArgumentNullException();
            }
            _unscaledVal = unscaledVal;
            _scale = scale;
        }
        public BigInteger UnscaledValue
        {
            get
            {
                return _unscaledVal;
            }
        }
        public int Scale
        {
            get
            {
                return _scale;
            }
        }
        public override bool Equals(object o)
        {
            BigDecimal other = o as BigDecimal;
            if (other != null)
            {
                return UnscaledValue.Equals(other.UnscaledValue) &&
                    Scale == other.Scale;
            }
            return false;
        }
        public override int GetHashCode()
        {
            return _unscaledVal.GetHashCode();
        }

        public override string ToString()
        {
            return UnscaledValue.ToString();
        }
    }
    #endregion

    #region BigInteger
    /// <summary>
    /// Placeholder since C# doesn't have a BigInteger
    /// </summary>
    public sealed class BigInteger
    {
        private string _value;
        public BigInteger(string val)
        {
            if (val == null)
            {
                throw new ArgumentNullException();
            }
            _value = val;
        }
        public string Value
        {
            get
            {
                return _value;
            }
        }
        public override bool Equals(object o)
        {
            BigInteger other = o as BigInteger;
            if (other != null)
            {
                return Value.Equals(other.Value);
            }
            return false;
        }
        public override int GetHashCode()
        {
            return _value.GetHashCode();
        }
        public override string ToString()
        {
            return _value;
        }
    }
    #endregion

    #region ConnectorAttribute
    /// <summary>
    /// Represents a named collection of values within a resource object, 
    /// although the simplest case is a name-value pair (e.g., email, 
    /// employeeID).  Values can be empty, null, or set with various types.
    /// Empty and null are supported because it makes a difference on some 
    /// resources (in particular database resources). The developer of a 
    /// Connector will use an builder to construct an instance of 
    /// ConnectorAttribute.
    /// </summary>
    public class ConnectorAttribute
    {
        private readonly string _name;
        private readonly IList<object> _value;

        internal ConnectorAttribute(string name, IList<object> val)
        {
            if (StringUtil.IsBlank(name))
            {
                throw new ArgumentException("Name must not be blank!");
            }
            if (OperationalAttributes.PASSWORD_NAME.Equals(name) ||
                OperationalAttributes.CURRENT_PASSWORD_NAME.Equals(name))
            {
                // check the value..
                if (val == null || val.Count != 1)
                {
                    String MSG = "Must be a single value.";
                    throw new ArgumentException(MSG);
                }
                if (!(val[0] is GuardedString))
                {
                    const string MSG = "Password value must be an instance of GuardedString.";
                    throw new ArgumentException(MSG);
                }
            }
            _name = name;
            // copy to prevent corruption preserve null
            _value = (val == null) ? null : CollectionUtil.NewReadOnlyList<object>(val);
        }

        public string Name
        {
            get
            {
                return _name;
            }
        }

        public IList<object> Value
        {
            get
            {
                return _value;
            }
        }

        public bool Is(string name)
        {
            return NameUtil.NamesEqual(_name, name);
        }

        public sealed override bool Equals(Object obj)
        {
            // test identity
            if (this == obj)
            {
                return true;
            }
            // test for null..
            if (obj == null)
            {
                return false;
            }
            // test that the exact class matches
            if (!(GetType().Equals(obj.GetType())))
            {
                return false;
            }
            // test name field..
            ConnectorAttribute other = (ConnectorAttribute)obj;
            if (!Is(other._name))
            {
                return false;
            }

            if (!CollectionUtil.Equals(_value, other._value))
            {
                return false;
            }
            return true;
        }

        public sealed override int GetHashCode()
        {
            return NameUtil.GetNameHashCode(_name);
        }


        public override string ToString()
        {
            // poor man's consistent toString impl..
            StringBuilder bld = new StringBuilder();
            bld.Append("ConnectorAttribute: ");
            IDictionary<string, object> map = new Dictionary<string, object>();
            map["Name"] = Name;
            map["Value"] = Value;
            bld.Append(map.ToString());
            return bld.ToString();
        }
    }
    #endregion

    #region ConnectorAttributeBuilder
    public sealed class ConnectorAttributeBuilder
    {
        private const String NAME_ERROR = "Name must not be blank!";

        private string _name;
        private IList<Object> _value;

        public ConnectorAttributeBuilder()
        {
        }
        public static ConnectorAttribute Build(String name)
        {
            return new ConnectorAttributeBuilder() { Name = name }.Build();
        }
        public static ConnectorAttribute Build(String name,
                                               params Object[] args)
        {
            ConnectorAttributeBuilder bld = new ConnectorAttributeBuilder();
            bld.Name = name;
            bld.AddValue(args);
            return bld.Build();
        }
        public static ConnectorAttribute Build(String name,
                                               ICollection<object> val)
        {
            ConnectorAttributeBuilder bld = new ConnectorAttributeBuilder();
            bld.Name = name;
            bld.AddValue(val);
            return bld.Build();
        }

        public string Name
        {
            get
            {
                return _name;
            }
            set
            {
                if (StringUtil.IsBlank(value))
                {
                    throw new ArgumentException(NAME_ERROR);
                }
                _name = value;
            }
        }

        public IList<Object> Value
        {
            get
            {
                return _value == null ? null : CollectionUtil.AsReadOnlyList(_value);
            }
        }

        public ConnectorAttributeBuilder AddValue(params Object[] args)
        {
            AddValuesInternal(args);
            return this;
        }
        public ConnectorAttributeBuilder AddValue(ICollection<Object> values)
        {
            AddValuesInternal(values);
            return this;
        }

        public ConnectorAttribute Build()
        {
            if (StringUtil.IsBlank(Name))
            {
                throw new ArgumentException(NAME_ERROR);
            }
            if (Uid.NAME.Equals(_name))
            {
                return new Uid(GetSingleStringValue());
            }
            else if (Org.IdentityConnectors.Framework.Common.Objects.Name.NAME.Equals(_name))
            {
                return new Name(GetSingleStringValue());
            }
            return new ConnectorAttribute(Name, _value);
        }
        private void CheckSingleValue()
        {
            if (_value == null || _value.Count != 1)
            {
                const String MSG = "Must be a single value.";
                throw new ArgumentException(MSG);
            }
        }
        private String GetSingleStringValue()
        {
            CheckSingleValue();
            if (!(_value[0] is String))
            {
                const String MSG = "Must be single string value.";
                throw new ArgumentException(MSG);
            }
            return (String)_value[0];
        }
        private void AddValuesInternal(IEnumerable<Object> values)
        {
            if (values != null)
            {
                // make sure the list is ready to receive values.
                if (_value == null)
                {
                    _value = new List<object>();
                }
                // add each value checking to make sure its correct
                foreach (Object v in values)
                {
                    FrameworkUtil.CheckAttributeValue(v);
                    _value.Add(v);
                }
            }
        }

        // =======================================================================
        // Operational Attributes
        // =======================================================================
        /// <summary>
        /// Builds an password expiration date <see cref="ConnectorAttribute" />.
        /// </summary>
        /// <remarks>
        /// This
        /// <see cref="ConnectorAttribute" /> represents the date/time a password will expire on a
        /// resource.
        /// </remarks>
        /// <param name="dateTime">UTC time in milliseconds.</param>
        /// <returns>an <see cref="ConnectorAttribute" /> built with the pre-defined name for password
        /// expiration date.</returns>
        public static ConnectorAttribute BuildPasswordExpirationDate(DateTime dateTime)
        {
            return BuildPasswordExpirationDate(DateTimeUtil.GetUtcTimeMillis(dateTime));
        }

        /// <summary>
        /// Builds an password expiration date <see cref="ConnectorAttribute" />.
        /// </summary>
        /// <remarks>
        /// This
        /// <see cref="ConnectorAttribute" /> represents the date/time a password will expire on a
        /// resource.
        /// </remarks>
        /// <param name="dateTime">UTC time in milliseconds.</param>
        /// <returns>an <see cref="ConnectorAttribute" /> built with the pre-defined name for password
        /// expiration date.</returns>
        public static ConnectorAttribute BuildPasswordExpirationDate(long dateTime)
        {
            return Build(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME,
                    dateTime);
        }

        /// <summary>
        /// Builds the operational attribute password.
        /// </summary>
        /// <param name="password">the string that represents a password.</param>
        /// <returns>an attribute that represents a password.</returns>
        public static ConnectorAttribute BuildPassword(GuardedString password)
        {
            return Build(OperationalAttributes.PASSWORD_NAME, password);
        }

        /// <summary>
        /// Builds the operational attribute current password.
        /// </summary>
        /// <remarks>
        /// The current password
        /// indicates this a password change by the account owner and not an
        /// administrator. The use case is that an administrator password change may
        /// not keep history or validate against policy.
        /// </remarks>
        /// <param name="password">the string that represents a password.</param>
        /// <returns>an attribute that represents a password.</returns>
        public static ConnectorAttribute BuildCurrentPassword(GuardedString password)
        {
            return Build(OperationalAttributes.CURRENT_PASSWORD_NAME, password);
        }

        public static ConnectorAttribute BuildPassword(SecureString password)
        {
            return Build(OperationalAttributes.PASSWORD_NAME, new GuardedString(password));
        }
        public static ConnectorAttribute BuildCurrentPassword(SecureString password)
        {
            return Build(OperationalAttributes.CURRENT_PASSWORD_NAME, new GuardedString(password));
        }
        /// <summary>
        /// Builds ant operational attribute that either represents the object is
        /// enabled or sets in disabled depending on where its used for instance on
        /// <see cref="CreateApiOp" /> it could be used to create a disabled account.
        /// </summary>
        /// <remarks>
        /// In
        /// <see cref="SearchApiOp" /> it would show the object is enabled or disabled.
        /// </remarks>
        /// <param name="value">true indicates the object is enabled otherwise false.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" /> that determines the enable/disable state of an
        /// object.</returns>
        public static ConnectorAttribute BuildEnabled(bool val)
        {
            return Build(OperationalAttributes.ENABLE_NAME, val);
        }

        /// <summary>
        /// Builds out an operational <see cref="ConnectorAttribute" /> that determines the enable
        /// date for an object.
        /// </summary>
        /// <param name="date">The date and time to enable a particular object, or the date
        /// time an object will be enabled.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildEnableDate(DateTime date)
        {
            return BuildEnableDate(DateTimeUtil.GetUtcTimeMillis(date));
        }

        /// <summary>
        /// Builds out an operational <see cref="ConnectorAttribute" /> that determines the enable
        /// date for an object.
        /// </summary>
        /// <remarks>
        /// The time parameter is UTC in milliseconds.
        /// </remarks>
        /// <param name="date">The date and time to enable a particular object, or the date
        /// time an object will be enabled.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildEnableDate(long date)
        {
            return Build(OperationalAttributes.ENABLE_DATE_NAME, date);
        }

        /// <summary>
        /// Builds out an operational <see cref="ConnectorAttribute" /> that determines the disable
        /// date for an object.
        /// </summary>
        /// <param name="date">The date and time to enable a particular object, or the date
        /// time an object will be enabled.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildDisableDate(DateTime date)
        {
            return BuildDisableDate(DateTimeUtil.GetUtcTimeMillis(date));
        }

        /// <summary>
        /// Builds out an operational <see cref="ConnectorAttribute" /> that determines the disable
        /// date for an object.
        /// </summary>
        /// <remarks>
        /// The time parameter is UTC in milliseconds.
        /// </remarks>
        /// <param name="date">The date and time to enable a particular object, or the date
        /// time an object will be enabled.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildDisableDate(long date)
        {
            return Build(OperationalAttributes.DISABLE_DATE_NAME, date);
        }

        /// <summary>
        /// Builds the lock attribute that determines if an object is locked out.
        /// </summary>
        /// <param name="lock">true if the object is locked otherwise false.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" /> that represents the lock state of an object.</returns>
        public static ConnectorAttribute BuildLockOut(bool lck)
        {
            return Build(OperationalAttributes.LOCK_OUT_NAME, lck);
        }

        /// <summary>
        /// Builds out an operational <see cref="ConnectorAttribute" /> that determines if a password
        /// is expired or expires a password.
        /// </summary>
        /// <param name="value">from the API true expires and from the SPI its shows its
        /// either expired or not.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildPasswordExpired(bool expired)
        {
            return Build(OperationalAttributes.PASSWORD_EXPIRED_NAME, expired);
        }

        // =======================================================================
        // Pre-defined Attributes
        // =======================================================================

        /// <summary>
        /// Builds out a pre-defined <see cref="ConnectorAttribute" /> that determines the last login
        /// date for an object.
        /// </summary>
        /// <param name="date">The date and time of the last login.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildLastLoginDate(DateTime date)
        {
            return BuildLastLoginDate(DateTimeUtil.GetUtcTimeMillis(date));
        }

        /// <summary>
        /// Builds out a pre-defined <see cref="ConnectorAttribute" /> that determines the last login
        /// date for an object.
        /// </summary>
        /// <remarks>
        /// The time parameter is UTC in milliseconds.
        /// </remarks>
        /// <param name="date">The date and time of the last login.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildLastLoginDate(long date)
        {
            return Build(PredefinedAttributes.LAST_LOGIN_DATE_NAME, date);
        }

        /// <summary>
        /// Builds out a pre-defined <see cref="ConnectorAttribute" /> that determines the last
        /// password change date for an object.
        /// </summary>
        /// <param name="date">The date and time the password was changed.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildLastPasswordChangeDate(DateTime date)
        {
            return BuildLastPasswordChangeDate(DateTimeUtil.GetUtcTimeMillis(date));
        }

        /// <summary>
        /// Builds out a pre-defined <see cref="ConnectorAttribute" /> that determines the last
        /// password change date for an object.
        /// </summary>
        /// <param name="date">The date and time the password was changed.</param>
        /// <returns>
        /// <see cref="ConnectorAttribute" />
        /// </returns>
        public static ConnectorAttribute BuildLastPasswordChangeDate(long date)
        {
            return Build(PredefinedAttributes.LAST_PASSWORD_CHANGE_DATE_NAME, date);
        }

        /// <summary>
        /// Common password policy attribute where the password must be changed every
        /// so often.
        /// </summary>
        /// <remarks>
        /// The value for this attribute is milliseconds since its the
        /// lowest common denominator.
        /// </remarks>
        public static ConnectorAttribute BuildPasswordChangeInterval(long val)
        {
            return Build(PredefinedAttributes.PASSWORD_CHANGE_INTERVAL_NAME, val);
        }
    }
    #endregion

    #region ConnectorMessages
    /// <summary>
    /// Message catalog for a given connector.
    /// </summary>
    public interface ConnectorMessages
    {
        /// <summary>
        /// Formats the given message key in the current UI culture.
        /// </summary>
        /// <param name="key">The message key to format.</param>
        /// <param name="dflt">The default message if key is not found. If null, defaults
        /// to key.</param>
        /// <param name="args">Parameters with which to format the message.</param>
        /// <returns>The formatted string.</returns>
        String Format(String key, String dflt, params object[] args);
    }
    #endregion

    #region ConnectorObject
    public sealed class ConnectorObject
    {
        private readonly ObjectClass _objectClass;
        private readonly IDictionary<string, ConnectorAttribute> _attrs;
        public ConnectorObject(ObjectClass objectClass, ICollection<ConnectorAttribute> attrs)
        {
            if (objectClass == null)
            {
                throw new ArgumentException("ObjectClass may not be null");
            }
            if (attrs == null || attrs.Count == 0)
            {
                throw new ArgumentException("attrs cannot be empty or null.");
            }
            _objectClass = objectClass;
            _attrs =
            CollectionUtil.NewReadOnlyDictionary(attrs,
                                         value => { return value.Name; });
            if (!_attrs.ContainsKey(Uid.NAME))
            {
                const String MSG = "The ConnectorAttribute set must contain a Uid.";
                throw new ArgumentException(MSG);
            }
            if (!_attrs.ContainsKey(Name.NAME))
            {
                const string MSG = "The ConnectorAttribute set must contain a Name.";
                throw new ArgumentException(MSG);
            }
        }
        public ICollection<ConnectorAttribute> GetAttributes()
        {
            return _attrs.Values;
        }
        public ConnectorAttribute GetAttributeByName(string name)
        {
            return CollectionUtil.GetValue(_attrs, name, null);
        }
        public Uid Uid
        {
            get
            {
                return (Uid)GetAttributeByName(Uid.NAME);
            }
        }
        public Name Name
        {
            get
            {
                return (Name)GetAttributeByName(Name.NAME);
            }
        }
        public ObjectClass ObjectClass
        {
            get
            {
                return _objectClass;
            }
        }
        public override int GetHashCode()
        {
            return CollectionUtil.GetHashCode(_attrs);
        }
        public override bool Equals(Object o)
        {
            ConnectorObject other = o as ConnectorObject;
            if (other != null)
            {
                if (!_objectClass.Equals(other.ObjectClass))
                {
                    return false;
                }
                return CollectionUtil.Equals(_attrs, other._attrs);
            }
            return false;
        }
    }
    #endregion

    #region ConnectorObjectBuilder
    public sealed class ConnectorObjectBuilder
    {
        private IDictionary<string, ConnectorAttribute> _attributes;
        public ConnectorObjectBuilder()
        {
            _attributes = new Dictionary<string, ConnectorAttribute>();
            // default always add the account object class..
            ObjectClass = ObjectClass.ACCOUNT;
        }

        public void SetUid(string uid)
        {
            AddAttribute(new Uid(uid));
        }

        public void SetUid(Uid uid)
        {
            AddAttribute(uid);
        }

        public void SetName(string name)
        {
            AddAttribute(new Name(name));
        }

        public void SetName(Name name)
        {
            AddAttribute(name);
        }

        public ObjectClass ObjectClass { get; set; }

        // =======================================================================
        // Clone basically..
        // =======================================================================
        /// <summary>
        /// Takes all the attribute from a <see cref="ConnectorObject" /> and add/overwrite
        /// the current attributes.
        /// </summary>
        public ConnectorObjectBuilder Add(ConnectorObject obj)
        {
            // simply add all the attributes it will include (Uid, ObjectClass..)
            foreach (ConnectorAttribute attr in obj.GetAttributes())
            {
                AddAttribute(attr);
            }
            ObjectClass = obj.ObjectClass;
            return this;
        }

        public ConnectorObjectBuilder AddAttribute(params ConnectorAttribute[] attrs)
        {
            ValidateParameter(attrs, "attrs");
            foreach (ConnectorAttribute a in attrs)
            {
                //DONT use Add - it throws exceptions if already there
                _attributes[a.Name] = a;
            }
            return this;
        }
        public ConnectorObjectBuilder AddAttributes(ICollection<ConnectorAttribute> attrs)
        {
            ValidateParameter(attrs, "attrs");
            foreach (ConnectorAttribute a in attrs)
            {
                _attributes[a.Name] = a;
            }
            return this;
        }
        /// <summary>
        /// Adds values to the attribute.
        /// </summary>
        public ConnectorObjectBuilder AddAttribute(String name, params object[] objs)
        {
            AddAttribute(ConnectorAttributeBuilder.Build(name, objs));
            return this;
        }

        /// <summary>
        /// Adds each object in the collection.
        /// </summary>
        public ConnectorObjectBuilder AddAttribute(String name, ICollection<object> obj)
        {
            AddAttribute(ConnectorAttributeBuilder.Build(name, obj));
            return this;
        }
        public ConnectorObject Build()
        {
            // check that there are attributes to return..
            if (_attributes.Count == 0)
            {
                throw new InvalidOperationException("No attributes set!");
            }
            return new ConnectorObject(ObjectClass, _attributes.Values);
        }
        private static void ValidateParameter(Object param, String paramName)
        {
            if (param == null)
            {
                String FORMAT = "Parameter " + param + " must not be null!";
                throw new NullReferenceException(FORMAT);
            }
        }
    }
    #endregion

    #region ConnectorAttributeInfo
    public sealed class ConnectorAttributeInfo
    {
        private readonly string _name;
        private readonly Type _type;
        private readonly Flags _flags;

        /// <summary>
        /// Enum of modifier flags to use for attributes.
        /// </summary>
        /// <remarks>
        /// Note that
        /// this enum is designed for configuration by exception such that
        /// an empty set of flags are the defaults:
        /// <list type="bullet">
        /// <item>
        /// <description>updateable
        /// </description>
        /// </item>
        /// <item>
        /// <description>creatable
        /// </description>
        /// </item>
        /// <item>
        /// <description>returned by default
        /// </description>
        /// </item>
        /// <item>
        /// <description>readable
        /// </description>
        /// </item>
        /// <item>
        /// <description>single-valued
        /// </description>
        /// </item>
        /// <item>
        /// <description>optional
        /// </description>
        /// </item>
        /// </list>
        /// </remarks>
        [FlagsAttribute]
        public enum Flags
        {
            NONE = 0,
            REQUIRED = 1,
            MULTIVALUED = 2,
            NOT_CREATABLE = 4,
            NOT_UPDATEABLE = 8,
            NOT_READABLE = 16,
            NOT_RETURNED_BY_DEFAULT = 32
        }

        internal ConnectorAttributeInfo(string name, Type type,
                Flags flags)
        {
            if (StringUtil.IsBlank(name))
            {
                throw new ArgumentException("Name must not be blank!");
            }
            if ((OperationalAttributes.PASSWORD_NAME.Equals(name) ||
                    OperationalAttributes.CURRENT_PASSWORD_NAME.Equals(name)) &&
                    !typeof(GuardedString).Equals(type))
            {
                String MSG = "Password based attributes must be of type GuardedString.";
                throw new ArgumentException(MSG);
            }
            // check the type..
            FrameworkUtil.CheckAttributeType(type);
            _name = name;
            _type = type;
            _flags = flags;
            if (!IsReadable && IsReturnedByDefault)
            {
                throw new ArgumentException("Attribute " + name + " is flagged as not-readable, so it should also be as not-returned-by-default.");
            }
        }


        /// <summary>
        /// The native name of the attribute.
        /// </summary>
        /// <returns>the native name of the attribute its describing.</returns>
        public string Name
        {
            get
            {
                return _name;
            }
        }

        /// <summary>
        /// The basic type associated with this attribute.
        /// </summary>
        /// <remarks>
        /// All primitives are
        /// supported.
        /// </remarks>
        /// <returns>the native type if uses.</returns>
        public Type ValueType
        {
            get
            {
                return _type;
            }
        }

        /// <summary>
        /// Returns the set of flags associated with the attribute.
        /// </summary>
        /// <returns>the set of flags associated with the attribute</returns>
        public Flags InfoFlags
        {
            get
            {
                return _flags;
            }
        }


        public bool Is(string name)
        {
            return NameUtil.NamesEqual(_name, name);
        }

        /// <summary>
        /// Determines if the attribute is readable.
        /// </summary>
        /// <returns>true if the attribute is readable else false.</returns>
        public bool IsReadable
        {
            get
            {
                return (_flags & Flags.NOT_READABLE) == 0;
            }
        }

        /// <summary>
        /// Determines if the attribute is writable on create.
        /// </summary>
        /// <returns>true if the attribute is writable on create else false.</returns>
        public bool IsCreatable
        {
            get
            {
                return (_flags & Flags.NOT_CREATABLE) == 0;
            }
        }

        /// <summary>
        /// Determines if the attribute is writable on update.
        /// </summary>
        /// <returns>true if the attribute is writable on update else false.</returns>
        public bool IsUpdateable
        {
            get
            {
                return (_flags & Flags.NOT_UPDATEABLE) == 0;
            }
        }

        /// <summary>
        /// Determines whether this attribute is required for creates.
        /// </summary>
        /// <returns>true if the attribute is required for an object else false.</returns>
        public bool IsRequired
        {
            get
            {
                return (_flags & Flags.REQUIRED) != 0;
            }
        }

        /// <summary>
        /// Determines if this attribute can handle multiple values.
        /// </summary>
        /// <remarks>
        /// There is a
        /// special case with byte[] since in most instances this denotes a single
        /// object.
        /// </remarks>
        /// <returns>true if the attribute is multi-value otherwise false.</returns>
        public bool IsMultiValued
        {
            get
            {
                return (_flags & Flags.MULTIVALUED) != 0;
            }
        }

        /// <summary>
        /// Determines if the attribute is returned by default.
        /// </summary>
        /// <remarks>
        /// Indicates if an
        /// <see cref="ConnectorAttribute" /> will be returned during <see cref="SearchApiOp" /> or
        /// <see cref="GetApiOp" /> inside a <see cref="ConnectorObject" /> by default. The default
        /// value is <code>true</code>.
        /// </remarks>
        /// <returns>false iff the attribute should not be returned by default.</returns>
        public bool IsReturnedByDefault
        {
            get
            {
                return (_flags & Flags.NOT_RETURNED_BY_DEFAULT) == 0;
            }
        }

        public override bool Equals(Object o)
        {
            ConnectorAttributeInfo other = o as ConnectorAttributeInfo;
            if (other != null)
            {
                if (!Is(other.Name))
                {
                    return false;
                }
                if (!ValueType.Equals(other.ValueType))
                {
                    return false;
                }
                if (_flags != other._flags)
                {
                    return false;
                }
                return true;
            }
            return false;
        }

        public override int GetHashCode()
        {
            return NameUtil.GetNameHashCode(_name);
        }

        public override string ToString()
        {
            return SerializerUtil.SerializeXmlObject(this, false);
        }
    }
    #endregion

    #region ConnectorAttributeInfoBuilder
    /// <summary>
    /// Simplifies the process of building 'AttributeInfo' objects.
    /// </summary>
    /// <remarks>
    /// This class is
    /// responsible for providing a default implementation of <see cref="ConnectorAttributeInfo" />.
    /// <code>
    /// AttributeInfoBuilder bld = new AttributeInfoBuilder("email");
    /// bld.setRequired(true);
    /// AttributeInfo info = bld.build();
    /// </code>
    /// </remarks>
    /// <author>Will Droste</author>
    /// <version>$Revision: 1.9 $</version>
    /// <since>1.0</since>
    public sealed class ConnectorAttributeInfoBuilder
    {

        private String _name;
        private Type _type;
        private ConnectorAttributeInfo.Flags _flags;

        /// <summary>
        /// Creates an builder with all the defaults set.
        /// </summary>
        /// <remarks>
        /// The name must be set before
        /// the 'build' method is called otherwise an <see cref="IllegalStateException" />
        /// is thrown.
        /// <pre>
        /// Name: &lt;not set&gt;
        /// Readable: true
        /// Writeable: true
        /// Required: false
        /// Type: string
        /// MultiValue: false
        /// </pre>
        /// </remarks>
        public ConnectorAttributeInfoBuilder()
        {
            ValueType = (typeof(String));
            _flags = ConnectorAttributeInfo.Flags.NONE;
        }

        /// <summary>
        /// Creates an builder with all the defaults set.
        /// </summary>
        /// <remarks>
        /// The name must be set before
        /// the 'build' method is called otherwise an <see cref="IllegalStateException" />
        /// is thrown.
        /// <pre>
        /// Name: &lt;not set&gt;
        /// Readable: true
        /// Writeable: true
        /// Required: false
        /// Type: string
        /// MultiValue: false
        /// </pre>
        /// </remarks>
        public ConnectorAttributeInfoBuilder(String name)
            : this(name, typeof(String))
        {
        }

        /// <summary>
        /// Creates an builder with all the defaults set.
        /// </summary>
        /// <remarks>
        /// The name must be set before
        /// the 'build' method is called otherwise an <see cref="IllegalStateException" />
        /// is thrown.
        /// <pre>
        /// Name: &lt;not set&gt;
        /// Readable: true
        /// Writeable: true
        /// Required: false
        /// Type: string
        /// MultiValue: false
        /// </pre>
        /// </remarks>
        public ConnectorAttributeInfoBuilder(String name, Type type)
        {
            Name = (name);
            ValueType = (type);
            //noneOf means the defaults
            _flags = ConnectorAttributeInfo.Flags.NONE;
        }

        /// <summary>
        /// Builds an <see cref="ConnectorAttributeInfo" /> object based on the properties set.
        /// </summary>
        /// <returns>
        /// <see cref="ConnectorAttributeInfo" /> based on the properties set.</returns>
        public ConnectorAttributeInfo Build()
        {
            return new ConnectorAttributeInfo(_name, _type, _flags);
        }

        /// <summary>
        /// Sets the unique name of the <see cref="ConnectorAttributeInfo" /> object.
        /// </summary>
        /// <param name="name">unique name of the <see cref="ConnectorAttributeInfo" /> object.</param>
        public String Name
        {
            set
            {
                if (StringUtil.IsBlank(value))
                {
                    throw new ArgumentException("Argument must not be blank.");
                }
                _name = value;
            }
        }

        /// <summary>
        /// Please see <see cref="FrameworkUtil.CheckAttributeType(Type)" /> for the
        /// definitive list of supported types.
        /// </summary>
        /// <param name="value">type for an <see cref="ConnectorAttribute" />'s value.</param>
        /// <exception cref="ArgumentException">if the Class is not a supported type.</exception>
        public Type ValueType
        {
            set
            {
                FrameworkUtil.CheckAttributeType(value);
                _type = value;
            }
        }

        /// <summary>
        /// Determines if the attribute is readable.
        /// </summary>
        public bool Readable
        {
            set
            {
                SetFlag(ConnectorAttributeInfo.Flags.NOT_READABLE, !value);
            }
        }

        /// <summary>
        /// Determines if the attribute is writable.
        /// </summary>
        public bool Creatable
        {
            set
            {
                SetFlag(ConnectorAttributeInfo.Flags.NOT_CREATABLE, !value);
            }
        }

        /// <summary>
        /// Determines if this attribute is required.
        /// </summary>
        public bool Required
        {
            set
            {
                SetFlag(ConnectorAttributeInfo.Flags.REQUIRED, value);
            }
        }

        /// <summary>
        /// Determines if this attribute supports multivalue.
        /// </summary>
        public bool MultiValued
        {
            set
            {
                SetFlag(ConnectorAttributeInfo.Flags.MULTIVALUED, value);
            }
        }

        /// <summary>
        /// Determines if this attribute writable during update.
        /// </summary>
        public bool Updateable
        {
            set
            {
                SetFlag(ConnectorAttributeInfo.Flags.NOT_UPDATEABLE, !value);
            }
        }

        public bool ReturnedByDefault
        {
            set
            {
                SetFlag(ConnectorAttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT, !value);
            }
        }

        /// <summary>
        /// Sets all of the flags for this builder.
        /// </summary>
        /// <param name="flags">The set of attribute info flags. Null means clear all flags.
        /// <para>
        /// NOTE: EnumSet.noneOf(AttributeInfo.Flags.class) results in
        /// an attribute with the default behavior:
        /// <list type="bullet">
        /// <item>
        /// <description>updateable
        /// </description>
        /// </item>
        /// <item>
        /// <description>creatable
        /// </description>
        /// </item>
        /// <item>
        /// <description>returned by default
        /// </description>
        /// </item>
        /// <item>
        /// <description>readable
        /// </description>
        /// </item>
        /// <item>
        /// <description>single-valued
        /// </description>
        /// </item>
        /// <item>
        /// <description>optional
        /// </description>
        /// </item>
        /// </list>
        /// </para>
        /// </param>
        public ConnectorAttributeInfo.Flags InfoFlags
        {
            set
            {
                _flags = value;
            }
        }

        private void SetFlag(ConnectorAttributeInfo.Flags flag, bool value)
        {
            if (value)
            {
                _flags = _flags | flag;
            }
            else
            {
                _flags = _flags & ~flag;
            }
        }

        /// <summary>
        /// Convenience method to create an AttributeInfo.
        /// </summary>
        /// <remarks>
        /// Equivalent to
        /// <code>
        /// new AttributeInfoBuilder(name,type).setFlags(flags).build()
        /// </code>
        /// </remarks>
        /// <param name="name">The name of the attribute</param>
        /// <param name="type">The type of the attribute</param>
        /// <param name="flags">The flags for the attribute. Null means clear all flags</param>
        /// <returns>The attribute info</returns>
        public static ConnectorAttributeInfo Build(String name, Type type,
                ConnectorAttributeInfo.Flags flags)
        {
            return new ConnectorAttributeInfoBuilder(name, type) { InfoFlags = flags }.Build();
        }
        /// <summary>
        /// Convenience method to create an AttributeInfo.
        /// </summary>
        /// <remarks>
        /// Equivalent to
        /// <code>
        /// AttributeInfoBuilder.build(name,type,null)
        /// </code>
        /// </remarks>
        /// <param name="name">The name of the attribute</param>
        /// <param name="type">The type of the attribute</param>
        /// <param name="flags">The flags for the attribute</param>
        /// <returns>The attribute info</returns>
        public static ConnectorAttributeInfo Build(String name, Type type)
        {
            return Build(name, type, ConnectorAttributeInfo.Flags.NONE);
        }

        /// <summary>
        /// Convenience method to create an AttributeInfo.
        /// </summary>
        /// <remarks>
        /// Equivalent to
        /// <code>
        /// AttributeInfoBuilder.build(name,type)
        /// </code>
        /// </remarks>
        /// <param name="name">The name of the attribute</param>
        /// <returns>The attribute info</returns>
        public static ConnectorAttributeInfo Build(String name)
        {
            return Build(name, typeof(String));
        }
    }
    #endregion

    #region FileName
    /// <summary>
    /// Placeholder for java.io.File since C#'s
    /// FileInfo class throws exceptions if the
    /// file doesn't exist.
    /// </summary>
    public sealed class FileName
    {
        private string _path;
        public FileName(string path)
        {
            if (path == null)
            {
                throw new ArgumentNullException();
            }
            _path = path;
        }
        public string Path
        {
            get
            {
                return _path;
            }
        }
        public override bool Equals(object o)
        {
            FileName other = o as FileName;
            if (other != null)
            {
                return Path.Equals(other.Path);
            }
            return false;
        }
        public override int GetHashCode()
        {
            return _path.GetHashCode();
        }
        public override string ToString()
        {
            return _path;
        }
    }
    #endregion

    #region Name
    public sealed class Name : ConnectorAttribute
    {
        public readonly static string NAME = ConnectorAttributeUtil.CreateSpecialName("NAME");
        public readonly static ConnectorAttributeInfo INFO =
            new ConnectorAttributeInfoBuilder(NAME) { Required = true }.Build();

        public Name(String value)
            : base(NAME, CollectionUtil.NewReadOnlyList<object>(value))
        {
        }

        /// <summary>
        /// The single value of the attribute that is the unique id of an object.
        /// </summary>
        /// <returns>value that identifies an object.</returns>
        public String GetNameValue()
        {
            return ConnectorAttributeUtil.GetStringValue(this);
        }
    }
    #endregion

    #region ObjectClassUtil
    public static class ObjectClassUtil
    {
        /// <summary>
        /// Determines if this object class is a special object class.
        /// Special object classes include the predefined ones, such as
        /// <see cref="ObjectClass.ACCOUNT"/> and <see cref="ObjectClass.GROUP"/>.
        /// </summary>
        /// <param name="oclass">the object class to test</param>
        /// <returns>true iff the object class is special</returns>
        /// <exception cref="NullReferenceException">if the object class parameter is null</exception>
        public static bool IsSpecial(ObjectClass oclass)
        {
            String name = oclass.GetObjectClassValue();
            return IsSpecialName(name);
        }

        /// <summary>
        /// Determines whether the specified object class name is special in the
        /// sense of <see cref="ObjectClassUtil.CreateSpecialName"/>.
        /// </summary>
        /// <param name="name">the name of the object class to test</param>
        /// <returns>true iff the object class name is special</returns>
        public static bool IsSpecialName(String name)
        {
            return NameUtil.IsSpecialName(name);
        }

        /// <summary>
        /// Create a special name from the specified name. Add the <code>__</code>
        /// string as both prefix and suffix. This indicates that a name
        /// identifies a special object class such as a predefined one.
        /// </summary>
        /// <param name="name">object class name to make special</param>
        /// <returns>name constructed for use as a special name</returns>
        public static string CreateSpecialName(string name)
        {
            return NameUtil.CreateSpecialName(name);
        }

        /// <summary>
        /// Compares two object class names for equality.
        /// </summary>
        /// <param name="name1">the first object class name</param>
        /// <param name="name2">the second object class name</param>
        /// <returns>true iff the two object class names are equal</returns>
        public static bool NamesEqual(string name1, string name2)
        {
            return NameUtil.NamesEqual(name2, name2);
        }
    }
    #endregion

    #region ObjectClass
    public sealed class ObjectClass
    {
        public static readonly String ACCOUNT_NAME = ObjectClassUtil.CreateSpecialName("ACCOUNT");
        public static readonly String GROUP_NAME = ObjectClassUtil.CreateSpecialName("GROUP");
        /// <summary>
        /// Denotes an account based object.
        /// </summary>
        public static readonly ObjectClass ACCOUNT = new ObjectClass(ACCOUNT_NAME);
        /// <summary>
        /// Denotes a group based object.
        /// </summary>
        public static readonly ObjectClass GROUP = new ObjectClass(GROUP_NAME);

        private readonly String _type;

        public ObjectClass(String type)
        {
            if (type == null)
            {
                throw new ArgumentException("Type cannot be null.");
            }
            _type = type;
        }
        public String GetObjectClassValue()
        {
            return _type;
        }

        /// <summary>
        /// Convenience method to build the display name key for
        /// an object class.
        /// </summary>
        /// <returns>The display name key.</returns>
        public String GetDisplayNameKey()
        {
            return "MESSAGE_OBJECT_CLASS_" + _type.ToUpper(CultureInfo.GetCultureInfo("en-US"));
        }

        /// <summary>
        /// Determines if the 'name' matches this <see cref="ObjectClass" />.
        /// </summary>
        /// <param name="name">case-insensitive string representation of the ObjectClass's
        /// type.</param>
        /// <returns>
        /// <code>true</code> if the case-insensitive name is equal to
        /// that of the one in this <see cref="ObjectClass" />.</returns>
        public bool Is(String name)
        {
            return NameUtil.NamesEqual(_type, name);
        }

        public override int GetHashCode()
        {
            return NameUtil.GetNameHashCode(_type);
        }

        public override bool Equals(object obj)
        {
            // test identity
            if (this == obj)
            {
                return true;
            }

            // test for null..
            if (obj == null)
            {
                return false;
            }

            // test that the exact class matches
            if (!(GetType().Equals(obj.GetType())))
            {
                return false;
            }

            ObjectClass other = (ObjectClass)obj;

            if (!Is(other._type))
            {
                return false;
            }

            return true;
        }

        public override string ToString()
        {
            return "ObjectClass: " + _type;
        }
    }
    #endregion

    #region ObjectClassInfo
    public sealed class ObjectClassInfo
    {
        private readonly String _type;
        private readonly ICollection<ConnectorAttributeInfo> _info;
        private readonly bool _isContainer;

        public ObjectClassInfo(String type,
                               ICollection<ConnectorAttributeInfo> attrInfo,
                               bool isContainer)
        {
            Assertions.NullCheck(type, "type");
            _type = type;
            _info = CollectionUtil.NewReadOnlySet(attrInfo);
            _isContainer = isContainer;
            // check to make sure name exists
            IDictionary<string, ConnectorAttributeInfo> dict
                = ConnectorAttributeInfoUtil.ToMap(attrInfo);
            if (!dict.ContainsKey(Name.NAME))
            {
                const string MSG = "Missing 'Name' connector attribute info.";
                throw new ArgumentException(MSG);
            }
        }

        public ICollection<ConnectorAttributeInfo> ConnectorAttributeInfos
        {
            get
            {
                return this._info;
            }
        }

        public String ObjectType
        {
            get
            {
                return this._type;
            }
        }

        /// <summary>
        /// Determines if the 'name' matches this <see cref="ObjectClassInfo" />.
        /// </summary>
        /// <param name="name">case-insensitive string representation of the ObjectClassInfo's
        /// type.</param>
        /// <returns>
        /// <code>true</code> if the case insensitive type is equal to
        /// that of the one in this <see cref="ObjectClassInfo" />.</returns>
        public bool Is(String name)
        {
            return NameUtil.NamesEqual(_type, name);
        }

        public bool IsContainer
        {
            get
            {
                return this._isContainer;
            }
        }

        public override int GetHashCode()
        {
            return NameUtil.GetNameHashCode(_type);
        }

        public override bool Equals(Object obj)
        {
            // test identity
            if (this == obj)
            {
                return true;
            }

            // test for null..
            if (obj == null)
            {
                return false;
            }

            if (!obj.GetType().Equals(this.GetType()))
            {
                return false;
            }

            ObjectClassInfo other = obj as ObjectClassInfo;

            if (!Is(other.ObjectType))
            {
                return false;
            }

            if (!CollectionUtil.Equals(ConnectorAttributeInfos,
                                       other.ConnectorAttributeInfos))
            {
                return false;
            }

            if (_isContainer != other._isContainer)
            {
                return false;
            }

            return true;
        }

        public override string ToString()
        {
            return SerializerUtil.SerializeXmlObject(this, false);
        }
    }
    #endregion

    #region ObjectClassInfoBuilder
    /// <summary>
    /// Used to help facilitate the building of <see cref="ObjectClassInfo" /> objects.
    /// </summary>
    public sealed class ObjectClassInfoBuilder
    {
        private bool _isContainer;
        private IDictionary<string, ConnectorAttributeInfo> _info;

        public ObjectClassInfoBuilder()
        {
            _info = new Dictionary<string, ConnectorAttributeInfo>();
            ObjectType = ObjectClass.ACCOUNT_NAME;
        }

        public string ObjectType { get; set; }

        /// <summary>
        /// Add each <see cref="ConnectorAttributeInfo" /> object to the <see cref="ObjectClassInfo" />.
        /// </summary>
        public ObjectClassInfoBuilder AddAttributeInfo(ConnectorAttributeInfo info)
        {
            if (_info.ContainsKey(info.Name))
            {
                const string MSG = "ConnectorAttributeInfo of name ''{0}'' already exists!";
                throw new ArgumentException(String.Format(MSG, info.Name));
            }
            _info[info.Name] = info;
            return this;
        }

        public ObjectClassInfoBuilder AddAllAttributeInfo(ICollection<ConnectorAttributeInfo> info)
        {
            foreach (ConnectorAttributeInfo cainfo in info)
            {
                AddAttributeInfo(cainfo);
            }
            return this;
        }

        public bool IsContainer
        {
            get
            {
                return _isContainer;
            }
            set
            {
                _isContainer = value;
            }
        }

        public ObjectClassInfo Build()
        {
            // determine if name is missing and add it by default
            if (!_info.ContainsKey(Name.NAME))
            {
                _info[Name.NAME] = Name.INFO;
            }
            return new ObjectClassInfo(ObjectType, _info.Values, _isContainer);
        }
    }
    #endregion

    #region OperationalAttributeInfos
    /// <summary>
    /// <see cref="ConnectorAttributeInfo" /> for each operational attribute.
    /// </summary>
    public static class OperationalAttributeInfos
    {
        /// <summary>
        /// Gets/sets the enable status of an object.
        /// </summary>
        public static readonly ConnectorAttributeInfo ENABLE =
            ConnectorAttributeInfoBuilder.Build(
                OperationalAttributes.ENABLE_NAME, typeof(bool));

        /// <summary>
        /// Gets/sets the enable date for an object.
        /// </summary>
        public static readonly ConnectorAttributeInfo ENABLE_DATE =
            ConnectorAttributeInfoBuilder.Build(
                OperationalAttributes.ENABLE_DATE_NAME, typeof(long));

        /// <summary>
        /// Gets/sets the disable date for an object.
        /// </summary>
        public static readonly ConnectorAttributeInfo DISABLE_DATE =
            ConnectorAttributeInfoBuilder.Build(
                OperationalAttributes.DISABLE_DATE_NAME, typeof(long));

        /// <summary>
        /// Gets/sets the lock out attribute for an object.
        /// </summary>
        public static readonly ConnectorAttributeInfo LOCK_OUT =
            ConnectorAttributeInfoBuilder.Build(
                OperationalAttributes.LOCK_OUT_NAME, typeof(bool));

        /// <summary>
        /// Gets/sets the password expiration date for an object.
        /// </summary>
        public static readonly ConnectorAttributeInfo PASSWORD_EXPIRATION_DATE =
            ConnectorAttributeInfoBuilder.Build(
                OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME, typeof(long));

        /// <summary>
        /// Normally this is a write-only attribute.
        /// </summary>
        /// <remarks>
        /// Sets the password for an object.
        /// </remarks>
        public static readonly ConnectorAttributeInfo PASSWORD =
            ConnectorAttributeInfoBuilder.Build(
                OperationalAttributes.PASSWORD_NAME, typeof(GuardedString),
                ConnectorAttributeInfo.Flags.NOT_READABLE |
                ConnectorAttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);

        /// <summary>
        /// Used in conjunction with password to do an account level password change.
        /// </summary>
        /// <remarks>
        /// This is for a non-administrator change of the password and therefore
        /// requires the current password.
        /// </remarks>
        public static readonly ConnectorAttributeInfo CURRENT_PASSWORD =
            ConnectorAttributeInfoBuilder.Build(
                OperationalAttributes.CURRENT_PASSWORD_NAME, typeof(GuardedString),
                ConnectorAttributeInfo.Flags.NOT_READABLE |
                ConnectorAttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);

        /// <summary>
        /// Used to determine if a password is expired or to expire a password.
        /// </summary>
        public static readonly ConnectorAttributeInfo PASSWORD_EXPIRED =
            ConnectorAttributeInfoBuilder.Build(
                OperationalAttributes.PASSWORD_EXPIRED_NAME, typeof(bool));

    }
    #endregion

    #region OperationalAttributes
    /// <summary>
    /// Operational attributes have special meaning and cannot be represented by pure
    /// operations.
    /// </summary>
    /// <remarks>
    /// For instance some administrators would like to create an account
    /// in the disabled state. The do not want this to be a two operation process
    /// since this can leave the door open to abuse. Therefore special attributes
    /// that can perform operations were introduced. The
    /// <see cref="OperationalAttributes.DISABLED" /> attribute could be added to the set of
    /// attribute sent to a Connector for the <see cref="CreateOp" /> operation. To tell the
    /// <see cref="Org.IdentityConnectors.Framework.Spi.Connector" /> to create the account with it in the disabled state whether
    /// the target resource itself has an attribute or an additional method must be
    /// called.
    /// </remarks>
    public static class OperationalAttributes
    {
        /// <summary>
        /// Gets/sets the enable status of an object.
        /// </summary>
        public static readonly string ENABLE_NAME = ConnectorAttributeUtil.CreateSpecialName("ENABLE");
        /// <summary>
        /// Gets/sets the enable date for an object.
        /// </summary>
        public static readonly string ENABLE_DATE_NAME = ConnectorAttributeUtil.CreateSpecialName("ENABLE_DATE");
        /// <summary>
        /// Gets/sets the disable date for an object.
        /// </summary>
        public static readonly string DISABLE_DATE_NAME = ConnectorAttributeUtil.CreateSpecialName("DISABLE_DATE");
        /// <summary>
        /// Gets/sets the lock out attribute for an object.
        /// </summary>
        public static readonly string LOCK_OUT_NAME = ConnectorAttributeUtil.CreateSpecialName("LOCK_OUT");
        /// <summary>
        /// Gets/sets the password expiration date for an object.
        /// </summary>
        public static readonly string PASSWORD_EXPIRATION_DATE_NAME = ConnectorAttributeUtil.CreateSpecialName("PASSWORD_EXPIRATION_DATE");
        /// <summary>
        /// Gets/sets the password expired for an object.
        /// </summary>
        public static readonly string PASSWORD_EXPIRED_NAME = ConnectorAttributeUtil.CreateSpecialName("PASSWORD_EXPIRED");
        /// <summary>
        /// Normally this is a write-only attribute.
        /// </summary>
        /// <remarks>
        /// Sets the password for an object.
        /// </remarks>
        public static readonly string PASSWORD_NAME = ConnectorAttributeUtil.CreateSpecialName("PASSWORD");
        /// <summary>
        /// Used in conjunction with password to do an account level password change.
        /// </summary>
        /// <remarks>
        /// This is for a non-administrator change of the password and therefore
        /// requires the current password.
        /// </remarks>
        public static readonly string CURRENT_PASSWORD_NAME = ConnectorAttributeUtil.CreateSpecialName("CURRENT_PASSWORD");

        // =======================================================================
        // Helper Methods..
        // =======================================================================
        public static readonly ICollection<string> OPERATIONAL_ATTRIBUTE_NAMES =
            CollectionUtil.NewReadOnlySet<string>(
                LOCK_OUT_NAME,
                ENABLE_NAME,
                ENABLE_DATE_NAME,
                DISABLE_DATE_NAME,
                PASSWORD_EXPIRATION_DATE_NAME,
                PASSWORD_NAME,
                CURRENT_PASSWORD_NAME,
                PASSWORD_EXPIRED_NAME
            );

        public static ICollection<string> GetOperationalAttributeNames()
        {
            return CollectionUtil.NewReadOnlySet<string>(OPERATIONAL_ATTRIBUTE_NAMES);
        }
        public static bool IsOperationalAttribute(ConnectorAttribute attr)
        {
            string name = (attr != null) ? attr.Name : null;
            return OPERATIONAL_ATTRIBUTE_NAMES.Contains(name);
        }
    }
    #endregion

    #region PredefinedAttributes
    /// <summary>
    /// List of well known or pre-defined attributes.
    /// </summary>
    /// <remarks>
    /// Common attributes that most
    /// resources have that are not operational in nature.
    /// </remarks>
    public static class PredefinedAttributes
    {
        /// <summary>
        /// Attribute that should hold a reasonable value to
        /// display for the value of an object.
        /// </summary>
        /// <remarks>
        /// If this is not present, then the
        /// application will have to use the NAME to show the value.
        /// </remarks>
        public static readonly String SHORT_NAME = ConnectorAttributeUtil.CreateSpecialName("SHORT_NAME");

        /// <summary>
        /// Attribute that should hold the value of the object's description,
        /// if one is available.
        /// </summary>
        public static readonly String DESCRIPTION = ConnectorAttributeUtil.CreateSpecialName("DESCRIPTION");

        /// <summary>
        /// Read-only attribute that shows the last date/time the password was
        /// changed.
        /// </summary>
        public static readonly string LAST_PASSWORD_CHANGE_DATE_NAME = ConnectorAttributeUtil.CreateSpecialName("LAST_PASSWORD_CHANGE_DATE");

        /// <summary>
        /// Common password policy attribute where the password must be changed every
        /// so often.
        /// </summary>
        /// <remarks>
        /// The value for this attribute is milliseconds since its the
        /// lowest common denominator.
        /// </remarks>
        public static readonly string PASSWORD_CHANGE_INTERVAL_NAME = ConnectorAttributeUtil.CreateSpecialName("PASSWORD_CHANGE_INTERVAL");

        /// <summary>
        /// Last login date for an account.
        /// </summary>
        /// <remarks>
        /// This is usually used to determine inactivity.
        /// </remarks>
        public static readonly string LAST_LOGIN_DATE_NAME = ConnectorAttributeUtil.CreateSpecialName("LAST_LOGIN_DATE");

        /// <summary>
        /// Groups an account object belongs to.
        /// </summary>
        public static readonly string GROUPS_NAME = ConnectorAttributeUtil.CreateSpecialName("GROUPS");
    }
    #endregion

    #region PredefinedAttributeInfos
    public static class PredefinedAttributeInfos
    {
        /// <summary>
        /// Attribute that should hold a reasonable value to
        /// display for the value of an object.
        /// </summary>
        /// <remarks>
        /// If this is not present, then the
        /// application will have to use the NAME to show the value.
        /// </remarks>
        public static readonly ConnectorAttributeInfo SHORT_NAME =
            ConnectorAttributeInfoBuilder.Build(PredefinedAttributes.SHORT_NAME);

        /// <summary>
        /// Attribute that should hold the value of the object's description,
        /// if one is available.
        /// </summary>
        public static readonly ConnectorAttributeInfo DESCRIPTION =
            ConnectorAttributeInfoBuilder.Build(PredefinedAttributes.DESCRIPTION);
        /// <summary>
        /// Read-only attribute that shows the last date/time the password was
        /// changed.
        /// </summary>
        public static readonly ConnectorAttributeInfo LAST_PASSWORD_CHANGE_DATE =
            ConnectorAttributeInfoBuilder.Build(
                PredefinedAttributes.LAST_PASSWORD_CHANGE_DATE_NAME,
                typeof(long),
                ConnectorAttributeInfo.Flags.NOT_CREATABLE |
                ConnectorAttributeInfo.Flags.NOT_UPDATEABLE);

        /// <summary>
        /// Common password policy attribute where the password must be changed every
        /// so often.
        /// </summary>
        /// <remarks>
        /// The value for this attribute is milliseconds since its the
        /// lowest common denominator.
        /// </remarks>
        public static readonly ConnectorAttributeInfo PASSWORD_CHANGE_INTERVAL =
            ConnectorAttributeInfoBuilder.Build(
                PredefinedAttributes.PASSWORD_CHANGE_INTERVAL_NAME, typeof(long));

        /// <summary>
        /// Last login date for an account.
        /// </summary>
        /// <remarks>
        /// This is usually used to determine
        /// inactivity.
        /// </remarks>
        public static readonly ConnectorAttributeInfo LAST_LOGIN_DATE =
            ConnectorAttributeInfoBuilder.Build(
                PredefinedAttributes.LAST_LOGIN_DATE_NAME,
                typeof(long),
                ConnectorAttributeInfo.Flags.NOT_CREATABLE |
                ConnectorAttributeInfo.Flags.NOT_UPDATEABLE);

        /// <summary>
        /// Groups that an account or person belong to.
        /// </summary>
        /// <remarks>
        /// The Attribute values are the
        /// UID value of each group that an account has membership in.
        /// </remarks>
        public static readonly ConnectorAttributeInfo GROUPS =
            ConnectorAttributeInfoBuilder.Build(PredefinedAttributes.GROUPS_NAME,
                    typeof(String),
                    ConnectorAttributeInfo.Flags.MULTIVALUED |
                    ConnectorAttributeInfo.Flags.NOT_RETURNED_BY_DEFAULT);
    }
    #endregion

    #region OperationOptions
    /// <summary>
    /// Arbitrary options to be passed into various operations.
    /// </summary>
    /// <remarks>
    /// This serves
    /// as a catch-all for extra options.
    /// </remarks>
    public sealed class OperationOptions
    {
        /// <summary>
        /// An option to use with <see cref="SearchApiOp" /> that specified the scope
        /// under which to perform the search.
        /// </summary>
        /// <remarks>
        /// To be used in conjunction with
        /// <see cref="OP_CONTAINER" />. Must be one of the following values
        /// <list type="number">
        /// <item>
        /// <description><see cref="SCOPE_OBJECT" />
        /// </description>
        /// </item>
        /// <item>
        /// <description><see cref="SCOPE_ONE_LEVEL" />
        /// </description>
        /// </item>
        /// <item>
        /// <description><see cref="SCOPE_SUBTREE" />
        /// </description>
        /// </item>
        /// </list>
        /// </remarks>
        public const String OP_SCOPE = "SCOPE";
        public const String SCOPE_OBJECT = "object";
        public const String SCOPE_ONE_LEVEL = "onelevel";
        public const String SCOPE_SUBTREE = "subtree";

        /// <summary>
        /// An option to use with <see cref="SearchApiOp" /> that specified the container
        /// under which to perform the search.
        /// </summary>
        /// <remarks>
        /// Must be of type <see cref="QualifiedUid" />.
        /// Should be implemented for those object classes whose <see cref="ObjectClassInfo.IsContainer" />
        /// returns true.
        /// </remarks>
        public const String OP_CONTAINER = "CONTAINER";

        /// <summary>
        /// An option to use with <see cref="Org.IdentityConnectors.Framework.Api.Operations.ScriptOnResourceApiOp" /> and possibly others
        /// that specifies an account under which to execute the script/operation.
        /// </summary>
        /// <remarks>
        /// The specified account will appear to have performed any action that the
        /// script/operation performs.
        /// <para>
        /// Check the javadoc for a particular connector to see whether that
        /// connector supports this option.
        /// </para>
        /// </remarks>
        public static readonly string OP_RUN_AS_USER = "RUN_AS_USER";

        /// <summary>
        /// An option to use with <see cref="Org.IdentityConnectors.Framework.Api.Operations.ScriptOnResourceApiOp" /> and possibly others
        /// that specifies a password under which to execute the script/operation.
        /// </summary>
        public static readonly string OP_RUN_WITH_PASSWORD = "RUN_WITH_PASSWORD";

        /// <summary>
        /// Determines the attributes to retrieve during <see cref="SearchApiOp" /> and
        /// <see cref="Org.IdentityConnectors.Framework.Api.Operations.SyncApiOp" />.
        /// </summary>
        public static readonly string OP_ATTRIBUTES_TO_GET = "ATTRS_TO_GET";

        private readonly IDictionary<String, Object> _operationOptions;

        /// <summary>
        /// Public only for serialization; please use <see cref="OperationOptionsBuilder" />.
        /// </summary>
        /// <param name="operationOptions">The options.</param>
        public OperationOptions(IDictionary<String, Object> operationOptions)
        {
            foreach (Object val in operationOptions.Values)
            {
                FrameworkUtil.CheckOperationOptionValue(val);
            }
            //clone options to do a deep copy in case anything
            //is an array
            IDictionary<Object, Object> operationOptionsClone = (IDictionary<Object, Object>)SerializerUtil.CloneObject(operationOptions);
            _operationOptions = CollectionUtil.NewReadOnlyDictionary<Object, Object, String, Object>(operationOptionsClone);
        }

        /// <summary>
        /// Returns a map of options.
        /// </summary>
        /// <remarks>
        /// Each value in the map
        /// must be of a type that the framework can serialize.
        /// See <see cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" /> for a list of supported types.
        /// </remarks>
        /// <returns>A map of options.</returns>
        public IDictionary<String, Object> Options
        {
            get
            {
                return _operationOptions;
            }
        }

        /// <summary>
        /// Convenience method that returns <see cref="OP_SCOPE" />.
        /// </summary>
        /// <returns>The value for <see cref="OP_SCOPE" />.</returns>
        public String Scope
        {
            get
            {
                return (String)CollectionUtil.GetValue(_operationOptions, OP_SCOPE, null);
            }
        }

        /// <summary>
        /// Convenience method that returns <see cref="OP_CONTAINER" />.
        /// </summary>
        /// <returns>The value for <see cref="OP_CONTAINER" />.</returns>
        public QualifiedUid getContainer
        {
            get
            {
                return (QualifiedUid)CollectionUtil.GetValue(_operationOptions, OP_CONTAINER, null);
            }
        }

        /// <summary>
        /// Get the string array of attribute names to return in the object.
        /// </summary>
        public string[] AttributesToGet
        {
            get
            {
                return (string[])CollectionUtil.GetValue(
                    _operationOptions, OP_ATTRIBUTES_TO_GET, null);
            }
        }

        /// <summary>
        /// Get the account to run the operation as..
        /// </summary>
        public string RunAsUser
        {
            get
            {
                return (string)CollectionUtil.GetValue(
                    _operationOptions, OP_RUN_AS_USER, null);
            }
        }

        /// <summary>
        /// Get the password to run the operation as..
        /// </summary>
        public GuardedString RunWithPassword
        {
            get
            {
                return (GuardedString)CollectionUtil.GetValue(
                    _operationOptions, OP_RUN_WITH_PASSWORD, null);
            }
        }

        public override string ToString()
        {
            StringBuilder bld = new StringBuilder();
            bld.Append("OperationOptions: ").Append(Options);
            return bld.ToString();
        }
    }
    #endregion

    #region OperationOptionsBuilder
    /// <summary>
    /// Builder for <see cref="OperationOptions" />.
    /// </summary>
    public sealed class OperationOptionsBuilder
    {
        private readonly IDictionary<String, Object> _options;

        /// <summary>
        /// Create a builder with an empty set of options.
        /// </summary>
        public OperationOptionsBuilder()
        {
            _options = new Dictionary<String, Object>();
        }

        /// <summary>
        /// Create a builder from an existing set of options.
        /// </summary>
        /// <param name="options">The existing set of options. Must not be null.</param>
        public OperationOptionsBuilder(OperationOptions options)
        {
            Assertions.NullCheck(options, "options");
            // clone options to do a deep copy in case anything
            // is an array
            IDictionary<Object, Object> operationOptionsClone = (IDictionary<Object, Object>)SerializerUtil
                    .CloneObject(options.Options);
            _options = CollectionUtil.NewDictionary<object, object, string, object>(operationOptionsClone);
        }

        /// <summary>
        /// Sets a given option and a value for that option.
        /// </summary>
        /// <param name="name">The name of the option</param>
        /// <param name="value">The value of the option. Must be one of the types that
        /// we can serialize.
        /// See <see cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" /> for a list of supported types.</param>
        public void SetOption(String name, Object value)
        {
            if (name == null)
            {
                throw new ArgumentException("Argument 'value' cannot be null.");
            }
            //don't validate value here - we do that implicitly when
            //we clone in the constructor of OperationOptions
            _options[name] = value;
        }

        /// <summary>
        /// Returns a mutable reference of the options map.
        /// </summary>
        /// <returns>A mutable reference of the options map.</returns>
        public IDictionary<String, Object> Options
        {
            get
            {
                //might as well be mutable since it's the builder and
                //we don't want to deep copy anyway
                return _options;
            }
        }

        /// <summary>
        /// Creates the <code>OperationOptions</code>.
        /// </summary>
        /// <returns>The newly-created <code>OperationOptions</code></returns>
        public OperationOptions Build()
        {
            return new OperationOptions(_options);
        }

        /// <summary>
        /// Sets the <see cref="OperationOptions.OP_ATTRIBUTES_TO_GET" /> option.
        /// </summary>
        /// <param name="attrNames">list of <see cref="ConnectorAttribute" /> names.</param>
        public string[] AttributesToGet
        {
            set
            {
                Assertions.NullCheck(value, "AttributesToGet");
                // don't validate value here - we do that in
                // the constructor of OperationOptions - that's
                // really the only place we can truly enforce this
                _options[OperationOptions.OP_ATTRIBUTES_TO_GET] = value;
            }
        }

        /// <summary>
        /// Set the run with password option.
        /// </summary>
        public GuardedString RunWithPassword
        {
            set
            {
                Assertions.NullCheck(value, "RunWithPassword");
                _options[OperationOptions.OP_RUN_WITH_PASSWORD] = value;
            }
        }

        /// <summary>
        /// Set the run as user option.
        /// </summary>
        public string RunAsUser
        {
            set
            {
                Assertions.NullCheck(value, "RunAsUser");
                _options[OperationOptions.OP_RUN_AS_USER] = value;
            }
        }
        /// <summary>
        /// Convenience method to set <see cref="OperationOptions.OP_SCOPE" />
        /// </summary>
        /// <param name="scope">The scope. May not be null.</param>
        /// <returns>A this reference to allow chaining</returns>
        public string Scope
        {
            set
            {
                Assertions.NullCheck(value, "scope");
                _options[OperationOptions.OP_SCOPE] = value;
            }
        }

        /// <summary>
        /// Convenience method to set <see cref="OperationOptions.OP_CONTAINER" />
        /// </summary>
        /// <param name="container">The container. May not be null.</param>
        /// <returns>A this reference to allow chaining</returns>
        public QualifiedUid Container
        {
            set
            {
                Assertions.NullCheck(value, "container");
                _options[OperationOptions.OP_CONTAINER] = value;
            }
        }

    }
    #endregion

    #region OperationOptionInfo
    public sealed class OperationOptionInfo
    {
        private String _name;
        private Type _type;

        public OperationOptionInfo(String name,
                Type type)
        {
            Assertions.NullCheck(name, "name");
            Assertions.NullCheck(type, "type");
            FrameworkUtil.CheckOperationOptionType(type);
            _name = name;
            _type = type;
        }

        public String Name
        {
            get
            {
                return _name;
            }
        }

        public Type OptionType
        {
            get
            {
                return _type;
            }
        }

        public override bool Equals(Object o)
        {
            if (o is OperationOptionInfo)
            {
                OperationOptionInfo other =
                    (OperationOptionInfo)o;
                if (!_name.Equals(other._name))
                {
                    return false;
                }
                if (!_type.Equals(other._type))
                {
                    return false;
                }
                return true;
            }
            return false;
        }

        public override int GetHashCode()
        {
            return _name.GetHashCode();
        }

        public override string ToString()
        {
            StringBuilder bld = new StringBuilder();
            bld.Append("OperationOptionInfo(");
            bld.Append(_name);
            bld.Append(_type.ToString());
            bld.Append(')');
            return bld.ToString();
        }

    }
    #endregion

    #region OperationOptionInfoBuilder
    public sealed class OperationOptionInfoBuilder
    {
        private String _name;
        private Type _type;

        public OperationOptionInfoBuilder()
        {
        }

        public OperationOptionInfoBuilder(String name,
                Type type)
        {
            _name = name;
            _type = type;
        }

        public String Name
        {
            get
            {
                return _name;
            }
            set
            {
                _name = value;
            }
        }

        public Type OptionType
        {
            get
            {
                return _type;
            }
            set
            {
                _type = value;
            }
        }

        public OperationOptionInfo Build()
        {
            return new OperationOptionInfo(_name, _type);
        }

        public static OperationOptionInfo Build(String name, Type type)
        {
            return new OperationOptionInfoBuilder(name, type).Build();
        }

        public static OperationOptionInfo Build(String name)
        {
            return Build(name, typeof(string));
        }

        public static OperationOptionInfo BuildAttributesToGet()
        {
            return Build(OperationOptions.OP_ATTRIBUTES_TO_GET, typeof(string[]));
        }

        public static OperationOptionInfo BuildRunWithPassword()
        {
            return Build(OperationOptions.OP_RUN_WITH_PASSWORD);
        }

        public static OperationOptionInfo BuildRunAsUser()
        {
            return Build(OperationOptions.OP_RUN_AS_USER);
        }
        public static OperationOptionInfo BuildScope()
        {
            return Build(OperationOptions.OP_SCOPE);
        }

        public static OperationOptionInfo BuildContainer()
        {
            return Build(OperationOptions.OP_CONTAINER, typeof(QualifiedUid));
        }
    }
    #endregion

    #region QualifiedUid
    /// <summary>
    /// A fully-qualified uid.
    /// </summary>
    /// <remarks>
    /// That is, a pair of <see cref="ObjectClass" /> and
    /// <see cref="Uid" />.
    /// </remarks>
    public sealed class QualifiedUid
    {
        private readonly ObjectClass _objectClass;
        private readonly Uid _uid;

        /// <summary>
        /// Create a QualifiedUid.
        /// </summary>
        /// <param name="objectClass">The object class. May not be null.</param>
        /// <param name="uid">The uid. May not be null.</param>
        public QualifiedUid(ObjectClass objectClass,
                Uid uid)
        {
            Assertions.NullCheck(objectClass, "objectClass");
            Assertions.NullCheck(uid, "uid");
            _objectClass = objectClass;
            _uid = uid;
        }

        /// <summary>
        /// Returns the object class.
        /// </summary>
        /// <returns>The object class.</returns>
        public ObjectClass ObjectClass
        {
            get
            {
                return _objectClass;
            }
        }

        /// <summary>
        /// Returns the uid.
        /// </summary>
        /// <returns>The uid.</returns>
        public Uid Uid
        {
            get
            {
                return _uid;
            }
        }

        /// <summary>
        /// Returns true iff o is a QualifiedUid and the object class and uid match.
        /// </summary>
        public override bool Equals(Object o)
        {
            if (o is QualifiedUid)
            {
                QualifiedUid other = (QualifiedUid)o;
                return (_objectClass.Equals(other._objectClass) &&
                         _uid.Equals(other._uid));
            }
            return false;
        }

        /// <summary>
        /// Returns a hash code based on uid
        /// </summary>
        public override int GetHashCode()
        {
            return _uid.GetHashCode();
        }

        /// <summary>
        /// Returns a string representation acceptible for debugging.
        /// </summary>
        public override String ToString()
        {
            return SerializerUtil.SerializeXmlObject(this, false);
        }

    }
    #endregion

    #region ResultsHandler
    /// <summary>
    /// Encapsulate the handling of each object returned by the search.
    /// </summary>
    public delegate bool ResultsHandler(ConnectorObject obj);
    #endregion

    #region Schema
    /// <summary>
    /// Determines the objects supported by a
    /// <see cref="Org.IdentityConnectors.Framework.Spi.Connector" />.
    /// </summary>
    /// <remarks>
    /// The <see cref="Schema" /> object is used to represent the basic objects that a
    /// connector supports. This does not prevent a connector from supporting more.
    /// Rather, this is informational for the caller of the connector to understand
    /// a minimum support level.
    /// The schema defines 4 primary data structures
    /// <list type="number">
    /// <item>
    /// <description>Declared ObjectClasses (<see cref="ObjectClassInfo" />).
    /// </description>
    /// </item>
    /// <item>
    /// <description>Declared OperationOptionInfo (<see cref="OperationOptionInfo" />).
    /// </description>
    /// </item>
    /// <item>
    /// <description>Supported ObjectClasses by operation (<see cref="SupportedObjectClassesByOperation" />).
    /// </description>
    /// </item>
    /// <item>
    /// <description>Supported OperationOptionInfo by operation(<see cref="SupportedOptionsByOperation" />).
    /// </description>
    /// </item>
    /// </list>
    /// TODO: add more to describe and what is expected from this call and how it is
    /// used.. based on OperationalAttribute etc..
    /// </remarks>
    public sealed class Schema
    {
        private readonly ICollection<ObjectClassInfo> _declaredObjectClasses;
        private readonly ICollection<OperationOptionInfo> _declaredOperationOptions;
        private readonly IDictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>>
        _supportedObjectClassesByOperation;
        private readonly IDictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>>
        _supportedOptionsByOperation;

        /// <summary>
        /// Public only for serialization; please use
        /// SchemaBuilder instead.
        /// </summary>
        /// <param name="info"></param>
        /// <param name="supportedObjectClassesByOperation"></param>
        public Schema(ICollection<ObjectClassInfo> info,
                      ICollection<OperationOptionInfo> options,
                      IDictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>> supportedObjectClassesByOperation,
                      IDictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>> supportedOptionsByOperation)
        {
            _declaredObjectClasses = CollectionUtil.NewReadOnlySet<ObjectClassInfo>(info);
            _declaredOperationOptions = CollectionUtil.NewReadOnlySet(options);

            //make read-only
            {
                IDictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>> temp =
                    new Dictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>>();
                foreach (KeyValuePair<SafeType<APIOperation>, ICollection<ObjectClassInfo>> entry in
                    supportedObjectClassesByOperation)
                {
                    SafeType<APIOperation> op =
                        entry.Key;
                    ICollection<ObjectClassInfo> resolvedClasses =
                        CollectionUtil.NewReadOnlySet(entry.Value);
                    temp[op] = resolvedClasses;
                }
                _supportedObjectClassesByOperation = CollectionUtil.AsReadOnlyDictionary(temp);
            }
            //make read-only
            {
                IDictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>> temp =
                    new Dictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>>();
                foreach (KeyValuePair<SafeType<APIOperation>, ICollection<OperationOptionInfo>> entry in
                    supportedOptionsByOperation)
                {
                    SafeType<APIOperation> op =
                        entry.Key;
                    ICollection<OperationOptionInfo> resolvedClasses =
                        CollectionUtil.NewReadOnlySet(entry.Value);
                    temp[op] = resolvedClasses;
                }
                _supportedOptionsByOperation = CollectionUtil.AsReadOnlyDictionary(temp);
            }
        }

        /// <summary>
        /// Returns the set of object classes that are defined in the schema, regardless
        /// of which operations support them.
        /// </summary>
        public ICollection<ObjectClassInfo> ObjectClassInfo
        {
            get
            {
                return _declaredObjectClasses;
            }
        }

        /// <summary>
        /// Returns the ObjectClassInfo for the given type.
        /// </summary>
        /// <param name="type">The type to find.</param>
        /// <returns>the ObjectClassInfo for the given type or null if not found.</returns>
        public ObjectClassInfo FindObjectClassInfo(String type)
        {
            foreach (ObjectClassInfo info in _declaredObjectClasses)
            {
                if (info.Is(type))
                {
                    return info;
                }
            }
            return null;
        }

        /// <summary>
        /// Returns the set of operation options that are defined in the schema, regardless
        /// of which operations support them.
        /// </summary>
        /// <returns>The options defined in this schema.</returns>
        public ICollection<OperationOptionInfo> OperationOptionInfo
        {
            get
            {
                return _declaredOperationOptions;
            }
        }

        /// <summary>
        /// Returns the OperationOptionInfo for the given name.
        /// </summary>
        /// <param name="name">The name to find.</param>
        /// <returns>the OperationOptionInfo for the given name or null if not found.</returns>
        public OperationOptionInfo FindOperationOptionInfo(String name)
        {
            Assertions.NullCheck(name, "name");
            foreach (OperationOptionInfo info in _declaredOperationOptions)
            {
                if (info.Name.Equals(name))
                {
                    return info;
                }
            }
            return null;
        }

        /// <summary>
        /// Returns the supported object classes for the given operation.
        /// </summary>
        /// <param name="apiop">The operation.</param>
        /// <returns>the supported object classes for the given operation.</returns>
        public ICollection<ObjectClassInfo> GetSupportedObjectClassesByOperation(SafeType<APIOperation> apiop)
        {
            ICollection<ObjectClassInfo> rv =
                CollectionUtil.GetValue(_supportedObjectClassesByOperation, apiop, null);
            if (rv == null)
            {
                ICollection<ObjectClassInfo> empty =
                    CollectionUtil.NewReadOnlySet<ObjectClassInfo>();

                return empty;
            }
            else
            {
                return rv;
            }
        }

        /// <summary>
        /// Returns the supported options for the given operation.
        /// </summary>
        /// <param name="apiop">The operation.</param>
        /// <returns>the supported options for the given operation.</returns>
        public ICollection<OperationOptionInfo> GetSupportedOptionsByOperation(SafeType<APIOperation> apiop)
        {
            ICollection<OperationOptionInfo> rv =
                CollectionUtil.GetValue(_supportedOptionsByOperation, apiop, null);
            if (rv == null)
            {
                ICollection<OperationOptionInfo> empty =
                    CollectionUtil.NewReadOnlySet<OperationOptionInfo>();
                return empty;
            }
            else
            {
                return rv;
            }
        }

        /// <summary>
        /// Returns the set of object classes that apply to a particular operation.
        /// </summary>
        /// <returns>the set of object classes that apply to a particular operation.</returns>
        public IDictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>> SupportedObjectClassesByOperation
        {
            get
            {
                return _supportedObjectClassesByOperation;
            }
        }
        /// <summary>
        /// Returns the set of operation options that apply to a particular operation.
        /// </summary>
        /// <returns>the set of operation options that apply to a particular operation.</returns>
        public IDictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>> SupportedOptionsByOperation
        {
            get
            {
                return _supportedOptionsByOperation;
            }
        }



        public override int GetHashCode()
        {
            return CollectionUtil.GetHashCode(_declaredObjectClasses);
        }

        public override bool Equals(object o)
        {
            Schema other = o as Schema;
            if (other != null)
            {
                if (!CollectionUtil.Equals(ObjectClassInfo, other.ObjectClassInfo))
                {
                    return false;
                }
                if (!CollectionUtil.Equals(OperationOptionInfo, other.OperationOptionInfo))
                {
                    return false;
                }
                if (!CollectionUtil.Equals(_supportedObjectClassesByOperation,
                                              other._supportedObjectClassesByOperation))
                {
                    return false;
                }
                if (!CollectionUtil.Equals(_supportedOptionsByOperation,
                                              other._supportedOptionsByOperation))
                {
                    return false;
                }
                return true;
            }
            return false;
        }

        public override string ToString()
        {
            return SerializerUtil.SerializeXmlObject(this, false);
        }
    }
    #endregion

    #region SchemaBuilder
    /// <summary>
    /// Simple builder class to help facilitate creating a <see cref="Schema" /> object.
    /// </summary>
    public sealed class SchemaBuilder
    {
        private readonly SafeType<Connector> _connectorClass;
        private readonly ICollection<ObjectClassInfo> _declaredObjectClasses
        = new HashSet<ObjectClassInfo>();
        private readonly ICollection<OperationOptionInfo> _declaredOperationOptions
        = new HashSet<OperationOptionInfo>();

        private readonly IDictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>>
            _supportedObjectClassesByOperation =
                new Dictionary<SafeType<APIOperation>, ICollection<ObjectClassInfo>>();
        private readonly IDictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>>
            _supportedOptionsByOperation =
                new Dictionary<SafeType<APIOperation>, ICollection<OperationOptionInfo>>();


        /// <summary>
        /// </summary>
        public SchemaBuilder(SafeType<Connector> connectorClass)
        {
            Assertions.NullCheck(connectorClass, "connectorClass");
            _connectorClass = connectorClass;
        }

        /// <summary>
        /// Adds another ObjectClassInfo to the schema.
        /// </summary>
        /// <remarks>
        /// Also, adds this
        /// to the set of supported classes for every operation defined by
        /// the Connector.
        /// </remarks>
        /// <param name="info"></param>
        /// <exception cref="IllegalStateException">If already defined</exception>
        public void DefineObjectClass(ObjectClassInfo info)
        {
            Assertions.NullCheck(info, "info");
            if (_declaredObjectClasses.Contains(info))
            {
                throw new InvalidOperationException("ObjectClass already defined: " +
                        info.ObjectType);
            }
            _declaredObjectClasses.Add(info);
            foreach (SafeType<APIOperation> op in
                FrameworkUtil.GetDefaultSupportedOperations(_connectorClass))
            {
                ICollection<ObjectClassInfo> oclasses =
                    CollectionUtil.GetValue(_supportedObjectClassesByOperation, op, null);
                if (oclasses == null)
                {
                    oclasses = new HashSet<ObjectClassInfo>();
                    _supportedObjectClassesByOperation[op] = oclasses;
                }
                oclasses.Add(info);
            }
        }
        /// <summary>
        /// Adds another OperationOptionInfo to the schema.
        /// </summary>
        /// <remarks>
        /// Also, adds this
        /// to the set of supported options for every operation defined by
        /// the Connector.
        /// </remarks>
        public void DefineOperationOption(OperationOptionInfo info)
        {
            Assertions.NullCheck(info, "info");
            if (_declaredOperationOptions.Contains(info))
            {
                throw new InvalidOperationException("OperationOption already defined: " +
                        info.Name);
            }
            _declaredOperationOptions.Add(info);
            foreach (SafeType<APIOperation> op in
                FrameworkUtil.GetDefaultSupportedOperations(_connectorClass))
            {
                ICollection<OperationOptionInfo> oclasses =
                    CollectionUtil.GetValue(_supportedOptionsByOperation, op, null);
                if (oclasses == null)
                {
                    oclasses = new HashSet<OperationOptionInfo>();
                    _supportedOptionsByOperation[op] = oclasses;
                }
                oclasses.Add(info);
            }
        }

        /// <summary>
        /// Adds another ObjectClassInfo to the schema.
        /// </summary>
        /// <remarks>
        /// Also, adds this
        /// to the set of supported classes for every operation defined by
        /// the Connector.
        /// </remarks>
        /// <exception cref="IllegalStateException">If already defined</exception>
        public void DefineObjectClass(String type, ICollection<ConnectorAttributeInfo> attrInfo)
        {
            ObjectClassInfoBuilder bld = new ObjectClassInfoBuilder();
            bld.ObjectType = type;
            bld.AddAllAttributeInfo(attrInfo);
            ObjectClassInfo obj = bld.Build();
            DefineObjectClass(obj);
        }

        /// <summary>
        /// Adds another OperationOptionInfo to the schema.
        /// </summary>
        /// <remarks>
        /// Also, adds this
        /// to the set of supported options for every operation defined by
        /// the Connector.
        /// </remarks>
        /// <exception cref="IllegalStateException">If already defined</exception>
        public void DefineOperationOption(String optionName, Type type)
        {
            OperationOptionInfoBuilder bld = new OperationOptionInfoBuilder();
            bld.Name = (optionName);
            bld.OptionType = (type);
            OperationOptionInfo info = bld.Build();
            DefineOperationOption(info);
        }

        /// <summary>
        /// Adds the given ObjectClassInfo as a supported ObjectClass for
        /// the given operation.
        /// </summary>
        /// <param name="op">The SPI operation</param>
        /// <param name="def">The ObjectClassInfo</param>
        /// <exception cref="ArgumentException">If the given ObjectClassInfo was
        /// not already defined using <see cref="DefineObjectClass(ObjectClassInfo)" />.</exception>
        public void AddSupportedObjectClass(SafeType<SPIOperation> op,
                ObjectClassInfo def)
        {
            Assertions.NullCheck(op, "op");
            Assertions.NullCheck(def, "def");
            ICollection<SafeType<APIOperation>> apis =
                FrameworkUtil.Spi2Apis(op);
            if (!_declaredObjectClasses.Contains(def))
            {
                throw new ArgumentException("ObjectClass " + def.ObjectType +
                        " not defined in schema.");
            }
            foreach (SafeType<APIOperation> api in apis)
            {
                ICollection<ObjectClassInfo> infos =
                    CollectionUtil.GetValue(_supportedObjectClassesByOperation, api, null);
                if (infos == null)
                {
                    throw new ArgumentException("Operation " + op +
                            " not implement by connector.");
                }
                if (infos.Contains(def))
                {
                    throw new ArgumentException("ObjectClass " + def.ObjectType +
                            " already supported for operation " + op);
                }
                infos.Add(def);
            }
        }

        /// <summary>
        /// Removes the given ObjectClassInfo as a supported ObjectClass for
        /// the given operation.
        /// </summary>
        /// <param name="op">The SPI operation</param>
        /// <param name="def">The ObjectClassInfo</param>
        /// <exception cref="ArgumentException">If the given ObjectClassInfo was
        /// not already defined using <see cref="DefineObjectClass(ObjectClassInfo)" />.</exception>
        public void RemoveSupportedObjectClass(SafeType<SPIOperation> op,
                ObjectClassInfo def)
        {
            Assertions.NullCheck(op, "op");
            Assertions.NullCheck(def, "def");
            ICollection<SafeType<APIOperation>> apis =
                FrameworkUtil.Spi2Apis(op);
            if (!_declaredObjectClasses.Contains(def))
            {
                throw new ArgumentException("ObjectClass " + def.ObjectType +
                        " not defined in schema.");
            }
            foreach (SafeType<APIOperation> api in apis)
            {
                ICollection<ObjectClassInfo> infos =
                    CollectionUtil.GetValue(_supportedObjectClassesByOperation, api, null);
                if (infos == null)
                {
                    throw new ArgumentException("Operation " + op +
                            " not implement by connector.");
                }
                if (!infos.Contains(def))
                {
                    throw new ArgumentException("ObjectClass " + def.ObjectType
                            + " already removed for operation " + op);
                }
                infos.Remove(def);
            }
        }
        /// <summary>
        /// Adds the given OperationOptionInfo as a supported option for
        /// the given operation.
        /// </summary>
        /// <param name="op">The SPI operation</param>
        /// <param name="def">The OperationOptionInfo</param>
        /// <exception cref="ArgumentException">If the given OperationOptionInfo was
        /// not already defined using <see cref="DefineOperationOption(OperationOptionInfo)" />.</exception>
        public void AddSupportedOperationOption(SafeType<SPIOperation> op,
                OperationOptionInfo def)
        {
            Assertions.NullCheck(op, "op");
            Assertions.NullCheck(def, "def");
            ICollection<SafeType<APIOperation>> apis =
                FrameworkUtil.Spi2Apis(op);
            if (!_declaredOperationOptions.Contains(def))
            {
                throw new ArgumentException("OperationOption " + def.Name +
                        " not defined in schema.");
            }
            foreach (SafeType<APIOperation> api in apis)
            {
                ICollection<OperationOptionInfo> infos =
                    CollectionUtil.GetValue(_supportedOptionsByOperation, api, null);
                if (infos == null)
                {
                    throw new ArgumentException("Operation " + op +
                            " not implement by connector.");
                }
                if (infos.Contains(def))
                {
                    throw new ArgumentException("OperationOption " + def.Name +
                            " already supported for operation " + op);
                }
                infos.Add(def);
            }
        }

        /// <summary>
        /// Removes the given OperationOptionInfo as a supported option for
        /// the given operation.
        /// </summary>
        /// <param name="op">The SPI operation</param>
        /// <param name="def">The OperationOptionInfo</param>
        /// <exception cref="ArgumentException">If the given OperationOptionInfo was
        /// not already defined using <see cref="DefineOperationOption(OperationOptionInfo)" />.</exception>
        public void RemoveSupportedOperationOption(SafeType<SPIOperation> op,
                OperationOptionInfo def)
        {
            Assertions.NullCheck(op, "op");
            Assertions.NullCheck(def, "def");
            ICollection<SafeType<APIOperation>> apis =
                FrameworkUtil.Spi2Apis(op);
            if (!_declaredOperationOptions.Contains(def))
            {
                throw new ArgumentException("OperationOption " + def.Name +
                        " not defined in schema.");
            }
            foreach (SafeType<APIOperation> api in apis)
            {
                ICollection<OperationOptionInfo> infos =
                    CollectionUtil.GetValue(_supportedOptionsByOperation, api, null);
                if (infos == null)
                {
                    throw new ArgumentException("Operation " + op +
                            " not implement by connector.");
                }
                if (!infos.Contains(def))
                {
                    throw new ArgumentException("OperationOption " + def.Name +
                            " already removed for operation " + op);
                }
                infos.Remove(def);
            }
        }

        /// <summary>
        /// Clears the operation-specific supported classes.
        /// </summary>
        /// <remarks>
        /// Normally, when
        /// you add an ObjectClass, using <see cref="DefineObjectClass(ObjectClassInfo)" />,
        /// it is added to all operations. You may then remove those that you need
        /// using <see cref="RemoveSupportedObjectClass(SafeType{SPIOperation}, ObjectClassInfo)" />. You
        /// may wish, as an alternative to clear everything out and instead add using
        /// <see cref="AddSupportedObjectClass(SafeType{SPIOperation}, ObjectClassInfo)" />.
        /// </remarks>
        public void ClearSupportedObjectClassesByOperation()
        {
            foreach (ICollection<ObjectClassInfo> values in
                _supportedObjectClassesByOperation.Values)
            {
                values.Clear();
            }
        }
        /// <summary>
        /// Clears the operation-specific supported options.
        /// </summary>
        /// <remarks>
        /// Normally, when
        /// you add an OperationOptionInfo, using <see cref="DefineOperationOption(OperationOptionInfo)" />,
        /// it is added to all operations. You may then remove those that you need
        /// using <see cref="RemoveSupportedOperationOption(SafeType{SPIOperation}, OperationOptionInfo)" />. You
        /// may wish, as an alternative to clear everything out and instead add using
        /// <see cref="AddSupportedOperationOption(SafeType{SPIOperation}, OperationOptionInfo)" />.
        /// </remarks>
        public void ClearSupportedOptionsByOperation()
        {
            foreach (ICollection<OperationOptionInfo> values in
                _supportedOptionsByOperation.Values)
            {
                values.Clear();
            }
        }

        /// <summary>
        /// Builds the <see cref="Schema" /> object based on the <see cref="ObjectClassInfo" />s
        /// added so far.
        /// </summary>
        /// <returns>new Schema object based on the info provided.</returns>
        public Schema Build()
        {
            if (_declaredObjectClasses.Count == 0)
            {
                String ERR = "Must be at least one ObjectClassInfo object!";
                throw new InvalidOperationException(ERR);
            }
            return new Schema(_declaredObjectClasses,
                              _declaredOperationOptions,
                              _supportedObjectClassesByOperation,
                              _supportedOptionsByOperation);
        }
    }
    #endregion

    #region Script
    /// <summary>
    /// Represents a script in a scripting language.
    /// </summary>
    /// <since>1.1</since>
    public sealed class Script
    {

        private readonly string scriptLanguage;
        private readonly string scriptText;

        internal Script(string scriptLanguage, string scriptText)
        {
            Assertions.BlankCheck(scriptLanguage, "scriptLanguage");
            Assertions.NullCheck(scriptText, "scriptText"); // Allow empty text.
            this.scriptLanguage = scriptLanguage;
            this.scriptText = scriptText;
        }

        /// <summary>
        /// Returns the language of this script.
        /// </summary>
        /// <returns>the script language; never null.</returns>
        public string ScriptLanguage
        {
            get
            {
                return scriptLanguage;
            }
        }

        /// <summary>
        /// Returns the text of this script.
        /// </summary>
        /// <returns>the script text; never null.</returns>
        public string ScriptText
        {
            get
            {
                return scriptText;
            }
        }

        public override int GetHashCode()
        {
            return scriptLanguage.GetHashCode() ^ scriptText.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (obj is Script)
            {
                Script other = (Script)obj;
                if (!scriptLanguage.Equals(other.scriptLanguage))
                {
                    return false;
                }
                if (!scriptText.Equals(other.scriptText))
                {
                    return false;
                }
                return true;
            }
            return false;
        }

        public override string ToString()
        {
            // Text can be large, probably should not be included.
            return "Script: " + scriptLanguage;
        }
    }
    #endregion

    #region ScriptBuilder
    /// <summary>
    /// Builder for <see cref="Script" />.
    /// </summary>
    public class ScriptBuilder
    {
        /// <summary>
        /// Creates a new <code>ScriptBuilder</code>.
        /// </summary>
        public ScriptBuilder()
        {
        }

        /// <summary>
        /// Gets/sets the language of the script.
        /// </summary>
        public string ScriptLanguage
        {
            get;
            set;
        }

        /// <summary>
        /// Gets/sets the text of the script.
        /// </summary>
        public string ScriptText
        {
            get;
            set;
        }

        /// <summary>
        /// Creates a <code>Script</code>.
        /// </summary>
        /// <remarks>
        /// Prior to calling this method the language
        /// and the text should have been set.
        /// </remarks>
        /// <returns>a new script; never null.</returns>
        public Script Build()
        {
            return new Script(ScriptLanguage, ScriptText);
        }
    }
    #endregion

    #region ScriptContext
    /// <summary>
    /// Encapsulates a script and all of its parameters.
    /// </summary>
    /// <seealso cref="Org.IdentityConnectors.Framework.Api.Operations.ScriptOnResourceApiOp" />
    /// <seealso cref="Org.IdentityConnectors.Framework.Api.Operations.ScriptOnConnectorApiOp" />
    public sealed class ScriptContext
    {
        private readonly String _scriptLanguage;
        private readonly String _scriptText;
        private readonly IDictionary<String, Object> _scriptArguments;

        /// <summary>
        /// Public only for serialization; please use <see cref="ScriptContextBuilder" />.
        /// </summary>
        /// <param name="scriptLanguage">The script language. Must not be null.</param>
        /// <param name="scriptText">The script text. Must not be null.</param>
        /// <param name="scriptArguments">The script arguments. May be null.</param>
        public ScriptContext(String scriptLanguage,
                String scriptText,
                IDictionary<String, Object> scriptArguments)
        {

            if (scriptLanguage == null)
            {
                throw new ArgumentException("Argument 'scriptLanguage' must be specified");
            }
            if (scriptText == null)
            {
                throw new ArgumentException("Argument 'scriptText' must be specified");
            }
            //clone script arguments and options - this serves two purposes
            //1)makes sure everthing is serializable
            //2)does a deep copy
            IDictionary<Object, Object> scriptArgumentsClone = (IDictionary<Object, Object>)SerializerUtil.CloneObject(scriptArguments);
            _scriptLanguage = scriptLanguage;
            _scriptText = scriptText;
            _scriptArguments = CollectionUtil.NewReadOnlyDictionary<object, object, string, object>(scriptArgumentsClone);
        }

        /// <summary>
        /// Identifies the language in which the script is written
        /// (e.g., <code>bash</code>, <code>csh</code>,
        /// <code>Perl4</code> or <code>Python</code>).
        /// </summary>
        /// <returns>The script language.</returns>
        public String ScriptLanguage
        {
            get
            {
                return _scriptLanguage;
            }
        }

        /// <summary>
        /// Returns the text (i.e., actual characters) of the script.
        /// </summary>
        /// <returns>The text of the script.</returns>
        public String ScriptText
        {
            get
            {
                return _scriptText;
            }
        }

        /// <summary>
        /// Returns a map of arguments to be passed to the script.
        /// </summary>
        /// <remarks>
        /// Values must be types that the framework can serialize.
        /// See <see cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" /> for a list of supported types.
        /// </remarks>
        /// <returns>A map of arguments to be passed to the script.</returns>
        public IDictionary<String, Object> ScriptArguments
        {
            get
            {
                return _scriptArguments;
            }
        }

        public override string ToString()
        {
            StringBuilder bld = new StringBuilder();
            bld.Append("ScriptContext: ");
            // poor man's to string method.
            IDictionary<string, object> map = new Dictionary<string, object>();
            map["Language"] = ScriptLanguage;
            map["Text"] = ScriptText;
            map["Arguments"] = ScriptArguments;
            bld.Append(map.ToString());
            return bld.ToString();
        }

    }
    #endregion

    #region ScriptContextBuilder
    /// <summary>
    /// Builds an <see cref="ScriptContext" />.
    /// </summary>
    public sealed class ScriptContextBuilder
    {
        private String _scriptLanguage;
        private String _scriptText;
        private readonly IDictionary<String, Object> _scriptArguments = new
        Dictionary<String, Object>();

        /// <summary>
        /// Creates an empty builder.
        /// </summary>
        public ScriptContextBuilder()
        {

        }

        /// <summary>
        /// Creates a builder with the required parameters specified.
        /// </summary>
        /// <param name="scriptLanguage">a string that identifies the language
        /// in which the script is written
        /// (e.g., <code>bash</code>, <code>csh</code>,
        /// <code>Perl4</code> or <code>Python</code>).</param>
        /// <param name="scriptText">The text (i.e., actual characters) of the script.</param>
        public ScriptContextBuilder(String scriptLanguage,
                String scriptText)
        {
            _scriptLanguage = scriptLanguage;
            _scriptText = scriptText;
        }

        /// <summary>
        /// Identifies the language in which the script is written
        /// (e.g., <code>bash</code>, <code>csh</code>,
        /// <code>Perl4</code> or <code>Python</code>).
        /// </summary>
        /// <returns>The script language.</returns>
        public String ScriptLanguage
        {
            get
            {
                return _scriptLanguage;
            }
            set
            {
                _scriptLanguage = value;
            }
        }

        /// <summary>
        /// Returns the actual characters of the script.
        /// </summary>
        /// <returns>the actual characters of the script.</returns>
        public String ScriptText
        {
            get
            {
                return _scriptText;
            }
            set
            {
                _scriptText = value;
            }
        }

        /// <summary>
        /// Adds or sets an argument to pass to the script.
        /// </summary>
        /// <param name="name">The name of the argument. Must not be null.</param>
        /// <param name="value">The value of the argument. Must be one of
        /// type types that the framework can serialize.</param>
        /// <seealso cref="Org.IdentityConnectors.Framework.Common.Serializer.ObjectSerializerFactory" />
        public ScriptContextBuilder AddScriptArgument(String name, Object value)
        {
            if (name == null)
            {
                throw new ArgumentException("Argument 'name' cannot be null.");
            }
            //don't validate value here - we do that implicitly when
            //we clone in the constructor of ScriptRequest
            _scriptArguments[name] = value;
            return this;
        }

        /// <summary>
        /// Removes the given script argument.
        /// </summary>
        /// <param name="name">The name of the argument. Must not be null.</param>
        public ScriptContextBuilder RemoveScriptArgument(String name)
        {
            if (name == null)
            {
                throw new ArgumentException("Argument 'name' cannot be null.");
            }
            _scriptArguments.Remove(name);
            return this;
        }

        /// <summary>
        /// Returns a mutable reference of the script arguments map.
        /// </summary>
        /// <returns>A mutable reference of the script arguments map.</returns>
        public IDictionary<String, Object> ScriptArguments
        {
            get
            {
                //might as well be mutable since it's the builder and
                //we don't want to deep copy anyway
                return _scriptArguments;
            }
        }

        /// <summary>
        /// Creates a <code>ScriptContext</code>.
        /// </summary>
        /// <remarks>
        /// The <code>scriptLanguage</code> and <code>scriptText</code>
        /// must be set prior to calling this.
        /// </remarks>
        /// <returns>The <code>ScriptContext</code>.</returns>
        public ScriptContext Build()
        {
            return new ScriptContext(_scriptLanguage,
                    _scriptText,
                    _scriptArguments);
        }
    }
    #endregion

    #region SyncDelta
    /// <summary>
    /// Represents a change to an object in a resource.
    /// </summary>
    /// <seealso cref="Org.IdentityConnectors.Framework.Api.Operations.SyncApiOp" />
    /// <seealso cref="Org.IdentityConnectors.Framework.Spi.Operations.SyncOp" />
    public sealed class SyncDelta
    {
        private readonly SyncToken _token;
        private readonly SyncDeltaType _deltaType;
        private readonly Uid _previousUid;
        private readonly Uid _uid;
        private readonly ConnectorObject _object;

        /// <summary>
        /// Creates a SyncDelata
        /// </summary>
        /// <param name="token">The token. Must not be null.</param>
        /// <param name="deltaType">The delta. Must not be null.</param>
        /// <param name="uid">The uid. Must not be null.</param>
        /// <param name="object">The object that has changed. May be null for delete.</param>
        internal SyncDelta(SyncToken token, SyncDeltaType deltaType,
                Uid previousUid, Uid uid,
                ConnectorObject obj)
        {
            Assertions.NullCheck(token, "token");
            Assertions.NullCheck(deltaType, "deltaType");
            Assertions.NullCheck(uid, "uid");

            //do not allow previous Uid for anything else than create or update
            if (previousUid != null && deltaType != SyncDeltaType.CREATE_OR_UPDATE)
            {
                throw new ArgumentException("The previous Uid can only be specified for create or update.");
            }

            //only allow null object for delete
            if (obj == null &&
                 deltaType != SyncDeltaType.DELETE)
            {
                throw new ArgumentException("ConnectorObject must be specified for anything other than delete.");
            }

            //if object not null, make sure its Uid
            //matches
            if (obj != null)
            {
                if (!uid.Equals(obj.Uid))
                {
                    throw new ArgumentException("Uid does not match that of the object.");
                }
            }

            _token = token;
            _deltaType = deltaType;
            _previousUid = previousUid;
            _uid = uid;
            _object = obj;

        }

        /// <summary>
        /// If the change described by this <code>SyncDelta</code> modified the
        /// object's Uid, this method returns the Uid before the change.
        /// </summary>
        /// <remarks>
        /// Not
        /// all resources can determine the previous Uid, so this method can
        /// return <code>null</code>.
        /// </remarks>
        /// <returns>the previous Uid or null if it could not be determined
        /// or the change did not modify the Uid.</returns>
        public Uid PreviousUid
        {
            get
            {
                return _previousUid;
            }
        }

        /// <summary>
        /// Returns the <code>Uid</code> of the object that changed.
        /// </summary>
        /// <returns>the <code>Uid</code> of the object that changed.</returns>
        public Uid Uid
        {
            get
            {
                return _uid;
            }
        }

        /// <summary>
        /// Returns the connector object that changed.
        /// </summary>
        /// <remarks>
        /// This
        /// may be null in the case of delete.
        /// </remarks>
        /// <returns>The object or possibly null if this
        /// represents a delete.</returns>
        public ConnectorObject Object
        {
            get
            {
                return _object;
            }
        }

        /// <summary>
        /// Returns the <code>SyncToken</code> of the object that changed.
        /// </summary>
        /// <returns>the <code>SyncToken</code> of the object that changed.</returns>
        public SyncToken Token
        {
            get
            {
                return _token;
            }
        }

        /// <summary>
        /// Returns the type of the change the occured.
        /// </summary>
        /// <returns>The type of change that occured.</returns>
        public SyncDeltaType DeltaType
        {
            get
            {
                return _deltaType;
            }
        }


        public override String ToString()
        {
            IDictionary<String, Object> values = new Dictionary<String, Object>();
            values["Token"] = _token;
            values["DeltaType"] = _deltaType;
            values["PreviousUid"] = _previousUid;
            values["Uid"] = _uid;
            values["Object"] = _object;
            return values.ToString();
        }

        public override int GetHashCode()
        {
            return _uid.GetHashCode();
        }

        public override bool Equals(Object o)
        {
            if (o is SyncDelta)
            {
                SyncDelta other = (SyncDelta)o;
                if (!_token.Equals(other._token))
                {
                    return false;
                }
                if (!_deltaType.Equals(other._deltaType))
                {
                    return false;
                }
                if (_previousUid == null)
                {
                    if (other._previousUid != null)
                    {
                        return false;
                    }
                }
                else if (!_previousUid.Equals(other._previousUid))
                {
                    return false;
                }
                if (!_uid.Equals(other._uid))
                {
                    return false;
                }
                if (_object == null)
                {
                    if (other._object != null)
                    {
                        return false;
                    }
                }
                else if (!_object.Equals(other._object))
                {
                    return false;
                }
                return true;
            }
            return false;
        }
    }
    #endregion

    #region SyncDeltaBuilder
    /// <summary>
    /// Builder for <see cref="SyncDelta" />.
    /// </summary>
    public sealed class SyncDeltaBuilder
    {
        private SyncToken _token;
        private SyncDeltaType _deltaType;
        private Uid _previousUid;
        private Uid _uid;
        private ConnectorObject _object;

        /// <summary>
        /// Create a new <code>SyncDeltaBuilder</code>
        /// </summary>
        public SyncDeltaBuilder()
        {

        }

        /// <summary>
        /// Creates a new <code>SyncDeltaBuilder</code> whose
        /// values are initialized to those of the delta.
        /// </summary>
        /// <param name="delta">The original delta.</param>
        public SyncDeltaBuilder(SyncDelta delta)
        {
            _token = delta.Token;
            _deltaType = delta.DeltaType;
            _previousUid = delta.PreviousUid;
            _uid = delta.Uid;
            _object = delta.Object;
        }

        /// <summary>
        /// Returns the <code>SyncToken</code> of the object that changed.
        /// </summary>
        /// <returns>the <code>SyncToken</code> of the object that changed.</returns>
        public SyncToken Token
        {
            get
            {
                return _token;
            }
            set
            {
                _token = value;
            }
        }

        /// <summary>
        /// Returns the type of the change that occurred.
        /// </summary>
        /// <returns>The type of change that occurred.</returns>
        public SyncDeltaType DeltaType
        {
            get
            {
                return _deltaType;
            }
            set
            {
                _deltaType = value;
            }
        }

        /// <summary>
        /// Returns the <code>Uid</code> before the change.
        /// </summary>
        /// <returns>the <code>Uid</code> before the change.</returns>
        public Uid PreviousUid
        {
            get
            {
                return _previousUid;
            }
            set
            {
                _previousUid = value;
            }
        }

        /// <summary>
        /// Returns the <code>Uid</code> of the object that changed.
        /// </summary>
        /// <remarks>
        /// Note that this is implicitly set when you call
        /// <see cref="Object" />.
        /// </remarks>
        /// <returns>the <code>Uid</code> of the object that changed.</returns>
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

        /// <summary>
        /// Returns the object that changed.
        /// </summary>
        /// <remarks>
        /// Sets the object that changed and implicitly
        /// sets Uid if object is not null.
        /// </remarks>
        /// <returns>The object that changed. May be null for
        /// deletes.</returns>
        public ConnectorObject Object
        {
            get
            {
                return _object;
            }
            set
            {
                _object = value;
                if (value != null)
                {
                    _uid = value.Uid;
                }
            }
        }

        /// <summary>
        /// Creates a SyncDelta.
        /// </summary>
        /// <remarks>
        /// Prior to calling the following must be specified:
        /// <list type="number">
        /// <item>
        /// <description><see cref="Object" /> (for anything other than delete)
        /// </description>
        /// </item>
        /// <item>
        /// <description><see cref="Uid" /> (this is implictly set when calling <see cref="Object" />)
        /// </description>
        /// </item>
        /// <item>
        /// <description><see cref="Token" />
        /// </description>
        /// </item>
        /// <item>
        /// <description><see cref="DeltaType" />
        /// </description>
        /// </item>
        /// </list>
        /// </remarks>
        public SyncDelta Build()
        {
            return new SyncDelta(_token, _deltaType, _previousUid, _uid, _object);
        }
    }
    #endregion

    #region SyncDeltaType
    /// <summary>
    /// The type of change.
    /// </summary>
    public enum SyncDeltaType
    {
        /// <summary>
        /// The change represents either a create or an update in
        /// the resource.
        /// </summary>
        /// <remarks>
        /// These are combined into a single value because:
        /// <list type="number">
        /// <item>
        /// <description>Many resources will not be able to distinguish a create from an update.
        /// Those that have an audit log will be able to. However, many implementations
        /// will only have the current record and a modification timestamp.
        /// </description>
        /// </item>
        /// <item>
        /// <description>Regardless of whether or not the resource can distinguish the two cases,
        /// the application needs to distinguish.
        /// </description>
        /// </item>
        /// </list>
        /// </remarks>
        CREATE_OR_UPDATE,

        /// <summary>
        /// The change represents a DELETE in the resource
        /// </summary>
        DELETE
    }
    #endregion

    #region SyncResultsHandler
    /// <summary>
    /// Called to handle a delta in the stream.
    /// </summary>
    /// <remarks>
    /// Will be called multiple times,
    /// once for each result. Although a callback, this is still invoked
    /// synchronously. That is, it is guaranteed that following a call to
    /// <see cref="SyncApiOp.Sync(ObjectClass, SyncToken, SyncResultsHandler, OperationOptions)" /> no
    /// more invocations to <see cref="Handle(SyncDelta)" /> will be performed.
    /// </remarks>
    /// <param name="delta">The change</param>
    /// <returns>True iff the application wants to continue processing more
    /// results.</returns>
    /// <exception cref="Exception">If the application encounters an exception. This will stop
    /// the interation and the exception will be propogated back to
    /// the application.</exception>
    public delegate bool SyncResultsHandler(SyncDelta delta);
    #endregion

    #region SyncToken
    /// <summary>
    /// Abstract "place-holder" for synchronization.
    /// </summary>
    /// <remarks>
    /// The application must not make
    /// any attempt to interpret the value of the token. From the standpoint of the
    /// application the token is merely a black-box. The application may only persist
    /// the value of the token for use on subsequent synchronization requests.
    /// <para>
    /// What this token represents is entirely connector-specific. On some connectors
    /// this might be a last-modified value. On others, it might be a unique ID of a
    /// log table entry. On others such as JMS, this might be a dummy value since
    /// JMS itself keeps track of the state of the sync.
    /// </para>
    /// </remarks>
    public sealed class SyncToken
    {

        private Object _value;

        /// <summary>
        /// Creates a new
        /// </summary>
        /// <param name="value">May not be null. TODO: define set of allowed value types
        /// (currently same as set of allowed attribute values).</param>
        public SyncToken(Object value)
        {
            Assertions.NullCheck(value, "value");
            FrameworkUtil.CheckAttributeValue(value);
            _value = value;
        }

        /// <summary>
        /// Returns the value for the token.
        /// </summary>
        /// <returns>The value for the token.</returns>
        public Object Value
        {
            get
            {
                return _value;
            }
        }

        public override String ToString()
        {
            return "SyncToken: " + _value.ToString();
        }

        public override int GetHashCode()
        {
            return CollectionUtil.GetHashCode(_value);
        }

        public override bool Equals(Object o)
        {
            if (o is SyncToken)
            {
                SyncToken other = (SyncToken)o;
                return CollectionUtil.Equals(_value, other._value);
            }
            return false;
        }


    }
    #endregion

    #region Uid
    public sealed class Uid : ConnectorAttribute
    {

        public static readonly string NAME = ConnectorAttributeUtil.CreateSpecialName("UID");

        public Uid(String val)
            : base(NAME, CollectionUtil.NewReadOnlyList<object>(Check(val)))
        {
        }
        private static String Check(String value)
        {
            if (StringUtil.IsBlank(value))
            {
                String ERR = "Uid value must not be blank!";
                throw new ArgumentException(ERR);
            }
            return value;
        }
        /// <summary>
        /// The single value of the attribute that is the unique id of an object.
        /// </summary>
        /// <returns>value that identifies an object.</returns>
        public String GetUidValue()
        {
            return ConnectorAttributeUtil.GetStringValue(this);
        }
    }
    #endregion

    #region ConnectorAttributesAccessor
    /// <summary>
    /// Attributes Accessor convenience methods for accessing attributes.
    /// </summary>
    /// <remarks>
    /// This class wraps a set of attributes to make lookup faster than the
    /// <see cref="ConnectorAttributeUtil.Find(String, ICollection{ConnectorAttribute})" /> method, since that method must
    /// re-create the map each time.
    /// </remarks>
    /// <author>Warren Strange</author>
    public class ConnectorAttributesAccessor
    {

        ICollection<ConnectorAttribute> _attrs;
        IDictionary<String, ConnectorAttribute> _attrMap;

        public ConnectorAttributesAccessor(ICollection<ConnectorAttribute> attrs)
        {
            _attrs = attrs;
            _attrMap = ConnectorAttributeUtil.ToMap(attrs);
        }

        /// <summary>
        /// Find the named attribute
        /// </summary>
        /// <param name="name">-
        /// the attribute name to search for</param>
        /// <returns>the Attribute, or null if not found.</returns>
        public ConnectorAttribute Find(String name)
        {
            return CollectionUtil.GetValue(_attrMap, name, null);
        }

        /// <summary>
        /// Get the <see cref="Name" /> attribute from the set of attributes.
        /// </summary>
        /// <returns>the <see cref="Name" /> attribute in the set.</returns>
        public Name GetName()
        {
            return (Name)Find(Name.NAME);
        }

        /// <summary>
        /// Return the enabled status of the account.
        /// </summary>
        /// <remarks>
        /// If the ENABLE operational attribute is present, it's value takes
        /// precedence over the current value. If it is missing, the currentlyEnabled
        /// status is returned instead.
        /// </remarks>
        /// <param name="dflt">the default state if enable is not found.</param>
        /// <returns>true if the account is enabled, false otherwise</returns>
        public bool GetEnabled(bool dflt)
        {
            bool e = dflt;
            ConnectorAttribute enable = Find(OperationalAttributes.ENABLE_NAME);
            if (enable != null)
            {
                e = ConnectorAttributeUtil.GetBooleanValue(enable).Value;
            }
            return e;
        }

        /// <summary>
        /// Get the password as a GuardeString
        /// </summary>
        /// <returns>the password as a guarded String</returns>
        public GuardedString GetPassword()
        {
            ConnectorAttribute a = Find(OperationalAttributes.PASSWORD_NAME);
            return a == null ? null : ConnectorAttributeUtil.GetGuardedStringValue(a);
        }

        /// <summary>
        /// Return a list of attributes
        /// </summary>
        /// <param name="name">-
        /// name of attribute to search for.</param>
        /// <returns>The List (generic object) iff it exists otherwise null.</returns>
        public IList<Object> FindList(String name)
        {
            ConnectorAttribute a = Find(name);
            return (a == null) ? null : a.Value;
        }

        /// <summary>
        /// Return the multivalued attribute as a list of strings.
        /// </summary>
        /// <remarks>
        /// This will throw a
        /// ClassCastException if the underlying attribute list is not of type
        /// String.
        /// </remarks>
        /// <param name="name">the name of the attribute to search for</param>
        /// <returns>a List of String values for the attribute</returns>
        public IList<String> FindStringList(String name)
        {
            IList<Object> l = FindList(name);
            if (l != null)
            {
                IList<String> ret = new List<String>(l.Count);
                foreach (object o in l)
                {
                    ret.Add((String)o);
                }
                return ret;
            }
            return null;
        }

        /// <summary>
        /// Determines if the set as the attribute specified.
        /// </summary>
        /// <param name="name">attribute name</param>
        /// <returns>true if the named attribute exists, false otherwise</returns>
        public bool HasAttribute(String name)
        {
            return Find(name) != null;
        }

        /// <summary>
        /// Get the string value from the specified (single-valued) attribute.
        /// </summary>
        /// <param name="name">Attribute from which to retrieve the long value.</param>
        /// <returns>null if the value is null otherwise the long value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an long.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi-valued (rather than
        /// single-valued).</exception>
        public String FindString(String name)
        {
            ConnectorAttribute a = Find(name);
            return a == null ? null : ConnectorAttributeUtil.GetStringValue(a);
        }

        /// <summary>
        /// Get the integer value from the specified (single-valued) attribute.
        /// </summary>
        /// <param name="name">Attribute from which to retrieve the long value.</param>
        /// <returns>null if the value is null otherwise the long value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an long.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi-valued (rather than
        /// single-valued).</exception>
        public int? FindInteger(String name)
        {
            ConnectorAttribute a = Find(name);
            return (a == null) ? null : ConnectorAttributeUtil.GetIntegerValue(a);
        }

        /// <summary>
        /// Get the long value from the specified (single-valued) attribute.
        /// </summary>
        /// <param name="name">Attribute from which to retrieve the long value.</param>
        /// <returns>null if the value is null otherwise the long value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an long.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi-valued (rather than
        /// single-valued).</exception>
        public long? FindLong(String name)
        {
            ConnectorAttribute a = Find(name);
            return a == null ? null : ConnectorAttributeUtil.GetLongValue(a);
        }

        /// <summary>
        /// Get the date value from the specified (single-valued) attribute that
        /// contains a long.
        /// </summary>
        /// <param name="name">Attribute from which to retrieve the date value.</param>
        /// <returns>null if the value is null otherwise the date value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an long.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi-valued (rather than
        /// single-valued).</exception>
        public DateTime? FindDateTime(String name)
        {
            ConnectorAttribute a = Find(name);
            return a == null ? null : ConnectorAttributeUtil.GetDateTimeValue(a);
        }

        /// <summary>
        /// Get the integer value from the specified (single-valued) attribute.
        /// </summary>
        /// <param name="name">Attribute from which to retrieve the integer value.</param>
        /// <returns>null if the value is null otherwise the integer value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an integer.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi-valued (rather than
        /// single-valued)..</exception>
        public double? FindDouble(String name)
        {
            ConnectorAttribute a = Find(name);
            return a == null ? null : ConnectorAttributeUtil.GetDoubleValue(a);
        }

        /// <summary>
        /// Get the boolean value from the specified (single-valued) attribute.
        /// </summary>
        /// <param name="name">Attribute from which to retrieve the boolean value.</param>
        /// <returns>null if the value is null otherwise the boolean value for the
        /// attribute.</returns>
        /// <exception cref="InvalidCastException ">iff the object in the attribute is not an <see cref="Boolean" />.</exception>
        /// <exception cref="ArgumentException">iff the attribute is a multi-valued (rather than
        /// single-valued).</exception>
        public bool? FindBoolean(String name)
        {
            ConnectorAttribute a = Find(name);
            return a == null ? null : ConnectorAttributeUtil.GetBooleanValue(a);
        }
    }
    #endregion

    #region CultureInfoCache
    internal static class CultureInfoCache
    {
        private static readonly object LOCK = new Object();
        private static CultureInfo _instance;

        public static CultureInfo Instance
        {
            get
            {
                lock (LOCK)
                {
                    if (_instance == null)
                    {
                        _instance = CultureInfo.CurrentCulture;
                        if (_instance == null)
                        {
                            _instance = CultureInfo.InstalledUICulture;
                        }
                    }
                    return _instance;
                }
            }
        }
    }
    #endregion
}