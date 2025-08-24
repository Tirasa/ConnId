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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.common.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.spi.operations.ScriptOnConnectorOp;
import org.identityconnectors.framework.spi.operations.ScriptOnResourceOp;

/**
 * Abstraction for finding script executors to allow us to invoke scripts from
 * java.
 * <p>
 * <b>NOTE: </b> This is intended purely for executing java-embedable scripting
 * languages. That is, the execution model is assumed to be same-JVM, so scripts
 * can have side effects on objects passed to the script. This is used
 * internally by the connector framework in its implementation of
 * {@link ScriptOnConnectorApiOp} and should be used by connectors should they
 * need to have a custom implementation of {@link ScriptOnConnectorOp}. This is
 * <b>not</b> intended for use by connectors that implement
 * {@link ScriptOnResourceOp}.
 */
public abstract class ScriptExecutorFactory {

    private static Map<String, Class<?>> FACTORY_CACHE;

    private static synchronized Map<String, Class<?>> getFactoryCache() {
        if (FACTORY_CACHE == null) {
            FACTORY_CACHE = CollectionUtil.newCaseInsensitiveMap();
            List<String> factories = getRegisteredFactories();
            factories.forEach(factory -> {
                try {
                    Class<?> clazz = Class.forName(factory);
                    // Create an instance in order to get the supported language.
                    ScriptExecutorFactory instance =
                            (ScriptExecutorFactory) clazz.getDeclaredConstructor().newInstance();
                    String language = instance.getLanguageName();
                    // Do not override a factory earlier in the classpath.
                    if (!FACTORY_CACHE.containsKey(language)) {
                        FACTORY_CACHE.put(language, clazz);
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    // Probably an error loading or instantiating the SEF implementation.
                    // Do not report.
                }
            });
        }
        return FACTORY_CACHE;
    }

    /**
     * Returns the factories registered through META-INF/services.
     *
     * @return a non-null list of factory class names.
     */
    private static List<String> getRegisteredFactories() {
        // Would be nice to move this method to IOUtil when external registrations for another SPI
        // are supported. Currently it would have two clients (ScriptExecutorFactory and Log).
        // Better to have three before it is turned into an API.
        List<String> result = new ArrayList<>();
        String path = "META-INF/services/" + ScriptExecutorFactory.class.getName();
        try {
            Enumeration<URL> configFiles = ScriptExecutorFactory.class.getClassLoader().getResources(path);
            while (configFiles.hasMoreElements()) {
                URL configFile = configFiles.nextElement();
                addFactories(configFile, result);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addFactories(final URL configFile, final List<String> result) throws IOException {
        // Encoding as per JAR file spec.
        try (InputStream input = configFile.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {

            reader.lines().forEach(l -> {
                String line = l;

                int comment = line.indexOf('#');
                if (comment >= 0) {
                    line = line.substring(0, comment);
                }
                line = line.trim();
                if (StringUtil.isNotBlank(line)) {
                    result.add(line);
                }
            });
        }
    }

    /**
     * Returns the set of supported languages.
     *
     * @return The set of supported languages.
     */
    public static Set<String> getSupportedLanguages() {
        return Collections.unmodifiableSet(getFactoryCache().keySet());
    }

    /**
     * Creates a ScriptExecutorFactory for the given language.
     *
     * @param language The name of the language
     * @return The script executor factory
     * @throws IllegalArgumentException If the given language is not supported.
     */
    public static ScriptExecutorFactory newInstance(final String language) {
        if (StringUtil.isBlank(language)) {
            throw new IllegalArgumentException("Language must be specified");
        }
        Class<?> clazz = getFactoryCache().get(language);
        if (clazz == null) {
            throw new IllegalArgumentException(String.format("Language not supported: %s", language));
        }
        // exceptions here should not happened because of the register
        try {
            return (ScriptExecutorFactory) clazz.getDeclaredConstructor().newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a script executor for the given script.
     *
     * @param loader The classloader that contains the java classes that the script should have access to.
     * @param script The script text.
     * @param compile A hint to tell the script executor whether or not to compile the given script.
     * This need not be implemented by all script executors. If true, the caller is saying that they intend to
     * call the script multiple times with different arguments, so compile if possible.
     * @return A script executor.
     */
    public abstract ScriptExecutor newScriptExecutor(ClassLoader loader, String script, boolean compile);

    /**
     * Returns the name of the language supported by this factory.
     *
     * @return the name of the language.
     */
    public abstract String getLanguageName();
}
