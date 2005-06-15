/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

/**
	NodeToTriplesMap: a map from nodes to sets of triples.
	Subclasses must override at least one of useXXXInFilter methods.
	@author kers
*/
public abstract class NodeToTriplesMap 
    {
    /**
         The map from nodes to Set(Triple).
    */
    private Map map = CollectionFactory.createHashedMap();
    
    public Map forTestingOnly_getMap()
        { return map; }
    
    /**
          The number of triples held in this NTM, maintained incrementally 
          (because it's a pain to compute from scratch).
    */
    private int size = 0;
    
    /**
         The nodes which appear in the index position of the stored triples; useful
         for eg listSubjects().
    */
    public Iterator domain()
        { return map.keySet().iterator(); }
    
    /**
         Subclasses must over-ride to return the node at the index position in the
         triple <code>t</code>; should be equivalent to one of getSubject(),
         getPredicate(), or getObject().
    */
    public abstract Node getIndexNode( Triple t );
    
    /**
         Add <code>t</code> to this NTM; the node <code>o</code> <i>must</i>
         be the index node of the triple. Answer <code>true</code> iff the triple
         was not previously in the set, ie, it really truly has been added. 
    */
    public boolean add( Triple t ) 
        {
        Node o = getIndexNode( t );
        Set s = (Set) map.get( o );
        if (s == null) map.put( o, s = CollectionFactory.createHashedSet() );
        if (s.add( t )) { size += 1; return true; } else return false; 
        }

    /**
         Remove <code>t</code> from this NTM. Answer <code>true</code> iff the 
         triple was previously in the set, ie, it really truly has been removed. 
    */
    public boolean remove( Triple t )
        { 
        Node o = getIndexNode( t );
        Set s = (Set) map.get( o );
        if (s == null)
            return false;
        else
            {
            boolean result = s.remove( t );
            if (result) size -= 1;
            if (s.isEmpty()) map.put( o, null );
            return result;
        	} }
    
    /**
         Answer an iterator over all the triples in this NTM which have index node
         <code>o</code>.
    */
    public Iterator iterator( Node o ) 
        {
        Set s = (Set) map.get( o );
        return s == null ? NullIterator.instance :  s.iterator();
        }
    
    /**
         Answer an iterator over all the triples in this NTM.
    */
    public ExtendedIterator iterator()
        {
        final Iterator nodes = domain();
        return new NiceIterator()
        	{
            private Iterator current = NullIterator.instance;
            
            public Object next()
                {
                if (hasNext() == false) noElements( "NodeToTriples iterator" );
                return current.next();
                }
            
            public boolean hasNext()
                {
                while (true)
                    {
                    if (current.hasNext()) return true;
                    if (nodes.hasNext() == false) return false;
                    current = iterator( (Node) nodes.next() );
                    }
                }
            
            public void remove()
                {
                current.remove();
                size -= 1;
                }
        	};
        }
    
    /**
         Clear this NTM; it will contain no triples.
    */
    public void clear() 
        { map.clear(); size = 0; }
    
    public int size()
        { return size; }
    
    public boolean isEmpty()
        { return size == 0; }

    /**
         Answer an iterator over all the triples in this NTM which are accepted by
         <code>pattern</code>.
    */
    public ExtendedIterator iterateAll( Triple pattern )
        {
        if (pattern.getSubject().isConcrete() 
           || pattern.getObject().isConcrete() 
           || pattern.getPredicate().isConcrete())
            return iterator() .filterKeep ( new TripleMatchFilter( pattern ) );
        else
            return iterator();
        }
    
    /**
         Answer an iterator over all the triples in this NTM with index node 
         <code>x</code> and which are accepted by <code>pattern</code>.
    */
    public ExtendedIterator iterator( Triple pattern )
        {
        Node x = getIndexNode( pattern );
        Filter f = buildFilterFromPattern( pattern );
        return f == null ? WrappedIterator.create(iterator( x )) : new FilterIterator( f, iterator(x)); 
//        return new FilterIterator( new TripleMatchFilter( pattern ), iterator( x ) );
        }

    /**
        Answer true iff this NTM contains the concrete triple <code>t</code>.
    */
    public boolean contains( Triple t )
        { 
        Set s = (Set) map.get( getIndexNode( t ) );
        return s == null ? false : s.contains( t );
        }
    
    boolean useInFilter(Node n) 
        {
        return n.isConcrete();
        }
    public boolean useSubjectInFilter( Triple t ) 
        {
        return useInFilter(t.getSubject());
        }
    public boolean useObjectInFilter( Triple t ) 
    {
    return useInFilter(t.getObject());
    }
    public boolean usePredicateInFilter( Triple t ) 
    {
       return useInFilter(t.getPredicate());
    }
    
    Filter buildFilterFromPattern(Triple pattern) {
        if (useSubjectInFilter(pattern)) {
            final Node sp = pattern.getSubject();
            if (usePredicateInFilter(pattern)) {
                if (useObjectInFilter(pattern)){
                   throw new BrokenException("logic error");
                } else {
                    return new Filter() {
                        public boolean accept(Object o) {
                            Triple t = (Triple)o;
                            return sp.matches(t.getSubject());
                        }
                    };
                }
            } else {
                if (useObjectInFilter(pattern)){

                    final Node op = pattern.getObject();
                    return new Filter() {
                        public boolean accept(Object o) {
                            Triple t = (Triple)o;
                            return sp.matches(t.getSubject())
                              && op.matches(t.getObject());
                        }
                    };                    
                } else {
                    return new Filter() {
                        public boolean accept(Object o) {
                            return sp.matches(((Triple)o).getSubject());
                        }
                    };
                }
            }
        } else {
        if (usePredicateInFilter(pattern)) {
            final Node pp = pattern.getPredicate();
            if (useObjectInFilter(pattern)){
                final Node op = pattern.getObject();
                return new Filter() {
                    public boolean accept(Object o) {
                        Triple t = (Triple)o;
                        return pp.matches(t.getPredicate())
                          && op.matches(t.getObject());
                    }
                };
            } else {
                return new Filter() {
                    public boolean accept(Object o) {
                        Triple t = (Triple)o;
                        return pp.matches(t.getPredicate());
                    }
                };
            }
        } else {
            if (useObjectInFilter(pattern)){
                final Node op = pattern.getObject();
                return new Filter() {
                    public boolean accept(Object o) {
                        Triple t = (Triple)o;
                        return op.matches(t.getObject());
                    }
                };
            } else {
                return null;
            }
        }
    }
    }
    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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