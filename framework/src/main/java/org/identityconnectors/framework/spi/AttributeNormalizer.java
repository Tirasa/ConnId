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
package org.identityconnectors.framework.spi;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

/**
 * Interface to be implemented by connectors that need
 * to normalize certain attributes. This might, for
 * example, be used to normalize whitespace within 
 * DN's to ensure consistent filtering whether that
 * filtering is natively on the resource or by the
 * connector framework. For connectors implementing
 * this interface, the method {@link #normalizeAttribute(ObjectClass, Attribute)}
 * will be applied to each of the following:
 * <ol>
 *    <li>The filter passed to {@link SearchOp}.</li>
 *    <li>The results returned from {@link SearchOp}.</li>
 *    <li>The results returned from {@link SyncOp}.</li>
 *    <li>The attributes passed to {@link UpdateAttributeValuesOp}.</li>
 *    <li>The <code>Uid</code> returned from {@link UpdateAttributeValuesOp}.</li>
 *    <li>The attributes passed to {@link UpdateOp}.</li>
 *    <li>The <code>Uid</code> returned from {@link UpdateOp}.</li>
 *    <li>The attributes passed to {@link CreateOp}.</li>
 *    <li>The <code>Uid</code> returned from {@link CreateOp}.</li>
 *    <li>The <code>Uid</code> passed to {@link org.identityconnectors.framework.spi.operations.DeleteOp}.</li>
 * </ol>
 */
public interface AttributeNormalizer 
{
    public Attribute normalizeAttribute(ObjectClass oclass, Attribute attribute);
}
