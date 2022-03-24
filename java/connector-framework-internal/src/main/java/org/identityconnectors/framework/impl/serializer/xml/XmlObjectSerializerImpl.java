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
 * Portions Copyrighted 2022 ConnId
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

    public XmlObjectSerializerImpl(final Writer output, final boolean includeHeader, final boolean multiObject) {
        this.output = output;
        this.includeHeader = includeHeader;
        this.multiObject = multiObject;
    }

    @Override
    public void writeObject(final Object object) {
        if (documentEnded) {
            throw new IllegalStateException("Attempt to writeObject after the document is already closed");
        }

        StringBuilder buf = new StringBuilder();
        XmlObjectEncoder encoder = new XmlObjectEncoder(buf);
        String elementName = encoder.writeObject(object);
        if (!firstObjectWritten) {
            startDocument(elementName);
        } else {
            if (!multiObject) {
                throw new IllegalStateException("Attempt to write multiple objects on a single-object document");
            }
        }
        write(buf.toString());
        firstObjectWritten = true;
    }

    @Override
    public void flush() {
        try {
            output.flush();
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    @Override
    public void close(final boolean closeStream) {
        if (!documentEnded) {
            if (!firstObjectWritten) {
                if (!multiObject) {
                    throw new IllegalStateException("Attempt to write zero objects on a single-object document");
                }
                startDocument(null);
            }

            if (multiObject) {
                write("</" + MULTI_OBJECT_ELEMENT + ">\n");
            }
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

    private void startDocument(final String firstElement) {
        if (includeHeader) {
            write("<?xml version='1.0' encoding='UTF-8'?>\n");

            String docType = multiObject ? MULTI_OBJECT_ELEMENT : firstElement;
            write("<!DOCTYPE " + docType + " PUBLIC '" + CONNECTORS_DTD + "' '" + CONNECTORS_DTD + "'>\n");
        }
        if (multiObject) {
            write("<" + MULTI_OBJECT_ELEMENT + ">\n");
        }
    }

    private void write(final String str) {
        try {
            output.write(str);
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }
}
