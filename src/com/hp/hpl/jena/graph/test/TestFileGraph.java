/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;

import java.io.*;

import junit.framework.*;

/**
    Test FileGraph by seeing if we can make some file graphs and then read
    them back.

 	@author hedgehog
*/

public class TestFileGraph extends GraphTestBase
    {
    public TestFileGraph( String name )
        { super( name ); }

    // TODO want a wider variety of cases, now we've discovered how to abstract.
    public static TestSuite suite()
        {
        TestSuite result = new TestSuite( TestFileGraph.class );
        result.addTest( new Case( "x /R y", "xxxA", ".rdf" ) );
        result.addTest( new Case( "x /R y", "xxxB", ".n3" ) );
        result.addTest( new Case( "x /R y", "xxxC", ".nt" ) );
        result.addTest( new Case( "x /R y; p /R q", "xxxD", ".rdf" ) );
        result.addTest( new Case( "x /R y; p /R q", "xxxE", ".n3" ) );
        result.addTest( new Case( "x /R y; p /R q", "xxxF", ".nt" ) );
        result.addTest( new Case( "http://domain/S ftp:ftp/P O", "xxxG", ".rdf" ) );
        result.addTest( new Case( "http://domain/S ftp:ftp/P O", "xxxH", ".nt" ) );
        result.addTest( new Case( "http://domain/S ftp:ftp/P O", "xxxI", ".n3" ) );
        return result;
        }
        
    /**
        Test that the language code is guessed "correctly".
    */
    public void testGuessLang()
        {
        assertEquals( "N3", FileGraph.guessLang( "simple.n3") );
        assertEquals( "N3", FileGraph.guessLang( "hello.there.n3") );
        assertEquals( "N-TRIPLE", FileGraph.guessLang( "simple.nt" ) );
        assertEquals( "N-TRIPLE", FileGraph.guessLang( "whats.up.nt" ) );
        assertEquals( "RDF/XML", FileGraph.guessLang( "poggle.rdf") );
        assertEquals( "RDF/XML", FileGraph.guessLang( "dotless" ) );
        }

    public void testPlausibleGraphname()
        {
        assertTrue( FileGraph.isPlausibleGraphName( "agnessi.rdf" ) ); 
        assertTrue( FileGraph.isPlausibleGraphName( "parabola.nt" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "hyperbola.n3" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "chris.dollin.n3" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "hedgehog.spine.end.rdf" ) );    
        }
        
    public void testisPlausibleUppercaseGraphname()
        {
        assertTrue( FileGraph.isPlausibleGraphName( "LOUDER.RDF" ) ); 
        assertTrue( FileGraph.isPlausibleGraphName( "BRIDGE.NT" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "NOTN2.N3" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "chris.dollin.N3" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "hedgehog.spine.end.RDF" ) );        
        }
        
    public void testImPlausibleGraphName()
        {
        assertFalse( FileGraph.isPlausibleGraphName( "undecorated" ) );    
        assertFalse( FileGraph.isPlausibleGraphName( "danger.exe" ) );    
        assertFalse( FileGraph.isPlausibleGraphName( "pretty.jpg" ) );    
        assertFalse( FileGraph.isPlausibleGraphName( "FileGraph.java" ) );    
        assertFalse( FileGraph.isPlausibleGraphName( "infix.rdf.c" ) );                
        }
        
    /**
        Test that the graph encoded as the test-string content can be
        written out to a temporary file generated from the prefix and suffix,
        and then read back correctly. The temporary files are marked as
        delete-on-exit to try and avoid cluttering the user's filespace ...
     */
    private static class Case extends TestFileGraph
        {
        String content;
        String prefix;
        String suffix;

        Case( String content, String prefix, String suffix )
            {
            super( "Case: " + content + " in " + prefix + "*" + suffix );
            this.content = content;
            this.prefix = prefix;
            this.suffix = suffix;
            }
            
        public void runTest()
            {
            File foo = tempFileName( prefix, suffix );
            Graph original = graphWith( content );
            Graph g = new FileGraph( foo, true, true );
            g.getBulkUpdateHandler().add( original );
            g.close();
            Graph g2 = new FileGraph( foo, false, true );
            assertIsomorphic( original, g2 );
            g2.close();
            }
        }
        
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
