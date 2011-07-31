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
package org.identityconnectors.common.security;

/**
 * Secure string implementation that solves the problems associated with
 * keeping passwords as <code>java.lang.String</code>. That is, anything 
 * represented as a <code>String</code> is kept in memory as a clear
 * text password and stays in memory <b>at least</b> until it is garbage collected.
 * <p>
 * The GuardedString class alleviates this problem by storing the characters in
 * memory in an encrypted form. The encryption key will be a randomly-generated
 * key.
 * <p>
 * In their serialized form, GuardedString will be encrypted using a known
 * default key. This is to provide a minimum level of protection regardless
 * of the transport. For communications with the Remote Connector Framework
 * it is recommended that deployments enable SSL for true encryption.
 * <p>
 * Applications may also wish to persist GuardedStrings. In the case of 
 * Identity Manager, it should convert GuardedStrings to EncryptedData so
 * that they can be stored and managed using the Manage Encryption features
 * of Identity Manager. Other applications may wish to serialize APIConfiguration
 * as a whole. These applications are responsible for encrypting the APIConfiguration
 * blob for an additional layer of security (beyond the basic default key encryption
 * provided by GuardedString).
 */
public final class GuardedString {
    
    /**
     * Callback interface for those times that it is necessary to access the
     * clear text of the secure string.
     */
    public interface Accessor {
        /**
         * This method will be called with the clear text of the string.
         * After the call the clearChars array will be automatically zeroed
         * out, thus keeping the window of potential exposure to a bare-minimum.
         * @param clearChars
         */
        public void access(char [] clearChars);
    }
    
    static Encryptor _encryptor;
    
    private boolean _readOnly;
    private boolean _disposed;
    private byte [] _encryptedBytes;
    private String _base64SHA1Hash;
    
    /**
     * Creates an empty secure string
     */
    public GuardedString() {
        this(new char[0]);
    }
    
    /**
     * Initialized the GuardedString from the given clear characters.
     * Caller is responsible for zeroing out the array of characters
     * after the call.
     * @param clearChars The clear-text characters
     */
    public GuardedString(char [] clearChars) {
        encryptChars(clearChars);
    }
    
    /**
     * Provides access to the clear-text value of the string in a controlled fashion.
     * The clear-text characters will only be available for the duration of the call
     * and automatically zeroed out following the call. 
     * 
     * <p>
     * <b>NOTE:</b> Callers are encouraged to use {@link #verifyBase64SHA1Hash(String)}
     * where possible if the intended use is merely to verify the contents of
     * the string match an expected hash value.
     * @param accessor Accessor callback.
     * @throws IllegalStateException If the string has been disposed
     */
    public void access(Accessor accessor) {
        checkNotDisposed();
        char [] clearChars = null; 
        try {
            clearChars = decryptChars();
            accessor.access(clearChars);
        }
        finally {
            SecurityUtil.clear(clearChars);
        }
    }
    
    /**
     * Appends a single clear-text character to the secure string.
     * The in-memory data will be decrypted, the character will be
     * appended, and then it will be re-encrypted.
     * @param c The character to append.
     * @throws IllegalStateException If the string is read-only
     * @throws IllegalStateException If the string has been disposed
     */
    public void appendChar(char c) {
        checkNotDisposed();
        checkWriteable();
        char [] clearChars = null;
        char [] clearChars2 = null;
        try {
            clearChars = decryptChars();
            clearChars2 = new char[clearChars.length+1];
            System.arraycopy(clearChars, 0, clearChars2, 0, clearChars.length);
            clearChars2[clearChars2.length-1] = c;
            encryptChars(clearChars2);
        }
        finally {
            SecurityUtil.clear(clearChars);
            SecurityUtil.clear(clearChars2);
        }
    }
    
    /**
     * Clears the in-memory representation of the string.
     */
    public void dispose() {
        SecurityUtil.clear(_encryptedBytes);
        _disposed = true;
    }
    
    /**
     * Returns true iff this string has been marked read-only
     * @return true iff this string has been marked read-only
     * @throws IllegalStateException If the string has been disposed
     */
    public boolean isReadOnly() {
        checkNotDisposed();
        return _readOnly;
    }
    
    /**
     * Mark this string as read-only.
     * @throws IllegalStateException If the string has been disposed
     */
    public void makeReadOnly() {
        checkNotDisposed();
        _readOnly = true;
    }
    
    /**
     * Create a copy of the string. If this instance is read-only,
     * the copy will not be read-only.
     * @return A copy of the string.
     * @throws IllegalStateException If the string has been disposed
     */
    public GuardedString copy() {
        checkNotDisposed();
        byte [] encryptedBytes2 = new byte[_encryptedBytes.length];
        System.arraycopy(_encryptedBytes, 0, encryptedBytes2, 0, _encryptedBytes.length);
        GuardedString rv = new GuardedString();
        rv._encryptedBytes = encryptedBytes2;
        return rv;
    }
    
    /**
     * Verifies that this base-64 encoded SHA1 hash of this string
     * matches the given value.
     * @param hash The hash to verify against.
     * @return True if the hash matches the given parameter.
     * @throws IllegalStateException If the string has been disposed
     */
    public boolean verifyBase64SHA1Hash(String hash) {
        checkNotDisposed();
        return _base64SHA1Hash.equals(hash);
    }
        
    private void checkWriteable() {
        if (_readOnly) {
            throw new IllegalStateException("String is read-only");
        }
    }

    private void checkNotDisposed() {
        if (_disposed) {
            throw new IllegalStateException("String is disposed");
        }
    }
    
    private char [] decryptChars() {
        byte [] clearBytes = null;
        try {
            clearBytes = decryptBytes();
            return SecurityUtil.bytesToChars(clearBytes);
        }
        finally {
            SecurityUtil.clear(clearBytes);
        }
    }
    
    private void encryptChars(char [] chars) {
        byte [] clearBytes = null;
        try {
            clearBytes = SecurityUtil.charsToBytes(chars);
            encryptBytes(clearBytes);
        }
        finally {
            SecurityUtil.clear(clearBytes);
        }
    }
    
    private static synchronized Encryptor getEncryptor() {
        if (_encryptor == null) {
            _encryptor = EncryptorFactory.getInstance().newRandomEncryptor();
        }
        return _encryptor;
    }
    
    static synchronized void setEncryptor(Encryptor encryptor) {
        _encryptor = encryptor;
    }
    
    private byte [] decryptBytes() {
        Encryptor encryptor = getEncryptor();
        return encryptor.decrypt(_encryptedBytes);
    }
    
    private void encryptBytes(byte [] bytes) {
        Encryptor encryptor = getEncryptor();
        byte [] newBytes = encryptor.encrypt(bytes);
        SecurityUtil.clear(_encryptedBytes);
        _encryptedBytes = newBytes;
        _base64SHA1Hash = SecurityUtil.computeBase64SHA1Hash(bytes);
    }

    @Override
    public boolean equals(Object o) {
        if ( o instanceof GuardedString ) {
            GuardedString other = (GuardedString)o;
            //not the true contract of equals. however,
            //due to the high mathematical improbability of
            //two unequal strings having the same secure hash,
            //this approach feels good. the alternative,
            //decrypting for comparison, is simply too
            //performance intensive to be used for equals
            return _base64SHA1Hash.equals(other._base64SHA1Hash);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return _base64SHA1Hash.hashCode();
    }
}
