/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Mar-2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.path;



// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;




/**
 * <p>
 * Represents a potential path through the triple graph, from some given node. Different
 * path operators encode different constructors for paths, such as:
 * <ul>
 *  <li>a single property</li>
 *  <li>the composition of two properties</li>
 *  <li>the closure of a transitive property</li>
 *  <li>an arbitrary unit edge</li>
 *  <li>any annotation property</li>
 * </ul>
 * </p>
 * <p>
 * When a path is evaluated against a given resource in an ontology, it corresponds to a
 * collection of statements. A {@link PathSet} represents this abstract collection.
 * </p>
 * 
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public interface PathExpr {
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Add the given value to the given root, using this path (if possible).  Not
     * all paths evaluate to a form that makes add possible; in this case an
     * exception is thrown.
     * </p>
     * 
     * @param root The resource that the path is to start from
     * @param value The value to add to the root
     * @exception PathException if this path expression cannot perform an add operation
     */
    public void add( Resource root, RDFNode value );
    
    /**
     * <p>
     * Evaluate the path expression against the given root, which will result in a set of paths
     * that can be iterated through
     * </p>
     * 
     * @param root The root resource to evaluate the path from
     * @return A path iterator
     */
    public PathIterator evaluate( Resource root );
    
    /**
     * <p>
     * For convenience in accessing the contents of a path expression, answer a path set
     * of the paths defined from the given root.  The {@link PathSet} is really just a 
     * set of wrapper functions for {@link #evaluate}. 
     * </p>
     * 
     * @return A set of the paths this path expression defines from the given root.
     */
    public PathSet asPathSet( Resource root );
}


/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
