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
package org.identityconnectors.framework.impl.serializer;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.EncryptorFactory;
import org.identityconnectors.common.security.GuardedByteArray;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

class Primitives {

    public static final List<ObjectTypeMapper> HANDLERS = new ArrayList<ObjectTypeMapper>();

    static {

        HANDLERS.add(new AbstractObjectSerializationHandler(Boolean.class, "Boolean") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readBooleanContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final boolean val = (Boolean) object;
                encoder.writeBooleanContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(boolean.class, "boolean") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readBooleanContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final boolean val = (Boolean) object;
                encoder.writeBooleanContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Character.class, "Character") {

            public Object deserialize(final ObjectDecoder decoder) {
                final String val = decoder.readStringContents();
                return val.charAt(0);
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Character val = (Character) object;
                encoder.writeStringContents(String.valueOf(val));
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(char.class, "char") {

            public Object deserialize(final ObjectDecoder decoder) {
                final String val = decoder.readStringContents();
                return val.charAt(0);
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Character val = (Character) object;
                encoder.writeStringContents(String.valueOf(val));
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Integer.class, "Integer") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readIntContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final int val = (Integer) object;
                encoder.writeIntContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(int.class, "int") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readIntContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final int val = (Integer) object;
                encoder.writeIntContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Long.class, "Long") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readLongContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final long val = (Long) object;
                encoder.writeLongContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(long.class, "long") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readLongContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final long val = (Long) object;
                encoder.writeLongContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Float.class, "Float") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readFloatContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final float val = (Float) object;
                encoder.writeFloatContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(float.class, "float") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readFloatContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final float val = (Float) object;
                encoder.writeFloatContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Double.class, "Double") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readDoubleContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final double val = (Double) object;
                encoder.writeDoubleContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(double.class, "double") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readDoubleContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final double val = (Double) object;
                encoder.writeDoubleContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(String.class, "String") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readStringContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final String val = (String) object;
                encoder.writeStringContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(URI.class, "URI") {

            public Object deserialize(final ObjectDecoder decoder) {
                final String val = decoder.readStringContents();
                try {
                    return new URI(val);
                } catch (URISyntaxException e) {
                    throw ConnectorException.wrap(e);
                }
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final URI val = (URI) object;
                encoder.writeStringContents(val.toString());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(File.class, "File") {

            public Object deserialize(final ObjectDecoder decoder) {
                final String val = decoder.readStringContents();
                return new File(val);
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final File val = (File) object;
                encoder.writeStringContents(val.getPath());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(BigDecimal.class, "BigDecimal") {

            public Object deserialize(final ObjectDecoder decoder) {
                final BigInteger unscaled =
                        new BigInteger(decoder.readStringField("unscaled", null));
                final int scale = decoder.readIntField("scale", 0);
                return new BigDecimal(unscaled, scale);
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final BigDecimal val = (BigDecimal) object;
                encoder.writeStringField("unscaled", val.unscaledValue().toString());
                encoder.writeIntField("scale", val.scale());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(BigInteger.class, "BigInteger") {

            public Object deserialize(final ObjectDecoder decoder) {
                final String val = decoder.readStringContents();
                return new BigInteger(val);
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final BigInteger val = (BigInteger) object;
                encoder.writeStringContents(val.toString());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Byte.class, "Byte") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readByteArrayContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                byte val = (Byte) object;
                encoder.writeByteContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(byte.class, "byte") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readByteArrayContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                byte val = (Byte) object;
                encoder.writeByteContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(byte[].class, "ByteArray") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readByteArrayContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final byte[] val = (byte[]) object;
                encoder.writeByteArrayContents(val);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Class.class, "Class") {

            public Object deserialize(final ObjectDecoder decoder) {
                return decoder.readClassContents();
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Class<?> val = (Class<?>) object;
                encoder.writeClassContents(val);
            }
        });

        class MapEntry {
            private Object key;
            private Object value;

            public MapEntry(Object key, Object value) {
                this.key = key;
                this.value = value;
            }
        }

        HANDLERS.add(new AbstractObjectSerializationHandler(MapEntry.class, "MapEntry") {

            public Object deserialize(final ObjectDecoder decoder) {
                final Object key = decoder.readObjectContents(0);
                final Object value = decoder.readObjectContents(1);
                return new MapEntry(key, value);
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final MapEntry entry = (MapEntry) object;
                encoder.writeObjectContents(entry.key);
                encoder.writeObjectContents(entry.value);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Map.class, "Map") {

            public Object deserialize(final ObjectDecoder decoder) {
                final boolean caseInsensitive = decoder.readBooleanField("caseInsensitive", false);
                if (caseInsensitive) {
                    final SortedMap<String, Object> rv =
                            CollectionUtil.<Object> newCaseInsensitiveMap();
                    final int count = decoder.getNumSubObjects();
                    for (int i = 0; i < count; i++) {
                        final MapEntry entry = (MapEntry) decoder.readObjectContents(i);
                        rv.put(String.valueOf(entry.key), entry.value);
                    }
                    return rv;
                } else {
                    final Map<Object, Object> rv = new HashMap<Object, Object>();
                    final int count = decoder.getNumSubObjects();
                    for (int i = 0; i < count; i++) {
                        final MapEntry entry = (MapEntry) decoder.readObjectContents(i);
                        rv.put(entry.key, entry.value);
                    }
                    return rv;
                }
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Map<?, ?> map = (Map<?, ?>) object;
                // special case - for case insensitive maps
                if (CollectionUtil.isCaseInsensitiveMap(map)) {
                    encoder.writeBooleanField("caseInsensitive", true);
                }
                // for all other sorted maps, we don't know how
                // to serialize them
                else if (map instanceof SortedMap) {
                    throw new IllegalArgumentException("Serialization of SortedMap not supported");
                }
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    final MapEntry myEntry = new MapEntry(entry.getKey(), entry.getValue());
                    encoder.writeObjectContents(myEntry);
                }
            }

            @Override
            public boolean isMatchSubclasses() {
                return true;
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(List.class, "List") {

            public Object deserialize(final ObjectDecoder decoder) {
                final List<Object> rv = new ArrayList<Object>();
                final int count = decoder.getNumSubObjects();
                for (int i = 0; i < count; i++) {
                    rv.add(decoder.readObjectContents(i));
                }
                return rv;
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final List<?> list = (List<?>) object;
                for (Object obj : list) {
                    encoder.writeObjectContents(obj);
                }
            }

            @Override
            public boolean isMatchSubclasses() {
                return true;
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Set.class, "Set") {

            public Object deserialize(final ObjectDecoder decoder) {
                final boolean caseInsensitive = decoder.readBooleanField("caseInsensitive", false);
                if (caseInsensitive) {
                    final Set<String> rv = CollectionUtil.newCaseInsensitiveSet();
                    final int count = decoder.getNumSubObjects();
                    for (int i = 0; i < count; i++) {
                        rv.add(String.valueOf(decoder.readObjectContents(i)));
                    }
                    return rv;
                } else {
                    final Set<Object> rv = new HashSet<Object>();
                    final int count = decoder.getNumSubObjects();
                    for (int i = 0; i < count; i++) {
                        rv.add(decoder.readObjectContents(i));
                    }
                    return rv;
                }
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Set<?> set = (Set<?>) object;
                // special case - for case insensitive sets
                if (CollectionUtil.isCaseInsensitiveSet(set)) {
                    encoder.writeBooleanField("caseInsensitive", true);
                }
                // for all other sorted sets, we don't know how
                // to serialize them
                else if (set instanceof SortedSet) {
                    throw new IllegalArgumentException("Serialization of SortedSet not supported");
                }
                for (Object obj : set) {
                    encoder.writeObjectContents(obj);
                }
            }

            @Override
            public boolean isMatchSubclasses() {
                return true;
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Locale.class, "Locale") {

            public Object deserialize(final ObjectDecoder decoder) {
                final String language = decoder.readStringField("language", "");
                final String country = decoder.readStringField("country", "");
                final String variant = decoder.readStringField("variant", "");
                return new Locale(language, country, variant);
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Locale locale = (Locale) object;
                encoder.writeStringField("language", locale.getLanguage());
                encoder.writeStringField("country", locale.getCountry());
                encoder.writeStringField("variant", locale.getVariant());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(GuardedString.class, "GuardedString") {

            public Object deserialize(final ObjectDecoder decoder) {
                byte[] encryptedBytes = null;
                byte[] clearBytes = null;
                char[] clearChars = null;
                try {
                    encryptedBytes = decoder.readByteArrayContents();
                    clearBytes =
                            EncryptorFactory.getInstance().getDefaultEncryptor().decrypt(
                                    encryptedBytes);
                    clearChars = SecurityUtil.bytesToChars(clearBytes);
                    return new GuardedString(clearChars);
                } finally {
                    SecurityUtil.clear(encryptedBytes);
                    SecurityUtil.clear(clearBytes);
                    SecurityUtil.clear(clearChars);
                }
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final GuardedString val = (GuardedString) object;
                val.access(new GuardedString.Accessor() {

                    public void access(final char[] clearChars) {
                        byte[] encryptedBytes = null;
                        byte[] clearBytes = null;
                        try {
                            clearBytes = SecurityUtil.charsToBytes(clearChars);
                            encryptedBytes =
                                    EncryptorFactory.getInstance().getDefaultEncryptor().encrypt(
                                            clearBytes);
                            encoder.writeByteArrayContents(encryptedBytes);
                        } finally {
                            SecurityUtil.clear(encryptedBytes);
                            SecurityUtil.clear(clearBytes);
                        }
                    }
                });
            }

        });
        HANDLERS.add(new AbstractObjectSerializationHandler(GuardedByteArray.class,
                "GuardedByteArray") {

            public Object deserialize(final ObjectDecoder decoder) {
                byte[] encryptedBytes = null;
                byte[] clearBytes = null;
                try {
                    encryptedBytes = decoder.readByteArrayContents();
                    clearBytes =
                            EncryptorFactory.getInstance().getDefaultEncryptor().decrypt(
                                    encryptedBytes);
                    return new GuardedByteArray(clearBytes);
                } finally {
                    SecurityUtil.clear(encryptedBytes);
                    SecurityUtil.clear(clearBytes);
                }
            }

            public void serialize(final Object object, final ObjectEncoder encoder) {
                final GuardedByteArray val = (GuardedByteArray) object;
                val.access(new GuardedByteArray.Accessor() {
                    public void access(byte[] clearBytes) {
                        byte[] encryptedBytes = null;
                        try {
                            encryptedBytes =
                                    EncryptorFactory.getInstance().getDefaultEncryptor().encrypt(
                                            clearBytes);
                            encoder.writeByteArrayContents(encryptedBytes);
                        } finally {
                            SecurityUtil.clear(encryptedBytes);
                        }
                    }
                });
            }
        });
    }
}
