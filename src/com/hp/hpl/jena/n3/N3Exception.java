package com.hp.hpl.jena.n3;

/** Root exception for errors from N3 parsing and conversion of
 *  N3 to RDF
 *   
 * @author		Andy Seaborne
 * @version 	$Id$
 */

import com.hp.hpl.jena.rdf.model.RDFException;

public class N3Exception extends RDFException
{
    public N3Exception(String message) { super(RDFException.SYNTAXERROR, message) ; }
    
    public String getMessage() { return message ; }
}
