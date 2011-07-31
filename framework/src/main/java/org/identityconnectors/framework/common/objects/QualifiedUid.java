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
package org.identityconnectors.framework.common.objects;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * A fully-qualified uid. That is, a pair of {@link ObjectClass} and
 * {@link Uid}.
 */
public final class QualifiedUid {
    private final ObjectClass _objectClass;
    private final Uid _uid;
    
    /**
     * Create a QualifiedUid.
     * @param objectClass The object class. May not be null.
     * @param uid The uid. May not be null.
     */
    public QualifiedUid(ObjectClass objectClass,
            Uid uid) {
        Assertions.nullCheck(objectClass,"objectClass");
        Assertions.nullCheck(uid,"uid");
        _objectClass = objectClass;
        _uid = uid;
    }
    
    /**
     * Returns the object class.
     * @return The object class.
     */
    public ObjectClass getObjectClass() {
        return _objectClass;
    }
    
    /**
     * Returns the uid.
     * @return The uid.
     */
    public Uid getUid() {
        return _uid;
    }
    
    /**
     * Returns true iff o is a QualifiedUid and the object class and uid match.
     */
    @Override
    public boolean equals(Object o) {
        if ( o instanceof QualifiedUid ) {
            QualifiedUid other = (QualifiedUid)o;
            return ( _objectClass.equals(other._objectClass) &&
                     _uid.equals(other._uid) );
        }
        return false;
    }
    
    /**
     * Returns a hash code based on uid
     */
    @Override
    public int hashCode() {
        return _uid.hashCode();
    }
    
    /**
     * Returns a string representation acceptible for debugging.
     */
    @Override
    public String toString() {
        return SerializerUtil.serializeXmlObject(this, false);
    }
    
}
