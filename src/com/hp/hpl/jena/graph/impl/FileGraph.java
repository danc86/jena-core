/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.util.JenaException;

import java.io.*;

/**
    A FileGraph is a memory-based graph that is optionally read in from a file
    when it is created, and is written back when it is closed. It is not 
    particularly robust, alas.
    
    TODO: consider a version which saves "every now and then"
    
 	@author hedgehog
*/
public class FileGraph extends GraphMem
    {
    File name;
    Model model;
    String lang;
    
    /**
        Construct a new FileGraph who's name is given by the specified File,
        If create is true, this is a new file, and any existing file will be destroyed;
        if create is false, this is an existing file, and its current contents will
        be loaded. The language code for the file is guessed from its suffix.
        
     	@param f the File naming the associated file-system file
     	@param create true to create a new one, false to read an existing one
     */
    public FileGraph( File f, boolean create )
        {
        this.name = f;
        this.model = new ModelCom( this );
        this.lang = guessLang( this.name.toString() );
        if (create == false) model.read( "file:" + name.toString(), this.lang );
        }
        
    /**
        As for FileGraph(File,boolean), except the name is given as a String.
     */
    public FileGraph( String s, boolean create )
        { this( new File( s ), create ); }
        
    /**
        Guess the language of the specified file by looking at the suffix.
        If it ends in .n3, assume N3; if it ends in .nt, assume N-TRIPLE;
        otherwise assume RDF/XML.
        
    	@param name the pathname of the file to guess from
    	@return "N3", "N-TRIPLE", or "RDF/XML"
     */
    public static String guessLang( String name )
        {
        String suffix = name.substring( name.lastIndexOf( '.' ) + 1 );
        if (suffix.equals( "n3" )) return "N3";
        if (suffix.equals( "nt" )) return "N-TRIPLE";
        return "RDF/XML";
        }
        
    /**
        Write out and then close this FileGraph. The graph is written out to the 
        named file in the language guessed from the suffix, and then the 
        parent close is invoked. The write-out goes to an intermediate file
        first, which is then renamed to the correct name, to try and ensure
        that the output is either done completely or not at all.

     	@see com.hp.hpl.jena.graph.Graph#close()
     */
    public void close()
        {
        try
            {
            File intermediate = new File( name.getPath() + ".new" );
            FileOutputStream out = new FileOutputStream( intermediate );
            model.write( out, lang ); 
            out.close();
            intermediate.renameTo( name );
            super.close();
            }
        catch (IOException e) 
            { throw new JenaException( e ); }
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
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