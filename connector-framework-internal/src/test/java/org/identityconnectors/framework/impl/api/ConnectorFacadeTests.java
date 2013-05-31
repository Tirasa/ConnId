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
package org.identityconnectors.framework.impl.api;

import static org.identityconnectors.framework.common.objects.ObjectClass.ACCOUNT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.ScriptContextBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.mockconnector.MockAllOpsConnector;
import org.identityconnectors.mockconnector.MockConfiguration;
import org.identityconnectors.mockconnector.MockConnector;
import org.identityconnectors.mockconnector.MockConnector.Call;
import org.identityconnectors.mockconnector.MockUpdateConnector;
import org.identityconnectors.test.common.TestHelpers;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConnectorFacadeTests {

    // =======================================================================
    // Setup/Tear down
    // =======================================================================
    @BeforeMethod
    public void setup() {
        // always reset the call patterns..
        MockConnector.reset();
    }

    // =======================================================================
    // Helper Methods
    // =======================================================================
    private interface TestOperationPattern {
        /**
         * Simple call back to make the 'facade' calls.
         */
        public void makeCall(ConnectorFacade facade);

        /**
         * Given the list of calls determine if they match expected values based
         * on the calls made in the {@link #makeCall(ConnectorFacade)} method.
         */
        public void checkCalls(List<MockConnector.Call> calls);
    }

    /**
     * Test the pattern of the common operations.
     *
     * @throws ClassNotFoundException
     */
    @Test(enabled = false)
    private void testCallPattern(TestOperationPattern pattern) {
        testCallPattern(pattern, MockAllOpsConnector.class);
    }

    @Test(enabled = false)
    private void testCallPattern(TestOperationPattern pattern, Class<? extends Connector> clazz) {
        Configuration config = new MockConfiguration(false);
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        // **test only**
        APIConfiguration impl = TestHelpers.createTestConfiguration(clazz, config);
        ConnectorFacade facade;
        facade = factory.newInstance(impl);
        // make the call on the connector facade..
        pattern.makeCall(facade);
        // check the call structure..
        List<MockConnector.Call> calls = MockConnector.getCallPattern();
        // check the call pattern..
        assertEquals(calls.remove(0).getMethodName(), "init");
        pattern.checkCalls(calls);
        assertEquals(calls.remove(0).getMethodName(), "dispose");
        assertTrue(calls.isEmpty());
    }

    // =======================================================================
    // Tests
    // =======================================================================
    /**
     * Tests that if an SPI operation is not implemented that the API will throw
     * an {@link UnsupportedOperationException}.
     */
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void unsupportedOperationTest() {
        Configuration config = new MockConfiguration(false);
        Class<? extends Connector> clazz = MockConnector.class;
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(clazz, config);
        ConnectorFacade facade;
        facade = factory.newInstance(impl);
        facade.authenticate(ObjectClass.ACCOUNT, "fadf", new GuardedString("fadsf".toCharArray()),
                null);
    }

    @Test
    public void runScriptOnConnectorCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                facade.runScriptOnConnector(new ScriptContextBuilder("lang", "script").build(),
                        null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "runScriptOnConnector");
            }
        });
    }

    @Test
    public void runScriptOnResourceCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                facade.runScriptOnResource(new ScriptContextBuilder("lang", "script").build(), null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "runScriptOnResource");
            }
        });
    }

    /**
     * Test the call pattern to get the schema.
     */
    @Test
    public void schemaCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                facade.schema();
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "schema");
            }
        });
    }

    @Test
    public void authenticateCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                facade.authenticate(ObjectClass.ACCOUNT, "dfadf", new GuardedString("fadfkj"
                        .toCharArray()), null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "authenticate");
            }
        });
    }

    @Test
    public void resolveUsernameCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                facade.resolveUsername(ObjectClass.ACCOUNT, "dfadf", null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "resolveUsername");
            }
        });
    }

    @Test
    public void createCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                Set<Attribute> attrs = CollectionUtil.<Attribute> newReadOnlySet();
                facade.create(ObjectClass.ACCOUNT, attrs, null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "create");
            }
        });
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void createWithOutObjectClassPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                Set<Attribute> attrs = new HashSet<Attribute>();
                facade.create(null, attrs, null);
            }

            public void checkCalls(List<Call> calls) {
                fail("Should not get here..");
            }
        });
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void createDuplicatAttributesPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                Set<Attribute> attrs = new HashSet<Attribute>();
                attrs.add(AttributeBuilder.build("abc", 1));
                attrs.add(AttributeBuilder.build("abc", 2));
                facade.create(ObjectClass.ACCOUNT, attrs, null);
            }

            public void checkCalls(List<Call> calls) {
                fail("Should not get here..");
            }
        });
    }

    @Test
    public void updateCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                Set<Attribute> attrs = new HashSet<Attribute>();
                attrs.add(AttributeBuilder.build("accountid"));
                facade.update(ObjectClass.ACCOUNT, newUid(0), attrs, null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "update");
            }
        });
    }

    @Test
    public void deleteCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                facade.delete(ObjectClass.ACCOUNT, newUid(0), null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "delete");
            }
        });
    }

    @Test
    public void searchCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                // create an empty results handler..
                ResultsHandler rh = new ResultsHandler() {
                    public boolean handle(ConnectorObject obj) {
                        return true;
                    }
                };
                // call the search method..
                facade.search(ObjectClass.ACCOUNT, null, rh, null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "createFilterTranslator");
                assertEquals(calls.remove(0).getMethodName(), "executeQuery");
            }
        });
    }

    @Test
    public void getCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                // create an empty results handler..
                // call the search method..
                facade.getObject(ObjectClass.ACCOUNT, newUid(0), null);
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "createFilterTranslator");
                assertEquals(calls.remove(0).getMethodName(), "executeQuery");
            }
        });
    }

    @Test
    public void testOpCallPattern() {
        testCallPattern(new TestOperationPattern() {
            public void makeCall(ConnectorFacade facade) {
                facade.test();
            }

            public void checkCalls(List<Call> calls) {
                assertEquals(calls.remove(0).getMethodName(), "test");
            }
        });
    }

    @Test
    public void updateMergeTests() {
        Attribute expected, actual;
        Configuration config = new MockConfiguration(false);
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        Class<? extends Connector> clazz = MockUpdateConnector.class;
        // **test only**
        APIConfiguration impl = TestHelpers.createTestConfiguration(clazz, config);
        impl.setTimeout(GetApiOp.class, APIOperation.NO_TIMEOUT);
        impl.setTimeout(UpdateApiOp.class, APIOperation.NO_TIMEOUT);
        impl.setTimeout(SearchApiOp.class, APIOperation.NO_TIMEOUT);
        ConnectorFacade facade = factory.newInstance(impl);
        // sniff test to make sure we can get an object..
        ConnectorObject obj = facade.getObject(ACCOUNT, newUid(1), null);
        assertEquals(obj.getUid(), newUid(1));
        // ok lets add an attribute that doesn't exist..
        final String ADDED = "somthing to add to the object";
        final String ATTR_NAME = "added";
        Set<Attribute> addAttrSet;
        addAttrSet = CollectionUtil.newSet(obj.getAttributes());
        addAttrSet.add(AttributeBuilder.build(ATTR_NAME, ADDED));
        Name name = obj.getName();
        addAttrSet.remove(name);
        Uid uid =
                facade.addAttributeValues(ACCOUNT, obj.getUid(), AttributeUtil
                        .filterUid(addAttrSet), null);
        // get back the object and see if there are the same..
        addAttrSet.add(name);
        ConnectorObject addO = new ConnectorObject(ACCOUNT, addAttrSet);
        obj = facade.getObject(ObjectClass.ACCOUNT, newUid(1), null);
        assertEquals(obj, addO);
        // attempt to add on to an existing attribute..
        addAttrSet.remove(name);
        uid =
                facade.addAttributeValues(ACCOUNT, obj.getUid(), AttributeUtil
                        .filterUid(addAttrSet), null);
        // get the object back out and check on it..
        obj = facade.getObject(ObjectClass.ACCOUNT, uid, null);
        expected = AttributeBuilder.build(ATTR_NAME, ADDED, ADDED);
        actual = obj.getAttributeByName(ATTR_NAME);
        assertEquals(actual, expected);
        // attempt to delete a value from an attribute..
        Set<Attribute> deleteAttrs = CollectionUtil.newSet(addO.getAttributes());
        deleteAttrs.remove(name);
        uid =
                facade.removeAttributeValues(ACCOUNT, addO.getUid(), AttributeUtil
                        .filterUid(deleteAttrs), null);
        obj = facade.getObject(ObjectClass.ACCOUNT, uid, null);
        expected = AttributeBuilder.build(ATTR_NAME, ADDED);
        actual = obj.getAttributeByName(ATTR_NAME);
        assertEquals(actual, expected);
        // attempt to delete an attribute that doesn't exist..
        Set<Attribute> nonExist = new HashSet<Attribute>();
        nonExist.add(newUid(1));
        nonExist.add(AttributeBuilder.build("does not exist", "asdfe"));
        uid =
                facade.removeAttributeValues(ACCOUNT, addO.getUid(), AttributeUtil
                        .filterUid(nonExist), null);
        obj = facade.getObject(ObjectClass.ACCOUNT, newUid(1), null);
        assertTrue(obj.getAttributeByName("does not exist") == null);
    }

    static Uid newUid(int id) {
        return new Uid(Integer.toString(id));
    }
}
