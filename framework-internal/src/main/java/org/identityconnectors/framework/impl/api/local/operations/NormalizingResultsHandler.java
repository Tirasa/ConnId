/**
 * ====================
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2011-2013 Tirasa. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License"). You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at https://oss.oracle.com/licenses/CDDL.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;

public class NormalizingResultsHandler implements ResultsHandler {
    
    private final ResultsHandler _target;
    private final ObjectNormalizerFacade _normalizer;
    
    public NormalizingResultsHandler(ResultsHandler target,
            ObjectNormalizerFacade normalizer) {
        Assertions.nullCheck(target, "target");
        Assertions.nullCheck(normalizer, "normalizer");
        _target = target;
        _normalizer = normalizer;
    }
    

    public boolean handle(ConnectorObject obj) {
        ConnectorObject normalized = _normalizer.normalizeObject(obj);
        return _target.handle(normalized);
    }

}
