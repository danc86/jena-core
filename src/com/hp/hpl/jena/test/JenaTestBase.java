/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.test;

import junit.framework.*;

/**
    A basis for Jena test cases which provides assertFalse and assertDiffer.
    Often the logic of the names is clearer than using a negation (well, Chris
    thinks so anyway).
    
 	@author kers
*/
public class JenaTestBase extends TestCase
    {
    public JenaTestBase( String name )
        { super( name ); }
        
    /**
        assert that the specified boolean must be false.
        @param title a labelling string for the assertion failure text
        @param b the boolean to test
    */
    public void assertFalse( String title, boolean b )
        { assertTrue( title, !b ); }
        
    /**
        Assert that the specified boolean must be false.
        @param b the boolean to test
    */
    public void assertFalse( boolean b )
        { assertTrue( !b ); }
        
    /**
        assert that the two objects must be unequal according to .equals().
        @param title a labelling string for the assertion failure text
        @param x an object to test; the subject of a .equals()
        @param y the other object; the argument of the .equals()
    */
    public void assertDiffer( String title, Object x, Object y )
        { assertFalse( title, x.equals( y ) ); }
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