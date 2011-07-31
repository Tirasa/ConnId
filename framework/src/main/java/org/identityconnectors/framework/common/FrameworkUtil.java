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
package org.identityconnectors.framework.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.ReflectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.Version;
import org.identityconnectors.common.script.Script;
import org.identityconnectors.common.security.GuardedByteArray;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.ResolveUsernameApiOp;
import org.identityconnectors.framework.api.operations.SchemaApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.api.operations.ValidateApiOp;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.QualifiedUid;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.AuthenticateOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.ResolveUsernameOp;
import org.identityconnectors.framework.spi.operations.SPIOperation;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.ScriptOnConnectorOp;
import org.identityconnectors.framework.spi.operations.ScriptOnResourceOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;


public final class FrameworkUtil {

    private static final String PROP_FRAMEWORK_VERSION = "framework.version";

    private static Version frameworkVersion;

    /**
     * Never allow this to be instantiated.
     */
    private FrameworkUtil() {
        throw new AssertionError();
    }

    // =======================================================================
    // Constants
    // =======================================================================
    /**
     * Map the SPI operation to the API operations.
     */
    private static final Map<Class<? extends SPIOperation>, Class<? extends APIOperation>> SPI_TO_API;
    static {
        SPI_TO_API = new HashMap<Class<? extends SPIOperation>, Class<? extends APIOperation>>();
        SPI_TO_API.put(AuthenticateOp.class, AuthenticationApiOp.class);
        SPI_TO_API.put(ResolveUsernameOp.class, ResolveUsernameApiOp.class);
        SPI_TO_API.put(CreateOp.class, CreateApiOp.class);
        SPI_TO_API.put(DeleteOp.class, DeleteApiOp.class);
        SPI_TO_API.put(SearchOp.class, SearchApiOp.class);
        SPI_TO_API.put(UpdateOp.class, UpdateApiOp.class);
        SPI_TO_API.put(UpdateAttributeValuesOp.class, UpdateApiOp.class);
        SPI_TO_API.put(SchemaOp.class, SchemaApiOp.class);
        SPI_TO_API.put(TestOp.class, TestApiOp.class);
        SPI_TO_API.put(ScriptOnConnectorOp.class, ScriptOnConnectorApiOp.class);
        SPI_TO_API.put(ScriptOnResourceOp.class, ScriptOnResourceApiOp.class);
        SPI_TO_API.put(SyncOp.class, SyncApiOp.class);
    }

    /**
     * Converts a {@link SPIOperation} to an set of {@link APIOperation}.
     */
    public static Set<Class<? extends APIOperation>> spi2apis(
            Class<? extends SPIOperation> spi) {
        Set<Class<? extends APIOperation>> set = new HashSet<Class<? extends APIOperation>>();
        set.add(SPI_TO_API.get(spi));
        // add GetApiOp if search is available..
        if (spi == SearchOp.class ) {
            set.add(GetApiOp.class);
        }
        return set;
    }

    /**
     * Return all the known {@link SPIOperation}s.
     */
    public static Set<Class<? extends SPIOperation>> allSPIOperations() {
        return CollectionUtil.newReadOnlySet(SPI_TO_API.keySet());
    }

    /**
     * Return all the known {@link APIOperation}s.
     */
    public static Set<Class<? extends APIOperation>> allAPIOperations() {
        Set<Class<? extends APIOperation>> set = new HashSet<Class<? extends APIOperation>>();
        set.addAll(SPI_TO_API.values());
        // add Get/Validate because it doesn't have a corresponding SPI.
        set.add(GetApiOp.class);
        set.add(ValidateApiOp.class);
        return CollectionUtil.newReadOnlySet(set);
    }

    /**
     * Determines the default set of operations that a {@link Connector}
     * supports.
     */
    public static Set<Class<? extends APIOperation>> getDefaultSupportedOperations(
            Class<? extends Connector> connector) {
        // determine all support operations..
        Set<Class<? extends APIOperation>> ret;
        ret = new HashSet<Class<? extends APIOperation>>();
        Set<Class<?>> itrfs = ReflectionUtil.getAllInterfaces(connector);
        for (Class<? extends SPIOperation> spi : allSPIOperations()) {
            // determine if the SPI is in the set of interfaces
            if (itrfs.contains(spi)) {
                // convert the SPI to API..
                ret.addAll(spi2apis(spi));
            }
        }
        //finally add unconditionally supported ops
        ret.addAll(getUnconditionallySupportedOperations());
        return ret;
    }
    
    /**
     * Returns the set of operations that are always supported
     * @return the set of operations that are always supported
     */
    public static Set<Class<? extends APIOperation>> getUnconditionallySupportedOperations() {
        Set<Class<? extends APIOperation>> ret;
        ret = new HashSet<Class<? extends APIOperation>>();
        //add validate api op always
        ret.add(ValidateApiOp.class);
        //add ScriptOnConnectorApiOp always
        ret.add(ScriptOnConnectorApiOp.class);
        return ret;        
    }

    /**
     * Supported types for configuration properties.
     */
    private static Set<Class<? extends Object>> CONFIG_SUPPORTED_TYPES;
    static {
        CONFIG_SUPPORTED_TYPES = new HashSet<Class<?>>();
        CONFIG_SUPPORTED_TYPES.add(String.class);
        CONFIG_SUPPORTED_TYPES.add(long.class);
        CONFIG_SUPPORTED_TYPES.add(Long.class);
        CONFIG_SUPPORTED_TYPES.add(char.class);
        CONFIG_SUPPORTED_TYPES.add(Character.class);
        CONFIG_SUPPORTED_TYPES.add(double.class);
        CONFIG_SUPPORTED_TYPES.add(Double.class);
        CONFIG_SUPPORTED_TYPES.add(float.class);
        CONFIG_SUPPORTED_TYPES.add(Float.class);
        CONFIG_SUPPORTED_TYPES.add(int.class);
        CONFIG_SUPPORTED_TYPES.add(Integer.class);
        CONFIG_SUPPORTED_TYPES.add(boolean.class);
        CONFIG_SUPPORTED_TYPES.add(Boolean.class);
        CONFIG_SUPPORTED_TYPES.add(URI.class);
        CONFIG_SUPPORTED_TYPES.add(File.class);
        CONFIG_SUPPORTED_TYPES.add(GuardedByteArray.class);
        CONFIG_SUPPORTED_TYPES.add(GuardedString.class);
        CONFIG_SUPPORTED_TYPES.add(Script.class);
    }
    
    public static Set<Class<? extends Object>> getAllSupportedConfigTypes() {
        return Collections.unmodifiableSet(CONFIG_SUPPORTED_TYPES);
    }

    /**
     * Determines if the class is a supported configuration type.
     * 
     * @param clazz
     *            the type to check against the list of supported types.
     * @return true if the type is in the list otherwise false.
     */
    public static boolean isSupportedConfigurationType(Class<?> clazz) {
        if ( clazz.isArray() ) {
            return isSupportedConfigurationType(clazz.getComponentType());
        }
        else {
            return CONFIG_SUPPORTED_TYPES.contains(clazz);
        }
    }

    /**
     * Supported type for the attributes.
     */
    private static final Set<Class<?>> ATTR_SUPPORTED_TYPES;
    static {
        ATTR_SUPPORTED_TYPES = new HashSet<Class<?>>();
        ATTR_SUPPORTED_TYPES.add(String.class);
        ATTR_SUPPORTED_TYPES.add(long.class);
        ATTR_SUPPORTED_TYPES.add(Long.class);
        ATTR_SUPPORTED_TYPES.add(char.class);
        ATTR_SUPPORTED_TYPES.add(Character.class);
        ATTR_SUPPORTED_TYPES.add(double.class);
        ATTR_SUPPORTED_TYPES.add(Double.class);
        ATTR_SUPPORTED_TYPES.add(float.class);
        ATTR_SUPPORTED_TYPES.add(Float.class);
        ATTR_SUPPORTED_TYPES.add(int.class);
        ATTR_SUPPORTED_TYPES.add(Integer.class);
        ATTR_SUPPORTED_TYPES.add(boolean.class);
        ATTR_SUPPORTED_TYPES.add(Boolean.class);
        ATTR_SUPPORTED_TYPES.add(byte[].class);
        ATTR_SUPPORTED_TYPES.add(BigDecimal.class);
        ATTR_SUPPORTED_TYPES.add(BigInteger.class);
        ATTR_SUPPORTED_TYPES.add(GuardedByteArray.class);
        ATTR_SUPPORTED_TYPES.add(GuardedString.class);
    }
    
    public static Set<Class<? extends Object>> getAllSupportedAttributeTypes() {
        return Collections.unmodifiableSet(ATTR_SUPPORTED_TYPES);
    }


    /**
     * Determines if the class is a supported attribute type.
     * 
     * @param clazz
     *            the type to check against a supported list of types.
     * @return true if the type is on the supported list otherwise false.
     */
    public static boolean isSupportedAttributeType(final Class<?> clazz) {
        return ATTR_SUPPORTED_TYPES.contains(clazz);
    }

    /**
     * Determines if the class is a supported attribute type. If not it throws
     * an {@link IllegalArgumentException}.
     * 
     * <ul>
     * <li>String.class</li>
     * <li>long.class</li>
     * <li>Long.class</li>
     * <li>char.class</li>
     * <li>Character.class</li>
     * <li>double.class</li>
     * <li>Double.class</li>
     * <li>float.class</li>
     * <li>Float.class</li>
     * <li>int.class</li>
     * <li>Integer.class</li>
     * <li>boolean.class</li>
     * <li>Boolean.class</li>
     * <li>byte[].class</li>
     * <li>BigDecimal.class</li>
     * <li>BigInteger.class</li>
     * </ul>
     * 
     * @param clazz
     *            type to check against the support list of types.
     * @throws IllegalArgumentException
     *             iff the type is not on the supported list.
     */
    public static void checkAttributeType(final Class<?> clazz) {
        if (!FrameworkUtil.isSupportedAttributeType(clazz)) {
            final String MSG = "Attribute type ''{0}'' is not supported.";
            throw new IllegalArgumentException(MessageFormat.format(MSG, clazz));
        }
    }
    /**
     * Determines if the class of the object is a supported attribute type.
     * If not it throws an {@link IllegalArgumentException}.
     * @param value The value to check or null.
     */
    public static void checkAttributeValue(Object value) {
        if ( value != null ) {
            checkAttributeType(value.getClass());
        }
    }
    /**
     * Determines if the class is a supported type for an OperationOption. If not it throws
     * an {@link IllegalArgumentException}.
     * 
     * @param clazz
     *            type to check against the support list of types.
     * @throws IllegalArgumentException
     *             iff the type is not on the supported list.
     */
    public static void checkOperationOptionType(final Class<?> clazz) {
        //the set of supported operation option types
        //is the same as that for configuration beans plus Name,
        //ObjectClass, Uid, and QualifiedUid
        
        if ( clazz.isArray() ) {
            checkOperationOptionType(clazz.getComponentType());
            return;
        }
                
        if (FrameworkUtil.isSupportedConfigurationType(clazz)) {
            return; //ok
        }

        if (ObjectClass.class.isAssignableFrom(clazz)) {
            return; //ok
        }
        
        if (Uid.class.isAssignableFrom(clazz)) {
            return; //ok
        }
        
        if (QualifiedUid.class.isAssignableFrom(clazz)) {
            return; //ok
        }
        
        final String MSG = "ConfigurationOption type '+"+clazz.getName()+"+' is not supported.";
        throw new IllegalArgumentException(MSG);
    }
    /**
     * Determines if the class of the object is a supported attribute type.
     * If not it throws an {@link IllegalArgumentException}.
     * @param value The value to check or null.
     */
    public static void checkOperationOptionValue(Object value) {
        if ( value != null ) {
            checkOperationOptionType(value.getClass());
        }
    }

    /**
     * Returns the version of the framework.
     *
     * @return the framework version; never null.
     */
    public static Version getFrameworkVersion() {
        synchronized (FrameworkUtil.class) {
            try {
                if (frameworkVersion == null) {
                    frameworkVersion = getFrameworkVersion(FrameworkUtil.class.getClassLoader());
                }
                return frameworkVersion;
            } catch (IOException e) {
                throw new ConnectorException(e);
            }
        }
    }

    static Version getFrameworkVersion(ClassLoader loader) throws IOException {
        InputStream stream = loader.getResourceAsStream("connectors-framework.properties");
        try {
            Properties props = new Properties();
            props.load(stream);
            String version = props.getProperty(PROP_FRAMEWORK_VERSION);
            if (version == null) {
                throw new IllegalStateException("connectors-framework.properties does not contain a " + PROP_FRAMEWORK_VERSION + " property");
            }
            if (StringUtil.isBlank(version)) {
                throw new IllegalStateException("connectors-framework.properties specifies a blank version");
            }
            return Version.parse(version);
        } finally {
            IOUtil.quietClose(stream);
        }
    }

    // For tests only!
    static synchronized void setFrameworkVersion(Version version) {
        frameworkVersion = version;
    }
}
