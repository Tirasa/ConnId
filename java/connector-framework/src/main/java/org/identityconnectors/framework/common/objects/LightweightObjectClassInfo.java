package org.identityconnectors.framework.common.objects;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

import java.util.Objects;

import static org.identityconnectors.framework.common.objects.NameUtil.nameHashCode;
import static org.identityconnectors.framework.common.objects.NameUtil.namesEqual;


/**
 * Definition of an object class without the attributeInfos.
 */
public class LightweightObjectClassInfo {

    private final String type;

    private final boolean isContainer;

    private final boolean isAuxiliary;

    private final boolean isEmbedded;

    private final String description;

    /**
     * Public only for serialization; Use LightweightObjectClassInfoBuilder instead.
     *
     * @param type The name of the object class
     * @param isContainer True if this can contain other object classes.
     */
    public LightweightObjectClassInfo(
            final String type,
            final boolean isContainer,
            final boolean isAuxiliary,
            final boolean isEmbedded) {

        this(type, isContainer, isAuxiliary, isEmbedded, null);
    }

    /**
     * Public only for serialization; Use LightweightObjectClassInfoBuilder instead.
     *
     * @param type The name of the object class
     * @param isContainer True if this can contain other object classes.
     * @param description The description of the object class.
     */

    public LightweightObjectClassInfo(
            final String type,
            final boolean isContainer,
            final boolean isAuxiliary,
            final boolean isEmbedded,
            final String description) {

        Assertions.nullCheck(type, "type");
        this.type = type;
        this.isContainer = isContainer;
        this.isAuxiliary = isAuxiliary;
        this.isEmbedded = isEmbedded;
        this.description = description;
        // check to make sure name exists and if not throw
    }

    public boolean isContainer() {
        return isContainer;
    }

    /**
     * Returns flag indicating whether this is a definition of auxiliary object class.
     * Auxiliary object classes define additional characteristics of the object.
     */
    public boolean isAuxiliary() {
        return isAuxiliary;
    }

    /**
     * If {@code true}, objects of this class are meant to be embedded in other objects.
     * (They may or may not be queryable or updatable directly.)
     *
     * Currently, this information serves just as a hint for the client code. In the future,
     * we may relax some of requirements on embedded objects, for example, they may not need to have
     * the {@link Name} and/or {@link Uid} attributes.
     */
    public boolean isEmbedded() {
        return isEmbedded;
    }

    public String getType() {
        return type;
    }

    /**
     * Returns the description of this object class.
     * Can be used to determine the potential use of the object class.
     * @return a string description of this object class
     */
    public String getDescription() {
        return description;
    }
    /**
     * Determines if the 'name' matches this {@link LightweightObjectClassInfo}.
     *
     * @param name case-insensitive string representation of the LightweightObjectClassInfo's type.
     * @return <code>true</code> if the case insensitive type is equal to that of the one in this
     * {@link LightweightObjectClassInfo}.
     */
    public boolean is(final String name) {
        return namesEqual(type, name);
    }

    @Override
    public boolean equals(final Object obj) {
        // test identity
        if (this == obj) {
            return true;
        }
        // test for null..
        if (obj == null) {
            return false;
        }
        // test that the exact class matches
        if (!(getClass().equals(obj.getClass()))) {
            return false;
        }

        LightweightObjectClassInfo other = (LightweightObjectClassInfo) obj;

        if (!is(other.getType())) {
            return false;
        }
        if (!isContainer == other.isContainer) {
            return false;
        }
        if (!isAuxiliary == other.isAuxiliary) {
            return false;
        }

        if (!Objects.equals(description, other.getDescription())) {
            return false;
        }

        return !isEmbedded != other.isEmbedded;
    }

    @Override
    public int hashCode() {
        return nameHashCode(type);
    }

    @Override
    public String toString() {
        return SerializerUtil.serializeXmlObject(this, false);
    }
}
