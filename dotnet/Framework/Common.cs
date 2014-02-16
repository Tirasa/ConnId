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
 */
using System;
using System.Reflection;
using System.Text;
using System.Collections.Generic;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Spi.Operations;
using Org.IdentityConnectors.Framework.Common.Objects;
namespace Org.IdentityConnectors.Framework.Common
{
    #region FrameworkInternalBridge
    internal static class FrameworkInternalBridge
    {
        private static readonly Object LOCK = new Object();
        private static Assembly _assembly = null;
        /// <summary>
        /// Loads a class from the FrameworkInternal module
        /// </summary>
        /// <param name="typeName"></param>
        /// <returns></returns>
        public static SafeType<T> LoadType<T>(String typeName) where T : class
        {

            Assembly assembly;
            lock (LOCK)
            {
                if (_assembly == null)
                {
                    AssemblyName assemName = new AssemblyName();
                    assemName.Name = "FrameworkInternal";
                    _assembly = Assembly.Load(assemName);
                }
                assembly = _assembly;
            }

            return SafeType<T>.ForRawType(assembly.GetType(typeName, true));

        }
    }
    #endregion

    #region FrameworkUtil
    public static class FrameworkUtil
    {
        private static readonly IDictionary<SafeType<SPIOperation>, SafeType<APIOperation>> SPI_TO_API;
        private static readonly ICollection<Type> CONFIG_SUPPORTED_TYPES;
        private static readonly ICollection<Type> ATTR_SUPPORTED_TYPES;

        static FrameworkUtil()
        {
            IDictionary<SafeType<SPIOperation>, SafeType<APIOperation>> temp =
                new Dictionary<SafeType<SPIOperation>, SafeType<APIOperation>>();
            temp[SafeType<SPIOperation>.Get<AuthenticateOp>()] =
                SafeType<APIOperation>.Get<AuthenticationApiOp>();
            temp[SafeType<SPIOperation>.Get<ResolveUsernameOp>()] =
                SafeType<APIOperation>.Get<ResolveUsernameApiOp>();
            temp[SafeType<SPIOperation>.Get<CreateOp>()] =
                SafeType<APIOperation>.Get<CreateApiOp>();
            temp[SafeType<SPIOperation>.Get<DeleteOp>()] =
                SafeType<APIOperation>.Get<DeleteApiOp>();
            temp[SafeType<SPIOperation>.ForRawType(typeof(SearchOp<>))] =
                SafeType<APIOperation>.Get<SearchApiOp>();
            temp[SafeType<SPIOperation>.Get<UpdateOp>()] =
                SafeType<APIOperation>.Get<UpdateApiOp>();
            temp[SafeType<SPIOperation>.Get<UpdateAttributeValuesOp>()] =
                SafeType<APIOperation>.Get<UpdateApiOp>();
            temp[SafeType<SPIOperation>.Get<SchemaOp>()] =
                SafeType<APIOperation>.Get<SchemaApiOp>();
            temp[SafeType<SPIOperation>.Get<TestOp>()] =
                SafeType<APIOperation>.Get<TestApiOp>();
            temp[SafeType<SPIOperation>.Get<ScriptOnConnectorOp>()] =
                SafeType<APIOperation>.Get<ScriptOnConnectorApiOp>();
            temp[SafeType<SPIOperation>.Get<ScriptOnResourceOp>()] =
                SafeType<APIOperation>.Get<ScriptOnResourceApiOp>();
            temp[SafeType<SPIOperation>.Get<SyncOp>()] =
                SafeType<APIOperation>.Get<SyncApiOp>();
            SPI_TO_API = CollectionUtil.NewReadOnlyDictionary(temp);

            CONFIG_SUPPORTED_TYPES = CollectionUtil.NewReadOnlySet<Type>
            (
                typeof(string),
                typeof(long),
                typeof(long?),
                typeof(char),
                typeof(char?),
                typeof(double),
                typeof(double?),
                typeof(float),
                typeof(float?),
                typeof(int),
                typeof(int?),
                typeof(bool),
                typeof(bool?),
                typeof(Uri),
                typeof(FileName),
                typeof(GuardedByteArray),
                typeof(GuardedString),
                typeof(Script)
            );
            ATTR_SUPPORTED_TYPES = CollectionUtil.NewReadOnlySet<Type>
            (
                typeof(string),
                typeof(long),
                typeof(long?),
                typeof(char),
                typeof(char?),
                typeof(double),
                typeof(double?),
                typeof(float),
                typeof(float?),
                typeof(int),
                typeof(int?),
                typeof(bool),
                typeof(bool?),
                typeof(byte),
                typeof(byte?),
                typeof(byte[]),
                typeof(BigDecimal),
                typeof(BigInteger),
                typeof(GuardedByteArray),
                typeof(GuardedString)
            );

        }


        /// <summary>
        /// Determines if the class is a supported attribute type.
        /// </summary>
        /// <remarks>
        /// If not it throws
        /// an <see cref="ArgumentException" />.
        /// <list type="bullet">
        /// <item>
        /// <description>string
        /// </description>
        /// </item>
        /// <item>
        /// <description>long
        /// </description>
        /// </item>
        /// <item>
        /// <description>long?
        /// </description>
        /// </item>
        /// <item>
        /// <description>char
        /// </description>
        /// </item>
        /// <item>
        /// <description>char?
        /// </description>
        /// </item>
        /// <item>
        /// <description>double
        /// </description>
        /// </item>
        /// <item>
        /// <description>double?
        /// </description>
        /// </item>
        /// <item>
        /// <description>float
        /// </description>
        /// </item>
        /// <item>
        /// <description>float?
        /// </description>
        /// </item>
        /// <item>
        /// <description>int
        /// </description>
        /// </item>
        /// <item>
        /// <description>int?
        /// </description>
        /// </item>
        /// <item>
        /// <description>bool
        /// </description>
        /// </item>
        /// <item>
        /// <description>bool?
        /// </description>
        /// </item>
        /// <item>
        /// <description>byte
        /// </description>
        /// </item>
        /// <item>
        /// <description>byte?
        /// </description>
        /// </item>
        /// <item>
        /// <description>byte[]
        /// </description>
        /// </item>
        /// <item>
        /// <description>BigDecimal
        /// </description>
        /// </item>
        /// <item>
        /// <description>BigInteger
        /// </description>
        /// </item>
        /// </list>
        /// </remarks>
        /// <param name="clazz">type to check against the support list of types.</param>
        /// <exception cref="ArgumentException">iff the type is not on the supported list.</exception>
        public static void CheckAttributeType(Type type)
        {
            if (!FrameworkUtil.IsSupportedAttributeType(type))
            {
                String MSG = "Attribute type ''" + type + "'' is not supported.";
                throw new ArgumentException(MSG);
            }
        }
        /// <summary>
        /// Determines if the class of the object is a supported attribute type. If
        /// not it throws an <seealso cref="IllegalArgumentException"/>.
        /// </summary>
        /// <param name="value">
        ///            The value to check or null. </param>
        /// <exception cref="ArgumentException">
        ///             If the class of the object is a supported attribute type. </exception>
        public static void CheckAttributeValue(Object value)
        {
            if (value != null)
            {
                CheckAttributeType(value.GetType());
            }
        }
        public static ICollection<SafeType<APIOperation>> Spi2Apis(SafeType<SPIOperation> type)
        {
            type = type.GetTypeErasure();
            HashSet<SafeType<APIOperation>> set = new HashSet<SafeType<APIOperation>>();
            set.Add(SPI_TO_API[type]);
            // add GetApiOp if search is available..

            if (type.RawType.Equals(typeof(SearchOp<>)))
            {
                set.Add(SafeType<APIOperation>.Get<GetApiOp>());
            }
            return set;
        }
        public static ICollection<SafeType<SPIOperation>> AllSPIOperations()
        {
            return SPI_TO_API.Keys;
        }
        public static ICollection<SafeType<APIOperation>> AllAPIOperations()
        {
            ICollection<SafeType<APIOperation>> set =
                new HashSet<SafeType<APIOperation>>();
            CollectionUtil.AddAll(set,
                                  SPI_TO_API.Values);
            // add Get because it doesn't have a corresponding SPI.
            set.Add(SafeType<APIOperation>.Get<GetApiOp>());
            set.Add(SafeType<APIOperation>.Get<ValidateApiOp>());
            return CollectionUtil.AsReadOnlySet(set);
        }
        public static ICollection<SafeType<APIOperation>> GetDefaultSupportedOperations(SafeType<Connector> connector)
        {
            ICollection<SafeType<APIOperation>> rv =
                new HashSet<SafeType<APIOperation>>();
            ICollection<Type> interfaces =
                ReflectionUtil.GetTypeErasure(ReflectionUtil.GetAllInterfaces(connector.RawType));
            foreach (SafeType<SPIOperation> spi in AllSPIOperations())
            {
                if (interfaces.Contains(spi.RawType))
                {
                    CollectionUtil.AddAll(rv, Spi2Apis(spi));
                }
            }
            //finally add unconditionally supported ops
            CollectionUtil.AddAll(rv, GetUnconditionallySupportedOperations());
            return CollectionUtil.AsReadOnlySet(rv);
        }
        public static ICollection<SafeType<APIOperation>> GetUnconditionallySupportedOperations()
        {
            HashSet<SafeType<APIOperation>> ret;
            ret = new HashSet<SafeType<APIOperation>>();
            //add validate api op always
            ret.Add(SafeType<APIOperation>.Get<ValidateApiOp>());
            //add ScriptOnConnectorApiOp always
            ret.Add(SafeType<APIOperation>.Get<ScriptOnConnectorApiOp>());
            return ret;
        }
        public static ICollection<Type> GetAllSupportedConfigTypes()
        {
            return CONFIG_SUPPORTED_TYPES;
        }
        public static bool IsSupportedConfigurationType(Type type)
        {
            if (type.IsArray)
            {
                return IsSupportedConfigurationType(type.GetElementType());
            }
            else
            {
                return CONFIG_SUPPORTED_TYPES.Contains(type);
            }
        }
        public static ICollection<Type> GetAllSupportedAttributeTypes()
        {
            return ATTR_SUPPORTED_TYPES;
        }
        public static bool IsSupportedAttributeType(Type clazz)
        {
            return ATTR_SUPPORTED_TYPES.Contains(clazz);
        }

        /// <summary>
        /// Determines if the class is a supported type for an OperationOption.
        /// </summary>
        /// <remarks>
        /// If not it throws
        /// an <see cref="ArgumentException" />.
        /// </remarks>
        /// <param name="clazz">type to check against the support list of types.</param>
        /// <exception cref="ArgumentException">iff the type is not on the supported list.</exception>
        public static void CheckOperationOptionType(Type clazz)
        {
            //the set of supported operation option types
            //is the same as that for configuration beans plus Name,
            //ObjectClass, Uid, and QualifiedUid

            if (clazz.IsArray)
            {
                CheckOperationOptionType(clazz.GetElementType());
                return;
            }

            if (FrameworkUtil.IsSupportedConfigurationType(clazz))
            {
                return; //ok
            }

            if (typeof(ObjectClass).IsAssignableFrom(clazz))
            {
                return; //ok
            }

            if (typeof(Uid).IsAssignableFrom(clazz))
            {
                return; //ok
            }

            if (typeof(QualifiedUid).IsAssignableFrom(clazz))
            {
                return; //ok
            }

            if (typeof(SortKey).IsAssignableFrom(clazz))
            {
                return; //ok
            }

            String MSG = "ConfigurationOption type '+" + clazz.Name + "+' is not supported.";
            throw new ArgumentException(MSG);
        }
        /// <summary>
        /// Determines if the class of the object is a supported attribute type.
        /// </summary>
        /// <remarks>
        /// If not it throws an <see cref="ArgumentException" />.
        /// </remarks>
        /// <param name="value">The value to check or null.</param>
        public static void CheckOperationOptionValue(Object val)
        {
            if (val != null)
            {
                CheckOperationOptionType(val.GetType());
            }
        }

        /// <summary>
        /// Returns the version of the framework.
        /// </summary>
        /// <returns>the framework version; never null.</returns>
        public static Version GetFrameworkVersion()
        {
            return Assembly.GetExecutingAssembly().GetName().Version;
        }
    }
    #endregion

    #region VersionRange
    /// <summary>
    /// A version range is an interval describing a set of <seealso cref="Version versions"/>.
    /// <p/>
    /// A range has a left (lower) endpoint and a right (upper) endpoint. Each
    /// endpoint can be open (excluded from the set) or closed (included in the set).
    /// 
    /// <p>
    /// {@code VersionRange} objects are immutable.
    /// 
    /// @author Laszlo Hordos
    /// @Immutable
    /// </summary>
    public class VersionRange
    {

        /// <summary>
        /// The left endpoint is open and is excluded from the range.
        /// <p>
        /// The value of {@code LEFT_OPEN} is {@code '('}.
        /// </summary>
        public const char LEFT_OPEN = '(';
        /// <summary>
        /// The left endpoint is closed and is included in the range.
        /// <p>
        /// The value of {@code LEFT_CLOSED} is {@code '['}.
        /// </summary>
        public const char LEFT_CLOSED = '[';
        /// <summary>
        /// The right endpoint is open and is excluded from the range.
        /// <p>
        /// The value of {@code RIGHT_OPEN} is {@code ')'}.
        /// </summary>
        public const char RIGHT_OPEN = ')';
        /// <summary>
        /// The right endpoint is closed and is included in the range.
        /// <p>
        /// The value of {@code RIGHT_CLOSED} is {@code ']'}.
        /// </summary>
        public const char RIGHT_CLOSED = ']';

        private const string ENDPOINT_DELIMITER = ",";

        private readonly Version floorVersion;
        private readonly bool isFloorInclusive;
        private readonly Version ceilingVersion;
        private readonly bool isCeilingInclusive;
        private readonly bool empty;

        /// <summary>
        /// Parse version component into a Version.
        /// </summary>
        /// <param name="version">
        ///            version component string </param>
        /// <param name="range">
        ///            Complete range string for exception message, if any </param>
        /// <returns> Version </returns>
        private static Version parseVersion(string version, string range)
        {
            try
            {
                return Version.Parse(version);
            }
            catch (System.ArgumentException e)
            {
                throw new System.ArgumentException("invalid range \"" + range + "\": " + e.Message, e);
            }
        }

        /// <summary>
        /// Creates a version range from the specified string.
        /// 
        /// <p>
        /// Version range string grammar:
        /// 
        /// <pre>
        /// range ::= interval | at least
        /// interval ::= ( '[' | '(' ) left ',' right ( ']' | ')' )
        /// left ::= version
        /// right ::= version
        /// at least ::= version
        /// </pre>
        /// </summary>
        /// <param name="range">
        ///            String representation of the version range. The versions in
        ///            the range must contain no whitespace. Other whitespace in the
        ///            range string is ignored. </param>
        /// <exception cref="IllegalArgumentException">
        ///             If {@code range} is improperly formatted. </exception>
        public static VersionRange Parse(string range)
        {
            Assertions.BlankCheck(range, "range");
            int idx = range.IndexOf(ENDPOINT_DELIMITER);
            // Check if the version is an interval.
            if (idx > 1 && idx == range.LastIndexOf(ENDPOINT_DELIMITER))
            {
                string vlo = range.Substring(0, idx).Trim();
                string vhi = range.Substring(idx + 1).Trim();

                bool isLowInclusive = true;
                bool isHighInclusive = true;
                if (vlo[0] == LEFT_OPEN)
                {
                    isLowInclusive = false;
                }
                else if (vlo[0] != LEFT_CLOSED)
                {
                    throw new System.ArgumentException("invalid range \"" + range + "\": invalid format");
                }
                vlo = vlo.Substring(1).Trim();

                if (vhi[vhi.Length - 1] == RIGHT_OPEN)
                {
                    isHighInclusive = false;
                }
                else if (vhi[vhi.Length - 1] != RIGHT_CLOSED)
                {
                    throw new System.ArgumentException("invalid range \"" + range + "\": invalid format");
                }
                vhi = vhi.Substring(0, vhi.Length - 1).Trim();

                return new VersionRange(parseVersion(vlo, range), isLowInclusive, parseVersion(vhi, range), isHighInclusive);
            }
            else if (idx == -1)
            {
                return new VersionRange(VersionRange.parseVersion(range.Trim(), range), true, null, false);
            }
            else
            {
                throw new System.ArgumentException("invalid range \"" + range + "\": invalid format");
            }
        }

        public VersionRange(Version low, bool isLowInclusive, Version high, bool isHighInclusive)
        {
            Assertions.NullCheck(low, "floorVersion");
            floorVersion = low;
            isFloorInclusive = isLowInclusive;
            ceilingVersion = high;
            isCeilingInclusive = isHighInclusive;
            empty = Empty0;
        }

        public  Version Floor
        {
            get
            {
                return floorVersion;
            }
        }

        public  bool FloorInclusive
        {
            get
            {
                return isFloorInclusive;
            }
        }

        public  Version Ceiling
        {
            get
            {
                return ceilingVersion;
            }
        }

        public bool CeilingInclusive
        {
            get
            {
                return isCeilingInclusive;
            }
        }

        public  bool IsInRange(Version version)
        {
            if (empty)
            {
                return false;
            }
            if (floorVersion.CompareTo(version) >= (isFloorInclusive ? 1 : 0))
            {
                return false;
            }
            if (ceilingVersion == null)
            {
                return true;
            }
            return ceilingVersion.CompareTo(version) >= (isCeilingInclusive ? 0 : 1);

        }

        /// <summary>
        /// Returns whether this version range contains only a single version.
        /// </summary>
        /// <returns> {@code true} if this version range contains only a single
        ///         version; {@code false} otherwise. </returns>
        public  bool Exact
        {
            get
            {
                if (empty)
                {
                    return false;
                }
                else if (ceilingVersion == null)
                {
                    return true;
                }
                if (isFloorInclusive)
                {
                    if (isCeilingInclusive)
                    {
                        // [f,c]: exact if f == c
                        return floorVersion.Equals(ceilingVersion);
                    }
                    else
                    {
                        // [f,c): exact if f++ >= c
                        Version adjacent1 = new Version(floorVersion.Major, floorVersion.Minor, floorVersion.Build, floorVersion.Revision + 1);
                        return adjacent1.CompareTo(ceilingVersion) >= 0;
                    }
                }
                else
                {
                    if (isCeilingInclusive)
                    {
                        // (f,c] is equivalent to [f++,c]: exact if f++ == c
                        Version adjacent1 = new Version(floorVersion.Major, floorVersion.Minor, floorVersion.Build, floorVersion.Revision + 1);
                        return adjacent1.Equals(ceilingVersion);
                    }
                    else
                    {
                        // (f,c) is equivalent to [f++,c): exact if (f++)++ >=c
                        Version adjacent2 = new Version(floorVersion.Major, floorVersion.Minor, floorVersion.Build, floorVersion.Revision + 2);
                        return adjacent2.CompareTo(ceilingVersion) >= 0;
                    }
                }
            }
        }

        /// <summary>
        /// Returns whether this version range is empty. A version range is empty if
        /// the set of versions defined by the interval is empty.
        /// </summary>
        /// <returns> {@code true} if this version range is empty; {@code false}
        ///         otherwise. </returns>
        public  bool Empty
        {
            get
            {
                return empty;
            }
        }

        /// <summary>
        /// Internal isEmpty behavior.
        /// </summary>
        /// <returns> {@code true} if this version range is empty; {@code false}
        ///         otherwise. </returns>
        private bool Empty0
        {
            get
            {
                if (ceilingVersion == null) // infinity
                {
                    return false;
                }
                int comparison = floorVersion.CompareTo(ceilingVersion);
                if (comparison == 0) // endpoints equal
                {
                    return !isFloorInclusive || !isCeilingInclusive;
                }
                return comparison > 0; // true if left > right
            }
        }

        public override bool Equals(object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (this.GetType() != obj.GetType())
            {
                return false;
            }
            VersionRange other = (VersionRange)obj;
            if (floorVersion != other.floorVersion && (floorVersion == null || !floorVersion.Equals(other.floorVersion)))
            {
                return false;
            }
            if (isFloorInclusive != other.isFloorInclusive)
            {
                return false;
            }
            if (ceilingVersion != other.ceilingVersion && (ceilingVersion == null || !ceilingVersion.Equals(other.ceilingVersion)))
            {
                return false;
            }
            if (isCeilingInclusive != other.isCeilingInclusive)
            {
                return false;
            }
            return true;
        }

        public override int GetHashCode()
        {
            int result = floorVersion.GetHashCode();
            result = 31 * result + (isFloorInclusive ? 1 : 0);
            result = 31 * result + (ceilingVersion != null ? ceilingVersion.GetHashCode() : 0);
            result = 31 * result + (isCeilingInclusive ? 1 : 0);
            return result;
        }

        public override string ToString()
        {
            if (ceilingVersion != null)
            {
                StringBuilder sb = new StringBuilder();
                sb.Append(isFloorInclusive ? LEFT_CLOSED : LEFT_OPEN);
                sb.Append(floorVersion.ToString()).Append(ENDPOINT_DELIMITER).Append(ceilingVersion.ToString());
                sb.Append(isCeilingInclusive ? RIGHT_CLOSED : RIGHT_OPEN);
                return sb.ToString();
            }
            else
            {
                return floorVersion.ToString();
            }
        }
    }
    #endregion
}