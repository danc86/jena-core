/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.enhanced;

import com.hp.hpl.jena.graph.*;
import org.apache.log4j.*;


/**
 * <p>
 * A specialisation of Polymorphic that models an extended node in a an extended graph. An extended node
 * wraps a normal node, and adds additional convenience access or user affordances, though the state
 * remains in the graph itself.
 * </p>
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a> (original code)<br>
 *         <a href="mailto:Chris.Dollin@hp.com">Chris Dollin</a> (original code)<br>
 *         <a href="mailto:Ian.Dickinson@hp.com">Ian Dickinson</a> (refactoring and commentage)
*/
public class EnhNode 
    extends Polymorphic 
{
    // Static variables
    /** For writing to the log file in the context of this class */
    private static Logger logger = Logger.getLogger( EnhNode.class );
    
    /** The graph node that this enhanced node is wrapping */
    final protected Node node;
    
    /** The enhnanced graph containing this node */
    final protected EnhGraph enhGraph;
    
    // Constructors
    
    public EnhNode(Node n,EnhGraph g,Type myTypes[]) {
        super(myTypes);
        node=n;
        enhGraph = g;
   }


    // External interface
    
    /** 
     * Answer the graph node that this enhanced node wraps
     * @return A plain node
     */
    public Node asNode() {
        return node;
    }
    
    /**
     * Answer the graph containing this node
     * @return An enhanced graph 
     */
    public EnhGraph getGraph() {
        return enhGraph;
    }
    

    /**
     * Answer a facet of this node, where that facet is denoted by the
     * given type.
     * 
     * @param t A type denoting the desired facet of the underlying node
     * @return An enhanced nodet that corresponds to t; this may be <i>this</i>
     *         Java object, or a different object.
     */ 
    public EnhNode as( Type t ) {
        return (EnhNode) asInternal( t ); 
    }
    
    
    /**
     * The hash code of an enhanced node is defined to be the same as the underlying node.
     * @return The hashcode as an int
     */
    final public int hashCode() {
     	return node.hashCode();
    }
     
    
    /**
     * An enhanced node is equal to another node n iff:
     * <ul>
     * <li>n is identical to <i>this</i></li>
     * <li>n has a facets map that is identical to the facets map on <i>this</i></li>
     * <li>the underlying nodes are equal</li>
     * </ul>
     * This is deemed to be a complete and correct interpretation of enhanced node
     * equality, which is why this method has been marked final.
     * 
     * @param o An object to test for equality with this node
     * @return True if o is equal to this node.
     */
    final public boolean equals(Object o) {
     	if (o instanceof EnhNode) {
     		return super.equals(o) ||
     		       node.equals(((EnhNode) o).asNode());
     	}
        else {
            return false;
        }
    }


    // Internal implementation

    /** 
     * Answer an enhanced node object that presents <i>this</i> in a way which satisfies type
     * t.
     * @param t A type
     * @return A polymorphic instance, possibly but not necessarily this, that conforms to t.
     */
    protected Polymorphic asInternal(Type t) {
        if (this.already(t))
            return this;
        else {
            // first look to see if there is already a realization of t we know about
            Polymorphic result = getFacet( t );
            if (result != null)
                return result;
                
            // otherwise, we generate a new one from the factory
            result = getPersonality().getImplementation(t).wrap( asNode(), getGraph() );
            
            // and make it share the facets map (so that facets can be re-used)
            result.setFacets( getFacets() );
            
            // additional test - @todo I'm not sure if this is really necessary ... perhaps move to unit tests? -ijd
            if (getFacet(t) != result) {
                logger.error( "Internal error: personality misconfigured - constructor did not setTypes()?." );
            }
            
            return result;
        }
    }


    /**
     * Answer the personality object bound to this enhanced node, which we obtain from 
     * the associated enhanced graph.
     * 
     * @return The personality object
     */
    protected Personality getPersonality() {
        return ((GraphPersonality) getGraph().getPersonality()).nodePersonality();
    }
    
    

}

/*
    (c) Copyright Hewlett-Packard Company 2002
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
