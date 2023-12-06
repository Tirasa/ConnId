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

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Stack;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.XmlObjectResultsHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlObjectParser {

    private static final SAXParserFactory SAX_PARSER_FACTORY;

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    static {
        try {
            SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
            SAX_PARSER_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    public static void parse(
            final InputSource inputSource, final XmlObjectResultsHandler handler, final boolean validate) {

        try {
            SAXParser parser = SAX_PARSER_FACTORY.newSAXParser();
            MySAXHandler saxHandler = new MySAXHandler(handler, validate);
            parser.parse(inputSource, saxHandler);
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    private static class MySAXHandler extends DefaultHandler {

        /**
         * The document for the current top-level element. with each top-level
         * element, we discard the previous to avoid accumulating memory
         */
        private Document currentTopLevelElementDocument;

        /**
         * Stack of elements we are creating.
         */
        private final Stack<Element> elementStack = new Stack<>();

        /**
         * Do we want to validate.
         */
        private final boolean validate;

        /**
         * Results handler that we write our objects to.
         */
        private final XmlObjectResultsHandler handler;

        /**
         * Is the handler still handing.
         */
        private boolean _stillHandling = true;

        public MySAXHandler(final XmlObjectResultsHandler handler, final boolean validate) {
            this.handler = handler;
            this.validate = validate;
        }

        private Optional<Element> currentElement() {
            return Optional.ofNullable(elementStack.isEmpty() ? null : elementStack.peek());
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) {
            currentElement().ifPresent(e -> e.appendChild(
                    currentTopLevelElementDocument.createTextNode(new String(ch, start, length))));
        }

        @Override
        public void endElement(final String namespaceURI, final String localName, final String qName) {
            // we don't push the top-level MULTI_OBJECT_ELEMENT on the stack
            if (!elementStack.isEmpty()) {
                Element element = elementStack.pop();
                if (elementStack.isEmpty()) {
                    currentTopLevelElementDocument = null;
                    if (_stillHandling) {
                        XmlObjectDecoder decoder = new XmlObjectDecoder(element, null);
                        Object object = decoder.readObject();
                        _stillHandling = handler.handle(object);
                    }
                }
            }
        }

        @Override
        public void ignorableWhitespace(final char[] ch, final int start, final int length) {
            currentElement().ifPresent(e -> e.appendChild(
                    currentTopLevelElementDocument.createTextNode(new String(ch, start, length))));
        }

        @Override
        public void startElement(
                final String namespaceURI, final String localName, final String qName, final Attributes atts) {

            String name = StringUtil.isBlank(localName) ? qName : localName;

            Element element = null;
            if (elementStack.isEmpty()) {
                if (!XmlObjectSerializerImpl.MULTI_OBJECT_ELEMENT.equals(name)) {
                    try {
                        currentTopLevelElementDocument = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument();
                    } catch (Exception e) {
                        throw ConnectorException.wrap(e);
                    }
                    element = currentTopLevelElementDocument.createElement(name);
                }
            } else {
                element = currentElement().
                        map(e -> (Element) e.appendChild(currentTopLevelElementDocument.createElement(name))).
                        orElse(null);
            }

            if (element != null) {
                elementStack.push(element);
                for (int i = 0; i < atts.getLength(); i++) {
                    element.setAttribute(atts.getLocalName(i), atts.getValue(i));
                }
            }
        }

        @Override
        public InputSource resolveEntity(final String pubid, final String sysid) throws SAXException {
            if (XmlObjectSerializerImpl.CONNECTORS_DTD.equals(pubid)) {
                // stupid freakin sax parser. even if validation is turned off it still takes the same amount of
                // time. need to return an empty dtd to fake it out
                if (!validate) {
                    return new InputSource(new StringReader("<?xml version='1.0' encoding='UTF-8'?>"));
                }
                try {
                    return new InputSource(XmlObjectParser.class.getResource(pubid).openStream());
                } catch (IOException e) {
                    throw new SAXException(e);
                }
            } else {
                return null;
            }
        }
    }
}
