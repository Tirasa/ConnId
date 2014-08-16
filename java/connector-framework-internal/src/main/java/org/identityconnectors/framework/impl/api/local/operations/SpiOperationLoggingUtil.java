/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Evolveum. All rights reserved.
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
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.framework.spi.operations.SPIOperation;

public class SpiOperationLoggingUtil {

	static final Log.Level LOG_LEVEL = Log.Level.OK;

	public static void logOpException(Log opLog, Class<?> opClass, String methodName, RuntimeException e) {
		if (!opLog.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL)) {
			return;
		}
		opLog.log(opClass, methodName, SpiOperationLoggingUtil.LOG_LEVEL, "Exception: ", e);
	}
	
}
