/*
 *  (c) Copyright Hewlett-Packard Company 2001 
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
 * $Id$
 */

package jena;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.mem.ModelMem;

import java.net.URL;
import java.io.FileInputStream;

/** A program which read an RDF model and copy it to the standard output stream.
 *
 *  <p>This program will read an RDF model, in a variety of languages,
 *     and copy it to the output stream in a possibly different langauge.
 *     Input can be read either from a URL or from a file.
 *     The program writes its results to the standard output stream and sets
 *     its exit code to 0 if the program terminate normally,  and
 *     to -1 if it encounters an error.</p>
 *
 *  <p></p>
 *
 *  <pre>java jena.rdfcopy model [inlang [outlang]]
 *
 *       model1 and model2 can be file names or URL's
 *       inlang and outlang specify the language of the input and output
 *       respectively and can be:
 *           RDF/XML
 *           N-TRIPLE
 *           N3
 *       The input language defaults to RDF/XML and the output language
 *       defaults to N-TRIPLE.
 *  </pre>
 *
 * @author  bwm
 * @version $Name$ $Revision$ $Date$
 */
public class rdfcopy extends java.lang.Object {

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {

        if (args.length < 1 || args.length > 3) {
            usage();
            System.exit(-1);
        }
         
        String in = args[0];
        String inlang = "RDF/XML";
        if (args.length > 1) {
            inlang = args[1];
        } 
        String outlang = "N-TRIPLE";
        if (args.length == 3) {
            outlang = args[2];
        }
        
        try {
            Model m = new ModelMem();
        
            read(m, in, inlang);
            m.write(System.out, outlang);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Unhandled exception:");
            System.err.println("    " + e.toString());
            System.exit(-1);
        }
    }
    
    protected static void usage() {
        System.err.println("usage:");
        System.err.println(
            "    java jena.rdfcopy in [inlang [outlang]]");
        System.err.println();
        System.err.println("    in can be a URL or a filename");
        System.err.println("    inlang and outlang can take values:");
        System.err.println("      RDF/XML");
        System.err.println("      N-TRIPLE");
        System.err.println("      N3");
        System.err.println("    inlang defaults to RDF/XML, outlang to N-TRIPLE");
        System.err.println();
    }
    
    protected static void read(Model model, String in, String lang) 
      throws RDFException, java.io.FileNotFoundException {
        try {
            URL url = new URL(in);
            model.read(in, lang);
        } catch (java.net.MalformedURLException e) {
            model.read(new FileInputStream(in), "", lang);
        }
    }
}
