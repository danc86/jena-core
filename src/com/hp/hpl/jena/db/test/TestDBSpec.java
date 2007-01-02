/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.db.test;

import com.hp.hpl.jena.rdf.model.test.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.*;

/**
    @author kers
*/
public class TestDBSpec extends ModelTestBase 
	{
	public TestDBSpec( String name )
		{ super( name ); }
		
    public static TestSuite suite()
        { return new TestSuite( TestDBSpec.class ); }
        
    public void testCreateFailsUser()
        {
        try
            {Resource me = ResourceFactory.createResource();
            Model spec = ModelFactory.createDefaultModel()
                .add( me, RDF.type, JenaModelSpec.RDBMakerSpec );
            ModelMaker maker = ModelSpecImpl.createMaker( spec );
            assertTrue( maker.getGraphMaker() instanceof GraphRDBMaker );
            fail( "should not be able to make RDB model from empty specification" ); 
            }
        catch (Exception e) 
            { pass(); }
        }
            
    public void testCreateSuccessUser() throws ClassNotFoundException
        {
        Resource me = ResourceFactory.createResource();
        Resource con = ResourceFactory.createResource();
        Resource mak = ResourceFactory.createResource();
        String dbType = TestPackage.M_DB;
        String className = TestPackage.M_DBDRIVER_CLASS;
        Model spec = ModelFactory.createDefaultModel()
            .add( me, JenaModelSpec.maker, mak )
            .add( mak, RDF.type, JenaModelSpec.RDBMakerSpec )
            .add( mak, JenaModelSpec.hasConnection, con )
            .add( con, JenaModelSpec.dbUser, TestPackage.M_DB_USER )
            .add( con, JenaModelSpec.dbPassword, TestPackage.M_DB_PASSWD )
            .add( con, JenaModelSpec.dbURL, resource( TestPackage.M_DB_URL ) )
            .add( con, JenaModelSpec.dbType, dbType )
            .add( con, JenaModelSpec.dbClass, className )
            ;
        ModelMaker maker = ModelSpecImpl.createMaker( spec );
        assertTrue( maker.getGraphMaker() instanceof GraphRDBMaker );
        maker.openModel( "something" ).close();
        maker.removeModel( "something" );
        maker.close();
        }
    
    public void testCreateDBModelSpec()
        {
        Resource me = ResourceFactory.createResource();
        Resource dbMaker = ResourceFactory.createResource();
        Resource con = ResourceFactory.createResource();
        String dbType = TestPackage.M_DB;
        String className = TestPackage.M_DBDRIVER_CLASS;
        Model spec = ModelFactory.createDefaultModel()
        	.add( me, JenaModelSpec.maker, dbMaker )
            .add( dbMaker, RDF.type, JenaModelSpec.RDBMakerSpec )
            .add( dbMaker, JenaModelSpec.hasConnection, con )
            .add( con, JenaModelSpec.dbUser, TestPackage.M_DB_USER )
            .add( con, JenaModelSpec.dbPassword, TestPackage.M_DB_PASSWD )
            .add( con, JenaModelSpec.dbURL, resource( TestPackage.M_DB_URL ) )
            .add( con, JenaModelSpec.dbType, dbType )
            .add( con, JenaModelSpec.dbClass, className )
            ;
        ModelSpec s = ModelFactory.createSpec( spec );
        Model d = s.createFreshModel();
        assertTrue( d instanceof ModelRDB );
        }
	}
    
/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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