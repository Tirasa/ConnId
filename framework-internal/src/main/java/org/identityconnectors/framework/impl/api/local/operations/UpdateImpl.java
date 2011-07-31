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
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;


/**
 * Handles both version of update this include simple replace and the advance
 * update.
 */
public class UpdateImpl extends ConnectorAPIOperationRunner implements
        org.identityconnectors.framework.api.operations.UpdateApiOp {

    /**
     * Determines which type of update a connector supports and then uses that
     * handler.
     */
    public UpdateImpl(final ConnectorOperationalContext context,
            final Connector connector) {
        super(context, connector);
    }

    /**
     * All the operational attributes that can not be added or deleted.
     */
    static Set<String> OPERATIONAL_ATTRIBUTE_NAMES = new HashSet<String>();
    static {
        OPERATIONAL_ATTRIBUTE_NAMES.addAll(OperationalAttributes
                .getOperationalAttributeNames());
        OPERATIONAL_ATTRIBUTE_NAMES.add(Name.NAME);
    };

    public Uid update(final ObjectClass objclass,
            Uid uid,
            Set<Attribute> replaceAttributes,
            OperationOptions options) {
        // validate all the parameters..
        validateInput(objclass,uid,replaceAttributes,false);
        //cast null as empty
        if ( options == null ) {
            options = new OperationOptionsBuilder().build();
        }

        final ObjectNormalizerFacade normalizer =
            getNormalizer(objclass);
        uid = (Uid)normalizer.normalizeAttribute(uid);
        replaceAttributes =
            normalizer.normalizeAttributes(replaceAttributes);
        UpdateOp op = (UpdateOp)getConnector();
        Uid ret = op.update(objclass, uid, replaceAttributes, options);
        return (Uid)normalizer.normalizeAttribute(ret);
    }
    
    public Uid addAttributeValues(ObjectClass objclass,
            Uid uid,
            Set<Attribute> valuesToAdd,
            OperationOptions options) {
        // validate all the parameters..
        validateInput(objclass,uid,valuesToAdd,true);
        //cast null as empty
        if ( options == null ) {
            options = new OperationOptionsBuilder().build();
        }
        
        final ObjectNormalizerFacade normalizer =
            getNormalizer(objclass);
        uid = (Uid)normalizer.normalizeAttribute(uid);
        valuesToAdd =
            normalizer.normalizeAttributes(valuesToAdd);
        UpdateOp op = (UpdateOp)getConnector();
        Uid ret;
        if ( op instanceof UpdateAttributeValuesOp ) {
            UpdateAttributeValuesOp valueOp =
                (UpdateAttributeValuesOp)op;
            ret = valueOp.addAttributeValues(objclass, uid, valuesToAdd, options);
        }
        else {
            Set<Attribute> replaceAttributes =
                fetchAndMerge(objclass,uid,valuesToAdd,true,options);
            ret = op.update(objclass, uid, replaceAttributes, options);
        }
        return (Uid)normalizer.normalizeAttribute(ret);
    }
    
    public Uid removeAttributeValues(ObjectClass objclass,
            Uid uid,
            Set<Attribute> valuesToRemove,
            OperationOptions options) {
        // validate all the parameters..
        validateInput(objclass,uid,valuesToRemove,true);
        //cast null as empty
        if ( options == null ) {
            options = new OperationOptionsBuilder().build();
        }
        
        final ObjectNormalizerFacade normalizer =
            getNormalizer(objclass);
        uid = (Uid)normalizer.normalizeAttribute(uid);
        valuesToRemove =
            normalizer.normalizeAttributes(valuesToRemove);
        UpdateOp op = (UpdateOp)getConnector();
        Uid ret;
        if ( op instanceof UpdateAttributeValuesOp ) {
            UpdateAttributeValuesOp valueOp =
                (UpdateAttributeValuesOp)op;
            ret = valueOp.removeAttributeValues(objclass, uid, valuesToRemove, options);
        }
        else {
            Set<Attribute> replaceAttributes =
                fetchAndMerge(objclass,uid,valuesToRemove,false,options);
            ret = op.update(objclass, uid, replaceAttributes, options);
        }
        return (Uid)normalizer.normalizeAttribute(ret);
    }
    
    private Set<Attribute> fetchAndMerge(ObjectClass objclass, Uid uid, 
            Set<Attribute> valuesToChange, 
            boolean add,
            OperationOptions options)
    {
        // check that this connector supports Search..
        if (!(getConnector() instanceof SearchOp)) {
            final String MSG = "Connector must support: " + SearchOp.class;
            throw new UnsupportedOperationException(MSG);
        }
        
        //add attrs to get to operation options, so that the
        //object we fetch has exactly the set of attributes we require
        //(there may be ones that are not in the default set)
        OperationOptionsBuilder builder = new OperationOptionsBuilder(options);
        Set<String> attrNames = new HashSet<String>();
        for (Attribute attribute : valuesToChange) {
            attrNames.add(attribute.getName());
        }
        builder.setAttributesToGet(attrNames);
        options = builder.build();
        
        // get the connector object from the resource...
        ConnectorObject o = getConnectorObject(objclass, uid, options);
        if (o == null) {
            throw new UnknownUidException(uid, objclass);
        }
        // merge the update data..
        Set<Attribute> mergeAttrs = merge(valuesToChange, o.getAttributes(),add);
        return mergeAttrs;
    }

    /**
     * Merges two connector objects into a single updated object.
     */
    public Set<Attribute> merge(Set<Attribute> updateAttrs,
            Set<Attribute> baseAttrs, boolean add) {
        // return the merged attributes
        Set<Attribute> ret = new HashSet<Attribute>();
        // create map that can be modified to get the subset of changes 
        Map<String, Attribute> baseAttrMap = AttributeUtil.toMap(baseAttrs);
        // run through attributes of the current object..
        for (final Attribute updateAttr : updateAttrs) {
            // get the name of the update attributes
            String name = updateAttr.getName();
            // remove each attribute that is an update attribute..
            Attribute baseAttr = baseAttrMap.get(name);
            List<Object> values;
            final Attribute modifiedAttr; 
            if (add) {
                if (baseAttr == null) {
                    modifiedAttr = updateAttr;
                } else {
                    // create a new list with the base attribute to add to..
                    values = CollectionUtil.newList(baseAttr.getValue());
                    values.addAll(updateAttr.getValue());
                    modifiedAttr = AttributeBuilder.build(name, values);
                }
            } 
            else {
                if (baseAttr == null) {
                    // nothing to actually do the attribute do not exist
                    continue;                    
                } else {
                    // create a list with the base attribute to remove from..
                    values = CollectionUtil.newList(baseAttr.getValue());
                    for (Object val : updateAttr.getValue()) {
                        values.remove(val);
                    }
                    // if the values are empty send a null to the connector..
                    if (values.isEmpty()) {
                        modifiedAttr = AttributeBuilder.build(name);
                    } else {
                        modifiedAttr = AttributeBuilder.build(name, values);
                    }
                }
            } 
            ret.add(modifiedAttr);
        }
        return ret;
    }

    /**
     * Get the {@link ConnectorObject} to modify.
     */
    private ConnectorObject getConnectorObject(ObjectClass oclass, Uid uid, OperationOptions options) {
        // attempt to get the connector object..
        GetApiOp get = new GetImpl(new SearchImpl(getOperationalContext(),
                getConnector()));
        return get.getObject(oclass, uid, options);
    }

    /**
     * Makes things easier if you can trust the input.
     */
    public static void validateInput(final ObjectClass objclass,
            final Uid uid,
            final Set<Attribute> attrs, boolean isDelta) {
        final String OPERATIONAL_ATTRIBUTE_ERR = 
            "Operational attribute '%s' can not be added or removed.";
        Assertions.nullCheck(uid, "uid");
        Assertions.nullCheck(objclass, "objclass");
        Assertions.nullCheck(attrs, "attrs");
        // check to make sure there's not a uid..
        if (AttributeUtil.getUidAttribute(attrs) != null) {
            throw new IllegalArgumentException(
                    "Parameter 'attrs' contains a uid.");
        }
        // check for things only valid during ADD/DELETE
        if (isDelta) {
            for (Attribute attr : attrs) {
                Assertions.nullCheck(attr, "attr");
                // make sure that none of the values are null..
                if (attr.getValue() == null) {
                    throw new IllegalArgumentException(
                            "Can not add or remove a 'null' value.");
                }
                // make sure that if this an delete/add that it doesn't include
                // certain attributes because it doesn't make any sense..
                String name = attr.getName();
                if (OPERATIONAL_ATTRIBUTE_NAMES.contains(name)) {
                    String msg = String.format(OPERATIONAL_ATTRIBUTE_ERR, name);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
    }
}
