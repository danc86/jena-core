/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.graph.query;

import java.util.*;

/**
	ExpressionSet: represent a set of (boolean) expressions ANDed together.

	@author kers
*/
public class ExpressionSet 
    {
    private Set expressions = new HashSet();
    /**
        Initialise an expression set with no members.
    */
	public ExpressionSet() 
        {}
    
    /**
        Answer this expressionset after e has been anded into it.
     	@param e the expression to and into the set
     	@return this ExpressionSet
    */
    public ExpressionSet add( Expression e )
        {
        expressions.add( e );
        return this;    
        }
    
    public ExpressionSet add( Valuator e )
        {
        expressions.add( e );
        return this;    
        }
    
    public boolean isComplex()
        { return !expressions.isEmpty(); }
    
    /**
        Evaluate this expression set, delivering true iff no member of the set evaluates
        to false.
        
     	@param vv the mapping from variables to values
     	@return true iff no member evaluates to false
     */    
    public boolean evalBool( VariableValues vv )
        { 
        Iterator it = expressions.iterator();
        while (it.hasNext()) 
            if (((Expression) it.next()).evalBool( vv ) == false) return false;
        return true;
        }
        
    public boolean evalBool( IndexValues vv )
        { 
        Iterator it = expressions.iterator();
        while (it.hasNext()) 
            if (((Valuator) it.next()).evalBool( vv ) == false) return false;
        return true;
        }
                
    public ExpressionSet prepare( VariableIndexes vi )
        {
        ExpressionSet result = new ExpressionSet();
        Iterator it = expressions.iterator();
        while (it.hasNext()) result.add( ((Expression) it.next()).prepare( vi ) );
        return result;    
        }
    
    public Iterator iterator()
        { return expressions.iterator(); }
    
    public String toString()
        { return expressions.toString(); }
    }

/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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