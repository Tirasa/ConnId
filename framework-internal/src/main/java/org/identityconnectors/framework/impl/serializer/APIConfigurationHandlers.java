/**
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2011-2013 Tirasa. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License"). You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at https://oss.oracle.com/licenses/CDDL.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertiesImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertyImpl;
import org.identityconnectors.framework.impl.api.ConnectorMessagesImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoImpl;

/**
 * Serialization handles for APIConfiguration and dependencies.
 */
class APIConfigurationHandlers {

    public static final List<ObjectTypeMapper> HANDLERS = new ArrayList<ObjectTypeMapper>();

    static {

        HANDLERS.add(new AbstractObjectSerializationHandler(ObjectPoolConfiguration.class, "ObjectPoolConfiguration") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final ObjectPoolConfiguration rv = new ObjectPoolConfiguration();
                rv.setMaxObjects(decoder.readIntField("maxObjects", rv.getMaxObjects()));
                rv.setMaxIdle(decoder.readIntField("maxIdle", rv.getMaxIdle()));
                rv.setMaxWait(decoder.readLongField("maxWait", rv.getMaxWait()));
                rv.setMinEvictableIdleTimeMillis(
                        decoder.readLongField("minEvictableIdleTimeMillis", rv.getMinEvictableIdleTimeMillis()));
                rv.setMinIdle(decoder.readIntField("minIdle", rv.getMinIdle()));
                return rv;
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                ObjectPoolConfiguration val = (ObjectPoolConfiguration) object;
                encoder.writeIntField("maxObjects", val.getMaxObjects());
                encoder.writeIntField("maxIdle", val.getMaxIdle());
                encoder.writeLongField("maxWait", val.getMaxWait());
                encoder.writeLongField("minEvictableIdleTimeMillis", val.getMinEvictableIdleTimeMillis());
                encoder.writeIntField("minIdle", val.getMinIdle());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(ConfigurationPropertyImpl.class, "ConfigurationProperty") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final ConfigurationPropertyImpl rv = new ConfigurationPropertyImpl();

                rv.setOrder(decoder.readIntField("order", 0));
                rv.setRequired(decoder.readBooleanField("required", false));
                rv.setConfidential(decoder.readBooleanField("confidential", false));
                rv.setName(decoder.readStringField("name", null));
                rv.setHelpMessageKey(decoder.readStringField("helpMessageKey", null));
                rv.setDisplayMessageKey(decoder.readStringField("displayMessageKey", null));
                rv.setType(decoder.readClassField("type", null));
                rv.setValue(decoder.readObjectField("value", null, null));
                @SuppressWarnings("unchecked")
                Set<Class<? extends APIOperation>> ops = (Set) decoder.readObjectField("operations", Set.class, null);
                rv.setOperations(ops);
                return rv;
            }

            @Override
            public void serialize(Object object, ObjectEncoder encoder) {
                final ConfigurationPropertyImpl val = (ConfigurationPropertyImpl) object;
                encoder.writeIntField("order", val.getOrder());
                encoder.writeBooleanField("confidential", val.isConfidential());
                encoder.writeBooleanField("required", val.isRequired());
                encoder.writeStringField("name", val.getName());
                encoder.writeStringField("helpMessageKey", val.getHelpMessageKey());
                encoder.writeStringField("displayMessageKey", val.getDisplayMessageKey());
                encoder.writeClassField("type", val.getType());
                encoder.writeObjectField("value", val.getValue(), false);
                encoder.writeObjectField("operations", val.getOperations(), true);
            }
        });

        HANDLERS.add(
                new AbstractObjectSerializationHandler(ConfigurationPropertiesImpl.class, "ConfigurationProperties") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final ConfigurationPropertiesImpl rv = new ConfigurationPropertiesImpl();
                final List<ConfigurationPropertyImpl> props = new ArrayList<ConfigurationPropertyImpl>();
                final int count = decoder.getNumSubObjects();
                for (int i = 0; i < count; i++) {
                    props.add((ConfigurationPropertyImpl) decoder.readObjectContents(i));
                }
                rv.setProperties(props);
                return rv;
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ConfigurationPropertiesImpl val = (ConfigurationPropertiesImpl) object;
                for (ConfigurationPropertyImpl prop : val.getProperties()) {
                    encoder.writeObjectContents(prop);
                }
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(APIConfigurationImpl.class, "APIConfiguration") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final APIConfigurationImpl rv = new APIConfigurationImpl();
                rv.setConnectorPoolingSupported(decoder.readBooleanField("connectorPoolingSupported", false));
                rv.setConnectorPoolConfiguration(
                        (ObjectPoolConfiguration) decoder.readObjectField("connectorPoolConfiguration", null, null));
                rv.setConfigurationProperties((ConfigurationPropertiesImpl) decoder.readObjectField(
                        "ConfigurationProperties", ConfigurationPropertiesImpl.class, null));
                @SuppressWarnings("unchecked")
                Map<Class<? extends APIOperation>, Integer> map =
                        (Map) decoder.readObjectField("timeoutMap", null, null);
                rv.setTimeoutMap(map);
                @SuppressWarnings("unchecked")
                Set<Class<? extends APIOperation>> set =
                        (Set) decoder.readObjectField("SupportedOperations", Set.class, null);
                rv.setSupportedOperations(set);
                rv.setProducerBufferSize(decoder.readIntField("producerBufferSize", 0));
                return rv;
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final APIConfigurationImpl val = (APIConfigurationImpl) object;
                encoder.writeIntField("producerBufferSize", val.getProducerBufferSize());
                encoder.writeBooleanField("connectorPoolingSupported", val.isConnectorPoolingSupported());
                encoder.writeObjectField("connectorPoolConfiguration", val.getConnectorPoolConfiguration(), false);
                encoder.writeObjectField("ConfigurationProperties", val.getConfigurationProperties(), true);
                encoder.writeObjectField("timeoutMap", val.getTimeoutMap(), false);
                encoder.writeObjectField("SupportedOperations", val.getSupportedOperations(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(ConnectorMessagesImpl.class, "ConnectorMessages") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final ConnectorMessagesImpl rv = new ConnectorMessagesImpl();
                @SuppressWarnings("unchecked")
                final Map<Locale, Map<String, String>> catalogs =
                        (Map) decoder.readObjectField("catalogs", null, null);
                rv.setCatalogs(catalogs);
                return rv;
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ConnectorMessagesImpl val = (ConnectorMessagesImpl) object;
                encoder.writeObjectField("catalogs", val.getCatalogs(), false);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(ConnectorKey.class, "ConnectorKey") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final String bundleName = decoder.readStringField("bundleName", null);
                final String bundleVersion = decoder.readStringField("bundleVersion", null);
                final String connectorName = decoder.readStringField("connectorName", null);
                return new ConnectorKey(bundleName, bundleVersion, connectorName);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ConnectorKey val = (ConnectorKey) object;
                encoder.writeStringField("bundleName", val.getBundleName());
                encoder.writeStringField("bundleVersion", val.getBundleVersion());
                encoder.writeStringField("connectorName", val.getConnectorName());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(RemoteConnectorInfoImpl.class, "ConnectorInfo") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final RemoteConnectorInfoImpl rv = new RemoteConnectorInfoImpl();
                rv.setConnectorDisplayNameKey(decoder.readStringField("connectorDisplayNameKey", null));
                rv.setConnectorKey((ConnectorKey) decoder.readObjectField("ConnectorKey", ConnectorKey.class, null));
                rv.setMessages((ConnectorMessagesImpl) decoder.readObjectField("ConnectorMessages",
                        ConnectorMessagesImpl.class, null));
                rv.setDefaultAPIConfiguration((APIConfigurationImpl) decoder.readObjectField("APIConfiguration",
                        APIConfigurationImpl.class, null));
                return rv;
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final RemoteConnectorInfoImpl val = (RemoteConnectorInfoImpl) object;
                encoder.writeStringField("connectorDisplayNameKey", val.getConnectorDisplayNameKey());
                encoder.writeObjectField("ConnectorKey", val.getConnectorKey(), true);
                encoder.writeObjectField("ConnectorMessages", val.getMessages(), true);
                encoder.writeObjectField("APIConfiguration", val.getDefaultAPIConfiguration(), true);
            }
        });
    }
}
