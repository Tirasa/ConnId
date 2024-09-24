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
 * Portions Copyrighted 2015-2016 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import static org.identityconnectors.framework.common.objects.NameUtil.nameHashCode;
import static org.identityconnectors.framework.common.objects.NameUtil.namesEqual;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.serializer.SerializerUtil;

/**
 * <i>AttributeInfo</i> is meta data responsible for describing an
 * {@link Attribute}. It can be programmatically determined at runtime or
 * statically constructed. The class determines if an {@link Attribute} is
 * required, readable, writable, or nullable. In also includes the native type
 * and name. It is recommended that date fields be represented as a long with
 * time zone UTC. It should be up to the display or separate attributes if the
 * time zone is necessary.
 */
public final class AttributeInfo {

    private final String name;

    private final Class<?> type;

    private final String subtype;

    private final String nativeName;

    private final Set<Flags> flags;

    private final String referencedObjectClassName;

    private final String roleInReference;

    /**
     * Enum of modifier flags to use for attributes. Note that this enum is
     * designed for configuration by exception such that an empty set of flags
     * are the defaults:
     * <ul>
     * <li>updateable</li>
     * <li>creatable</li>
     * <li>returned by default</li>
     * <li>readable</li>
     * <li>single-valued</li>
     * <li>optional</li>
     * </ul>
     */
    public static enum Flags {
        REQUIRED,
        MULTIVALUED,
        NOT_CREATABLE,
        NOT_UPDATEABLE,
        NOT_READABLE,
        NOT_RETURNED_BY_DEFAULT

    }

    /**
     * Enumeration of pre-defined attribute subtypes.
     */
    public static enum Subtypes {
        /**
         * Case-ignore (case-insensitive) string.
         */
        STRING_CASE_IGNORE(AttributeUtil.createSpecialName("STRING_CASE_IGNORE")),

        /**
         * Unique Resource Identifier (RFC 3986)
         */
        STRING_URI(AttributeUtil.createSpecialName("STRING_URI")),

        /**
         * LDAP Distinguished Name (RFC 4511)
         */
        STRING_LDAP_DN(AttributeUtil.createSpecialName("STRING_LDAP_DN")),

        /**
         * Universally unique identifier (UUID)
         */
        STRING_UUID(AttributeUtil.createSpecialName("STRING_UUID")),

        /**
         * XML-formatted string (https://www.w3.org/TR/REC-xml/)
         */
        STRING_XML(AttributeUtil.createSpecialName("STRING_XML")),

        /**
         * JSON-formatted string
         */
        STRING_JSON(AttributeUtil.createSpecialName("STRING_JSON"));

        private final String value;

        private Subtypes(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * The role of an object in the relationship (provided by specific reference attribute).
     * We call this object a "holder" below.
     *
     * @see #getRoleInReference()
     */
    public enum RoleInReference {

        /**
         * The holder can be considered a subject of the relationship. It is the usual source (starting point)
         * of relationship navigation, i.e. we usually ask "what objects does the subject hold", not the
         * way around.
         *
         * Typical example: account or other type of group member (when regarding group membership relation).
         */
        SUBJECT(AttributeUtil.createSpecialName("SUBJECT")),

        /**
         * The holder can be considered an object of the relationship. It is the usual target (ending point)
         * of relationship navigation.
         *
         * Typical example: the group that has some members (when regarding group membership relation).
         */
        OBJECT(AttributeUtil.createSpecialName("OBJECT"));

        private final String value;

        private RoleInReference(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    AttributeInfo(final String name, final Class<?> type, final String subtype, final String nativeName,
            final Set<Flags> flags) {
        this(name, type, subtype, nativeName, flags, null, null);
    }

    AttributeInfo(final String name, final Class<?> type, final String subtype, final String nativeName,
            final Set<Flags> flags,
            String referencedObjectClassName, String roleInReference) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalStateException("Name must not be blank!");
        }
        if ((OperationalAttributes.PASSWORD_NAME.equals(name) || OperationalAttributes.CURRENT_PASSWORD_NAME
                .equals(name))
                && !GuardedString.class.equals(type)) {
            throw new IllegalArgumentException(
                    "Password based attributes must be of type GuardedString.");
        }
        Assertions.nullCheck(flags, "flags");
        // check the type..
        FrameworkUtil.checkAttributeType(type);
        this.name = name;
        this.type = type;
        this.subtype = subtype;
        this.nativeName = nativeName;
        this.flags = Collections.unmodifiableSet(EnumSet.copyOf(flags));
        if (!isReadable() && isReturnedByDefault()) {
            throw new IllegalArgumentException(
                    "Attribute "
                    + name
                    + " is flagged as not-readable, so it should also be as not-returned-by-default.");
        }
        if ((referencedObjectClassName != null || roleInReference != null)
                && !ConnectorObjectReference.class.equals(type)) {

            throw new IllegalArgumentException(
                    "Referenced object class name and/or role in reference can be set only for reference attributes.");
        }
        this.referencedObjectClassName = referencedObjectClassName;
        this.roleInReference = roleInReference;
    }

    /**
     * The name of the attribute. This the attribute name as it is known by the
     * framework. It may be derived from the native attribute name. Or it may
     * be one of the special names such as __NAME__ or __PASSWORD__.
     *
     * @return the name of the attribute its describing.
     */
    public String getName() {
        return name;
    }

    /**
     * The basic type associated with this attribute. All primitives are
     * supported.
     *
     * @return the native type if uses.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Optional subtype of the attribute. This defines a subformat or provides
     * more specific definition what the attribute contains. E.g. it may define
     * that the attribute contains case-insensitive string, URL, LDAP distinguished
     * name and so on.
     *
     * The subtype may contain one of the pre-defined subtypes
     * (a value form the Subtype enumeration). The subtype may also contain an URI
     * that specifies a custom subtype that the connector recognizes and it is not
     * defined in the pre-defined subtype enumeration.
     *
     * For {@link ConnectorObjectReference} attributes, the subtype - if present - contains
     * the connector-wide identification of the reference. This is important especially for
     * bi-directional relations (like the group membership), where the client might be interested
     * that the reference attribute (e.g.) {@code group} on the {@code user} object is bound
     * to the reference attribute (e.g.) {@code member} on the {@code group} object.
     *
     * This feature is optional: the subtype may be missing for a reference attribute.
     *
     * @return attribute subtype.
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * The native name of the attribute. This is the attribute name as it is
     * known by the resource. It is especially useful for attributes with
     * special names such as __NAME__ or __PASSWORD__. In this case the
     * nativeName will contain the real name of the attribute.
     * The nativeName may be null. In such a case it is assumed that the
     * native name is the same as name.
     *
     * @return the native name of the attribute its describing.
     */
    public String getNativeName() {
        return nativeName;
    }

    /**
     * Returns the set of flags associated with the attribute.
     *
     * @return the set of flags associated with the attribute
     */
    public Set<Flags> getFlags() {
        return flags;
    }

    /**
     * Determines if the attribute is readable.
     *
     * @return true if the attribute is readable else false.
     */
    public boolean isReadable() {
        return !flags.contains(Flags.NOT_READABLE);
    }

    /**
     * Determines if the attribute is writable on create.
     *
     * @return true if the attribute is writable on create else false.
     */
    public boolean isCreateable() {
        return !flags.contains(Flags.NOT_CREATABLE);
    }

    /**
     * Determines if the attribute is writable on update.
     *
     * @return true if the attribute is writable on update else false.
     */
    public boolean isUpdateable() {
        return !flags.contains(Flags.NOT_UPDATEABLE);
    }

    /**
     * Determines whether this attribute is required for creates.
     *
     * @return true if the attribute is required for an object else false.
     */
    public boolean isRequired() {
        return flags.contains(Flags.REQUIRED);
    }

    /**
     * Determines if this attribute can handle multiple values.
     *
     * There is a special case with byte[] since in most instances this denotes
     * a single object.
     *
     * @return true if the attribute is multi-value otherwise false.
     */
    public boolean isMultiValued() {
        return flags.contains(Flags.MULTIVALUED);
    }

    /**
     * Determines if the attribute is returned by default.
     *
     * Indicates if an {@link Attribute} will be returned during
     * {@link SearchApiOp}, {@link SyncApiOp} or {@link GetApiOp} inside a
     * {@link ConnectorObject} by default. The default value is
     * <code>true</code>.
     *
     * @return false if the attribute should not be returned by default.
     */
    public boolean isReturnedByDefault() {
        return !flags.contains(Flags.NOT_RETURNED_BY_DEFAULT);
    }

    /**
     * For reference attributes, this method returns the object class of referenced objects.
     *
     * It is optional: the connector may not have this information, or sometimes, there may be more than a single object
     * class that can be referenced by the attribute. (For example, {@code member} attribute on the
     * {@code group} object can reference accounts, groups, and other kinds of objects.)
     */
    public String getReferencedObjectClassName() {
        return referencedObjectClassName;
    }

    /**
     * For reference attributes, this method provides an indication of the role the holding object plays in the
     * reference.
     * The standard roles are described in {@link RoleInReference} enumeration.
     *
     * May be null if not known or not supported.
     */
    public String getRoleInReference() {
        return roleInReference;
    }

    public boolean isReference() {
        return ConnectorObjectReference.class.equals(type);
    }

    /**
     * Determines if the name parameter matches this {@link AttributeInfo}.
     */
    public boolean is(String name) {
        return namesEqual(this.name, name);
    }

    // =======================================================================
    // Object Overrides
    // =======================================================================
    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof AttributeInfo) {
            AttributeInfo other = (AttributeInfo) obj;
            if (!is(other.getName())) {
                return false;
            }
            if (!getType().equals(other.getType())) {
                return false;
            }
            if (!CollectionUtil.equals(flags, other.flags)) {
                return false;
            }
            if (!Objects.equals(referencedObjectClassName, other.referencedObjectClassName)) {
                return false;
            }
            return Objects.equals(roleInReference, other.roleInReference);
        }
        return ret;
    }

    @Override
    public int hashCode() {
        return nameHashCode(name);
    }

    @Override
    public String toString() {
        return SerializerUtil.serializeXmlObject(this, false);
    }
}
