/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.Triple;

import java.util.*;

/**
    A subclass of TrackingTripleIterator that implements .remove() so that as well as
    removing the designated triple from the underlying source, it also removes it from
    a base triple set and from two other triple sets. [Thus GraphMem index iterators
    can update their siblings.]
	@author kers
 */
public class TripleFieldIterator extends TrackingTripleIterator
    {
    /**
        The other triple-maps to update 
    */
    private NodeToTriplesMap A, B;
    private Set triples;
    
    TripleFieldIterator
        ( Triple t , Iterator it, Set triples, NodeToTriplesMap A, NodeToTriplesMap B )
        { super( t, it ); this.triples = triples; this.A = A; this.B = B; }    
        
    /**
        Remove the current triple from its source [using super.remove()], from the given
        triple-set, and from the other two NodeMaps.
         
     	@see java.util.Iterator#remove()
    */
    public void remove()
        {
        super.remove();     
        triples.remove( current );
        // A.remove( current.getSubject(), current );
        // B.remove( current.getObject(), current );
        }       
    }
/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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