/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
    a FragmentMap maps Nodes to Triples/Fragments and from Triples to Nodes.
    
    @author kers
*/

public class FragmentMap 
    {
    public FragmentMap() { super(); }
    
    protected Map inverseMap = HashUtils.createMap();
    
    protected Map forwardMap = HashUtils.createMap();
    
    /**
        update the map with (node -> triple); return the triple
    */
    public Triple putTriple( Node key, Triple value )
        {
        forwardMap.put( key, value );
        inversePut( value, key );
        return value;
        }
    
    public Object get( Node tag )
        { return forwardMap.get( tag ); }
    
    public void removeFragments( Node key )
        {
        forwardMap.remove( key );
        }
    
    /**
     * @param value
     * @param key
     */
    private void inversePut( Triple value, Node key )
        {
        Set s = (Set) inverseMap.get( value );
        if (s == null) inverseMap.put( value, s = new HashSet() );
        s.add( key );
        }

    /**
         remove the binding key -> triple. key *must* be bound to triple - this is a 
         utility method designed for those circumstances.
    */
    public void removeTriple( Node key, Triple value )
        {
        forwardMap.remove( key );
        inverseRemove( value, key );
        }
    
    public void removeTriple( Node key )
        {
        Object t = forwardMap.get( key );
        forwardMap.remove( key );
        if (t instanceof Triple) inverseRemove( (Triple) t, key );
        }
        
    /**
     * @param value
     * @param key
     */
    private void inverseRemove( Triple value, Node key )
        {
        Set s = (Set) inverseMap.get( value );
        if (s != null)
            {
            s.remove( key );
            if (s.isEmpty()) inverseMap.remove( value );
            }
        
        }

    /**
        update the map with (node -> fragment); return the fragment.
    */
    public Fragments putFragments( Node key, Fragments value )
        {
        forwardMap.put( key, value );
        return value;
        }        
        
    /**
        add to a graphy thing all of the triples that correspond to a reification
        of t on tag.
    */         
    public static void graphAddQuad( GraphAdd g, Node node, Triple t )
        {
        g.add( Triple.create( node, RDF.Nodes.subject, t.getSubject() ) );
        g.add( Triple.create( node, RDF.Nodes.predicate, t.getPredicate() ) );
        g.add( Triple.create( node, RDF.Nodes.object, t.getObject() ) );
        g.add( Triple.create( node, RDF.Nodes.type, RDF.Nodes.Statement ) );
        }
                
    /**
        Answer an iterator over all the reification triples in this map that match the
        filter <code>tm</code>. We optimise slightly; if the subject of the match is
        concrete, then we only look at the triples arising from that subject, which
        is easy because our map keys on it. Otherwise we hand the job over to
        a FragmentTripleIterator and just let it filter the iterator slowly.
    <p>
        We *could* see if the predicate is sensible, since we'll only have
        rdf:[subject|predicate|object|type] triples, but we don't, for now.
        
        @param tm the triple match for the interesting triples
        @return an iterator over all the reification triples
    */
    public ExtendedIterator allTriples( TripleMatch tm )
        {
        Triple t = tm.asTriple();
        Node subject = t.getSubject();
        if (subject.isConcrete())
            {
            Object x = forwardMap.get( subject );  
            return x == null
                ? new NiceIterator()
                : FragmentTripleIterator.toIterator( t, subject, x )
                ; 
            }
        else
            {
            final Iterator it = forwardMap.entrySet().iterator();   
            return new FragmentTripleIterator( t, it );
            }
        }
        
    /**
        Return the fragment map as a read-only Graph of triples. We rely on the
        default code in GraphBase which allows us to only implement find(TripleMatch)
        to present a Graph. All the hard work is done by allTriples.
    */
    public Graph asGraph()
        {
        return new GraphBase()
            { public ExtendedIterator find( TripleMatch tm ) { return allTriples( tm ); } };
        }

    /**
         Answer true iff we have a reified triple <code>t</code>.
    */
    public boolean hasTriple( Triple t )
        { return inverseMap.containsKey( t ); }

    /**
         Answer an iterator over all the fragment tags in this map.
     */
    public Iterator tagIterator()
        { return forwardMap.keySet().iterator(); }
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