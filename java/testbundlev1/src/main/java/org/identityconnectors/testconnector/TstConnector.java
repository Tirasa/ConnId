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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.testconnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfo.RoleInReference;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObjectReference;
import org.identityconnectors.framework.common.objects.LightweightObjectClassInfo;
import org.identityconnectors.framework.common.objects.LightweightObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.LiveSyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.LiveSyncResultsHandler;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.framework.spi.SyncTokenResultsHandler;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.LiveSyncOp;
import org.identityconnectors.framework.spi.operations.PartialSchemaOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.testcommon.TstCommon;

@ConnectorClass(
        displayNameKey = "TestConnector",
        categoryKey = "TestConnector.category",
        configurationClass = TstConnectorConfig.class)
public class TstConnector implements CreateOp, PoolableConnector, SchemaOp, SearchOp<String>, SyncOp, LiveSyncOp,
        PartialSchemaOp {

    public static final String USER_CLASS_NAME = "user";

    public static final String GROUP_CLASS_NAME = "group";

    public static final String ACCESS_CLASS_NAME = "access";

    public static final String MEMBER_OF_ATTR_NAME = "memberOf";

    public static final String MEMBERS_ATTR_NAME = "members";

    public static final String ACCESS_ATTR_NAME = "access";

    public static final String GROUP_ATTR_NAME = "group";

    public static final String GROUP_MEMBERSHIP_REFERENCE_TYPE_NAME = "groupMembership";

    // test objects
    public static final String USER_100_UID = "b2ca2464-8aff-4bc4-9b7f-e68ad27d9f3d";

    public static final String USER_101_UID = "96010a29-aad5-43eb-b583-d5b897e3243c";

    public static final String USER_100_NAME = "user100";

    public static final String USER_101_NAME = "user101";

    public static final String GROUP_1_UID = "0a4be7af-157d-49fc-985f-f782ab4eef5e";

    public static final String GROUP_2_UID = "84fe911a-2e5c-4b67-9423-048e82445961";

    public static final String GROUP_1_NAME = "group1";

    public static final String GROUP_2_NAME = "group2";

    public static final String USER_CLASS_DESCRIPTION =
            "A User object is a digital identity object that represents a single human user or, a non-human agent "
            + "(e.g., service account) authorized to access digital resources.";

    private static final String USER_CLASS_UID_DESCRIPTION = "A unique, immutable identifier for the user.";

    private static final String USER_CLASS_NAME_DESCRIPTION = "A human-readable login name.";

    private static final String USER_CLASS_MEMBER_OF_DESCRIPTION =
            "Unique identifiers of groups represented as a list of memberships for policy inheritance.";

    private static final String USER_CLASS_ACCESS_DESCRIPTION = "Unique identifiers of group access policies, .";

    public static final String GROUP_CLASS_DESCRIPTION =
            "A Group is a logical container object that represents a collection of user accounts or other groups.";

    private static final String GROUP_CLASS_UID_DESCRIPTION = "A unique, immutable identifier for the group.";

    private static final String GROUP_CLASS_NAME_DESCRIPTION = "A human-readable name.";

    private static final String GROUP_CLASS_MEMBERS_ATTR_DESCRIPTION =
            "List of user identifiers or nested group identifiers.";

    private static final String ACCESS_CLASS_DESCRIPTION =
            "This object represents a form of access to a group, either by another group or a user.";

    private static final String ACCESS_CLASS_ATTR_REFERENCE_DESCRIPTION =
            "Reference attribute representing the relationship between a group and a user";

    private static int CONNECTION_COUNT = 0;

    private MyTstConnection myConnection;

    private TstConnectorConfig config;

    public static void checkClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != TstConnector.class.getClassLoader()) {
            throw new IllegalStateException("Unexpected classloader");
        }
    }

    public TstConnector() {
        checkClassLoader();
    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
        checkClassLoader();
        Integer delay = (Integer) options.getOptions().get("delay");
        if (delay != null) {
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
            }
        }
        if (options.getOptions().get("testPooling") != null) {
            return new Uid(String.valueOf(myConnection.getConnectionNumber()));
        } else {
            String version = TstCommon.getVersion();
            return new Uid(version);
        }
    }

    @Override
    public void init(Configuration cfg) {
        checkClassLoader();
        config = (TstConnectorConfig) cfg;
        if (config.getResetConnectionCount()) {
            CONNECTION_COUNT = 0;
        }
        myConnection = new MyTstConnection(CONNECTION_COUNT++);
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public void dispose() {
        checkClassLoader();
        if (myConnection != null) {
            myConnection.dispose();
            myConnection = null;
        }
    }

    @Override
    public void checkAlive() {
        checkClassLoader();
        myConnection.test();
    }

    /**
     * Used by the script tests
     */
    public String concat(String s1, String s2) {
        checkClassLoader();
        return s1 + s2;
    }

    @Override
    public FilterTranslator<String> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        checkClassLoader();
        //no translation - ok since this is just for tests
        return new AbstractFilterTranslator<String>() {
        };
    }

    @Override
    public void executeQuery(ObjectClass objectClass, String query, ResultsHandler handler, OperationOptions options) {
        checkClassLoader();

        if (objectClass.is(USER_CLASS_NAME)) {
            executeUsersQuery(handler);
            return;
        }

        int remaining = config.getNumResults();
        for (int i = 0; i < config.getNumResults(); i++) {
            Integer delay = (Integer) options.getOptions().get("delay");
            if (delay != null) {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                }
            }
            ConnectorObjectBuilder builder =
                    new ConnectorObjectBuilder();
            builder.setUid(Integer.toString(i));
            builder.setName(Integer.toString(i));
            builder.setObjectClass(objectClass);
            for (int j = 0; j < 50; j++) {
                builder.addAttribute("myattribute" + j, "myvaluevaluevalue" + j);
            }

            ConnectorObject rv = builder.build();
            if (handler.handle(rv)) {
                remaining--;
            } else {
                break;
            }
        }

        if (handler instanceof SearchResultsHandler searchResultsHandler) {
            searchResultsHandler.handleResult(new SearchResult("", remaining));
        }
    }

    private void executeUsersQuery(ResultsHandler handler) {
        ConnectorObjectReference user100Ref = createUserIdOnlyReference(USER_100_NAME);
        ConnectorObjectReference user101Ref = createUserIdOnlyReference(USER_101_NAME);

        ConnectorObjectReference group1Ref =
                createGroupFullReference(GROUP_1_UID, GROUP_1_NAME, user100Ref, user101Ref);
        ConnectorObjectReference group2Ref = createGroupFullReference(GROUP_2_UID, GROUP_2_NAME, user100Ref);

        ConnectorObject user100 = createUser(USER_100_UID, USER_100_NAME, group1Ref, group2Ref);
        ConnectorObject user101 = createUser(USER_101_UID, USER_101_NAME, group1Ref);

        if (handler.handle(user100)) {
            handler.handle(user101);
        }
    }

    private ConnectorObjectReference createUserIdOnlyReference(String name) {
        return new ConnectorObjectReference(
                new ConnectorObjectBuilder()
                        .setName(name)
                        .setObjectClass(null) // intentionally not setting object class here
                        .buildIdentification());
    }

    private ConnectorObject createUser(String uid, String name, ConnectorObjectReference... memberOf) {
        return new ConnectorObjectBuilder()
                .setUid(uid)
                .setName(name)
                .setObjectClass(new ObjectClass(USER_CLASS_NAME))
                .addAttribute(MEMBER_OF_ATTR_NAME, List.of(memberOf))
                .build();
    }

    private ConnectorObjectReference createGroupFullReference(
            String uid, String name, ConnectorObjectReference... members) {

        return new ConnectorObjectReference(
                new ConnectorObjectBuilder()
                        .setUid(uid)
                        .setName(name)
                        .setObjectClass(new ObjectClass(GROUP_CLASS_NAME))
                        .addAttribute(MEMBERS_ATTR_NAME, List.of(members))
                        .build());
    }

    @Override
    public void sync(
            ObjectClass objectClass,
            SyncToken token,
            SyncResultsHandler handler,
            OperationOptions options) {

        checkClassLoader();
        int remaining = config.getNumResults();
        for (int i = 0; i < config.getNumResults(); i++) {
            ConnectorObjectBuilder obuilder = new ConnectorObjectBuilder();
            obuilder.setUid(Integer.toString(i));
            obuilder.setName(Integer.toString(i));
            obuilder.setObjectClass(objectClass);

            SyncDeltaBuilder builder = new SyncDeltaBuilder();
            builder.setObject(obuilder.build());
            builder.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
            builder.setToken(new SyncToken("mytoken"));

            SyncDelta rv = builder.build();
            if (!handler.handle(rv)) {
                break;
            }
            remaining--;
        }
        if (handler instanceof SyncTokenResultsHandler syncTokenResultsHandler) {
            syncTokenResultsHandler.handleResult(new SyncToken(remaining));
        }
    }

    @Override
    public SyncToken getLatestSyncToken(ObjectClass objectClass) {
        checkClassLoader();
        return new SyncToken("mylatest");
    }

    @Override
    public void livesync(
            ObjectClass objectClass,
            LiveSyncResultsHandler handler,
            OperationOptions options) {

        checkClassLoader();

        handler.handle(new LiveSyncDeltaBuilder().
                setObject(new ConnectorObjectBuilder().
                        setUid(UUID.randomUUID().toString()).
                        setName(UUID.randomUUID().toString()).
                        setObjectClass(objectClass).
                        build()).
                build());
    }

    @Override
    public Schema schema() {
        checkClassLoader();
        SchemaBuilder schemaBuilder = new SchemaBuilder(TstConnector.class);
        for (int i = 0; i < 2; i++) {
            ObjectClassInfoBuilder classBuilder = new ObjectClassInfoBuilder();
            String type = "class" + i;
            classBuilder.setType(type);
            classBuilder.addAllAttributeInfo(buildAttributeInfos(type));
            schemaBuilder.defineObjectClass(classBuilder.build());
        }
        // Special classes to test object references
        schemaBuilder.defineObjectClass(
                new ObjectClassInfoBuilder()
                        .setType(USER_CLASS_NAME)
                        .setDescription(USER_CLASS_DESCRIPTION)
                        .addAllAttributeInfo(buildAttributeInfos(USER_CLASS_NAME))
                        .build());

        schemaBuilder.defineObjectClass(
                new ObjectClassInfoBuilder()
                        .setType(GROUP_CLASS_NAME)
                        .setDescription(GROUP_CLASS_DESCRIPTION)
                        .addAllAttributeInfo(buildAttributeInfos(GROUP_CLASS_NAME))
                        .build());

        // A bit artificial class to test object references: defines which user
        // has what access (e.g., read, write, ...) to what group
        schemaBuilder.defineObjectClass(
                new ObjectClassInfoBuilder()
                        .setType(ACCESS_CLASS_NAME)
                        .setEmbedded(true)
                        .setDescription(ACCESS_CLASS_DESCRIPTION)
                        .addAllAttributeInfo(buildAttributeInfos(ACCESS_CLASS_NAME))
                        .build());

        return schemaBuilder.build();
    }

    public static ObjectClass userObjectClass() {
        return new ObjectClass(USER_CLASS_NAME);
    }

    // Get only the parts of the schema which are requested by the IAM system.
    @Override
    public Schema getPartialSchema(LightweightObjectClassInfo... objectClassInfos) {

        SchemaBuilder schemaBuilder = new SchemaBuilder(TstConnector.class);
        Iterator<LightweightObjectClassInfo> iterator = Arrays.stream(objectClassInfos).iterator();

        while (iterator.hasNext()) {

            LightweightObjectClassInfo lightweightObjectClassInfo = iterator.next();
            ObjectClassInfoBuilder objectClassInfoBuilder = new ObjectClassInfoBuilder();
            String type = lightweightObjectClassInfo.getType();
            objectClassInfoBuilder.setType(type);
            objectClassInfoBuilder.setDescription(lightweightObjectClassInfo.getDescription());
            objectClassInfoBuilder.setEmbedded(lightweightObjectClassInfo.isEmbedded());
            objectClassInfoBuilder.setAuxiliary(lightweightObjectClassInfo.isAuxiliary());
            objectClassInfoBuilder.setContainer(lightweightObjectClassInfo.isContainer());
            objectClassInfoBuilder.addAllAttributeInfo(buildAttributeInfos(type));
            schemaBuilder.defineObjectClass(objectClassInfoBuilder.build());
        }

        return schemaBuilder.build();
    }

    private Collection<AttributeInfo> buildAttributeInfos(String type) {

        Collection<AttributeInfo> attributeInfos = new ArrayList<>();
        if (null == type) {
            for (int j = 0; j < 200; j++) {
                attributeInfos.add(AttributeInfoBuilder.build("attributename" + j, String.class));
            }
        } else {
            switch (type) {
                case USER_CLASS_NAME -> {
                    attributeInfos.add(
                            new AttributeInfoBuilder(Uid.NAME, String.class)
                                    .setRequired(true)
                                    .setDescription(USER_CLASS_UID_DESCRIPTION)
                                    .build());
                    attributeInfos.add(
                            new AttributeInfoBuilder(Name.NAME, String.class)
                                    .setRequired(true)
                                    .setDescription(USER_CLASS_NAME_DESCRIPTION)
                                    .build());
                    attributeInfos.add(
                            new AttributeInfoBuilder(MEMBER_OF_ATTR_NAME, ConnectorObjectReference.class)
                                    .setReferencedObjectClassName(GROUP_CLASS_NAME)
                                    .setSubtype(GROUP_MEMBERSHIP_REFERENCE_TYPE_NAME)
                                    .setRoleInReference(RoleInReference.SUBJECT.toString())
                                    .setMultiValued(true)
                                    .setDescription(USER_CLASS_MEMBER_OF_DESCRIPTION)
                                    .build());
                    attributeInfos.add(
                            new AttributeInfoBuilder(ACCESS_ATTR_NAME, ConnectorObjectReference.class)
                                    .setReferencedObjectClassName(ACCESS_CLASS_NAME)
                                    .setRoleInReference(RoleInReference.SUBJECT.toString())
                                    .setMultiValued(true)
                                    .setDescription(USER_CLASS_ACCESS_DESCRIPTION)
                                    .build());
                }
                case GROUP_CLASS_NAME -> {
                    attributeInfos.add(
                            new AttributeInfoBuilder(Uid.NAME, String.class)
                                    .setRequired(true)
                                    .setDescription(GROUP_CLASS_UID_DESCRIPTION)
                                    .build());
                    attributeInfos.add(
                            new AttributeInfoBuilder(Name.NAME, String.class)
                                    .setRequired(true)
                                    .setDescription(GROUP_CLASS_NAME_DESCRIPTION)
                                    .build());
                    attributeInfos.add(
                            new AttributeInfoBuilder(MEMBERS_ATTR_NAME, ConnectorObjectReference.class)
                                    .setReferencedObjectClassName(USER_CLASS_NAME)
                                    .setSubtype(GROUP_MEMBERSHIP_REFERENCE_TYPE_NAME)
                                    .setRoleInReference(RoleInReference.OBJECT.toString())
                                    .setMultiValued(true)
                                    .setDescription(GROUP_CLASS_MEMBERS_ATTR_DESCRIPTION)
                                    .build());
                }
                case ACCESS_CLASS_NAME ->
                    attributeInfos.add(
                            new AttributeInfoBuilder(GROUP_ATTR_NAME, ConnectorObjectReference.class)
                                    .setReferencedObjectClassName(GROUP_CLASS_NAME)
                                    .setDescription(ACCESS_CLASS_ATTR_REFERENCE_DESCRIPTION)
                                    .build());
                default -> {
                    for (int j = 0; j < 200; j++) {
                        attributeInfos.add(AttributeInfoBuilder.build("attributename" + j, String.class));
                    }
                }
            }
        }
        return attributeInfos;
    }

    // Provide this to the IAM system to choose the object class information.
    @Override
    public LightweightObjectClassInfo[] getObjectClassInformation() {

        ArrayList<LightweightObjectClassInfo> lightweightObjectClassInfos = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            lightweightObjectClassInfos.add(
                    new LightweightObjectClassInfoBuilder()
                            .setType("class" + i)
                            .setDescription("Object class of the type class" + i)
                            .build()
            );
        }
        lightweightObjectClassInfos.add(
                new LightweightObjectClassInfoBuilder()
                        .setType(USER_CLASS_NAME)
                        .setDescription(USER_CLASS_DESCRIPTION)
                        .build());

        lightweightObjectClassInfos.add(
                new LightweightObjectClassInfoBuilder()
                        .setType(GROUP_CLASS_NAME)
                        .setDescription(GROUP_CLASS_DESCRIPTION)
                        .build());
        lightweightObjectClassInfos.add(
                new LightweightObjectClassInfoBuilder()
                        .setType(ACCESS_CLASS_NAME)
                        .setEmbedded(true)
                        .setDescription(ACCESS_CLASS_DESCRIPTION)
                        .build());

        return lightweightObjectClassInfos.toArray(LightweightObjectClassInfo[]::new);
    }
}
