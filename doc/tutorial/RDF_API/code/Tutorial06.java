/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.tutorial.rdf;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.io.*;

/** Tutorial navigating a model
 *
 * @author  bwm - updated by kers/Daniel
 * @version Release='$Name$' Revision='$Revision$' Date='$Date$'
 */
public class Tutorial06 extends Object {
    
    static final String inputFileName = "vc-db-1.rdf";
    static final String johnSmithURI = "http://somewhere/JohnSmith/";
    
    public static void main (String args[]) {
       try {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();
           
            // use the class loader to find the input file
            InputStream in = Tutorial06.class
                                       .getClassLoader()
                                       .getResourceAsStream(inputFileName);
            if (in == null) {
                throw new IllegalArgumentException(
                                       "File: " + inputFileName + " not found");
            }
            
            // read the RDF/XML file
            model.read(new InputStreamReader(in), "");
            
            // retrieve the Adam Smith vcard resource from the model
            Resource vcard = model.getResource(johnSmithURI);

            // retrieve the value of the N property
            Resource name = (Resource) vcard.getProperty(VCARD.N)
                                            .getObject();
            // retrieve the given name property
            String fullName = vcard.getProperty(VCARD.FN)
                                   .getString();
            // add two nick name properties to vcard
            vcard.addProperty(VCARD.NICKNAME, "Smithy")
                 .addProperty(VCARD.NICKNAME, "Adman");
            
            // set up the output
            System.out.println("The nicknames of \"" + fullName + "\" are:");
            // list the nicknames
            StmtIterator iter = vcard.listProperties(VCARD.NICKNAME);
            while (iter.hasNext()) {
                System.out.println("    " + iter.nextStatement().getObject()
                                                .toString());
            }
        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2003
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
 *
 * Created on 27 January 2001
 */