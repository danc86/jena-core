/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;
import junit.framework.*;

/**
    Tests for model events and listeners.
 	@author kers
*/
public class TestModelEvents extends ModelTestBase
    {
    public TestModelEvents(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestModelEvents.class ); }
        
    protected Model model;
    protected SimpleListener SL;
    
    public void setUp()
        { 
        model = ModelFactory.createDefaultModel(); 
        SL = new SimpleListener();
        }
        
    static class SimpleListener implements ModelChangedListener
        {
        List history = new ArrayList();
        
        public void addedStatement( Statement s )
            { history.add( "add" ); history.add( s ); }
            
        public void removedStatement( Statement s )
            { history.add( "remove" ); history.add( s ); }
            
        boolean has( Object [] things ) 
            { return history.equals( Arrays.asList( things ) ); }
        }
        
    public void testRegistrationCompiles()
        {
        assertSame( model, model.register( new SimpleListener() ) );
        }
        
    public void testUnregistrationCompiles()
        {
        model.unregister( new SimpleListener() );
        }
        
    public void testAddSingleStatements()
        {
        Statement S1 = statement( model, "S P O" );
        Statement S2 = statement( model, "A B C" );
        assertFalse( SL.has( new Object [] { "add", S1 } ) );
        model.register( SL );
        model.add( S1 );
        assertTrue( SL.has( new Object[] { "add", S1 } ) );
        model.add( S2 );
        assertTrue( SL.has( new Object[] { "add", S1, "add", S2 } ) );
        model.add( S1 );
        assertTrue( SL.has( new Object[] { "add", S1, "add", S2, "add", S1 } ) );
        }
        
    public void testTwoListeners()
        {
        Statement S = statement( model, "S P O" );
        SimpleListener SL1 = new SimpleListener();
        SimpleListener SL2 = new SimpleListener();
        model.register( SL1 ).register( SL2 );
        model.add( S );
        assertTrue( SL2.has( new Object[] { "add", S } ) );
        assertTrue( SL1.has( new Object[] { "add", S } ) );
        }
        
    public void testUnregisterWorks()
        {
        model.register( SL );
        model.unregister( SL );
        model.add( statement( model, "X R Y" ) );
        assertTrue( "SL should not have been poked", SL.has( new Object[] {} ) );
        }
        
    public void testRemoveSingleStatements()
        {
        Statement S = statement( model, "D E F" );
        model.register( SL );
        model.add( S );
        model.remove( S );
        assertTrue( SL.has( new Object[] { "add", S, "remove", S } ) );
        }
        
    public void testAddInPieces()
        {
        model.register( SL );
        model.add( resource( model, "S" ), property( model, "P" ), resource( model, "O" ) );
        // assertTrue( SL.has( new Object[] { "add", statement( model, "S P O ") } ) );
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