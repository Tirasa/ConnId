/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Evolveum. All rights reserved.
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
package org.identityconnectors.framework.spi.operations;

import java.util.Set;

import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * <p>
 * Proper implementation of {@link UpdateOp} and {@link UpdateAttributeValuesOp}.
 * This operation updates the values in relative or absolute way. Modification
 * of each attribute is described by {@link AttributeDelta}.
 * </p>
 * <p>
 * This operation is meant as a complete replacement of {@link UpdateOp} and
 * {@link UpdateAttributeValuesOp} that were poorly designed in the early
 * framework versions. The old operations have not provided any practical
 * way how to execute complex attribute changes in an efficient or atomic
 * way. The presence of a single operation in {@link UpdateOp} could not
 * support any kind of relative changes. The two operations added in
 * {@link UpdateAttributeValuesOp} did not really solve the problem either.
 * The fact that there are two operations made it impossible to implement
 * all scenarios due to operation ordering. E.g. invoking remove operation
 * first and add operation second will end up with an error when working
 * with mandatory multi-value attribute. On the other hand invoking add
 * operation first and remove operation second will cause issues with
 * attributes that represent some limited entitlements (e.g. groups that
 * may be assigned only once). Also the fact that the old update operations
 * returns just the Uid make it very hard to implement an efficient and
 * reliable connector. E.g. it is a common case that the name of the object
 * changes when some of the attributes are changed. The old update operations
 * provided no way how to indicate this change even if the connector was aware
 * of the change.
 * </p>
 *
 * @author Radovan Semancik
 * @since 1.4.3
 */
public interface UpdateDeltaOp extends SPIOperation {

    /**
     * <p>
     * Update the object specified by the {@link ObjectClass} and {@link Uid},
     * modifying the values according to the attribute deltas.
     * </p>
     * <p>
     * The connector is supposed to return side-effect changes as a return value
     * from this operation. E.g. if the modification of some of the
     * attributes changed other attribute then these changes should be returned.
     * The connector must return a new value of primary identifier (Uid) if it is
     * changed. But the connector should return other changes only if the connector
     * has an efficient way how to detect them. Connector is not supposed to return
     * all side-effect changes if it does not know about them or if additional
     * operation is required to fetch them.
     * </p>
     *
     * @param objclass
     * the type of object to modify. Will never be null.
     * @param uid
     * the uid of the object to modify. Will never be null.
     * @param modifications
     * set of attribute deltas. Each delta describes modification
     * of one attribute. Each attribute will be in the set at most once.
     * The set will never be null.
     * @param options
     * additional options that impact the way this operation is run.
     * Will never be null.
     * @return the set of modifications that were a side-effect of the primary modifications
     * specified in the modifications parameters.
     * @throws org.identityconnectors.framework.common.exceptions.UnknownUidException
     * if the {@link Uid} does not exist on the resource.
     * @throws org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException
     * if any of the specified values is not appropriate for the attribute,
     * if the delta execution would result in violation of the schema,
     * if the result would be missing mandatory attribute and in similar cases.
     */
    public Set<AttributeDelta> updateDelta(ObjectClass objclass, Uid uid, Set<AttributeDelta> modifications,
            OperationOptions options);

}
