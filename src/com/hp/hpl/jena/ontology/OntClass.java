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


/**
 * <p>
 * Interface that encapsulates a class axiom representing a named class.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public interface OntClass
    extends ClassDescription
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    // Boolean class expressions
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>intersectionOf</code>
     * property of a class or class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the intersection class description
     */
    public PathSet p_intersectionOf();


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
    public PathSet p_unionOf();


    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>complementOf</code>
     * property of a class or class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the complement class description
     */
    public PathSet p_complementOf();


    // Enumerated class constructor
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>oneOf</code>
     * property of an enumerated class. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the enumerated class constructor
     */
    public PathSet p_oneOf();
    
         
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

