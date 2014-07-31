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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.identityconnectors.framework.common.exceptions.ConnectorException;

public final class ObjectSerializerRegistry {

    private static final List<ObjectTypeMapper> HANDLERS = new ArrayList<ObjectTypeMapper>();

    private static final Map<String, ObjectTypeMapper> HANDLERS_BY_SERIAL_TYPE =
            new HashMap<String, ObjectTypeMapper>();

    // initialize list of handlers
    static {
        HANDLERS.addAll(Primitives.HANDLERS);
        HANDLERS.addAll(OperationMappings.MAPPINGS);
        HANDLERS.addAll(APIConfigurationHandlers.HANDLERS);
        HANDLERS.addAll(FilterHandlers.HANDLERS);
        HANDLERS.addAll(CommonObjectHandlers.HANDLERS);
        HANDLERS.addAll(MessageHandlers.HANDLERS);
        // object is special - just map the type, but don't actually
        // serialize
        HANDLERS.add(new ObjectTypeMapperImpl(Object.class, "Object"));

        for (ObjectTypeMapper handler : HANDLERS) {
            final ObjectTypeMapper previous =
                    HANDLERS_BY_SERIAL_TYPE.put(handler.getHandledSerialType(), handler);
            if (previous != null) {
                throw new ConnectorException("More than one handler of the" + " same type: "
                        + handler.getHandledSerialType());
            }
        }
    }

    /**
     * Mapping by class. Dynamically built since actual class may be a subclass.
     */
    private static final Map<Class<?>, ObjectTypeMapper> HANDLERS_BY_OBJECT_TYPE = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, ObjectTypeMapper>());

    public static ObjectTypeMapper getMapperBySerialType(final String type) {
        return HANDLERS_BY_SERIAL_TYPE.get(type);
    }

    public static ObjectTypeMapper getMapperByObjectType(final Class<?> clazz) {
        ObjectTypeMapper mapper = HANDLERS_BY_OBJECT_TYPE.get(clazz);
        if (mapper == null) {
            for (ObjectTypeMapper handler : HANDLERS) {
                if (handler.isMatchSubclasses()) {
                    if (handler.getHandledObjectType().isAssignableFrom(clazz)) {
                        mapper = handler;
                        break;
                    }
                } else if (handler.getHandledObjectType().equals(clazz)) {
                    mapper = handler;
                    break;
                }
            }
            HANDLERS_BY_OBJECT_TYPE.put(clazz, mapper);
        }
        return mapper;
    }

    public static ObjectSerializationHandler getHandlerBySerialType(final String type) {
        final ObjectTypeMapper mapper = getMapperBySerialType(type);
        if (mapper instanceof ObjectSerializationHandler) {
            return (ObjectSerializationHandler) mapper;
        } else {
            return null;
        }
    }

    public static ObjectSerializationHandler getHandlerByObjectType(final Class<?> clazz) {
        final ObjectTypeMapper mapper = getMapperByObjectType(clazz);
        if (mapper instanceof ObjectSerializationHandler) {
            return (ObjectSerializationHandler) mapper;
        } else {
            return null;
        }
    }

    private ObjectSerializerRegistry() {
        // empty constructor for static utility class
    }
}
