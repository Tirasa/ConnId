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
using System.Reflection.Emit;
using System.Collections.Generic;
namespace Org.IdentityConnectors.Common.Proxy
{
    /// <summary>
    /// Similar to java.lang.reflect.InvocationHandler
    /// </summary>
    public interface InvocationHandler
    {
        Object
        Invoke(Object proxy, MethodInfo method, Object[] args);
    }


    /// <summary>
    /// Similar to java.lang.reflect.Proxy
    /// </summary>
    public static class Proxy
    {
        private static readonly MethodInfo INVOCATION_HANDLER_METHOD =
            typeof(InvocationHandler).GetMethod("Invoke",
                        new Type[]{typeof(object),
                                   typeof(MethodInfo),
                                   typeof(object[])});
        private static readonly MethodInfo GET_METHOD_FROM_HANDLE_METHOD =
            typeof(MethodBase).GetMethod("GetMethodFromHandle",
                                         new Type[] { typeof(RuntimeMethodHandle) });

        private static readonly object LOCK = new Object();

        private static IDictionary<Type, ConstructorInfo>
            _implementationMap = new Dictionary<Type, ConstructorInfo>();

        private static int _count = 0;

        public static Object NewProxyInstance(Type interfaze,
                                              InvocationHandler handler)
        {
            ConstructorInfo constructor = GetOrCreateConstructorInfo(interfaze);
            return constructor.Invoke(new object[] { handler });
        }

        private static ConstructorInfo GetOrCreateConstructorInfo(Type type)
        {
            lock (LOCK)
            {
                ConstructorInfo rv =
                    CollectionUtil.GetValue(_implementationMap, type, null);
                if (rv == null)
                {
                    Type impl = GenerateImplementation(type);
                    rv =
                        impl.GetConstructor(new Type[] { typeof(InvocationHandler) });

                    _implementationMap[type] = rv;
                }
                return rv;
            }
        }

        private static String NextName()
        {
            int count;
            lock (LOCK)
            {
                count = _count;
                count++;
            }
            return "___proxy" + count;
        }



        private static Type GenerateImplementation(Type interfaze)
        {
            if (!interfaze.IsInterface)
            {
                throw new ArgumentException("Type is not an interface: " + interfaze);
            }
            if (interfaze.IsGenericType)
            {
                throw new ArgumentException("Type is a generic type: " + interfaze);
            }

            String uniqueName = NextName();

            AssemblyName assemblyName = new AssemblyName(uniqueName);
            AssemblyBuilder assemblyBuilder =
                AppDomain.CurrentDomain.DefineDynamicAssembly(
                    assemblyName,
                    AssemblyBuilderAccess.RunAndSave);
            // For a single-module assembly, the module name is usually
            // the assembly name plus an extension.
            ModuleBuilder moduleBuilder =
                assemblyBuilder.DefineDynamicModule(assemblyName.Name, assemblyName.Name + ".dll");

            TypeBuilder typeBuilder = moduleBuilder.DefineType(
                uniqueName,
                 TypeAttributes.Public);
            typeBuilder.AddInterfaceImplementation(interfaze);

            MethodInfo[] methods = interfaze.GetMethods();
            ConstructorBuilder classInitializer =
                typeBuilder.DefineTypeInitializer();
            ILGenerator classInitializerCode =
                classInitializer.GetILGenerator();
            //generate constructor and _handler field
            FieldBuilder handlerField =
                typeBuilder.DefineField("_handler",
                            typeof(InvocationHandler),
                            FieldAttributes.Private | FieldAttributes.InitOnly);
            int count = 0;
            foreach (MethodInfo method in methods)
            {
                GenerateMethod(typeBuilder, method, classInitializerCode, count,
                              handlerField);
                count++;
            }
            classInitializerCode.Emit(OpCodes.Ret);


            ConstructorBuilder constructorBuilder = typeBuilder.DefineConstructor(
            MethodAttributes.Public,
            CallingConventions.Standard,
            new Type[] { typeof(InvocationHandler) });
            ILGenerator constructorCode = constructorBuilder.GetILGenerator();
            // For a constructor, argument zero is a reference to the new
            // instance. Push it on the stack before calling the base
            // class constructor. Specify the default constructor of the 
            // base class (System.Object) by passing an empty array of 
            // types (Type.EmptyTypes) to GetConstructor.
            constructorCode.Emit(OpCodes.Ldarg_0);
            constructorCode.Emit(OpCodes.Call,
                typeof(object).GetConstructor(Type.EmptyTypes));
            constructorCode.Emit(OpCodes.Ldarg_0);
            constructorCode.Emit(OpCodes.Ldarg_1);
            constructorCode.Emit(OpCodes.Stfld, handlerField);
            constructorCode.Emit(OpCodes.Ret);


            Type t = typeBuilder.CreateType();

            //assemblyBuilder.Save(assemblyName.Name+".dll");

            return t;
        }

        private static void ValidateMethod(MethodInfo info)
        {
            if (info.GetGenericArguments().Length != 0)
            {
                throw new ArgumentException(
                    "Method not supported since it has generic arguments: " + info);

            }
            foreach (ParameterInfo parameter in info.GetParameters())
            {
                if (parameter.IsOut)
                {
                    throw new ArgumentException(
                        "Method not supported since it has output paramaters: " + info);
                }
                if (parameter.IsRetval)
                {
                    throw new ArgumentException(
                        "Method not supported since it has retval paramaters: " + info);
                }
            }
        }

        private static void GenerateMethod(TypeBuilder typeBuilder,
                                           MethodInfo method,
                                           ILGenerator classInitializerCode,
                                           int methodCount,
                                           FieldBuilder handlerField)
        {
            ValidateMethod(method);

            FieldBuilder methodField =
                typeBuilder.DefineField("METHOD" + methodCount,
                                        typeof(MethodInfo),
                                        FieldAttributes.Private | FieldAttributes.InitOnly | FieldAttributes.Static);



            classInitializerCode.Emit(OpCodes.Ldtoken,
                                      method);
            classInitializerCode.Emit(OpCodes.Call,
                                      GET_METHOD_FROM_HANDLE_METHOD);
            classInitializerCode.Emit(OpCodes.Castclass, typeof(MethodInfo));
            classInitializerCode.Emit(OpCodes.Stsfld, methodField);


            Type[] parameterTypes = ReflectionUtil.GetParameterTypes(method);
            MethodBuilder methodBuilder = typeBuilder.DefineMethod(
                method.Name,
                MethodAttributes.Public |
                MethodAttributes.HideBySig |
                MethodAttributes.NewSlot |
                MethodAttributes.Virtual |
                MethodAttributes.Final,
                method.CallingConvention,
                method.ReturnType,
                parameterTypes);

            ILGenerator methodCode = methodBuilder.GetILGenerator();
            methodCode.Emit(OpCodes.Ldarg_0);
            methodCode.Emit(OpCodes.Ldfld, handlerField);
            methodCode.Emit(OpCodes.Ldarg_0);
            methodCode.Emit(OpCodes.Ldsfld, methodField);
            methodCode.Emit(OpCodes.Ldc_I4, parameterTypes.Length);
            methodCode.Emit(OpCodes.Newarr, typeof(object));

            for (int index = 0; index < parameterTypes.Length; index++)
            {
                Type parameterType = parameterTypes[index];
                methodCode.Emit(OpCodes.Dup);
                methodCode.Emit(OpCodes.Ldc_I4, index);
                methodCode.Emit(OpCodes.Ldarg, index + 1);
                if (parameterType.IsValueType)
                {
                    methodCode.Emit(OpCodes.Box, parameterType);
                }
                methodCode.Emit(OpCodes.Stelem_Ref);
            }

            methodCode.Emit(OpCodes.Callvirt, INVOCATION_HANDLER_METHOD);
            Type returnType = method.ReturnType;

            if (typeof(void).Equals(returnType))
            {
                methodCode.Emit(OpCodes.Pop);
            }
            else if (returnType.IsValueType)
            {
                methodCode.Emit(OpCodes.Unbox_Any, returnType);
            }
            else
            {
                methodCode.Emit(OpCodes.Castclass, returnType);
            }

            methodCode.Emit(OpCodes.Ret);

            typeBuilder.DefineMethodOverride(methodBuilder, method);
        }
    }
}