/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id$
*/
package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestSuite;

/**
     TestModelRead - test that the new model.read operation(s) exist.
     @author kers
 */
public class TestModelRead extends ModelTestBase
    {
    public TestModelRead( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelRead.class ); }
    
    public void testReturnsSelf()
        {
        Model m = ModelFactory.createDefaultModel();
        assertSame( m, m.read( "file:testing/modelReading/empty.n3", "base", "N3" ) );
        assertTrue( m.isEmpty() );
        }
    
    public void testLoadsSimpleModel()
        {
        Model expected = ModelFactory.createDefaultModel();
        Model m = ModelFactory.createDefaultModel();
        expected.read( "file:testing/modelReading/simple.n3", "N3" );
        assertSame( m, m.read( "file:testing/modelReading/simple.n3", "base", "N3" ) );
        assertIsoModels( expected, m );
        }    
    
    /*
         Suppressed, since the other Model::read(String url) operations apparently
         don't retry failing URLs as filenames. But the code text remains, so that
         when-and-if, we have a basis.
     */
//    public void testLoadsSimpleModelWithoutProtocol()
//        {
//        Model expected = ModelFactory.createDefaultModel();
//        Model m = ModelFactory.createDefaultModel();
//        expected.read( "testing/modelReading/simple.n3", "RDF/XML" );
//        assertSame( m, m.read( "testing/modelReading/simple.n3", "base", "N3" ) );
//        assertIsoModels( expected, m );
//        }    
    
    public void testSimpleLoadImplictBase()
        {
        Model mBasedImplicit = ModelFactory.createDefaultModel();
        String fn = RelURI.resolveFileURL( "file:testing/modelReading/based.n3" );
        Model wanted = 
            ModelFactory.createDefaultModel()
            .add( resource( fn ), property( "jms:predicate" ), resource( "jms:object" ) );
        mBasedImplicit.read( fn, "N3" );
        assertIsoModels( wanted, mBasedImplicit );
        }
    
    public void testSimpleLoadExplicitBase()
        {
        Model mBasedExplicit = ModelFactory.createDefaultModel();
        mBasedExplicit.read( "file:testing/modelReading/based.n3", "http://example/", "N3" );
        assertIsoModels( modelWithStatements( "http://example/ jms:predicate jms:object" ), mBasedExplicit );
        }
    
    public void testDefaultLangXML()
        {
        Model m = ModelFactory.createDefaultModel();
        m.read( "file:testing/modelReading/plain.rdf", null, null );
        }
    }


/*
	(c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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