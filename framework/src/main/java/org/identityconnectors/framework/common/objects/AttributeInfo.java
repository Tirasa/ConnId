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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.common.objects;

import static org.identityconnectors.framework.common.objects.NameUtil.nameHashCode;
import static org.identityconnectors.framework.common.objects.NameUtil.namesEqual;

import java.util.Collections;
import java.util.EnumSet;
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

	private final String _name;
	private final Class<?> _type;
	private final Set<Flags> _flags;

	/**
	 * Enum of modifier flags to use for attributes. Note that
	 * this enum is designed for configuration by exception such that
	 * an empty set of flags are the defaults:
	 * <ul>
     *     <li>updateable</li>
     *     <li>creatable</li>
     *     <li>returned by default</li>
     *     <li>readable</li>
     *     <li>single-valued</li>
     *     <li>optional</li>
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
	
	AttributeInfo(final String name, final Class<?> type,
	        final Set<Flags> flags) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalStateException("Name must not be blank!");
        }
        if ((OperationalAttributes.PASSWORD_NAME.equals(name) ||
                OperationalAttributes.CURRENT_PASSWORD_NAME.equals(name)) &&
                !GuardedString.class.equals(type)) {
            final String MSG = "Password based attributes must be of type GuardedString.";
            throw new IllegalArgumentException(MSG);
        }
        Assertions.nullCheck(flags, "flags");
        // check the type..
        FrameworkUtil.checkAttributeType(type);
		_name = name;
		_type = type;
		_flags = Collections.unmodifiableSet(EnumSet.copyOf(flags));
		if (!isReadable() && isReturnedByDefault()) {
		    throw new IllegalArgumentException("Attribute "+name+" is flagged as not-readable, so it should also be as not-returned-by-default.");
		}
	}

	/**
	 * The native name of the attribute.
	 * 
	 * @return the native name of the attribute its describing.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * The basic type associated with this attribute. All primitives are
	 * supported.
	 * 
	 * @return the native type if uses.
	 */
	public Class<?> getType() {
		return _type;
	}
	
	/**
	 * Returns the set of flags associated with the attribute.
	 * @return the set of flags associated with the attribute
	 */
	public Set<Flags> getFlags() {
	    return _flags;
	}

	/**
	 * Determines if the attribute is readable.
	 * 
	 * @return true if the attribute is readable else false.
	 */
	public boolean isReadable() {
		return !_flags.contains(Flags.NOT_READABLE);
	}

	/**
	 * Determines if the attribute is writable on create.
	 * 
	 * @return true if the attribute is writable on create else false.
	 */
	public boolean isCreateable() {
        return !_flags.contains(Flags.NOT_CREATABLE);
	}

	/**
	 * Determines if the attribute is writable on update.
	 * 
	 * @return true if the attribute is writable on update else false.
	 */
	public boolean isUpdateable() {
		return !_flags.contains(Flags.NOT_UPDATEABLE);
	}

	/**
	 * Determines whether this attribute is required for creates.
	 * 
	 * @return true if the attribute is required for an object else false.
	 */
	public boolean isRequired() {
		return _flags.contains(Flags.REQUIRED);
	}

	/**
	 * Determines if this attribute can handle multiple values. There is a
	 * special case with byte[] since in most instances this denotes a single
	 * object.
	 * 
	 * @return true if the attribute is multi-value otherwise false.
	 */
	public boolean isMultiValued() {
		return _flags.contains(Flags.MULTIVALUED);
	}

	/**
	 * Determines if the attribute is returned by default. Indicates if an
	 * {@link Attribute} will be returned during {@link SearchApiOp}, {@link SyncApiOp}
	 * or {@link GetApiOp} inside a {@link ConnectorObject} by default. The default
	 * value is <code>true</code>.
	 * 
	 * @return false iff the attribute should not be returned by default.
	 */
	public boolean isReturnedByDefault() {
		return !_flags.contains(Flags.NOT_RETURNED_BY_DEFAULT);
	}

	/**
	 * Determines if the name parameter matches this {@link AttributeInfo}.
	 */
	public boolean is(String name) {
	    return namesEqual(_name, name);
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
			if (!CollectionUtil.equals(_flags, other._flags)) {
			    return false;
			}
			return true;
		}
		return ret;
	}

	@Override
	public int hashCode() {
	    return nameHashCode(_name);
	}

	@Override
	public String toString() {
	    return SerializerUtil.serializeXmlObject(this, false);
	}

}
