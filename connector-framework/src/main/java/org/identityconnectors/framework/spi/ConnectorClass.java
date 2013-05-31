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
package org.identityconnectors.framework.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation must be present on each connector class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConnectorClass {

    /**
     * The configuration class for a given connector.
     */
    Class<? extends Configuration> configurationClass();

    /**
     * The display name key. This must be a key in the message catalog.
     */
    String displayNameKey();

    /**
     * Category the connector belongs to such as 'LDAP' or 'DB'.
     */
    String categoryKey() default "";

    /**
     * The resource path(s) to the message catalog. Message catalogs are
     * searched in the order given such that the first one wins. By default, if
     * no paths are specified, we use
     * <code>connector-package.Messages.properties</code>
     */
    String[] messageCatalogPaths() default {};

}
