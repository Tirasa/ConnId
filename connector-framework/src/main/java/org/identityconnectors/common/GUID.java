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

import java.rmi.dgc.VMID;

/**
 * Create a globally unique identifier.
 *
 * @author Will Droste
 * @since 1.0
 */
public class GUID {
    private final VMID vmid;

    public GUID() {
        vmid = new VMID();
    }

    /**
     * Get the string version of the VMID.
     *
     * @return a string representation of the object.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return vmid.toString().toUpperCase();
    }

    /**
     * Get the hashcode of the VMID.
     *
     * @return a hash code value for this object.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return vmid.hashCode();
    }

    /**
     * Returns true if the value parameter is a {@link GUID} and the VMID is
     * equal.
     *
     * @param o
     *            the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument;
     *         <code>false</code> otherwise.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        boolean ret = false;
        if (o instanceof GUID) {
            ret = vmid.equals(((GUID) o).vmid);
        }
        return ret;
    }
}
