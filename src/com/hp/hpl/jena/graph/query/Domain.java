/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.graph.query;

/**
    A Domain is an answer to a Binding query. It satisfies the List
    interface so that casual users don't have to worry about its special
    features - for them, it is immutable (they only ever get to see Domains
    that have emerged from the query process).
    
    @author kers
*/

import java.util.*;

public class Domain extends AbstractList
	{
	private Object [] value;
	private int width;
	
	public Domain( Object [] value ) { this.value = value; }
	
	public int size() { return width; }
	public Object get( int i ) { return value[i]; }
	  
	public void setElement( int i, Object x ) 
		{ 
		value[i] = x; 
		if (i >= width) width = i + 1; 
		}
	
	public Domain extend()
		{
		Object [] result = new Object[value.length];
		for (int i = 0; i < value.length; i += 1) result[i] = value[i];
		Domain out = new Domain( result );
		out.width = this.width;
		return out;
		}
        
    public boolean equals( Object x )
        {
        return x instanceof Domain && Arrays.equals( this.value, ((Domain) x).value );
        }
		
	public String toString()
		{
		StringBuffer b = new StringBuffer( 200 );
        b.append( "<domain" );
        for (int i = 0; i < value.length; i += 1) b.append( " " + i + ":" + value[i] );
        b.append( ">" );
        return b.toString();
		}
	}

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
