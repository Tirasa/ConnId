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
 */
package org.identityconnectors.framework.impl.api.local;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.identityconnectors.framework.common.exceptions.ConfigurationException;

public class WorkingBundleInfo {

    // The original location for this bundle (for error reporting).
    private String originalLocation;

    // The manifest for this bundle.
    private ConnectorBundleManifest bundleManifest;

    // Immediate contents of the bundle.
    private Set<String> immediateBundleContents = new HashSet<String>();

    // The immediate classpath of the bundle. Normally this only contains the
    // bundle JAR.
    // It does not include the embedded bundles (which are in
    // effectiveClassPath).
    private List<URL> immediateClassPath = new ArrayList<URL>();

    // The immediate native libraries in the bundle.
    // The key is the short library name (passed to System.loadLibrary()), and
    // the value is the library location on the file system.
    private Map<String, String> immediateNativeLibraries = new HashMap<String, String>();

    // List of included bundles.
    private List<WorkingBundleInfo> embeddedBundles = new ArrayList<WorkingBundleInfo>();

    // Effective classpath (includes the classpaths of embedded bundles).
    private List<URL> effectiveClassPath;

    // Effective native libraries (includes the native libraries of embedded
    // bundles).
    private Map<String, String> effectiveNativeLibraries;

    // Effective contents (included the contents of embedded bundles).
    private Set<String> effectiveContents;

    public WorkingBundleInfo(String originalLocation) {
        this.originalLocation = originalLocation;
    }

    public String getOriginalLocation() {
        return originalLocation;
    }

    public ConnectorBundleManifest getManifest() {
        return bundleManifest;
    }

    public void setManifest(ConnectorBundleManifest manifest) {
        bundleManifest = manifest;
    }

    public Set<String> getImmediateBundleContents() {
        return immediateBundleContents;
    }

    public List<URL> getImmediateClassPath() {
        return immediateClassPath;
    }

    public Map<String, String> getImmediateNativeLibraries() {
        return immediateNativeLibraries;
    }

    public List<WorkingBundleInfo> getEmbeddedBundles() {
        return embeddedBundles;
    }

    public List<URL> getEffectiveClassPath() {
        return effectiveClassPath;
    }

    public Map<String, String> getEffectiveNativeLibraries() {
        return effectiveNativeLibraries;
    }

    public Set<String> getEffectiveContents() {
        return effectiveContents;
    }

    /**
     * Populates the effective properties (<code>effectiveClassPath</code>,
     * <code>effectiveContents</code>, etc.) while taking into account any
     * embedded bundles.
     */
    public static void resolve(List<? extends WorkingBundleInfo> infos)
            throws ConfigurationException {
        for (WorkingBundleInfo info : infos) {
            info.effectiveClassPath = null;
            info.effectiveContents = null;
        }
        ensureBundlesAreUnique(infos);
        resolveEffectiveProperties(infos);
    }

    private static final String FORMAT =
            "There is more than one bundle with the same name+version: %s";

    private static void ensureBundlesAreUnique(List<? extends WorkingBundleInfo> working)
            throws ConfigurationException {
        Set<BundleKey> bundleKeys = new HashSet<BundleKey>();
        for (WorkingBundleInfo info : working) {
            BundleKey key =
                    new BundleKey(info.bundleManifest.getBundleName(), info.bundleManifest
                            .getBundleVersion());
            if (bundleKeys.contains(key)) {
                throw new ConfigurationException(String.format(FORMAT, key));
            }
            bundleKeys.add(key);
        }
    }

    /**
     * Recursively populates the effective properties.
     */
    private static void resolveEffectiveProperties(List<? extends WorkingBundleInfo> infos)
            throws ConfigurationException {
        for (WorkingBundleInfo info : infos) {
            List<URL> classPath = new ArrayList<URL>();
            Map<String, String> nativeLibraries = new LinkedHashMap<String, String>();
            Set<String> contents = new HashSet<String>();
            // This bundle's classpath must go first, before the embedded
            // bundles' classpaths.
            classPath.addAll(info.getImmediateClassPath());
            nativeLibraries.putAll(info.getImmediateNativeLibraries());
            contents.addAll(info.getImmediateBundleContents());
            resolveEffectiveProperties(info.getEmbeddedBundles());
            for (WorkingBundleInfo embedded : info.getEmbeddedBundles()) {
                classPath.addAll(embedded.getEffectiveClassPath());
                // Do now allow native libraries from embedded bundles to
                // override this bundle's libraries.
                for (Entry<String, String> entry : embedded.getEffectiveNativeLibraries()
                        .entrySet()) {
                    if (!nativeLibraries.containsKey(entry.getKey())) {
                        nativeLibraries.put(entry.getKey(), entry.getValue());
                    }
                }
                contents.addAll(embedded.getEffectiveContents());
            }
            info.effectiveClassPath = classPath;
            info.effectiveNativeLibraries = nativeLibraries;
            info.effectiveContents = contents;
        }
    }
}
