/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.ontology.path.PathSet;
import com.hp.hpl.jena.rdf.model.Resource;

import java.util.Iterator;



/**
 * <p>
 * Interface providing an encapsulation for general class descriptions, and
 * which provides a super-type for all ontology class expressions.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public interface ClassDescription
    extends OntResource
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>subClassOf</code>
     * property of a class or class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the sub-class class axiom
     */
    public PathSet p_subClassOf();
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>equivalentClass</code>
     * property of a class or class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the class equivalance class axiom
     */
    public PathSet p_equivalentClass();
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>disjointWith</code>
     * property of a class or class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the disjoint-with class axiom
     */
    public PathSet p_disjointWith();


    /**
     * <p>
     * Answer an iterator over the class descriptions
     * that mention this class as one of its super-classes.  Will iterate over the
     * closure of the sub-class relationship.
     * </p>
     * 
     * @return an iterator over the resources representing this class's sub-classes.
     */
    public Iterator getSubClasses();


    /**
     * <p>
     * Answer an iterator over the class descriptions
     * that mention this class as one of its super-classes.
     * </p>
     * <p>
     * TODO: the closed parameter is ignored at the current time
     * </p>
     * 
     * @param closed If true, close the iteration over the sub-class relation: i&#046;e&#046;
     *               return the sub-classes of the sub-classes, etc.
     * @return an iterator over the resources representing this class's sub-classes
     */
    public Iterator getSubClasses( boolean closed );


    /**
     * <p>
     * Answer an iterator over the class descriptions
     * for which this class is a sub-class. Will generate the
     * closure of the iteration over the super-class relationship.
     * <p>
     * 
     * @return an iterator over the resources representing this class's super-classes.
     */
    public Iterator getSuperClasses();


    /**
     * <p>
     * Answer an iterator over the class descriptions
     * for which this class is a sub-class. 
     * </p>
     * <p>
     * TODO: the closed parameter is ignored at the current time
     * </p>
     * 
     * @param closed If true, close the iteration over the super-class relation: i&#046;e&#046;
     *               return the super-classes of the super-classes, etc.
     * @return an iterator over the resources representing this class's sub-classes.
     */
    public Iterator getSuperClasses( boolean closed );


    /**
     * <p>
     * Answer true if the given class is a sub-class of this class.
     * </p>
     * 
     * @param cls A resource representing a class
     * @return True if this class is a super-class of the given class <code>cls</code>.
     */
    public boolean hasSubClass( Resource cls );


    /**
     * <p>
     * Answer true if the given class is a super-class of this class.
     * </p>
     * 
     * @param cls A resource representing a class
     * @return True if this class is a sub-class of the given class <code>cls</code>.
     */
    public boolean hasSuperClass( Resource cls );
    
    

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

