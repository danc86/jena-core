/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id$
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;


import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.mem.ObjectIterator;
import com.hp.hpl.jena.util.iterator.*;

public class FasterTripleStore
    {
    protected NodeToTriplesMapFaster subjects = new NodeToTriplesMapFaster
        ( Field.getSubject, Field.getPredicate, Field.getObject );
    
    protected NodeToTriplesMapFaster predicates = new NodeToTriplesMapFaster
        ( Field.getPredicate, Field.getObject, Field.getSubject );
        
    protected NodeToTriplesMapFaster objects = new NodeToTriplesMapFaster
        ( Field.getObject, Field.getSubject, Field.getPredicate );
        
    public NodeToTriplesMapFaster forTestingOnly_getObjects()
        { return objects; }
    
    public NodeToTriplesMapFaster forTestingOnly_getSubjects()
        { return subjects; }
    
    protected Graph parent;
    
    public FasterTripleStore( Graph parent )
        { this.parent = parent; }
    
    /**
         Destroy this triple store - discard the indexes.
    */
    public void close()
        { subjects = predicates = objects = null; }
    
    /**
         Add a triple to this triple store.
    */
    public void add( Triple t )
        {
        if (subjects.add( t ))
            {
            predicates.add( t );
            objects.add( t ); 
            }
        }
    
    /**
         Remove a triple from this triple store.
    */
    public void delete( Triple t )
        {
        if (subjects.remove( t ))
            {
            predicates.remove( t );
            objects.remove( t ); 
            }
        }
    
    /**
         Clear this store, ie remove all triples from it.
    */
    public void clear()
        {
        subjects.clear();
        predicates.clear();
        objects.clear();
        }
    
    /**
         Answer the size (number of triples) of this triple store.
    */
    public int size()
        { return subjects.size(); }
    
    /**
         Answer true iff this triple store is empty.
    */
    public boolean isEmpty()
        { return subjects.isEmpty(); }
    
    public ExtendedIterator listSubjects()
        { return WrappedIterator.createNoRemove( subjects.domain() ); }
    
    public ExtendedIterator listPredicates()
        { return WrappedIterator.createNoRemove( predicates.domain() ); }
        
    public ExtendedIterator listObjects()
        {
        return new ObjectIterator( objects.domain() )
            {
            protected Iterator iteratorFor( Object y )
                { return objects.get( y ).iterator(); }
            };
        }
    
    /**
         Answer true iff this triple store contains the (concrete) triple <code>t</code>.
    */
    public boolean contains( Triple t )
        { return subjects.contains( t ); }
    
    /** 
        Answer an ExtendedIterator returning all the triples from this store that
        match the pattern <code>m = (S, P, O)</code>.
        
        <p>Because the node-to-triples maps index on each of subject, predicate,
        and (non-literal) object, concrete S/P/O patterns can immediately select
        an appropriate map. Because the match for literals must be by sameValueAs,
        not equality, the optimisation is not applied for literals. [This is probably a
        Bad Thing for strings.]
        
        <p>Practice suggests doing the predicate test <i>last</i>, because there are
        "usually" many more statements than predicates, so the predicate doesn't
        cut down the search space very much. By "practice suggests" I mean that
        when the order went, accidentally, from S/O/P to S/P/O, performance on
        (ANY, P, O) searches on largish models with few predicates declined
        dramatically - specifically on the not-galen.owl ontology.
    */
    public ExtendedIterator find( TripleMatch tm )
        {
        Triple t = tm.asTriple();
        Node pm = t.getPredicate();
        Node om = t.getObject();
        Node sm = t.getSubject();
            
        if (sm.isConcrete())
            return new StoreTripleIteratorFaster( parent, subjects.iterator( sm, pm, om ), subjects, predicates, objects );
        else if (om.isConcrete())
            return new StoreTripleIteratorFaster( parent, objects.iterator( om, sm, pm ), objects, subjects, predicates );
        else if (pm.isConcrete())
            return new StoreTripleIteratorFaster( parent, predicates.iterator( pm, om, sm ), predicates, subjects, objects );
        else
            return new StoreTripleIteratorFaster( parent, subjects.iterateAll( sm, pm, om ), subjects, predicates, objects );
        }
    
    public Applyer createApplyer( ProcessedTriple pt )
        {
        if (pt.S instanceof QueryNode.Fixed) 
            return subjects.createFixedSApplyer( pt );
        if (pt.O instanceof QueryNode.Fixed) 
            return objects.createFixedOApplyer( pt );
        if (pt.S instanceof QueryNode.Bound) 
            return subjects.createBoundSApplyer( pt );
        if (pt.O instanceof QueryNode.Bound) 
            return objects.createBoundOApplyer( pt );
        return varSvarOApplyer( pt );
        }

    protected Applyer varSvarOApplyer( final QueryTriple pt )
        { 
        return new Applyer()
            {
            protected final QueryNode p = pt.P;
        
            public Iterator find( Domain d )
                {
                Node P = p.finder( d );
                if (P.isConcrete())
                    return predicates.iterator( P, Node.ANY, Node.ANY );
                else
                    return subjects.iterator();
                }
    
            public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                Iterator it = find( d );
                while (it.hasNext())
                    if (m.match( d, (Triple) it.next() )) 
                         next.run( d );
                }
            };
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