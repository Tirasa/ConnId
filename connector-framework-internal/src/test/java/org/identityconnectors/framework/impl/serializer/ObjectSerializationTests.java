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
 *
 * Portions Copyrighted 2011-2013 ForgeRock
 */

package org.identityconnectors.framework.impl.serializer;


import org.testng.Assert;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.common.script.Script;
import org.identityconnectors.common.script.ScriptBuilder;
import org.identityconnectors.common.security.GuardedByteArray;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectionBrokenException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.ConnectorSecurityException;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.exceptions.InvalidPasswordException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.exceptions.PasswordExpiredException;
import org.identityconnectors.framework.common.exceptions.PermissionDeniedException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptionInfo;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.QualifiedUid;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.objects.ScriptContextBuilder;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.AttributeInfo.Flags;
import org.identityconnectors.framework.common.objects.filter.AndFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.NotFilter;
import org.identityconnectors.framework.common.objects.filter.OrFilter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertiesImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertyImpl;
import org.identityconnectors.framework.impl.api.ConnectorMessagesImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.remote.messages.EchoMessage;
import org.identityconnectors.framework.impl.api.remote.messages.HelloRequest;
import org.identityconnectors.framework.impl.api.remote.messages.HelloResponse;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequest;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequestMoreData;
import org.identityconnectors.framework.impl.api.remote.messages.OperationRequestStopData;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponseEnd;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePart;
import org.identityconnectors.framework.impl.api.remote.messages.OperationResponsePause;


public class ObjectSerializationTests {

    @Test
    public void testBoolean() throws Exception {
        Boolean v1 = true;
        Boolean v2 = (Boolean)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = false;
        v2 = (Boolean)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testCharacter() throws Exception {
        Character v1 = 'h';
        Character v2 = (Character)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testInteger() throws Exception {
        Integer v1 = 12345;
        Integer v2 = (Integer)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Integer.MIN_VALUE;
        v2 = (Integer)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Integer.MAX_VALUE;
        v2 = (Integer)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = -1;
        v2 = (Integer)cloneObject(v1);
        assertEquals(v1, v2);
   }

    @Test
    public void testLong() throws Exception {
        Long v1 = 12345L;
        Long v2 = (Long)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Long.MIN_VALUE;
        v2 = (Long)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Long.MAX_VALUE;
        v2 = (Long)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = -1L;
        v2 = (Long)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testFloat() throws Exception {
        Float v1 = 1.1F;
        Float v2 = (Float)cloneObject(v1);
        assertTrue(v1!=v2);
        assertEquals(v1, v2);

        v1 = Float.MAX_VALUE;
        v2 = (Float)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = -Float.MAX_VALUE;
        v2 = (Float)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Float.MIN_VALUE;
        v2 = (Float)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Float.NaN;
        v2 = (Float)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Float.NEGATIVE_INFINITY;
        v2 = (Float)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Float.POSITIVE_INFINITY;
        v2 = (Float)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testDouble() throws Exception {
        Double v1 = 1.1;
        Double v2 = (Double)cloneObject(v1);
        assertTrue(v1!=v2);
        assertEquals(v1, v2);

        v1 = Double.MAX_VALUE;
        v2 = (Double)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = -Double.MAX_VALUE;
        v2 = (Double)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Double.MIN_VALUE;
        v2 = (Double)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Double.NaN;
        v2 = (Double)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Double.NEGATIVE_INFINITY;
        v2 = (Double)cloneObject(v1);
        assertEquals(v1, v2);

        v1 = Double.POSITIVE_INFINITY;
        v2 = (Double)cloneObject(v1);
        assertEquals(v1, v2);

    }

    @Test
    public void testString() throws Exception {
        String v1 = "abcd";
        String v2 = (String)cloneObject(v1);
        assertTrue(v1!=v2);
        assertEquals(v1, v2);
    }

    @Test
    public void testURI() throws Exception {
        URI v1 = new URI("mailto:java-net@java.sun.com");
        URI v2 = (URI)cloneObject(v1);
        assertTrue(v1!=v2);
        assertEquals(v1, v2);
    }

    @Test
    public void testFile() throws Exception {
        File v1 = new File("c:/foo.txt");
        File v2 = (File)cloneObject(v1);
        assertTrue(v1!=v2);
        assertEquals(v1, v2);
    }

    @Test
    public void testBigInteger() throws Exception {
        BigInteger v1 = new BigInteger("983423442347324324324");
        BigInteger v2 = (BigInteger)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testBigDecimal() throws Exception {
        BigDecimal v1 = new BigDecimal(new BigInteger("9847324324324"),45);
        BigDecimal v2 = (BigDecimal)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testByteArray() throws Exception {
        byte [] v1 = {1,2,3};
        byte [] v2 = (byte[])cloneObject(v1);
        assertEquals(3, v2.length);
        assertEquals(1,v2[0]);
        assertEquals(2,v2[1]);
        assertEquals(3,v2[2]);
    }

    @Test
    public void testClasses() throws Exception {
        assertEquals(int.class,
                cloneObject(int.class));
        assertEquals(int[].class,
                cloneObject(int[].class));
        assertEquals(int[][][].class,
                cloneObject(int[][][].class));
        assertEquals(long.class,
                cloneObject(long.class));
        assertEquals(float.class,
                cloneObject(float.class));
        assertEquals(double.class,
                cloneObject(double.class));
        assertEquals(char.class,
                cloneObject(char.class));
        //if this fails, we have added a new type and we need to add
        //a serializer for it
        assertEquals(FrameworkUtil.getAllSupportedConfigTypes(),
                cloneObject(FrameworkUtil.getAllSupportedConfigTypes()));
        //if this fails, we have added a new type and we need to add
        //a serializer for it
        assertEquals(FrameworkUtil.getAllSupportedAttributeTypes(),
                cloneObject(FrameworkUtil.getAllSupportedAttributeTypes()));
        //if this fails, need to add to the OperationMappings class
        assertEquals(FrameworkUtil.allAPIOperations(),
                cloneObject(FrameworkUtil.allAPIOperations()));
    }

    @Test
    public void testArrays() throws Exception {
        int [] v1 = {1,2,3};
        int [] v2 = (int[])cloneObject(v1);
        assertEquals(3, v2.length);
        assertEquals(1,v2[0]);
        assertEquals(2,v2[1]);
        assertEquals(3,v2[2]);
    }


    @Test
    public void testObjectArrays() throws Exception {
        Object [] v1 = {"1","2","3"};
        Object [] v2 = (Object[])cloneObject(v1);
        assertEquals(3, v2.length);
        assertEquals("1",v2[0]);
        assertEquals("2",v2[1]);
        assertEquals("3",v2[2]);
    }

    @Test
    public void testMap() throws Exception {
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        Map<?,?> map2 = (Map<?,?>)cloneObject(map);
        assertEquals(map, map2);
    }

    @Test
    public void testCaseInsensitiveMap() {
        Set<Attribute> set = new HashSet<Attribute>();
        set.add(AttributeBuilder.build("foo1"));
        set.add(AttributeBuilder.build("foo2"));
        Map<String,Attribute> map = AttributeUtil.toMap(set);
        assertTrue(map.containsKey("Foo1"));
        assertTrue(map.containsKey("Foo2"));
        @SuppressWarnings("unchecked")
        Map<String,Attribute> map2 = (Map)cloneObject(map);
        assertTrue(map2.containsKey("Foo1"));
        assertTrue(map2.containsKey("Foo2"));
    }

    @Test
    public void testList() throws Exception {
        List<Object> v1 = new ArrayList<Object>();
        v1.add("val1");
        v1.add("val2");
        List<?> v2 = (List<?>)cloneObject(v1);
        assertEquals(v1,v2);
    }

    @Test
    public void testSet() throws Exception {
        Set<Object> v1 = new HashSet<Object>();
        v1.add("val1");
        v1.add("val2");
        Set<?> v2 = (Set<?>)cloneObject(v1);
        assertEquals(v1,v2);
    }

    @Test
    public void testCaseInsensitiveSet() {
        Set<String> v1 = CollectionUtil.newCaseInsensitiveSet();
        v1.add("foo");
        v1.add("foo2");
        Set<?> v2 = (Set<?>)cloneObject(v1);
        assertTrue(v2.contains("Foo"));
        assertTrue(v2.contains("Foo2"));
    }

    @Test
    public void testLocale() throws Exception {
        Locale v1 = new Locale("no","NO","NY");
        Locale v2 = (Locale)cloneObject(v1);
        assertEquals(v1,v2);

        v1 = new Locale("");
        v2 = (Locale)cloneObject(v1);
        assertEquals(v1,v2);
    }


    @Test
    public void testConnectionPoolingConfiguration() {
        ObjectPoolConfiguration v1 = new ObjectPoolConfiguration();
        v1.setMaxObjects(1);
        v1.setMaxIdle(2);
        v1.setMaxWait(3);
        v1.setMinEvictableIdleTimeMillis(4);
        v1.setMinIdle(5);

        ObjectPoolConfiguration v2 =
            (ObjectPoolConfiguration)cloneObject(v1);
        assertTrue(v1 != v2);

        assertEquals(v1,v2);
        assertEquals(1, v2.getMaxObjects());
        assertEquals(2, v2.getMaxIdle());
        assertEquals(3, v2.getMaxWait());
        assertEquals(4, v2.getMinEvictableIdleTimeMillis());
        assertEquals(5, v2.getMinIdle());
    }

    @Test
    public void testConfigurationProperty() {
        ConfigurationPropertyImpl v1 = new ConfigurationPropertyImpl();
        v1.setOrder(1);
        v1.setConfidential(true);
        v1.setRequired(true);
        v1.setName("foo");
        v1.setHelpMessageKey("help key");
        v1.setDisplayMessageKey("display key");
        v1.setGroupMessageKey("group key");
        v1.setValue("bar");
        v1.setType(String.class);
        v1.setOperations(FrameworkUtil.allAPIOperations());

        ConfigurationPropertyImpl v2 = (ConfigurationPropertyImpl)
            cloneObject(v1);
        assertEquals(1, v2.getOrder());
        assertTrue(v2.isConfidential());
        assertTrue(v2.isRequired());
        assertEquals("foo", v2.getName());
        assertEquals("help key", v2.getHelpMessageKey());
        assertEquals("display key", v2.getDisplayMessageKey());
        assertEquals("group key", v2.getGroupMessageKey());
        assertEquals("bar", v2.getValue());
        assertEquals(String.class, v2.getType());
        assertEquals(FrameworkUtil.allAPIOperations(), v2.getOperations());
    }

    @Test
    public void testConfigurationProperties() {
        ConfigurationPropertyImpl prop1 = new ConfigurationPropertyImpl();
        prop1.setName("foo");
        prop1.setOrder(1);
        prop1.setConfidential(true);
        prop1.setName("foo");
        prop1.setHelpMessageKey("help key");
        prop1.setDisplayMessageKey("display key");
        prop1.setGroupMessageKey("group key");
        prop1.setValue("bar");
        prop1.setType(String.class);
        prop1.setOperations(null);

        ConfigurationPropertiesImpl v1 = new ConfigurationPropertiesImpl();
        v1.setProperties(CollectionUtil.newList(prop1));
        v1.setPropertyValue("foo", "bar");

        ConfigurationPropertiesImpl v2 = (ConfigurationPropertiesImpl)
            cloneObject(v1);
        assertEquals("bar", v2.getProperty("foo").getValue());
    }

    @Test
    public void testAPIConfiguration() {
        ConfigurationPropertyImpl prop1 = new ConfigurationPropertyImpl();
        prop1.setName("foo");
        prop1.setOrder(1);
        prop1.setConfidential(true);
        prop1.setName("foo");
        prop1.setHelpMessageKey("help key");
        prop1.setDisplayMessageKey("display key");
        prop1.setGroupMessageKey("group key");
        prop1.setValue("bar");
        prop1.setType(String.class);
        prop1.setOperations(null);

        ConfigurationPropertiesImpl props1 = new ConfigurationPropertiesImpl();
        props1.setProperties(CollectionUtil.newList(prop1));

        APIConfigurationImpl v1 = new APIConfigurationImpl();
        v1.setConnectorPoolConfiguration(new ObjectPoolConfiguration());
        v1.setConfigurationProperties(props1);
        v1.setConnectorPoolingSupported(true);
        v1.setProducerBufferSize(200);
        v1.setSupportedOperations(FrameworkUtil.allAPIOperations());
        Map<Class<? extends APIOperation>,Integer> map =
            CollectionUtil.<Class<? extends APIOperation>,Integer>newMap(CreateApiOp.class,new Integer(6));
        v1.setTimeoutMap(map);

        APIConfigurationImpl v2 = (APIConfigurationImpl)
            cloneObject(v1);
        assertTrue(v1 != v2);
        assertNotNull(v2.getConnectorPoolConfiguration());
        assertNotNull(v2.getConfigurationProperties());
        assertEquals(v1.getConnectorPoolConfiguration(),v2.getConnectorPoolConfiguration());
        assertEquals(v1.getConfigurationProperties(),v2.getConfigurationProperties());
        assertTrue(v2.isConnectorPoolingSupported());
        assertEquals(200, v2.getProducerBufferSize());
        assertEquals(FrameworkUtil.allAPIOperations(),
                v2.getSupportedOperations());
        assertEquals(map, v2.getTimeoutMap());
    }

    @Test
    public void testConnectorMessages() {
        ConnectorMessagesImpl v1 = new ConnectorMessagesImpl();
        Map<String,String> defaultMap = new HashMap<String,String>();
        defaultMap.put("key1","val1");
        Map<Locale,Map<String,String>> messages =
            new HashMap<Locale,Map<String,String>>();
        messages.put(new Locale("en"), defaultMap);
        messages.put(new Locale(""), defaultMap);
        v1.setCatalogs(messages);

        ConnectorMessagesImpl v2 = (ConnectorMessagesImpl)
            cloneObject(v1);
        assertEquals(messages,v2.getCatalogs());
    }

    @Test
    public void testRemoteConnectorInfo() {
        RemoteConnectorInfoImpl v1 = new RemoteConnectorInfoImpl();
        v1.setMessages(new ConnectorMessagesImpl());
        v1.setConnectorKey(new ConnectorKey("my bundle",
                "my version",
            "my connector"));
        ConfigurationPropertiesImpl configProperties = new ConfigurationPropertiesImpl();
        configProperties.setProperties(new ArrayList<ConfigurationPropertyImpl>());
        APIConfigurationImpl apiImpl = new APIConfigurationImpl();
        apiImpl.setConfigurationProperties(configProperties);
        v1.setDefaultAPIConfiguration(apiImpl);
        v1.setConnectorDisplayNameKey("mykey");
        v1.setConnectorCategoryKey("LDAP");
        RemoteConnectorInfoImpl v2 = (RemoteConnectorInfoImpl)
            cloneObject(v1);

        assertNotNull(v2.getMessages());
        assertEquals("my bundle", v2.getConnectorKey().getBundleName());
        assertEquals("my version", v2.getConnectorKey().getBundleVersion());
        assertEquals("my connector", v2.getConnectorKey().getConnectorName());
        assertEquals("mykey", v2.getConnectorDisplayNameKey());
        Assert.assertEquals(v2.getConnectorCategoryKey(), "LDAP");
        assertNotNull(v2.getDefaultAPIConfiguration());
    }

    @Test
    public void testAttribute() {
        Attribute v1 = AttributeBuilder.build("foo", "val1", "val2");
        Attribute v2 = (Attribute)cloneObject(v1);
        assertEquals(v1,v2);
    }

    @Test
    public void testAttributeInfo() {

        AttributeInfoBuilder builder = new AttributeInfoBuilder("foo",String.class);
        builder.setRequired(true);
        builder.setReadable(true);
        builder.setCreateable(true);
        builder.setMultiValued(true);
        builder.setUpdateable(false);
        builder.setReturnedByDefault(false);
        AttributeInfo v1 = builder.build();
        AttributeInfo v2 = (AttributeInfo)cloneObject(v1);
        assertEquals(v1,v2);
        assertEquals("foo", v2.getName());
        assertEquals(String.class, v2.getType());
        assertTrue(v2.isMultiValued());
        assertTrue(v2.isReadable());
        assertTrue(v2.isRequired());
        assertTrue(v2.isCreateable());
        assertFalse(v2.isUpdateable());
        assertFalse(v2.isReturnedByDefault());

        builder.setFlags(EnumSet.allOf(Flags.class));
        v1 = builder.build();
        v2 = (AttributeInfo)cloneObject(v1);
        assertEquals(v1,v2);

    }

    @Test
    public void testConnectorObject() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid("foo");
        bld.setName("name");

        ConnectorObject v1 = bld.build();
        ConnectorObject v2 = (ConnectorObject)cloneObject(v1);

        assertEquals(v1, v2);
    }

    @Test
    public void testName() {
        Name v1 = new Name("test");
        Name v2 = (Name)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testObjectClass() {
        ObjectClass v1 = new ObjectClass("test");
        ObjectClass v2 = (ObjectClass)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testObjectClassInfo() {
        AttributeInfoBuilder builder = new AttributeInfoBuilder("foo",String.class);
        builder.setRequired(true);
        builder.setReadable(true);
        builder.setCreateable(true);
        builder.setMultiValued(true);
        ObjectClassInfoBuilder obld = new ObjectClassInfoBuilder();
        obld.addAttributeInfo(builder.build());
        obld.setContainer(true);
        ObjectClassInfo v1 = obld.build();
        ObjectClassInfo v2 = (ObjectClassInfo)cloneObject(v1);
        assertEquals(v1, v2);
        assertTrue(v2.isContainer());
    }

    @Test
    public void testUid() {
        Uid v1 = new Uid("test");
        Uid v2 = (Uid)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testOperationOptionInfo() {
        OperationOptionInfo v1 =
            new OperationOptionInfo("name",Integer.class);
        OperationOptionInfo v2 =
            (OperationOptionInfo)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testSchema() {
        OperationOptionInfo opInfo =
            new OperationOptionInfo("name",Integer.class);
        ObjectClassInfoBuilder bld = new ObjectClassInfoBuilder();
        bld.setType(ObjectClass.ACCOUNT_NAME);
        ObjectClassInfo info = bld.build();
        Set<ObjectClassInfo> temp = CollectionUtil.newSet(info);
        Map<Class<? extends APIOperation>,Set<ObjectClassInfo>> map =
            new HashMap<Class<? extends APIOperation>,Set<ObjectClassInfo>>();
        map.put(CreateApiOp.class, temp);
        Set<OperationOptionInfo> temp2 = CollectionUtil.newSet(opInfo);
        Map<Class<? extends APIOperation>,Set<OperationOptionInfo>> map2 =
            new HashMap<Class<? extends APIOperation>,Set<OperationOptionInfo>>();
        map2.put(CreateApiOp.class, temp2);
        Schema v1 = new Schema(CollectionUtil.newSet(info),
                CollectionUtil.newSet(opInfo),
                map,
                map2);
        Schema v2 = (Schema)cloneObject(v1);
        assertEquals(v1, v2);
        assertEquals(info, v2.getObjectClassInfo().iterator().next());
        assertEquals(1, v2.getSupportedObjectClassesByOperation().size());
        assertEquals(1, v2.getSupportedOptionsByOperation().size());
        assertEquals(1, v2.getOperationOptionInfo().size());
    }

    @Test
    public void testContainsFilter() {
        ContainsFilter v1 = new ContainsFilter(AttributeBuilder.build("foo", "bar"));
        ContainsFilter v2 = (ContainsFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testAndFilter() {
        ContainsFilter left1 = new ContainsFilter(AttributeBuilder.build("foo", "bar"));
        ContainsFilter right1 = new ContainsFilter(AttributeBuilder.build("foo2", "bar2"));
        AndFilter v1 = new AndFilter(left1,right1);
        AndFilter v2 = (AndFilter)cloneObject(v1);
        ContainsFilter left2 = (ContainsFilter)v2.getLeft();
        ContainsFilter right2 = (ContainsFilter)v2.getRight();
        assertEquals(left1.getAttribute(),left2.getAttribute());
        assertEquals(right1.getAttribute(),right2.getAttribute());
    }

    @Test
    public void testEndsWithFilter() {
        EndsWithFilter v1 = new EndsWithFilter(AttributeBuilder.build("foo", "bar"));
        EndsWithFilter v2 = (EndsWithFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testEqualsFilter() {
        EqualsFilter v1 = new EqualsFilter(AttributeBuilder.build("foo", "bar"));
        EqualsFilter v2 = (EqualsFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testGreaterThanFilter() {
        GreaterThanFilter v1 = new GreaterThanFilter(AttributeBuilder.build("foo", "bar"));
        GreaterThanFilter v2 = (GreaterThanFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testGreaterThanOrEqualFilter() {
        GreaterThanOrEqualFilter v1 = new GreaterThanOrEqualFilter(AttributeBuilder.build("foo", "bar"));
        GreaterThanOrEqualFilter v2 = (GreaterThanOrEqualFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testLessThanFilter() {
        LessThanFilter v1 = new LessThanFilter(AttributeBuilder.build("foo", "bar"));
        LessThanFilter v2 = (LessThanFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testLessThanOrEqualFilter() {
        LessThanOrEqualFilter v1 = new LessThanOrEqualFilter(AttributeBuilder.build("foo", "bar"));
        LessThanOrEqualFilter v2 = (LessThanOrEqualFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testNotFilter() {
        ContainsFilter left1 = new ContainsFilter(AttributeBuilder.build("foo", "bar"));
        NotFilter v1 = new NotFilter(left1);
        NotFilter v2 = (NotFilter)cloneObject(v1);
        ContainsFilter left2 = (ContainsFilter)v2.getFilter();
        assertEquals(left1.getAttribute(),left2.getAttribute());
    }

    @Test
    public void testOrFilter() {
        ContainsFilter left1 = new ContainsFilter(AttributeBuilder.build("foo", "bar"));
        ContainsFilter right1 = new ContainsFilter(AttributeBuilder.build("foo2", "bar2"));
        OrFilter v1 = new OrFilter(left1,right1);
        OrFilter v2 = (OrFilter)cloneObject(v1);
        ContainsFilter left2 = (ContainsFilter)v2.getLeft();
        ContainsFilter right2 = (ContainsFilter)v2.getRight();
        assertEquals(left1.getAttribute(),left2.getAttribute());
        assertEquals(right1.getAttribute(),right2.getAttribute());
    }

    @Test
    public void testStartsWithFilter() {
        StartsWithFilter v1 = new StartsWithFilter(AttributeBuilder.build("foo", "bar"));
        StartsWithFilter v2 = (StartsWithFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testContainsAllValuesFilter() {
        ContainsAllValuesFilter v1 = new ContainsAllValuesFilter(AttributeBuilder.build("foo", "a", "b"));
        ContainsAllValuesFilter v2 = (ContainsAllValuesFilter)cloneObject(v1);
        assertEquals(v1.getAttribute(), v2.getAttribute());
    }

    @Test
    public void testExceptions() {
        {
            AlreadyExistsException v1 = new AlreadyExistsException("ex");
            AlreadyExistsException v2 = (AlreadyExistsException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            ConfigurationException v1 = new ConfigurationException("ex");
            ConfigurationException v2 = (ConfigurationException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            ConnectionBrokenException v1 = new ConnectionBrokenException("ex");
            ConnectionBrokenException v2 = (ConnectionBrokenException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            ConnectionFailedException v1 = new ConnectionFailedException("ex");
            ConnectionFailedException v2 = (ConnectionFailedException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            ConnectorException v1 = new ConnectorException("ex");
            ConnectorException v2 = (ConnectorException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }
        {
            ConnectorIOException v1 = new ConnectorIOException("ex");
            ConnectorIOException v2 = (ConnectorIOException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }
        {
            ConnectorSecurityException v1 = new ConnectorSecurityException("ex");
            ConnectorSecurityException v2 = (ConnectorSecurityException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            InvalidCredentialException v1 = new InvalidCredentialException("ex");
            InvalidCredentialException v2 = (InvalidCredentialException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            InvalidPasswordException v1 = new InvalidPasswordException("ex");
            InvalidPasswordException v2 = (InvalidPasswordException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            PasswordExpiredException v1 = new PasswordExpiredException("ex");
            v1.initUid(new Uid("myuid"));
            PasswordExpiredException v2 = (PasswordExpiredException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
            assertEquals("myuid", v2.getUid().getUidValue());
        }

        {
            OperationTimeoutException v1 = new OperationTimeoutException();
            OperationTimeoutException v2 = (OperationTimeoutException)cloneObject(v1);
            assertNotNull(v2);
        }

        {
            PermissionDeniedException v1 = new PermissionDeniedException("ex");
            PermissionDeniedException v2 = (PermissionDeniedException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            UnknownUidException v1 = new UnknownUidException("ex");
            UnknownUidException v2 = (UnknownUidException)cloneObject(v1);
            assertEquals("ex",v2.getMessage());
        }

        {
            IllegalArgumentException v1 = new IllegalArgumentException("my msg");
            IllegalArgumentException v2 = (IllegalArgumentException)cloneObject(v1);
            assertEquals("my msg", v2.getMessage());
        }

        {
        	RuntimeException v1 = new RuntimeException("my msg");
            RuntimeException v2 = (RuntimeException)cloneObject(v1);
            assertEquals("my msg", v2.getMessage());
        }

        {
            Exception v1 = new Exception("my msg2");
            Exception v2 = (Exception)cloneObject(v1);
            assertEquals("my msg2", v2.getMessage());
        }

        {
            Throwable v1 = new Throwable("my msg3");
            Exception v2 = (Exception)cloneObject(v1);
            assertEquals("my msg3", v2.getMessage());
        }

    }

    @Test
    public void testHelloRequest() {
        HelloRequest v1 = new HelloRequest(HelloRequest.CONNECTOR_INFO);
        HelloRequest v2 = (HelloRequest)cloneObject(v1);
        Assert.assertNotNull(v2);
        Assert.assertEquals(v2.getInfoLevel(),HelloRequest.CONNECTOR_INFO);
    }

    @Test
    public void testHelloResponse() {
        RuntimeException ex = new RuntimeException("foo");
        Map<String,Object> serverInfo = new HashMap<String, Object>(1);
        serverInfo.put(HelloResponse.SERVER_START_TIME,System.currentTimeMillis());
        ConnectorKey key = new ConnectorKey("my bundle", "my version", "my connector");
        RemoteConnectorInfoImpl info = new RemoteConnectorInfoImpl();
        info.setMessages(new ConnectorMessagesImpl());
        info.setConnectorKey(key);
        ConfigurationPropertiesImpl configProperties = new ConfigurationPropertiesImpl();
        configProperties.setProperties(new ArrayList<ConfigurationPropertyImpl>());
        APIConfigurationImpl apiImpl = new APIConfigurationImpl();
        apiImpl.setConfigurationProperties(configProperties);
        info.setDefaultAPIConfiguration(apiImpl);
        info.setConnectorDisplayNameKey("mykey");
        info.setConnectorCategoryKey("");
        HelloResponse v1 = new HelloResponse(ex, serverInfo, CollectionUtil.newList(key), CollectionUtil.newList(info));
        HelloResponse v2 = (HelloResponse)cloneObject(v1);
        Assert.assertNotNull(v2.getException());
        Assert.assertNotNull(v2.getServerInfo().get(HelloResponse.SERVER_START_TIME));
        Assert.assertNotNull(v2.getConnectorKeys().iterator().next());
        Assert.assertNotNull(v2.getConnectorInfos().iterator().next());
    }

    @Test
    public void testOperationRequest() {
        ConfigurationPropertiesImpl configProperties = new ConfigurationPropertiesImpl();
        configProperties.setProperties(new ArrayList<ConfigurationPropertyImpl>());
        APIConfigurationImpl apiImpl = new APIConfigurationImpl();
        apiImpl.setConfigurationProperties(configProperties);
        List<Object> args = new ArrayList<Object>();
        args.add("my arg");
        OperationRequest v1 = new
            OperationRequest(
                    new ConnectorKey(
                    "my bundle",
                    "my version",
                "my connector"),
                apiImpl,
                CreateApiOp.class,
                "mymethodName",
                args);
        OperationRequest v2 = (OperationRequest)cloneObject(v1);
        assertEquals("my bundle", v2.getConnectorKey().getBundleName());
        assertEquals("my version", v2.getConnectorKey().getBundleVersion());
        assertEquals("my connector", v2.getConnectorKey().getConnectorName());
        assertNotNull(v2.getConfiguration());
        assertEquals(CreateApiOp.class, v2.getOperation());
        assertEquals("mymethodName", v2.getOperationMethodName());
        assertEquals(args, v2.getArguments());
    }

    @Test
    public void testOperationResponseEnd() {
        OperationResponseEnd v1 = new OperationResponseEnd();
        OperationResponseEnd v2 = (OperationResponseEnd)cloneObject(v1);
        assertNotNull(v2);
    }
    @Test
    public void testOperationResponsePart() {
        RuntimeException ex = new RuntimeException("foo");
        OperationResponsePart v1 = new OperationResponsePart(ex,"bar");
        OperationResponsePart v2 = (OperationResponsePart)cloneObject(v1);
        assertNotNull(v2.getException());
        assertEquals("bar", v2.getResult());
    }

    @Test
    public void testOperationResponsePause() {
        OperationResponsePause v1 = new OperationResponsePause();
        OperationResponsePause v2 = (OperationResponsePause)cloneObject(v1);
        assertNotNull(v2);
    }

    @Test
    public void testOperationRequestMoreData() {
        OperationRequestMoreData v1 = new OperationRequestMoreData();
        OperationRequestMoreData v2 = (OperationRequestMoreData)cloneObject(v1);
        assertNotNull(v2);
    }

    @Test
    public void testOperationRequestStopData() {
        OperationRequestStopData v1 = new OperationRequestStopData();
        OperationRequestStopData v2 = (OperationRequestStopData)cloneObject(v1);
        assertNotNull(v2);
    }

    @Test
    public void testEchoMessage() {
        EchoMessage v1 = new EchoMessage("test","xml");
        EchoMessage v2 = (EchoMessage)cloneObject(v1);
        assertEquals("test", v2.getObject());
        assertEquals("xml", v2.getXml());
    }

    @Test
    public void testOperationOptions() {
        OperationOptionsBuilder builder = new OperationOptionsBuilder();
        builder.setOption("foo", "bar");
        builder.setOption("foo2", "bar2");
        OperationOptions v1 = builder.build();
        OperationOptions v2 = (OperationOptions)cloneObject(v1);
        assertEquals(2,v2.getOptions().size());
        assertEquals("bar",v2.getOptions().get("foo"));
        assertEquals("bar2",v2.getOptions().get("foo2"));
    }

    @Test
    public void testScript() {
        ScriptBuilder builder = new ScriptBuilder();
        builder.setScriptLanguage("language");
        builder.setScriptText("text");
        Script v1 = builder.build();
        Script v2 = (Script)cloneObject(v1);
        assertEquals("language",v2.getScriptLanguage());
        assertEquals("text",v2.getScriptText());
    }

    @Test
    public void testScriptContext() {
        ScriptContextBuilder builder = new ScriptContextBuilder();
        builder.setScriptLanguage("language");
        builder.setScriptText("text");
        builder.addScriptArgument("foo", "bar");
        builder.addScriptArgument("foo2", "bar2");
        ScriptContext v1 = builder.build();
        ScriptContext v2 = (ScriptContext)cloneObject(v1);
        assertEquals(2,v2.getScriptArguments().size());
        assertEquals("bar",v2.getScriptArguments().get("foo"));
        assertEquals("bar2",v2.getScriptArguments().get("foo2"));
        assertEquals("language",v2.getScriptLanguage());
        assertEquals("text",v2.getScriptText());
    }

    @Test
    public void testSyncDeltaType() {
        SyncDeltaType v1 = SyncDeltaType.DELETE;
        SyncDeltaType v2 = (SyncDeltaType)cloneObject(v1);
        assertEquals(v1, v2);
    }

    @Test
    public void testSyncToken() {
        SyncToken v1 = new SyncToken("mytoken");
        SyncToken v2 = (SyncToken)cloneObject(v1);
        assertEquals(v1.getValue(),v2.getValue());
        assertEquals(v1,v2);
    }

    @Test
    public void testSyncDelta() {
        ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
        bld.setUid("foo");
        bld.setName("name");
        SyncDeltaBuilder builder = new SyncDeltaBuilder();
        builder.setPreviousUid(new Uid("previous"));
        builder.setObject(bld.build());
        builder.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
        builder.setToken(new SyncToken("mytoken"));
        SyncDelta v1 = builder.build();
        SyncDelta v2 = (SyncDelta)cloneObject(v1);
        assertEquals(new Uid("previous"),v2.getPreviousUid());
        assertEquals(new Uid("foo"),v2.getObject().getUid());
        assertEquals(new SyncToken("mytoken"),v2.getToken());
        assertEquals(SyncDeltaType.CREATE_OR_UPDATE,v2.getDeltaType());
        assertEquals(v1,v2);
    }

    @Test
    public void testNull() {
        Object v1 = null;
        Object v2 = cloneObject(v1);
        assertNull(v2);
    }

    @Test
    public void testGuardedString() {
        GuardedString v1 = new GuardedString("foobar".toCharArray());
        GuardedString v2 = (GuardedString)cloneObject(v1);
        assertEquals("foobar", decryptToString(v2));
    }

    @Test
    public void testGuardedByteArray() {
        GuardedByteArray v1 = new GuardedByteArray(new byte[] { 0x00, 0x01, 0x02, 0x03 });
        GuardedByteArray v2 = (GuardedByteArray)cloneObject(v1);
        assertTrue(Arrays.equals(new byte[] { 0x00, 0x01, 0x02, 0x03 }, decryptToBytes(v2)));
    }

    @Test
    public void testQualifiedUid() {
        QualifiedUid v1 = new QualifiedUid(new ObjectClass("myclass"),
                new Uid("myuid"));
        QualifiedUid v2 = (QualifiedUid)cloneObject(v1);
        assertEquals(v1, v2);
        assertTrue(v2.getObjectClass().is("myclass"));
        assertEquals("myuid", v2.getUid().getUidValue());
    }

    /**
     * Highly insecure method! Do not do this in production
     * code. This is only for test purposes
     */
    private String decryptToString(GuardedString string) {
        final StringBuilder buf = new StringBuilder();
        string.access(
                new GuardedString.Accessor() {
                    public void access(char [] chars) {
                        buf.append(chars);
                    }
                });
        return buf.toString();
    }

    /**
     * Highly insecure method! Do not do this in production
     * code. This is only for test purposes
     */
    private byte[] decryptToBytes(GuardedByteArray bytes) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        bytes.access(
                new GuardedByteArray.Accessor() {
                    public void access(byte[] bytes) {
                        out.write(bytes, 0, bytes.length);
                    }
                });
        return out.toByteArray();
    }

    protected Object cloneObject(Object o) {
        return SerializerUtil.cloneObject(o);
    }


}
