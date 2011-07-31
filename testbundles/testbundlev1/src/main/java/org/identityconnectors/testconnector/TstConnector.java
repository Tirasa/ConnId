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
package org.identityconnectors.testconnector;

import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.testcommon.TstCommon;

@ConnectorClass(
    displayNameKey="TestConnector",
    configurationClass=TstConnectorConfig.class)
public class TstConnector implements CreateOp, PoolableConnector, SchemaOp, SearchOp<String>, SyncOp {
   
    private static int _connectionCount = 0;
    private MyTstConnection _myConnection;
    private TstConnectorConfig _config;
    
    public static void checkClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != 
            TstConnector.class.getClassLoader()) {
            throw new IllegalStateException("Unexpected classloader");
        }
    }

    public TstConnector() {
        checkClassLoader();
    }

    public Uid create(ObjectClass oclass, Set<Attribute> attrs, OperationOptions options) {
        checkClassLoader();
        Integer delay = (Integer)options.getOptions().get("delay");
        if ( delay != null ) {
            try { Thread.sleep(delay.intValue()); } catch (Exception e) {}
        }
        if ( options.getOptions().get("testPooling") != null) {
            return new Uid(String.valueOf(_myConnection.getConnectionNumber()));
        }
        else {
            String version = TstCommon.getVersion();
            return new Uid(version);
        }
    }
    public void init(Configuration cfg) {
        checkClassLoader();
        _config = (TstConnectorConfig)cfg;
        if (_config.getResetConnectionCount()) {
            _connectionCount = 0;
        }
        _myConnection = new MyTstConnection(_connectionCount++);
    }
    public Configuration getConfiguration() {
        return _config;
    }

    public void dispose() {
        checkClassLoader();
        if (_myConnection != null) {
            _myConnection.dispose();
            _myConnection = null;
        }
    }

    public void checkAlive() {
        checkClassLoader();
        _myConnection.test();
    }

    /**
     * Used by the script tests
     */
    public String concat(String s1, String s2) {
        checkClassLoader();
        return s1+s2;
    }

    public FilterTranslator<String> createFilterTranslator(ObjectClass oclass, OperationOptions options) {
         checkClassLoader();
         //no translation - ok since this is just for tests
         return new AbstractFilterTranslator<String>(){};
    }
    public void executeQuery(ObjectClass oclass, String query, ResultsHandler handler, OperationOptions options) {
        checkClassLoader();
        for (int i = 0; i < _config.getNumResults(); i++ ) {
            Integer delay = (Integer)options.getOptions().get("delay");
            if ( delay != null ) {
                try { Thread.sleep(delay.intValue()); } catch (Exception e) {}
            }
            ConnectorObjectBuilder builder =
                new ConnectorObjectBuilder();
            builder.setUid(Integer.toString(i));
            builder.setName(Integer.toString(i));
            builder.setObjectClass(oclass);
            for ( int j = 0; j < 50; j++ ) {
                builder.addAttribute("myattribute"+j,"myvaluevaluevalue"+j);
            }
        
            ConnectorObject rv = builder.build();
            if (!handler.handle(rv)) {
                break;
            }
        }
    }
    public void sync(ObjectClass objClass, SyncToken token,
                     SyncResultsHandler handler,
                     OperationOptions options) {
        checkClassLoader();
        for (int i = 0; i < _config.getNumResults(); i++ ) {
            ConnectorObjectBuilder obuilder =
                new ConnectorObjectBuilder();
            obuilder.setUid(Integer.toString(i));
            obuilder.setName(Integer.toString(i));
            obuilder.setObjectClass(objClass);

            SyncDeltaBuilder builder =
                new SyncDeltaBuilder();
            builder.setObject(obuilder.build());
            builder.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
            builder.setToken(new SyncToken("mytoken"));

            SyncDelta rv = builder.build();
            if (!handler.handle(rv)) {
                break;
            }
        }
    }
    public SyncToken getLatestSyncToken(ObjectClass objClass) {
        checkClassLoader();
        return new SyncToken("mylatest");
    }

    public Schema schema() {
        checkClassLoader();
        SchemaBuilder builder = new SchemaBuilder(TstConnector.class);
        for ( int i = 0 ; i < 2; i++ ) {
            ObjectClassInfoBuilder classBuilder = new ObjectClassInfoBuilder();
            classBuilder.setType("class"+i);
            for ( int j = 0; j < 200; j++) {
                classBuilder.addAttributeInfo(AttributeInfoBuilder.build("attributename"+j, String.class));
            }            
            builder.defineObjectClass(classBuilder.build());
        }
        return builder.build();
    }
}
