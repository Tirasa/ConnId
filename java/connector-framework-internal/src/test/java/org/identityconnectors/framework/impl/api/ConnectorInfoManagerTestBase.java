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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.framework.impl.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.Version;
import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.LiveSyncApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.common.FrameworkUtilTestHelpers;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectReference;
import org.identityconnectors.framework.common.objects.ConnectorObjectIdentification;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ScriptContextBuilder;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.impl.api.local.ConnectorPoolManager;
import org.identityconnectors.testconnector.TstConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public abstract class ConnectorInfoManagerTestBase {

    private static ConnectorInfo findConnectorInfo(ConnectorInfoManager manager, String version, String connectorName) {
        for (ConnectorInfo info : manager.getConnectorInfos()) {
            ConnectorKey key = info.getConnectorKey();
            if (version.equals(key.getBundleVersion()) && connectorName.equals(key.getConnectorName())) {
                //intentionally ineffecient to test more code
                return manager.findConnectorInfo(key);
            }
        }
        return null;
    }

    @BeforeEach
    public void before() {
        // LocalConnectorInfoManagerImpl needs to know the framework version.
        // In case the framework doesn't know its version (for instance, because
        // we are running against the classes, not a JAR file, so META-INF/MANIFEST.MF
        // is not available), fake the version here.
        FrameworkUtilTestHelpers.setFrameworkVersion(Version.parse("2.0"));
    }

    @AfterEach
    public void after() {
        shutdownConnnectorInfoManager();
        FrameworkUtilTestHelpers.setFrameworkVersion(null);
    }

    @Test
    public void testClassLoading() throws Exception {
        ClassLoader startLocal = Thread.currentThread().getContextClassLoader();
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info1 = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");
        assertNotNull(info1);
        ConnectorInfo info2 = findConnectorInfo(manager,
                "2.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");
        assertNotNull(info2);

        APIConfiguration apiConfig1 = info1.createDefaultAPIConfiguration();
        ConfigurationProperties props = apiConfig1.getConfigurationProperties();
        ConfigurationProperty property = props.getProperty("numResults");
        property.setValue(1);

        ConnectorFacade facade1 = ConnectorFacadeFactory.getInstance().
                newInstance(apiConfig1);

        ConnectorFacade facade2 = ConnectorFacadeFactory.getInstance().
                newInstance(info2.createDefaultAPIConfiguration());

        Set<Attribute> attrs = CollectionUtil.<Attribute>newReadOnlySet();
        assertEquals("1.0", facade1.create(ObjectClass.ACCOUNT, attrs, null).getUidValue());
        assertEquals("2.0", facade2.create(ObjectClass.ACCOUNT, attrs, null).getUidValue());

        int[] count = new int[1];
        facade1.search(ObjectClass.ACCOUNT, null, obj -> {
            count[0]++;
            // make sure thread local classloader is restored
            assertSame(startLocal, Thread.currentThread().getContextClassLoader());
            return true;
        }, null);
        assertEquals(1, count[0]);

        // make sure thread local classloader is restored
        assertSame(startLocal, Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void testNativeLibraries() throws Exception {
        // Localize expected error messages (see catch() below)
        ResourceBundle errorMessageBundle = ResourceBundle.getBundle("Messages");

        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "2.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration api = info.createDefaultAPIConfiguration();
        ConnectorFacade facade = ConnectorFacadeFactory.getInstance().newInstance(api);

        try {
            // The connector will do a System.loadLibrary().
            facade.authenticate(ObjectClass.ACCOUNT, "username", new GuardedString("password".toCharArray()), null);
        } catch (UnsatisfiedLinkError e) {
            // If this particular exception occurs, then the bundle class loader has
            // correctly pointed to the native library (but the library could not be
            // loaded, since it is not a valid library--we want to keep our tests platform-independent).
            assertTrue(e.getMessage().contains(errorMessageBundle.getString("filetooshort"))
                    || e.getMessage().contains(errorMessageBundle.getString("nosuitableimagefound"))
                    || e.getMessage().contains(errorMessageBundle.getString("nonativein"))
                    || e.getMessage().contains(errorMessageBundle.getString("notvalidwin32application")));
        } catch (RuntimeException e) {
            // Remote framework serializes UnsatisfiedLinkError as RuntimeException.
            assertTrue(e.getMessage().contains(errorMessageBundle.getString("filetooshort"))
                    || e.getMessage().contains(errorMessageBundle.getString("nosuitableimagefound"))
                    || e.getMessage().contains(errorMessageBundle.getString("nonativein"))
                    || e.getMessage().contains(errorMessageBundle.getString("notvalidwin32application")));
        }
    }

    /**
     * Attempt to test the information from the configuration.
     *
     * @throws Exception iff there is an issue.
     */
    @Test
    public void testAPIConfiguration() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration api = info.createDefaultAPIConfiguration();

        ConfigurationProperties props = api.getConfigurationProperties();
        ConfigurationProperty property = props.getProperty("tstField");
        assertNotNull(property);

        Set<Class<? extends APIOperation>> operations = property.getOperations();
        assertEquals(2, operations.size());
        assertTrue(operations.contains(SyncApiOp.class));
        assertTrue(operations.contains(LiveSyncApiOp.class));

        CurrentLocale.clear();
        assertEquals("Help for test field.", property.getHelpMessage(null));
        assertEquals("Display for test field.", property.getDisplayName(null));
        assertEquals("Test Framework Value", info.getMessages().format("TEST_FRAMEWORK_KEY", "empty"));

        Locale xlocale = new Locale("es");
        CurrentLocale.set(xlocale);
        assertEquals("tstField.help_es", property.getHelpMessage(null));
        assertEquals("tstField.display_es", property.getDisplayName(null));

        Locale esESlocale = new Locale("es", "ES");
        CurrentLocale.set(esESlocale);
        assertEquals("tstField.help_es-ES", property.getHelpMessage(null));
        assertEquals("tstField.display_es-ES", property.getDisplayName(null));

        Locale esARlocale = new Locale("es", "AR");
        CurrentLocale.set(esARlocale);
        assertEquals("tstField.help_es", property.getHelpMessage(null));
        assertEquals("tstField.display_es", property.getDisplayName(null));

        CurrentLocale.clear();

        ConnectorFacadeFactory facf = ConnectorFacadeFactory.getInstance();
        ConnectorFacade facade = facf.newInstance(api);
        // call the various create/update/delete commands..
        facade.schema();
    }

    /**
     * Attempt to test the information from the configuration.
     *
     * @throws Exception iff there is an issue.
     */
    @Test
    public void testValidate() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration api = info.createDefaultAPIConfiguration();

        ConfigurationProperties props = api.getConfigurationProperties();
        ConfigurationProperty property = props.getProperty("failValidation");
        property.setValue(false);
        ConnectorFacadeFactory facf = ConnectorFacadeFactory.getInstance();
        ConnectorFacade facade = facf.newInstance(api);
        facade.validate();
        property.setValue(true);
        facade = facf.newInstance(api);
        // validate and also test that locale is propagated properly
        try {
            CurrentLocale.set(new Locale("en"));
            facade.validate();

            fail("exception expected");
        } catch (ConnectorException e) {
            assertEquals("validation failed en", e.getMessage());
        } finally {
            CurrentLocale.clear();
        }
        // validate and also test that locale is propagated properly
        try {
            CurrentLocale.set(new Locale("es"));
            facade.validate();

            fail("exception expected");
        } catch (ConnectorException e) {
            assertEquals("validation failed es", e.getMessage());
        } finally {
            CurrentLocale.clear();
        }
    }

    /**
     * Main purpose of this is to test searching with many results and that we can properly handle stopping in the
     * middle of this. There's a bunch of code in the remote stuff that is there to handle this in particular that we
     * want to excercise.
     */
    @Test
    public void testSearchWithManyResults() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration api = info.createDefaultAPIConfiguration();

        ConfigurationProperties props = api.getConfigurationProperties();
        ConfigurationProperty property = props.getProperty("numResults");

        //1000 is several times the remote size between pauses
        property.setValue(1000);

        ConnectorFacadeFactory facf = ConnectorFacadeFactory.getInstance();
        ConnectorFacade facade = facf.newInstance(api);

        final List<ConnectorObject> results = new ArrayList<>();

        facade.search(ObjectClass.ACCOUNT, null, (final ConnectorObject obj) -> {
            results.add(obj);
            return true;
        }, null);

        assertEquals(1000, results.size());
        for (int i = 0; i < results.size(); i++) {
            ConnectorObject obj = results.get(i);
            assertEquals(String.valueOf(i), obj.getUid().getUidValue());
        }

        results.clear();

        facade.search(ObjectClass.ACCOUNT, null, (final ConnectorObject obj) -> {
            if (results.size() < 500) {
                results.add(obj);
                return true;
            } else {
                return false;
            }
        }, null);

        assertEquals(500, results.size());
        for (int i = 0; i < results.size(); i++) {
            ConnectorObject obj = results.get(i);
            assertEquals(String.valueOf(i), obj.getUid().getUidValue());
        }
    }

    @Test
    public void testSearchStress() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration api = info.createDefaultAPIConfiguration();

        ConfigurationProperties props = api.getConfigurationProperties();
        ConfigurationProperty property = props.getProperty("numResults");

        property.setValue(10000);

        ConnectorFacadeFactory facf = ConnectorFacadeFactory.getInstance();
        ConnectorFacade facade = facf.newInstance(api);
        long start = System.currentTimeMillis();
        facade.search(ObjectClass.ACCOUNT, null, obj -> true, null);
        long end = System.currentTimeMillis();
        System.out.println("Test took: " + (end - start) / 1000);
    }

    /** Checks the schema (mainly regarding reference attributes). */
    @Test
    public void testSchema() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        ConnectorFacade facade = ConnectorFacadeFactory.getInstance()
                .newInstance(info.createDefaultAPIConfiguration());
        Schema schema = facade.schema();

        assertEquals(5, schema.getObjectClassInfo().size());

        ObjectClassInfo userObjectClass = schema.findObjectClassInfo(TstConnector.USER_CLASS_NAME);
        assertNotNull(userObjectClass);
        userObjectClass.getAttributeInfo().stream()
                .filter(attr -> attr.getName().equals(TstConnector.MEMBER_OF_ATTR_NAME))
                .findFirst()
                .ifPresentOrElse(attr -> {
                    assertEquals(TstConnector.GROUP_CLASS_NAME, attr.getReferencedObjectClassName());
                    assertEquals(TstConnector.GROUP_MEMBERSHIP_REFERENCE_TYPE_NAME, attr.getSubtype());
                    assertEquals(AttributeInfo.RoleInReference.SUBJECT.toString(), attr.getRoleInReference());
                    assertTrue(attr.isMultiValued());
                }, () -> {
                    fail("Attribute " + TstConnector.MEMBER_OF_ATTR_NAME + " not found");
                });

        ObjectClassInfo accessObjectClass = schema.findObjectClassInfo(TstConnector.ACCESS_CLASS_NAME);
        assertNotNull(accessObjectClass);
        assertTrue(accessObjectClass.isEmbedded());
    }

    /** Checks that the connector can return objects with references. */
    @Test
    public void testSearchWithReferences() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        ConnectorFacade facade = ConnectorFacadeFactory.getInstance()
                .newInstance(info.createDefaultAPIConfiguration());

        final List<ConnectorObject> results = new ArrayList<>();

        facade.search(TstConnector.userObjectClass(), null, (final ConnectorObject obj) -> {
            results.add(obj);
            return true;
        }, null);

        assertEquals(2, results.size());
        ConnectorObject user100 = results.get(0);
        assertEquals(TstConnector.USER_100_UID, user100.getUid().getUidValue());
        assertEquals(TstConnector.USER_100_NAME, user100.getName().getNameValue());

        List<?> user100Groups = user100.getAttributeByName(TstConnector.MEMBER_OF_ATTR_NAME).getValue();

        assertEquals(2, user100Groups.size());
        ConnectorObjectReference firstGroupRef = (ConnectorObjectReference) user100Groups.get(0);
        assertTrue(firstGroupRef.hasObject());
        ConnectorObject firstGroup = (ConnectorObject) firstGroupRef.getValue();
        assertEquals(TstConnector.GROUP_1_UID, firstGroup.getUid().getUidValue());
        assertEquals(TstConnector.GROUP_1_NAME, firstGroup.getName().getNameValue());
        assertEquals(2, firstGroup.getAttributeByName(TstConnector.MEMBERS_ATTR_NAME).getValue().size());

        ConnectorObjectReference secondGroupRef = (ConnectorObjectReference) user100Groups.get(1);
        assertTrue(secondGroupRef.hasObject());
        ConnectorObject secondGroup = (ConnectorObject) secondGroupRef.getValue();
        assertEquals(TstConnector.GROUP_2_UID, secondGroup.getUid().getUidValue());
        assertEquals(TstConnector.GROUP_2_NAME, secondGroup.getName().getNameValue());
        List<?> memberRefs = secondGroup.getAttributeByName(TstConnector.MEMBERS_ATTR_NAME).getValue();
        assertEquals(1, memberRefs.size());
        ConnectorObjectReference firstMemberRef = (ConnectorObjectReference) memberRefs.get(0);
        assertFalse(firstMemberRef.hasObject());
        ConnectorObjectIdentification firstMemberIds = (ConnectorObjectIdentification) firstMemberRef.getValue();
        assertEquals(TstConnector.USER_100_NAME, firstMemberIds.getAttributeByName(Name.NAME).getValue().get(0));
        assertEquals(1, firstMemberIds.getAttributes().size());
    }

    @RepeatedTest(100)
    @Test
    public void testSchemaStress(TestInfo testInfo) throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration api = info.createDefaultAPIConfiguration();

        ConnectorFacadeFactory facf = ConnectorFacadeFactory.getInstance();
        ConnectorFacade facade = facf.newInstance(api);

        facade.schema();
    }

    @RepeatedTest(100)
    @Test
    public void testCreateStress(TestInfo testInfo) throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration api = info.createDefaultAPIConfiguration();

        ConnectorFacadeFactory facf = ConnectorFacadeFactory.getInstance();
        ConnectorFacade facade = facf.newInstance(api);

        Set<Attribute> attrs = new HashSet<>();
        for (int j = 0; j < 50; j++) {
            attrs.add(AttributeBuilder.build("myattributename" + j, "myattributevalue" + j));
        }
        facade.create(ObjectClass.ACCOUNT, attrs, null);
    }

    /**
     * Main purpose of this is to test sync with many results and that we can properly handle stopping in the middle of
     * this. There's a bunch of code in the remote stuff that is there to handle this in particular that we want to
     * excercise.
     */
    @Test
    public void testSyncWithManyResults() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration api = info.createDefaultAPIConfiguration();

        ConfigurationProperties props = api.getConfigurationProperties();
        ConfigurationProperty property = props.getProperty("numResults");

        //1000 is several times the remote size between pauses
        property.setValue(1000);

        ConnectorFacadeFactory facf = ConnectorFacadeFactory.getInstance();
        ConnectorFacade facade = facf.newInstance(api);

        SyncToken latest = facade.getLatestSyncToken(ObjectClass.ACCOUNT);
        assertEquals("mylatest", latest.getValue());

        final List<SyncDelta> results = new ArrayList<>();

        facade.sync(ObjectClass.ACCOUNT, null, (final SyncDelta obj) -> {
            results.add(obj);
            return true;
        }, null);

        assertEquals(1000, results.size());
        for (int i = 0; i < results.size(); i++) {
            SyncDelta obj = results.get(i);
            assertEquals(String.valueOf(i), obj.getObject().getUid().getUidValue());
        }

        results.clear();

        facade.sync(ObjectClass.ACCOUNT, null, delta -> {
            if (results.size() < 500) {
                results.add(delta);
                return true;
            } else {
                return false;
            }
        }, null);

        assertEquals(500, results.size());
        for (int i = 0; i < results.size(); i++) {
            SyncDelta delta = results.get(i);
            assertEquals(String.valueOf(i), delta.getObject().getUid().getUidValue());
        }
    }

    //TODO: this needs to overridden for C# testing
    @Test
    public void testScripting() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");
        APIConfiguration api = info.createDefaultAPIConfiguration();

        ConnectorFacadeFactory facf = ConnectorFacadeFactory.getInstance();
        ConnectorFacade facade = facf.newInstance(api);

        ScriptContextBuilder builder = new ScriptContextBuilder();
        builder.addScriptArgument("arg1", "value1");
        builder.addScriptArgument("arg2", "value2");
        builder.setScriptLanguage("GROOVY");

        // test that they can run the script and access the connector object
        {
            String script = "return connector.concat(arg1,arg2)";
            builder.setScriptText(script);
            String result = (String) facade.runScriptOnConnector(builder.build(), null);

            assertEquals("value1value2", result);
        }

        // test that they can access a class in the class loader
        {
            String script = "return org.identityconnectors.testcommon.TstCommon.getVersion()";
            builder.setScriptText(script);
            String result = (String) facade.runScriptOnConnector(builder.build(), null);
            assertEquals("1.0", result);
        }

        // test that they cannot access a class in internal
        {
            String clazz = ConfigurationPropertyImpl.class.getName();

            String script = "return new " + clazz + "()";
            builder.setScriptText(script);
            try {
                facade.runScriptOnConnector(builder.build(), null);
                fail("exception expected");
            } catch (Throwable t) {
                String expectedMessage = "org/identityconnectors/framework/impl/api/ConfigurationPropertyImpl";
                assertTrue(t.getMessage().contains(expectedMessage));
            }
        }

        // test that they can access a class in common
        {
            String clazz = AttributeBuilder.class.getName();
            String script = "return " + clazz + ".build(\"myattr\")";
            builder.setScriptText(script);
            Attribute attr = (Attribute) facade.runScriptOnConnector(builder.build(), null);
            assertEquals("myattr", attr.getName());
        }
    }

    @Test
    public void testConnectionPooling() throws Exception {
        ConnectorPoolManager.shutdown();
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info1 = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");
        assertNotNull(info1);

        //reset connection count
        {
            //trigger TstConnection.init to be called
            APIConfiguration config = info1.createDefaultAPIConfiguration();
            config.getConfigurationProperties().getProperty("resetConnectionCount").setValue(true);
            ConnectorFacade facade1 = ConnectorFacadeFactory.getInstance().newInstance(config);
            facade1.schema(); //force instantiation            
        }

        APIConfiguration config = info1.createDefaultAPIConfiguration();

        config.getConnectorPoolConfiguration().setMinIdle(0);
        config.getConnectorPoolConfiguration().setMaxIdle(0);

        ConnectorFacade facade1 = ConnectorFacadeFactory.getInstance().newInstance(config);

        OperationOptionsBuilder builder = new OperationOptionsBuilder();
        builder.setOption("testPooling", "true");
        OperationOptions options = builder.build();
        Set<Attribute> attrs = CollectionUtil.<Attribute>newReadOnlySet();
        assertEquals("1", facade1.create(ObjectClass.ACCOUNT, attrs, options).getUidValue());
        assertEquals("2", facade1.create(ObjectClass.ACCOUNT, attrs, options).getUidValue());
        assertEquals("3", facade1.create(ObjectClass.ACCOUNT, attrs, options).getUidValue());
        assertEquals("4", facade1.create(ObjectClass.ACCOUNT, attrs, options).getUidValue());
        config = info1.createDefaultAPIConfiguration();
        config.getConnectorPoolConfiguration().setMinIdle(1);
        config.getConnectorPoolConfiguration().setMaxIdle(2);
        facade1 = ConnectorFacadeFactory.getInstance().newInstance(config);
        assertEquals("5", facade1.create(ObjectClass.ACCOUNT, attrs, options).getUidValue());
        assertEquals("5", facade1.create(ObjectClass.ACCOUNT, attrs, options).getUidValue());
        assertEquals("5", facade1.create(ObjectClass.ACCOUNT, attrs, options).getUidValue());
        assertEquals("5", facade1.create(ObjectClass.ACCOUNT, attrs, options).getUidValue());
    }

    @Test
    public void testTimeout() throws Exception {
        ConnectorInfoManager manager = getConnectorInfoManager();
        ConnectorInfo info1 = findConnectorInfo(manager,
                "1.0.0.0",
                "org.identityconnectors.testconnector.TstConnector");

        APIConfiguration config = info1.createDefaultAPIConfiguration();
        config.setTimeout(CreateApiOp.class, 5000);
        config.setTimeout(SearchApiOp.class, 5000);
        ConfigurationProperties props = config.getConfigurationProperties();
        ConfigurationProperty property = props.getProperty("numResults");
        //1000 is several times the remote size between pauses
        property.setValue(2);
        OperationOptionsBuilder opBuilder = new OperationOptionsBuilder();
        opBuilder.setOption("delay", 10000);

        ConnectorFacade facade1 = ConnectorFacadeFactory.getInstance().newInstance(config);

        Set<Attribute> attrs = CollectionUtil.<Attribute>newReadOnlySet();
        try {
            facade1.create(ObjectClass.ACCOUNT, attrs, opBuilder.build()).getUidValue();
            fail("expected timeout");
        } catch (OperationTimeoutException e) {
            //expected
        }

        try {
            facade1.search(ObjectClass.ACCOUNT, null, (final ConnectorObject obj) -> true, opBuilder.build());
            fail("expected timeout");
        } catch (OperationTimeoutException e) {
            //expected
        }
    }

    static File getTestBundlesDir() throws URISyntaxException {
        URL testOutputDirectory = ConnectorInfoManagerTestBase.class.getResource("/");
        File testBundlesDir = Path.of(testOutputDirectory.toURI()).toFile();
        if (!testBundlesDir.isDirectory()) {
            throw new ConnectorException(testBundlesDir.getPath() + " does not exist");
        }
        return testBundlesDir;
    }

    // Originally, this method used getTestBundlesDir.
    // We stopped doing that in order to allow tests to be run directly from IDE.
    List<URL> getTestBundles() {
        List<URL> rv = new ArrayList<>();
        rv.add(getTestBundleUrl("testbundlev1.jar"));
        rv.add(getTestBundleUrl("testbundlev2.jar"));
        return rv;
    }

    private URL getTestBundleUrl(String name) {
        URL url = ConnectorInfoManagerTestBase.class.getResource("/" + name);
        if (url == null) {
            throw new IllegalStateException("Bundle '" + name + "' could not be found");
        }
        return url;
    }

    /**
     * To be overridden by subclasses to get different ConnectorInfoManagers
     *
     * @return
     * @throws Exception
     */
    protected abstract ConnectorInfoManager getConnectorInfoManager() throws Exception;

    protected abstract void shutdownConnnectorInfoManager();
}
