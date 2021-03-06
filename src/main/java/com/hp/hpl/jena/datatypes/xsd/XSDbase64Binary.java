/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.datatypes.xsd;

import java.util.Arrays;

import org.apache.xerces.impl.dv.util.Base64;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Implement base64binary type. Most of the work is done in the superclass.
 * This only needs to implement the unparsing.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:56:03 $
 */
public class XSDbase64Binary extends XSDDatatype {
    
    /**
     * Constructor. 
     * @param typeName the name of the XSD type to be instantiated, this is 
     * used to lookup a type definition from the Xerces schema factory.
     */
    public XSDbase64Binary(String typeName) {
        super(typeName, byte[].class);
    }
         
    /**
     * Test whether the given object is a legal value form
     * of this datatype. Brute force implementation.
     */
    @Override
    public boolean isValidValue(Object valueForm) {
        return (valueForm instanceof byte[]);
    }
    
    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        if (value instanceof byte[]) {
            return Base64.encode((byte[])value);
        } else {
            throw new DatatypeFormatException("base64 asked to encode an unwrapped byte array");
        }
    }
    
    /**
     * Compares two instances of values of the given datatype.
     * This ignores lang tags and just uses the java.lang.Number 
     * equality.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return value1.getDatatype() == value2.getDatatype()
            && Arrays.equals((byte[])value1.getValue(), (byte[])value2.getValue());
//      && value1.getLexicalForm().equals(value2.getLexicalForm());  // bug tracking, not real code
    }
   
    @Override
    public int getHashCode( LiteralLabel lit )
        { return getHashCode( (byte []) lit.getValue() ); }
}
