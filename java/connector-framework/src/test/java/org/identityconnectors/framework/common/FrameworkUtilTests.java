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
package org.identityconnectors.framework.common;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Properties;

import org.identityconnectors.common.Version;

public class FrameworkUtilTests {

    @Test
    public void testFrameworkVersion() throws Exception {
        ClassLoader loader = new VersionClassLoader(this.getClass().getClassLoader(), "1.2.3-alpha");
        assertEquals(FrameworkUtil.getFrameworkVersion(loader), Version.parse("1.2.3"));
    }

    @Test
    public void testFrameworkVersionCannotBeBlank() throws Exception {
        try {
            FrameworkUtil.getFrameworkVersion(new VersionClassLoader(this.getClass().getClassLoader(), " "));
            Assert.fail();
        } catch (IllegalStateException e) {
            // OK.
        }
    }

    private static final class VersionClassLoader extends ClassLoader {

        private final String version;

        public VersionClassLoader(ClassLoader parent, String version) {
            super(parent);
            this.version = version;
        }

        @Override
        public URL getResource(String name) {
            if (!"connectors-framework.properties".equals(name)) {
                return getParent().getResource(name);
            }
            Properties props = new Properties();
            props.put("framework.version", version);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                props.store(output, null);
                return new URL("fakejar", null, 0, "connectors-framework.properties",
                        new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL u) throws IOException {
                                return new URLConnection(u) {
                                    @Override
                                    public void connect() throws IOException {
                                    }

                                    @Override
                                    public InputStream getInputStream() throws IOException {
                                        return new ByteArrayInputStream(output.toByteArray());
                                    }
                                };
                            }
                        });
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }
}
