/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            3 August 2001
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
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DAML_OIL_2000_12;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


import com.hp.hpl.jena.util.Log;

import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.rdf.model.impl.Util;




/**
 * Contains knowledge of different versions of the DAML vocabulary, to help
 * with managing the different versions of the namespace.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id$
 */
public class VocabularyManager
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
     * Answer the vocabulary that corresponds to the namespace of the
     * given resource. By default, answer the most recent vocabulary.
     *
     * @param resource The RDF resource denoting a namespace
     * @return a DAML vocabulary object for the namespace
     */
    public static DAMLVocabulary getVocabulary( Resource resource ) {
        return getVocabulary( resource.getURI() );
    }


    /**
     * Answer the vocabulary that corresponds to the namespace of the
     * given URI. By default, answer the most recent vocabulary.
     *
     * @param uri A URI denoting a namespace
     * @return a DAML vocabulary object for the namespace
     */
    public static DAMLVocabulary getVocabulary( String uri ) {
        if (uri != null) {
            // pull out the namespace of the uri
            int splitPoint = Util.splitNamespace( uri );
            String namespace = (splitPoint < 0) ? uri : uri.substring( 0, splitPoint );

            // test the known namespaces
            if (namespace != null) {
                if (namespace.equals( DAML_OIL.NAMESPACE_DAML_2000_12_URI )) {
                    return DAML_OIL_2000_12.getInstance();
                }

                // add further namespace tests here as the namespaces are defined.
            }
        }

        // to get here, we assume the default vocabulary
        return getDefaultVocabulary();
    }


    /**
     * Answer the default (latest) vocabulary.
     *
     * @return a DAML+OIL vocabulary
     */
    public static DAMLVocabulary getDefaultVocabulary() {
        return DAML_OIL.getInstance();
    }

    // Internal implementation methods
    //////////////////////////////////




    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
