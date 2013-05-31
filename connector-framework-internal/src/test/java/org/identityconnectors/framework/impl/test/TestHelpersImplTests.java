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
package org.identityconnectors.framework.impl.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Map;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.identityconnectors.framework.spi.Configuration;
import org.testng.annotations.Test;

public class TestHelpersImplTests {

    @Test
    public void testFillConfiguration() {
        TestConfiguration testConfig = new TestConfiguration();
        // There is no "foo" property in the config bean. We want to ensure
        // that fillConfiguration() does not fail for unknown properties.
        Map<String, ? extends Object> configData =
                CollectionUtil.newMap("host", "example.com", "port", 1234, "foo", "bar");
        new TestHelpersImpl().fillConfiguration(testConfig, configData);

        assertEquals("example.com", testConfig.getHost());
        assertEquals(1234, testConfig.getPort());
    }

    public final static class TestConfiguration implements Configuration {

        private String host;
        private int port;

        public ConnectorMessages getConnectorMessages() {
            return null;
        }

        public void setConnectorMessages(ConnectorMessages messages) {
            fail("Should not call setConnectorMessages()");
        }

        public void validate() {
            fail("Should not call validate()");
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
