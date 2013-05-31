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
package org.identityconnectors.framework.impl.serializer.xml;

import java.io.Writer;

import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.XmlObjectSerializer;

public class XmlObjectSerializerImpl implements XmlObjectSerializer {

    public static final String MULTI_OBJECT_ELEMENT = "MultiObject";
    public static final String CONNECTORS_DTD = "connectors.dtd";

    private final Writer output;

    private final boolean multiObject;

    private final boolean includeHeader;

    private boolean firstObjectWritten;

    private boolean documentEnded;

    public XmlObjectSerializerImpl(Writer output, boolean includeHeader, boolean multiObject) {
        this.output = output;
        this.includeHeader = includeHeader;
        this.multiObject = multiObject;
    }

    /**
     * Writes the next object to the stream.
     *
     * @param object
     *            The object to write.
     * @see org.identityconnectors.framework.common.serializer.ObjectSerializerFactory
     *      for a list of supported types.
     * @throws ConnectorException
     *             if there is more than one object and this is not configured
     *             for multi-object document.
     */
    public void writeObject(Object object) {
        if (documentEnded) {
            throw new IllegalStateException(
                    "Attempt to writeObject after the document is already closed");
        }
        StringBuilder buf = new StringBuilder();
        XmlObjectEncoder encoder = new XmlObjectEncoder(buf);
        String elementName = encoder.writeObject(object);
        if (!firstObjectWritten) {
            startDocument(elementName);
        } else {
            if (!multiObject) {
                throw new IllegalStateException(
                        "Attempt to write multiple objects on a single-object document");
            }
        }
        write(buf.toString());
        firstObjectWritten = true;
    }

    public void flush() {
        try {
            output.flush();
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    public void close(boolean closeStream) {
        if (!documentEnded) {
            if (!firstObjectWritten) {
                if (!multiObject) {
                    throw new IllegalStateException(
                            "Attempt to write zero objects on a single-object document");
                }
                startDocument(null);
            }
            writeEndDocument();
            documentEnded = true;
        }
        if (closeStream) {
            try {
                output.close();
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }
    }

    private void startDocument(String firstElement) {
        if (includeHeader) {
            String docType = multiObject ? MULTI_OBJECT_ELEMENT : firstElement;
            String line1 = "<?xml version='1.0' encoding='UTF-8'?>\n";
            String line2 =
                    "<!DOCTYPE " + docType + " PUBLIC '" + CONNECTORS_DTD + "' '" + CONNECTORS_DTD
                            + "'>\n";
            write(line1);
            write(line2);
        }
        if (multiObject) {
            String line3 = "<" + MULTI_OBJECT_ELEMENT + ">\n";
            write(line3);
        }
    }

    private void writeEndDocument() {
        if (multiObject) {
            String line1 = "</" + MULTI_OBJECT_ELEMENT + ">\n";
            write(line1);
        }
    }

    private void write(String str) {
        try {
            output.write(str);
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

}
