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
import java.util.List;

import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.remote.messages.EchoMessage;
import org.identityconnectors.framework.impl.api.remote.messages.HelloRequest;
import org.identityconnectors.framework.impl.api.remote.messages.HelloResponse;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequest;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequestMoreData;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequestStopData;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponseEnd;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePart;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePause;


/**
 * Serialization handles for remote messages
 */
class MessageHandlers {
    
    public static final List<ObjectTypeMapper> HANDLERS =
        new ArrayList<ObjectTypeMapper>();
    

    
    static { 
        
        
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(HelloRequest.class,"HelloRequest") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                return new HelloRequest();
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
            }
            
        });
    
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(HelloResponse.class,"HelloResponse") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                Throwable exception =
                    (Throwable)decoder.readObjectField("exception",null,null);
                @SuppressWarnings("unchecked")
                List<RemoteConnectorInfoImpl> connectorInfos =
                    (List)decoder.readObjectField("ConnectorInfos",List.class,null);
                
                return new HelloResponse(exception,connectorInfos);
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
                HelloResponse val = (HelloResponse)object;
                encoder.writeObjectField("exception", val.getException(), false);
                encoder.writeObjectField("ConnectorInfos", val.getConnectorInfos(), true);
            }
            
        });
        
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(OperationRequest.class,"OperationRequest") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                ConnectorKey connectorKey = 
                    (ConnectorKey)decoder.readObjectField("ConnectorKey",ConnectorKey.class,null);
                APIConfigurationImpl configuration =
                    (APIConfigurationImpl)decoder.readObjectField("APIConfiguration",APIConfigurationImpl.class,null);
                @SuppressWarnings("unchecked")
                Class<? extends APIOperation> operation = 
                    (Class)decoder.readClassField("operation",null);
                String operationMethodName =
                    decoder.readStringField("operationMethodName", null);
                @SuppressWarnings("unchecked")
                List<Object> arguments = (List)
                    decoder.readObjectField("Arguments",List.class,null);
                return new OperationRequest(
                        connectorKey,
                        configuration,
                        operation,
                        operationMethodName,
                        arguments);
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
                OperationRequest val = 
                    (OperationRequest)object;
                encoder.writeClassField("operation", 
                        val.getOperation());
                encoder.writeStringField("operationMethodName", 
                        val.getOperationMethodName());
                encoder.writeObjectField("ConnectorKey", 
                        val.getConnectorKey(),true);
                encoder.writeObjectField("APIConfiguration", 
                        val.getConfiguration(),true);
                encoder.writeObjectField("Arguments", 
                        val.getArguments(),true);
            }
            
        });
        
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(OperationResponseEnd.class,"OperationResponseEnd") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                return new OperationResponseEnd();
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
            }
            
        });
        
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(OperationResponsePart.class,"OperationResponsePart") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                Throwable exception =
                    (Throwable)decoder.readObjectField("exception",null,null);
                Object result =
                    decoder.readObjectField("result",null,null);
                
                return new OperationResponsePart(exception,result);
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
                OperationResponsePart val = (OperationResponsePart)object;
                encoder.writeObjectField("exception", val.getException(),false);
                encoder.writeObjectField("result", val.getResult(),false);
            }
            
        });
        
        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(OperationRequestMoreData.class,"OperationRequestMoreData") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                return new OperationRequestMoreData();
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
            }
            
        });

        HANDLERS.add(
                
            new AbstractObjectSerializationHandler(OperationRequestStopData.class,"OperationRequestStopData") {
            
            public Object deserialize(ObjectDecoder decoder)  {
                return new OperationRequestStopData();
            }
    
            public void serialize(Object object, ObjectEncoder encoder)
                     {
            }
            
        });

        HANDLERS.add(
                
                new AbstractObjectSerializationHandler(OperationResponsePause.class,"OperationResponsePause") {
                
                public Object deserialize(ObjectDecoder decoder)  {
                    return new OperationResponsePause();
                }
        
                public void serialize(Object object, ObjectEncoder encoder)
                         {
                }
                
            });
        HANDLERS.add(
                
                new AbstractObjectSerializationHandler(EchoMessage.class,"EchoMessage") {
                
                public Object deserialize(ObjectDecoder decoder)  {
                    return new EchoMessage(decoder.readObjectField("value",null,null),
                            (String)decoder.readObjectField("objectXml", String.class, null));
                }
        
                public void serialize(Object object, ObjectEncoder encoder)
                         {
                    EchoMessage val = (EchoMessage)object;
                    encoder.writeObjectField("value",val.getObject(),false);
                    encoder.writeObjectField("objectXml", val.getXml(), true);
                }
                
            });


    }
}
