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
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl.test;


// Imports
///////////////
import java.util.*;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;


/**
 * <p>
 * Generic test case for DAML ontology unit testing
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public abstract class DAMLTestBase 
    extends TestSuite
{
    // Constants
    //////////////////////////////////

    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";
    
    
    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    
    // Constructors
    //////////////////////////////////

    public DAMLTestBase( String name ) {
        super( name );
        TestCase[] tc = getTests();
        
        for (int i = 0;  i < tc.length;  i++) {
            addTest( tc[i] );
        }
    }
    
    // External signature methods
    //////////////////////////////////


    // Internal implementation methods
    //////////////////////////////////

    /** Return the array of tests for the suite */
    protected  OntTestCase[] getTests() {
        return null;
    }
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected abstract class OntTestCase
        extends TestCase
    {
        protected String m_langElement;

        public OntTestCase( String langElement ) {
            super( "DAML API test " + langElement );
            m_langElement = langElement;
        }

        public void runTest()
            throws Exception
        {
            boolean profileEx = false;
            DAMLModel m = ModelFactory.createDAMLModel();
        
            try {
                doTest( m );
            }
            catch (ProfileException e) {
                profileEx = true;
            }
        
            assertTrue( "language element " + m_langElement + " was  expected in DAML model ", !profileEx );
        }
    
        /** Does the work in the test sub-class */
        protected abstract void doTest( DAMLModel m ) throws Exception;
    
        /** Test that an iterator delivers the expected values */
        protected void iteratorTest( Iterator i, Object[] expected ) {
            Logger logger = Logger.getLogger( getClass() );
            List expList = new ArrayList();
            for (int j = 0; j < expected.length; j++) {
                expList.add( expected[j] );
            }
        
            while (i.hasNext()) {
                Object next = i.next();
                
                // debugging
                if (!expList.contains( next )) {
                    logger.debug( getName() + " - Unexpected iterator result: " + next );
                }
                
                assertTrue( "Value " + next + " was not expected as a result from this iterator ", expList.contains( next ) );
                assertTrue( "Value " + next + " was not removed from the list ", expList.remove( next ) );
            }
        
            if (!(expList.size() == 0)) {
                logger.debug( getName() + "Expected iterator results not found" );
                for (Iterator j = expList.iterator(); j.hasNext(); ) {
                    logger.debug( getName() + " - missing: " + j.next() );
                }
            }
            assertEquals( "There were expected elements from the iterator that were not found", 0, expList.size() );
        }
    }
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

