/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Evolveum. All rights reserved.
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
package org.identityconnectors.framework.common.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.FrameworkUtil;

public class AttributeDeltaBuilder {

    private final static String NAME_ERROR = "Name must not be blank!";

    private final static String COLLISION_ERROR =
            "Collision, valuesToReplace, valuesToAdd and valuesToRemove can't be used together!";

    private final static String COLLISION_ERROR_REPLACE =
            "Collision, valuesToReplace can't be used together with valuesToAdd or valuesToRemove!";

    private final static String COLLISION_ERROR_ADDORREMOVE =
            "Collision, valuesToAdd or valuesToRemove can't be used together with valuesToReplace!";

    private String name;

    private List<Object> valuesToAdd;

    private List<Object> valuesToRemove;

    private List<Object> valuesToReplace;

    /**
     * Creates a attributeDelta with the specified name and a {@code null} value
     * for valuesToAdd, valuesToRemove and valuesToReplace.
     *
     * @param name
     * unique name of the attributeDelta.
     * @return instance of {@code AttributeDelta} with a {@code null} or actual values for
     * valuesToAdd, valuesToRemove and valuesToReplace.
     */
    public static AttributeDelta build(final String name) {
        AttributeDeltaBuilder bld = new AttributeDeltaBuilder();
        bld.setName(name);
        return bld.build();
    }

    /**
     * Creates an {@code AttributeDelta} with the name and the values provided
     * for valuesToAdd and valuesToRemove.
     *
     * @param name
     * unique name of the attributeDelta.
     * @param valuesToAdd
     * a collection of objects that are used as values for the
     * valuesToAdd of the attributeDelta.
     * @param valuesToRemove
     * a collection of objects that are used as values for the
     * valuesToRemove of the attributeDelta.
     * @return instance of {@code AttributeDelta} with the specified name and a
     * value that includes the arguments provided.
     */
    public static AttributeDelta build(
            final String name, final Collection<?> valuesToAdd, final Collection<?> valuesToRemove) {

        AttributeDeltaBuilder bld = new AttributeDeltaBuilder();
        bld.setName(name);
        bld.addValueToAdd(valuesToAdd);
        bld.addValueToRemove(valuesToRemove);
        return bld.build();
    }

    /**
     * Creates an {@code AttributeDelta} with the name and the values provided
     * for valuesToReplace.
     *
     * @param name
     * unique name of the attributeDelta.
     * @param args
     * variable number of arguments that are used as values for the
     * valuesToReplace of the attributeDelta.
     * @return instance of {@code AttributeDelta} with the specified name and a
     * value that includes the arguments provided.
     */
    public static AttributeDelta build(final String name, final Object... args) {
        AttributeDeltaBuilder bld = new AttributeDeltaBuilder();
        bld.setName(name);
        bld.addValueToReplace(args);
        return bld.build();
    }

    /**
     * Creates an {@code AttributeDelta} with the name and the values provided
     * for valuesToReplace.
     *
     * @param name
     * unique name of the attributeDelta.
     * @param valuesToReplace
     * a collection of objects that are used as values for the
     * valuesToReplace of the attributeDelta.
     * @return instance of {@code AttributeDelta} with the specified name and a
     * value that includes the arguments provided.
     */
    public static AttributeDelta build(final String name, final Collection<?> valuesToReplace) {
        AttributeDeltaBuilder bld = new AttributeDeltaBuilder();
        bld.setName(name);
        bld.addValueToReplace(valuesToReplace);
        return bld.build();
    }

    /**
     * Get the name of the attributeDelta that is being built.
     *
     * @return The name of the attributeDelta.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the attributeDelta that is being built.
     *
     * @throws IllegalArgumentException
     * if the name parameter is blank.
     * @return instance of {@code AttributeDelta} with the specified name and a
     * value that includes the arguments provided.
     */
    public AttributeDeltaBuilder setName(final String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException(NAME_ERROR);
        }
        this.name = name;
        return this;
    }

    /**
     * Return any current value of the valuesToAdd in the attributeDelta that is
     * being built.
     *
     * @return any current value of the valuesToAdd in the attributeDelta that
     * is being built.
     */
    public List<Object> getValuesToAdd() {
        return valuesToAdd == null ? null : CollectionUtil.asReadOnlyList(valuesToAdd);
    }

    /**
     * Return any current value of the valuesToRemove in the the attributeDelta
     * that is being built.
     *
     * @return any current value of the valuesToRemove in the attributeDelta
     * that is being built.
     */
    public List<Object> getValueToRemove() {
        return valuesToRemove == null ? null : CollectionUtil.asReadOnlyList(valuesToRemove);
    }

    /**
     * Return any current value of the valuesToReplace in the attribute that is
     * being built.
     *
     * @return any current value of the valuesToReplace in the attribute that is
     * being built.
     */
    public List<Object> getValueToReplace() {
        return valuesToReplace == null ? null : CollectionUtil.asReadOnlyList(valuesToReplace);
    }

    /**
     * Adds each object in the collection as a value for the valuesToAdd of the
     * attributeDelta that is being built.
     *
     * @param obj
     * the values to add for ValueToAdd
     * @return instance of {@code AttributeDelta} with the specified name and a
     * value that includes the arguments provided.
     */
    public AttributeDeltaBuilder addValueToAdd(final Collection<?> obj) {
        if (valuesToReplace == null) {
            valuesToAdd = addValuesInternal(obj, valuesToAdd);
        } else {
            throw new IllegalArgumentException(COLLISION_ERROR_ADDORREMOVE);
        }
        return this;
    }

    /**
     * Adds each of the specified objects as a value for the valuesToAdd of the
     * attributeDelta that is being built.
     *
     * @param objs
     * the values to add for ValueToAdd
     * @return instance of {@code AttributeDelta} with the specified name and a
     * value that includes the arguments provided.
     */
    public AttributeDeltaBuilder addValueToAdd(final Object... objs) {
        if (valuesToReplace == null) {
            if (objs != null) {
                valuesToAdd = addValuesInternal(Arrays.asList(objs), valuesToAdd);
            }
        } else {
            throw new IllegalArgumentException(COLLISION_ERROR_ADDORREMOVE);
        }
        return this;
    }

    /**
     * Adds each object in the collection as a value for the valuesToRemove of
     * the attributeDelta that is being built.
     *
     * @param obj
     * the values to add for ValueToRemove
     * @return a new attributeDelta with the name and any values of the
     * valuesToAdd that have been provided to the builder.
     *
     * @throws NullPointerException
     * if any of the values is null.
     * @throws IllegalArgumentException
     * if no name has been provided.
     */
    public AttributeDeltaBuilder addValueToRemove(final Collection<?> obj) {
        if (valuesToReplace == null) {
            valuesToRemove = addValuesInternal(obj, valuesToRemove);
        } else {
            throw new IllegalArgumentException(COLLISION_ERROR_ADDORREMOVE);
        }
        return this;
    }

    /**
     * Adds each of the specified objects as a value for the valuesToRemove of
     * the attributeDelta that is being built.
     *
     * @param objs
     * the values to add for ValueToRemove
     * @return instance of {@code AttributeDelta} with the specified name and a
     * value that includes the arguments provided.
     */
    public AttributeDeltaBuilder addValueToRemove(final Object... objs) {
        if (valuesToReplace == null) {
            if (objs != null) {
                valuesToRemove = addValuesInternal(Arrays.asList(objs), valuesToRemove);
            }
        } else {
            throw new IllegalArgumentException(COLLISION_ERROR_ADDORREMOVE);
        }
        return this;
    }

    /**
     * Adds each object in the collection as a value for the valuesToReplace of
     * the attributeDelta that is being built.
     *
     * @param obj
     * the values to add for ValueToReplace
     * @return a new attributeDelta with the name and any values of the
     * valuesToRemove that have been provided to the builder.
     *
     * @throws NullPointerException
     * if any of the values is null.
     * @throws IllegalArgumentException
     * if no name has been provided.
     */
    public AttributeDeltaBuilder addValueToReplace(final Collection<?> obj) {
        if (valuesToAdd == null && valuesToRemove == null) {
            valuesToReplace = addValuesInternal(obj, valuesToReplace);
        } else {
            throw new IllegalArgumentException(COLLISION_ERROR_REPLACE);
        }
        return this;
    }

    /**
     * Adds each of the specified objects as a value for the valuesToReplace of
     * the attributeDelta that is being built.
     *
     * @param objs
     * the values to add for ValueToReplace
     * @return instance of {@code AttributeDelta} with the specified name and a
     * value that includes the arguments provided.
     */
    public AttributeDeltaBuilder addValueToReplace(final Object... objs) {
        if (valuesToAdd == null && valuesToRemove == null) {
            if (objs != null) {
                valuesToReplace = addValuesInternal(Arrays.asList(objs), valuesToReplace);
            }
        } else {
            throw new IllegalArgumentException(COLLISION_ERROR_REPLACE);
        }
        return this;
    }

    /**
     * Creates a attributeDelta with the specified name and the value for
     * valuesToAdd, valuesToRemove and valuesToReplace that have been provided
     * to the builder.
     *
     * @return instance of {@code AttributeDelta} with name and
     * {@code List<Object>} for valuesToAdd, valuesToRemove and
     * valuesToReplace.
     * @throws IllegalArgumentException
     * if no name has been provided.
     */
    public AttributeDelta build() {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException(NAME_ERROR);
        }
        if (valuesToReplace != null && (valuesToAdd != null || valuesToRemove != null)) {
            throw new IllegalArgumentException(COLLISION_ERROR);
        }
        if (Uid.NAME.equals(name)) {
            System.out.println("UID");
            return new AttributeDelta(Uid.NAME, null, null, getSingleStringValue());
        } else if (Name.NAME.equals(name)) {
            return new AttributeDelta(Name.NAME, null, null, getSingleStringValue());
        }
        return new AttributeDelta(name, valuesToAdd, valuesToRemove, valuesToReplace);
    }

    /**
     * Confirm that the attributeDelta that is being built has at most a single
     * value.
     *
     * @throws IllegalArgumentException
     * if the attribute contains more than a single value.
     */
    private void checkSingleValue() {
        if (valuesToReplace == null || valuesToReplace.size() != 1) {
            throw new IllegalArgumentException(
                    "ValueToReplace of attributeDelta '" + name + "' must be a single value, but it has "
                    + (valuesToReplace == null ? null : valuesToReplace.size()) + "values");
        }
    }

    /**
     * @return the single string value of the attributeDelta that is being built.
     * @throws IllegalArgumentException
     * if the attribute contains more than a single value, or if the
     * value is not of type {@code String}.
     */
    private List<Object> getSingleStringValue() {
        checkSingleValue();
        System.out.println("get" + valuesToReplace.get(0).getClass().toString());
        if (!(valuesToReplace.get(0) instanceof String)) {
            System.out.println("ou");
            throw new IllegalArgumentException("ValueToReplace of attributeDelta '" + name
                    + "' must be an instance of String.");
        }
        return Arrays.asList(valuesToReplace.get(0));
    }

    private List<Object> addValuesInternal(final Iterable<?> values, List<Object> ListValues) {
        if (values != null) {
            // make sure the list is ready to receive values.
            List<Object> ret = new ArrayList<Object>();
            if (ListValues != null) {
                ret.addAll(ListValues);
            }
            // add each value checking to make sure its correct
            for (Object v : values) {
                System.out.println(v.getClass());
                FrameworkUtil.checkAttributeValue(name, v);
                ret.add(v);
            }
            return ret;
        }
        return ListValues;
    }

    // =======================================================================
    // Operational Attributes
    // =======================================================================
    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the date and time that a password will expire on a target
     * system or application.
     *
     * @param dateTime
     * UTC time in milliseconds.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#PASSWORD_EXPIRATION_DATE_NAME
     *         pre-defined name for password expiration date}.
     */
    public static AttributeDelta buildPasswordExpirationDate(final Date dateTime) {
        return buildPasswordExpirationDate(dateTime.getTime());
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the date and time that a password will expire on a target
     * system or application.
     *
     * @param dateTime
     * UTC time in milliseconds.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#PASSWORD_EXPIRATION_DATE_NAME
     *         pre-defined name for password expiration date}.
     */
    public static AttributeDelta buildPasswordExpirationDate(final long dateTime) {
        return build(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME, dateTime);
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the password of an object on a target system or application.
     *
     * @param password
     * the string that represents a password.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#PASSWORD_NAME predefined name
     *         for password}.
     */
    public static AttributeDelta buildPassword(final GuardedString password) {
        return build(OperationalAttributes.PASSWORD_NAME, password);
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the password of an object on a target system or application.
     * <p>
     * The caller is responsible for clearing out the array of characters.
     *
     * @param password
     * the characters that represent a password.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#PASSWORD_NAME predefined name
     *         for password}.
     */
    public static AttributeDelta buildPassword(final char[] password) {
        return buildPassword(new GuardedString(password));
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the current password of an object on a target system or
     * application.
     * <p>
     * Passing the current password indicates the account owner (and not an
     * administrator) is changing the password. The use case is that an
     * administrator password change may not keep history or validate against
     * policy.
     *
     * @param password
     * the string that represents a password.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#CURRENT_PASSWORD_NAME
     *         predefined name for current password}.
     */
    public static AttributeDelta buildCurrentPassword(final GuardedString password) {
        return build(OperationalAttributes.CURRENT_PASSWORD_NAME, password);
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the current password of an object on a target system or
     * application.
     * <p>
     * Passing the current password indicates the account owner (and not an
     * administrator) is changing the password. The use case is that an
     * administrator password change may not keep history or validate against
     * policy.
     * <p>
     * The caller is responsible for clearing out the array of characters.
     *
     * @param password
     * the characters that represent a password.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#CURRENT_PASSWORD_NAME
     *         predefined name for current password}.
     */
    public static AttributeDelta buildCurrentPassword(final char[] password) {
        return buildCurrentPassword(new GuardedString(password));
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents whether object is enabled on a target system or application.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} or
     * {@link org.identityconnectors.framework.api.operations.UpdateApiOp} to
     * enable or disable an object.</li>
     * <li>Read this attribute from
     * {@link org.identityconnectors.framework.api.operations.GetApiOp} to
     * determine whether an object currently is enabled or disabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} to select objects that
     * are enabled or to select objects that are disabled.</li>
     * </ul>
     *
     * @param value
     * true indicates the object is enabled; otherwise false.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#ENABLE_NAME predefined name for
     *         enabled}.
     */
    public static AttributeDelta buildEnabled(final boolean value) {
        return build(OperationalAttributes.ENABLE_NAME, value);
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the date and time to enable an object on a target system or
     * application.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} or
     * {@link org.identityconnectors.framework.api.operations.UpdateApiOp} to
     * set a date and time to enable an object.</li>
     * <li>Read this attribute from
     * {@link org.identityconnectors.framework.api.operations.GetApiOp} to
     * determine when an object will be enabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} to select objects that
     * are scheduled to be enabled at a certain date and time.</li>
     * </ul>
     *
     * @param date
     * The date and time to enable a particular object.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#ENABLE_DATE_NAME predefined
     *         name for enable date}.
     */
    public static AttributeDelta buildEnableDate(final Date date) {
        return buildEnableDate(date.getTime());
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the date and time to enable an object on a target system or
     * application. The date-and-time parameter is UTC in milliseconds.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} or
     * {@link org.identityconnectors.framework.api.operations.UpdateApiOp} to
     * set a date and time to enable an object.</li>
     * <li>Read this attribute from
     * {@link org.identityconnectors.framework.api.operations.GetApiOp} to
     * determine when an object will be enabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} to select objects that
     * are scheduled to be enabled at a certain date and time.</li>
     * </ul>
     *
     * @param date
     * The date and time (UTC in milliseconds) to enable a particular
     * object.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#ENABLE_DATE_NAME predefined
     *         name for enable date}.
     */
    public static AttributeDelta buildEnableDate(final long date) {
        return build(OperationalAttributes.ENABLE_DATE_NAME, date);
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the date and time to disable an object on a target system or
     * application.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} or
     * {@link org.identityconnectors.framework.api.operations.UpdateApiOp} to
     * set a date and time to disable an object.</li>
     * <li>Read this attribute from
     * {@link org.identityconnectors.framework.api.operations.GetApiOp} to
     * determine when an object will be disabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} to select objects that
     * are scheduled to be disabled at a certain date and time.</li>
     * </ul>
     *
     * @param date
     * The date and time to disable a particular object.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#DISABLE_DATE_NAME predefined
     *         name for disable date}.
     */
    public static AttributeDelta buildDisableDate(final Date date) {
        return buildDisableDate(date.getTime());
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents the date and time to disable an object on a target system or
     * application. The date-and-time parameter is UTC in milliseconds.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} or
     * {@link org.identityconnectors.framework.api.operations.UpdateApiOp} to
     * set a date and time to disable an object.</li>
     * <li>Read this attribute from
     * {@link org.identityconnectors.framework.api.operations.GetApiOp} to
     * determine when an object will be disabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} to select objects that
     * are scheduled to be disabled at a certain date and time.</li>
     * </ul>
     *
     * @param date
     * The date and time (UTC in milliseconds) to disable a
     * particular object.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#DISABLE_DATE_NAME predefined
     *         name for disable date}.
     */
    public static AttributeDelta buildDisableDate(final long date) {
        return build(OperationalAttributes.DISABLE_DATE_NAME, date);
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents whether an object is locked out on a target system or
     * application.
     * <ul>
     * <li>Read this attribute from
     * {@link org.identityconnectors.framework.api.operations.GetApiOp} to
     * determine whether an object is currently locked out.</li>
     * <li>Use this attribute with
     * {@link org.identityconnectors.framework.api.operations.UpdateApiOp} to
     * clear the lock-out status of an object (or to set the lock-out status of
     * an object).</li>
     * <li>Use this attribute with {@link SearchApiOp} to select objects that
     * are currently locked out (or to select objects that are not currently
     * locked out).</li>
     * </ul>
     *
     * @param lock
     * true if the object is locked out; otherwise false.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#LOCK_OUT_NAME predefined name
     *         for lockout state}.
     */
    public static AttributeDelta buildLockOut(final boolean lock) {
        return build(OperationalAttributes.LOCK_OUT_NAME, lock);
    }

    /**
     * Builds an {@linkplain AttributeDelta of operational attribute} that
     * represents whether the password of an object is expired on a target
     * system or application.
     * <ul>
     * <li>Read this attribute from
     * {@link org.identityconnectors.framework.api.operations.GetApiOp} to
     * determine whether the password of an object is currently expired.</li>
     * <li>Use this attribute with
     * {@link org.identityconnectors.framework.api.operations.UpdateApiOp} to
     * expire the password of an object (or to clear the expired status of the
     * password of an object).</li>
     * <li>Use this attribute with {@link SearchApiOp} to select objects that
     * have passwords that are currently expired (or to select objects that have
     * passwords that are not currently expired).</li>
     * </ul>
     *
     * @param value
     * from the API true expires and from the SPI its shows its
     * either expired or not.
     * @return an {@code AttributeDelta} with the
     * {@linkplain OperationalAttributes#PASSWORD_EXPIRED_NAME
     *         predefined name for password expiration state}.
     */
    public static AttributeDelta buildPasswordExpired(final boolean value) {
        return build(OperationalAttributes.PASSWORD_EXPIRED_NAME, value);
    }

    // =======================================================================
    // Pre-defined Attributes
    // =======================================================================
    /**
     * Builds an {@linkplain AttributeDelta of pre-defined attribute} that
     * represents the date and time of the most recent login for an object (such
     * as an account) on a target system or application.
     *
     * @param date
     * The date and time of the last login.
     * @return an {@code AttributeDelta} with the
     * {@linkplain PredefinedAttributes#LAST_LOGIN_DATE_NAME predefined
     *         name for password expiration state}.
     */
    public static AttributeDelta buildLastLoginDate(final Date date) {
        return buildLastLoginDate(date.getTime());
    }

    /**
     * Builds an {@linkplain AttributeDelta of pre-defined attribute} that
     * represents the date and time of the most recent login for an object (such
     * as an account) on a target system or application.
     * <p>
     * The time parameter is UTC in milliseconds.
     *
     * @param date
     * The date and time (UTC in milliseconds) of the last login.
     * @return an {@code AttributeDelta} with the
     * {@linkplain PredefinedAttributes#LAST_LOGIN_DATE_NAME predefined
     *         name for password expiration state}.
     */
    public static AttributeDelta buildLastLoginDate(final long date) {
        return build(PredefinedAttributes.LAST_LOGIN_DATE_NAME, date);
    }

    /**
     * Builds an {@linkplain AttributeDelta of pre-defined attribute} that
     * represents the date and time that the password was most recently changed
     * for an object (such as an account) on a target system or application.
     *
     * @param date
     * The date and time that the password was most recently changed.
     * @return an {@code AttributeDelta} with the
     * {@linkplain PredefinedAttributes#LAST_PASSWORD_CHANGE_DATE_NAME
     *         predefined name for password expiration state}.
     */
    public static AttributeDelta buildLastPasswordChangeDate(final Date date) {
        return buildLastPasswordChangeDate(date.getTime());
    }

    /**
     * Builds an {@linkplain AttributeDelta of pre-defined attribute} that
     * represents the date and time that the password was most recently changed
     * for an object (such as an account) on a target system or application.
     * <p>
     * The time parameter is UTC in milliseconds.
     *
     * @param date
     * The date and time that the password was most recently changed.
     * @return an {@code AttributeDelta} with the
     * {@linkplain PredefinedAttributes#LAST_PASSWORD_CHANGE_DATE_NAME
     *         predefined name for password expiration state}.
     */
    public static AttributeDelta buildLastPasswordChangeDate(final long date) {
        return build(PredefinedAttributes.LAST_PASSWORD_CHANGE_DATE_NAME, date);
    }

    /**
     * Builds an {@linkplain AttributeDelta of pre-defined attribute} that
     * represents how often the password must be changed for an object (such as
     * an account) on a target system or application.
     * <p>
     * The value for this attribute is expressed in milliseconds.
     *
     * @param value
     * The number of milliseconds between the time that the password
     * was most recently changed and the time when the password must
     * be changed again.
     * @return an {@code AttributeDelta} with the
     * {@linkplain PredefinedAttributes#PASSWORD_CHANGE_INTERVAL_NAME
     *         predefined name for password expiration state}.
     */
    public static AttributeDelta buildPasswordChangeInterval(final long value) {
        return build(PredefinedAttributes.PASSWORD_CHANGE_INTERVAL_NAME, value);
    }
}
