/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            01-Apr-2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.*;



/**
 * <p>
 * Class comment
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public class TestAxioms
    extends PathTestCase 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestAxioms( String s ) {
        super( s );
    }
    
    // External signature methods
    //////////////////////////////////

    protected String getTestName() {
        return "TestAxioms";
    }
    
    public static TestSuite suite() {
        return new TestAxioms( "TestAxioms" ).getSuite();
    }
    
    
    /** Fields are testID, pathset, property, profileURI, sourceData, expected, count, valueURI, rdfTypeURI, valueLit */
    protected Object[][] psTestData() {
        return new Object[][] {
            {   
                "OWL AllDifferent.distinctMembers",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        Resource r = m.listSubjectsWithProperty( RDF.type, m.getProfile().ALL_DIFFERENT() ).nextResource();
                        return ((AllDifferent) r.as( AllDifferent.class )).p_distinctMembers(); } 
                },
                OWL.distinctMembers,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Axioms/test.rdf",
                T,
                new Integer( 1 ),
                null,
                OWL.List,
                null
            },
            {   
                "DAML AllDifferent.distinctMembers",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        Resource r = m.createResource();  // there's no resource of rdf:type AllDifferent in the test file
                        return ((AllDifferent) r.as( AllDifferent.class )).p_distinctMembers(); } 
                },
                OWL.distinctMembers,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Axioms/test.rdf",
                F,
                null,
                null,
                null,
                null
            },
        };
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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

