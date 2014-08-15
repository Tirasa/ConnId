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
package org.identityconnectors.framework.impl.api;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;

public class ResultHandlerLoggingProxy implements ResultsHandler {
	
	private static final Log LOG = Log.getLog(ResultHandlerLoggingProxy.class);
	
	private ResultsHandler origHandler;

	public ResultHandlerLoggingProxy(ResultsHandler origHandler) {
		this.origHandler = origHandler;
	}

	public ResultsHandler getOrigHandler() {
		return origHandler;
	}

	@Override
	public boolean handle(ConnectorObject connectorObject) {
		LOG.log(ResultsHandler.class, "handle", LoggingProxy.LOG_LEVEL, "Enter: " + connectorObject, null);
		try {
			boolean ret = origHandler.handle(connectorObject);
			LOG.log(ResultsHandler.class, "handle", LoggingProxy.LOG_LEVEL, "Return: " + ret, null);
			return ret;
		} catch (RuntimeException e) {
			LOG.log(ResultsHandler.class, "handle", LoggingProxy.LOG_LEVEL, "Exception: ", e);
			throw e;
		}
	}

}
