/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;

import java.util.Properties;

/**
 *
 * @author  bwm
 * @version $Revision$ $Date$
 */
public class RDFReaderFImpl extends Object implements RDFReaderF {

    protected static Properties langToClassName = null;

    // predefined languages - these should probably go in a properties file

    protected static final String LANGS[] = { "RDF/XML",
                                              "RDF/XML-ABBREV",
                                              "N-TRIPLE",
                                              "N3"};
    // default readers for each language

    protected static final String DEFAULTREADERS[] = {
        "com.hp.hpl.jena.rdf.arp.JenaReader",
        "com.hp.hpl.jena.rdf.arp.JenaReader",
        Jena.PATH + ".rdf.model.impl.NTripleReader",
        "com.hp.hpl.jena.n3.N3JenaReader"
    };

    protected static final String DEFAULTLANG = LANGS[0];

    protected static final String PROPNAMEBASE = Jena.PATH + ".reader.";

    static { // static initializer - set default readers
        langToClassName = new Properties();
        for (int i = 0; i<LANGS.length; i++) {
            langToClassName.setProperty(
                               LANGS[i],
                               Util.getProperty(PROPNAMEBASE + LANGS[i],
                                                  DEFAULTREADERS[i]));
        }
    }


    /** Creates new RDFReaderFImpl */
    public  RDFReaderFImpl() {
    }

    public RDFReader getReader() throws RDFException {
        return getReader(DEFAULTLANG);
    }

    public RDFReader getReader(String lang) throws RDFException {

        // setup default language
        if (lang==null || lang.equals("")) {
            lang = LANGS[0];
        }

        String className = langToClassName.getProperty(lang);
        if (className == null || className.equals("")) {
            throw new RDFException(RDFException.NOREADERFORLANG);
        }
        try {
          return (RDFReader) Class.forName(className)
                                  .newInstance();
        } catch (Exception e) {
            throw new RDFException(e);
        }
    }

    public String setReaderClassName(String lang,String className) {
        String oldClassName = langToClassName.getProperty(lang);
        langToClassName.setProperty(lang, className);
        return oldClassName;
    }
}
