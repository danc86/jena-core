/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            16 Jan 2001
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     Preview-release $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright Hewlett-Packard Company 2001
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
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.common;


// Imports
///////////////
import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLDisjoint;

import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.DAML_OIL_2000_12;


/**
 * Java representation of a DAML Disjoint instance.  Note that Disjoint may be removed
 * from the DAML spec, so this class may become obsolete quite quickly.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id$
 */
public class DAMLDisjointImpl
    extends DAMLCommonImpl
    implements DAMLDisjoint
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    /**
     * Constructor, takes the URI for this disjoint, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the disjoint
     * @param store Reference to the DAML store that will contain statements about this DAML disjoint.
     * @param vocabulary Reference to the DAML vocabulary used by this disjoint.
     */
    public DAMLDisjointImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary );
    }



    /**
     * Constructor, takes the name and namespace for this disjoint, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the disjoint inhabits, or null
     * @param name The name of the disjoint
     * @param store Reference to the DAML store that will contain statements about this DAML disjoint.
     * @param vocabulary Reference to the DAML vocabulary used by this disjoint.
     */
    public DAMLDisjointImpl( String namespace, String name, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( namespace, name, store, vocabulary );
    }




    // External signature methods
    //////////////////////////////////


    /**
     * Answer a key that can be used to index collections of this DAML disjoint for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    Object getKey() {
        return DAML_OIL_2000_12.Disjoint.getURI();
    }


    // Internal implementation methods
    //////////////////////////////////




    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
