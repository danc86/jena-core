/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            28-Apr-2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.PathSet;
import com.hp.hpl.jena.rdf.model.Property;


/**
 * <p>
 * Implementation of a node representing a union class description.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public class UnionClassImpl 
    extends OntClassImpl
    implements UnionClass
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating UnionClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new UnionClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to UnionClass");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an UnionClass facet if it has rdf:type owl:Class and an owl:unionOf statement (or equivalents) 
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            Property union = (profile == null) ? null : profile.UNION_OF();
            
            return (profile != null)  &&  
                   profile.isSupported( node, eg, OntClass.class )  &&
                   union != null && 
                   eg.asGraph().contains( node, union.getNode(), null );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a union class node represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public UnionClassImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>unionOf</code>
     * property of a class or class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the union class description
     */
    public PathSet p_unionOf() {
        return asPathSet( getProfile().UNION_OF(), "UNION_OF" );
    }



    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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


