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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2016-2022 Evolveum
 */
package org.identityconnectors.framework.common.objects;

import java.util.Map;
import java.util.Objects;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;

/**
 * A single-valued attribute that represents the <i>unique identifier</i> of an
 * object within the name-space of the target resource. If possible, this unique
 * identifier also should be immutable.
 * <br/>
 * When an application creates an object on a target resource, the
 * {@link CreateApiOp#create create} operation returns as its result the
 * <code>Uid</code> of the created object. An application also can use the
 * {@link SearchApiOp#search search} operation to discover the <code>Uid</code>
 * value for an existing object. An application must use the <code>Uid</code>
 * value to identify the object in any subsequent call to
 * {@link GetApiOp#getObject get}, {@link DeleteApiOp#delete delete} or
 * {@link UpdateApiOp#update update} that object. See the documentation for
 * {@link Name} for comparison.
 * <br/>
 * Ideally, the value of <code>Uid</code> would be a <i>Globally Unique
 * IDentifier (GUID)</i>. However, not every target resource provides a globally
 * unique and immutable identifier for each of its objects. For some connector
 * implementations, therefore, the <code>Uid</code> value is only <i>locally</i>
 * unique and may change when an object is modified. For instance, an LDAP
 * directory service that lacks GUID might use <i>Distinguished Name (DN)</i> as
 * the <code>Uid</code> for each object. A connector that represents each object
 * as a row in a database table might use the value of the <i>primary key</i> as
 * the <code>Uid</code> of an object. The fact that changing an object might
 * change its <code>Uid</code> is the reason that {@link UpdateApiOp#update
 * update} returns <code>Uid</code>.
 * <br/>
 * {@link Uid} by definition must be a single-valued attribute. Its value must
 * always convert to a string, regardless of the underlying type of the native
 * identifier on the target. The string value of any native id must be
 * canonical.
 * <br/>
 * Uid is never allowed to appear in the {@link Schema}, nor may Uid appear in
 * the attribute set of a {@link CreateApiOp#create create} operation. This is
 * because Uid is not a true attribute of an object, but rather a reference to
 * that object. Uid extends {@link Attribute} only so that Uid can be searchable
 * and compatible with the filter translators.
 */
public final class Uid extends Attribute {

    public static final String NAME = AttributeUtil.createSpecialName("UID");

    private final String revision;

    private final Name nameHint;

    public Uid(String value) {
        super(NAME, CollectionUtil.<Object>newReadOnlyList(check(value)));
        revision = null;
        nameHint = null;
    }

    public Uid(String value, String revision) {
        super(NAME, CollectionUtil.<Object>newReadOnlyList(check(value)));
        if (StringUtil.isBlank(revision)) {
            throw new IllegalArgumentException("Revision value must not be blank!");
        }
        this.revision = revision;
        this.nameHint = null;
    }

    public Uid(String value, Name nameHint) {
        super(NAME, CollectionUtil.<Object>newReadOnlyList(check(value)));
        this.revision = null;
        this.nameHint = nameHint;
    }

    public Uid(String value, String revision, Name nameHint) {
        super(NAME, CollectionUtil.<Object>newReadOnlyList(check(value)));
        if (StringUtil.isBlank(revision)) {
            throw new IllegalArgumentException("Revision value must not be blank!");
        }
        this.revision = revision;
        this.nameHint = nameHint;
    }

    /**
     * Throws an {@link IllegalArgumentException} if the value passed in blank.
     */
    private static String check(String value) {
        if (StringUtil.isBlank(value)) {
            throw new IllegalArgumentException("Uid value must not be blank!");
        }
        return value;
    }

    /**
     * Obtain a string representation of the value of this attribute, which
     * value uniquely identifies a {@link ConnectorObject object} on the target
     * resource.
     *
     * @return value that uniquely identifies an object.
     */
    public String getUidValue() {
        return AttributeUtil.getStringValue(this);
    }

    /**
     * Returns the last known name of the object that is identified by this Uid.
     * This returns the __NAME__ attribute of the object as either the connector
     * or the framework client seen it. This hint can be used to optimize some
     * connector operations (e.g. read operations in environments with several
     * servers). It can also be used to make the rename (update) operations more
     * reliable. It can be used to make the use of QuailifiedUid more useful,
     * e.g. when used in the CONTAINER operation option.
     *
     * @return last known name of the object that is identified by this Uid.
     */
    public Name getNameHint() {
        return nameHint;
    }

    /**
     * Obtain a string representation of the value of name hint.
     * Convenience method.
     *
     * @return string representation of the name hint.
     */
    public String getNameHintValue() {
        return AttributeUtil.getStringValue(nameHint);
    }

    /**
     * Return the string representation of the revision value of the
     * <br/>
     * The revision number specifies a given version ot the
     * {@link ConnectorObject object} identified by the
     * {@link org.identityconnectors.framework.common.objects.Uid#getUidValue()}
     *
     * @return null if the connector does not support the MVCC and does not set
     * this value otherwise return the revision number of the object.
     */
    public String getRevision() {
        return revision;
    }

    @Override
    protected void extendToStringMap(final Map<String, Object> map) {
        super.extendToStringMap(map);
        if (revision != null) {
            map.put("Revision", revision);
        }
        if (nameHint != null) {
            map.put("NameHint", nameHint);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Uid uid = (Uid) o;
        return Objects.equals(revision, uid.revision)
                && Objects.equals(nameHint, uid.nameHint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), revision, nameHint);
    }
}
