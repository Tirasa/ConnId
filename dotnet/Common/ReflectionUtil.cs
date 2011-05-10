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
using System.Linq;
namespace Org.IdentityConnectors.Common
{
    /// <summary>
    /// Miscellaenous reflection utilities.
    /// </summary>
    public static class ReflectionUtil
    {
        /// <summary>
        /// Gets all the interfaces in the hierarchy
        /// </summary>
        /// <param name="type"></param>
        /// <returns></returns>
        public static Type[] GetAllInterfaces(Type type)
        {
            HashSet<Type> temp = new HashSet<Type>();
            GetAllInterfaces2(type, temp);
            return temp.ToArray();
        }

        private static void GetAllInterfaces2(Type type, HashSet<Type> rv)
        {
            if (type != null)
            {
                if (type.IsInterface)
                {
                    rv.Add(type);
                }
                GetAllInterfaces2(type.BaseType, rv);
                foreach (Type inter in type.GetInterfaces())
                {
                    GetAllInterfaces2(inter, rv);
                }
            }
        }

        /// <summary>
        /// Returns the generic type definition of the given type
        /// </summary>
        /// <param name="type"></param>
        /// <returns></returns>
        public static Type[] GetTypeErasure(Type[] types)
        {
            Type[] rv = new Type[types.Length];
            for (int i = 0; i < types.Length; i++)
            {
                rv[i] = GetTypeErasure(types[i]);
            }
            return rv;
        }
        /// <summary>
        /// Returns the generic type definition of the given type
        /// </summary>
        /// <param name="type"></param>
        /// <returns></returns>
        public static Type GetTypeErasure(Type type)
        {
            if (type.IsGenericType)
            {
                type = type.GetGenericTypeDefinition();
            }
            return type;
        }

        /// <summary>
        /// Returns true iff t1 is a parent type of t2. Unlike
        /// Type.isAssignableFrom, this handles generic types as
        /// well. 
        /// </summary>
        /// <param name="t1"></param>
        /// <param name="t2"></param>
        /// <returns></returns>
        public static bool IsParentTypeOf(Type t1,
                                          Type t2)
        {
            if (t2 == null)
            {
                return t1 == null;
            }
            Type found = FindInHierarchyOf(t1, t2);
            return found != null;
        }

        /// <summary>
        /// Finds t1 in the hierarchy of t2 or null if not found. The
        /// returned value will have generic parameters applied to it.
        /// </summary>
        /// <param name="t1"></param>
        /// <param name="t2"></param>
        /// <returns></returns>
        public static Type FindInHierarchyOf(Type t1, Type t2)
        {
            if (t2 == null)
            {
                return null;
            }
            if (EqualsIgnoreGeneric(t1, t2))
            {
                return t2;
            }
            Type found1 = FindInHierarchyOf(t1, t2.BaseType);
            if (found1 != null)
            {
                return found1;
            }
            foreach (Type inter in t2.GetInterfaces())
            {
                Type found2 = FindInHierarchyOf(t1, inter);
                if (found2 != null)
                {
                    return found2;
                }
            }
            return null;
        }

        private static bool EqualsIgnoreGeneric(Type t1,
                                            Type t2)
        {
            if (t1 == null || t2 == null)
            {
                return t1 == null && t2 == null;
            }
            if (t1.IsGenericType)
            {
                t1 = t1.GetGenericTypeDefinition();
            }
            if (t2.IsGenericType)
            {
                t2 = t2.GetGenericTypeDefinition();
            }
            return t1.Equals(t2);
        }

        public static Type[] GetParameterTypes(MethodInfo method)
        {
            ParameterInfo[] parameters = method.GetParameters();
            Type[] rv = new Type[parameters.Length];
            for (int i = 0; i < parameters.Length; i++)
            {
                rv[i] = parameters[i].ParameterType;
            }
            return rv;
        }
    }
}