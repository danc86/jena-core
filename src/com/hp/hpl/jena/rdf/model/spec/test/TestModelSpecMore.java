/*
(c) Copyright 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
[See end of file]
$$
*/
package com.hp.hpl.jena.rdf.model.spec.test;

import java.io.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecImpl;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.* ;

import junit.framework.TestSuite;

/**
    A second bunch of tests for ModelSpecs [because the first bunch is too busy]
*/
public class TestModelSpecMore extends ModelTestBase
    {
    public TestModelSpecMore( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestModelSpecMore.class ); }    
    
    public void testLoadWorks() throws Exception
        {
        String url = makeModel( "a bb c" );
        Model wanted = FileManager.get().loadModel( url );
        Model spec = modelWithStatements( "_root rdf:type jms:PlainModelSpec; _root jms:maker jms:MemMaker; _root jms:loadWith " + url );
        ModelSpec ms = ModelFactory.createSpec( spec );
        Model m = ModelFactory.createModel( ms );
        assertIsoModels( wanted, m );
        }
    
    public void testLoadOnInfModel() throws Exception
        {
        String url = makeModel( "birds fly south" );
        Model wanted = FileManager.get().loadModel( url );
        Model spec = modelWithStatements
            ( "_this jms:maker _maker; _this jms:reasonsWith _reasoner; _this jms:loadWith " + url
            + "; _reasoner jms:reasoner http://jena.hpl.hp.com/2003/RDFSExptRuleReasoner" );
        ModelSpec ms = ModelFactory.createSpec( spec );
        Model m = ModelFactory.createModel( ms );
        assertSubModelOf( wanted, m );
        }
    
    protected void assertSubModelOf( Model sub, Model entire )
        {
        if (sub.difference( entire ).size() > 0)
            fail( "not a sub-model" );
        }
    
    public void testLoadMultiWorks() throws Exception
	    {
        String url1 = makeModel( "dogs may bark" ), url2 = makeModel( "pigs might fly" );
	    Model wanted = FileManager.get().loadModel(url1) ;
        FileManager.get().readModel(wanted,url2) ;
	    Model spec = modelWithStatements( "_root rdf:type jms:PlainModelSpec; _root jms:maker jms:MemMaker" );
	    modelAdd( spec, "_root jms:loadWith " + url1 );
	    modelAdd( spec, "_root jms:loadWith " + url2 );
	    ModelSpec ms = ModelFactory.createSpec( spec );
	    Model m = ModelFactory.createModel( ms );
	    assertIsoModels( wanted, m );
	    }
    
    protected String makeModel( String statements ) throws FileNotFoundException, IOException
        {
	    String name = FileUtils.tempFileName( "test-load-with-", ".rdf" ).getAbsolutePath();
        Model m = modelWithStatements( statements );
        FileOutputStream fos = new FileOutputStream( name );
        m.write( fos, FileUtils.guessLang( name ) ); 
        fos.close();
	    return "file:" + name;
        }
    
    public void testOpenModel()
        {
        Model s = modelWithStatements( "_root jms:maker jms:MemMaker" );
        assertInstanceOf( Model.class, ModelFactory.createSpec( s ).openModel( "nosuch" ) );
        }
    
    public void testModelSpecImpl() 
        {
        Model d = modelWithStatements( "_x jms:modelName 'redrose'" );
        ModelSpecImpl s = new MiniModelSpec( resource( d, "_x" ), d );
        ModelMaker maker = s.getModelMaker();
        assertFalse( maker.hasModel( "redrose" ) );
        Model m1 = s.createDefaultModel();
        assertTrue( maker.hasModel( "redrose" ) );
        }
    
    public void testModelSpecImplOpen()
        {
        Model d = modelWithStatements( "" );
        ModelSpecImpl s = new MiniModelSpec( resource( d, "_x" ), d );
        s.openModel( "sundog" );
        assertTrue( s.getModelMaker().hasModel( "sundog" ) );
        }    
    
    public void testModelSpecImplOpenIfPresent()
        {
        Model d = modelWithStatements( "" );
        ModelSpecImpl s = new MiniModelSpec( resource( d, "_x" ), d );
        assertNull( s.openModelIfPresent( "sundog" ) );
        s.openModel( "sundog" );
        assertNotNull( s.openModelIfPresent( "sundog" ) );
        }
    
    protected final class MiniModelSpec extends ModelSpecImpl
        {
        protected MiniModelSpec( Resource root, Model description )
            {
            super( root, description );
            }
    
        protected Model doCreateModel()
            {            
            return null;
            }
    
        public Model createModelOver( String name )
            {            
            return null;
            }
    
        public Property getMakerProperty()
            {            
            return null;
            }

        public Model implementCreateModelOver(String name)
            {
            return null;
            }
        }

    }

/*
    (c) Copyright 2004, 2005, 2006 Hewlett-Packard Development Company, LP
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
