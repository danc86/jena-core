/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;


/**
 *
 * DBPropNode
 * 
 * A wrapper to assist in getting and setting DB property information from 
 * a persistent store.
 * 
 * This is written in the style of enhanced nodes - no state is
 * stored in the DBStoreDesc, instead all state is in the
 * underlying graph and this is just provided as a convenience.
 * 
 * (We don't use enhanced nodes because, since we control everything
 * in the persistent store system description, we can avoid any
 * need to handle polymorhphism).
 * 
 * 
 * @author csayers
 * @version $Revision$
 */
public abstract class DBProp {

	protected SpecializedGraph graph = null;
	protected Node self = null;
	
	public DBProp( SpecializedGraph g, Node n) {
		graph = g;
		self = n;
	}			
	
	public Node getNode() { return self; }
	
	protected void putPropString( Node_URI predicate, String value) {
		putPropNode(predicate, new Node_Literal( new LiteralLabel(value)));
	}		
	
	protected void putPropNode( Node_URI predicate, Node node) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Triple t = new Triple( self, predicate, node);
		graph.add( t, complete);
	}			
	
	protected String getPropString( Node_URI predicate) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		TripleMatch match = new StandardTripleMatch(self, predicate, null);
		ClosableIterator it = graph.find(match, complete);
		if( !it.hasNext() ) {
			it.close();
			return null;
		}
		Node result = ((Triple)it.next()).getObject();
		it.close();
		return result.toString();
	}			
	
	protected void remove() {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		TripleMatch match = new StandardTripleMatch(self, null, null);
		ClosableIterator it = graph.find(match, complete);
		while( it.hasNext() )
			graph.delete( (Triple) it.next(), complete);
		it.close();
		self = null;
		graph = null;
	}
	
	public static ExtendedIterator listTriples( SpecializedGraph g, Node self ) {
		// Get all the triples about the requested node.
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		return g.find(new StandardTripleMatch(self, null, null), complete);
	}
		
	protected static Node findProperty( Graph graph, Node_URI predicate ) {
		TripleMatch match = new StandardTripleMatch(null, predicate, null);
		ClosableIterator it = graph.find(match);
		Node result = null;
		if( it.hasNext() )
			result = ((Triple)it.next()).getObject();
		it.close();
		return result;
	}	

}

/*
 *  (c) Copyright Hewlett-Packard Company 2003.
 *  All rights reserved.
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