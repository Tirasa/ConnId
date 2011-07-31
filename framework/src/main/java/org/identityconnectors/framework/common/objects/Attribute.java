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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;


/**
 * Represents a named collection of values within a target object, although
 * the simplest case is a name-value pair (e.g., email, employeeID). Values can
 * be empty, null, or set with various types. Empty and null are supported
 * because it makes a difference on some resources (in particular database
 * resources).
 * <p>
 * The developer of a Connector should use an {@link AttributeBuilder} to
 * construct an instance of Attribute.
 * <p>
 * The precise meaning of an instance of {@code Attribute} depends 
 * on the context in which it occurs.
 * <ul>
 *  <li>When 
 *      {@linkplain org.identityconnectors.framework.api.operations.GetApiOp#getObject 
 *      an object is read} or is returned by 
 *      {@linkplain org.identityconnectors.framework.api.operations.SearchApiOp#search search},
 *      an {@code Attribute} represents the <i>complete state</i> of an attribute 
 *      of the target object, current as of the point in time that the object was read.
 *  </li>
 *  <li>When an {@code Attribute} is supplied to 
 *      {@linkplain org.identityconnectors.framework.api.operations.UpdateApiOp 
 *      the update operation},
 *      the {@code Attribute} represents a change 
 *      to the corresponding attribute of the target object:
 *      <ul>
 *      <li>For calls to 
 *          {@link org.identityconnectors.framework.api.operations.UpdateApiOp#update(ObjectClass, Uid, java.util.Set, OperationOptions) update},
 *          the {@code Attribute} contains the <i>complete, intended state</i> of the attribute.
 *      </li>
 *      <li>When the update type is 
 *          {@link org.identityconnectors.framework.api.operations.UpdateApiOp#addAttributeValues(ObjectClass, Uid, java.util.Set, OperationOptions) addAttributeValues},
 *          the {@code Attribute} contains <i>values to append</i>.
 *      </li>
 *      <li>When the update type is 
 *          {@link org.identityconnectors.framework.api.operations.UpdateApiOp#removeAttributeValues(ObjectClass, Uid, java.util.Set, OperationOptions) removeAttributeValues},
 *          the {@code Attribute} contains <i>values to remove</i>.
 *      </li>
 *      </ul>
 *  </li>
 *  <li>When an {@code Attribute} is used to build a 
 *      {@link org.identityconnectors.framework.common.objects.filter.Filter Filter} 
 *      that is an argument to 
 *      {@linkplain org.identityconnectors.framework.api.operations.SearchApiOp#search search},
 *      an {@code Attribute} represents a <i>subset of the current state</i> of an attribute
 *      that will be used as a search criterion.
 *      Specifically, the {@code Attribute} {@linkplain #getName() names the attribute to match}
 *      and {@linkplain #getValue() contains the values to match}.
 *  </li>
 * </ul>
 * 
 * TODO: define the set of allowed values
 * 
 * @author Will Droste
 * @version $Revision: 1.7 $
 * @since 1.0
 */
public class Attribute {
    /**
     * Name of the {@link Attribute}.
     */
    private final String name;

    /**
     * Values of the {@link Attribute}.
     */
    private final List<Object> value;

    /**
     * Create an attribute.
     */
    Attribute(String name, List<Object> value) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Name must not be blank!");
        }
        if (OperationalAttributes.PASSWORD_NAME.equals(name)
                || OperationalAttributes.CURRENT_PASSWORD_NAME.equals(name)) {
            // check the value..
            if (value == null || value.size() != 1) {
                final String MSG = "Must be a single value.";
                throw new IllegalArgumentException(MSG);
            }
            if (!(value.get(0) instanceof GuardedString)) {
                final String MSG = "Password value must be an instance of GuardedString";
                throw new IllegalArgumentException(MSG);
            }
        }
        // make this case insensitive
        this.name = name;
        // copy to prevent corruption..
        this.value = (value == null) ? null : CollectionUtil.newReadOnlyList(value);
    }

    public String getName() {
        return this.name;
    }

    public List<Object> getValue() {
        return (this.value == null) ? null : Collections
                .unmodifiableList(this.value);
    }

    /**
     * Determines if the 'name' matches this {@link Attribute}.
     * 
     * @param name
     *            case insensitive string representation of the attribute's
     *            name.
     * @return <code>true</code> iff the case insentitive name is equal to
     *         that of the one in {@link Attribute}.
     */
    public boolean is(String name) {
        return namesEqual(this.name, name);
    }

    // ===================================================================
    // Object Overrides
    // ===================================================================
    @Override
    public final int hashCode() {
        return nameHashCode(name);
    }

    @Override
    public String toString() {
        // poor man's consistent toString impl..
        StringBuilder bld = new StringBuilder();
        bld.append("Attribute: ");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("Name", getName());
        map.put("Value", getValue());
        bld.append(map);
        return bld.toString();
    }

    @Override
    public final boolean equals(Object obj) {
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
        // test name field..
        final Attribute other = (Attribute) obj;
        if (!is(other.name)) {
            return false;
        }
        
        if (!CollectionUtil.equals(value, other.value)) {
            return false;
        }
        return true;
    }
}
