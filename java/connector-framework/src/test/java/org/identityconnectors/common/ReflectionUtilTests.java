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
package org.identityconnectors.common;

import static org.identityconnectors.common.ReflectionUtil.containsInterface;
import static org.identityconnectors.common.ReflectionUtil.getAllInterfaces;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;

public class ReflectionUtilTests {

    @Test
    public void functional() {
        // getInterfaces
        Set<Class<?>> set = getAllInterfaces(C1.class);
        assertTrue(set.contains(I1.class));
        assertTrue(set.contains(I2.class));
        assertFalse(set.contains(I3.class));
        // containsInterface
        assertTrue(containsInterface(C1.class, I1.class));
        assertTrue(containsInterface(C1.class, I2.class));
        assertFalse(containsInterface(C1.class, I3.class));
    }

    // =======================================================================
    // Helper Classes/Interfaces
    // =======================================================================
    interface I1 {

    }

    interface I2 {

    }

    interface I3 {

    }

    class C1 implements I1, I2 {

    }

    interface I5 extends I1 {

    }

    class C2 implements I5 {

    }
}
