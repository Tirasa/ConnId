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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.identityconnectors.framework.spi.operations.SPIOperation;


/**
 * The {@link Configuration} interface is traversed through reflection. This
 * annotation provides a way to override the default configuration operation for
 * each property.
 * 
 * @author Will Droste
 * @version $Revision: 1.1 $
 * @since 1.0
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigurationProperty {
    
    /**
     * Order in which this property is displayed.
     */
    public int order() default -1;

    /**
     * Change the default help message key.
     */
    public String helpMessageKey() default "";

    /**
     * Change the default display message key.
     */
    public String displayMessageKey() default "";
    
    /**
     * Is this a confidential property whose value should be encrypted by
     * the application when persisted?
     */
    public boolean confidential() default false;
    
    /**
     * Is this property required?
     * @return True iff the property is required
     */
    public boolean required() default false;
    
    /**
     * List of operations for which this property must be specified.
     * This is used for the case where a connector may or may not
     * implement certain operations depending in the configuration.
     * The default value of "empty array" is special in that
     * it means that this property is applicable to all operations.
     */
    public Class<? extends SPIOperation> [] operations() default {};
}
