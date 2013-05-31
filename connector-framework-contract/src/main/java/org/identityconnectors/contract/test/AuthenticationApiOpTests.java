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
 *
 * Portions Copyrighted 2012 ForgeRock AS
 *
 */
package org.identityconnectors.contract.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.exceptions.PasswordExpiredException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.PredefinedAttributes;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.SkipException;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/**
 * Contract test of {@link AuthenticationApiOp}
 */
@Test(testName =  AuthenticationApiOpTests.TEST_NAME)
public class AuthenticationApiOpTests extends ObjectClassRunner {

    /**
     * Logging..
     */
    private static final Logger logger = Logger.getLogger(ValidateApiOpTests.class);
    public static final String TEST_NAME = "Authentication";
    static final String USERNAME_PROP = "username";
    private static final String WRONG_PASSWORD_PROP = "wrong.password";

    private static final String MAX_ITERATIONS = "maxIterations";
    private static final String SLEEP_MILLISECONDS = "sleepMilliseconds";


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getAPIOperations() {
        Set<Class<? extends APIOperation>> requiredOps = new HashSet<Class<? extends APIOperation>>();
        // list of required operations by this test:
        requiredOps.add(CreateApiOp.class);
        requiredOps.add(AuthenticationApiOp.class);
        requiredOps.add(GetApiOp.class);
        return requiredOps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void testRun(ObjectClass objectClass) {

        Uid uid = null;

        try {
            // create a user
            Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                    getObjectClassInfo(objectClass), getTestName(), 0, true, false);

            // Remove enabled and password_expired, connector must create valid account then
            for (Iterator<Attribute> i = attrs.iterator(); i.hasNext();) {
                Attribute attr = i.next();
                if (attr.is(OperationalAttributes.PASSWORD_EXPIRED_NAME)
                        || attr.is(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME)
                        || attr.is(OperationalAttributes.ENABLE_DATE_NAME)
                        || attr.is(OperationalAttributes.ENABLE_NAME)) {

                    if (!ConnectorHelper.isRequired(getObjectClassInfo(objectClass), attr)) {
                        i.remove();
                    }
                }
            }

            uid = getConnectorFacade().create(objectClass, attrs, getOperationOptionsByOp(objectClass, CreateApiOp.class));

            // get the user to make sure it exists now
            ConnectorObject obj = getConnectorFacade().getObject(objectClass, uid,
                    getOperationOptionsByOp(objectClass, GetApiOp.class));
            assertNotNull(obj,"Unable to retrieve newly created object");

            // get username
            String name = (String) getDataProvider().getTestSuiteAttribute(objectClass.getObjectClassValue() + "." + USERNAME_PROP,
                    TEST_NAME);

            // test negative case with valid user, but wrong password
            boolean authenticateFailed = false;

            // get wrong password
            GuardedString wrongPassword = (GuardedString) getDataProvider().getTestSuiteAttribute(objectClass.getObjectClassValue() + "." + WRONG_PASSWORD_PROP,
                    TEST_NAME);

            authenticateFailed = authenticateExpectingInvalidCredentials(objectClass, name, wrongPassword);

            assertTrue(authenticateFailed,"Negative test case for Authentication failed, should throw InvalidCredentialException");

            // now try with the right password
            GuardedString password = (GuardedString) ConnectorHelper.get(getDataProvider(), getTestName(), GuardedString.class, OperationalAttributes.PASSWORD_NAME, objectClass.getObjectClassValue(), 0, false);

            Uid authenticatedUid = authenticateExpectingSuccess(objectClass, name, password);

            String MSG = "Authenticate returned wrong Uid, expected: %s, returned: %s.";
            assertEquals(uid, authenticatedUid,String.format(MSG, uid, authenticatedUid));

            // test that PASSWORD change works, CURRENT_PASSWORD should be set
            // to old password value if supported
            if (isOperationalAttributeUpdateable(objectClass, OperationalAttributes.PASSWORD_NAME)) {
                GuardedString newpassword = (GuardedString) ConnectorHelper.get(getDataProvider(), getTestName(), GuardedString.class, OperationalAttributes.PASSWORD_NAME, UpdateApiOpTests.MODIFIED, objectClass.getObjectClassValue(), 0, false);
                Set<Attribute> replaceAttrs = new HashSet<Attribute>();
                replaceAttrs.add(AttributeBuilder.buildPassword(newpassword));

                if (ConnectorHelper.isAttrSupported(getObjectClassInfo(objectClass),
                        OperationalAttributes.CURRENT_PASSWORD_NAME)) {
                    // CURRENT_PASSWORD must be set to old password
                    replaceAttrs.add(AttributeBuilder.buildCurrentPassword(password));
                }
                // update to new password
                uid = getConnectorFacade().update(objectClass,
                        uid, replaceAttrs, getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                // authenticate with new password
                authenticatedUid = authenticateExpectingSuccess(objectClass, name, newpassword);

                assertEquals(uid, authenticatedUid,String.format(MSG, uid, authenticatedUid));

                // LAST_PASSWORD_CHANGE_DATE
                if (ConnectorHelper.isAttrSupported(getObjectClassInfo(objectClass),
                        PredefinedAttributes.LAST_PASSWORD_CHANGE_DATE_NAME)) {
                    logger.info("LAST_PASSWORD_CHANGE_DATE test.");
                    // LAST_PASSWORD_CHANGE_DATE must be readable, we suppose it is
                    // add LAST_PASSWORD_CHANGE_DATE to ATTRS_TO_GET
                    OperationOptionsBuilder builder = new OperationOptionsBuilder();
                    builder.setAttributesToGet(PredefinedAttributes.LAST_PASSWORD_CHANGE_DATE_NAME);

                    ConnectorObject lastPasswordChange = getConnectorFacade().getObject(
                            objectClass, uid, builder.build());

                    // check that LAST_PASSWORD_CHANGE_DATE was set to a value
                    assertNotNull(lastPasswordChange.getAttributeByName(PredefinedAttributes.LAST_PASSWORD_CHANGE_DATE_NAME),"LAST_PASSWORD_CHANGE_DATE attribute is null.");
                } else {
                    logger.info("Skipping LAST_PASSWORD_CHANGE_DATE test.");
                }
            }

            // LAST_LOGIN_DATE
            if (ConnectorHelper.isAttrSupported(getObjectClassInfo(objectClass), PredefinedAttributes.LAST_LOGIN_DATE_NAME)) {
                logger.info("LAST_LOGIN_DATE test.");
                // LAST_LOGIN_DATE must be readable, we suppose it is
                // add LAST_LOGIN_DATE to ATTRS_TO_GET
                OperationOptionsBuilder builder = new OperationOptionsBuilder();
                builder.setAttributesToGet(PredefinedAttributes.LAST_LOGIN_DATE_NAME);

                ConnectorObject lastLogin = getConnectorFacade().getObject(objectClass, uid,
                        builder.build());

                // check that LAST_LOGIN_DATE was set to some value
                assertNotNull(lastLogin.getAttributeByName(PredefinedAttributes.LAST_LOGIN_DATE_NAME),"LAST_LOGIN_DATE attribute is null.");
            }
            else {
                logger.info("Skipping LAST_LOGIN_DATE test.");
            }
        } finally {
            if (uid != null) {
                // delete the object
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                        false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }

        }
    }

    /**
     * Tests that disabled user cannot authenticate.
     * RuntimeException should be thrown.
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testOpEnable(ObjectClass objectClass) {
        // now try to set the password to be expired and authenticate again
        // it's possible only in case Update and PASSWORD_EXPIRED are supported
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())
                && isOperationalAttributeUpdateable(objectClass, OperationalAttributes.ENABLE_NAME)) {
            Uid uid = null;
            try {
                // create an user
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 0, true, false);

                // Remove enabled and password_expired, connector must create valid account then
                for (Iterator<Attribute> i = attrs.iterator(); i.hasNext();) {
                    Attribute attr = i.next();
                    if (attr.is(OperationalAttributes.PASSWORD_EXPIRED_NAME)
                            || attr.is(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME)
                            || attr.is(OperationalAttributes.ENABLE_DATE_NAME)) {

                        if (!ConnectorHelper.isRequired(getObjectClassInfo(objectClass), attr)) {
                            i.remove();
                        }
                    }
                }

                uid = getConnectorFacade().create(objectClass, attrs,
                        getOperationOptionsByOp(objectClass, CreateApiOp.class));

                // get username
                String name = (String) getDataProvider().getTestSuiteAttribute(objectClass.getObjectClassValue() + "." + USERNAME_PROP,
                        TEST_NAME);

                Set<Attribute> updateAttrs = new HashSet<Attribute>();
                updateAttrs.add(AttributeBuilder.buildEnabled(false));


                Uid newUid = getConnectorFacade().update(objectClass, uid,
                        updateAttrs, null);
                if (!uid.equals(newUid) && newUid != null) {
                    uid = newUid;
                }

                // get the right password
                GuardedString password = (GuardedString) ConnectorHelper.get(getDataProvider(), getTestName(), GuardedString.class, OperationalAttributes.PASSWORD_NAME, getObjectClassInfo(objectClass).getType(), 0, false);

                // and now authenticate
                assertTrue(authenticateExpectingRuntimeException(objectClass, name, password),"Authenticate must throw for disabled account");

            } finally {
                // delete the object
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                        false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        } else {
            logger.info("Skipping testOpEnable test.");
        }
    }

    /**
     * Tests that PasswordExpiredException is thrown in case PasswordExpirationDate is set to today.
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testOpPasswordExpirationDate(ObjectClass objectClass) {
        // now try to set the password to be expired and authenticate again
        // it's possible only in case Update and PASSWORD_EXPIRED
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())
                && isOperationalAttributeUpdateable(objectClass, OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME)) {
            Uid uid = null;
            try {
                // create an user
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 0, true, false);

                // Remove enabled and password_expired, connector must create valid account then
                for (Iterator<Attribute> i = attrs.iterator(); i.hasNext();) {
                    Attribute attr = i.next();
                    if (attr.is(OperationalAttributes.ENABLE_NAME)
                            || attr.is(OperationalAttributes.ENABLE_DATE_NAME)
                            || attr.is(OperationalAttributes.PASSWORD_EXPIRED_NAME)) {

                        if (!ConnectorHelper.isRequired(getObjectClassInfo(objectClass), attr)) {
                            i.remove();
                        }
                    }
                }

                uid = getConnectorFacade().create(objectClass, attrs,
                        getOperationOptionsByOp(objectClass, CreateApiOp.class));

                // get username
                String name = (String) getDataProvider().getTestSuiteAttribute(objectClass.getObjectClassValue() + "." + USERNAME_PROP,
                        TEST_NAME);

                Set<Attribute> updateAttrs = new HashSet<Attribute>();
                updateAttrs.add(AttributeBuilder.buildPasswordExpirationDate(new Date()));


                Uid newUid = getConnectorFacade().update(objectClass, uid,
                        updateAttrs, null);
                if (!uid.equals(newUid) && newUid != null) {
                    uid = newUid;
                }

                // get the right password
                GuardedString password = (GuardedString) ConnectorHelper.get(getDataProvider(), getTestName(), GuardedString.class, OperationalAttributes.PASSWORD_NAME, getObjectClassInfo(objectClass).getType(), 0, false);

                // and now authenticate
                PasswordExpiredException pwe = authenticateExpectingPasswordExpired(objectClass, name, password);
                assertNotNull(pwe,"Authenticate should throw PasswordExpiredException.");

                final String MSG = "PasswordExpiredException contains wrong Uid, expected: %s, returned: %s";
                assertEquals(uid, pwe.getUid(),String.format(MSG, uid, pwe.getUid()));


            } finally {
                // delete the object
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                        false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        } else {
            logger.info("Skipping testOpPasswordExpirationDate test.");
        }
    }

    /**
     * Tests that PasswordExpiredException is thrown in case PasswordExpired is updated to true.
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testOpPasswordExpired(ObjectClass objectClass) {
        // now try to set the password to be expired and authenticate again
        // it's possible only in case Update and PASSWORD_EXPIRED
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())
                && isOperationalAttributeUpdateable(objectClass, OperationalAttributes.PASSWORD_EXPIRED_NAME)) {
            Uid uid = null;
            try {
                // create an user
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 0, true, false);

                // Remove enabled and password_expired, connector must create valid account then
                for (Iterator<Attribute> i = attrs.iterator(); i.hasNext();) {
                    Attribute attr = i.next();
                    if (attr.is(OperationalAttributes.ENABLE_NAME)
                            || attr.is(OperationalAttributes.ENABLE_DATE_NAME)
                            || attr.is(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME)) {

                        if (!ConnectorHelper.isRequired(getObjectClassInfo(objectClass), attr)) {
                            i.remove();
                        }
                    }
                }

                uid = getConnectorFacade().create(objectClass, attrs,
                        getOperationOptionsByOp(objectClass, CreateApiOp.class));

                // get username
                String name = (String) getDataProvider().getTestSuiteAttribute(objectClass.getObjectClassValue() + "." + USERNAME_PROP,
                        TEST_NAME);

                Set<Attribute> updateAttrs = new HashSet<Attribute>();
                updateAttrs.add(AttributeBuilder.buildPasswordExpired(true));


                Uid newUid = getConnectorFacade().update(objectClass, uid,
                        updateAttrs, null);
                if (!uid.equals(newUid) && newUid != null) {
                    uid = newUid;
                }

                // get the right password
                GuardedString password = (GuardedString) ConnectorHelper.get(getDataProvider(), getTestName(), GuardedString.class, OperationalAttributes.PASSWORD_NAME, getObjectClassInfo(objectClass).getType(), 0, false);

                // and now authenticate
                PasswordExpiredException pwe = authenticateExpectingPasswordExpired(objectClass, name, password);
                assertNotNull(pwe,"Authenticate should throw PasswordExpiredException.");
                final String MSG = "PasswordExpiredException contains wrong Uid, expected: %s, returned: %s";
                assertEquals(uid, pwe.getUid(),String.format(MSG, uid, pwe.getUid()));

            } finally {
                // delete the object
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                        false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        } else {
            logger.info("Skipping testOpPasswordExpired test.");
            throw new SkipException("Skipping testOpPasswordExpired test.");
        }
    }

    /**
     * Tests that connector respects order of PASSWORD and PASSWORD_EXPIRED
     * attributes during update. PASSWORD should be performed before
     * PASSWORD_EXPIRED.
     */
    @Test(dataProvider = OBJECTCLASS_DATAPROVIDER)
    public void testPasswordBeforePasswordExpired(ObjectClass objectClass) {
        // run test only in case operation is supported and both PASSWORD and PASSWORD_EXPIRED are supported
        if (ConnectorHelper.operationsSupported(getConnectorFacade(), objectClass, getAPIOperations())
                && isOperationalAttributeUpdateable(objectClass, OperationalAttributes.PASSWORD_NAME)
                && isOperationalAttributeUpdateable(objectClass, OperationalAttributes.PASSWORD_EXPIRED_NAME)) {
            Uid uid = null;
            try {
                // create an user
                Set<Attribute> attrs = ConnectorHelper.getCreateableAttributes(getDataProvider(),
                        getObjectClassInfo(objectClass), getTestName(), 0, true, false);

                // Remove enabled and password_expired, connector must create valid account then
                for (Iterator<Attribute> i = attrs.iterator(); i.hasNext();) {
                    Attribute attr = i.next();
                    if (attr.is(OperationalAttributes.ENABLE_NAME)
                            || attr.is(OperationalAttributes.ENABLE_DATE_NAME)
                            || attr.is(OperationalAttributes.PASSWORD_EXPIRATION_DATE_NAME)) {

                        if (!ConnectorHelper.isRequired(getObjectClassInfo(objectClass), attr)) {
                            i.remove();
                        }
                    }
                }

                uid = getConnectorFacade().create(objectClass, attrs,
                        getOperationOptionsByOp(objectClass, CreateApiOp.class));

                // get username
                String name = (String) getDataProvider().getTestSuiteAttribute(objectClass.getObjectClassValue() + "." + USERNAME_PROP,
                        TEST_NAME);

                // get new password
                GuardedString newpassword = (GuardedString) ConnectorHelper.get(getDataProvider(), getTestName(), GuardedString.class, OperationalAttributes.PASSWORD_NAME, UpdateApiOpTests.MODIFIED, getObjectClassInfo(objectClass).getType(), 0, false);

                // change password and expire password
                Set<Attribute> replaceAttrs = new HashSet<Attribute>();
                replaceAttrs.add(AttributeBuilder.buildPassword(newpassword));
                replaceAttrs.add(AttributeBuilder.buildPasswordExpired(true));

                if (ConnectorHelper.isAttrSupported(getObjectClassInfo(objectClass), OperationalAttributes.CURRENT_PASSWORD_NAME)) {
                    // get old password
                    GuardedString password = (GuardedString) ConnectorHelper.get(getDataProvider(), getTestName(), GuardedString.class, OperationalAttributes.PASSWORD_NAME, getObjectClassInfo(objectClass).getType(), 0, false);

                    // CURRENT_PASSWORD must be set to old password
                    replaceAttrs.add(AttributeBuilder.buildCurrentPassword(password));
                }
                // update to new password and expire password
                uid = getConnectorFacade().update(objectClass,
                        uid, replaceAttrs, getOperationOptionsByOp(objectClass, UpdateApiOp.class));

                PasswordExpiredException pwe = authenticateExpectingPasswordExpired(objectClass, name, newpassword);

                assertNotNull(pwe,"Authenticate should throw PasswordExpiredException.");

            } finally {
                // delete the object
                ConnectorHelper.deleteObject(getConnectorFacade(), objectClass, uid,
                        false, getOperationOptionsByOp(objectClass, DeleteApiOp.class));
            }
        }
        else {
            logger.info("Skipping test ''testPasswordBeforePasswordExpired'' for object class "+objectClass);
        }
    }

    /**
     * Returns true if operational attribute is supported and updateable.
     */
    private boolean isOperationalAttributeUpdateable(ObjectClass objectClass, String name) {
        ObjectClassInfo oinfo = getObjectClassInfo(objectClass);
        for (AttributeInfo ainfo : oinfo.getAttributeInfo()) {
            if (ainfo.is(name)) {
                return ainfo.isUpdateable();
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestName() {
        return TEST_NAME;
    }

    private long getLongTestParam(String name, long defaultValue) {
    	long longValue = defaultValue;

        try {
            Object valueObject =  getDataProvider().getTestSuiteAttribute(name,
                TEST_NAME);
            if(valueObject != null) {
            	longValue = Long.parseLong(valueObject.toString());
            }
        } catch (ObjectNotFoundException ex) {
        }

        return longValue;
    }

    private void sleepIngoringInterruption(long sleepTime) {
    	try {
    		Thread.sleep(sleepTime);
    	} catch (InterruptedException e) {

    	}
    }

    private boolean authenticateExpectingRuntimeException(ObjectClass objectClass, String name, GuardedString password) {
    	boolean authenticateFailed = false;

    	for(int i=0;i<getLongTestParam(MAX_ITERATIONS, 1);i++) {
            try {
                getConnectorFacade().authenticate(ObjectClass.ACCOUNT, name,password,
                        getOperationOptionsByOp(objectClass, AuthenticationApiOp.class));
            } catch (RuntimeException e) {
                // it failed as it should have
                authenticateFailed = true;
                break;
            }
        	logger.info(String.format("Retrying authentication - iteration %d", i));
            sleepIngoringInterruption(getLongTestParam(SLEEP_MILLISECONDS, 0));
        }

    	return authenticateFailed;
    }

    private boolean authenticateExpectingInvalidCredentials(ObjectClass objectClass, String name, GuardedString password) {
    	boolean authenticateFailed = false;

    	for(int i=0;i<getLongTestParam(MAX_ITERATIONS, 1);i++) {
            try {
                getConnectorFacade().authenticate(ObjectClass.ACCOUNT, name, password,
                        getOperationOptionsByOp(objectClass, AuthenticationApiOp.class));
            } catch (InvalidCredentialException e) {
                // it failed as it should have
                authenticateFailed = true;
                break;
            }
        	logger.info(String.format("Retrying authentication - iteration %d", i));
            sleepIngoringInterruption(getLongTestParam(SLEEP_MILLISECONDS, 0));
        }

    	return authenticateFailed;
    }

    private Uid authenticateExpectingSuccess(ObjectClass objectClass, String name, GuardedString password) {
    	Uid authenticatedUid = null;
    	RuntimeException lastException = null;

    	for(int i=0;i<getLongTestParam(MAX_ITERATIONS, 1);i++) {
            try {
                authenticatedUid = getConnectorFacade().authenticate(ObjectClass.ACCOUNT, name,password,
                        getOperationOptionsByOp(objectClass, AuthenticationApiOp.class));
                lastException = null;
                break;
            } catch (RuntimeException e) {
            	lastException = e;
            	logger.info(String.format("Retrying authentication - iteration %d", i));
                sleepIngoringInterruption(getLongTestParam(SLEEP_MILLISECONDS, 0));
            }
        }

    	if(lastException != null) {
    		throw lastException;
    	}

    	return authenticatedUid;
    }

    private PasswordExpiredException authenticateExpectingPasswordExpired(ObjectClass objectClass, String name, GuardedString password) {
    	PasswordExpiredException passwordExpiredException = null;
    	RuntimeException lastException = null;

    	for(int i=0;i<getLongTestParam(MAX_ITERATIONS, 1);i++) {
            try {
                getConnectorFacade().authenticate(ObjectClass.ACCOUNT, name,password,
                        getOperationOptionsByOp(objectClass, AuthenticationApiOp.class));
            } catch (PasswordExpiredException e) {
                // it failed as it should have
            	passwordExpiredException = e;
            	lastException = null;
                break;
            } catch (RuntimeException e) {
            	lastException = e;
            }
        	logger.info(String.format("Retrying authentication - iteration %d", i));
            sleepIngoringInterruption(getLongTestParam(SLEEP_MILLISECONDS, 0));
        }

    	if(lastException != null) {
    		throw lastException;
    	}

    	return passwordExpiredException;
    }

}
