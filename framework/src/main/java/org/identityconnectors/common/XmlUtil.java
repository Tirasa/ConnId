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

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtil {
    private XmlUtil() {

    }

    /////////////////////////////////////////////////////////////
    //
    // Constants
    //
    ////////////////////////////////////////////////////////////

    public static final char NO_DELIM     = 0;
    public static final char DOUBLE_QUOTE = '"';
    public static final char SINGLE_QUOTE = '\'';

    /////////////////////////////////////////////////////////////
    //
    // Parsing
    //
    ////////////////////////////////////////////////////////////

    /**
     * Parses a string without validation and returns the Document.
     */
    public static Document parseString(String xml)
        throws IOException, SAXException, ParserConfigurationException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        //some parsers will attempt to find and parse dtd even
        //if not validating and that makes it very slow
        db.setEntityResolver(new DummyDTDResolver());
        InputSource is = new InputSource(new StringReader(xml));
        return db.parse(is);
    }

    private static class DummyDTDResolver implements EntityResolver {
        public InputSource resolveEntity(String publicID, String systemID) {
            if ((publicID != null && publicID.endsWith(".dtd"))
                    || (systemID != null && systemID.endsWith(".dtd"))) {
                return new InputSource(new StringReader(""));
            } else {
                return null;
            }
        }
    }

    /////////////////////////////////////////////////////////////
    //
    // DOM Navigation utilities
    //
    ////////////////////////////////////////////////////////////

    /**
     * Return the value of an attribute on an element. <p/> The DOM getAttribute
     * method returns an empty string if the attribute doesn't exist. Here, we
     * detect this and return null.
     */
    public static String getAttribute(Element e, String name) {
        String value = e.getAttribute(name);
        if (value != null && value.length() == 0)
            value = null;
        return value;
    }

    /**
     * Find an immediate child of the given name
     */
    public static Element findImmediateChildElement(Node node, String name) {

        Element found = null;
        
        if (node != null) {

            for (Node child = node.getFirstChild(); child != null
                    && found == null; child = child.getNextSibling()) {

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element tmp = (Element) child;
                    if ( tmp.getTagName().equals(name) ) {
                        return tmp;
                    }
                }
            }
        }

        return found;
    }

    /**
     * Returns the First child element or null if none found
     * @param node The node. May be null.
     * @return the First child element or null if none found
     */
    public static Element getFirstChildElement(Node node) {
        if ( node == null ) {
            return null;
        }
        Node child = node.getFirstChild();
        if ( child instanceof Element ) {
            return (Element)child;
        }
        else {
            return getNextElement(child);
        }
    }
    
    /**
     * Get the next right sibling that is an element.
     */
    public static Element getNextElement(Node node) {

        Element found = null;

        if (node != null) {

            for (Node next = node.getNextSibling(); next != null
                    && found == null; next = next.getNextSibling()) {

                if (next.getNodeType() == Node.ELEMENT_NODE)
                    found = (Element) next;
            }
        }

        return found;
    }

    /**
     * Locate the first text node at any level below the given node. If the
     * ignoreEmpty flag is true, we will ignore text nodes that contain only
     * whitespace characteres. <p/> Note that if you're trying to extract
     * element content, you probably don't want this since parser's can break up
     * pcdata into multiple adjacent text nodes. See getContent() for a more
     * useful method.
     */
    private static Text findText(Node node, boolean ignoreEmpty) {

        Text found = null;

        if (node != null) {

            if (node.getNodeType() == Node.TEXT_NODE
                    || node.getNodeType() == Node.CDATA_SECTION_NODE) {

                Text t = (Text) node;
                if (!ignoreEmpty)
                    found = t;
                else {
                    String s = t.getData().trim();
                    if (s.length() > 0)
                        found = t;
                }
            }

            if (found == null) {

                for (Node child = node.getFirstChild(); child != null
                        && found == null; child = child.getNextSibling()) {

                    found = findText(child, ignoreEmpty);
                }
            }
        }

        return found;
    }


    /**
     * Return the content of the given element. <p/> We will descend to an
     * arbitrary depth looking for the first text node. <p/> Note that
     * the parser may break what was originally a single string of pcdata into
     * multiple adjacent text nodes. Xerces appears to do this when it
     * encounters a '$' in the text, not sure if there is specified behavior, or
     * if its parser specific. <p/> Here, we will congeal adjacent text nodes.
     * <p/> We will NOT ignore text nodes that have only whitespace.
     */
    public static String getContent(Element e) {

        String content = null;

        if (e != null) {

            // find the first inner text node,
            Text t = findText(e, false);
            if (t != null) {
                // we have at least some text
                StringBuilder b = new StringBuilder();
                while (t != null) {
                    b.append(t.getData());
                    Node n = t.getNextSibling();

                    t = null;
                    if (n != null
                            && ((n.getNodeType() == Node.TEXT_NODE) || 
                                    (n.getNodeType() == Node.CDATA_SECTION_NODE))) {
                        t = (Text) n;
                    }
                }
                content = b.toString();
            }
        }

        return content;
    }

    /////////////////////////////////////////////////////////////
    //
    // Xml Encoding Utilities
    //
    ////////////////////////////////////////////////////////////

    /**
     * Escapes the given string and appends to the given buffer
     * @param b The buffer
     * @param s The script to be escaped. May be null.
     * @param delim May be {@link #SINGLE_QUOTE}, {@link #DOUBLE_QUOTE}, or {@link #NO_DELIM}.
     */
    public static void escape(StringBuilder b, String s, char delim) {
        if (s != null) {

            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);

                if (ch == '&') {
                    // Ampersand: introduces a character entity.
                    b.append("&amp;");
                } else if (ch == '<') {
                    // LessThan: introduces a tag.
                    b.append("&lt;");
                } else if (ch == '>') {
                    // GreaterThan: some browsers impute an opening "<".
                    b.append("&gt;");
                } else if (ch == 0xA) {
                    // LineFeed: preserve format.
                    b.append("&#xA;");
                } else if (ch == 0xD) {
                    // CarriageReturn: preserve format.
                    b.append("&#xD;");
                } else if (ch == 0x9) {
                    // HorizontalTab: preserve format.
                    b.append("&#x9;");
                } else if (ch == delim && delim == SINGLE_QUOTE) {
                    // Accept only single or double quote as delimiter.
                    b.append("&#39;");
                } else if (ch == delim && delim == DOUBLE_QUOTE) {
                    // Accept only single or double quote as delimiter.
                    // Does "&quot;" work in XML?
                    b.append("&#34;");
                } else if (ch >= 0x20 && ch < 0x7f) {
                    b.append(ch);
                } else if (validXmlChar(ch)) {
                    b.append(ch);
                }
            }
        }
    }

    /**
     * legal xml chars from http://www.xml.com/axml/testaxml.htm Char::= #x9 |
     * #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     */
    private static boolean validXmlChar(char ch) {
        if (ch >= 0x20 && ch < 0x7f)
            return true; // short circuit test

        if (ch == 0x09 || ch == 0x0A || ch == 0x0D
                || (ch >= 0x20 && ch <= 0xfd7ff)
                || (ch >= 0x0E000 && ch <= 0xffffd)
                || (ch >= 0x010000 && ch <= 0xF10ffff))
            return true;

        return false;
    }
}
