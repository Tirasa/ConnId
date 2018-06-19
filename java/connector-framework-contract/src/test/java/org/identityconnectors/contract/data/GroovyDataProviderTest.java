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
package org.identityconnectors.contract.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * unit test for GroovyDataProvider
 *
 * @author David Adam
 *
 * Note: if getting MissingMethodException, update imports in configfile.groovy
 *
 */
public class GroovyDataProviderTest {

    private static final String NON_EXISTING_PROPERTY = "abcdefghi123asiosfjds";

    private static GroovyDataProvider gdp;

    public static final String CONFIG_FILE_NAME = "configfileTest.groovy";

    private static URL getConfigFileUrl() {
        return GroovyDataProviderTest.class.getResource(CONFIG_FILE_NAME);
    }

    @BeforeEach
    public void setUp() {
        gdp = new GroovyDataProvider(getConfigFileUrl());
    }

    @AfterEach
    public void tearDown() {
        gdp = null;
    }

    @Test
    public void testListAcquire() throws Exception {
        Object o = getProperty(gdp, "sampleFooBarList");
        assertNotNull(o);
        assertTrue(o instanceof List<?>);
        assertTrue(((List<?>) o).size() == 3);
        @SuppressWarnings("unchecked") // because of list retyping to List<Object>
        List<Object> l = (List<Object>) o;
        int iter = 0;
        for (Object object : l) {
            switch (iter) {
                case 0:
                    assertTrue(object.equals("a"));
                    break;
                case 1:
                    assertTrue(object.equals("b"));
                    break;
                case 2:
                    assertTrue(object.equals("b"));
                    break;
            }
            iter++;
        }
    }

    @Test
    public void testListAcquireWithLazy() throws Exception {
        Object o = getProperty(gdp, "sampleFooBarListWithLazy");
        assertNotNull(o);
        assertTrue(o instanceof List<?>);
        assertTrue(((List<?>) o).size() == 3);
        @SuppressWarnings("unchecked") // collection retyping
        List<Object> l = (List<Object>) o;
        int iter = 0;
        for (Object object : l) {
            switch (iter) {
                case 0:
                    assertTrue(object.equals("a"));
                    break;
                case 1:
                    assertTrue(object.equals("b"));
                    break;
                case 2:
                    assertTrue(object instanceof String);
                    assertTrue(object.toString().contains("X"));
                    break;
            }
            iter++;
        }
    }

    @Test
    public void testSimpleStr() throws Exception {
        assertEquals(gdp.get("aSimpleString", "string", true),
                "If you think you can do a thing or think you can't do a thing, you're right. (H. Ford)");
    }

    @Test
    public void testProperDefaulting() {
        Object o = get("nonexistingAttribute");
        assertNotNull(o);
        assertTrue(o instanceof String);
        String nonExistingAttribute = (String) o;

        Object o2 = get("nonexistingAttribute");
        assertNotNull(o2);
        assertTrue(o2 instanceof String);
        String nonExistingAttribute2 = (String) o2;

        final String message = "if we query the same attribute twice, it should return the same default value";
        assertTrue(nonExistingAttribute.equals(nonExistingAttribute2), message);

        Object o3 = get("anotherNonExistingAttribute");
        assertNotNull(o3);
        assertTrue(o3 instanceof String);
        String anotherNonExistingAttribute = (String) o3;

        assertTrue(
                !anotherNonExistingAttribute.equals(nonExistingAttribute),
                "different properties should return different 'generated' values!");

        Object o4 = get("anotherNonExistingAttribute");
        assertNotNull(o4);
        assertTrue(o4 instanceof String);
        String anotherNonExistingAttribute2 = (String) o4;

        assertTrue(anotherNonExistingAttribute.equals(anotherNonExistingAttribute2), message);
    }

    /** helper method of {@link GroovyDataProviderTest#testProperDefaulting()};
     * in case of missing value should return default one.
     */
    private Object get(String string) {
        return gdp.get(String.class, string, "foocomponent");
    }

    @SuppressWarnings("unchecked") // collection retyping
    @Test
    public void testProperDefaultingMulti() {
        Object o = getMulti("nonexistingAttribute");
        assertNotNull(o);
        assertTrue(o instanceof List && ((List<Object>) o).get(0) instanceof String);
        List<Object> nonExistingAttribute = (List<Object>) o;

        Object o2 = getMulti("nonexistingAttribute");
        assertNotNull(o2);
        assertTrue(o2 instanceof List && ((List<Object>) o2).get(0) instanceof String);
        List<Object> nonExistingAttribute2 = (List<Object>) o2;

        final String message = "if we query the same attribute twice, it should return the same default value";
        assertTrue(nonExistingAttribute
                .equals(nonExistingAttribute2), message);

        Object o3 = getMulti("anotherNonExistingAttribute");
        assertNotNull(o3);
        assertTrue(o3 instanceof List && ((List<Object>) o3).get(0) instanceof String);
        List<Object> anotherNonExistingAttribute = (List<Object>) o3;

        //TODO fix problem with uniform unique values.
        assertTrue(
                !anotherNonExistingAttribute.equals(nonExistingAttribute),
                "different properties should return different 'generated' values!");

        Object o4 = getMulti("anotherNonExistingAttribute");
        assertNotNull(o4);
        assertTrue(o4 instanceof List && ((List<Object>) o4).get(0) instanceof String);
        List<Object> anotherNonExistingAttribute2 = (List<Object>) o4;

        assertTrue(anotherNonExistingAttribute.equals(anotherNonExistingAttribute2), message);
    }

    /** helper method of {@link GroovyDataProviderTest#testProperDefaultingMulti()
     * in case of missing value should return default one.
     */
    private Object getMulti(String string) {
        return gdp.get(String.class, string, "foocomponent", 0, true);
    }

    public void testNonExistingProperty() throws Exception {
        assertThrows(ObjectNotFoundException.class, () -> {
            Object o = getProperty(gdp, NON_EXISTING_PROPERTY);
            assertNotNull(o);
            assertTrue(o instanceof ConfigObject);
            if (o instanceof ConfigObject) {
                ConfigObject co = (ConfigObject) o;
                assertEquals(co.size(), 0);
            }
        });
    }

    @Test
    public void testSimpleMapAcquire() throws Exception {
        Object o = getProperty(gdp, "sampleMap");
        assertNotNull(o);
        assertTrue(o instanceof Map<?, ?>);
    }

    @Test
    public void testDotInNameMapAcquire() throws Exception {
        Object o = getProperty(gdp, "sampleMap.foo.bar");
        assertNotNull(o);
        assertTrue(o instanceof Map<?, ?>);
    }

    @Test
    public void testRecursiveAcquire() throws Exception {
        // query for a property with non-existing prefix foo
        // the DataProvider should try to evaluate substrings of the property
        // name (divided by .)
        // and find "abc"
        Object o = getProperty(gdp, "foo.abc");
        assertNotNull(o);
        assertEquals(o.toString(), "abc");
    }

    @Test
    public void testDotNameString() throws Exception {
        Object o = getProperty(gdp, "eggs.spam.sausage");
        assertNotNull(o);
        assertTrue(o instanceof String);
        assertEquals(o.toString(), "the spanish inquisition");
    }

    @Test
    public void testRandom() throws Exception {
        Object o = getProperty(gdp, "random");
        Object o2 = getProperty(gdp, "random");
        assertNotNull(o);
        assertEquals(o, o2);
    }

    @Test
    public void testRandomHierarchicalName() throws Exception {
        Object o = getProperty(gdp, "foo.bla.horror.random");
        Object o2 = getProperty(gdp, "foo.bla.horror.random");
        assertNotNull(o);
        assertEquals(o, o2);
    }

    @Test
    public void configFileMerger() {
        ConfigSlurper cs = new ConfigSlurper();

        ConfigObject co1 = cs.parse("a = '1'\n b = '2'");
        assert "1" == co1.getProperty("a");
        ConfigObject co2 = cs.parse("c='3'\n d='4'");
        assert "3" == co2.getProperty("c");

        ConfigObject f = GroovyDataProvider.mergeConfigObjects(co1, co2);
        assert "1" == f.getProperty("a");
        assert "3" == f.getProperty("c");
    }

    @Test
    public void configFileMergerAdvanced() {
        ConfigSlurper cs = new ConfigSlurper();

        ConfigObject lowPriorityConfig = cs.parse("a = '1'\n c = '2'");
        assert "1" == lowPriorityConfig.getProperty("a");
        ConfigObject highPriorityConfig = cs.parse("c='3'\n d='4'");
        assert "3" == highPriorityConfig.getProperty("c");

        ConfigObject f = GroovyDataProvider.mergeConfigObjects(
                lowPriorityConfig, highPriorityConfig);
        assert "1" == f.getProperty("a");
        assert "3" == f.getProperty("c");
    }

    @Test
    public void testNewRandomGenerator() throws Exception {
        Object o = getProperty(gdp, "randomNewAge");
        Object o2 = getProperty(gdp, "remus");

        assertNotNull(o);
        assertNotNull(o2);

        assertTrue(o instanceof Long);
        assertTrue(o2 instanceof Integer);
    }

    @Test
    public void testMapAttributesNew() throws Exception {
        {
            Object o = getProperty(gdp, "attributeMap.string");
            assertNotNull(o);
            assertTrue(o instanceof String);
            assertTrue("Good morning!".equals(o.toString()));
        }

        {
            Object o = getProperty(gdp, "attributeMapSecond.stringSec");
            assertNotNull(o);
            assertTrue(o instanceof String);
            assertTrue("Good morning Mrs. Smith!".equals(o.toString()));
        }

        {
            Object o = getProperty(gdp, "Delete.account.__NAME__.string");
            assertNotNull(o);
            assertTrue(o instanceof String);
            assertTrue("blaf".equals(o.toString()));
        }

        {
            Object o = getProperty(gdp, "account.__NAME__.string");
            assertNotNull(o);
            assertTrue(o instanceof String);
            assertTrue("blaf blaf".equals(o.toString()));
        }
    }

    @Test
    public void literalsMacroReplacementTest() throws Exception {
        Object o = getProperty(gdp, "Tfloat");
        assertNotNull(o);
        assertTrue(o instanceof Float);
    }

    @Test
    public void multiStringListTest() throws Exception {
        // multi.Tstring=[Lazy.random("AAAAA##") , Lazy.random("AAAAA##")]
        Object obj = getProperty(gdp, "multi.Tstring");
        assertNotNull(obj);
        assertTrue(obj instanceof List<?>);
        if (obj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Object> objects = (List<Object>) obj;
            Object previous = null;
            for (Object object : objects) {
                String msg = String.format(
                        "default value for Random multiString should contain Strings only, "
                        + "but found different type: %s", object.getClass().getName());
                assertTrue(object instanceof String, msg);

                if (previous != null) {
                    msg = String.format(
                            "two objects on the randomized list should differ, but there are two equal values: %s = %s",
                            previous, object);
                    assertTrue(!previous.equals(object), msg);
                }
                previous = object;
            }
        }
    }

    @Test
    public void multiStringRecursiveTest() throws Exception {
        Object o = getProperty(gdp, "multi.recursive.Tstring");
        assertNotNull(o);
        assertTrue(o instanceof List<?>);
        if (o instanceof List<?>) {
            @SuppressWarnings("unchecked") // collection retyping
            List<Object> l = (List<Object>) o;

            boolean recursiveListPresent = false;
            boolean recursiveListPresent2 = false;
            for (Object object : l) {
                if (object instanceof List<?>) {
                    recursiveListPresent = true;

                    @SuppressWarnings("unchecked") // collection retyping
                    List<Object> lRec = (List<Object>) object;
                    for (Object object2 : lRec) {
                        if (object2 instanceof List<?>) {
                            recursiveListPresent2 = true;
                        }
                    }
                }
            }
            assertTrue(recursiveListPresent);
            assertTrue(recursiveListPresent2);
        }
    }

    @Test
    public void testByteArray() throws Exception {
        Object o = getProperty(gdp, "byteArray.test");
        assertNotNull(o);
        assertTrue(o instanceof byte[]);
    }

    @Test
    public void characterTest() throws Exception {
        Object o = getProperty(gdp, "charTest");
        assertNotNull(o);
        assertTrue(o instanceof Character);
    }

    public void testNonExistingDefault() throws Exception {
        assertThrows(ObjectNotFoundException.class, () -> {
            // should not return default vale
            getProperty(gdp, "connector.login");
        });
    }

    @Test
    public void testNestedPropertyQuery() throws Exception {
        Object o = getProperty(gdp, "SchemaXX.sample");
        assertNotNull(o);
        assertTrue("Mysterious universe" == o.toString() && o instanceof String);
    }

    @Test
    public void testAtAtPropertyNamesQuery() throws Exception {
        Object o = getProperty(gdp, "Schema.__NAME__.attribute.account");
        assertNotNull(o);
        assertTrue(o instanceof String && "Ahoj ship!".equals(o.toString()));
    }

    /**
     * tests {@link GroovyDataProvider#getShortTypeName(Class)} method.
     */
    @Test
    public void testGetShortTypeName() {
        assertTrue(GroovyDataProvider.getShortTypeName(String.class).equals("Tstring"));
        byte[] barr = new byte[0];
        assertTrue(GroovyDataProvider.getShortTypeName(barr.getClass()).equals("Tbytearray"));
        assertTrue(GroovyDataProvider.getShortTypeName(GuardedString.class).equals("Tguardedstring"));
    }

    @Test
    /**
     * test {@link GroovyDataProvider#get(String)}
     * test {@link GroovyDataProvider#get(String, int)}
     */
    public void testSimpleGet() {
        DataProvider dp = (DataProvider) gdp;
        assertTrue(dp.get("aaa.bbb.xxx").equals("ahoj"));
        assertTrue(dp.get("param", 9).equals("foobar"));
    }

    @Test
    /**
     * Test queries for non-existing parameter "foo.bar"
     * and expects a List to return based on one defined in multi.Tstring.
     * (resides in bootsrap.groovy)
     */
    @SuppressWarnings("unchecked") // collection retyping
    public void testDefaultValues() {
        DataProvider dp = (DataProvider) gdp;
        Object o = dp.get(String.class, "bar", "foo", -1, true);
        assertNotNull(o);
        assertTrue(o instanceof List && ((List<Object>) o).size() > 0);
        List<Object> l = (List<Object>) o;
        assertTrue(l.get(0) instanceof String);
    }

    /**
     * Tests the resolving of Lazy values inside map
     */
    @Test
    public void testLazyMap() {
        Object o = ((DataProvider) gdp).get("mapWithLazyCalls");
        assertNotNull(o);
        assertTrue(o instanceof Map<?, ?>);
        @SuppressWarnings("unchecked") // collection retyping
        Map<Object, Object> m = (Map<Object, Object>) o;
        int cntr = 0;
        m.entrySet().forEach((current) -> {
            if (cntr == 0 || cntr == 1) {
                assertTrue(current.getValue() instanceof String);
            } else {
                fail("should not be more than two items in the map");
            }
        });
    }

    /**
     * test acquiring a map all at once (good for unit tests)
     */
    @Test
    public void testAcquireMap() {
        Object o = ((DataProvider) gdp).get("abcAccount.all");
        assertNotNull(o);
        assertTrue(o instanceof Map<?, ?>);
        @SuppressWarnings("unchecked") // collection retyping
        Map<Object, Object> m = (Map<Object, Object>) o;
        assertTrue(m.get("__NAME__") instanceof String);
        assertTrue(((String) m.get("__NAME__")).startsWith("CONUSR-"));
        assertTrue(m.get("__PASSWORD__") instanceof String);
        assertTrue(((String) m.get("__PASSWORD__")).equals("tstpwd"));
    }

    /* ************* UTILITY METHODS ***************** */
    private Object getProperty(GroovyDataProvider gdp2, String propertyName) throws Exception {
        return gdp2.get(propertyName, "string", true);
    }

    /* *********************** SNAPSHOT GENERATING FEATURE TESTS ************************** */
    /** Test of Lazy.get() and Lazy.random() */
    @Test
    public void getPropertyTest() {
        assertTrue(gdp.get("rumulus", null, false).equals(gdp.get("rumulus", null, false)));
        assertTrue(gdp.get("remus", null, false).equals(gdp.get("rumulus", null, false)));
    }

    /** Test of left sides for the snapshot output */
    @Test
    public void testSnapshotGenerating() throws IOException {
        gdp.writeDataToFile();

        // read the file line by line
        List<String> lines = readLines(getConfigFileUrl());

        // parse and control the properties in the written file
        // FOR NOW just left side from the assigment is controlled.
        parseAndControl(lines);
    }

    @Test
    public void testCombinedLazyValue() {
        String firstName = (String) gdp.get("Xfirst");
        assertNotNull(firstName);
        String lastName = (String) gdp.get("Xlast");
        assertNotNull(firstName);
        String fullName = (String) gdp.get("Xfull");
        assertNotNull(firstName);

        String msg = String.format(
                "Error, doesn't fulfill the concatenation: \n firstname: '%s' lastname: '%s' fullname: '%s'", firstName,
                lastName, fullName);
        assertTrue(String.format("%s %s", firstName, lastName).equals(fullName), msg);
    }

    @Test
    public void testGetAttributeSet() throws Exception {
        Set<Attribute> as = gdp.getAttributeSet("abcAccount.tst");

        assertNotNull(as);
        assertEquals(15, as.size());

        as.forEach((attr) -> {
            if (attr.getName().equals("name")) {
                assertEquals(attr.getValue().get(0), "String");
            } else if (attr.getName().equals("id")) {
                assertEquals(attr.getValue().get(0), 15);
            } else if (attr.getName().equals("arl")) {
                assertEquals(attr.getValue().size(), 2);
                assertEquals(attr.getValue().get(0), "elm1");
                assertEquals(attr.getValue().get(1), "elm2");
            } else if (attr.getName().equals("ara")) {
                assertEquals(attr.getValue().size(), 2);
                assertEquals(attr.getValue().get(0), "elm1");
                assertEquals(attr.getValue().get(1), "elm2");
            } else if (attr.getName().equals("bool")) {
                assertEquals(attr.getValue().get(0), true);
            } else {
                fail("Unexpected attribute");
            }
        });
    }

    @Test
    public void testGuardedStringDefaulting() {
        Object defaultedValue = ((DataProvider) gdp).get(GuardedString.class, "nonexistingAttributeFooBarBaz", "");
        assertNotNull(defaultedValue);
        assertTrue(defaultedValue instanceof GuardedString);
        GuardedString gs = (GuardedString) defaultedValue;
        gs.access((char[] clearChars) -> {
            String result = new String(clearChars);
            assertTrue(result.length() > 0);
        });
    }

    @Test
    public void testGuardedStringSuccess() {
        Object seekedValue = ((DataProvider) gdp).get(GuardedString.class, "generatedPassword", "");
        assertNotNull(seekedValue);
        assertTrue(seekedValue instanceof GuardedString);
        GuardedString gs = (GuardedString) seekedValue;
        gs.access((char[] clearChars) -> {
            String result = new String(clearChars);
            assertTrue(result.length() > 0 && result.endsWith("ahoj"));
        });
    }

    @Test
    public void testGuardedStringUniqueness() {
        Object defaultedValue1 = ((DataProvider) gdp).get(GuardedString.class, "nonexistingAttributeFooBarBaz_123", "");
        Object defaultedValue2 = ((DataProvider) gdp).get(GuardedString.class, "nonexistingAttributeFooBarBaz_456", "");
        // second query should return the same value
        assertEquals(((DataProvider) gdp).get(GuardedString.class, "nonexistingAttributeFooBarBaz_123", ""),
                defaultedValue1);
        assertEquals(((DataProvider) gdp).get(GuardedString.class, "nonexistingAttributeFooBarBaz_456", ""),
                defaultedValue2);
        // the two passwords should be unique
        assertFalse(defaultedValue1.equals(defaultedValue2));
    }

    /**
     * method controls, if single parameters are correctly quoted, and multi
     * params.
     *
     * Correct quotation means: foo."bar"."boo" = "baa" bar = "baa"
     *
     * @param lines
     */
    private void parseAndControl(List<String> lines) {
        lines.stream().
                filter((currentLine) -> (currentLine.contains(GroovyDataProvider.ASSIGNMENT_MARK))).
                map((currentLine) -> currentLine.split(GroovyDataProvider.ASSIGNMENT_MARK)).
                filter((arr) -> (arr.length == 2)).
                map((arr) -> arr[0]).
                map((leftPart) -> {
                    assertTrue(!leftPart.equals(""));
                    // split the left side based on "." separators
                    return leftPart;
                }).map((leftPart) -> leftPart.split(GroovyDataProvider.PROPERTY_SEPARATOR)).
                forEachOrdered((subparts) -> {
                    for (int i = 0; i < subparts.length; i++) {
                        if (i == 0) {
                            assertTrue(!subparts[i]
                                    .startsWith(GroovyDataProvider.PROPERTY_SEPARATOR));
                            assertTrue(!subparts[i]
                                    .endsWith(GroovyDataProvider.PROPERTY_SEPARATOR));
                        } else {
                            assertTrue(subparts[i]
                                    .startsWith(GroovyDataProvider.PROPERTY_SEPARATOR));
                            assertTrue(subparts[i]
                                    .endsWith(GroovyDataProvider.PROPERTY_SEPARATOR));
                        }
                    }
                }); // divide the line based on "=" delimiter
    }

    /** read lines from given file line by line */
    private List<String> readLines(URL url) throws IOException {
        List<String> result = new ArrayList<>();

        // get the lines of the original property file
        try (BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line = null; // not declared within while loop

            while ((line = input.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }
}
