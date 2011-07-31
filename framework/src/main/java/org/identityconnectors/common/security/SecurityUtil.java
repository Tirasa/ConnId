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

import java.security.MessageDigest;
import java.util.Arrays;

import org.identityconnectors.common.Base64;


public final class SecurityUtil {
    private SecurityUtil() {
        
    }
    
    /**
     * Converts chars to bytes without using any external functions
     * that might allocate additional buffers for the potentially
     * sensitive data. This guarantees the caller that they only
     * need to cleanup the input and result.
     * @param chars The chars
     * @return The bytes
     */
    public static byte[] charsToBytes(char [] chars)
    {
        byte [] bytes = new byte[chars.length*2];
        
        for ( int i = 0; i < chars.length; i++ ) {
            char v = chars[i];
            bytes[i*2] = (byte)(0xff & (v >>  8));
            bytes[i*2+1] = (byte)(0xff & (v));
        }
        return bytes;
    }
    
    /**
     * Converts bytes to chars without using any external functions
     * that might allocate additional buffers for the potentially
     * sensitive data. This guarantees the caller that they only
     * need to cleanup the input and result.
     * @param bytes The bytes (to convert into characters).
     * @return The characters (converted from the specified bytes).
     */
    public static char[] bytesToChars(byte [] bytes)
    {
        char [] chars = new char[bytes.length/2];
        for ( int i = 0; i < chars.length; i++ ) {
            char v = (char)(((0xFF&(bytes[i*2]))<<8) | (0xFF&bytes[i*2+1]));
            chars[i] = v;
        }
        return chars;
    }

    /**
     * Clears an array of potentially sensitive bytes
     * @param bytes The bytes. May be null.
     */
    public static void clear(byte [] bytes)
    {
        if ( bytes != null )
        {
            Arrays.fill(bytes, (byte)0);
        }
    }
    
    /**
     * Clears an array of potentially sensitive chars
     * @param chars The characters. May be null.
     */
    public static void clear(char [] chars)
    {
        if ( chars != null ) 
        {
            Arrays.fill(chars, (char)0);
        }
    }
    
    /**
     * Computes the base 64 encoded SHA1 hash of the input
     * @param input The input chars
     * @return the hash
     */
    public static String computeBase64SHA1Hash(char [] input)
    {
        //convert the char [] to bytes. I know there
        //are utility methods for doing this, but I don't
        //know what sort of buffering they use. because it
        //is possibly sensitive data, we do this in line so
        //that we can clear out our bytes after we are done.
        byte [] bytes = null;
        try {
            bytes = SecurityUtil.charsToBytes(input);
            return SecurityUtil.computeBase64SHA1Hash(bytes);
        }
        finally {            
            //clear the possibly sensitive bytes out
            SecurityUtil.clear(bytes);
            //no need to clear "data" since it is now just a hash
        }         
    }
    
    /**
     * Computes the base 64 encoded SHA1 hash of the input
     * @param bytes The input bytes.
     * @return the hash (computed from the input bytes).
     */
    public static String computeBase64SHA1Hash(byte [] bytes)
    {        
        byte [] data;
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA");
            data = hasher.digest(bytes);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Base64.encode(data);
    }

    /**
     * Verifies the base 64-encoded SHA1 hash of the input.
     * @param input The input chars
     * @param hash The expected hash
     * @return true if the hash of the input characters matches the expected hash.
     */
    public static boolean verifyBase64SHA1Hash(char [] input, String hash)
    {
        String inputHash = computeBase64SHA1Hash(input);
        return inputHash.equals(hash);
    }
}
