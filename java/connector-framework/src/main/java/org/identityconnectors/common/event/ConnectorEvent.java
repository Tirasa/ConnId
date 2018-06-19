/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 ForgeRock AS. All rights reserved.
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
package org.identityconnectors.common.event;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.ConnectorKey;

/**
 * NOTICE: This package is an early specification of the Events API for 1.2.x.x
 * version. Use carefully, this package may change before the final 1.2.0.0
 * release.
 *
 * @author Laszlo Hordos
 * @since 1.4
 */
public class ConnectorEvent extends EventObject {

    private static final long serialVersionUID = 585375138003446584L;

    /*
     * A valid iPOJO token is. token ::= ( alphanum | "_" | "-" )+
     */
    public static final String CONNECTOR_REGISTERED = "CONNID_CONNECTOREVENT-REGISTERED";

    public static final String CONNECTOR_UNREGISTERING = "CONNID_CONNECTOREVENT-UNREGISTERING";

    public static final String BUNDLE_SYMBOLICNAME = "bundle.symbolicName";

    public static final String BUNDLE_ID = "bundle.id";

    public static final String BUNDLE = "bundle";

    public static final String BUNDLE_VERSION = "bundle.version";

    public static final String CONNECTOR_BUNDLE_NAME = "connector.bundleName";

    public static final String CONNECTOR_VERSION = "connector.version";

    public static final String CONNECTOR_NAME = "connector.name";

    private final String topic;

    private final Map<String, Object> properties;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ConnectorEvent(String topic, ConnectorKey source) {
        super(source);
        this.topic = topic;
        properties = new HashMap<>(2);
        properties.put(ConnectorEvent.CONNECTOR_BUNDLE_NAME, source.getBundleName());
        properties.put(ConnectorEvent.CONNECTOR_VERSION, source.getBundleVersion());
        properties.put(ConnectorEvent.CONNECTOR_NAME, source.getConnectorName());
    }

    /**
     * Copy Constructor.
     *
     * @param source
     */
    public ConnectorEvent(ConnectorEvent source) {
        super(source.getSource());
        this.topic = source.getTopic();
        properties = CollectionUtil.asReadOnlyMap(source.getProperties());
    }

    public String getTopic() {
        return topic;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
