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
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.spi.SearchResultsHandler;

public class SearchResultsHandlerLoggingProxy implements SearchResultsHandler {
	
	private static final Log HANDLER_LOG = Log.getLog(SearchResultsHandler.class);
	
	private SearchResultsHandler origHandler;

	public SearchResultsHandlerLoggingProxy(SearchResultsHandler origHandler) {
		super();
		this.origHandler = origHandler;
	}

	public SearchResultsHandler getOrigHandler() {
		return origHandler;
	}

	@Override
	public boolean handle(ConnectorObject connectorObject) {
		HANDLER_LOG.log(SearchResultsHandler.class, "handle", SpiOperationLoggingUtil.LOG_LEVEL, 
				"Enter: handle("+connectorObject+")", null);
		try {
			boolean ret = origHandler.handle(connectorObject);
			HANDLER_LOG.log(SearchResultsHandler.class, "handle", SpiOperationLoggingUtil.LOG_LEVEL, 
					"Return: "+ret, null);
			return ret;
		} catch (RuntimeException e) {
			SpiOperationLoggingUtil.logOpException(HANDLER_LOG, SearchResultsHandler.class,"handle",e);
			throw e;
		}
	}

	@Override
	public void handleResult(SearchResult result) {
		HANDLER_LOG.log(SearchResultsHandler.class, "handleResult", SpiOperationLoggingUtil.LOG_LEVEL, 
				"Enter: handleResult("+result+")", null);
		try {
			origHandler.handleResult(result);
			HANDLER_LOG.log(SearchResultsHandler.class, "handleResult", SpiOperationLoggingUtil.LOG_LEVEL, 
					"Return", null);
		} catch (RuntimeException e) {
			SpiOperationLoggingUtil.logOpException(HANDLER_LOG, SearchResultsHandler.class,"handleResult",e);
			throw e;
		}
	}

}
