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
package org.identityconnectors.test.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.junit.jupiter.api.Test;

public class TestHelpersTest {

    @Test
    public void testLoadGroovyConfigFileIssue393() {
        Map<?, ?> props =
                TestHelpers.loadGroovyConfigFile(TestHelpersTest.class
                        .getResource("properties.groovy"), null);
        assertEquals(props.get("prop.integerclass"), Integer.class);
    }

    @Test
    public void testLoadConnectorProperties() {
        String oldTestConfig = System.getProperty("testConfig");
        System.setProperty("testConfig", "myconfig");
        try {
            PropertyBag properties1 =
                    TestHelpers.getProperties(DummyConnector.class, null, new ConfigClassLoader());
            checkProperties(properties1);
            PropertyBag properties2 =
                    TestHelpers.getProperties(DummyConnector.class, null, new ConfigClassLoader());
            assertSame(properties1, properties2,
                    "TestHepers must create same PropertyBag for same connector");
        } finally {
            if (oldTestConfig == null) {
                System.getProperties().remove("testConfig");
            } else {
                System.setProperty("testConfig", oldTestConfig);
            }
        }
    }

    void checkProperties(PropertyBag bag) {
        Properties valid = new Properties();
        valid.setProperty("publickey", "value");
        valid.setProperty("myconfig.publickey", "value");
        valid.setProperty("privatekey", "value");
        valid.setProperty("myconfig.privatekey", "value");
        valid.setProperty("override1", "bar1");
        valid.setProperty("override2", "bar2");
        valid.setProperty("override3", "bar3");
        assertEquals(bag.toMap(), valid, "Expected test properties not equal");
    }

    static class DummyConnector implements Connector {

        @Override
        public void dispose() {
        }

        @Override
        public Configuration getConfiguration() {
            return null;
        }

        @Override
        public void init(Configuration cfg) {
        }
    }

    static class ConfigClassLoader extends ClassLoader {

        @Override
        public URL getResource(String name) {
            String prefix = DummyConnector.class.getName();
            if ((prefix + "/config/config.groovy").equals(name)) {
                Properties properties = new Properties();
                properties.setProperty("publickey", "\"value\"");
                properties.setProperty("override1", "\"foo1\"");
                properties.setProperty("override2", "\"foo2\"");
                properties.setProperty("override3", "\"foo3\"");
                return map2URL(properties);
            } else if ((prefix + "/config/myconfig/config.groovy").equals(name)) {
                Properties properties = new Properties();
                properties.setProperty("myconfig.publickey", "\"value\"");
                properties.setProperty("override1", "\"bar1\"");
                return map2URL(properties);
            } else if ((prefix + "/config-private/config.groovy").equals(name)) {
                Properties properties = new Properties();
                properties.setProperty("privatekey", "\"value\"");
                properties.setProperty("override2", "\"bar2\"");
                return map2URL(properties);
            } else if ((prefix + "/config-private/myconfig/config.groovy").equals(name)) {
                Properties properties = new Properties();
                properties.setProperty("myconfig.privatekey", "\"value\"");
                properties.setProperty("override3", "\"bar3\"");
                return map2URL(properties);
            }
            return null;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return new SingleURLEnumeration(null);
        }
    }

    private static class SingleURLEnumeration implements Enumeration<URL> {

        private URL url;

        private SingleURLEnumeration(URL url) {
            super();
            this.url = url;
        }

        @Override
        public boolean hasMoreElements() {
            return url != null;
        }

        @Override
        public URL nextElement() {
            if (url == null) {
                throw new NoSuchElementException();
            }
            URL ret = url;
            url = null;
            return ret;
        }
    }

    private static URL map2URL(final Properties properties) {
        URLStreamHandler handler = new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return new URLConnection(u) {

                    @Override
                    public void connect() throws IOException {
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        // Here we store properties to stream, we cannot use
                        // Properties.store(), because
                        // this method adds comment # character which would not
                        // be parsable by Groovy
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try (BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(baos))) {
                            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                                bfw.append(entry.getKey().toString());
                                bfw.append("=");
                                bfw.append(entry.getValue().toString());
                                bfw.newLine();
                            }
                        }
                        return new ByteArrayInputStream(baos.toByteArray());
                    }
                };
            }

        };
        try {
            return new URL(null, "file:///map", handler);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid url", e);
        }
    }
}
