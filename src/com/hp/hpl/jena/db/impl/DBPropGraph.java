/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.DB;

import java.util.*;

/**
 *
 * DBStoreDesc
 * 
 * A wrapper to assist in getting and setting DB information from 
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
public class DBPropGraph extends DBProp {

	/**
	 * @since Jena 2.0
	 */

	public static String graphNamePrefix = DB.getURI() + "Graph.";
	public static Node_URI graphType = (Node_URI)DB.graphType.getNode();
	public static Node_URI graphLSet = (Node_URI)DB.graphLSet.getNode();
	public static Node_URI graphPrefix = (Node_URI)DB.graphPrefix.getNode();
	
	public DBPropGraph( SpecializedGraph g, String name, String type) {
		super(g, new Node_URI(graphNamePrefix+name));
		
		putPropString(graphType, type);
	}
	
	public DBPropGraph( SpecializedGraph g, Node n) {
		super(g,n);
	}	
	
	public DBPropGraph( SpecializedGraph g, String newName, Graph oldProperties) {
		super(g, new Node_URI(graphNamePrefix+newName));
		
		putPropNode(graphType, DBProp.findProperty(oldProperties, graphType));
		
	}
	
	public void addLSet( DBPropLSet lset ) {
		putPropNode( graphLSet, lset.getNode() );
	}

	public void addPrefix( DBPropPrefix prefix ) {
		// First check it doesn't already exist
		DBPropPrefix existing = getPrefix( prefix.getValue());
		if( existing != null)
			removePrefix( existing);
		putPropNode( graphPrefix, prefix.getNode() );
	}
	
	public void removePrefix( DBPropPrefix prefix ) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		TripleMatch match = new StandardTripleMatch(self, graphPrefix, prefix.getNode());
		Iterator matches = graph.find( match, complete);
		if( matches.hasNext() )
			graph.delete( (Triple)(matches.next()), complete );
		prefix.remove();
	}
	
	public void addPrefix( String prefix, String uri ) {
		addPrefix( new DBPropPrefix( graph, prefix, uri) );
	}
	
	public String getName() { return self.getURI().substring(graphNamePrefix.length()); }
	public String getType() { return getPropString( graphType); };
	
	public ExtendedIterator getAllLSets() {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		TripleMatch match = new StandardTripleMatch(self, graphLSet, null);
		Iterator matches = graph.find( match, complete);
		return new Map1Iterator(new MapToLSet(), matches);
	}
	
	public ExtendedIterator getAllPrefixes() {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		TripleMatch match = new StandardTripleMatch(self, graphPrefix, null);
		Iterator matches = graph.find( match, complete);
		return new Map1Iterator(new MapToPrefix(), matches);
	}
	
	public DBPropPrefix getPrefix( String value ) {
		ExtendedIterator prefixes = getAllPrefixes();
		while( prefixes.hasNext() ) {
			DBPropPrefix prefix = (DBPropPrefix)prefixes.next();
			if( prefix.getValue().matches(value)) 
				return prefix;
		}
		return null;
	}
	
	public ExtendedIterator listTriples() {
		// First get all the triples that directly desrcribe this graph
		ExtendedIterator result = DBProp.listTriples( graph, self );
		
		// Now get all the triples that describe any lsets
		ExtendedIterator lsets = getAllLSets();
		while( lsets.hasNext()) {
			result = result.andThen( ((DBPropLSet)lsets.next()).listTriples() );
		}

		// Now get all the triples that describe any prefixes
		ExtendedIterator prefixes = getAllPrefixes();
		while( prefixes.hasNext()) {
			result = result.andThen( ((DBPropPrefix)prefixes.next()).listTriples() );
		}
		return result;
	}
	
	private class MapToLSet implements Map1 {
		public Object map1( Object o) {
			Triple t = (Triple) o;
			return new DBPropLSet( graph, t.getObject() );			
		}
	}
	
	private class MapToPrefix implements Map1 {
		public Object map1( Object o) {
			Triple t = (Triple) o;
			return new DBPropPrefix( graph, t.getObject() );			
		}
	}
	
	public static DBPropGraph findPropGraph( SpecializedGraph graph, String name ) {
		Node_URI myNode = new Node_URI(graphNamePrefix+name);
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator it =  graph.find(new StandardTripleMatch(myNode, null, null), complete);
		if( it.hasNext() )
			return new DBPropGraph( graph, myNode);
		else
			return null;
	}
	
	public void remove() {
		Iterator it = getAllPrefixes();
		while( it.hasNext()) {
			((DBPropPrefix)it.next()).remove();			
		}
		it = getAllLSets();
		while( it.hasNext()) {
			((DBPropLSet)it.next()).remove();			
		}
		super.remove();
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