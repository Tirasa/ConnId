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
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;

/**
 * Simple class for common results handler components that involve
 * {@link OperationOptions#OP_ATTRIBUTES_TO_GET}.
 */
public abstract class AttributesToGetResultsHandler {

    private final String[] attrsToGet;

    /**
     * Keep the attribute to get..
     */
    public AttributesToGetResultsHandler(final String[] attributesToGet) {
        Assertions.nullCheck(attributesToGet, "attrsToGet");
        this.attrsToGet = attributesToGet;
    }

    /**
     * Simple method that clones the object and remove the attribute thats are
     * not in the {@link OperationOptions#OP_ATTRIBUTES_TO_GET} set.
     *
     * @param attributesToGet
     * case insensitive set of attribute names.
     */
    public Set<Attribute> reduceToAttrsToGet(Set<Attribute> attributesToGet) {
        Set<Attribute> ret = new HashSet<>(attrsToGet.length);
        Map<String, Attribute> map = AttributeUtil.toMap(attributesToGet);
        for (String attrName : attrsToGet) {
            Attribute attr = map.get(attrName);
            // TODO: Should we throw if the attribute is not yet it was requested?? Or do we ignore because the API
            // maybe asking for what the resource doesn't have??
            Optional.ofNullable(attr).ifPresent(ret::add);
        }
        return ret;
    }

    public ConnectorObject reduceToAttrsToGet(ConnectorObject obj) {
        // clone the object and reduce the attributes only the set of attributes.
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid(obj.getUid());
        bld.setName(obj.getName());
        bld.setObjectClass(obj.getObjectClass());
        Set<Attribute> objAttrs = obj.getAttributes();
        Set<Attribute> attrs = reduceToAttrsToGet(objAttrs);
        bld.addAttributes(attrs);
        return bld.build();
    }
}
