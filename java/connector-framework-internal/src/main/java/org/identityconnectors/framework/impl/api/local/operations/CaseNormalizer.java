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
 * Portions Copyrighted 2013 Evolveum
 */
package org.identityconnectors.framework.impl.api.local.operations;

import java.util.ArrayList;
import java.util.List;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.spi.AttributeNormalizer;

/**
 * @author mederly
 */
public class CaseNormalizer implements AttributeNormalizer {

    public Attribute normalizeAttribute(ObjectClass oclass, Attribute attribute) {
        Attribute rv = attribute;
        boolean converted = false;

        List<Object> values = rv.getValue();
        if (values != null) {
            List<Object> newValues = new ArrayList<Object>();
            for (Object value : values) {
                if (value instanceof String) {
                    newValues.add(((String) value).toUpperCase());
                    converted = true;
                } else {
                    newValues.add(value);
                }
            }
            if (converted) { // only when something changed; to save a few cpu cycles...
                rv = AttributeBuilder.build(attribute.getName(), newValues);
            }
        }
        return rv;
    }
}
