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

/**
 * The type of change.
 */
public enum SyncDeltaType {
    /**
     * The change represents either a create or an update in
     * the resource. These are combined into a single value because:
     * <ol>
     *    <li>Many resources will not be able to distinguish a create from an update.
     *    Those that have an audit log will be able to. However, many implementations
     *    will only have the current record and a modification timestamp.</li>
     *    <li>Regardless of whether or not the resource can distinguish the two cases,
     *    the application needs to distinguish. </li>
     * </ol>
     */
    CREATE_OR_UPDATE, 
    
    /**
     * The change represents a DELETE in the resource
     */
    DELETE
}
