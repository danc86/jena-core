/******************************************************************
 * File:        Datatype.java
 * Created by:  Dave Reynolds
 * Created on:  07-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.graph.dt;

import com.hp.hpl.jena.graph.LiteralLabel;

/**
 * Interface on a datatype representation. An instance of this
 * interface is needed to convert typed literals between lexical
 * and value forms. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public interface RDFDatatype {

    /**
     * Return the URI which is the label for this datatype
     */
    public String getURI();
    
    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    public String unparse(Object value);
    
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    public Object parse(String lexicalForm) throws DatatypeFormatException;
    
    /**
     * Test whether the given string is a legal lexical form
     * of this datatype.
     */
    public boolean isValid(String lexicalForm);
    
    /**
     * Test whether the given object is a legal value form
     * of this datatype.
     */
    public boolean isValidValue(Object valueForm);
    
    /**
     * Compares two instances of values of the given datatype.
     * This defaults to just testing equality of the java value
     * representation but datatypes can override this. We pass the
     * entire LiteralLabel to allow the equality function to take
     * the xml:lang tag and the datatype itself into account.
     */
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2);
    
    /**
     * If this datatype is used as the cannonical representation
     * for a particular java datatype then return that java type,
     * otherwise returns null.
     */
    public Class getJavaClass();
    
    /**
     * Returns an object giving more details on the datatype.
     * This is type system dependent. In the case of XSD types
     * this will be an instance of 
     * <code>org.apache.xerces.impl.xs.dv.XSSimpleType</code>.
     */
    public Object extendedTypeDefinition();
    
}

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

