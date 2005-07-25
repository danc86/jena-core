/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    [See end of file]
    $Id$
*/
package com.hp.hpl.jena.graph.query;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;

/**
    PatternStageBase contains the features that are common to the 
    traditional PatternStage engine and the Faster engine. (Eventually
    the two will merge back together.)
    @author hedgehog
*/
public abstract class PatternStageBase extends Stage
    {
    protected static int count = 0;
    protected ValuatorSet [] guards;
    
    public abstract static class Finder
        {   
        public abstract Iterator find( Domain d );
        }
    /**
        Set the <code>guards</code> from the <code>constraints</code>, using the
        variable-bindings in <code>map</code>, according to where they are fully
        bound by the bindings in <code>triples</code>. 
        @see GuardArranger
    */
    protected void setGuards( Mapping map, ExpressionSet constraints, Triple[] triples )
        { this.guards = new GuardArranger( triples ).makeGuards( map, constraints ); }

    protected void run( Pipe source, Pipe sink, StageElement se )
        {
        try { while (stillOpen && source.hasNext()) se.run( source.get() ); }
        catch (Exception e) { sink.close( e ); return; }
        sink.close();
        }

    public synchronized Pipe deliver( final Pipe result )
        {
        final Pipe stream = previous.deliver( new BufferPipe() );
        final StageElement s = makeStageElementChain( result, 0 );
        new Thread( "PatternStage-" + ++count ) 
            { public void run() { PatternStageBase.this.run( stream, result, s ); } } 
            .start();
        return result;
        }
    
    protected abstract StageElement makeStageElementChain( Pipe sink, int index );
    }
/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
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