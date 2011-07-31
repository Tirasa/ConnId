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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.exceptions.InvalidPasswordException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.exceptions.PasswordExpiredException;
import org.identityconnectors.framework.common.exceptions.PermissionDeniedException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionInfo;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.QualifiedUid;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.AttributeInfo.Flags;


/**
 * Serialization handles for APIConfiguration and dependencies
 */
class CommonObjectHandlers {
    
    public static final List<ObjectTypeMapper> HANDLERS =
        new ArrayList<ObjectTypeMapper>();
    

    private static abstract class AttributeHandler<T extends Attribute> 
    extends AbstractObjectSerializationHandler {
        
        protected AttributeHandler(Class<T> clazz, String typeName) {
            super(clazz,typeName);
        }
        
        
        public final Object deserialize(ObjectDecoder decoder)  {
            String name = decoder.readStringField("name",null);
            @SuppressWarnings("unchecked")
            List<Object> value = (List)decoder.readObjectField("Values",List.class,null);
            return createAttribute(name, value);
        }

        public final void serialize(Object object, ObjectEncoder encoder)
                 {
            Attribute val = (Attribute)object;
            encoder.writeStringField("name", val.getName());
            encoder.writeObjectField("Values", val.getValue(),true);
        }
        
        protected abstract T createAttribute(String name, List<Object> value);
    }
            
    private static abstract class ThrowableHandler<T extends Throwable> 
    extends AbstractObjectSerializationHandler {
        
        protected ThrowableHandler(Class<T> clazz, String typeName) {
            super(clazz,typeName);
        }
        
        
        public Object deserialize(ObjectDecoder decoder)  {
            String message = decoder.readStringField("message",null);
            return createException(message);
        }

        public void serialize(Object object, ObjectEncoder encoder)
                 {
            Throwable val = (Throwable)object;
            encoder.writeStringField("message", val.getMessage());
        }
        @Override
        public boolean getMatchSubclasses() {
            return true;
        }
        protected abstract T createException(String message);
    }
    
    static { 
        
                
        HANDLERS.add(
                
            new ThrowableHandler<AlreadyExistsException>(AlreadyExistsException.class,"AlreadyExistsException") {
            
            protected AlreadyExistsException createException(String message) {
                return new AlreadyExistsException(message);
            }
        });

        HANDLERS.add(
                
            new ThrowableHandler<ConfigurationException>(ConfigurationException.class,"ConfigurationException") {
            
            protected ConfigurationException createException(String message) {
                return new ConfigurationException(message);
            }
        });
        
        HANDLERS.add(
                
                new ThrowableHandler<ConnectionBrokenException>(ConnectionBrokenException.class,"ConnectionBrokenException") {
                
                protected ConnectionBrokenException createException(String message) {
                    return new ConnectionBrokenException(message);
                }
            });
        
        HANDLERS.add(
                
                new ThrowableHandler<ConnectionFailedException>(ConnectionFailedException.class,"ConnectionFailedException") {
                
                protected ConnectionFailedException createException(String message) {
                    return new ConnectionFailedException(message);
                }
            });
        
        HANDLERS.add(
                
                new ThrowableHandler<ConnectorIOException>(ConnectorIOException.class,"ConnectorIOException") {
                
                protected ConnectorIOException createException(String message) {
                    return new ConnectorIOException(message);
                }
            });
        
        
        HANDLERS.add(
                
                new ThrowableHandler<PasswordExpiredException>(PasswordExpiredException.class,"PasswordExpiredException") {
                
                @Override
                public Object deserialize(ObjectDecoder decoder)  {
                    Uid uid = (Uid)decoder.readObjectField("Uid", Uid.class, null);
                    PasswordExpiredException ex =
                        (PasswordExpiredException)super.deserialize(decoder);
                    return ex.initUid(uid);
                }
                
                @Override
                public void serialize(Object object, ObjectEncoder encoder)
                {
                    super.serialize(object, encoder);
                    PasswordExpiredException val = (PasswordExpiredException)object;
                    encoder.writeObjectField("Uid", val.getUid(), true);
                }
                
                protected PasswordExpiredException createException(String message) {
                    return new PasswordExpiredException(message);
                }
            });
        
        HANDLERS.add(
                
            new ThrowableHandler<InvalidPasswordException>(InvalidPasswordException.class,"InvalidPasswordException") {
            
            protected InvalidPasswordException createException(String message) {
                return new InvalidPasswordException(message);
            }
        });
        
        HANDLERS.add(
                
                new ThrowableHandler<UnknownUidException>(UnknownUidException.class,"UnknownUidException") {
                
                protected UnknownUidException createException(String message) {
                    return new UnknownUidException(message);
                }
            });
        
        HANDLERS.add(
                
                new ThrowableHandler<InvalidCredentialException>(InvalidCredentialException.class,"InvalidCredentialException") {
                
                protected InvalidCredentialException createException(String message) {
                    return new InvalidCredentialException(message);
                }
            });
        
        HANDLERS.add(
                
            new ThrowableHandler<PermissionDeniedException>(PermissionDeniedException.class,"PermissionDeniedException") {
            
            protected PermissionDeniedException createException(String message) {
                return new PermissionDeniedException(message);
            }
        });
        
        HANDLERS.add(
                
                new ThrowableHandler<ConnectorSecurityException>(ConnectorSecurityException.class,"ConnectorSecurityException") {
                
                protected ConnectorSecurityException createException(String message) {
                    return new ConnectorSecurityException(message);
                }
            });
        
        HANDLERS.add(
                
            new ThrowableHandler<OperationTimeoutException>(OperationTimeoutException.class,"OperationTimeoutException") {
            
            protected OperationTimeoutException createException(String message) {
                return new OperationTimeoutException(message);
            }
        });
        
        
        
        HANDLERS.add(
                
                new ThrowableHandler<ConnectorException>(ConnectorException.class,"ConnectorException") {
                
                protected ConnectorException createException(String message) {
                    return new ConnectorException(message);
                }
            });
        
        HANDLERS.add(
                
                new ThrowableHandler<IllegalArgumentException>(IllegalArgumentException.class,"IllegalArgumentException") {
                
                protected IllegalArgumentException createException(String message) {
                    return new IllegalArgumentException(message);
                }
            });
        
        HANDLERS.add(
                
            new ThrowableHandler<RuntimeException>(RuntimeException.class,"RuntimeException") {
            
            protected RuntimeException createException(String message) {
                return new RuntimeException(message);
            }
        });
        
        HANDLERS.add(
                
            new ThrowableHandler<Exception>(Exception.class,"Exception") {
            
            protected Exception createException(String message) {
                return new Exception(message);
            }
        });
        
        HANDLERS.add(
                
            new ThrowableHandler<Throwable>(Throwable.class,"Throwable") {
            
            protected Throwable createException(String message) {
                return new RuntimeException(message);
            }
        });
        
        HANDLERS.add(
                
            new AttributeHandler<Attribute>(Attribute.class,"Attribute") {
            
            protected Attribute createAttribute(String name, List<Object> value) {
                return AttributeBuilder.build(name, value);
            }
        });
        
        HANDLERS.add(
           new EnumSerializationHandler(Flags.class,"AttributeInfoFlag")
           );
        
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(AttributeInfo.class,"AttributeInfo") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                AttributeInfoBuilder builder = 
                    new AttributeInfoBuilder(decoder.readStringField("name",null),
                            decoder.readClassField("type",null));
                Set<Flags> flags = new HashSet<Flags>();
                int count = decoder.getNumSubObjects();
                for ( int i = 0; i < count; i++ ) {
                    Object o = decoder.readObjectContents(i);
                    if ( o instanceof AttributeInfo.Flags ) {
                        flags.add((AttributeInfo.Flags)o);
                    }
                }
                builder.setFlags(flags);
                return builder.build();
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
            {
                AttributeInfo val = (AttributeInfo)object;
                encoder.writeStringField("name", val.getName());
                encoder.writeClassField("type", val.getType());
                Set<Flags> flags = val.getFlags();
                for (Flags flag : flags) {
                    encoder.writeObjectContents(flag);
                }
            }
            
        });
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(ConnectorObject.class,"ConnectorObject") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                ObjectClass objectClass =
                    (ObjectClass)decoder.readObjectField("ObjectClass",ObjectClass.class,null);
                @SuppressWarnings("unchecked")
                Set<? extends Attribute> atts =
                    (Set)decoder.readObjectField("Attributes",Set.class,null);
                return new ConnectorObject(objectClass,atts);
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
                ConnectorObject val = (ConnectorObject)object;
                encoder.writeObjectField("ObjectClass", val.getObjectClass(),true);
                encoder.writeObjectField("Attributes", val.getAttributes(),true);
            }
                
        });
        HANDLERS.add(
                
                new AbstractObjectSerializationHandler(Name.class,"Name") {
                    public Object deserialize(ObjectDecoder decoder)  {
                        String val = decoder.readStringContents();
                        return new Name(val);
                    }
                    public void serialize(Object object, ObjectEncoder encoder)
                    {
                        Name val = (Name)object;
                        encoder.writeStringContents(val.getNameValue());
                    }
            });
        

        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(ObjectClass.class,"ObjectClass") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                String type =
                    decoder.readStringField("type",null);
                return new ObjectClass(type);
            }
            public void serialize(Object object, ObjectEncoder encoder)
            {
               ObjectClass val = (ObjectClass)object;
               encoder.writeStringField("type", val.getObjectClassValue());
            }
        });
                
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(ObjectClassInfo.class,"ObjectClassInfo") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                
                String type = 
                    decoder.readStringField("type",null);
                boolean container =
                    decoder.readBooleanField("container", false);
                
                @SuppressWarnings("unchecked")
                Set<AttributeInfo> attrInfo =
                    (Set)decoder.readObjectField("AttributeInfos",Set.class,null);
                
                
                return new ObjectClassInfo(type,attrInfo,container);
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
                ObjectClassInfo val = (ObjectClassInfo)object;
                
                encoder.writeStringField("type",val.getType());
                encoder.writeBooleanField("container", val.isContainer());
                encoder.writeObjectField("AttributeInfos", val.getAttributeInfo(),true);
            }
                
        });

        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(Schema.class,"Schema") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                @SuppressWarnings("unchecked")
                Set<ObjectClassInfo> objectClasses =
                    (Set)decoder.readObjectField("ObjectClassInfos",Set.class,null);
                Map<String,ObjectClassInfo> objectClassesByName = new HashMap<String,ObjectClassInfo>();
                for (ObjectClassInfo info : objectClasses) {
                    objectClassesByName.put(info.getType(), info);
                }
                @SuppressWarnings("unchecked")
                Set<OperationOptionInfo> operationOptions =
                    (Set)decoder.readObjectField("OperationOptionInfos",Set.class,null);
                Map<String,OperationOptionInfo> optionsByName = new HashMap<String,OperationOptionInfo>();
                for (OperationOptionInfo info : operationOptions) {
                    optionsByName.put(info.getName(), info);
                }
                @SuppressWarnings("unchecked")
                Map<Class<? extends APIOperation>,Set<String>>
                   objectClassNamesByOperation =
                       (Map)decoder.readObjectField("objectClassesByOperation",null,null);
                @SuppressWarnings("unchecked")
                Map<Class<? extends APIOperation>,Set<String>>
                optionsNamesByOperation =
                       (Map)decoder.readObjectField("optionsByOperation",null,null);
                Map<Class<? extends APIOperation>,Set<ObjectClassInfo>>
                objectClassesByOperation = new HashMap<Class<? extends APIOperation>,Set<ObjectClassInfo>>();
                for (Map.Entry<Class<? extends APIOperation>, Set<String>> 
                entry : objectClassNamesByOperation.entrySet()) {
                    Set<String> names = entry.getValue();
                    Set<ObjectClassInfo> infos = new HashSet<ObjectClassInfo>();
                    for (String name : names) {
                        ObjectClassInfo objectClass = objectClassesByName.get(name);
                        if ( objectClass != null ) {
                            infos.add(objectClass);
                        }
                    }
                    objectClassesByOperation.put(entry.getKey(), infos);
                }
                
                Map<Class<? extends APIOperation>,Set<OperationOptionInfo>>
                optionsByOperation = new HashMap<Class<? extends APIOperation>,Set<OperationOptionInfo>>();
                for (Map.Entry<Class<? extends APIOperation>, Set<String>> 
                entry : optionsNamesByOperation.entrySet()) {
                    Set<String> names = entry.getValue();
                    Set<OperationOptionInfo> infos = new HashSet<OperationOptionInfo>();
                    for (String name : names) {
                        OperationOptionInfo info = optionsByName.get(name);
                        if ( info != null ) {
                            infos.add(info);
                        }
                    }
                    optionsByOperation.put(entry.getKey(), infos);
                }
                return new Schema(objectClasses,
                        operationOptions,
                        objectClassesByOperation,
                        optionsByOperation);
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
            {
                Schema val = (Schema)object;
                encoder.writeObjectField("ObjectClassInfos", val.getObjectClassInfo(),true);
                encoder.writeObjectField("OperationOptionInfos", val.getOperationOptionInfo(),true);
                
                Map<Class<? extends APIOperation>,Set<String>>
                objectClassNamesByOperation = new HashMap<Class<? extends APIOperation>,Set<String>>();
                
                Map<Class<? extends APIOperation>,Set<String>>
                optionNamesByOperation = new HashMap<Class<? extends APIOperation>,Set<String>>();
                
                for (Map.Entry<Class<? extends APIOperation>, Set<ObjectClassInfo>> 
                entry : val.getSupportedObjectClassesByOperation().entrySet()) {
                    Set<ObjectClassInfo> value = entry.getValue();
                    Set<String> names = new HashSet<String>();
                    for (ObjectClassInfo info : value) {
                        names.add(info.getType());
                    }
                    objectClassNamesByOperation.put(entry.getKey(), names);
                }
                for (Map.Entry<Class<? extends APIOperation>, Set<OperationOptionInfo>> 
                entry : val.getSupportedOptionsByOperation().entrySet()) {
                    Set<OperationOptionInfo> value = entry.getValue();
                    Set<String> names = new HashSet<String>();
                    for (OperationOptionInfo info : value) {
                        names.add(info.getName());
                    }
                    optionNamesByOperation.put(entry.getKey(), names);
                }
                
                encoder.writeObjectField("objectClassesByOperation",objectClassNamesByOperation,false);
                encoder.writeObjectField("optionsByOperation",optionNamesByOperation,false);
            }
                
        });
        
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(Uid.class,"Uid") {
                public Object deserialize(ObjectDecoder decoder)  {
                    String val = decoder.readStringContents();
                    return new Uid(val);
                }
                public void serialize(Object object, ObjectEncoder encoder)
                {
                    Uid val = (Uid)object;
                    encoder.writeStringContents(val.getUidValue());
                }
        });
        HANDLERS.add(
                
                new AbstractObjectSerializationHandler(Script.class,"Script") {
                
                public Object deserialize(ObjectDecoder decoder)  {
                    ScriptBuilder builder = new ScriptBuilder();
                    builder.setScriptLanguage(decoder.readStringField("scriptLanguage",null));
                    //don't used string field - don't want it to be an attribute
                    builder.setScriptText((String)decoder.readObjectField("scriptText",String.class,null));
                    return builder.build();
                }
                
                public void serialize(Object object, ObjectEncoder encoder)
                {
                    Script val = (Script)object;
                    encoder.writeStringField("scriptLanguage", val.getScriptLanguage());
                    encoder.writeObjectField("scriptText", val.getScriptText(),true);
                }
                
                });
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(ScriptContext.class,"ScriptContext") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                String scriptLanguage =
                    decoder.readStringField("scriptLanguage",null);
                @SuppressWarnings("unchecked")
                Map<String,Object> arguments =
                    (Map<String,Object>)decoder.readObjectField("scriptArguments",null,null);
                //don't used string field - don't want it to be an attribute
                String scriptText =
                    (String)decoder.readObjectField("scriptText",String.class,null);
                return new ScriptContext(scriptLanguage,scriptText,arguments);
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
            {
                ScriptContext val = (ScriptContext)object;
                encoder.writeStringField("scriptLanguage", val.getScriptLanguage());
                encoder.writeObjectField("scriptArguments", val.getScriptArguments(),false);
                encoder.writeObjectField("scriptText", val.getScriptText(),true);
            }
                
        });
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(OperationOptions.class,"OperationOptions") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                @SuppressWarnings("unchecked")
                Map<String,Object> options =
                    (Map<String,Object>)decoder.readObjectField("options",null,null);
                return new OperationOptions(options);
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
            {
                OperationOptions val = (OperationOptions)object;
                encoder.writeObjectField("options", val.getOptions(),false);
            }
                
        });
        HANDLERS.add(
                
                new AbstractObjectSerializationHandler(OperationOptionInfo.class,"OperationOptionInfo") {
                
                public Object deserialize(ObjectDecoder decoder)  {
                    String name = decoder.readStringField("name",null);
                    Class<?> type = decoder.readClassField("type",Class.class);
                    return new OperationOptionInfo(name,type);
                }
        
                public void serialize(Object object, ObjectEncoder encoder)
                {
                    OperationOptionInfo val = (OperationOptionInfo)object;
                    encoder.writeStringField("name", val.getName());
                    encoder.writeClassField("type", val.getType());
                }
                    
            });
        HANDLERS.add( new EnumSerializationHandler(SyncDeltaType.class,
        "SyncDeltaType") );
        
        HANDLERS.add(
                
                new AbstractObjectSerializationHandler(SyncToken.class,"SyncToken") {
                
                public Object deserialize(ObjectDecoder decoder)  {
                    Object value = decoder.readObjectField("value",null,null);
                    return new SyncToken(value);
                }
        
                public void serialize(Object object, ObjectEncoder encoder)
                {
                    SyncToken val = (SyncToken)object;
            		encoder.writeObjectField("value", val.getValue(), false);
                }
                    
            });
        HANDLERS.add(
                
                new AbstractObjectSerializationHandler(SyncDelta.class,"SyncDelta") {
                
                public Object deserialize(ObjectDecoder decoder)  {
                    SyncDeltaBuilder builder = new SyncDeltaBuilder();
                    builder.setDeltaType((SyncDeltaType)decoder.readObjectField("SyncDeltaType",SyncDeltaType.class,null));
                    builder.setToken((SyncToken)decoder.readObjectField("SyncToken",SyncToken.class,null));
                    builder.setPreviousUid((Uid)decoder.readObjectField("PreviousUid",Uid.class,null));
                    builder.setUid((Uid)decoder.readObjectField("Uid",Uid.class,null));
                    builder.setObject((ConnectorObject)decoder.readObjectField("ConnectorObject",ConnectorObject.class,null));
                    return builder.build();
                }
        
                public void serialize(Object object, ObjectEncoder encoder)
                {
                    SyncDelta val = (SyncDelta)object;
                    encoder.writeObjectField("SyncDeltaType", val.getDeltaType(), true);
                    encoder.writeObjectField("SyncToken", val.getToken(), true);
                    encoder.writeObjectField("PreviousUid", val.getPreviousUid(), true);
                    encoder.writeObjectField("Uid", val.getUid(), true);
                    encoder.writeObjectField("ConnectorObject", val.getObject(), true);
                }
                    
            });
        HANDLERS.add(
                
                new AbstractObjectSerializationHandler(QualifiedUid.class,"QualifiedUid") {
                
                public Object deserialize(ObjectDecoder decoder)  {
                    ObjectClass objectClass = (ObjectClass)decoder.readObjectField("ObjectClass", ObjectClass.class,null);
                    Uid uid = (Uid)decoder.readObjectField("Uid",Uid.class,null);
                    return new QualifiedUid(objectClass,uid);
                }
        
                public void serialize(Object object, ObjectEncoder encoder)
                {
                    QualifiedUid val = (QualifiedUid)object;
                    encoder.writeObjectField("ObjectClass", val.getObjectClass(), true);
                    encoder.writeObjectField("Uid", val.getUid(), true);
                }
                    
            });
    
    }
}
