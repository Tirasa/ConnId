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
using System.Collections.Generic;
using Org.IdentityConnectors.Common;
using Org.IdentityConnectors.Common.Security;
using Org.IdentityConnectors.Framework.Spi;
using Org.IdentityConnectors.Framework.Api.Operations;
using Org.IdentityConnectors.Framework.Spi.Operations;
using Org.IdentityConnectors.Framework.Common.Objects;
namespace Org.IdentityConnectors.Framework.Common
{
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
}