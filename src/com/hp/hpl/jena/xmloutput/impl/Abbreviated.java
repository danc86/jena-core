/*
 *  (c)     Copyright 2000, 2001, 2002 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id$
 */

package com.hp.hpl.jena.xmloutput.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.io.*;
//Writer;
//import java.io.PrintWriter;

/** Writes out RDF in the abbreviated syntax,  for human consumption 
   not only machine readable.
 * It is not normal to call the constructor directly, but to use
 * the method RDFWriterF.getWriter("RDF/XML-ABBREV").
 * Does not support the <code>NSPREFIXPROPBASE</code> system properties.
 * Use <code>setNsPrefix</code>.
 * For best results it is necessary to set the property 
   <code>"prettyTypes"</code>. See setProperty for information.
   @see com.hp.hpl.jena.rdf.model.RDFWriterF#getWriter
 * @author jjc
 * @version  Release='$Name$' Revision='$Revision$' Date='$Date$'
 */
public class Abbreviated extends BaseXMLWriter implements RDFErrorHandler {

	private Resource types[] =
		new Resource[] {
			DAML_OIL.Ontology,
			OWL.Ontology,
			DAML_OIL.Datatype,
			//OWL.DataRange, named or orphaned dataranges unusual.      
			RDFS.Datatype,
			DAML_OIL.Class,
			RDFS.Class,
			OWL.Class,
			DAML_OIL.Property,
			OWL.ObjectProperty,
			RDF.Property,
			DAML_OIL.ObjectProperty,
			OWL.DatatypeProperty,
			DAML_OIL.DatatypeProperty,
			OWL.TransitiveProperty,
			OWL.SymmetricProperty,
			OWL.FunctionalProperty,
			OWL.InverseFunctionalProperty,
			DAML_OIL.TransitiveProperty,
			DAML_OIL.UnambiguousProperty,
			DAML_OIL.UniqueProperty,
			};
            
	boolean sReification;
    
    
	boolean sIdAttr;
    boolean sDamlCollection;
    boolean sParseTypeCollectionPropertyElt;
    boolean sListExpand;
    boolean sParseTypeLiteralPropertyElt;
    boolean sParseTypeResourcePropertyElt;
    boolean sPropertyAttr;
    

    boolean sResourcePropertyElt;

	void unblockAll() {
		sDamlCollection = false;
		sReification = false;
		sResourcePropertyElt = false;
		sParseTypeLiteralPropertyElt = false;
		sParseTypeResourcePropertyElt = false;
		sParseTypeCollectionPropertyElt = false;
		sIdAttr = false;
		sPropertyAttr = false;
        sListExpand = false;
	}
    {
        unblockAll();
        blockRule(RDFSyntax.propertyAttr);
    }
    void blockRule(Resource r) {
        if (r.equals(RDFSyntax.sectionReification)) sReification=true;
       // else if (r.equals(RDFSyntax.resourcePropertyElt)) sResourcePropertyElt=true;
        else if (r.equals(RDFSyntax.sectionListExpand)) sListExpand=true;
        else if (r.equals(RDFSyntax.parseTypeLiteralPropertyElt)) sParseTypeLiteralPropertyElt=true;
        else if (r.equals(RDFSyntax.parseTypeResourcePropertyElt)) sParseTypeResourcePropertyElt=true;
        else if (r.equals(RDFSyntax.parseTypeCollectionPropertyElt)) sParseTypeCollectionPropertyElt=true;
        else if (r.equals(RDFSyntax.idAttr)) {
            sIdAttr=true;
            sReification = true;
        }
        else if (r.equals(RDFSyntax.propertyAttr)) sPropertyAttr=true;
        else if (r.equals(DAML_OIL.collection)) sDamlCollection=true;
        else {
            logger.warn("Cannot block rule <"+r.getURI()+">");
        }
    }
	Resource[] setTypes(Resource[] propValue) {
		Resource[] rslt = types;
		types = (Resource[]) propValue;
		return rslt;
	}

	void writeBody(
		Model model,
		PrintWriter pw,
		String base,
		boolean useXMLBase) {
		Unparser unp = new Unparser(this, base, model, pw);

		unp.setTopLevelTypes(types);
		//unp.useNameSpaceDecl(nameSpacePrefices);
		if (useXMLBase)
			unp.setXMLBase(base);
		unp.write();
	}

	// Implemenatation of RDFErrorHandler
	public void error(Exception e) {
		errorHandler.error(e);
	}

	public void warning(Exception e) {
		errorHandler.warning(e);
	}

	public void fatalError(Exception e) {
		errorHandler.fatalError(e);
	}

	static public void main(String args[]) throws Exception {
		System.out.println("Test code for bug 77");
		Model m = new com.hp.hpl.jena.mem.ModelMem();
		m.read(
			new FileInputStream("modules/rdf/regression/arp/bug51_0.rdf"),
			"http://example.org/file");
		RDFWriter pw = m.getWriter("RDF/XML-ABBREV");
		m.setNsPrefix("eg", "http://example.org/");
		m.setNsPrefix("eg2", "http://example.org/foo#");
		pw.write(m, System.out, "http://example.org/file");
	}

}
/*
	(c) Copyright 200, 2003 Hewlett-Packard Development Company, LP
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
