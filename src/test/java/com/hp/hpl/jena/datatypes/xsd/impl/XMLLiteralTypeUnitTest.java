package com.hp.hpl.jena.datatypes.xsd.impl;

import static org.junit.Assert.*;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import org.junit.Test;

public class XMLLiteralTypeUnitTest {
    
    private final RDFDatatype type = XMLLiteralType.theXMLLiteralType;
    
    @Test
    public void isValidShouldReturnFalseForBrokenXml() {
        assertFalse(type.isValid("<div><asdf></div>"));
        assertFalse(type.isValid("asdf"));
    }
    
    @Test
    public void isValidShouldReturnTrueForValidXml() {
        assertTrue(type.isValid("<div xmlns=\"http://www.w3.org/1999/xhtml\">" +
            "<p>Details of the <a href=\"/rdfschema/1.0/\">RDF schema used" + 
            "throughout the site</a> are now published in human-readable HTML" + 
            "format, and as RDF. Suggestions for improvement are welcome.</p></div>"));
    }

}
