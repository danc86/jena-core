/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.xmloutput.test;


import java.util.*;

import java.io.*;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.xmloutput.impl.BaseXMLWriter;

/**
 * @author Jeremy J. Carroll
 *
 */
public class TestMacEncodings  extends ModelTestBase
{
	private static Log logger = LogFactory.getLog( TestMacEncodings.class );

	public TestMacEncodings( String name )
		{ super( name ); }
			
	public static TestSuite suite()
    	{ return new TestSuite( TestMacEncodings.class ); }
    static private boolean InUse = false;
    public void test00InitMacTests() {
        try {
            OutputStream out = new ByteArrayOutputStream();
            
            Writer wrtr = new OutputStreamWriter(out,"MacRoman");
            InUse = true;
        } catch (Exception e){
            InUse = false;
        }
     if (!InUse){
         logger.warn("MacRoman not supported on this Java installation: mac encoding tests suppressed.");
         
     }
        
    }

    public void testMacRoman() throws IOException {
        if (!InUse) return;
        File f = File.createTempFile("jena", ".rdf");
    	String fileName = f.getAbsolutePath();
    	Model m = createMemModel();
    	m.read(
    		new FileInputStream("testing/wg/rdfms-syntax-incomplete/test001.rdf"),
    		"");
    	FileOutputStream fos = new FileOutputStream(fileName);
    	Writer w = new OutputStreamWriter(fos,"MacRoman");
    	m.write(w, "RDF/XML");
    	fos.close();
    	Model m1 = createMemModel();
    	m1.read(new FileInputStream(fileName), "");
    	assertTrue("Use of FileWriter", m.isIsomorphicWith(m1));
    	f.delete();
    }	


    public void testMacArabic() throws IOException {
        if (!InUse) return;
    	File f = File.createTempFile("jena", ".rdf");
    	String fileName = f.getAbsolutePath();
    	Model m = createMemModel();
    	m.read(
    		new FileInputStream("testing/wg/rdfms-syntax-incomplete/test001.rdf"),
    		"");
    	FileOutputStream fos = new FileOutputStream(fileName);
    	Writer w = new OutputStreamWriter(fos,"MacArabic");
    	m.write(w, "RDF/XML");
    	fos.close();
    	Model m1 = createMemModel();
    	m1.read(new FileInputStream(fileName), "");
    	assertTrue("Use of FileWriter", m.isIsomorphicWith(m1));
    	f.delete();
    }	
    
    
    	

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 */
 