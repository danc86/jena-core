/*
  (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.graph.impl;

/**
	@author kers
<p>
    A base-level implementation of Reifier, intended to be straightforward
    and obvious. It fails this test nowadays ...
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.DisjointUnion;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

import com.hp.hpl.jena.vocabulary.RDF;

public class SimpleReifier implements Reifier
    {
    protected final GraphBase parent;
    protected final boolean intercepting;
    protected final boolean concealing;
    protected final ReificationStyle style;
    
    protected ReifierFragmentsMap fragmentsMap;
    protected ReifierTripleMap tripleMap;
    
    protected Graph reificationTriples;
    
    /** 
        construct a simple reifier that is bound to the parent graph .
        
        @param parent the Graph which we're reifiying for
        @param style the reification style to use
    */
    public SimpleReifier( GraphBase parent, ReificationStyle style )
        { this( parent, new SimpleReifierTripleMap(), new SimpleReifierFragmentsMap(), style ); }
    
    public SimpleReifier
        ( GraphBase parent, ReifierTripleMap tm, ReifierFragmentsMap fm, ReificationStyle style )
        {
        this.parent = parent;
        this.fragmentsMap = fm;
        this.tripleMap = tm;
        this.intercepting = style.intercepts();
        this.concealing = style.conceals();
        this.style = style; 
        }
        
    public ReificationStyle getStyle()
        { return style; }
            
    /** return the parent graph we are bound to */
    public Graph getParentGraph()
        { return parent; }
        
    /** return the triple bound to _n_ */
    public Triple getTriple( Node n )        
        { 
        return tripleMap.getTriple( n );
        }
        
    /** true iff there is a triple bound to _n_ */
    public boolean hasTriple( Node n )
    	{ return getTriple( n ) != null; }
        
    /** */
    public ExtendedIterator allNodes()
        { return tripleMap.tagIterator(); }
        
    public ExtendedIterator allNodes( Triple t )
        { return tripleMap.tagIterator( t ); }

    /** 
        reifiy a triple _t_ with tag _tag_. If a different triple is already
        reified under _tag_, throw an AlreadyReifiedException.
    */
    public Node reifyAs( Node tag, Triple t )
    	{
        Triple existing = (Triple) tripleMap.getTriple( tag );
        Fragments partial = fragmentsMap.getFragments( tag );
        if (existing != null)
            { if (!t.equals( existing )) throw new AlreadyReifiedException( tag ); }
        else if (partial == null)
            tripleMap.putTriple( tag, t );
        else
            { // TODO
            graphAddQuad( parent, tag, t );
            Triple t2 = getTriple( tag );
            if (t2 == null) throw new CannotReifyException( tag );
            }
        if (concealing == false) graphAddQuad( parent, tag, t );
        return tag; 
    	}
        
    /**
        If n is bound to the triple t, remove that triple. If we're not concealing reification 
        quadlets, we need to remove them from the parent graph too.
    */    	
    public void remove( Node n, Triple t )
        {
        Triple x = (Triple) tripleMap.getTriple( n );
        if (t.equals( x )) 
            { tripleMap.removeTriple( n, t ); 
            if (!concealing) parentRemoveQuad( n, t ); }
        }

    public void remove( Triple t )
        { tripleMap.removeTriple( t ); }
            
    public boolean hasTriple( Triple t )
        { return tripleMap.hasTriple( t ); }
          
    public boolean handledAdd( Triple fragment )
        {
        if (intercepting)
            {
            Fragments.Slot s = fragmentsMap.getFragmentSelector( fragment );  
            if (s == null)
                return false;
            else     
                {
                addFragment( s, fragment );
                return concealing;
                }
            }
        else
            return false;
        }

    /**
         Add <code>fragment</code> to the fragments already present. This may
         create a complete triple, or over-specify.
     * @param s
     * @param fragment
     */
    protected void addFragment( Fragments.Slot s, Triple fragment )
        {
        Node tag = fragment.getSubject(), object = fragment.getObject();
        Triple reified = tripleMap.getTriple( tag );
        if (reified == null)
            {
            Fragments partial = fragmentsMap.getFragments( tag );
            if (partial == null) fragmentsMap.putFragments( tag, partial = new Fragments( tag ) );
            partial.add( s, object );
            if (partial.isComplete())
                {
                tripleMap.putTriple( fragment.getSubject(), partial.asTriple() );
                fragmentsMap.removeFragments( fragment.getSubject() );
                }
            }
        else
            {
            if (s.clashedWith( fragmentsMap, object, reified )) 
                tripleMap.removeTriple( tag, reified );
//            if (s.clashesWith( fragmentsMap, object, reified ))
//                {
//                tripleMap.removeTriple( tag, reified );
//                fragmentsMap.putAugmentedTriple( s, tag, object, reified );
//                }
            }
        }

    public boolean handledRemove( Triple t )
        {
        if (intercepting)
            {
            Fragments.Slot s = fragmentsMap.getFragmentSelector( t );  
            if (s == null)
                return false;
            else     
                {
                Fragments fs = getFragment( t );
                fs.remove( s, t.getObject() );
                if (fs.isComplete()) 
                    {
                    tripleMap.putTriple( t.getSubject(), fs.asTriple() );
                    fragmentsMap.removeFragments( t.getSubject() );
                    }
                else 
                    {
                    tripleMap.removeTriple( t.getSubject() );
                    if (fs.isEmpty()) fragmentsMap.removeFragments( t.getSubject() );
                    }
                return concealing;
                }
            }
        else
            return false;
        }
                  
    private Fragments getFragment( Triple t )
        {
        Node s = t.getSubject();
        Triple already = (Triple) tripleMap.getTriple( s );
        Fragments partial = fragmentsMap.getFragments( s );
        return
            already != null ? explode( s, already )
            : partial == null ? fragmentsMap.putFragments( s, new Fragments( s ) )
            : (Fragments) partial;
        }
        
    private Fragments explode( Node s, Triple t )
        { return fragmentsMap.putFragments( s, new Fragments( s, t ) ); }

    public Graph getHiddenTriples()
        { return style == ReificationStyle.Standard ? Graph.emptyGraph : getReificationTriples(); }
    
    public Graph getReificationTriples()
        { if (reificationTriples == null) reificationTriples = new DisjointUnion( tripleMap.asGraph(), fragmentsMap.asGraph() ); 
        return reificationTriples; }
        
    /**
        remove from the parent all of the triples that correspond to a reification
        of t on tag.
    */
    private void parentRemoveQuad( Node n, Triple t )
        {
        parent.delete( Triple.create( n, RDF.Nodes.type, RDF.Nodes.Statement ) );
        parent.delete( Triple.create( n, RDF.Nodes.subject, t.getSubject() ) );
        parent.delete( Triple.create( n, RDF.Nodes.predicate, t.getPredicate() ) );
        parent.delete( Triple.create( n, RDF.Nodes.object, t.getObject() ) ); 
        }        
    
    public static void graphAddQuad( GraphAdd g, Node node, Triple t )
        {
        g.add( Triple.create( node, RDF.Nodes.subject, t.getSubject() ) );
        g.add( Triple.create( node, RDF.Nodes.predicate, t.getPredicate() ) );
        g.add( Triple.create( node, RDF.Nodes.object, t.getObject() ) );
        g.add( Triple.create( node, RDF.Nodes.type, RDF.Nodes.Statement ) );
        }      
    
    /**
        our string representation is <R ...> wrapped round the string representation
        of our node map.
    */
    public String toString()
        { return "<R " + fragmentsMap + "|" + tripleMap + ">"; }
    }
    
/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
