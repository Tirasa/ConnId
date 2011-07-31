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
package org.identityconnectors.framework.spi.operations;

import java.util.Set;

import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;


/**
 * The developer of a Connector should implement either this interface or the
 * {@link UpdateAttributeValuesOp} interface if the Connector will allow an authorized
 * caller to update (i.e., modify or replace) objects on the target resource.
 * <p>
 * This update method is simpler to implement than {link UpdateAttributeValuesOp},
 * which must handle any of several different types of update that the caller
 * may specify. However a true implementation of {link UpdateAttributeValuesOp}
 * offers better performance and atomicity semantics.
 * 
 * @author Will Droste
 * @version $Revision $
 * @since 1.0
 */
public interface UpdateOp extends SPIOperation {
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
     *            the type of object to modify. Will never be null.
     * @param uid
     *            the uid of the object to modify. Will never be null.
     * @param replaceAttributes
     *            set of new {@link Attribute}. the values in this set
     *            represent the new, merged values to be applied to the object. 
     *            This set may also include {@link OperationalAttributes operational attributes}. 
     *            Will never be null.
     * @param options
     *            additional options that impact the way this operation is run.
     *            Will never be null.
     * @return the {@link Uid} of the updated object in case the update changes
     *            the formation of the unique identifier.
     * @throws UnknownUidException
     *            iff the {@link Uid} does not exist on the resource.
     */
    public Uid update(ObjectClass objclass,
            Uid uid,
            Set<Attribute> replaceAttributes,
            OperationOptions options);
}
