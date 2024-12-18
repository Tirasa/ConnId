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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2015-2016 Evolveum
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.serializer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.identityconnectors.common.script.Script;
import org.identityconnectors.common.script.ScriptBuilder;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectionBrokenException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.ConnectorSecurityException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.exceptions.InvalidPasswordException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.exceptions.PasswordExpiredException;
import org.identityconnectors.framework.common.exceptions.PermissionDeniedException;
import org.identityconnectors.framework.common.exceptions.PreconditionFailedException;
import org.identityconnectors.framework.common.exceptions.PreconditionRequiredException;
import org.identityconnectors.framework.common.exceptions.RetryableException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.AttributeInfo.Flags;
import org.identityconnectors.framework.impl.api.remote.RemoteWrappedException;

/**
 * Serialization handles for APIConfiguration and dependencies.
 */
class CommonObjectHandlers {

    public static final List<ObjectTypeMapper> HANDLERS = new ArrayList<>();

    private static abstract class AttributeHandler<T extends Attribute> extends AbstractObjectSerializationHandler {

        protected AttributeHandler(final Class<T> clazz, final String typeName) {
            super(clazz, typeName);
        }

        @Override
        public final Object deserialize(final ObjectDecoder decoder) {
            final String name = decoder.readStringField("name", null);
            @SuppressWarnings("unchecked")
            final List<Object> value = (List<Object>) decoder.readObjectField("Values", List.class, null);
            return createAttribute(name, value);
        }

        @Override
        public final void serialize(final Object object, final ObjectEncoder encoder) {
            final Attribute val = (Attribute) object;
            encoder.writeStringField("name", val.getName());
            encoder.writeObjectField("Values", val.getValue(), true);
        }

        protected abstract T createAttribute(String name, List<Object> value);
    }

    private static abstract class ThrowableHandler<T extends Throwable> extends
            AbstractObjectSerializationHandler {

        protected ThrowableHandler(final Class<T> clazz, final String typeName) {
            super(clazz, typeName);
        }

        @Override
        public Object deserialize(final ObjectDecoder decoder) {
            final String message = decoder.readStringField("message", null);
            return createException(message);
        }

        @Override
        public void serialize(final Object object, final ObjectEncoder encoder) {
            final Throwable val = (Throwable) object;
            encoder.writeStringField("message", val.getMessage());
        }

        @Override
        public boolean isMatchSubclasses() {
            return true;
        }

        protected abstract T createException(String message);
    }

    static {

        HANDLERS.add(new ThrowableHandler<AlreadyExistsException>(
                AlreadyExistsException.class, "AlreadyExistsException") {

            @Override
            protected AlreadyExistsException createException(final String message) {
                return new AlreadyExistsException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<ConfigurationException>(
                ConfigurationException.class, "ConfigurationException") {

            @Override
            protected ConfigurationException createException(final String message) {
                return new ConfigurationException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<ConnectionBrokenException>(
                ConnectionBrokenException.class, "ConnectionBrokenException") {

            @Override
            protected ConnectionBrokenException createException(final String message) {
                return new ConnectionBrokenException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<ConnectionFailedException>(
                ConnectionFailedException.class, "ConnectionFailedException") {

            @Override
            protected ConnectionFailedException createException(final String message) {
                return new ConnectionFailedException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<ConnectorIOException>(
                ConnectorIOException.class, "ConnectorIOException") {

            @Override
            protected ConnectorIOException createException(final String message) {
                return new ConnectorIOException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<PasswordExpiredException>(
                PasswordExpiredException.class, "PasswordExpiredException") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final Uid uid = (Uid) decoder.readObjectField("Uid", Uid.class, null);
                final PasswordExpiredException ex =
                        (PasswordExpiredException) super.deserialize(decoder);
                return ex.initUid(uid);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                super.serialize(object, encoder);
                final PasswordExpiredException val = (PasswordExpiredException) object;
                encoder.writeObjectField("Uid", val.getUid(), true);
            }

            @Override
            protected PasswordExpiredException createException(final String message) {
                return new PasswordExpiredException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<InvalidPasswordException>(
                InvalidPasswordException.class, "InvalidPasswordException") {

            @Override
            protected InvalidPasswordException createException(final String message) {
                return new InvalidPasswordException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<UnknownUidException>(UnknownUidException.class, "UnknownUidException") {

            @Override
            protected UnknownUidException createException(final String message) {
                return new UnknownUidException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<InvalidCredentialException>(
                InvalidCredentialException.class, "InvalidCredentialException") {

            @Override
            protected InvalidCredentialException createException(final String message) {
                return new InvalidCredentialException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<PermissionDeniedException>(
                PermissionDeniedException.class, "PermissionDeniedException") {

            @Override
            protected PermissionDeniedException createException(final String message) {
                return new PermissionDeniedException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<ConnectorSecurityException>(
                ConnectorSecurityException.class, "ConnectorSecurityException") {

            @Override
            protected ConnectorSecurityException createException(final String message) {
                return new ConnectorSecurityException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<OperationTimeoutException>(
                OperationTimeoutException.class, "OperationTimeoutException") {

            @Override
            protected OperationTimeoutException createException(final String message) {
                return new OperationTimeoutException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<InvalidAttributeValueException>(
                InvalidAttributeValueException.class, "InvalidAttributeValueException") {

            @Override
            protected InvalidAttributeValueException createException(final String message) {
                return new InvalidAttributeValueException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<PreconditionFailedException>(
                PreconditionFailedException.class, "PreconditionFailedException") {

            @Override
            protected PreconditionFailedException createException(final String message) {
                return new PreconditionFailedException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<PreconditionRequiredException>(
                PreconditionRequiredException.class, "PreconditionRequiredException") {

            @Override
            protected PreconditionRequiredException createException(final String message) {
                return new PreconditionRequiredException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<RetryableException>(RetryableException.class, "RetryableException") {

            @Override
            protected RetryableException createException(final String message) {
                return RetryableException.wrap(message, (Throwable) null);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(RemoteWrappedException.class, "RemoteWrappedException") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                String throwableClass = decoder.readStringField(
                        RemoteWrappedException.FIELD_CLASS, ConnectorException.class.getName());
                String message = decoder.readStringField(RemoteWrappedException.FIELD_MESSAGE, null);
                RemoteWrappedException cause = (RemoteWrappedException) decoder.
                        readObjectField("RemoteWrappedException", RemoteWrappedException.class, null);
                String stackTrace = decoder.readStringField(RemoteWrappedException.FIELD_STACK_TRACE, null);

                return new RemoteWrappedException(throwableClass, message, cause, stackTrace);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final RemoteWrappedException val = (RemoteWrappedException) object;
                encoder.writeStringField(RemoteWrappedException.FIELD_CLASS, val.getExceptionClass());
                encoder.writeStringField(RemoteWrappedException.FIELD_MESSAGE, val.getMessage());
                encoder.writeObjectField("RemoteWrappedException", val.getCause(), true);
                encoder.writeStringField(RemoteWrappedException.FIELD_STACK_TRACE, val.readStackTrace());
            }
        });

        HANDLERS.add(new ThrowableHandler<ConnectorException>(ConnectorException.class, "ConnectorException") {

            @Override
            protected ConnectorException createException(final String message) {
                return new ConnectorException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<IllegalArgumentException>(
                IllegalArgumentException.class, "IllegalArgumentException") {

            @Override
            protected IllegalArgumentException createException(final String message) {
                return new IllegalArgumentException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<RuntimeException>(RuntimeException.class, "RuntimeException") {

            @Override
            protected RuntimeException createException(final String message) {
                return new RuntimeException(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<Exception>(Exception.class, "Exception") {

            @Override
            protected Exception createException(final String message) {
                return new Exception(message);
            }
        });

        HANDLERS.add(new ThrowableHandler<Throwable>(Throwable.class, "Throwable") {

            @Override
            protected Throwable createException(final String message) {
                return new RuntimeException(message);
            }
        });

        HANDLERS.add(new AttributeHandler<Attribute>(Attribute.class, "Attribute") {

            @Override
            protected Attribute createAttribute(final String name, final List<Object> value) {
                return AttributeBuilder.build(name, value);
            }
        });

        HANDLERS.add(new EnumSerializationHandler(Flags.class, "AttributeInfoFlag"));

        HANDLERS.add(new AbstractObjectSerializationHandler(AttributeInfo.class, "AttributeInfo") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final AttributeInfoBuilder builder = new AttributeInfoBuilder(
                        decoder.readStringField("name", null), decoder.readClassField("type", null));
                final Set<Flags> flags = EnumSet.noneOf(Flags.class);
                final int count = decoder.getNumSubObjects();
                for (int i = 0; i < count; i++) {
                    final Object o = decoder.readObjectContents(i);
                    if (o instanceof AttributeInfo.Flags) {
                        flags.add((AttributeInfo.Flags) o);
                    }
                }
                builder.setFlags(flags);
                builder.setNativeName(decoder.readStringField("nativeName", null));
                builder.setSubtype(decoder.readStringField("subtype", null));
                builder.setReferencedObjectClassName(decoder.readStringField("referencedObjectClassName", null));
                builder.setRoleInReference(decoder.readStringField("roleInReference", null));
                return builder.build();
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final AttributeInfo val = (AttributeInfo) object;
                encoder.writeStringField("name", val.getName());
                encoder.writeClassField("type", val.getType());
                for (Flags flag : val.getFlags()) {
                    encoder.writeObjectContents(flag);
                }
                encoder.writeStringField("nativeName", val.getNativeName());
                encoder.writeStringField("subtype", val.getSubtype());
                encoder.writeStringField("referencedObjectClassName", val.getReferencedObjectClassName());
                encoder.writeStringField("roleInReference", val.getRoleInReference());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(ConnectorObject.class, "ConnectorObject") {

            @Override
            public Object deserialize(ObjectDecoder decoder) {
                final ObjectClass objectClass = (ObjectClass) decoder.
                        readObjectField("ObjectClass", ObjectClass.class, null);
                @SuppressWarnings("unchecked")
                Set<? extends Attribute> atts = (Set<? extends Attribute>) decoder.
                        readObjectField("Attributes", Set.class, null);
                return new ConnectorObject(objectClass, atts);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ConnectorObject val = (ConnectorObject) object;
                encoder.writeObjectField("ObjectClass", val.getObjectClass(), true);
                encoder.writeObjectField("Attributes", val.getAttributes(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(
                ConnectorObjectIdentification.class, "ConnectorObjectIdentification") {

            @SuppressWarnings("unchecked")
            @Override
            public Object deserialize(ObjectDecoder decoder) {
                return new ConnectorObjectIdentification(
                        (ObjectClass) decoder.readObjectField("ObjectClass", ObjectClass.class, null),
                        (Set<? extends Attribute>) decoder.readObjectField("Attributes", Set.class, null));
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ConnectorObjectIdentification val = (ConnectorObjectIdentification) object;
                encoder.writeObjectField("ObjectClass", val.getObjectClass(), true);
                encoder.writeObjectField("Attributes", val.getAttributes(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(ConnectorObjectReference.class,
                "ConnectorObjectReference") {

            @Override
            public Object deserialize(ObjectDecoder decoder) {
                return new ConnectorObjectReference(
                        (BaseConnectorObject) decoder.readObjectField("Value", BaseConnectorObject.class, null));
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ConnectorObjectReference val = (ConnectorObjectReference) object;
                encoder.writeObjectField("Value", val.getValue(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Name.class, "Name") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final String val = decoder.readStringContents();
                return new Name(val);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Name val = (Name) object;
                encoder.writeStringContents(val.getNameValue());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(ObjectClass.class, "ObjectClass") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final String type = decoder.readStringField("type", null);
                return new ObjectClass(type);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ObjectClass val = (ObjectClass) object;
                encoder.writeStringField("type", val.getObjectClassValue());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(ObjectClassInfo.class, "ObjectClassInfo") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final String type = decoder.readStringField("type", null);
                final boolean container = decoder.readBooleanField("container", false);
                final boolean auxiliary = decoder.readBooleanField("auxiliary", false);
                final boolean embedded = decoder.readBooleanField("embedded", false);

                @SuppressWarnings("unchecked")
                final Set<AttributeInfo> attrInfo = (Set) decoder.readObjectField("AttributeInfos", Set.class, null);

                return new ObjectClassInfo(type, attrInfo, container, auxiliary, embedded);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ObjectClassInfo val = (ObjectClassInfo) object;

                encoder.writeStringField("type", val.getType());
                encoder.writeBooleanField("container", val.isContainer());
                encoder.writeBooleanField("auxiliary", val.isAuxiliary());
                encoder.writeBooleanField("embedded", val.isEmbedded());
                encoder.writeObjectField("AttributeInfos", val.getAttributeInfo(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Schema.class, "Schema") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                @SuppressWarnings("unchecked")
                final Set<ObjectClassInfo> objectClasses =
                        (Set<ObjectClassInfo>) decoder.readObjectField("ObjectClassInfos", Set.class, null);
                final Map<String, ObjectClassInfo> objectClassesByName = objectClasses.stream().
                        collect(Collectors.toMap(ObjectClassInfo::getType, Function.identity()));

                @SuppressWarnings("unchecked")
                final Set<OperationOptionInfo> operationOptions =
                        (Set<OperationOptionInfo>) decoder.readObjectField("OperationOptionInfos", Set.class, null);
                final Map<String, OperationOptionInfo> optionsByName = operationOptions.stream().
                        collect(Collectors.toMap(OperationOptionInfo::getName, Function.identity()));

                @SuppressWarnings("unchecked")
                final Map<Class<? extends APIOperation>, Set<String>> objectClassNamesByOperation =
                        (Map<Class<? extends APIOperation>, Set<String>>) decoder.
                                readObjectField("objectClassesByOperation", null, null);
                @SuppressWarnings("unchecked")
                final Map<Class<? extends APIOperation>, Set<String>> optionsNamesByOperation =
                        (Map<Class<? extends APIOperation>, Set<String>>) decoder.
                                readObjectField("optionsByOperation", null, null);
                final Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> objectClassesByOperation =
                        new HashMap<>();
                for (var entry : objectClassNamesByOperation.entrySet()) {
                    Set<ObjectClassInfo> infos = entry.getValue().stream().
                            map(objectClassesByName::get).filter(Objects::nonNull).
                            collect(Collectors.toSet());
                    objectClassesByOperation.put(entry.getKey(), infos);
                }

                final Map<Class<? extends APIOperation>, Set<OperationOptionInfo>> optionsByOperation =
                        new HashMap<>();
                for (var entry : optionsNamesByOperation.entrySet()) {
                    Set<OperationOptionInfo> infos = entry.getValue().stream().
                            map(optionsByName::get).filter(Objects::nonNull).
                            collect(Collectors.toSet());
                    optionsByOperation.put(entry.getKey(), infos);
                }
                return new Schema(objectClasses, operationOptions, objectClassesByOperation, optionsByOperation);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Schema val = (Schema) object;
                encoder.writeObjectField("ObjectClassInfos", val.getObjectClassInfo(), true);
                encoder.writeObjectField("OperationOptionInfos", val.getOperationOptionInfo(), true);

                final Map<Class<? extends APIOperation>, Set<String>> objectClassNamesByOperation = new HashMap<>();

                final Map<Class<? extends APIOperation>, Set<String>> optionNamesByOperation = new HashMap<>();

                for (var entry : val.getSupportedObjectClassesByOperation().entrySet()) {
                    Set<String> names = entry.getValue().stream().
                            map(ObjectClassInfo::getType).collect(Collectors.toSet());
                    objectClassNamesByOperation.put(entry.getKey(), names);
                }
                for (var entry : val.getSupportedOptionsByOperation().entrySet()) {
                    Set<String> names = entry.getValue().stream().
                            map(OperationOptionInfo::getName).collect(Collectors.toSet());
                    optionNamesByOperation.put(entry.getKey(), names);
                }

                encoder.writeObjectField("objectClassesByOperation", objectClassNamesByOperation, false);
                encoder.writeObjectField("optionsByOperation", optionNamesByOperation, false);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Uid.class, "Uid") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final String val = decoder.readStringField("uid", null);
                final String revision = decoder.readStringField("revision", null);
                final Name nameHint = (Name) decoder.readObjectField("nameHint", Name.class, null);
                // revision parameter is not-null checked, nameHint is nullable
                return revision == null
                        ? new Uid(val, nameHint)
                        : new Uid(val, revision, nameHint);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Uid val = (Uid) object;
                encoder.writeStringField("uid", val.getUidValue());
                encoder.writeStringField("revision", val.getRevision());
                encoder.writeObjectField("nameHint", val.getNameHint(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(Script.class, "Script") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final ScriptBuilder builder = new ScriptBuilder();
                builder.setScriptLanguage(decoder.readStringField("scriptLanguage", null));
                // don't used string field - don't want it to be an attribute
                builder.setScriptText((String) decoder.readObjectField("scriptText", String.class,
                        null));
                return builder.build();
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final Script val = (Script) object;
                encoder.writeStringField("scriptLanguage", val.getScriptLanguage());
                encoder.writeObjectField("scriptText", val.getScriptText(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(ScriptContext.class, "ScriptContext") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final String scriptLanguage = decoder.readStringField("scriptLanguage", null);
                @SuppressWarnings("unchecked")
                final Map<String, Object> arguments = (Map<String, Object>) decoder.
                        readObjectField("scriptArguments", null, null);
                // don't used string field - don't want it to be an attribute
                final String scriptText = (String) decoder.readObjectField("scriptText", String.class, null);
                return new ScriptContext(scriptLanguage, scriptText, arguments);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final ScriptContext val = (ScriptContext) object;
                encoder.writeStringField("scriptLanguage", val.getScriptLanguage());
                encoder.writeObjectField("scriptArguments", val.getScriptArguments(), false);
                encoder.writeObjectField("scriptText", val.getScriptText(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(OperationOptions.class, "OperationOptions") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> options = (Map<String, Object>) decoder.
                        readObjectField("options", null, null);
                return new OperationOptions(options);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final OperationOptions val = (OperationOptions) object;
                encoder.writeObjectField("options", val.getOptions(), false);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(SearchResult.class, "SearchResult") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                return new SearchResult(decoder.readStringField("pagedResultsCookie", null),
                        decoder.readIntField("remainingPagedResults", -1));
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final SearchResult val = (SearchResult) object;
                encoder.writeStringField("pagedResultsCookie", val.getPagedResultsCookie());
                encoder.writeIntField("remainingPagedResults", val.getRemainingPagedResults());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(SortKey.class, "SortKey") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                return new SortKey(
                        decoder.readStringField("field", null), decoder.readBooleanField("isAscending", true));
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final SortKey val = (SortKey) object;
                encoder.writeStringField("field", val.getField());
                encoder.writeBooleanField("isAscending", val.isAscendingOrder());
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(OperationOptionInfo.class, "OperationOptionInfo") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final String name = decoder.readStringField("name", null);
                Class<?> type = decoder.readClassField("type", Class.class);
                return new OperationOptionInfo(name, type);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final OperationOptionInfo val = (OperationOptionInfo) object;
                encoder.writeStringField("name", val.getName());
                encoder.writeClassField("type", val.getType());
            }
        });

        HANDLERS.add(new EnumSerializationHandler(SyncDeltaType.class, "SyncDeltaType"));

        HANDLERS.add(new AbstractObjectSerializationHandler(SyncToken.class, "SyncToken") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final Object value = decoder.readObjectField("value", null, null);
                return new SyncToken(value);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final SyncToken val = (SyncToken) object;
                encoder.writeObjectField("value", val.getValue(), false);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(LiveSyncDelta.class, "LiveSyncDelta") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                return new LiveSyncDeltaBuilder().
                        setObjectClass((ObjectClass) decoder.readObjectField("ObjectClass", ObjectClass.class, null)).
                        setUid((Uid) decoder.readObjectField("Uid", Uid.class, null)).
                        setObject((ConnectorObject) decoder.
                                readObjectField("ConnectorObject", ConnectorObject.class, null)).
                        build();
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final LiveSyncDelta val = (LiveSyncDelta) object;
                encoder.writeObjectField("ObjectClass", val.getObjectClass(), true);
                encoder.writeObjectField("Uid", val.getUid(), true);
                encoder.writeObjectField("ConnectorObject", val.getObject(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(SyncDelta.class, "SyncDelta") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                return new SyncDeltaBuilder().
                        setDeltaType((SyncDeltaType) decoder.
                                readObjectField("SyncDeltaType", SyncDeltaType.class, null)).
                        setToken((SyncToken) decoder.readObjectField("SyncToken", SyncToken.class, null)).
                        setPreviousUid((Uid) decoder.
                                readObjectField("PreviousUid", Uid.class, null)).
                        setObjectClass((ObjectClass) decoder.readObjectField("ObjectClass", ObjectClass.class, null)).
                        setUid((Uid) decoder.readObjectField("Uid", Uid.class, null)).
                        setObject((ConnectorObject) decoder.
                                readObjectField("ConnectorObject", ConnectorObject.class, null)).
                        build();
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final SyncDelta val = (SyncDelta) object;
                encoder.writeObjectField("SyncDeltaType", val.getDeltaType(), true);
                encoder.writeObjectField("SyncToken", val.getToken(), true);
                encoder.writeObjectField("PreviousUid", val.getPreviousUid(), true);
                encoder.writeObjectField("ObjectClass", val.getObjectClass(), true);
                encoder.writeObjectField("Uid", val.getUid(), true);
                encoder.writeObjectField("ConnectorObject", val.getObject(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(AttributeDelta.class, "AttributeDelta") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
                builder.setName((String) decoder.readObjectField("Name", String.class, null));

                @SuppressWarnings("unchecked")
                List<Object> addList = (List<Object>) decoder.readObjectField("ValuesToAdd", List.class, null);
                @SuppressWarnings("unchecked")
                List<Object> removeList = (List<Object>) decoder.readObjectField("ValuesToRemove", List.class, null);
                @SuppressWarnings("unchecked")
                List<Object> replaceList = (List<Object>) decoder.readObjectField("ValuesToReplace", List.class, null);

                if ((addList != null || removeList != null)) {
                    builder.addValueToAdd(addList);
                    builder.addValueToRemove(removeList);
                } else {

                    builder.addValueToReplace(replaceList);
                }
                return builder.build();
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final AttributeDelta val = (AttributeDelta) object;
                encoder.writeObjectField("Name", val.getName(), true);
                encoder.writeObjectField("ValuesToAdd", val.getValuesToAdd(), true);
                encoder.writeObjectField("ValuesToRemove", val.getValuesToRemove(), true);
                encoder.writeObjectField("ValuesToReplace", val.getValuesToReplace(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(QualifiedUid.class, "QualifiedUid") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final ObjectClass objectClass = (ObjectClass) decoder.
                        readObjectField("ObjectClass", ObjectClass.class, null);
                final Uid uid = (Uid) decoder.readObjectField("Uid", Uid.class, null);
                return new QualifiedUid(objectClass, uid);
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final QualifiedUid val = (QualifiedUid) object;
                encoder.writeObjectField("ObjectClass", val.getObjectClass(), true);
                encoder.writeObjectField("Uid", val.getUid(), true);
            }
        });

        HANDLERS.add(new AbstractObjectSerializationHandler(SuggestedValues.class, "SuggestedValues") {

            @Override
            public Object deserialize(final ObjectDecoder decoder) {
                final SuggestedValuesBuilder builder = new SuggestedValuesBuilder();

                @SuppressWarnings("unchecked")
                List<Object> values = (List<Object>) decoder.readObjectField("Values", List.class, null);
                if (values != null) {
                    builder.addValues(values);
                }

                builder.setOpenness((ValueListOpenness) decoder.
                        readObjectField("ValueListOpenness", ValueListOpenness.class, ValueListOpenness.CLOSED));

                return builder.build();
            }

            @Override
            public void serialize(final Object object, final ObjectEncoder encoder) {
                final SuggestedValues val = (SuggestedValues) object;
                encoder.writeObjectField("Values", val.getValues(), true);
                encoder.writeObjectField("ValueListOpenness", val.getOpenness(), true);
            }
        });

        HANDLERS.add(new EnumSerializationHandler(ValueListOpenness.class, "ValueListOpenness"));
    }
}
