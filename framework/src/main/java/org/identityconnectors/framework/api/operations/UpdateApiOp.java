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
package org.identityconnectors.framework.api.operations;

import java.util.Set;

import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

/**
 * Updates a {@link ConnectorObject}. This operation
 * is supported for those connectors that implement
 * either {@link UpdateOp} or the more advanced
 * {@link UpdateAttributeValuesOp}.
 */
public interface UpdateApiOp extends APIOperation {

    /**
     * Update the object specified by the {@link ObjectClass} and {@link Uid}, 
     * replacing the current values of each attribute with the values
     * provided.
     * <p>
     * For each input attribute, replace
     * all of the current values of that attribute in the target object with
     * the values of that attribute.
     * <p>
     * If the target object does not currently contain an attribute that the
     * input set contains, then add this
     * attribute (along with the provided values) to the target object.
     * <p>
     * If the value of an attribute in the input set is
     * {@code null}, then do one of the following, depending on
     * which is most appropriate for the target:
     * <ul>
     * <li>If possible, <em>remove</em> that attribute from the target
     * object entirely.</li>
     * <li>Otherwise, <em>replace all of the current values</em> of that
     * attribute in the target object with a single value of
     * {@code null}.</li>
     * </ul>
     * @param objclass
     *            the type of object to modify. Must not be null.
     * @param uid
     *            the uid of the object to modify. Must not be null.
     * @param replaceAttributes
     *            set of new {@link Attribute}. the values in this set
     *            represent the new, merged values to be applied to the object. 
     *            This set may also include {@link OperationalAttributes operational attributes}. 
     *            Must not be null.
     * @param options
     *            additional options that impact the way this operation is run.
     *            May be null.
     * @return the {@link Uid} of the updated object in case the update changes
     *            the formation of the unique identifier.
     * @throws UnknownUidException
     *            iff the {@link Uid} does not exist on the resource.
     */
    public Uid update(ObjectClass objclass,
            Uid uid,
            Set<Attribute> replaceAttributes,
            OperationOptions options);
    
    /**
     * Update the object specified by the {@link ObjectClass} and {@link Uid}, 
     * adding to the current values of each attribute the values provided.
     * <p>
     * For each attribute that the input set contains, add to
     * the current values of that attribute in the target object all of the
     * values of that attribute in the input set.
     * <p>
     * NOTE that this does not specify how to handle duplicate values. 
     * The general assumption for an attribute of a {@code ConnectorObject} 
     * is that the values for an attribute may contain duplicates. 
     * Therefore, in general simply <em>append</em> the provided values 
     * to the current value for each attribute.
     * <p>
     * IMPLEMENTATION NOTE: for connectors that merely implement {@link UpdateOp}
     * and not {@link UpdateAttributeValuesOp} this method will be simulated by 
     * fetching, merging, and calling 
     * {@link UpdateOp#update(ObjectClass, Uid, Set, OperationOptions)}. Therefore,
     * connector implementations are encourage to implement {@link UpdateAttributeValuesOp}
     * from a performance and atomicity standpoint.
     * @param objclass
     *            the type of object to modify. Must not be null.
     * @param uid
     *            the uid of the object to modify. Must not be null.
     * @param valuesToAdd
     *            set of {@link Attribute} deltas. The values for the attributes
     *            in this set represent the values to add to attributes in the object.
     *            merged. This set must not include {@link OperationalAttributes operational attributes}. 
     *            Must not be null.
     * @param options
     *            additional options that impact the way this operation is run.
     *            May be null.
     * @return the {@link Uid} of the updated object in case the update changes
     *            the formation of the unique identifier.
     * @throws UnknownUidException
     *            iff the {@link Uid} does not exist on the resource.
     */
    public Uid addAttributeValues(ObjectClass objclass,
            Uid uid,
            Set<Attribute> valuesToAdd,
            OperationOptions options);
    
    /**
     * Update the object specified by the {@link ObjectClass} and {@link Uid}, 
     * removing from the current values of each attribute the values provided.
     * <p>
     * For each attribute that the input set contains, 
     * remove from the current values of that attribute in the target object 
     * any value that matches one of the values of the attribute from the input set.
     * <p>
     * NOTE that this does not specify how to handle unmatched values. 
     * The general assumption for an attribute of a {@code ConnectorObject}
     * is that the values for an attribute are merely <i>representational state</i>.
     * Therefore, the implementer should simply ignore any provided value
     * that does not match a current value of that attribute in the target
     * object. Deleting an unmatched value should always succeed.
     * <p>
     * IMPLEMENTATION NOTE: for connectors that merely implement {@link UpdateOp}
     * and not {@link UpdateAttributeValuesOp} this method will be simulated by 
     * fetching, merging, and calling 
     * {@link UpdateOp#update(ObjectClass, Uid, Set, OperationOptions)}. Therefore,
     * connector implementations are encourage to implement {@link UpdateAttributeValuesOp}
     * from a performance and atomicity standpoint.
     * @param objclass
     *            the type of object to modify. Must not be null.
     * @param uid
     *            the uid of the object to modify. Must not be null.
     * @param valuesToRemove
     *            set of {@link Attribute} deltas. The values for the attributes
     *            in this set represent the values to remove from attributes in the object.
     *            merged. This set must not include {@link OperationalAttributes operational attributes}. 
     *            Must not be null.
     * @param options
     *            additional options that impact the way this operation is run.
     *            May be null.
     * @return the {@link Uid} of the updated object in case the update changes
     *            the formation of the unique identifier.
     * @throws UnknownUidException
     *            iff the {@link Uid} does not exist on the resource.
     */
    public Uid removeAttributeValues(ObjectClass objclass,
            Uid uid,
            Set<Attribute> valuesToRemove,
            OperationOptions options);

}
