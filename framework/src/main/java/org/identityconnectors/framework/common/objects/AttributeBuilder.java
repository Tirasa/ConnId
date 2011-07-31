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


/**
 * Simplifies constructing instances of {@link Attribute}. 
 * A {@code Connector} developer does not need to implement the {@code Attribute} interface.
 * The builder returns an instance of an implementation of {@link Attribute} 
 * that overrides the methods {@code equals()}, {@code hashcode()} and {@code toString()}
 * to provide a uniform and robust class. 
 * This implementation is backed by an {@link ArrayList} that contains the values
 * and preserves the order of those values 
 * (in case the order of values is significant to the target system or application).
 * 
 * @author Will Droste
 * @version $Revision: 1.7 $
 * @since 1.0
 */
public final class AttributeBuilder {

    private final static String NAME_ERROR = "Name must not be blank!";

    String _name;

    List<Object> _value;

    /**
     * Creates a attribute with the specified name and a {@code null} value.
     * 
     * @param name
     *            unique name of the attribute.
     * @return instance of {@code Attribute} with a {@code null} value.
     */
    public static Attribute build(final String name) {
        AttributeBuilder bld = new AttributeBuilder();
        bld.setName(name);
        return bld.build();
    }

    /**
     * Creates an {@code Attribute} with the name and the values provided.
     * 
     * @param name
     *            unique name of the attribute.
     * @param args
     *            variable number of arguments that are used as values for the
     *            attribute.
     * @return instance of {@code Attribute} with the specified name 
     *         and a value that includes the arguments provided.
     */
    public static Attribute build(final String name, final Object... args) {
        AttributeBuilder bld = new AttributeBuilder();
        bld.setName(name);
        bld.addValue(args);
        return bld.build();
    }

    /**
     * Creates an {@code Attribute} with the name and the values provided.
     * 
     * @param name
     *            unique name of the attribute.
     * @param obj
     *            a collection of objects that are used as values for the
     *            attribute.
     * @return instance of {@code Attribute} with the specified name 
     *         and a value that includes the arguments provided.
     */
    public static Attribute build(final String name, final Collection<?> obj) {
        // this method needs to be able to create the sub-classes
        // Name, Uid, ObjectClass
        AttributeBuilder bld = new AttributeBuilder();
        bld.setName(name);
        bld.addValue(obj);
        return bld.build();
    }

    /**
     * Get the name of the attribute that is being built.
     * 
     * @return The name of the attribute.
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the attribute that is being built.
     * 
     * @throws IllegalArgumentException
     *             iff the name parameter is blank.
     */
    public AttributeBuilder setName(final String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException(NAME_ERROR);
        }
        _name = name;
        return this;
    }

    /**
     * Return any current value of the attribute that is being built.
     * @return any current value of the attribute that is being built.
     */
    public List<Object> getValue() {
        return _value == null ? null : CollectionUtil.asReadOnlyList(_value);
    }

    /**
     * Add each of the specified objects 
     * as a values for the attribute that is being built.
     * 
     * @param objs
     *             the values to add
     * @throws NullPointerException
     *             iff any of the values is null.
     */
    public AttributeBuilder addValue(final Object... objs) {
        if (objs != null) {
            addValuesInternal(Arrays.asList(objs));
        }
        return this;
    }

    /**
     * Adds each object in the collection
     * as a value for the attribute that is being built.
     * 
     * @param obj
     *             the values to add
     * @throws NullPointerException
     *             iff any of the values is null.
     */
    public AttributeBuilder addValue(final Collection<?> obj) {
        addValuesInternal(obj);
        return this;
    }

    /**
     * @return a new attribute with the name and any values 
     * that have been provided to the builder.
     * @throws IllegalArgumentException if no name has been provided.
     */
    public Attribute build() {
        if (StringUtil.isBlank(_name)) {
            throw new IllegalArgumentException(NAME_ERROR);
        }
        // check for subclasses and some operational attributes..
        if (Uid.NAME.equals(_name)) {
            return new Uid(getSingleStringValue());
        } else if (Name.NAME.equals(_name)) {
            return new Name(getSingleStringValue());
        }
        return new Attribute(_name, _value);
    }

    /**
     * Confirm that the attribute that is being built
     * has at most a single value.
     * @throws IllegalArgumentException 
     * if the attribute contains more than a single value.
     */
    private void checkSingleValue() {
        if (_value == null || _value.size() != 1) {
            final String MSG = "Must be a single value.";
            throw new IllegalArgumentException(MSG);
        }        
    }
    
    /**
     * @return the single string value of the attribute that is being built.
     * @throws IllegalArgumentException 
     * if the attribute contains more than a single value,
     * or if the value is not of type {@code String}.
     */
    private String getSingleStringValue() {
        checkSingleValue();
        if (!(_value.get(0) instanceof String)) {
            final String MSG = "Attribute value must be an instance of String.";
            throw new IllegalArgumentException(MSG);
        }
        return (String) _value.get(0);
    }

    private void addValuesInternal(final Iterable<?> values) {
        if (values != null) {
            // make sure the list is ready to receive values.
            if (_value == null) {
                _value = new ArrayList<Object>();
            }
            // add each value checking to make sure its correct
            for (Object v : values) {
                FrameworkUtil.checkAttributeValue(v);
                _value.add(v);
            }
        }
    }

    // =======================================================================
    // Operational Attributes
    // =======================================================================
    /**
     * Builds an {@linkplain OperationalAttributes operational attribute}
     * that represents the date and time that a password will expire on a target system or application.
     * 
     * @param dateTime
     *            UTC time in milliseconds.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#PASSWORD_EXPIRATION_DATE_NAME
     * pre-defined name for password expiration date}.
     */
    public static Attribute buildPasswordExpirationDate(final Date dateTime) {
        return buildPasswordExpirationDate(dateTime.getTime());
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents the date/time that a password will expire on a target system or application.
     * 
     * @param dateTime
     *            UTC time in milliseconds.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#PASSWORD_EXPIRATION_DATE_NAME
     * pre-defined name for password expiration date}.
     */
    public static Attribute buildPasswordExpirationDate(final long dateTime) {
        return build(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME,
                dateTime);
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute}
     * that represents the password of an object on a target system or application.
     * 
     * @param password
     *            the string that represents a password.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#PASSWORD_NAME
     * predefined name for password}.
     */
    public static Attribute buildPassword(final GuardedString password) {
        return build(OperationalAttributes.PASSWORD_NAME, password);
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute}
     * that represents the password of an object on a target system or application.
     * <p>
     * The caller is responsible for clearing out the array of characters.
     * 
     * @param password
     *            the characters that represent a password.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#PASSWORD_NAME
     * predefined name for password}.
     */
    public static Attribute buildPassword(final char[] password) {
        return buildPassword(new GuardedString(password));
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents the current password of an object on a target system or application. 
     * <p>
     * Passing the current password indicates the account owner (and not an administrator)
     * is changing the password. The use case is that an administrator password change may
     * not keep history or validate against policy.
     * 
     * @param password
     *            the string that represents a password.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#CURRENT_PASSWORD_NAME
     * predefined name for current password}.
     */
    public static Attribute buildCurrentPassword(final GuardedString password) {
        return build(OperationalAttributes.CURRENT_PASSWORD_NAME, password);
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents the current password of an object on a target system or application. 
     * <p>
     * Passing the current password indicates the account owner (and not an administrator)
     * is changing the password. The use case is that an administrator password change may
     * not keep history or validate against policy.
     * <p>
     * The caller is responsible for clearing out the array of characters.
     * 
     * @param password
     *            the characters that represent a password.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#CURRENT_PASSWORD_NAME
     * predefined name for current password}.
     */
    public static Attribute buildCurrentPassword(final char[] password) {
        return buildCurrentPassword(new GuardedString(password));
    }
    
    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents whether object is enabled on a target system or application.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} 
     * or {@link org.identityconnectors.framework.api.operations.UpdateApiOp}
     * to enable or disable an object.</li>
     * <li>Read this attribute from {@link org.identityconnectors.framework.api.operations.GetApiOp} 
     * to determine whether an object currently is enabled or disabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} 
     * to select objects that are enabled or to select objects that are disabled.</li>
     * </ul>
     * 
     * @param value
     *            true indicates the object is enabled; otherwise false.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#ENABLE_NAME
     * predefined name for enabled}.
     */
    public static Attribute buildEnabled(final boolean value) {
        return build(OperationalAttributes.ENABLE_NAME, value);
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents the date and time to enable an object 
     * on a target system or application.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp}
     * or {@link org.identityconnectors.framework.api.operations.UpdateApiOp}
     * to set a date and time to enable an object.</li>
     * <li>Read this attribute from {@link org.identityconnectors.framework.api.operations.GetApiOp} 
     * to determine when an object will be enabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} 
     * to select objects that are scheduled to be enabled 
     * at a certain date and time.</li>
     * </ul>
     * 
     * @param date
     *            The date and time to enable a particular object.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#ENABLE_DATE_NAME
     * predefined name for enable date}.
     */
    public static Attribute buildEnableDate(final Date date) {
        return buildEnableDate(date.getTime());
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents the date and time to enable an object 
     * on a target system or application.
     * The date-and-time parameter is UTC in milliseconds.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} 
     * or {@link org.identityconnectors.framework.api.operations.UpdateApiOp}
     * to set a date and time to enable an object.</li>
     * <li>Read this attribute from {@link org.identityconnectors.framework.api.operations.GetApiOp} 
     * to determine when an object will be enabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} 
     * to select objects that are scheduled to be enabled 
     * at a certain date and time.</li>
     * </ul>
     * 
     * @param date
     *            The date and time (UTC in milliseconds) to enable a particular object.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#ENABLE_DATE_NAME
     * predefined name for enable date}.
     */
    public static Attribute buildEnableDate(final long date) {
        return build(OperationalAttributes.ENABLE_DATE_NAME, date);
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents the date and time to disable an object 
     * on a target system or application.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} 
     * or {@link org.identityconnectors.framework.api.operations.UpdateApiOp}
     * to set a date and time to disable an object.</li>
     * <li>Read this attribute from {@link org.identityconnectors.framework.api.operations.GetApiOp} 
     * to determine when an object will be disabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} 
     * to select objects that are scheduled to be disabled 
     * at a certain date and time.</li>
     * </ul>
     * 
     * @param date
     *            The date and time to disable a particular object.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#DISABLE_DATE_NAME
     * predefined name for disable date}.
     */
    public static Attribute buildDisableDate(final Date date) {
        return buildDisableDate(date.getTime());
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents the date and time to disable an object 
     * on a target system or application.
     * The date-and-time parameter is UTC in milliseconds.
     * <ul>
     * <li>Use this attribute with {@link CreateApiOp} 
     * or {@link org.identityconnectors.framework.api.operations.UpdateApiOp}
     * to set a date and time to disable an object.</li>
     * <li>Read this attribute from {@link org.identityconnectors.framework.api.operations.GetApiOp} 
     * to determine when an object will be disabled.</li>
     * <li>Use this attribute with {@link SearchApiOp} 
     * to select objects that are scheduled to be disabled 
     * at a certain date and time.</li>
     * </ul>
     * 
     * @param date
     *            The date and time (UTC in milliseconds) to disable a particular object.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#DISABLE_DATE_NAME
     * predefined name for disable date}.
     */
    public static Attribute buildDisableDate(final long date) {
        return build(OperationalAttributes.DISABLE_DATE_NAME, date);
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents whether an object is locked out 
     * on a target system or application.
     * <ul>
     * <li>Read this attribute from {@link org.identityconnectors.framework.api.operations.GetApiOp} 
     * to determine whether an object is currently locked out.</li>
     * <li>Use this attribute with {@link org.identityconnectors.framework.api.operations.UpdateApiOp} 
     * to clear the lock-out status of an object 
     * (or to set the lock-out status of an object).</li>
     * <li>Use this attribute with {@link SearchApiOp} 
     * to select objects that are currently locked out
     * (or to select objects that are not currently locked out).</li> 
     * </ul>
     * 
     * @param lock
     *            true if the object is locked out; otherwise false.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#LOCK_OUT_NAME
     * predefined name for lockout state}.
     */
    public static Attribute buildLockOut(final boolean lock) {
        return build(OperationalAttributes.LOCK_OUT_NAME, lock);
    }

    /**
     * Builds an {@linkplain OperationalAttributes operational attribute} 
     * that represents whether the password of an object is expired 
     * on a target system or application.
     * <ul>
     * <li>Read this attribute from {@link org.identityconnectors.framework.api.operations.GetApiOp} 
     * to determine whether the password of an object is currently expired.</li>
     * <li>Use this attribute with {@link org.identityconnectors.framework.api.operations.UpdateApiOp} 
     * to expire the password of an object 
     * (or to clear the expired status of the password of an object).</li>
     * <li>Use this attribute with {@link SearchApiOp} 
     * to select objects that have passwords that are currently expired
     * (or to select objects that have passwords that are not currently expired).</li> 
     * </ul>
     * 
     * @param value
     *            from the API true expires and from the SPI its shows its
     *            either expired or not.
     * @return an {@code Attribute} with the 
     * {@linkplain OperationalAttributes#PASSWORD_EXPIRED_NAME
     * predefined name for password expiration state}.
     */
    public static Attribute buildPasswordExpired(final boolean value) {
        return build(OperationalAttributes.PASSWORD_EXPIRED_NAME, value);
    }

    // =======================================================================
    // Pre-defined Attributes
    // =======================================================================

    /**
     * Builds an {@linkplain PredefinedAttributes pre-defined attribute} 
     * that represents the date and time of the most recent login for an object 
     * (such as an account) on a target system or application.
     * 
     * @param date
     *            The date and time of the last login.
     * @return an {@code Attribute} with the 
     * {@linkplain PredefinedAttributes#LAST_LOGIN_DATE_NAME
     * predefined name for password expiration state}.
     */
    public static Attribute buildLastLoginDate(final Date date) {
        return buildLastLoginDate(date.getTime());
    }

    /**
     * Builds an {@linkplain PredefinedAttributes pre-defined attribute} 
     * that represents the date and time of the most recent login for an object 
     * (such as an account) on a target system or application.
     * <p>
     * The time parameter is UTC in milliseconds.
     * 
     * @param date
     *            The date and time (UTC in milliseconds) of the last login.
     * @return an {@code Attribute} with the 
     * {@linkplain PredefinedAttributes#LAST_LOGIN_DATE_NAME
     * predefined name for password expiration state}.
     */
    public static Attribute buildLastLoginDate(final long date) {
        return build(PredefinedAttributes.LAST_LOGIN_DATE_NAME, date);
    }

    /**
     * Builds an {@linkplain PredefinedAttributes pre-defined attribute} 
     * that represents the date and time that the password was most recently changed
     * for an object (such as an account) on a target system or application.
     * 
     * @param date
     *            The date and time that the password was most recently changed.
     * @return an {@code Attribute} with the 
     * {@linkplain PredefinedAttributes#LAST_PASSWORD_CHANGE_DATE_NAME
     * predefined name for password expiration state}.
     */
    public static Attribute buildLastPasswordChangeDate(final Date date) {
        return buildLastPasswordChangeDate(date.getTime());
    }

    /**
     * Builds an {@linkplain PredefinedAttributes pre-defined attribute} 
     * that represents the date and time that the password was most recently changed
     * for an object (such as an account) on a target system or application.
     * <p>
     * The time parameter is UTC in milliseconds.
     * 
     * @param date
     *            The date and time that the password was most recently changed.
     * @return an {@code Attribute} with the 
     * {@linkplain PredefinedAttributes#LAST_PASSWORD_CHANGE_DATE_NAME
     * predefined name for password expiration state}.
     */
    public static Attribute buildLastPasswordChangeDate(final long date) {
        return build(PredefinedAttributes.LAST_PASSWORD_CHANGE_DATE_NAME, date);
    } 

    /**
     * Builds an {@linkplain PredefinedAttributes pre-defined attribute} 
     * that represents how often the password must be changed
     * for an object (such as an account) on a target system or application.
     * <p>
     * The value for this attribute is expressed in milliseconds.
     * 
     * @param value
     *            The number of milliseconds between  
     *            the time that the password was most recently changed
     *            and the time when the password must be changed again.
     * @return an {@code Attribute} with the 
     * {@linkplain PredefinedAttributes#PASSWORD_CHANGE_INTERVAL_NAME
     * predefined name for password expiration state}.
     */
    public static Attribute buildPasswordChangeInterval(final long value) {
        return build(PredefinedAttributes.PASSWORD_CHANGE_INTERVAL_NAME, value);
    }
    
}
