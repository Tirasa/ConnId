package org.identityconnectors.test.common;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates a read-only bag of properties, which can be accessed 
 * in a type-safe manner.
 * 
 * <p>The simplest way to obtain a required (i.e., the property must be in the bag,
 * otherwise an exception is thrown) property value is {@link #getProperty(String, Class)}.
 * If the property is not a required one, the {@link #getProperty(String, Class, Object)} method 
 * can be used, which also takes a default value which is returned when the property is not
 * present in the bag.</p>
 */
public final class PropertyBag {

    private final Map<String, Object> bag;

    PropertyBag(Map<String, Object> bag) {
        this.bag = new HashMap<String, Object>(bag);
    }

    /**
     * Gets the value of a required property in a type-safe manner. If no property exists with the given name,
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param <T> the type of the property.
     * @param name the name of the property.
     * @param type the {@link Class} representing the type of the property.
     * @return the value of the property in bag; <code>null</code> if the value of the property was <code>null</code>.
     * @throws IllegalArgumentException if no property with the given name name exists in the bag.
     * @throws ClassCastException if the property exists, but is not of the specified type.
     */
    public <T> T getProperty(String name, Class<T> type) {
        if (!bag.containsKey(name)) {
            throw new IllegalArgumentException(MessageFormat.format("Property named \"{0}\" not found in bag", name));
        }
        return castValue(name, type);
    }

    /**
     * Gets a property from the bag and casts it to the specified type, while providing a nice error
     * message to the caller when the property value is not of the specified type. Returns <code>null</code>
     * if the property does not exists or its value is <code>null</code>.
     */
    private <T> T castValue(String name, Class<T> type) {
        Object value = bag.get(name);
        // This means the property value is null, so return null.
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException(MessageFormat.format("Property named \"{0}\" is of type \"{1}\" but expected type was \"{2}\"",
                    name, value.getClass(), type));
        }
        return type.cast(value);

    }

    /**
     * Gets a property value, returning a default value when no property with the specified name exists in the bag.
     *
     * @param <T> the type of the property.
     * @param name the name of the property.
     * @param type the {@link Class} representing the type of the property.
     * @param def the default value returned when no property with the specified name exists in the bag.
     * @return the value of the property in bag or the default value; <code>null</code> if the value of the property 
     *             was <code>null</code>.
     * @throws ClassCastException if the property exists, but is not of the specified type.
     */
    public <T> T getProperty(String name, Class<T> type, T def) {
        if (!bag.containsKey(name)) {
            return def;
        }
        return castValue(name, type);
    }

    /**
     * Gets a required property value known to be of string type. The method expects that the value 
     * is an instance of {@link String}. It does not attempt to call {@link Object#toString()} on the value.
     * 
     * @param name the name of the property.
     * @return the value of the property in bag; <code>null</code> if the value of the property was <code>null</code>.
     * @throws IllegalArgumentException if no property with the given name exists in the bag.
     * @throws ClassCastException if the property exists, but is not an instance of {@link String}.
     */
    public String getStringProperty(String name) {
        return getProperty(name, String.class);
    }

    @Override
    public String toString() {
        return bag.toString();
    }

    Map<String, Object> toMap() {
        return bag;
    }

}
