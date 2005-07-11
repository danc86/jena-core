/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id$
*/

package com.hp.hpl.jena.mem.faster;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.CollectionFactory;

public class FasterPatternStage extends Stage
    {
    protected GraphMemFaster graph;
    protected MatcherAndFinder [] compiled;
    protected ValuatorSet [] guards;
    protected Set [] boundVariables;
    
    protected abstract static class Matcher
        {
        public abstract boolean match( Domain d, Triple t );
        
        public static final Matcher always = new Matcher() 
            {
            public boolean match( Domain d, Triple t )
                { return true; }
            };
        }

    protected abstract static class Finder
        {   
        public abstract Iterator find( Domain d );
        }
    
    protected static class MatcherAndFinder
        {
        final Matcher m;
        final Finder f;
        
        MatcherAndFinder( Matcher m, Finder f )
            { this.m = m; this.f = f; }
        }
    
    public FasterPatternStage( Graph graph, Mapping map, ExpressionSet constraints, Triple [] triples )
        {
        this.graph = (GraphMemFaster) graph;
        this.boundVariables = makeBoundVariables( triples );
        ProcessedTriple [] s = ProcessedTriple.allocateBindings( map, triples );
        this.guards = makeGuards( map, constraints, triples.length );
        this.compiled = matcherAndFinder( s, this.guards );
        }
                
    /**
        Answer an array of sets exactly as long as the argument array of Triples.
        The i'th element of the answer is the set of all variables that have been 
        matched when the i'th triple has been matched.
    */
    protected Set [] makeBoundVariables( Triple [] triples )
        {
        int length = triples.length;
        Set [] result = new Set[length];
        Set prev = CollectionFactory.createHashedSet();
        for (int i = 0; i < length; i += 1) 
            prev = result[i] = Util.union( prev, Util.variablesOf( triples[i] ) );
        return result;
        }
    
    /**
        Answer an array of ExpressionSets exactly as long as the supplied length.
        The i'th ExpressionSet contains the prepared [against <code>map</code>]
        expressions that can be evaluated as soon as the i'th triple has been matched.
        By "can be evaluated as soon as" we mean that all its variables are bound.
        The original ExpressionSet is updated by removing those elements that can
        be so evaluated.
        
        @param map the Mapping to prepare Expressions against
        @param constraints the set of constraint expressions to plant
        @param length the number of evaluation slots available
        @return the array of prepared ExpressionSets
    */
    protected ValuatorSet [] makeGuards( Mapping map, ExpressionSet constraints, int length )
        {        
        ValuatorSet [] result = new ValuatorSet [length];
        for (int i = 0; i < length; i += 1) result[i] = new ValuatorSet();
        Iterator it = constraints.iterator();
        while (it.hasNext())
            plantWhereFullyBound( (Expression) it.next(), it, map, result );
        return result;
        }
    
    /**
        Find the earliest triple index where this expression can be evaluated, add it
        to the appropriate expression set, and remove it from the original via the
        iterator.
    */
    protected void plantWhereFullyBound( Expression e, Iterator it, Mapping map, ValuatorSet [] es )
        {
        for (int i = 0; i < boundVariables.length; i += 1)
            if (canEval( e, i )) 
                { 
                es[i].add( e.prepare( map ) ); 
                it.remove(); 
                return; 
                }
        }
    
    /**
        Answer true iff this Expression can be evaluated after the index'th triple
        has been matched, ie, all the variables of the expression have been bound.
    */
    protected boolean canEval( Expression e, int index )
        { return Expression.Util.containsAllVariablesOf( boundVariables[index], e ); }
        
    protected MatcherAndFinder [] matcherAndFinder( ProcessedTriple [] patterns, ValuatorSet [] guards )
        {
        MatcherAndFinder [] result = new MatcherAndFinder[patterns.length];
        for (int i = 0; i < patterns.length; i += 1) 
            {
            final ValuatorSet s = guards[i];
            final Matcher m = patterns[i].makeMatcher( this );
            final Finder f = patterns[i].finder( graph );
            if (s.isNonTrivial())
                {                    
                Matcher m2 = new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        { return m.match( d, t ) && s.evalBool( d ); }
                    };
                result[i] = new MatcherAndFinder( m2, f );
                }
            else
                {
                result[i] = new MatcherAndFinder( m, f );
                }
            }
        return result;
        }
    
    public static abstract class PreindexedFind
        {
        public abstract Iterator find( Node X, Node Y );
        }    
    
    public static abstract class HalfindexedFind
        {
        public abstract Iterator find( Node X, Node Y, Node Z );
        }
    
    private static int count = 0;
    
    public synchronized Pipe deliver( final Pipe result )
        {
        final Pipe stream = previous.deliver( new BufferPipe() );
        final StageElement r = makeStageElement( result, 0 );
        new Thread( "PatternStage-" + ++count ) 
            { public void run() { FasterPatternStage.this.run( stream, result, r ); } } 
            .start();
        return result;
        }
        
    protected void run( Pipe source, Pipe sink, StageElement s )
        {
        try { while (stillOpen && source.hasNext()) s.run( source.get() ); } 
        catch (Exception e) { sink.close( e ); return; }
        sink.close();
        }        
    
    protected StageElement makeStageElement( Pipe sink, int index )
        {
        if (index == compiled.length)
            return new Put( sink );
        else
            {
            MatcherAndFinder p = compiled[index];
            return new Nest( p.m, p.f, makeStageElement( sink, index + 1 ) );
            }
        }
    
    protected static abstract class StageElement
        {
        public abstract void run( Domain current );
        }
    
    protected static final class Put extends StageElement
        {
        protected final Pipe sink;
        
        public Put( Pipe sink )
            { this.sink = sink; }
        
        public final void run( Domain current )
            { sink.put( current.copy() ); }
        }
    
    protected final class Nest extends StageElement
        {
        protected final Matcher matcher;
        protected final Finder finder;
        protected final StageElement nest;
        
        public Nest( Matcher matcher, Finder finder, StageElement nest )
            { this.matcher = matcher; this.finder = finder; this.nest = nest; }
        
        public final void run( Domain current )
            {
            Iterator it = finder.find( current );
            while (stillOpen && it.hasNext())
                if (matcher.match( current, (Triple) it.next() )) 
                    nest.run( current );
            }
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