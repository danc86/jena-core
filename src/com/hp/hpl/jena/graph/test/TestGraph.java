/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.graph.test;

/**
    @author kers
<br>
    even more extended testcase code
*/

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;

import junit.framework.*;

public class TestGraph extends GraphTestBase
    { 
	public TestGraph( String name )
		{
		super( name );
		};
		
    public static Test suite()
        { return new TestSuite( TestGraph.class ); }   

    public static void testAGraph( String title, Graph g )
        {
        graphAdd( g, "x R y; p S q; a T b" );
    /* */
        assertContainsAll( title + ": simple graph", g, "x R y; p S q; a T b" );
        assertEquals( title + ": size", g.size(), 3 );
        graphAdd( g, "spindizzies lift cities; Diracs communicate instantaneously" );
        assertEquals( title + ": size after adding", g.size(), 5 );
        g.delete( triple( "x R y" ) );
        g.delete( triple( "a T b" ) );
        assertEquals( title + ": size after deleting", g.size(), 3 );
        assertContainsAll( title + ": modified simple graph", g, "p S q; spindizzies lift cities; Diracs communicate instantaneously" );
        assertOmitsAll( title + ": modified simple graph", g, "x R y; a T b" );
    /* */ 
        ClosableIterator it = g.find( null, node("lift"), null );
        assertTrue( title + ": finds some triple(s)", it.hasNext() );
        assertEquals( title + ": finds a 'lift' triple", triple("spindizzies lift cities"), it.next() );
        assertFalse( title + ": finds exactly one triple", it.hasNext() );
        }

    public static void testStuff()
        {
        testAGraph( "StoreMem", new GraphMem() );
        testAGraph( "StoreMemBySubject", new GraphMem() );
//        String [] empty = new String [] {};
//        Graph g = graphWith( "x R y; p S q; a T b" );
//    /* */
//        assertContainsAll( "simple graph", g, "x R y; p S q; a T b" );
//        graphAdd( g, "spindizzies lift cities; Diracs communicate instantaneously" );
//        g.delete( triple( "x R y" ) );
//        g.delete( triple( "a T b" ) );
//        assertContainsAll( "modified simple graph", g, "p S q; spindizzies lift cities; Diracs communicate instantaneously" );
//        assertOmitsAll( "modified simple graph", g, "x R y; a T b" );
        }
                    
                    
	public static void testModelEquals()
		{
		Graph g1 = graphWith( "x R y; p R q" );
        assertEquals( "model must equal a copy of itself", new ModelMem( g1 ), new ModelMem( g1 ) );
		}
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
