/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            23-May-2003
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


/**
 * <p>
 * Unit tests for ontology individuals
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public class TestIndividual 
    extends OntTestBase 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////



    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    static public TestSuite suite() {
        return new TestIndividual( "TestIndividual" );
    }
    
    public TestIndividual( String name ) {
        super( name );
    }
    
    
    // External signature methods
    //////////////////////////////////

    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "Individual.sameIndividualAs", true, false, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    Individual x = m.createIndividual( A );
                    Individual y = m.createIndividual( A );
                    Individual z = m.createIndividual( A );
                    
                    x.addSameIndividualAs( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_INDIVIDUAL_AS() ) );
                    assertEquals( "x should be the same as y", y, x.getSameIndividualAs() );
                    assertTrue( "x should be the same as y", x.isSameIndividualAs( y ) );
                    
                    x.addSameIndividualAs( z );
                    assertEquals( "Cardinality should be 2", 2, x.getCardinality( prof.SAME_INDIVIDUAL_AS() ) );
                    iteratorTest( x.listSameIndividualAs(), new Object[] {z,y} );
                    
                    x.setSameIndividualAs( z );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_INDIVIDUAL_AS() ) );
                    assertEquals( "x should be same indiv. as z", z, x.getSameIndividualAs() );
                    
                    x.removeSameIndividualAs( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_INDIVIDUAL_AS() ) );
                    x.removeSameIndividualAs( z );
                    assertEquals( "Cardinality should be 0", 0, x.getCardinality( prof.SAME_INDIVIDUAL_AS() ) );
                }
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


