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
package org.identityconnectors.framework.impl.api;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;

public class StreamHandlerUtil {
    
   /**
    * Adapts from a ObjectStreamHandler to a ResultsHandler
    */
   private static class ResultsHandlerAdapter implements ResultsHandler {
       private final ObjectStreamHandler _target;
       public ResultsHandlerAdapter(ObjectStreamHandler target) {
           _target = target;
       }
       public boolean handle(final ConnectorObject obj) {
           return _target.handle(obj);
       }
   }
   
   /**
    * Adapts from a ObjectStreamHandler to a SyncResultsHandler
    */
   private static class SyncResultsHandlerAdapter implements SyncResultsHandler {
       private final ObjectStreamHandler _target;
       public SyncResultsHandlerAdapter(ObjectStreamHandler target) {
           _target = target;
       }
       public boolean handle(final SyncDelta obj) {
           return _target.handle(obj);
       }
   }
   
   /**
    * Adapts from a ObjectStreamHandler to a SyncResultsHandler
    */
   private static class ObjectStreamHandlerAdapter implements ObjectStreamHandler {
       private final Class<?> _targetInterface;
       private final Object _target;
       public ObjectStreamHandlerAdapter(Class<?> targetInterface, Object target) {
           Assertions.nullCheck(targetInterface, "targetInterface");
           Assertions.nullCheck(target, "target");
           if (!targetInterface.isInstance(target)) {
               throw new IllegalArgumentException("Target" +targetInterface+" "+target);
           }
           if (!isAdaptableToObjectStreamHandler(targetInterface)) {
               throw new IllegalArgumentException("Target interface not supported: "+targetInterface);
           }
           _targetInterface = targetInterface;
           _target = target;
       }
       public boolean handle(final Object obj) {
           if (_targetInterface == ResultsHandler.class ) {
               return ((ResultsHandler)_target).handle((ConnectorObject)obj);
           }
           else if (_targetInterface == SyncResultsHandler.class ) {
               return ((SyncResultsHandler)_target).handle((SyncDelta)obj);
           }
           else {
               throw new UnsupportedOperationException("Unhandled case: "+_targetInterface);
           }
       }
   }
    
   public static boolean isAdaptableToObjectStreamHandler(Class<?> clazz) {
       return ( ResultsHandler.class.isAssignableFrom(clazz) ||
          SyncResultsHandler.class.isAssignableFrom(clazz));
   }
   
   public static ObjectStreamHandler adaptToObjectStreamHandler(Class<?> interfaceType,
           Object target) {
       return new ObjectStreamHandlerAdapter(interfaceType,target);
   }
   public static Object adaptFromObjectStreamHandler(Class<?> interfaceType,
           ObjectStreamHandler target) {
       if (interfaceType == ResultsHandler.class ) {
           return new ResultsHandlerAdapter(target);
       }
       else if (interfaceType == SyncResultsHandler.class ) {
           return new SyncResultsHandlerAdapter(target);
       }
       else {
           throw new UnsupportedOperationException("Unhandled case: "+interfaceType);
       }       
   }
   
}
