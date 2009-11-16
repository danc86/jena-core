/******************************************************************
 * File:        XMLLiteralType.java
 * Created by:  Dave Reynolds
 * Created on:  08-Dec-02
 * 
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.vocabulary.RDF;
import org.xml.sax.SAXException;
import java.io.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;

/**
 * Builtin data type to represent XMLLiteral (i.e. items created
 * by use of <code>rdf:parsetype='literal'</code>.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public class XMLLiteralType extends BaseDatatype implements RDFDatatype {
    /** Singleton instance */
    public static final RDFDatatype theXMLLiteralType = new XMLLiteralType(RDF.getURI() + "XMLLiteral");
    
    /**
     * Private constructor.
     */
    private XMLLiteralType(String uri) {
        super(uri);
    }
    
    /**
     * Convert a serialize a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        return value.toString();
    }
    
    
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            parseXml(lexicalForm);
        } catch (SAXException e) {
            throw new DatatypeFormatException(lexicalForm, this, "SAXException: " + e);
        } catch (IOException e) {
            throw new DatatypeFormatException(lexicalForm, this, "IOException: " + e);
        }
        return lexicalForm; // XXX could we return the parsed form instead here?
    }

    @Override
    public boolean isValid(String lexicalForm) {
        try {
            parseXml(lexicalForm);
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    private void parseXml(String xml) throws SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        SAXParser parser;
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        parser.parse(new InputSource(new StringReader(xml)), new DefaultHandler());
    }

}

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
