package org.identityconnectors.test.common;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PropertyBagTests {
    private PropertyBag bag = createBag();

    @Test
    public void testGetProperty() {
        assertEquals("value1", bag.getProperty("key1", String.class));
        assertNull(bag.getProperty("key2", String.class));
        assertEquals(new Integer(1), bag.getProperty("key3", Integer.class));
        assertEquals(new Long(1), bag.getProperty("key5", Long.class));

        // try not existing
        try {
            bag.getProperty("key4", String.class);
            fail("Get Property must fail for unexisting property");
        } catch (IllegalArgumentException e) {
        }

        // Try cast
        try {
            bag.getProperty("key3", Long.class);
            fail("Get Property with incompatible type must fail on ClassCastException");
        } catch (ClassCastException e) {
        }

    }

    @Test
    public void testGetPropertyWithDef() {
        assertEquals("value1", bag.getProperty("key1", String.class, "def"));
        assertNull(bag.getProperty("key2", String.class, "def"));
        assertEquals("def", bag.getProperty("key4", String.class, "def"));
        assertNull(bag.getProperty("key4", String.class, null));
    }

    @Test
    public void testGetStringProperty() {
        assertEquals("value1", bag.getStringProperty("key1"));
        assertNull(bag.getStringProperty("key2"));
        // Try cast
        try {
            bag.getStringProperty("key3");
            fail("Get Property with incompatible type must fail on ClassCastException");
        } catch (ClassCastException e) {
        }

    }

    private PropertyBag createBag() {
        Map<String, Object> bag = new HashMap<String, Object>();
        bag.put("key1", "value1");
        bag.put("key2", null);
        bag.put("key3", new Integer(1));
        bag.put("key5", new Long(1));
        return new PropertyBag(bag);
    }

}
