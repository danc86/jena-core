/******************************************************************
 * File:        TestBugs.java
 * Created by:  Dave Reynolds
 * Created on:  22-Aug-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

//import java.util.*;

/**
 * Unit tests for reported bugs in the rule system.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public class TestBugs extends TestCase {

    /**
     * Boilerplate for junit
     */ 
    public TestBugs( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestBugs.class );
    }  

    /**
     * Report of NPE during processing on an ontology with a faulty intersection list,
     * from Hugh Winkler.
     * 
     * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
     * @version $Revision$ on $Date$
     */
    public void testIntersectionNPE() {
        Model base = ModelFactory.createDefaultModel();
        base.read("file:testing/reasoners/bugs/bad-intersection.owl");
        boolean foundBadList = false;
        try {
            InfGraph infgraph = ReasonerRegistry.getOWLReasoner().bind(base.getGraph());
            ExtendedIterator ci = infgraph.find(null, RDF.Nodes.type, OWL.Class.asNode());
            ci.close();
        } catch (ReasonerException e) {
            foundBadList = true;
        }
        assertTrue("Correctly detected the illegal list", foundBadList);
    }
    
    /**
     * Report of problems with cardinality v. maxCardinality usage in classification,
     * from Hugh Winkler.
     * 
     * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
     * @version $Revision$ on $Date$
     */
    public void testCardinality1() {
        Model base = ModelFactory.createDefaultModel();
        base.read("file:testing/reasoners/bugs/cardFPTest.owl");
        InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);
        String NAMESPACE = "urn:foo#";
        Resource aDocument = test.getResource(NAMESPACE + "aDocument");
        Resource documentType = test.getResource(NAMESPACE + "Document");
        assertTrue("Cardinality-based classification", test.contains(aDocument, RDF.type, documentType));
    }
    
    /**
     * Report of functor literals leaking out of inference graphs and raising CCE
     * in iterators.
     */
    public void testFunctorCCE() {
        Model base = ModelFactory.createDefaultModel();
        base.read("file:testing/reasoners/bugs/cceTest.owl");
        InfModel test = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), base);

        boolean b = anyInstancesOfNothing(test);
        ResIterator rIter = test.listSubjects();
        while (rIter.hasNext()) { 
            Resource res = rIter.nextResource();
        }
    }
    
    /** Helper function used in testFunctorCCE */
    private boolean anyInstancesOfNothing(Model model) { 
        boolean hasAny = false;
        try {
            ExtendedIterator it = model.listStatements(null, RDF.type, OWL.Nothing);
            hasAny = it.hasNext();
            it.close();
        } catch (ConversionException x) {
            hasAny = false;
        }
        return hasAny;
    }
    
    /**
     * Report of functor literals leaking out in DAML processing as well.
     */
    public void testDAMLCCE() {
        DAMLModel m = ModelFactory.createDAMLModel();
        m.read( "file:testing/reasoners/bugs/literalLeak.daml", 
                "http://www.daml.org/2001/03/daml+oil-ex",  null );
        ResIterator rIter = m.listSubjects();
        while (rIter.hasNext()) { 
            Resource res = rIter.nextResource();
            if (res.getNode().isLiteral()) {
                System.out.println("ERROR: " + res);
            }
        }

//        m.writeAll( System.err, "RDF/XML-ABBREV", "http://www.daml.org/2001/03/daml+oil-ex" );        
    }
}


/*
    (c) Copyright Hewlett-Packard Company 2003
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