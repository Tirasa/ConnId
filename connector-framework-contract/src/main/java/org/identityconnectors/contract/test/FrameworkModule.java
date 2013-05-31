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

package org.identityconnectors.contract.test;

import org.identityconnectors.contract.data.DataProvider;
import org.identityconnectors.contract.test.ConnectorHelper;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorKey;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * A NAME does ...
 *
 * @author Laszlo Hordos
 */
public class FrameworkModule extends AbstractModule {

    private final DataProvider dataProvider;

    public FrameworkModule(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    protected void configure() {
        bind(DataProvider.class).annotatedWith(Names.named("DataProvider")).toInstance(dataProvider);
    }

    @Provides
    @Singleton
    public ConnectorKey providesConnectorKey() {
        return new ConnectorKey("bundle", "1.1.0.0", "connectorName");
    }

    @Provides
    @Singleton
    public ConnectorInfoManager providesConnectorInfoManager() {
        return ConnectorHelper.getInfoManager(dataProvider);
    }

    @Provides
    @Singleton
    @Inject
    public APIConfiguration providesAPIConfiguration(ConnectorInfoManager connectorInfoManager) {
        return ConnectorHelper.getDefaultConfigurationProperties(dataProvider, connectorInfoManager);
    }

    @Provides
    @Singleton
    public ConnectorFacade providesConnectorFacade() {
        return ConnectorHelper.createConnectorFacade(dataProvider);
    }

    @Provides
    public DataProvider providesDataProvider() {
        return ConnectorHelper.createDataProvider();
    }

}
