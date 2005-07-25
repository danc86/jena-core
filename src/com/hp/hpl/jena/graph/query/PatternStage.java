/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
    A PatternStage is a Stage that handles some bunch of related patterns; those patterns
    are encoded as Triples.
    
    @author hedgehog
*/

public class PatternStage extends PatternStageBase
    {
    protected Graph graph;
    protected QueryTriple [] compiled;
    
    public PatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple [] triples )
        {
        this.graph = graph;
        this.compiled = QueryTriple.classify( getFactory(), map, triples );
        setGuards( map, constraints, triples );
        }

    protected QueryNodeFactory getFactory()
        { return QueryNode.factory; }
        
    protected StageElement makeStageElementChain( Pipe sink, int index )
        {
        if (index == compiled.length)
            return new StageElement.PutBindings( sink );
        else
            {
            QueryTriple p = compiled[index];
            Matcher m = p.createMatcher();
            Finder f = p.finder( graph );
            ValuatorSet s = guards[index];
            StageElement nextElement = makeStageElementChain( sink, index + 1 );
            StageElement next = s.isNonTrivial() 
                ? new StageElement.RunValuatorSet( s, nextElement ) 
                : nextElement
                ;
            return new FindTriples( m, f, next );
            }
        }    
    
    protected final class FindTriples extends StageElement
        {
        protected final Finder f;
        protected final Matcher m;
        protected final StageElement next;
        
        public FindTriples( Matcher m, Finder f, StageElement next )
            { this.f = f; this.next = next; this.m = m; }
        
        public final void run( Domain current )
            { 
            Iterator it = f.find( current );
            while (stillOpen && it.hasNext())
                if (m.match( current, (Triple) it.next() )) 
                    next.run( current );
            }
        }       
    }

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
