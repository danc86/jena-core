/*
 (c) Copyright 2005 Hewlett-Packard Development Company, LP
 All rights reserved - see end of file.
 $Id$
 */

package com.hp.hpl.jena.assembler;

import java.lang.reflect.*;
import java.util.*;

import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.assembler.exceptions.NoSpecificTypeException;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;

/**
    AssemblerHelp provides utility methods used by, and useful for working with,
    the Assembler code, including the methods that expand a model to include the
    required inferences and to find the most specific type of a root in an
    assembler specification.
    
    @author kers
*/
public class AssemblerHelp
    {
    /**
        Answer a Resource .equals() to <code>root</code>, but in the expanded
        model.
    */
    public static Resource withFullModel( Resource root )
        { return (Resource) root.inModel( fullModel( root.getModel() ) ); }
    
    /**
        Answer the full model of <code>m</code>, with all its imports included and
        with the necessary properties added from the JA schema. However, if
        the magic footprint triple (ja:this, rdf:type, ja:Expanded) is present in the
        model, it is returned unchanged.
    */
    public static Model fullModel( Model m )
        {
        if (m.contains( JA.This, RDF.type, JA.Expanded )) 
            return m;
        else
            {
            Model result = ModelExpansion.withSchema( withImports( m ), JA.getSchema() )
                .add( JA.This, RDF.type, JA.Expanded )
                ;
            result
                .setNsPrefixes( PrefixMapping.Extended )
                .setNsPrefixes( m )
                .setNsPrefix( "ja", JA.getURI() )
                ;
            return result;
            }
        }

    /**
        Answer <code>model</code> if it has no imports, or a union model with
        <code>model</code> as its base and its imported models as the other
        components.
    */
    public static Model withImports( Model model )
        { return withImports( FileManager.get(), model ); }

    /**
        Answer <code>model</code> if it has no imports, or a union model with
        <code>model</code> as its base and its imported models as the other
        components. The file manager <code>fm</code> is used to load the
        imported models.
    */
    public static Model withImports( FileManager fm, Model model )
        {
        StmtIterator it = model.listStatements( null, OWL.imports, (RDFNode) null );
        if (it.hasNext())
            {
            MultiUnion g = new MultiUnion( new Graph[] { model.getGraph() } );
            Model result = ModelFactory.createModelForGraph( g );
            while (it.hasNext()) g.addGraph( graphFor( fm, it.nextStatement() ) );
            return result;
            }
        else
            return model;
        }

    private static Map cache = new HashMap();
    
    private static Graph graphFor( FileManager fm, Statement s )
        {
        Resource url = s.getResource();
        Graph already = (Graph) cache.get( url );
        if (already == null)
            {
            Graph result = withImports( fm, fm.loadModel( url.getURI() ) ).getGraph();
            cache.put( url, result );
            return result;
            }
        else
            return already;
        }

    /**
         Load all the classes which are objects of any (t, ja:assembler, S) statements 
         in <code>m</code>. <code>group.implementWIth(t,c)</code> is called
         for each statement, where <code>c</code> is an instance of the class named
         by <code>S</code>. The order in which the classes are loaded is not
         specified, and loading stops immediately if any class cannot be loaded.
    */
    public static void loadClasses( AssemblerGroup group, Model m )
        {
        Property ANY = null;
        StmtIterator it = m.listStatements( null, JA.assembler, ANY );
        while (it.hasNext()) loadClass( group, it.nextStatement() );
        }

    private static void loadClass( AssemblerGroup group, Statement statement )
        {
        try
            {
            Resource type = statement.getSubject();
            Class c = Class.forName( statement.getString() );
            Constructor con = getResourcedConstructor( c );
            if (con == null)
                establish( group, type, c.newInstance() );
            else
                establish( group, type, con.newInstance( new Object[] { statement.getSubject() } ) );
            }
        catch (Exception e)
            { throw new JenaException( e ); }
        }

    private static void establish( AssemblerGroup group, Resource type, Object x )
        {
        if (x instanceof Assembler)
            group.implementWith( type, (Assembler) x );
        else
            throw new JenaException( "constructed entity is not an Assembler: " + x );
        }

    private static Constructor getResourcedConstructor( Class c )
        {
        try { return c.getConstructor( new Class[] { Resource.class } ); }
        catch (SecurityException e) { return null; }
        catch (NoSuchMethodException e) { return null; }
        }

    /**
         Answer the most specific type of <code>root</code> that is a subclass of
         ja:Object. If there are no candidate types, answer <code>null</code> (for
         historical reasons that may evaporate). If there is more than one type,
         throw a NoSpecificTypeException.
    */
    public static Resource findSpecificType( Resource root )
        {
        Model desc = root.getModel();
        StmtIterator types = root.listProperties( RDF.type );
        List results = new ArrayList();
        // System.err.println( ">> considering " + root + " ---------------------------" );
        while (types.hasNext())
            {
            Resource type = types.nextStatement().getResource();
            // System.err.println( "]]  possible type " + type );
            if (desc.contains( type, RDFS.subClassOf, JA.Object ))
                {
                // System.err.println( "]]    and it's a subClass of Object" );
                boolean allowed = true;
                for (StmtIterator subs = desc.listStatements( null, RDFS.subClassOf, type ); subs.hasNext(); )
                    {
                    Resource sub = subs.nextStatement().getSubject();
                    if (!sub.equals( type ) && root.hasProperty( RDF.type, sub )) 
                        { 
                        // System.err.println( "]]    rejected: it has a more specific subtype " + sub ); 
                        allowed = false; 
                        }
                    }
                if (allowed) 
                    { 
                    // System.err.println( "]]    we can add it" ); 
                    results.add( type );
                    }
                }
            }
        if (results.size() == 1)
            return (Resource) results.get(0);
        if (results.size() == 0)
            return JA.Object;
        throw new NoSpecificTypeException( root, results );
        }
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */