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
package org.identityconnectors.common;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Simple extension of PrintWriter so you don't have to create a StringWriter to
 * pass to it, when you want the functionality of PrintWriter but you want it in
 * a buffer.
 */
public class StringPrintWriter extends PrintWriter {
    // =======================================================================
    // Constants
    // =======================================================================
    private static final int DEFAULT_SIZE = 256;
    // =======================================================================
    // Fields
    // =======================================================================
    final private int _initialSize;

    /**
     * Create with the default initial size.
     */
    public StringPrintWriter() {
        this(DEFAULT_SIZE);
    }

    /**
     * Create with an initialize size with the parameter supplied..
     */
    public StringPrintWriter(final int initSize) {
        super(new StringWriter(initSize));
        _initialSize = initSize;
    }

    /**
     * Return the string in the internal string writer.
     */
    public String getString() {
        flush();
        return ((StringWriter) out).toString();
    }

    /**
     * Return a reader for the accumulated string.
     */
    public Reader getReader() {
        return new StringReader(getString());
    }

    /**
     * Call println for every string in the array.
     */
    public void println(final String[] value) {
        assert value != null;
        for (int i = 0; i < value.length; i++) {
            assert value[i] != null;
            println(value[i]);
        }
    }

    /**
     * Call print for every string in the array.
     * 
     * @throws NullPointerException
     *             iff value is null.
     */
    public void print(final String[] value) {
        for (int i = 0; i < value.length; i++) {
            assert value[i] != null;
            print(value[i]);
        }
    }

    /**
     * Clear out the underlying string writer.
     */
    public void clear() {
        // replace the current string writer..
        out = new StringWriter(_initialSize);
    }
}
