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
 */
package org.identityconnectors.common;

import static org.testng.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.annotations.Test;

/**
 * Simple tests to check that its functioning.
 */
public class StringPrintWriterTests {

    String[] DATA = new String[] { "Some random text to use!", "Some more text to use wee!",
        "Even more text to use woo hoo!" };

    @Test
    public void getString() {
        StringBuilder bld = new StringBuilder();
        StringPrintWriter wrt = new StringPrintWriter();
        for (String data : DATA) {
            bld.append(data);
            wrt.print(data);
        }
        // check that it works..
        assertEquals(bld.toString(), wrt.getString());
        wrt.clear();
        assertEquals(wrt.getString(), "");
    }

    @Test
    public void getReader() {
        StringWriter wrt = new StringWriter();
        StringPrintWriter pwrt = new StringPrintWriter();
        for (String data : DATA) {
            wrt.append(data);
            pwrt.append(data);
        }
        // get the string of the reader..
        String actual = IOUtil.readerToString(pwrt.getReader());
        assertEquals(actual, wrt.toString());
    }

    @Test
    public void println() {
        StringWriter swrt = new StringWriter();
        PrintWriter wrt = new PrintWriter(swrt);
        StringPrintWriter pwrt = new StringPrintWriter();
        for (String data : DATA) {
            wrt.println(data);
            pwrt.println(data);
        }
        assertEquals(swrt.toString(), pwrt.getString());
    }

    public void printlnArray() {
        StringWriter swrt = new StringWriter();
        PrintWriter wrt = new PrintWriter(swrt);
        StringPrintWriter pwrt = new StringPrintWriter();
        for (String data : DATA) {
            wrt.println(data);
        }
        pwrt.println(DATA);
        assertEquals(swrt.toString(), pwrt.getString());
    }

    @Test
    public void printArray() {
        StringWriter swrt = new StringWriter();
        PrintWriter wrt = new PrintWriter(swrt);
        StringPrintWriter pwrt = new StringPrintWriter();
        for (String data : DATA) {
            wrt.print(data);
        }
        pwrt.print(DATA);
        assertEquals(swrt.toString(), pwrt.getString());
    }
}
