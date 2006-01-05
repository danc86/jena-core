/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id$
*/

package com.hp.hpl.jena.assembler.test;


import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.assembler.exceptions.CannotConstructException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

public abstract class AssemblerTestBase extends ModelTestBase
    {
    protected abstract Class getAssemblerClass();
    
    /**
         An assembler that always returns the same fixed object.
        @author kers
    */
    protected static final class FixedObjectAssembler extends AssemblerBase
        {
        private final Object x;
        
        protected FixedObjectAssembler( Object x )
            { this.x = x; }
        
        
        public Object create( Assembler a, Resource root )
            { return x; }
        }

    /**
        An assembler that insists on being called on a given name, and always
        returns the same fixed object.
        @author kers
    */
    protected static class NamedObjectAssembler extends AssemblerBase
            {
            final Resource name;
            final Object result;
            
            NamedObjectAssembler( Resource name, Object result )
                { this.name = name; this.result = result; }
            
            public Model createModel( Resource root )
                { return (Model) create( root ); }
            
            public Object create( Assembler a, Resource root )
                {
                assertEquals( name, root );
                return result;
                }
            }

    protected static final Model schema = JA.getSchema();

    public AssemblerTestBase( String name )
        { super( name ); }
    
    protected static Model model( String string )
        { 
        Model result = createModel( ReificationStyle.Standard );
        result.setNsPrefix( "ja", JA.getURI() );
        result.setNsPrefix( "lm", LocationMappingVocab.getURI() );
        return modelAdd( result, string );
        }

    protected static Resource resourceInModel( String string )
        {
        Model m = model( string );
        Resource r = resource( string.substring( 0, string.indexOf( ' ' ) ) );
        return (Resource) r.inModel( m );        
        }

    protected void testDemandsMinimalType( Assembler a, Resource type )
        {
        try
            { a.create( resourceInModel( "x rdf:type rdf:Resource" ) ); 
            fail( "should trap insufficient type" ); }
        catch (CannotConstructException e)
            {
            assertEquals( getAssemblerClass(), e.getAssemblerClass() );
            assertEquals( type, e.getType() ); 
            assertEquals( resource( "x" ), e.getRoot() );
            }
        }

    protected void assertSamePrefixMapping( PrefixMapping wanted, PrefixMapping got )
        {
        if (!wanted.samePrefixMappingAs( got ))
            fail( "wanted: " + wanted + " but got: " + got );
        }

    /**
         assert that the property <code>p</code> has <code>domain</code> as
         its rdfs:domain.
    */
    protected void assertDomain( Resource domain, Property p )
        { 
        if (!schema.contains( p, RDFS.domain, domain ))
            fail( p + " was expected to have domain " + domain );
        }    

    /**
         assert that the property <code>p</code> has <code>range</code> as
         its rdfs:range.
    */
    protected void assertRange( Resource range, Property p )
        { 
        if (!schema.contains( p, RDFS.range, range ))
            fail( p + " was expected to have range " + range );
        }

    /**
         assert that <code>expectedSub</code> is an rdfs:subClassOf
         <code>expectedSuper</code>.
    */
    protected void assertSubclassOf( Resource expectedSub, Resource expectedSuper )
        { 
        if (!schema.contains( expectedSub, RDFS.subClassOf, expectedSuper ))
            fail( expectedSub + " should be a subclass of " + expectedSuper ); 
        }

    /**
         assert that <code>instance</code> has rdf:type <code>type</code>.
    */
    protected void assertType( Resource type, Resource instance )
        {
        if (!schema.contains( instance, RDF.type, type ))
            fail( instance + " should have rdf:type " + type );
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/