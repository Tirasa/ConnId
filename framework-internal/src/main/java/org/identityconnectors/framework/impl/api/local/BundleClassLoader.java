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
package org.identityconnectors.framework.impl.api.local;

import static org.identityconnectors.common.CollectionUtil.newReadOnlyMap;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

class BundleClassLoader extends URLClassLoader {
    
    private static final String FRAMEWORK_PACKAGE = "org.identityconnectors.framework";
    
    // The set of packages a connector is allowed to access from the
    // parent class loader.
    private static final String [] ALLOWED_FRAMEWORK_PACKAGES = {
        FRAMEWORK_PACKAGE+".api",
        FRAMEWORK_PACKAGE+".common",
        FRAMEWORK_PACKAGE+".spi"
    };
    
    private final Map<String, String> nativeLibs;
    
    public BundleClassLoader(List<URL> urls, Map<String, String> nativeLibs, ClassLoader parent) {
        super(urls.toArray(new URL[urls.size()]), parent);
        this.nativeLibs = newReadOnlyMap(nativeLibs);
    }
    
    /**
     * Overrides <code>super.loadClass()</code>, to change loading model to
     * child-first and to restrict access to certain classes.
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                //try to find it in the bundle's class loader
                //anything there is considered accessible
                c = findClass(name);     
            }
            catch (ClassNotFoundException ex) {
                // Hack for OIM: the framework is loaded by OIM's tcADPClassLoader, whose
                // delegation strategy (parent first) is to first try to load
                // the class through Thread.currentThread().getContextClassLoader(). When the
                // framework is running a connector operation, the thread context class loader is
                // BundleClassLoader. Without the hack, BundleClassLoader would delegate to its parent
                // (i.e., tcADPClassLoader), which would again delegate to the thread context class loader
                // (i.e., BundleClassLoader), resulting in an infinite loop reported by the JVM through a ClassCircularityError.
                // The hack sets the thread context class loader to its initial value when
                // BundleClassLoader delegates to its parent.
                if (runningInOIM()) {
                    List<ClassLoader> loaders = ThreadClassLoaderManager.getInstance().popAll();
                    try {
                        // check parents class loader
                        c = getParent().loadClass(name);
                        // We cannot check the allowed packages; because of the OIM bug,
                        // BundleClassLoader may be asked to load framework-internal classes too.
                    } finally {
                        ThreadClassLoaderManager.getInstance().pushAll(loaders);
                    }
                } else {
                    // check parents class loader
                    c = getParent().loadClass(name);
                    //make sure it's only in set of allowed packages
                    checkAccessAllowed(c);
                }
            }
        }
        if (resolve) {
            resolveClass(c);
        }        
        return c;
    }

    private boolean runningInOIM() {
        ClassLoader loader = this.getClass().getClassLoader();
        return loader != null && loader.getClass().getName().contains("tcADPClassLoader");
    }

    private void checkAccessAllowed(Class<?> c) throws ClassNotFoundException {
        String name = c.getName();
        if ( !name.startsWith(FRAMEWORK_PACKAGE+".") ) {
            return;
        }
        for (String pack : ALLOWED_FRAMEWORK_PACKAGES) {
            if ( name.startsWith(pack+".") ) {
                return;
            }
        }
        String message =
            "Connector may not reference class '"+name+"', "+
            "it is an internal framework class.";
        throw new ClassNotFoundException(message);
    }

    @Override
    protected String findLibrary(String libname) {
        return nativeLibs.get(System.mapLibraryName(libname));
    }
    
}
