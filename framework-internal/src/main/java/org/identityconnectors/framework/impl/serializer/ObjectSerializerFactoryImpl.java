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
package org.identityconnectors.framework.impl.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.identityconnectors.framework.common.serializer.BinaryObjectDeserializer;
import org.identityconnectors.framework.common.serializer.BinaryObjectSerializer;
import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;
import org.identityconnectors.framework.common.serializer.XmlObjectResultsHandler;
import org.identityconnectors.framework.common.serializer.XmlObjectSerializer;
import org.identityconnectors.framework.impl.serializer.binary.BinaryObjectDecoder;
import org.identityconnectors.framework.impl.serializer.binary.BinaryObjectEncoder;
import org.identityconnectors.framework.impl.serializer.xml.XmlObjectParser;
import org.identityconnectors.framework.impl.serializer.xml.XmlObjectSerializerImpl;
import org.xml.sax.InputSource;


public class ObjectSerializerFactoryImpl extends ObjectSerializerFactory {

    @Override
    public BinaryObjectDeserializer newBinaryDeserializer(InputStream is) {
        return new BinaryObjectDecoder(is);
    }

    @Override
    public BinaryObjectSerializer newBinarySerializer(OutputStream os) {
        return new BinaryObjectEncoder(os);
    }
    
    @Override
    public XmlObjectSerializer newXmlSerializer(Writer w, 
            boolean includeHeader,
            boolean multiObject) {
        return new XmlObjectSerializerImpl(w,includeHeader,multiObject);
    }

    @Override
    public void deserializeXmlStream(InputSource is, 
            XmlObjectResultsHandler handler,
            boolean validate) {
        XmlObjectParser.parse(is, handler, validate);
    }

}
