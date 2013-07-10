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
package org.identityconnectors.contract.data;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;

import java.net.URL;
import java.text.MessageFormat;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

final class GroovyConfigReader {
	private GroovyConfigReader(){}

    
    static ConfigObject loadResourceConfiguration(String prefix, ClassLoader loader){
		String cfg = System.getProperty("testConfig", null);
		URL url = loader.getResource(prefix + "/config/config.groovy");
		ConfigObject co = null;
		ConfigSlurper cs = new ConfigSlurper();
		if(url != null){
			co = mergeConfigObjects(co, cs.parse(url));
		}
		if (StringUtil.isNotBlank(cfg) && !"default".equals(cfg)) {
		    url = loader.getResource(prefix + "/config/" + cfg + "/config.groovy");
		    if(url != null){
		    	co = mergeConfigObjects(co, cs.parse(url));
		    }
		}
		url = loader.getResource(prefix + "/config-private/config.groovy");
		if (url != null){
		    co = mergeConfigObjects(co, cs.parse(url));
		}
		if (StringUtil.isNotBlank(cfg) && !"default".equals(cfg)) {
		    url = loader.getResource(prefix + "/config-private/" + cfg + "/config.groovy");
		    if(url != null){
		    	co = mergeConfigObjects(co, cs.parse(url));
		    }
		}
		if(co == null || co.flatten().isEmpty()){
		    throw new ConnectorException(MessageFormat.format("No properties read from classpath with prefix [{0}] ",prefix));
		}
		return co;
    	
    }
    
    

    static ConfigObject mergeConfigObjects(ConfigObject lowPriorityCO, ConfigObject highPriorityCO) {
    	if(lowPriorityCO != null){
    		return highPriorityCO != null ? (ConfigObject) lowPriorityCO.merge(highPriorityCO) : lowPriorityCO;
    	}
    	return highPriorityCO;
    }

}
