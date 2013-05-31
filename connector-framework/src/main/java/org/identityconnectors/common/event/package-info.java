/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/**
 * NOTICE: This package is an early specification of the Events API for 1.2.x.x version.
 * Use carefully, this package may change bifore the final 1.2.0.0 release.
 * <p/>
 * Classes and interfaces for asynchronous event based monitoring of the connectors.
 * The parent application can use these the check if a remote connector server status
 * has been changed or connectors has be added/removed.
 *
 * @author Laszlo Hordos
 * @since 1.2
 */
package org.identityconnectors.common.event;
