/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2025 Evolveum. All rights reserved.
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

package org.identityconnectors.contract.test;
import org.identityconnectors.framework.api.operations.*;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Contract test of {@link PartialSchemaApiOp} operation.
 */
public class PartialSchemaApiOpTests extends ContractTestBase {


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> s = new HashSet<>();
        // list of required operations by this test:
        s.add(PartialSchemaApiOp.class);
        return s;
    }


    /**
     * Basic test case.
     */
    @Test
    protected void testBasic() {

        LightweightObjectClassInfo[] lightweightObjectClassInfos =  getConnectorFacade().getObjectClassInformation();
        assertNotNull(lightweightObjectClassInfos, "Null Object class information found");

        Iterator<LightweightObjectClassInfo> iterator =  Arrays.stream(lightweightObjectClassInfos).iterator();

        Schema schema = null;
        LightweightObjectClassInfo lightweightObjectClassInfo = null;

        if (iterator.hasNext()){

            lightweightObjectClassInfo = iterator.next();

            assertNotNull(lightweightObjectClassInfo, "Null object class information produced");
            schema = getConnectorFacade().getPartialSchema(lightweightObjectClassInfo);

        }

        assertNotNull(schema, "Null schema produced");

        Set<ObjectClassInfo> objectClassInfos = schema.getObjectClassInfo();
        Iterator<ObjectClassInfo> infoIterator = objectClassInfos.iterator();

        assertTrue(objectClassInfos.size() ==1);
        assertTrue(lightweightObjectClassInfo.is(infoIterator.next().getType()));


    }

}
