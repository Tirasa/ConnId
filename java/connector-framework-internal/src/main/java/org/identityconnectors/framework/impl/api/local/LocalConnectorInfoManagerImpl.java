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
 * Portions Copyrighted 2010-2014 ForgeRock AS.
 * Portions Copyrighted 2010 ConnId
 */
package org.identityconnectors.framework.impl.api.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.ReflectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConnectorMessagesImpl;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;

public class LocalConnectorInfoManagerImpl implements ConnectorInfoManager {

    private static final Log LOG = Log.getLog(LocalConnectorInfoManagerImpl.class);

    private final List<ConnectorInfo> connectorInfos;

    public LocalConnectorInfoManagerImpl(final List<URL> bundleURLs,
            final ClassLoader bundleParentClassLoader) throws ConfigurationException {
        final List<WorkingBundleInfo> workingInfo = expandBundles(bundleURLs);
        WorkingBundleInfo.resolve(workingInfo);
        connectorInfos = createConnectorInfo(workingInfo, bundleParentClassLoader);
    }

    /**
     * First pass - expand bundles as needed. populates originalURL,
     * parsedManifest, libContents, and topLevelContents
     */
    private static List<WorkingBundleInfo> expandBundles(final List<URL> bundleURLs)
            throws ConfigurationException {

        final List<WorkingBundleInfo> rv = new ArrayList<>();
        bundleURLs.forEach(url -> {
            WorkingBundleInfo info = null;
            try {
                if ("file".equals(url.getProtocol())) {
                    Path file = Path.of(url.toURI());
                    if (Files.isDirectory(file)) {
                        info = processDirectory(file);
                    }
                }
                if (info == null) {
                    info = processURL(url, true);
                }
            } catch (URISyntaxException e) {
                throw new ConfigurationException("Invalid bundleURL: " + url.toExternalForm(), e);
            }
            rv.add(info);
        });
        return rv;
    }

    private static WorkingBundleInfo processDirectory(final Path dir) throws ConfigurationException {
        final WorkingBundleInfo info = new WorkingBundleInfo(dir.toAbsolutePath().toString());
        try {
            // easy case - nothing needs to be copied
            Path manifest = dir.resolve("META-INF/MANIFEST.MF");
            try (InputStream in = Files.newInputStream(manifest)) {
                Manifest rawManifest = new Manifest(in);
                ConnectorBundleManifestParser parser =
                        new ConnectorBundleManifestParser(info.getOriginalLocation(), rawManifest);
                info.setManifest(parser.parse());
            }
            info.getImmediateClassPath().add(dir.toUri().toURL());
            info.getImmediateBundleContents().addAll(listBundleContents(dir.toFile()));
            final File libDir = dir.resolve("lib").toFile();
            if (libDir.exists()) {
                final List<URL> libURLs = BundleLibSorter.getSortedURLs(libDir);
                libURLs.forEach((lib) -> {
                    info.getEmbeddedBundles().add(processURL(lib, false));
                });
            }
            final File nativeDir = dir.resolve("native").toFile();
            if (nativeDir.exists()) {
                for (File file : BundleLibSorter.getSortedFiles(nativeDir)) {
                    if (file.isFile()) {
                        info.getImmediateNativeLibraries().put(file.getName(), file.getAbsolutePath());
                    }
                }
            }
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
        return info;
    }

    /**
     * Lists the contents of a directory and its contents. Result will be given
     * as a list of forward-slash separated relative paths
     */
    private static List<String> listBundleContents(final File dir) {
        final List<String> rv = new ArrayList<>();
        for (File file : dir.listFiles()) {
            listBundleContents2("", file, rv);
        }
        return rv;
    }

    private static void listBundleContents2(final String prefix, final File file, final List<String> result) {
        String path = prefix + file.getName();
        result.add(path);
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                listBundleContents2(path + "/", sub, result);
            }
        }
    }

    private static WorkingBundleInfo processURL(final URL url, final boolean topLevel)
            throws ConfigurationException {

        final WorkingBundleInfo info = new WorkingBundleInfo(url.toString());
        final BundleTempDirectory tempDir = new BundleTempDirectory();

        try {
            JarInputStream stream = null;
            if ("file".equals(url.getProtocol())) {
                info.getImmediateClassPath().add(url);
            } else {
                // if we're in a WAR, this might not be the kind of URL
                // that URLClassLoader can handle, so copy it as well
                InputStream stream2 = null;
                try {
                    stream2 = url.openStream();
                    info.getImmediateClassPath().add(
                            tempDir.copyStreamToFile(stream2).toURI().toURL());
                } finally {
                    IOUtil.quietClose(stream2);
                }
            }
            final TreeMap<String, URL> libURLs = new TreeMap<>();
            try {
                stream = new JarInputStream(url.openStream());
                // only parse the manifest for top-level bundles
                // other bundles may not be bundles - they might be jars instead
                if (topLevel) {
                    final Manifest rawManifest = stream.getManifest();
                    final ConnectorBundleManifestParser parser =
                            new ConnectorBundleManifestParser(info.getOriginalLocation(), rawManifest);
                    info.setManifest(parser.parse());
                }

                JarEntry entry;
                while ((entry = stream.getNextJarEntry()) != null) {
                    final String name = entry.getName();
                    info.getImmediateBundleContents().add(name);
                    if (name.startsWith("lib/") && !entry.isDirectory()) {
                        final String localName = name.substring("lib/".length());
                        final URL tempurl = tempDir.copyStreamToFile(stream, name).toURI().toURL();
                        libURLs.put(localName, tempurl);
                    }
                    if (name.startsWith("native/") && !entry.isDirectory()) {
                        final String localName = name.substring("native/".length());
                        // It is important that the name of the native library
                        // be preserved!
                        final File tempFile = tempDir.copyStreamToFile(stream, name);
                        info.getImmediateNativeLibraries().put(localName,
                                tempFile.getAbsolutePath());
                    }
                }
            } finally {
                IOUtil.quietClose(stream);
            }
            libURLs.values().forEach((lib) -> {
                info.getEmbeddedBundles().add(processURL(lib, false));
            });
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
        return info;
    }

    /**
     * Final pass - create connector infos
     */
    private static List<ConnectorInfo> createConnectorInfo(
            final Collection<WorkingBundleInfo> parsed, final ClassLoader bundleParentClassLoader)
            throws ConfigurationException {

        final List<ConnectorInfo> rv = new ArrayList<>();
        parsed.forEach((bundleInfo) -> {
            final ClassLoader loader = new BundleClassLoader(
                    bundleInfo.getEffectiveClassPath(),
                    bundleInfo.getEffectiveNativeLibraries(),
                    bundleParentClassLoader);
            bundleInfo.getImmediateBundleContents().forEach(name -> {
                Class<?> connectorClass = null;
                ConnectorClass options = null;
                if (name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - ".class".length());
                    className = className.replace('/', '.');
                    try {
                        connectorClass = loader.loadClass(className);
                        options = connectorClass.getAnnotation(ConnectorClass.class);
                    } catch (Throwable e) {
                        // probe for the class. this might not be an error since
                        // it might be from a bundle
                        // fragment ( a bundle only included by other bundles ).
                        // However, we should definitely warn
                        LOG.info(LOG.isOk() ? e : null,
                                "Unable to load class {0} from bundle {1}. Class will be ignored and will not be "
                                + "listed in list of connectors.",
                                className, bundleInfo.getOriginalLocation());
                    }
                    if (connectorClass != null && options == null) {
                        for (Annotation annotation : connectorClass.getAnnotations()) {
                            if (ConnectorClass.class.getName().equals(annotation.annotationType().getName())) {
                                // Same class name as the annotation we are looking for. But the previous code haven't 
                                // found it.
                                // So it looks like the annotation on this class is actually the correct one but it is
                                // loaded by wrong classloader.
                                // Note: This error is very difficult to diagnose. Therefore we are explicitly checking 
                                // for it here.
                                throw new ConfigurationException("Class " + connectorClass.getName()
                                        + " has ConnectorClass annotation but it looks like it is "
                                        + "loaded by a wrong classloader. Maybe the connector bundle contains the "
                                        + "connector frameworks JAR? (it should NOT contain it).");
                            }
                        }
                    }
                }
                if (connectorClass != null && options != null) {
                    if (!Connector.class.isAssignableFrom(connectorClass)) {
                        throw new ConfigurationException("Class " + connectorClass
                                + " does not implement " + Connector.class.getName());
                    }
                    final LocalConnectorInfoImpl info = new LocalConnectorInfoImpl();
                    info.setConnectorClass(connectorClass.asSubclass(Connector.class));
                    try {
                        info.setConnectorConfigurationClass(options.configurationClass());
                        info.setConnectorDisplayNameKey(options.displayNameKey());
                        info.setConnectorCategoryKey(options.categoryKey());
                        info.setConnectorKey(new ConnectorKey(bundleInfo.getManifest().getBundleName(),
                                bundleInfo.getManifest().getBundleVersion(), connectorClass.getName()));
                        final ConnectorMessagesImpl messages =
                                loadMessageCatalog(bundleInfo.getEffectiveContents(), loader, info
                                        .getConnectorClass());
                        info.setMessages(messages);
                        info.setDefaultAPIConfiguration(createDefaultAPIConfiguration(info));
                        rv.add(info);
                        LOG.info("Add ConnectorInfo {0} to Local Connector Info Manager from {1}",
                                info.getConnectorKey(), bundleInfo.getOriginalLocation());
                    } catch (final NoClassDefFoundError e) {
                        LOG.info(LOG.isOk() ? e : null,
                                "Unable to load configuration class of connector {0} from bundle {1}. "
                                + "Class will be ignored and will not be listed in list of connectors.",
                                connectorClass, bundleInfo.getOriginalLocation());
                    } catch (final TypeNotPresentException e) {
                        LOG.info(LOG.isOk() ? e : null,
                                "Unable to load configuration class of connector {0} from bundle {1}. "
                                + "Class will be ignored and will not be listed in list of connectors.",
                                connectorClass, bundleInfo.getOriginalLocation());
                    }
                }
            });
        });
        return rv;
    }

    /**
     * Create an instance of the {@link APIConfiguration} object to setup the
     * framework etc..
     */
    public static APIConfigurationImpl createDefaultAPIConfiguration(final LocalConnectorInfoImpl localInfo) {
        // setup classloader since we are going to construct the config bean
        ThreadClassLoaderManager.getInstance().pushClassLoader(localInfo.getConnectorClass().getClassLoader());
        try {
            final Class<? extends Connector> connectorClass = localInfo.getConnectorClass();
            final APIConfigurationImpl rv = new APIConfigurationImpl();
            final Configuration config =
                    localInfo.getConnectorConfigurationClass().getDeclaredConstructor().newInstance();
            final boolean pooling = PoolableConnector.class.isAssignableFrom(connectorClass);
            rv.setConnectorPoolingSupported(pooling);
            rv.setConfigurationProperties(JavaClassProperties.createConfigurationProperties(config));
            rv.setConnectorInfo(localInfo);
            rv.setSupportedOperations(FrameworkUtil.getDefaultSupportedOperations(connectorClass));
            return rv;
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        } finally {
            ThreadClassLoaderManager.getInstance().popClassLoader();
        }
    }

    public static ConnectorMessagesImpl loadMessageCatalog(final Set<String> bundleContents,
            final ClassLoader loader, final Class<? extends Connector> connector)
            throws ConfigurationException {
        try {
            final String[] prefixes = getBundleNamePrefixes(connector);
            final String suffix = ".properties";
            final ConnectorMessagesImpl rv = new ConnectorMessagesImpl();
            // iterate last to first so that first one wins
            for (int i = prefixes.length - 1; i >= 0; i--) {
                String prefix = prefixes[i];
                for (String path : bundleContents) {
                    if (path.startsWith(prefix)) {
                        String localeStr = path.substring(prefix.length());
                        if (localeStr.endsWith(suffix)) {
                            localeStr =
                                    localeStr.substring(0, localeStr.length() - suffix.length());
                            final Locale locale = parseLocale(localeStr);
                            Properties properties = IOUtil.getResourceAsProperties(loader, path);
                            // get or create map
                            Map<String, String> map = rv.getCatalogs().get(locale);
                            if (map == null) {
                                map = new HashMap<>();
                                rv.getCatalogs().put(locale, map);
                            }
                            // merge properties into map, overwriting
                            // any that already exist
                            map.putAll(CollectionUtil.newMap(properties));
                        }
                    }
                }
            }
            return rv;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private static Locale parseLocale(final String str) {
        String lang = null;
        String country = null;
        String variant = null;
        final StringTokenizer tok = new StringTokenizer(str, "_", false);
        if (tok.hasMoreTokens()) {
            lang = tok.nextToken();
        }
        if (tok.hasMoreTokens()) {
            country = tok.nextToken();
        }
        if (tok.hasMoreTokens()) {
            variant = tok.nextToken();
        }
        if (variant != null) {
            return new Locale(lang, country, variant);
        } else if (country != null) {
            return new Locale(lang, country);
        } else if (lang != null) {
            return new Locale(lang);
        } else {
            return new Locale("");
        }
    }

    private static String[] getBundleNamePrefixes(final Class<? extends Connector> connector) {
        // figure out the message catalog..
        final ConnectorClass configOpts = connector.getAnnotation(ConnectorClass.class);
        String[] paths = null;
        if (configOpts != null) {
            paths = configOpts.messageCatalogPaths();
        }
        if (paths == null || paths.length == 0) {
            final String pkage = ReflectionUtil.getPackage(connector);
            final String messageCatalog = pkage + ".Messages";
            paths = new String[] { messageCatalog };
        }
        for (int i = 0; i < paths.length; i++) {
            paths[i] = paths[i].replace('.', '/');
        }
        return paths;
    }

    @Override
    public ConnectorInfo findConnectorInfo(final ConnectorKey key) {
        for (ConnectorInfo info : connectorInfos) {
            if (info.getConnectorKey().equals(key)) {
                return info;
            }
        }
        return null;
    }

    @Override
    public List<ConnectorInfo> getConnectorInfos() {
        return Collections.unmodifiableList(connectorInfos);
    }

    private static final class BundleTempDirectory {

        private final Random _random = new Random(System.currentTimeMillis());

        private File _bundleTempDir;

        public File copyStreamToFile(final InputStream stream) throws IOException {
            final File bundleDir = getBundleTempDir();
            File candidate;
            do {
                candidate = bundleDir.toPath().resolve("file-" + nextRandom()).toFile();
            } while (!candidate.createNewFile());
            candidate.deleteOnExit();
            copyStream(stream, candidate.toPath());
            return candidate;
        }

        public File copyStreamToFile(final InputStream stream, final String name) throws IOException {
            final File bundleDir = getBundleTempDir();
            final File newFile = bundleDir.toPath().resolve(name).toFile();
            if (newFile.exists()) {
                throw new IOException("File " + newFile + " already exists");
            }
            File parent = newFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create directory " + parent);
            }
            while (parent != null && !parent.equals(bundleDir)) {
                parent.deleteOnExit();
                parent = parent.getParentFile();
            }
            newFile.deleteOnExit();
            copyStream(stream, newFile.toPath());
            return newFile;
        }

        private static void copyStream(final InputStream stream, final Path toFile) throws IOException {
            try (OutputStream out = Files.newOutputStream(toFile)) {
                IOUtil.copyFile(stream, out);
            }
        }

        private File getBundleTempDir() throws IOException {
            if (_bundleTempDir != null) {
                return _bundleTempDir;
            }
            final File tempDir = Path.of(System.getProperty("java.io.tmpdir")).toFile();
            if (!tempDir.exists()) {
                throw new IOException("Temporary directory " + tempDir + " does not exist");
            }
            File candidate;
            do {
                candidate = tempDir.toPath().resolve("bundle-" + nextRandom()).toFile();
            } while (!candidate.mkdir());
            candidate.deleteOnExit();
            _bundleTempDir = candidate;
            return candidate;
        }

        private int nextRandom() {
            return _random.nextInt() & 0x7fffffff; // Want only positive numbers.
        }
    }
}
