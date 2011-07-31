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


/**
 * Interface to abstract away the difference between deserializing
 * xml and binary
 */
public interface ObjectDecoder {
    /**
     * Reads an object using the appropriate serializer for that object
     * @param fieldName. A hint of the field name. Ignored for binary
     * serialization. The subelement name for xml serialization
     * @expectedType Ignored for binary serialization. For xml serialization,
     * this must be specified if it was written in-line.
     * @dflt The default value if there is no value. 
     */
    public Object readObjectField(String fieldName,
            Class<?> expectedType,
            Object dflt);
    
    /**
     * Reads a boolean.
     * @param fieldName. A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value. 
     */
    public boolean readBooleanField(String fieldName, boolean dflt);
    
    /**
     * Reads an int.
     * @param fieldName. A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value. 
     */
    public int readIntField(String fieldName, int dflt);
    
    /**
     * Reads a long.
     * @param fieldName. A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value. 
     */
    public long readLongField(String fieldName, long dflt);
    
    /**
     * Reads a float.
     * @param fieldName. A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value. 
     */
    public float readFloatField(String fieldName, float dflt );
    
    /**
     * Reads a Class.
     * @param fieldName. A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value. 
     */
    public Class<?> readClassField(String fieldName, Class<?> dflt );

    /**
     * Reads a String.
     * @param fieldName. A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value. 
     */
    public String readStringField(String fieldName, String dflt );
    
    /**
     * Reads a double.
     * @param fieldName. A hint of the field name. Ignored for binary
     * serialization. The attribute name for xml serialization
     * @dflt The default value if there is no value. 
     */
    public double readDoubleField(String fieldName, double dflt );
    
    /**
     * Reads the value in-line. 
     */
    public String readStringContents( );

    /**
     * Reads the value in-line. 
     */
    public boolean readBooleanContents( );

    /**
     * Reads the value in-line. 
     */
    public int readIntContents( );
    
    /**
     * reads the value in-line. 
     */
    public long readLongContents();
    
    /**
     * Reads the value in-line. 
     */
    public float readFloatContents();
    
    /**
     * reads the value in-line. 
     */
    public double readDoubleContents();
    
    /**
     * reads the value in-line. 
     */
    public byte [] readByteArrayContents();
    
    /**
     * reads the value in-line. 
     */
    public Class<?> readClassContents();
    
    /**
     * Returns the number of anonymous sub-objects.
     * @return
     */
    public int getNumSubObjects();
    
    /**
     * Reads a sub-object
     */
    public Object readObjectContents(int index);
    
}
