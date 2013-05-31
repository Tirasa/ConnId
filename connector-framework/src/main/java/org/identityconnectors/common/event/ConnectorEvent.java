/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * <p/>
 *
 * @author Laszlo Hordos
 * @since 1.2
 */
public class ConnectorEvent extends EventObject {

    private static final long serialVersionUID = 0L;

    /*
     * A valid iPOJO token is. token ::= ( alphanum | "_" | "-" )+
     */
    public static final String CONNECTOR_REGISTERED =
            "ORG_FORGEROCK_OPENICF_CONNECTOREVENT-REGISTERED";
    public static final String CONNECTOR_UNREGISTERING =
            "ORG_FORGEROCK_OPENICF_CONNECTOREVENT-UNREGISTERING";

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
     * @param source
     *            The object on which the Event initially occurred.
     * @throws IllegalArgumentException
     *             if source is null.
     */
    public ConnectorEvent(String topic, ConnectorKey source) {
        super(source);
        this.topic = topic;
        properties = new HashMap<String, Object>(2);
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
